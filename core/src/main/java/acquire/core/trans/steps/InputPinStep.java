package acquire.core.trans.steps;

import android.text.TextUtils;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.DisplayUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.display2.Display1PinFragment;
import acquire.core.fragment.pin.AccessPinFragment;
import acquire.core.fragment.pin.EmvExternPinFragment;
import acquire.core.fragment.pin.ExternPinFragment;
import acquire.core.fragment.pin.PinFragment;
import acquire.core.fragment.pin.PinFragmentArgs;
import acquire.core.tools.MultiMerchantUtils;
import acquire.core.tools.PinpadHelper;
import acquire.core.trans.BaseStep;
import acquire.database.model.Merchant;
import acquire.database.model.Record;
import acquire.database.repository.RecordRepository;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * The {@link BaseStep} that is used to inputs pin.
 *
 * @author Janson
 * @date 2018/11/22 23:12
 */
public class InputPinStep extends BaseStep {
    private boolean isOfflinePin;
    private boolean emvMode;

    public void setOfflinePin(boolean isOfflinePin) {
        this.isOfflinePin = isOfflinePin;
    }

    public void setEmvMode(boolean emvMode) {
        this.emvMode = emvMode;
    }

    @Override
    public void intercept(Callback callback) {
        boolean voidTrans = TransType.TRANS_VOID_SALE.equals(pubBean.getTransType())
                || TransType.TRANS_VOID_AUTH_COMPLETE.equals(pubBean.getTransType())
                || TransType.TRANS_VOID_PRE_AUTH.equals(pubBean.getTransType())
                || TransType.TRANS_VOID_INSTALLMENT.equals(pubBean.getTransType());
        if (voidTrans && !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_OTHER_VOID_PIN,true)) {
            callback.onResult(true);
            return;
        }
        if (TextUtils.isEmpty(pubBean.getCardNo())){
            if (!TextUtils.isEmpty(pubBean.getOrigTraceNo())) {
                RecordRepository recordRepository = new RecordRepository();
                Record origRecord = recordRepository.findByTrace(pubBean.getOrigTraceNo());
                if (!TextUtils.isEmpty(origRecord.getCardNo())) {
                    pubBean.setCardNo(origRecord.getCardNo());
                }
            }
        }
        Merchant merchant = MultiMerchantUtils.getMerchant(pubBean);
        PinpadHelper pinpadHelper = new PinpadHelper(merchant.getMasterKeyIndex(),merchant.getAlgorithm());
        if (pubBean.isRequireKsnInc()){
            pinpadHelper.asyncIncreaseKsn();
            pubBean.setRequireKsnInc(false);
        }
        PinFragmentArgs args = new PinFragmentArgs();
        args.setPan(pubBean.getCardNo());
        args.setAmount(pubBean.getAmount());
        args.setCardScheme(pubBean.getCardScheme());
        args.setCurrencyCode(pubBean.getCurrencyCode());
        //online pin
        args.setOnlinePin(!isOfflinePin);
        args.setSupportPinLengths(new byte[]{0, 4, 5, 6, 7, 8, 9, 10, 11, 12});
        args.setAlgorithm(merchant.getAlgorithm());
        args.setMasterIndex(merchant.getMasterKeyIndex());
        LoggerUtils.d("LogOn: (PayFlex) args.setMasterIndex: "+merchant.getType());
        LoggerUtils.d("LogOn: (PayFlex) args.setMasterIndex: "+merchant.getMasterKeyIndex());
        args.setAccessPinPad(pubBean.isAccessPin());
        if (emvMode && ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)) {
            //external PIN pad
            mActivity.mSupportDelegate.switchContent(EmvExternPinFragment.newInstance(args));
            PinpadHelper.waitKsn();
            callback.onResult(true);
        } else {
            FragmentCallback<byte[]> fragmentCallback = new FragmentCallback<byte[]>() {
                @Override
                public void onSuccess(byte[] pin) {
                    if (pin == null || pin.length == 0) {
                        callback.onResult(true);
                        return;
                    }
                    String strPin = BytesUtils.bcdToString(pin);
                    if (isOfflinePin) {
                        pubBean.setOfflinePinBlock(strPin);
                    }else{
                        pubBean.setPinBlock(strPin);
                    }
                    callback.onResult(true);
                }

                @Override
                public void onFail(int errorType, String errorMsg) {
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
            if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)){
                mActivity.mSupportDelegate.switchContent(ExternPinFragment.newInstance(args,fragmentCallback ));
            }else {
                //built-in PIN pad
                if (Model.X800.equals(BDevice.getDeviceModel())) {
                    //X800
                    mActivity.mSupportDelegate.switchContent(Display1PinFragment.newInstance(args,fragmentCallback ));
                } else{
                    boolean accessPinPad = pubBean.isAccessPin() && !BDevice.supportPhysicalKeyboard();
                    if (accessPinPad){
                        DisplayUtils.showNavigationBar(mActivity.getWindow(),false);
                        mActivity.mSupportDelegate.switchContent(AccessPinFragment.newInstance(args, new FragmentCallback<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                DisplayUtils.showNavigationBar(mActivity.getWindow(),true);
                                fragmentCallback.onSuccess(bytes);
                            }

                            @Override
                            public void onFail(int errorType, String errorMsg) {
                                DisplayUtils.showNavigationBar(mActivity.getWindow(),true);
                                fragmentCallback.onFail(errorType,errorMsg);
                            }
                        }));
                    }else{
                        mActivity.mSupportDelegate.switchContent(PinFragment.newInstance(args,fragmentCallback ));
                    }
                }
            }

        }

    }
}
