package acquire.base.widget.dialog.check.adapter;

import java.util.ArrayList;
import java.util.List;

import acquire.base.databinding.BaseCheckDialogItemBinding;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.message.MessageDialog;


/**
 * The menu adapter of {@link MessageDialog}
 *
 * @author Janson
 * @date 2018/11/19 10:57
 */
public class CheckDialogAdapter extends BaseBindingRecyclerAdapter<BaseCheckDialogItemBinding> {
    private final List<String> mItems;
    private final List<Integer> selects = new ArrayList<>();

    public CheckDialogAdapter(List<String> items) {
        this.mItems = items;
    }


    public int[] getSelected() {
        int[] indexs = new int[selects.size()];
        for (int i = 0; i < selects.size(); i++) {
            indexs[i] = selects.get(i);
        }
        return indexs;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    protected void bindItemData(BaseCheckDialogItemBinding itemBinding, int position) {
        String item = mItems.get(position);
        if (item != null) {
            itemBinding.radio.setText(item);
            itemBinding.radio.setChecked(selects.contains(position));
            itemBinding.getRoot().setOnClickListener(v -> {
                if (selects.contains(position)){
                    selects.remove(Integer.valueOf(position));
                }else{
                    selects.add(position);
                }
                notifyItemChanged(position);
            });
        }
    }
}
