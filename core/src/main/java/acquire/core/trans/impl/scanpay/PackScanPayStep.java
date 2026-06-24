package acquire.core.trans.impl.scanpay;

import acquire.base.utils.LoggerUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.iso.Caller;
import acquire.core.trans.pack.iso.Packet8583;

/**
 * The step that packs {@link ScanPay} data and sends them to the server.
 *
 * @author Janson
 * @date 2021/9/13 15:30
 */
class PackScanPayStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!doReversal()){
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }
        initPubBean();
        pubBean.setMessageId("0700");
        pubBean.setProcessCode("072000");
        pubBean.setServerCode("00");
        //pack 8583
        iso8583.initPack();
        try {
            iso8583.setField(0, pubBean.getMessageId());
            iso8583.setField(3, pubBean.getProcessCode());
            iso8583.setField(4, pubBean.getAmountField());
            iso8583.setField(11, pubBean.getTraceNo());
            iso8583.setField(25, pubBean.getServerCode());
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid());
            iso8583.setField(57, pubBean.getKsn());
            iso8583.setField(60, pubBean.getQrPayCode());
            iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
        }catch (Exception e){
            LoggerUtils.e("set 8583 field error!",e);
            pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error)+e.getMessage());
            pubBean.setResultCode(ResultCode.FL);
            callback.onResult(false);
            return;
        }
        //send to the server
        int result = new Caller.Builder(mActivity, pubBean, iso8583)
                .preSaveReversal(true)
                .checkResp(true)
                .packComm();
        if (result == CallerResult.OK){
            pubBean.setBizOrderNo(iso8583.getField(61));
            callback.onResult(true);
        }else{
            callback.onResult(false);
        }
    }
}
