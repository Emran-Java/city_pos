package acquire.core.fragment.receipt;

import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentHceReceiptBinding;
import acquire.core.databinding.CorePresentationHceReceiptBinding;
import acquire.core.display2.BasePresentation;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;
import acquire.sdk.hce.BCardEmulator;

public class HCEReceiptFragment extends BaseDialogFragment {


    private FragmentCallback<Void> callback;
    private String url;
    private CoreFragmentHceReceiptBinding binding ;
    private HceReceiptPresentation hcePresentation ;

    CountDownTimer timer ;


    public static HCEReceiptFragment newInstance(String url, FragmentCallback<Void> callback){
        HCEReceiptFragment fragment = new HCEReceiptFragment();
        fragment.url = url;
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentHceReceiptBinding.inflate(inflater, container, false);
        if (Model.X800.equals(BDevice.getDeviceModel())) {
            //X800
            hcePresentation = new HceReceiptPresentation(mActivity);
            hcePresentation.show();
        }
        binding.btnCancel.setOnClickListener(view -> complete());
        LoggerUtils.d("HCE RECEIPT startDetectForUrl");
        BCardEmulator.getInstance(mActivity).startDetectForUrl(url, new BCardEmulator.DeviceReadListener() {
            @Override
            public void onDetected() {
                LoggerUtils.d("HCE RECEIPT onDetected");
                complete();
            }

            @Override
            public void onError(String msg) {
                ToastUtils.showToast(msg);
                LoggerUtils.d("HCE RECEIPT onError");
                complete();

            }
        });
        //count down timer
        timer = new CountDownTimer(30 * 1000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                int countDown = (int) Math.ceil(millisUntilFinished / 1000f);
                String content = String.format(Locale.getDefault(),"%s(%d)",
                        mActivity.getString(R.string.base_cancel), countDown );
                binding.btnCancel.setText(content);
            }

            @Override
            public void onFinish() {
                LoggerUtils.d("HCE RECEIPT onFinish");
                complete();
            }
        }.start();
        return binding.getRoot();
    }

    @Override
    public FragmentCallback getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        LoggerUtils.d("HCE RECEIPT onBack");
        complete();
        return true;
    }

    public void complete(){
        if (timer != null) {
            timer.cancel();
        }
        if (callback != null) {
            callback.onSuccess(null);
            callback = null;
        }
        ThreadPool.execute(() -> {
            BCardEmulator.getInstance(mActivity).stopDetect();
            if (hcePresentation != null){
                hcePresentation.dismiss();
            }
        });
    }

//    @Override
//    public void onDestroy() {
//        if (timer != null) {
//            timer.cancel();
//        }
//        ThreadPool.execute(() -> {
//            BCardEmulator.getInstance(mActivity).stopDetect();
//            if (hcePresentation != null){
//                hcePresentation.dismiss();
//            }
//        });
//        super.onDestroy();
//    }


    private class HceReceiptPresentation extends BasePresentation {
        public HceReceiptPresentation(Context outerContext) {
            super(outerContext);
        }
        private CorePresentationHceReceiptBinding presentationBinding;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            presentationBinding = CorePresentationHceReceiptBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
        }
    }

}
