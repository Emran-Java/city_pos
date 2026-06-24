package acquire.base.widget;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import acquire.base.widget.BaseBindingRecyclerAdapter.BindingHolder;

/**
 * A base {@link RecyclerView.Adapter} with {@link ViewBinding}.
 * <p><hr><b>e.g.:</b></p>
 * <pre>
 *     public class TestAdapter extends BaseBindingRecyclerAdapter<TestItemBinding> {
 *          private final List<DataItem> items;
 *
 *          public TestAdapter( List<DataItem> items) {
 *              this.items = items;
 *          }
 *          protected void bindItemData(TestItemBinding itemBinding, int position) {
 *              DataItem item = items.get(position);
 *              //set view
 *              itemBinding.tvName.setText(item.getName());
 *              ...
 *              //set item click listener
 *              itemBinding.getRoot().setOnClickListener(v -> {
 *                  //do your event
 *                  ....
 *              });
 *
 *          }
 *          protected Class<TestItemBinding> getViewBindingClass() {
 *              //Fixed collocation
 *              return TestItemBinding.class;
 *          }
 *          public int getItemCount() {
 *              return items.size();
 *          }
 *      }
 * </pre>
 *
 * @author Janson
 * @date 2020/9/3 11:39
 */
@Keep
public abstract class BaseBindingRecyclerAdapter<VB extends ViewBinding> extends RecyclerView.Adapter<BindingHolder<VB>> {
    private Method inflateMethod;
    private Class<VB> clz;
    /**
     * Bind data item
     *
     * @param itemBinding {@link ViewBinding} of item
     * @param position    item position
     */
    protected abstract void bindItemData(VB itemBinding, int position);


    @NonNull
    @Override
    public BindingHolder<VB> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        try {
            if (clz == null){
                Class<?> cls = getClass();
                Type type = cls.getGenericSuperclass();
                while (type != null && cls != null && !(type instanceof ParameterizedType)) {
                    cls = getClass().getSuperclass();
                    if (cls != null) {
                        type = cls.getGenericSuperclass();
                    }

                }
                //clz = (Class<VB>) ((ParameterizedType) type).getActualTypeArguments()[0];
                assert type != null;
                assert type instanceof ParameterizedType;
                Type actualType = ((ParameterizedType) type)
                        .getActualTypeArguments()[0];

                if (actualType instanceof Class) {
                    clz = (Class<VB>) actualType;
                } else if (actualType instanceof ParameterizedType) {
                    clz = (Class<VB>) ((ParameterizedType) actualType).getRawType();
                } else {
                    throw new RuntimeException(
                            "Cannot resolve ViewBinding generic type."
                    );
                }
            }

            if (inflateMethod == null) {
                inflateMethod = clz.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            }
            VB binding = (VB) inflateMethod.invoke(clz, LayoutInflater.from(parent.getContext()), parent, false);
            return new BindingHolder<>(binding);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BindingHolder<VB> holder, int position) {
        bindItemData(holder.itemBinding, position);
    }

    static class BindingHolder<VB extends ViewBinding> extends RecyclerView.ViewHolder {
        private final VB itemBinding;

        private BindingHolder(@NonNull VB itemBinding) {
            super(itemBinding.getRoot());
            this.itemBinding = itemBinding;
        }
    }

}