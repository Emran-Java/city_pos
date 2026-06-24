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
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentDisplay1SignatureBinding;
import acquire.core.databinding.CorePresentationSignatureBinding;
import acquire.sdk.device.BDevice;


/**
 * A display1 pinpad {@link Fragment} for dual screen
 *
 * @author Janson
 * @date 2022/9/8 13:53
 */
public class Display1SignatureFragment extends BaseDialogFragment {
    private FragmentCallback<Bitmap> callback;

    @NonNull
    public static Display1SignatureFragment newInstance(FragmentCallback<Bitmap> callback) {
        Display1SignatureFragment fragment = new Display1SignatureFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Override
    public View onCreateDialogView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        acquire.core.databinding.CoreFragmentDisplay1SignatureBinding fragmentBinding = CoreFragmentDisplay1SignatureBinding.inflate(inflater, container, false);
        SignaturePresentation presentation = new SignaturePresentation(mActivity);
        presentation.show();
        fragmentBinding.btnExit.setOnClickListener(v ->onBack());
        return fragmentBinding.getRoot();
    }


    @Override
    public int[] getPopAnimation() {
        return null;
    }
    @Override
    public FragmentCallback<Bitmap> getCallback() {
        return callback;
    }

    @Override
    public boolean onBack() {
        new MessageDialog.Builder(mActivity)
                .setMessage(R.string.core_sign_abort_prompt)
                .setConfirmButton(R.string.base_ok, dialog -> {
                    LoggerUtils.e("ElecSign cancelled!");
                    callback.onFail(FragmentCallback.FAIL, getString(R.string.core_transaction_result_user_cancel));
                })
                .setCancelButton(R.string.base_cancel, dialog -> {
                })
                .show();
        return true;
    }


    private class SignaturePresentation extends BasePresentation {
        private CorePresentationSignatureBinding presentationBinding;

        public SignaturePresentation(Context outerContext) {
            super(outerContext);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            presentationBinding = CorePresentationSignatureBinding.inflate(LayoutInflater.from(getContext()));
            setContentView(presentationBinding.getRoot());
            //clear signature button
            presentationBinding.btnClear.setOnClickListener(v -> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                LoggerUtils.e("clear signature.");
                presentationBinding.handWrite.clear();
            });
            //complete button
            presentationBinding.btnDone.setOnClickListener(v -> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                LoggerUtils.d("confirm signature.");
                if (presentationBinding.handWrite.isEmpty()){
                    //skip
                    LoggerUtils.e("skip signature.");
                    callback.onFail(FragmentCallback.FAIL, getString(R.string.core_sign_skip));
                    return;
                }
                //check valid
                if (presentationBinding.handWrite.isInValid()) {
                    LoggerUtils.e("Signature data is not valid.");
                    ToastUtils.showToast(R.string.core_sign_not_valid);
                    return;
                }
                LoggerUtils.d("save signature bitmap.");
                //bmpWidth shouldn't be greater than receipt width.
                int bmpWidth = 384;
                int bmpHeight = 192;
                if (BDevice.isCpos()){
                    bmpWidth = 576;
                    bmpHeight = 288;
                }
                Bitmap signature = presentationBinding.handWrite.getBitmap(bmpWidth, bmpHeight);
                callback.onSuccess(signature);
            });
        }

    }


}
