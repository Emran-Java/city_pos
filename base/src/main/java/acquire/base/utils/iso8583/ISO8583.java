package acquire.base.utils.iso8583;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.StringUtils;


/**
 * ISO8583 Utils
 *
 * @author Janson
 * @date 2021/3/23 17:32
 */
public class ISO8583 {
    private static volatile ISO8583 defaultInstance;

    private final static int TYPE_ASCII = 0, TYPE_BCD = 1, TYPE_BINARY = 2;

    private final IsoFormat[] isoFormats = new IsoFormat[129];
    private final IsoField[] isoFields = new IsoField[129];
    private final IsoFormat bitmapFormat = new IsoFormat();
    private final IsoField bitmapField = new IsoField();

    private static boolean isLogOnTran = false;//if we fix 62 field issue, we want set this false.
    // And we should call setField62FormatForLogOn() function with pass true flag before perform LogOn, InstallmentLogOn

    public String getBitmapValue() {
        return bitmapField.data;
    }

    public static void setField62FormatForLogOn(boolean isLogOnTransaction) {
        isLogOnTran = isLogOnTransaction;
    }

    /**
     * Get default {@link ISO8583} object
     */
    public static ISO8583 getDefault() {
        if (defaultInstance == null) {
            synchronized (ISO8583.class) {
                if (defaultInstance == null) {
                    defaultInstance = new ISO8583();
                }
            }
        }
        return defaultInstance;
    }

    public ISO8583() {
        for (int i = 0; i < isoFormats.length; i++) {
            isoFormats[i] = new IsoFormat();
        }
        for (int i = 0; i < isoFields.length; i++) {
            isoFields[i] = new IsoField();
        }
    }

    /**
     * Load 8583 configur xml
     *
     * @param xmlFilename xml asset file name
     */
    public boolean loadXmlFile(Context context, String xmlFilename) {
        LoggerUtils.d("start to parse ISO8583 configuration xml.");
        XmlNode node = new XmlNode();
        try (InputStream ins = context.getAssets().open(xmlFilename)) {
            node.decodeXml(ins);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        //field format
        XmlNode fieldSetNode = node.getChild("FIELD_SETTING");
        if (fieldSetNode == null) {
            // no continue
            return false;
        }
        XmlNode filedNode = fieldSetNode.getChild("BITMAP");
        if (filedNode != null) {
            boolean valid = setIsoFormat(filedNode, bitmapFormat);
            if (valid) {
                LoggerUtils.d("BITMAP   >> " + bitmapFormat);
            } else {
                LoggerUtils.e("BITMAP setIsoFormat(XmlNode,IsoFormat) failed >> " + bitmapFormat);
                return false;
            }
        } else {
            LoggerUtils.e("BITMAP configuration not found");
            return false;
        }

        for (int i = 0; i < isoFormats.length; i++) {
            String fieldName = String.format(Locale.US, "FIELD%03d", i);
            filedNode = fieldSetNode.getChild(fieldName);
            if (filedNode != null) {
                boolean valid = setIsoFormat(filedNode, isoFormats[i]);
                if (valid) {
                    LoggerUtils.d(String.format(Locale.US, "FIELD%03d >> %s", i, isoFormats[i]));
                } else {
                    LoggerUtils.e(String.format(Locale.US, "FIELD%03d setIsoFormat(XmlNode,IsoFormat) failed >> %s", i, isoFormats[i]));
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * Init pack
     */
    public void initPack() {
        for (IsoField isoField : isoFields) {
            isoField.data = null;
            isoField.isExist = false;
        }
        bitmapField.data = null;
        bitmapField.isExist = false;
    }

    private boolean setIsoFormat(XmlNode node, IsoFormat fieldFormat) {
        Map<String, String> atrrs = node.getAttrs();
        fieldFormat.fill = atrrs.get("fill");
        String type = atrrs.get("type");
        if (type == null) {
            LoggerUtils.e("node[type] is wrong.");
            return false;
        }
        type = type.toUpperCase();
        switch (type) {
            case "BCD":
                fieldFormat.type = TYPE_BCD;
                if (TextUtils.isEmpty(fieldFormat.fill)) {
                    fieldFormat.fill = "0";
                }
                break;
            case "BINARY":
                fieldFormat.type = TYPE_BINARY;
                if (TextUtils.isEmpty(fieldFormat.fill)) {
                    fieldFormat.fill = "0";
                }
                break;
            case "ASCII":
                fieldFormat.type = TYPE_ASCII;
                if (TextUtils.isEmpty(fieldFormat.fill)) {
                    fieldFormat.fill = " ";
                }
                break;
            default:
                LoggerUtils.e("node type[" + type + "] is wrong.");
                return false;
        }
        String length = atrrs.get("length");
        if (length == null) {
            LoggerUtils.e("node[length] is wrong.");
            return false;
        }
        length = length.toUpperCase();
        switch (length) {
            case "LLVAR":
                fieldFormat.lengthVar = true;
                fieldFormat.maxLen = 99;
                break;
            case "LLLVAR":
                fieldFormat.lengthVar = true;
                fieldFormat.maxLen = 999;
                break;
            default:
                fieldFormat.lengthVar = false;
                try {
                    fieldFormat.maxLen = Integer.parseInt(length);
                } catch (Exception e) {
                    LoggerUtils.e("node length[" + length + "] is wrong.");
                    return false;
                }
                break;
        }
        String align = atrrs.get("align");
        if (align == null) {
            LoggerUtils.e("node[align] is wrong.");
            return false;
        }
        align = align.toUpperCase();
        switch (align) {
            case "RIGHT":
                fieldFormat.alignRight = true;
                break;
            case "LEFT":
                fieldFormat.alignRight = false;
                break;
            default:
                LoggerUtils.e("node align[" + align + "] is wrong.");
                return false;
        }
        return true;
    }

    /**
     * Get bitmap
     */
    public char[] getIsoBitmap() {
        String bitmap = bitmapField.data;
        String binary = StringUtils.hexToBinary(bitmap);
        return binary.toCharArray();
    }

    /**
     * Get secondary bitmap
     */
    public char[] getSecondaryBitmap() {
        String bitmap = getField(1);
        if (TextUtils.isEmpty(bitmap)) {
            bitmap = StringUtils.fill("0", "0", 64, true);
        }
        String binary = StringUtils.hexToBinary(bitmap);
        return binary.toCharArray();
    }


    /**
     * Set ISO8583 field value
     *
     * @param index field index
     * @param value field value
     */
    public void setField(int index, String value) {
        if (index < 0 || index >= isoFields.length) {
            return;
        }
        isoFields[index].data = value;
        isoFields[index].isExist = true;
        if (index >= 1 && isoFields[index].data != null) {
            addFieldToBitmap(index, true);
        }
    }

    /**
     * Get ISO8583 field value
     *
     * @param index field index
     * @return field value
     */
    public String getField(int index) {
        if (index < 0 || index > isoFields.length) {
            return null;
        }
        return isoFields[index].data;

    }

    /**
     * Pack field
     *
     * @param fieldTag  field tag for log.
     * @param value     field value (HEX)
     * @param isoFormat field format
     * @return field value(HEX) after dealing
     * @throws ISO8583Exception iso8583 pack error
     */
    private String packField(String fieldTag, String value, IsoFormat isoFormat) throws ISO8583Exception {
        int dataLength = value.length();

        //Brac LogOn field 62
        /*if (isLogOnTran && fieldTag.equals("62")) {
            isLogOnTran = false;
            isoFormat.type = TYPE_BINARY;
        } else if (!isLogOnTran && fieldTag.equals("62")) {
            isoFormat.type = TYPE_ASCII;
        }*/

        if (isoFormat.type == TYPE_BCD) {
            //value isn't hex error
            if (!StringUtils.checkHexStr(value)) {
                throw new ISO8583Exception(fieldTag, "【Pack】Field [" + fieldTag + "] isn't in Hex format." + "["
                        + value + "]");
            }
        } else if (isoFormat.type == TYPE_BINARY) {
            //length isn't multiple of 2, error
            if (dataLength % 2 != 0) {
                throw new ISO8583Exception(fieldTag, "【Pack】Field [" + fieldTag + "] length isn't an integral multiple of 2." + "["
                        + value + "]");
            }
            dataLength = (dataLength + 1) / 2;
        }
        if (dataLength > isoFormat.maxLen) {
            //length too long error
            throw new ISO8583Exception(fieldTag, "【Pack】Field[" + fieldTag + "] exceeds the maximum limit. Max length is " + isoFormat.maxLen + ",["
                    + value + "]");
        }

        if (isoFormat.lengthVar) {
            String strLen;
            int fieldLen = value.length();
            String result;
            switch (isoFormat.type) {
                case TYPE_ASCII:
                    strLen = StringUtils.intToBcd(fieldLen, isoFormat.maxLen > 99 ? 2 : 1);
                    result = StringUtils.strToHex(value);
                    break;
                case TYPE_BINARY:
                    int binFieldLen = (fieldLen + 1) / 2;
                    strLen = StringUtils.intToBcd(binFieldLen, isoFormat.maxLen > 99 ? 2 : 1);
                    result = value;
                    break;
                case TYPE_BCD:
                    strLen = StringUtils.intToBcd(fieldLen, isoFormat.maxLen > 99 ? 2 : 1);
                    int bcdFieldLen = ((fieldLen + 1) / 2) * 2;
                    result = StringUtils.paddingString(value, bcdFieldLen, isoFormat.fill, isoFormat.alignRight ? 0 : 1);
                    break;
                default:
                    throw new ISO8583Exception(fieldTag, "【Pack】Field[" + fieldTag + "] XML configuration file 'type' is wrong.");
            }
            return strLen + result;
        } else {
            String result;
            switch (isoFormat.type) {
                case TYPE_ASCII:
                    result = StringUtils.paddingString(value, isoFormat.maxLen, isoFormat.fill, isoFormat.alignRight ? 0 : 1);
                    result = StringUtils.strToHex(result);
                    break;
                case TYPE_BINARY:
                    result = StringUtils.paddingString(value, isoFormat.maxLen * 2, isoFormat.fill, isoFormat.alignRight ? 0 : 1);
                    break;
                case TYPE_BCD:
                    int bcdFieldLen = ((isoFormat.maxLen + 1) / 2) * 2;
                    result = StringUtils.paddingString(value, bcdFieldLen, isoFormat.fill, isoFormat.alignRight ? 0 : 1);
                    break;
                default:
                    throw new ISO8583Exception(fieldTag, "【Pack】Field[" + fieldTag + "] XML configuration file 'type' is wrong.");
            }
            return result;
        }
    }


    /**
     * set bitmap field
     *
     * @param fieldIndex standard field index
     * @param exit       true if it exists.
     */
    public void addFieldToBitmap(int fieldIndex, boolean exit) {
        String strBitmap;
        if (fieldIndex > 64) {
            //secondary bitmap
            strBitmap = getField(1);
        } else {
            //primary bitmap
            strBitmap = bitmapField.data;
        }
        String bitmap = StringUtils.hexToBinary(strBitmap);
        if (bitmap == null) {
            bitmap = StringUtils.fill("", "0", 64, true);
        }
        char[] bits = bitmap.toCharArray();
        if (fieldIndex > 64) {
            //secondary bitmap
            bits[fieldIndex - 64 - 1] = exit ? '1' : '0';
            String newStrBitmap = StringUtils.binaryToHex(new String(bits));
            setField(1, newStrBitmap);
        } else {
            //primary bitmap
            if (fieldIndex > 0) {
                bits[fieldIndex - 1] = exit ? '1' : '0';
            }
            bitmapField.data = StringUtils.binaryToHex(new String(bits));
            bitmapField.isExist = true;
        }
    }


    /**
     * Pack 8583(HEX)
     *
     * @return iso8583 data
     * @throws ISO8583Exception pack error
     */
    public String pack() throws ISO8583Exception {
        StringBuilder packet = new StringBuilder();
        for (int index = 0; index < isoFields.length; index++) {
            if (!isoFields[index].isExist) {
                continue;
            }
            if (isoFields[index].data == null) {
                LoggerUtils.e(String.format(Locale.US, "【Pack】Field [%3d] :null", index));
                continue;
            }
            //pack field
            String field = packField(index + "", isoFields[index].data, isoFormats[index]);
            packet.append(field);

            //Brac: LogOn.
            /*if (isLogOnTran && index == 62) {
                isLogOnTran = false;
                isoFormats[index].type = TYPE_BINARY;
            } else if (!isLogOnTran && index == 62) {
                isoFormats[index].type = TYPE_ASCII;
            }*/
            //--------------------------

            if (isoFormats[index].type == TYPE_ASCII) {
                LoggerUtils.d(String.format(Locale.US, "【Pack】Field [%3d] :[%s] ==>%s", index, field, isoFields[index].data));
            } else {
                LoggerUtils.d(String.format(Locale.US, "【Pack】Field [%3d] :[%s]", index, field));
            }
            if (index == 0) {
                //pack bitmap field
                String bitmap = packField("bitmap", bitmapField.data, bitmapFormat);
                packet.append(bitmap);
                if (bitmapFormat.type == TYPE_ASCII) {
                    LoggerUtils.d(String.format(Locale.US, "【Pack】Bitmap      :[%s] ==>%s", bitmap, bitmapField.data));
                } else {
                    LoggerUtils.d(String.format(Locale.US, "【Pack】Bitmap      :[%s]", bitmap));
                }
            }
        }
        return packet.toString();
    }

    /**
     * pack specific fields for mac for Field64
     *
     * @return the data to be mac encrypted
     */
    public String getMacSrcData() throws ISO8583Exception {
        StringBuilder packet = new StringBuilder();
        boolean hasAddBitmap = false;
        for (int index = 0; index <= 63; index++) {
            IsoField isoField = isoFields[index];
            if (!isoField.isExist || isoField.data == null) {
                continue;
            }
            //pack field
            String field = packField(index + "", isoField.data, isoFormats[index]);
            if (!hasAddBitmap) {
                String bitmap = packField("bitmap", bitmapField.data, bitmapFormat);
                //pack bitmap field
                if (index == 0) {
                    packet.append(field);
                    packet.append(bitmap);
                } else {
                    packet.append(bitmap);
                    packet.append(field);
                }
                hasAddBitmap = true;
            } else {
                packet.append(field);
            }
        }
        return packet.toString();
    }

    /**
     * pack specific fields for mac for Field128
     *
     * @return the data to be mac encrypted
     */
    public String getMac2SrcData() throws ISO8583Exception {
        StringBuilder packet = new StringBuilder();
        boolean hasAddBitmap = false;
        for (int index = 0; index <= 127; index++) {
            IsoField isoField = isoFields[index];
            if (!isoField.isExist || isoField.data == null) {
                continue;
            }
            //pack field
            String field = packField(index + "", isoField.data, isoFormats[index]);
            if (!hasAddBitmap) {
                String bitmap = packField("bitmap", bitmapField.data, bitmapFormat);
                //pack bitmap field
                if (index == 0) {
                    packet.append(field);
                    packet.append(bitmap);
                } else {
                    packet.append(bitmap);
                    packet.append(field);
                }
                hasAddBitmap = true;
            } else {
                packet.append(field);
            }
        }
        return packet.toString();
    }

    /**
     * Unpack field
     *
     * @param hexPacket  8583 packet data(HEX)
     * @param fieldTag   field tag for log.
     * @param currentPos This current index of hexPacket
     * @param isoFormat  field format
     * @return New index of hexPacket
     * @throws ISO8583Exception iso8583 unpack error
     */
    private int unpackField(String hexPacket, String fieldTag, int currentPos, IsoFormat isoFormat, IsoField outField) throws ISO8583Exception {
        //for fix LogOn feature. LogOn feature want
        String tmpField = fieldTag;
        tmpField = tmpField.replace(" ", "");

       /* if (isLogOnTran && tmpField.equals("62")) {
            isLogOnTran = false;
            isoFormat.type = TYPE_BINARY;
        } else if (!isLogOnTran && tmpField.equals("62")) {
            isoFormat.type = TYPE_ASCII;
        }*/


        String hexValue;
        String value;
        int fieldLen;
        if (isoFormat.lengthVar) {
            //var length
            int lengthBytes = isoFormat.maxLen > 99 ? 4 : 2;
            fieldLen = Integer.parseInt(hexPacket.substring(currentPos, currentPos + lengthBytes));
            currentPos += lengthBytes;
            if (fieldLen > isoFormat.maxLen) {
                throw new ISO8583Exception(fieldTag, "Field [" + fieldTag + "] length out of limit. " + "[fieldLen=" + fieldLen + "]");
            }
        } else {
            //fix length
            fieldLen = isoFormat.maxLen;
        }
        if (isoFormat.type != TYPE_BCD) {
            fieldLen = fieldLen * 2;
        }
        if (fieldLen % 2 == 0) {
            hexValue = hexPacket.substring(currentPos, currentPos + fieldLen);
        } else {
            //odd data
            if (isoFormat.alignRight) {
                //align right
                hexValue = hexPacket.substring(currentPos + 1, currentPos + 1 + fieldLen);
            } else {
                //align left
                hexValue = hexPacket.substring(currentPos, currentPos + fieldLen);
            }
            fieldLen++;
        }
        currentPos += fieldLen;
        if (isoFormat.type == TYPE_ASCII) {
            value = StringUtils.hexToStr(hexValue);
        } else {
            value = hexValue;
        }
        outField.data = value;
        return currentPos;
    }


    /**
     * unpack 8583
     *
     * @param hexBuffer packet data(HEX)
     * @throws ISO8583Exception unpack 8583 error
     */
    public void unpack(String hexBuffer) throws NumberFormatException, ISO8583Exception {
        int currentStrPos = 0;
        initPack();
        IsoField outField;
        char[] bitmap = new char[64];
        //parse every field
        for (int index = 0; index <= bitmap.length; index++) {
            int bitIndex = index - 1;
            outField = new IsoField();
            //index ==0 ,it's message type
            if (index == 0 || bitmap[bitIndex] == '1') {
                int start = currentStrPos;
                currentStrPos = unpackField(hexBuffer, index + "", currentStrPos, isoFormats[index], outField);
                String log = hexBuffer.substring(start, currentStrPos);
                if (isoFormats[index].type == TYPE_ASCII) {
                    LoggerUtils.d(String.format(Locale.US, "【Unpack】Field [%3d] :" + "[%s] ==>%s", index, log, outField.data));
                } else {
                    LoggerUtils.d(String.format(Locale.US, "【Unpack】Field [%3d] :" + "[%s]", index, log));
                }
                setField(index, outField.data);
                if (index == 1) {
                    //secondary bitmap
                    bitmap = (new String(getIsoBitmap()) + new String(getSecondaryBitmap())).toCharArray();
                }
            }
            if (index == 0) {
                //parse primary bitmap
                outField = new IsoField();
                int start = currentStrPos;
                currentStrPos = unpackField(hexBuffer, "BITMAP", currentStrPos, bitmapFormat, outField);
                String log = hexBuffer.substring(start, currentStrPos);
                if (bitmapFormat.type == TYPE_ASCII) {
                    LoggerUtils.d(String.format(Locale.US, "【Unpack】Bitmap      :" + "[%s] ==>%s", log, outField.data));
                } else {
                    LoggerUtils.d(String.format(Locale.US, "【Unpack】Bitmap      :" + "[%s]", log));
                }
                bitmapField.data = outField.data;
                bitmapField.isExist = true;
                bitmap = getIsoBitmap();
            }
        }
        if (hexBuffer.length() > currentStrPos) {
            throw new ISO8583Exception("Response 8583 packet length out of limit");
        }
    }


    /**
     * Field format
     */
    private static class IsoFormat {
        private int maxLen;
        private int type;
        private boolean lengthVar;
        private boolean alignRight;
        private String fill;

        @NonNull
        @Override
        public String toString() {
            return "IsoFormat{" +
                    "maxLen=" + maxLen +
                    ", type=" + type +
                    ", lengthVar=" + lengthVar +
                    ", alignRight=" + alignRight +
                    ", fill='" + fill + '\'' +
                    '}';
        }
    }


    private static class IsoField {
        private String data;
        private boolean isExist;
    }
}
