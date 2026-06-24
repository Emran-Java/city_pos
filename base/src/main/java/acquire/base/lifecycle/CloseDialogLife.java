package acquire.base.lifecycle;


import android.app.Dialog;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;


/**
 * Close dialog according activity life
 *
 * @author Janson
 * @date 2018/11/19 21:03
 */
public class CloseDialogLife implements DefaultLifecycleObserver {
    private final Dialog dialog;

    public CloseDialogLife(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        if (dialog.isShowing()) {
            dialog.dismiss();
        }
    }
}
