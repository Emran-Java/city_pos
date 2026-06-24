package acquire.core.constant;

import acquire.sdk.ConnectMode;

/**
 * The keys of parameters for local file storage
 *
 * @author Janson
 * @date 2018/3/26
 */
public class ParamsConst_bkp {

    /**
     * App first start flag.
     */
    public final static String PARAMS_KEY_FIRST_RUN = "FIRST_RUN";

    //===========================[CONFIG VERSION]===========================
    
    public final static String PARAMS_KEY_EMV_AID_CAPK = "EMV_AID_CAPK";

    //===========================[BASE]===========================
    /**
     * merchant name
     */
    public final static String PARAMS_KEY_BASE_MERCHANT_NAME = "BASE_MERCHANT_NAME";
    /**
     * Trace No.
     */
    public final static String PARAMS_KEY_BASE_TRACE_NO = "BASE_TRACE_NO";
    /**
     * Max amount of refund in cents (long type)
     */
    public final static String PARAMS_KEY_BASE_MAX_REFUND_AMOUNT = "BASE_MAX_REFUND_AMOUNT";
    /**
     * Currency code. Such as, 840($),156(¥)
     */
    public final static String PARAMS_KEY_BASE_CURRENCY_CODE = "BASE_CURRENCY_CODE";
    /**
     * Transaction records max count
     */
    public final static String PARAMS_KEY_BASE_MAX_TRANS_COUNT = "BASE_MAX_TRANS_COUNT";

    //----------------------------[PINPAD]----------------------------
    /**
     * Pinpad timeout in seconds
     */
    public final static String PARAMS_KEY_PINPAD_TIMEOUT = "PINPAD_TIMEOUT";

    /**
     * input PIN&Read Card by external PIN pad
     */
    public final static String PARAMS_KEY_PINPAD_EXTERNAL = "PINPAD_EXTERNAL";
    /**
     * external PIN pad connection mode.
     *
     * @see ConnectMode
     */
    public final static String PARAMS_KEY_PINPAD_EXTERNAL_CONNECT_MODE = "PINPAD_EXTERNAL_CONNECT_MODE";
    /**
     * use Accessibility  PIN pad
     */
    public final static String PARAMS_KEY_PINPAD_ACCESSIBILITY = "PINPAD_ACCESSIBILITY";
    /**
     * Accessibility PIN pad timeout
     */
    public final static String PARAMS_KEY_PINPAD_ACCESSIBILITY_TIMEOUT = "PINPAD_ACCESSIBILITY_TIMEOUT";

    //===========================[Card Reader]===========================
    public final static String PARAMS_KEY_CARD_READER_TIMEOUT = "CARD_READER_TIMEOUT";
    //===========================[TRANS SUPPORT]===========================
    /**
     * Sale
     */
    public final static String PARAMS_KEY_TRANS_SALE = "TRANS_SALE";
    /**
     * Void sale
     */
    public final static String PARAMS_KEY_TRANS_VOID = "TRANS_VOID";
    /**
     * Refund
     */
    public final static String PARAMS_KEY_TRANS_REFUND = "TRANS_REFUND";
    /**
     * Balance
     */
    public final static String PARAMS_KEY_TRANS_BALANCE = "TRANS_BALANCE";
    /**
     * Pre-Auth
     */
    public final static String PARAMS_KEY_TRANS_PREAUTH = "TRANS_PREAUTH";
    /**
     * Mobile Pay
     */
    public final static String PARAMS_KEY_TRANS_MOBILE_PAY = "TRANS_MOBILE_PAY";
    /**
     * Installment
     */
    public final static String PARAMS_KEY_TRANS_INSTALLMENT = "TRANS_INSTALLMENT";
    /**
     * Cash Advance
     */
    public final static String PARAMS_KEY_TRANS_CASH_ADVANCE = "TRANS_CASH_ADVANCE";
    /**
     * Cash Back
     */
    public final static String PARAMS_KEY_TRANS_CASH_BACK = "TRANS_CASH_BACK";


    //===========================[ELECSIGN]===========================
    /**
     * use electric signature
     */
    public final static String PARAMS_KEY_ELECSIGN_IS_SUPPORT = "ELECSIGN_IS_SUPPORT";

    //===========================[RECEIPT SUPPORT]===========================

    /**
     * Automatic_print_mode
     */
    public final static String PARAMS_KEY_RECEIPT_AUTO_PRINT = "RECEIPT_AUTO_PRINT";
    /**
     * Merchant Paper
     */
    public final static String PARAMS_KEY_RECEIPT_MERCHANT_PAPER = "RECEIPT_MERCHANT_PAPER";
    /**
     * Customer Paper
     */
    public final static String PARAMS_KEY_RECEIPT_CUSTOMER_PAPER = "RECEIPT_CUSTOMER_PAPER";
    /**
     * Fly Receipt
     */
    public final static String PARAMS_KEY_RECEIPT_FLY_RECEIPT = "RECEIPT_FLY_RECEIPT";
    /**
     * Customer EReceipt NPIDemo
     */
    public final static String PARAMS_KEY_RECEIPT_E_NPI_DEMO = "RECEIPT_E_NPI_DEMO";
    /**
     * Customer EReceipt PIXCEL
     */
    public final static String PARAMS_KEY_RECEIPT_E_PIXCEL = "RECEIPT_E_PIXCEL";


    //===========================[PRINT]===========================
    /**
     * Printing remarks
     */
    public final static String PARAMS_KEY_PRINT_REMARKS = "PRINT_REMARKS";
    /**
     * use external printer
     */
    public final static String PARAMS_KEY_PRINT_EXTERNAL = "PRINT_EXTERNAL";
    /**
     * external printer connection mode.
     *
     * @see ConnectMode
     */
    public final static String PARAMS_KEY_PRINT_EXTERNAL_CONNECT_MODE = "PRINT_EXTERNAL_CONNECT_MODE";
    /**
     * External scanner serial baud rate. e.g. 115200, 9600
     */
    public final static String PARAMS_KEY_PRINT_EXTERNAL_SERIAL_BAUDRATE = "PRINT_EXTERNAL_SERIAL_BAUDRATE";
    //===========================[SCAN]===========================
    /**
     * first priority scanner.
     *
     * @see Scanner
     */
    public final static String PARAMS_KEY_SCAN_PRIORITY_SCANNER = "SCAN_PRIORITY_SCANNER";
    /**
     * The external scanner mode. {@link ConnectMode}
     */
    public final static String PARAMS_KEY_SCAN_EXTERN_CONNECT_MODE = "SCAN_EXTERN_CONNECT_MODE";
    /**
     * Waiting time for USB receipting completion.
     */
    public final static String PARAMS_KEY_SCAN_EXTERN_USB_WAIT_TIME = "SCAN_EXTERN_USB_WAIT_TIME";
    /**
     * External scanner serial baud rate. e.g. 115200, 9600
     */
    public final static String PARAMS_KEY_SCAN_EXTERN_SERIAL_BAUDRATE = "SCAN_EXTERN_SERIAL_BAUDRATE";

    //===========================[PASSWORD]===========================
    /**
     * Transaction password
     */
    public final static String PARAMS_KEY_PASSWORD_ADMIN = "PASSWORD_ADMIN";
    public final static String PARAMS_KEY_PASSWORD_ADMIN_LENGTH = "PASSWORD_ADMIN_LENGTH";
    /**
     * Setting password
     */
    public final static String PARAMS_KEY_PASSWORD_SYSTEM_ADMIN = "PASSWORD_SYSTEM_ADMIN";
    public final static String PARAMS_KEY_PASSWORD_SYSTEM_ADMIN_LENGTH = "PASSWORD_SYSTEM_ADMIN_LENGTH";
    /**
     * Safe password
     */
    public final static String PARAMS_KEY_PASSWORD_SECURITY = "PASSWORD_SECURITY";
    public final static String PARAMS_KEY_PASSWORD_SECURITY_LENGTH = "PASSWORD_SECURITY_LENGTH";

    //===========================[TOMS]===========================
    /**
     * TOMS FLY Parameter
     */
    public final static String PARAMS_KEY_TOMS_FLY_PARAMETERS = "TOMS_FLY_PARAMETERS";
    /**
     * TOMS FLY Receipt
     */
    public final static String PARAMS_KEY_TOMS_FLY_RECEIPT = "TOMS_FLY_RECEIPT";

    //===========================[AUTO SETTLE]===========================
    /**
     * Auto settle enbale
     */
    public final static String PARAMS_KEY_AUTO_SETTLE_OPEN = "AUTO_SETTLE_OPEN";
    /**
     * Auto settle time, HHmm(hour+minute)
     */
    public final static String PARAMS_KEY_AUTO_SETTLE_TIME = "AUTO_SETTLE_TIME";

    //===========================[OTHER]===========================
    /**
     * Void transaction need card
     */
    public final static String PARAMS_KEY_OTHER_VOID_CARD = "OTHER_VOID_CARD";
    /**
     * Void transaction need PIN
     */
    public final static String PARAMS_KEY_OTHER_VOID_PIN = "OTHER_VOID_PIN";
    /**
     * Third application display transaction result
     */
    public final static String PARAMS_KEY_OTHER_THIRD_BILL_SHOW = "OTHER_THIRD_BILL_SHOW";
    /**
     * Input tip
     */
    public final static String PARAMS_KEY_OTHER_TIP_INPUT = "OTHER_TIP_INPUT";
    /**
     * The interface overlay others on the second screen.
     */
    public final static String PARAMS_KEY_OTHER_SECOND_SCREEN_TOP = "OTHER_SECOND_SCREEN_TOP";

    public final static String PARAMS_KEY_SAVER_SCREEN_TIME="SAVER_SCREEN_TIME";
}
