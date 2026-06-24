package acquire.base.widget.dialog.image;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import acquire.base.databinding.BaseDialogImageBinding;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.dialog.BaseDialog;

/**
 * A dialog with a title、a message and two button.
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2020/2/19 12:53
 */
public class ImageDialog extends BaseDialog {

    private BaseDialogImageBinding binding;

    protected ImageDialog(Context context) {
        super(context);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogImageBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void init() {
        setCancelable(false);
        //Set the amount of dim behind the window
        // 0f Transparent background
//        getWindow().setDimAmount(0f);
    }


    /**
     * A builder of {@link ImageDialog}
     */
    public static class Builder {
        private final Context context;
        private CharSequence message;
        private CharSequence confirmText;
        private @ColorInt Integer confirmColor;
        private OnClickListener confirmListener;
        private CharSequence cancelText;
        private @ColorInt Integer cancelColor;
        private OnClickListener cancelListener;
        private long timeoutMillis;
        private TimeoutListener timeoutListener;
        private @DrawableRes Integer image;
        private boolean backEnable = true;
        private OnShowListener showListener;
        private boolean timeoutAttachConfirm;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setImage(@DrawableRes int image) {
            this.image = image;
            return this;
        }


        public Builder setMessage(@StringRes int messageId) {
            this.message = context.getString(messageId);
            return this;
        }

        public Builder setMessage(CharSequence message) {
            this.message = message;
            return this;
        }

        public Builder setCancelButton(OnClickListener listener) {
            this.cancelListener = listener;
            return this;
        }

        public Builder setCancelButton(@StringRes int textId, OnClickListener listener) {
            this.cancelText = context.getString(textId);
            this.cancelListener = listener;
            return this;
        }

        public Builder setCancelButton(CharSequence text, OnClickListener listener) {
            this.cancelText = text;
            this.cancelListener = listener;
            return this;
        }

        public Builder setCancelButton(@ColorRes int colorResId, @StringRes int textId, OnClickListener listener) {
            this.cancelColor = ContextCompat.getColor(context, colorResId);
            this.cancelText = context.getString(textId);
            this.cancelListener = listener;
            return this;
        }


        public Builder setCancelButton(@ColorRes int colorResId, CharSequence text, OnClickListener listener) {
            this.cancelColor = ContextCompat.getColor(context, colorResId);
            this.cancelText = text;
            this.cancelListener = listener;
            return this;
        }

        public Builder setConfirmButton(OnClickListener listener) {
            this.confirmListener = listener;
            return this;
        }

        public Builder setConfirmButton(@StringRes int textId, OnClickListener listener) {
            this.confirmText = context.getString(textId);
            this.confirmListener = listener;
            return this;
        }

        public Builder setConfirmButton(CharSequence text, OnClickListener listener) {
            this.confirmText = text;
            this.confirmListener = listener;
            return this;
        }

        public Builder setConfirmButton(@ColorRes int colorResId, @StringRes int textId, OnClickListener listener) {
            this.confirmColor = ContextCompat.getColor(context, colorResId);
            this.confirmText = context.getString(textId);
            this.confirmListener = listener;
            return this;
        }

        public Builder setConfirmButton(@ColorRes int colorResId, CharSequence text, OnClickListener listener) {
            this.confirmColor = ContextCompat.getColor(context, colorResId);
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
        public Builder setBackEnable(boolean enable) {
            this.backEnable = enable;
            return this;
        }

        public Builder setShowListener(OnShowListener listener) {
            this.showListener = listener;
            return this;
        }

        /**
         * Create a {@link ImageDialog}
         */
        public ImageDialog create() {
            final ImageDialog dialog = new ImageDialog(context);
            //image
            if (image != null) {
                dialog.binding.ivIcon.setImageResource(image);
                dialog.binding.ivIcon.setVisibility(View.VISIBLE);
            }
            //message
            if (message != null) {
                dialog.binding.tvMessage.setText(message);
                dialog.binding.tvMessage.setVisibility(View.VISIBLE);
            }
            //show listener
            dialog.setOnShowListener(showListener);
            //cancel button
            if (cancelText == null && cancelListener == null) {
                dialog.binding.btnCancel.setVisibility(View.GONE);
                dialog.binding.vLine.setVisibility(View.GONE);
            } else {
                if (cancelText != null) {
                    dialog.binding.btnCancel.setText(cancelText);
                }
                if (cancelColor != null) {
                    dialog.binding.btnCancel.setTextColor(cancelColor);
                }
                dialog.binding.btnCancel.setOnClickListener(v -> {
                    if (ViewUtils.isFastClick()) {
                        return;
                    }
                    dialog.dismiss();
                    if (cancelListener != null) {
                        cancelListener.onClick(dialog);
                    }
                });
            }
            //listen keyboard or navigation key
            dialog.setOnKeyListener(new OnKeyListener() {
                private int handleKey;

                @Override
                public boolean onKey(DialogInterface d, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        handleKey = keyCode;
                    } else if (event.getAction() == KeyEvent.ACTION_UP) {
                        if (handleKey == keyCode) {
                            if (keyCode == KeyEvent.KEYCODE_BACK) {
                                //cancel
                                if (backEnable) {
                                    dialog.binding.btnCancel.callOnClick();
                                    return true;
                                }
                            } else if (keyCode == KeyEvent.KEYCODE_ENTER) {
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
            if (confirmText == null && confirmListener == null) {
                dialog.binding.btnConfirm.setVisibility(View.GONE);
                dialog.binding.vLine.setVisibility(View.GONE);
            } else {
                if (confirmText != null) {
                    dialog.binding.btnConfirm.setText(confirmText);
                }
                if (confirmColor != null) {
                    dialog.binding.btnConfirm.setTextColor(confirmColor);
                }
                dialog.binding.btnConfirm.setOnClickListener(v -> {
                    if (ViewUtils.isFastClick()) {
                        return;
                    }
                    dialog.dismiss();
                    if (confirmListener != null) {
                        confirmListener.onClick(dialog);
                    }
                });
            }
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
            if (dialog.binding.btnConfirm.getVisibility() != View.VISIBLE && dialog.binding.btnCancel.getVisibility() != View.VISIBLE) {
                dialog.binding.llButtonContainer.setVisibility(View.GONE);
            }
            return dialog;
        }

        /**
         * Create a {@link ImageDialog} and immediately displays the dialog.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     MessageDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public ImageDialog show() {
            ImageDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
