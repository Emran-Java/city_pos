package acquire.base.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import acquire.base.lifecycle.LogLife;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.ThreadPool;


/**
 * The delegate class of {@link Activity}.
 *
 * @author Janson
 * @date 2018/10/9 16:47
 */
public class SupportDelegate {
    private final FragmentManager mFragmentManager;
    private @IdRes
    final int mFragmentViewId;

    public SupportDelegate(@NonNull FragmentManager mFragmentManager, @IdRes int fragmentViewId) {
        this.mFragmentManager = mFragmentManager;
        this.mFragmentViewId = fragmentViewId;
    }

    /**
     * Pop some {@link Fragment}
     *
     * @param n number to pop
     */
    public void popBackFragment(final int n) {
        ThreadPool.postOnMain(() -> {
            int num = n;
            try {
                int count = mFragmentManager.getBackStackEntryCount();
                if (num > count) {
                    num = count;
                }
                List<Fragment> fragments = mFragmentManager.getFragments();
                int topIndex = fragments.size() - 1;
                Fragment topFragment;
                for (int i = 0; i < num; topIndex--) {
                    if (topIndex < 0) {
                        break;
                    }
                    topFragment = fragments.get(topIndex);
                    if (topFragment != null && topFragment.getId() == mFragmentViewId) {
                        i++;
                        LoggerUtils.d("pop " + topFragment.getClass().getName());
                        hideTopFragment();
                        //set FragmentManager not saved
                        noteStateNotSaved();
                        boolean result = mFragmentManager.popBackStackImmediate();
                        if (!result) {
                            return;
                        }
                    }
                }
                if (num < count) {
                    showTopFragment();
                }
            } catch (Exception e) {
                LoggerUtils.e("popBackFragment "+n+" failed!",e);
            }
        });
    }

    /**
     * Replace {@link Fragment}
     *
     * @param fragment       {@link Fragment} to be replaced
     * @param addToBackStack true to add the fragment to back stack.
     * @param shareElement   share element bean if using share animation.
     */
    public void replace(@NonNull final Fragment fragment, final boolean addToBackStack, @Nullable final ShareElement shareElement) {
        ThreadPool.postOnMain(() -> {
            hideTopFragment();
            String name = fragment.getClass().getName();
            LoggerUtils.d("replace " + name);
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            if (mFragmentManager.getBackStackEntryCount() != 0) {
                if (fragment instanceof BaseFragment) {
                    //pop Animation
                    int[] animation = ((BaseFragment) fragment).getPopAnimation();
                    if (animation != null) {
                        int popIn = animation.length >= 1 ? animation[0] : 0;
                        int popUp = animation.length >= 2 ? animation[1] : 0;
                        fragmentTransaction.setCustomAnimations(popIn, popUp, popIn, popUp);
                    }
                }
            }
            if (shareElement != null) {
                //share animation
                //set enter transaction
                fragment.setSharedElementEnterTransition(shareElement.getTransition());
                //set share elements
                for (View shareView : shareElement.getShareViews()) {
                    String transitionName = ViewCompat.getTransitionName(shareView);
                    if (transitionName != null) {
                        fragmentTransaction.addSharedElement(shareView, transitionName);
                    }
                }
            }
            if (addToBackStack) {
                fragmentTransaction.addToBackStack(name);
            }
            fragmentTransaction.replace(mFragmentViewId, fragment)
                    .commitAllowingStateLoss();
            mFragmentManager.executePendingTransactions();
            if (fragment instanceof BaseFragment) {
                ((BaseFragment) fragment).onFragmentShow();
            }
        });
    }

    /**
     * Show and add a {@link Fragment} to back stack
     *
     * @param fragment {@link Fragment} to show
     */
    public void switchContent(@NonNull final Fragment fragment) {
        ThreadPool.postOnMain(() -> {
            try {
                hideTopFragment();
                if (mFragmentManager.isDestroyed()) {
                    LoggerUtils.e("FragmentManager is destroyed.");
                    return;
                }
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                String name = fragment.getClass().getName();
                //not use pop animation if it is first fragment in the back stack
                if (mFragmentManager.getBackStackEntryCount() != 0) {
                    if (fragment instanceof BaseFragment) {
                        //pop animation
                        int[] animation = ((BaseFragment) fragment).getPopAnimation();
                        if (animation != null) {
                            int popIn = animation.length >= 1 ? animation[0] : 0;
                            int popUp = animation.length >= 2 ? animation[1] : 0;
                            //set fragment animation into fragmentTransaction
                            fragmentTransaction.setCustomAnimations(popIn, popUp, popIn, popUp);
                        }
                    }
                }
                LoggerUtils.d("add " + name);
                fragmentTransaction.add(mFragmentViewId, fragment, name)
                        //add back stack
                        .addToBackStack(name)
                        .commitAllowingStateLoss();
                mFragmentManager.executePendingTransactions();
                if (fragment instanceof BaseFragment) {
                    ((BaseFragment) fragment).onFragmentShow();
                }
            } catch (Exception e) {
                LoggerUtils.e("switchContent "+fragment+" failed!",e);
            }
        });
    }

    //animationMode = 0 //No Animation
    public void switchBracContent(@NonNull final Fragment fragment, int animationMode) {
        ThreadPool.postOnMain(() -> {
            try {
                hideTopFragment();
                if (mFragmentManager.isDestroyed()) {
                    LoggerUtils.e("FragmentManager is destroyed.");
                    return;
                }
                FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
                String name = fragment.getClass().getName();
                //not use pop animation if it is first fragment in the back stack
                if (mFragmentManager.getBackStackEntryCount() != 0) {
                    if (fragment instanceof BaseFragment && animationMode!=0) {
                        //pop animation
                        int[] animation = ((BaseFragment) fragment).getPopAnimation();
                        if (animation != null) {
                            int popIn = animation.length >= 1 ? animation[0] : 0;
                            int popUp = animation.length >= 2 ? animation[1] : 0;
                            //set fragment animation into fragmentTransaction
                            fragmentTransaction.setCustomAnimations(popIn, popUp, popIn, popUp);
                        }
                    }
                }

                LoggerUtils.d("add " + name);
                fragmentTransaction.add(mFragmentViewId, fragment, name)
                        //add back stack
                        .addToBackStack(name)
                        .commitAllowingStateLoss();

                mFragmentManager.executePendingTransactions();
                if (fragment instanceof BaseFragment) {
                    ((BaseFragment) fragment).onFragmentShow();
                }
            } catch (Exception e) {
                LoggerUtils.e("switchContent "+fragment+" failed!",e);
            }
        });
    }

    /**
     * Back to last same {@link Fragment} in the back stack .If it don't exist,create and show the {@link Fragment}
     *
     * @param fragment {@link Fragment} to show
     */
    public void returnContent(@NonNull Fragment fragment) {
        ThreadPool.postOnMain(() -> {
            try {
                hideTopFragment();
                String excludeName = fragment.getClass().getName();
                noteStateNotSaved();
                boolean popped = mFragmentManager.popBackStackImmediate(excludeName, 0);
                if (popped) {
                    showTopFragment();
                } else {
                    switchContent(fragment);
                }
            } catch (Exception e) {
                LoggerUtils.e("returnContent "+fragment+" failed!",e);

            }
        });
    }

    /**
     * Hide top {@link Fragment}
     */
    private void hideTopFragment() {
        List<Fragment> fragments = mFragmentManager.getFragments();
        for (int i = fragments.size(); i >0; i--) {
            Fragment topFragment = fragments.get(i-1);
            if (topFragment instanceof BaseFragment && topFragment.getId() == mFragmentViewId) {
                BaseFragment baseFragment = (BaseFragment) topFragment;
                if (baseFragment.isShowing()) {
                    baseFragment.onFragmentHide();
                }
            }
        }
    }

    /**
     * Show top {@link Fragment}
     */
    private void showTopFragment() {
        List<Fragment> fragments = mFragmentManager.getFragments();
        for (int i = fragments.size(); i >0; i--) {
            Fragment topFragment = fragments.get(i-1);
            if (topFragment instanceof BaseFragment && topFragment.getId() == mFragmentViewId) {
                BaseFragment baseFragment = (BaseFragment) topFragment;
                if (!baseFragment.isShowing()) {
                    ((BaseFragment) topFragment).onFragmentShow();
                }
            }
        }
    }

    private static Class<?> fmClass;

    /**
     * Set {@link FragmentManager} state to not saved
     */
    private void noteStateNotSaved() {
        if (fmClass == null) {
            fmClass = mFragmentManager.getClass();
        }
        while (fmClass != null) {
            try {
                Method m = fmClass.getDeclaredMethod("noteStateNotSaved");
                m.setAccessible(true);
                m.invoke(mFragmentManager);
                break;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                LoggerUtils.e("Not found method [noteStateNotSaved] in " + fmClass);
                fmClass = fmClass.getSuperclass();
            }
        }
    }

    /**
     * start an activity with a callback
     *
     * @param intent                 activity intent
     * @param options                animation
     * @param activityResultCallback result callback
     */
    public void startActivityForResult(Intent intent, @Nullable ActivityOptionsCompat options, @Nullable ActivityResultCallback<ActivityResult> activityResultCallback) {
        ThreadPool.postOnMain(() -> {
            mFragmentManager.beginTransaction()
                    .add(ForActivityResultFragment.newInstance(intent, options, activityResultCallback), "ForActivityResultFragment#" + System.currentTimeMillis())
                    .commitAllowingStateLoss();
            mFragmentManager.executePendingTransactions();
        });
    }


    /**
     * start an activity.
     *
     * @param intent  activity intent
     * @param options animation
     */
    public void startActivity(Intent intent, @Nullable ActivityOptionsCompat options) {
        if (!mFragmentManager.getFragments().isEmpty()) {
            Fragment fragment = mFragmentManager.getFragments().get(0);
            FragmentActivity activity = fragment.getActivity();
            if (activity != null) {
                if (options != null) {
                    ActivityCompat.startActivity(activity, intent, options.toBundle());
                } else {
                    ActivityCompat.startActivity(activity, intent, null);
                }
                return;
            }
        }
        startActivityForResult(intent, options, null);
    }

    public static class ForActivityResultFragment extends Fragment {
        private ActivityResultCallback<ActivityResult> activityResultCallback;
        private Intent intent;
        private ActivityOptionsCompat options;
        private boolean hasRun;

        static ForActivityResultFragment newInstance(Intent intent, @Nullable ActivityOptionsCompat options, @Nullable ActivityResultCallback<ActivityResult> activityResultCallback) {
            ForActivityResultFragment fragment = new ForActivityResultFragment();
            fragment.activityResultCallback = activityResultCallback;
            fragment.intent = intent;
            fragment.options = options;
            return fragment;
        }

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (!hasRun) {
                getLifecycle().addObserver(new LogLife());
                ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                    getParentFragmentManager().beginTransaction().remove(this).commit();
                    if (activityResultCallback != null) {
                        activityResultCallback.onActivityResult(result);
                    }
                });
                launcher.launch(intent, options);
                hasRun = true;
            }
        }
    }




    /**
     * Share animation element
     */
    public static class ShareElement {
        /**
         * Share view
         */
        private List<View> shareViews;
        /**
         * Transition must be an {@link android.transition.Transition} or {@link androidx.transition.Transition},such as {@link android.transition.ChangeBounds}.
         */
        private Object transition;

        public List<View> getShareViews() {
            return shareViews;
        }

        public Object getTransition() {
            return transition;
        }

        public void setShareViews(View... shareViews) {
            this.shareViews = Arrays.asList(shareViews);
        }

        public void setTransition(Object transition) {
            this.transition = transition;
        }
    }
}
