package acquire.app.fragment.splash;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import com.zztl.pos.ucb.R;
import com.zztl.pos.ucb.databinding.AppFragmentSplashBinding;
import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.AppUtils;
import kotlinx.coroutines.Delay;

/**
 * A {@link Fragment} with the splash animation
 *
 * @author Yjy
 * @date 2021/1/27 9:10
 */
public class SplashFragment extends BaseFragment {
    private SimpleCallback mCallback;
    private static final int SPLASH_DELAY = 5000; // 2 seconds

    public static SplashFragment newInstance( SimpleCallback callback) {
        SplashFragment fragment = new SplashFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        AppFragmentSplashBinding binding = AppFragmentSplashBinding.inflate(inflater, container, false);
        //listen to animation over
        /*binding.animLogo.setListener(() -> {
            if (!isDetached() && mCallback != null){
                mCallback.onSuccess(null);
            }
        });*/

        binding.tvVersion.setText(String.format(Locale.getDefault(),"%s %s",getString(R.string.app_version),AppUtils.getAppVersionName(mActivity)));

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            mCallback.onSuccess(null);
        }, SPLASH_DELAY);


        return binding.getRoot();
    }

    @Override
    public SimpleCallback getCallback() {
        return mCallback;
    }

    @Override
    public boolean onBack() {
        return true;
    }


}
