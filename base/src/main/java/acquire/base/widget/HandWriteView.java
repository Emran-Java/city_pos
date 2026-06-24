package acquire.base.widget;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import acquire.base.R;


/**
 *  A board used to draw signatures
 *
 * @author Janson
 * @date 2021/3/25 16:06
 */
public class HandWriteView extends View {

    
    private static final float PAINT_SIZE = 3.7f;
    /**
     * Signatrue paint
     */
    private final Paint mPaint;
    /**
     * Paint of background
     */
    private final Paint mBgPaint;
    private final Paint mCodePaint;
    /**
     * The canvas of drawing signature
     */
    private final Canvas mCacheCanvas;
    private Bitmap mCachebBmp;
    private final Rect mCodeRect;
    /**
     * Signature path
     */
    private final Path mPath;
    /**
     * Start coordinate of finger
     */
    private float mStartX, mStartY;
    /**
     * Current coordinate of finger
     */
    private float mClickX, mClickY;
    /**
     * Feature code that is drew on tha background
     */
    private String mBackgroundText = null;
    /**
     * Pixel count
     */
    private long mPxCount;
    /**
     * Last pixel count
     */
    private long mLastPxCount;
    /**
     * Rotate canvas
     */
    private boolean mRotate;

    private final static int MIN_VALID_PX = 7;


    public HandWriteView(Context context) {
        this(context,null);
    }

    public HandWriteView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (attrs != null){
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.HandWriteView);
            mRotate = typedArray.getBoolean(R.styleable.HandWriteView_rotate, false);
            typedArray.recycle();
        }
        //signature paint
        float scale = context.getResources().getDisplayMetrics().density;
        float paintSize = PAINT_SIZE * scale;
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(paintSize);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.BLACK);
        mPaint.setDither(true);
        mPaint.setFilterBitmap(true);
        mPaint.setSubpixelText(true);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mBgPaint = new Paint(Paint.DITHER_FLAG);
        mPath = new Path();
        mCodePaint = new Paint();
        mCodePaint.setColor(Color.GREEN);
        mCodePaint.setTextSize(90.0f);
        mCodeRect = new Rect();
        mCacheCanvas = new Canvas();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int mWidth = measureWidth(widthMeasureSpec);
        int mHeight = measureHeight(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
        init(mWidth, mHeight);
    }

    private void init(int width, int height) {
        requestFocus();

        if (width == 0 || height == 0){
            return;
        }
        if (mPxCount == 0){
            //Not on the drawing board
            //create canvas
            mCachebBmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCacheCanvas.setBitmap(mCachebBmp);
            drawBackgroundText();
        }

    }

    private int measureHeight(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }

        return result;
    }

    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        int result = 500;
        if (specMode == MeasureSpec.AT_MOST) {
            result = specSize;
        } else if (specMode == MeasureSpec.EXACTLY) {
            result = specSize;
        }
        return result;
    }

    /**
     * Draw background text
     */
    private void drawBackgroundText() {
        if (mBackgroundText == null|| mBackgroundText.isEmpty()) {
            return;
        }
        if (mCachebBmp == null || mCacheCanvas == null) {
            return;
        }
        mCacheCanvas.save();
        float width;
        float height;
        if (mRotate) {
            //feature code rotate 90
            width = mCachebBmp.getHeight();
            height = mCachebBmp.getWidth();
            mCacheCanvas.rotate(90);
            mCacheCanvas.translate(0, -mCachebBmp.getWidth());
        } else {
            width = mCachebBmp.getWidth();
            height = mCachebBmp.getHeight();
        }
        mCacheCanvas.drawText(mBackgroundText, (width - mCodeRect.width()) / 2, (height + mCodeRect.height()) / 2, mCodePaint);
        mCacheCanvas.restore();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //draw signature
        canvas.drawBitmap(mCachebBmp, 0, 0, mBgPaint);
        canvas.drawPath(mPath, mPaint);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mStartX = event.getX();
        mStartY = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastPxCount = mPxCount;
                mPath.reset();
                mPath.moveTo(mStartX, mStartY);
                mClickX = mStartX;
                mClickY = mStartY;
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mPath.quadTo(mClickX, mClickY, (mClickX + mStartX) / 2, (mClickY + mStartY) / 2);
                mPxCount++;
                mClickX = mStartX;
                mClickY = mStartY;
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                if (mPxCount - mLastPxCount <= MIN_VALID_PX) {
                    mPxCount = mLastPxCount;
                }
                //path to canvas
                mPath.lineTo(mClickX, mClickY);
                mCacheCanvas.drawPath(mPath, mPaint);
                mPath.reset();
                invalidate();
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * Create a {@link Bitmap} by canvas
     *
     * @param dstWidth bitmap width
     * @param dstHeight bitmap height
     * @return {@link Bitmap}
     */
    public Bitmap getBitmap(int dstWidth,int dstHeight) {
        //draw white canvas
        Bitmap outBmp = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(outBmp);
        c.drawColor(Color.WHITE);
        //drawn signature scale on the desired bitmap
        if (mRotate) {
            //since the original signature has been rotated 90 degrees, it needs to be restored
            Bitmap validBitmap = Bitmap.createScaledBitmap(mCachebBmp, dstHeight, dstWidth, true);
            c.rotate(-90);
            c.translate(-dstHeight, 0);
            c.drawBitmap(validBitmap, 0, 0, new Paint());
        } else {
            Bitmap validBitmap = Bitmap.createScaledBitmap(mCachebBmp, dstWidth, dstHeight, true);
            c.drawBitmap(validBitmap, 0, 0, new Paint());
        }
        return outBmp;
    }

    public Bitmap getBitmap() {
        return getBitmap(mCachebBmp.getWidth(),mCachebBmp.getHeight());
    }

    /**
     * Clear canvas
     */
    public void clear() {
        mPxCount = 0;
        mPath.reset();
        mPath.moveTo(mStartX, mStartY);
        if (mCacheCanvas != null) {
            mCachebBmp.eraseColor(Color.TRANSPARENT);
            mCacheCanvas.drawColor(Color.TRANSPARENT);
            drawBackgroundText();
            invalidate();
        }
    }

    public boolean isInValid() {
        return mPxCount < MIN_VALID_PX;
    }

    public boolean isEmpty(){
        return mPxCount == 0;
    }


    /**
     * Set background text
     */
    public void setBackgroundText(String backgroundText) {
        if (backgroundText == null || backgroundText.isEmpty()){
            return;
        }
        //measure signature code  size
        this.mCodePaint.getTextBounds(mBackgroundText, 0, mBackgroundText.length(), mCodeRect);
    }


}
