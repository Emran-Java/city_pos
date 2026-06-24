package acquire.sdk.pin;

import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.crypto.CipherOutput;
import com.newland.nsdk.core.api.common.crypto.CipherType;
import com.newland.nsdk.core.api.common.crypto.KCVMode;
import com.newland.nsdk.core.api.common.crypto.MACOutput;
import com.newland.nsdk.core.api.common.crypto.MACType;
import com.newland.nsdk.core.api.common.crypto.PaddingMode;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.keymanager.DUKPTKey;
import com.newland.nsdk.core.api.common.keymanager.Key;
import com.newland.nsdk.core.api.common.keymanager.KeyGenerateMethod;
import com.newland.nsdk.core.api.common.keymanager.KeyInfoID;
import com.newland.nsdk.core.api.common.keymanager.KeyType;
import com.newland.nsdk.core.api.common.keymanager.KeyUsage;
import com.newland.nsdk.core.api.common.keymanager.SymmetricKey;
import com.newland.nsdk.core.api.common.pinentry.PINBlockMode;
import com.newland.nsdk.core.api.external.crypto.ExtCrypto;
import com.newland.nsdk.core.api.external.keymanager.ExtKeyManager;
import com.newland.nsdk.core.api.external.pinentry.ExtOfflinePINParameters;
import com.newland.nsdk.core.api.external.pinentry.ExtPINEntry;
import com.newland.nsdk.core.api.external.pinentry.ExtPINEntryListener;
import com.newland.nsdk.core.api.external.pinentry.ExtPINEntryParameters;
import com.newland.nsdk.core.api.external.pinentry.ExtPINMaskLine;
import com.newland.nsdk.core.external.ExtNSDKModuleManagerImpl;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.sdk.R;
import acquire.sdk.pin.constant.KeyAlgorithmType;
import acquire.sdk.pin.constant.MacMode;
import acquire.sdk.pin.constant.WorkKeyType;
import acquire.sdk.pin.listener.PinpadListener;

/**
 * External Pin Pad
 * <p><hr><b>Feature</b></p>
 * <pre>
 *     1.Load/Clear key (Development firmware or installed checkSum device);
 *     2.Enter card PIN;
 *     3.Calculate MAC data;
 *     4.Encrypt data by DES;
 *     5.Get/Increase ksn;
 * </pre>
 *
 * @author Janson
 * @date 2021/11/4 9:01
 */
public class BExternalPinpad {

    private final ExtKeyManager mExtKeyManager;
    private final ExtPINEntry mExtPinEntry;
    private final ExtCrypto mExtCrypto;

    public BExternalPinpad() {
        mExtKeyManager = (ExtKeyManager) ExtNSDKModuleManagerImpl.getInstance().getModule(ModuleType.EXT_KEY_MANAGER);
        mExtPinEntry = (ExtPINEntry) ExtNSDKModuleManagerImpl.getInstance().getModule(ModuleType.EXT_PIN_ENTRY);
        mExtCrypto = (ExtCrypto) ExtNSDKModuleManagerImpl.getInstance().getModule(ModuleType.EXT_CRYPTO);
    }

    /**
     * Input PIN pad
     *
     * @param keyAlgorithm   {@link KeyAlgorithmType}
     * @param isOnline       Set the PIN type. True to be online PIN, false to be offline PIN.
     * @param pinKeyIndex    Set the PIN key index used for starting PIN pad
     * @param pan            Set the card number used for encryption.
     * @param maxPinLength   Set the maximum length of PIN.
     * @param timeoutSec     Set starting PIN pad timeout in second units.
     * @param pinpadListener starting PIN pad listener.
     */
    public void startPinInput(int keyAlgorithm, boolean isOnline, int pinKeyIndex, @NonNull String pan, byte maxPinLength, int timeoutSec, final PinpadListener pinpadListener) {
        Key key;
        if (keyAlgorithm == KeyAlgorithmType.MKSK) {
            SymmetricKey symmetricKey = new SymmetricKey();
            symmetricKey.setKeyUsage(KeyUsage.PIN);
            symmetricKey.setKeyID((byte) pinKeyIndex);
            symmetricKey.setKeyType(KeyType.DES);
            key = symmetricKey;
            LoggerUtils.d("[NSDK ExternalPinpad]--start to input " + (isOnline ? "online" : "offline") + " MKSK pin(pin key index = " + pinKeyIndex + ").");
        } else {
            DUKPTKey dukptKey = new DUKPTKey();
            dukptKey.setKeyID((byte) pinKeyIndex);
            dukptKey.setKeyUsage(KeyUsage.DUKPT);
            dukptKey.setKeyType(KeyType.DES);
            key = dukptKey;
            LoggerUtils.d("[NSDK ExternalPinpad]--start to input " + (isOnline ? "online" : "offline") + " DUKPT pin(pin key index = " + pinKeyIndex + ").");
        }

        if (isOnline) {
            ExtPINEntryParameters parameter = new ExtPINEntryParameters();
            parameter.setMaxPINLen(maxPinLength);
            parameter.setDisplayMessages(new String[]{null,null,NSDKModuleManagerImpl.getInstance().getContext().getString(R.string.sdk_helper_pinpad_online_pin)});
            parameter.setMaskLine(ExtPINMaskLine.LINE_3);
            parameter.setPINBlockMode(PINBlockMode.ISO9564_0);
            parameter.setAutoComplete(true);
            try {
                mExtPinEntry.startOnlinePINEntry(key, pan,timeoutSec,  parameter, new ExPinInputListenerIml(pinpadListener));
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK ExternalPinpad]--start pin failed.", e);
                pinpadListener.onError(e.getCode(), e.getMessage());
            }
        } else {
            ExtOfflinePINParameters parameter = new ExtOfflinePINParameters();
            parameter.setMaxPINLen( maxPinLength);
            parameter.setMaskLine(ExtPINMaskLine.LINE_2);
            parameter.setDisplayMessages(new String[]{NSDKModuleManagerImpl.getInstance().getContext().getString(R.string.sdk_helper_pinpad_offline_pin)});
            parameter.setPINBlockMode(PINBlockMode.ISO9564_0);
            parameter.setAutoComplete(true);
            parameter.setRandomProtectMode(true);
            try {
                mExtPinEntry.startOfflinePINEntry(key, pan, timeoutSec, parameter, new ExPinInputListenerIml(pinpadListener));
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK ExternalPinpad]--start pin failed.", e);
                pinpadListener.onError(e.getCode(), e.getMessage());
            }
        }
    }

    /**
     * Cancel PIN entry.
     */
    public boolean cancelPinInput() {
        try {
            LoggerUtils.d("[NSDK ExternalPinpad]--cancel pinpad.");
            mExtPinEntry.cancelPINEntry();
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--cancel pinpad failed.", e);
            return false;
        }
    }

    /**
     * Encrypt data
     *
     * @param plainText    The value to be encrypted.
     * @param dataKeyIndex Data key index.
     * @return result.
     */
    public byte[] encryptData(byte[] plainText, int dataKeyIndex, int algorithmType) {
        CipherType cipherType;
        SymmetricKey symmetricKey = new SymmetricKey();
        symmetricKey.setKeyID((byte) dataKeyIndex);
        symmetricKey.setKeyType(KeyType.DES);
        if (algorithmType == KeyAlgorithmType.DUKPT) {
            LoggerUtils.d("[NSDK ExternalPinpad]--encrypt DUKPT data(data key index = " + dataKeyIndex + ").");
            symmetricKey.setKeyUsage(KeyUsage.DUKPT);
            cipherType = CipherType.DUKPT_ECB_BOTH;
        } else {
            LoggerUtils.d("[NSDK ExternalPinpad]--encrypt MKSK data(data key index = " + dataKeyIndex + ").");
            symmetricKey.setKeyUsage(KeyUsage.DATA);
            cipherType = CipherType.DES_ECB;
        }

        try {
            CipherOutput cipherOutput = mExtCrypto.encrypt(symmetricKey, cipherType, PaddingMode.NONE, null, plainText);
            byte[] cipherData = cipherOutput.getData();
            if (cipherData == null || cipherData.length == 0) {
                LoggerUtils.e("[NSDK ExternalPinpad]--encrypt data failed.");
                return null;
            } else {
                LoggerUtils.d("[NSDK ExternalPinpad]--encrypt data success.");
                return cipherData;
            }
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--encrypt data failed.", e);
            return null;
        }
    }

    /**
     * Obtain Mac
     *
     * @param macIndex      Mac key index .
     * @param plainText     The value to be encrypted.
     * @param algorithmType Algorithm type. {@link KeyAlgorithmType}
     * @param macMode       MAC mode. {@link MacMode}
     * @return Mac result.
     */
    public byte[] getMac(byte[] plainText, @KeyAlgorithmType.AlgorithmTypeDef int algorithmType, @MacMode.MacModeTypeDef int macMode, int macIndex) {
        MACType macType = MACType.DUKPT_X99;
        if (algorithmType == KeyAlgorithmType.DUKPT) {
            switch (macMode) {
                case MacMode.TYPE_9606:
                    macType = MACType.DUKPT_LAST;
                    break;
                case MacMode.TYPE_X919:
                    macType = MACType.DUKPT_X919;
                    break;
                case MacMode.TYPE_X99:
                    macType = MACType.DUKPT_X99;
                    break;
                case MacMode.TYPE_ECB:
                    macType = MACType.DUKPT_UNIONPAY_ECB;
                    break;
                default:
                    break;
            }
        } else {
            switch (macMode) {
                case MacMode.TYPE_9606:
                    macType = MACType.TDES_LAST;
                    break;
                case MacMode.TYPE_X919:
                    macType = MACType.TDES_X919;
                    break;
                case MacMode.TYPE_X99:
                    macType = MACType.TDES_X99;
                    break;
                case MacMode.TYPE_ECB:
                    macType = MACType.TDES_UNIONPAY_ECB;
                    break;
                default:
                    break;
            }
        }
        try {
            MACOutput macOutput = mExtCrypto.generateMAC((byte) macIndex, macType, null, plainText);
            byte[] mac = macOutput.getData();
            LoggerUtils.d("[NSDK ExternalPinpad]--get mac(type = " + macType + ",mac index = " + macIndex + "): " + BytesUtils.bcdToString(mac) + ".");
            return mac;
        } catch (Exception e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--get mac(type = " + macType + ",mac index = " + macIndex + ") failed.", e);
            return null;
        }
    }

    /**
     * Increase KSN.
     *
     * @param masterIndex master key index.
     */
    public boolean increaseKsn(int masterIndex) {
        try {
            LoggerUtils.d("[NSDK ExternalPinpad]--increase ksn(master key index = " + masterIndex + ").");
            mExtKeyManager.increaseKSN((byte) masterIndex);
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--increase ksn failed.", e);
            return false;
        }
    }

    /**
     * Get KSN
     *
     * @param masterIndex master key index
     * @return ksn
     */
    public byte[] getKsn(int masterIndex) {
        DUKPTKey keyInfo = new DUKPTKey();
        try {
            keyInfo.setKeyID((byte) masterIndex);
            keyInfo.setKeyType(KeyType.DES);
            keyInfo.setKeyUsage(KeyUsage.DUKPT);
            byte[] ksn = mExtKeyManager.getKeyInfo(KeyInfoID.KSN, keyInfo);
            LoggerUtils.d("[NSDK ExternalPinpad]--get ksn(master key index = " + masterIndex + "):" + BytesUtils.bcdToString(ksn));
            return ksn;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--get ksn failed.", e);
            return null;
        }
    }

    /**
     * Get the work key KCV under the specified index. It can be used to determine whether the key exists
     *
     * @param index       work key index
     * @param workKeyType key type. PIN\MAC\DATA key.
     * @return kcv byte array
     */
    public byte[] getWorkKcv(byte index, WorkKeyType workKeyType) {
        SymmetricKey key = new SymmetricKey();
        key.setKeyID(index);
        key.setKeyType(KeyType.DES);
        switch (workKeyType) {
            case MAC_KEY:
                key.setKeyUsage(KeyUsage.MAC);
                break;
            case PIN_KEY:
                key.setKeyUsage(KeyUsage.PIN);
                break;
            case DATA_KEY:
                key.setKeyUsage(KeyUsage.DATA);
                break;
            default:
                LoggerUtils.e("[NSDK ExternalPinpad]--work key type error!");
                return null;
        }
        try {
            byte[] kcv = mExtKeyManager.getKeyInfo(KeyInfoID.KCV, key);
            LoggerUtils.d("[NSDK ExternalPinpad]--get work key's kcv (type = " + key.getKeyUsage() + ",index = " + index + "): " + BytesUtils.bcdToString(kcv));
            return null;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--get work key's kcv failed.", e);
            return null;
        }
    }

    /**
     * Get the master key KCV under the specified index. It can be used to determine whether the key exists
     *
     * @param index            master key index
     * @param keyAlgorithmType key type. DUKPT or MKSK.
     * @return kcv byte array
     */
    public byte[] getMasterKcv(byte index, @KeyAlgorithmType.AlgorithmTypeDef int keyAlgorithmType) {
        SymmetricKey key = new SymmetricKey();
        key.setKeyID(index);
        if (keyAlgorithmType == KeyAlgorithmType.DUKPT) {
            key.setKeyUsage(KeyUsage.DUKPT);
        } else {
            key.setKeyUsage(KeyUsage.KEK);
        }
        key.setKeyType(KeyType.DES);
        try {
            byte[] kcv = mExtKeyManager.getKeyInfo(KeyInfoID.KCV, key);
            LoggerUtils.d("[NSDK ExternalPinpad]--get master key's kcv (type = " + key.getKeyUsage() + ",index = " + index + "): " + BytesUtils.bcdToString(kcv));
            return null;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--get master key's kcv failed.", e);
            return null;
        }
    }

    /**
     * load master key.
     *
     * @param masterIndex master key index.
     */
    public boolean loadMkskMasterKey(int masterIndex, byte[] key) {
        SymmetricKey destKey = new SymmetricKey();
        destKey.setKeyID((byte) masterIndex);
        destKey.setKeyType(KeyType.DES);
        destKey.setKeyUsage(KeyUsage.KEK);
        destKey.setKeyLen(16);
        destKey.setKeyData(key);
        destKey.setKCVMode(KCVMode.NONE);
        try {
            LoggerUtils.d("[NSDK ExternalPinpad]--load MKSK master key(key index = " + masterIndex + ").");
            mExtKeyManager.generateKey(KeyGenerateMethod.CLEAR, null, null, destKey, null);
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--load MKSK master key failed.", e);
            return false;
        }
    }


    /**
     * load master key.
     *
     * @param masterIndex index of master key.
     * @param key         the master key to be loaded.
     */
    public boolean loadMkskWorkKey(int masterIndex, WorkKeyType workKeyType, int keyIndex, byte[] key, byte[] kcv) {
        SymmetricKey sourceKey = new SymmetricKey();
        sourceKey.setKeyID((byte) masterIndex);
        sourceKey.setKeyType(KeyType.DES);
        sourceKey.setKeyUsage(KeyUsage.KEK);
        SymmetricKey destKey = new SymmetricKey();
        destKey.setKeyID((byte) keyIndex);
        destKey.setKeyType(KeyType.DES);
        destKey.setKeyLen(16);
        destKey.setKeyData(key);
        destKey.setKCVMode(KCVMode.ZERO);
        destKey.setKCV(kcv);
        switch (workKeyType) {
            case MAC_KEY:
                destKey.setKeyUsage(KeyUsage.MAC);
                break;
            case PIN_KEY:
                destKey.setKeyUsage(KeyUsage.PIN);
                break;
            case DATA_KEY:
                destKey.setKeyUsage(KeyUsage.DATA);
                break;
            default:
                LoggerUtils.e("[NSDK ExternalPinpad]-- work key type error!");
                return false;
        }
        try {
            LoggerUtils.d("[NSDK ExternalPinpad]--load MKSK "+destKey.getKeyUsage()+" key" +
                    "(MASTER key index = " + masterIndex + ", "+destKey.getKeyUsage()+" key index = " + keyIndex + ").");
            mExtKeyManager.generateKey(KeyGenerateMethod.CIPHER, null, sourceKey, destKey, null);
            return true;
        } catch (NSDKException e) {
            if (e.getCode() == -1309){
                LoggerUtils.e("[NSDK ExternalPinpad]--"+destKey.getKeyUsage()+"=>> master key doesn't exist.", e);
            }else if (e.getCode() == -1014){
                LoggerUtils.e("[NSDK ExternalPinpad]--"+destKey.getKeyUsage()+"=>> key parameters or check value error.", e);
            }else if (e.getCode() == -1702){
                LoggerUtils.e("[NSDK ExternalPinpad]--"+destKey.getKeyUsage()+"=>> duplicate work key.", e);
            }else{
                LoggerUtils.e("[NSDK ExternalPinpad]--"+destKey.getKeyUsage()+"=>> load work key failed.", e);
            }
            return false;
        }
    }

    /**
     * load dukpt key.
     *
     * @param masterKeyIndex index of dukpt key.
     * @param ipek           Dukpt ipek
     * @param ksn            10 bytes ksn.
     */
    public boolean loadIpekKey(int masterKeyIndex, byte[] ipek, byte[] ksn) {
        DUKPTKey destKey = new DUKPTKey();
        destKey.setKeyID((byte) masterKeyIndex);
        destKey.setKeyType(KeyType.DES);
        destKey.setKeyUsage(KeyUsage.DUKPT);
        destKey.setKeyData(ipek);
        destKey.setKSN(ksn);
        destKey.setKeyLen(16);
        destKey.setKCVMode(KCVMode.NONE);

        try {
            LoggerUtils.d("[NSDK ExternalPinpad]--load DUKPT ipek (key index = " + masterKeyIndex + ").");
            mExtKeyManager.generateKey(KeyGenerateMethod.CLEAR, null, null, destKey, null);
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExternalPinpad]--load DUKPT ipek failed.", e);
            return false;
        }
    }

    /**
     * Clear  key.
     *
     * @param keyAlgorithm   {@link KeyAlgorithmType}
     * @param masterKeyIndex master key index.
     * @param pikIndex       PIN key index.
     * @param makIndex       mac key index.
     * @param dataIndex      data key index.
     */
    public void clearKey(int keyAlgorithm, int masterKeyIndex, int pikIndex, int makIndex, int dataIndex) {
        SymmetricKey symmetricKey = new SymmetricKey();
        symmetricKey.setKeyType(KeyType.DES);
        if (keyAlgorithm == KeyAlgorithmType.DUKPT) {
            symmetricKey.setKeyID((byte) masterKeyIndex);
            symmetricKey.setKeyUsage(KeyUsage.DUKPT);
            try {
                LoggerUtils.d("[NSDK ExternalPinpad]--delete IPEK key(key index = " + masterKeyIndex + ").");
                mExtKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK ExternalPinpad]--clear IPEK key failed.", e);
            }
        } else {
            symmetricKey.setKeyID((byte) masterKeyIndex);
            symmetricKey.setKeyUsage(KeyUsage.KEK);
            try {
                LoggerUtils.d("[NSDK ExternalPinpad]--delete MKSK master key(key index = " + masterKeyIndex + ").");
                mExtKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK ExternalPinpad]--delete MKSK master key failed.", e);
            }

            symmetricKey.setKeyID((byte) pikIndex);
            symmetricKey.setKeyUsage(KeyUsage.PIN);
            try {
                LoggerUtils.d("[NSDK ExternalPinpad]--delete MKSK PIN key(key index = " + pikIndex + ").");
                mExtKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK ExternalPinpad]--delete MKSK PIN key failed.", e);
            }

            symmetricKey.setKeyID((byte) makIndex);
            symmetricKey.setKeyUsage(KeyUsage.MAC);
            try {
                LoggerUtils.d("[NSDK ExternalPinpad]--delete MKSK mac key(key index = " + makIndex + ").");
                mExtKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK ExternalPinpad]--delete MKSK mac key failed.", e);
            }

            symmetricKey.setKeyID((byte) dataIndex);
            symmetricKey.setKeyUsage(KeyUsage.DATA);
            try {
                LoggerUtils.d("[NSDK ExternalPinpad]--delete MKSK data key(key index = " + dataIndex + ").");
                mExtKeyManager.deleteKey(symmetricKey);
            } catch (NSDKException e) {
                LoggerUtils.e("[NSDK ExternalPinpad]--delete MKSK data key failed.", e);
            }
        }
    }
    public byte[] getKcv(byte index,int keyAlgorithmType){
        SymmetricKey key = new SymmetricKey();
        key.setKeyID(index);
        if (keyAlgorithmType == KeyAlgorithmType.DUKPT) {
            key.setKeyUsage(KeyUsage.DUKPT);
        } else {
            key.setKeyUsage(KeyUsage.KEK);
        }
        key.setKeyType(KeyType.DES);
        try {
            return mExtKeyManager.getKeyInfo(KeyInfoID.KCV, key);
        } catch (NSDKException e) {
            return null;
        }
    }
    private static class ExPinInputListenerIml implements ExtPINEntryListener {
        private PinpadListener pinPadListener;

        private ExPinInputListenerIml(PinpadListener listener) {
            this.pinPadListener = listener;
        }

        @Override
        public void onOnlineSuccess(int pinLen, byte[] pinBlock, byte[] dukptKsn) {
            LoggerUtils.d("[NSDK ExternalPinpad]--input online PIN success. Pin block is " + BytesUtils.bcdToString(pinBlock));
            if (pinPadListener != null) {
                pinPadListener.onPinRslt(pinBlock);
                pinPadListener = null;
            }
        }

        @Override
        public void onOfflineSuccess(int pinLen, byte[] pinBlock, byte[] randomKey) {
            LoggerUtils.d("[NSDK ExternalPinpad]--input offline PIN success. Pin block is " + BytesUtils.bcdToString(pinBlock));
            if (pinPadListener != null) {
                pinPadListener.onPinRslt(pinBlock);
                pinPadListener = null;
            }

        }

        @Override
        public void onTimeout() {
            LoggerUtils.e("[NSDK ExternalPinpad]--input PIN timeout.");
            if (pinPadListener != null) {
                pinPadListener.onTimeout();
                pinPadListener = null;
            }
        }

        @Override
        public void onCancel() {
            LoggerUtils.e("[NSDK ExternalPinpad]--Cancel PIN.");
            if (pinPadListener != null) {
                pinPadListener.onCancel();
                pinPadListener = null;
            }
        }

        @Override
        public void onError(int errorCode, String message) {
            LoggerUtils.e("[NSDK ExternalPinpad]--input PIN error, error code is " + errorCode + ", error message is " + message);
            if (pinPadListener != null) {
                pinPadListener.onError(-1, "Input PIN timeout");
                pinPadListener = null;
            }
        }
    }
}
