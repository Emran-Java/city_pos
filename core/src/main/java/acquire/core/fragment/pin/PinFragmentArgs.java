package acquire.core.fragment.pin;

import java.io.Serializable;

import acquire.sdk.pin.constant.KeyAlgorithmType;

/**
 * The PIN pad fragment arguments
 *
 * @author Janson
 * @date 2021/6/25 9:12
 */
public class PinFragmentArgs implements Serializable {
    /**
     * Index of Master key
     */
    private int masterIndex;

    /**
     * Key algorithm.
     * @see KeyAlgorithmType
     */
    private int algorithm;
    private String pan;
    private String currencyCode;
    private long amount;
    private String cardScheme;
    private boolean isOnlinePin;
    private boolean accessPinPad;
    /**
     * Supported pin length
     */
    private byte[] supportPinLengths;

    public void setPan(String pan) {
        this.pan = pan;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public void setCardScheme(String cardScheme) {
        this.cardScheme = cardScheme;
    }

    public void setOnlinePin(boolean onlinePin) {
        isOnlinePin = onlinePin;
    }

    public void setSupportPinLengths(byte... supportPinLengths) {
        this.supportPinLengths = supportPinLengths;
    }

    public String getPan() {
        return pan;
    }

    public long getAmount() {
        return amount;
    }

    public String getCardScheme() {
        return cardScheme;
    }

    public boolean isOnlinePin() {
        return isOnlinePin;
    }

    public byte[] getSupportPinLengths() {
        return supportPinLengths;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public int getMasterIndex() {
        return masterIndex;
    }

    public void setMasterIndex(int masterIndex) {
        this.masterIndex = masterIndex;
    }

    public int getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(int algorithm) {
        this.algorithm = algorithm;
    }

    public boolean isAccessPinPad() {
        return accessPinPad;
    }

    public void setAccessPinPad(boolean accessPinPad) {
        this.accessPinPad = accessPinPad;
    }
}
