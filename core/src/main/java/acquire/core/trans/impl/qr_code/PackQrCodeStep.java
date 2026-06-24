package acquire.core.trans.impl.qr_code;

import android.text.TextUtils;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.FormatUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.currency.CurrencyUtils;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ResultCode;
import acquire.core.display2.Display1QrCodeFragment;
import acquire.core.fragment.qrcode.IQrCodeRequester;
import acquire.core.fragment.qrcode.QrCodeFragment;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.iso.Caller;
import acquire.core.trans.pack.iso.Packet8583;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;
import acquire.sdk.emv.constant.EntryMode;

/**
 * The step that packs {@link QrCode} data and sends them to the server.
 *
 * @author Janson
 * @date 2021/9/13 15:30
 */
class PackQrCodeStep extends BaseStep {

    @Override
    public void intercept(Callback callback) {
        if (!doReversal()) {
            //check reversal
            pubBean.setResultCode(ResultCode.FL);
            pubBean.setMessage(R.string.core_reversal_fail);
            callback.onResult(false);
            return;
        }
        pubBean.setEntryMode(EntryMode.SHOW_QR);
        FragmentCallback<String> fragmentCallback = new FragmentCallback<String>() {
            private boolean hasRun;
            @Override
            public void onSuccess(String qrCode) {
                if (hasRun){
                    return;
                }
                hasRun = true;
                pubBean.setResultCode(ResultCode.OK);
                pubBean.setMessage(R.string.core_transaction_result_success);
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                if (hasRun){
                    return;
                }
                hasRun = true;
                switch (errorType) {
                    case FragmentCallback.CANCEL:
                        pubBean.setResultCode(ResultCode.UC);
                        break;
                    case FragmentCallback.TIMEOUT:
                    case FragmentCallback.FAIL:
                    default:
                        pubBean.setResultCode(ResultCode.FL);
                        break;
                }
                pubBean.setMessage(errorMsg);
                callback.onResult(false);
            }
        };
        IQrCodeRequester qrCodeRequester = new IQrCodeRequester() {
            @Override
            public String requestQrCode() {
                initPubBean();
                if (!TextUtils.isEmpty(pubBean.getQrPayCode()) && !TextUtils.isEmpty(pubBean.getBizOrderNo())){
                    return pubBean.getQrPayCode();
                }
                pubBean.setMessageId("0700");
                pubBean.setProcessCode("076000");
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
                    iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
                } catch (Exception e) {
                    LoggerUtils.e("set 8583 field error!",e);
                    return null;
                }
                //send to the server
                int result = new Caller.Builder(mActivity, pubBean, iso8583)
                        .checkResp(true)
                        .packComm();
                String responseCode = iso8583.getField(39);
                if (result == CallerResult.OK && ResultCode.OK.equals(responseCode)){
                    pubBean.setBizOrderNo(iso8583.getField(61));
                    pubBean.setQrPayCode(iso8583.getField(60));
                    return pubBean.getQrPayCode();
                }else{
                    return null;
                }
            }

            @Override
            public boolean queryResult() {
                try {
                    //TODO Simulate a delay test. please delete this code.
                    Thread.sleep(4000);
                } catch (InterruptedException e) {
                    LoggerUtils.e("queryResult sleep error!",e);
                }

                initPubBean();
                pubBean.setMessageId("0700");
                pubBean.setProcessCode("078000");
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
                    iso8583.setField(61, pubBean.getBizOrderNo());
                    iso8583.setField(64, Packet8583.getMac(pubBean,iso8583));
                } catch (Exception e) {
                    LoggerUtils.e("set 8583 field error!",e);
                    return false;
                }
                //send to the server
                int result = new Caller.Builder(mActivity, pubBean, iso8583)
                        .withoutPrompts()
                        .packComm();
                String responseCode = iso8583.getField(39);
                if (result == CallerResult.OK && ResultCode.OK.equals(responseCode)){
                    return true;
                }
                return false;
            }
        };
        if (Model.X800.equals(BDevice.getDeviceModel())) {
            //X800
            mActivity.mSupportDelegate.switchContent(Display1QrCodeFragment.newInstance(qrCodeRequester, fragmentCallback));
        } else {
            String amountPrompt = null;
            if (pubBean.getAmount() != 0) {
                amountPrompt = CurrencyUtils.getCurrencySymbol(pubBean.getCurrencyCode()) + FormatUtils.formatAmount(pubBean.getAmount());
            }
            mActivity.mSupportDelegate.switchContent(QrCodeFragment.newInstance(amountPrompt, qrCodeRequester, fragmentCallback));
        }

    }

}
