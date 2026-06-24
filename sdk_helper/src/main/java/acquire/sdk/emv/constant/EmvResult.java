package acquire.sdk.emv.constant;

import com.newland.sdk.emvl3.api.common.EmvL3Const;

/**
 * EMV result code. It is the copy of {@link EmvL3Const.TransResult}
 *
 * @author Janson
 * @date 2022/8/2 11:15
 */
public class EmvResult {
    /**
     * 1,Simple EMV Process
     */
    public static final int TXN_OK = EmvL3Const.TransResult.L3_TXN_OK;
    public static final int TXN_TERMINATE = EmvL3Const.TransResult.L3_TXN_TERMINATE;
    public static final int TXN_TRY_ANOTHER = EmvL3Const.TransResult.L3_TXN_TRY_ANOTHER;
    public static final int TXN_DECLINE = EmvL3Const.TransResult.L3_TXN_DECLINE;
    /**
     * 4,Process approved, No need for secondary GAC
     */
    public static final int TXN_APPROVED = EmvL3Const.TransResult.L3_TXN_APPROVED;
    /**
     * 5,Standard EMV Process
     */
    public static final int TXN_ONLINE = EmvL3Const.TransResult.L3_TXN_ONLINE;
} 
