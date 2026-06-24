package acquire.sdk.emv;

import acquire.sdk.emv.bean.CapkBean;


/**
 * emv aid and capk loader
 *
 * @author Janson
 * @date 2021/12/9 15:57
 */
public interface IEmvParamLoader {

    /**
     * load contact aid
     *
     * @param aidTlv        one aid tlv data
     * @param isTerminalAid the aid is terminal default data
     * @return load result
     */
    boolean loadCtAid(String aidTlv, boolean isTerminalAid);

    /**
     * load contactless aid
     *
     * @param aidTlv        one aid tlv data
     * @param isTerminalAid the aid is terminal default data
     * @return load result
     */
    boolean loadClessAid(String aidTlv, boolean isTerminalAid);

    /**
     * load capk
     *
     * @param capkBean one capk data
     * @return load result
     */
    boolean loadCapk(CapkBean capkBean);

    /**
     * delete one contact emv aid
     *
     * @param aid9F06 the 9F06 value of the aid to be deleted.
     * @return true if The aid is deleted successfully
     */
    boolean deleteOneCtAid(String aid9F06);

    /**
     * delete one contactless emv aid
     *
     * @param aid9F06 the aid 9F06 to be deleted.
     * @return true if The aid is deleted successfully
     */
    boolean deleteOneClessAid(String aid9F06);

    /**
     * delete one emv capk
     *
     * @param rid   the capk rid to be deleted.
     * @param index the capk index to be deleted.
     * @return true if The capk is deleted successfully
     */
    boolean deleteOneCapk(String rid, String index);

    /**
     * delete all contact aid
     *
     * @return true if The all aids are deleted successfully
     */
    boolean clearCtAid();

    /**
     * delete all contactless aid
     *
     * @return true if The all aids are deleted successfully
     */
    boolean clearClessAid();

    /**
     * delete all capk
     *
     * @return true if The all capks are deleted successfully
     */
    boolean clearCapk();

    /**
     * contact aid loss
     *
     * @return true if there is no contact aid.
     */
    boolean isCtAidLoss();

    /**
     * contactless aid loss
     *
     * @return true if there is no contactless aid.
     */
    boolean isClessAidLoss();

    /**
     * capk loss
     *
     * @return true if there is no capk.
     */
    boolean isCapkLoss();


}
