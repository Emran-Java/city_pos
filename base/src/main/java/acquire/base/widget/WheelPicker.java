package acquire.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.OverScroller;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Arrays;

import acquire.base.R;


/**
 * A wheel picker view
 *
 * @author Janson
 * @date 2020/4/12 10:49
 */
public class WheelPicker extends View {
    private final static float TOP_AND_BOTTOM_FADING_EDGE_STRENGTH = 0.9f;
    private final static int SNAP_SCROLL_DURATION = 300;
    private final static int SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT = 4;
    /**
     * Default item count
     */
    private final static int DEFAULT_ITEM_COUNT = 3;
    /**
     * Show item count
     */
    private final int mShowCount;
    /**
     * Center item index
     */
    private final int mShowMiddleIndex;
    /**
     * Primary text pain
     */
    private final Paint mTextPaint;
    /**
     * Side text pain
     */
    private final Paint mSidePaint;

    private float mLeftTextWidth, mRightTextWidth, mTextWidth;
    /**
     * The text color to be selected
     */
    private final int mSelectedTextColor;
    /**
     * The text color to be unselected
     */
    private int mUnSelectedTextColor;
    /**
     * Text size
     */
    private final int mTextSize;

    private final OverScroller mOverScroller;
    private VelocityTracker mVelocityTracker;
    private final int mTouchSlop;
    private final int mMaximumVelocity;
    private final int mMinimumVelocity;
    private float mLastY = 0;
    private boolean mIsDragging;
    /**
     * first item position
     */
    private int mCurFirstPosition = 0;
    /**
     * Text padding
     */
    private int mTextPadding = 0;
    private int mTextHeitht = 0;
    /**
     * Item height
     */
    private int mItemHeight = 0;
    private int mPreviousScrollerY = 0;
    private OnValueChangeListener mOnValueChangeListener;
    private OnScrollListener mOnScrollListener;
    private final float mSelectedTextScale;
    private final WheelData mWheelData;
    private String mLeftText,mRightText;

    private int mScrollState = OnScrollListener.SCROLL_STATE_IDLE;

    public WheelPicker(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WheelPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        TypedArray attributesArray = context.obtainStyledAttributes(attrs, R.styleable.WheelPicker, defStyleAttr, 0);
        mWheelData = new WheelData();
        //show count
        mShowCount = attributesArray.getInt(R.styleable.WheelPicker_showCount, DEFAULT_ITEM_COUNT);
        //center index
        mShowMiddleIndex = (mShowCount - 1) / 2;
        //left and right content
        mLeftText = attributesArray.getString(R.styleable.WheelPicker_leftText);
        mRightText = attributesArray.getString(R.styleable.WheelPicker_rightText);
        //whether to be cycled
        mWheelData.setCycle(attributesArray.getBoolean(R.styleable.WheelPicker_cycle, false));
        //the item text scale to be selected
        mSelectedTextScale = attributesArray.getFloat(R.styleable.WheelPicker_selectedTextScale, 0.3f);

        TypedArray themeArray = getContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.colorPrimary,
                android.R.attr.textColor,
                android.R.attr.textSize});
        try {
            //the item text color to be selected
            int defaultSelectColor = themeArray.getColor(0, Color.BLUE);
            mSelectedTextColor = attributesArray.getColor(R.styleable.WheelPicker_selectedTextColor,defaultSelectColor);
            //the item text color to be unselected
            int defaultUnselectColor = themeArray.getColor(1, Color.BLACK);
            mUnSelectedTextColor = attributesArray.getColor(R.styleable.WheelPicker_textColor,defaultUnselectColor);
            //text size
            int defaultTextSize = themeArray.getDimensionPixelSize(2, 64);
            mTextSize = attributesArray.getDimensionPixelSize(R.styleable.WheelPicker_textSize, defaultTextSize);
        }finally {
            themeArray.recycle();
        }


        //primary pain
        mTextPaint = new Paint();
        mTextPaint.setTextAlign(Align.CENTER);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //side pain
        mSidePaint = new Paint();
        mSidePaint.setTextAlign(Align.CENTER);
        mSidePaint.setAntiAlias(true);
        mSidePaint.setTextSize(mTextSize >> 1);
        mSidePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mSidePaint.setColor(mSelectedTextColor);
        attributesArray.recycle();

        //rolling inertia effect
        mOverScroller = new OverScroller(context, new DecelerateInterpolator(2.5f));
        ViewConfiguration configuration = ViewConfiguration.get(context);
        mTouchSlop = configuration.getScaledTouchSlop();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity() / SELECTOR_MAX_FLING_VELOCITY_ADJUSTMENT;
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (changed) {
            //get item height
            mItemHeight = getHeight() / mShowCount;
            //get text hegith and padding
            Paint.FontMetricsInt metricsInt = mTextPaint.getFontMetricsInt();
            mTextHeitht = Math.abs(metricsInt.bottom + metricsInt.top);
            mTextPadding = (mItemHeight - mTextHeitht)/2;
            mCurFirstPosition = 0;
            //edge gradient
            setVerticalFadingEdgeEnabled(true);
            setFadingEdgeLength((getBottom() - getTop() - mItemHeight) / 2);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // Try greedily to fit the max width and height.
        ViewGroup.LayoutParams lp = getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        int width = calculateSize(getSuggestedMinimumWidth(), lp.width, widthMeasureSpec);
        int height = calculateSize(getSuggestedMinimumHeight(), lp.height, heightMeasureSpec);

        width += getPaddingLeft() + getPaddingRight();
        height += getPaddingTop() + getPaddingBottom();
        setMeasuredDimension(width, height);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        int suggested = super.getSuggestedMinimumWidth();
        if (mShowCount > 0) {
            mTextPaint.setTextSize(mTextSize * (1 + mSelectedTextScale));
            mTextWidth =  mTextPaint.measureText(mWheelData.getMaxItem());
            if (mLeftText != null){
                mLeftTextWidth = mSidePaint.measureText(mLeftText);
            }
            if (mRightText != null){
                mRightTextWidth = mSidePaint.measureText(mRightText);
            }
            suggested =  Math.max(suggested, (int)(mTextWidth + mLeftTextWidth+mRightTextWidth));
            mTextPaint.setTextSize(mTextSize * 1.0f);
        }
        return suggested;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        int suggested = super.getSuggestedMinimumHeight();
        if (mShowCount > 0) {
            Paint.FontMetricsInt fontMetricsInt = mTextPaint.getFontMetricsInt();
            int height = fontMetricsInt.descent - fontMetricsInt.ascent;
            height = height * 2;
            suggested = Math.max(suggested, height * mShowCount);
        }
        return suggested;
    }

    /**
     * used in {@link #onMeasure(int, int)}
     */
    private int calculateSize(int suggestedSize, int paramSize, int measureSpec) {
        int size = MeasureSpec.getSize(measureSpec);
        int mode = MeasureSpec.getMode(measureSpec);
        switch (MeasureSpec.getMode(mode)) {
            case MeasureSpec.AT_MOST:
                if (paramSize == ViewGroup.LayoutParams.WRAP_CONTENT) {
                    return Math.min(suggestedSize, size);
                } else if (paramSize == ViewGroup.LayoutParams.MATCH_PARENT) {
                    return size;
                } else {
                    return Math.min(paramSize, size);
                }
            case MeasureSpec.EXACTLY:
                return size;
            case MeasureSpec.UNSPECIFIED:
                if (paramSize == ViewGroup.LayoutParams.WRAP_CONTENT || paramSize == ViewGroup.LayoutParams.MATCH_PARENT) {
                    return suggestedSize;
                } else {
                    return paramSize;
                }
            default:
                return 0;
        }
    }


    @Override
    protected float getBottomFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected float getTopFadingEdgeStrength() {
        return TOP_AND_BOTTOM_FADING_EDGE_STRENGTH;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWheelData.count() == 0) {
            return;
        }
        float x;
        //options text draw on both sides
        switch (mTextPaint.getTextAlign()) {
            case LEFT:
                x = getPaddingLeft();
                if (mLeftText != null){
                    canvas.save();
                    canvas.drawText(mLeftText, x, getHeight() >> 1, mSidePaint);
                    canvas.restore();
                    x += mLeftTextWidth;
                }
                if (mRightText != null){
                    canvas.save();
                    canvas.drawText(mRightText, x+mTextWidth, getHeight() >> 1, mSidePaint);
                    canvas.restore();
                }
                break;
            case RIGHT:
                x = getRight() - getLeft() - getPaddingRight();
                if (mRightText != null){
                    canvas.save();
                    canvas.drawText(mRightText, x, getHeight() >> 1, mSidePaint);
                    canvas.restore();
                    x -= mRightTextWidth;
                }
                if (mLeftText != null){
                    canvas.save();
                    canvas.drawText(mLeftText, x - mTextWidth, getHeight() >> 1, mSidePaint);
                    canvas.restore();
                }
                break;
            case CENTER:
            default:
                x = (getRight() - getLeft()) >> 1;
                if (mLeftText != null){
                    canvas.save();
                    canvas.drawText(mLeftText, x - mTextWidth/2- mLeftTextWidth /2, getHeight() >> 1, mSidePaint);
                    canvas.restore();
                }
                if (mRightText != null){
                    canvas.save();
                    canvas.drawText(mRightText, x+mTextWidth/2 +mRightTextWidth /2, getHeight() >> 1, mSidePaint);
                    canvas.restore();
                }
                break;
        }
        //calculate the distance between the middle and the two sides,
        // which is used to gradually enlarge the effect from the two sides to the middle
        int topIndexDiffToMid = mShowMiddleIndex;
        int bottomIndexDiffToMid = mShowCount - mShowMiddleIndex - 1;
        int maxIndexDiffToMidPix = Math.max(topIndexDiffToMid, bottomIndexDiffToMid) * mItemHeight;
        int startIndex = mWheelData.getSelectIndex() - mShowMiddleIndex;
        float y = mCurFirstPosition;
        for (int i = 0; i < mShowCount; i++) {
            //1.set scale
            float scale = 1f;
            float offsetToMiddle = Math.abs(y - mShowMiddleIndex* mItemHeight);
            if (maxIndexDiffToMidPix != 0) {
                scale = mSelectedTextScale * (maxIndexDiffToMidPix - offsetToMiddle) / (maxIndexDiffToMidPix) + 1;
            }
            //2.set text color
            if (offsetToMiddle < mItemHeight >> 1) {
                mTextPaint.setColor(mSelectedTextColor);
            } else {
                mTextPaint.setColor(mUnSelectedTextColor);
            }
            canvas.save();
            canvas.scale(scale, scale, x, y+mItemHeight- mTextPadding);
            /*
                draw text,
                if not cycle scroll, need to determine whether the options exceed the upper and lower limits
             */
            if (mWheelData.isCycle() || startIndex + i >= 0 && startIndex + i < mWheelData.count()) {
                canvas.drawText(mWheelData.getItem(startIndex + i), x, y+mItemHeight- mTextPadding, mTextPaint);
            }
            canvas.restore();
            //add 1 cell height to draw the next one
            y += mItemHeight;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                if (mOverScroller.isFinished()) {
                    mOverScroller.forceFinished(true);
                }
                mLastY = event.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = event.getY() - mLastY;
                if (!mIsDragging && Math.abs(deltaY) > mTouchSlop) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop;
                    } else {
                        deltaY += mTouchSlop;
                    }
                    onScrollStateChange(OnScrollListener.SCROLL_STATE_TOUCH_SCROLL);
                    mIsDragging = true;
                }
                if (mIsDragging) {
                    scrollBy(0, (int) deltaY);
                    invalidate();
                    mLastY = event.getY();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsDragging) {
                    mIsDragging = false;
                    getParent().requestDisallowInterceptTouchEvent(false);

                    mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int velocity = (int) mVelocityTracker.getYVelocity();
                    //惯性滚动
                    if (Math.abs(velocity) > mMinimumVelocity) {
                        mPreviousScrollerY = 0;
                        mOverScroller.fling(getScrollX(), getScrollY(), 0, velocity, 0, 0,
                                Integer.MIN_VALUE, Integer.MAX_VALUE, 0, (int) (mItemHeight * 0.7));
                        onScrollStateChange(OnScrollListener.SCROLL_STATE_FLING);
                    }
                    postInvalidateOnAnimation();
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                } else {
                    //不滑动，直接点击选项，则自动滚动到点击项位置
                    int selectorIndexOffset = (int) (event.getY() / mItemHeight) - mShowMiddleIndex;
                    changeValueBySteps(selectorIndexOffset);
                }
                break;
            default:
                break;
        }
        return true;
    }

    @Override
    public void scrollBy(int x, int y) {
        if (y == 0) {
            return;
        }
        mCurFirstPosition += y;
        if (!mWheelData.isCycle() && y > 0 && mWheelData.getSelectIndex() == 0) {
            // Do not cycle, the last stop rolling down
            if (mCurFirstPosition >= mTextPadding) {
                mCurFirstPosition =  0;
                if (!mOverScroller.isFinished() && !mIsDragging) {
                    mOverScroller.abortAnimation();
                }
            }
        } else if (!mWheelData.isCycle() && y < 0 && mWheelData.getSelectIndex() == mWheelData.count() - 1) {
            //Do not cycle, the first stop up rolling
            if (mCurFirstPosition  <= -mTextPadding) {
                mCurFirstPosition =  0;
                if (!mOverScroller.isFinished() && !mIsDragging) {
                    mOverScroller.abortAnimation();
                }
            }
        } else {
            String oldValue = mWheelData.getSelectValue();
            while (mCurFirstPosition  <= -mTextPadding) {
                mCurFirstPosition += mItemHeight;
                mWheelData.next();
                if (!mWheelData.isCycle() && mWheelData.getSelectIndex() == mWheelData.count() - 1) {
                    break;
                }
            }

            while (mCurFirstPosition  >= mTextPadding) {
                mCurFirstPosition -= mItemHeight;
                mWheelData.previous();
                if (!mWheelData.isCycle() && mWheelData.getSelectIndex() == 0) {
                    break;
                }
            }
            if (!oldValue.equals(mWheelData.getSelectValue())) {
                if (mOnValueChangeListener != null) {
                    mOnValueChangeListener.onValueChange(this, oldValue, mWheelData.getSelectValue());
                }
            }
        }


    }


    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mOverScroller.computeScrollOffset()) {
            //inertia roll
            int x = mOverScroller.getCurrX();
            int y = mOverScroller.getCurrY();
            if (mPreviousScrollerY == 0) {
                mPreviousScrollerY = mOverScroller.getStartY();
            }
            scrollBy(x, y - mPreviousScrollerY);
            mPreviousScrollerY = y;
            invalidate();
        } else {
            if (!mIsDragging) {
                //When stopped, automatically scrolls to the nearest option position
                mPreviousScrollerY = 0;
                int deltaY = -mCurFirstPosition;
                if (Math.abs(deltaY) >= mItemHeight >> 1) {
                    if (deltaY > 0) {
                        deltaY -= mItemHeight;
                    } else {
                        deltaY += mItemHeight;
                    }
                }
                if (deltaY != 0) {
                    mOverScroller.startScroll(getScrollX(), getScrollY(), 0, deltaY, 800);
                    postInvalidateOnAnimation();
                }
                onScrollStateChange(OnScrollListener.SCROLL_STATE_IDLE);
            }
        }
    }

    /**
     * Roller state change
     */
    private void onScrollStateChange(int scrollState) {
        if (mScrollState == scrollState) {
            return;
        }
        mScrollState = scrollState;
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollStateChange(this, scrollState);
        }
    }

    private void changeValueBySteps(int steps) {
        mPreviousScrollerY = 0;
        mOverScroller.startScroll(0, 0, 0, -mItemHeight * steps, SNAP_SCROLL_DURATION);
        invalidate();
    }

    private void scrollTo(int position) {
        if (mWheelData.getSelectIndex() == position) {
            return;
        }
        mWheelData.setSelectIndex(position);
        invalidate();
    }

    /**
     * Called when value change
     */
    public void setOnValueChangedListener(OnValueChangeListener onValueChangeListener) {
        mOnValueChangeListener = onValueChangeListener;
    }

    /**
     * Called when scrolling
     */
    public void setOnScrollListener(OnScrollListener onScrollListener) {
        mOnScrollListener = onScrollListener;
    }

    /**
     * Scroll to index position
     */
    public void smoothScrollTo(int position) {
        changeValueBySteps(position - mWheelData.getSelectIndex());
    }
    /**
     * Slide to target smoothly
     */
    public void smoothScrollToValue(String value) {
        smoothScrollTo(mWheelData.index(value));
    }

    /**
     * Slide to target
     */
    public void scrollToValue(String value) {
        scrollTo(mWheelData.index(value));
    }

    /**
     * Set unselected font color
     */
    public void setUnselectedTextColor(int resourceId) {
        mUnSelectedTextColor = resourceId;
    }


    private int lastStart, lastEnd;
    public void setRange(final int start,final int end) {
        if (this.lastStart == start && this.lastEnd == end){
            //range unchanged, skip directly
            return;
        }
        this.lastStart = start;
        this.lastEnd = end;
        mWheelData.setAdapter(new AbstractWheelAdapter() {
            @Override
            public int count() {
                return end - start +1;
            }

            @Override
            public String getValue(int index) {
                return (start + index)+"";
            }

            @Override
            public String maxWidthItem() {
                return end+"";
            }

            @Override
            public int index(String value) {
                try {
                    int n =Integer.parseInt(value);
                    return n - start;
                }catch (NumberFormatException e){
                    e.printStackTrace();
                    return 0;
                }


            }
        });

        float newTextWidth =  mTextPaint.measureText(mWheelData.getMaxItem());
        if (newTextWidth > mTextWidth){
            //option data text width becomes larger, refresh size becomes smaller
            requestLayout();
        }
        invalidate();
    }

    private String[] lastItems;
    public void setLastItems(@NonNull final String... items) {
        if (Arrays.equals(this.lastItems, items)){
            //range unchanged, skip directly
            return;
        }
        this.lastItems = items;
        mWheelData.setAdapter(new AbstractWheelAdapter() {
            @Override
            public int count() {
                return items.length;
            }

            @Override
            public String getValue(int index) {
                return items[index];
            }

            @Override
            public String maxWidthItem() {
                String max = "";
                for (String item : items) {
                    if (max.length() < item.length()){
                        max = item;
                    }
                }
                return max;
            }

            @Override
            public int index(String value) {
                for (int i = 0; i < items.length; i++) {
                    if (value.equals(items[i])){
                        return i;
                    }
                }
                return 0;
            }
        });
        invalidate();
    }

    public void setAdapter(AbstractWheelAdapter adapter){
        mWheelData.setAdapter(adapter);
        invalidate();
    }

    public void setValue(int value) {
        setValue(value + "");
    }

    public void setValue(String value) {
        mWheelData.setSelectIndex(mWheelData.index(value));
        invalidate();
    }

    public String getValue() {
        return mWheelData.getSelectValue();
    }

    public void setLeftText(String leftText) {
        this.mLeftText = leftText;
        invalidate();
    }

    public void setRightText(String rightText) {
        this.mRightText = rightText;
        invalidate();
    }

    /**
     * Interface to listen for changes of the current value.
     */
    public interface OnValueChangeListener {

        /**
         * Called upon a change of the current value.
         *
         * @param picker The NumberPicker associated with this listener.
         * @param oldVal The previous value.
         * @param newVal The new value.
         */
        void onValueChange(WheelPicker picker, String oldVal, String newVal);
    }

    interface OnScrollListener {
        /**
         * The view is not scrolling.
         */
        int SCROLL_STATE_IDLE = 0;

        /**
         * The user is scrolling using touch, and his finger is still on the screen.
         */
        int SCROLL_STATE_TOUCH_SCROLL = 1;

        /**
         * The user had previously been scrolling using touch and performed a fling.
         */
        int SCROLL_STATE_FLING = 2;

        /**
         * Callback invoked while the number picker scroll state has changed.
         *
         * @param picker      The view whose scroll state is being reported.
         * @param scrollState The current scroll state. One of
         *                    [.SCROLL_STATE_IDLE],
         *                    [.SCROLL_STATE_TOUCH_SCROLL] or
         *                    [.SCROLL_STATE_IDLE].
         */
        void onScrollStateChange(WheelPicker picker, int scrollState);

    }

    private static class WheelData {
        private AbstractWheelAdapter adapter;
        private int selectIndex;
        private boolean cycle;

        void setAdapter(AbstractWheelAdapter adapter){
            this.adapter = adapter;
        }

        /**
         * The index of value
         */
        int index(String value) {
            if (adapter != null){
                return adapter.index(value);
            }
            return 0;
        }

        /**
         * Item count
         */
        int count() {
            if (adapter == null){
                return 0;
            }
            return adapter.count();
        }

        /**
         * The value of the index in picker
         */
        String getItem(int index) {
            int count = count();
            if (count == 0){
                return null;
            }
            if (index < 0) {
                index = count + index % count;
            } else if (index > count - 1) {
                index = (index + 1) % count - 1;
            }
            return adapter.getValue(index);
        }

        /**
         * Get max width item
         */
        String getMaxItem() {
            if (adapter == null){
                return null;
            }
            return adapter.maxWidthItem();
        }

        /**
         * Deal valid index
         */
        private void validIndex(){
            int count = count();
            if (selectIndex >= count) {
                selectIndex = count - 1;
            } else if (selectIndex < 0) {
                selectIndex = 0;
            }
        }

        int getSelectIndex() {
            validIndex();
            return selectIndex;
        }

        String getSelectValue() {
            validIndex();
            return adapter.getValue(selectIndex);
        }

        void setSelectIndex(int selectIndex) {
            this.selectIndex = selectIndex;
            validIndex();
        }

        boolean isCycle() {
            return cycle;
        }

        void setCycle(boolean cycle) {
            this.cycle = cycle;
        }

        void previous() {
            int count = count();
            selectIndex--;
            if (!cycle) {
                if (selectIndex < 0) {
                    selectIndex = 0;
                }
            } else {
                if (selectIndex < 0) {
                    selectIndex = count + selectIndex % count;
                }
            }


        }

        void next() {
            int count = count();
            selectIndex++;
            if (!cycle) {
                if (selectIndex > count - 1) {
                    selectIndex = count - 1;
                }
            } else {
                if (selectIndex > count - 1) {
                    selectIndex = (selectIndex + 1) % count - 1;
                }
            }
        }

    }

    /**
     * Wheel data adapter
     *
     * @author Janson
     * @date 2020/4/15 14:43
     */
    public abstract static class AbstractWheelAdapter{
        /**
         * Items count
         */
         public abstract int count();

        /**
         * Get value by index.  index starts with 0
         */
         public abstract String getValue(int index);

        /**
         * Get the longest string
         */
         public abstract String maxWidthItem();

        /**
         * Get index by value. index starts with 0
         */
         public abstract int index(String value);
    }

}
