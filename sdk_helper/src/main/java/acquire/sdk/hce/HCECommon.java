package acquire.sdk.hce;

import com.newland.nsdk.cardemulation.utils.EmulateCardType;

import java.nio.charset.StandardCharsets;

public class HCECommon {


    public static class EMVErrorCode {
        public static final int ACTIVE_ERROR = -1362;
        public static final int PPSE_ERROR = -1321;

        public static final int CODE_INVALID_CARD = -1000;
    }

    public static class DemoMode{
        public static final int MODE_DEFAULT = 100;
        public static final int MODE_ERECEIPT_PIPERKS = 0;
        public static final int MODE_ERECEIPT_NPIDEMO = 1;
        public static final int MODE_HCE_AND_SALE = 2;
    }


//
//    public @StringRes int saleDescription() {
//        int saleMode = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_SALE_MODE);
//        Class<? extends AbstractTrans> trans ;
//        switch (saleMode){
//            case HCECommon.DemoMode.MODE_ERECEIPT_PIPERKS://e-receipt piperks
//                return R.string.app_menu_top_description_piperks;
//            case HCECommon.DemoMode.MODE_ERECEIPT_NPIDEMO://e-receipt npidemo
//                return R.string.app_menu_top_description_npidemo ;
//            case HCECommon.DemoMode.MODE_HCE_AND_SALE://hce + sale
//                return R.string.app_menu_top_description_hce_and_sale ;
//            default:
//                return R.string.app_menu_top_description ;
//        }
//    }

    public static byte[] spliceDataForConfiguration(EmulateCardType cardType, String url, byte uriCode) {
        byte[] tempData = new byte[200];
        byte[] result = null;
        int inLen = url.length();
        if (cardType == EmulateCardType.T4T) {
            int ndefLen = inLen + 5;
            tempData[0] = (byte) (ndefLen >> 8);
            tempData[1] = (byte) ndefLen;
            tempData[2] = (byte) 0xD1;
            tempData[3] = 0x01;
            tempData[4] = (byte) (inLen + 1);
            tempData[5] = (byte) 0x55;
            tempData[6] = uriCode;
            System.arraycopy(url.getBytes(StandardCharsets.UTF_8), 0, tempData, 7, inLen);
            result = new byte[inLen + 7];
            System.arraycopy(tempData, 0, result, 0, result.length);
        }
        return result;
    }




}
