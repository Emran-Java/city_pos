package acquire.core.trans.impl.balance;

import android.text.TextUtils;

import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.PackField;
import acquire.core.trans.pack.iso.Caller;
import acquire.core.trans.pack.iso.Packet8583;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The {@link BaseStep} that packs {@link Balance} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2019/7/24 14:50
 */
class PackBalanceStep extends BaseStep {

    @Override
    public void intercept(Callback callback)  {
        if (!doReversal()){
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }
        initPubBean();

        pubBean.setMessageId("0100");
        pubBean.setProcessCode("310000");
        pubBean.setServerCode("00");
        pubBean.setField22(PackField.packField22(pubBean));

        iso8583.initPack();
        try {
            iso8583.setField(0, pubBean.getMessageId());
            if (pubBean.getEntryMode() == EntryMode.MANUAL) {
                iso8583.setField(2, pubBean.getCardNo());
            }
            iso8583.setField(3, pubBean.getProcessCode());

            iso8583.setField(11, pubBean.getTraceNo());
            if (pubBean.getEntryMode() == EntryMode.MANUAL) {
                iso8583.setField(14, pubBean.getExpDate());
            }

            iso8583.setField(22, pubBean.getField22());
            if (!TextUtils.isEmpty(pubBean.getCardSn())) {
                iso8583.setField(23, pubBean.getCardSn());
            }
            if (!TextUtils.isEmpty(pubBean.getNii())) {
                iso8583.setField(24, pubBean.getNii());
            }
            iso8583.setField(25, pubBean.getServerCode());
            if (!TextUtils.isEmpty(pubBean.getTrack2())) {
                iso8583.setField(35, pubBean.getTrack2());
            }
            if (!TextUtils.isEmpty(pubBean.getTrack3())) {
                iso8583.setField(36, pubBean.getTrack3());
            }
            iso8583.setField(41, pubBean.getTid());
            iso8583.setField(42, pubBean.getMid());
            iso8583.setField(49, pubBean.getCurrencyCode());
            if (!TextUtils.isEmpty(pubBean.getPinBlock())) {
                iso8583.setField(52, pubBean.getPinBlock());
            }
            if (pubBean.getEntryMode() == EntryMode.INSERT || pubBean.getEntryMode() == EntryMode.TAP) {
                iso8583.setField(55, pubBean.getField55());
            }
            iso8583.setField(57, pubBean.getKsn());
            iso8583.setField(62, pubBean.getBatchNo());
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
                .checkResp(true)
                .packComm();
        if (result == CallerResult.OK){
            pubBean.setBalance(pubBean.getAmount());
            pubBean.setMessage(mActivity.getString(R.string.core_balance_display_prefix)+FormatUtils.formatAmount(pubBean.getBalance(),2));
            callback.onResult(true);
        }else{
            callback.onResult(false);
        }
    }
}
