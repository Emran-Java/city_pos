package acquire.settings.fragment;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.settings.BaseSettingGridFragment;
import acquire.settings.R;
import acquire.settings.fragment.clear.SettingClearFragment;
import acquire.settings.fragment.elecsign.SettingElecSignFragment;
import acquire.settings.fragment.merchant.SettingMerchantFragment;
import acquire.settings.fragment.password.SettingPasswordFragment;
import acquire.settings.fragment.pinpad.SettingPinpadFragment;
import acquire.settings.fragment.print.SettingPrintFragment;
import acquire.settings.fragment.scanner.SettingScannerFragment;
import acquire.settings.fragment.system.SettingSystemFragment;
import acquire.settings.fragment.update.SettingUpdateFragment;

/**
 * The main admin settings {@link Fragment}.
 *
 * @author Janson
 * @date 2019/2/13 9:19
 */
public class SettingFragment extends BaseSettingGridFragment {

    private SimpleCallback mCallback;

    @NonNull
    public static SettingFragment newInstance(SimpleCallback callback) {
        SettingFragment fragment = new SettingFragment();
        fragment.mCallback = callback;
        return fragment;
    }

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_title);
    }

    @Override
    protected List<GridItem> getItems() {
        List<GridItem> items = new ArrayList<>();
        //Merchant configuration
        items.add(new GridItem(R.drawable.settings_menu_merchant,R.color.settings_menu_light_pink_background, R.string.settings_menu_item_merchant, v ->
                mSupportDelegate.switchContent(new SettingMerchantFragment())
        ));
        //System configuration
        items.add(new GridItem(R.drawable.settings_menu_system,R.color.settings_menu_light_blue_background, R.string.settings_menu_item_system, v ->
                mSupportDelegate.switchContent(new SettingSystemFragment())
        ));
        //PIN pad
        items.add(new GridItem(R.drawable.settings_menu_pinpad,R.color.settings_menu_light_purple_background, R.string.settings_menu_item_pinpad, v ->
                mSupportDelegate.switchContent(new SettingPinpadFragment())
        ));
        //Password configuration
        items.add(new GridItem(R.drawable.settings_menu_password,R.color.settings_menu_light_pink_background, R.string.settings_menu_item_password, v ->
                mSupportDelegate.switchContent(new SettingPasswordFragment())
        ));
        //Print configuration
        items.add(new GridItem(R.drawable.settings_menu_printer,R.color.settings_menu_light_pink_background, R.string.settings_menu_item_print, v ->
                mSupportDelegate.switchContent(new SettingPrintFragment())
        ));
        //Signature configuration
        items.add(new GridItem(R.drawable.settings_menu_elec_sign,R.color.settings_menu_light_blue_background, R.string.settings_menu_item_signature, v ->
                mSupportDelegate.switchContent(new SettingElecSignFragment())
        ));
        //Scanner
        items.add(new GridItem(R.drawable.settings_menu_scanner,R.color.settings_menu_light_green_background, R.string.settings_menu_item_scanner, v ->
                mSupportDelegate.switchContent(new SettingScannerFragment())
        ));
        //Clear data
        items.add(new GridItem(R.drawable.settings_menu_clear,R.color.settings_menu_light_green_background, R.string.settings_menu_item_other, v ->
                mSupportDelegate.switchContent(new SettingClearFragment())
        ));
        //Update params
        items.add(new GridItem(R.drawable.settings_menu_update, R.color.settings_menu_light_purple_background,R.string.settings_menu_item_update, v ->
                mSupportDelegate.switchContent(new SettingUpdateFragment())
        ));
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return mCallback;
    }

}
