package acquire.core.fragment.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.InputUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentLoginBinding;

/**
 * Input login user&password
 *
 * @author Janson
 * @date 2022/10/8 14:42
 */
public class LoginFragment extends BaseFragment {
    private CoreFragmentLoginBinding binding;
    private FragmentCallback<String[]> callback;
    public static LoginFragment newInstance( FragmentCallback<String[]> callback) {
        LoginFragment fragment = new LoginFragment();
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentLoginBinding.inflate(inflater,container,false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v->mActivity.getOnBackPressedDispatcher().onBackPressed());
        ThreadPool.postDelayOnMain(() -> InputUtils.showKeyboard(binding.etUser), 500);
        binding.btnDone.setOnClickListener(v->{
            String user = binding.etUser.getText().toString();
            String password = binding.etPassword.getText().toString();
            if (TextUtils.isEmpty(user)){
                binding.tilUser.setError(getString(R.string.core_login_user_empty));
                ViewUtils.setFocus(binding.etUser);
                return;
            }
            if (TextUtils.isEmpty(password)){
                ViewUtils.setFocus(binding.etPassword);
                return;
            }
            InputUtils.hideKeyboard(mActivity);
            callback.onSuccess(new String[]{user,password});
        });
        binding.etUser.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                binding.tilUser.setError(null);
            }
        });
        return binding.getRoot();
    }

    @Override
    public FragmentCallback<String[]> getCallback() {
        return callback;
    }

}
