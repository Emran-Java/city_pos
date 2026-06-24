package acquire.core.trans.impl.void_sale;

import android.text.TextUtils;

import acquire.base.utils.LoggerUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.PackField;
import acquire.core.trans.pack.iso.Caller;
import acquire.core.trans.pack.iso.Packet8583;
import acquire.database.model.Record;
import acquire.database.repository.PreStepVoidAbleRecordData;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The {@link BaseStep} that packs {@link VoidSale} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2019/7/25 9:35
 */
class PackVoidSaleStep extends BaseStep {

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
        Record origRecord = getOrigRecord();
        pubBean.setOrigReferNo(origRecord.getReferNo());
        pubBean.setOrigAuthCode(origRecord.getAuthCode());
        pubBean.setAmount(origRecord.getAmount());

        pubBean.setMessageId("0200");
        pubBean.setProcessCode("020000");
        pubBean.setServerCode("00");
        pubBean.setField22(PackField.packField22(pubBean));
        //pack 8583
        iso8583.initPack();

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

        try {
            iso8583.setField(0, pubBean.getMessageId());
            if (pubBean.getEntryMode() != EntryMode.MAG) {
                iso8583.setField(2, pubBean.getCardNo());
            }
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(4, pubBean.getAmountField());
            iso8583.setField(11, pubBean.getTraceNo());

            if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                iso8583.setField(14, pubBean.getExpDate());
            } else {
                iso8583.setField(14, "0000");
            }

            iso8583.setField(22, pubBean.getField22());
            if (!TextUtils.isEmpty(pubBean.getCardSn())) {
                iso8583.setField(23, pubBean.getCardSn());
            }else{
                iso8583.setField(23, "000");
            }
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            } else {
                iso8583.setField(24, "0001"); // Network International Identifier (NII), 0001 - On-us / Internal
            }

            iso8583.setField(25, pubBean.getServerCode());

            /*if (!TextUtils.isEmpty(pubBean.getTrack2())) {
                iso8583.setField(35, pubBean.getTrack2());
            }
            if (!TextUtils.isEmpty(pubBean.getTrack3())) {
                iso8583.setField(36, pubBean.getTrack3());
            }*/

            iso8583.setField(37, pubBean.getOrigReferNo());

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

           /*if (!TextUtils.isEmpty(pubBean.getPinBlock())) {
                iso8583.setField(52, pubBean.getPinBlock());
            }
            iso8583.setField(57, pubBean.getKsn());*/

            iso8583.setField(62, pubBean.getTraceNo());//STAN
//            iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
        } catch (Exception e) {
            LoggerUtils.e("set 8583 field error!", e);
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error) + e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            callback.onResult(false);
            return;
        }
        //send to the server
        int result = new Caller.Builder(mActivity, pubBean, iso8583)
                .checkResp(true)
                .preSaveReversal(true)
                .packComm();
        callback.onResult(result == CallerResult.OK);
    }
}
