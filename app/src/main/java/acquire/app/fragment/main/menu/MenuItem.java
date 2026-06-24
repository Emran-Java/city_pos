package acquire.app.fragment.main.menu;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.List;

import acquire.base.BaseApplication;
import acquire.base.activity.BaseActivity;
import acquire.core.tools.TransUtils;


/**
 * A menu item of {@link MainMenu}
 *
 * @author Janson
 * @date 2019/4/26 14:24
 */
public class MenuItem{
    /**
     *  the transaction type for this item.
     */
    private String transType;
    /**
     * item icon
     */
    private final @DrawableRes int icon;
    /**
     * item background color
     */
    private final @ColorRes int colorId;
    /**
     * If not-non, the item will jump to a secondary directory with sub-items.And {@link #transType} will be invalid.
     */
    private List<MenuItem> subItems;
    /**
     * If not-NULL, the secondary directory title.
     */
    private final String name;

    private OnClickItemListener onClickItemListener;

    public MenuItem(String transType,@DrawableRes int icon,@ColorRes int colorId) {
        this.transType = transType;
        this.icon = icon;
        this.colorId = colorId;
        if (transType == null){
            this.name = "";
        }else{
            this.name = TransUtils.getName(transType);
        }

    }

    public MenuItem(List<MenuItem> subItems, @StringRes int nameResId, @DrawableRes int icon, @ColorRes int colorId) {
        this.subItems = subItems;
        this.icon = icon;
        this.colorId = colorId;
        this.name = BaseApplication.getAppString(nameResId);
    }

    public MenuItem(OnClickItemListener onClickItemListener, @StringRes int nameResId, @DrawableRes int icon, @ColorRes int colorId) {
        this.onClickItemListener = onClickItemListener;
        this.icon = icon;
        this.colorId = colorId;
        this.name = BaseApplication.getAppString(nameResId);
    }

    public String getTransType() {
        return transType;
    }

    public @DrawableRes int getIcon() {
        return icon;
    }

    public int getColorId() {
        return colorId;
    }

    public List<MenuItem> getSubItems() {
        return subItems;
    }

    public String getName() {
        return name;
    }

    public OnClickItemListener getOnClickItemListener() {
        return onClickItemListener;
    }

    public interface OnClickItemListener {
        void onItemClick(BaseActivity activity);
    }

    @NonNull
    @Override
    public String toString() {
        if (subItems == null){
            return "MenuItem{" +
                    "transType='" + transType + '\'' +
                    ", name='" + name + '\'' +
                    '}';
        }else{
            StringBuilder log = new StringBuilder();
            for (MenuItem child : subItems) {
                log.append(child).append(" ");
            }
            return "MenuItem{" +
                    "name='" + name + '\'' +
                    ", subItems=[" + log +']' +
                    '}';
        }
    }

}
