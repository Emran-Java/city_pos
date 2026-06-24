package acquire.sdk.pin.listener;

/**
 * Listener of PIN enter.
 *
 * @author Janson
 * @date 2018/5/24 1:12
 */
public interface PinpadListener {
    /**
     * Cancel
     */
    void onCancel();

    /**
     * Error
     *
     * @param errorCode        Code of error
     * @param errorDescription detail
     */
    void onError(int errorCode, String errorDescription);

    void onTimeout();

    /**
     * Key
     *
     * @param len Length of currently entered data
     */
    void onKeyDown(int len);

    /**
     * Result
     *
     * @param pin PIN block
     */
    void onPinRslt(byte[] pin);
}
