package acquire.base.utils.thread;




import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;

import acquire.base.utils.LoggerUtils;


/**
 * A schedule thread pool executor
 *
 * @author Janson
 * @date 2018/11/23 14:57
 */
public class CommonScheduledPoolExecutor extends ScheduledThreadPoolExecutor {

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_THREAD_COUNT = CPU_COUNT + 1;

    /**
     * Create {@link CommonScheduledPoolExecutor}.
     *
     * @param taskGroup Thread pool name
     * @return created schedule thread pool
     */
    public static CommonScheduledPoolExecutor newInstance(String taskGroup) {
        return new CommonScheduledPoolExecutor(CORE_THREAD_COUNT, new CommonThreadFactory(taskGroup));
    }

    private CommonScheduledPoolExecutor(int corePoolSize, ThreadFactory threadFactory) {
        super(corePoolSize, threadFactory, (r, executor) -> LoggerUtils.e("Task rejected, too many task!"));
    }

    /**
     * Handle something after thread executed.
     *
     * @param r the runnable that has completed
     * @param t the exception that caused termination, or null if
     */
    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (t != null) {
            LoggerUtils.e("Running task appeared exception! Thread ["+Thread.currentThread().getName()+"], because ["+t.getMessage()+"]" +formatStackTrace(t.getStackTrace()));
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
