package acquire.base.widget.keyboard.listener;


import acquire.base.widget.keyboard.BaseKeyboard;

/**
 * A basic {@link BaseKeyboard} listener
 *
 * @author Janson
 * @date 2018/3/2
 */
public interface KeyboardListener {

    /**
     * Click key
     *
     * @param code key code
     * @see BaseKeyboard
     */
    void onText(int code);

    /**
     * Click enter
     */
    void onEnter();

    /**
     * Click back
     */
    void onBackspace();

    /**
     * Click clear
     */
    void onClear();

    /**
     * Click cancel
     */
    void onCancel();

}
