package acquire.app.brac.ui.menu;

import android.content.Intent;
import android.graphics.Color;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.zztl.pos.city.R;
import com.zztl.pos.city.databinding.AppMenuUcbItemBinding;

import acquire.base.activity.bottom_sheet.MessageBottomSheet;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.CoreContent;
import acquire.app.fragment.main.SubMenuFragment;
import acquire.app.fragment.main.menu.MenuItem;
import acquire.base.ActivityStackManager;
import acquire.base.activity.BaseActivity;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.TransActivity;
import acquire.core.constant.IntentParamKeyContent;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;


/**
 * A {@link RecyclerView.Adapter} to display {@link MenuItem}
 *
 * @author Janson
 * @date 2022/5/12 17:44
 */
public class MenuAdapter extends BaseBindingRecyclerAdapter<AppMenuUcbItemBinding> {
    private final List<MenuItem> items;
    private final BaseActivity activity;
    public final static MenuItem FILL_PLACE_ITEM = new MenuItem(null, -1, -1);

    public MenuAdapter(BaseActivity activity, List<MenuItem> items) {
        this.activity = activity;
        this.items = items;
    }

    @Override
    protected void bindItemData(@NonNull AppMenuUcbItemBinding itemBinding, int position) {
        MenuItem item = items.get(position);
        if (FILL_PLACE_ITEM == item) {
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
        itemBinding.tvName.setText(new StringBuilder().append(position + 1).append(".").append(item.getName()));

        if (item.getName() != null && !item.getName().isEmpty()) {
            itemBinding.cvIconBg.setVisibility(View.VISIBLE);
        } else {
            itemBinding.cvIconBg.setVisibility(View.GONE);
        }

        //click listener
        itemBinding.getRoot().setOnClickListener(v -> {
            if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_IS_OPERATOR_AVAILABLE, false)) {
                showBsMessage(item);

            } else {
                actionItemListener(item);
            }

        });

    }

    private void actionItemListener(MenuItem item) {
        if (ActivityStackManager.getTopActivity() instanceof TransActivity) {
            return;
        }
        if (item.getOnClickItemListener() != null) {
            //Custom click listenter
            item.getOnClickItemListener().onItemClick(activity);
        } else if (item.getSubItems() != null && !item.getSubItems().isEmpty()) {
            //Secondary menu
            activity.mSupportDelegate.switchContent(SubMenuFragment.newInstance(item.getName(), item.getSubItems()));
        } else {
            //start transaction
            Intent intent = new Intent(activity, TransActivity.class);
            intent.putExtra(TransTag.TRANS_TYPE, item.getTransType());
            intent.putExtra(IntentParamKeyContent.TRANS_REPORT_MENU, CoreContent.REPORT_MENU);

            intent.putExtra(IntentParamKeyContent.TRANS_TITLE_TEXT, item.getName());

            ActivityCompat.startActivity(activity, intent, null);
//                ActivityStackManager.getTopActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            ActivityStackManager.getTopActivity().overridePendingTransition(R.anim.pop_in, R.anim.pop_out);
        }
    }

    private void showBsMessage(MenuItem item) {
        MessageBottomSheet sheet =
                MessageBottomSheet.newInstance(
                        "Connectivity exception",
                        R.drawable.ic_connectivity_issue,
                        true,
                        false,
                        "OK",
                        "Yes"
                );

        sheet.setActionListener(new MessageBottomSheet.BottomSheetActionListener() {
            @Override
            public void onLeftButtonClick() {
                if (item.getTransType().equalsIgnoreCase(TransType.TRANS_REPRINT_RECEIPT)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_REPRINT_RECEIPT_MENU)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_REPRINT_LAST_RECEIPT)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_SETTINGS)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_ABOUT)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_HELP_CENTER)
                        || item.getTransType().equalsIgnoreCase(TransType.TRANS_REPORTS_PRINT)
                ) {
                    actionItemListener(item);
                }
            }

            @Override
            public void onRightButtonClick() {

            }
        });

        sheet.show(
                activity.getSupportFragmentManager(),
                "message_sheet"
        );
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

}
