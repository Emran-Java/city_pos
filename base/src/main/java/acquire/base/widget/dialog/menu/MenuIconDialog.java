package acquire.base.widget.dialog.menu;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.StringRes;

import java.util.LinkedHashMap;

import acquire.base.databinding.BaseDialogMenuIconBinding;
import acquire.base.utils.LoggerUtils;
import acquire.base.widget.dialog.BaseDialog;
import acquire.base.widget.dialog.menu.adapter.MenuIconDialogAdapter;

/**
 * A dialog with a icon menu.
 * <p>Create this dialog by {@link Builder}</p>
 *
 * @author Janson
 * @date 2023/1/11 16:20
 */
public class MenuIconDialog extends BaseDialog {
    private BaseDialogMenuIconBinding binding;

    protected MenuIconDialog(Context context) {
        super(context);
    }

    @Override
    protected View bindView(LayoutInflater inflater) {
        binding = BaseDialogMenuIconBinding.inflate(inflater);
        return binding.getRoot();
    }

    @Override
    protected void init() {
        setCancelable(false);
    }

    public static class Builder {
        private final Context context;
        private CharSequence title;
        /**
         * item name and drawable res id.
         */
        private LinkedHashMap<String,Integer> items;
        private MenuDialog.OnClickMenuListener confirmListener;
        private OnClickListener cancelListener;
        private long timeoutMillis;
        private TimeoutListener timeoutListener;

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
        public Builder setItems(LinkedHashMap<String,Integer> items) {
            this.items = items;
            return this;
        }


        public Builder setCancelButton(OnClickListener listener) {
            this.cancelListener = listener;
            return this;
        }


        public Builder setConfirmButton(MenuDialog.OnClickMenuListener listener) {
            this.confirmListener = listener;
            return this;
        }


        public Builder setTimeout(long timeoutMillis, TimeoutListener listener) {
            this.timeoutMillis = timeoutMillis;
            this.timeoutListener = listener;
            return this;
        }

        /**
         * Create a {@link MenuIconDialog}
         */
        public MenuIconDialog create() {
            final MenuIconDialog dialog = new MenuIconDialog(context);
            if (items == null) {
                throw new RuntimeException("MenuIconDialog items can not be empty");
            }
            //title
            if (title != null) {
                dialog.binding.tvTitle.setText(title);
                dialog.binding.tvTitle.setVisibility(View.VISIBLE);
            }
            //menu list
            final MenuIconDialogAdapter menuAdapter = new MenuIconDialogAdapter(items, index -> {
                dialog.dismiss();
                confirmListener.onSelect(index);
            });
            dialog.binding.rvMenu.setAdapter(menuAdapter);
            //cancel
            if (cancelListener != null){
                dialog.setOnKeyListener((kd, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        dialog.dismiss();
                        cancelListener.onClick(dialog);
                        return true;
                    }
                    return false;
                });
            }
            //set timeout
            if (timeoutMillis > 0 && timeoutListener != null) {
                dialog.setTimeOut(timeoutMillis, d -> {
                    LoggerUtils.e("Time out");
                    dialog.dismiss();
                    timeoutListener.onTimeout(dialog);
                });
            }
            return dialog;
        }

        /**
         * Create a {@link MenuIconDialog} and immediately displays the dialog.
         * <p>
         * Calling this method is functionally identical to:
         * <pre>
         *     MenuIconDialog dialog = builder.create();
         *     dialog.show();
         * </pre>
         */
        public MenuIconDialog show() {
            MenuIconDialog dialog = create();
            dialog.show();
            return dialog;
        }
    }
}
