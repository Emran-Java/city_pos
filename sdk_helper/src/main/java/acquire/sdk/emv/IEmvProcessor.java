package acquire.sdk.emv;

import java.util.List;

import acquire.sdk.emv.bean.EmvLaunchParam;
import acquire.sdk.emv.listener.EmvListener;
import acquire.sdk.emv.listener.EmvSecondGacListener;

/**
 * Emv processor interface
 *
 * @author Janson
 * @date 2021/11/5 17:35
 */
public interface IEmvProcessor {
    /**
     * read emv data by tag list
     *
     * @param tags          emv tag list
     * @param isPackZeroLen true if the result includes the tag that its value is null or 0-length.
     * @return reading data list. tag+len+value
     */
    byte[] getListData(List<Integer> tags, boolean isPackZeroLen);

    /**
     * read one emv data
     *
     * @param tag emv tag
     * @return reading data.
     */
    byte[] getData(int tag);

    /**
     * set emv data
     *
     * @param tag  EMV tag to be set
     * @param data EMV data to be set
     */
    void setData(int tag, byte[] data);

    /**
     * Release EMV L3 resource
     */
    void terminateTransaction();

    /**
     * cancel emv transaction performation
     */
    void cancelEmv();

    /**
     * start emv process
     *
     * @param launchParam emv parameter
     * @param emvListener EMV read listener
     */
    void readCard(final EmvLaunchParam launchParam, final EmvListener emvListener);

    /**
     * import online response to EMV.
     *
     * @param online               If the device has made an EMV online request, it is true.
     * @param gacData              Gac request TLV data.(Valid tags: 0x89,0x71,0x72,0x91,0x8A)
     * @param emvSecondGacListener Second gac result listener
     */
    void secondGac(boolean online, byte[] gacData, EmvSecondGacListener emvSecondGacListener);


    /**
     * Get emv libs version
     */
    String getEmvApiVersion();
}
