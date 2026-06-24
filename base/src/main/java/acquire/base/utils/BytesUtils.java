package acquire.base.utils;



import java.util.Arrays;

/**
 * Bytes utils
 *
 * @author Janson
 * @date 2021/3/24 15:58
 */
public class BytesUtils {
    private static final String HEX = "0123456789ABCDEF";
    private static final char[] ASCII = HEX.toCharArray();

    /**
     * A hex char to a byte .
     * <p>e.g. 'A'->10
     *
     * @param hexC hex char
     * @return a byte result
     */
    public static byte toByte(char hexC) {
        String s = String.valueOf(hexC);
        return (byte) HEX.indexOf(s.toUpperCase());
    }

    /**
     * hex string to byte[]
     * <p>e.g. hex string "12345"，isLeft is true， --> {0x01,0x23,0x45}
     *
     * @param hex    source hex string
     * @param isLeft if 0,fill 0 left.else fill 0 rigth
     * @return byte array
     */
    public static byte[] str2bcd(String hex, boolean isLeft) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        if (hex.length() % 2 != 0) {
            if (isLeft) {
                hex = "0" + hex;
            } else {
                hex += "0";
            }
        }
        return hexToBytes(hex);
    }

    /**
     * hex string to byte[]
     * <p>e.g. "1234"-->[0x12,0x34]
     */
    public static byte[] hexToBytes(String hex) {
        if (hex == null || hex.isEmpty()) {
            return null;
        }
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("input string should be any multiple of 2! wrong value is "+hex);
        }
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0, index = 0; i < hex.length(); i += 2, index++) {
            result[index] = (byte) (HEX.indexOf(hex.substring(i, i + 1).toUpperCase()) << 4
                    | HEX.indexOf(hex.substring(i + 1, i + 2).toUpperCase()));
        }
        return result;
    }

    /**
     * Bcd bytes[] to hex string
     * <p>e.g. [0x12,0x34,0x56]-->"123456"
     */
    public static String bcdToString(byte[] bcds) {
        if (bcds == null || bcds.length == 0) {
            return null;
        }
        StringBuilder res = new StringBuilder();
        for (byte bcd : bcds) {
            res.append(ASCII[(bcd >> 4) & 0x0f]);
            res.append(ASCII[bcd & 0x0f]);
        }
        return res.toString();
    }

    /**
     * A byte to int
     * <p>e.g. 0x23 ->23
     */
    public static int bcdToInt(byte value) {
        return ((value >> 4) * 10) + (value & 0x0F);
    }

    /**
     * A byte to hex string
     * <p>e.g. 0x12 -->"12"
     */
    public static String byteToHex(byte b) {
        int high = (b >> 4) & 0x0f;
        int low = b & 0x0f;
        return HEX.charAt(high) + HEX.substring(low, low + 1);
    }

    /**
     * Reverse every bit of byte array
     * <p>e.g. {0x12,0x34} -->{0xED,0xCB}
     */
    public static byte[] negate(byte[] src) {
        if (src == null || src.length == 0) {
            return null;
        }
        byte[] result = new byte[src.length];
        for (int i = 0; i < src.length; i++) {
            result[i] = (byte) (0xFF ^ src[i]);
        }
        return result;
    }


    /**
     * A byte array  xor another a byte array
     * <p>e.g. [0x01 0x02] xor [0x03 0x06] --> [0x02 0x04]
     */
    public static byte[] xor(byte[] a, byte[] b) {
        if (a == null || a.length == 0 || b == null || a.length != b.length) {
            return null;
        }
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    /**
     * Len bytes of one byte array XOR len bytes of another byte array
     * <p>e.g. len=2, [0x01 0x02 0x05] xor [0x03 0x06 0x08] --> [0x02 0x04]
     */
    public static byte[] xor(byte[] a, byte[] b, int len) {
        if (a == null || a.length == 0 || b == null || b.length == 0) {
            return null;
        }
        if (a.length < len || b.length < len) {
            return null;
        }
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }

    /**
     * long to byte[byteSize]
     * <p>e.g. (258,3) --> [0x00,0x01,0x02]
     */
    public static byte[] numberToBytes(long num, int byteSize) {
        byte[] result = new byte[byteSize];
        int length = Math.min(byteSize, 8);
        for (int i = 0; i < length; i++) {
            int leftBit = i * 8;
            result[result.length - 1 - i] = (byte) ((num >> leftBit) & 0xFF);
        }
        while (length < byteSize) {
            result[length] = 0x00;
            length++;
        }
        return result;
    }

    /**
     * Int to byte[4]
     * <p>e.g. 258 --> [0x00,0x00,0x01,0x02]
     */
    public static byte[] intToBytes(int integer) {
        return numberToBytes(integer, 4);
    }

    /**
     * Int to byte[byteSize]
     * <p>e.g.(258,2) --> [0x01,0x02]
     */
    public static byte[] intToBytes(int integer, int byteSize) {
        if (byteSize < 1 || byteSize > 4) {
            return null;
        }
        return numberToBytes(integer, byteSize);
    }

    /**
     * Long to byte[8]
     * <p>e.g.(258) --> [0x00,0x00,0x00,0x00,0x00,0x00,0x01,0x02]
     */
    public static byte[] longToBytes(long longint) {
        return numberToBytes(longint, 8);
    }


    /**
     * Byte[4] to int
     * <p>e.g. [0x01,0x02,0x03,0x04]-> 0x01*256*256*256 + 0x02*256*256 + 0x03*256 + 0x04-> 16909060
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes.length > 4) {
            throw new IllegalArgumentException("At most 4 bytes");
        }
        return (int) bytesToLong(bytes);
    }

    /**
     * Byte[8] to long
     * <p>e.g. [0x01,0x02,0x03,0x04]-> 0x01*256*256*256 + 0x02*256*256 + 0x03*256 + 0x04-> 16909060
     */
    public static long bytesToLong(byte[] bytes) {
        if (bytes.length > 8) {
            throw new IllegalArgumentException("At most 8 bytes");
        }
        int len = bytes.length;
        long result = 0;
        for (int i = len - 1; i >= 0; i--) {
            byte b = bytes[i];
            int leftBit = (len - 1 - i) * 8;
            result |= (long) (b & 0xFF) << leftBit;
        }
        return result;
    }

    /**
     * Byte[] to binary string.
     * <p>e.g. [0x01,0x02]->"00000001 00000010"
     */
    public static String bytesToBinaryString(byte[] items) {
        if (items == null || items.length == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (byte item : items) {
            sb.append(byteToBinaryString(item));
        }
        return sb.toString();
    }

    /**
     * Byte to binary string.
     * <p>e.g. 0x01->"00000001"
     */
    private static String byteToBinaryString(byte item) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            byte tmp = (byte) (0x80 >> i);
            buf.append((tmp & item) != 0 ? "1" : "0");
        }
        return buf.toString();
    }

    /**
     * xor every byte of the byte array
     *
     * <p>e.g. [0x01,0x02,0x04]->0x07
     */
    public static byte checkXorSum(byte[] bytes) {
        byte sum = 0x00;
        for (byte b : bytes) {
            sum ^= b;
        }
        return sum;
    }

    /**
     * Merge byte[] and byte
     * <p>e.g. ([0x01,0x03],0x04)->  [0x01,0x03,0x04]
     */
    public static byte[] merge(byte[] src, byte b) {
        byte[] result = Arrays.copyOf(src, src.length + 1);
        result[src.length] = b;
        return result;
    }

    /**
     * Merge two byte[]
     * <p>e.g. ([0x01,0x03],[0x04,0x05])-> [0x01,0x03,0x04,0x05]
     */
    public static byte[] merge(byte[] src, byte[]... adds) {
        byte[] result = src;
        for (byte[] add : adds) {
            int start = result.length;
            result = Arrays.copyOf(result, result.length + add.length);
            System.arraycopy(add, 0, result, start, add.length);
        }
        return result;
    }
}
