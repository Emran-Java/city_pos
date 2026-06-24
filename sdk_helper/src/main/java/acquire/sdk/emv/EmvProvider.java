package acquire.sdk.emv;

import android.content.Context;

import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.emvl2.type.publickey;
import com.newland.sdk.emvl3.api.common.EmvL3Const;
import com.newland.sdk.emvl3.api.external.ExtEMVL3;
import com.newland.sdk.emvl3.api.external.configuration.ExtAID;
import com.newland.sdk.emvl3.api.external.configuration.ExtCAPK;
import com.newland.sdk.emvl3.api.internal.EmvL3;
import com.newland.sdk.emvl3.api.internal.configuration.AID;
import com.newland.sdk.emvl3.api.internal.configuration.CAPK;
import com.newland.sdk.emvl3.external.configuration.ExtAidImpl;
import com.newland.sdk.emvl3.external.configuration.ExtCapkImpl;
import com.newland.sdk.emvl3.external.transaction.ExtEMVL3Impl;
import com.newland.sdk.emvl3.internal.configuration.AidImpl;
import com.newland.sdk.emvl3.internal.configuration.CapkImpl;
import com.newland.sdk.emvl3.internal.transaction.EmvL3Impl;

import java.io.File;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;


/**
 * Emv Provider
 *
 * @author Janson
 * @date 2019/10/15 9:51
 */
public class EmvProvider {
    /**
     * Emv level3
     */
    private EmvL3 emvL3;
    private ExtEMVL3 extEmvL3;
    /**
     * OFFLINE
     */
    private publickey offPublicKey;

    private static volatile EmvProvider instance;

    private EmvProvider() {
    }

    public static EmvProvider getInstance() {
        if (instance == null) {
            synchronized (EmvProvider.class) {
                if (instance == null) {
                    instance = new EmvProvider();
                }
            }
        }
        return instance;
    }

    /**
     * init built-in emv
     */
    private void initEmv(boolean log) {
        LoggerUtils.d("[NSDK Emv Provider]--init Emv L3.");
        emvL3 = new EmvL3Impl();
        byte[] config = new byte[10];
        emvL3.configUnSet(config, EmvL3Const.CONFIG.L3_CFG_SUPPORT_EC);
        emvL3.configUnSet(config, EmvL3Const.CONFIG.L3_CFG_SUPPORT_SM);
        emvL3.configSet(config, EmvL3Const.CONFIG.L3_CFG_SUPPORT_RF_AFTERFINALSELECT);
        Context context = BaseApplication.getAppContext();
        emvL3.init(context.getFilesDir() + File.separator + "emv" + File.separator, config);
        //Internal EMV debug: 0 none, 3 debug
        emvL3.setDebugMode(log?3:0);
    }

    /**
     * init external emv
     */
    private void initExtEmv(boolean log) {
        LoggerUtils.d("[NSDK Emv Provider]--init External Emv L3.");
        extEmvL3 = new ExtEMVL3Impl();
        byte[] config = new byte[8];
        extEmvL3.configUnSet(config, EmvL3Const.CONFIG.L3_CFG_SUPPORT_EC);
        extEmvL3.configUnSet(config, EmvL3Const.CONFIG.L3_CFG_SUPPORT_SM);
        try {
            extEmvL3.init(config);
            //External EMV debug: 0 none, 3 debug
            extEmvL3.setDebugMode(log?3:0);
        } catch (NSDKException e) {
            LoggerUtils.e("extEmvL3 init failed!",e);
            extEmvL3 = null;
        }
    }

    public void openEmvl2Log(boolean emvL2log){
        initEmv(emvL2log);
    }
    public EmvL3 getEmvL3() {
        if (emvL3 == null){
            initEmv(false);
        }
        return emvL3;
    }

    public void openExtEmvl2Log(boolean emvL2log){
        initExtEmv(emvL2log);
    }
    public ExtEMVL3 getExtEmvL3() {
        if (extEmvL3 == null) {
            initExtEmv(false);
        }
        return extEmvL3;
    }

    public static void release() {
        instance = null;
    }

    public publickey getOffPublicKey() {
        return offPublicKey;
    }

    public void setOffPublicKey(publickey offPublicKey) {
        this.offPublicKey = offPublicKey;
    }


    public AID getAidLoader(boolean isContact) {
        if (emvL3 == null){
            initEmv(false);
        }
        return new AidImpl(isContact ? EmvL3Const.CardInterface.CONTACT : EmvL3Const.CardInterface.CONTACTLESS);
    }

    public CAPK getCapkLoader() {
        if (emvL3 == null){
            initEmv(false);
        }
        return new CapkImpl();
    }

    public ExtAID getExtAidLoader(boolean isContact) {
        return new ExtAidImpl(isContact ? EmvL3Const.CardInterface.CONTACT : EmvL3Const.CardInterface.CONTACTLESS);
    }

    public ExtCAPK getExtCapkLoader() {
        return new ExtCapkImpl();
    }
}
