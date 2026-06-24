package acquire.core.display2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.StringRes;

import java.util.List;

import acquire.base.utils.ViewUtils;
import acquire.base.widget.dialog.menu.MenuDialog;
import acquire.base.widget.dialog.menu.adapter.MenuDialogAdapter;
import acquire.core.databinding.CorePresentationMenuBinding;

/**
 * A menu with icon presentation for dual screen
 *
 * @author Janson
 * @date 2023/1/11 17:21
 */
public class MenuPresentation extends BasePresentation {
    private final CorePresentationMenuBinding binding;

    public MenuPresentation(Context outerContext) {
        super(outerContext);
        binding = CorePresentationMenuBinding.inflate(LayoutInflater.from(getContext()));
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(binding.getRoot());
    }

    public static class Builder {
        private final Context outerContext;
        private CharSequence title;
        /**
         * item name and drawable res id.
         */
        private List<String> items;
        private MenuDialog.OnClickMenuListener confirmListener;
        private View.OnClickListener cancelListener;

        public Builder(Context outerContext) {
            this.outerContext = outerContext;
        }

        public Builder setTitle(@StringRes int titleId) {
            this.title = outerContext.getString(titleId);
            return this;
        }

        public Builder setTitle(CharSequence title) {
            this.title = title;
            return this;
        }
        public Builder setItems(List<String> items) {
            this.items = items;
            return this;
        }

        public Builder setConfirmButton(MenuDialog.OnClickMenuListener listener) {
            this.confirmListener = listener;
            return this;
        }
        public Builder setCancelButton(View.OnClickListener cancelListener) {
            this.cancelListener = cancelListener;
            return this;
        }

        public MenuPresentation create() {
            final MenuPresentation menuPresentation = new MenuPresentation(outerContext);
            if (items == null) {
                throw new RuntimeException("MenuIconPresentation items can not be empty");
            }
            //title
            if (title != null) {
                menuPresentation.binding.tvTitle.setText(title);
                menuPresentation.binding.tvTitle.setVisibility(View.VISIBLE);
            }
            //menu list
            final MenuDialogAdapter menuAdapter = new MenuDialogAdapter(items, 0);
            menuPresentation.binding.rvMenu.setAdapter(menuAdapter);
            menuPresentation.binding.btnConfirm.setOnClickListener(v->{
                menuPresentation.dismiss();
                confirmListener.onSelect(menuAdapter.getSelected());
            });
            menuPresentation.binding.btnCancel.setOnClickListener(v-> {
                if (ViewUtils.isFastClick()) {
                    return;
                }
                menuPresentation.dismiss();
                if (cancelListener != null) {
                    cancelListener.onClick(v);
                }
            });
            return menuPresentation;
        }

        public MenuPresentation show() {
            MenuPresentation menuIconPresentation = create();
            menuIconPresentation.show();
            return menuIconPresentation;
        }
        
    }

}
