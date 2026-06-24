package acquire.sdk.emv;

import com.newland.sdk.emvl3.api.common.configuration.AIDEntry;
import com.newland.sdk.emvl3.api.common.configuration.CAPKEntry;
import com.newland.sdk.emvl3.api.internal.configuration.AID;
import com.newland.sdk.emvl3.api.internal.configuration.CAPK;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.StringUtils;
import acquire.sdk.emv.bean.CapkBean;


/**
 * Internal emv aid and capk loader.The card reader must have AID and CAPK.
 * So, it is necessary to install AID and CAPK before using the card reader for the first time.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *   IEmvParamLoader loader = new BEmvParamLoader();
 *   loader.loadCtAid(defaultCtTlv,true);
 *   loader.loadCtAid(ctTlv,false);
 *   ...
 *   loader.loadClessAid(defaultClessTlv,true);
 *   loader.loadClessAid(clessTv,false);
 *   ...
 *   CapkBean capkBean = new CapkBean();
 *   capkBean.setRid(xx);
 *   ...
 *   loader.loadCapk(capkBean)
 * </pre>
 *
 * @author Janson
 * @date 2019/10/21 9:25
 */
public class BEmvParamLoader implements IEmvParamLoader {
    /**
     * Aid contact loader
     */
    private final AID aidContactLoader;
    /**
     * Aid contactless loader
     */
    private final AID aidContactlessLoader;
    /**
     * Capk loader
     */
    private final CAPK capkLoader;

    public BEmvParamLoader() {
        aidContactLoader = EmvProvider.getInstance().getAidLoader(true);
        aidContactlessLoader = EmvProvider.getInstance().getAidLoader(false);
        capkLoader = EmvProvider.getInstance().getCapkLoader();
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
            LoggerUtils.e("[NSDK EmvParamLoader]--Aid is null,load contact AID failed.");
            return false;
        }
        byte[] aidTlvBytes = BytesUtils.hexToBytes(aidTlv);
        int ret;
        if (isTerminalAid) {
            ret = aidContactLoader.loadTerminalConfig(aidTlvBytes);
        } else {
            ret = aidContactLoader.loadAID(aidTlvBytes);
        }

        if (ret == 0) {
            LoggerUtils.d("[NSDK EmvParamLoader]--load contact AID " + aidTlv + " success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--load contact AID " + aidTlv + " failed.");
        }
        return ret == 0;
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
            LoggerUtils.e("[NSDK EmvParamLoader]--Aid is null,load contactless AID failed.");
            return false;
        }
        byte[] aidTlvBytes = BytesUtils.hexToBytes(aidTlv);
        int ret;
        if (isTerminalAid) {
            ret = aidContactlessLoader.loadTerminalConfig(aidTlvBytes);
        } else {
            ret = aidContactlessLoader.loadAID(aidTlvBytes);
        }

        if (ret == 0) {
            LoggerUtils.d("[NSDK EmvParamLoader]--load contactless AID " + aidTlv + " success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--load contactless AID " + aidTlv + " failed.");
        }
        return ret == 0;
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
            LoggerUtils.e("[NSDK EmvParamLoader]--CAPK is null, load CAPK failed.");
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
        int ret = capkLoader.load(capkEntry);
        if (ret == 0) {
            LoggerUtils.d("[NSDK EmvParamLoader]--load CAPK(index = " + capkBean.getIndex() + ",rid = " + capkBean.getRid() + ") success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--load CAPK(index = " + capkBean.getIndex() + ",rid = " + capkBean.getRid() + ") failed.");
        }
        return ret == 0;
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
            LoggerUtils.e("[NSDK EmvParamLoader]--delete one contact aid failed, because aid is null.");
            return false;
        }
        byte[] b9f06 = BytesUtils.str2bcd(aid9F06, true);
        AIDEntry aidEntry = new AIDEntry();
        aidEntry.setAidLen(b9f06.length);
        aidEntry.setAid(b9f06);
        boolean result = aidContactLoader.remove(aidEntry);
        if (result) {
            LoggerUtils.d("[NSDK EmvParamLoader]--delete one contact aid[" + aid9F06 + "] success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--delete one contact aid[" + aid9F06 + "] failed.");
        }
        return result;
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
            LoggerUtils.e("[NSDK EmvParamLoader]--delete one contactless aid failed, because aid is null.");
            return false;
        }
        byte[] b9f06 = BytesUtils.str2bcd(aid9F06, true);
        AIDEntry aidEntry = new AIDEntry();
        aidEntry.setAidLen(b9f06.length);
        aidEntry.setAid(b9f06);
        boolean result = aidContactlessLoader.remove(aidEntry);
        if (result) {
            LoggerUtils.d("[NSDK EmvParamLoader]--delete one contactless aid[" + aid9F06 + "] success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--delete one contactless aid[" + aid9F06 + "] failed.");
        }
        return result;
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
            LoggerUtils.e("[NSDK EmvParamLoader]--delete one capk failed,because capkRid or capkIndex is null.");
            return false;
        }
        boolean result = capkLoader.remove(BytesUtils.str2bcd(rid, true), BytesUtils.str2bcd(index, true)[0]);
        if (result) {
            LoggerUtils.d("[NSDK EmvParamLoader]--delete one capk[rid= " + rid + ",index= " + index + "] success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--delete one capk[rid= " + rid + ",index= " + index + "] failed.");
        }
        return result;
    }

    /**
     * delete all contact aid
     *
     * @return true if The all aids are deleted successfully
     */
    @Override
    public boolean clearCtAid() {
        boolean result = aidContactLoader.flush();
        if (result) {
            LoggerUtils.d("[NSDK EmvParamLoader]--clear all contact aids success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--clear all contact aids failed.");
        }
        return result;
    }

    /**
     * delete all contactless aid
     *
     * @return true if The all aids are deleted successfully
     */
    @Override
    public boolean clearClessAid() {
        boolean result = aidContactlessLoader.flush();
        if (result) {
            LoggerUtils.d("[NSDK EmvParamLoader]--clear all contactless aids success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--clear all contactless aids failed.");
        }
        return result;
    }

    /**
     * delete all capk
     *
     * @return true if The all capks are deleted successfully
     */
    @Override
    public boolean clearCapk() {
        boolean result = capkLoader.flush();
        if (result) {
            LoggerUtils.d("[NSDK EmvParamLoader]--clear all capks success.");
        } else {
            LoggerUtils.e("[NSDK EmvParamLoader]--clear all capks failed.");
        }
        return result;
    }

    /**
     * contact aid loss
     *
     * @return true if there is no contact aid.
     */
    @Override
    public boolean isCtAidLoss() {
        return aidContactLoader.getAidCount() <= 0;
    }

    /**
     * contactless aid loss
     *
     * @return true if there is no contactless aid.
     */
    @Override
    public boolean isClessAidLoss() {
        return aidContactlessLoader.getAidCount() <= 0;
    }

    /**
     * capk loss
     *
     * @return true if there is no capk.
     */
    @Override
    public boolean isCapkLoss() {
        return capkLoader.getCapkCount() <= 0;
    }


}
