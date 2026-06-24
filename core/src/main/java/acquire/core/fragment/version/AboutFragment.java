package acquire.core.fragment.version;

import android.icu.util.TimeZone;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.AppUtils;
import acquire.base.utils.DateUtils;
import acquire.base.utils.network.NetworkUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.BuildConfig;
import acquire.core.R;
import acquire.core.constant.ResultCode;
import acquire.core.databinding.CoreAboutItemBinding;
import acquire.core.databinding.CoreFragmentAboutBinding;
import acquire.core.fragment.print.PrintFragment;
import acquire.core.fragment.print.PrintViewModel;
import acquire.core.fragment.result.ResultFragment;
import acquire.core.model.DeviceItem;
import acquire.core.tools.DataConverter;
import acquire.core.tools.sim.SimUtility;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.BExtDevice;
import acquire.sdk.emv.BEmvProcessor;
import acquire.sdk.emv.BExtEmvProcessor;

/**
 * A {@link Fragment} that displays app information.
 *
 * @author Janson
 * @date 2019/1/28 10:22
 */
public class AboutFragment extends BaseFragment {
    private SimpleCallback callback;

    List<DeviceItem> items = new ArrayList<>();

    @NonNull
    public static AboutFragment newInstance(SimpleCallback callback) {
        AboutFragment fragment = new AboutFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentAboutBinding binding = CoreFragmentAboutBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(R.string.core_transaction_name_about);
        binding.toolbar.setBackListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());

        /*
            App Name| BRACBANK |
            App Version| 1.0.3.3 |
            Release Time| 07/09/2025 22:32:10 |
            NSDK Version| 2.3.2 |
            EMV Version| 4.3.7 |
            Serial Number| NAA700771653 |
            Firmware Version| D1.1.51 |
            Sim Operator1| GrameenPhone |
            CCID1| 8988012804523654241F |
            Phone No1| 8801755638400 |
            Terminal Model| N910 Pro |
        *
        * */

        items.add(new DeviceItem(R.string.core_about_app_name, AppUtils.getAppName(mActivity)));
        items.add(new DeviceItem(R.string.core_about_app_version, AppUtils.getAppVersionName(mActivity)));

        //app creation time
        String creationTime = DateUtils.formatTimeStamp(BuildConfig.RELEASE_TIMESTAMP);
        items.add(new DeviceItem(R.string.core_about_app_creation_time, creationTime /* + "\n" + TimeZone.getDefault().getDisplayName()*/));
        items.add(new DeviceItem(R.string.core_about_nsdk_version, BDevice.getSdkVersion()));
        if (!BDevice.isCpos()) {
            //built-in
            items.add(new DeviceItem(R.string.core_about_emv_version, new BEmvProcessor().getEmvApiVersion()));
        }
        items.add(new DeviceItem(R.string.core_about_serial_number, BDevice.getSn()));
        items.add(new DeviceItem(R.string.core_about_firmware_version, BDevice.getFirmwareVersion()));

        String opName = NetworkUtils.getOperatorName(mActivity);
        int slotNo = NetworkUtils.getDefaultDataSlotId(mActivity);
        ++slotNo;
        items.add(new DeviceItem(getString(R.string.core_about_sip_operator) + slotNo, opName));
        String opCcid = SimUtility.getICCID(mActivity);
        items.add(new DeviceItem(getString(R.string.core_about_ccid) + slotNo, opCcid));

        //todo: get Phone number
        items.add(new DeviceItem(getString(R.string.core_get_about_phone) + slotNo, "+8801XXXXXXXXX"));

        items.add(new DeviceItem(R.string.core_about_model, BDevice.getDeviceModel()));

        DeviceAdapter deviceAdapter = new DeviceAdapter(items);
        if (ExtServiceHelper.getInstance().isInit()) {
            //external
            ThreadPool.execute(() -> {
                int start = items.size();
                items.add(new DeviceItem(R.string.core_about_external_emv_version, new BExtEmvProcessor().getEmvApiVersion()));
                items.add(new DeviceItem(R.string.core_about_external_device_version, BExtDevice.getVersion()));
                items.add(new DeviceItem(R.string.core_about_external_device_baudrate, BExtDevice.getBaudRateMode()));
                mActivity.runOnUiThread(() -> deviceAdapter.notifyItemRangeChanged(start, items.size() - start));
            });
        }
        binding.rvVersion.setAdapter(deviceAdapter);
        binding.btnConfirm.setOnClickListener(view -> callback.result());
        binding.btnPrint.setOnClickListener(view ->{
            printAboutInfo();
        });

        return binding.getRoot();
    }

    private void printAboutInfo() {

        mActivity.mSupportDelegate.switchContent(PrintFragment.newPrintAboutInfoInstance(items, false, PrintViewModel.RECEIPT_OWNER_MERCHANT,new FragmentCallback<Void>(){
            @Override
            public void onSuccess(Void unused) {
                mActivity.mSupportDelegate.popBackFragment(1);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {

            }
        }));
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }


    /**
     * A {@link RecyclerView.Adapter} used to display infomation from {@link DeviceItem}
     *
     * @author Janson
     * @date 2020/12/4 13:50
     */
    private static class DeviceAdapter extends BaseBindingRecyclerAdapter<CoreAboutItemBinding> {
        private final List<DeviceItem> mDeviceItems;

        public DeviceAdapter(List<DeviceItem> mDeviceItems) {
            this.mDeviceItems = mDeviceItems;
        }

        @Override
        protected void bindItemData(CoreAboutItemBinding itemBinding, int position) {
            DeviceItem item = mDeviceItems.get(position);

            if (item.getTitle() == 0 && item.getSTitle() != null) {
                itemBinding.tvInfoTitle.setText(item.getSTitle());
            } else {
                itemBinding.tvInfoTitle.setText(item.getTitle());
            }

            itemBinding.tvInfoContent.setText(item.getContent());
        }

        @Override
        public int getItemCount() {
            return mDeviceItems.size();
        }

    }

/*    private static class DeviceItem {
        private final @StringRes int title;
        private final String content, sTitle;

        public DeviceItem(@StringRes int title, String content) {
            this.sTitle = null;
            this.title = title;
            this.content = content;
        }

        public DeviceItem(String sTitle, String content) {
            this.title = 0;
            this.sTitle = sTitle;
            this.content = content;
        }

        public @StringRes int getTitle() {
            return title;
        }

        public String getSTitle() {
            return sTitle;
        }

        public String getContent() {
            return content;
        }


    }*/
}
