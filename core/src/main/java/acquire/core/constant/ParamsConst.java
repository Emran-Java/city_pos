package acquire.core.constant;

import acquire.base.constants.BasePrefKey;

/**
 * The keys of parameters for local file storage
 *
 * @author Janson
 * @date 2018/3/26
 */
public class ParamsConst {


    //===========================[EXTRANAL PATH]===========================

    public final static String PARAMS_KEY_MENU_FEATURE_MAIN = "MAIN_FEATURE_MENU";

    //===========================[EXTRANAL PATH]===========================
    public final static String PARAMS_KEY_LOGO_PATH = "LOGO_PATH";
    public final static String PARAMS_KEY_SLIDER_PATH = "SLIDER_PATH";
    public final static String PARAMS_KEY_SCREEN_SAVER_PATH= "SCREEN_SAVER_PATH";


    //===========================[SYSTEM & BASE]===========================
    /** App first start flag */
    public final static String PARAMS_KEY_FIRST_RUN = "FIRST_RUN";
    public final static String PARAMS_KEY_IS_PRESENT_INI_FILE = "IS_INI_FILE_PREASENT";
    public final static String PARAMS_KEY_SALE_TIP_FLAG = "SALETIPFLAG";
    public final static String PARAMS_KEY_TIP_ADJUST_ENABLE_FLAG = "SALEADJUSTFLAG";
    public final static String PARAMS_KEY_COUNTRY_CODE = "COUNTRYCODE";
    public final static String PARAMS_KEY_BASE_MERCHANT_NAME = "BASE_MERCHANT_NAME";
    public final static String PARAMS_KEY_MERCHANT_NAME_NEW = "MERCHANTNAME";
    public final static String PARAMS_KEY_MERCHANT_ID = "MERCHANTID";
    public final static String PARAMS_KEY_POS_ID = "POSID";
    public final static String PARAMS_KEY_BASE_TRACE_NO = "BASE_TRACE_NO";
    public final static String PARAMS_KEY_TRACE_NO = "TRACENO";
    public final static String PARAMS_KEY_BATCH_NO = "BATCHNO";
    public final static String PARAMS_KEY_MENU_TITLE = "MENUTITLE";
    public final static String PARAMS_KEY_EMV_AID_CAPK = "EMV_AID_CAPK";

    // Address Lines
    public final static String PARAMS_KEY_MERCHANT_ADDR1 = "MERCHANTADDR1";
    public final static String PARAMS_KEY_MERCHANT_ADDR2 = "MERCHANTADDR2";
    public final static String PARAMS_KEY_MERCHANT_ADDR3 = "MERCHANTADDR3";
    public final static String PARAMS_KEY_MERCHANT_ADDR4 = "MERCHANTADDR4";
    public final static String PARAMS_KEY_MERCHANT_ADDR5 = "MERCHANTADDR5";

    //===========================[EMI CONFIG]===========================
    public final static String PARAMS_KEY_EMI_MERCHANT_ID = BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIMERCHANTID; //"EMIMERCHANTID";
    public final static String PARAMS_KEY_EMI_POS_ID = "EMIPOSID";
    public final static String PARAMS_KEY_EMI_MERCHANT_NAME = "EMIMERCHANTNAME";
    public final static String PARAMS_KEY_EMI_BATCH_NO = "EMIBATCHNO";
    public final static String PARAMS_KEY_EMI_TENURE = "EMITENURE";
    public final static String PARAMS_KEY_EMI_BIN = "EMIBIN";
    public final static String PARAMS_KEY_EMI_PRODUCT_ID = "EMIPRODUCTID";
    public final static String PARAMS_KEY_TENURE_LIMIT = "TENURELIMIT";

    //===========================[LIMITS & AMOUNTS]===========================
    /** Max amount of refund in cents (long type) */
    public final static String PARAMS_KEY_BASE_MAX_REFUND_AMOUNT = "BASE_MAX_REFUND_AMOUNT";
    /** Currency code. Such as, 840($),156(¥) */
    public final static String PARAMS_KEY_BASE_CURRENCY_CODE = "BASE_CURRENCY_CODE";
    /** Transaction records max count */
    public final static String PARAMS_KEY_BASE_MAX_TRANS_COUNT = "BASE_MAX_TRANS_COUNT";
    public final static String PARAMS_KEY_MAX_TRANS_COUNT = "MAXTRANSCOUNT";
    public final static String PARAMS_KEY_UPPER_AMOUNT = "UPAMT";
    public final static String PARAMS_KEY_LOWER_AMOUNT = "LOWAMT";

    // CVM & Floor Limits
    public final static String PARAMS_KEY_PP_CVM_LIMIT = "PPCVMLIMIT";
    public final static String PARAMS_KEY_PP_FLOOR_LIMIT = "PPFLOORLIMIT";
    public final static String PARAMS_KEY_PW_CVM_LIMIT = "PWCVMLIMIT";
    public final static String PARAMS_KEY_PW_FLOOR_LIMIT = "PWFLOORLIMIT";

    //===========================[PASSWORDS]===========================
    /** Transaction password / Admin Password */
    public final static String PARAMS_KEY_PASSWORD_ADMIN = "PASSWORD_ADMIN";
    public final static String PARAMS_KEY_PASSWORD_ADMIN_LENGTH = "PASSWORD_ADMIN_LENGTH";
    public final static String PARAMS_KEY_ADMIN_PWD = "ADMINPWD";
    public final static String PARAMS_KEY_USER_PWD = "USERPWD";
    public final static String PARAMS_KEY_FUNC_PWD = "FUNCPWD";
    public final static String PARAMS_KEY_SETTLE_PWD = "SETTLEPWD";
    public final static String PARAMS_KEY_ON_OFF_PWD = "ONOFFPWD";

    /** Setting password */
    public final static String PARAMS_KEY_PASSWORD_SYSTEM_ADMIN = "PASSWORD_SYSTEM_ADMIN";
    public final static String PARAMS_KEY_PASSWORD_SYSTEM_ADMIN_LENGTH = "PASSWORD_SYSTEM_ADMIN_LENGTH";
    /** Safe password */
    public final static String PARAMS_KEY_PASSWORD_SECURITY = "PASSWORD_SECURITY";
    public final static String PARAMS_KEY_PASSWORD_SECURITY_LENGTH = "PASSWORD_SECURITY_LENGTH";

    //===========================[TRANSACTION SUPPORT]===========================
    public final static String PARAMS_KEY_TRANS_SALE = "TRANS_SALE";
    public final static String PARAMS_KEY_SALE = "SALE";
    public final static String PARAMS_KEY_TRANS_VOID = "TRANS_VOID";
    public final static String PARAMS_KEY_BALANCE = "BALANCE";
    public final static String PARAMS_KEY_VOID = "VOID";
    public final static String PARAMS_KEY_TRANS_REFUND = "TRANS_REFUND";
    public final static String PARAMS_KEY_TRANS_BALANCE = "TRANS_BALANCE";
    public final static String PARAMS_KEY_TRANS_PREAUTH = "TRANS_PREAUTH";
    public final static String PARAMS_KEY_PREAUTH = "PREAUTH";
    public final static String PARAMS_KEY_TRANS_MOBILE_PAY = "TRANS_MOBILE_PAY";
    public final static String PARAMS_KEY_TRANS_INSTALLMENT = "TRANS_INSTALLMENT";
    public final static String PARAMS_KEY_TRANS_CASH_ADVANCE = "TRANS_CASH_ADVANCE";
    public final static String PARAMS_KEY_TRANS_CASH_BACK = "TRANS_CASH_BACK";
    public final static String PARAMS_KEY_SALE_COMP = "SALECOMP";
    public final static String PARAMS_KEY_ADJUST = "ADJUST";
    public final static String PARAMS_KEY_OFFLINE = "OFFLINE";
    public final static String PARAMS_KEY_REWARD = "REWARD";
    public final static String PARAMS_KEY_CASH_WITHDRAWAL = "CASH";
    public final static String PARAMS_KEY_CASH_BACK = "CASHBACK";
    public final static String PARAMS_KEY_CASH_ADVANCE = "CASHADVANCE";
    public final static String PARAMS_KEY_CASH_PAYFLEX = "EMI";
    public final static String PARAMS_KEY_SHOW_TXN = "SHOWTXN";
    public final static String PARAMS_KEY_OTHER_TXN = "OTHER";
    public final static String PARAMS_KEY_SERVICE_MENU = "SERVICEMENU";
    public final static String PARAMS_KEY_HELP_CENTER = "HELPCENTER";
    public final static String PARAMS_KEY_SHOW_MSS_PORAL = "SHOWMSS";

    //===========================[PRINTER & RECEIPTS]===========================
    public final static String PARAMS_KEY_PRINT_REMARKS = "PRINT_REMARKS";
    public final static String PARAMS_KEY_FOOTER1 = "FOOTER1";
    public final static String PARAMS_KEY_FOOTER2 = "FOOTER2";
    public final static String PARAMS_KEY_FOOTER3 = "FOOTER3";
    public final static String PARAMS_KEY_FOOTER4 = "FOOTER4";
    public final static String PARAMS_KEY_COPY1 = "COPY1";//Merchant Copy
    public final static String PARAMS_KEY_COPY2 = "COPY2";//CUSTOMER COPY
    public final static String PARAMS_KEY_COPY3 = "COPY3";//VENDOR COPY
    public final static String PARAMS_KEY_PRINT_CUSTOMER_COPY = "PRINTCUSTOMERCOPY";

    public final static String PARAMS_KEY_RECEIPT_AUTO_PRINT = "RECEIPT_AUTO_PRINT";
    public final static String PARAMS_KEY_RECEIPT_MERCHANT_PAPER = "RECEIPT_MERCHANT_PAPER";
    public final static String PARAMS_KEY_RECEIPT_CUSTOMER_PAPER = "RECEIPT_CUSTOMER_PAPER";
    public final static String PARAMS_KEY_RECEIPT_FLY_RECEIPT = "RECEIPT_FLY_RECEIPT";
    public final static String PARAMS_KEY_RECEIPT_E_NPI_DEMO = "RECEIPT_E_NPI_DEMO";
    public final static String PARAMS_KEY_RECEIPT_E_PIXCEL = "RECEIPT_E_PIXCEL";

    /** External printer settings */
    public final static String PARAMS_KEY_PRINT_EXTERNAL = "PRINT_EXTERNAL";
    public final static String PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE = "PRINT_EXTERNAL_CONNECT_MODE";
    public final static String PARAMS_KEY_PRINT_EXTERNAL_SERIAL_BAUDRATE = "PRINT_EXTERNAL_SERIAL_BAUDRATE";

    //===========================[HARDWARE: PINPAD & SCANNER]===========================
    /** Pinpad timeout in seconds */
    public final static String PARAMS_KEY_PINPAD_TIMEOUT = "PINPAD_TIMEOUT";
    public final static String PARAMS_KEY_PINPAD_EXTERNAL = "PINPAD_EXTERNAL";
    public final static String PARAMS_KEY_PINPAD_EXTERNAL_CONNECT_MODE = "PINPAD_EXTERNAL_CONNECT_MODE";
    public final static String PARAMS_KEY_PINPAD_ACCESSIBILITY = "PINPAD_ACCESSIBILITY";
    public final static String PARAMS_KEY_PINPAD_ACCESSIBILITY_TIMEOUT = "PINPAD_ACCESSIBILITY_TIMEOUT";

    public final static String PARAMS_KEY_CARD_READER_TIMEOUT = "CARD_READER_TIMEOUT";

    /** Scanner settings */
    public final static String PARAMS_KEY_SCAN_PRIORITY_SCANNER = "SCAN_PRIORITY_SCANNER";
    public final static String PARAMS_KEY_SCAN_EXTERN_CONNECT_MODE = "SCAN_EXTERN_CONNECT_MODE";
    public final static String PARAMS_KEY_SCAN_EXTERN_USB_WAIT_TIME = "SCAN_EXTERN_USB_WAIT_TIME";
    public final static String PARAMS_KEY_SCAN_EXTERN_SERIAL_BAUDRATE = "SCAN_EXTERN_SERIAL_BAUDRATE";

    //===========================[UI & TIMEOUTS]===========================
    public final static String PARAMS_KEY_TIMEOUT = "TIMEOUT";
    public final static String PARAMS_KEY_IDLE_TIMEOUT = "IDLETIMEOUT";
    public final static String PARAMS_KEY_SAVER_SCREEN_TIME = "SAVER_SCREEN_TIME";
    public final static String PARAMS_KEY_IDLE_LINE1 = "IDLELINE1";
    public final static String PARAMS_KEY_IDLE_LINE2 = "IDLELINE2";
    public final static String PARAMS_KEY_IDLE_LINE3 = "IDLELINE3";
    public final static String PARAMS_KEY_ELECSIGN_IS_SUPPORT = "ELECSIGN_IS_SUPPORT";
    public final static String PARAMS_KEY_OTHER_SECOND_SCREEN_TOP = "OTHER_SECOND_SCREEN_TOP";

    //===========================[NETWORK & SERVER]===========================
    public final static String PARAMS_KEY_SERVER_IP1 = "SERVERIP1";
    public final static String PARAMS_KEY_PORT1 = "PORT1";
    public final static String PARAMS_KEY_SERVER_IP2 = "SERVERIP2";
    public final static String PARAMS_KEY_PORT2 = "PORT2";
    public final static String PARAMS_KEY_APN = "APN";
    public final static String PARAMS_KEY_COMM_TYPE = "COMMTYPE";
    public final static String PARAMS_KEY_TYPE = "TYPE";
    public final static String PARAMS_KEY_TPDU = "TPDU";
    public final static String PARAMS_KEY_NII = "NII";

    //===========================[CARD BIN & KEYS]===========================
    public final static String PARAMS_KEY_ONUS_BIN = "ONUSBIN";
    public final static String PARAMS_KEY_SALE_BIN = "SALEBIN";
    public final static String PARAMS_KEY_CARD_TYPE_BIN = "CARDTYPEBIN";
    public final static String PARAMS_KEY_PREAUTH_BIN = "PREAUTHBIN";
    public final static String PARAMS_KEY_TPK = "TPK";
    public final static String PARAMS_KEY_INDEX = "INDEX";
    public final static String PARAMS_KEY_MASTER_KEY_INDEX = "MASTERKEYINDEX";
    public final static String PARAMS_KEY_TPK_KEY_INDEX = "TPKKEYINDEX";
    public final static String PARAMS_KEY_ALGORITHM_TYPE = "ALGORITHMTYPE";

    //===========================[CONTACT & REMINDERS]===========================
    public final static String PARAMS_KEY_CONTACT_NAME = "CONTACT_NAME";
    public final static String PARAMS_KEY_CONTACT_PHONE = "CONTACT_PHONE";
    public final static String PARAMS_KEY_CONTACT_ALTERNATIVE_PHONE = "CONTACT_ALTERNATIVE";
    public final static String PARAMS_KEY_CONTACT_EMAIL = "CONTACT_EMAIL";
    public final static String PARAMS_KEY_SETTLE_REMINDER = "SETTLEREMINDER";
    public final static String PARAMS_KEY_FORCE_SETTLE_FLAG = "FORCESETTLEFLAG";
    public final static String PARAMS_KEY_AUTO_SETTLE_OPEN = "AUTO_SETTLE_OPEN";
    public final static String PARAMS_KEY_AUTO_SETTLE_TIME = "AUTO_SETTLE_TIME";

    //===========================[TOMS & OTHER]===========================
    public final static String PARAMS_KEY_TOMS_FLY_PARAMETERS = "TOMS_FLY_PARAMETERS";
    public final static String PARAMS_KEY_TOMS_FLY_RECEIPT = "TOMS_FLY_RECEIPT";
    public final static String PARAMS_KEY_OTHER_VOID_CARD = "OTHER_VOID_CARD";
    public final static String PARAMS_KEY_OTHER_VOID_PIN = "OTHER_VOID_PIN";
    public final static String PARAMS_KEY_OTHER_THIRD_BILL_SHOW = "OTHER_THIRD_BILL_SHOW";
    public final static String PARAMS_KEY_OTHER_TIP_INPUT = "OTHER_TIP_INPUT";

    //===========================[ON OFF]===========================
    public final static String PARAMS_KEY_ENABLE_QR_RECEIPT = "ENABLE_KEY_QR_RECEIPT";
    public final static String PARAMS_KEY_ENABLE_QR_RECEIPT_SLIP = "ENABLE_QR_RECEIPT_SLIP";

    //for internal use
    //===========================[TRACK & NOTE]========================
    public final static String PARAMS_KEY_IS_OPERATOR_AVAILABLE = "isOperatorAvailAble";


}
