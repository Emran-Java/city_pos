package acquire.settings.fragment.system;

import android.util.DisplayMetrics;

import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.activity.callback.SimpleCallback;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.core.constant.Characters;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.SaverScreenTime;
import acquire.core.constant.Scanner;
import acquire.core.constant.ScreenHeightDps;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;
import acquire.settings.BaseSettingFragment;
import acquire.settings.R;
import acquire.settings.widgets.IItemView;
import acquire.settings.widgets.item.AmountItem;
import acquire.settings.widgets.item.EditTextItem;
import acquire.settings.widgets.item.MenuDialogItem;
import acquire.settings.widgets.item.SwitchItem;
import acquire.settings.widgets.item.TextItem;
import acquire.settings.widgets.item.listener.ItemGetSet;

/**
 * A {@link Fragment} that configures system parameters.
 *
 * @author Janson
 * @date 2019/2/13 9:48
 */
public class SettingSystemFragment extends BaseSettingFragment {


    private TextItem currencyItem ;

    @Override
    protected String getTitle() {
        return getString(R.string.settings_menu_item_system);
    }

    @Override
    protected List<IItemView> getItems() {
        List<IItemView> items = new ArrayList<>();
        //Support trans
        items.add(new TextItem.Builder(mActivity)
                .setTitle(getString(R.string.settings_system_item_support_transaction))
                .setOnClickListener(v -> mSupportDelegate.switchContent(new SystemSupportFragment()))
                .create());
        //Current Date/Time
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_time)
                .setOnClickListener(v -> mSupportDelegate.switchContent(new SystemTimeFragment()))
                .create());
        //Trace No
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_trace_no)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_TRACE_NO)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(6)
                .setInputMaxLen(6)
                .create());

        //Refund max amount(long)
        items.add(new AmountItem.Builder(mActivity)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_MAX_REFUND_AMOUNT)
                .setTitle(R.string.settings_system_item_refund_max_money)
                .setMaxValue(9999999999.99d)
                .setMinValue(1.00d)
                .create());
        //Max record count
        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_max_count)
                .setParamKey(ParamsConst.PARAMS_KEY_BASE_MAX_TRANS_COUNT)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(1)
                .setInputMaxLen(4)
                .create());
        //Currency code
        currencyItem = new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_currency_code)
                .setMessage(getCurrencyString())
                .setOnClickListener(v -> {
                    mActivity.mSupportDelegate.switchContent(CurrencyFragment.newInstance(new SimpleCallback() {
                        @Override
                        public void result() {
                            mSupportDelegate.popBackFragment(1);
                            currencyItem.setMessage(getCurrencyString());
                        }
                    }));
                })
                .create();
        items.add(currencyItem);

        items.add(new EditTextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_card_reader_timeout)
                .setParamKey(ParamsConst.PARAMS_KEY_CARD_READER_TIMEOUT)
                .setDigits(Characters.NUMBER)
                .setInputMinLen(2)
                .setInputMaxLen(3)
                .create());

        DisplayMetrics metrics = DisplayUtils.getDisplayMetrics(mActivity);
        float heightDps = metrics.heightPixels / metrics.density;
        if(BDevice.supportPhysicalKeyboard() &&heightDps<= ScreenHeightDps.HEIGHT_285_DPS){
            List<MenuDialogItem.MenuBean> menu = new ArrayList<>();
            menu.add(new MenuDialogItem.MenuBean(getString(R.string.settings_system_item_saver_time_15_seconds), SaverScreenTime.TIME_15_SECONDS));
            menu.add(new MenuDialogItem.MenuBean(getString(R.string.settings_system_item_saver_time_30_seconds), SaverScreenTime.TIME_30_SECONDS));
            menu.add(new MenuDialogItem.MenuBean(getString(R.string.settings_system_item_saver_time_1_minute), SaverScreenTime.TIME_1_MINUTE));
            menu.add(new MenuDialogItem.MenuBean(getString(R.string.settings_system_item_saver_time_2_minutes), SaverScreenTime.TIME_2_MINUTES));
            menu.add(new MenuDialogItem.MenuBean(getString(R.string.settings_system_item_saver_time_5_minutes), SaverScreenTime.TIME_5_MINUTES));
            menu.add(new MenuDialogItem.MenuBean(getString(R.string.settings_system_item_saver_time_10_minutes), SaverScreenTime.TIME_10_MINUTES));
            items.add(new MenuDialogItem.Builder(mActivity)
                    .setTitle(R.string.settings_system_item_saver_time)
                    .setParamKey(ParamsConst.PARAMS_KEY_SAVER_SCREEN_TIME)
                    .setParamBean(menu)
                    .setOnChangeListener(index -> refreshItems())
                    .create());
        }
        //Auto Settle
        items.add(new TextItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_auto_settle)
                .setOnClickListener(v -> mSupportDelegate.switchContent(new SystemAutoSettleFragment()))
                .create());
        //Void card
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_void_card)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_VOID_CARD)
                .create());
        //Void PIN
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_void_pin)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_VOID_PIN)
                .create());

        //Second Screen overlay
        if (DisplayUtils.getDisplay2(mActivity) != null) {
            items.add(new SwitchItem.Builder(mActivity)
                    .setTitle(R.string.settings_system_item_second_screen_top)
                    .setParamKey(ParamsConst.PARAMS_KEY_OTHER_SECOND_SCREEN_TOP)
                    .create());
        }
        //Third result
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_third_bill_show)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_THIRD_BILL_SHOW)
                .create());
        //Enable tip
        items.add(new SwitchItem.Builder(mActivity)
                .setTitle(R.string.settings_system_item_input_tip)
                .setParamKey(ParamsConst.PARAMS_KEY_OTHER_TIP_INPUT)
                .create());
        return items;
    }

    @Override
    public FragmentCallback<Void> getCallback() {
        return null;
    }

    private String getCurrencyString(){
        String currencyCode = ParamsUtils.getString(ParamsConst.PARAMS_KEY_BASE_CURRENCY_CODE);
        CurrencyUtils.CurrencyBean currencyBean = CurrencyUtils.getCurrency(currencyCode);
        return currencyBean == null ? "" : currencyBean.getNumericCode() + " " + currencyBean.getAlphaCode() + " " + currencyBean.getSymbol();
    }
}
