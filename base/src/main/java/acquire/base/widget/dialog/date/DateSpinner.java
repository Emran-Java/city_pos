package acquire.base.widget.dialog.date;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import acquire.base.databinding.BaseSpinnerDateBinding;


/**
 * A date spinner view.
 *
 * @author Janson
 * @date 2020/4/10 11:18
 */
public class DateSpinner extends FrameLayout {
    private final BaseSpinnerDateBinding binding;
    private List<OnDateChangedListener> mOnDateChangedListeners;
    private int[] start = new int[]{1900,1,31}, end = new int[]{2100,12,31};

    public DateSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DateSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DateSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        binding = BaseSpinnerDateBinding.inflate(LayoutInflater.from(context),this,true);

        final Calendar date = Calendar.getInstance();

        binding.wpYear.setRange(1900, 2100);
        binding.wpMonth.setRange(1, 12);
        binding.wpDay.setRange(1, getDaysInMonthAndYear(binding.wpYear.getValue(), binding.wpMonth.getValue()));

        binding.wpYear.setValue(date.get(Calendar.YEAR));
        binding.wpMonth.setValue(date.get(Calendar.MONTH) + 1);
        binding.wpDay.setValue(date.get(Calendar.DAY_OF_MONTH));

        refreshRange(date.get(Calendar.YEAR),date.get(Calendar.MONTH) + 1);

        binding.wpYear.setOnValueChangedListener((picker, oldValue, newValue) -> {
            //When year picker scrolls,updates month and day range.
            refreshRange(Integer.parseInt(newValue),Integer.parseInt(binding.wpMonth.getValue()));
            onDateChanged();
        });
        binding.wpMonth.setOnValueChangedListener((picker, oldValue, newValue) -> {
            //When month picker scrolls,updates day range.
            refreshRange(Integer.parseInt(binding.wpYear.getValue()),Integer.parseInt(newValue));
            onDateChanged();
        });
        binding.wpDay.setOnValueChangedListener((picker, oldValue, newValue) -> onDateChanged());
    }


    /**
     * update date range
     */
    private void refreshRange(int year, int month){
        int startYear = start[0];
        int endYear = end[0];
        binding.wpYear.setRange(startYear,endYear);

        int startMonth = start[1];
        int endMonth = end[1];
        if (year != startYear){
            //it's not start year
            startMonth = 1;
        }
        if (year != endYear){
            //it's not end year
            endMonth = 12;
        }
        if (month < startMonth){
            month = startMonth;
        }
        if (month > endMonth){
            month = endMonth;
        }
        binding.wpMonth.setRange(startMonth,endMonth);

        int startDay = start[2];
        int endDay = end[2];
        if (year != startYear || month != startMonth){
            startDay = 1;
        }
        if (year != endYear || month != endMonth){
            endDay = getDaysInMonthAndYear(year+"", month+"");
        }
        binding.wpDay.setRange(startDay,endDay);
    }
    /**
     * Set spinner date
     */
    public void setDate(int year, int month, int day) {
        refreshRange(year,month);
        binding.wpYear.setValue(year);
        binding.wpMonth.setValue(month);
        binding.wpDay.setValue(day);
    }

    /**
     * Set spinner range
     * @param start start date [yyyy,MM,DD]
     * @param end end date [yyyy,MM,DD]
     */
    public void setRange(int[] start,int[] end){
        if (start != null && start.length == 3){
            this.start = start;
        }
        if (end != null && end.length == 3){
            this.end = end;
        }
        String strStart = String.format(Locale.getDefault(),"%04d%02d%02d",this.start[0],this.start[1],this.start[2]);
        String strEnd = String.format(Locale.getDefault(),"%04d%02d%02d",this.end[0],this.end[1],this.end[2]);
        if (strStart.compareTo(strEnd)>0){
            int[] tmp =  this.start;
            this.start = this.end;
            this.end = tmp;
        }
    }

    /**
     * Get spinner date
     *
     * @return [yyyy,MM,DD]
     */
    public int[] getDate() {
        return new int[]{Integer.parseInt(binding.wpYear.getValue()),
                Integer.parseInt(binding.wpMonth.getValue()),
                Integer.parseInt(binding.wpDay.getValue())};
    }

    /**
     * hide picker
     *
     * @param hideYear  hide year picker
     * @param hideMonth hide month picker
     * @param hideDay   hide day picker
     */
    public void hideChild(boolean hideYear, boolean hideMonth, boolean hideDay) {
        if (( binding.rlYear.getVisibility() == VISIBLE) == hideYear) {
             binding.rlYear.setVisibility(!hideYear ? VISIBLE : GONE);
        }
        if (( binding.rlMonth.getVisibility() == VISIBLE) == hideMonth) {
             binding.rlMonth.setVisibility(!hideMonth ? VISIBLE : GONE);
        }
        if (( binding.rlDay.getVisibility() == VISIBLE) == hideDay) {
             binding.rlDay.setVisibility(!hideDay ? VISIBLE : GONE);
        }
    }

    /**
     * Add a listenr that is called when spinner changes.
     */
    public void addOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        if (mOnDateChangedListeners == null) {
            mOnDateChangedListeners = new ArrayList<>();
        }
        mOnDateChangedListeners.add(onDateChangedListener);
    }

    /**
     * Remove an {@link OnDateChangedListener}
     */
    public void removeOnDateChangedListener(OnDateChangedListener onDateChangedListener) {
        if (mOnDateChangedListeners != null) {
            mOnDateChangedListeners.remove(onDateChangedListener);
        }
    }

    /**
     * This method is called when spinner changes.
     */
    private void onDateChanged() {
        if (mOnDateChangedListeners != null) {
            for (OnDateChangedListener onDateChangedListener : mOnDateChangedListeners) {
                onDateChangedListener.onChange(Integer.parseInt(binding.wpYear.getValue()),
                        Integer.parseInt(binding.wpMonth.getValue()),
                        Integer.parseInt(binding.wpDay.getValue()));
            }
        }
    }

    /**
     * Get the day count of this month
     */
    private int getDaysInMonthAndYear(String year, String month) {
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, Integer.parseInt(year));
        calendar.set(Calendar.MONTH, Integer.parseInt(month) - 1);
        return calendar.getActualMaximum(Calendar.DATE);
    }

    public interface OnDateChangedListener {
        void onChange(int year, int month, int day);
    }
}