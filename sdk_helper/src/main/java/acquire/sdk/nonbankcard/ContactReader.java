package acquire.sdk.nonbankcard;

import com.newland.nsdk.core.api.common.card.contact.ContactCardSlot;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.card.contact.CPUContactCard;
import com.newland.nsdk.core.internal.card.contact.CPUContactCardImpl;

import acquire.base.utils.LoggerUtils;

/**
 * A contact card reader
 *
 * @author Janson
 * @date 2022/11/22 15:34
 * @since 3.7
 */
public class ContactReader {
    private final CPUContactCard cpuContactCard;

    public ContactReader() {
        //see ContactCardSlot
        cpuContactCard = new CPUContactCardImpl(ContactCardSlot.IC1);
    }

    /**
     * power up card
     *
     * @return ATR data,if success; else ,null
     */
    public byte[] powerUp() {
        try {
            return cpuContactCard.powerUp();
        } catch (NSDKException e) {
            LoggerUtils.e("ContactReader powerUp failed!",e);
            return null;
        }
    }

    /**
     * power down card
     */
    public boolean powerDown() {
        try {
            cpuContactCard.powerDown();
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("ContactReader powerDown failed!",e);
            return false;
        }
    }

    /**
     * Send apdu command
     *
     * @param command APDU command request data
     * @return APDU response data.
     */
    public byte[] sendApdu(byte[] command) {
        try {
            return cpuContactCard.performAPDU(command);
        } catch (NSDKException e) {
            LoggerUtils.e("ContactReader sendApdu failed!",e);
            return null;
        }
    }
}
