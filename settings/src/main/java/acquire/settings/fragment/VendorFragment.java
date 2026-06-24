package acquire.settings.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.AppUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.SensitiveGuard;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.constant.ParamsConst;
import acquire.database.repository.ReversalDataRepository;
import acquire.sdk.ServiceHelper;
import acquire.sdk.emv.EmvProvider;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;

import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;
import acquire.settings.widgets.item.listener.ItemGetSet;

/**
 * A vendor {@link Fragment}. It's a hide interface,usually,the merchant doesn't it.
 *
 * @author Janson
 * @date 2019/2/18 15:47
 */
public class VendorFragment extends BaseSettingFragment {

    private SimpleCallback mCallback;

    private static boolean log;

    @NonNull
    public static VendorFragment newInstance(SimpleCallback callback) {
        VendorFragment fragment = new VendorFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_vendor_title);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Reset password
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_vendor_item_reset_password)
                .setOnClickListener(v ->
                        new MessageDialog.Builder(mActivity)
                                .setMessage(R.string.settings_vendor_item_reset_password)
                                .setConfirmButton(v1 -> {
                                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_SECURITY, "000000");
                                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_ADMIN, "000000");
                                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN, "000000");
                                    ToastUtils.showToast(R.string.settings_reset_password_success);
                                })
                                .setCancelButton(v1 -> {
                                })
                                .show()
                )
                .create());
        //init app params
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_vendor_item_reset_pos)
                .setOnClickListener(v ->
                        new MessageDialog.Builder(mActivity)
                                .setMessage(R.string.settings_vendor_item_reset_pos)
                                .setConfirmButton(dialog -> {
                                    ParamsUtils.clear();
                                    new MessageDialog.Builder(mActivity)
                                            .setMessage(R.string.settings_reset_pos_success_dialog_message)
                                            .setConfirmButton(R.string.settings_restart_button, dialog1 -> AppUtils.reStartApp(mActivity))
                                            .show();

                                })
                                .setCancelButton(dialog -> {
                                })
                                .show()
                )
                .create());
        //Clear reversal
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_vendor_flag_item_clear_reversal)
                .setOnClickListener(v ->
                        new MessageDialog.Builder(mActivity)
                                .setMessage(R.string.settings_vendor_flag_item_clear_reversal)
                                .setConfirmButton(dialog -> {
                                    new ReversalDataRepository().deleteAllReversalData();
                                    ToastUtils.showToast(R.string.settings_clear_reversal_success);
                                })
                                .setCancelButton(dialog -> {
                                })
                                .show()

                )
                .create());
        //open deep log
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_vendor_deep_log)
                .setIGetSet(new ItemGetSet<Boolean>() {
                    @Override
                    public Boolean getValue() {
                        return log;
                    }

                    @Override
                    public void setValue(Boolean value) {
                        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)) {
                            EmvProvider.getInstance().openExtEmvl2Log(value);
                        } else {
                            EmvProvider.getInstance().openEmvl2Log(value);
                        }
                        ServiceHelper.getInstance().openNatvieLog(value);
                        log = value;
                    }
                })
                .create());
        //show sensitive log
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_vendor_show_sensitive_log)
                .setIGetSet(new ItemGetSet<Boolean>() {
                    @Override
                    public Boolean getValue() {
                        return SensitiveGuard.isShowSensitiveData();
                    }

                    @Override
                    public void setValue(Boolean value) {
                        SensitiveGuard.setShowSensitiveData(value);
                    }
                })
                .create());
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }

}
