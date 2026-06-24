package acquire.core;

/**
 * Transaction result listener.
 *
 * @author Janson
 * @date 2019/12/13 16:44
 */
public interface TransResultListener {
    /**
     * Called at the end of a transaction.
     *
     * @param success   transaction success.
     */
    void onTransResult(boolean success);
}
