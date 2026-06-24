package acquire.core.fragment.card;

import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import acquire.base.activity.callback.FragmentCallback;
import acquire.base.chain.Chain;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.R;
import acquire.core.bean.PubBean;
import acquire.core.bean.StepBean;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.ResultCode;
import acquire.core.constant.TransType;
import acquire.core.tools.EmvHelper;
import acquire.core.tools.MultiMerchantUtils;
import acquire.core.trans.BaseStep;
import acquire.core.trans.steps.EmvOfflineStep;
import acquire.core.trans.steps.InputPinStep;
import acquire.database.model.Merchant;
import acquire.sdk.emv.bean.EmvFetchBean;
import acquire.sdk.emv.bean.EmvLaunchParam;
import acquire.sdk.emv.bean.EmvReadyBean;
import acquire.sdk.emv.bean.PinResult;
import acquire.sdk.emv.constant.EmvResult;
import acquire.sdk.emv.constant.EmvTransType;
import acquire.sdk.emv.constant.EntryMode;
import acquire.sdk.emv.listener.EmvListener;
import acquire.sdk.emv.listener.EmvResponser;
import acquire.sdk.emv.listener.EmvSecondGacListener;
import acquire.sdk.sound.BBeeper;

/**
 * Card view model
 *
 * @author Janson
 * @date 2023/9/20 9:20
 */
public class CardViewModel extends ViewModel {
    private boolean forceExit;
    private CardFragmentArgs cardFragmentArgs;

    private CardFragmentCallback callback;
    private final EmvHelper mEmvHelper = new EmvHelper();

    private final MutableLiveData<CardStatus> cardStatus = new MutableLiveData<>();
    /**
     * 1 insert, 2 tap;
     */
    private final MutableLiveData<Integer> removeCard = new MutableLiveData<>();

    private final MutableLiveData<Integer> toastText = new MutableLiveData<>();


    public MutableLiveData<CardStatus> getCardStatus() {
        return cardStatus;
    }

    public MutableLiveData<Integer> getRemoveCard() {
        return removeCard;
    }

    public MutableLiveData<Integer> getToastText() {
        return toastText;
    }


    public void cancel() {
        PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
        pubBean.setResultCode(ResultCode.UC);
        pubBean.setMessage(R.string.core_card_cancel);
        mEmvHelper.cancelEmv();
    }

    public void gotoManual() {
        LoggerUtils.e("cancel card,go to manual.");
        mEmvHelper.cancelEmv();
        forceExit = true;
        callback.onManual();
    }

    public void init(CardFragmentArgs cardFragmentArgs, CardFragmentCallback callback) {
        this.callback = callback;
        this.cardFragmentArgs = cardFragmentArgs;
    }

    public void startReadCard() {
        ThreadPool.execute(() -> {
            StepBean stepBean = cardFragmentArgs.getStepBean();
            PubBean pubBean = cardFragmentArgs.getStepBean().getPubBean();
            int support = cardFragmentArgs.getSupportEntry() &(EntryMode.MAG |EntryMode.INSERT|EntryMode.TAP) ;
            executeEmv(pubBean, support, cardFragmentArgs.isForcePin(), stepBean, cardFragmentArgs.getPackStep(), cardFragmentArgs.getPinStep(), callback);
        });
    }

    private void executeEmv(PubBean pubBean, int supportEntry, boolean forcePin, StepBean stepBean, BaseStep packStep, BaseStep pinStep, CardFragmentCallback callback) {
        int emvTransType;
        switch (pubBean.getTransType()) {
            case TransType.TRANS_SALE:
            case TransType.TRANS_AUTH_COMPLETE:
            case TransType.TRANS_INSTALLMENT:
                emvTransType = EmvTransType.SALE;
                break;
            case TransType.TRANS_PRE_AUTH:
                emvTransType = EmvTransType.PREAUTH;
                break;
            case TransType.TRANS_BALANCE:
                emvTransType = EmvTransType.BALANCE;
                break;
            case TransType.TRANS_REFUND:
                emvTransType = EmvTransType.REFUND;
                break;
            case TransType.TRANS_CASH_ADVANCE:
                emvTransType = EmvTransType.CASH_ADVANCE;
                break;
            case TransType.TRANS_CASH_BACK:
                emvTransType = EmvTransType.CASH_BACK;
                break;
            case TransType.TRANS_VOID_SALE:
            default:
                emvTransType = EmvTransType.VOID;
                break;
        }
        EmvLaunchParam param = new EmvLaunchParam.Builder(emvTransType)
                .timeout(ParamsUtils.getInt(ParamsConst.PARAMS_KEY_CARD_READER_TIMEOUT,60))
                .entryMode(supportEntry)
                .amount(pubBean.getAmount())
                .cashAmount(pubBean.getCashAmount())
                .create();
        mEmvHelper.terminateTransaction();
        mEmvHelper.readCard(param, new EmvListener() {
            private boolean hasInputPin;
            private boolean retry;

            @Override
            public void onReady(EmvReadyBean emvReadyBean, EmvResponser.SimpleResponser simpleResponser) {
                LoggerUtils.d("startReadCard: onReady");
                cardStatus.postValue(new CardReadyStatus(emvReadyBean));
                simpleResponser.finish();
            }

            @Override
            public void onReading(EmvResponser.SimpleResponser simpleResponser) {
                LoggerUtils.d("startReadCard: onReading");
                cardStatus.postValue(new CardReadingStatus());
                simpleResponser.finish();
            }

            @Override
            public void onSelectAid(List<String> preferNames, EmvResponser.IntegerResponser integerResponser) {
                LoggerUtils.d("startReadCard: onSelectAid");
                cardStatus.postValue(new CardSelectAidStatus(preferNames, integerResponser));
            }

            @Override
            public void onInsertError(EmvResponser.BooleanResponser booleanResponser) {
                LoggerUtils.d("startReadCard: onInsertError");
                booleanResponser.finish(true);

            }

            @Override
            public void onFinalSelect(EmvResponser.SimpleResponser simpleResponser) {
                LoggerUtils.d("startReadCard: onFinalSelect");
                EmvFetchBean emvFetchBean = mEmvHelper.getEmvFetchBean();
                pubBean.setEntryMode(emvFetchBean.getUserEntryMode());
                simpleResponser.finish();
            }

            @Override
            public void onSeePhone(EmvResponser.BooleanResponser booleanResponser) {
                LoggerUtils.d("startReadCard: onSeePhone");
                cardStatus.postValue(new CardSeePhoneStatus(result -> {
                    if (!result) {
                        pubBean.setResultCode(ResultCode.UC);
                        pubBean.setMessage(R.string.core_card_cancel);
                        LoggerUtils.e("cancel see phone");
                    }
                    booleanResponser.finish(result);
                }));
            }

            @Override
            public void onCardNum(String pan, EmvResponser.BooleanResponser booleanResponser) {
                LoggerUtils.d("startReadCard: onCardNum");
                if (TextUtils.isEmpty(pan)) {
                    pubBean.setMessage(R.string.core_card_null);
                    pubBean.setResultCode(ResultCode.FL);
                    booleanResponser.finish(false);
                    return;
                }
                EmvFetchBean emvFetchBean = mEmvHelper.getEmvFetchBean();
                if (pubBean.getEntryMode() == 0) {
                    pubBean.setEntryMode(emvFetchBean.getUserEntryMode());
                }
                if (pubBean.getCardScheme() == null) {
                    //card scheme
                    pubBean.setCardScheme(mEmvHelper.getCardScheme(pubBean.getEntryMode(), pan));
                }
                if (pubBean.getEntryMode() == EntryMode.MAG) {
                    // mag
                    String track2 = emvFetchBean.getTrack2();
                    if (TextUtils.isEmpty(track2)) {
                        toastText.postValue(R.string.core_card_swipe_again);
                        retry = true;
                        booleanResponser.finish(false);
                        return;
                    }
                }
                pubBean.setTrack2(emvFetchBean.getTrack2());
                pubBean.setTrack3(emvFetchBean.getTrack3());
                pubBean.setExpDate(emvFetchBean.getExpDate());
                pubBean.setCardNo(pan);
                //TODO 测试
//                if (pubBean.isThirdCall()) {
//                    booleanResponser.finish(true);
//                    return;
//                }
                cardStatus.postValue(new CardNumberStatus(pan, result -> {
                    if (!result) {
                        pubBean.setResultCode(ResultCode.UC);
                        pubBean.setMessage(R.string.core_card_cancel);
                        LoggerUtils.e("cancel card");
                    }
                    booleanResponser.finish(result);
                }));
            }

            @Override
            public void onInputPin(boolean isOnlinePin, int pinTryCount, EmvResponser.PinResponser pinResponser) {
                LoggerUtils.d("startReadCard: onInputPin");
                hasInputPin = true;
                if (pubBean.getCardNo() == null) {
                    EmvFetchBean emvFetchBean = mEmvHelper.getEmvFetchBean();
                    pubBean.setCardNo(emvFetchBean.getPan());
                }
                if (pubBean.getCardScheme() == null) {
                    //card scheme
                    pubBean.setCardScheme(mEmvHelper.getCardScheme(pubBean.getEntryMode(), pubBean.getCardNo()));
                }
                cardStatus.postValue(new CardPinStatus());
                if (pinStep == null) {
                    // Not need pin
                    pinResponser.finish(PinResult.newStatusByPass());
                    return;
                }
                if (pinStep instanceof InputPinStep) {
                    InputPinStep inputPinStep = (InputPinStep) pinStep;
                    inputPinStep.setOfflinePin(!isOnlinePin);
                    inputPinStep.setEmvMode(true);
                }
                Chain<StepBean> chain = new Chain<>(stepBean);
                chain.next(pinStep)
                        .proceed(isSucc -> {
                            if (isSucc) {
                                if (ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)) {
                                    //external PIN pad
                                    Merchant merchant = MultiMerchantUtils.getMerchant(pubBean);
                                    int pinIndex = merchant.getMasterKeyIndex();
                                    int algorithmType = merchant.getAlgorithm();
                                    int timeoutSec = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_TIMEOUT, 60);
                                    if (pubBean.isAccessPin()){
                                        timeoutSec = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_ACCESSIBILITY_TIMEOUT, 120);

                                    }
                                    pinResponser.finish(PinResult.newStatusExtPinpad(pinIndex, algorithmType, timeoutSec));
                                } else {
                                    //built-in card reader
                                    String pinBlock;
                                    if (isOnlinePin) {
                                        pinBlock = pubBean.getPinBlock();
                                    } else {
                                        pinBlock = pubBean.getOfflinePinBlock();
                                    }
                                    if (TextUtils.isEmpty(pinBlock)) {
                                        //No input any PIN.
                                        pinResponser.finish(PinResult.newStatusByPass());
                                    } else {
                                        pinResponser.finish(PinResult.newStatusOk(BytesUtils.hexToBytes(pinBlock)));
                                    }
                                }
                            } else {
                                //Cancel PIN
                                pinResponser.finish(PinResult.newStatusCancel());
                            }
                        });
            }

            @Override
            public void onResult(boolean success, int emvResult) {
                LoggerUtils.d("startReadCard: onResult");
                cardStatus.postValue(new CardFinishStatus());
                if (retry) {
                    LoggerUtils.e("Icc. Re read card!");
                    pubBean.setEntryMode(0);
                    executeEmv(pubBean, supportEntry, forcePin, stepBean, packStep, pinStep, callback);
                    return;
                }
                if (forceExit) {
                    LoggerUtils.e("force exit from Emv.");
                    terminateTransaction();
                    return;
                }
                if (!success) {
                    if (emvResult == EmvResult.TXN_TRY_ANOTHER){
                        toastText.postValue(R.string.core_card_try_another_entry);
                        pubBean.setEntryMode(0);
                        executeEmv(pubBean, supportEntry & ~pubBean.getEntryMode(), forcePin, stepBean, packStep, pinStep, callback);
                        return;
                    }
                    if (TextUtils.isEmpty(pubBean.getMessage())) {
                        pubBean.setMessage(R.string.core_card_emv_fail);
                    }
                    if (TextUtils.isEmpty(pubBean.getResultCode())) {
                        pubBean.setResultCode(ResultCode.FL);
                    }
                    terminateTransaction();
                    callback.onFail(FragmentCallback.FAIL, pubBean.getMessage());
                    return;
                }
                if (!mEmvHelper.dealEmvData(pubBean)) {
                    terminateTransaction();
                    callback.onFail(FragmentCallback.FAIL, pubBean.getMessage());
                    return;
                }
                boolean needInputPin = !hasInputPin && forcePin;
                if (packStep == null && !needInputPin) {
                    terminateTransaction();
                    callback.onSuccess(null);
                    return;
                }
                boolean needSecondGac = emvResult == EmvResult.TXN_ONLINE && pubBean.getEntryMode() == EntryMode.INSERT;
                if (!needSecondGac) {
                    //End of EMV, power off
                    terminateTransaction();
                }
                Chain<StepBean> chain = new Chain<>(stepBean);
                if (needInputPin && pinStep != null) {
                    chain.next(pinStep);
                }
                //check emvResult
                if (emvResult == EmvResult.TXN_OK || emvResult == EmvResult.TXN_ONLINE) {
                    //Simple/Standard Process OK
                    chain.next(packStep);
                } else {
                    //Approved
                    LoggerUtils.d("Emv offline");
                    chain.next(new EmvOfflineStep());
                }
                chain.proceed(stepSucc -> {
                    if (!needSecondGac) {
                        //Not require Secondary Authorization
                        if (stepSucc) {
                            callback.onSuccess(null);
                        } else {
                            callback.onFail(FragmentCallback.FAIL, pubBean.getMessage());
                        }
                        return;
                    }
                    //start Secondary Authorization
                    byte[] gac = mEmvHelper.packGac(stepSucc, pubBean.getResultCode(), pubBean.getField55());
                    mEmvHelper.secondGac(pubBean.isRequestOnlineSucc(), gac, new EmvSecondGacListener() {
                        @Override
                        public void completeResult(boolean result) {
                            //finish
                            if (stepSucc && !result) {
                                //host success, but second gac failed
                                pubBean.setResultCode(ResultCode.FL);
                                pubBean.setMessage(R.string.core_card_second_gac_failed);
                            }
                            if (result) {
                                //update emv print data
                                pubBean.setEmvPrintData(mEmvHelper.packEmvPrintData());
                                terminateTransaction();
                                callback.onSuccess(null);
                            } else {
                                terminateTransaction();
                                callback.onFail(FragmentCallback.FAIL, pubBean.getMessage());
                            }
                        }

                        @Override
                        public void recard() {
                            cardStatus.postValue(new CardReadingStatus());
                        }
                    });

                });
            }
        });
    }

    private void terminateTransaction() {
        mEmvHelper.terminateTransaction();
        //TODO 测试
//        if (cardFragmentArgs.getStepBean().getPubBean().isThirdCall()) {
//            return;
//        }
        LoggerUtils.d("check whether the card exists...");
        int count = 0;
        boolean isShow = false;
        int ret;
        while ((ret = mEmvHelper.cardExist()) != 0) {
            if (!isShow) {
                removeCard.postValue(ret);
                isShow = true;
            }
            try {
                if (count > 10 && count % 8 == 0) {
                    BBeeper.beep(750, 200);
                }
                count++;
                Thread.sleep(100);
            } catch (Exception e) {
                LoggerUtils.e("Thread sleep error!",e);
            }
        }
        if (isShow) {
            removeCard.postValue(0);
        }
        LoggerUtils.d("check card over...");
    }

    public static class CardStatus {
    }

    public static class CardReadyStatus extends CardStatus {
        public final EmvReadyBean emvReadyBean;

        public CardReadyStatus(EmvReadyBean emvReadyBean) {
            this.emvReadyBean = emvReadyBean;
        }
    }

    public static class CardReadingStatus extends CardStatus {
    }

    public static class CardSelectAidStatus extends CardStatus {
        public final List<String> preferNames;
        public final EmvResponser.IntegerResponser integerResponser;

        public CardSelectAidStatus(List<String> preferNames, EmvResponser.IntegerResponser integerResponser) {
            this.preferNames = preferNames;
            this.integerResponser = integerResponser;
        }
    }


    public static class CardSeePhoneStatus extends CardStatus {
        public final EmvResponser.BooleanResponser booleanResponser;

        public CardSeePhoneStatus(EmvResponser.BooleanResponser booleanResponser) {
            this.booleanResponser = booleanResponser;
        }
    }

    public static class CardNumberStatus extends CardStatus {
        public final String cardNumber;
        public final EmvResponser.BooleanResponser booleanResponser;

        public CardNumberStatus(String cardNumber, EmvResponser.BooleanResponser booleanResponser) {
            this.cardNumber = cardNumber;
            this.booleanResponser = booleanResponser;
        }
    }

    public static class CardPinStatus extends CardStatus {
    }

    public static class CardFinishStatus extends CardStatus {
    }
} 
