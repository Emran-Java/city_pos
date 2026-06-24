package acquire.base.widget.dialog.time;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import acquire.base.databinding.BaseSpinnerTimeBinding;


/**
 * A time spinner view.
 *
 * @author Janson
 * @date 2020/4/10 11:18
 */
public class TimeSpinner extends FrameLayout {
    private final BaseSpinnerTimeBinding binding;
    private List<OnTimeChangedListener> mOnTimeChangedListeners;

    public TimeSpinner(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TimeSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimeSpinner(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        binding = BaseSpinnerTimeBinding.inflate(LayoutInflater.from(context),this, true);
        final Calendar date = Calendar.getInstance();

        binding.wpHour.setRange(0, 23);
        binding.wpHour.setValue(date.get(Calendar.HOUR));

        binding.wpMinute.setRange(0, 59);
        binding.wpMinute.setValue(date.get(Calendar.MINUTE));

        binding.wpSecond.setRange(0, 59);
        binding.wpSecond.setValue(date.get(Calendar.SECOND));

        binding.wpHour.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());
        binding.wpMinute.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());
        binding.wpSecond.setOnValueChangedListener((picker, oldVal, newVal) -> onTimeChanged());
    }

    /**
     * Set spinner time
     */
    public void setTime(int hour, int minute, int second) {
        binding.wpHour.setValue(hour);
        binding.wpMinute.setValue(minute);
        binding.wpSecond.setValue(second);
    }

    /**
     * Get spinner time
     *
     * @return {HH,mm,ss}
     */
    public int[] getTime() {
        return new int[]{Integer.parseInt(binding.wpHour.getValue()),
                Integer.parseInt(binding.wpMinute.getValue()),
                Integer.parseInt(binding.wpSecond.getValue())};
    }

    /**
     * hide picker
     *
     * @param hideHour   hide hour picker
     * @param hideMinute hide minute picker
     * @param hideSecond hide second picker
     */
    public void hideChild(boolean hideHour, boolean hideMinute, boolean hideSecond) {
        if ((binding.rlHour.getVisibility() == VISIBLE) == hideHour) {
            binding.rlHour.setVisibility(!hideHour ? VISIBLE : GONE);
        }
        if ((binding.rlMinute.getVisibility() == VISIBLE) == hideMinute) {
            binding.rlMinute.setVisibility(!hideMinute ? VISIBLE : GONE);
        }
        if ((binding.rlSecond.getVisibility() == VISIBLE) == hideSecond) {
            binding.rlSecond.setVisibility(!hideSecond ? VISIBLE : GONE);
        }
    }

    /**
     * Add a listenr that is called when spinner changes.
     */
    public void addOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        if (mOnTimeChangedListeners == null) {
            mOnTimeChangedListeners = new ArrayList<>();
        }
        mOnTimeChangedListeners.add(onTimeChangedListener);
    }

    /**
     * Remove an {@link OnTimeChangedListener}
     */
    public void removeOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        if (mOnTimeChangedListeners != null) {
            mOnTimeChangedListeners.remove(onTimeChangedListener);
        }
    }

    /**
     * This method is called when spinner changes.
     */
    private void onTimeChanged() {
        if (mOnTimeChangedListeners != null) {
            for (OnTimeChangedListener onTimeChangedListener : mOnTimeChangedListeners) {
                onTimeChangedListener.onChange(Integer.parseInt(binding.wpHour.getValue()),
                        Integer.parseInt(binding.wpMinute.getValue()),
                        Integer.parseInt(binding.wpSecond.getValue()));
            }
        }
    }


    public interface OnTimeChangedListener {
        void onChange(int hour, int minute, int second);
    }
}
