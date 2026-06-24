package acquire.base.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Amount utils
 *
 * @author Janson
 * @date 2024/9/9 9:35
 */
public class AmountUtils {
    /**
     * 1.23 ->123
     */
    public static long getAmountFromFloat(float floatAmount){
        return getAmountFromFloat(floatAmount,2);
    }
    /**
     * decimal = 2; 1.23 ->123;
     * decimal = 3; 1.23 ->1230;
     */
    public static long getAmountFromFloat(float floatAmount, int decimal){
        long n = (long) Math.pow(10,decimal);
        return (long) (floatAmount*n);
    }

    public static String getAmountWithDigit(String strAmount){
        String amount = getAmountFromStringToString(strAmount);
        return amount.substring(0, amount.length() - 2) + "." + amount.substring(amount.length() - 2);
    }

    public static String getAmountFromStringToString(String strAmount){
        return String.format(Locale.getDefault(), "%012d", AmountUtils.getAmountFromString(strAmount));
    }
    /**
     * "1.23" ->123; or "$1.23"->123
     */
    public static long getAmountFromString(String strAmount){
        return getAmountFromString(strAmount,2);

    }
    /**
     * decimal = 2,  "1,234.56" ->123456;
     */
    public static long getAmountFromString(String strAmount, int decimal){
        return getAmountFromString(strAmount,decimal,",",".");
    }
    /**
     * decimal = 3,spiltFlag =",",dotFlag =".";  "1,234.56" ->1234560;
     */
    public static long getAmountFromString(String strAmount, int decimal, String spiltFlag, String dotFlag){
        if (TextUtils.isEmpty(strAmount)){
            return 0;
        }
        if (!TextUtils.isEmpty(spiltFlag)){
            strAmount = strAmount.replace(spiltFlag, "");
        }
        if (!TextUtils.isEmpty(dotFlag)){
            strAmount = strAmount.replace(dotFlag,".");
        }
        strAmount = strAmount.replaceAll("[^0-9.]", "");
        BigDecimal floatAmount = new BigDecimal(strAmount);
        long n = (long) Math.pow(10,decimal);
        BigDecimal amount = floatAmount.multiply(new BigDecimal(n));
        return amount.longValue();
    }
} 
