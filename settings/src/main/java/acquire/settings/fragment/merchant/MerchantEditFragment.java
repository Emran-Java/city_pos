package acquire.settings.fragment.merchant;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputLayout;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.InputUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.StringUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.constant.CardSchemes;
import acquire.core.constant.PasswordType;
import acquire.core.fragment.password.PasswordFragment;
import acquire.database.model.Merchant;
import acquire.database.model.ReversalData;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;
import acquire.database.repository.ReversalDataRepository;
import acquire.sdk.pin.constant.KeyAlgorithmType;
import acquire.settings.R;
import acquire.settings.databinding.SettingsFragmentMerchantEditBinding;

/**
 * A {@link Fragment} that displays and configures merchant information.
 *
 * @author Janson
 * @date 2019/2/14 15:12
 */
public class MerchantEditFragment extends BaseFragment {
    private SettingsFragmentMerchantEditBinding binding;
    private Merchant merchant;
    private FragmentCallback<Merchant> callback;
    private boolean isModified;
    private boolean editStatus;
    private final Map<EditText, TextInputLayout> itemViews = new HashMap<>();

    /**
     * create a fragment to edit merchant information.
     *
     * @param merchant if null, it is to add a merchant; else, it is to modify a merchant.
     * @param callback the editing result.
     * @return A fragment to edit merchant information.
     */
    @NonNull
    public static MerchantEditFragment newInstance(@Nullable Merchant merchant, FragmentCallback<Merchant> callback) {
        MerchantEditFragment fragment = new MerchantEditFragment();
        if (merchant != null){
            fragment.merchant = merchant;
        }else{
            fragment.editStatus = true;
        }
        fragment.callback = callback;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SettingsFragmentMerchantEditBinding.inflate(inflater, container, false);
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        MerchantRepository merchantRepository = new MerchantRepository();
        //card scheme
        List<String> items = new ArrayList<>();
        for (Field field : CardSchemes.class.getDeclaredFields()) {
            if (field.getType() == String.class
                    && (Modifier.FINAL & field.getModifiers()) != 0
                    && (Modifier.STATIC & field.getModifiers()) != 0) {
                try {
                    String cardScheme = (String) field.get(CardSchemes.class);
                    if (merchantRepository.findByType(cardScheme) == null) {
                        items.add(cardScheme);
                    }
                } catch (IllegalAccessException e) {
                    LoggerUtils.e("Can not access field ["+field+"]!",e);
                }
            }
        }
        if (merchant != null) {
            String currentType = merchant.getType();
            binding.toolbar.setTitle(currentType);
            if (!items.contains(currentType)) {
                items.add(currentType);
            }
            binding.spType.setText(currentType);
            binding.spType.setSimpleItems(items.toArray(new String[0]));
        } else {
            merchant = new Merchant();
            binding.spType.setSimpleItems(items.toArray(new String[0]));
            binding.btnDone.setVisibility(View.VISIBLE);
        }
        binding.spType.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                InputUtils.hideKeyboard(binding.spType);
            }
        });
        addClearErrorWatch(binding.tilType, binding.spType);

        //merchant id
        binding.etMid.setText(merchant.getMid());
        addClearErrorWatch(binding.tilMid, binding.etMid);

        //terminal id
        binding.etTid.setText(merchant.getTid());
        addClearErrorWatch(binding.tilTid, binding.etTid);

        //batch No
        binding.etBatch.setText(merchant.getBatchNo());
        addClearErrorWatch(binding.tilBatch, binding.etBatch);
        //ip
        binding.etIp.setText(merchant.getIp());
        addClearErrorWatch(binding.tilIp, binding.etIp);
        //port
        binding.etPort.setText(String.valueOf(merchant.getPort()));
        addClearErrorWatch(binding.tilPort, binding.etPort);
        //timeout
        binding.etTimeout.setText(String.valueOf(merchant.getCommTimeout()));
        addClearErrorWatch(binding.tilTimeout, binding.etTimeout);
        //tpdu
        binding.etTpdu.setText(merchant.getTpdu());
        addClearErrorWatch(binding.tilTpdu, binding.etTpdu);
        //nii
        binding.etNii.setText(merchant.getNii());
        addClearErrorWatch(binding.tilNii, binding.etNii);
        //key index
        binding.etKeyIndex.setText(String.valueOf(merchant.getMasterKeyIndex()));
        addClearErrorWatch(binding.tilKeyIndex, binding.etKeyIndex);
        //Algorithm
        String mkskText = getString(R.string.settings_merchant_edit_mksk);
        String dukptText = getString(R.string.settings_merchant_edit_dukpt);
        if (merchant.getAlgorithm() == KeyAlgorithmType.DUKPT){
            binding.spAlgorithm.setText(dukptText);
        }else{
            binding.spAlgorithm.setText(mkskText);
        }
        binding.spAlgorithm.setSimpleItems(new String[]{mkskText,dukptText});
        addClearErrorWatch(binding.tilAlgorithm, binding.spAlgorithm);
        if (editStatus){
            for (TextInputLayout textInputLayout : itemViews.values()) {
                textInputLayout.setEnabled(true);
            }
        }else{
            binding.toolbar.setRightIcon(ContextCompat.getDrawable(mActivity,R.drawable.setttings_merchant_edit));
            binding.toolbar.setRightListener(v->{
                new MessageDialog.Builder(mActivity)
                        .setTitle(R.string.settings_merchant_edit_dialog_message)
                        .setCancelButton(d->{})
                        .setConfirmButton(d->{
                            binding.toolbar.setRightIcon(null);
                            setEditStatus();
                        })
                        .show();
            });
            binding.btnDone.setVisibility(View.GONE);
        }
        //confirm button
        binding.btnDone.setOnClickListener(v->{
            for (Map.Entry<EditText, TextInputLayout> entry : itemViews.entrySet()) {
                EditText editText = entry.getKey();
                TextInputLayout textInputLayout = entry.getValue();
                if (TextUtils.isEmpty(editText.getText())) {
                    textInputLayout.setError(mActivity.getString(R.string.settings_merchant_edit_item_error));
                    ToastUtils.showToast(R.string.settings_merchant_edit_null_error);
                    return;
                }
            }
            String type = binding.spType.getText().toString();
            String mid = binding.etMid.getText().toString();
            String tid = binding.etTid.getText().toString();
            String batch = binding.etBatch.getText().toString();
            String ip = binding.etIp.getText().toString();
            int port = Integer.parseInt(binding.etPort.getText().toString());
            String tpdu = binding.etTpdu.getText().toString();
            String nii = binding.etNii.getText().toString();
            int timeout = Integer.parseInt(binding.etTimeout.getText().toString());
            int keyIndex = Integer.parseInt(binding.etKeyIndex.getText().toString());
            int algorithm ;
            if (dukptText.equals(binding.spAlgorithm.getText().toString())){
                algorithm = KeyAlgorithmType.DUKPT;
            }else{
                algorithm = KeyAlgorithmType.MKSK;
            }
            if (mid.length() != 15) {
                binding.tilMid.setError(mActivity.getString(R.string.settings_merchant_edit_mid_error_length));
                ToastUtils.showToast(R.string.settings_merchant_edit_null_error);
                return;
            }
            if (tid.length() != 8) {
                binding.tilTid.setError(mActivity.getString(R.string.settings_merchant_edit_tid_error_length));
                ToastUtils.showToast(R.string.settings_merchant_edit_null_error);
                return;
            }
            if (timeout < 10) {
                binding.tilTimeout.setError(mActivity.getString(R.string.settings_merchant_edit_timeout_error_length));
                ToastUtils.showToast(R.string.settings_merchant_edit_null_error);
                return;
            }
            merchant.setType(type);
            merchant.setMid(mid);
            merchant.setTid(tid);
            merchant.setBatchNo(StringUtils.fill(batch, "0", 6, true));
            merchant.setIp(ip);
            merchant.setPort(port);
            merchant.setTpdu(tpdu);
            merchant.setNii(nii);
            merchant.setCommTimeout(timeout);
            merchant.setMasterKeyIndex(keyIndex);
            merchant.setAlgorithm(algorithm);
            if (callback != null){
                callback.onSuccess(merchant);
            }

        });
        return binding.getRoot();
    }

    private void addClearErrorWatch(TextInputLayout textInputLayout, EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                textInputLayout.setError(null);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                isModified = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        itemViews.put(editText, textInputLayout);
    }

    private void setEditStatus(){
        String mid = merchant.getMid();
        String tid = merchant.getTid();
        RecordRepository recordRepository = new RecordRepository();
        if (recordRepository.getCountByMidTid(mid, tid) > 0) {
            ToastUtils.showToast(R.string.settings_settle_record_exist);
            return ;
        }
        ReversalData reversalData = new ReversalDataRepository().getReverseRecord();
        if (reversalData != null && reversalData.getMid().equals(mid) && reversalData.getTid().equals(tid)) {
            ToastUtils.showToast(R.string.settings_settle_record_exist);
            return ;
        }
        mSupportDelegate.switchContent(PasswordFragment.newInstance(getString(R.string.settings_menu_item_merchant), PasswordType.SECURITY, new FragmentCallback<String>() {

            @Override
            public void onSuccess(String password) {
                ViewUtils.isFastClick();
                for (TextInputLayout textInputLayout : itemViews.values()) {
                    textInputLayout.setEnabled(true);
                }
                editStatus = true;
                ViewUtils.setFocus(binding.spType);
                binding.btnDone.setVisibility(View.VISIBLE);
                mSupportDelegate.popBackFragment(1);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                mSupportDelegate.popBackFragment(1);
            }
        }));
    }
    @Override
    public boolean onBack() {
        if (isModified) {
            new MessageDialog.Builder(mActivity)
                    .setMessage(R.string.settings_merchant_edit_exit_prompt)
                    .setCancelButton(R.string.base_cancel, v -> {
                    })
                    .setConfirmButton(R.string.settings_merchant_edit_dialog_discard, v -> callback.onFail(FragmentCallback.CANCEL, getString(R.string.base_exit)))
                    .show();
        } else {
            callback.onFail(FragmentCallback.CANCEL, getString(R.string.base_exit));
        }
        return true;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }

}
