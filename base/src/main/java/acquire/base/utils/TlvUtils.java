package acquire.base.utils;


import android.util.SparseArray;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



/**
 * TLV utils
 *
 * @author spy
 * @version 1.0
 */
public class TlvUtils {

    /**
     * Parse a string into a tlv string array
     * <p>e.g.：9F0605A0000000039F220101DF060100 -->{9F0605A000000003,9F220101,DF060100}
     */
    public static String[] getTlvList(String tlvs) {
        int current = 0;
        int lenValue = 0;
        int pre = 0;
        int tagLen;
        byte[] data = BytesUtils.hexToBytes(tlvs);

        List<String> list = new ArrayList<>();
        while (current < data.length) {
            tagLen = getTagLen(data, current);

            current += tagLen;
            if ((data[current] & 0x80) == 0x80) {
                int tmpLen = data[current] & 0x7F;
                switch (tmpLen) {
                    case 1:
                        lenValue = data[current + 1] & 0xFF;
                        break;
                    case 2:
                        lenValue = (data[current + 1] << 8) & 0xFF00 + (data[current + 2] & 0xFF);
                        break;
                    case 3:
                        lenValue = (data[current + 1] << 16) & 0xFF0000 + (data[current + 2] << 8) & 0xFF00 + (data[current + 3] & 0xFF);
                        break;
                    default:
                        break;
                }
                current += tmpLen + 1;
            } else {
                lenValue = data[current] & 0xFF;
                current += 1;
            }

            current += lenValue;
            byte[] tmp = Arrays.copyOfRange(data,pre,current);
            list.add(BytesUtils.bcdToString(tmp));
            pre = current;
        }

        return list.toArray(new String[0]);
    }

    /**
     * Parse byte array into a {@link SparseArray}
     * <p>e.g.：9F0605A0000000039F220101DF060100 -->{(0x9F06,A000000003),(0x9F22,01),(0xDF06,00)}
     */
    public static SparseArray<byte[]> getTlvList(byte[] tlvData) {
        if (tlvData == null){
            return null;
        }
        int current = 0;
        int tagLen;
        int lenValue = 0;
        SparseArray<byte[]> sparseArray = new SparseArray<>();
        while (current < tlvData.length) {
            tagLen = getTagLen(tlvData, current);
            byte[] tmp = Arrays.copyOfRange(tlvData,current,current+tagLen);

            current += tagLen;
            if ((tlvData[current] & 0x80) == 0x80) {
                int tmpLen = tlvData[current] & 0x7F;
                switch (tmpLen) {
                    case 1:
                        lenValue = tlvData[current + 1] & 0xFF;
                        break;
                    case 2:
                        lenValue = (tlvData[current + 1] << 8) & 0xFF00 + (tlvData[current + 2] & 0xFF);
                        break;
                    case 3:
                        lenValue = (tlvData[current + 1] << 16) & 0xFF0000 + (tlvData[current + 2] << 8) & 0xFF00 + (tlvData[current + 3] & 0xFF);
                        break;
                    default:
                        break;
                }
                current += tmpLen + 1;
            } else {
                lenValue = tlvData[current] & 0xFF;
                current += 1;
            }

            byte[] value = new byte[lenValue];
            System.arraycopy(tlvData, current, value, 0, lenValue);
            current += lenValue;
            sparseArray.append(BytesUtils.bytesToInt(tmp),value);
        }

        return sparseArray;
    }


    /**
     * Get tag length
     *
     * @param input  tlv content
     * @param offset TAG index
     * @return tag length
     */
    private static int getTagLen(byte[] input, int offset) {
        int tagLen = 1;
        for (int i = 0; i < 2; i++) {
            byte b = input[i + offset];
            if ((b & 0x1F) == 0x1F) {
                tagLen++;
            } else {
                break;
            }
        }
        return tagLen;
    }

    /**
     * Get tag length
     *
     * @param input  tlv content
     * @return tag length
     */
    public static int getTagLen(String input) {
        int tagLen = 1;
        for (int i = 0; i < 2; i++) {
            int b = Integer.valueOf(input.substring(i * 2, i * 2 + 2), 16);
            if ((b & 0x1F) == 0x1F) {
                tagLen++;
            } else {
                break;
            }
        }
        return tagLen;
    }

    public static int getTagLen(int tag) {
        for (int i = 2; i > 0; i--) {
            if (((tag >> (i * 8)) & 0xFF) != 0) {
                return i + 1;
            }
        }
        return 1;
    }

    /**
     * Create a {@link PackTlv}
     */
    public static PackTlv newPackTlv() {
        return new PackTlv();
    }


    public static class PackTlv {
        private final ByteArrayOutputStream bos ;

        private PackTlv() {
            bos = new ByteArrayOutputStream();
        }

        /**
         * Add tlv
         *
         * @param tag   tlv-tag
         * @param value tlv-value
         */
        public void append(int tag, byte[] value) {
            if (value == null) {
                LoggerUtils.e("Tlv append->value is null.");
                return;
            }
            byte tmp;
            int i;
            boolean mark = false;
            for (i = 3; i >= 0; i--) {
                tmp = (byte) ((tag >> (i * 8)) & 0xFF);
                if (tmp != 0) {
                    mark = true;
                }
                if (mark) {
                    bos.write(tmp);
                }
            }

            int len = value.length;

            if (len <= 0x7F) {
                bos.write(len);
            } else {
                for (i = 3; i >= 0; i--) {
                    tmp = (byte) ((len >> (i * 8)) & 0xFF);
                    if (tmp != 0) {
                        break;
                    }
                }

                tmp = (byte) (0x80 | i + 1);
                bos.write(tmp);

                for (; i >= 0; i--) {
                    tmp = (byte) ((len >> (i * 8)) & 0xFF);
                    bos.write(tmp);
                }
            }

            try {
                bos.write(value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        /**
         * Pack all tlv
         *
         * @return packet data
         */
        public byte[] pack() {
            try {
                return bos.toByteArray();
            } finally {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}



