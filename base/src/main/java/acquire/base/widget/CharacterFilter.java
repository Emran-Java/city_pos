package acquire.base.widget;

import android.text.InputFilter;
import android.text.Spanned;

import androidx.annotation.NonNull;

/**
 * A specify character filter used fo {@link android.widget.EditText}
 *
 * @author Janson
 * @date 2020/8/25 11:28
 */
public class CharacterFilter implements InputFilter {
    private final String characters;

    public CharacterFilter(String characters) {
        this.characters = characters;
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
        String newText =destText.substring(0,dstart)+sourceText+destText.substring(dstart);
        for (int i = 0; i < newText.length(); i++) {
            if (!characters.contains(newText.charAt(i) + "")) {
                return "";
            }
        }
        return null;
    }
} 