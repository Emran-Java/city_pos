package acquire.settings.fragment.print;


import android.graphics.Bitmap;
import android.graphics.Paint;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.bluetooth.BluetoothCore;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PrintSize;
import acquire.core.esc.EscPrinter;
import acquire.core.native_usb.NativeUsbCore;
import acquire.sdk.ConnectMode;
import acquire.sdk.dock.BaseDock;
import acquire.sdk.printer.BPrinter;
import acquire.sdk.printer.BitmapDraw;
import acquire.sdk.printer.IPrinter;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.EditTextItem;
import acquire.settings.widgets.item.MenuDialogItem;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that configures the printer.
 *
 * @author Janson
 * @date 2019/2/13 9:47
 */
public class SettingPrintFragment extends BaseSettingFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_print);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Support receipt
        items.add(new TextItem.Builder(mActivity)
                .setTitle(getString(R.string.settings_receipt_support))
                .setOnClickListener(v -> mSupportDelegate.switchContent(new ReceiptSupportFragment()))
                .create());
//        List<MenuDialogItem.MenuBean> menu = new ArrayList<>();
//        menu.add(new MenuDialogItem.MenuBean("1", 1));
//        menu.add(new MenuDialogItem.MenuBean("2", 2));
//        menu.add(new MenuDialogItem.MenuBean("3", 3));
//        items.add(new MenuDialogItem.Builder(mActivity)
//                .setTitle(R.string.settings_print_item_count)
//                .setParamKey(ParamsConst.PARAMS_KEY_PRINT_COUNT)
//                .setParamBean(menu)
//                .create());
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_print_item_remarks)
                .setParamKey(ParamsConst.PARAMS_KEY_PRINT_REMARKS)
                .setInputMaxLen(99)
                .create());
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_print_item_external)
                .setParamKey(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)
                .setCheckChangeListener((buttonView, isChecked) -> refreshItems())
                .create());
        if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)){
            List<MenuDialogItem.MenuBean> ports = new ArrayList<>();
            ports.add(new MenuDialogItem.MenuBean(getString(R.string.settings_connect_mode_serial_port)
                    , ConnectMode.SERIAL_PORT));
            ports.add(new MenuDialogItem.MenuBean(getString(R.string.settings_connect_mode_usb)
                    , ConnectMode.USB));
            ports.add(new MenuDialogItem.MenuBean(getString(R.string.settings_connect_mode_bluetooth)
                    , ConnectMode.BLUETOOTH));
            ports.add(new MenuDialogItem.MenuBean(getString(R.string.settings_connect_mode_dock_serial_port)
                    , ConnectMode.DOCK_SERIAL_PORT));
            ports.add(new MenuDialogItem.MenuBean(getString(R.string.settings_connect_mode_dock_usb1)
                    , ConnectMode.DOCK_USB1));
            ports.add(new MenuDialogItem.MenuBean(getString(R.string.settings_connect_mode_dock_usb2)
                    , ConnectMode.DOCK_USB2));
            items.add(new MenuDialogItem.Builder(mActivity)
                    .setTitle(R.string.settings_print_item_connect_mode)
                    .setParamKey(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE)
                    .setParamBean(ports)
                    .setOnChangeListener(index -> refreshItems())
                    .create());
            int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE);
            switch (connectMode){
                case ConnectMode.SERIAL_PORT:
                case ConnectMode.DOCK_SERIAL_PORT:
                    //Serial port baud rate
                    List<MenuDialogItem.MenuBean> menuBeans = new ArrayList<>();
                    menuBeans.add(new MenuDialogItem.MenuBean("115200", 115200));
                    menuBeans.add(new MenuDialogItem.MenuBean("57600", 57600));
                    menuBeans.add(new MenuDialogItem.MenuBean("38400", 38400));
                    menuBeans.add(new MenuDialogItem.MenuBean("19200", 19200));
                    menuBeans.add(new MenuDialogItem.MenuBean("9600", 9600));
                    items.add(new MenuDialogItem.Builder(mActivity)
                            .setTitle(R.string.settings_print_item_serial_baudrate)
                            .setParamKey(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_SERIAL_BAUDRATE)
                            .setParamBean(menuBeans)
                            .create());
                    break;
                case ConnectMode.BLUETOOTH:
                    items.add(new TextItem.Builder(mActivity)
                            .setTitle(R.string.settings_print_item_bluetooth_setting)
                            .setOnClickListener(v->{
                                BluetoothCore core = new BluetoothCore();
                                core.startConfig(mActivity);
                            })
                            .create());
                    break;
                case ConnectMode.USB:
                    items.add(new TextItem.Builder(mActivity)
                            .setTitle(R.string.settings_print_item_usb_setting)
                            .setOnClickListener(v->{
                                NativeUsbCore core = new NativeUsbCore();
                                core.startConfig(mActivity);
                            })
                            .create());
                    break;
                default:
                    break;
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
        }
        items.add(new TextItem.Builder(mActivity)
                        .setTitle(R.string.settings_print_item_test)
                        .setOnClickListener(v->{
                            BitmapDraw draw = new BitmapDraw();
                            draw.text("print test1", PrintSize.NORMAL,true, Paint.Align.CENTER);
                            draw.text("print test2", PrintSize.NORMAL,true, Paint.Align.CENTER);
                            draw.text("print test3", PrintSize.NORMAL,true, Paint.Align.CENTER);
                            Bitmap bitmap = draw.getBitmap();
                            IPrinter printer;
                            if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL)) {
                                int mode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE);
                                int baudRata = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PRINT_EXTERNAL_SERIAL_BAUDRATE);
                                printer = new EscPrinter(mode, baudRata);
                            }else{
                                printer = new BPrinter();
                            }
                            printer.print(bitmap, true,new IPrinter.PrintCallback() {
                                @Override
                                public void onFinish() {
                                    ToastUtils.showToast(R.string.settings_print_test_success);
                                }
                                @Override
                                public void onError(String message) {
                                    ToastUtils.showToast(message);
                                }
                                @Override
                                public void onOutOfPaper() {
                                    ToastUtils.showToast(R.string.sdk_helper_printer_status_outof_paper);
                                }
                            });
                        })
                .create());
        //Not yet implemented
//        items.add(new SwitchItem.Builder(mActivity)
//                .setTitle(R.string.settings_print_item_fly_receipt)
//                .setMessage(R.string.settings_print_item_fly_receipt_message)
//                .setParamKey(ParamsConst.PARAMS_KEY_TOMS_FLY_RECEIPT)
//                .create());
        return items;
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}
