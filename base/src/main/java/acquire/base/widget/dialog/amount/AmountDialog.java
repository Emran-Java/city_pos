package acquire.base.widget.dialog.amount;

import android.content.Context;
import android.content.DialogInterface;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.StringRes;

import acquire.base.R;
import acquire.base.databinding.BaseDialogAmountBinding;
import acquire.base.utils.InputUtils;
import acquire.base.widget.AmountFilter;
import acquire.base.widget.dialog.BaseDialog;


/**
 * A dialog with an edit text.
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2019/2/16 16:13
 */
public class AmountDialog extends BaseDialog {
    private BaseDialogAmountBinding binding;

    protected AmountDialog(Context context) {
        super(context, R.style.BaseDialog_Edit);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogAmountBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void init() {
        setCancelable(false);
    }

    @Override
    public void dismiss() {
        InputUtils.hideKeyboard(binding.etContent);
        super.dismiss();
    }

    /**
     * A builder of {@link AmountDialog}
     */
    public static class Builder {
        private final Context context;
        private CharSequence title;
        private CharSequence content;
        private OnClickListener cancelListener;
        private OnConfimrListener confirmListener;
        private boolean backEnable = true;

        public Builder(Context context) {
            this.context = context;
        }


        public Builder setTitle(@StringRes int titleId) {
            this.title = context.getString(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }

        public Builder setContent(CharSequence content) {
            this.content = content;
            return this;
        }

        /**
         * Set cancel button
         */
        public Builder setCancelButton(OnClickListener listener) {
            this.cancelListener = listener;
            return this;
        }

        /**
         * Set confirm button
         */
        public Builder setConfirmButton(OnConfimrListener listener) {
            this.confirmListener = listener;
            return this;
        }

        public Builder setBackEnable(boolean enable) {
            this.backEnable = enable;
            return this;
        }
        /**
         * Create an {@link AmountDialog}
         */
        public AmountDialog create() {
            AmountDialog dialog = new AmountDialog(context);
            dialog.binding.tvTitle.setText(title);
            dialog.binding.etContent.setText(content);
            dialog.binding.etContent.setSelection(content.length());
            InputFilter[] filters = new InputFilter[2];
            filters[0] = new InputFilter.LengthFilter(12);
            filters[1] = new AmountFilter(2);
            dialog.binding.etContent.setFilters(filters);
            dialog.binding.btnCancel.setOnClickListener(v -> {
                dialog.dismiss();
                if (cancelListener != null){
                    cancelListener.onClick(dialog);
                }
            });
            //listen keyboard or navigation key
            dialog.setOnKeyListener(new OnKeyListener() {
                private int handleKey;
                @Override
                public boolean onKey(DialogInterface d, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        handleKey = keyCode;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (handleKey == keyCode) {
                            if (keyCode == KeyEvent.KEYCODE_BACK){
                                //cancel
                                if (backEnable) {
                                    dialog.binding.btnCancel.callOnClick();
                                    return true;
                                }
                            }else if (keyCode == KeyEvent.KEYCODE_ENTER){
                                //back
                                dialog.binding.btnConfirm.callOnClick();
                                return true;
                            }
                        }
                    }
                    return false;
                }
            });
            dialog.binding.btnConfirm.setOnClickListener(v -> {
                String text = dialog.binding.etContent.getText().toString();
                if (TextUtils.isEmpty(text)){
                    return;
                }
                dialog.dismiss();
                float amount = Float.parseFloat(text);
                if (confirmListener != null){
                    confirmListener.onClick(amount);
                }
            });
            return dialog;
        }

        /**
         * Create an {@link AmountDialog} and immediately displays the dialog.
         */
        public AmountDialog show() {
            AmountDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    /**
     * The callback is invoked when {@link AmountDialog} confirm button is pressed
     *
     * @author Janson
     * @date 2019/2/16 16:16
     */
    public interface OnConfimrListener {
        void onClick(float amount);
    }
}
