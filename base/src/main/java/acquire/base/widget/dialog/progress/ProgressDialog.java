package acquire.base.widget.dialog.progress;

import android.content.Context;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import androidx.annotation.StringRes;

import acquire.base.R;
import acquire.base.databinding.BaseDialogProgressBinding;
import acquire.base.utils.LoggerUtils;
import acquire.base.widget.dialog.BaseDialog;


/**
 * A dialog with progress
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2018/10/9 16:11
 */
public class ProgressDialog extends BaseDialog {
    private BaseDialogProgressBinding binding;

    protected ProgressDialog(Context context) {
        super(context, R.style.BaseDialog_Progress);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogProgressBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void init() {
        setCancelable(false);
    }


    public TextView getContent() {
        return binding.tvContent;
    }


    /**
     * A builder of {@link ProgressDialog}
     */
    public static class Builder {
        private final Context context;
        private CharSequence content;
        private OnShowListener showListener;
        private long timeoutMillis;
        private BaseDialog.TimeoutListener timeoutListener;

        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set content
         */
        public Builder setContent(@StringRes int textId) {
            this.content = context.getString(textId);
            return this;
        }

        /**
         * Set content
         */
        public Builder setContent(CharSequence text) {
            this.content = text;
            return this;
        }


        public Builder setShowListener(OnShowListener listener) {
            this.showListener = listener;
            return this;
        }
        public Builder setTimeout(long timeoutMillis, TimeoutListener listener) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutListener = listener;
            return this;
        }

        /**
         * Create an {@link ProgressDialog} with the arguments supplied to this builder.
         */

        int secondsCounter=0;
        public ProgressDialog create() {
            ProgressDialog dialog = new ProgressDialog(context);
            dialog.binding.tvContent.setText(content);
            dialog.setOnShowListener(showListener);

            if (timeoutMillis > 0 && timeoutListener != null) {
                dialog.setTimeOut(timeoutMillis, d -> {
                    LoggerUtils.e("Time out");
                    dialog.dismiss();
                    timeoutListener.onTimeout(dialog);
                });
            }

            Chronometer chronometer = dialog.binding.chronometer;
            chronometer.setBase(0);
            chronometer.setTextSize(36);
            chronometer.setOnChronometerTickListener(c -> {
                //long elapsedMillis = SystemClock.elapsedRealtime() - c.getBase();
                //long seconds = elapsedMillis / 1000;

                c.setText(String.valueOf(++secondsCounter));
            });

            chronometer.start();
            return dialog;
        }

        /**
         * Create an {@link ProgressDialog} with the arguments supplied to this builder and immediately displays the dialog..
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     ProgressDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public ProgressDialog show() {
            ProgressDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
