package acquire.base.widget.dialog.date;

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
import java.util.Date;
import java.util.Locale;

import acquire.base.R;
import acquire.base.databinding.BaseDialogDateBinding;
import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.widget.dialog.BaseDialog;


/**
 * A date dialog with {@link DateSpinner}
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2020/3/24 14:51
 */
public class DateDialog extends BaseDialog {
    private int year, month, day;
    private boolean hideYear, hideMonth, hideDay;
    private BaseDialogDateBinding binding;

    protected DateDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogDateBinding.inflate(inflater);
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
        setDateContent(year, month, day);
        binding.dateSpinner.setDate(year, month, day);
        binding.dateSpinner.hideChild(hideYear, hideMonth, hideDay);
        super.show();
    }

    /**
     * Set date
     */
    private void setDateContent(int year, int month, int day) {
        StringBuilder pattern = new StringBuilder();
        if (!hideMonth) {
            pattern.append("MMMM");
            if (!hideDay) {
                pattern.append(" ");
            }
        }
        if (!hideDay) {
            pattern.append("d");
        }
        if (!hideYear) {
            if (!hideMonth || !hideDay) {
                pattern.append(",");
            }
            pattern.append("yyyy");
        }
        Date newDate =  DateUtils.getDate(String.format(Locale.getDefault(), "%04d%02d%02d", year, month, day),DateUtils.YYYYMMDD);
        binding.tvDate.setText(DateUtils.formatTime(newDate,DateUtils.YYYY_MM_DD));
    }


    /**
     * {@link DateDialog} builder
     *
     * @author Janson
     * @date 2020/4/22 8:47
     */
    public static class Builder {
        private int year, month, day;
        private int[] start, end;
        private long timeoutMillis;
        private CharSequence title;
        private boolean hideYear, hideMonth, hideDay;
        private boolean hideContent;
        private final Context context;
        private OnClickConfirmListener confirmListener;
        private OnClickListener cancelListener;
        private TimeoutListener timeoutListener;
        private boolean backEnable = true;
        private boolean timeoutAttachConfirm;

        public Builder(Context context) {
            this.context = context;
            //default current date
            Calendar date = Calendar.getInstance();
            year = date.get(Calendar.YEAR);
            month = date.get(Calendar.MONTH) + 1;
            day = date.get(Calendar.DAY_OF_MONTH);
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
         * hide year picker view
         */
        public Builder hideYear() {
            this.hideYear = true;
            return this;
        }

        /**
         * hide month picker view
         */
        public Builder hideMonth() {
            this.hideMonth = true;
            return this;
        }

        /**
         * hide day picker view
         */
        public Builder hideDay() {
            this.hideDay = true;
            return this;
        }

        /**
         * hide content textview
         */
        public Builder hideContent() {
            this.hideContent = true;
            return this;
        }


        public Builder year(int year) {
            this.year = year;
            return this;
        }

        public Builder month(int month) {
            this.month = month;
            return this;
        }


        public Builder day(int day) {
            this.day = day;
            return this;
        }

        /**
         * Set start date
         */
        public Builder start(int year, int month, int day) {
            this.start = new int[]{year, month, day};
            return this;
        }

        /**
         * Set end date
         */
        public Builder end(int year, int month, int day) {
            this.end = new int[]{year, month, day};
            return this;
        }

        /**
         * Set end to today
         */
        public Builder endToday() {
            Calendar date = Calendar.getInstance();
            this.end = new int[]{date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH)};
            return this;
        }

        /**
         * Set confirm button listener
         */
        public Builder setConfirmListener(OnClickConfirmListener listener) {
            this.confirmListener = listener;
            return this;
        }

        /**
         * Set cancel button listener
         */
        public Builder setCancelListener(OnClickListener listener) {
            this.cancelListener = listener;
            return this;
        }

        /**
         * Set timeout
         *
         * @param timeoutMillis time out in  millisecond
         * @param listener      timeout listener
         */
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
         * Create a {@link DateDialog} with the arguments supplied to this builder.
         */
        public DateDialog create() {
            final DateDialog dialog = new DateDialog(context);
            dialog.year = year;
            dialog.month = month;
            dialog.day = day;
            dialog.hideYear = hideYear;
            dialog.hideMonth = hideMonth;
            dialog.hideDay = hideDay;
            if (hideContent) {
                dialog.binding.tvDate.setVisibility(View.GONE);
            }
            if (!TextUtils.isEmpty(title)) {
                dialog.binding.tvTitle.setText(title);
            }
            dialog.binding.dateSpinner.addOnDateChangedListener(dialog::setDateContent);
            dialog.binding.dateSpinner.setRange(start, end);
            dialog.binding.btnConfirm.setOnClickListener(v -> {
                int[] date = dialog.binding.dateSpinner.getDate();
                dialog.year = date[0];
                dialog.month = date[1];
                dialog.day = date[2];
                if (confirmListener != null) {
                    confirmListener.onConfirm(dialog.year, dialog.month, dialog.day);
                }
                dialog.dismiss();
            });
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
         * Create a {@link DateDialog} with the arguments supplied to this
         * builder and immediately displays the dialog.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     DateDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public DateDialog show() {
            DateDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    public interface OnClickConfirmListener {
        /**
         * data listener
         *
         * @param year  Year value。  e.g. 2023.
         * @param month Month value. e.g. 1 for January.
         * @param day   Day value of a month. e.g. 30
         */
        void onConfirm(int year, int month, int day);
    }
}
