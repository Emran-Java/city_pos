package acquire.sdk.emv.constant;

import com.newland.sdk.emvl3.api.common.EmvL3Const;

import acquire.base.BaseApplication;
import acquire.sdk.R;

/**
 * Card entry mode.
 *
 * @author Janson
 * @date 2021/7/8 16:09
 */
public class EntryMode {
    public static final int NONE = 0x00;
    public static final int MAG = EmvL3Const.CardInterface.MAGSTRIPE;
    public static final int INSERT = EmvL3Const.CardInterface.CONTACT;
    public static final int TAP = EmvL3Const.CardInterface.CONTACTLESS;
    public static final int MANUAL = EmvL3Const.CardInterface.MANUAL;
    public static final int SCAN = 0x10;
    public static final int SHOW_QR = 0x20;

    public static String getDescription(int cardEntry){
        switch (cardEntry) {
            case EntryMode.MAG:
                return BaseApplication.getAppString(R.string.sdk_helper_entry_description_mag);
            case EntryMode.INSERT:
                return BaseApplication.getAppString(R.string.sdk_helper_entry_description_insert);
            case EntryMode.TAP:
                return BaseApplication.getAppString(R.string.sdk_helper_entry_description_tap);
            case EntryMode.MANUAL:
                return BaseApplication.getAppString(R.string.sdk_helper_entry_description_manual);
            case EntryMode.SCAN:
                return BaseApplication.getAppString(R.string.sdk_helper_entry_description_scan);
            case EntryMode.SHOW_QR:
                return BaseApplication.getAppString(R.string.sdk_helper_entry_description_qr_code);
            case EntryMode.NONE:
            default:
                return BaseApplication.getAppString(R.string.sdk_helper_entry_description_unknown);
        }
    }
} 
