package acquire.core.fragment.card;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.keyboard.listener.EditKeyboardListener;
import acquire.core.databinding.CoreFragmentCardManualBinding;
import acquire.sdk.device.BDevice;

/**
 * Edit card number
 *
 * @author Janson
 * @date 2021/8/18 16:01
 */
public class CardManualFragment extends BaseFragment {
    private FragmentCallback<String[]> callback;
    private CoreFragmentCardManualBinding binding;

    public static CardManualFragment newInstance(FragmentCallback<String[]> callback) {
        CardManualFragment fragment = new CardManualFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentCardManualBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        CardManualViewModel viewModel = new ViewModelProvider(this).get(CardManualViewModel.class);
        //card NO
        viewModel.getCardNo().observe(getViewLifecycleOwner(), cardNo -> {
            binding.etCardNo.setText(cardNo);
            binding.etCardNo.setSelection(cardNo.length());

        });
        viewModel.getCardNoError().observe(getViewLifecycleOwner(), error -> {
            binding.tilCardNo.setError(error);
            if (error != null) {
                ViewUtils.setFocus(binding.etCardNo);
            }
        });
        //Expire date
        viewModel.getExpDate().observe(getViewLifecycleOwner(), expDate -> {
            binding.etExpdate.setText(expDate);
            binding.etExpdate.setSelection(expDate.length());
        });
        viewModel.getExpDateError().observe(getViewLifecycleOwner(), error -> {
            binding.tilExpdate.setError(error);
            if (error != null) {
                ViewUtils.setFocus(binding.etExpdate);
            }
        });
        //result
        viewModel.getResult().observe(getViewLifecycleOwner(), cardAndExpdate -> {
            callback.onSuccess(cardAndExpdate);
        });

        //Card EditText
        EditKeyboardListener cardNoKeyboardListener = new EditKeyboardListener(mActivity,binding.etCardNo) {
            @Override
            public void onEnter() {
                if (binding.etExpdate.getText().length() == 0
                        && binding.etCardNo.getText().length() != 0
                        && binding.etCardNo.isFocused()) {
                    ViewUtils.setFocus(binding.etExpdate);
                    return;
                }
                viewModel.checkResult(binding.etCardNo.getText().toString(), binding.etExpdate.getText().toString());
            }
        };
        binding.keyboardNumber.setKeyBoardListener(cardNoKeyboardListener);
        binding.etCardNo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.formatCardNumber(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.etCardNo.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.keyboardNumber.setKeyBoardListener(cardNoKeyboardListener);
            }
        });
        binding.etCardNo.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterAction = actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
            if (enterAction && isShowing()) {
                cardNoKeyboardListener.onEnter();
            }
            return true;
        });

        //Expire date EditText.
        EditKeyboardListener expdateKeyboardListener = new EditKeyboardListener(mActivity,binding.etExpdate) {
            @Override
            public void onEnter() {
                if (binding.etCardNo.getText().length() == 0
                        && binding.etExpdate.getText().length() != 0) {
                    ViewUtils.setFocus(binding.etCardNo);
                    return;
                }
                viewModel.checkResult(binding.etCardNo.getText().toString(), binding.etExpdate.getText().toString());
            }
        };
        binding.etExpdate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.formatExpDate(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        binding.etExpdate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.keyboardNumber.setKeyBoardListener(expdateKeyboardListener);
            }
        });
        binding.etExpdate.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterAction = actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
            if (enterAction && isShowing()) {
                expdateKeyboardListener.onEnter();
            }
            return true;
        });
        if (BDevice.supportPhysicalKeyboard()){
            binding.keyboardNumber.setVisibility(View.GONE);
        }
        ViewUtils.setFocus(binding.etCardNo);
        return binding.getRoot();
    }


    @Override
    public FragmentCallback<String[]> getCallback() {
        return callback;
    }
}
