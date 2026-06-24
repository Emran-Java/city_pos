package acquire.sdk.emv;

import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.internal.emvl2.type.publickey;
import com.newland.sdk.emvl3.api.common.EmvL3Const;
import com.newland.sdk.emvl3.api.common.ErrorCode;
import com.newland.sdk.emvl3.api.common.listener.Candidate;
import com.newland.sdk.emvl3.api.internal.EmvL3;
import com.newland.sdk.emvl3.api.internal.listener.CompleteTransactionListener;
import com.newland.sdk.emvl3.api.internal.listener.PerfromTransactionListener;

import java.util.ArrayList;
import java.util.List;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.SensitiveGuard;
import acquire.sdk.device.BDevice;
import acquire.sdk.emv.bean.EmvLaunchParam;
import acquire.sdk.emv.bean.EmvReadyBean;
import acquire.sdk.emv.bean.PinResult;
import acquire.sdk.emv.constant.EmvSensitiveTag;
import acquire.sdk.emv.listener.EmvListener;
import acquire.sdk.emv.listener.EmvSecondGacListener;
import acquire.sdk.led.BLed;


/**
 * Internal EMV processor.It's used to perform EMV process.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *      EmvLaunchParam param = new EmvLaunchParam.Builder(EmvTransType.SALE)
 *                     .entryMode(EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP)
 *                     .amount(1)
 *                     .create();
 *       BEmvProcessor emvProcessor = new BEmvProcessor();
 *       emvProcessor.readCard(param, new EmvListener() {
 *                 public void onReady(EmvReadyBean emvReadyBean) {
 *                 }
 *
 *                 public void onReading() {
 *                 }
 *
 *                 public int onSelectAid(List preferNames){
 *
 *                 }
 *
 *                 public void onFinalSelect() {
 *                 }
 *
 *                 public boolean onSeePhone() {
 *                 }
 *
 *                 public boolean onCardNum(String pan) {
 *                 }
 *
 *                 public PinResult onInputPin(boolean isOnlinePin, int pinTryCount) {
 *
 *                 }
 *
 *                 public void onResult(boolean success, int emvResult) {
 *                 }
 *       ｝;
 * </pre>
 *
 * @author Janson
 * @date 2019/10/21 9:26
 */
public class BEmvProcessor implements IEmvProcessor {

    /**
     * read emv data by tag list
     *
     * @param tags          emv tag list
     * @param isPackZeroLen true if the result includes the tag that its value is null or 0-length.
     * @return reading data list. tag+len+value
     */
    @Override
    public byte[] getListData(List<Integer> tags, boolean isPackZeroLen) {
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        return emvL3.getListData(tags, isPackZeroLen);
    }

    /**
     * read an EMV data
     *
     * @param tag EMV tag
     * @return reading data. tag+len+value
     */
    @Override
    public byte[] getData(int tag) {
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        byte[] value = emvL3.getData(tag);
        if (value != null && value.length>0){
            if (EmvSensitiveTag.SENSITIVE_EMV_TAG.contains(tag)){
                SensitiveGuard.addSensitiveText(BytesUtils.bcdToString(value));
            }else if (EmvSensitiveTag.SENSITIVE_EMV_L3_TAG.contains(tag)){
                SensitiveGuard.addSensitiveText(new String(value));
            }
        }
        //if u want show card data in log data
        SensitiveGuard.setShowSensitiveData(true);
        return value;
    }

    /**
     * set emv data
     *
     * @param tag  EMV tag
     * @param data the value to be set.
     */
    @Override
    public void setData(int tag, byte[] data) {
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        emvL3.setData(tag, data);
    }


    /**
     * Release EMV L3 resource
     */
    @Override
    public void terminateTransaction() {
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        emvL3.terminateTransaction();
    }

    /**
     * cancel emv transaction performation
     */
    @Override
    public void cancelEmv() {
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        emvL3.responseEvent(EmvL3Const.ResponseEvent.CANCEL, null);
    }


    /**
     * start emv process
     *
     * @param launchParam emv parameter
     */
    @Override
    public void readCard(final EmvLaunchParam launchParam, @NonNull final EmvListener emvListener) {
        LoggerUtils.i("[NSDK EmvProcessor]-- Start to read card[" + launchParam + "].");
        //execute EMV process
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        if (BDevice.supportTapLed()&& BDevice.supportRf()){
            BLed.cardLightTurnOn();
        }
        emvL3.performTransaction((byte) launchParam.getEntryMode(), launchParam.getTimeoutSec(), launchParam.getTransData(), new PerfromTransactionListener() {

            @Override
            public void selectCandidateList(ArrayList<Candidate> candidates) {
                LoggerUtils.d("[NSDK EmvProcessor]--Select Aid. Count:" + candidates.size());
                if (candidates.size() == 1){
                    emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, new byte[]{0});
                    return;
                }
                List<String> items = new ArrayList<>();
                for (Candidate candidate : candidates) {
                    items.add(new String(candidate.getPreferName()));
                }
                emvListener.onSelectAid(items, index -> {
                    if (index >= 0){
                        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, new byte[]{(byte) index});
                    }else{
                        emvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                    }
                });

            }

            @Override
            public void onFinalSelect(int cardInterface, byte[] aid) {
                LoggerUtils.d("[NSDK EmvProcessor]-- Final Select Interface: " + cardInterface+",aid:"+BytesUtils.bcdToString(aid));
                //user entry mode
                emvListener.onFinalSelect(() -> emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null));

            }

            @Override
            public void uiEvent(int eventId, byte[] eventData) {
                //ui event, just callback user interface event.
                switch (eventId) {
                    case EmvL3Const.UIEvent.UI_PRESENT_CARD:
                        LoggerUtils.d("[NSDK EmvProcessor]-- UI EVENT(id= " + eventId + "): UI PRESENT CARD.");
                        emvListener.onReady(new EmvReadyBean(launchParam.getEntryMode(), eventData[0]), () -> emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null));
                        break;
                    case EmvL3Const.UIEvent.UI_PROCESSING:
                        LoggerUtils.d("[NSDK EmvProcessor]-- UI EVENT(id= " + eventId + "): PROCESSING.");
                        //callback reading status
                        emvListener.onReading(() -> emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null));
                        break;
                    case EmvL3Const.UIEvent.UI_CHIP_ERR_RETRY:
                        LoggerUtils.d("[NSDK EmvProcessor]-- UI EVENT(id= " + eventId + "): CHIP ERROR and RETRY.");
                        emvListener.onInsertError(result -> {
                            if (result){
                                LoggerUtils.d("[NSDK EmvProcessor]-- CHIP ERROR and RETRY card ok.");
                                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                            }else{
                                LoggerUtils.e("[NSDK EmvProcessor]-- CHIP ERROR and RETRY was cancelled.");
                                emvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                            }
                        });

                        break;
                    case EmvL3Const.UIEvent.UI_PIN_STATUS:
                        LoggerUtils.d("[NSDK EmvProcessor]-- UI EVENT(id= " + eventId + "): PIN STATUS.");
                        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                        break;
                    case EmvL3Const.UIEvent.UI_CAPK_LOAD_FAIL:
                        LoggerUtils.d("[NSDK EmvProcessor]-- UI EVENT(id= " + eventId + "): CAPK LOAD FAILED.");
                        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                        break;
                    case EmvL3Const.UIEvent.UI_SEE_PHONE:
                        LoggerUtils.d("[NSDK EmvProcessor]-- UI EVENT(id= " + eventId + "): UI SEE PHONE.");
                        emvListener.onSeePhone(result -> {
                            if (result) {
                                LoggerUtils.d("[NSDK EmvProcessor]-- SEE PHONE retry card ok.");
                                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                            } else {
                                LoggerUtils.e("[NSDK EmvProcessor]-- SEE PHONE retry card cancel.");
                                emvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                            }
                        });
                        break;
                    default:
                        LoggerUtils.d("[NSDK EmvProcessor]-- UI EVENT(id= " + eventId + "): other.");
                        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                        break;
                }
            }

            @Override
            public void confirmPAN(String pan) {
                //get card number
                SensitiveGuard.addSensitiveText(pan);
                LoggerUtils.d("[NSDK EmvProcessor]-- Card Number: " + pan);
                if (BDevice.supportTapLed() && BDevice.supportRf()){
                    BLed.cardLightTurnOff();
                }
                emvListener.onCardNum(pan, result -> {
                    if (result) {
                        LoggerUtils.d("[NSDK EmvProcessor]-- confirm card number success.");
                        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                    } else {
                        LoggerUtils.e("[NSDK EmvProcessor]-- confirm card number cancel.");
                        emvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                    }
                });
            }

            @Override
            public void selectTrack(byte[] trackFormat, byte[] trackStatus) {

            }

            @Override
            public void checkExceptionList(String pan, int cardSn) {

            }

            @Override
            public void getPIN(int pinType, int pinTryCount, publickey publickey) {
                LoggerUtils.d("[NSDK EmvProcessor]-- Pin Entry(type = " + pinType + ").");
                boolean onlinePin = EmvL3Const.PINType.PIN_ONLINE == pinType;
                if (!onlinePin) {
                    //set the offline public key to EmvProvider,and use it when input offline PIN.
                    EmvProvider.getInstance().setOffPublicKey(publickey);
                }
                emvListener.onInputPin(onlinePin, pinTryCount, pinResult -> {
                    switch (pinResult.getResult()) {
                        case PinResult.CANCEL:
                            LoggerUtils.e("[NSDK EmvProcessor]-- input PIN cancel.");
                            emvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                            break;
                        case PinResult.OK:
                            LoggerUtils.d("[NSDK EmvProcessor]-- input PIN success.");
                            byte[] pinBlock = pinResult.getPinBlock();
                            emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, pinBlock);
                            break;
                        case PinResult.BYPASS:
                        default:
                            LoggerUtils.e("[NSDK EmvProcessor]-- input PIN, by pass.");
                            emvL3.responseEvent(ErrorCode.L3_ERR_BYPASS, new byte[0]);
                            break;
                    }
                });

            }

            @Override
            public void transResult(int resultCode, int errorCode) {
                if (BDevice.supportTapLed()&& BDevice.supportRf()){
                    BLed.cardLightTurnOff();
                }
                switch (resultCode) {
                    case EmvL3Const.TransResult.L3_TXN_OK:
                    case EmvL3Const.TransResult.L3_TXN_APPROVED:
                    case EmvL3Const.TransResult.L3_TXN_ONLINE:
                        //result code ok
                        LoggerUtils.d("[NSDK EmvProcessor]-- Emv Success(result code = " + resultCode + ", error code = " + errorCode + ").");
                        emvListener.onResult(true, resultCode);
                        break;
                    default:
                        //result code failed
                        LoggerUtils.e("[NSDK EmvProcessor]-- Emv Rejection(result code = " + resultCode + ", error code = " + errorCode + ").");
                        emvListener.onResult(false, resultCode);
                        break;
                }
            }

            @Override
            public void getManualData() {
                //Temporarily useless
                LoggerUtils.d("[NSDK EmvProcessor]-- Get Manual Data");
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }

            @Override
            public void selectAccount() {
                LoggerUtils.d("[NSDK EmvProcessor]-- Select Account");
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }

            @Override
            public void selectLanguage(byte[] language) {
                LoggerUtils.d("[NSDK EmvProcessor]-- Select Language:" + new String(language));
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }

            @Override
            public void checkCredentials(byte type, byte[] number) {
                LoggerUtils.d("[NSDK EmvProcessor]-- Check Credentials(type = " + type + ").");
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }

            @Override
            public void dek_det(int type, byte[] data) {
                LoggerUtils.d("[NSDK EmvProcessor]-- Dek_Det(type = " + type + ").");
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }

            @Override
            public void getApduData(int cardInterface, byte[] reqdata, int reqdatalen, byte[] resdata, int resdatalen) {
                //Temporarily useless
                LoggerUtils.d("[NSDK EmvProcessor]-- Get Apdu Data.");
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }
        });
    }

    /**
     * import online response to emv l3
     */
    @Override
    public void secondGac(boolean online, byte[] gacData, @NonNull EmvSecondGacListener emvSecondGacListener) {
        LoggerUtils.i("[NSDK EmvProcessor]-- Second Gac.");
        LoggerUtils.d("[NSDK EmvProcessor]-- Gac params: " + BytesUtils.bcdToString(gacData));
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        emvL3.completeTransaction(online, gacData, new CompleteTransactionListener() {
            @Override
            public void voiceReferrals() {
                LoggerUtils.d("[NSDK EmvProcessor]-- Second Gac Voice Referrals.");
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }

            @Override
            public void getApduData(int cardInterface, byte[] reqdata, int reqdatalen, byte[] resdata, int resdatalen) {
                emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
            }

            @Override
            public void uiEvent(int eventId, byte[] eventData) {
                switch (eventId) {
                    // The Reader is processing the transaction
                    case EmvL3Const.UIEvent.UI_PROCESSING:
                        LoggerUtils.d("[NSDK EmvProcessor]-- Second Gac,UI EVENT(id= " + eventId + "): UI PROCESSING.Need retry card");
                        emvSecondGacListener.recard();
                        break;
                    default:
                        LoggerUtils.d("[NSDK EmvProcessor]-- Second Gac,UI EVENT(id= " + eventId + "): other.");
                        emvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                        break;
                }
            }

            @Override
            public void transResult(int resultCode, int errorCode) {
                switch (resultCode) {
                    case EmvL3Const.TransResult.L3_TXN_OK:
                    case EmvL3Const.TransResult.L3_TXN_APPROVED:
                    case EmvL3Const.TransResult.L3_TXN_ONLINE:
                        //second GAC result code ok
                        LoggerUtils.d("[NSDK EmvProcessor]-- Second Gac Success(result code = " + resultCode + ", error code = " + errorCode + ").");
                        emvSecondGacListener.completeResult(true);
                        break;
                    default:
                        //second GAC result code failed
                        LoggerUtils.e("[NSDK EmvProcessor]-- Second Gac Failed(result code =" + resultCode + ", error code = " + errorCode + ").");
                        emvSecondGacListener.completeResult(false);
                        break;
                }
            }
        });
    }

    @Override
    public String getEmvApiVersion() {
        EmvL3 emvL3 = EmvProvider.getInstance().getEmvL3();
        return emvL3.getVersion(EmvL3Const.MODULE.L3_MODULE_API);
    }
}
