package acquire.sdk.emv;

import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.sdk.emvl3.api.common.EmvL3Const;
import com.newland.sdk.emvl3.api.common.ErrorCode;
import com.newland.sdk.emvl3.api.common.listener.Candidate;
import com.newland.sdk.emvl3.api.external.ExtEMVL3;
import com.newland.sdk.emvl3.api.external.TLVResult;
import com.newland.sdk.emvl3.api.external.listener.ExtCompleteTransactionListener;
import com.newland.sdk.emvl3.api.external.listener.ExtPerformTransactionListener;
import com.newland.sdk.emvl3.api.external.listener.ExtTransactionResult;

import java.util.ArrayList;
import java.util.List;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.SensitiveGuard;
import acquire.base.utils.TlvUtils;
import acquire.sdk.emv.bean.EmvLaunchParam;
import acquire.sdk.emv.bean.EmvReadyBean;
import acquire.sdk.emv.bean.PinResult;
import acquire.sdk.emv.constant.EmvSensitiveTag;
import acquire.sdk.emv.listener.EmvListener;
import acquire.sdk.emv.listener.EmvSecondGacListener;
import acquire.sdk.pin.constant.KeyAlgorithmType;


/**
 * External EMV processIt's used to perform EMV process.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *       EmvLaunchParam param = new EmvLaunchParam.Builder(EmvTransType.SALE)
 *                      .entryMode(EntryMode.MAG|EntryMode.INSERT|EntryMode.TAP)
 *                      .amount(1)
 *                      .create();
 *        BExtEmvProcessor emvProcessor = new BExtEmvProcessor();
 *        emvProcessor.readCard(param, new EmvListener() {
 *                  public void onReady(EmvReadyBean emvReadyBean) {
 *                  }
 *
 *                  public void onReading() {
 *                  }
 *
 *                  public int onSelectAid(List preferNames){
 *
 *                  }
 *                  public void onFinalSelect() {
 *
 *                  }
 *                  public boolean onSeePhone() {
 *
 *                  }
 *                  public boolean onCardNum(String pan) {
 *
 *                  }
 *                  public PinResult onInputPin(boolean isOnlinePin, int pinTryCount) {
 *
 *                  }
 *                  public void onResult(boolean success, int emvResult) {
 *
 *                  }
 *        ｝;
 *  </pre>
 *
 * @author Janson
 * @date 2021/11/5 16:57
 */
public class BExtEmvProcessor implements IEmvProcessor {
    /**
     * read emv data by tag list
     *
     * @param tags          emv tag list
     * @param isPackZeroLen true if the result includes the tag that its value is null or 0-length.
     * @return reading data list. tag+len+value
     */
    @Override
    public byte[] getListData(List<Integer> tags, boolean isPackZeroLen) {
        try {
            //0: not encrypt
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            TLVResult result = extEmvL3.getListData((byte) 0, tags, isPackZeroLen);
            return result.getData();
        } catch (Exception e) {
            LoggerUtils.e("Ext getListData failed!",e);
            return null;
        }

    }

    /**
     * read an EMV data
     *
     * @param tag EMV tag.
     * @return EMV data.
     */
    @Override
    public byte[] getData(int tag) {
        try {
            //work key index 0
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            TLVResult data = extEmvL3.getData((byte) 0, tag);
            byte[] value= data.getData();
            if (value != null && value.length>0){
                if (EmvSensitiveTag.SENSITIVE_EMV_TAG.contains(tag)){
                    SensitiveGuard.addSensitiveText(BytesUtils.bcdToString(value));
                }else if (EmvSensitiveTag.SENSITIVE_EMV_L3_TAG.contains(tag)){
                    SensitiveGuard.addSensitiveText(new String(value));
                }
            }
            return value;
        } catch (Exception e) {
            LoggerUtils.e("Ext getData failed!",e);
            return null;
        }
    }

    /**
     * set EMV data
     */
    @Override
    public void setData(int tag, byte[] data) {
        try {
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            extEmvL3.setData(tag, data);
        } catch (Exception e) {
            LoggerUtils.e("Ext setData failed!",e);
        }
    }


    /**
     * Release EMV L3 resource
     */
    @Override
    public void terminateTransaction() {
        try {
            //message null, timeout 0 second
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            extEmvL3.terminateTransaction(null, 0);
        } catch (Exception e) {
            LoggerUtils.e("Ext terminateTransaction failed!",e);
        }
    }

    /**
     * cancel emv transaction performation
     */
    @Override
    public void cancelEmv() {
        try {
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            extEmvL3.responseEvent(EmvL3Const.ResponseEvent.CANCEL, null);
        } catch (Exception e) {
            LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[EmvL3Const.ResponseEvent.CANCEL] failed!",e);
        }
    }


    /**
     * start emv process
     *
     * @param launchParam emv parameter
     */
    @Override
    public void readCard(final EmvLaunchParam launchParam, final EmvListener emvListener) {
        LoggerUtils.i("[NSDK ExtEmvProcessor]-- Start to read card["+launchParam+"].");
        //execute L3 process
        try {
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            extEmvL3.performTransaction((byte)launchParam.getEntryMode(),  launchParam.getTimeoutSec(), launchParam.getTransData(), new ExtPerformTransactionListener() {

                @Override
                public void onCandidateAIDList(ArrayList<Candidate> candidates) {
                    LoggerUtils.d("[NSDK ExtEmvProcessor]--Select Aid. Count:" + candidates.size());
                    if (candidates.size() == 1){
                        try {
                            extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, new byte[]{0});
                        } catch (NSDKException e) {
                            LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent ErrorCode.L3_ERR_SUCC failed!",e);
                        }
                        return;
                    }
                    List<String> items = new ArrayList<>();
                    for (Candidate candidate : candidates) {
                        items.add(new String(candidate.getPreferName()));
                    }
                    emvListener.onSelectAid(items, index -> {
                        if (index >= 0){
                            try {
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, new byte[]{(byte) index});
                            } catch (NSDKException e) {
                                LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC "+index+"] failed!",e);
                            }
                        }else{
                            try {
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                            } catch (NSDKException e) {
                                LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_CANCEL] failed!",e);
                            }
                        }
                    });

                }

                @Override
                public void onFinalSelect(byte cardInterface, byte[] aid) {
                    LoggerUtils.d("[NSDK ExtEmvProcessor]-- Final Select Interface: " + cardInterface+",aid:"+BytesUtils.bcdToString(aid));
                    emvListener.onFinalSelect(() -> {
                        try {
                            extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                        } catch (NSDKException e) {
                            LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                        }
                    });

                }

                @Override
                public void uiEvent(int eventId, byte[] eventData) {
                    switch (eventId) {
                        case EmvL3Const.UIEvent.UI_PRESENT_CARD:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- UI EVENT(id = "+eventId+"): UI PRESENT CARD.");
                            emvListener.onReady(new EmvReadyBean(launchParam.getEntryMode(), eventData[0]), () -> {
                                try {
                                    extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                                } catch (NSDKException e) {
                                    LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                                }
                            });
                            break;
                        case EmvL3Const.UIEvent.UI_PROCESSING:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- UI EVENT(id = "+eventId+"): UI PROCESSING.");
                            emvListener.onReading(() -> {
                                try {
                                    extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                                } catch (NSDKException e) {
                                    LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                                }
                            });

                            break;
                        case EmvL3Const.UIEvent.UI_CHIP_ERR_RETRY:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- UI EVENT(id = "+eventId+"): UI CHIP ERROR and RETRY.");
                            emvListener.onInsertError(result -> {
                                if (result){
                                    LoggerUtils.d("[NSDK ExtEmvProcessor]-- CHIP ERROR and RETRY card ok.");
                                    try {
                                        extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                                    } catch (NSDKException e) {
                                        LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                                    }
                                }else{
                                    LoggerUtils.e("[NSDK ExtEmvProcessor]-- CHIP ERROR and RETRY was cancelled.");
                                    try {
                                        extEmvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                                    } catch (NSDKException e) {
                                        LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_CANCEL] failed!",e);
                                    }
                                }
                            });
                            break;
                        case EmvL3Const.UIEvent.UI_SEE_PHONE:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- UI EVENT(id = "+eventId+"): UI SEE PHONE.");
                            emvListener.onSeePhone(result -> {
                                try {
                                    if (result) {
                                        LoggerUtils.d("[NSDK ExtEmvProcessor]-- SEE PHONE retry card ok.");
                                        extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                                    } else {
                                        LoggerUtils.e("[NSDK ExtEmvProcessor]-- SEE PHONE retry card cancel.");
                                        extEmvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                                    }
                                } catch (NSDKException e) {
                                    LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC/ErrorCode.L3_ERR_CANCEL] failed!",e);
                                }
                            });
                            break;
                        case EmvL3Const.UIEvent.UI_PIN_STATUS:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- UI EVENT(id = "+eventId+"): PIN STATUS.");
                            try {
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                            } catch (NSDKException e) {
                                LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                            }
                            break;
                        case EmvL3Const.UIEvent.UI_CAPK_LOAD_FAIL:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- UI EVENT(id = "+eventId+"): CAPK LOAD FAILED.");
                            try {
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                            } catch (NSDKException e) {
                                LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                            }
                            break;
                        default:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- UI EVENT(id = "+eventId+"): other.");
                            try {
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                            } catch (NSDKException e) {
                                LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                            }
                            break;
                    }
                }

                @Override
                public void onCardNumberConfirm(String pan) {
                    if(!pan.matches("[0-9]*")){
                        //It's a hidden pan,so re-get pan.
                        try {
                            TLVResult data = extEmvL3.getData((byte) 0, EmvL3Const.L3_DATA.PAN);
                            pan = new String(data.getData());
                        } catch (NSDKException e) {
                            LoggerUtils.e("[NSDK ExtEmvProcessor]--getData[EmvL3Const.L3_DATA.PAN] failed!",e);
                        }
                    }
                    SensitiveGuard.addSensitiveText(pan);
                    LoggerUtils.d("[NSDK ExtEmvProcessor]-- Card Number: " + pan);
                    emvListener.onCardNum(pan, result -> {
                        try {
                            if (result){
                                LoggerUtils.d("[NSDK ExtEmvProcessor]-- confirm card number success.");
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                            }else{
                                LoggerUtils.e("[NSDK ExtEmvProcessor]-- confirm card number cancel.");
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                            }
                        }catch (NSDKException e){
                            LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC/ErrorCode.L3_ERR_CANCEL] failed!",e);
                        }
                    });

                }

                @Override
                public void onPinEntry(byte pinType, byte[] tlvData) {
                    LoggerUtils.d("[NSDK ExtEmvProcessor]-- PIN Entry(type = "+pinType+")");
                    boolean onlinePin = 0 == pinType;
                    emvListener.onInputPin(onlinePin, 0, pinResult -> {
                        try {
                            switch (pinResult.getResult()){
                                case PinResult.CANCEL:
                                    LoggerUtils.e("[NSDK ExtEmvProcessor]-- input PIN cancel.");
                                    extEmvL3.responseEvent(ErrorCode.L3_ERR_CANCEL, null);
                                    break;
                                case PinResult.OK:
                                    LoggerUtils.d("[NSDK ExtEmvProcessor]-- input PIN success.");
                                    byte[] pinBlock = pinResult.getPinBlock();
                                    extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, pinBlock);
                                    break;
                                case PinResult.EXT_PINPAD:
                                    LoggerUtils.d("[NSDK ExtEmvProcessor]-- call PIN pad to start PIN.");
                                    TlvUtils.PackTlv packTlv = TlvUtils.newPackTlv();
                                    if (onlinePin){
                                        //1 DUKPT,0 MKSK
                                        if (pinResult.getExtKeyAlgorithm() == KeyAlgorithmType.DUKPT){
                                            packTlv.append(0x1F8136,new byte[]{1});
                                        }else{
                                            packTlv.append(0x1F8136,new byte[]{0});
                                        }
                                        //PIN key index
                                        packTlv.append(0x1F8137,new byte[]{(byte) pinResult.getExtPinIndex()});
                                    }
                                    //timeout
                                    packTlv.append(0x1F8138,new byte[]{(byte) pinResult.getExtTimeoutSec()});
                                    extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, packTlv.pack());
                                    break;
                                case PinResult.BYPASS:
                                default:
                                    LoggerUtils.e("[NSDK ExtEmvProcessor]-- input PIN, by pass.");
                                    extEmvL3.responseEvent(ErrorCode.L3_ERR_BYPASS, new byte[0]);
                                    break;
                            }
                        }catch (NSDKException e){
                            LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent failed!",e);
                        }
                    });
                }

                @Override
                public void onTransResult(ExtTransactionResult extTransactionResult) {
                    int resultCode = extTransactionResult.getResult();
                    int errorCode = extTransactionResult.getErrorCode();
                    switch (resultCode) {
                        case EmvL3Const.TransResult.L3_TXN_OK:
                        case EmvL3Const.TransResult.L3_TXN_APPROVED:
                        case EmvL3Const.TransResult.L3_TXN_ONLINE:
                            //result code ok
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- Emv Success(result code = " + resultCode + ", error code = " + errorCode + ").");
                            emvListener.onResult(true, resultCode);
                            break;
                        default:
                            //result code failed
                            LoggerUtils.e("[NSDK ExtEmvProcessor]-- Emv Rejection(result code = " + resultCode + ", error code = " + errorCode + ").");
                            emvListener.onResult(false, resultCode);
                            break;
                    }
                }

                @Override
                public void onCredentialsCheck(byte type, byte[] number) {
                    LoggerUtils.d("[NSDK ExtEmvProcessor]-- Check Credentials(type = "+type+").");
                    try {
                        extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                    } catch (NSDKException e) {
                        LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                    }
                }

            });
        } catch (NSDKException e) {
            LoggerUtils.e("Ext performTransaction failed!",e);
            emvListener.onResult(false,EmvL3Const.TransResult.L3_TXN_TERMINATE);
        }
    }

    /**
     * import online response to emv l3
     */
    @Override
    public void secondGac(boolean online,byte[] gacData, EmvSecondGacListener emvSecondGacListener) {
        LoggerUtils.i("[NSDK ExtEmvProcessor]-- Second Gac.");
        LoggerUtils.d("[NSDK ExtEmvProcessor]-- Gac params: " + BytesUtils.bcdToString(gacData));
        try {
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            extEmvL3.completeTransaction(online, gacData, new ExtCompleteTransactionListener() {
                @Override
                public void uiEvent(int eventId, byte[] eventData) {
                    switch (eventId) {
                        // The Reader is processing the transaction
                        case EmvL3Const.UIEvent.UI_PROCESSING:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- Second Gac,UI EVENT(id = "+eventId+"): UI PROCESSING.Need retry card");
                            emvSecondGacListener.recard();
                            break;
                        default:
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- Second Gac,UI EVENT(id = "+eventId+"): other.");
                            try {
                                extEmvL3.responseEvent(ErrorCode.L3_ERR_SUCC, null);
                            } catch (NSDKException e) {
                                LoggerUtils.e("[NSDK ExtEmvProcessor]--responseEvent[ErrorCode.L3_ERR_SUCC] failed!",e);
                            }
                            break;
                    }
                }

                @Override
                public void onTransResult(ExtTransactionResult extTransactionResult) {
                    int resultCode = extTransactionResult.getResult();
                    int errorCode = extTransactionResult.getErrorCode();
                    switch (resultCode) {
                        case EmvL3Const.TransResult.L3_TXN_OK:
                        case EmvL3Const.TransResult.L3_TXN_APPROVED:
                        case EmvL3Const.TransResult.L3_TXN_ONLINE:
                            //second GAC result code ok
                            LoggerUtils.d("[NSDK ExtEmvProcessor]-- Second Gac Success(result code = "+resultCode+", error code = "+errorCode+").");
                            emvSecondGacListener.completeResult(true);
                            break;
                        default:
                            //second GAC result code failed
                            LoggerUtils.e("[NSDK ExtEmvProcessor]-- Second Gac Failed(result code ="+resultCode+", error code = "+errorCode+").");
                            emvSecondGacListener.completeResult(false);
                            break;
                    }
                }
            });
        } catch (Exception e) {
            LoggerUtils.e("Ext completeTransaction failed!",e);
            emvSecondGacListener.completeResult(false);
        }
    }


    @Override
    public String getEmvApiVersion() {
        try {
            ExtEMVL3 extEmvL3 = EmvProvider.getInstance().getExtEmvL3();
            return extEmvL3.getVersion(EmvL3Const.MODULE.L3_MODULE_API);
        } catch (Exception e) {
            LoggerUtils.e("Ext getVersion(EmvL3Const.MODULE.L3_MODULE_API) failed!",e);
            return null;
        }
    }
}
