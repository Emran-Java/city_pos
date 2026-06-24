package acquire.core.constant;


/**
 * Transaction type
 *
 * @author Janson
 * @date 2020/9/1 14:22
 */
public class TransType {
    /**
     * Void Tip SALE
     */
    public final static String TRANS_VOID_TIP_ADJUST = "VoidTipSale";
    /**
     * TIP SALE (Adjust)
     */
    public final static String TRANS_TIP_SALE = "TipSale";
    /**
     * Sale
     */
    public final static String TRANS_SALE = "Sale";
    /**
     * Void Sale
     */
    public final static String TRANS_VOID_SALE = "VoidSale";
    /**
     * Refund
     */
    public final static String TRANS_REFUND = "Refund";
    /**
     * Query balance
     */
    public final static String TRANS_BALANCE = "Balance";
    public final static String TRANS_TEST_TRX = "TestTxn";
    public final static String TRANS_LOG_ON = "LogOn";
    public final static String TRANS_INSTALLMENT_LOG_ON = "InstallmentLogOn";

    /**
     * Pre-Auth
     */
    public final static String TRANS_PRE_AUTH_MENU = "PreAuthMenu";
    public final static String TRANS_PRE_AUTH = "PreAuth";
    /**
     * Void Pre-Auth
     */
    public final static String TRANS_VOID_PRE_AUTH = "VoidPreAuth";
    /**
     * Auth Complete
     */
    public final static String TRANS_AUTH_COMPLETE = "AuthComplete";
    /**
     * Void Auth Complete
     */
    public final static String TRANS_VOID_AUTH_COMPLETE = "VoidAuthComplete";
    /**
     * Reversal
     */
    public final static String TRANS_REVERSAL = "Reversal";
    /**
     * Installment Sale
     */
    public final static String TRANS_INSTALLMENT_MENU = "InstallmentMenu";

    public final static String TRANS_INSTALLMENT = "Installment";

    /**
     * Void Installment Sale
     */
    public final static String TRANS_VOID_INSTALLMENT = "VoidInstallment";

    /**
     * POS scan QR code for payment.
     */
    public final static String TRANS_SCAN_PAY = "ScanPay";

    /**
     * POS shows the payment QR code.
     */
    public final static String TRANS_QR_CODE = "QrCode";

    /**
     * QR refund.
     */
    public final static String TRANS_QR_REFUND = "QrRefund";
    /**
     * Settle
     */
    public final static String TRANS_SETTLE = "Settle";
    /**
     * Reports Print
     */
    public final static String TRANS_REPORTS_PRINT = "PrintDetail";

    /**
     * REPRINT Last Receipt
     */
    public final static String TRANS_REPRINT_LAST_RECEIPT = "ReprintLastReceipt";
    /**
     * REPRINT Receipt
     */
    public final static String TRANS_REPRINT_RECEIPT_MENU = "ReprintReceiptMenu";
    public final static String TRANS_REPRINT_RECEIPT = "ReprintReceipt";
    /**
     * REPRINT settle data
     */
    public final static String TRANS_REPRINT_SETTLE = "ReprintSettle";
    /**
     * Print detail
     */
    public final static String TRANS_PRINT_DETAIL = "PrintDetail";
    /**
     * Settings
     */
    public final static String TRANS_SETTINGS = "Settings";

    /**
     * Version information
     */
    public final static String TRANS_ABOUT = "About"; //App Version
    public final static String TRANS_HELP_CENTER = "HelpCenter"; //Help Center
    /**
     * Login
     */
    public final static String TRANS_LOGIN = "Login";
    /**
     * Cash Back
     */
    public final static String TRANS_CASH_BACK = "CashBack";
    /**
     * Cash Advance
     */
    public final static String TRANS_CASH_ADVANCE = "CashAdvance";

    /**
     * HCE Sale
     */
    public final static String TRANS_HCE_SALE = "HCESale";

}
