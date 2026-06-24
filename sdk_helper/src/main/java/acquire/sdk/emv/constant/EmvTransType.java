package acquire.sdk.emv.constant;


/**
 * EMV transaction type. It comes from the first 2 characters of ISO8583 field3.
 *
 * @author Janson
 * @date 2019/10/21 9:25
 */
public class EmvTransType {
    public static final int SALE = 0x00;

    public static final int VOID = 0x02;
    public static final int REFUND = 0x20;
    public static final int CASH_ADVANCE= 0x01;

    public static final int CASH_BACK = 0x09;
    public static final int PREAUTH = 0x30;
    public static final int BALANCE = 0x31;

}
