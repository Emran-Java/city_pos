package acquire.base.widget.keyboard.listener;

import android.text.Editable;
import android.view.View;
import android.widget.EditText;

import androidx.activity.OnBackPressedDispatcherOwner;

import acquire.base.utils.InputUtils;
import acquire.base.utils.LoggerUtils;


/**
 * A subclass of {@link KeyboardListener} for {@link EditText}
 *
 * @author Janson
 * @date 2018/3/2
 */
public abstract class EditKeyboardListener implements KeyboardListener {

    private int mMaxLength;
    protected EditText mEditText;
    private final OnBackPressedDispatcherOwner dispatcherOwner;

    public EditKeyboardListener(OnBackPressedDispatcherOwner dispatcherOwner, EditText editText) {
        this(dispatcherOwner,editText,  999);
    }

    public EditKeyboardListener(OnBackPressedDispatcherOwner dispatcherOwner, EditText editText, int maxLength) {
        this.mEditText = editText;
        this.mMaxLength = maxLength;
        this.dispatcherOwner = dispatcherOwner;
        InputUtils.hideKeyboardByEditText(editText);
    }

    @Override
    public void onText(int code) {
        Editable editable= mEditText.getText();
        int start = mEditText.getSelectionStart();
        if (editable.length() < mMaxLength) {
            if (mEditText.isFocused()) {
                editable.insert(start, Character.toString((char) code));
            } else {
                editable.append((char) code);
            }
        }
    }

    @Override
    public void onBackspace() {
        Editable editable= mEditText.getText();
        int start = mEditText.getSelectionStart();
        if (editable != null && editable.length() > 0) {
            if (start > 0) {
                editable.delete(start - 1, start);
            }
        }
    }

    @Override
    public void onClear() {
        mEditText.setText("");
    }

    @Override
    public void onCancel() {
        dispatcherOwner.getOnBackPressedDispatcher().onBackPressed();
    }

    /**
     * Set max input length
     */
    public void setMaxLength(int maxLength) {
        this.mMaxLength = maxLength;
    }

    /**
     * Set attached {@link EditText}
     */
    public void setTargetView(EditText editText) {
        this.mEditText = editText;
        InputUtils.hideKeyboardByEditText(editText);
    }
    public View getTargetView() {
        return mEditText;
    }

}
