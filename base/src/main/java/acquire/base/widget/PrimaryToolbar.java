package acquire.base.widget;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import acquire.base.R;
import acquire.base.databinding.BasePrimaryToolbarBinding;
import acquire.base.utils.DisplayUtils;


/**
 * A custom toolbar with a left navigation and title automatically filled according to {@link Activity#getTitle()}.
 *
 * @author Janson
 * @date 2019/2/1 15:54
 */
public class PrimaryToolbar extends Toolbar {
    private BasePrimaryToolbarBinding binding;

    public PrimaryToolbar(Context context) {
        this(context, null);
    }

    public PrimaryToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PrimaryToolbar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context, attrs, defStyle);
    }

    /**
     * Init views
     */
    protected void initView(Context context, AttributeSet attrs, int defStyle) {
        binding = BasePrimaryToolbarBinding.inflate(LayoutInflater.from(context));
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PrimaryToolbar, defStyle, 0);
            //title
            String title = a.getString(R.styleable.PrimaryToolbar_title);
            binding.tvTitle.setText(title);
            Drawable rightDrawable = a.getDrawable(R.styleable.PrimaryToolbar_rightIcon);
            String rightText = a.getString(R.styleable.PrimaryToolbar_rightContent);
            if (rightDrawable != null || rightText!=null){
                binding.ivRight.setImageDrawable(rightDrawable);
                binding.tvRight.setText(rightText);
                binding.llRight.setVisibility(VISIBLE);
            }
            a.recycle();
        }
        if (getBackground() == null) {
            //setBackgroundColor(ContextCompat.getColor(context,R.color.base_colorPrimary));
            setBackgroundResource(R.drawable.bg_primary_toolbar);
        }
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER);
        addView(binding.getRoot(), lp);
        //fits system windows
        DisplayUtils.fitsWindowStatus(binding.getRoot());
    }

    @Override
    public void setNavigationIcon(@Nullable Drawable icon) {
        binding.ivBack.setBackground(icon);
    }

    @Override
    public void setTitle(CharSequence title) {
        if (binding != null) {
            binding.tvTitle.setText(title);
        }
    }

    public void setBackListener(OnClickListener listener){
        if (binding != null) {
            binding.ivBack.setVisibility(listener !=null ? VISIBLE : GONE);
            binding.ivBack.setOnClickListener(listener);
        }
    }
    public void setRightListener(OnClickListener listener) {
        if (binding != null) {
            binding.llRight.setOnClickListener(listener);
        }
    }
    public void setRightIcon(@Nullable Drawable icon) {
        if (binding != null) {
            binding.ivRight.setImageDrawable(icon);
            binding.llRight.setVisibility(VISIBLE);
        }
    }
    public void setRightText(CharSequence text) {
        if (binding != null) {
            binding.tvRight.setText(text);
            binding.llRight.setVisibility(VISIBLE);
        }
    }
}
