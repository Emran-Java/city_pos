package acquire.base.utils.crypto;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.StringUtils;

/**
 * A util for PIN encryption
 *
 * @author Janson
 * @date 2022/10/10 17:02
 */
public class PinUtils {
    public static byte[] softPinBlock(byte[] pinKey, String pin, String cardNo) {
        int cardEndIndex = cardNo.length() - 1;
        int len = 8;
        String pinPan = cardNo.substring(cardEndIndex - 12, cardEndIndex);
        byte[] bPinPan = BytesUtils.str2bcd(pinPan, true);
        byte[] fillByte = new byte[len - bPinPan.length];
        byte[] pan = BytesUtils.merge(fillByte, bPinPan);
        byte bPinLen = (byte) pin.length();
        byte[] bPin = BytesUtils.str2bcd(StringUtils.fill(pin, "F", (len - 1) * 2, false), true);
        byte[] pinBlock = BytesUtils.merge(new byte[]{bPinLen}, bPin);
        byte[] xorData = BytesUtils.xor(pan, pinBlock);
        return DesUtils.softDes(pinKey, xorData);
    }
} 
