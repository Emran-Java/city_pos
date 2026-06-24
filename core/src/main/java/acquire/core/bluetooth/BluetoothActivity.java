package acquire.core.bluetooth;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseActivity;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.bluetooth.proxy.IBluetoothProxy;
import acquire.core.bluetooth.view.BtDeviceListAdapter;
import acquire.core.bluetooth.view.BtListBean;
import acquire.core.databinding.CoreActivityBluetoothBinding;


/**
 * Bluetooth setting activity
 *
 * @author Janson
 * @date 2023/7/5 17:25
 */
@SuppressLint("MissingPermission")
public class BluetoothActivity extends BaseActivity {

    private CoreActivityBluetoothBinding binding;
    private final List<BtListBean> btListBeans = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;
    private final BtDeviceListAdapter btDeviceAdapter = new BtDeviceListAdapter(btListBeans);
    private final BluetoothReceiver bluetoothReceiver = new BluetoothReceiver();

    @Override
    public int attachFragmentResId() {
        //unused
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DisplayUtils.immersedStatusBar(getWindow());
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        binding = CoreActivityBluetoothBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
        binding.toolbar.setBackListener(v-> onBackPressed());
        binding.rvDevices.setAdapter(btDeviceAdapter);

        //able devices list
        btDeviceAdapter.setOnItemClickListener(position -> {
            BtListBean btListBean = btListBeans.get(position);
            //check if its address can be found.
            final BluetoothDevice device = bluetoothAdapter.getRemoteDevice(btListBean.getAddress());
            if (device == null) {
                return;
            }
            LoggerUtils.d("click connection：" + device.getName() + "," + device.getAddress());
            String name = device.getName();
            String message;
            if (TextUtils.isEmpty(name)) {
                message = getString(R.string.core_bluetooth_dialog_connect_message_without_name, device.getAddress());
            } else {
                message = getString(R.string.core_bluetooth_dialog_connect_message, device.getName(), device.getAddress());
            }
            new MessageDialog.Builder(this)
                    .setMessage(message)
                    .setConfirmButton(R.string.core_bluetooth_dialog_button_connect, dialog -> {
                        if (btDeviceAdapter.isConnecting()){
                            return;
                        }
                        if (!bluetoothAdapter.isEnabled()) {
                            return;
                        }
                        btDeviceAdapter.showConnecting(new BtListBean(device));
                        btDeviceAdapter.notifyItemChanged(position);
                        ThreadPool.execute(() -> {
                            bluetoothAdapter.cancelDiscovery();
                            try {
                                BluetoothCore.proxy.connect(this, device);
                                showConnect(device);
                            } catch (Exception e) {
                                //connect failed
                                runOnUiThread(()->{
                                    ToastUtils.showToast(e.getMessage());
                                    showDisconnect(BluetoothCore.proxy.getConnectDevice());
                                });

                            }
                        });

                    })
                    .setCancelButton(d -> {
                    })
                    .show();
        });
        binding.swOpen.setChecked(bluetoothAdapter.isEnabled());
        binding.swOpen.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //open bluetooth
                if (!bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.enable();
                }
                binding.llDeviceLayout.setVisibility(View.VISIBLE);
                binding.btnRefresh.setEnabled(true);
            } else {
                //close bluetooth
                if (bluetoothAdapter.isEnabled()) {
                    bluetoothAdapter.disable();
                }
                binding.llDeviceLayout.setVisibility(View.GONE);
                binding.bivConnectedDevice.setVisibility(View.GONE);
                binding.btnRefresh.setEnabled(false);
            }
        });
        if (!bluetoothAdapter.isEnabled()) {
            //bluetooth is closed, hide UI
            binding.llDeviceLayout.setVisibility(View.GONE);
            binding.bivConnectedDevice.setVisibility(View.GONE);
            binding.btnRefresh.setEnabled(false);
        }
        binding.bivConnectedDevice.setOnClickListener(v -> {
            String message = getString(R.string.core_bluetooth_dialog_disconnect_message, binding.bivConnectedDevice.getDevice());
            new MessageDialog.Builder(this)
                    .setMessage(message)
                    .setConfirmButton(R.string.core_bluetooth_dialog_button_disconnect, dialog -> {
                        if (!bluetoothAdapter.isEnabled()) {
                            return;
                        }
                        ThreadPool.execute(() -> {
                            bluetoothAdapter.cancelDiscovery();
                            BluetoothCore.proxy.disconnect();
                            BluetoothDevice device = BluetoothCore.proxy.getConnectDevice();
                            showDisconnect(device);
                            if (addDeviceToList(device)) {
                                //add this device into search list
                                btDeviceAdapter.notifyItemChanged(btListBeans.size() - 1);
                                if (binding.pbDiscovering.getVisibility() != View.VISIBLE) {
                                    binding.pbDiscovering.setVisibility(View.VISIBLE);
                                }
                            }
                        });

                    })
                    .setCancelButton(d -> {})
                    .show();
        });
        //refresh devices
        binding.btnRefresh.setOnClickListener(v -> {
            if (!bluetoothAdapter.isEnabled()) {
                //bluetooth is closed
                return;
            }
            if (bluetoothAdapter.isDiscovering()) {
                //cancel discovery
                bluetoothAdapter.cancelDiscovery();
            }
            LoggerUtils.d("start refresh...");
            btListBeans.clear();
            btDeviceAdapter.notifyDataSetChanged();
            bluetoothAdapter.startDiscovery();
        });
        //the connected device
        if (BluetoothCore.proxy.getConnectDevice() != null) {
            showConnect(BluetoothCore.proxy.getConnectDevice());
        }

        //register broadcast receiver
        LoggerUtils.d("register BluetoothReceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        filter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        filter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(bluetoothReceiver, filter);
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
        }
    }

    /**
     * add a device to the list
     */
    private boolean addDeviceToList(BluetoothDevice device) {
        if (device == null || !bluetoothAdapter.isEnabled()) {
            return false;
        }
        BtListBean target = new BtListBean(device);
        if (!btListBeans.contains(target)) {
            btListBeans.add(target);
            return true;
        } else {
            return false;
        }
    }

    /**
     * show that a device is connected
     */
    private void showConnect(final BluetoothDevice device) {
        if (device == null) {
            return;
        }
        runOnUiThread(() -> {
            //show connecting UI
            if (!bluetoothAdapter.isEnabled()) {
                //bluetooth is closed
                return;
            }
            BtListBean target = new BtListBean(device);
            btListBeans.remove(target);
            btDeviceAdapter.closeConnecting();
            btDeviceAdapter.notifyDataSetChanged();
            if (binding.bivConnectedDevice.getVisibility() != View.VISIBLE) {
                binding.bivConnectedDevice.setVisibility(View.VISIBLE);
            }
            binding.bivConnectedDevice.setDevice(device.getName());
            binding.bivConnectedDevice.setConnected(true);
            binding.bivConnectedDevice.setDescription(getString(R.string.core_bluetooth_connected_device_content));
            binding.tvStatus.setText(R.string.core_bluetooth_status_connected);
        });
    }

    /**
     * show that a device is disconnected
     */
    private void showDisconnect(final BluetoothDevice device) {
        runOnUiThread(() -> {
            //close connecting UI
            addDeviceToList(device);
            btDeviceAdapter.closeConnecting();
            btDeviceAdapter.notifyDataSetChanged();
            if (binding.bivConnectedDevice.getVisibility() == View.VISIBLE) {
                binding.bivConnectedDevice.setVisibility(View.GONE);
            }
            binding.tvStatus.setText(R.string.core_bluetooth_status_disconnected);
        });
    }

    @Override
    protected void onDestroy() {
        LoggerUtils.d("unregister BluetoothReceiver");
        unregisterReceiver(bluetoothReceiver);
        super.onDestroy();
    }



    /**
     * The broadcast receiver for bluetooth status
     *
     * @author Janson
     * @date 2023/7/5 17:37
     */
    @SuppressLint("MissingPermission")
    class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                return;
            }
            BluetoothDevice device;
            IBluetoothProxy proxy;

            switch (action) {
                case BluetoothDevice.ACTION_FOUND:
                    //find device
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        LoggerUtils.d("BluetoothReceiver--found device[" + device.getName() + "]--" + device.getAddress());
                        if (device.equals(BluetoothCore.proxy.getConnectDevice())) {
                            //this device has been connected
                            return;
                        }
                        if (addDeviceToList(device)) {
                            //add this device into search list
                            btDeviceAdapter.notifyItemChanged(btListBeans.size() - 1);
                            if (binding.pbDiscovering.getVisibility() != View.VISIBLE) {
                                binding.pbDiscovering.setVisibility(View.VISIBLE);
                            }

                        }
                    }
                    break;
                case BluetoothAdapter.ACTION_DISCOVERY_FINISHED:
                    LoggerUtils.d("BluetoothReceiver--bluetooth discovery finished");
                    //discovery finish
                    if (binding.pbDiscovering.getVisibility() == View.VISIBLE) {
                        binding.pbDiscovering.setVisibility(View.GONE);
                    }
                    btDeviceAdapter.notifyDataSetChanged();
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    //disconnect
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null) {
                        LoggerUtils.d("BluetoothReceiver--disconnect bluetooth device[" + device.getName() + "]--" + device.getAddress());
                        showDisconnect(device);
                    } else {
                        LoggerUtils.d("BluetoothReceiver--bluetooth disconnected ");
                    }
                    proxy = BluetoothCore.proxy;
                    proxy.disconnect();
                    break;
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    //connect devices
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    LoggerUtils.d("BluetoothReceiver--bluetooth device connected[" + device.getName() + "]--" + device.getAddress());
                    if (device.equals(BluetoothCore.proxy.getConnectDevice()) && binding.bivConnectedDevice.getVisibility() != View.VISIBLE) {
                        LoggerUtils.d("synchronize bluetooth device: " + device);
                        showConnect(BluetoothCore.proxy.getConnectDevice());
                    }
                    break;
                case BluetoothAdapter.ACTION_STATE_CHANGED:
                    //bluetooth state changed
                    int newState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1);
                    int oldState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, -1);
                    switch (newState) {
                        case BluetoothAdapter.STATE_TURNING_ON:
                            LoggerUtils.d("BluetoothReceiver--bluetooth opening");
                            break;
                        case BluetoothAdapter.STATE_ON:
                            LoggerUtils.d("BluetoothReceiver--bluetooth has open");
                            if (binding.llDeviceLayout.getVisibility() != View.VISIBLE) {
                                binding.llDeviceLayout.setVisibility(View.VISIBLE);
                            }
                            if (!bluetoothAdapter.isDiscovering()) {
                                //start discovery bluetooth device
                                bluetoothAdapter.startDiscovery();
                            }
                            binding.swOpen.setChecked(true);
                            break;
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            LoggerUtils.d("BluetoothReceiver--bluetooth closing");
                            break;
                        case BluetoothAdapter.STATE_OFF:
                            LoggerUtils.d("BluetoothReceiver--bluetooth has closed");
                            //disconnect
                            proxy = BluetoothCore.proxy;
                            proxy.disconnect();
                            if (binding.llDeviceLayout.getVisibility() == View.VISIBLE) {
                                binding.llDeviceLayout.setVisibility(View.GONE);
                            }
                            binding.swOpen.setChecked(false);
                            binding.tvStatus.setText(R.string.core_bluetooth_status_disconnected);
                            break;
                        default:
                            break;
                    }
                    break;
                case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                    //bond state changed
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    switch (device.getBondState()) {
                        case BluetoothDevice.BOND_BONDING:
                            LoggerUtils.d("BluetoothReceiver--bonding--device[" + device.getName() + "]--" + device.getAddress());
                            break;
                        case BluetoothDevice.BOND_BONDED:
                            LoggerUtils.d("BluetoothReceiver--bond successfully--device[" + device.getName() + "]--" + device.getAddress());
                            break;
                        case BluetoothDevice.BOND_NONE:
                            LoggerUtils.d("BluetoothReceiver--bond failed--device[" + device.getName() + "]--" + device.getAddress());
                            break;
                        default:
                            break;
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
