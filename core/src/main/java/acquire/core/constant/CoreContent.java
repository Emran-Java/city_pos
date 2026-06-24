package acquire.core.constant;

import java.util.ArrayList;
import java.util.HashMap;

import acquire.core.model.CardBinModel;
import acquire.core.model.FeatureSubMenuModel;

public class CoreContent {

    /**
     * After open app, then the report menu set in this array list.
     * */
    public static ArrayList<FeatureSubMenuModel> PRINT_RECEIPT_MENU = new ArrayList<>();
    public static ArrayList<FeatureSubMenuModel> PAY_FLEX_MENU = new ArrayList<>();
    public static ArrayList<FeatureSubMenuModel> PRE_AUTH_MENU = new ArrayList<>();
    public static ArrayList<FeatureSubMenuModel> REPORT_MENU = new ArrayList<>();
    public static ArrayList<CardBinModel> REPORT_CARD_ONUS_LIST = new ArrayList<>();

    //public static HashMap<String, CardBinModel> REPORT_CARD_ONUS_MAP = new HashMap<>();

    //For Report UI and Report Print
    public static final String REPORT_DISPLAY_TEXT_SALE = "SALE";
    public static final String REPORT_DISPLAY_TEXT_VOID = "VOID";
    public static final String REPORT_DISPLAY_TEXT_PRE_AUTH = "PRE-AUTH";
    public static final String REPORT_DISPLAY_TEXT_VOID_PRE_AUTH = "VOID PRE-AUTH";
    public static final String REPORT_DISPLAY_TEXT_PRE_AUTH_COMPLETE = "SALE COMPLETE";
    public static final String REPORT_DISPLAY_TEXT_VOID_PRE_AUTH_COMPLETE = "VOID SALE COMPLETE";
    public static final String REPORT_DISPLAY_TEXT_SALE_TIP = "TIP";
    public static final String REPORT_DISPLAY_TEXT_VOID_TIP_ADJUST = "VOID TIP";
    public static final String REPORT_DISPLAY_ONUS = "ONUS";
    public static final String REPORT_DISPLAY_OFFUS = "OFFUS";
    public static final String REPORT_DISPLAY_UNKNOWN = "UNKNOWN";

}
