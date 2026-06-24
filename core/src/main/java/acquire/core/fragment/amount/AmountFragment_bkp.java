package acquire.core.fragment.amount;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.keyboard.BaseKeyboard;
import acquire.base.widget.keyboard.listener.ViewKeyboardListener;
import acquire.core.databinding.CoreFragmentAmountBinding;
import acquire.core.tools.PhysicalKeyboardUtils;
import acquire.sdk.device.BDevice;


/**
 * A entry amount {@link Fragment}
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
public class AmountFragment_bkp extends BaseFragment {

    private FragmentCallback<Long> mCallback;
    private CoreFragmentAmountBinding binding;
    private final static int DECIMAL = 2;
    private final static String DEFAULT_AMOUNT = "0.00";
    private final static String ARG_CURRENCY ="CURRENCY_CODE";
    @NonNull
    public static AmountFragment_bkp newInstance(String currencyCode, FragmentCallback<Long> callback) {
        AmountFragment_bkp fragment =  new AmountFragment_bkp();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putString(ARG_CURRENCY,currencyCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentAmountBinding.inflate(inflater,container,false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        if (getArguments() != null){
            binding.tvCurrency.setText(CurrencyUtils.getCurrencySymbol(getArguments().getString(ARG_CURRENCY)));
        }

        binding.tvAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //enable enter key
                boolean enableEnter = !DEFAULT_AMOUNT.equals(s.toString());
                binding.keyboardNumber.findKey(BaseKeyboard.K_ENTER).setEnabled(enableEnter);
            }
        });

        //set default amount
        binding.tvAmount.setText(DEFAULT_AMOUNT);
        //keyboard
        binding.keyboardNumber.setKeyBoardListener(new ViewKeyboardListener(mActivity,13) {

            @Override
            public void onClear() {
                binding.tvAmount.setText(DEFAULT_AMOUNT);
            }

            @Override
            public String getText() {
                return binding.tvAmount.getText().toString();
            }

            @Override
            public void setText(String text) {
                long amount = getAmount(text);
                String strAmount = FormatUtils.formatAmount(amount,DECIMAL);
                binding.tvAmount.setText(strAmount);
                LoggerUtils.d("amount: "+strAmount );
            }
            @Override
            public void onEnter(){
                //enter amount
                LoggerUtils.d("enter amount: "+binding.tvAmount.getText().toString() );
                long amount = getAmount(binding.tvAmount.getText().toString());
                if (amount == 0){
                    return;
                }
                mCallback.onSuccess(amount);
            }
        });

        //check physical keyboard
        if (BDevice.supportPhysicalKeyboard()){
            binding.keyboardNumber.setVisibility(View.GONE);
            PhysicalKeyboardUtils.setKeyboardListener(binding.tvAmount,binding.keyboardNumber.getKeyBoardListener());
        }
        return binding.getRoot();
    }

    @Override
    public void onFragmentHide() {
        if (BDevice.supportPhysicalKeyboard()){
            PhysicalKeyboardUtils.removeKeyboardListener(binding.tvAmount);
        }
        super.onFragmentHide();
    }

 /*   private long /getAmount(String amountText){
        //delete non numeric characters
        String strAmount = amountText.replaceAll("[^\\d]", "");
        return Long.parseLong(strAmount);
    }*/

    @Override
    public FragmentCallback<Long> getCallback() {
        return mCallback;
    }
}
