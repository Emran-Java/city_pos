package acquire.core.fragment.qrcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.BitmapUtils;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.qrcode.QRCodeUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentQrCodeBinding;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * A {@link Fragment} that displays the QR code
 *
 * @author Janson
 * @date 2020/9/2 14:02
 */
public class QrCodeFragment extends BaseFragment {

    private CoreFragmentQrCodeBinding binding;
    private FragmentCallback<String> callback;
    private String amountPrompt;
    private IQrCodeRequester qrCodeRequester;
    private boolean isStop;

    @NonNull
    public static QrCodeFragment newInstance(String amountPrompt,IQrCodeRequester qrCodeRequester, FragmentCallback<String> callback) {
        QrCodeFragment fragment = new QrCodeFragment();
        fragment.amountPrompt = amountPrompt;
        fragment.callback = callback;
        fragment.qrCodeRequester = qrCodeRequester;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentQrCodeBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        binding.tvAmount.setText(amountPrompt);
        binding.cvContainer.setEnabled(false);
        binding.cvContainer.setOnClickListener(v -> requestQrCode());
        //request QR code
        requestQrCode();
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<String> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        isStop = true;
        return false;
    }

    private void requestQrCode(){
        ThreadPool.execute(()->{
            if (isStop){
                return;
            }
            String qrCode = qrCodeRequester.requestQrCode();
            if (qrCode == null){
                mActivity.runOnUiThread(()->{
                    binding.ivQrcode.setImageResource(R.drawable.core_net_error);
                    binding.tvContent.setText(R.string.core_qrcode_net_error_please_retry);
                    binding.cvContainer.setEnabled(true);
                });
            }else{
                int qrLen = binding.ivQrcode.getWidth();
                Bitmap bitmap = QRCodeUtils.create2dCode(qrCode, qrLen);
                if (bitmap == null){
                    callback.onFail(FragmentCallback.FAIL, getString(R.string.core_qrcode_create_fail));
                    return;
                }
                mActivity.runOnUiThread(()->{
                    binding.cvContainer.setEnabled(false);
                    binding.ivQrcode.setImageBitmap(bitmap);
                    binding.tvContent.setText(R.string.core_qrcode_please_scan);
                });
                queryResult(qrCode);
            }
        });

    }

    private void queryResult(String qrCode){
        if (isStop){
            return;
        }
        if (qrCodeRequester.queryResult()){
            callback.onSuccess(qrCode);
        }else{
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LoggerUtils.e("queryResult Thread sleep error!",e);
            }
            queryResult(qrCode);
        }
    }
    @Override
    public void onDestroy() {
        isStop = true;
        super.onDestroy();
    }

}