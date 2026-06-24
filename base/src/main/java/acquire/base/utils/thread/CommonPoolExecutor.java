package acquire.base.utils.thread;



import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import acquire.base.utils.LoggerUtils;


/**
 * A common thread pool executor
 *
 * @author Janson
 * @date 2018/11/23 14:57
 */
public class CommonPoolExecutor extends ThreadPoolExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_THREAD_COUNT = CPU_COUNT + 1;
    private static final int MAX_THREAD_COUNT = Integer.MAX_VALUE;
    /**
     * Keep alive tiem in seconds
     */
    private static final long ALIVE_TIME = 60L;

    /**
     * Create a unbounded unbuffered thread pool.
     *
     * @param taskGroup Thread pool name
     * @return created thread pool
     */
    public static CommonPoolExecutor newCachePool(String taskGroup) {
        return new CommonPoolExecutor(CORE_THREAD_COUNT, MAX_THREAD_COUNT, ALIVE_TIME, TimeUnit.SECONDS, new SynchronousQueue<>(), new CommonThreadFactory(taskGroup));
    }

    /**
     * Create a single thread pool.
     *
     * @param taskGroup Thread pool name
     * @return created single thread pool
     */
    public static CommonPoolExecutor newSinglePool(String taskGroup) {
        return new CommonPoolExecutor(1, 1, 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new CommonThreadFactory(taskGroup));
    }

    private CommonPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue, ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, (r, executor) -> LoggerUtils.e("Task rejected, too many task!"));
    }

    /**
     * Handle something after thread executed.
     *
     * @param r the runnable that has completed.
     * @param t the exception that caused termination, or null
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t == null && r instanceof Future<?>) {
            try {
                ((Future<?>) r).get();
            } catch (CancellationException ce) {
                t = ce;
            } catch (ExecutionException ee) {
                t = ee.getCause();
            } catch (InterruptedException ie) {
                // ignore/reset
                Thread.currentThread().interrupt();
            }
        }
        if (t != null) {
            LoggerUtils.e("Running task appeared exception! Thread ["+Thread.currentThread().getName()+"], because ["+t.getMessage()+"]\n" +formatStackTrace(t.getStackTrace()));
        }
    }

    private String formatStackTrace( StackTraceElement[] stackTrace) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTrace) {
            sb.append("    at ").append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
