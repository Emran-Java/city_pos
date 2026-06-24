package acquire.core.bluetooth.proxy;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import acquire.base.utils.LoggerUtils;


/**
 * bluetooth proxy.(match)
 *
 * @author Janson
 * @date 2023/7/5 17:49
 */
@SuppressLint("MissingPermission")
public class DefaultWithBondProxy extends DefaultProxy {

    private static volatile DefaultWithBondProxy instance;
    private DefaultWithBondProxy() {}
    public static DefaultWithBondProxy getInstance() {
        if (instance == null){
            synchronized (DefaultWithBondProxy.class){
                if (instance == null){
                    instance = new DefaultWithBondProxy();
                 }
            }
        }
        return instance;
    }

    @Override
    protected boolean bond(BluetoothDevice device) {
        if (device.getBondState() == BluetoothDevice.BOND_NONE){
            LoggerUtils.d("bond device："+device.getName()+","+device.getAddress());
            return device.createBond();
        }
        return true;
    }
}
