package acquire.base.utils;

import android.app.Activity;
import android.content.Context;
import android.os.IBinder;
import android.text.InputType;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

/**
 * Input Utils
 *
 * @author CB
 * @date 2014/7/2
 */
public class InputUtils {

    /**
     * Show system keyboard
     */
    public static void showKeyboard(@NonNull View v) {
        InputMethodManager imm = (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.showSoftInput(v, InputMethodManager.SHOW_FORCED);
        }
    }


    /**
     * hide system keyboard (must invoke before system keyboard showing)
     *
     * @param editText attached view
     */
    public static void alwaysHideKeyBoard(TextView editText) {
        if (editText != null) {
            editText.setInputType(InputType.TYPE_NULL);
        }
    }

    /**
     * hide system keyboard in a activity life。(invoke in onCreate)
     */
    public static void hideKeyboard(@NonNull Window window) {
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    /**
     * hide the shown system keyboard
     */
    public static void hideKeyboard(@NonNull View view) {
        hideKeyboard(view.getContext(),view.getWindowToken());
    }
    public static void hideKeyboard(@NonNull Activity activity) {
        View view = activity.getWindow().peekDecorView();
        hideKeyboard(activity,view.getWindowToken());
    }
    private static void hideKeyboard(@NonNull Context context, IBinder windowToken){
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isActive()) {
            //hide system keyboard if it is shown
            imm.hideSoftInputFromWindow(windowToken, 0);
        }
    }
    /**
     * hide system keyboard of editText
     */
    public static void hideKeyboardByEditText(@NonNull EditText editText) {
        editText.setShowSoftInputOnFocus(false);
    }
}
