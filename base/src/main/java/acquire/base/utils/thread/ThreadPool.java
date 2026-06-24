package acquire.base.utils.thread;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Thread pool
 *
 * @author Janson
 * @date 2019/4/24 15:18
 */
public class ThreadPool {
    /**
     * Common thread pool
     */
    private final static Executor POOL = CommonPoolExecutor.newCachePool("Acquire");

    /**
     * Schedule thread pool
     */
    private final static ScheduledExecutorService SCHEDULE_POOL = CommonScheduledPoolExecutor.newInstance("Schedule Acquire");

    /**
     * Execute a runnable in a new thread.
     */
    public static void execute(Runnable runnable) {
        POOL.execute(runnable);
    }

    /**
     * Execute a runnable in the main thread.
     */
    public static void postOnMain(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    /**
     * Execute a delayed  runnable in the main thread.
     *
     * @param delayMillis delay time in ms
     */
    public static void postDelayOnMain(Runnable runnable, long delayMillis) {
        new Handler(Looper.getMainLooper()).postDelayed(runnable, delayMillis);
    }

    /**
     * Execute task regularly.
     *
     * @param command     task to be executed
     * @param delayMillis  delay time in ms. start the first task after delayMills
     * @param periodMillis period in ms
     * @return {@link ScheduledFuture} schedule manager
     */
    public static ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long delayMillis, long periodMillis) {
        return SCHEDULE_POOL.scheduleAtFixedRate(command, delayMillis, periodMillis, TimeUnit.MILLISECONDS);
    }

    /**
     * Execute a runnable in a new thread after some milliseconds.
     */
    public static ScheduledFuture<?> executeDelay(Runnable command, long delayMillis) {
        return SCHEDULE_POOL.schedule(command, delayMillis, TimeUnit.MILLISECONDS);
    }

}
