package acquire.core.fragment.scan;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraInfo;
import androidx.camera.core.CameraInfoUnavailableException;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.UseCase;
import androidx.camera.core.impl.ImageOutputConfig;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.common.util.concurrent.ListenableFuture;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.CommonPoolExecutor;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.sdk.scan.BScanner;

/**
 * A scanner view model.
 *
 * @author Janson
 * @date 2022/3/16 9:51
 */
public class ScanViewModel extends ViewModel {
    private final static Executor ANALYSIS_EXECUTOR = CommonPoolExecutor.newSinglePool("CameraX-Analysis");
    private final MutableLiveData<String> scanError = new MutableLiveData<>();
    private final MutableLiveData<String> qrCode = new MutableLiveData<>();
    private final MutableLiveData<CameraData> cameraData = new MutableLiveData<>();
    private final BScanner scanner = new BScanner();
    private boolean isRelease;
    private @ImageOutputConfig.RotationValue Integer rotation;
    private Camera mCamera;
    private ProcessCameraProvider mCameraProvider;
    private static Boolean HAS_BACK_CAMERA;
    private static Boolean HAS_FRONT_CAMERA;

    public LiveData<String> getScanError() {
        return scanError;
    }

    public LiveData<String> getQrCode() {
        return qrCode;
    }

    public LiveData<CameraData> getCameraData() {
        return cameraData;
    }

    /**
     * open/close flash light
     */
    public void operaFlashLight(boolean open) {
        if (mCamera == null) {
            return;
        }
        //on/off
        mCamera.getCameraControl().enableTorch(open);
    }

    public void setRotation(@ImageOutputConfig.RotationValue int rotation) {
        this.rotation = rotation;
    }

    /**
     * start to scan
     *
     * @param lensFacing      camera. {@link CameraSelector#LENS_FACING_FRONT} or {@link CameraSelector#LENS_FACING_BACK}.
     * @param lifecycleOwner  {@link android.app.Activity} or {@link androidx.fragment.app.Fragment} life.
     * @param surfaceProvider {@link Preview} surface provider.
     */
    public void scan(@CameraSelector.LensFacing int lensFacing, @NonNull LifecycleOwner lifecycleOwner, @Nullable Preview.SurfaceProvider surfaceProvider) {
        isRelease = false;
        //set scan result callback
        scanner.observeScanner(new BScanner.ScannerListener() {
            @Override
            public void onDecoded(String result) {
                LoggerUtils.d("Scan qr code:" + result);
                qrCode.postValue(result);
            }

            @Override
            public void onError(String msg) {
                scanError.postValue(msg);
            }
        });
        Context context = BaseApplication.getAppContext();
        if (mCameraProvider == null) {
            ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(context);
            cameraProviderFuture.addListener(() -> {
                try {
                    mCameraProvider = cameraProviderFuture.get();
                    //check camera
                    if (HAS_BACK_CAMERA == null) {
                        HAS_BACK_CAMERA = mCameraProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA);
                    }
                    if (HAS_FRONT_CAMERA == null) {
                        HAS_FRONT_CAMERA = mCameraProvider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA);
                    }
                    if (!HAS_BACK_CAMERA
                            && !HAS_FRONT_CAMERA) {
                        scanError.postValue(context.getString(R.string.core_scan_no_camera));
                        return;
                    }
                } catch (ExecutionException | InterruptedException |
                         CameraInfoUnavailableException e) {
                    LoggerUtils.e("Check camera failed!",e);
                    mCameraProvider = null;
                    scanError.postValue(context.getString(R.string.core_scan_camera_configure_error));
                    return;
                }
                //bind use case
                if (!compatBindCameraUseCases(lensFacing, lifecycleOwner, surfaceProvider)) {
                    scanError.postValue(context.getString(R.string.core_scan_camera_configure_error));
                }
            }, ContextCompat.getMainExecutor(context));
        } else {
            //bind use case
            if (!compatBindCameraUseCases(lensFacing, lifecycleOwner, surfaceProvider)) {
                scanError.postValue(context.getString(R.string.core_scan_camera_configure_error));
            }
        }
    }

    /**
     * bind camera use case in compatibility mode. When one camera is not available, use the other camera.
     */
    private boolean compatBindCameraUseCases(@CameraSelector.LensFacing int lensFacing, @NonNull LifecycleOwner lifecycleOwner, @Nullable Preview.SurfaceProvider surfaceProvider) {
        boolean result = bindCameraUseCases(lensFacing, lifecycleOwner, surfaceProvider);
        if (!result) {
            if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                result = bindCameraUseCases(CameraSelector.LENS_FACING_FRONT, lifecycleOwner, surfaceProvider);
            } else {
                result = bindCameraUseCases(CameraSelector.LENS_FACING_BACK, lifecycleOwner, surfaceProvider);
            }
        }
        return result;
    }

    /**
     * bind camera use case
     *
     * @param lensFacing camera. {@link CameraSelector#LENS_FACING_FRONT} or {@link CameraSelector#LENS_FACING_BACK}
     * @return true if success, false if the camera doesn't work.
     */
    private boolean bindCameraUseCases(@CameraSelector.LensFacing int lensFacing, @NonNull LifecycleOwner lifecycleOwner, @Nullable Preview.SurfaceProvider surfaceProvider) {
        //1. select camera
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(lensFacing).build();
        try {
            if (!mCameraProvider.hasCamera(cameraSelector)) {
                return false;
            }
        } catch (CameraInfoUnavailableException e) {
            LoggerUtils.e("check hasCamera "+cameraSelector+" failed!",e);
            return false;
        }

        //2. pre-view
        Preview preview = null;
        if (surfaceProvider != null) {
            if (rotation != null) {
                //Note: previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
                preview = new Preview.Builder()
                        .setTargetRotation(rotation)
                        .build();
            } else {
                preview = new Preview.Builder()
                        .build();
            }
        }
        //3. analysis image
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        imageAnalysis.setAnalyzer(ANALYSIS_EXECUTOR, new ImageAnalysis.Analyzer() {
            private boolean first = true;

            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                if (isRelease) {
                    imageProxy.close();
                    return;
                }
                int height = imageProxy.getHeight();
                int width = imageProxy.getWidth();
                if (first) {
                    int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();
                    LoggerUtils.d("image rotation degrees:" + rotationDegrees);
                    LoggerUtils.d("image size:" + width + "x" + height);
                    first = false;
                }
                int ylen = width * height;
                byte[] data = new byte[(int) (ylen * 1.5)];
                byte[] yuvData = new byte[ylen];
                ImageProxy.PlaneProxy planeProxy = imageProxy.getPlanes()[0];
                ByteBuffer buffer = planeProxy.getBuffer();
                buffer.get(yuvData);
                System.arraycopy(yuvData, 0, data, 0, ylen);
                scanner.startDecode(data, width, height);
                imageProxy.close();
            }
        });
        //4. binds the collection of UseCase to a LifecycleOwner.
        List<UseCase> useCases = new ArrayList<>();
        if (preview != null) {
            useCases.add(preview);
        }
        useCases.add(imageAnalysis);
        UseCase[] caseArray = useCases.toArray(new UseCase[0]);
        mCameraProvider.unbindAll();
        mCamera = mCameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, caseArray);
        if (surfaceProvider != null) {
            //surface view
            preview.setSurfaceProvider(surfaceProvider);
        }
        CameraInfo cameraInfo = mCamera.getCameraInfo();
        CameraData data = new CameraData(HAS_BACK_CAMERA, HAS_FRONT_CAMERA, cameraInfo.hasFlashUnit(), lensFacing, cameraInfo.getTorchState());
        LoggerUtils.d("camera data: " + data);
        cameraData.postValue(data);
        return true;
    }

    /**
     * release camera
     */
    public void release() {
        if (isRelease) {
            return;
        }
        LoggerUtils.d("release scanner decoder");
        isRelease = true;
        scanner.stopDecode();
        if (mCameraProvider != null) {
            ThreadPool.postOnMain(() -> mCameraProvider.unbindAll());
        }
    }

    public static class CameraData {
        private final boolean hasBackCamera;
        private final boolean hasFrontCamera;
        private final boolean hasFlahLight;
        private final @CameraSelector.LensFacing int lensFacing;
        private final LiveData<Integer> torchState;

        public CameraData(boolean hasBackCamera, boolean hasFrontCamera, boolean hasFlahLight,@CameraSelector.LensFacing int lensFacing, LiveData<Integer> torchState) {
            this.hasBackCamera = hasBackCamera;
            this.hasFrontCamera = hasFrontCamera;
            this.hasFlahLight = hasFlahLight;
            this.lensFacing = lensFacing;
            this.torchState = torchState;
        }

        public boolean hasBackCamera() {
            return hasBackCamera;
        }

        public boolean hasFrontCamera() {
            return hasFrontCamera;
        }

        public boolean isHasFlahLight() {
            return hasFlahLight;
        }

        public int getLensFacing() {
            return lensFacing;
        }

        public LiveData<Integer> getTorchState() {
            return torchState;
        }

        @NonNull
        @Override
        public String toString() {
            return "CameraData{" +
                    "hasBackCamera=" + hasBackCamera +
                    ", hasFrontCamera=" + hasFrontCamera +
                    ", hasFlahLight=" + hasFlahLight +
                    ", lensFacing=" + lensFacing +
                    ", torchState=" + torchState +
                    '}';
        }
    }

}
