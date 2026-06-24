package acquire.base.widget.dialog.date;

import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import acquire.base.R;
import acquire.base.databinding.BaseDialogDateRangeBinding;
import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.widget.dialog.BaseDialog;


/**
 * A date range dialog with {@link DateSpinner}
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2020/3/24 14:51
 */
public class DateRangeDialog extends BaseDialog {
    private boolean hideYear, hideMonth, hideDay;
    private BaseDialogDateRangeBinding binding;

    protected DateRangeDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogDateRangeBinding.inflate(inflater);
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
        binding.startSpinner.hideChild(hideYear, hideMonth, hideDay);
        binding.endSpinner.hideChild(hideYear, hideMonth, hideDay);
        super.show();
    }

    private void toStartTextView() {
        binding.tvStart.setSelected(true);
        binding.tvEnd.setSelected(false);
        binding.startSpinner.setVisibility(View.VISIBLE);
        binding.endSpinner.setVisibility(View.GONE);
    }

    private void toEndTextView() {
        binding.tvStart.setSelected(false);
        binding.tvEnd.setSelected(true);
        binding.startSpinner.setVisibility(View.GONE);
        binding.endSpinner.setVisibility(View.VISIBLE);
    }

    private int[] getDate(TextView textView) {
        String text = textView.getText().toString();
        if (text.isEmpty()) {
            return null;
        }
        String[] strs = text.split("/");
        int[] date = new int[3];
        date[0] = Integer.parseInt(strs[0]);
        date[1] = Integer.parseInt(strs[1]);
        date[2] = Integer.parseInt(strs[2]);
        return date;
    }

    /**
     * Set date
     */
    private void setDateContent(TextView textView, int[] date) {
        Date newDate =  DateUtils.getDate(String.format(Locale.getDefault(), "%04d%02d%02d", date[0], date[1], date[2]),DateUtils.YYYYMMDD);
        textView.setText(DateUtils.formatTime(newDate,DateUtils.YYYY_MM_DD));
    }

    private void adjustDateTextView() {
        String startDate = binding.tvStart.getText().toString();
        String endDate = binding.tvEnd.getText().toString();
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            if (startDate.compareTo(endDate) > 0) {
                int[] startTmp = getDate(binding.tvStart);
                int[] endTmp = getDate(binding.tvEnd);
                if (startTmp != null) {
                    binding.endSpinner.setDate(startTmp[0], startTmp[1], startTmp[2]);
                }
                if (endTmp != null) {
                    binding.startSpinner.setDate(endTmp[0], endTmp[1], endTmp[2]);
                }
                binding.tvStart.setText(endDate);
                binding.tvEnd.setText(startDate);
            }
        }
    }

    /**
     * {@link DateRangeDialog} builder
     *
     * @author Janson
     * @date 2020/4/22 8:47
     */
    public static class Builder {
        private int[] start, end;
        private long timeoutMillis;
        private boolean hideYear, hideMonth, hideDay;
        private final Context context;
        private OnClickConfirmListener confirmListener;
        private OnClickListener cancelListener;
        private TimeoutListener timeoutListener;
        private int[] startDate, endDate;
        private boolean backEnable = true;
        private boolean timeoutAttachConfirm;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder dateContent(int[] startDate, int[] endDate) {
            this.startDate = startDate;
            this.endDate = endDate;
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
         * @param timeoutMillis time out in millisecond
         * @param listener     timeout listener
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
         * Create a {@link DateRangeDialog} with the arguments supplied to this builder.
         */
        public DateRangeDialog create() {
            final DateRangeDialog dialog = new DateRangeDialog(context);
            dialog.hideYear = hideYear;
            dialog.hideMonth = hideMonth;
            dialog.hideDay = hideDay;
            dialog.toStartTextView();
            if (startDate != null) {
                dialog.setDateContent(dialog.binding.tvStart, startDate);
                dialog.toEndTextView();
            }
            if (endDate != null) {
                dialog.setDateContent(dialog.binding.tvEnd, endDate);
            }


            dialog.binding.startSpinner.setRange(start, end);
            dialog.binding.endSpinner.setRange(start, end);
            final Calendar date = Calendar.getInstance();
            dialog.binding.startSpinner.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
            dialog.binding.endSpinner.setDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH) + 1, date.get(Calendar.DAY_OF_MONTH));
            //confirm button
            dialog.binding.btnConfirm.setOnClickListener(v -> {
                dialog.adjustDateTextView();
                if (dialog.binding.startSpinner.getVisibility() == View.VISIBLE) {
                    if (dialog.binding.tvStart.length() == 0) {
                        dialog.setDateContent(dialog.binding.tvStart, dialog.binding.startSpinner.getDate());
                    }
                    dialog.toEndTextView();
                    return;
                }

                if (dialog.binding.endSpinner.getVisibility() == View.VISIBLE
                        && dialog.binding.tvEnd.length() == 0) {
                    dialog.setDateContent(dialog.binding.tvEnd, dialog.binding.endSpinner.getDate());
                    return;
                }
                if (confirmListener != null) {
                    int[] startTemp = dialog.getDate(dialog.binding.tvStart);
                    int[] endTemp = dialog.getDate(dialog.binding.tvEnd);
                    confirmListener.onConfirm(startTemp, endTemp);
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
                            if (keyCode == KeyEvent.KEYCODE_BACK){
                                //cancel
                                if (backEnable) {
                                    dialog.binding.btnCancel.callOnClick();
                                    return true;
                                }
                            }else if (keyCode == KeyEvent.KEYCODE_ENTER){
                                //back
                                dialog.binding.btnConfirm.callOnClick();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
            //set timeout
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
            dialog.binding.tvStart.setOnClickListener(v -> {
                dialog.toStartTextView();
            });
            dialog.binding.tvEnd.setOnClickListener(v -> {
                dialog.toEndTextView();
            });

            dialog.binding.startSpinner.addOnDateChangedListener((year, month, day) -> {
                dialog.setDateContent(dialog.binding.tvStart, new int[]{year, month, day});
            });
            dialog.binding.endSpinner.addOnDateChangedListener((year, month, day) -> {
                dialog.setDateContent(dialog.binding.tvEnd, new int[]{year, month, day});
            });
            return dialog;
        }

        /**
         * Create an {@link DateRangeDialog} with the arguments supplied to this
         * builder and immediately displays the dialog.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     DateDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public DateRangeDialog show() {
            DateRangeDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    public interface OnClickConfirmListener {
        void onConfirm(int[] startDate, int[] endDate);
    }
}
