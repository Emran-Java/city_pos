package acquire.base.chain;

/**
 * The interceptor of {@link Chain}.
 *
 * @author Janson
 * @date 2019/12/24 9:40
 */
public interface Interceptor<T> {
    /**
     * Init param
     * @param t param
     */
    void init(T t);

    /**
     * Intercept the interceptor
     * @param callback interceptor result
     */
    void intercept(Callback callback);

    interface Callback{
        void onResult(boolean isSucc);
    }
}
