package acquire.sdk.emv.bean;

import com.newland.sdk.emvl3.api.common.EmvL3Const;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.emv.IEmvProcessor;
import acquire.sdk.emv.constant.EntryMode;

/**
 * Fetch EMV processed data.It both includes cipher card and mag card information.
 *
 * @author Janson
 * @date 2021/6/15 14:21
 */
public class EmvFetchBean {
    private final IEmvProcessor emvProcessor;

    public EmvFetchBean(IEmvProcessor emvProcessor) {
        this.emvProcessor = emvProcessor;
    }

    public String getPan() {
        byte[] pan = emvProcessor.getData(EmvL3Const.L3_DATA.PAN);
        if (pan != null) {
            return new String(pan);
        }else{
            return null;
        }
    }

    public String getExpDate() {
        byte[] expData= emvProcessor.getData(EmvL3Const.L3_DATA.EXPIRE_DATE);
        if (expData != null){
            String strExpData = new String(expData);
            if (strExpData.length() >4){
                return strExpData.substring(0,4);
            }else{
                return strExpData;
            }
        }
        return null;
    }

    public String getTrack1() {
        byte[] track1 = emvProcessor.getData(EmvL3Const.L3_DATA.TRACK1);
        if (track1 != null) {
            return new String(track1);
        }else{
            return null;
        }
    }

    public String getTrack2() {
        byte[] track2 = emvProcessor.getData(EmvL3Const.L3_DATA.TRACK2);
        if (track2 != null) {
            return new String(track2).replace("=","D");
        }else{
            return null;
        }
    }

    public String getTrack3() {
        byte[] track3 = emvProcessor.getData(EmvL3Const.L3_DATA.TRACK3);
        if (track3 != null) {
            return new String(track3).replace("=","D");
        }else{
            return null;
        }
    }

    public String getServiceCode() {
        byte[] serviceCode = emvProcessor.getData(EmvL3Const.L3_DATA.SERVICE_CODE);
        if (serviceCode != null) {
            return new String(serviceCode);
        }else{
            return null;
        }
    }

    /**
     * return true if EMV requires no pin.
     */
    public boolean freeSign() {
        byte[] signature = emvProcessor.getData(EmvL3Const.L3_DATA.SIGNATURE);
        return signature != null && signature[0] == 0;
    }

    /**
     * get the entry mode of user using。
     * @see EntryMode
     */
    public int getUserEntryMode() {
        int n = emvProcessor.getData(EmvL3Const.L3_DATA.POS_ENTRY_MODE)[0] & 0xFF;
        switch (n) {
            case EmvL3Const.EntryMode.CLSS:
                LoggerUtils.d("[NSDK EmvProcessor]-- User Entry is contactless");
                return EntryMode.TAP;
            case EmvL3Const.EntryMode.ICC:
                LoggerUtils.d("[NSDK EmvProcessor]-- User Entry is contact");
                return EntryMode.INSERT;
            case EmvL3Const.EntryMode.MSR:
                LoggerUtils.d("[NSDK EmvProcessor]-- User Entry is mag");
                return EntryMode.MAG;
            case EmvL3Const.EntryMode.CT_FALLBACK:
                LoggerUtils.d("[NSDK EmvProcessor]-- User Entry is mag and fallback");
                return EntryMode.MAG;
            case EmvL3Const.EntryMode.MANUAL:
                LoggerUtils.d("[NSDK EmvProcessor]-- User Entry is manual");
                return EntryMode.MANUAL;
            default:
                LoggerUtils.d("[NSDK EmvProcessor]-- User Entry unknow: "+n);
                return 0;
        }
    }



}
