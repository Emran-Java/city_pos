package acquire.core.esc;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.text.TextUtils;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Esc protocol.
 * <p>Code taken from printersdkv1.jar
 *
 * @author Janson
 * @date 2023/5/11 15:14
 */
public class EscCommand {
    private final Vector<Byte> escData;

    public EscCommand() {
        escData = new Vector<>();
    }

    private void addArrayToCommand(byte[] array) {
        for (byte b : array) {
            escData.add(b);
        }
    }

    private void addStrToCommand(String str) {
        if (!TextUtils.isEmpty(str)) {
            byte[] bs = str.getBytes();
            for (byte b : bs) {
                escData.add(b);
            }
        }

    }

    private void addStrToCommand(String str, String charset) {
        if (!TextUtils.isEmpty(str)) {
            try {
                byte[] bs = str.getBytes(charset);
                for (byte b : bs) {
                    escData.add(b);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    private void addStrToCommandUTF8Encoding(String str, int length) {
        if (!TextUtils.isEmpty(str)) {
            byte[] bs = str.getBytes(StandardCharsets.UTF_8);
            if (length > bs.length) {
                length = bs.length;
            }
            for (int i = 0; i < length; ++i) {
                escData.add(bs[i]);
            }
        }

    }

    private void addStrToCommand(String str, int length) {
        if (!TextUtils.isEmpty(str)) {
            byte[] bs = str.getBytes();
            if (length > bs.length) {
                length = bs.length;
            }
            for (int i = 0; i < length; ++i) {
                escData.add(bs[i]);
            }
        }
    }

    public void addHorTab() {
        byte[] command = new byte[]{9};
        addArrayToCommand(command);
    }

    public void addText(String text) {
        addStrToCommand(text);
    }

    public void addText(String text, String charsetName) {
        addStrToCommand(text, charsetName);
    }

    public void addArabicText(String text) {
        text = GpUtils.reverseLetterAndNumber(text);
        text = GpUtils.splitArabic(text);
        String[] fooInput = text.split("\\n");

        for (String in : fooInput) {
            byte[] output = GpUtils.string2Cp864(in);
            for (byte b : output) {
                if (b == (byte) 0xF0) {
                    addArrayToCommand(new byte[]{0x1B, 0x74, 0x1D, (byte) 0x84, 0x1B, 0x74, 0x16});
                } else if (b == 0x7F) {
                    escData.add((byte) 0xD7);
                } else {
                    escData.add(b);
                }
            }
        }

    }

    public void addPrintAndLineFeed() {
        byte[] command = new byte[]{0x0A};
        addArrayToCommand(command);
    }

    public void RealtimeStatusTransmission(STATUS status) {
        byte[] command = new byte[]{0x10, 0x04, status.getValue()};
        addArrayToCommand(command);
    }

    public void addGeneratePluseAtRealtime(LabelCommand.FOOT foot, byte t) {
        byte[] command = new byte[]{0x10, 0x14, 0x01, (byte) foot.getValue(), 0};
        if (t > 8) {
            t = 8;
        }
        command[4] = t;
        addArrayToCommand(command);
    }

    public void addSound(byte n, byte t) {
        byte[] command = new byte[]{0x1B, 0x42, 0, 0};
        if (n < 0) {
            n = 1;
        } else if (n > 9) {
            n = 9;
        }
        if (t < 0) {
            t = 1;
        } else if (t > 9) {
            t = 9;
        }
        command[2] = n;
        command[3] = t;
        addArrayToCommand(command);
    }

    public void addSetRightSideCharacterSpacing(byte n) {
        byte[] command = new byte[]{0x1B, 0x20, n};
        addArrayToCommand(command);
    }

    public Vector<Byte> getEscCommand() {
        return escData;
    }

    public void addSelectPrintModes(FONT font, ENABLE emphasized, ENABLE doubleheight, ENABLE doublewidth, ENABLE underline) {
        byte temp = 0;
        if (font == FONT.FONTB) {
            temp = 1;
        }

        if (emphasized == ENABLE.ON) {
            temp = (byte) (temp | 0x08);
        }

        if (doubleheight == ENABLE.ON) {
            temp = (byte) (temp | 0x10);
        }

        if (doublewidth == ENABLE.ON) {
            temp = (byte) (temp | 0x20);
        }

        if (underline == ENABLE.ON) {
            temp = (byte) (temp | 0x80);
        }

        byte[] command = new byte[]{0x1B, 0x21, temp};
        addArrayToCommand(command);
    }

    public void addSetAbsolutePrintPosition(short n) {
        byte[] command = new byte[]{0x1B, 0x24, 0, 0};
        byte nl = (byte) (n % 256);
        byte nh = (byte) (n / 256);
        command[2] = nl;
        command[3] = nh;
        addArrayToCommand(command);
    }

    public void addSelectOrCancelUserDefineCharacter(ENABLE enable) {
        byte[] command = new byte[]{0x1B, 0x25, 0};
        if (enable == ENABLE.ON) {
            command[2] = 1;
        }
        addArrayToCommand(command);
    }

    public void addTurnUnderlineModeOnOrOff(UNDERLINE_MODE underline) {
        byte[] command = new byte[]{0x1B, 0x2D, underline.getValue()};
        addArrayToCommand(command);
    }

    public void addSelectDefualtLineSpacing() {
        byte[] command = new byte[]{0x1B, 0x32};
        addArrayToCommand(command);
    }

    public void addSetLineSpacing(byte n) {
        byte[] command = new byte[]{0x1B, 0x33, n};
        addArrayToCommand(command);
    }

    public void addCancelUserDefinedCharacters(byte n) {
        byte[] command = new byte[]{0x1B, 0x3F, 0x20};
        if (n >= 0x20 && n <= 0x7E) {
            command[2] = n;
        }

        addArrayToCommand(command);
    }

    public void addInitializePrinter() {
        byte[] command = new byte[]{0x1B, 0x40};
        addArrayToCommand(command);
    }

    public void addTurnEmphasizedModeOnOrOff(ENABLE enabel) {
        byte[] command = new byte[]{0x1B, 0x45, enabel.getValue()};
        addArrayToCommand(command);
    }

    public void addTurnDoubleStrikeOnOrOff(ENABLE enabel) {
        byte[] command = new byte[]{0x1B, 0x47, enabel.getValue()};
        addArrayToCommand(command);
    }

    public void addPrintAndFeedPaper(byte n) {
        byte[] command = new byte[]{0x1B, 0x4A, n};
        addArrayToCommand(command);
    }

    public void addSelectCharacterFont(FONT font) {
        byte[] command = new byte[]{0x1B, 0x4D, font.getValue()};
        addArrayToCommand(command);
    }

    public void addSelectInternationalCharacterSet(CHARACTER_SET set) {
        byte[] command = new byte[]{0x1B, 0x52, set.getValue()};
        addArrayToCommand(command);
    }

    public void addTurn90ClockWiseRotatin(ENABLE enabel) {
        byte[] command = new byte[]{0x1B, 0x56, enabel.getValue()};
        addArrayToCommand(command);
    }

    public void addSetRelativePrintPositon(short n) {
        byte[] command = new byte[]{0x1B, 0x5C, 0, 0};
        byte nl = (byte) (n % 256);
        byte nh = (byte) (n / 256);
        command[2] = nl;
        command[3] = nh;
        addArrayToCommand(command);
    }

    public void addSelectJustification(JUSTIFICATION just) {
        byte[] command = new byte[]{0x1B, 0x61, just.getValue()};
        addArrayToCommand(command);
    }

    public void addPrintAndFeedLines(byte n) {
        byte[] command = new byte[]{0x1B, 0x64, n};
        addArrayToCommand(command);
    }

    public void addGeneratePlus(LabelCommand.FOOT foot, byte t1, byte t2) {
        byte[] command = new byte[]{0x1B, 0x70, (byte) foot.getValue(), t1, t2};
        addArrayToCommand(command);
    }

    public void addSelectCodePage(CODEPAGE page) {
        byte[] command = new byte[]{0x1B, 0x74, page.getValue()};
        addArrayToCommand(command);
    }

    public void addTurnUpsideDownModeOnOrOff(ENABLE enable) {
        byte[] command = new byte[]{0x1B, 0x7B, enable.getValue()};
        addArrayToCommand(command);
    }

    public void addSetCharacterSize(WIDTH_ZOOM width, HEIGHT_ZOOM height) {
        byte[] command = new byte[]{0x1D, 0x21, 0};
        byte temp = width.getValue();
        temp |= height.getValue();
        command[2] = temp;
        addArrayToCommand(command);
    }

    public void addTurnReverseModeOnOrOff(ENABLE enable) {
        byte[] command = new byte[]{0x1D, 0x42, enable.getValue()};
        addArrayToCommand(command);
    }

    public void addSelectPrintingPositionForHRICharacters(HRI_POSITION position) {
        byte[] command = new byte[]{0x1D, 0x48, position.getValue()};
        addArrayToCommand(command);
    }

    public void addSetLeftMargin(short n) {
        byte[] command = new byte[]{0x1D, 0x4C, 0, 0};
        byte nl = (byte) (n % 256);
        byte nh = (byte) (n / 256);
        command[2] = nl;
        command[3] = nh;
        addArrayToCommand(command);
    }

    public void addSetHorAndVerMotionUnits(byte x, byte y) {
        byte[] command = new byte[]{0x1D, 0x50, x, y};
        addArrayToCommand(command);
    }

    public void addCutAndFeedPaper(byte length) {
        byte[] command = new byte[]{0x1D, 0x56, 0x42, length};
        addArrayToCommand(command);
    }

    public void addCutPaper() {
        byte[] command = new byte[]{0x1D, 0x56, 0x01};
        addArrayToCommand(command);
    }

    public void addSetPrintingAreaWidth(short width) {
        byte nl = (byte) (width % 256);
        byte nh = (byte) (width / 256);
        byte[] command = new byte[]{0x1D, 0x57, nl, nh};
        addArrayToCommand(command);
    }

    public void addSetAutoSatusBack(ENABLE enable) {
        byte[] command = new byte[]{0x1D, 0x61, 0};
        if (enable == ENABLE.ON) {
            command[2] = (byte) 0xFF;
        }

        addArrayToCommand(command);
    }

    public void addSetFontForHRICharacter(FONT font) {
        byte[] command = new byte[]{0x1D, 0x66, font.getValue()};
        addArrayToCommand(command);
    }

    public void addSetBarcodeHeight(byte height) {
        byte[] command = new byte[]{0x1D, 0x68, height};
        addArrayToCommand(command);
    }

    public void addSetBarcodeWidth(byte width) {
        byte[] command = new byte[]{0x1D, 0x77, 0};
        if (width > 6) {
            width = 6;
        }

        if (width < 2) {
            width = 1;
        }

        command[2] = width;
        addArrayToCommand(command);
    }

    public void addSetKanjiFontMode(ENABLE doubleWidth, ENABLE doubleHeight, ENABLE underline) {
        byte[] command = new byte[]{0x1C, 0x21, 0};
        byte temp = 0;
        if (doubleWidth == ENABLE.ON) {
            temp = (byte) (temp | 4);
        }
        if (doubleHeight == ENABLE.ON) {
            temp = (byte) (temp | 8);
        }
        if (underline == ENABLE.ON) {
            temp = (byte) (temp | 0x80);
        }
        command[2] = temp;
        addArrayToCommand(command);
    }

    public void addSelectKanjiMode() {
        byte[] command = new byte[]{0x1C, 0x26};
        addArrayToCommand(command);
    }

    public void addSetKanjiUnderLine(UNDERLINE_MODE underline) {
        byte[] command = new byte[]{0x1C, 0x2D, underline.getValue()};
        addArrayToCommand(command);
    }

    public void addCancelKanjiMode() {
        byte[] command = new byte[]{0x1C, 0x2E};
        addArrayToCommand(command);
    }

    public void addSetKanjiLefttandRightSpace(byte left, byte right) {
        byte[] command = new byte[]{0x1C, 0x53, left, right};
        addArrayToCommand(command);
    }

    public void addSetQuadrupleModeForKanji(ENABLE enable) {
        byte[] command = new byte[]{0x1C, 0x57, enable.getValue()};
        addArrayToCommand(command);
    }

    public void addRastBitImage(Bitmap bitmap, int nWidth, int nMode) {
        if (bitmap != null) {
            int width = (nWidth + 7) / 8 * 8;
            int height = bitmap.getHeight() * width / bitmap.getWidth();
            Bitmap grayBitmap = GpUtils.toGrayscale(bitmap);
            Bitmap rszBitmap = GpUtils.resizeImage(grayBitmap, width, height);
            byte[] src = GpUtils.bitmapToBWPix(rszBitmap);
            byte[] command = new byte[8];
            height = src.length / width;
            command[0] = 29;
            command[1] = 118;
            command[2] = 48;
            command[3] = (byte) (nMode & 1);
            command[4] = (byte) (width / 8 % 256);
            command[5] = (byte) (width / 8 / 256);
            command[6] = (byte) (height % 256);
            command[7] = (byte) (height / 256);
            addArrayToCommand(command);
            byte[] codecontent = GpUtils.pixToEscRastBitImageCmd(src);
            for (byte b : codecontent) {
                escData.add(b);
            }
        }
    }

    public void addOriginRastBitImage(Bitmap bitmap, int nWidth) {
        if (bitmap != null) {
            int width = (nWidth + 7) / 8 * 8;
            int height = bitmap.getHeight() * width / bitmap.getWidth();
            Bitmap rszBitmap = GpUtils.resizeImage(bitmap, width, height);
            byte[] data = GpUtils.printEscDraw(rszBitmap);
            addArrayToCommand(data);
        }
    }

    public void addRastBitImageWithMethod(Bitmap bitmap, int nWidth, int nMode) {
        if (bitmap != null) {
            int width = (nWidth + 7) / 8 * 8;
            int height = bitmap.getHeight() * width / bitmap.getWidth();
            Bitmap resizeImage = GpUtils.resizeImage(bitmap, width, height);
            Bitmap rszBitmap = GpUtils.filter(resizeImage, resizeImage.getWidth(), resizeImage.getHeight());
            byte[] src = GpUtils.bitmapToBWPix(rszBitmap);
            byte[] command = new byte[8];
            height = src.length / width;
            command[0] = 29;
            command[1] = 118;
            command[2] = 48;
            command[3] = (byte) (nMode & 1);
            command[4] = (byte) (width / 8 % 256);
            command[5] = (byte) (width / 8 / 256);
            command[6] = (byte) (height % 256);
            command[7] = (byte) (height / 256);
            addArrayToCommand(command);
            byte[] codecontent = GpUtils.pixToEscRastBitImageCmd(src);
            for (byte b : codecontent) {
                escData.add(b);
            }
        }
    }

    public void addDownloadNvBitImage(Bitmap[] bitmaps) {
        if (bitmaps != null) {
            int n = bitmaps.length;
            if (n > 0) {
                byte[] command = new byte[]{0x1C, 113, (byte) n};
                addArrayToCommand(command);
                for (Bitmap bitmap : bitmaps) {
                    int height = (bitmap.getHeight() + 7) / 8 * 8;
                    int width = bitmap.getWidth() * height / bitmap.getHeight();
                    Bitmap grayBitmap = GpUtils.toGrayscale(bitmap);
                    Bitmap rszBitmap = GpUtils.resizeImage(grayBitmap, width, height);
                    byte[] src = GpUtils.bitmapToBWPix(rszBitmap);
                    height = src.length / width;
                    byte[] codecontent = GpUtils.pixToEscNvBitImageCmd(src, width, height);
                    for (byte b : codecontent) {
                        escData.add(b);
                    }
                }
            }
        }
    }

    public void addPrintNvBitmap(byte n, byte mode) {
        byte[] command = new byte[]{0x1C, 0x70, n, mode};
        addArrayToCommand(command);
    }

    public void addUPCA(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x41, 0x0B};
        if (content.length() >= command[3]) {
            addArrayToCommand(command);
            addStrToCommand(content, 11);
        }
    }

    public void addUPCE(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x42, 0x0B};
        if (content.length() >= command[3]) {
            addArrayToCommand(command);
            addStrToCommand(content, command[3]);
        }
    }

    public void addEAN13(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x43, 0x0C};
        if (content.length() >= command[3]) {
            addArrayToCommand(command);
            addStrToCommand(content, command[3]);
        }
    }

    public void addEAN8(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x44, 0x07};
        if (content.length() >= command[3]) {
            addArrayToCommand(command);
            addStrToCommand(content, command[3]);
        }
    }

    @SuppressLint({"DefaultLocale"})
    public void addCODE39(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x45, (byte) content.length()};
        content = content.toUpperCase();
        addArrayToCommand(command);
        addStrToCommand(content, command[3]);
    }

    public void addITF(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x46, (byte) content.length()};
        addArrayToCommand(command);
        addStrToCommand(content, command[3]);
    }

    public void addCODABAR(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x47, (byte) content.length()};
        addArrayToCommand(command);
        addStrToCommand(content, command[3]);
    }

    public void addCODE93(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x48, (byte) content.length()};
        addArrayToCommand(command);
        addStrToCommand(content, command[3]);
    }

    public void addCODE128(String content) {
        byte[] command = new byte[]{0x1D, 0x6B, 0x49, (byte) content.length()};
        addArrayToCommand(command);
        addStrToCommand(content, command[3]);
    }

    public String genCodeC(String content) {
        List<Byte> bytes = new ArrayList<>();
        int len = content.length();
        bytes.add((byte) 0x7B);
        bytes.add((byte) 0x43);

        for (int i = 0; i < len; i += 2) {
            i = (content.charAt(i) - 48) * 10;
            int bits = content.charAt(i + 1) - 48;
            int current = i + bits;
            bytes.add((byte) current);
        }
        byte[] bb = new byte[bytes.size()];
        for (int i = 0; i < bb.length; ++i) {
            bb[i] = bytes.get(i);
        }
        return new String(bb, 0, bb.length);
    }

    public String genCodeB(String content) {
        return String.format("{B%s", content);
    }

    public String genCode128(String content) {
        String regex = "([^0-9])";
        String[] strings = content.split(regex);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(content);
        String splitString = null;
        int strlen = strings.length;
        if (strlen > 0 && matcher.find()) {
            splitString = matcher.group(0);
        }

        StringBuilder sb = new StringBuilder();
        for (String string : strings) {
            int len = string.length();
            int result = len % 2;
            if (result == 0) {
                String codeC = this.genCodeC(string);
                sb.append(codeC);
            } else {
                sb.append(this.genCodeB(String.valueOf(string.charAt(0))));
                sb.append(this.genCodeC(string.substring(1)));
            }

            if (splitString != null) {
                sb.append(this.genCodeB(splitString));
                splitString = null;
            }
        }

        return sb.toString();
    }

    public void addSelectSizeOfModuleForQRCode(byte n) {
        byte[] command = new byte[]{0x1D, 0x28, 0x6B, 0x03, 0, 0x31, 0x43, 0x03};
        command[7] = n;
        addArrayToCommand(command);
    }

    public void addSelectErrorCorrectionLevelForQRCode(byte n) {
        byte[] command = new byte[]{0x1D, 0x28, 0x6B, 0x03, 0, 0x31, 0x45, n};
        addArrayToCommand(command);
    }

    public void addStoreQRCodeData(String content) {
        byte[] command = new byte[]{0x1D, 0x28, 0x6B,
                (byte) ((content.getBytes().length + 3) % 256),
                (byte) ((content.getBytes().length + 3) / 256), 0x31, 0x50, 0x30};
        addArrayToCommand(command);
        if (!TextUtils.isEmpty(content)) {
            byte[] bs = content.getBytes();
            for (byte b : bs) {
                escData.add(b);
            }
        }
    }

    public void addPrintQRCode() {
        byte[] command = new byte[]{0x1D, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30};
        addArrayToCommand(command);
    }

    public void addQueryPrinterStatus() {
        byte[] command = new byte[]{0x10, 0x04, 0x02};
        addArrayToCommand(command);
    }

    public void addUserCommand(byte[] command) {
        addArrayToCommand(command);
    }

    public enum CHARACTER_SET {
        USA(0),
        FRANCE(1),
        GERMANY(2),
        UK(3),
        DENMARK_I(4),
        SWEDEN(5),
        ITALY(6),
        SPAIN_I(7),
        JAPAN(8),
        NORWAY(9),
        DENMARK_II(10),
        SPAIN_II(11),
        LATIN_AMERCIA(12),
        KOREAN(13),
        SLOVENIA(14),
        CHINA(15);

        private final int value;

        private CHARACTER_SET(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum CODEPAGE {
        PC437(0),
        KATAKANA(1),
        PC850(2),
        PC860(3),
        PC863(4),
        PC865(5),
        WEST_EUROPE(6),
        GREEK(7),
        HEBREW(8),
        EAST_EUROPE(9),
        IRAN(10),
        WPC1252(16),
        PC866(17),
        PC852(18),
        PC858(19),
        IRANII(20),
        LATVIAN(21),
        ARABIC(22),
        PT151(23),
        PC747(24),
        WPC1257(25),
        VIETNAM(27),
        PC864(28),
        PC1001(29),
        UYGUR(30),
        THAI(255);

        private final int value;

        private CODEPAGE(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum ENABLE {
        OFF(0),
        ON(1);

        private final int value;

        private ENABLE(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum FONT {
        FONTA(0),
        FONTB(1);

        private final int value;

        private FONT(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum HEIGHT_ZOOM {
        MUL_1(0),
        MUL_2(1),
        MUL_3(2),
        MUL_4(3),
        MUL_5(4),
        MUL_6(5),
        MUL_7(6),
        MUL_8(7);

        private final int value;

        private HEIGHT_ZOOM(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum HRI_POSITION {
        NO_PRINT(0),
        ABOVE(1),
        BELOW(2),
        ABOVE_AND_BELOW(3);

        private final int value;

        private HRI_POSITION(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum JUSTIFICATION {
        LEFT(0),
        CENTER(1),
        RIGHT(2);

        private final int value;

        private JUSTIFICATION(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum STATUS {
        PRINTER_STATUS(1),
        PRINTER_OFFLINE(2),
        PRINTER_ERROR(3),
        PRINTER_PAPER(4);

        private final int value;

        private STATUS(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum UNDERLINE_MODE {
        OFF(0),
        UNDERLINE_1DOT(1),
        UNDERLINE_2DOT(2);

        private final int value;

        private UNDERLINE_MODE(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }

    public enum WIDTH_ZOOM {
        MUL_1(0),
        MUL_2(16),
        MUL_3(32),
        MUL_4(48),
        MUL_5(64),
        MUL_6(80),
        MUL_7(96),
        MUL_8(112);

        private final int value;

        private WIDTH_ZOOM(int value) {
            this.value = value;
        }

        public byte getValue() {
            return (byte) this.value;
        }
    }
}
