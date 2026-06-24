package acquire.base.widget.dialog.menu;

import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.IntRange;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acquire.base.databinding.BaseDialogMenuBinding;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ViewUtils;
import acquire.base.widget.dialog.BaseDialog;
import acquire.base.widget.dialog.menu.adapter.MenuDialogAdapter;

/**
 * A dialog with a menu.
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2020/2/19 11:00
 */
public class MenuDialog extends BaseDialog {
    private BaseDialogMenuBinding binding;

    protected MenuDialog(Context context) {
        super(context);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogMenuBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void init() {
        setCancelable(false);
    }

    public static class Builder {
        private final Context context;
        private int defaultItem;
        private CharSequence title;
        private List<String> items;
        private CharSequence confirmText;
        private OnClickMenuListener confirmListener;
        private CharSequence cancelText;
        private BaseDialog.OnClickListener cancelListener;
        private long timeoutMillis;
        private BaseDialog.TimeoutListener timeoutListener;
        private boolean backEnable = true;
        private boolean timeoutAttachConfirm;

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

        public Builder setDefaultItem(int defaultItem) {
            this.defaultItem = defaultItem;
            return this;
        }

        public Builder setItems(List<String> items) {
            this.items = items;
            return this;
        }

        public Builder setItems(String... items) {
            this.items = new ArrayList<>();
            this.items.addAll(Arrays.asList(items));
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

        public Builder setConfirmButton(OnClickMenuListener listener) {
            this.confirmListener = listener;
            return this;
        }

        public Builder setConfirmButton(@StringRes int textId, OnClickMenuListener listener) {
            this.confirmText = context.getString(textId);
            this.confirmListener = listener;
            return this;
        }

        public Builder setConfirmButton(CharSequence text, OnClickMenuListener listener) {
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

        /**
         * Create a {@link MenuDialog}
         */
        public MenuDialog create() {
            final MenuDialog dialog = new MenuDialog(context);
            if (items == null) {
                throw new RuntimeException("MenuDialog items can not be empty");
            }
            //title
            if (title != null) {
                dialog.binding.tvTitle.setText(title);
                dialog.binding.tvTitle.setVisibility(View.VISIBLE);
            }
            //menu list
            final MenuDialogAdapter menuAdapter = new MenuDialogAdapter(items, defaultItem);
            dialog.binding.rvMenu.setAdapter(menuAdapter);

            //cancel button
            if (cancelText == null && cancelListener == null) {
                dialog.binding.btnCancel.setVisibility(View.GONE);
                dialog.binding.vLine.setVisibility(View.GONE);
            }else{
                if (cancelText != null) {
                    dialog.binding.btnCancel.setText(cancelText);
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

            //confirm button
            if (confirmText != null) {
                dialog.binding.btnConfirm.setText(confirmText);
            }
            dialog.binding.btnConfirm.setOnClickListener(v -> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                dialog.dismiss();
                if (confirmListener != null) {
                    confirmListener.onSelect(menuAdapter.getSelected());
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
         * Create a {@link MenuDialog} and immediately displays the dialog.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     MenuDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public MenuDialog show() {
            MenuDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }

    /**
     * {@link MenuDialog} confirm button listener
     *
     * @author Janson
     * @date 2019/2/16 16:07
     */
    public interface OnClickMenuListener {
        void onSelect(@IntRange(from = 0) int index);
    }
}
