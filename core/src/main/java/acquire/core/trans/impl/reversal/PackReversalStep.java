package acquire.core.trans.impl.reversal;

import android.text.TextUtils;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.tools.DataConverter;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.iso.Caller;
import acquire.database.model.Merchant;
import acquire.database.model.ReversalData;
import acquire.database.repository.MerchantRepository;
import acquire.database.repository.RecordRepository;
import acquire.database.repository.ReversalDataRepository;

/**
 * The {@link BaseStep} that packs {@link Reversal} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2021/6/30 8:58
 */
class PackReversalStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        ReversalDataRepository reversalDataRepository = new ReversalDataRepository();
        ReversalData reversalData = reversalDataRepository.getReverseRecord();
        if (reversalData == null) {
            LoggerUtils.d("No reversal data.");
            callback.onResult(true);
            return;
        }
        int failTimes = reversalData.getHasSend();
        final int maxTimes = 3;

        int connectTimes = 0;
        boolean success = false;
        while (!success && failTimes < maxTimes) {
            initPubBean();
            DataConverter.reversalToPubBean(reversalData, pubBean);
            LoggerUtils.d("send transaction reversal:"+(failTimes+1));
            MerchantRepository merchantRepository = new MerchantRepository();
            Merchant merchant = merchantRepository.findByMidTid(reversalData.getMid(), reversalData.getTid());
            pubBean.setBatchNo(merchant.getBatchNo());
            pubBean.setMessageId("0400");
            pubBean.setServerCode("00");
            iso8583.initPack();
            try {
                iso8583.setField(0, pubBean.getMessageId());
                iso8583.setField(2, pubBean.getCardNo());
                /*if (pubBean.getEntryMode() == EntryMode.MANUAL) {
                    iso8583.setField(2, pubBean.getCardNo());
                }*/
                iso8583.setField(3, pubBean.getProcessCode());
                iso8583.setField(4,  pubBean.getAmountField());
                iso8583.setField(11, pubBean.getTraceNo());
                if (!TextUtils.isEmpty(pubBean.getExpDate())) {
                    iso8583.setField(14, pubBean.getExpDate());
                }
                iso8583.setField(22, pubBean.getField22());
//                if (!TextUtils.isEmpty(pubBean.getCardSn())) {
//                    iso8583.setField(23, pubBean.getCardSn());
//                }
                if (!TextUtils.isEmpty(pubBean.getNii())) {
                    iso8583.setField(24, pubBean.getNii());
                }

                iso8583.setField(25, pubBean.getServerCode());
                if (!TextUtils.isEmpty(pubBean.getOrigAuthCode())) {
                    iso8583.setField(38, pubBean.getOrigAuthCode());
                }
                iso8583.setField(41, pubBean.getTid());
                iso8583.setField(42, pubBean.getMid());
                iso8583.setField(49, pubBean.getCurrencyCode());
                if (!TextUtils.isEmpty(pubBean.getField55())) {
                    iso8583.setField(55, pubBean.getField55());
                }
                iso8583.setField(57, pubBean.getKsn());
                iso8583.setField(62, pubBean.getBatchNo());
                // Date: 20260202 we remove MAC for BRAC for developing time
                //iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
            } catch (Exception e) {
                LoggerUtils.e("set 8583 field error!",e);
                pubBean.setMessage(mActivity.getString(R.string.core_comm_pack_error)+e.getMessage());
                pubBean.setResultCode(ResultCode.FL);
                ToastUtils.showToast(R.string.core_reversal_pack_fail);
                callback.onResult(false);
                return;
            }

            //send to the server
            int result = new Caller.Builder(mActivity, pubBean, iso8583)
                    .withPrompts(R.string.core_reversal_sending)
                    .packComm();
            switch (result) {
                case CallerResult.OK:
                    //check response code
                    String responseCode = iso8583.getField(39);
                    LoggerUtils.d("Reversal response code: "+responseCode);
                    if (ResultCode.OK.equals(responseCode)) {
                        //success
                        success = true;
                    }else {
                        //fail,toast error
                        ToastUtils.showToast(pubBean.getMessage());
                        failTimes++;
                        reversalData.setHasSend(failTimes);
                        LoggerUtils.e("Failed times is "+failTimes);
                    }
                    break;
                case CallerResult.FAIL_NET_CONNECT:
                case CallerResult.FAIL_NET_RECV:
                    //connect failed
                    LoggerUtils.e("Connect failed: "+connectTimes);
                    ToastUtils.showToast(pubBean.getMessage());
                    connectTimes++;
                    if (connectTimes > maxTimes) {
                        //connectTimes > maxTimes,network doesn't work. Try again later
                        callback.onResult(false);
                        return;
                    }
                    break;
                case CallerResult.FAIL_REQUEST_DATA_ERROR:
                case CallerResult.FAIL_RESPONSE_DATA_ERROR:
                default:
                    //continue to resend
                    ToastUtils.showToast(pubBean.getMessage());
                    failTimes++;
                    reversalData.setHasSend(failTimes);
                    LoggerUtils.e("Failed times is "+failTimes);
                    break;
            }

        }
        //delete reversal record
        reversalDataRepository.deleteAllReversalData();
        RecordRepository recordRepository = new RecordRepository();
        if (recordRepository.findByTrace(reversalData.getTraceNo()) != null) {
            //  This step will not be performed normally,
            //  mainly to check whether there are associated records in the record database table.
            recordRepository.deleteByTrace(reversalData.getTraceNo());
        }
        // No matter success or failure, this means that the implementation is successful
        if (success) {
            ToastUtils.showToast(R.string.core_reversal_success);
        } else {
            ToastUtils.showToast(R.string.core_reversal_fail);
        }
        //recover pubbean
        callback.onResult(true);
    }

}
