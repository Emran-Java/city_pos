package acquire.sdk.emv;

import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.sdk.emvl3.api.common.configuration.AIDEntry;
import com.newland.sdk.emvl3.api.common.configuration.CAPKEntry;
import com.newland.sdk.emvl3.api.external.configuration.ExtAID;
import com.newland.sdk.emvl3.api.external.configuration.ExtCAPK;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.StringUtils;
import acquire.sdk.emv.bean.CapkBean;


/**
 * external emv aid and capk loader.The card reader must have AID and CAPK.
 * So, it is necessary to install AID and CAPK before using the card reader for the first time.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *    IEmvParamLoader loader = new BExtEmvParamLoader();
 *    loader.loadCtAid(defaultCtTlv,true);
 *    loader.loadCtAid(ctTlv,false);
 *    ...
 *    loader.loadClessAid(defaultClessTlv,true);
 *    loader.loadClessAid(clessTv,false);
 *    ...
 *    CapkBean capkBean = new CapkBean();
 *    capkBean.setRid(xx);
 *    ...
 *    loader.loadCapk(capkBean)
 * </pre>
 *
 * @author Janson
 * @date 2021/12/9 15:57
 */
public class BExtEmvParamLoader implements IEmvParamLoader {
    /**
     * Aid contact loader
     */
    private final ExtAID aidContactLoader;
    /**
     * Aid contactless loader
     */
    private final ExtAID aidContactlessLoader;
    /**
     * Capk loader
     */
    private final ExtCAPK capkLoader;

    public BExtEmvParamLoader() {
        aidContactLoader = EmvProvider.getInstance().getExtAidLoader(true);
        aidContactlessLoader = EmvProvider.getInstance().getExtAidLoader(false);
        capkLoader = EmvProvider.getInstance().getExtCapkLoader();
    }


    /**
     * load contact aid
     *
     * @param aidTlv        one aid tlv data
     * @param isTerminalAid the aid is terminal default data
     * @return load result
     */
    @Override
    public boolean loadCtAid(String aidTlv, boolean isTerminalAid) {
        if (aidTlv == null) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--Aid is null,load contact AID failed.");
            return false;
        }
        byte[] aidTlvBytes = BytesUtils.hexToBytes(aidTlv);
        try {
            if (isTerminalAid) {
                aidContactLoader.loadTerminalConfig(aidTlvBytes);
            } else {
                aidContactLoader.loadAID(aidTlvBytes);
            }
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--load contact AID " + aidTlv + " success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--load contact AID " + aidTlv + " failed.");
            return false;
        }
    }

    /**
     * load contactless aid
     *
     * @param aidTlv        one aid tlv data
     * @param isTerminalAid the aid is terminal default data
     * @return load result
     */
    @Override
    public boolean loadClessAid(String aidTlv, boolean isTerminalAid) {
        if (aidTlv == null) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--Aid is null,load contactless AID failed.");
            return false;
        }
        byte[] aidTlvBytes = BytesUtils.hexToBytes(aidTlv);
        try {
            if (isTerminalAid) {
                aidContactlessLoader.loadTerminalConfig(aidTlvBytes);
            } else {
                aidContactlessLoader.loadAID(aidTlvBytes);
            }
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--load contactless AID " + aidTlv + " success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--load contactless AID " + aidTlv + " failed.");
            return false;
        }
    }

    /**
     * load capk
     *
     * @param capkBean one capk data
     * @return load result
     */
    @Override
    public boolean loadCapk(CapkBean capkBean) {
        if (capkBean == null) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--CAPK is null, load CAPK failed.");
            return false;
        }
        CAPKEntry capkEntry = new CAPKEntry();
        capkEntry.setIndex(capkBean.getIndex());
        capkEntry.setRID(BytesUtils.hexToBytes(capkBean.getRid()));
        capkEntry.setHash(BytesUtils.hexToBytes(capkBean.getHash()));
        capkEntry.setExponent(BytesUtils.hexToBytes(StringUtils.fill(capkBean.getExponent(), "0", 6, true)));
        capkEntry.setModulus(BytesUtils.hexToBytes(capkBean.getModulus()));
        capkEntry.setModuleLen(capkEntry.getModulus().length);
        capkEntry.setHashAlgorithm((byte) capkBean.getHashAlgorithm());
        capkEntry.setAlgorithmIndicator((byte) capkBean.getAlgorithmIndicator());
        try {
            capkLoader.load(capkEntry);
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--load CAPK(index = " + capkBean.getIndex() + ",rid = " + capkBean.getRid() + ") success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--load CAPK(index = " + capkBean.getIndex() + ",rid = " + capkBean.getRid() + ") failed.", e);
            return false;
        }
    }

    /**
     * delete one contact emv aid
     *
     * @param aid9F06 the aid 9F06 to be deleted.
     * @return true if The aid is deleted successfully
     */
    @Override
    public boolean deleteOneCtAid(String aid9F06) {
        if (null == aid9F06) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--delete one contact aid failed, because aid is null.");
            return false;
        }
        byte[] b9f06 = BytesUtils.str2bcd(aid9F06, true);
        AIDEntry aidEntry = new AIDEntry();
        aidEntry.setAidLen(b9f06.length);
        aidEntry.setAid(b9f06);
        try {
            aidContactLoader.remove(aidEntry);
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--delete one contact aid[" + aid9F06 + "] success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--delete one contact aid[" + aid9F06 + "] failed.");
            return false;
        }
    }

    /**
     * delete one contactless emv aid
     *
     * @param aid9F06 the aid 9F06 to be deleted.
     * @return true if The aid is deleted successfully
     */
    @Override
    public boolean deleteOneClessAid(String aid9F06) {
        if (null == aid9F06) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--delete one contactless aid failed, because aid is null.");
            return false;
        }
        byte[] b9f06 = BytesUtils.str2bcd(aid9F06, true);
        AIDEntry aidEntry = new AIDEntry();
        aidEntry.setAidLen(b9f06.length);
        aidEntry.setAid(b9f06);
        try {
            aidContactlessLoader.remove(aidEntry);
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--delete one contactless aid[" + aid9F06 + "] success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--delete one contactless aid[" + aid9F06 + "] failed.");
            return false;
        }
    }

    /**
     * delete one emv capk
     *
     * @param rid   the capk rid to be deleted.
     * @param index the capk index to be deleted.
     * @return true if The capk is deleted successfully
     */
    @Override
    public boolean deleteOneCapk(String rid, String index) {
        if (rid == null || index == null) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--delete one capk failed,because capkRid or capkIndex is null.");
            return false;
        }
        try {
            capkLoader.remove(BytesUtils.str2bcd(rid, true), BytesUtils.str2bcd(index, true)[0]);
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--delete one capk[rid= " + rid + ",index= " + index + "] success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--delete one capk[rid= " + rid + ",index= " + index + "] failed.",e);
            return false;
        }
    }

    /**
     * delete all contact aid
     *
     * @return true if The all aids are deleted successfully
     */
    @Override
    public boolean clearCtAid() {
        try {
            aidContactLoader.flush();
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--clear all contact aids success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--clear all contact aids failed.", e);
            return false;
        }
    }

    /**
     * delete all contactless aid
     *
     * @return true if The all aids are deleted successfully
     */
    @Override
    public boolean clearClessAid() {
        try {
            aidContactlessLoader.flush();
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--clear all contactless aids success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--clear all contactless aids failed.", e);
            return false;
        }
    }

    /**
     * delete all capk
     *
     * @return true if The all capks are deleted successfully
     */
    @Override
    public boolean clearCapk() {
        try {
            capkLoader.flush();
            LoggerUtils.d("[NSDK ExtEmvParamLoader]--clear all capks success.");
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtEmvParamLoader]--clear all capks failed.", e);
            return false;
        }
    }

    /**
     * contact aid loss
     *
     * @return true if there is no contact aid.
     */
    @Override
    public boolean isCtAidLoss() {
        try {
            return aidContactLoader.getAidCount() <= 0;
        } catch (Exception e) {
            LoggerUtils.e("Ext Contact getAidCount failed!",e);
            return false;
        }
    }

    /**
     * contactless aid loss
     *
     * @return true if there is no contactless aid.
     */
    @Override
    public boolean isClessAidLoss() {
        try {
            return aidContactlessLoader.getAidCount() <= 0;
        } catch (Exception e) {
            LoggerUtils.e("Ext Contactless getAidCount failed!",e);
            return false;
        }
    }

    /**
     * capk loss
     *
     * @return true if there is no capk.
     */
    @Override
    public boolean isCapkLoss() {
        try {
            return capkLoader.getCapkCount() <= 0;
        } catch (Exception e) {
            LoggerUtils.e("Ext getCapkCount failed!",e);
            return false;
        }
    }


}
