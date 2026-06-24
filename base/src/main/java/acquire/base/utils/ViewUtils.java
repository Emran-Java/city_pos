package acquire.base.utils;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;


/**
 * View utils
 *
 * @author CB
 * @date 2015/5/7
 */
public class ViewUtils {

    /**
     * Shake view
     */
    public static void shakeAnimatie(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationX", 0, 30, 0, -30, 0).setDuration(350);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    public static void rotationZ(View view) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationZ", 0f, 170f);
        animator.setDuration(500);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }



    private static long lastClickTime;

    /**
     * Returns true if you click too fast
     */
    public static boolean isFastClick() {
        return isFastClick(500);
    }

    /**
     * Returns true if you click too fast
     *
     * @param millisecond the time range of quick click
     */
    public static boolean isFastClick(int millisecond) {
        long time = SystemClock.elapsedRealtime();
        if (time - lastClickTime < millisecond) {
            return true;
        }
        lastClickTime = time;
        return false;
    }


    /**
     * focus a view
     */
    public static void setFocus(@NonNull View view) {
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
    }


    public static Bitmap getBitmapFromDrawable(@NonNull Context context, @DrawableRes int drawableId) {
        Drawable drawable = ContextCompat.getDrawable(context, drawableId);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof VectorDrawable || drawable instanceof VectorDrawableCompat) {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } else {
            throw new IllegalArgumentException("unsupported drawable type");
        }
    }

    /**
     * Add click and zoom animation to the view
     */
    public static void setScaleTouch(final View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    view.setScaleX(0.95f);
                    view.setScaleY(0.95f);
                    break;
                case MotionEvent.ACTION_UP:
                    view.setScaleX(1);
                    view.setScaleY(1);
                    view.performClick();
                    break;
                default:
                    break;
            }
            return false;
        });
    }

    /**
     * create a pressed selector drawable
     */
    public static Drawable createPressedSelector(Drawable normal, Drawable pressed) {
        StateListDrawable drawable = new StateListDrawable();
        // pressed
        drawable.addState(new int[]{android.R.attr.state_pressed}, pressed);
        // normal
        drawable.addState(new int[]{}, normal);
        return drawable;
    }

    /**
     * darker color
     */
    public static int getDarkerColor(int color){
        float[] hsv = new float[3];
        // convert to hsv
        Color.colorToHSV(color, hsv);
        // make darker\
        // more saturation
        hsv[1] = hsv[1] + 0.1f;
        // less brightness
        hsv[2] = hsv[2] - 0.1f;
        return Color.HSVToColor(hsv);
    }

    /**
     * brigther color
     */
    public int getBrighterColor(int color){
        float[] hsv = new float[3];
        // convert to hsv
        Color.colorToHSV(color, hsv);
        // less saturation
        hsv[1] = hsv[1] - 0.1f;
        // more brightness
        hsv[2] = hsv[2] + 0.1f;
        return Color.HSVToColor(hsv);
    }
}
