package acquire.base.activity.callback;

import acquire.base.activity.BaseFragment;

/**
 * A simple result callback for {@link BaseFragment}
 *
 * @author Janson
 * @date 2019/1/23 11:22
 */
public abstract class SimpleCallback implements FragmentCallback<Void> {
    @Override
    public void onSuccess(Void o) {
        result();
    }

    @Override
    public void onFail(int errorType, String errorMsg) {
        result();
    }

    /**
     * Called as {@link BaseFragment} finish
     */
    public abstract void result();
}
