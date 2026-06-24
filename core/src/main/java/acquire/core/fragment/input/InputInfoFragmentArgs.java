package acquire.core.fragment.input;

import android.text.InputFilter;
import android.text.InputType;

/**
 * The arguments of {@link InputInfoFragment}
 *
 * @author Janson
 * @date 2020/6/10 20:07
 */
public class InputInfoFragmentArgs {
    private String hint;
    /**
     * @see InputType
     */
    private int inputType = InputType.TYPE_CLASS_TEXT;

    private int minLen;
    private int maxLen = 64;

    private InputFilter[] filters;


    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public int getInputType() {
        return inputType;
    }

    /**
     * set {@link android.widget.EditText} input type.
     * @see InputType
     */
    public void setInputType(int inputType) {
        this.inputType = inputType;
    }

    public int getMinLen() {
        return minLen;
    }

    public void setMinLen(int minLen) {
        this.minLen = minLen;
    }

    public int getMaxLen() {
        return maxLen;
    }

    public void setMaxLen(int maxLen) {
        this.maxLen = maxLen;
    }

    public InputFilter[] getFilters() {
        return filters;
    }

    public void setFilters(InputFilter... filters) {
        this.filters = filters;
    }
}
