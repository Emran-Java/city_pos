package acquire.core.fragment.input;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.Arrays;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.constants.BasePrefKey;
import acquire.base.utils.InputUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentBracInstallmentInputInfoBinding;


/**
 * Input information in this {@link Fragment}
 *
 * @author Janson
 * @date 2019/7/25 11:20
 *
 * @author emran
 * Update:
 * @date 2026/4/21 13:00
 */
public class InstallmentBracInputInfoFragment extends BaseFragment {
    private CoreFragmentBracInstallmentInputInfoBinding binding;
    private FragmentCallback<String> mCallback;
    private InputInfoFragmentArgs args;
    private String cardMaskNumber;

    private int mPosition = 0;
    private String mInstallmentMonth, tranType, amount, cardHolderName;

    @NonNull
    public static InstallmentBracInputInfoFragment newInstance(String cardMaskNumber, String tranType, String amount, String cardHolderName, InputInfoFragmentArgs args, FragmentCallback<String> callback) {
        InstallmentBracInputInfoFragment fragment = new InstallmentBracInputInfoFragment();
        fragment.mCallback = callback;
        fragment.args = args;

        fragment.cardMaskNumber = cardMaskNumber;
        fragment.tranType = tranType;
        fragment.amount = amount;
        fragment.cardHolderName = cardHolderName;

        return fragment;
    }

    @NonNull
    public static InstallmentBracInputInfoFragment newInstance(InputInfoFragmentArgs args, FragmentCallback<String> callback) {
        InstallmentBracInputInfoFragment fragment = new InstallmentBracInputInfoFragment();
        fragment.mCallback = callback;
        fragment.args = args;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentBracInstallmentInputInfoBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v->mActivity.getOnBackPressedDispatcher().onBackPressed());


        binding.llInstallmentOptions.setVisibility(View.VISIBLE);
        binding.llInstallmentDetails.setVisibility(View.GONE);
        binding.inclInsDtls.btnOk.setEnabled(false);

        //Set data
//        String installmentMonthOptions = "3,6,9,12,15,18";
        String installmentMonthOptions = ParamsUtils.getString("EMITENURE", "3,6,9,12,15,18");
        List<String> monthList = Arrays.asList(installmentMonthOptions.split(","));
        LinearLayoutManager layoutManager =
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        binding.rvEmiInstallmentOption.setLayoutManager(layoutManager);
        EmiMonthOptionAdapter adapter = new EmiMonthOptionAdapter(monthList, new EmiMonthOptionAdapter.OnMonthClickListener() {
            @Override
            public void onMonthClick(int position) {
                //done(monthList.get(position));
                mPosition = position;
                mInstallmentMonth = monthList.get(mPosition);
                setInstallmentDetailsUi();
            }
        });
        binding.rvEmiInstallmentOption.setAdapter(adapter);
        //-----------------------


        //Listener
        binding.inclInsDtls.btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                done(monthList.get(mPosition));
            }
        });


/*        binding.tilInputInfo.setHint(args.getHint());
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
        });*/
        return binding.getRoot();
    }

    private void setInstallmentDetailsUi() {
        binding.llInstallmentOptions.setVisibility(View.GONE);
        binding.llInstallmentDetails.setVisibility(View.VISIBLE);
        binding.inclInsDtls.btnOk.setEnabled(true);

        binding.inclInsDtls.tvCardNoVal.setText(cardMaskNumber);
        binding.inclInsDtls.txTransTypeVal.setText(tranType);
        binding.inclInsDtls.tvAmountVal.setText(amount);
        binding.inclInsDtls.tvCardHolderNameVal.setText(cardHolderName);
        binding.inclInsDtls.tvTenureVal.setText(mInstallmentMonth);

        String vendorName = "";
        String vendorCode = "";

        String vendorID = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_VENDOR, "");
        try {
            List<String> vendorIDs = Arrays.asList(vendorID.split(":"));
            vendorCode = vendorIDs.get(1);
            vendorName = vendorIDs.get(0);
        }catch (Exception ex){
            LoggerUtils.e("newCall Brac PayFlex-Sale check INI parm file (EMIVENDOR) vendorID Exception:  "+ex.getMessage());
            return;
        }

        binding.inclInsDtls.tvVendorNameVal.setText(vendorName);

        binding.inclInsDtls.tvVendorCodeVal.setText(vendorCode);

        //Set PayFlex/Installment Details

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
        try {
            ParamsUtils.setInt("EMI_TENURE_SELECTED", Integer.parseInt(text));
        }catch (Exception ex){
            LoggerUtils.e("newCall PayFlex Number of Installment: "+ ex.getMessage());
        }
        mCallback.onSuccess(text);
    }

    @Override
    public FragmentCallback<String> getCallback() {
        return mCallback;
    }

}
