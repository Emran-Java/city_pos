package acquire.core.display2;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ViewUtils;
import acquire.core.databinding.CorePresentationDialogBinding;

/**
 * A prompt presentation for dual screen
 *
 * @author Janson
 * @date 2022/8/29 13:51
 */
public class DialogPresentation extends BasePresentation {
    private final CorePresentationDialogBinding binding;
    private CountDownTimer countDownTimer;
    public DialogPresentation(Context outerContext) {
        super(outerContext);
        binding = CorePresentationDialogBinding.inflate(LayoutInflater.from(getContext()));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
    }
    @Override
    protected void onStop() {
        super.onStop();
        //close timer
        if (countDownTimer != null){
            countDownTimer.cancel();
        }
    }

    public Button getCancelButton(){
        return binding.btnCancel;
    }
    public Button getConfirmButton(){
        return binding.btnConfirm;
    }
    /**
     * A builder of {@link DialogPresentation}
     */
    public static class Builder {
        private final Context outerContext;
        private CharSequence message;
        private CharSequence confirmText;
        private @ColorInt
        Integer confirmColor;
        private OnClickListener confirmListener;
        private CharSequence cancelText;
        private @ColorInt Integer cancelColor;
        private OnClickListener cancelListener;
        private long timeoutMillis;
        private Runnable timeoutListener;
        private @DrawableRes Integer image;

        public Builder(Context outerContext) {
            this.outerContext = outerContext;
        }

        public Builder setImage(@DrawableRes int image) {
            this.image = image;
            return this;
        }

        public DialogPresentation.Builder setMessage(@StringRes int messageId) {
            this.message = outerContext.getString(messageId);
            return this;
        }

        public DialogPresentation.Builder setMessage(CharSequence message) {
            this.message = message;
            return this;
        }

        public DialogPresentation.Builder setCancelButton(OnClickListener listener) {
            this.cancelListener = listener;
            return this;
        }

        public DialogPresentation.Builder setCancelButton(@StringRes int textId, OnClickListener listener) {
            this.cancelText = outerContext.getString(textId);
            this.cancelListener = listener;
            return this;
        }

        public DialogPresentation.Builder setCancelButton(CharSequence text, OnClickListener listener) {
            this.cancelText = text;
            this.cancelListener = listener;
            return this;
        }

        public DialogPresentation.Builder setCancelButton(@ColorRes int colorResId, @StringRes int textId, OnClickListener listener) {
            this.cancelColor = ContextCompat.getColor(outerContext, colorResId);
            this.cancelText = outerContext.getString(textId);
            this.cancelListener = listener;
            return this;
        }


        public DialogPresentation.Builder setCancelButton(@ColorRes int colorResId, CharSequence text, OnClickListener listener) {
            this.cancelColor = ContextCompat.getColor(outerContext, colorResId);
            this.cancelText = text;
            this.cancelListener = listener;
            return this;
        }

        public DialogPresentation.Builder setConfirmButton(OnClickListener listener) {
            this.confirmListener = listener;
            return this;
        }

        public DialogPresentation.Builder setConfirmButton(@StringRes int textId, OnClickListener listener) {
            this.confirmText = outerContext.getString(textId);
            this.confirmListener = listener;
            return this;
        }

        public DialogPresentation.Builder setConfirmButton(CharSequence text, OnClickListener listener) {
            this.confirmText = text;
            this.confirmListener = listener;
            return this;
        }

        public DialogPresentation.Builder setConfirmButton(@ColorRes int colorResId, @StringRes int textId, OnClickListener listener) {
            this.confirmColor = ContextCompat.getColor(outerContext, colorResId);
            this.confirmText = outerContext.getString(textId);
            this.confirmListener = listener;
            return this;
        }

        public DialogPresentation.Builder setConfirmButton(@ColorRes int colorResId, CharSequence text, OnClickListener listener) {
            this.confirmColor = ContextCompat.getColor(outerContext, colorResId);
            this.confirmText = text;
            this.confirmListener = listener;
            return this;
        }

        public DialogPresentation.Builder setTimeout(long timeoutMillis, Runnable listener) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutListener = listener;
            return this;
        }
        /**
         * Create a {@link DialogPresentation}
         */
        public DialogPresentation create() {
            final DialogPresentation presentation = new DialogPresentation(outerContext);

            //image
            if (image != null){
                presentation.binding.ivIcon.setImageResource(image);
                presentation.binding.ivIcon.setVisibility(View.VISIBLE);
            }
            //message
            if (message != null) {
                presentation.binding.tvMessage.setText(message);
            }
            //cancel button
            if (cancelText == null && cancelListener == null) {
                presentation.binding.btnCancel.setVisibility(View.GONE);
                presentation.binding.vLine.setVisibility(View.GONE);
            } else {
                if (cancelText != null) {
                    presentation.binding.btnCancel.setText(cancelText);
                }
                if (cancelColor != null) {
                    presentation.binding.btnCancel.setTextColor(cancelColor);
                }
                presentation.binding.btnCancel.setOnClickListener(v -> {
                    if (ViewUtils.isFastClick()) {
                        return;
                    }
                    presentation.dismiss();
                    if (cancelListener != null) {
                        cancelListener.onClick(presentation, DialogInterface.BUTTON_NEGATIVE);
                    }
                });
            }
            //confirm button
            if (confirmText == null && confirmListener == null) {
                presentation.binding.btnConfirm.setVisibility(View.GONE);
                presentation.binding.vLine.setVisibility(View.GONE);
            } else {
                if (confirmText != null) {
                    presentation.binding.btnConfirm.setText(confirmText);
                }
                if (confirmColor != null) {
                    presentation.binding.btnConfirm.setTextColor(confirmColor);
                }
                presentation.binding.btnConfirm.setOnClickListener(v -> {
                    if (ViewUtils.isFastClick()) {
                        return;
                    }
                    presentation.dismiss();
                    if (confirmListener != null) {
                        confirmListener.onClick(presentation, DialogInterface.BUTTON_POSITIVE);
                    }
                });
            }
            //set timeout
            if (timeoutMillis > 0 && timeoutListener != null) {
                presentation.countDownTimer = new CountDownTimer(timeoutMillis, 500) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                    }

                    @Override
                    public void onFinish() {
                        LoggerUtils.e("Time out");
                        presentation.dismiss();
                        timeoutListener.run();
                    }
                }.start();
            }
            if (presentation.binding.btnConfirm.getVisibility() != View.VISIBLE && presentation.binding.btnCancel.getVisibility() != View.VISIBLE) {
                presentation.binding.llButtonContainer.setVisibility(View.GONE);
            }
            return presentation;
        }

        /**
         * Create a {@link DialogPresentation} and immediately displays the presentation.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     DialogPresentation presentation = builder.create();
         *     presentation.show();
         * </pre>
         */
        public DialogPresentation show() {
            DialogPresentation presentation = create();
            presentation.show();
            return presentation;
        }
    }

}
