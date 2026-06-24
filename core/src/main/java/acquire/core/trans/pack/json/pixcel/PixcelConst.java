package acquire.core.trans.pack.json.pixcel;

import android.util.SparseArray;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.TlvUtils;
import acquire.base.utils.emv.EmvTag;
import acquire.sdk.emv.constant.EntryMode;

public class PixcelConst {
    public static final String HCE_BASE_URL = "https://sandbox.api.piperks.com/deviceReceipts?" ;

    public static final String STATE_INCOMPLETE = "state=incomplete";
    public static final String STATE_COMPLETE = "state=complete";

    public static final String API_KEY = "x-api-key";
    public static final String NPT_API_KEY = "sk_SBDARI_96A_5YTzRQp8_VWXsN3v2K0JE_1gBLoZfM7_T9udY6Xw" ;

    public static void getAID(PixcelReceiptReq request, int cardEntry, String emvData) {
        if (request == null || emvData == null) return;
        if (cardEntry == EntryMode.INSERT || cardEntry == EntryMode.TAP) {
            SparseArray<byte[]> emvTlvs = TlvUtils.getTlvList(BytesUtils.hexToBytes(emvData));

            if (emvTlvs != null) {
                for (int i = 0; i < emvTlvs.size(); i++) {
                    int tag = emvTlvs.keyAt(i);
                    byte[] value = emvTlvs.get(tag);
                    switch (tag) {
                        case EmvTag.TAG_9F12_IC_APPNAME:
                        case EmvTag.TAG_50_IC_APPLABEL:
                            request.applicationLable = new String(value);
                            break;
                        case EmvTag.TAG_4F_IC_AID:
                            request.applicationIdentifier = BytesUtils.bcdToString(value);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public static String getDescription(int cardEntry) {
        switch (cardEntry) {
            case EntryMode.MAG:
                return "MAGSTRIPE";
            case EntryMode.INSERT:
                return "EMV CHIP";
            case EntryMode.TAP:
                return "CONTACTLESS";
            case EntryMode.MANUAL:
                return "MANUAL";
            case EntryMode.SCAN:
                return "SCANNED";
            case EntryMode.SHOW_QR:
                return "QRCODE";
            case EntryMode.NONE:
            default:
                return "UNKNOWN";
        }
    }

}
