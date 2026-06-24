package acquire.core.tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class KeyboardAmountUtility {


    public static String removeLastDigit(String value) {

        String clean = value.replaceAll("[^\\d]", "");
        if (clean.length() <= 1) return "0.00";
        String newStr = clean.substring(0, clean.length()-1);

        double value2 = Double.parseDouble(newStr) / 100;

        // 3. Format with commas and 2 decimals
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);

        return df.format(value2);
    }

    public static String inputDigit(String input) {
        // 1. Remove any non-digit characters
        String clean = input.replaceAll("[^\\d]", "");

        if (clean.isEmpty()) return "0.00";

        // 2. Convert to double cents -> dollars
        double value = Double.parseDouble(clean) / 100;

        // 3. Format with commas and 2 decimals
        DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        df.setMinimumFractionDigits(2);
        df.setMaximumFractionDigits(2);

        return df.format(value);
    }


}
