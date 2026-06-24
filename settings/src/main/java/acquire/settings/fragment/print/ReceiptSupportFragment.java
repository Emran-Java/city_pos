package acquire.settings.fragment.print;

import android.content.Intent;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.SwitchItem;

/**
 * A {@link Fragment} that configures receipt support.
 */
public class ReceiptSupportFragment extends BaseSettingFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.settings_receipt_support);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Multi-Receipt Support
        items.add(new SwitchItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_RECEIPT_AUTO_PRINT)
                .setTitle(R.string.settings_receipt_auto_print)
                .setCheckChangeListener((compoundButton, b) -> refreshItems())
                .create());
        if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_RECEIPT_AUTO_PRINT)) {
            //Merchant paper
            items.add(new SwitchItem.Builder(mActivity)
                    .setParamKey(ParamsConst.PARAMS_KEY_RECEIPT_MERCHANT_PAPER)
                    .setTitle(R.string.settings_receipt_support_item_merchant_paper)
                    .create());
            //Customer paper
            items.add(new SwitchItem.Builder(mActivity)
                    .setParamKey(ParamsConst.PARAMS_KEY_RECEIPT_CUSTOMER_PAPER)
                    .setTitle(R.string.settings_receipt_support_item_customer_paper)
                    .create());
            //Fly Receipt
            items.add(new SwitchItem.Builder(mActivity)
                    .setParamKey(ParamsConst.PARAMS_KEY_RECEIPT_FLY_RECEIPT)
                    .setTitle(R.string.settings_receipt_support_item_fly_receipt)
                    .create());
            //EReceipt NPI-Demo
            items.add(new SwitchItem.Builder(mActivity)
                    .setParamKey(ParamsConst.PARAMS_KEY_RECEIPT_E_NPI_DEMO)
                    .setTitle(R.string.settings_receipt_support_item_e_npidemo)
                    .create());
            //EReceipt Pixcel
            items.add(new SwitchItem.Builder(mActivity)
                    .setParamKey(ParamsConst.PARAMS_KEY_RECEIPT_E_PIXCEL)
                    .setTitle(R.string.settings_receipt_support_item_e_pixcel)
                    .create());
        }
        return items;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //send the main ui update broadcast
        Intent intent = new Intent();
        String packageName = mActivity.getPackageName();
        intent.setPackage(packageName);
        intent.setAction(packageName);
        mActivity.sendBroadcast(intent);
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}
