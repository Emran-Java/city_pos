package acquire.base.activity.callback;

import acquire.base.activity.BaseFragment;

/**
 * A {@link BaseFragment} result callback
 *
 * @author Janson
 * @date 2019/3/29 17:52
 */
public interface FragmentCallback<T> {
    int FAIL = -1;
    int CANCEL = -2;
    int TIMEOUT = -3;

    /**
     * Called as {@link BaseFragment} success
     *
     * @param t result data
     */
    void onSuccess(T t);

    /**
     * Called as {@link BaseFragment} timeout or canceld or error.
     *
     * @param errorType error code：{@link #FAIL}、{@link #CANCEL}、{@link #TIMEOUT}.
     * @param errorMsg  error message
     */
    void onFail(int errorType, String errorMsg);

}
