package acquire.settings.fragment.merchant;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.BaseBindingRecyclerAdapter;
import acquire.base.widget.dialog.edit.EditDialog;
import acquire.base.widget.dialog.message.MessageDialog;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.PasswordType;
import acquire.core.fragment.password.PasswordFragment;
import acquire.database.model.Merchant;
import acquire.database.model.ReversalData;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;
import acquire.database.repository.ReversalDataRepository;
import acquire.settings.R;
import acquire.settings.databinding.SettingsFragmentMerchantBinding;
import acquire.settings.databinding.SettingsMerchantItemBinding;

/**
 * A {@link Fragment} that displays and configures merchant information.
 *
 * @author Janson
 * @date 2019/2/14 15:12
 */
public class SettingMerchantFragment extends BaseFragment {
    private List<Merchant> merchants;
    private final MerchantRepository merchantRepository = new MerchantRepository();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        SettingsFragmentMerchantBinding binding = SettingsFragmentMerchantBinding.inflate(inflater, container, false);
        merchants = merchantRepository.findAll();
        MerchantAdapter merchantAdapter = new MerchantAdapter();
        binding.rvMerchants.setAdapter(merchantAdapter);
        binding.toolbar.setTitle(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME));
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        binding.toolbar.setRightListener(v->
            new EditDialog.Builder(mActivity)
                    .setTitle(R.string.settings_merchant_edit_name_dialog_title)
                    .setContent(ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME))
                    .setConfirmButton((editDialog, text) -> {
                        if (TextUtils.isEmpty(text)){
                            ToastUtils.showToast(R.string.settings_merchant_edit_name_dialog_null);
                            return false;
                        }
                        ParamsUtils.setString(ParamsConst.PARAMS_KEY_BASE_MERCHANT_NAME,text);
                        binding.toolbar.setTitle(text);
                        return true;
                    })
                    .show()
        );
        //button: add a merchant
        binding.btnAdd.setOnClickListener(v -> {
            if (ViewUtils.isFastClick()) {
                return;
            }
            enterSafePassword(() ->
                    mSupportDelegate.switchContent(MerchantEditFragment.newInstance(null, new FragmentCallback<Merchant>() {
                        @Override
                        public void onSuccess(Merchant newMerchant) {
                            if (merchantRepository.findByMidTid(newMerchant.getMid(), newMerchant.getTid()) != null) {
                                ToastUtils.showToast(R.string.settings_merchant_edit_mid_error_exist);
                                return;
                            }
                            mSupportDelegate.popBackFragment(2);
                            //add merchant
                            merchantRepository.add(newMerchant);
                            merchants.add(newMerchant);
                            //refresh ui
                            merchantAdapter.notifyItemChanged(merchants.size() - 1);
                            ToastUtils.showToast(getString(R.string.settings_merchant_add_success));

                        }

                        @Override
                        public void onFail(int errorType, String errorMsg) {
                            mSupportDelegate.popBackFragment(2);
                        }
                    }))
            );
        });
        return binding.getRoot();
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }

    /**
     * Merchant recycler adapter
     *
     * @author Janson
     * @date 2021/3/18 15:45
     */
    private class MerchantAdapter extends BaseBindingRecyclerAdapter<SettingsMerchantItemBinding> {

        @Override
        protected void bindItemData(SettingsMerchantItemBinding itemBinding, int position) {
            Merchant merchant = merchants.get(position);
            itemBinding.tvType.setText(merchant.getType());
            itemBinding.tvMid.setText(merchant.getMid());
            itemBinding.tvTid.setText(merchant.getTid());
            itemBinding.tvBatch.setText(merchant.getBatchNo());
            //button: delete a merchant
            itemBinding.ivDelete.setOnClickListener(v -> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                if (merchants.size() == 1) {
                    ToastUtils.showToast(R.string.settings_merchant_not_delete_last_merchant);
                    return;
                }
                new MessageDialog.Builder(mActivity)
                        .setTitle(R.string.settings_merchant_delete_prompt)
                        .setConfirmButton(dialog -> {
                            if (!checkRecord(merchant.getMid(), merchant.getTid())) {
                                return;
                            }
                            enterSafePassword(() -> {
                                //delete merchant
                                merchantRepository.deleteByMidTid(merchant.getMid(), merchant.getTid());
                                merchants.remove(merchant);
                                //refresh ui
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, getItemCount());
                                mSupportDelegate.popBackFragment(1);
                                ToastUtils.showToast(R.string.settings_merchant_delete_completion);
                            });
                        })
                        .setCancelButton(dialog -> {
                        })
                        .show();
            });
            //modify merchant
            itemBinding.getRoot().setOnClickListener(v -> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                String oldMid = merchant.getMid();
                String oldTid = merchant.getTid();
                mSupportDelegate.switchContent(MerchantEditFragment.newInstance(merchant, new FragmentCallback<Merchant>() {
                    @Override
                    public void onSuccess(Merchant newMerchant) {
                        boolean sameOld = newMerchant.getMid().equals(oldMid) && newMerchant.getTid().equals(oldTid);
                        if (!sameOld) {
                            if (merchantRepository.findByMidTid(newMerchant.getMid(), newMerchant.getTid()) != null) {
                                ToastUtils.showToast(R.string.settings_merchant_edit_mid_error_exist);
                                return;
                            }
                        }
                        int count = new RecordRepository().getCountByMidTid(newMerchant.getMid(), newMerchant.getTid());
                        if (count > 0) {
                            ToastUtils.showToast(R.string.settings_merchant_edit_mid_error_exist);
                            return;
                        }
                        mSupportDelegate.popBackFragment(1);
                        //update merchant
                        merchantRepository.update(newMerchant);
                        merchants.set(position, newMerchant);
                        //refresh ui
                        notifyItemChanged(position);
                        ToastUtils.showToast(R.string.settings_merchant_update_success);
                    }

                    @Override
                    public void onFail(int errorType, String errorMsg) {
                        mSupportDelegate.popBackFragment(1);
                    }
                }));


            });
        }

        @Override
        public int getItemCount() {
            return merchants.size();
        }
    }

    /**
     * Check record exist
     *
     * @return true no record。 false record exist
     */
    private boolean checkRecord(String mid, String tid) {
        RecordRepository recordRepository = new RecordRepository();
        if (recordRepository.getCountByMidTid(mid, tid) > 0) {
            ToastUtils.showToast(R.string.settings_settle_record_exist);
            return false;
        }
        ReversalData reversalData = new ReversalDataRepository().getReverseRecord();
        if (reversalData != null && reversalData.getMid().equals(mid) && reversalData.getTid().equals(tid)) {
            ToastUtils.showToast(R.string.settings_settle_record_exist);
            return false;
        }
        return true;
    }

    /**
     * enter safe password
     */
    private void enterSafePassword(Runnable pass) {
        mSupportDelegate.switchContent(PasswordFragment.newInstance(getString(R.string.settings_menu_item_merchant), PasswordType.SECURITY, new FragmentCallback<String>() {

            @Override
            public void onSuccess(String password) {
                ViewUtils.isFastClick();
                pass.run();
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                mSupportDelegate.popBackFragment(1);
            }
        }));
    }
}
