package acquire.core.bluetooth.view;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;

import androidx.annotation.Nullable;


/**
 * A bluetooth device information bean for list display
 *
 * @author Janson
 * @date 2023/7/5 16:22
 */
@SuppressLint("MissingPermission")
public class BtListBean {
    private final String name;
    private final String address;
    private final boolean isBonded;

    public BtListBean(BluetoothDevice bluetoothDevice) {
        this.name = bluetoothDevice.getName();
        this.address = bluetoothDevice.getAddress();
        this.isBonded = bluetoothDevice.getBondState() == BluetoothDevice.BOND_BONDED;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public boolean isBonded() {
        return isBonded;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof BtListBean){
            return address.equals(((BtListBean)obj).address);
        }
        return super.equals(obj);
    }
}
