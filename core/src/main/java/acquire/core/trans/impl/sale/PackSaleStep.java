package acquire.core.trans.impl.sale;

import android.text.TextUtils;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.PackField;
import acquire.core.trans.pack.iso.Caller;
import acquire.core.trans.pack.iso.Packet8583;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The step that packs {@link Sale} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2019/2/12 15:49
 */
class PackSaleStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!doReversal()) {
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }
        initPubBean();
        pubBean.setMessageId("0200");       // Field 0 MTI
        pubBean.setProcessCode("000000");   // Field 3
        //pubBean.setServerCode("00");
        pubBean.setField22(PackField.packField22(pubBean)); //POS Entry Mode, Contactless EMV 0071
        //pack 8583
        iso8583.initPack();
        try {
            /*iso8583.setField(0, pubBean.getMessageId());
            if (pubBean.getEntryMode() != EntryMode.MAG) {
                iso8583.setField(2, pubBean.getCardNo());
            }*/
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(4, pubBean.getAmountField());
            iso8583.setField(11, pubBean.getTraceNo());
            /*if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                iso8583.setField(14, pubBean.getExpDate());
            }*/
            iso8583.setField(22, pubBean.getField22());
            if (!TextUtils.isEmpty(pubBean.getCardSn())) {
                iso8583.setField(23, pubBean.getCardSn());
            }
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            }

            if (!TextUtils.isEmpty(pubBean.getServerCode())) {
                iso8583.setField(25, pubBean.getServerCode());
            } else {
                iso8583.setField(25, "00");
            }

            if (!TextUtils.isEmpty(pubBean.getTrack2())) {
                iso8583.setField(35, pubBean.getTrack2());
            }
            /*if (!TextUtils.isEmpty(pubBean.getTrack3())) {
                iso8583.setField(36, pubBean.getTrack3());
            }*/
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid());
            iso8583.setField(49, pubBean.getCurrencyCode());
            if (!TextUtils.isEmpty(pubBean.getPinBlock())) {
                iso8583.setField(52, pubBean.getPinBlock());
            }

            if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_OTHER_TIP_INPUT, false)) {
                String tipAmnt = "000000000000", baseAmount = "000000000000";
                try {
                    tipAmnt = String.format(java.util.Locale.US, "%012d", pubBean.getTipAmount());
                    baseAmount = String.format(java.util.Locale.US, "%012d", pubBean.getBaseAmount());
                } catch (Exception ex) {
                    LoggerUtils.d("newCall Field54() value exception:  " + ex.getMessage());
                }
                //iso8583.setField(54, tipAmnt);
                //iso8583.setField(60, baseAmount);
            }

            if (!TextUtils.isEmpty(pubBean.getField55()) && (pubBean.getEntryMode() == EntryMode.INSERT || pubBean.getEntryMode() == EntryMode.TAP)) {
                iso8583.setField(55, pubBean.getField55());
                LoggerUtils.d("newCall Brac Sale-Sale .getField55():  " + pubBean.getField55());
            }

//            iso8583.setField(57, pubBean.getKsn());
            iso8583.setField(62, pubBean.getBatchNo());
            // Date: 2026012, comment for now because QA team say remove this field for the Brac Bank
            //iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
        } catch (Exception e) {
            LoggerUtils.e("set 8583 field error!", e);
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error) + e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            callback.onResult(false);
            return;
        }

        //send to the server
        int result = 0;
        result = new Caller.Builder(mActivity, pubBean, iso8583)
                .checkResp(true)
                .preSaveReversal(true)
                .packComm();
        //Date: 20260511, we don't update f55 after transaction
        //pubBean.setField55(iso8583.getField(55));

        callback.onResult(result == CallerResult.OK);
    }
}
