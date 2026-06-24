package acquire.base.chain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.ThreadPool;


/**
 * A chain used to execute a serial of task in a single thread.
 * <p>e.g.</p>
 * <pre>
 *     chain.next(new InterceptorA())
 *             .next(new InterceptorB())
 *              ...
 *             .proceed(isSucc -> {});
 * </pre>
 *
 * @author Janson
 * @date 2019/12/24 9:43
 */
public class Chain<T> {

    protected T param;
    /**
     * A queue of interceptors.
     */
    protected Queue<Interceptor<T>> interceptors;


    public Chain(T param) {
        this.param = param;
    }

    /**
     * Set interceptors list
     */
    public void setInterceptors(@NonNull List<Interceptor<T>> list) {
        interceptors = new LinkedList<>();
        for (Interceptor<T> item : list) {
            if (item != null) {
                interceptors.offer(item);
            }
        }
    }

    /**
     * Add one interceptor
     */
    public Chain<T> next(@Nullable Interceptor<T> interceptor) {
        if (interceptors == null) {
            interceptors = new LinkedList<>();
        }
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
        return this;
    }

    /**
     * Proceed interceptors one by one
     *
     * @param callback chain result callback
     */
    public void proceed(final Interceptor.Callback callback) {
        //execute a interceptor
        ThreadPool.execute(() -> {
            if (interceptors == null) {
                throw new RuntimeException("proceed chain fail because interceptors are null");
            }
            //If interceptors is empty, proceed completion.
            if (interceptors.isEmpty()) {
                callback.onResult(true);
                return;
            }
            final Interceptor<T> interceptor = interceptors.poll();
            if (interceptor == null) {
                throw new RuntimeException("Execute chain fail! Intercetprot is null.");
            }
            LoggerUtils.d("Interceptor >> " + interceptor.getClass().getSimpleName());
            //set param to this interceptor
            interceptor.init(param);
            interceptor.intercept(new Interceptor.Callback() {
                private boolean done;

                @Override
                public void onResult(boolean isSucc) {
                    if (done) {
                        LoggerUtils.e("Repeat interceptor: " + interceptor.getClass().getSimpleName());
                        return;
                    }
                    done = true;
                    if (isSucc) {
                        proceed(callback);
                    } else {
                        //clear all interceptors
                        interceptors.clear();
                        ThreadPool.execute(() ->
                                callback.onResult(false));
                    }
                }
            });
        });
    }


}
