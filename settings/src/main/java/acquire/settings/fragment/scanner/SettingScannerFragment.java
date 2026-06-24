package acquire.settings.fragment.scanner;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.constant.Characters;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.Scanner;
import acquire.sdk.ConnectMode;
import acquire.sdk.device.BDevice;
import acquire.sdk.dock.BaseDock;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.EditTextItem;
import acquire.settings.widgets.item.MenuDialogItem;
import acquire.settings.widgets.item.MenuDialogItem.MenuBean;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that configures the scanner.
 *
 * @author Janson
 * @date 2019/2/14 14:44
 */
public class SettingScannerFragment extends BaseSettingFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_scanner);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //priority scanner
        List<MenuBean> menu = new ArrayList<>();
        menu.add(new MenuBean(getString(R.string.settings_scanner_back_camera), Scanner.BACK_CAMERA));
        menu.add(new MenuBean(getString(R.string.settings_scanner_front_camera), Scanner.FRONT_CAMERA));
        menu.add(new MenuBean(getString(R.string.settings_scanner_external), Scanner.EXTERNAL));
        if (BDevice.supportHardScanner()){
            menu.add(new MenuBean(getString(R.string.settings_scanner_hard), Scanner.HARD_SCANNER));
        }
        items.add(new MenuDialogItem.Builder(mActivity)
                .setTitle(R.string.settings_scanner_item_priority)
                .setParamKey(ParamsConst.PARAMS_KEY_SCAN_PRIORITY_SCANNER)
                .setParamBean(menu)
                .setOnChangeListener(index -> refreshItems())
                .create());
        int scanner = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SCAN_PRIORITY_SCANNER);
        switch (scanner) {
            case Scanner.EXTERNAL:
                //external scanner
                List<MenuBean> ports = new ArrayList<>();
                ports.add(new MenuBean(getString(R.string.settings_connect_mode_serial_port)
                        , ConnectMode.SERIAL_PORT));
                ports.add(new MenuBean(getString(R.string.settings_connect_mode_usb)
                        , ConnectMode.USB));
                ports.add(new MenuBean(getString(R.string.settings_connect_mode_dock_serial_port)
                        , ConnectMode.DOCK_SERIAL_PORT));
                ports.add(new MenuBean(getString(R.string.settings_connect_mode_dock_usb1)
                        , ConnectMode.DOCK_USB1));
                ports.add(new MenuBean(getString(R.string.settings_connect_mode_dock_usb2)
                        , ConnectMode.DOCK_USB2));
                items.add(new MenuDialogItem.Builder(mActivity)
                        .setTitle(R.string.settings_scanner_item_connect_mode)
                        .setParamKey(ParamsConst.PARAMS_KEY_SCAN_EXTERN_CONNECT_MODE)
                        .setParamBean(ports)
                        .setOnChangeListener(index -> refreshItems())
                        .create());
                int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SCAN_EXTERN_CONNECT_MODE);
                if (connectMode == ConnectMode.USB) {
                    //USB host mode
                    items.add(new EditTextItem.Builder(mActivity)
                            .setTitle(R.string.settings_scanner_item_usb_delay)
                            .setParamKey(ParamsConst.PARAMS_KEY_SCAN_EXTERN_USB_WAIT_TIME)
                            .setInputMaxLen(8)
                            .setDigits(Characters.NUMBER)
                            .create());
                } else if (connectMode == ConnectMode.SERIAL_PORT || connectMode == ConnectMode.DOCK_SERIAL_PORT) {
                    //Serial port baud rate
                    List<MenuBean> menuBeans = new ArrayList<>();
                    menuBeans.add(new MenuBean("115200", 115200));
                    menuBeans.add(new MenuBean("57600", 57600));
                    menuBeans.add(new MenuBean("38400", 38400));
                    menuBeans.add(new MenuBean("19200", 19200));
                    menuBeans.add(new MenuBean("9600", 9600));
                    items.add(new MenuDialogItem.Builder(mActivity)
                            .setTitle(R.string.settings_scanner_item_serial_baudrate)
                            .setParamKey(ParamsConst.PARAMS_KEY_SCAN_EXTERN_SERIAL_BAUDRATE)
                            .setParamBean(menuBeans)
                            .create());
                }
                if (connectMode == ConnectMode.DOCK_SERIAL_PORT || connectMode == ConnectMode.DOCK_USB1 || connectMode == ConnectMode.DOCK_USB2) {
                    //Dock
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
                break;
            default:
                break;
        }
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}
