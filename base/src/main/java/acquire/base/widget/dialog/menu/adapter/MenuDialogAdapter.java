package acquire.base.widget.dialog.menu.adapter;

import java.util.List;

import acquire.base.databinding.BaseMenuDialogItemBinding;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.menu.MenuDialog;


/**
 * The menu adapter of {@link MenuDialog}
 *
 * @author Janson
 * @date 2018/11/19 10:57
 */
public class MenuDialogAdapter extends BaseBindingRecyclerAdapter<BaseMenuDialogItemBinding> {
    private final List<String> mItems;
    private int selected;

    public MenuDialogAdapter(List<String> items, int defalut) {
        this.mItems = items;
        this.selected = defalut;
    }

    public int getSelected() {
        return selected;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    protected void bindItemData(BaseMenuDialogItemBinding itemBinding, int position) {
        String item = mItems.get(position);
        if (item != null) {
            itemBinding.radio.setText(item);
            itemBinding.radio.setChecked(selected == position);
            itemBinding.getRoot().setOnClickListener(v -> {
                if (selected == position){
                    return;
                }
                notifyItemChanged(selected);
                selected = position;
                notifyItemChanged(selected);
            });
        }
    }
}
