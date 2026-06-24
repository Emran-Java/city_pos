package acquire.core.fragment.help_center;

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
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.core.BuildConfig;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.databinding.CoreAboutItemBinding;
import acquire.core.databinding.CoreFragmentAboutBinding;
import acquire.core.databinding.CoreFragmentHeplCenterBinding;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.BExtDevice;
import acquire.sdk.emv.BEmvProcessor;
import acquire.sdk.emv.BExtEmvProcessor;

/**
 * A {@link Fragment} that displays app information.
 *
 * @author Emran
 * @date 2026/6/9 17:30
 */
public class HelpCenterFragment extends BaseFragment {
    private SimpleCallback callback;

    private CoreFragmentHeplCenterBinding binding;

    @NonNull
    public static HelpCenterFragment newInstance(SimpleCallback callback) {
        HelpCenterFragment fragment = new HelpCenterFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentHeplCenterBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(R.string.core_transaction_name_help_center);
        binding.toolbar.setBackListener(v -> mActivity.getOnBackPressedDispatcher().onBackPressed());

        setValue();
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }

    private void setValue() {

        binding.etContactName.setText(ParamsUtils.getString(ParamsConst.PARAMS_KEY_CONTACT_NAME, ""));
        binding.etMobile.setText(ParamsUtils.getString(ParamsConst.PARAMS_KEY_CONTACT_PHONE, ""));
        binding.etAlternative.setText(ParamsUtils.getString(ParamsConst.PARAMS_KEY_CONTACT_ALTERNATIVE_PHONE, ""));
        binding.etEmail.setText(ParamsUtils.getString(ParamsConst.PARAMS_KEY_CONTACT_EMAIL, ""));

    }

/*
    private static class DeviceAdapter extends BaseBindingRecyclerAdapter<CoreAboutItemBinding> {
        private final List<DeviceItem> mDeviceItems;

        public DeviceAdapter(List<DeviceItem> mDeviceItems) {
            this.mDeviceItems = mDeviceItems;
        }

        @Override
        protected void bindItemData(CoreAboutItemBinding itemBinding, int position) {
            DeviceItem item = mDeviceItems.get(position);
            itemBinding.tvInfoTitle.setText(item.getTitle());
            itemBinding.tvInfoContent.setText(item.getContent());
        }

        @Override
        public int getItemCount() {
            return mDeviceItems.size();
        }

    }

    private static class DeviceItem {
        private final @StringRes int title;
        private final String content;

        public DeviceItem(@StringRes int title, String content) {
            this.title = title;
            this.content = content;
        }

        public @StringRes int getTitle() {
            return title;
        }

        public String getContent() {
            return content;
        }

    }*/
}
