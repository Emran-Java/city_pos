package acquire.base.utils;


import java.text.NumberFormat;


/**
 * String utils
 *
 * @author chenkh
 * 2015-03-12
 */
public class StringUtils {

    /**
     * fill string
     * <p>e.g. ("123","0",5) -->"00123"
     *
     * @param sour    source string
     * @param fillStr filled flag
     * @param len     result length after filling
     * @param isLeft  true if fillStr is filled at left
     * @return string after filling
     */
    public static String fill(String sour, String fillStr, int len, boolean isLeft) {
        if (sour == null) {
            sour = "";
        }
        int fillLen = len - sour.length();
        StringBuilder fill = new StringBuilder();
        for (int i = 0; i < fillLen; i++) {
            fill.append(fillStr);
        }
        if (fill.length()>fillLen){
            fill.delete(fillLen,fill.length());
        }
        if (isLeft) {
            return fill + sour;
        } else {
            return sour + fill;
        }
    }

    /**
     * fill string
     * <p>e.g. ("123",5,"0","0") -->"00123"
     *
     * @param srcData    source string
     * @param destLength result length after filling
     * @param flag       filled flag
     * @param nOption    0:fill at left; 1:fill at right; 2:fill both sides
     * @return string after filling
     */
    public static String paddingString(String srcData, int destLength, String flag,
                                       int nOption) {
        int srcLength, addCharLen;

        StringBuilder strHead = new StringBuilder();
        StringBuilder strEnd = new StringBuilder();

        srcLength = srcData.length();
        if (srcLength >= destLength) {
            return srcData;
        }

        switch (nOption) {
            case 0:
                addCharLen = (destLength - srcLength) / flag.length();
                for (srcLength = 0; srcLength < addCharLen; srcLength++) {
                    strHead.append(flag);
                }
                strHead.append(srcData);
                return strHead.toString();
            case 1:
                addCharLen = (destLength - srcLength) / flag.length();
                for (srcLength = 0; srcLength < addCharLen; srcLength++) {
                    strEnd.append(flag);
                }
                return srcData + strEnd;
            case 2:
                addCharLen = (destLength - srcLength) / (flag.length() * 2);
                for (srcLength = 0; srcLength < addCharLen; srcLength++) {
                    strHead.append(flag);
                    strEnd.append(flag);
                }
                return strHead + srcData + strEnd;
            default:
                return srcData;
        }
    }

    /**
     * Int to hex string
     * <p>e.g. (123,2) --> "0123"
     */
    public static String intToBcd(int value, int bytesNum) {
        switch (bytesNum) {
            case 1:
                if (value >= 0 && value <= 99) {
                    return paddingString(String.valueOf(value), 2, "0", 0);
                }
                break;
            case 2:
                if (value >= 0 && value <= 999) {
                    return paddingString(String.valueOf(value), 4, "0", 0);
                }
                break;

            case 3:
                if (value >= 0 && value <= 999) {
                    return paddingString(String.valueOf(value), 6, "0", 0);
                }
                break;
            default:
                break;
        }

        return "";
    }

    /**
     * A Hex string is compressed into a string
     * <p>e.g. "3132" --> "12"
     */
    public static String hexToStr(String value) {
        byte[] bcdValue = BytesUtils.hexToBytes(value);
        if (bcdValue == null) {
            return null;
        }
        return new String(bcdValue);
    }

    /**
     * A string is expanded into a hex string
     * <p>e.g. "12" --> "3132"
     */
    public static String strToHex(String value) {
        if (value == null) {
            return null;
        }
        return BytesUtils.bcdToString(value.getBytes());
    }

    /**
     * Returns true if the string hexadecimal data.
     *
     * @param str the string to be examined
     * @return true if str is hexadecimal data
     */
    public static boolean checkHexStr(String str) {
        if (str == null) {
            return false;
        }
        return str.matches("^[a-f0-9A-F]+$");
    }

    /**
     * Binary string to Hex string
     * <p>e.g. "0001 0010" --> "12"
     */
    public static String binaryToHex(String binaryStr) {
        int i, j, len;
        char[] hexVocable = {'0', '1', '2', '3',
                '4', '5', '6', '7',
                '8', '9', 'A', 'B',
                'C', 'D', 'E', 'F'};
        String[] binString = {"0000", "0001", "0010", "0011",
                "0100", "0101", "0110", "0111",
                "1000", "1001", "1010", "1011",
                "1100", "1101", "1110", "1111"};

        len = binaryStr.length();
        StringBuilder result = new StringBuilder();
        for (i = 0; i < len; i += 4) {
            for (j = 0; j < 16; j++) {
                if (binString[j].equals(binaryStr.substring(i, i + 4))) {
                    result.append(hexVocable[j]);
                    break;
                }
            }
        }
        return result.toString();
    }

    /**
     * Hex String to Binary String
     * <p>e.g. "12" --> "00010010"
     */
    public static String hexToBinary(String hexStr) {
        if (hexStr == null){
            return null;
        }
        int i, j, len;
        char[] hexVocable = {'0', '1', '2', '3',
                '4', '5', '6', '7',
                '8', '9', 'A', 'B',
                'C', 'D', 'E', 'F'};
        String[] binString = {"0000", "0001", "0010", "0011",
                "0100", "0101", "0110", "0111",
                "1000", "1001", "1010", "1011",
                "1100", "1101", "1110", "1111"};

        len = hexStr.length();
        StringBuilder result = new StringBuilder();
        for (i = 0; i < len; i++) {
            for (j = 0; j < 16; j++) {
                if (hexStr.charAt(i) == hexVocable[j]) {
                    result.append(binString[j]);
                    break;
                }
            }
        }
        return result.toString();
    }

    /**
     * a double to string
     * <p> 1.20 --> "1.20"
     */
    public static String doubelToStr(double value) {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setGroupingUsed(false);
        return nf.format(value);
    }
}
