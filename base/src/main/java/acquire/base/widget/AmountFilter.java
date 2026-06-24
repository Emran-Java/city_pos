package acquire.base.widget;

import android.text.InputFilter;
import android.text.Spanned;

import androidx.annotation.NonNull;

/**
 * A amount format filter used fo {@link android.widget.EditText}
 *
 * @author Janson
 * @date 2020/8/25 11:28
 */
public class AmountFilter implements InputFilter {
    private final int decimal;

    public AmountFilter(int decimal) {
        this.decimal = decimal;
    }

    /**
     * @return ""，not insert；null，insert；else，insert custom text
     */
    @Override
    public CharSequence filter(@NonNull CharSequence source, int start, int end, @NonNull Spanned dest, int dstart, int dend) {
        if (source.length() == 0) {
            return "";
        }
        String sourceText = source.toString();
        String destText = dest.toString();
        String newText = destText.substring(0,dstart)+sourceText+destText.substring(dstart);
        //Amount  regex，such as "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,2})?$"
        String regex;
        if (decimal <= 0){
            regex = "0|([1-9][0-9]*)";
        }else{
            regex = "^(([1-9]{1}\\d*)|([0]{1}))(\\.(\\d){0,"+decimal+"})?$";
        }
        return newText.matches(regex)?null:"";
    }
} 