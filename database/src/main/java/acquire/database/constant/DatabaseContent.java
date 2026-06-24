package acquire.database.constant;

public class DatabaseContent {

    //transaction types
    public final static String TRANS_SALE = "Sale";
    public final static String TRANS_VOID_SALE = "VoidSale";
    public final static String TRANS_TEST_TRX = "TestTxn";
    public final static String TRANS_PRE_AUTH_MENU = "PreAuthMenu";
    public final static String TRANS_PRE_AUTH = "PreAuth";
    public final static String TRANS_VOID_PRE_AUTH = "VoidPreAuth";
    public final static String TRANS_AUTH_COMPLETE = "AuthComplete";
    public final static String TRANS_VOID_AUTH_COMPLETE = "VoidAuthComplete";

    /**
     * Void Tip SALE
     */
    public final static String TRANS_VOID_TIP_ADJUST = "VoidTipSale";
    public final static String TRANS_DISPLAY_VOID_TIP_ADJUST = "Void Tip";
    /**
     * TIP SALE (Adjust)
     */
    public final static String TRANS_TIP_SALE = "TipSale";
    public final static String TRANS_DISPLAY_TIP_SALE = "TIP ADJUST";


    public final static String TRANS_SETTLE = "Settle";


    //For Report UI and Report Print
    public static final String REPORT_DISPLAY_TEXT_SALE = "SALE";
    public static final String REPORT_DISPLAY_TEXT_VOID = "VOID";
    public static final String REPORT_DISPLAY_TEXT_PRE_AUTH = "PRE-AUTH";
    public static final String REPORT_DISPLAY_TEXT_VOID_PRE_AUTH = "VOID PRE-AUTH";
    public static final String REPORT_DISPLAY_TEXT_PRE_AUTH_COMPLETE = "SALE COMPLETE";
    public static final String REPORT_DISPLAY_TEXT_VOID_PRE_AUTH_COMPLETE = "VOID SALE COMPLETE";
    //public static final String REPORT_DISPLAY_TEXT_SALE_TIP = "TIP";
    public static final String REPORT_DISPLAY_TEXT_VOID_TIP_ADJUST = "VOID TIP";
    public static final String REPORT_DISPLAY_TEXT_INSTALLMENT = "PAYFLEX";
    public static final String REPORT_DISPLAY_TEXT_VOID_INSTALLMENT = "VOID PAYFLEX";

    public final static String TRANS_INSTALLMENT_MENU = "InstallmentMenu";
    public final static String TRANS_INSTALLMENT = "Installment";
    public final static String TRANS_VOID_INSTALLMENT = "VoidInstallment";

}
