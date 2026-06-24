package acquire.core.fragment.tip;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;

/**
 * @author Janson
 * @date 2024/1/4 14:12
 */
public class TipViewModel extends ViewModel {
    private long origAmount;
    private long percentAmount;
    private int decimal;
    private final MutableLiveData<Long> result = new MutableLiveData<>();
    private final MutableLiveData<String> tipAmtText = new MutableLiveData<>();
    private final MutableLiveData<String> totalAmtText = new MutableLiveData<>();

    private final MutableLiveData<String> errorText = new MutableLiveData<>();

    private final MutableLiveData<Boolean> percentClean = new MutableLiveData<>();

    public MutableLiveData<Long> getResult() {
        return result;
    }

    public MutableLiveData<String> getTipAmtText() {
        return tipAmtText;
    }


    public MutableLiveData<String> getTotalAmtText() {
        return totalAmtText;
    }

    public MutableLiveData<String> getErrorText() {
        return errorText;
    }

    public MutableLiveData<Boolean> getPercentClean() {
        return percentClean;
    }


//    private double maxTipAmt=0.0;

    private final MutableLiveData<String> maxTipAmtText = new MutableLiveData<>();

    public MutableLiveData<String> getMaxTipAmtText() {
        return maxTipAmtText;
    }

    private double inputMinPercentage = 0.0f;
    private double inputMaxPercentage = 0.0f;
    private double maxIn = 0.0f;

    public void init(long origAmount, int decimal) {
        this.origAmount = origAmount;
        String amount = FormatUtils.formatAmount(origAmount, decimal, "");
        this.decimal = decimal;
        totalAmtText.postValue(amount);
        try {
//            inputMinPercentage = Double.parseDouble(ParamsUtils.getString("SALETIPFLAG", "0"));
            inputMaxPercentage = Double.parseDouble(ParamsUtils.getString("TIPRATE", "1"));
            double baseAmount = Double.parseDouble(amount);
            maxIn = (baseAmount * inputMaxPercentage) / 100;
            maxTipAmtText.postValue(String.format("%.2f", maxIn));

        } catch (Exception ex) {

        }
    }

    public void enter(String tipAmount) {
        //enter tip
        LoggerUtils.d("enter tip: " + tipAmount);
        if (TextUtils.isEmpty(tipAmount)) {
            result.postValue(0L);
            return;
        }
        long tip = getAmount(tipAmount);

        /* old
        if (tip > origAmount) {
            errorText.postValue(R.string.core_tip_amount_limit_error);
            return;
        }*/

        //brac
        if (tip < getAmount(inputMinPercentage+"") ) {
//            ToastUtils.showLongToast("Enter amount below or equal " + maxIn);
            errorText.postValue("Enter amount below or equal " + maxIn);
            return;
        }
        else if( tip > getAmount(maxIn+"")){
            errorText.postValue("Enter amount below or equal " + maxIn);
            return;
        }

        maxTipAmtText.postValue(String.format("%.2f", maxIn));
        //---------

        result.postValue(tip);
    }

    public void setTip(String tipAmount) {
        long tip = 0;
        String regex = "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0," + decimal + "})?$";

        if (tipAmount.length() > 0 && tipAmount.matches(regex)) {
            tip = getAmount(tipAmount);
        }

        long totalAmount = origAmount + tip;
        totalAmtText.postValue(FormatUtils.formatAmount(totalAmount));

        if (percentAmount != tip) {
            percentClean.postValue(true);
            percentAmount = 0;
        }
    }

    public void selectPercent(String percentText) {
        String percents = percentText.replace("%", "");
        long tip = origAmount * Long.parseLong(percents) / 100;
        percentAmount = tip;
        tipAmtText.postValue(FormatUtils.formatAmount(tip, decimal, ""));
        totalAmtText.postValue(FormatUtils.formatAmount(tip + origAmount, decimal, ""));

    }


    private long getAmount(String amountText) {
        double power = Math.pow(10, decimal);
        try {
            double amount = Double.parseDouble(amountText);
            return (long) (amount * power);
        } catch (Exception e) {
            LoggerUtils.e("TIP parseDouble " + amountText + " failed!", e);
            return 0;
        }
    }
} 
