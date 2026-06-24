package acquire.base.utils.thread;


import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import acquire.base.utils.LoggerUtils;


/**
 * A factory that creates the thread.
 *
 * @author Janson
 * @date 2018/12/10 9:31
 */
public class CommonThreadFactory implements ThreadFactory {
    private static final AtomicInteger POOL_NUMBER = new AtomicInteger(1);

    private final AtomicInteger threadNumber = new AtomicInteger(1);
    private final ThreadGroup group;
    private final String namePrefix;

    public CommonThreadFactory(String taskName) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        namePrefix = taskName + " task pool No." + POOL_NUMBER.getAndIncrement() + ", thread No.";
    }

    @Override
    public Thread newThread( Runnable runnable) {
        String threadName = namePrefix + threadNumber.getAndIncrement();
        LoggerUtils.d("Thread production, name is ["+threadName+"].");
        Thread thread = new Thread(group, runnable, threadName, 0);
        if (thread.isDaemon()) {
            //set as non background thread
            thread.setDaemon(false);
        }
        if (thread.getPriority() != Thread.NORM_PRIORITY) {
            // normal priority
            thread.setPriority(Thread.NORM_PRIORITY);
        }

        // catch thread exception
        thread.setUncaughtExceptionHandler((t, e) -> LoggerUtils.e("Running task appeared exception! Thread ["+t.getName()+"], because ["+e.getMessage()+"]."));
        return thread;
    }
}