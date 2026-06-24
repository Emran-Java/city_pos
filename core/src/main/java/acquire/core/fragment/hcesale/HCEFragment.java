package acquire.core.fragment.hcesale;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.concurrent.ScheduledFuture;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentHceSaleBinding;
import acquire.core.databinding.CorePresentationHceBinding;
import acquire.core.display2.BasePresentation;
import acquire.core.trans.pack.json.npi_sale.HCESaleResp;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;
import acquire.sdk.hce.BCardEmulator;

/**
 * A {@link Fragment} that displays the hce
 */
public class HCEFragment extends BaseFragment {

    private CoreFragmentHceSaleBinding binding;
    private FragmentCallback<String> callback;
    private HCERequester hceRequester;
    private String amountPrompt;
    private HcePresentation hcePresentation ;

    private ScheduledFuture<?> future;
    private boolean isStop = false;
    private boolean isDecoded = false;

    @NonNull
    public static HCEFragment newInstance(String amountPrompt, HCERequester hceRequester, FragmentCallback<String> callback) {
        HCEFragment fragment = new HCEFragment();
        fragment.callback = callback;
        fragment.hceRequester = hceRequester;
        fragment.amountPrompt = amountPrompt;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentHceSaleBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        binding.tvAmount.setText(amountPrompt);
        binding.cvContainer.setEnabled(false);

        if (Model.X800.equals(BDevice.getDeviceModel())) {
            //X800
            hcePresentation = new HcePresentation(mActivity);
            hcePresentation.show();
        }


        ThreadPool.execute(() -> {
            if (isStop) {
                return;
            }
            HCESaleResp hceSaleResp = hceRequester.createOrder() ;
            if (hceSaleResp.getCode() != 200 || TextUtils.isEmpty(hceSaleResp.getUrl())){
                LoggerUtils.d("createOrder failed");
                callback.onFail(FragmentCallback.FAIL, hceSaleResp.getMsg());
                return;
            }

            BCardEmulator.getInstance(mActivity).startDetectForUrl(hceSaleResp.getUrl(), new BCardEmulator.DeviceReadListener() {
                @Override
                public void onDetected() {
                    if (!isDecoded) {
                        queryResult();
                        isDecoded = true;
                    }
                }

                @Override
                public void onError(String msg) {
                    callback.onFail(FragmentCallback.FAIL, msg);
                }
            });


            mActivity.runOnUiThread(() -> {
                binding.ivHce.setImageResource(R.drawable.core_hce_tap_phone);
                binding.tvContent.setText(getString(R.string.core_hce_read_device));
            });

        });

        //timeout 60s
        future = ThreadPool.executeDelay(() -> {
            future = null;
            stopHCE();
            if (callback != null) {
                callback.onFail(FragmentCallback.TIMEOUT, getString(R.string.core_hce_timeout));
            }
        }, 60 * 1000);
        return binding.getRoot();
    }


    private void queryResult() {
        if (isStop) {
            return;
        }
        HCESaleResp resp = hceRequester.queryResult();
        if (resp.getCode() == 200 && resp.getState() == HCESaleResp.STATE_PIAD) {
            callback.onSuccess("");
        } else if (resp.getCode() == 501 || resp.getCode() == 502 || resp.getCode() == 503) {
            callback.onFail(FragmentCallback.FAIL, resp.getMsg());
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            queryResult();
        }
    }


    @Override
    public FragmentCallback<String> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        stopHCE();
        return false;
    }

    @Override
    public void onDestroy() {
        stopHCE();
        super.onDestroy();
    }

    private void stopHCE(){
        if (future != null) {
            future.cancel(true);
            future = null;
        }
        isStop = true;
        ThreadPool.execute(() -> BCardEmulator.getInstance(mActivity).stopDetect());
    }

    private class HcePresentation extends BasePresentation {
        public HcePresentation(Context outerContext) {
            super(outerContext);
        }
        private CorePresentationHceBinding presentationBinding;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            presentationBinding = CorePresentationHceBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
        }
    }
}