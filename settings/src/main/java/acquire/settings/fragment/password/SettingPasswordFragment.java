package acquire.settings.fragment.password;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.core.constant.PasswordType;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.TextItem;

/**
 * A {@link Fragment} that configures the authorization password.
 *
 * @author Janson
 * @date 2019/2/13 9:43
 */
public class SettingPasswordFragment extends BaseSettingFragment {

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_password);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Settings password
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_password_item_system_admin)
                .setOnClickListener(v ->
                        mSupportDelegate.switchContent(PasswordChangeFragment.newInstance(PasswordType.SYSTEM_ADMIN, new SimpleCallback() {
                            @Override
                            public void result() {
                                mSupportDelegate.popBackFragment(1);
                            }
                        })))
                .create());
        //Transaction password
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_password_item_admin)
                .setOnClickListener(v ->
                        mSupportDelegate.switchContent(PasswordChangeFragment.newInstance(PasswordType.ADMIN, new SimpleCallback() {
                            @Override
                            public void result() {
                                mSupportDelegate.popBackFragment(1);
                            }
                        })))
                .create());
        //Safe password
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_password_item_security)
                .setOnClickListener(v ->
                        mSupportDelegate.switchContent(PasswordChangeFragment.newInstance(PasswordType.SECURITY, new SimpleCallback() {
                            @Override
                            public void result() {
                                mSupportDelegate.popBackFragment(1);
                            }
                        })))
                .create());
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }
}
