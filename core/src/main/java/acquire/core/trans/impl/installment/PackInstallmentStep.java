package acquire.core.trans.impl.installment;

import android.text.TextUtils;

import java.util.Arrays;
import java.util.List;

import acquire.base.constants.BasePrefKey;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.model.PayFlexField63ResponseModel;
import acquire.core.tools.FieldDataParseUtility;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.PackField;
import acquire.core.trans.pack.iso.Caller;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The {@link BaseStep} that packs {@link Installment} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2019/2/12 15:49
 */
class PackInstallmentStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!doReversal()) {
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }

        String mEmiId = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIMERCHANTID, "");
        String mEmiPosTerminalId = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_EMIPOSID, "");

        pubBean.setTid(mEmiPosTerminalId);
        pubBean.setMid(mEmiId);

        initPubBean();

        pubBean.setMessageId("0200");
        pubBean.setProcessCode("000080");
        pubBean.setServerCode("00");
        pubBean.setField22(PackField.packField22(pubBean));
        //pack 8583
        iso8583.initPack();
        try {
            iso8583.setField(0, pubBean.getMessageId());

            /*if (pubBean.getEntryMode() != EntryMode.MAG) {
                iso8583.setField(2, pubBean.getCardNo());
            }*/
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(4, pubBean.getAmountField());
            iso8583.setField(11, pubBean.getTraceNo());
          /*  if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                iso8583.setField(14, pubBean.getExpDate());
            }*/
            iso8583.setField(22, pubBean.getField22());
            /*if (!TextUtils.isEmpty(pubBean.getCardSn())) {
                iso8583.setField(23, pubBean.getCardSn());
            }*/
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            }
            iso8583.setField(25, pubBean.getServerCode());
            if (!TextUtils.isEmpty(pubBean.getTrack2())) {
                iso8583.setField(35, pubBean.getTrack2());
            }
           /* if (!TextUtils.isEmpty(pubBean.getTrack3())) {
                iso8583.setField(36, pubBean.getTrack3());
            }*/


            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid());

            iso8583.setField(49, pubBean.getCurrencyCode());
            if (!TextUtils.isEmpty(pubBean.getPinBlock())) {
                iso8583.setField(52, pubBean.getPinBlock());
            }
            if (pubBean.getEntryMode() == EntryMode.INSERT || pubBean.getEntryMode() == EntryMode.TAP) {
                iso8583.setField(55, pubBean.getField55());
                LoggerUtils.d("newCall Brac PayFlex-SalepubBean.getField55():  " + pubBean.getField55());
            }
            //iso8583.setField(57, pubBean.getKsn());
//            iso8583.setField(61, pubBean.getInstalmentTerm()+"");
            iso8583.setField(62, pubBean.getBatchNo());
//            iso8583.setField(63, pubBean.getBatchNo());

            /*
            [Length]4[Tenure]4[Instalmet AMT]9[DownPayment AMT]9[Vendor ID]7[Product ID]4
                0033
                30303036 		   //form tenure list user select one
                303030303031303030 //user input amount
                303030303030303030 //9 digit always 000000000
                31313131313131 	   //form ini file
                39393939 		   //from ini file
            * */
//            String f63 = PackField.packField63(pubBean.getMid(),pubBean.getTid());
            int tenure = ParamsUtils.getInt(BasePrefKey.PREF_KEY_PAYPLEX_EMI_TENURE_SELECTED, 3); //ln: 4
//            int tenure = 3; //ln: 4
            long payFlaxInputAmount = pubBean.getAmount(); //ln: 9
            long downPayment = 0; //ln: 9, Default all time sent 0

            String vendorID = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_VENDOR, "");
            try {
                List<String> vendorIDs = Arrays.asList(vendorID.split(":"));
                vendorID = vendorIDs.get(1);
            } catch (Exception ex) {
                LoggerUtils.e("newCall Brac PayFlex-Sale check INI parm file (" + BasePrefKey.PREF_KEY_PAYPLEX_EMI_VENDOR + ") vendorID Exception:  " + ex.getMessage());
                return;
            }

//            String vendorID = "1111111"; //ln: 7, get from INI param
            String productID = ParamsUtils.getString(BasePrefKey.PREF_KEY_PAYPLEX_EMI_PRODUCTID, "");
//            String productID = "9999"; //ln: 4, get from INI param

            String tenureAsci = field63Padding(4, false, String.valueOf(tenure));
            String payFlaxInputAmountAsci = field63Padding(9, false, String.valueOf(payFlaxInputAmount));
            String downPaymentAsci = field63Padding(9, false, String.valueOf(downPayment));
            String vendorIDAsci = field63Padding(7, false, String.valueOf(vendorID));
            String productIDAsci = field63Padding(4, false, String.valueOf(productID));

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(tenureAsci);
            stringBuilder.append(payFlaxInputAmountAsci);
            stringBuilder.append(downPaymentAsci);
            stringBuilder.append(vendorIDAsci);
            stringBuilder.append(productIDAsci);

            String fieldValueLan = field63Padding(4, false, String.valueOf(stringBuilder.length()));

            LoggerUtils.d("PayFlax field 63: Main string: " + stringBuilder.toString());
            String convertedAsci = fieldDecToHax(stringBuilder.toString());
            LoggerUtils.d("PayFlax field 63: decToAscii: " + convertedAsci);

            //iso8583.setField(63, PackField.packField63(pubBean.getMid(),pubBean.getTid()));
            iso8583.setField(63, stringBuilder.toString());
            //--------------------

//            iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
        } catch (Exception e) {
            LoggerUtils.e("set 8583 field error!", e);
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error) + e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            callback.onResult(false);
            LoggerUtils.e("newCall Brac PayFlex-Sale Exception:  " + e.getMessage());
            return;
        }
        //send to the server, old code
       /* int result = new Caller.Builder(mActivity, pubBean, iso8583)
                .checkResp(true)
                .preSaveReversal(true)
                .packComm();*/

        //Brac-Dev

        int result = 0;
        result = new Caller.Builder(mActivity, pubBean, iso8583)
                .checkResp(true)
                .preSaveReversal(true)
                .packComm();

        if (result != CallerResult.OK) {
            LoggerUtils.e("newCall Brac PayFlex-Sale *NOT* response : ISO BIRMAP: " + iso8583.getIsoBitmap());
            LoggerUtils.e("newCall Brac PayFlex-Sale *NOT* response : ISO8385: " + iso8583.toString());
            callback.onResult(false);
        }

        //Date: 20260511, we don't update f55 after transaction
        //pubBean.setField55(iso8583.getField(55));

        //int result = 0;
//        iso8583.setField(63, "30303132313230303030303030303030313530303030303030303030303030303430303030303030303030303030303030303130303030303030303131313131313139393939");

        String response39 = iso8583.getField(39);
        if(response39==null || response39.equals("55")){
            LoggerUtils.e("newCall Brac PayFlex-Sale response 55: "+pubBean.getMessage());
            callback.onResult(false);
            return;
        }

        pubBean.setField63(iso8583.getField(63));
        String responseCode = iso8583.getField(63);
        if (responseCode == null || responseCode.isEmpty()) {
            LoggerUtils.e("newCall Brac PayFlex-Sale response ok but 63field data null or empty: " + responseCode);
            callback.onResult(false);
            return;
        }
        LoggerUtils.i("newCall Brac PayFlex-Sale response ok but 63field data size: " + responseCode.length() + ", Data: " + responseCode);
        PayFlexField63ResponseModel detailsF63 = FieldDataParseUtility.parseField63(responseCode);
        LoggerUtils.i("newCall Brac PayFlex-Sale detailsF63: " + detailsF63.toString());

        callback.onResult(result == CallerResult.OK);

    }
}
