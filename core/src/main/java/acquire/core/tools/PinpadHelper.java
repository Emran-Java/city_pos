package acquire.core.tools;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Map;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.core.constant.ParamsConst;
import acquire.sdk.ExtServiceHelper;
import acquire.sdk.pin.BExternalPinpad;
import acquire.sdk.pin.BPinpad;
import acquire.sdk.pin.constant.KeyAlgorithmType;
import acquire.sdk.pin.constant.MacMode;
import acquire.sdk.pin.constant.PinKeyCode;
import acquire.sdk.pin.constant.WorkKeyType;
import acquire.sdk.pin.listener.PinpadListener;
import acquire.sdk.pin.listener.RnibPinpadListener;

/**
 * PIN pad helper utils
 *
 * @author Janson
 * @date 2021/11/26 9:34
 */
public class PinpadHelper {
    /**
     * Index of Master key
     */
    private final int masterIndex;

    /**
     * Key algorithm.
     * @see KeyAlgorithmType
     */
    private final int algorithmType;
    /**
     * External PIN pad flag
     */
    private final boolean mIsExternal;

    private BPinpad pinpad;

    private BExternalPinpad externalPinpad;



    public PinpadHelper(int masterIndex,int algorithmType) {
        mIsExternal = ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_PINPAD_EXTERNAL)
                && ExtServiceHelper.getInstance().isInit();
        if (mIsExternal) {
            LoggerUtils.d("external PIN pad");
            externalPinpad = new BExternalPinpad();
        } else {
            LoggerUtils.d("built-in PIN pad");
            pinpad = new BPinpad();
        }
        this.masterIndex = masterIndex;
        LoggerUtils.d("current master key index: " + this.masterIndex);
        this.algorithmType = algorithmType;
    }

    /***
     * Set layout of PIN Pad,only the built-in PIN pad is valid.
     *
     * @param buttons      The map's key is {@link PinKeyCode}, value is button location(left top and right bottom).
     * @param isRandomLayout true if the key layout is placed randomly.
     * @return 10 number values that match the 10 number button. This can be used to display keyboard.
     */
    public byte[] setPinpadLayout(Map<String,int[]> buttons, boolean isRandomLayout) {
        return pinpad.setPinpadLayout(buttons, isRandomLayout);
    }
    /***
     * Set layout of Rnib PIN Pad,only the built-in PIN pad is valid.
     *
     * @param buttons      The map's key is {@link PinKeyCode}, value is button location(left top and right bottom).
     */
    public void setRnibPinpadLayout(Map<String,int[]> buttons) {
         pinpad.setRnibPinpadLayout(buttons);
    }

    /**
     * input PIN
     *
     * @param isOnline       if ture, online PIN，otherwise offline PIN.
     * @param pan            card number
     * @param supportPinLens only PIN lengths within the range of this array are allowed. e.g. {0,4,6,12} => support no pin,4,6,12 pin bytes.
     * @param pinpadListener PIN input listener
     */
    public void startPinInput(boolean isOnline, @NonNull String pan, @NonNull byte[] supportPinLens, final PinpadListener pinpadListener) {
        if (algorithmType == KeyAlgorithmType.DUKPT) {
            waitKsn();
        }
        int pinIndex = masterIndex;
        int timeoutSec = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_TIMEOUT, 60);
        if (mIsExternal) {
            byte maxLen = 0;
            for (byte supportPinLen : supportPinLens) {
                maxLen = (byte) Math.max(maxLen, supportPinLen);
            }
            externalPinpad.startPinInput(algorithmType, isOnline, pinIndex, pan, maxLen, timeoutSec, pinpadListener);
        } else {
            pinpad.startPinInput(algorithmType, isOnline, pinIndex, pan, supportPinLens, timeoutSec, pinpadListener);
        }
    }

    /**
     * input Rnib PIN
     *
     * @param isOnline       if ture, online PIN，otherwise offline PIN.
     * @param pan            card number
     * @param supportPinLens only PIN lengths within the range of this array are allowed. e.g. {0,4,6,12} => support no pin,4,6,12 pin bytes.
     * @param pinpadListener PIN input listener
     */
    public void startRnibPinInput(boolean isOnline, @NonNull String pan, @NonNull byte[] supportPinLens, final RnibPinpadListener pinpadListener) {
        if (algorithmType == KeyAlgorithmType.DUKPT) {
            waitKsn();
        }
        int pinIndex = masterIndex;
        int timeoutSec = ParamsUtils.getInt(ParamsConst.PARAMS_KEY_PINPAD_ACCESSIBILITY_TIMEOUT, 120);
        pinpad.startRnibPinInput(algorithmType, isOnline, pinIndex, pan, supportPinLens, timeoutSec, pinpadListener);
    }

    /**
     * cancel PIN input
     */
    public boolean cancelPinInput() {
        if (mIsExternal) {
            return externalPinpad.cancelPinInput();
        } else {
            return pinpad.cancelPinInput();
        }
    }

    /**
     * load master key.
     *
     * @param key the master key to be loaded.
     */
    public boolean loadMkskMasterKey(byte[] key) {
        if (mIsExternal) {
            return externalPinpad.loadMkskMasterKey(masterIndex, key);
        } else {
            return pinpad.loadMkskMasterKey(masterIndex, key);
        }
    }


    /**
     * load mksk work key.
     *
     * @param workKeyType Work key type.
     * @param key         work key (cipher data)
     * @param kcv         check value
     */
    public boolean loadMkskWorkKey(WorkKeyType workKeyType, byte[] key, @Nullable byte[] kcv) {
        int workIndex = masterIndex;
        if (mIsExternal) {
            return externalPinpad.loadMkskWorkKey(masterIndex, workKeyType, workIndex, key, kcv);
        } else {
            return pinpad.loadBracMkskWorkKey(masterIndex, workIndex, key);
        }
    }

    public boolean loadBracMkskWorkKey(int workIndex, byte[] key) {
        //int workIndex = masterIndex;
        /*if (mIsExternal) {
            return externalPinpad.loadMkskWorkKey(masterIndex, workKeyType, workIndex, key, kcv);
        } else {
            return pinpad.loadBracMkskWorkKey(masterIndex, workIndex, key);
        }*/
        return pinpad.loadBracMkskWorkKey(masterIndex, workIndex, key);
    }

    /**
     * encrypt data by data key
     *
     * @param plainData plain text.
     * @return cipher text
     */
    public byte[] encryptData(byte[] plainData) {
        if (algorithmType == KeyAlgorithmType.DUKPT) {
            waitKsn();
        }
        if (plainData.length == 0) {
            return null;
        }
        int dataIndex = masterIndex;
        byte[] cipherData;
        if (mIsExternal) {
            cipherData = externalPinpad.encryptData(plainData, dataIndex, algorithmType);
        } else {
            cipherData = pinpad.encryptData(plainData, dataIndex, algorithmType);
        }
        return cipherData;
    }

    /**
     * calculate mac
     *
     * @param src source data
     * @return hexadecimal mac value
     */
    public String getMac(String src) {
        if (KeyAlgorithmType.DUKPT == algorithmType) {
            waitKsn();
        }
        int macIndex = masterIndex;
        byte[] srcBytes = BytesUtils.hexToBytes(src);
        if (srcBytes == null || srcBytes.length == 0) {
            return null;
        }
        byte[] mac;
        if (mIsExternal) {
            mac = externalPinpad.getMac(srcBytes, algorithmType, MacMode.TYPE_X919, macIndex);
        } else {
            mac = pinpad.getMac(srcBytes, algorithmType, MacMode.TYPE_X919, macIndex);
        }
        String result = BytesUtils.bcdToString(mac);
        if (!TextUtils.isEmpty(result) && result.length() > 16) {
            result = result.substring(0, 16);
        }
        return result;
    }

    /**
     * increase ksn
     *
     * @return true -succ, false -failed
     */
    public boolean increaseKsn() {
        if (algorithmType != KeyAlgorithmType.DUKPT) {
            return true;
        }
        if (mIsExternal) {
            return externalPinpad.increaseKsn((byte) masterIndex);
        } else {
            return pinpad.increaseKsn((byte) masterIndex);
        }
    }

    /**
     * increase ksn in a thread
     */
    public void asyncIncreaseKsn() {
        ThreadPool.execute(() -> {
            increasing = true;
            increaseKsn();
            increasing = false;
        });
    }

    public static boolean increasing;

    /**
     * wait the {@link #asyncIncreaseKsn} result.
     */
    public static void waitKsn() {
        if (!increasing) {
            return;
        }
        long start = System.currentTimeMillis();
        while (increasing) {
            if (System.currentTimeMillis() - start > 1000) {
                increasing = false;
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    /**
     * Get ksn.
     *
     * @return ksn string.
     */
    public String getKsn() {
        waitKsn();
        byte[] ksn;
        if (mIsExternal) {
            ksn = externalPinpad.getKsn(masterIndex);
        } else {
            ksn = pinpad.getBracKCV(masterIndex);
        }
        return BytesUtils.bcdToString(ksn);
    }
}
