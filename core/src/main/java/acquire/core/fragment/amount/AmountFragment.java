package acquire.core.fragment.amount;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.ToastUtils;
import acquire.core.databinding.CoreFragmentAmountBinding;
import acquire.core.fragment.base.CoreBaseFragment;
import acquire.core.tools.KeyboardAmountUtility;
import acquire.core.tools.PhysicalKeyboardUtils;
import acquire.sdk.device.BDevice;


/**
 * A entry amount {@link Fragment}
 *
 * @author Janson
 * @date 2021/1/5 17:18
 */
public class AmountFragment extends CoreBaseFragment implements View.OnClickListener{

    private boolean mIsNumberShuffle = false;
    private boolean mIsReadyForPin = false;
    private  boolean mIsBtnCancelActDismiss = false;



    private FragmentCallback<Long> mCallback;
    private CoreFragmentAmountBinding binding;
    private final static int DECIMAL = 2;
    private final static String DEFAULT_AMOUNT = "0.00";
    private final static String ARG_CURRENCY ="CURRENCY_CODE";
    @NonNull
    public static AmountFragment newInstance(String currencyCode,FragmentCallback<Long> callback) {
        AmountFragment fragment =  new AmountFragment();
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
        binding.toolbar.setTitle((mActivity.getTitle()+ " amount").toUpperCase());
        binding.toolbar.setBackListener(v-> mActivity.getOnBackPressedDispatcher().onBackPressed());
        /*if (getArguments() != null){
            binding.tvCurrency.setText(CurrencyUtils.getCurrencySymbol(getArguments().getString(ARG_CURRENCY)));
        }*/

       /*
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
                //binding.keyboardNumber.findKey(BaseKeyboard.K_ENTER).setEnabled(enableEnter);
            }
        });
       */

        //showPinSubmitSheet(mActivity.getTitle().toString(), binding.tvAmount, true, mCallback);

        //set default amount
        binding.tvAmount.setText(DEFAULT_AMOUNT);
        //keyboard
        /*binding.keyboardNumber.setKeyBoardListener(new ViewKeyboardListener(mActivity,13) {

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
        });*/

        //check physical keyboard
        if (BDevice.supportPhysicalKeyboard()){
            //binding.keyboardNumber.setVisibility(View.GONE);
           // PhysicalKeyboardUtils.setKeyboardListener(binding.tvAmount,binding.keyboardNumber.getKeyBoardListener());
        }

        initialView();
        return binding.getRoot();
    }

    @Override
    public void onFragmentHide() {
        if (BDevice.supportPhysicalKeyboard()){
            PhysicalKeyboardUtils.removeKeyboardListener(binding.tvAmount);
        }
        super.onFragmentHide();
    }

 /*   private long getAmount(String amountText){
        //delete non numeric characters
        String strAmount = amountText.replaceAll("[^\\d]", "");
        return Long.parseLong(strAmount);
    }*/

    @Override
    public boolean onBack() {
        getCallback();
        return super.onBack();
    }

    @Override
    public FragmentCallback<Long> getCallback() {
        return mCallback;
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
    public void onClick(View view) {
        try {
            int id = view.getId();

            if (id == binding.inclNumKey.tvPositionOk.getId()) {

                boolean isTakeAction = false;
                String input = binding.tvAmount.getText().toString();

                if (input != null && input.length() >= 3) {
                    input = input.replace(",", "");
                    try {
                        double amount = Double.parseDouble(input);
                        if (amount > 0) isTakeAction = true;
                    } catch (Exception ignored) {

                    }
                }

                //dismiss();
                long amount = getAmount(binding.tvAmount.getText().toString());
               if(isTakeAction) {
                   mCallback.onSuccess(amount);
               }
               else{
                   ToastUtils.showLongToast("Please input amount");
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

    private void clearDeleteDisplay() {
        String text = binding.tvAmount.getText().toString();
        text = KeyboardAmountUtility.removeLastDigit(text);

        binding.tvAmount.setText(text);
//        onClickedSubmit(text, false);
    }

//    public String removeLastDigit(String value) {
//
//        String clean = value.replaceAll("[^\\d]", "");
//        if (clean.length() <= 1) return "0.00";
//        String newStr = clean.substring(0, clean.length()-1);
//
//        double value2 = Double.parseDouble(newStr) / 100;
//
//        // 3. Format with commas and 2 decimals
//        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
//        df.setMinimumFractionDigits(2);
//        df.setMaximumFractionDigits(2);
//
//        return df.format(value2);
//    }


    private void setDisplay(String value) {

        String current = binding.tvAmount.getText().toString();

        if (current.length() == 10) return;

        String updated = KeyboardAmountUtility.inputDigit(current + value);

        binding.tvAmount.setText(updated);

        //onClickedSubmit(updated, false);
    }

//    public String inputDigit(String input) {
//        // 1. Remove any non-digit characters
//        String clean = input.replaceAll("[^\\d]", "");
//
//        if (clean.isEmpty()) return "0.00";
//
//        // 2. Convert to double cents -> dollars
//        double value = Double.parseDouble(clean) / 100;
//
//        // 3. Format with commas and 2 decimals
//        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
//        df.setMinimumFractionDigits(2);
//        df.setMaximumFractionDigits(2);
//
//        return df.format(value);
//    }


}
