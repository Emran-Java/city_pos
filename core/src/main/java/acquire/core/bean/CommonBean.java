package acquire.core.bean;

/**
 * A common bean
 *
 * @author Janson
 * @date 2019/1/30 0:17
 */
public class CommonBean<T> {
    private T t;

    public CommonBean() {
    }

    public CommonBean(T t) {
        this.t = t;
    }

    public T getValue() {
        return t;
    }

    public void setValue(T t) {
        this.t = t;
    }
}
