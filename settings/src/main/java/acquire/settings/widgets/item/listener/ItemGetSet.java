package acquire.settings.widgets.item.listener;

/**
 * @author Janson
 * @date 2024/3/8 15:26
 */
public interface ItemGetSet<T> {
    T getValue();
    void setValue(T value);
}
