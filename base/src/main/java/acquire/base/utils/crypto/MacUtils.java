package acquire.base.utils.crypto;

import java.util.Arrays;

import acquire.base.utils.BytesUtils;

/**
 * A util for PIN encryption
 *
 * @author Janson
 * @date 2022/10/10 17:04
 */
public class MacUtils {
    public static byte[] softMacX99(byte[] key, byte[] data) {
        if (data.length == 0) {
            return data;
        }
        int segment = 8;
        int length = data.length;
        int n = length % segment;
        if (n != 0) {
            data = Arrays.copyOf(data, length + segment - n);
            Arrays.fill(data, length, length + segment - n, (byte) 0x00);
        }
        int count = data.length / segment;
        byte[] result = new byte[segment];
        Arrays.fill(result, (byte) 0x00);
        for (int i = 0; i < count; i++) {
            byte[] tmp = Arrays.copyOfRange(data, i * segment, (i + 1) * segment);
            result = BytesUtils.xor(result, tmp);
            result = DesUtils.softDes(key, result);
        }
        return result;
    }

    public static byte[] softMacX919(byte[] key, byte[] data) {
        byte[] leftKey = Arrays.copyOfRange(key,0,8);
        byte[] rightKey = Arrays.copyOfRange(key,8,16);
        byte[] result1 = softMacX99(leftKey,data);
        byte[] result2 = DesUtils.softUndes(rightKey,result1);
        return DesUtils.softDes(leftKey,result2);
    }
} 
