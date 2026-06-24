package acquire.base.utils.thread;

/**
 * A locker for {@link Thread}
 *
 * @author Janson
 * @date 2019/6/2 20:12
 */
public class Locker<T> {
    private boolean awake;
    private final Object lock = new Object();
    private T t;

    public Locker() {
    }

    public Locker(T t) {
        this.t = t;
    }

    public void setResult(T t) {
        this.t = t;
    }

    public T getResult() {
        return t;
    }

    /**
     * wait for the thread locker to be notified
     *
     * @param millis waiting timeout in milliseconds.
     */
    public void waiting(int millis) {
        synchronized (lock) {
            if (!awake) {
                try {
                    lock.wait(millis);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * wait for the thread locker to be notified
     */
    public void waiting() {
        waiting(0);
    }

    /**
     * Notify the thread locker
     */
    public void wakeUp() {
        synchronized (lock) {
            awake = true;
            lock.notify();
        }
    }

    boolean isAwaked() {
        return awake;
    }
}
