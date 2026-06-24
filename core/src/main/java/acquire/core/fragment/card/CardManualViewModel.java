package acquire.core.fragment.card;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import acquire.base.BaseApplication;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.core.R;

/**Card Manual View Model
 *
 * @author Janson
 * @date 2022/9/16 15:11
 */
public class CardManualViewModel extends ViewModel {
    private final static String CARD_SEPARATOR = " ";
    private final static int CARD_MAX_LEN = 19;
    private final static int CARD_MIN_LEN = 13;

    private final MutableLiveData<String> cardNo = new MutableLiveData<>();
    private final MutableLiveData<String> cardNoError = new MutableLiveData<>();
    private final MutableLiveData<String> expDate = new MutableLiveData<>();
    private final MutableLiveData<String> expDateError = new MutableLiveData<>();
    private final MutableLiveData<String[]> result = new MutableLiveData<>();

    public MutableLiveData<String> getCardNo() {
        return cardNo;
    }

    public MutableLiveData<String> getCardNoError() {
        return cardNoError;
    }

    public MutableLiveData<String> getExpDate() {
        return expDate;
    }

    public MutableLiveData<String> getExpDateError() {
        return expDateError;
    }

    public MutableLiveData<String[]> getResult() {
        return result;
    }

    public void formatCardNumber(String text){
        String card = text.replace(CARD_SEPARATOR, "");
        //format card No =>e.g. 1234 5678 9012 3456
        if (card.length() > CARD_MAX_LEN){
            card = card.substring(0,CARD_MAX_LEN);
        }
        String formatStr = FormatUtils.formatCardNo(card,CARD_SEPARATOR);
        if (!formatStr.equals(text)) {
            cardNo.setValue(formatStr);
        }
        cardNoError.postValue(null);
    }

    public void formatExpDate(String text){
        if (TextUtils.isEmpty(text)){
            return;
        }
        String validText = text.replace("/","");
        int monthYear;
        try {
            monthYear = Integer.parseInt(validText);
        }catch (Exception e){
            LoggerUtils.e("parseInt expdate "+validText+" failed!",e);
            return;
        }
        expDateError.postValue(null);
        String formatText;
        switch (validText.length()){
            case 0:
                formatText = "";
                break;
            case 1:
                if (monthYear>1){
                    formatText = "0"+validText;
                }else {
                    formatText = validText;
                }
                break;
            case 2:
                if (monthYear>12){
                    formatText = "0"+validText.charAt(0)+"/"+validText.charAt(1);
                }else {
                    if(monthYear == 0){
                        //month error, remove first char
                        formatExpDate(validText.substring(1));
                        return;
                    }
                    formatText = validText;
                }
                break;
            default:
                String month = validText.substring(0,2);
                if ("00".equals(month)){
                    //month error, remove first char
                    formatExpDate(validText.substring(1));
                    return;
                }
                String year = validText.substring(2,Math.min(4,validText.length()));
                formatText = month+"/"+year;
                break;
        }
        if (formatText.equals(text)){
            return;
        }
        expDate.setValue(formatText);

    }


    public void checkResult(String cardText,String expText){
        //card NO
        cardText = cardText.replace(CARD_SEPARATOR,"");
        if (cardText.length() <CARD_MIN_LEN){
            cardNoError.postValue(BaseApplication.getAppString(R.string.core_card_manual_card_number_too_short_format,CARD_MIN_LEN));
            return ;
        }
        //Expire Date
        expText = expText.replace("/","");
        if (expText.length() != 4){
            expDateError.postValue(BaseApplication.getAppString(R.string.core_card_manual_expdate_incorrect));
            return ;
        }
        expText = expText.substring(2,4)+expText.substring(0,2);
        result.postValue(new String[]{cardText,expText});
    }
} 
