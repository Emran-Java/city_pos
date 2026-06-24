package acquire.base.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import java.lang.ref.WeakReference;

import acquire.base.BaseApplication;


/**
 * Toast utils
 *
 * @author Janson
 * @date 2019/5/13 10:25
 */
public class ToastUtils {
    /**
     * Toast instance
     */
    private static WeakReference<Toast> weakToast;

    /**
     * Show message
     *
     * @param resId 文本资源
     */
    public static void showToast(int resId, Object... formatArgs) {
        showToast(BaseApplication.getAppString(resId,formatArgs));
    }
    /**
     * Show message
     */
    public static void showToast(String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _showToast(msg,Toast.LENGTH_SHORT);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> _showToast(msg,Toast.LENGTH_SHORT));
        }
    }

    /**
     * Show message long time
     */
    public static void showLongToast(int resId) {
        showLongToast(BaseApplication.getAppString(resId));
    }
    /**
     * Show message long time
     */
    public static void showLongToast(String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            _showToast(msg,Toast.LENGTH_LONG);
        } else {
            new Handler(Looper.getMainLooper()).post(() -> _showToast(msg,Toast.LENGTH_LONG));
        }
    }



    /**
     * Show toast
     * @see Toast#LENGTH_SHORT
     * @see Toast#LENGTH_LONG
     */
    private static void _showToast(String msg,int duration) {
        if (msg == null){
            return;
        }
        if (weakToast != null && weakToast.get() != null ) {
            weakToast.get().cancel();
        }
        Toast toast = Toast.makeText(BaseApplication.getAppContext(), msg, duration);
        toast.show();
        weakToast = new WeakReference<>(toast);
    }
}
