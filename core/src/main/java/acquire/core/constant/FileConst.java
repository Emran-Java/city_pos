package acquire.core.constant;

import android.content.Context;

import acquire.base.utils.iso8583.ISO8583;
import acquire.core.tools.AnswerCodeProvider;
import acquire.core.tools.AppParamsImporter;
import acquire.core.tools.CardRangeProvider;
import acquire.core.tools.EmvConfigXmlParser;
import acquire.sdk.emv.IEmvParamLoader;

/**
 * Asset file name
 *
 * @author Janson
 * @date 2018/3/26
 */
public class FileConst {

    public final static String EXTRA_FILE_URL_CUSTOM_FOLDER = "/storage/emulated/0/custom_city";
    public final static String EXTRA_FILE_URL_INI = "/storage/emulated/0/custom_city/CITYPARAM.ini";

    public final static String MENU_FILE_PRINT_RECEIPT = "print-receipt.json";
    public final static String MENU_KEY_PRINT_RECEIPT = "printReceiptMenuPrefKey";


    public final static String MENU_FILE_FEATURE_MAIN_MENU = "feature-main-menu.json";
    public final static String MENU_KEY_FEATURE_MAIN_MENU = "payFlexMenuPrefKey";

    public final static String MENU_FILE_PAY_FLEX = "pay-flex.json";
    public final static String MENU_KEY_PAY_FLEX = "payFlexMenuPrefKey";
    public final static String MENU_FILE_PRE_AUTH = "pre-auth.json";
    public final static String MENU_PREF_KEY_PRE_AUTH = "preauthMenuPrefKey";
    public final static String MENU_FILE_RECORDS = "records.json";
    public final static String MENU_FILE_REPORT = "reports.json";
    public final static String MENU_PREF_KEY_REPORT = "reportMenuPrefKey";

    /**
     * 8583 configuration file as the argument {@link ISO8583#loadXmlFile(Context, String)}
     */
    public final static String CUPS8583 = "8583.xml";
    /**
     * Default parameters file used in {@link AppParamsImporter}
     */
    public final static String PARAMS = "params.properties";
    /**
     * Default merchants file used in {@link AppParamsImporter}
     */
    public final static String MERCHANTS = "merchants.xml";
    /**
     * Receipt LOGO.
     */
//    public final static String LOGO_IMG = "RECEIPT_LOGO.bmp";
    public final static String LOGO_IMG = "brac_print_logo.bmp";

    /**
     * Response code mapping file used in {@link AnswerCodeProvider}
     */
    public final static String ANSWER_CODE = "answercode.properties";
    /**
     * Card range table used in {@link CardRangeProvider}
     */
    public final static String CARD_RANGE = "cardrange.xml";


    /**
     * EMV configuration as the argument {@link EmvConfigXmlParser#parseXml(Context, String, IEmvParamLoader)}
     * Newland_L3_configuration_UL.xml：UL EMV configuration
     */
    public final static String EMV_CONFIG = "Newland_L3_configuration.xml";



}
