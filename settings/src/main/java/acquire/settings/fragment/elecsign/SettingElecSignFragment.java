package acquire.settings.fragment.elecsign;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.core.constant.ParamsConst;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.SwitchItem;

/**
 * A {@link Fragment} that configures electric signature.
 *
 * @author Janson
 * @date 2019/2/15 21:07
 */
public class SettingElecSignFragment extends BaseSettingFragment {


    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_signature);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Open electric signatures
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_signature_item_open)
                .setParamKey(ParamsConst.PARAMS_KEY_ELECSIGN_IS_SUPPORT)
                .create());
        return items;
    }


    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}
