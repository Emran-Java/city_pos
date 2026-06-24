package acquire.base.widget;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import androidx.annotation.NonNull;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A {@link BaseAdapter} with {@link ViewBinding}.
 * <p><hr><b>e.g.:</b></p>
 * <pre>
 *     public class TestAdapter extends BaseBindingListAdapter<TestItemBinding> {
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
 *          public int getCount() {
 *              return items.size();
 *          }
 *          public Object getItem(int position) {
 *              return items.get(position);
 *          }
 *      }
 * </pre>
 *
 * @author Janson
 * @date 2023/11/20 16:39
 */
public abstract class BaseBindingListAdapter<VB extends ViewBinding> extends BaseAdapter {
    private Method inflateMethod;

    /**
     * Bind data item
     *
     * @param itemBinding {@link ViewBinding} of item
     * @param position    item position
     */
    protected abstract void bindItemData(VB itemBinding, int position);

    /**
     * Get {@link ViewBinding} class
     */
    protected abstract Class<VB> getViewBindingClass();


    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BindingHolder<VB> holder;
        // 如果convertView为空，说明是第一次加载，需要创建ViewHolder
        if (convertView == null) {
            Class<VB> clz = getViewBindingClass();
            try {
                if (inflateMethod == null) {
                    inflateMethod = clz.getMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
                }
                VB binding = (VB) inflateMethod.invoke(clz, LayoutInflater.from(parent.getContext()), parent, false);
                convertView = binding.getRoot();
                holder = new BindingHolder(binding);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
            convertView.setTag(holder);
        } else {
            holder = (BindingHolder) convertView.getTag();
        }
        bindItemData(holder.itemBinding,position);
        return convertView;
    }

    static class BindingHolder<VB extends ViewBinding>  {
        private final VB itemBinding;

        private BindingHolder(@NonNull VB itemBinding) {
            this.itemBinding = itemBinding;
        }
    }

}