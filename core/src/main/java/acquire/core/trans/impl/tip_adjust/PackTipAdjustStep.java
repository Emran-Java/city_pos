package acquire.core.trans.impl.tip_adjust;

import android.text.TextUtils;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.PackField;
import acquire.core.trans.pack.iso.Caller;
import acquire.database.repository.PreStepVoidAbleRecordData;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The step that packs {@link TipAdjust} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2019/2/12 15:49
 */
class PackTipAdjustStep extends BaseStep {

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


        if (PreStepVoidAbleRecordData.preStepVoidRecord != null) {

            if (pubBean.getCardNo() == null)
                pubBean.setCardNo(PreStepVoidAbleRecordData.preStepVoidRecord.getCardNo());

            if (pubBean.getExpDate() == null)
                pubBean.setExpDate(PreStepVoidAbleRecordData.preStepVoidRecord.getExpDate());

            if (pubBean.getEntryMode() == 0)
                pubBean.setEntryMode(PreStepVoidAbleRecordData.preStepVoidRecord.getEntryMode());

            if (pubBean.getCardScheme() == null)
                pubBean.setCardScheme(PreStepVoidAbleRecordData.preStepVoidRecord.getCardScheme());

            if (pubBean.getOrigReferNo() == null)
                pubBean.setOrigReferNo(PreStepVoidAbleRecordData.preStepVoidRecord.getOrigReferNo());

            if (pubBean.getReferNo() == null)
                pubBean.setReferNo(PreStepVoidAbleRecordData.preStepVoidRecord.getReferNo());

            if (pubBean.getOrigAuthCode() == null)
                pubBean.setOrigAuthCode(PreStepVoidAbleRecordData.preStepVoidRecord.getOrigAuthCode());

            if (pubBean.getAuthCode() == null) {
                pubBean.setAuthCode(PreStepVoidAbleRecordData.preStepVoidRecord.getAuthCode());
            }
            pubBean.setField22(PreStepVoidAbleRecordData.preStepVoidRecord.getField22());

            PreStepVoidAbleRecordData.oldTransactionType = PreStepVoidAbleRecordData.preStepVoidRecord.getTransType();
            PreStepVoidAbleRecordData.oltTransactionInvId = PreStepVoidAbleRecordData.preStepVoidRecord.getTraceNo();
        }

        PreStepVoidAbleRecordData.preStepVoidRecord = null;
        pubBean.setTransType(TransType.TRANS_TIP_SALE);

        pubBean.setMessageId("0220");       // Field 0 MTI
        pubBean.setProcessCode("020000");   // Field 3
        //pubBean.setServerCode("00");
        pubBean.setField22(PackField.packField22(pubBean)); //POS Entry Mode, Contactless EMV 0071
        //pack 8583
        iso8583.initPack();

        try {
            //iso8583.setField(0, pubBean.getMessageId());
            if (pubBean.getEntryMode() != EntryMode.MAG) {
                iso8583.setField(2, pubBean.getCardNo());
            }
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(4, pubBean.getAmountField());
            iso8583.setField(11, pubBean.getTraceNo());
            iso8583.setField(11, pubBean.getTraceNo());
            iso8583.setField(12, pubBean.getTime());
            //MMdd
            iso8583.setField(13, pubBean.getDate().substring(4));

            if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                iso8583.setField(14, pubBean.getExpDate());
            }

            iso8583.setField(22, pubBean.getField22());
           /* if (!TextUtils.isEmpty(pubBean.getCardSn())) {
                iso8583.setField(23, pubBean.getCardSn());
            }*/
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            }

            if (!TextUtils.isEmpty(pubBean.getServerCode())) {
                iso8583.setField(25, pubBean.getServerCode());
            } else {
                iso8583.setField(25, "00");
            }

            /*if (!TextUtils.isEmpty(pubBean.getTrack2())) {
                iso8583.setField(35, pubBean.getTrack2());
            }*/
            /*if (!TextUtils.isEmpty(pubBean.getTrack3())) {
                iso8583.setField(36, pubBean.getTrack3());
            }*/

            String rrnCode = "000000000000";
            if (!TextUtils.isEmpty(pubBean.getOrigReferNo())) {
                rrnCode = pubBean.getOrigReferNo();
            }
            else if(!TextUtils.isEmpty(pubBean.getReferNo())){
                rrnCode = pubBean.getReferNo();
            }
            iso8583.setField(37, rrnCode);

            if (!TextUtils.isEmpty(pubBean.getOrigAuthCode())) {
                iso8583.setField(38, pubBean.getOrigAuthCode());
            } else if (!TextUtils.isEmpty(pubBean.getAuthCode())) {
                iso8583.setField(38, pubBean.getAuthCode());
            } else {
                iso8583.setField(38, "000000");
            }


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
                iso8583.setField(54, tipAmnt);
                iso8583.setField(60, baseAmount);
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
