package acquire.core.trans.steps;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.chain.Chain;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.SensitiveGuard;
import acquire.core.R;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.display2.Display1CardFragment;
import acquire.core.display2.Display1CardManualFragment;
import acquire.core.fragment.card.CardFragment;
import acquire.core.fragment.card.CardFragmentArgs;
import acquire.core.fragment.card.CardFragmentCallback;
import acquire.core.fragment.card.CardManualFragment;
import acquire.core.tools.EmvHelper;
import acquire.core.tools.SoundPlayer;
import acquire.core.trans.BaseStep;
import acquire.database.model.Record;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;
import acquire.sdk.emv.constant.EntryMode;


/**
 * The {@link BaseStep} that read card {@link ReadCardStep} ,and it contains EMV、PIN、PACK and Secondary authorization steps
 *
 * @author Janson
 * @date 2022/7/18 14:30
 */
public class ReadCardStep extends BaseStep {
    private final BaseStep packStep;
    private final BaseStep pinStep;
    private boolean forcePin;
    private int supportEntry;

    public ReadCardStep(BaseStep pinStep, BaseStep packStep, int supportEntry, boolean forcePin) {
        this.pinStep = pinStep;
        this.packStep = packStep;
        this.forcePin = forcePin;
        this.supportEntry = supportEntry;
    }

    public ReadCardStep(BaseStep pinStep, BaseStep packStep, int supportEntry) {
        this.pinStep = pinStep;
        this.packStep = packStep;
        this.supportEntry = supportEntry;
    }

    /**
     * Intercept the interceptor
     *
     * @param callback interceptor result
     */
    @Override
    public void intercept(Callback callback) {
        //Original record  -> PubBean
        Record origRecord = getOrigRecord();
        if (pubBean.getAmount() == 0 && origRecord != null) {
            pubBean.setAmount(origRecord.getAmount());
        }
        boolean voidTrans = TransType.TRANS_VOID_SALE.equals(pubBean.getTransType())
                || TransType.TRANS_VOID_AUTH_COMPLETE.equals(pubBean.getTransType())
                || TransType.TRANS_VOID_INSTALLMENT.equals(pubBean.getTransType())
                || TransType.TRANS_VOID_PRE_AUTH.equals(pubBean.getTransType());
        if (voidTrans && !ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_OTHER_VOID_CARD, true)) {
            if (origRecord != null) {
                pubBean.setCardNo(origRecord.getCardNo());
                pubBean.setCardScheme(origRecord.getCardScheme());
                pubBean.setExpDate(origRecord.getExpDate());
                pubBean.setEntryMode(EntryMode.MANUAL);
            }
            callback.onResult(true);
            return;
        }
        //check support entry card
        if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)) {
            if (!BDevice.supportMag()) {
                supportEntry &= ~EntryMode.MAG;
            }
            if (!BDevice.supportInsert()) {
                supportEntry &= ~EntryMode.INSERT;
            }
            if (!BDevice.supportRf()) {
                supportEntry &= ~EntryMode.TAP;
            }
        }
        if (supportEntry == 0) {
            pubBean.setMessage(R.string.core_card_unsupport_any_mode);
            pubBean.setResultCode(ResultCode.FL);
            callback.onResult(false);
            return;
        }
        if (supportEntry == EntryMode.MANUAL){
            //only manual
            pubBean.setEntryMode(EntryMode.MANUAL);
            manual(callback);
            return;
        }
        LoggerUtils.d("supportEntry: " + supportEntry);
        boolean accessPin = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_ACCESSIBILITY);
        CardFragmentArgs cardArgs = new CardFragmentArgs(supportEntry, packStep, pinStep, forcePin, stepBean,accessPin);
        CardFragmentCallback cardFragmentCallback = new CardFragmentCallback() {
            @Override
            public void onSuccess(Void unused) {
                SoundPlayer.getInstance().stop();
                callback.onResult(true);
            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                SoundPlayer.getInstance().stop();
                callback.onResult(false);
            }

            @Override
            public void onManual() {
                SoundPlayer.getInstance().stop();
                pubBean.setEntryMode(EntryMode.MANUAL);
                manual(callback);
            }
        };
        if (Model.X800.equals(BDevice.getDeviceModel())) {
            //X800
            mActivity.mSupportDelegate.switchContent(Display1CardFragment.newInstance(cardArgs, cardFragmentCallback));
        } else {
            mActivity.mSupportDelegate.switchContent(CardFragment.newInstance(cardArgs, cardFragmentCallback));
        }
    }

    private void manual(Callback callback) {
        FragmentCallback<String[]> fragmentCallback = new FragmentCallback<String[]>() {
            private boolean done;
            @Override
            public void onSuccess(String[] strings) {
                if (done){
                    return;
                }
                done = true;
                pubBean.setCardNo(strings[0]);
                pubBean.setExpDate(strings[1]);
                pubBean.setCardScheme(new EmvHelper().getCardScheme(EntryMode.MANUAL, pubBean.getCardNo()));
                new Chain<>(stepBean)
                        .next(pinStep)
                        .next(packStep)
                        .proceed(callback);

            }

            @Override
            public void onFail(int errorType, String errorMsg) {
                if (done){
                    return;
                }
                done = true;
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
        if (Model.X800.equals(BDevice.getDeviceModel())) {
            //X800
            mActivity.mSupportDelegate.switchContent(Display1CardManualFragment.newInstance(fragmentCallback));
        }else {
            mActivity.mSupportDelegate.switchContent(CardManualFragment.newInstance(fragmentCallback));
        }
    }


}
