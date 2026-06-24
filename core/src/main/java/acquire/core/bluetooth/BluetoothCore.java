package acquire.core.bluetooth;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.core.R;
import acquire.core.bluetooth.proxy.DefaultProxy;
import acquire.core.bluetooth.proxy.IBluetoothProxy;

/**
 * Bluetooth connection utils.
 * <pre><b>e.g.</b></pre>
 * <pre>
 *     BluetoothCore core = new BluetoothCore();
 *     try {
 *          core.connectLast();
 *     } catch (Exception e) {
 *        return false;
 *     }
 *     return core.send(esc);
 *
 * </pre>
 *
 * @author Janson
 * @date 2023/7/5 17:20
 */
@SuppressLint("MissingPermission")
public class BluetoothCore {
    private final BluetoothAdapter bluetoothAdapter;

    static IBluetoothProxy proxy = DefaultProxy.getInstance();

    public BluetoothCore() {
        BluetoothManager bluetoothManager = (BluetoothManager) BaseApplication.getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
        this.bluetoothAdapter = bluetoothManager.getAdapter();
    }

    /**
     * connect the last device
     */
    public void connectLast() throws Exception {
        connect(null);
    }

    /**
     * connect the targe device
     */
    public void connectTarget(BluetoothDevice device) throws Exception {
        connect(device);
    }

    public void connectTarget(String address) throws Exception {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        connect(device);
    }

    private void connect(final BluetoothDevice device) throws Exception {
        //check if the device is connected
        if (proxy.getConnectDevice() != null) {
            //device is connected
            LoggerUtils.d("The bluetooth device has been connected");
            return;
        }
        LoggerUtils.d("start to connect the bluetooth device");
        Context context = BaseApplication.getAppContext();
        //connect bluetooth device
        if (!bluetoothAdapter.isEnabled()) {
            boolean result = openBluetooth();
            if (!result) {
                throw new Exception(context.getString(R.string.core_bluetooth_connect_timeout));
            }
        }
        if (device != null) {
            LoggerUtils.d("connect device: " + device);
            proxy.connect(context, device);
        } else {
            LoggerUtils.d("auto connect");
            proxy.connectLast(context);
        }
    }

    /**
     * send data
     */
    public boolean send(byte[] data) {
        if (data == null) {
            return false;
        }
        try {
            proxy.write(data);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            proxy.disconnect();
            return false;
        }
    }

    /**
     * read data
     */
    public byte[] read() {
        try {
            return proxy.read();
        } catch (Exception e) {
            e.printStackTrace();
            proxy.disconnect();
            return null;
        }
    }

    /**
     * disconnect the device
     */
    public void disconnect() {
        proxy.disconnect();
    }

    /**
     * open bluetooth setting
     */
    public void startConfig(Context context) {
        Intent intent = new Intent(context, BluetoothActivity.class);
        if (!(context instanceof Activity)) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    /**
     * return true if the bluetooth is connected
     */
    public boolean isConnected() {
        return proxy.getConnectDevice() != null;
    }

    /**
     * open bluetooth
     */
    private boolean openBluetooth() {
        LoggerUtils.d("open bluetooth");
        bluetoothAdapter.enable();
        //timeout 3 second.
        long start = System.currentTimeMillis();
        while (!bluetoothAdapter.isEnabled()) {
            if (System.currentTimeMillis() - start > 3 * 1000) {
                LoggerUtils.e("open bluetooth failed");
                return false;
            }
        }
        LoggerUtils.d("open bluetooth successfully");
        return true;
    }
}
