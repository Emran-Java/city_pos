package acquire.core.trans.impl.login;

import android.text.TextUtils;

import java.util.Date;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.DateUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.ToastUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.constant.CallerResult;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.fragment.login.LoginFragment;
import acquire.core.tools.MultiMerchantUtils;
import acquire.core.tools.PinpadHelper;
import acquire.core.trans.BaseStep;
import acquire.core.trans.pack.iso.Caller;
import acquire.database.model.Merchant;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.BEmvParamLoader;
import acquire.sdk.emv.BExtEmvParamLoader;
import acquire.sdk.emv.IEmvParamLoader;
import acquire.sdk.pin.constant.WorkKeyType;

/**
 * The {@link BaseStep} that packs {@link Login} 8583 and sends them to the server.
 *
 * @author Janson
 * @date 2022/12/21 9:24
 */
class PackLoginStep extends BaseStep {
    @Override
    public void intercept(Callback callback) {
        mActivity.mSupportDelegate.switchContent(LoginFragment.newInstance(new FragmentCallback<String[]>() {

            @Override
            public void onSuccess(String[] strings) {
                String user = strings[0];
                String password = strings[1];
                //change following codes to your logic
                if (!"01".equals(user) || !"0000".equals(password)) {
                    ToastUtils.showToast(R.string.core_login_incorrect_password);
                    return;
                }
                ThreadPool.execute(() -> {
                    initPubBean();
                    pubBean.setResultCode(null);
                    pubBean.setMessage(null);

                    pubBean.setMessageId("0800");
                    pubBean.setProcessCode("920000");

                    iso8583.initPack();
                    iso8583.setField(0, pubBean.getMessageId());
                    iso8583.setField(3, pubBean.getProcessCode());
                    iso8583.setField(11, pubBean.getTraceNo());
                    iso8583.setField(12, pubBean.getTime());
                    //MMdd
                    iso8583.setField(13, pubBean.getDate().substring(4));
                    iso8583.setField(24, pubBean.getNii());
                    iso8583.setField(41, pubBean.getTid());
                    iso8583.setField(42, pubBean.getMid());
                    //send to the server
                    int result = new Caller.Builder(mActivity, pubBean, iso8583)
                            .checkResp(true)
                            .packComm();
                    if (result != CallerResult.OK) {
                        //failed
                        ToastUtils.showToast(pubBean.getMessage());
                        return;
                    }
                    //AID/CAPK
                    IEmvParamLoader emvParamLoader = new BEmvParamLoader();
                    if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)) {
                        emvParamLoader = new BExtEmvParamLoader();
                    }
                    String field60 = iso8583.getField(60);
                    if (!TextUtils.isEmpty(field60)) {
                        emvParamLoader.loadCtAid(field60, false);
                    }
                    String field61 = iso8583.getField(61);
                    if (!TextUtils.isEmpty(field61)) {
                        emvParamLoader.loadClessAid(field61, false);
                    }

                    String workKey = iso8583.getField(57);
                    LoggerUtils.d("workKey:" + workKey);
                    String pik = null, mak = null;
                    String pikCheck = null, makCheck = null;
                    if (workKey.length() >= 32) {
                        pik = workKey.substring(0, 32);
                    }
                    if (workKey.length() >= 38) {
                        pikCheck = workKey.substring(32, 38);
                    }
                    if (workKey.length() >= 70) {
                        mak = workKey.substring(38, 70);
                    }
                    if (workKey.length() >= 76) {
                        makCheck = workKey.substring(70, 76);
                    }
                    LoggerUtils.d("PIN Key:" + pik + ",check Value:" + pikCheck);
                    LoggerUtils.d("MAC Key:" + mak + ",check Value:" + makCheck);
                    Merchant merchant = MultiMerchantUtils.getMerchant(pubBean);
                    PinpadHelper pinpadHelper = new PinpadHelper(merchant.getMasterKeyIndex(),merchant.getAlgorithm());
                    if (!TextUtils.isEmpty(pik)) {
                        //pin key
                        boolean pinResult = pinpadHelper.loadMkskWorkKey(WorkKeyType.PIN_KEY,
                                BytesUtils.hexToBytes(pik), BytesUtils.hexToBytes(pikCheck));
                        if (!pinResult) {
                            ToastUtils.showToast(R.string.core_login_load_pin_key_failed);
                            return ;
                        }
                    }
                    if (!TextUtils.isEmpty(mak)) {
                        //mac key
                        boolean macResult = pinpadHelper.loadMkskWorkKey(WorkKeyType.MAC_KEY,
                                BytesUtils.hexToBytes(mak), BytesUtils.hexToBytes(makCheck));
                        if (!macResult) {
                            ToastUtils.showToast(R.string.core_login_load_mac_key_failed);
                            return ;
                        }
                    }
                    //Update time
                    String timeStr = iso8583.getField(12);
                    String dateStr = iso8583.getField(13);
                    if (!TextUtils.isEmpty(dateStr) && !TextUtils.isEmpty(timeStr)) {
                        dateStr = DateUtils.formatTime(new Date(),DateUtils.YYYY) + dateStr;
                        Date date = DateUtils.getDate(dateStr + timeStr,DateUtils.YYYYMMDDHHMMSS);
                        BDevice.setSystemTime(date);
                    }
                    pubBean.setMessage(R.string.core_login_success);
                    callback.onResult(true);
                });
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                pubBean.setResultCode(ResultCode.UC);
                pubBean.setMessage(R.string.core_transaction_result_user_cancel);
                callback.onResult(false);
            }
        }));
    }
}
