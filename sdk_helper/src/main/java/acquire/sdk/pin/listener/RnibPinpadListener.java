package acquire.sdk.pin.listener;

/**
 * Listener of RNIB PIN pad  (blink).
 *
 * @author Janson
 * @date 2025/2/20 10:33
 */
public interface RnibPinpadListener extends PinpadListener {
    void onSlidNumberKey();

    void onSlidNoDigitKey();

    void onSlidBackSpace();

    void onSlidEnter();

    void onSlidCancel();

    void onSlidUp();

    void onSlidDown();

    void onSlidLeft();

    void onSlidRight();
}
