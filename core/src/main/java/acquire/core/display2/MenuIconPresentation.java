package acquire.core.display2;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.StringRes;

import java.util.LinkedHashMap;

import acquire.base.widget.dialog.menu.MenuDialog;
import acquire.base.widget.dialog.menu.adapter.MenuIconDialogAdapter;
import acquire.core.databinding.CorePresentationMenuIconBinding;

/**
 * A menu with icon presentation for dual screen
 *
 * @author Janson
 * @date 2023/1/11 17:21
 */
public class MenuIconPresentation extends BasePresentation {
    private final CorePresentationMenuIconBinding binding;

    public MenuIconPresentation(Context outerContext) {
        super(outerContext);
        binding = CorePresentationMenuIconBinding.inflate(LayoutInflater.from(getContext()));
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
        private LinkedHashMap<String,Integer> items;
        private MenuDialog.OnClickMenuListener confirmListener;

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
        public Builder setItems(LinkedHashMap<String,Integer> items) {
            this.items = items;
            return this;
        }

        public Builder setConfirmButton(MenuDialog.OnClickMenuListener listener) {
            this.confirmListener = listener;
            return this;
        }


        public MenuIconPresentation create() {
            final MenuIconPresentation menuIconPresentation = new MenuIconPresentation(outerContext);
            if (items == null) {
                throw new RuntimeException("MenuIconPresentation items can not be empty");
            }
            //title
            if (title != null) {
                menuIconPresentation.binding.tvTitle.setText(title);
                menuIconPresentation.binding.tvTitle.setVisibility(View.VISIBLE);
            }
            //menu list
            final MenuIconDialogAdapter menuAdapter = new MenuIconDialogAdapter(items, index -> {
                menuIconPresentation.dismiss();
                confirmListener.onSelect(index);
            });
            menuIconPresentation.binding.rvMenu.setAdapter(menuAdapter);

            return menuIconPresentation;
        }

        public MenuIconPresentation show() {
            MenuIconPresentation menuIconPresentation = create();
            menuIconPresentation.show();
            return menuIconPresentation;
        }
        
    }

}
