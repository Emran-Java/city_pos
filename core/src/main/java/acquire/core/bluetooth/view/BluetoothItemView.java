package acquire.core.bluetooth.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import acquire.core.R;
import acquire.core.databinding.CoreBluetoothItemViewBinding;


/**
 * A custom bluetooth device information view
 *
 * @author Janson
 * @date 2023/7/5 16:24
 */
public class BluetoothItemView extends FrameLayout {
    private final CoreBluetoothItemViewBinding binding;

    public BluetoothItemView(Context context) {
        this(context, null);
    }
    public BluetoothItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    public BluetoothItemView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        binding = CoreBluetoothItemViewBinding.inflate(LayoutInflater.from(context),this,true);
    }

    /**
     * Set the bluetooth icon for connection status
     */
    public void setConnected(boolean isConnected) {
        if (isConnected) {
            binding.ivType.setImageResource(R.drawable.core_bluetooth_ic_connected);
        } else {
            binding.ivType.setImageResource(R.drawable.core_bluetooth_ic_disconnected);
        }
    }

    /**
     * If the device is bonded, A bonded icon will show.
     */
    public void setbonded(boolean isBonded) {
        if (isBonded) {
            if (binding.ivBonded.getVisibility() != View.VISIBLE) {
                binding.ivBonded.setVisibility(View.VISIBLE);
            }
        } else {
            if (binding.ivBonded.getVisibility() == View.VISIBLE) {
                binding.ivBonded.setVisibility(View.GONE);
            }
        }
    }

    /**
     * set the device name or address
     */
    public void setDevice(String device) {
        binding.tvDevice.setText(device);
    }

    /**
     * get device
     */
    public String getDevice() {
        return binding.tvDevice.getText().toString();
    }

    /**
     * set the description for device status
     */
    public void setDescription(String status) {
        binding.tvDescription.setText(status);
        if (status == null || status.isEmpty()) {
            binding.tvDescription.setVisibility(View.GONE);
        } else {
            binding.tvDescription.setVisibility(View.VISIBLE);
        }
    }
}
