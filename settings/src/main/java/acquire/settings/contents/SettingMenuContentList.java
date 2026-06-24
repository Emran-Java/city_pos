package acquire.settings.contents;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;

import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.settings.fragment.brac_setting.BracSecondSettingMenuFragment;
import acquire.settings.fragment.brac_setting.BracSettingThirdSettingMenuFragment;
import acquire.settings.models.BracSettingMenuItemModel;
import acquire.settings.models.PrefKeyValType;

public class SettingMenuContentList {

    private static Fragment getBracSecondSettingFragment(ArrayList<BracSettingMenuItemModel> menuList, String title) {
        Fragment manageFragment = new BracSecondSettingMenuFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(BracSecondSettingMenuFragment.BUNDLE_ARG_KEY_MENU_LIST, menuList);
        bundle.putString(BracSecondSettingMenuFragment.BUNDLE_ARG_KEY_TITLE_BAR_TITLE, title);
        manageFragment.setArguments(bundle);
        return manageFragment;
    }

    /** first layer*/
    public static Fragment getManageFragment(String title) {
        ArrayList<BracSettingMenuItemModel> menuList = new ArrayList<>();
        menuList.add(new BracSettingMenuItemModel("Merchant Info"));
        menuList.add(new BracSettingMenuItemModel("ON-OFF"));
        menuList.add(new BracSettingMenuItemModel("Trans Params"));
        menuList.add(new BracSettingMenuItemModel("Key Management"));
        menuList.add(new BracSettingMenuItemModel("Clear"));
        menuList.add(new BracSettingMenuItemModel("System"));
        menuList.add(new BracSettingMenuItemModel("Print"));
        menuList.add(new BracSettingMenuItemModel("Other"));

        return getBracSecondSettingFragment(menuList, title);
    }

    public static Fragment getCommunicationFragment(String title) {
        ArrayList<BracSettingMenuItemModel> menuList = new ArrayList<>();

        menuList.add(new BracSettingMenuItemModel("TPDU"));
        menuList.add(new BracSettingMenuItemModel("NII"));
        menuList.add(new BracSettingMenuItemModel("Communication Timeout"));
        menuList.add(new BracSettingMenuItemModel("COMMTYPE"));
        menuList.add(new BracSettingMenuItemModel("WIFI"));
        menuList.add(new BracSettingMenuItemModel("SIM SLOT"));
        menuList.add(new BracSettingMenuItemModel("Operator Config"));
        menuList.add(new BracSettingMenuItemModel("SIM Selection"));

        return getBracSecondSettingFragment(menuList, title);
    }
    /** ---------------------------------------------------------------------------- */


    /** second layer*/
    private static Fragment getBracSettingSubListFragment(ArrayList<BracSettingMenuItemModel> list, String title){
        Fragment manageFragment = new BracSettingThirdSettingMenuFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(BracSettingThirdSettingMenuFragment.BUNDLE_ARG_KEY_MENU_LIST, list);
        bundle.putString(BracSettingThirdSettingMenuFragment.BUNDLE_ARG_KEY_TITLE_BAR_TITLE, title);

        manageFragment.setArguments(bundle);

        return manageFragment;
    }

    public static Fragment getMerchantInfoFragment(String title) {
        ArrayList<BracSettingMenuItemModel> list = new ArrayList<>();
        list.add(new BracSettingMenuItemModel("Merchant Name",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_NAME_NEW, "")));
        list.add(new BracSettingMenuItemModel("Merchant ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ID, ""),false));
        list.add(new BracSettingMenuItemModel("Terminal ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_POS_ID, ""),false));
        list.add(new BracSettingMenuItemModel("PayFlex Merchant ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_EMI_MERCHANT_ID, ""),false));
        list.add(new BracSettingMenuItemModel("PayFlex Terminal ID",ParamsUtils.getString(ParamsConst.PARAMS_KEY_EMI_POS_ID, ""),false));
        list.add(new BracSettingMenuItemModel("Merchant Address",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR1, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 2",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR2, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 3",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR3, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 4",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR4, "")));
        list.add(new BracSettingMenuItemModel("Merchant Address 5",ParamsUtils.getString(ParamsConst.PARAMS_KEY_MERCHANT_ADDR5, "")));
        list.add(new BracSettingMenuItemModel("Footer1",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER1, "")));
        list.add(new BracSettingMenuItemModel("Footer2",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER2, "")));
        list.add(new BracSettingMenuItemModel("Footer3",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER3, "")));
        list.add(new BracSettingMenuItemModel("Footer4",ParamsUtils.getString(ParamsConst.PARAMS_KEY_FOOTER4, "")));
        list.add(new BracSettingMenuItemModel("Merchant Copy", ParamsUtils.getString(ParamsConst.PARAMS_KEY_COPY1, "")));
        list.add(new BracSettingMenuItemModel("Customer Copy",ParamsUtils.getString(ParamsConst.PARAMS_KEY_COPY2, "")));
        list.add(new BracSettingMenuItemModel("Vendor Copy",ParamsUtils.getString(ParamsConst.PARAMS_KEY_COPY3, "")));

        return getBracSettingSubListFragment(list,title);

    }
    public static Fragment getOnOffFragment(String title) {
        ArrayList<BracSettingMenuItemModel> list = new ArrayList<>();
        list.add(new BracSettingMenuItemModel("Trans Enable"));
        list.add(new BracSettingMenuItemModel("Payment Type"));
        list.add(new BracSettingMenuItemModel("Tip Enable"));
        list.add(new BracSettingMenuItemModel("Fallback Enable"));
        list.add(new BracSettingMenuItemModel("CVV2"));
        list.add(new BracSettingMenuItemModel("Tip Adjust Enable"));
        list.add(new BracSettingMenuItemModel("Last Four Digit Check"));

        return getBracSettingSubListFragment(list,title);
    }
    public static Fragment getTransParamFragment(String title) {
        ArrayList<BracSettingMenuItemModel> menuList = new ArrayList<>();
        menuList.add(new BracSettingMenuItemModel("Trans No"));
        menuList.add(new BracSettingMenuItemModel("Set Print"));
        menuList.add(new BracSettingMenuItemModel("PIN"));
        menuList.add(new BracSettingMenuItemModel("Percentage"));
        menuList.add(new BracSettingMenuItemModel("NFC"));
        menuList.add(new BracSettingMenuItemModel("Amount Range"));
        menuList.add(new BracSettingMenuItemModel("Manage PAYFLEX"));
        menuList.add(new BracSettingMenuItemModel("Currency Type"));
        menuList.add(new BracSettingMenuItemModel("Manage Reward"));
        menuList.add(new BracSettingMenuItemModel("Manage Sale"));
        menuList.add(new BracSettingMenuItemModel("Manage PreAuth"));
        menuList.add(new BracSettingMenuItemModel("Other"));
        return getBracSettingSubListFragment(menuList,title);
    }
    /** ---------------------------------------------------------------------------- */



    /** third layer*/
    public static Fragment getTransEnableFragment(String title) {
        ArrayList<BracSettingMenuItemModel> menuList = new ArrayList<>();
        menuList.add(new BracSettingMenuItemModel("Sale", ParamsConst.PARAMS_KEY_SALE, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Void", ParamsConst.PARAMS_KEY_VOID, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Balance Inquiry", ParamsConst.PARAMS_KEY_BALANCE, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Pre-Auth", ParamsConst.PARAMS_KEY_PREAUTH, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Sale-Complete", ParamsConst.PARAMS_KEY_SALE_COMP, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Offline Sale", ParamsConst.PARAMS_KEY_OFFLINE, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Reward Sale", ParamsConst.PARAMS_KEY_REWARD, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Cash Withdrawal", ParamsConst.PARAMS_KEY_CASH_WITHDRAWAL, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Cash Back", ParamsConst.PARAMS_KEY_CASH_BACK, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Cash Advance", ParamsConst.PARAMS_KEY_CASH_ADVANCE, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("ADJUSTED", ParamsConst.PARAMS_KEY_ADJUST, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("PAYFLEX +", ParamsConst.PARAMS_KEY_CASH_PAYFLEX, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Show Txn", ParamsConst.PARAMS_KEY_SHOW_TXN, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Other Txn", ParamsConst.PARAMS_KEY_OTHER_TXN, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Service Menu", ParamsConst.PARAMS_KEY_SERVICE_MENU, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("Help Center", ParamsConst.PARAMS_KEY_HELP_CENTER, PrefKeyValType.STRING, true, true));
        menuList.add(new BracSettingMenuItemModel("MSS Portal", ParamsConst.PARAMS_KEY_SHOW_MSS_PORAL, PrefKeyValType.STRING, true, true));
        return getBracSettingSubListFragment(menuList,title);
    }
    /** ---------------------------------------------------------------------------- */



}
