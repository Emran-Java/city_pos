package acquire.app.fragment.main.menu;

import android.content.Intent;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.zztl.pos.ucb.databinding.AppMenuItemBinding;
import acquire.app.fragment.main.SubMenuFragment;
import acquire.base.ActivityStackManager;
import acquire.base.activity.BaseActivity;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.TransActivity;
import acquire.core.constant.TransTag;


/**
 * A {@link RecyclerView.Adapter} to display {@link MenuItem}
 *
 * @author Janson
 * @date 2022/5/12 17:44
 */
public class MainMenuAdapter_bkp extends BaseBindingRecyclerAdapter<AppMenuItemBinding> {
    private final List<MenuItem> items;
    private final BaseActivity activity;
    public final static MenuItem FILL_PLACE_ITEM = new MenuItem(null,-1,-1);

    public MainMenuAdapter_bkp(BaseActivity activity, List<MenuItem> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    protected void bindItemData(@NonNull AppMenuItemBinding itemBinding, int position) {
        MenuItem item = items.get(position);
        if (FILL_PLACE_ITEM == item){
            //fill item
            itemBinding.cvIconBg.setCardBackgroundColor(Color.TRANSPARENT);
            itemBinding.icon.setBackgroundResource(0);
            itemBinding.tvName.setText(null);
            itemBinding.getRoot().setEnabled(false);
            return;
        }
        //background
        if (item.getColorId() != 0) {
            itemBinding.cvIconBg.setCardBackgroundColor(ContextCompat.getColor(activity, item.getColorId()));
        }
        //icon
        itemBinding.icon.setImageResource(item.getIcon());
        //name
        itemBinding.tvName.setText(item.getName());
      /*  if(itemBinding.tvIndex!=null){
            itemBinding.tvIndex.setText(String.valueOf(position+1));
        }*/
        //click listener
        itemBinding.getRoot().setOnClickListener(v -> {
            if (ActivityStackManager.getTopActivity() instanceof TransActivity) {
                return;
            }
            if (item.getOnClickItemListener() != null){
                //Custom click listenter
                item.getOnClickItemListener().onItemClick(activity);
            }else if (item.getSubItems() != null && !item.getSubItems().isEmpty()){
                //Secondary menu
                activity.mSupportDelegate.switchContent(SubMenuFragment.newInstance(item.getName(),item.getSubItems()));
            }else{
                //start transaction
                Intent intent = new Intent(activity, TransActivity.class);
                intent.putExtra(TransTag.TRANS_TYPE, item.getTransType());
                ActivityCompat.startActivity(activity, intent, null);
            }
        });

    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
