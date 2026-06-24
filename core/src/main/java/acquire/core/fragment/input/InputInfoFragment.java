package acquire.core.fragment.input;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Arrays;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.InputUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.base.widget.keyboard.BaseKeyboard;
import acquire.base.widget.keyboard.listener.EditKeyboardListener;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentInputInfoBinding;
import acquire.sdk.device.BDevice;


/**
 * Input information in this {@link Fragment}
 *
 * @author Janson
 * @date 2019/7/25 11:20
 */
public class InputInfoFragment extends BaseFragment {
    private CoreFragmentInputInfoBinding binding;
    private FragmentCallback<String> mCallback;
    private InputInfoFragmentArgs args;

    @NonNull
    public static InputInfoFragment newInstance(InputInfoFragmentArgs args, FragmentCallback<String> callback) {
        InputInfoFragment fragment = new InputInfoFragment();
        fragment.mCallback = callback;
        fragment.args = args;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentInputInfoBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v->mActivity.getOnBackPressedDispatcher().onBackPressed());
        binding.tilInputInfo.setHint(args.getHint());
        binding.tilInputInfo.setCounterMaxLength(args.getMaxLen());
        if (args.getFilters() != null) {
            InputFilter[] filters = Arrays.copyOf(args.getFilters(), args.getFilters().length + 1);
            filters[filters.length - 1] = new InputFilter.LengthFilter(args.getMaxLen());
            binding.etInputInfo.setFilters(filters);
        } else {
            binding.etInputInfo.setFilters(new InputFilter[]{new InputFilter.LengthFilter(args.getMaxLen())});
        }
        ViewUtils.setFocus(binding.etInputInfo);
        binding.etInputInfo.setInputType(args.getInputType());
        if (args.getInputType() == InputType.TYPE_CLASS_NUMBER) {
            //Custom number keyboard
            binding.tvDone.setVisibility(View.GONE);
            binding.keyboardNumber.setVisibility(View.VISIBLE);
            binding.keyboardNumber.findKey(BaseKeyboard.K_ENTER).setEnabled(false);
            binding.keyboardNumber.setKeyBoardListener(new EditKeyboardListener(mActivity,binding.etInputInfo, args.getMaxLen()) {
                @Override
                public void onEnter() {
                    if (ViewUtils.isFastClick()) {
                        return;
                    }
                    done(binding.etInputInfo.getText().toString());
                }
            });
            //check physical keyboard
            if (BDevice.supportPhysicalKeyboard()){
                binding.keyboardNumber.setVisibility(View.GONE);
            }
        } else {
            //System keyboard
            ThreadPool.postDelayOnMain(() -> InputUtils.showKeyboard(binding.etInputInfo), 500);
            binding.tvDone.setOnClickListener(v -> done(binding.etInputInfo.getText().toString()));
        }
        binding.etInputInfo.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterAction = actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
            if (enterAction && isShowing()) {
                if (ViewUtils.isFastClick()) {
                    return true;
                }
                done(binding.etInputInfo.getText().toString());
                return true;
            }
            return false;
        });
        binding.etInputInfo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tilInputInfo.setError(null);
                if (View.VISIBLE == binding.keyboardNumber.getVisibility()) {
                    binding.keyboardNumber.findKey(BaseKeyboard.K_ENTER)
                            .setEnabled(s.length() != 0);
                }
            }
        });
        return binding.getRoot();
    }

    /**
     * input complete
     */
    private void done(String text) {
        if (TextUtils.isEmpty(text)) {
            binding.tilInputInfo.setError(getString(R.string.core_input_info_require_not_null));
            return;
        }
        if (text.length() < args.getMinLen()) {
            binding.tilInputInfo.setError(getString(R.string.core_input_info_length_min_limit) + args.getMinLen());
            return;
        }
        InputUtils.hideKeyboard(binding.etInputInfo);
        mCallback.onSuccess(text);
    }

    @Override
    public FragmentCallback<String> getCallback() {
        return mCallback;
    }

}
