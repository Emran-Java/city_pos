package acquire.core.trans.pack;

import android.app.Dialog;
import android.view.KeyEvent;

import androidx.appcompat.app.AppCompatActivity;

import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.R;
import acquire.core.display2.ProgressPresentation;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * The basic class of request tool
 *
 * @author Janson
 * @date 2021/7/2 14:44
 */
public abstract class BaseCaller {
    private long dialogShowStart = 0;
    private ProgressDialog progressDialog;
    private Dialog backDialog;
    private ProgressPresentation presentation;
    protected void showProgress(AppCompatActivity activity, String content) {
        ThreadPool.postOnMain(() -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.getContent().setText(content);
            } else {
                progressDialog = new ProgressDialog.Builder(activity)
                        .setContent(content)
                        .show();
            }
            if (Model.X800.equals(BDevice.getDeviceModel())) {
                if (presentation == null || !presentation.isShowing()) {
                    presentation = new ProgressPresentation(activity);
                    presentation.show();
                }

            }


        });
        dialogShowStart = System.currentTimeMillis();
    }

    protected void hideProgress() {
        Runnable runnable = () -> {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
                progressDialog = null;
            }
            if (backDialog != null && backDialog.isShowing()) {
                backDialog.dismiss();
                backDialog = null;
            }
            if (presentation != null && presentation.isShowing()){
                presentation.dismiss();
            }
        };
        long showTime = System.currentTimeMillis() - dialogShowStart;
        if (showTime > 700) {
            ThreadPool.postOnMain(runnable);
        } else {
            ThreadPool.postDelayOnMain(runnable, 500);
        }

    }

    /**
     * Set cancel button
     *
     * @param cancelRunnable cancel listener
     */
    protected void setProgressCancelListener(AppCompatActivity activity, Runnable cancelRunnable) {
        ThreadPool.postOnMain(()->{
            if (progressDialog == null || !progressDialog.isShowing()) {
                return;
            }
            progressDialog.setOnKeyListener((dialog, keyCode, event) -> {
                if (keyCode == KeyEvent.KEYCODE_BACK
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getRepeatCount() == 0) {
                    if (backDialog != null && backDialog.isShowing()) {
                        backDialog.dismiss();
                    }
                    backDialog = new MessageDialog.Builder(activity)
                            .setMessage(R.string.core_comm_cancel)
                            .setConfirmButton(v -> cancelRunnable.run())
                            .setCancelButton(v -> {})
                            .show();
                    return true;
                } else {
                    return false;
                }
            });
        });
    }
} 
