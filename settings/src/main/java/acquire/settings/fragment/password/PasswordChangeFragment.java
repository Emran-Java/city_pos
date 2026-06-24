package acquire.settings.fragment.password;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Objects;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.crypto.ShaUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PasswordType;
import acquire.settings.R;
import acquire.settings.databinding.SettingsFragmentPasswordChangeBinding;

/**
 * A {@link Fragment} that changes authorization password.
 *
 * @author Janson
 * @date 2019/8/9 9:09
 */
public class PasswordChangeFragment extends BaseFragment {
    private SettingsFragmentPasswordChangeBinding binding;
    private final static String ARG_TYPE ="type";
    private @PasswordType.TypeDef int type;
    private SimpleCallback callback;
    @NonNull
    public static PasswordChangeFragment newInstance(@PasswordType.TypeDef int type, SimpleCallback callback){
        PasswordChangeFragment fragment = new PasswordChangeFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TYPE,type);
        fragment.setArguments(args);
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentPasswordChangeBinding.inflate(inflater,container,false);
        if (getArguments() != null){
            type = getArguments().getInt(ARG_TYPE);
        }
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        String password;
        int maxPasswordLength;
        switch (type){
            case PasswordType.SECURITY:
                binding.toolbar.setTitle(R.string.settings_password_item_security);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_SECURITY);
                maxPasswordLength = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PASSWORD_SECURITY_LENGTH,6);
                break;
            case PasswordType.SYSTEM_ADMIN:
                binding.toolbar.setTitle(R.string.settings_password_item_system_admin);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN);
                maxPasswordLength = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN_LENGTH,6);
                break;
            case PasswordType.ADMIN:
                binding.toolbar.setTitle(R.string.settings_password_item_admin);
                password = ParamsUtils.getString(ParamsConst.PARAMS_KEY_PASSWORD_ADMIN);
                maxPasswordLength = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PASSWORD_ADMIN_LENGTH,6);
                break;
            default:
                throw new IllegalArgumentException("PasswordChangeFragment type error!");
        }
        binding.tilOldPassword.setCounterMaxLength(maxPasswordLength);
        binding.etOldPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxPasswordLength)});
        binding.tilNewPassword.setCounterMaxLength(maxPasswordLength);
        binding.etNewPassword.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxPasswordLength)});
        binding.tilNewPassword2.setCounterMaxLength(maxPasswordLength);
        binding.etNewPassword2.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxPasswordLength)});

        binding.etOldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tilOldPassword.setError(null);
            }
        });
        binding.etNewPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tilNewPassword.setError(null);
            }
        });
        binding.etNewPassword2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.tilNewPassword2.setError(null);
            }
        });
        binding.btnDone.setOnClickListener(v->{
            String oldPassword = binding.etOldPassword.getText().toString();
            String newPassword = binding.etNewPassword.getText().toString();
            String newPassword2 = binding.etNewPassword2.getText().toString();
            String lengthError = getString(R.string.settings_password_error_password_length, maxPasswordLength);
            //Orig. password is null
            if (oldPassword.isEmpty()){
                binding.tilOldPassword.setError(getString(R.string.settings_password_error_password_null));
                return;
            }
            //New password length wrong
            if (oldPassword.length() != maxPasswordLength){
                binding.tilOldPassword.setError(lengthError);
                return;
            }
            //Orig. password is wrong
            if (!Objects.equals(BytesUtils.bcdToString(ShaUtils.sha256(oldPassword.getBytes())), password)){
                binding.tilOldPassword.setError(getString(R.string.settings_password_error_old_password_incorrect));
                return;
            }
            //New password is null
            if (newPassword.isEmpty()){
                binding.tilNewPassword.setError(getString(R.string.settings_password_error_password_null));
                return;
            }
            //New password length wrong
            if (newPassword.length() != maxPasswordLength){
                binding.tilNewPassword.setError(lengthError);
                return;
            }
            //Confirm password is null
            if (newPassword2.isEmpty()){
                binding.tilNewPassword2.setError(getString(R.string.settings_password_error_password_null));
                return;
            }
            //Confirm password length wrong
            if (newPassword2.length() != maxPasswordLength){
                binding.tilNewPassword2.setError(lengthError);
                return;
            }
            //New password and Confirm password  is not same.
            if (!newPassword.equals(newPassword2)){
                binding.tilNewPassword2.setError(getString(R.string.settings_password_error_new_password_difference));
                return;
            }

            if (newPassword.equals(oldPassword)){
                binding.tilNewPassword.setError(getString(R.string.settings_password_error_new_password_same_old));
                return;
            }
            newPassword = BytesUtils.bcdToString(ShaUtils.sha256(newPassword.getBytes()));
            switch (type){
                case PasswordType.SECURITY:
                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_SECURITY,newPassword);
                    break;
                case PasswordType.SYSTEM_ADMIN:
                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_SYSTEM_ADMIN,newPassword);
                    break;
                case PasswordType.ADMIN:
                    ParamsUtils.setString(ParamsConst.PARAMS_KEY_PASSWORD_ADMIN,newPassword);
                    break;
                default:
                    throw new IllegalArgumentException("PasswordChangeFragment type error!");
            }
            ToastUtils.showToast(R.string.settings_password_update_password_success);
        });
        binding.etNewPassword2.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterAction = actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
            if (enterAction && isShowing()) {
                if (ViewUtils.isFastClick()) {
                    return true;
                }
                binding.btnDone.performClick();
                return true;
            }
            return false;
        });
        return binding.getRoot();
    }
    @Override
    public FragmentCallback<Void> getCallback() {
        return callback;
    }


}
