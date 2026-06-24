package acquire.base.activity;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.AnimRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.util.Locale;

import acquire.base.R;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.constants.BasePrefKey;
import acquire.base.lifecycle.LogLife;
import acquire.base.utils.InputUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;


/**
 * A basic class extends {@link Fragment}.When it's open by {@link BaseActivity#mSupportDelegate},it can implement follow feature:
 * <ul>
 *      <li>listen fragment show/hide by {@link #onFragmentShow()}/{@link #onFragmentHide()}</li>
 *      <li>intercept system back button event by {@link #onBack()}</li>
 *      <li>set fragment entering and exiting animation by {@link #getPopAnimation()}</li>
 *      <li>set fragment callback by {@link #getCallback()}.It will be invoked when system back button event occurs.</li>
 * </ul>
 *
 * @author Janson
 * @date 2018/9/11 14:43
 */
public abstract class BaseFragment extends Fragment {
    /**
     * Own class name
     */
    private final String className = String.format(Locale.getDefault(), "%s{%02x}", getClass().getSimpleName(), System.identityHashCode(this));
    /**
     * {@link BaseFragment} shown status
     */
    private boolean mShowing;
    /**
     * Attach {@link BaseActivity}
     */
    protected BaseActivity mActivity;

    protected SupportDelegate mSupportDelegate;

    private LogLife lifecycleObserver;

    /**
     * Fragment callback
     */
    @SuppressWarnings("rawtypes")
    public abstract FragmentCallback getCallback();

    /**
     * Back event
     *
     * @return If true,event be intrrupted.Else passed to next
     */
    public boolean onBack() {
        return false;
    }


    /**
     * Get {@link BaseFragment} animation
     *
     * @return fragment {@link android.R.anim}。If null, no animation
     * <p>First，enter animatino
     * <P>Second，exit animation
     */
    public @AnimRes
    int[] getPopAnimation() {
        return new int[]{R.anim.slide_left_in, R.anim.slide_right_out};
    }

    /**
     * Fragment show
     */
    public void onFragmentShow() {
        LoggerUtils.v(className + "->onFragmentShow");
        mShowing = true;
    }

    /**
     * Fragment hide
     */
    public void onFragmentHide() {
        LoggerUtils.v(className + "->onFragmentHide");
        //hide system keyboard
        InputUtils.hideKeyboard(mActivity);
        mShowing = false;
    }

    /**
     * Get the fragment display status
     *
     * @return true If fragment is shown.
     */
    public boolean isShowing() {
        return mShowing;
    }

    /**
     * Attach {@link Activity}
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        LoggerUtils.v(className + "->onAttach");
        //print life log
        if (lifecycleObserver == null) {
            lifecycleObserver = new LogLife();
            getLifecycle().addObserver(lifecycleObserver);
        }
        if (!(requireActivity() instanceof BaseActivity)) {
            throw new RuntimeException("activity must extends BaseActivity!");
        }
        mActivity = (BaseActivity) requireActivity();
        mSupportDelegate = mActivity.mSupportDelegate;
        //set back event callback
        mActivity.getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (onBack()) {
                    //back event is interrupted,do nothing.
                    return;
                }
                if (getCallback() != null) {
                    //back event call back
                    getCallback().onFail(FragmentCallback.CANCEL, getString(R.string.base_fragment_callback_cancel));
                    return;
                }
                FragmentManager fragmentManager = getParentFragmentManager();
                if (fragmentManager != mActivity.getSupportFragmentManager()) {
                    setEnabled(false);
                    mActivity.getOnBackPressedDispatcher().onBackPressed();
                    return;
                }
                if (fragmentManager.getBackStackEntryCount() == 0) {
                    setEnabled(false);
                    mActivity.getOnBackPressedDispatcher().onBackPressed();
                } else {
                    mSupportDelegate.popBackFragment(1);
                }
            }
        });

    }

    public void logoClickCount() {
        int logoClickCount = ParamsUtils.getInt("MainLogoClickCount", 0);
        ++logoClickCount;
        ParamsUtils.setInt("MainLogoClickCount", logoClickCount);
    }

    public void logoLongClick() {
        ParamsUtils.setInt("MainLogoClickCount", 0);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        LoggerUtils.v(className + "->onViewCreated");
        super.onViewCreated(view, savedInstanceState);
        // Prevent click penetration after fragment superposition
        view.setOnTouchListener((v, event) -> true);
    }


    @Override
    public void onStop() {
        //hide system keyboard
        InputUtils.hideKeyboard(mActivity);
        super.onStop();
    }

    public long getAmount(String amountText){
        //delete non numeric characters
        String strAmount = amountText.replaceAll("[^\\d]", "");
        return Long.parseLong(strAmount);
    }

}
