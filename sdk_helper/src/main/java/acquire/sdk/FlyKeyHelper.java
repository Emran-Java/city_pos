package acquire.sdk;

import android.content.Context;

import androidx.annotation.NonNull;

import com.google.gson.Gson;
import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.crypto.AsymAlgorithmParameters;
import com.newland.nsdk.core.api.common.crypto.MessageDigestType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.keymanager.AsymKeyType;
import com.newland.nsdk.core.api.common.keymanager.AsymKeyUsage;
import com.newland.nsdk.core.api.common.keymanager.AsymmetricKey;
import com.newland.nsdk.core.api.common.keymanager.KeyGenerateMethod;
import com.newland.nsdk.core.api.internal.NSDKModuleManager;
import com.newland.nsdk.core.api.internal.crypto.Crypto;
import com.newland.nsdk.core.api.internal.keymanager.KeyManager;
import com.newland.nsdk.core.external.ExtNSDKModuleManagerImpl;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;
import com.newland.nsdk.plugin.rkl.common.DeviceType;
import com.newland.nsdk.plugin.rkl.external.ExternalRKLAPI;
import com.newland.nsdk.plugin.rkl.internal.InternalRKLAPI;
import com.newland.rkl.KeyInfo;
import com.newland.rkl.RKLErrorCode;
import com.newland.rkl.RKLProcessor;
import com.newland.rkl.RKLStatus;
import com.newland.rkl.api.RKLAPI;
import com.newland.rkl.exception.DeviceException;
import com.newland.rkl.exception.RKLException;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.file.FileUtils;

/**
 * A tool for connecting FlyKey Service. FlyKey is a service for download key remotely.
 *
 * @author Janson
 * @date 2022/5/24 17:08
 */
public class FlyKeyHelper {

    private final static String CONFIG_PRO = "RKLCONF_PRO";
    private final static String CONFIG_DEV = "RKLCONF_DEV";
    private final static boolean DEBUG = true;

    /**
     * load key remotely (fly key)
     */
    public static void downloadMsterKey(Context context, boolean isExternalPinpad, FlyKeyListener listener) {
        dependenciesDetect();
        try {
            String configFile;
            RKLAPI rklapi;
            LoggerUtils.d("start fly key");
            if (DEBUG) {
                //Debug
                configFile = CONFIG_DEV;
                rklapi = new DemoRKLAPI(context, NSDKModuleManagerImpl.getInstance());
                ((DemoRKLAPI)rklapi).installDefaultDeviceCertKey();
            } else {
                //Product
                configFile = CONFIG_PRO;
                if (isExternalPinpad) {
                    rklapi = new ExternalRKLAPI(context, ExtNSDKModuleManagerImpl.getInstance(), DeviceType.PRO);
                } else {
                    rklapi = new InternalRKLAPI(context, NSDKModuleManagerImpl.getInstance());
                }
            }
            String filePath = context.getExternalFilesDir(null) + File.separator + configFile;
            if (new File(filePath).exists()) {
                new File(filePath).delete();
            }
            try (InputStream inputStream = context.getAssets().open(configFile)) {
                FileUtils.copyFileByInputStream(inputStream, new File(filePath));
            }
            RKLProcessor.getInstance().setAPI(rklapi);
            RKLProcessor.getInstance().start(filePath, (status, errorCode, message) -> {
                LoggerUtils.d("status:" + status + ",error code:" + errorCode + ",message:" + message);
                if (status != RKLStatus.ENDED) {
                    return;
                }
                if (errorCode != RKLErrorCode.SUCCESS) {
                    //failed
                    FlyKeyErrorBean errorBean;
                    try {
                        errorBean = new Gson().fromJson(message,FlyKeyErrorBean.class);
                    }catch (Exception e){
                        LoggerUtils.e("RKL Response to FlyKeyErrorBean failed!",e);
                        errorBean = new FlyKeyErrorBean();
                        errorBean.setMessage(context.getString(R.string.sdk_helper_fly_key_error_unavailable_message));
                        errorBean.setDetailErrorCode(-999);
                    }
                    listener.onFailed(errorBean);
                    return;
                }
                //success
                try {
                    int keyCount = RKLProcessor.getInstance().getInstalledKeyNum();
                    LoggerUtils.d("response key count: " + keyCount);
                    if (keyCount == 0) {
                        //NO new key
                        listener.onSuccess(new int[0]);
                        return;
                    }
                    //installation completed
                    List<KeyInfo> keys = RKLProcessor.getInstance().getInstalledKeyInfo();
                    int[] indexes = new int[keys.size()];
                    for (int i = 0; i < keys.size(); i++) {
                        KeyInfo key = keys.get(i);
                        indexes[i] = key.getIndex();
                        LoggerUtils.d("installed key: " + key);
                    }
                    listener.onSuccess(indexes);
                } catch (RKLException e) {
                    listener.onException(e);
                }

            });
        } catch (Exception e) {
            LoggerUtils.e("Request Error");
            listener.onException(e);
        }
    }

    private static boolean hasDependencies;
    private static void dependenciesDetect(){
        if (hasDependencies){
            return;
        }
        try {
            Class.forName("com.google.gson.Gson");
        }catch (ClassNotFoundException ex) {
            throw new RuntimeException("Make sure to add the ’Gson’ library to your dependencies.");
        }
        try {
            Class.forName("retrofit2.Retrofit");
        }catch (ClassNotFoundException ex) {
            throw new RuntimeException("Make sure to add the ’retrofit’ library to your dependencies.");
        }
        try {
            Class.forName("retrofit2.converter.gson.GsonConverterFactory");
        }catch (ClassNotFoundException ex) {
            throw new RuntimeException("Make sure to add the ’converter-gson’ library to your dependencies.");
        }
        hasDependencies = true;
    }

    /**
     * Fly key error information
     *
     * @author Janson
     * @date 2022/5/25 11:48
     */
    public static class FlyKeyErrorBean{
        private int detailErrorCode;
        private String errorType;
        private String message;

        public int getDetailErrorCode() {
            return detailErrorCode;
        }

        public void setDetailErrorCode(int detailErrorCode) {
            this.detailErrorCode = detailErrorCode;
        }

        public String getErrorType() {
            return errorType;
        }

        public void setErrorType(String errorType) {
            this.errorType = errorType;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
    public interface FlyKeyListener {
        /**
         * download and install success.
         *
         * @param indexes key indexes。
         */
        void onSuccess(@NonNull int[] indexes);
        /**
         * download failed
         *
         * @param errorBean Fly Key error
         */
        void onFailed(FlyKeyErrorBean errorBean);

        /**
         * caught an exception when installing keys.
         */
        void onException(Exception e);
    }

    /**
     * demo rkl api. It's just used for debug mode.
     *
     * @author Janson
     * @date 2022/5/24 17:40
     */
    private static class DemoRKLAPI extends InternalRKLAPI {
        public static final int CERT_PRIVATE_KEY_ID = 249;

        public static final byte[] CERT_PRIVATE_KEY = {
                (byte) 0x30, (byte) 0x82, (byte) 0x04, (byte) 0xA3, (byte) 0x02, (byte) 0x01, (byte) 0x00, (byte) 0x02, (byte) 0x82, (byte) 0x01, (byte) 0x01, (byte) 0x00, (byte) 0xDE, (byte) 0xE3, (byte) 0x32, (byte) 0x80, (byte) 0x0C, (byte) 0x7B, (byte) 0x0A, (byte) 0x1C, (byte) 0xF1, (byte) 0x12, (byte) 0x80, (byte) 0x2D, (byte) 0xA1, (byte) 0x84, (byte) 0x74, (byte) 0x99, (byte) 0xFE, (byte) 0x0C, (byte) 0x83, (byte) 0x09,
                (byte) 0xC4, (byte) 0x0D, (byte) 0x4C, (byte) 0x3D, (byte) 0x28, (byte) 0xE8, (byte) 0x5A, (byte) 0x53, (byte) 0x4F, (byte) 0xD6, (byte) 0xB4, (byte) 0xE3, (byte) 0x14, (byte) 0x20, (byte) 0xB6, (byte) 0x3C, (byte) 0x88, (byte) 0x20, (byte) 0x9E, (byte) 0x47, (byte) 0x7B, (byte) 0x5F, (byte) 0xCB, (byte) 0x87, (byte) 0xD4, (byte) 0x7B, (byte) 0x88, (byte) 0x57, (byte) 0x42, (byte) 0xD1, (byte) 0xE5, (byte) 0x7E,
                (byte) 0xCB, (byte) 0xA8, (byte) 0xF9, (byte) 0xAD, (byte) 0x9D, (byte) 0x61, (byte) 0x9F, (byte) 0xAC, (byte) 0xFA, (byte) 0xB0, (byte) 0x58, (byte) 0x41, (byte) 0x49, (byte) 0x88, (byte) 0xF8, (byte) 0xCA, (byte) 0xDB, (byte) 0x4F, (byte) 0x4B, (byte) 0xC5, (byte) 0xDA, (byte) 0xDB, (byte) 0x7A, (byte) 0xD9, (byte) 0x4B, (byte) 0x63, (byte) 0x35, (byte) 0x24, (byte) 0x50, (byte) 0xAE, (byte) 0xF3, (byte) 0x4B,
                (byte) 0x64, (byte) 0x4B, (byte) 0x1D, (byte) 0xA3, (byte) 0xE0, (byte) 0xEF, (byte) 0xFD, (byte) 0x13, (byte) 0x8F, (byte) 0x3D, (byte) 0xC9, (byte) 0xFB, (byte) 0xEC, (byte) 0x15, (byte) 0x1A, (byte) 0x1F, (byte) 0xFB, (byte) 0xB0, (byte) 0xBF, (byte) 0xCE, (byte) 0x53, (byte) 0x91, (byte) 0x10, (byte) 0x9E, (byte) 0xC9, (byte) 0xD7, (byte) 0x3A, (byte) 0x6C, (byte) 0xA1, (byte) 0x87, (byte) 0x5B, (byte) 0x1A,
                (byte) 0x4B, (byte) 0xAE, (byte) 0xD1, (byte) 0x16, (byte) 0x1B, (byte) 0x00, (byte) 0x33, (byte) 0x9D, (byte) 0x61, (byte) 0xDA, (byte) 0x50, (byte) 0x08, (byte) 0xC9, (byte) 0xAB, (byte) 0xF3, (byte) 0xFC, (byte) 0x17, (byte) 0xE2, (byte) 0xC5, (byte) 0xEF, (byte) 0x84, (byte) 0x95, (byte) 0x2C, (byte) 0x20, (byte) 0x2B, (byte) 0xEB, (byte) 0xFA, (byte) 0x53, (byte) 0xE4, (byte) 0x6E, (byte) 0x33, (byte) 0xBB,
                (byte) 0x2B, (byte) 0xE4, (byte) 0xEE, (byte) 0xC1, (byte) 0x83, (byte) 0xF3, (byte) 0xC3, (byte) 0x93, (byte) 0x6C, (byte) 0xCB, (byte) 0x92, (byte) 0xA4, (byte) 0x5C, (byte) 0x09, (byte) 0x67, (byte) 0x54, (byte) 0xA1, (byte) 0x26, (byte) 0x77, (byte) 0x8B, (byte) 0xE3, (byte) 0x8C, (byte) 0xC2, (byte) 0x77, (byte) 0xE7, (byte) 0x98, (byte) 0xFF, (byte) 0xC0, (byte) 0xBF, (byte) 0xD6, (byte) 0xB0, (byte) 0x0E,
                (byte) 0xB2, (byte) 0x67, (byte) 0xD4, (byte) 0x74, (byte) 0x5B, (byte) 0xF5, (byte) 0x6C, (byte) 0x4F, (byte) 0xE6, (byte) 0x64, (byte) 0x09, (byte) 0x01, (byte) 0x96, (byte) 0xC4, (byte) 0xB3, (byte) 0x67, (byte) 0x90, (byte) 0x48, (byte) 0xF8, (byte) 0x06, (byte) 0x18, (byte) 0x6F, (byte) 0x1B, (byte) 0x4E, (byte) 0x0D, (byte) 0x01, (byte) 0xF9, (byte) 0x4F, (byte) 0xBD, (byte) 0xBE, (byte) 0x98, (byte) 0xCB,
                (byte) 0xB0, (byte) 0xA0, (byte) 0x90, (byte) 0x43, (byte) 0x46, (byte) 0x42, (byte) 0x26, (byte) 0xEC, (byte) 0x4C, (byte) 0x11, (byte) 0xF4, (byte) 0xD4, (byte) 0x55, (byte) 0xDB, (byte) 0x89, (byte) 0xCB, (byte) 0xDC, (byte) 0x46, (byte) 0x19, (byte) 0xDB, (byte) 0xB3, (byte) 0xA8, (byte) 0x33, (byte) 0x9C, (byte) 0xB4, (byte) 0x61, (byte) 0xB3, (byte) 0x38, (byte) 0x07, (byte) 0x87, (byte) 0x5C, (byte) 0x30,
                (byte) 0x2F, (byte) 0x59, (byte) 0x31, (byte) 0x1F, (byte) 0xD1, (byte) 0xFE, (byte) 0xEF, (byte) 0x95, (byte) 0x5A, (byte) 0x79, (byte) 0x19, (byte) 0x85, (byte) 0x02, (byte) 0x03, (byte) 0x01, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x82, (byte) 0x01, (byte) 0x00, (byte) 0x1F, (byte) 0xC6, (byte) 0x66, (byte) 0x09, (byte) 0xA9, (byte) 0x3D, (byte) 0xD5, (byte) 0x38, (byte) 0x41, (byte) 0x09, (byte) 0xF1,
                (byte) 0x2A, (byte) 0x10, (byte) 0x2F, (byte) 0xB0, (byte) 0xEB, (byte) 0xCF, (byte) 0x32, (byte) 0xFB, (byte) 0x6F, (byte) 0x65, (byte) 0xFA, (byte) 0xBB, (byte) 0x56, (byte) 0xB0, (byte) 0xC1, (byte) 0x47, (byte) 0x37, (byte) 0x3F, (byte) 0x57, (byte) 0x8D, (byte) 0x9C, (byte) 0x1D, (byte) 0xDF, (byte) 0x77, (byte) 0xFB, (byte) 0x73, (byte) 0x30, (byte) 0xAB, (byte) 0x3F, (byte) 0xE7, (byte) 0x8F, (byte) 0xC5,
                (byte) 0x95, (byte) 0x4C, (byte) 0xAE, (byte) 0x7B, (byte) 0xC6, (byte) 0x6C, (byte) 0xE9, (byte) 0x3D, (byte) 0x7D, (byte) 0x71, (byte) 0x0D, (byte) 0x5A, (byte) 0xE2, (byte) 0xEC, (byte) 0x5F, (byte) 0xE1, (byte) 0x82, (byte) 0xA8, (byte) 0x8E, (byte) 0x81, (byte) 0x56, (byte) 0x75, (byte) 0x64, (byte) 0x48, (byte) 0x8B, (byte) 0xAA, (byte) 0xEE, (byte) 0x48, (byte) 0x10, (byte) 0x21, (byte) 0xDE, (byte) 0x4E,
                (byte) 0x4A, (byte) 0x32, (byte) 0x1C, (byte) 0x27, (byte) 0x94, (byte) 0x50, (byte) 0xC8, (byte) 0x50, (byte) 0x38, (byte) 0xFC, (byte) 0x48, (byte) 0x55, (byte) 0x26, (byte) 0x6A, (byte) 0xC7, (byte) 0xEB, (byte) 0xDD, (byte) 0x60, (byte) 0xFB, (byte) 0x5F, (byte) 0x13, (byte) 0x42, (byte) 0x19, (byte) 0xD4, (byte) 0x0A, (byte) 0xA1, (byte) 0x38, (byte) 0x16, (byte) 0x70, (byte) 0x14, (byte) 0xAB, (byte) 0xC6,
                (byte) 0xA1, (byte) 0xCC, (byte) 0x86, (byte) 0x99, (byte) 0x76, (byte) 0xA9, (byte) 0x24, (byte) 0x81, (byte) 0xD1, (byte) 0x62, (byte) 0xDE, (byte) 0xBE, (byte) 0x42, (byte) 0x17, (byte) 0x81, (byte) 0x54, (byte) 0xDA, (byte) 0x67, (byte) 0xFB, (byte) 0xD6, (byte) 0x92, (byte) 0xD6, (byte) 0x01, (byte) 0xA8, (byte) 0x9C, (byte) 0xE7, (byte) 0xE5, (byte) 0xA8, (byte) 0xC3, (byte) 0x81, (byte) 0x27, (byte) 0x69,
                (byte) 0x52, (byte) 0xA9, (byte) 0xD0, (byte) 0xAA, (byte) 0xE4, (byte) 0xF4, (byte) 0xC9, (byte) 0x67, (byte) 0xA2, (byte) 0x9E, (byte) 0x68, (byte) 0x72, (byte) 0x39, (byte) 0x1B, (byte) 0x77, (byte) 0xFE, (byte) 0x56, (byte) 0x3D, (byte) 0x51, (byte) 0x80, (byte) 0x62, (byte) 0x22, (byte) 0x0D, (byte) 0x9E, (byte) 0xD5, (byte) 0x83, (byte) 0xB7, (byte) 0x90, (byte) 0x23, (byte) 0xCE, (byte) 0xA0, (byte) 0xF6,
                (byte) 0xC9, (byte) 0x46, (byte) 0x1E, (byte) 0xF8, (byte) 0xAF, (byte) 0x76, (byte) 0x63, (byte) 0x7F, (byte) 0x39, (byte) 0x1B, (byte) 0xB0, (byte) 0x1C, (byte) 0x83, (byte) 0xCB, (byte) 0x89, (byte) 0x87, (byte) 0x1D, (byte) 0x23, (byte) 0x84, (byte) 0xAC, (byte) 0x69, (byte) 0xC7, (byte) 0x90, (byte) 0x61, (byte) 0xB6, (byte) 0x5B, (byte) 0x1F, (byte) 0x20, (byte) 0x77, (byte) 0x38, (byte) 0x2B, (byte) 0xE9,
                (byte) 0x3C, (byte) 0x42, (byte) 0x0B, (byte) 0x26, (byte) 0x2C, (byte) 0x2F, (byte) 0xD7, (byte) 0xE7, (byte) 0x43, (byte) 0x9F, (byte) 0x80, (byte) 0xD6, (byte) 0x95, (byte) 0x6B, (byte) 0xDA, (byte) 0xD7, (byte) 0xF0, (byte) 0x87, (byte) 0xFF, (byte) 0x62, (byte) 0x09, (byte) 0x68, (byte) 0x36, (byte) 0xF9, (byte) 0x53, (byte) 0x90, (byte) 0x95, (byte) 0xEE, (byte) 0x6A, (byte) 0xDF, (byte) 0xF7, (byte) 0x04,
                (byte) 0x3E, (byte) 0xA3, (byte) 0x89, (byte) 0xD9, (byte) 0xC5, (byte) 0x81, (byte) 0xBE, (byte) 0x96, (byte) 0x80, (byte) 0xFF, (byte) 0xD0, (byte) 0x1F, (byte) 0x86, (byte) 0x75, (byte) 0xF8, (byte) 0x3D, (byte) 0x67, (byte) 0xB0, (byte) 0x43, (byte) 0xBB, (byte) 0x35, (byte) 0x02, (byte) 0x81, (byte) 0x81, (byte) 0x00, (byte) 0xF6, (byte) 0x3C, (byte) 0x8D, (byte) 0xAD, (byte) 0x48, (byte) 0x05, (byte) 0x93,
                (byte) 0xB8, (byte) 0xEC, (byte) 0x7B, (byte) 0x3F, (byte) 0xD8, (byte) 0xB4, (byte) 0x58, (byte) 0x60, (byte) 0x34, (byte) 0x55, (byte) 0xE2, (byte) 0xB7, (byte) 0x6A, (byte) 0x3D, (byte) 0x16, (byte) 0x69, (byte) 0x2F, (byte) 0xC9, (byte) 0xC7, (byte) 0x04, (byte) 0xE7, (byte) 0x2E, (byte) 0x8B, (byte) 0xE8, (byte) 0xCD, (byte) 0xF9, (byte) 0x28, (byte) 0x7C, (byte) 0x8A, (byte) 0x3E, (byte) 0x30, (byte) 0x03,
                (byte) 0x80, (byte) 0xC7, (byte) 0xC9, (byte) 0x6C, (byte) 0x57, (byte) 0x0A, (byte) 0x2F, (byte) 0xB8, (byte) 0x78, (byte) 0x1D, (byte) 0xAA, (byte) 0x8F, (byte) 0xD3, (byte) 0xAC, (byte) 0xB2, (byte) 0x9F, (byte) 0x7D, (byte) 0xEE, (byte) 0x34, (byte) 0xD6, (byte) 0x4D, (byte) 0x27, (byte) 0x79, (byte) 0xD6, (byte) 0x72, (byte) 0xC8, (byte) 0x82, (byte) 0x9A, (byte) 0xF0, (byte) 0xED, (byte) 0x5C, (byte) 0x57,
                (byte) 0x36, (byte) 0x32, (byte) 0x6C, (byte) 0x8C, (byte) 0x18, (byte) 0xD2, (byte) 0xA9, (byte) 0x2D, (byte) 0x39, (byte) 0x94, (byte) 0x83, (byte) 0x24, (byte) 0x43, (byte) 0x78, (byte) 0x86, (byte) 0x53, (byte) 0x12, (byte) 0x02, (byte) 0xB3, (byte) 0x64, (byte) 0x0D, (byte) 0xBD, (byte) 0xBD, (byte) 0x9C, (byte) 0x59, (byte) 0xDA, (byte) 0x88, (byte) 0x07, (byte) 0x3E, (byte) 0x30, (byte) 0xD7, (byte) 0x31,
                (byte) 0x6A, (byte) 0x76, (byte) 0xE9, (byte) 0x91, (byte) 0x69, (byte) 0xA1, (byte) 0xA0, (byte) 0x96, (byte) 0x33, (byte) 0x40, (byte) 0x5D, (byte) 0x1A, (byte) 0x7F, (byte) 0x23, (byte) 0x02, (byte) 0x9C, (byte) 0xF4, (byte) 0xEC, (byte) 0x63, (byte) 0x40, (byte) 0x4E, (byte) 0xF9, (byte) 0xE3, (byte) 0x54, (byte) 0xAF, (byte) 0x02, (byte) 0x81, (byte) 0x81, (byte) 0x00, (byte) 0xE7, (byte) 0xB9, (byte) 0xA3,
                (byte) 0x1E, (byte) 0xEE, (byte) 0x0B, (byte) 0x27, (byte) 0xD2, (byte) 0xF8, (byte) 0x24, (byte) 0x28, (byte) 0x56, (byte) 0x9D, (byte) 0x99, (byte) 0x56, (byte) 0x2E, (byte) 0x5C, (byte) 0x95, (byte) 0x9C, (byte) 0xB3, (byte) 0xD8, (byte) 0xAA, (byte) 0x64, (byte) 0x19, (byte) 0x33, (byte) 0xC6, (byte) 0x29, (byte) 0xF0, (byte) 0x2D, (byte) 0x7C, (byte) 0xA3, (byte) 0x14, (byte) 0x61, (byte) 0x83, (byte) 0xEC,
                (byte) 0x8A, (byte) 0x5D, (byte) 0x57, (byte) 0xF2, (byte) 0x75, (byte) 0x2E, (byte) 0x51, (byte) 0xB9, (byte) 0x4F, (byte) 0x80, (byte) 0xA7, (byte) 0xA5, (byte) 0x87, (byte) 0x4B, (byte) 0xB2, (byte) 0xCE, (byte) 0xF8, (byte) 0xEF, (byte) 0x7A, (byte) 0xF2, (byte) 0xD2, (byte) 0x34, (byte) 0x49, (byte) 0x05, (byte) 0x66, (byte) 0xD2, (byte) 0x43, (byte) 0xB7, (byte) 0x3E, (byte) 0x9B, (byte) 0x20, (byte) 0x00,
                (byte) 0x8C, (byte) 0xDC, (byte) 0xCE, (byte) 0xC5, (byte) 0xF5, (byte) 0x10, (byte) 0x25, (byte) 0xDD, (byte) 0xFA, (byte) 0x95, (byte) 0x78, (byte) 0x00, (byte) 0x07, (byte) 0x33, (byte) 0x3B, (byte) 0x18, (byte) 0x97, (byte) 0x0C, (byte) 0x8D, (byte) 0x0B, (byte) 0x20, (byte) 0x0B, (byte) 0x68, (byte) 0x4C, (byte) 0x06, (byte) 0x42, (byte) 0xB5, (byte) 0x11, (byte) 0x3E, (byte) 0x62, (byte) 0xDF, (byte) 0xF2,
                (byte) 0xD3, (byte) 0x1A, (byte) 0xAA, (byte) 0x17, (byte) 0xA1, (byte) 0x7A, (byte) 0x81, (byte) 0xF6, (byte) 0xBD, (byte) 0x11, (byte) 0x0A, (byte) 0xC8, (byte) 0xB2, (byte) 0xCF, (byte) 0x53, (byte) 0xE5, (byte) 0xE9, (byte) 0x89, (byte) 0xE8, (byte) 0xF0, (byte) 0xFB, (byte) 0xC1, (byte) 0x48, (byte) 0x9F, (byte) 0xD9, (byte) 0xBE, (byte) 0x9C, (byte) 0x6A, (byte) 0x0B, (byte) 0x02, (byte) 0x81, (byte) 0x80,
                (byte) 0x7F, (byte) 0x23, (byte) 0xEF, (byte) 0x07, (byte) 0x6C, (byte) 0xEB, (byte) 0xAC, (byte) 0x45, (byte) 0xCC, (byte) 0x9D, (byte) 0x2A, (byte) 0xEF, (byte) 0x4A, (byte) 0xD7, (byte) 0x62, (byte) 0xFD, (byte) 0x42, (byte) 0xE7, (byte) 0x68, (byte) 0x4B, (byte) 0x0B, (byte) 0xB5, (byte) 0xB9, (byte) 0xA4, (byte) 0x3A, (byte) 0x2C, (byte) 0x6F, (byte) 0xE4, (byte) 0xAA, (byte) 0x8D, (byte) 0x05, (byte) 0x2C,
                (byte) 0xED, (byte) 0x1C, (byte) 0xB1, (byte) 0x89, (byte) 0x73, (byte) 0xE8, (byte) 0xB8, (byte) 0xB6, (byte) 0x52, (byte) 0xF2, (byte) 0x36, (byte) 0xE5, (byte) 0x8E, (byte) 0x55, (byte) 0xEB, (byte) 0x8A, (byte) 0xE1, (byte) 0xCE, (byte) 0x82, (byte) 0xAB, (byte) 0x0A, (byte) 0x3D, (byte) 0xC4, (byte) 0xCA, (byte) 0x72, (byte) 0x27, (byte) 0x88, (byte) 0x0B, (byte) 0x98, (byte) 0x79, (byte) 0x79, (byte) 0x78,
                (byte) 0x14, (byte) 0xFE, (byte) 0x12, (byte) 0xE0, (byte) 0x99, (byte) 0xBE, (byte) 0x97, (byte) 0x2D, (byte) 0x5D, (byte) 0xC5, (byte) 0xDC, (byte) 0x64, (byte) 0xA8, (byte) 0xC9, (byte) 0x3F, (byte) 0x97, (byte) 0xBE, (byte) 0x8E, (byte) 0x24, (byte) 0x38, (byte) 0x4E, (byte) 0x50, (byte) 0x15, (byte) 0x2E, (byte) 0x74, (byte) 0x84, (byte) 0x8A, (byte) 0x01, (byte) 0xEA, (byte) 0x59, (byte) 0xB0, (byte) 0x14,
                (byte) 0x59, (byte) 0x18, (byte) 0xE9, (byte) 0x9C, (byte) 0x7B, (byte) 0xFD, (byte) 0x70, (byte) 0xFF, (byte) 0x86, (byte) 0xDC, (byte) 0xBC, (byte) 0xFF, (byte) 0x6C, (byte) 0xBC, (byte) 0x0D, (byte) 0x31, (byte) 0xF3, (byte) 0x66, (byte) 0xDF, (byte) 0x5A, (byte) 0x8C, (byte) 0x0A, (byte) 0x3A, (byte) 0x5E, (byte) 0xE0, (byte) 0x7B, (byte) 0xB2, (byte) 0xE5, (byte) 0xD4, (byte) 0x73, (byte) 0x43, (byte) 0xF3,
                (byte) 0x02, (byte) 0x81, (byte) 0x81, (byte) 0x00, (byte) 0xC1, (byte) 0xB4, (byte) 0x6C, (byte) 0xA7, (byte) 0x6E, (byte) 0x55, (byte) 0xE4, (byte) 0xF4, (byte) 0x76, (byte) 0x79, (byte) 0xC3, (byte) 0x3E, (byte) 0xA4, (byte) 0x7F, (byte) 0x89, (byte) 0x8B, (byte) 0x37, (byte) 0xD9, (byte) 0xD9, (byte) 0x24, (byte) 0x7D, (byte) 0xF9, (byte) 0xF1, (byte) 0xB3, (byte) 0x1F, (byte) 0x94, (byte) 0x87, (byte) 0x7A,
                (byte) 0x3E, (byte) 0x8B, (byte) 0xF1, (byte) 0xC7, (byte) 0x17, (byte) 0xBD, (byte) 0x2F, (byte) 0xFE, (byte) 0x7A, (byte) 0x09, (byte) 0x0A, (byte) 0xA4, (byte) 0xEA, (byte) 0x13, (byte) 0x66, (byte) 0xD5, (byte) 0x0E, (byte) 0xB7, (byte) 0x15, (byte) 0xA8, (byte) 0x03, (byte) 0x9F, (byte) 0x75, (byte) 0x64, (byte) 0xA2, (byte) 0xCC, (byte) 0x24, (byte) 0x2F, (byte) 0x93, (byte) 0x85, (byte) 0x76, (byte) 0xFE,
                (byte) 0x7C, (byte) 0xC6, (byte) 0x1E, (byte) 0x68, (byte) 0x37, (byte) 0x44, (byte) 0x89, (byte) 0x31, (byte) 0x37, (byte) 0x63, (byte) 0xA2, (byte) 0x17, (byte) 0x39, (byte) 0x68, (byte) 0x6E, (byte) 0x27, (byte) 0x0A, (byte) 0xCB, (byte) 0x45, (byte) 0x3A, (byte) 0xBF, (byte) 0x98, (byte) 0xA6, (byte) 0xF5, (byte) 0x9D, (byte) 0x88, (byte) 0x49, (byte) 0xC4, (byte) 0x7F, (byte) 0xC4, (byte) 0xAF, (byte) 0xC8,
                (byte) 0x8B, (byte) 0xFA, (byte) 0xD0, (byte) 0x6F, (byte) 0x56, (byte) 0x37, (byte) 0xDE, (byte) 0xC4, (byte) 0x99, (byte) 0x85, (byte) 0x96, (byte) 0x3B, (byte) 0x66, (byte) 0x2D, (byte) 0x3E, (byte) 0x14, (byte) 0xCE, (byte) 0x2A, (byte) 0x35, (byte) 0x9B, (byte) 0x43, (byte) 0xB4, (byte) 0xDE, (byte) 0x7C, (byte) 0x5A, (byte) 0xCC, (byte) 0x5E, (byte) 0xA6, (byte) 0x14, (byte) 0xEC, (byte) 0xA0, (byte) 0xB3,
                (byte) 0x64, (byte) 0xA3, (byte) 0x5C, (byte) 0x01, (byte) 0x02, (byte) 0x81, (byte) 0x80, (byte) 0x02, (byte) 0xE1, (byte) 0x20, (byte) 0x38, (byte) 0x52, (byte) 0xE2, (byte) 0x10, (byte) 0x16, (byte) 0xB6, (byte) 0x4E, (byte) 0x8F, (byte) 0x91, (byte) 0x03, (byte) 0xAA, (byte) 0x98, (byte) 0x42, (byte) 0x34, (byte) 0x0A, (byte) 0x56, (byte) 0x44, (byte) 0x06, (byte) 0x4A, (byte) 0x4F, (byte) 0x14, (byte) 0xD7,
                (byte) 0xAD, (byte) 0x44, (byte) 0x45, (byte) 0xFB, (byte) 0x2A, (byte) 0xB0, (byte) 0x45, (byte) 0x76, (byte) 0x2D, (byte) 0x59, (byte) 0xCC, (byte) 0x04, (byte) 0x33, (byte) 0xA9, (byte) 0x7C, (byte) 0xB2, (byte) 0x91, (byte) 0xDB, (byte) 0x85, (byte) 0x9E, (byte) 0x72, (byte) 0x0E, (byte) 0x6D, (byte) 0x1C, (byte) 0x84, (byte) 0xD0, (byte) 0x10, (byte) 0x0F, (byte) 0x6E, (byte) 0x14, (byte) 0x0A, (byte) 0xB5,
                (byte) 0xEB, (byte) 0x3B, (byte) 0x54, (byte) 0xA5, (byte) 0x7F, (byte) 0x4A, (byte) 0x91, (byte) 0x08, (byte) 0x21, (byte) 0x7D, (byte) 0xE1, (byte) 0xEC, (byte) 0xE6, (byte) 0x2B, (byte) 0x8D, (byte) 0x44, (byte) 0x81, (byte) 0x48, (byte) 0x8B, (byte) 0x45, (byte) 0x05, (byte) 0x4A, (byte) 0x3D, (byte) 0x73, (byte) 0xEA, (byte) 0xFA, (byte) 0xE9, (byte) 0xB4, (byte) 0xE1, (byte) 0xA5, (byte) 0x97, (byte) 0xDD,
                (byte) 0xE0, (byte) 0x0E, (byte) 0x32, (byte) 0xC3, (byte) 0x89, (byte) 0xA1, (byte) 0x8A, (byte) 0x3D, (byte) 0x1A, (byte) 0xC2, (byte) 0x63, (byte) 0x98, (byte) 0x60, (byte) 0xD5, (byte) 0xF5, (byte) 0xD1, (byte) 0x40, (byte) 0x51, (byte) 0x68, (byte) 0xE8, (byte) 0x2F, (byte) 0x2A, (byte) 0xDE, (byte) 0x84, (byte) 0x00, (byte) 0x5D, (byte) 0xDB, (byte) 0x48, (byte) 0x00, (byte) 0x75, (byte) 0x86, (byte) 0xAC,
                (byte) 0x3C, (byte) 0x4A, (byte) 0xA2, (byte) 0x89, (byte) 0x52, (byte) 0x08, (byte) 0x4F
        };

        public static final String ASYM_CERT = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDzTCCArWgAwIBAgIHWRyUABMTgDANBgkqhkiG9w0BAQsFADCBjTELMAkGA1UE\n" +
                "BhMCQ04xDzANBgNVBAgMBkZ1SmlhbjEPMA0GA1UEBwwGRnVaaG91MSswKQYDVQQK\n" +
                "DCJOZXdsYW5kIFBheW1lbnQgVGVjaG5vbG9neSBDby4sTHRkMRcwFQYDVQQLEw5E\n" +
                "RVYgTlBUIE1GRyBDQTEWMBQGA1UEAwwNREVWIE1GRyBTdWJDQTAeFw0yMTA3MDcw\n" +
                "MDAwMDBaFw0zMTA3MDgwMDAwMDBaMIGRMQswCQYDVQQGEwJDTjEPMA0GA1UECAwG\n" +
                "RnVqaWFuMQ8wDQYDVQQHDAZGdXpob3UxKzApBgNVBAoMIk5ld2xhbmRfUGF5bWVu\n" +
                "dF9UZWNobm9sb2d5X0NvLixMdGQxFzAVBgNVBAsMDk5QVC5UZWNoQ2VudGVyMRow\n" +
                "GAYDVQQDDBFTTjAxMjM0NTY3ODktQVVUSDCCASIwDQYJKoZIhvcNAQEBBQADggEP\n" +
                "ADCCAQoCggEBAN7jMoAMewoc8RKALaGEdJn+DIMJxA1MPSjoWlNP1rTjFCC2PIgg\n" +
                "nkd7X8uH1HuIV0LR5X7LqPmtnWGfrPqwWEFJiPjK209LxdrbetlLYzUkUK7zS2RL\n" +
                "HaPg7/0Tjz3J++wVGh/7sL/OU5EQnsnXOmyhh1saS67RFhsAM51h2lAIyavz/Bfi\n" +
                "xe+ElSwgK+v6U+RuM7sr5O7Bg/PDk2zLkqRcCWdUoSZ3i+OMwnfnmP/Av9awDrJn\n" +
                "1HRb9WxP5mQJAZbEs2eQSPgGGG8bTg0B+U+9vpjLsKCQQ0ZCJuxMEfTUVduJy9xG\n" +
                "GduzqDOctGGzOAeHXDAvWTEf0f7vlVp5GYUCAwEAAaMsMCowHQYDVR0OBBYEFHFd\n" +
                "xzQnsxQkMmhd7jzD0DJehJpDMAkGA1UdEwQCMAAwDQYJKoZIhvcNAQELBQADggEB\n" +
                "ACpXnWNuq7y5pZXwwLc/qRf6rsLyNu9qUKeCuViTq2ARRCzrgs2ZwKXCYFC2TKNh\n" +
                "ftJf1vfwECrKKOGoddo6rFlncbPsYga2yfiaqvZrGdmHXfBpashIIGt3JnT7SzWK\n" +
                "sv0OIyw6BxoggwtqrMaHRtbiiyZgWL8817S/qcRHDeBQBPkpcuS6SW+qOX1HcsnL\n" +
                "B7Eet6iPfQ6Ym2/phkACck1lySQ0if07M2SXjnne/tyb4rwHK1N1sPyu2KYqILsw\n" +
                "KkbqSCGg03dsRVH7aZ3G6O8VfjcjZemAgLPEvtDfGjeJcH4uVkaNFLtwJoMq12eO\n" +
                "oUUBwM/bZ7SpZQThq/9nZVQ=\n" +
                "-----END CERTIFICATE-----";

        private final KeyManager keyManager;
        private final Crypto crypto;

        public DemoRKLAPI(Context context, NSDKModuleManager moduleManager) {
            super(context, moduleManager);
            this.keyManager = (KeyManager) moduleManager.getModule(ModuleType.KEY_MANAGER);
            this.crypto = (Crypto) moduleManager.getModule(ModuleType.CRYPTO);
        }

        @Override
        public String getDeviceCert(int id) {
            return ASYM_CERT;
        }

        public void installDefaultDeviceCertKey() throws RKLException {
            AsymmetricKey deviceCertKey = new AsymmetricKey();
            deviceCertKey.setKeyID((byte) CERT_PRIVATE_KEY_ID);
            deviceCertKey.setKeyUsage(AsymKeyUsage.AUTH);
            deviceCertKey.setKeyType(AsymKeyType.RSA);
            deviceCertKey.setKeyData(CERT_PRIVATE_KEY);
            deviceCertKey.setKeyLen(CERT_PRIVATE_KEY.length);
            try {
                keyManager.generateKey(KeyGenerateMethod.CLEAR, null, null, deviceCertKey, ASYM_CERT.getBytes());
            } catch (NSDKException e) {
                LoggerUtils.e("installDefaultDeviceCertKey failed!",e);
                throw new RKLException("Failed to generate cert private key.", e);
            }
        }

        @Override
        public byte[] sign(int certId, byte[] hash) throws DeviceException {
            AsymmetricKey key = new AsymmetricKey();

            // Use default cert private key
            key.setKeyID((byte) CERT_PRIVATE_KEY_ID);
            key.setKeyType(AsymKeyType.RSA);
            key.setKeyUsage(AsymKeyUsage.AUTH);

            AsymAlgorithmParameters parameters = new AsymAlgorithmParameters();
            parameters.setMessageDigestType(MessageDigestType.SHA256);
            parameters.setEncodingMode(com.newland.nsdk.core.api.common.crypto.AsymEncodingMode.PKCS_V15);
            try {
                return crypto.signAsym(key, parameters, hash);
            } catch (NSDKException e) {
                LoggerUtils.e("sign failed!",e);
                throw new DeviceException("Failed to sign data.", e);
            }
        }
    }

} 
