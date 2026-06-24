package acquire.settings.fragment.pinpad;


import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.CommonPoolExecutor;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.base.widget.dialog.progress.ProgressDialog;
import acquire.core.constant.Characters;
import acquire.core.constant.ParamsConst;
import acquire.core.tools.SelfCheckHelper;
import acquire.sdk.ConnectMode;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.FlyKeyHelper;
import acquire.sdk.dock.BaseDock;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.EditTextItem;
import acquire.settings.widgets.item.MenuDialogItem;
import acquire.settings.widgets.item.MenuDialogItem.MenuBean;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;

/**
 * Set external PIN pad
 *
 * @author Janson
 * @date 2021/11/25 10:44
 */
public class SettingPinpadFragment extends BaseSettingFragment {
    private final Executor EXECUTOR = CommonPoolExecutor.newSinglePool("SettingPinpadFragment");

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_pinpad);
    }

    private boolean isDock;

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //PIN pad timeout
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_ACCESSIBILITY)){
            //accessibility PIN pad
            items.add(new EditTextItem.Builder(mActivity)
                    .setTitle(R.string.settings_pinpad_item_accessibility_timeout)
                    .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_ACCESSIBILITY_TIMEOUT)
                    .setDigits(Characters.NUMBER)
                    .setInputMinLen(3)
                    .setInputMaxLen(4)
                    .create());
        }else{
            //normal PIN pad
            items.add(new EditTextItem.Builder(mActivity)
                    .setTitle(R.string.settings_pinpad_item_timeout)
                    .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_TIMEOUT)
                    .setDigits(Characters.NUMBER)
                    .setInputMinLen(2)
                    .setInputMaxLen(3)
                    .create());
        }
        //RNIB PIN pad
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_pinpad_item_accessibility)
                .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_ACCESSIBILITY)
                .setCheckChangeListener((buttonView, isChecked) -> refreshItems())
                .create());
        //FLY key
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_pinpad_item_fly_key)
                .setMessage(R.string.settings_pinpad_fly_key_summary)
                .setOnClickListener(v -> {
                    boolean externalPinpad = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL);
                    if (externalPinpad && !ExtServiceHelper.getInstance().isInit()) {
                        ToastUtils.showToast(R.string.settings_pinpad_fly_key_connect_external_pin_pad);
                        return;
                    }
                    flykey(externalPinpad);
                })
                .create());
        //External PIN pad
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_pinpad_item_enable)
                .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)
                .setCheckChangeListener((buttonView, isChecked) -> {
                    EXECUTOR.execute(() -> ExtServiceHelper.getInstance().destroy());
                    refreshItems();
                })
                .create());
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)) {
            //External connect
            List<MenuBean> menu = new ArrayList<>();
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_serial_port)
                    , ConnectMode.SERIAL_PORT));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_usb)
                    , ConnectMode.USB));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_dock_serial_port)
                    , ConnectMode.DOCK_SERIAL_PORT));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_dock_usb1)
                    , ConnectMode.DOCK_USB1));
            menu.add(new MenuBean(getString(R.string.settings_connect_mode_dock_usb2)
                    , ConnectMode.DOCK_USB2));
            items.add(new MenuDialogItem.Builder(mActivity)
                    .setTitle(R.string.settings_pinpad_item_external_connect_mode)
                    .setParamKey(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL_CONNECT_MODE)
                    .setParamBean(menu)
                    .setOnChangeListener(index -> {
                        int connectMode = (int) menu.get(index).getValue();
                        if (isDock != isDockMode(connectMode)) {
                            refreshItems();
                        }
                        EXECUTOR.execute(() -> ExtServiceHelper.getInstance().notifyConnect(connectMode));
                    })
                    .create());
            int mode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL_CONNECT_MODE);
            isDock = isDockMode(mode);
            if (isDock) {
                //Test dock
                items.add(new TextItem.Builder(mActivity)
                        .setTitle(R.string.settings_dock_test)
                        .setOnClickListener(v ->
                                ThreadPool.execute(() -> {
                                    if (new BaseDock().init()) {
                                        ToastUtils.showToast(R.string.settings_dock_serivce_test_success);
                                    } else {
                                        ToastUtils.showToast(R.string.settings_dock_serivce_test_failed);
                                    }
                                })
                        )
                        .create());
                //Dock set
                items.add(new TextItem.Builder(mActivity)
                        .setTitle(R.string.settings_dock_set)
                        .setOnClickListener(v -> {
                            if (!new BaseDock().startSettings()) {
                                ToastUtils.showToast(R.string.settings_dock_serivce_not_exist);
                            }
                        })
                        .create());
            }
            //Connect external PIN pad
            items.add(new TextItem.Builder(mActivity)
                    .setTitle(R.string.settings_pinpad_item_external_connect)
                    .setOnClickListener(v ->
                            new ProgressDialog.Builder(mActivity)
                                    .setContent(R.string.settings_pinpad_item_external_connect)
                                    .setShowListener(dialog -> {
                                        //Ping External PIN Pad
                                        EXECUTOR.execute(() -> {
                                            int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL_CONNECT_MODE);
                                            ExtServiceHelper.getInstance().destroy();
                                            boolean isConnect = ExtServiceHelper.getInstance().init(mActivity, connectMode);
                                            if (isConnect) {
                                                LoggerUtils.d("Re-init device");
                                                SelfCheckHelper.initDevice(mActivity);
                                                ToastUtils.showToast(R.string.settings_pinpad_external_test_success);
                                            } else {
                                                ToastUtils.showToast(R.string.settings_pinpad_external_test_failed);
                                            }

                                            ThreadPool.postDelayOnMain(dialog::dismiss, 500);
                                        });

                                    })
                                    .show()
                    )
                    .create());
        }
        return items;
    }

    private void flykey(boolean externalPinpad) {
        new ProgressDialog.Builder(mActivity)
                .setContent(R.string.settings_pinpad_fly_key_sending)
                .setShowListener(dialog ->
                        FlyKeyHelper.downloadMsterKey(mActivity, externalPinpad, new FlyKeyHelper.FlyKeyListener() {
                            @Override
                            public void onSuccess(@NonNull int[] indexes) {
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    if (indexes.length == 0) {
                                        ToastUtils.showToast(R.string.settings_pinpad_fly_key_no_key);
                                    } else {
                                        ToastUtils.showToast(R.string.settings_pinpad_fly_key_success);
                                    }
                                });

                            }

                            @Override
                            public void onFailed(FlyKeyHelper.FlyKeyErrorBean errorBean) {
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    new MessageDialog.Builder(mActivity)
                                            .setMessage(errorBean.getMessage())
                                            .setConfirmButton(dialog1 -> {
                                            })
                                            .show();
                                });
                            }


                            @Override
                            public void onException(Exception e) {
                                LoggerUtils.e("Fly key execute failed!", e);
                                mActivity.runOnUiThread(() -> {
                                    dialog.dismiss();
                                    ToastUtils.showToast(R.string.settings_pinpad_fly_key_exception);
                                });

                            }
                        })
                )
                .show();
    }

    private boolean isDockMode(int connectMode) {
        return ConnectMode.DOCK_USB1 == connectMode || ConnectMode.DOCK_USB2 == connectMode || ConnectMode.DOCK_SERIAL_PORT == connectMode;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}
