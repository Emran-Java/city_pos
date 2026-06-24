package acquire.core.bluetooth.view;

import android.view.View;

import java.util.List;

import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.R;
import acquire.core.databinding.CoreBluetoothItemDeviceBinding;


/**
 * A bluetooth device list adapter
 *
 * @author Janson
 * @date 2023/7/5 16:32
 */
public class BtDeviceListAdapter extends BaseBindingRecyclerAdapter<CoreBluetoothItemDeviceBinding> {

    private final List<BtListBean> btListBeans;
    private BtListBean mConnectBtListBean;

    private OnItemClickListener listener;

    public BtDeviceListAdapter(List<BtListBean> btListBeans) {
        this.btListBeans = btListBeans;

    }

    @Override
    protected void bindItemData(CoreBluetoothItemDeviceBinding itemBinding, int position) {
        final BtListBean btListBean = btListBeans.get(position);
        //bluetooth name
        itemBinding.bivDevice.setDevice(btListBean.getName());
        if (btListBean.getName() == null || btListBean.getName().isEmpty()) {
            itemBinding.bivDevice.setDevice(btListBean.getAddress());
        }
        //bonded status
        itemBinding.bivDevice.setbonded(btListBean.isBonded());

        //icon
        itemBinding.bivDevice.setConnected(false);
        //device is connected
        if (btListBean.equals(mConnectBtListBean)) {
            itemBinding.vMask.setVisibility(View.VISIBLE);
            itemBinding.bivDevice.setDescription(itemBinding.getRoot().getContext().getString(R.string.core_bluetooth_connecting));
        } else {
            itemBinding.vMask.setVisibility(View.GONE);
            itemBinding.bivDevice.setDescription(null);
        }
        if (listener != null) {
            itemBinding.getRoot().setOnClickListener(v -> {
                listener.onItemClick(position);
            });
        }
    }

    @Override
    public int getItemCount() {
        return btListBeans.size();
    }

    /**
     * The item of this device show being connected
     */
    public void showConnecting(BtListBean btListBean) {
        mConnectBtListBean = btListBean;

    }

    /**
     * Is there an item displayed as being connected
     */
    public boolean isConnecting() {
        return mConnectBtListBean != null;

    }

    /**
     * close connecting status UI
     */
    public void closeConnecting() {
        mConnectBtListBean = null;
    }

    /**
     * The listener wiil invoked when the item is clicked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

}


