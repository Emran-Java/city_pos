package acquire.sdk.nonbankcard;

import com.newland.nsdk.rfic.RFICActiveCallback;
import com.newland.nsdk.rfic.RFICCardException;
import com.newland.nsdk.rfic.RFICCardReader;
import com.newland.nsdk.rfic.card.RFICCard;
import com.newland.nsdk.rfic.enums.DebugStatus;
import com.newland.nsdk.rfic.enums.RFICCardInfoKey;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.Locker;
import acquire.base.utils.thread.ThreadPool;
import acquire.sdk.nonbankcard.contactless.MifareClassicReader;
import acquire.sdk.nonbankcard.contactless.NtagReader;

/**
 * A contactless card reader basic class.
 *
 * @author Janson
 * @date 2022/11/22 15:34
 * @see MifareClassicReader
 * @see NtagReader
 * @since 3.7
 */
public abstract class BasicContactlessReader<T extends RFICCard> {
    public final static int CONNECT_SUCCESS = 0, CONNECT_ERROR = -1, CONNECT_TIMEOUT = -2, CONNECT_CANCEL = -3, CONNECT_UNSUPPORT = -4;
    protected T rfCard;
    private boolean isActivated;

    protected abstract Class<T> getRfCardClass();

    public static void openLog(){
        //open log
        try {
            RFICCardReader.getInstance().setDebugLogStatus(DebugStatus.DEBUG_ON);
        } catch (RFICCardException e) {
            LoggerUtils.e("BasicContactlessReader openLog failed!",e);
        }
    }
    public static String getRfidVersion(){
        return RFICCardReader.getInstance().getRFICVersion();
    }
    /**
     * Enable I/O operations to the card from this {@link BasicContactlessReader} object..
     * <p>
     * May cause RF activity and may block.
     * Must not be called from the main application thread.
     * A blocked call will be canceled and return {@link #CONNECT_CANCEL} by calling close from another thread.
     * </p>
     * <p>Only one BasicContactlessReader object can be connected to a Card at a time.</p>
     * <p>Applications must call {@link #close()} when I/O operations are complete.</p>
     *
     * @param timeoutSec the connection time out in seconds. 0 means no timeout.
     * @return {@link #CONNECT_SUCCESS} on success, others on connection failure.
     */
    public int connect(int timeoutSec) {
        Locker<Integer> locker = new Locker<>();
        ThreadPool.execute(() ->
                RFICCardReader.getInstance().active(timeoutSec, null, new RFICActiveCallback() {
                    @Override
                    public void onError(int code, String message) {
                        LoggerUtils.e("Connection error. Error code:" + code + ", message:" + message);
                        locker.setResult(CONNECT_ERROR);
                        locker.wakeUp();
                    }

                    @Override
                    public void onTimeout() {
                        locker.setResult(CONNECT_TIMEOUT);
                        locker.wakeUp();
                    }

                    @Override
                    public void onCancel() {
                        locker.setResult(CONNECT_CANCEL);
                        locker.wakeUp();
                    }

                    @Override
                    public void onSuccess(RFICCard card) {
                        isActivated = true;
                        Class<T> clz = getRfCardClass();
                        if (card.getClass() == clz) {
                            rfCard = (T) card;
                            locker.setResult(CONNECT_SUCCESS);
                            locker.wakeUp();
                        } else {
                            close();
                            locker.setResult(CONNECT_UNSUPPORT);
                            locker.wakeUp();
                        }
                    }
                })
        );
        locker.waiting();
        return locker.getResult();
    }

    /**
     * Disable I/O operations to the card from this {@link BasicContactlessReader} object.
     * <p>If the {@link #connect(int)} is being executing, {@link #connect(int)} will return {@link #CONNECT_CANCEL}.
     * If blocked I/O operations are executing on other thread, they will be canceled and return with failed result.
     * </p>
     *
     * @return true on success, false on I/O exception.
     */
    public boolean close() {
        if (isActivated) {
            try {
                RFICCardReader.getInstance().deactive();
                isActivated = false;
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("BasicContactlessReader close failed!",e);
                return false;
            }
        } else {
            return true;
        }
    }
    public boolean cancelActivation() {
        if (!isActivated) {
            try {
                RFICCardReader.getInstance().cancelActivation();
                return true;
            } catch (RFICCardException e) {
                LoggerUtils.e("BasicContactlessReader cancelActivation failed!",e);
                return false;
            }
        }else{
            return true;
        }
    }

    public byte[] getUid() {
        if (rfCard != null) {
            return rfCard.getUID();
        }
        return null;
    }


    public byte[] getAtqa() {
        if (rfCard != null) {
            return rfCard.getATQA();
        }
        return null;
    }


    public byte getSak() {
        if (rfCard != null) {
            return rfCard.getSAK();
        }
        return -1;
    }

    public byte[] getManufacture() {
        if (rfCard != null) {
            try {
                return rfCard.getCardInfo(RFICCardInfoKey.MANUFACTURE);
            } catch (RFICCardException e) {
                LoggerUtils.e("BasicContactlessReader getManufacture(RFICCardInfoKey.MANUFACTURE) failed!",e);
            }
        }
        return null;
    }

    public byte[] getCardType() {
        if (rfCard != null) {
            try {
                return rfCard.getCardInfo(RFICCardInfoKey.CARD_TYPE);
            } catch (RFICCardException e) {
                LoggerUtils.e("BasicContactlessReader getCardType(RFICCardInfoKey.CARD_TYPE) failed!",e);
            }
        }
        return null;
    }

    public byte[] getSecurityLevel() {
        if (rfCard != null) {
            try {
                return rfCard.getCardInfo(RFICCardInfoKey.SEC_LEVEL);
            } catch (RFICCardException e) {
                LoggerUtils.e("BasicContactlessReader getSecurityLevel(RFICCardInfoKey.SEC_LEVEL) failed!",e);
            }
        }
        return null;
    }

    public byte[] getMemorySize() {
        if (rfCard != null) {
            try {
                return rfCard.getCardInfo(RFICCardInfoKey.MEM_SIZE);
            } catch (RFICCardException e) {
                LoggerUtils.e("BasicContactlessReader getMemorySize(RFICCardInfoKey.MEM_SIZE) failed!",e);
            }
        }
        return null;
    }

    public byte[] getCardVersion() {
        if (rfCard != null) {
            try {
                return rfCard.getCardInfo(RFICCardInfoKey.CARD_VERSION);
            } catch (RFICCardException e) {
                LoggerUtils.e("BasicContactlessReader getCardVersion(RFICCardInfoKey.CARD_VERSION) failed!",e);
            }
        }
        return null;
    }
}
