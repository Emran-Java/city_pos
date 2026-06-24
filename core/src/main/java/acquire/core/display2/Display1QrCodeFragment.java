package acquire.core.display2;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseDialogFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.qrcode.QRCodeUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentDisplay1QrcodeBinding;
import acquire.core.databinding.CorePresentationQrcodeBinding;
import acquire.core.fragment.qrcode.IQrCodeRequester;


/**
 * A display1 qr code {@link Fragment} for dual screen
 *
 * @author Janson
 * @date 2022/9/8 13:53
 */
public class Display1QrCodeFragment extends BaseDialogFragment {
    private FragmentCallback<String> callback;
    private IQrCodeRequester qrCodeRequester;
    private boolean isStop;
    @NonNull
    public static Display1QrCodeFragment newInstance(IQrCodeRequester qrCodeRequester, FragmentCallback<String> callback) {
        Display1QrCodeFragment fragment = new Display1QrCodeFragment();
        fragment.callback = callback;
        fragment.qrCodeRequester = qrCodeRequester;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentDisplay1QrcodeBinding fragmentBinding = CoreFragmentDisplay1QrcodeBinding.inflate(inflater, container, false);
        QrCodePresentation presentation = new QrCodePresentation(mActivity);
        presentation.show();
        fragmentBinding.btnExit.setOnClickListener(v -> {
            isStop = true;
        });
        return fragmentBinding.getRoot();
    }


    @Override
    public int[] getPopAnimation() {
        return null;
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

    @Override
    public void onDestroy() {
        isStop = true;
        super.onDestroy();
    }

    private class QrCodePresentation extends BasePresentation {
        public QrCodePresentation(Context outerContext) {
            super(outerContext);
        }
        private CorePresentationQrcodeBinding presentationBinding;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            presentationBinding = CorePresentationQrcodeBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
            requestQrCode();
        }

        private void requestQrCode(){
            ThreadPool.execute(()->{
                if (isStop){
                    return;
                }
                String qrCode = qrCodeRequester.requestQrCode();
                if (qrCode == null){
                    mActivity.runOnUiThread(()->
                        new MessageDialog.Builder(mActivity)
                                .setMessage(R.string.core_qrcode_net_error)
                                .setConfirmButton(R.string.core_qrcode_retry,dialog -> requestQrCode())
                                .setCancelButton(dialog -> callback.onFail(FragmentCallback.CANCEL, getString(R.string.base_fragment_callback_cancel)))
                                .show()
                    );
                }else{
                    Bitmap bitmap = QRCodeUtils.create2dCode(qrCode,600);
                    if (bitmap == null){
                        callback.onFail(FragmentCallback.FAIL, getString(R.string.core_qrcode_create_fail));
                        return;
                    }
                    mActivity.runOnUiThread(()-> presentationBinding.ivQrcode.setImageBitmap(bitmap));
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
                    LoggerUtils.e("Thread sleep error !",e);
                }
                queryResult(qrCode);
            }
        }
    }


}
