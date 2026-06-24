package acquire.base.widget.dialog.menu.adapter;

import java.util.LinkedHashMap;
import java.util.Map;

import acquire.base.databinding.BaseMenuIconDialogItemBinding;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.menu.MenuDialog;
import acquire.base.widget.dialog.menu.MenuIconDialog;


/**
 * The menu adapter of {@link MenuIconDialog}
 *
 * @author Janson
 * @date 2023/1/11 16:29
 */
public class MenuIconDialogAdapter extends BaseBindingRecyclerAdapter<BaseMenuIconDialogItemBinding> {
    private final LinkedHashMap<String,Integer> mItems;
    private final MenuDialog.OnClickMenuListener onClickMenuListener;
    public MenuIconDialogAdapter(LinkedHashMap<String,Integer> items,MenuDialog.OnClickMenuListener onClickMenuListener) {
        this.mItems = items;
        this.onClickMenuListener = onClickMenuListener;
    }



    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    protected void bindItemData(BaseMenuIconDialogItemBinding itemBinding, int position) {
        String name = null;
        int drawableId = 0;
        int count =0;
        for (Map.Entry<String, Integer> entry : mItems.entrySet()) {
            if (count == position){
                name = entry.getKey();
                drawableId = entry.getValue();
                break;
            }
            count++;
        }
        if (name != null) {
            itemBinding.tvName.setText(name);
            itemBinding.ivIcon.setImageResource(drawableId);
            itemBinding.getRoot().setOnClickListener(v -> onClickMenuListener.onSelect(position));
        }
    }
}
