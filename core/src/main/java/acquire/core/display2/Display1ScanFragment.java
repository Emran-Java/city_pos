package acquire.core.display2;

import android.animation.Animator;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.CameraSelector;
import androidx.camera.view.PreviewView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.concurrent.ScheduledFuture;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentDisplay1ScanBinding;
import acquire.core.databinding.CorePresentationScanBinding;
import acquire.core.fragment.scan.ScanViewModel;
import acquire.core.tools.SoundPlayer;


/**
 * A display1 scan {@link Fragment} for dual screen
 *
 * @author Janson
 * @date 2022/9/8 13:53
 */
public class Display1ScanFragment extends BaseDialogFragment {
    private FragmentCallback<String> mCallback;
    private int lensFacing = CameraSelector.LENS_FACING_BACK;
    private ScanPresentation presentation;

    @NonNull
    public static Display1ScanFragment newInstance(int lensFacing, FragmentCallback<String> callback) {
        Display1ScanFragment fragment = new Display1ScanFragment();
        fragment.lensFacing = lensFacing;
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentDisplay1ScanBinding fragmentBinding = CoreFragmentDisplay1ScanBinding.inflate(inflater, container, false);
        presentation = new ScanPresentation(mActivity);
        presentation.show();
        fragmentBinding.btnExit.setOnClickListener(v -> {
            presentation.stopScan();
            mCallback.onFail(FragmentCallback.CANCEL, getString(R.string.core_transaction_result_user_cancel));
        });
        return fragmentBinding.getRoot();
    }

    @Override
    public int[] getPopAnimation() {
        return null;
    }

    @Override
    public FragmentCallback<String> getCallback() {
        return mCallback;
    }

    @Override
    public boolean onBack() {
        presentation.stopScan();
        return false;
    }

    private class ScanPresentation extends BasePresentation {
        private ScanViewModel scanViewModel;
        private ScheduledFuture<?> future;
        private boolean isFinish;

        private CorePresentationScanBinding presentationBinding;

        public ScanPresentation(Context outerContext) {
            super(outerContext);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            presentationBinding = CorePresentationScanBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
            presentationBinding.previewView.setImplementationMode(PreviewView.ImplementationMode.COMPATIBLE);
            scanViewModel = new ViewModelProvider(Display1ScanFragment.this).get(ScanViewModel.class);
            //scan result
            scanViewModel.getQrCode().observe(getViewLifecycleOwner(), code -> {
                if (isFinish) {
                    return;
                }
                SoundPlayer.getInstance().playScan();
                stopScan();
                if (mCallback != null) {
                    mCallback.onSuccess(code);
                }
            });
            //scan error
            scanViewModel.getScanError().observe(getViewLifecycleOwner(), error -> {
                if (mCallback != null) {
                    mCallback.onFail(FragmentCallback.FAIL, error);
                }
            });

            //camera changed
            scanViewModel.getCameraData().observe(getViewLifecycleOwner(), cameraData -> {
                lensFacing = cameraData.getLensFacing();
            });

            //start to scan
            presentationBinding.previewView.post(() -> {
                scanViewModel.scan(lensFacing, getViewLifecycleOwner(), presentationBinding.previewView.getSurfaceProvider());
                animation();
            });
            //timeout 60s
            future = ThreadPool.executeDelay(() -> {
                future = null;
                stopScan();
                if (mCallback != null) {
                    mCallback.onFail(FragmentCallback.TIMEOUT, getString(R.string.core_scan_timeout));
                }
            }, 60 * 1000);
        }

        private void animation(){
            //scan Line
            Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scan_line);
            presentationBinding.scanLine.startAnimation(animation);
            //camera
            presentationBinding.lavCameraDown.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {}

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    presentationBinding.lavCameraRotate.setVisibility(View.VISIBLE);
                    presentationBinding.lavCameraRotate.playAnimation();
                    presentationBinding.lavCameraDown.setVisibility(View.GONE);
                    presentationBinding.lavCameraRotate.addAnimatorListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(@NonNull Animator animation) {}

                        @Override
                        public void onAnimationEnd(@NonNull Animator animation) {
                        }

                        @Override
                        public void onAnimationCancel(@NonNull Animator animation) {}

                        @Override
                        public void onAnimationRepeat(@NonNull Animator animation) {}
                    });
                }
                @Override
                public void onAnimationCancel(@NonNull Animator animation) {}
                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {}
            });
        }

        @Override
        public void dismiss() {
            presentationBinding.lavCameraRotate.setVisibility(View.GONE);
            presentationBinding.lavCameraUp.setVisibility(View.VISIBLE);
            presentationBinding.lavCameraUp.playAnimation();
            presentationBinding.lavCameraUp.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(@NonNull Animator animation) {}

                @Override
                public void onAnimationEnd(@NonNull Animator animation) {
                    ScanPresentation.super.dismiss();
                }

                @Override
                public void onAnimationCancel(@NonNull Animator animation) {}

                @Override
                public void onAnimationRepeat(@NonNull Animator animation) {}
            });

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
            mActivity.runOnUiThread(this::dismiss);
        }
    }


}
