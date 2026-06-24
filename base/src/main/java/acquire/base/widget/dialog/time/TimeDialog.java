package acquire.base.widget.dialog.time;

import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.Calendar;
import java.util.Locale;

import acquire.base.R;
import acquire.base.databinding.BaseDialogTimeBinding;
import acquire.base.utils.LoggerUtils;
import acquire.base.widget.dialog.BaseDialog;

/**
 * A date dialog with {@link TimeSpinner}
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2020/3/24 14:51
 */
public class TimeDialog extends BaseDialog {
    private BaseDialogTimeBinding binding;

    private int hour, minute, second;
    private boolean hideHour, hideMinute, hideSecond;

    public TimeDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogTimeBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void init() {
        setCancelable(false);
        Window window = getWindow();
        window.setGravity(Gravity.BOTTOM);
        //set dialog animation
        window.setWindowAnimations(R.style.BottomDialogAnimation);

        //set dialog width and height
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(params);

    }


    @Override
    public void show() {
        setTimeContent(hour, minute, second);
        binding.timeSpinner.setTime(hour, minute, second);
        binding.timeSpinner.hideChild(hideHour, hideMinute, hideSecond);
        super.show();
    }

    /**
     * set time contetn
     */
    private void setTimeContent(int hour, int minute, int second) {
        StringBuilder sb = new StringBuilder();
        if (!hideHour) {
            sb.append(String.format(Locale.getDefault(), "%02d", hour));
            if (!hideMinute || !hideSecond) {
                sb.append(":");
            }
        }
        if (!hideMinute) {
            sb.append(String.format(Locale.getDefault(), "%02d", minute));
            if (!hideSecond) {
                sb.append(":");
            }
        }
        if (!hideSecond) {
            sb.append(String.format(Locale.getDefault(), "%02d", second));
        }
        binding.tvTime.setText(sb.toString());
    }


    /**
     * A builder of {@link TimeDialog}
     *
     * @author Janson
     * @date 2020/4/22 8:47
     */
    public static class Builder {
        private int hour, minute, second;
        private CharSequence title;
        private boolean hideHour, hideMinute, hideSecond;
        private boolean hideContnet;
        private final Context context;
        private OnClickConfirmListener confirmListener;
        private OnClickListener cancelListener;
        private long timeoutMillis;
        private TimeoutListener timeoutListener;
        private boolean backEnable = true;
        private boolean timeoutAttachConfirm;

        public Builder(Context context) {
            this.context = context;
            //default current time
            Calendar time = Calendar.getInstance();
            hour = time.get(Calendar.HOUR_OF_DAY);
            minute = time.get(Calendar.MINUTE);
            second = time.get(Calendar.SECOND);
        }

        public Builder setTitle(@StringRes int titleId) {
            title = context.getString(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        /**
         * hide hour picker view
         */
        public Builder hideHour() {
            this.hideHour = true;
            return this;
        }

        /**
         * hide minute picker view
         */
        public Builder hideMinute() {
            this.hideMinute = true;
            return this;
        }

        /**
         * hide second picker view
         */
        public Builder hideSecond() {
            this.hideSecond = true;
            return this;
        }

        /**
         * hide content
         */
        public Builder hideContent() {
            this.hideContnet = true;
            return this;
        }

        public Builder hour(int hour) {
            this.hour = hour;
            return this;
        }

        public Builder minute(int minute) {
            this.minute = minute;
            return this;
        }


        public Builder second(int second) {
            this.second = second;
            return this;
        }


        public Builder setConfirmListener(OnClickConfirmListener listener) {
            this.confirmListener = listener;
            return this;
        }

        public Builder setCancelListener(OnClickListener listener) {
            this.cancelListener = listener;
            return this;
        }


        public Builder setTimeout(long timeoutMillis, TimeoutListener listener) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutListener = listener;
            return this;
        }
        public Builder setConfirmTimeout(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutAttachConfirm = true;
            return this;
        }
        public Builder setCancelTimeout(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutAttachConfirm = false;
            return this;
        }
        public Builder setBackEnable(boolean enable) {
            this.backEnable = enable;
            return this;
        }

        /**
         * Create a {@link TimeDialog} with the arguments supplied to this builder.
         */
        public TimeDialog create() {
            final TimeDialog dialog = new TimeDialog(context);
            dialog.hour = hour;
            dialog.minute = minute;
            dialog.second = second;
            dialog.hideHour = hideHour;
            dialog.hideMinute = hideMinute;
            dialog.hideSecond = hideSecond;
            if (hideContnet) {
                dialog.binding.tvTime.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(title)) {
                dialog.binding.tvTitle.setText(title);
            }
            dialog.binding.timeSpinner.addOnTimeChangedListener(dialog::setTimeContent);

            //confirm button
            dialog.binding.btnConfirm.setOnClickListener(v -> {
                int[] time = dialog.binding.timeSpinner.getTime();
                dialog.hour = time[0];
                dialog.minute = time[1];
                dialog.second = time[2];
                if (confirmListener != null) {
                    confirmListener.onConfirm(dialog.hour, dialog.minute, dialog.second);
                }
                dialog.dismiss();
            });
            //cancel button
            dialog.binding.btnCancel.setOnClickListener(v -> {
                if (cancelListener != null) {
                    cancelListener.onClick(dialog);
                }
                dialog.dismiss();
            });
            //listen keyboard or navigation key
            dialog.setOnKeyListener(new OnKeyListener() {
                private int handleKey;

                @Override
                public boolean onKey(DialogInterface d, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        handleKey = keyCode;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (handleKey == keyCode) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                //cancel
                                if (backEnable) {
                                    dialog.binding.btnCancel.callOnClick();
                                    return true;
                                }
                            } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
                                //back
                                dialog.binding.btnConfirm.callOnClick();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
            //timeout
            if (timeoutMillis > 0 ) {
                if (timeoutListener != null){
                    dialog.setTimeOut(timeoutMillis, d -> {
                        LoggerUtils.e("Time out");
                        dialog.dismiss();
                        timeoutListener.onTimeout(dialog);
                    });
                }else{
                    dialog.setTimeOut(timeoutMillis,timeoutAttachConfirm?dialog.binding.btnConfirm:dialog.binding.btnCancel);
                }
            }
            return dialog;
        }

        /**
         * Create an {@link TimeDialog} with the arguments supplied to this
         * builder and immediately displays the dialog.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     TimeDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public TimeDialog show() {
            TimeDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    public interface OnClickConfirmListener {
        /**
         * time listemer
         *
         * @param hour   Hour value of a day. e.g. 13
         * @param minute Minute value of a hour. e.g. 52
         * @param second Second value of a minute. e.g. 55
         */
        void onConfirm(int hour, int minute, int second);
    }
}
