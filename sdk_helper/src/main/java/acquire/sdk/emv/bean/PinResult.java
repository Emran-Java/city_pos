package acquire.sdk.emv.bean;

import acquire.sdk.pin.constant.KeyAlgorithmType;

/**
 * Input PIN result
 *
 * @author Janson
 * @date 2022/3/23 14:01
 */
public class PinResult {
    public final static int CANCEL = -1, OK = 0, BYPASS= 1, EXT_PINPAD = 2;
    private int result;
    /**
     * PIN block
     */
    private byte[] pinBlock;

    /**
     * external PIN pad arguments ↓
     */
    private int extPinIndex;
    /**
     * see {@link KeyAlgorithmType}
     */
    private int extKeyAlgorithm;
    private int extTimeoutSec;


    public static PinResult newStatusCancel(){
        PinResult pinResult = new PinResult();
        pinResult.result = CANCEL;
        return pinResult;
    }
    public static PinResult newStatusOk(byte[] pinblock){
        PinResult pinResult = new PinResult();
        pinResult.result = OK;
        pinResult.pinBlock = pinblock;
        return pinResult;
    }
    public static PinResult newStatusByPass(){
        PinResult pinResult = new PinResult();
        pinResult.result = BYPASS;
        return pinResult;
    }
    public static PinResult newStatusExtPinpad(int extPinIndex, int extKeyAlgorithm,int extTimeoutSec){
        PinResult pinResult = new PinResult();
        pinResult.result = EXT_PINPAD;
        pinResult.extPinIndex = extPinIndex;
        pinResult.extKeyAlgorithm = extKeyAlgorithm;
        pinResult.extTimeoutSec = extTimeoutSec;
        return pinResult;
    }

    private PinResult(){}

    public int getResult() {
        return result;
    }

    public byte[] getPinBlock() {
        return pinBlock;
    }

    public int getExtPinIndex() {
        return extPinIndex;
    }

    public int getExtKeyAlgorithm() {
        return extKeyAlgorithm;
    }

    public int getExtTimeoutSec() {
        return extTimeoutSec;
    }
}
