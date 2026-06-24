package acquire.core.fragment.tip;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acquire.base.activity.BaseFragment;
import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.base.widget.AmountFilter;
import acquire.base.widget.keyboard.listener.EditKeyboardListener;
import acquire.core.R;
import acquire.core.databinding.CoreFragmentTipBinding;
import acquire.core.tools.KeyboardAmountUtility;
import acquire.core.tools.PhysicalKeyboardUtils;
import acquire.sdk.device.BDevice;


/**
 * A entry tip amount {@link Fragment}
 *
 * @author Janson
 * @date 2022/8/9 8:55
 */
public class TipFragment extends BaseFragment implements View.OnClickListener{

    private FragmentCallback<Long> mCallback;
    private CoreFragmentTipBinding binding;
    private final static int DECIMAL = 2;
    private final static String ARG_CURRENCY = "CURRENCY_CODE";
    private final static String ARG_AMOUNTY = "AMOUNT";

    private boolean mIsNumberShuffle = false;
    private boolean mIsReadyForPin = false;

    @NonNull
    public static TipFragment newInstance(String currencyCode, long amount, FragmentCallback<Long> callback) {
        TipFragment fragment = new TipFragment();
        fragment.mCallback = callback;
        Bundle args = new Bundle();
        args.putString(ARG_CURRENCY, currencyCode);
        args.putLong(ARG_AMOUNTY, amount);
        fragment.setArguments(args);
        return fragment;
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = CoreFragmentTipBinding.inflate(inflater, container, false);
        binding.toolbar.setTitle(mActivity.getTitle()+" TIP");
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        TipViewModel viewModel = new ViewModelProvider(this).get(TipViewModel.class);

        long origAmount = 0;
        String currencySymbol = "";
        if (getArguments() != null) {
            currencySymbol = CurrencyUtils.getCurrencySymbol(getArguments().getString(ARG_CURRENCY));
            origAmount = getArguments().getLong(ARG_AMOUNTY);
        }
        binding.tvCurrency.setText(currencySymbol);
        binding.tvOrigAmount.setText(getString(R.string.core_tip_ori_amount_format,currencySymbol,FormatUtils.formatAmount(origAmount,DECIMAL)));
        viewModel.init(origAmount,DECIMAL);
        viewModel.getTipAmtText().observe(getViewLifecycleOwner(), tipText -> {
            binding.etTip.setText(tipText);
            binding.etTip.setSelection(binding.etTip.getText().length());
        });

        //brac
        viewModel.getMaxTipAmtText().observe(getViewLifecycleOwner(), maxTipAmtText -> binding.tvMaxTipAmount.setText("(MAX: "+maxTipAmtText+")"));

        viewModel.getTotalAmtText().observe(getViewLifecycleOwner(), totalText -> binding.tvTotalAmount.setText(totalText));
        viewModel.getErrorText().observe(getViewLifecycleOwner(), ToastUtils::showToast);
        viewModel.getResult().observe(getViewLifecycleOwner(), tip -> mCallback.onSuccess(tip));
        viewModel.getPercentClean().observe(getViewLifecycleOwner(), isClean -> {
            if (isClean){
                binding.groupPercents.clearChecked();
            }
        });
        //keyboard
        ViewUtils.setFocus(binding.etTip);
        binding.etTip.setFilters(new InputFilter[]{new AmountFilter(DECIMAL),new InputFilter.LengthFilter(11)});

        /*binding.keyboardNumber.setKeyBoardListener(new EditKeyboardListener(mActivity,binding.etTip, 13) {
            @Override
            public void onEnter() {
                viewModel.enter(binding.etTip.getText().toString());
            }
        });

        binding.etTip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                viewModel.setTip(s.toString());
            }
        });

        binding.groupPercents.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                Button button = binding.getRoot().findViewById(checkedId);
                viewModel.selectPercent(button.getText().toString());
            }
        });
        binding.etTip.setOnEditorActionListener((v, actionId, event) -> {
            boolean enterAction = actionId == EditorInfo.IME_ACTION_DONE
                    || actionId == EditorInfo.IME_ACTION_NEXT
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN);
            if (enterAction && isShowing()) {
                viewModel.enter(binding.etTip.getText().toString());
            }
            return true;
        });*/


        //check physical keyboard
        if (BDevice.supportPhysicalKeyboard()){
//            binding.keyboardNumber.setVisibility(View.GONE);
//            binding.inclNumKey.(View.GONE);
            PhysicalKeyboardUtils.adaptPoint(binding.etTip);
        }

        initialView();

        return binding.getRoot();
    }


    @Override
    public void onClick(View view) {
        try {
            int id = view.getId();

            if (id == binding.inclNumKey.tvPositionOk.getId()) {

                boolean isTakeAction = true;
                String input = binding.tvAmount.getText().toString();

                if (input != null && input.length() >= 3) {
                    input = input.replace(",", "");
                    try {
                        double amount = Double.parseDouble(input);
                        //if (amount > 0) isTakeAction = true;
                    } catch (Exception ignored) {
                        isTakeAction = false;
                    }
                }

                //dismiss();
                long tipAmount = getAmount(binding.tvAmount.getText().toString());
                if(isTakeAction) {
                    //viewModel.enter(binding.etTip.getText().toString());
                    mCallback.onSuccess(tipAmount);
                }
                else{
                    ToastUtils.showLongToast("Please input TIP amount");
                }
                // onClickedSubmit(binding.tvAmount.getText().toString(), isTakeAction);

            } else if (id == binding.inclNumKey.tvPositionDouble0.getId()) {
                setDisplay("0");
                setDisplay("0");

            } else if (id == binding.inclNumKey.tvPositionHash.getId()) {
                setDisplay("#");

            } else if (id == binding.inclNumKey.tvPositionCancel.getId()) {
                clearAllCloseDisplay();

            } else if (id == binding.inclNumKey.tvPositionDelete.getId()) {
                clearDeleteDisplay();

            } else {
                setDisplay(((TextView)view).getText().toString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearDeleteDisplay() {
        String text = binding.tvAmount.getText().toString();
        text = KeyboardAmountUtility.removeLastDigit(text);

        binding.tvAmount.setText(text);
//        onClickedSubmit(text, false);
    }

    private void clearAllCloseDisplay() {

        String text = binding.tvAmount.getText().toString();

        if (text!=null && !text.isEmpty() && text.equals("0.00")){
            onBack();
            mActivity.finish();
        }
        else{
            binding.tvAmount.setText("0.00");
        }
    }

    private void setDisplay(String value) {

        String current = binding.tvAmount.getText().toString();

        if (current.length() == 10) return;

        String updated = KeyboardAmountUtility.inputDigit(current + value);

        binding.tvAmount.setText(updated);

        //onClickedSubmit(updated, false);
    }

    private void initialView() {

        binding.inclNumKey.tvPosition1.setOnClickListener(this);
        binding.inclNumKey.tvPosition2.setOnClickListener(this);
        binding.inclNumKey.tvPosition3.setOnClickListener(this);
        binding.inclNumKey.tvPosition4.setOnClickListener(this);
        binding.inclNumKey.tvPosition5.setOnClickListener(this);
        binding.inclNumKey.tvPosition6.setOnClickListener(this);
        binding.inclNumKey.tvPosition7.setOnClickListener(this);
        binding.inclNumKey.tvPosition8.setOnClickListener(this);
        binding.inclNumKey.tvPosition9.setOnClickListener(this);
        binding.inclNumKey.tvPosition10.setOnClickListener(this);
        binding.inclNumKey.tvPositionDelete.setOnClickListener(this);
        binding.inclNumKey.tvPositionOk.setOnClickListener(this);
        binding.inclNumKey.tvPositionCancel.setOnClickListener(this);

        binding.inclNumKey.tvPositionHash.setEnabled(mIsReadyForPin);
        binding.inclNumKey.tvPositionHash.setVisibility(View.INVISIBLE);
        binding.inclNumKey.tvPositionDouble0.setEnabled(mIsReadyForPin);
        binding.inclNumKey.tvPositionDouble0.setVisibility(View.INVISIBLE);

        if(mIsReadyForPin){
            binding.inclNumKey.tvPositionHash.setOnClickListener(this);

            binding.inclNumKey.tvPositionHash.setText("#");

            binding.inclNumKey.tvPositionDouble0.setOnClickListener(this);
            binding.inclNumKey.tvPositionDouble0.setText("00");
        }else{
            binding.inclNumKey.tvPositionHash.setText("");
            binding.inclNumKey.tvPositionDouble0.setText("");

        }

//        mExInputPin = view.findViewById(R.id.exInputPin);


        setPositionsValue(mIsNumberShuffle);
    }

    private void setPositionsValue(boolean mIsNumberShuffle) {

        List<Integer> numbers = new ArrayList<>();

        for (int i = 1; i <= 9; i++) numbers.add(i);
        if (mIsNumberShuffle) {
            numbers.clear();
            for (int i = 0; i <= 9; i++) numbers.add(i);

        } else {
            numbers.add(0);
        }

        if (mIsNumberShuffle) {
            Collections.shuffle(numbers);
        }


        TextView[] views = {
                binding.inclNumKey.tvPosition1, binding.inclNumKey.tvPosition2, binding.inclNumKey.tvPosition3,
                binding.inclNumKey.tvPosition4, binding.inclNumKey.tvPosition5, binding.inclNumKey.tvPosition6,
                binding.inclNumKey.tvPosition7, binding.inclNumKey.tvPosition8, binding.inclNumKey.tvPosition9,
                binding.inclNumKey.tvPosition10
        };

        for (int i = 0; i < numbers.size(); i++) {
            views[i].setText(String.valueOf(numbers.get(i)));
        }
    }

    @Override
    public FragmentCallback<Long> getCallback() {
        return mCallback;
    }




}
