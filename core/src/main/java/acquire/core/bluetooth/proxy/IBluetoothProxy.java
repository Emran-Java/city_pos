package acquire.core.bluetooth.proxy;

import android.bluetooth.BluetoothDevice;
import android.content.Context;


/**
 * Bluetooth proxy
 *
 * @author Janson
 * @date 2023/7/5 17:50
 */
public interface IBluetoothProxy {

    /**
     * connect the last device automatically
     */
    void connectLast(Context context)throws Exception;
    /**
     * connect a device automatically
     */
    void connect(Context context, BluetoothDevice device)throws Exception;

    /**
     * disconnect the device
     */
    boolean disconnect();

    /**
     * send data by bluetooth
     */
    void write(byte[] data) throws Exception;

    /**
     * read data by bluetooth
     */
    byte[] read() throws Exception;

    /**
     * get the connected devices
     */
    BluetoothDevice getConnectDevice();




}
