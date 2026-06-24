package acquire.core.fragment.card;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;

import acquire.base.widget.dialog.BaseDialog;
import acquire.core.databinding.CoreDialogCardReadingBinding;

/**
 * A card reading prompt dialog
 *
 * @author Janson
 * @date 2022/11/11 14:56
 */
public class CardReadingDialog extends BaseDialog {
    public CardReadingDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        CoreDialogCardReadingBinding binding = CoreDialogCardReadingBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void init() {
        setCancelable(false);
    }

}
