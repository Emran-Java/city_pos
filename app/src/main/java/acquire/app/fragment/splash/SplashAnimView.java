package acquire.app.fragment.splash;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.annotation.Nullable;

import acquire.base.utils.AppUtils;
import acquire.base.utils.LoggerUtils;


/**
 * Splash animation view
 */
public class SplashAnimView extends View {
    /**
     * Image alpha time
     */
    private static final int ALPHA_LOGO_DURATION = 700;
    /**
     * Text offset time
     */
    private static final int OFFSET_DURATION = 1000;
    /**
     * Canvas gradient time
     */
    private static final int LOGO_GRADIENT_DURATION = 300;

    private final SparseArray<String> mLogoTexts = new SparseArray<>();
    private final SparseArray<PointF> mQuietPoints = new SparseArray<>();
    private final SparseArray<PointF> mRadonPoints = new SparseArray<>();
    /**
     * 1. Image Alpha aiimation
     */
    private ValueAnimator mImageAlphaAnimator;
    /**
     * 1.Text Offset animation.
     */
    private ValueAnimator mTextOffsetAnimator;
    /**
     * 2.Canvas Gradient animation
     */
    private ValueAnimator mGradientAnimator;
    private final Paint mImagePaint;
    private final Paint mPaint;
    private float mImageAlphaAnimProgress;
    private float mTextOffsetAnimProgress;
    private boolean isOffsetAnimEnd;
    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix;
    private int mMatrixTranslate;
    private int mWidth, mHeight;
    /**
     * text gradient
     */
    private final static boolean GRADIENT_TEXT = true;
    private final static boolean AUTO_PLAY = true;
    private final Drawable mLogoImage;
    private AnimListener mListener;
    private  static int TEXT_MARGIN_TOP = 60;
    private final float mTextSize;
    private final float mTextPadding;
    private final int mTextColor;
    private final int mTextGradientColor;
    private final int mLogoSize;

    public SplashAnimView(Context context) {
        this(context, null);
    }

    public SplashAnimView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SplashAnimView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTextSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 16,
                getResources().getDisplayMetrics());
        TEXT_MARGIN_TOP = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                30,
                getResources().getDisplayMetrics()
        );
        mTextPadding = 4;
        TypedArray themeArray = getContext().getTheme().obtainStyledAttributes(new int[] {android.R.attr.colorPrimary});
        try {
            mTextColor = themeArray.getColor(0, 0);
        }finally {
            themeArray.recycle();
        }
        mTextGradientColor = Color.alpha(mTextColor);
        //logo
        mLogoImage = AppUtils.getAppIcon(context);
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        if (displayMetrics.heightPixels > displayMetrics.widthPixels) {
            //port screen
            mLogoSize = displayMetrics.widthPixels/3;
        } else {
            //land screen
            mLogoSize = displayMetrics.heightPixels/3;
        }

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setFakeBoldText(true);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(mTextColor);

        mImagePaint = new Paint();
        mImagePaint.setAntiAlias(true);
        mImagePaint.setStyle(Paint.Style.STROKE);
        //String to char array
        if (mLogoTexts.size() > 0) {
            mLogoTexts.clear();
        }
        String packageName = context.getPackageName();
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            String logoName = context.getResources().getString(labelRes);
            for (int i = 0; i < logoName.length(); i++) {
                char c = logoName.charAt(i);
                String s = String.valueOf(c);
                mLogoTexts.put(i, s);
            }
        } catch (PackageManager.NameNotFoundException e) {
            LoggerUtils.e("Not found app "+packageName+" !",e);
        }
        initAlphaAnimation();
        initOffsetAnimation();
    }


    /**
     * Init offset animation
     */
    private void initOffsetAnimation() {
        if (mTextOffsetAnimator != null) {
            return;
        }
        //Set Animation AccelerateDecelerateInterpolator
        mTextOffsetAnimator = ValueAnimator.ofFloat(0, 1);
        mTextOffsetAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mTextOffsetAnimator.addUpdateListener(animation -> {
            mTextOffsetAnimProgress = (float) animation.getAnimatedValue();
            if (mQuietPoints.size() <= 0 || mRadonPoints.size() <= 0) {
                return;
            }
            invalidate();
        });
        //Set listener to animation end
        mTextOffsetAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (GRADIENT_TEXT) {
                    isOffsetAnimEnd = true;
                    mPaint.setShader(mLinearGradient);
                    if (mGradientAnimator != null) {
                        mGradientAnimator.start();
                    } else {
                        if (mListener != null) {
                            mListener.onAnimEnd();
                            mListener = null;
                        }
                    }
                }
            }
        });
        mTextOffsetAnimator.setDuration(OFFSET_DURATION);
    }

    /**
     * Init logo alpha
     */
    private void initAlphaAnimation() {
        if (mImageAlphaAnimator != null) {
            return;
        }
        //Set transparency acceleration interpolator ,from 0 to 1
        mImageAlphaAnimator = ValueAnimator.ofFloat(0, 1);
        mImageAlphaAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        mImageAlphaAnimator.addUpdateListener(animation -> {
            mImageAlphaAnimProgress = (float) animation.getAnimatedValue();
            invalidate();
        });
        mImageAlphaAnimator.setDuration(ALPHA_LOGO_DURATION);

    }

    /**
     * Init text gradient animation
     */
    private void initGradientAnimation(int width) {
        if (width == 0 || mGradientAnimator != null) {
            return;
        }
        mGradientAnimator = ValueAnimator.ofInt(0, 2 * width);
        mGradientAnimator.addUpdateListener(animation -> {
            mMatrixTranslate = (int) animation.getAnimatedValue();
            invalidate();
        });
        //Animation LinearGradient
        mLinearGradient = new LinearGradient(-width, 0, 0, 0, new int[]{mTextColor, mTextGradientColor, mTextColor},
                new float[]{0, 0.5f, 1}, Shader.TileMode.CLAMP);
        mGradientMatrix = new Matrix();
        //Set animation duration
        mGradientAnimator.setDuration(LOGO_GRADIENT_DURATION);
        //Post animation over to listener
        mGradientAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (mListener != null) {
                    mListener.onAnimEnd();
                    mListener = null;
                }
            }
        });

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        //auto play animation
        if (getVisibility() == VISIBLE && AUTO_PLAY) {
            mTextOffsetAnimator.start();
            mImageAlphaAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        // release animation
        if (mImageAlphaAnimator != null && mImageAlphaAnimator.isRunning()) {
            mImageAlphaAnimator.cancel();
        }
        if (mTextOffsetAnimator != null && mTextOffsetAnimator.isRunning()) {
            mTextOffsetAnimator.cancel();
        }
        if (mGradientAnimator != null && mGradientAnimator.isRunning()) {
            mGradientAnimator.cancel();
        }
        super.onDetachedFromWindow();
    }

    /**
     * Start animation
     */
    public void startAnimation() {
        if (getVisibility() == VISIBLE) {
            if (mImageAlphaAnimator.isRunning()) {
                mImageAlphaAnimator.cancel();
            }
            if (mTextOffsetAnimator.isRunning()) {
                mTextOffsetAnimator.cancel();
            }
            isOffsetAnimEnd = false;
            mImageAlphaAnimator.start();
            mTextOffsetAnimator.start();
        } else {
            LoggerUtils.e( "The view is not visible, not to play the animation.");
        }
    }

    /**
     * Stop animation
     */
    public void endAnimation() {
        if (mImageAlphaAnimator.isRunning()) {
            mImageAlphaAnimator.end();
        }
        if (mTextOffsetAnimator.isRunning()) {
            mTextOffsetAnimator.end();
        }
        if (mGradientAnimator.isRunning()) {
            mGradientAnimator.end();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mWidth != w || mHeight != h) {
            mWidth = w;
            mHeight = h;
            mRadonPoints.clear();
            initTextCoordinate(w, h);
            mGradientAnimator = null;
            initGradientAnimation(w);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mWidth == 0 || mHeight == 0) {
            mWidth = getMeasuredWidth();
            mHeight = getMeasuredHeight();
            initTextCoordinate(mWidth, mHeight);
            initGradientAnimation(mWidth);
        }
    }

    /**
     * Init text place
     */
    private void initTextCoordinate(int w, int h) {
        if (mRadonPoints.size() > 0) {
            return;
        }
        if (w == 0 || h == 0) {
            return;
        }
        float centerY;
        if (mLogoImage != null) {
            //Logo exist
            centerY = h / 2f + TEXT_MARGIN_TOP + mTextSize;
        } else {
            //Logo not exist
            centerY = h / 2f;
        }
        float totalLength = 0;
        for (int i = 0; i < mLogoTexts.size(); i++) {
            String str = mLogoTexts.get(i);
            float currentLength = mPaint.measureText(str);
            if (i != mLogoTexts.size() - 1) {
                totalLength += currentLength + mTextPadding;
            } else {
                totalLength += currentLength;
            }
        }
        if (totalLength > w) {
            throw new IllegalStateException("LOGO too long");
        }
        float startX = (w - totalLength) / 2;

        if (mQuietPoints.size() > 0) {
            mQuietPoints.clear();
        }
        for (int i = 0; i < mLogoTexts.size(); i++) {
            String str = mLogoTexts.get(i);
            float currentLength = mPaint.measureText(str);
            mQuietPoints.put(i, new PointF(startX, centerY));
            startX += currentLength + mTextPadding;
        }
        for (int i = 0; i < mLogoTexts.size(); i++) {
            mRadonPoints.put(i, new PointF((float) Math.random() * w, (float) Math.random() * h));
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (getVisibility() != View.VISIBLE) {
            return;
        }
        if (mLogoImage != null) {
            mImagePaint.setAlpha((int) Math.min(255, 255 * mImageAlphaAnimProgress));
            Bitmap bm = drawableToBitmap(mLogoImage);
            canvas.drawBitmap(bm, (mWidth - bm.getWidth()) / 2f, mHeight / 2f - bm.getHeight(), mImagePaint);
        }
        if (!isOffsetAnimEnd) {
            mPaint.setAlpha(255);
            for (int i = 0; i < mQuietPoints.size(); i++) {
                PointF quietP = mQuietPoints.get(i);
                PointF radonP = mRadonPoints.get(i);
                float x = radonP.x + (quietP.x - radonP.x) * mTextOffsetAnimProgress;
                float y = radonP.y + (quietP.y - radonP.y) * mTextOffsetAnimProgress;
                canvas.drawText(mLogoTexts.get(i), x, y, mPaint);
            }
        } else {
            for (int i = 0; i < mQuietPoints.size(); i++) {
                PointF quietP = mQuietPoints.get(i);
                canvas.drawText(mLogoTexts.get(i), quietP.x, quietP.y, mPaint);
            }
            mGradientMatrix.setTranslate(mMatrixTranslate, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);
        }
    }


    private int realH,realW;
    private Bitmap drawableToBitmap(Drawable drawable) {
        if (realH == 0 || realW == 0){
            int h = drawable.getIntrinsicHeight();
            int w = drawable.getIntrinsicWidth();
            float rate = (float) h /w;
            if (rate>1){
                h = (int) (mLogoSize*rate);
                w = mLogoSize;
            }else{
                h = mLogoSize;
                w = (int) (mLogoSize/rate);
            }
            int heightMax = getHeight()*3/4;
            int widthMax = getWidth()*3/4;
            if (h>heightMax){
                w = w*heightMax/h;
                h = heightMax;
            }
            if (w>widthMax){
                h = h*widthMax/w;
                w = widthMax;
            }
            realH = h;
            realW = w;
        }

        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(realW, realH, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, realW, realH);
        drawable.draw(canvas);
        return bitmap;
    }


    public void setListener(AnimListener listener) {
        mListener = listener;
    }

    public interface AnimListener {
        void onAnimEnd();

    }
}
