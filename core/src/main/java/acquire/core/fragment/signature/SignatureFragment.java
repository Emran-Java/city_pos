package acquire.core.fragment.signature;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.base.widget.keyboard.listener.ViewKeyboardListener;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentSignatureBinding;
import acquire.core.tools.PhysicalKeyboardUtils;
import acquire.sdk.device.BDevice;

/**
 * A signature {@link androidx.fragment.app.Fragment}
 *
 * @author Janson
 * @date 2022/8/5 9:07
 */
public class SignatureFragment extends BaseFragment {
    private FragmentCallback<Bitmap> callback;

    public static SignatureFragment newInstance(FragmentCallback<Bitmap> callback) {
        SignatureFragment fragment = new SignatureFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        CoreFragmentSignatureBinding binding = CoreFragmentSignatureBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        //clear signature button
        binding.btnClear.setOnClickListener(v -> {
            if (ViewUtils.isFastClick()) {
                return;
            }
            LoggerUtils.e("clear signature.");
            binding.handWrite.clear();
        });
        //complete button
        binding.btnDone.setOnClickListener(v -> {
            if (ViewUtils.isFastClick()) {
                return;
            }
            LoggerUtils.d("confirm signature.");
            if (binding.handWrite.isEmpty()) {
                LoggerUtils.e("skip signature.");
                callback.onFail(FragmentCallback.FAIL, getString(R.string.core_sign_skip));
                return;
            }
            //check valid
            if (binding.handWrite.isInValid()) {
                LoggerUtils.e("Signature data is not valid.");
                ToastUtils.showToast(R.string.core_sign_not_valid);
                return;
            }
            //confirm signature prompt
            new MessageDialog.Builder(mActivity)
                    .setMessage(R.string.core_sign_confirm_complete)
                    .setConfirmButton(R.string.base_ok, dialog -> {
                        LoggerUtils.d("save signature bitmap.");
                        //bmpWidth shouldn't be greater than receipt width.
                        int bmpWidth = 384;
                        int bmpHeight = 192;
                        if (BDevice.isCpos()){
                            bmpWidth = 576;
                            bmpHeight = 288;
                        }
                        Bitmap signature = binding.handWrite.getBitmap(bmpWidth, bmpHeight);
                        callback.onSuccess(signature);
                    })
                    .setCancelButton(R.string.base_cancel, dialog -> {
                    })
                    .show();
        });
        //check physical keyboard
        if (BDevice.supportPhysicalKeyboard()){
            PhysicalKeyboardUtils.setKeyboardListener(binding.handWrite, new ViewKeyboardListener(mActivity) {
                @Override
                public String getText() {
                    return "";
                }

                @Override
                public void setText(String text) {

                }

                @Override
                public void onEnter() {
                    binding.btnDone.callOnClick();
                }
            });
        }
        return binding.getRoot();
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
                    LoggerUtils.e("Signature cancelled!");
                    callback.onFail(FragmentCallback.FAIL, getString(R.string.core_transaction_result_user_cancel));
                })
                .setCancelButton(R.string.base_cancel, dialog -> {
                })
                .show();
        return true;
    }
}
