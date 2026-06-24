package acquire.core.fragment.scan;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.TorchState;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.ScheduledFuture;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentScanBinding;
import acquire.core.tools.SoundPlayer;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * A {@link Fragment} to scan.
 *
 * @author Janson
 * @date 2022/8/4 11:32
 */
public class ScanFragment extends BaseFragment {
    private FragmentCallback<String> callback;
    private boolean isFinish;
    private ScanViewModel scanViewModel;
    private ScheduledFuture<?> future;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private CoreFragmentScanBinding binding;
    public final static int MANUAL_ENTRY = -4;

    /**
     * create a {@link ScanFragment}
     *
     * @param lensFacing {@link CameraSelector#LENS_FACING_FRONT} or {@link CameraSelector#LENS_FACING_BACK}
     * @param callback   scan result
     */
    public static ScanFragment newInstance(int lensFacing, FragmentCallback<String> callback) {
        ScanFragment fragment = new ScanFragment();
        fragment.callback = callback;
        fragment.lensFacing = lensFacing;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentScanBinding.inflate(inflater, container, false);
        DisplayUtils.fitsWindowStatus(binding.ivBack);
        //immersed NavigationBar
        DisplayUtils.immersedStatusAndNavigationBar(mActivity.getWindow());

        scanViewModel = new ViewModelProvider(this).get(ScanViewModel.class);
        //scan result
        scanViewModel.getQrCode().observe(getViewLifecycleOwner(), code -> {
            if (isFinish) {
                return;
            }
            SoundPlayer.getInstance().playScan();
            stopScan();
            if (callback != null) {
                callback.onSuccess(code);
            }
        });
        //scan error
        scanViewModel.getScanError().observe(getViewLifecycleOwner(), error -> {
            if (callback != null) {
                callback.onFail(FragmentCallback.FAIL, error);
            }
        });
        //camera changed
        scanViewModel.getCameraData().observe(getViewLifecycleOwner(), cameraData -> {
            lensFacing = cameraData.getLensFacing();
            if (cameraData.hasBackCamera() && cameraData.hasFrontCamera()) {
                binding.ivSwitchCamera.setVisibility(View.VISIBLE);
            } else {
                binding.ivSwitchCamera.setVisibility(View.GONE);
            }
            binding.cbFlash.setVisibility(cameraData.isHasFlahLight() ? View.VISIBLE : View.GONE);
            cameraData.getTorchState().observe(getViewLifecycleOwner(), torch -> {
                boolean open = TorchState.ON == torch;
                if (binding.cbFlash.isChecked() != open) {
                    binding.cbFlash.setChecked(open);
                }
            });
        });
        //flash light
        binding.cbFlash.setOnCheckedChangeListener((buttonView, isChecked) -> scanViewModel.operaFlashLight(isChecked));
        //camera switch
        binding.ivSwitchCamera.setOnClickListener(v -> {
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                scan(CameraSelector.LENS_FACING_BACK);
            } else {
                scan(CameraSelector.LENS_FACING_FRONT);
            }
        });
        //manual button
        binding.ivManual.setVisibility(View.VISIBLE);
        binding.ivManual.setOnClickListener(v ->{
            stopScan();
            if (callback != null) {
                callback.onFail(MANUAL_ENTRY,getString(R.string.core_scan_manual_entry));
            }
        });
        //start to scan
        binding.previewView.post(() -> scan(lensFacing));
        //back button
        binding.ivBack.setOnClickListener(v->{
            stopScan();
            if (callback != null) {
                callback.onFail(FragmentCallback.CANCEL,getString(R.string.core_scan_cancel));
            }
        });
        //timeout 60s
        future = ThreadPool.executeDelay(() -> {
            future = null;
            stopScan();
            if (callback != null) {
                callback.onFail(FragmentCallback.TIMEOUT, getString(R.string.core_scan_timeout));
            }
        }, 60 * 1000);
        showAnimation();
        return binding.getRoot();
    }

    private void showAnimation(){
        Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scan_line);
        binding.scanLine.startAnimation(animation);
    }
    @Override
    public void onFragmentHide() {
        //recover immersed bar
        DisplayUtils.immersedStatusBar(mActivity.getWindow());
        binding.previewView.setVisibility(View.GONE);
        super.onFragmentHide();
    }

    @Override
    public FragmentCallback<String> getCallback() {
        return callback;
    }

    @Override
    public void onDestroy() {
        stopScan();
        super.onDestroy();
    }

    /**
     * start scanner
     */
    private void scan(int lensFacing) {
        if (Model.N700.equals(BDevice.getDeviceModel())) {
            //N700 camera problem
            binding.previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
            if (lensFacing == CameraSelector.LENS_FACING_FRONT) {
                scanViewModel.setRotation(Surface.ROTATION_90);
            } else if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                scanViewModel.setRotation(Surface.ROTATION_0);
            }
        }
        scanViewModel.scan(lensFacing, getViewLifecycleOwner(), binding.previewView.getSurfaceProvider());
    }

    /**
     * stop scanner
     */
    private void stopScan() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        isFinish = true;
        scanViewModel.release();
    }
}
