package acquire.settings.fragment.brac_setting;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import acquire.base.BaseApplication;
import acquire.base.activity.bottom_sheet.MessageBottomSheet;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.AppUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.SensitiveGuard;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.constant.ParamsConst;
import acquire.core.fragment.common.DataLoader;
import acquire.core.fragment.common.report.AllDetailsReportFragment;
import acquire.database.repository.ReversalDataRepository;
import acquire.sdk.ServiceHelper;
import acquire.sdk.emv.EmvProvider;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.contents.SettingMenuContentList;
import acquire.settings.models.BracSettingMenuItemModel;
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
public class BracMainSettingFragment extends BaseSettingFragment {

    private SimpleCallback mCallback;

    private static boolean log;

    @NonNull
    public static BracMainSettingFragment newInstance(SimpleCallback callback) {
        BracMainSettingFragment fragment = new BracMainSettingFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_title);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();

        Fragment manageFragment = SettingMenuContentList.getManageFragment("Manage");

        items.add(
                new TextItem.Builder(mActivity)
                        .setTitle("Manage")
                        .setOnClickListener(v ->
                                mSupportDelegate.switchContent(manageFragment)
                        )
                        .create()
        );

        items.add(
                new TextItem.Builder(mActivity)
                        .setTitle("Communication")
                        .setOnClickListener(v ->
                                mSupportDelegate.switchContent(SettingMenuContentList.getCommunicationFragment("Communication"))
                        )
                        .create()
        );

        items.add(
                new TextItem.Builder(mActivity)
                        .setTitle("Clear reversal")
                        .setOnClickListener(v ->
                                {
                                    // mSupportDelegate.switchContent(manageFragment)
                                    removeReversalData();
                                }

                        )
                        .create()
        );

        items.add(
                new TextItem.Builder(mActivity)
                        .setTitle("Session KCV")
                        .setOnClickListener(v ->{
                            //TODO: Setting first layer "Session KCV" -> want develop another fragment
                                }
                        )
                        .create()
        );

/*
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
                .create());*/
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }

    private void removeReversalData() {

        MessageBottomSheet sheet =
                MessageBottomSheet.newInstance(
                        "Do you want to remove Reversal records?",
                        R.drawable.ic_delete_record,
                        true,
                        true,
                        "NO",
                        "Yes"
                );

        sheet.setActionListener(new MessageBottomSheet.BottomSheetActionListener() {
            @Override
            public void onLeftButtonClick() {

            }

            @Override
            public void onRightButtonClick() {

                DataLoader.getInstance().show(
                        requireContext(),
                        "Removing...",
                        "Delete Reversal records."
                );

                BaseApplication.SINGLE_EXECUTOR.execute(() -> {
                    ReversalDataRepository recordRepository = new ReversalDataRepository();
                    // ArrayList<Record> dummyRecords = LoadMenuData.loadDummyRecords(this);
                    boolean result = recordRepository.deleteAllReversalData(); //deleteAll
                    LoggerUtils.d("newCall BracMainSettingFragment"+ "result" + result);

                    try {
                        long delay = ThreadLocalRandom.current().nextLong(1000, 3001);
                        LoggerUtils.d("newCall BracMainSettingFragment"+ "Waiting " + delay + " ms");
                        Thread.sleep(delay);
                    } catch (InterruptedException e) {
                        LoggerUtils.e("newCall BracMainSettingFragment exception : " + e.getMessage());
                        Thread.currentThread().interrupt();
                    }

                    mActivity.runOnUiThread(() -> {
                        DataLoader.getInstance().dismiss();
                    });
                });

            }
        });

        sheet.show(
                mActivity.getSupportFragmentManager(),
                "message_sheet"
        );

    }
/*    private Fragment getManageFragment() {
        ArrayList<BracSettingMenuItemModel> menuList = new ArrayList<>();

        menuList.add(new BracSettingMenuItemModel("Merchant Info"));
        menuList.add(new BracSettingMenuItemModel("ON-OFF"));
        menuList.add(new BracSettingMenuItemModel("Trans Params"));
        menuList.add(new BracSettingMenuItemModel("Key Management"));
        menuList.add(new BracSettingMenuItemModel("Clear"));
        menuList.add(new BracSettingMenuItemModel("System"));
        menuList.add(new BracSettingMenuItemModel("Print"));
        menuList.add(new BracSettingMenuItemModel("Other"));

        Fragment manageFragment = new BracSettingManageFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(BracSettingManageFragment.BUNDLE_ARG_KEY_MENU_LIST, menuList);
        bundle.putString(BracSettingManageFragment.BUNDLE_ARG_KEY_TITLE_BAR_TITLE, "Manage");

        manageFragment.setArguments(bundle);
        return manageFragment;
    }*/
}
