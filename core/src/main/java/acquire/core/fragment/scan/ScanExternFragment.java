package acquire.core.fragment.scan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.ScheduledFuture;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.databinding.CoreFragmentScanExternBinding;
import acquire.sdk.ConnectMode;
import acquire.sdk.scan.BExternScanner;

/**
 * A {@link Fragment} that displays external pinpad tips message.
 *
 * @author Janson
 * @date 2018/6/21 14:25
 */
public class ScanExternFragment extends BaseFragment {
    private BExternScanner externScanner;
    private FragmentCallback<String> callback;
    private ScheduledFuture<?> future;
    private boolean isFinish;

    @NonNull
    public static ScanExternFragment newInstance(FragmentCallback<String> callback) {
        ScanExternFragment fragment = new ScanExternFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentScanExternBinding binding = CoreFragmentScanExternBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        ViewUtils.setFocus(binding.etPaycode);
        externScanner = new BExternScanner();
        BExternScanner.ScanExternCallback scanSallback = new BExternScanner.ScanExternCallback() {
            @Override
            public void onSuccess(String result) {
                if (isFinish) {
                    return;
                }
                stopScan();
                result = result.replaceAll("[\r\n]", "");
                callback.onSuccess(result);
            }

            @Override
            public void onFailed(String error) {
                callback.onFail(FragmentCallback.FAIL, error);
            }
        };

        int connectMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SCAN_EXTERN_CONNECT_MODE);
        switch (connectMode) {
            case ConnectMode.USB:
                int delayMillis = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SCAN_EXTERN_USB_WAIT_TIME, 300);
                externScanner.doScanUsbHost(binding.etPaycode, delayMillis, scanSallback);
                break;
            case ConnectMode.SERIAL_PORT:
            {
                int baudrate = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SCAN_EXTERN_SERIAL_BAUDRATE);
                externScanner.doScanSerial(baudrate, scanSallback);
            }
                break;
            case ConnectMode.DOCK_USB1:
                externScanner.doScanDockUsb(1, scanSallback);
                break;
            case ConnectMode.DOCK_USB2:
                externScanner.doScanDockUsb(2, scanSallback);
                break;
            case ConnectMode.DOCK_SERIAL_PORT:
                int baudrate = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SCAN_EXTERN_SERIAL_BAUDRATE);
                externScanner.doScanDockSerial(baudrate, scanSallback);
                break;
            default:
                break;
        }
        future = ThreadPool.executeDelay(() -> {
            future = null;
            stopScan();
            if (callback != null) {
                callback.onFail(FragmentCallback.TIMEOUT, getString(R.string.core_scan_timeout));
            }
        }, 60 * 1000);
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<String> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        stopScan();
        return super.onBack();
    }

    @Override
    public void onDestroy() {
        stopScan();
        super.onDestroy();
    }

    private void stopScan() {
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        isFinish = true;
        externScanner.close();
    }


}
