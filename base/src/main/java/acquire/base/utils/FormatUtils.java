package acquire.base.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Locale;

/**
 * Format data utils
 *
 * @author tao
 */
public class FormatUtils {

    /**
     * format amount as a string with two decimal and ",".
     * <p>e.g. 123,333.02</p>
     */
    public static String formatAmount(long amount) {
        return formatAmount(amount, 2);
    }

    /**
     * format amount as a string with decimal and ",".
     * @param format decimal number
     */
    public static String formatAmount(long amount, int format) {
        return formatAmount(amount, format, ",");
    }

    /**
     * format amount as a string with decimal and segmenter.
     * <p>e.g. 123,333.02</p>
     *
     * @param format    decimal number
     * @param segmenter segmenter flag per 3
     */
    public static String formatAmount(long amount, int format, String segmenter) {
        String strInteger = "0";
        String strDecimal;
        StringBuilder tmp = new StringBuilder();
        String flag = "";
        if (amount < 0) {
            flag = "-";
            amount = -amount;
        }
        String strValue = amount + "";
        if (strValue.length() > format) {
            strInteger = Long.valueOf(strValue.substring(0, strValue.length() - format)).toString();
            if (strInteger.length() > 3) {
                int end = strInteger.length() % 3;
                int len = strInteger.length();
                for (int start = 0; end <= len; start = end, end += 3) {
                    tmp.append(strInteger.subSequence(start, end));
                    if (end < len && start != end) {
                        tmp.append(segmenter);
                    }
                }
                strInteger = tmp.toString();
            }
            strDecimal = strValue.substring(strValue.length() - format);
        } else {
            strDecimal = String.format(Locale.US,"%0" + format + "d", amount);
        }
        if (strDecimal.isEmpty()) {
            return flag + strInteger;
        } else {
            return flag + strInteger + "." + strDecimal;
        }
    }

    /**
     * To convert an amount  to a long type in cents.
     * format amount
     * @param amountStr (e.g., '10.00905')
     * @return (e.g., '1000')
     */
    public static Long formatAmount(String amountStr){
        if(!TextUtils.isEmpty(amountStr)){
            BigDecimal decimalAmount = new BigDecimal(amountStr);
            BigDecimal amountInCents = decimalAmount.multiply(new BigDecimal(100));
            return amountInCents.longValue();
        }
        return 0L;
    }

    /**
     *Convert a value in cents (e.g., 9999) to an amount represented with two decimal places, such as 99.99
     * @param amountInCents (e.g., 9999)
     * @return (e.g., 99.99)
     */
    public static String formatAmountWithTwoDecimals(long amountInCents) {
        double amountInYuan = amountInCents / 100.0;
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(amountInYuan);
    }

    /**
     * format card as a string with " "
     * <p>e.g. "1234567890123456789"=>"1234 5678 9012 3456"</p>
     */
    public static String formatCardNoWithSpace(String card) {
        return formatCardNo(card, " ");
    }

    /**
     * format card as a string with separator
     * <p>e.g. separator = '-', "1234567890123456789"=>"1234-5678-9012-3456"</p>
     */
    public static String formatCardNo(String card, String separator) {
        if (card == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] cardChs = card.toCharArray();
        for (int i = 0; i < cardChs.length; i++) {
            if (i != 0 && i % 4 == 0) {
                builder.append(separator);
            }
            builder.append(cardChs[i]);
        }
        return builder.toString();
    }

    /**
     * mask card no with '*'.
     * <p>e.g. "6666666666666666"=>"666666******6666"</p>
     */
    public static String maskCardNo(String card) {
        if (card == null) {
            return "";
        }
        int len = card.length();
        if (len <= 4) {
            return card;
        }

        StringBuilder builder = new StringBuilder();
//        for (int i = 0; i < len - 4; i++) {
//            builder.append("*");
//        }
        builder.append(card.substring(0,6));
        for (int i = 6; i < len - 4; i++) {
            builder.append("*");
        }

        builder.append(card.substring(len - 4));

        return builder.toString();
    }

    /**
     * mask track1.
     * <p>e.g. "%B6222021234567890^CARDHOLDER/TEST^25092210000012345678?" => "%B************7890^CARDHOLDER/TEST^****"</p>
     */
    public static String maskTrack1(String track1) {
        if (track1 == null || track1.isEmpty()) return track1;

        int firstCaret = track1.indexOf('^');
        if (firstCaret < 7) {
            return track1;
        }
        String pan = track1.substring(2, firstCaret);
        String maskedPan = maskCardNo(pan);
        int secondCaret = track1.indexOf('^', firstCaret + 1);
        String name = secondCaret > firstCaret ? track1.substring(firstCaret + 1, secondCaret) : "";
        return "%B" + maskedPan + "^" + name + "^****";
    }

    /**
     * mask Track 2 & Track 3.
     * <p>e.g. "6222021234567890D25092210000012345678" => "************7890D****************"</p>
     */
    public static String maskTrack2or3(String track) {
        if (track == null || track.isEmpty()) {
            return track;
        }

        // '='  or 'D' or 'd'
        int sep = track.indexOf('=');
        if (sep < 0) {
            sep = track.indexOf('D');
        }
        if (sep < 0) {
            sep = track.indexOf('d');
        }

        // PAN
        String pan = sep > 0 ? track.substring(0, sep) : track;
        String maskedPan = maskCardNo(pan);

        // Sensitive information after mask delimiter
        if (sep > 0 && sep < track.length() - 1) {
            String sensitive = track.substring(sep + 1);
            char[] arr = new char[sensitive.length()];
            Arrays.fill(arr, '*');
            String maskedSensitive = new String(arr);
            return maskedPan + track.charAt(sep) + maskedSensitive;
        }

        return maskedPan;
    }

    /**
     * format phone number
     * <p>e.g. "1234567890"=>"123 456 7890"</p>
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        char[] phoneChs = phone.toCharArray();
        for (int i = 0; i < phoneChs.length; i++) {
            if (i == 3 || i == 6) {
                builder.append(' ');
            }
            builder.append(phoneChs[i]);
        }
        return builder.toString();
    }


}
