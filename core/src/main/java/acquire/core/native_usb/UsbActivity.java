package acquire.core.native_usb;


import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acquire.base.activity.BaseActivity;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.LinearItemDecoration;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.databinding.CoreActivityUsbBinding;
import acquire.core.databinding.CoreUsbItemDeviceBinding;


/**
 * USB setting activity
 *
 * @author Janson
 * @date 2023/7/19 8:55
 */
public class UsbActivity extends BaseActivity {
    static final String ACTION_USB_PERMISSION = "com.newland.usb.permission";

    private final UsbPermissionReceiver usbPermissionReceiver = new UsbPermissionReceiver();
    private final UsbDeviceAdapter usbDeviceAdapter = new UsbDeviceAdapter();
    private final List<UsbDevice> usbDevices = new ArrayList<>();
    private int selectVid, selectPid;
    final static String KEY_USB_VID = "USB_VID", KEY_USB_PID = "USB_PID";
    private CoreActivityUsbBinding binding;
    @Override
    public int attachFragmentResId() {
        //unused
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtils.immersedStatusBar(getWindow());
        binding = CoreActivityUsbBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.toolbar.setBackListener(v-> onBackPressed());
        binding.rvDevices.addItemDecoration(new LinearItemDecoration(2,false, ContextCompat.getColor(this,R.color.base_divider)));

        //register broadcast receiver
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbPermissionReceiver, filter);
        fetchUsbDevices(usbDevices);
        selectVid = ParamsUtils.getInt(KEY_USB_VID, -1);
        selectPid = ParamsUtils.getInt(KEY_USB_PID, -1);
        binding.rvDevices.setAdapter(usbDeviceAdapter);
        if (usbDevices.isEmpty()) {
            binding.tvNoDevice.setVisibility(View.VISIBLE);
        } else {
            binding.tvNoDevice.setVisibility(View.GONE);
        }
        //refresh devices
        binding.btnRefresh.setOnClickListener(v -> {
            LoggerUtils.d("start refresh...");
            fetchUsbDevices(usbDevices);
            if (usbDevices.isEmpty()) {
                binding.tvNoDevice.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoDevice.setVisibility(View.GONE);
            }
            usbDeviceAdapter.notifyDataSetChanged();
        });

    }

    private void fetchUsbDevices(List<UsbDevice> usbDevices) {
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> usbMap = usbManager.getDeviceList();
        usbDevices.clear();
        if (usbMap == null || usbMap.isEmpty()) {
            return;
        }
        for (Map.Entry<String, UsbDevice> entry : usbMap.entrySet()) {
            usbDevices.add(entry.getValue());
        }
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(usbPermissionReceiver);
        super.onDestroy();
    }

    class UsbDeviceAdapter extends BaseBindingRecyclerAdapter<CoreUsbItemDeviceBinding> {
        @Override
        protected void bindItemData(CoreUsbItemDeviceBinding itemBinding, int position) {
            UsbDevice usbDevice = usbDevices.get(position);
            LoggerUtils.i("find usb device:" + usbDevice);
            if (!TextUtils.isEmpty(usbDevice.getProductName())) {
                itemBinding.tvName.setText(usbDevice.getProductName());
            } else if (!TextUtils.isEmpty(usbDevice.getDeviceName())) {
                itemBinding.tvName.setText(usbDevice.getDeviceName());
            }
            int vid = usbDevice.getVendorId();
            int pid = usbDevice.getProductId();
            itemBinding.tvIds.setText(getString(R.string.core_usb_ids_format, Integer.toHexString(vid), Integer.toHexString(pid)));
            itemBinding.getRoot().setOnClickListener(v -> {
                if (selectPid == pid && selectVid == vid){
                    //unbind
                    String message = getString(R.string.core_usb_unbind_dialog_message_format, itemBinding.tvName.getText());
                    new MessageDialog.Builder(UsbActivity.this)
                            .setMessage(message)
                            .setConfirmButton(R.color.base_warning,R.string.core_usb_dialog_unbind,dialog -> {
                                selectPid = -1;
                                selectVid = -1;
                                ParamsUtils.setInt(KEY_USB_PID, selectPid);
                                ParamsUtils.setInt(KEY_USB_VID, selectVid);
                                notifyDataSetChanged();
                            })
                            .setCancelButton(d -> {})
                            .show();
                }else{
                    //bind
                    String message = getString(R.string.core_usb_bind_dialog_message_format, itemBinding.tvName.getText());
                    new MessageDialog.Builder(UsbActivity.this)
                            .setMessage(message)
                            .setConfirmButton(R.string.core_usb_dialog_bind,dialog -> {
                                selectPid = pid;
                                selectVid = vid;
                                ParamsUtils.setInt(KEY_USB_PID, selectPid);
                                ParamsUtils.setInt(KEY_USB_VID, selectVid);
                                UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                                if (!usbManager.hasPermission(usbDevice)) {
                                    PendingIntent pi = PendingIntent.getBroadcast(UsbActivity.this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                                    usbManager.requestPermission(usbDevice, pi);
                                } else {
                                    notifyDataSetChanged();
                                }
                            })
                            .setCancelButton(d -> {})
                            .show();
                }

            });
            if (vid == selectVid && pid == selectPid) {
                itemBinding.tvSelected.setVisibility(View.VISIBLE);
            } else {
                itemBinding.tvSelected.setVisibility(View.GONE);
            }
        }


        @Override
        public int getItemCount() {
            return usbDevices.size();
        }
    }

    class UsbPermissionReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            switch (action) {
                case ACTION_USB_PERMISSION:
                    boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                    if (granted && device != null) {
                        selectPid = device.getProductId();
                        selectVid = device.getVendorId();
                        ParamsUtils.setInt(KEY_USB_PID, selectPid);
                        ParamsUtils.setInt(KEY_USB_VID, selectVid);
                        usbDeviceAdapter.notifyDataSetChanged();
                    } else {
                        ToastUtils.showToast(R.string.core_usb_request_permission_failed);
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                    usbDevices.add(device);
                    usbDeviceAdapter.notifyDataSetChanged();
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    usbDevices.remove(device);
                    usbDeviceAdapter.notifyDataSetChanged();
                    break;
                default:
                    break;
            }
            if (usbDevices.isEmpty()) {
                binding.tvNoDevice.setVisibility(View.VISIBLE);
            } else {
                binding.tvNoDevice.setVisibility(View.GONE);
            }
        }
    }
}
