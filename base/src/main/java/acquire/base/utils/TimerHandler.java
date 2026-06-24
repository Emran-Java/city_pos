package acquire.base.utils;

import android.os.Handler;
import android.os.Looper;


/**
 * A handler for timeout manage
 *
 * @author Janson
 * @date 2022/7/25 15:45
 */
public class TimerHandler {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable timeoutRunnable;
    private long timeoutMillis;

    public void startTimeout(long timeoutMillis, Runnable timeoutRunnable) {
        if (timeoutRunnable == null){
            return;
        }
        stopTimeout();
        this.timeoutRunnable = timeoutRunnable;
        this.timeoutMillis = timeoutMillis;
        handler.postDelayed(timeoutRunnable, timeoutMillis);
    }

    public void stopTimeout() {
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable);
        }
    }

    public void resetTimeout() {
        startTimeout(timeoutMillis, timeoutRunnable);
    }

} 
