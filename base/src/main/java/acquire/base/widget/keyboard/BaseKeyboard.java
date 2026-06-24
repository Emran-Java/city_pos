package acquire.base.widget.keyboard;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import acquire.base.widget.keyboard.listener.KeyboardListener;

/**
 * A basic keyboard
 *
 * @author Janson
 * @date 2018/3/6
 */
public class BaseKeyboard extends FrameLayout {
    /**
     * Number&Alpha
     */
    public static final int K_0 = 0x30;
    public static final int K_1 = 0x31;
    public static final int K_2 = 0x32;
    public static final int K_3 = 0x33;
    public static final int K_4 = 0x34;
    public static final int K_5 = 0x35;
    public static final int K_6 = 0x36;
    public static final int K_7 = 0x37;
    public static final int K_8 = 0x38;
    public static final int K_9 = 0x39;
    public static final int K_A = 0x41;
    public static final int K_B = 0x42;
    public static final int K_C = 0x43;
    public static final int K_D = 0x44;
    public static final int K_E = 0x45;
    public static final int K_F = 0x46;
    /**
     * UNBALE
     */
    public static final int K_NULL = 0x00;
    /**
     * CANCEL
     */
    public static final int K_CANCEL = 0x18;
    /**
     * CLEAR
     */
    public static final int K_CLEAR = 0x1B;
    /**
     * BACK
     */
    public static final int K_BACKSPACE = 0x08;
    /**
     * ENTER
     */
    public static final int K_ENTER = 0x0D;
    /**
     * DOT
     */
    public static final int K_POINT = 0x2E;
    /**
     * *
     */
    public static final int K_STAR = 0x2A;
    /**
     * +
     */
    public static final int K_PLUS = 0x2B;
    /**
     * -
     */
    public static final int K_MINUS = 0x2D;
    /**
     * #
     */
    public static final int K_POUND = 0x23;

    /**
     * 00
     */
    public static final int K_00 = 0x3030;

    private KeyboardListener mKeyBoardListener;


    public BaseKeyboard(Context context) {
        this(context, null);
    }

    public BaseKeyboard(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BaseKeyboard(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void mapKey(View keyButton, int keyCode) {
        //key click listener
        if (keyCode != K_NULL) {
            keyButton.setTag(keyCode);

            if (keyCode == K_BACKSPACE) {
                keyButton.setOnLongClickListener(v -> {
                    if (mKeyBoardListener == null) {
                        return false;
                    }
                    mKeyBoardListener.onClear();
                    return false;
                });
            }
            keyButton.setOnClickListener(v -> {
                if (mKeyBoardListener == null) {
                    return;
                }
                switch (keyCode) {
                    case K_CANCEL:
                        mKeyBoardListener.onCancel();
                        break;
                    case K_BACKSPACE:
                        mKeyBoardListener.onBackspace();
                        break;
                    case K_CLEAR:
                        mKeyBoardListener.onClear();
                        break;
                    case K_ENTER:
                        mKeyBoardListener.onEnter();
                        break;
                    case K_00:
                        mKeyBoardListener.onText(K_0);
                        mKeyBoardListener.onText(K_0);
                        break;
                    default:
                        mKeyBoardListener.onText(keyCode);
                        break;
                }
            });
        }
    }


    public <T extends View> T findKey(int keyCode) {
        return findViewWithTag(keyCode);
    }

    public void setKeyBoardListener(KeyboardListener keyBoardListener) {
        this.mKeyBoardListener = keyBoardListener;
    }

    public KeyboardListener getKeyBoardListener() {
        return mKeyBoardListener;
    }


}
