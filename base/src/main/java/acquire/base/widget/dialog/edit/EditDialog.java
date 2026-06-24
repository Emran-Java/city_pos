package acquire.base.widget.dialog.edit;

import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acquire.base.R;
import acquire.base.databinding.BaseDialogEditBinding;
import acquire.base.utils.InputUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.CharacterFilter;
import acquire.base.widget.dialog.BaseDialog;


/**
 * A dialog with an edit text.
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2019/2/16 16:13
 */
public class EditDialog extends BaseDialog {
    private BaseDialogEditBinding binding;

    protected EditDialog(Context context) {
        super(context, R.style.BaseDialog_Edit);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogEditBinding.inflate(inflater);
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


    public EditText getContentEdit() {
        return binding.etContent;
    }

    /**
     * A builder of {@link EditDialog}
     */
    public static class Builder {
        private final Context context;
        private CharSequence title;
        private CharSequence content;
        private CharSequence confirmText;
        private @ColorInt
        Integer confirmColor;
        private EditDialog.OnConfimrListener confirmListener;
        private CharSequence cancelText;
        private @ColorInt
        Integer cancelColor;
        private Integer inputType;
        private String digits;
        private InputFilter[] filters;
        private long timeoutMillis;
        private boolean singleLine;
        private int maxLength;
        private int minLength;
        private TimeoutListener timeoutListener;
        private BaseDialog.OnClickListener cancelListener;
        private boolean backEnable = true;
        private boolean timeoutAttachConfirm;

        public Builder(Context context) {
            this.context = context;
        }


        public Builder setTitle(@StringRes int titleId) {
            this.title = this.context.getString(titleId);
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
         * Set input type of {@link EditText}
         *
         * @param inputType use {@link android.text.InputType}
         */
        public Builder setInputType(Integer inputType) {
            this.inputType = inputType;
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
         * Set cancel button
         *
         * @param textId cancel button text。
         */
        public Builder setCancelButton(@StringRes int textId, OnClickListener listener) {
            this.cancelText = this.context.getString(textId);
            this.cancelListener = listener;
            return this;
        }

        /**
         * Set cancel button
         *
         * @param text cancel button text。
         */
        public Builder setCancelButton(CharSequence text, OnClickListener listener) {
            this.cancelText = text;
            this.cancelListener = listener;
            return this;
        }

        /**
         * Set cancel button
         *
         * @param colorResId cancel button color
         * @param textId     cancel button text。
         */
        public Builder setCancelButton(@ColorRes int colorResId, @StringRes int textId, OnClickListener listener) {
            this.cancelColor = ContextCompat.getColor(this.context, colorResId);
            this.cancelText = this.context.getString(textId);
            this.cancelListener = listener;
            return this;
        }

        /**
         * Set cancel button
         *
         * @param colorResId cancel button color
         * @param text       cancel button text。
         */
        public Builder setCancelButton(@ColorRes int colorResId, CharSequence text, OnClickListener listener) {
            this.cancelColor = ContextCompat.getColor(this.context, colorResId);
            this.cancelText = text;
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

        /**
         * Set confirm button
         *
         * @param textId cancel button text。
         */
        public Builder setConfirmButton(@StringRes int textId, OnConfimrListener listener) {
            this.confirmText = this.context.getString(textId);
            this.confirmListener = listener;
            return this;
        }

        /**
         * Set confirm button
         *
         * @param text cancel button text。
         */
        public Builder setConfirmButton(CharSequence text, OnConfimrListener listener) {
            this.confirmText = text;
            this.confirmListener = listener;
            return this;
        }

        /**
         * Set confirm button
         *
         * @param colorResId confirm button color
         * @param textId     cancel button text。
         */
        public Builder setConfirmButton(@ColorRes int colorResId, @StringRes int textId, OnConfimrListener listener) {
            this.confirmColor = ContextCompat.getColor(this.context, colorResId);
            this.confirmText = this.context.getString(textId);
            this.confirmListener = listener;
            return this;
        }

        /**
         * Set confirm button
         *
         * @param colorResId confirm button color
         * @param text       cancel button text。
         */
        public Builder setConfirmButton(@ColorRes int colorResId, CharSequence text, OnConfimrListener listener) {
            this.confirmColor = ContextCompat.getColor(this.context, colorResId);
            this.confirmText = text;
            this.confirmListener = listener;
            return this;
        }


        public Builder setTimeout(long timeoutMillis, TimeoutListener listener) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutListener = listener;
            return this;
        }
        public Builder setConfirmTimeout(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutAttachConfirm = true;
            return this;
        }
        public Builder setCancelTimeout(long timeoutMillis) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutAttachConfirm = false;
            return this;
        }
        /**
         * Set edit filters
         */
        public Builder setFilters(InputFilter... filters) {
            this.filters = filters;
            return this;
        }

        /**
         * Set edit digits limits
         */
        public Builder setDigits(String digits) {
            this.digits = digits;
            return this;
        }


        public Builder setSingleLine(boolean singleLine) {
            this.singleLine = singleLine;
            return this;
        }


        public Builder setFixLength(int fixLength) {
            this.maxLength = fixLength;
            this.minLength = fixLength;
            return this;
        }

        public Builder setMaxLength(int maxLength) {
            this.maxLength = maxLength;
            return this;
        }

        public Builder setMinLength(int minLength) {
            this.minLength = minLength;
            return this;
        }

        public Builder setBackEnable(boolean enable) {
            this.backEnable = enable;
            return this;
        }

        /**
         * Create an {@link EditDialog}
         */
        public EditDialog create() {
            final EditDialog dialog = new EditDialog(this.context);
            //clear edit button
            dialog.binding.ivClear.setOnClickListener(v -> dialog.binding.etContent.setText(""));
            //set text watcher
            //add length watcher
            dialog.binding.etContent.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    //clear image will hide if edit is null
                    if (s.length() == 0) {
                        dialog.binding.ivClear.setVisibility(View.GONE);
                    } else {
                        dialog.binding.ivClear.setVisibility(View.VISIBLE);
                    }

                    //show length hint
                    String lengthHint;
                    if (maxLength > 0) {
                        lengthHint = s.length() + "/" + maxLength;
                    } else {
                        lengthHint = String.valueOf(s.length());
                    }
                    dialog.binding.tvLengthHint.setText(lengthHint);
                }

                @Override
                public void afterTextChanged(Editable s) {

                }
            });
            //set single line
            dialog.binding.etContent.setSingleLine(singleLine);
            //set input type
            if (inputType != null) {
                dialog.binding.etContent.setInputType(inputType);
            }
            //title
            if (this.title != null) {
                dialog.binding.tvTitle.setText(this.title);
                dialog.binding.tvTitle.setVisibility(View.VISIBLE);
            }
            //set init content
            if (content != null) {
                dialog.binding.etContent.setText(content);
                dialog.binding.etContent.setSelection(content.length());
            }
            //filters
            List<InputFilter> editFilters = new ArrayList<>();
            if (this.filters != null) {
                editFilters.addAll(Arrays.asList(this.filters));
            }
            //set digital
            if (!TextUtils.isEmpty(digits)) {
                editFilters.add(new CharacterFilter(digits));
            }
            //set input length
            if (maxLength > 0) {
                editFilters.add(new InputFilter.LengthFilter(maxLength));
            }
            //set filters
            if (editFilters.size() > 0) {
                dialog.binding.etContent.setFilters(editFilters.toArray(new InputFilter[0]));
            }

            //cancel button
            if (this.cancelText != null) {
                dialog.binding.btnCancel.setText(cancelText);
            }
            if (this.cancelColor != null) {
                dialog.binding.btnCancel.setTextColor(cancelColor);
            }
            //bottom back button
            dialog.binding.btnCancel.setOnClickListener(v -> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                dialog.dismiss();
                if (this.cancelListener != null) {
                    this.cancelListener.onClick(dialog);
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
            //confirm button
            if (this.confirmText != null) {
                dialog.binding.btnConfirm.setText(confirmText);
            }
            if (this.confirmColor != null) {
                dialog.binding.btnConfirm.setTextColor(confirmColor);
            }
            dialog.binding.btnConfirm.setOnClickListener(v -> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                if (this.maxLength == this.minLength
                        && this.maxLength != 0
                        && dialog.binding.etContent.length() != this.maxLength) {
                    dialog.binding.etContent.setError(context.getString(R.string.base_dialog_edit_length_wrong_format, this.maxLength));
                    return;
                } else if (this.minLength != 0 && dialog.binding.etContent.length() < this.minLength) {
                    dialog.binding.etContent.setError(context.getString(R.string.base_dialog_edit_too_short_format, this.minLength));
                    return;
                }
                if (this.confirmListener != null) {
                    if (this.confirmListener.onClick(dialog, dialog.binding.etContent.getText().toString())) {
                        dialog.dismiss();
                    }
                } else {
                    dialog.dismiss();
                }
            });
            //set timeout
            if (timeoutMillis > 0 ) {
                if (timeoutListener != null){
                    dialog.setTimeOut(timeoutMillis, d -> {
                        LoggerUtils.e("Time out");
                        dialog.dismiss();
                        timeoutListener.onTimeout(dialog);
                    });
                }else{
                    dialog.setTimeOut(timeoutMillis,timeoutAttachConfirm?dialog.binding.btnConfirm:dialog.binding.btnCancel);
                }
            }
            return dialog;
        }

        /**
         * Create an {@link EditDialog} and immediately displays the dialog.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     EditDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public EditDialog show() {
            EditDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    /**
     * The callback is invoked when {@link EditDialog} confirm button is pressed
     *
     * @author Janson
     * @date 2019/2/16 16:16
     */
    public interface OnConfimrListener {
        /**
         * click confirm button
         *
         * @param editDialog dialog
         * @param text       edit text content
         * @return true success,false failed;
         */
        boolean onClick(EditDialog editDialog, String text);
    }
}
