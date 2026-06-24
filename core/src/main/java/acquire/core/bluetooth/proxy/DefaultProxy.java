package acquire.core.bluetooth.proxy;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.UUID;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;

/**
 * default bluetooth proxy.(Not match)
 *
 * @author Janson
 * @date 2023/7/5 17:41
 */
@SuppressLint("MissingPermission")
public class DefaultProxy implements IBluetoothProxy {
    private final static String BT_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    protected BluetoothDevice connectDevice;
    protected BluetoothSocket socket;
    protected final static String KEY_ADDRESS = "BT_ADDRESS";
    private boolean connecting;
    private static volatile DefaultProxy instance;

    protected DefaultProxy() {}
    public static DefaultProxy getInstance() {
        if (instance == null){
            synchronized (DefaultProxy.class){
                if (instance == null){
                    instance = new DefaultProxy();
                 }
            }
        }
        return instance;
    }


    @Override
    public void connectLast(Context context) throws Exception{
        //connect the last device automatically
        LoggerUtils.d("Auto connect the last bluetooth device");
        String lastAddress = ParamsUtils.getString(KEY_ADDRESS, null);
        if (TextUtils.isEmpty(lastAddress)) {
            LoggerUtils.e("The connection record is empty ");
            throw new Exception(context.getString(R.string.core_bluetooth_configure_bluetooth_prompt));
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(lastAddress);
        connect(context,device);
    }

    protected boolean bond(BluetoothDevice device){
        return true;
    }

    @Override
    public void connect(Context context, BluetoothDevice device)throws Exception{
        if (isConnected() && this.connectDevice != null && this.connectDevice.equals(device)) {
            LoggerUtils.d("The bluetooth device has been connected");
            return ;
        }
        LoggerUtils.d("connect new device[uuid = "+ BT_UUID +"]");
        //close the last device
        disconnect();
        UUID uuidEntry;
        try {
            uuidEntry = UUID.fromString(BT_UUID);
        }catch (IllegalArgumentException e){
            LoggerUtils.e("uuid wrong format");
            throw new Exception(context.getString(R.string.core_bluetooth_connect_uuid_error));
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.cancelDiscovery();
        if (!bond(device)){
            LoggerUtils.e("bond failed");
            throw new Exception(context.getString(R.string.core_bluetooth_connect_bond_error));
        }
        //connect the device
        try {
            connecting = true;
            LoggerUtils.e("create bluetooth socket");
            socket = device.createInsecureRfcommSocketToServiceRecord(uuidEntry);
            LoggerUtils.e("bluetooth socket connecting");
            socket.connect();
            //save the device address
            ParamsUtils.setString(KEY_ADDRESS, device.getAddress());
            this.connectDevice = device;
            LoggerUtils.d("socket connect successfully");
        } catch (Exception e) {
            LoggerUtils.e(e.getMessage());
            this.connectDevice = null;
            socket = null;
            throw new Exception(context.getString(R.string.core_bluetooth_connect_fail));
        }finally {
            connecting = false;
        }
    }

    @Override
    public boolean disconnect() {
        connectDevice = null;
        if (socket != null) {
            LoggerUtils.e("disconnect bluetooth");
            try {
                if (socket.getInputStream() != null) {
                    socket.getInputStream().close();
                }
                if (socket.getOutputStream() != null) {
                    socket.getOutputStream().close();
                }
                if (socket != null) {
                    socket.close();
                    socket = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }


    @Override
    public void write(byte[] data) throws Exception {
        LoggerUtils.d("send bluetooth data");
        waitConnectFinish();
        if (!isConnected()){
            throw new Exception("Bluetooth not connected or disconnected") ;
        }
        socket.getOutputStream().write(data, 0, data.length);
        LoggerUtils.d("send bluetooth data finish");
    }

    @Override
    public byte[] read() throws Exception {
        LoggerUtils.d("receive bluetooth data");
        waitConnectFinish();
        if (!isConnected()){
            throw new Exception("Bluetooth not connected or disconnected") ;
        }
        int count = socket.getInputStream().available();
        if (count == 0){
            return null;
        }
        byte[] result = new byte[count];
        int readLength = socket.getInputStream().read(result);
        if (readLength != count){
            result = Arrays.copyOf(result, readLength);
        }
        LoggerUtils.d("receive bluetooth data finish");
        return result;
    }

    @Override
    public BluetoothDevice getConnectDevice() {
        if (connectDevice == null){
            return null;
        }
        if (!isConnected()){
            connectDevice = null;
            return null;
        }
        return connectDevice;
    }

    protected void waitConnectFinish(){
        long start = System.currentTimeMillis();
        while (connecting){
            if (System.currentTimeMillis() - start > 3*1000){
                return;
            }
        }
    }

    private boolean isConnected(){
        if (socket == null){
            LoggerUtils.e("socket not created");
            return false;
        }
        if (!socket.isConnected()){
            LoggerUtils.e("socket disconnected");
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) BaseApplication.getAppContext().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (!bluetoothAdapter.isEnabled()){
            LoggerUtils.e("bluetooth has bean disconnected");
            return false;
        }
        return true;
    }
}
