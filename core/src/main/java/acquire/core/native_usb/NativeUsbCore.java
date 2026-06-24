package acquire.core.native_usb;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbRequest;
import android.os.Build;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.concurrent.TimeoutException;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.Locker;

/**
 * A android native usb utils for large USB ports.
 *
 * @author Janson
 * @date 2023/7/18 9:12
 */
public class NativeUsbCore {
    private UsbDevice usbDevice;
    private UsbEndpoint usbEpIn;
    private UsbEndpoint usbEpOut;

    private UsbDeviceConnection connection;
    private Locker<Boolean> locker;

    public boolean init(int pid, int vid) {
        Context context = BaseApplication.getAppContext();
        usbDevice = null;
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            LoggerUtils.i("find usb device:" + device);
            if (device.getVendorId() == vid && device.getProductId() == pid) {
                if (!usbManager.hasPermission(device)) {
                    PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(UsbActivity.ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    locker = new Locker<>();
                    IntentFilter filter = new IntentFilter(UsbActivity.ACTION_USB_PERMISSION);
                    PermissionBroadcast permissionBroadcast = new PermissionBroadcast();
                    context.registerReceiver(permissionBroadcast, filter);
                    locker.waiting(2000);
                    context.unregisterReceiver(permissionBroadcast);
                    if (locker.getResult()) {
                        usbDevice = device;
                        return true;
                    }
                } else {
                    usbDevice = device;
                    return true;
                }

            }
        }
        LoggerUtils.e("init Usb failed,pid:" + pid + ",vid:" + vid);
        return false;
    }

    public boolean init() {
        usbDevice = null;
        int pid = ParamsUtils.getInt(UsbActivity.KEY_USB_PID, -1);
        int vid = ParamsUtils.getInt(UsbActivity.KEY_USB_VID, -1);
        if (pid == -1 || vid == -1 ){
            LoggerUtils.e("init Usb failed, not selected the device.");
            return false;
        }
        return init(pid,vid);
    }

    public boolean open() {
        if (usbDevice == null) {
            LoggerUtils.e("open USB failed, not inited.");
            return false;
        }
        Context context = BaseApplication.getAppContext();
        UsbManager usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        UsbInterface usbInterface = usbDevice.getInterface(0);
        try {
            for (int i = 0; i < usbInterface.getEndpointCount(); i++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(i);
                int direction = endpoint.getDirection();
                switch (direction) {
                    case UsbConstants.USB_DIR_IN:
                        usbEpIn = endpoint;
                        break;
                    case UsbConstants.USB_DIR_OUT:
                        usbEpOut = endpoint;
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        connection = usbManager.openDevice(usbDevice);
        if (connection != null && !connection.claimInterface(usbInterface, true)) {
            connection.close();
            connection = null;
            LoggerUtils.e("open USB failed");
            return false;
        }
        return true;
    }

    public boolean write(byte[] data) {
        if (connection == null || usbEpOut == null) {
            LoggerUtils.e("write USB data failed, not opened.");
            return false;
        }
        int writeLength = connection.bulkTransfer(usbEpOut, data, data.length, 2000);
        return writeLength == data.length;
    }

    public byte[] read() {
        if (connection == null || usbEpIn == null) {
            LoggerUtils.e("read USB data failed, not opened.");
            return null;
        }
        int inMax = usbEpIn.getMaxPacketSize();
        ByteBuffer byteBuffer = ByteBuffer.allocate(inMax);
        UsbRequest usbRequest = new UsbRequest();
        usbRequest.initialize(connection, usbEpIn);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            usbRequest.queue(byteBuffer);
            try {
                if (connection.requestWait(2000) != usbRequest) {
                    LoggerUtils.e("request USB read task failed.");
                    return null;
                }
            } catch (TimeoutException e) {
                e.printStackTrace();
                return null;
            }
        } else {
            usbRequest.queue(byteBuffer, inMax);
            if (connection.requestWait() != usbRequest) {
                LoggerUtils.e("request USB read task failed.");
                return null;
            }
        }
        return byteBuffer.array();
    }

    public void close() {
        if (connection != null) {
            connection.releaseInterface(usbDevice.getInterface(0));
            connection.close();
        }
        connection = null;
        usbDevice = null;
        usbEpIn = null;
        usbEpOut = null;
    }

    public void startConfig(Context context) {
        Intent intent = new Intent(context, UsbActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    class PermissionBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (UsbActivity.ACTION_USB_PERMISSION.equals(action)) {
                UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                boolean granted = intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false);
                if (device != null && granted) {
                    usbDevice = device;
                    locker.setResult(true);
                    locker.wakeUp();
                } else {
                    locker.setResult(false);
                    locker.wakeUp();
                }
            }
        }
    }

} 
