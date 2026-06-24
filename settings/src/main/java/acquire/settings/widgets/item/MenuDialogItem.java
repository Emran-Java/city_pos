package acquire.settings.widgets.item;

import android.content.Context;
import android.view.View;

import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acquire.base.activity.BaseActivity;
import acquire.base.utils.ParamsUtils;
import acquire.base.widget.dialog.menu.MenuDialog;
import acquire.settings.widgets.item.listener.ItemGetSet;


/**
 * Menu item
 *
 * @author Janson
 * @date 2019/2/13 18:59
 */
public class MenuDialogItem extends TextItem {
    protected MenuDialogItem(Context context) {
        super(context);
    }

    @Override
    public View getView() {
        return binding.getRoot();
    }

    /**
     * {@link MenuDialogItem} Builder
     */
    public static class Builder {
        private List<MenuBean> paramBeans;
        private final BaseActivity activity;
        private String title;
        private ItemGetSet<String> getSet;
        private MenuDialog.OnClickMenuListener onChangeListener;

        public Builder(BaseActivity activity) {
            this.activity = activity;
        }

        /**
         * Set title
         *
         * @param title title text
         * @return {@code Builder.this}
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set title
         *
         * @param titleId title resource id
         * @return {@code Builder.this}
         */
        public Builder setTitle(@StringRes int titleId) {
            this.title = activity.getString(titleId);
            return this;
        }

        /**
         * Set params key
         *
         * @param paramKey params key
         * @return {@code Builder.this}
         */
        public Builder setParamKey(String paramKey) {
            this.getSet = new ItemGetSet<String>() {
                @Override
                public String getValue() {
                    return ParamsUtils.getString(paramKey);
                }

                @Override
                public void setValue(String value) {
                    ParamsUtils.setString(paramKey,value);
                }
            };
            return this;
        }
        public Builder setIGetSet(ItemGetSet<String> getSet) {
            this.getSet = getSet;
            return this;
        }
        /**
         * Set menu items
         *
         * @param paramBeans menu items.e.g. {@link MenuBean}
         * @return {@code Builder.this}
         */
        public Builder setParamBean(List<MenuBean> paramBeans) {
            this.paramBeans = paramBeans;
            return this;
        }

        /**
         * Set menu items
         *
         * @param paramBeans menu items.e.g. {@link MenuBean}
         * @return {@code Builder.this}
         */
        public Builder setParamBean(MenuBean... paramBeans) {
            this.paramBeans = Arrays.asList(paramBeans);
            return this;
        }

        public Builder setOnChangeListener(MenuDialog.OnClickMenuListener onChangeListener) {
            this.onChangeListener = onChangeListener;
            return this;
        }

        public MenuDialogItem create() {
            MenuDialogItem item = new MenuDialogItem(activity);
            //set title
            item.binding.tvTitle.setText(title);
            //set content
            item.binding.tvMessage.setText(paramBeans.get(getCurPosition()).getName());
            //confirm button
            item.binding.getRoot().setOnClickListener(v ->{
                List<String> items = new ArrayList<>();
                for (MenuBean paramBean : paramBeans) {
                    items.add(paramBean.getName());
                }
                int curPosition = getCurPosition();
                new MenuDialog.Builder(activity)
                        .setTitle(title)
                        .setDefaultItem(curPosition)
                        .setItems(items)
                        .setCancelButton(v2 -> {})
                        .setConfirmButton(position -> {
                            item.binding.tvMessage.setText(paramBeans.get(position).getName());
                            if (getSet != null){
                                getSet.setValue(String.valueOf(paramBeans.get(position).getValue()));
                            }
                            if (onChangeListener != null && curPosition != position){
                                onChangeListener.onSelect(position);
                            }
                        })
                        .show();
            });
            return item;
        }

        /**
         * Get current selected index
         *
         * @return selected index，return -1 if error
         */
        private int getCurPosition() {
            String value = null;
            if (getSet != null){
                value = getSet.getValue();
            }
            for (int i = 0; i < paramBeans.size(); i++) {
                if (value != null && value.equals(String.valueOf(paramBeans.get(i).getValue()))) {
                    return i;
                }
            }
            return 0;
        }
    }

    public static class MenuBean{
        private final String name;
        private final Object value;

        public MenuBean(String name, Object value) {
            this.name = name;
            this.value = value;

        }

        public String getName() {
            return name;
        }


        public Object getValue() {
            return value;
        }
    }
}
