package acquire.core.tools;

import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

import acquire.base.utils.ViewUtils;
import acquire.base.widget.keyboard.BaseKeyboard;
import acquire.base.widget.keyboard.listener.KeyboardListener;

/**
 * Physical keyboard utils.(For P300)
 *
 * @author Janson
 * @date 2023/4/11 14:45
 */
public class PhysicalKeyboardUtils {

    public static void setKeyboardListener(View view, KeyboardListener keyboardListener) {
        ViewUtils.setFocus(view);
        view.setOnKeyListener(new View.OnKeyListener() {
            private int handleKey;

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    handleKey = keyCode;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (handleKey == keyCode) {
                        switch (keyCode) {
                            case KeyEvent.KEYCODE_DEL:
                                keyboardListener.onBackspace();
                                return true;
                            case KeyEvent.KEYCODE_ENTER:
                                keyboardListener.onEnter();
                                return true;
                            default:
                                if (keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
                                    keyboardListener.onText(keyCode - KeyEvent.KEYCODE_0 + BaseKeyboard.K_0);
                                    return true;
                                }
                                break;
                        }
                    }
                }
                return false;
            }
        });
    }


    public static void adaptPoint(EditText editText) {
        editText.setOnKeyListener(new View.OnKeyListener() {
            private int handleKey;

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    handleKey = keyCode;
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    if (handleKey == keyCode) {
                        if (keyCode == KeyEvent.KEYCODE_STAR) {
                            Editable editable = editText.getText();
                            int start = editText.getSelectionStart();
                            if (editText.isFocused()) {
                                editable.insert(start, ".");
                            } else {
                                editable.append(".");
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        });
    }

    public static void removeKeyboardListener(View view) {
        view.setOnKeyListener(null);
    }
} 
