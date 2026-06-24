package acquire.core.fragment.amount;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.keyboard.BaseKeyboard;
import acquire.base.widget.keyboard.listener.ViewKeyboardListener;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentAmountCashbackBinding;
import acquire.core.tools.PhysicalKeyboardUtils;
import acquire.sdk.device.BDevice;


/**
 * A  {@link Fragment} for entring purchase and Naqd amount
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
public class CashBackAmountFragment extends BaseFragment {

    private FragmentCallback<Long[]> mCallback;
    private CoreFragmentAmountCashbackBinding binding;
    private final static int DECIMAL = 2;
    private final static String DEFAULT_AMOUNT = "0.00";
    private final static String ARG_CURRENCY = "CURRENCY_CODE";
    private boolean selectCashBack;

    @NonNull
    public static CashBackAmountFragment newInstance(String currencyCode, FragmentCallback<Long[]> callback) {
        CashBackAmountFragment fragment = new CashBackAmountFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putString(ARG_CURRENCY, currencyCode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentAmountCashbackBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        if (getArguments() != null) {
            String currency = CurrencyUtils.getCurrencySymbol(getArguments().getString(ARG_CURRENCY));
            binding.tvCurrency.setText(currency);
            binding.tvCashCurrency.setText(currency);
        }
        binding.tvPurchaseAmount.addTextChangedListener(new TextWatcher() {
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
        binding.tvPurchaseAmount.setText(DEFAULT_AMOUNT);
        binding.tvCashAmount.setText(DEFAULT_AMOUNT);

        //keyboard
        binding.keyboardNumber.setKeyBoardListener(new ViewKeyboardListener(mActivity,13) {

            @Override
            public void onClear() {
                if (selectCashBack) {
                    binding.tvCashAmount.setText(DEFAULT_AMOUNT);
                } else {
                    binding.tvPurchaseAmount.setText(DEFAULT_AMOUNT);
                }

            }

            @Override
            public String getText() {
                if (selectCashBack) {
                    return binding.tvCashAmount.getText().toString();
                } else {
                    return binding.tvPurchaseAmount.getText().toString();
                }
            }

            @Override
            public void setText(String text) {
                long amount = getAmount(text);
                String strAmount = FormatUtils.formatAmount(amount, DECIMAL);
                if (selectCashBack) {
                    binding.tvCashAmount.setText(strAmount);
                    LoggerUtils.d("cashback amount: " + strAmount);
                } else {
                    binding.tvPurchaseAmount.setText(strAmount);
                    LoggerUtils.d("purchase amount: " + strAmount);
                }

            }

            @Override
            public void onEnter() {
                long cashAmount = getAmount(binding.tvCashAmount.getText().toString());
                long purchaseAmount = getAmount(binding.tvPurchaseAmount.getText().toString());

                if (selectCashBack) {
                    LoggerUtils.d("enter cashback amount: " + binding.tvCashAmount.getText().toString());
                    if (purchaseAmount != 0) {
                        mCallback.onSuccess(new Long[]{purchaseAmount, cashAmount});
                    }
                } else {
                    LoggerUtils.d("enter purchase amount: " + binding.tvCashAmount.getText().toString());
                    long amount = getAmount(binding.tvPurchaseAmount.getText().toString());
                    if (amount == 0) {
                        return;
                    }
                    selectCashBack();
                }

            }
        });
        binding.cvPurchaseAmount.setOnClickListener(v-> selectPurchase());
        binding.cvCashBack.setOnClickListener(v-> selectCashBack());
        selectPurchase();
        binding.cvCashBack.setVisibility(View.GONE);
        return binding.getRoot();
    }

    @Override
    public void onFragmentHide() {
        if (BDevice.supportPhysicalKeyboard()) {
            PhysicalKeyboardUtils.removeKeyboardListener(binding.tvCashAmount);
            PhysicalKeyboardUtils.removeKeyboardListener(binding.tvPurchaseAmount);
        }
        super.onFragmentHide();
    }

    @Override
    public void onFragmentShow() {
     //   DisplayUtils.fitNavigationColor(mActivity,true);
        super.onFragmentShow();
    }

    private void selectCashBack() {
        if (binding.cvCashBack.getVisibility() != View.VISIBLE){
            binding.cvCashBack.setVisibility(View.VISIBLE);
        }
        binding.cvCashBack.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.core_cash_amount_select));
        binding.cvPurchaseAmount.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.core_purchase_amount_unselect));
        if (BDevice.supportPhysicalKeyboard()) {
            //check P300 physical keyboard
            binding.keyboardNumber.setVisibility(View.GONE);
            PhysicalKeyboardUtils.setKeyboardListener(binding.tvCashAmount, binding.keyboardNumber.getKeyBoardListener());
            PhysicalKeyboardUtils.removeKeyboardListener(binding.tvPurchaseAmount);
        }
        selectCashBack = true;
    }

    private void selectPurchase() {
        binding.cvPurchaseAmount.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.core_purchase_amount_select));
        binding.cvCashBack.setCardBackgroundColor(ContextCompat.getColor(mActivity, R.color.core_cash_amount_unselect));
        binding.cvCashBack.setStrokeWidth(0);
        if (BDevice.supportPhysicalKeyboard()) {
            //check P300 physical keyboard
            binding.keyboardNumber.setVisibility(View.GONE);
            PhysicalKeyboardUtils.setKeyboardListener(binding.tvPurchaseAmount, binding.keyboardNumber.getKeyBoardListener());
            PhysicalKeyboardUtils.removeKeyboardListener(binding.tvCashAmount);
        }
        selectCashBack = false;
    }

/*    private long getAmount(String amountText) {
        //delete non numeric characters
        String strAmount = amountText.replaceAll("[^\\d]", "");
        return Long.parseLong(strAmount);
    }*/

    @Override
    public FragmentCallback<Long[]> getCallback() {
        return mCallback;
    }
}
