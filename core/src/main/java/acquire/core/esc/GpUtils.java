package acquire.core.esc;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ESC protocol utils
 *
 * @author Janson
 * @date 2023/5/11 15:44
 */
public class GpUtils {
    private static final Integer[][] FORMAT_TABLE = new Integer[][]{{65152, 65152, 65152, 65152}, {65153, 65154, 65153, 65154}, {65155, 65156, 65155, 65156}, {65157, 65157, 65157, 65157}, {65149, 65149, 65149, 65149}, {65163, 65163, 65163, 65163}, {65165, 65166, 65165, 65166}, {65167, 65167, 65169, 65169}, {65171, 65171, 65171, 65171}, {65173, 65173, 65175, 65175}, {65177, 65177, 65179, 65179}, {65181, 65181, 65183, 65183}, {65185, 65185, 65187, 65187}, {65189, 65189, 65191, 65191}, {65193, 65193, 65193, 65193}, {65195, 65195, 65195, 65195}, {65197, 65197, 65197, 65197}, {65199, 65199, 65199, 65199}, {65201, 65201, 65203, 65203}, {65205, 65205, 65207, 65207}, {65209, 65209, 65211, 65211}, {65213, 65213, 65215, 65215}, {65217, 65217, 65217, 65217}, {65221, 65221, 65221, 65221}, {65225, 65226, 65227, 65228}, {65229, 65230, 65231, 65232}, {65233, 65233, 65235, 65235}, {65237, 65237, 65239, 65239}, {65241, 65241, 65243, 65243}, {65245, 65245, 65247, 65247}, {65249, 65249, 65251, 65251}, {65253, 65253, 65255, 65255}, {65257, 65257, 65259, 65259}, {65261, 65261, 65261, 65261}, {65263, 65264, 65263, 65264}, {65265, 65266, 65267, 65267}, {65269, 65270, 65269, 65270}, {65271, 65272, 65271, 65272}, {65273, 65274, 65273, 65274}, {65275, 65276, 65275, 65276}};
    private static final Integer[] THE_SET0 = new Integer[]{1569, 1570, 1571, 1572, 1573, 1574, 1575, 1576, 1577, 1578, 1579, 1580, 1581, 1582, 1583, 1584, 1585, 1586, 1587, 1588, 1589, 1590, 1591, 1592, 1593, 1594, 1601, 1602, 1603, 1604, 1605, 1606, 1607, 1608, 1609, 1610, 17442, 17443, 17445, 17447};
    private final static Integer[] THE_SET1 = new Integer[]{1574, 1576, 1578, 1579, 1580, 1581, 1582, 1587, 1588, 1589, 1590, 1591, 1592, 1593, 1594, 1600, 1601, 1602, 1603, 1604, 1605, 1606, 1607, 1610};
    private final static Integer[] THE_SET2 = new Integer[]{1570, 1571, 1572, 1573, 1574, 1575, 1576, 1577, 1578, 1579, 1580, 1581, 1582, 1583, 1584, 1585, 1586, 1587, 1588, 1589, 1590, 1591, 1592, 1593, 1594, 1600, 1601, 1602, 1603, 1604, 1605, 1606, 1607, 1608, 1609, 1610};
    private static final int[] P0 = new int[]{0, 128};
    private static final int[] P1 = new int[]{0, 64};
    private static final int[] P2 = new int[]{0, 32};
    private static final int[] P3 = new int[]{0, 16};
    private static final int[] P4 = new int[]{0, 8};
    private static final int[] P5 = new int[]{0, 4};
    private static final int[] P6 = new int[]{0, 2};
    private static final Pattern PATTERN = Pattern.compile("([a-zA-Z0-9!@#$^&*()~{}:\",.<>/]+)");
    private static int sPaperWidth = 48;
    public static final int[][] COLOR_PALETTE = new int[][]{new int[3], {255, 255, 255}};
    private static int method = 1;
    private static final int[][] FLOYD_16_X_16 = new int[][]{{0, 128, 32, 160, 8, 136, 40, 168, 2, 130, 34, 162, 10, 138, 42, 170}, {192, 64, 224, 96, 200, 72, 232, 104, 194, 66, 226, 98, 202, 74, 234, 106}, {48, 176, 16, 144, 56, 184, 24, 152, 50, 178, 18, 146, 58, 186, 26, 154}, {240, 112, 208, 80, 248, 120, 216, 88, 242, 114, 210, 82, 250, 122, 218, 90}, {12, 140, 44, 172, 4, 132, 36, 164, 14, 142, 46, 174, 6, 134, 38, 166}, {204, 76, 236, 108, 196, 68, 228, 100, 206, 78, 238, 110, 198, 70, 230, 102}, {60, 188, 28, 156, 52, 180, 20, 148, 62, 190, 30, 158, 54, 182, 22, 150}, {252, 124, 220, 92, 244, 116, 212, 84, 254, 126, 222, 94, 246, 118, 214, 86}, {3, 131, 35, 163, 11, 139, 43, 171, 1, 129, 33, 161, 9, 137, 41, 169}, {195, 67, 227, 99, 203, 75, 235, 107, 193, 65, 225, 97, 201, 73, 233, 105}, {51, 179, 19, 147, 59, 187, 27, 155, 49, 177, 17, 145, 57, 185, 25, 153}, {243, 115, 211, 83, 251, 123, 219, 91, 241, 113, 209, 81, 249, 121, 217, 89}, {15, 143, 47, 175, 7, 135, 39, 167, 13, 141, 45, 173, 5, 133, 37, 165}, {207, 79, 239, 111, 199, 71, 231, 103, 205, 77, 237, 109, 197, 69, 229, 101}, {63, 191, 31, 159, 55, 183, 23, 151, 61, 189, 29, 157, 53, 181, 21, 149}, {254, 127, 223, 95, 247, 119, 215, 87, 253, 125, 221, 93, 245, 117, 213, 85}};

    static String reverseLetterAndNumber(String input) {
        StringBuilder sb = new StringBuilder(input);
        Matcher matcher = PATTERN.matcher(input);

        while (matcher.find()) {
            String matcherString = matcher.group();
            int matcherStart = matcher.start();
            int matcherEnd = matcher.end();
            sb.replace(matcherStart, matcherEnd, (new StringBuilder(matcherString)).reverse().toString());
        }

        return sb.toString();
    }

    static String splitArabic(String input) {
        StringBuilder sb = new StringBuilder();
        String[] arabics = input.split("\\n");
        int i;
        int lastArabic;
        if (arabics.length == 1 && arabics[0].length() > sPaperWidth) {
            i = arabics[0].length() / sPaperWidth;
            lastArabic = 1;

            for (int j = 0; lastArabic <= i; ++lastArabic) {
                sb.append(arabics[0].substring(j, sPaperWidth * lastArabic));
                j += sPaperWidth;
            }

            if (sb.length() >= 0) {
                sb.append('\n');
            }

            lastArabic = arabics[0].length() % sPaperWidth;
            sb.append(arabics[0].substring(arabics[0].length() - lastArabic, arabics[0].length()));
            return splitArabic(sb.toString());
        } else {
            for (i = 0; i < arabics.length; ++i) {
                lastArabic = arabics[i].length();
                if (lastArabic > sPaperWidth) {
                    sb.append(splitArabic(arabics[i]));
                } else {
                    sb.append(addSpaceAfterArabicString(arabics[i], sPaperWidth - lastArabic));
                }
            }

            return sb.toString();
        }
    }

    static String addSpaceAfterArabicString(String arabic, int number) {
        StringBuilder sb = new StringBuilder();
        sb.append(arabic);

        for (int i = 0; i < number; ++i) {
            sb.append(' ');
        }

        sb.append('\n');
        return sb.toString();
    }

    static byte[] string2Cp864(String arabicString) {
        Integer[] originUnicode = new Integer[arabicString.length()];
        Integer[] outputUnicode = new Integer[arabicString.length()];
        Integer[] outputChars = new Integer[originUnicode.length];
        copy(arabicString.toCharArray(), originUnicode, arabicString.length());
        List<Integer> list = Hyphen(Arrays.asList(originUnicode));
        list = deformation(list);
        Collections.reverse(list);
        list.toArray(outputUnicode);
        char[] chs = integer2Character(outputUnicode);
        byte[] cp864bytes = new byte[0];

        try {
            cp864bytes = (new String(chs)).getBytes("cp864");
        } catch (UnsupportedEncodingException var8) {
            var8.printStackTrace();
        }

        return cp864bytes;
    }

    static char[] integer2Character(Integer[] integers) {
        char[] chs = new char[integers.length];

        for (int i = 0; i < integers.length; ++i) {
            if (integers[i] != null) {
                chs[i] = (char) integers[i].intValue();
            } else {
                chs[i] = ' ';
            }
        }

        return chs;
    }

    static List<Integer> deformation(List<Integer> inputlist) {
        List<Integer> outputlist = new ArrayList<>();
        Map<Integer, Integer[]> formHashTable = new HashMap<>();

        int i;
        for (i = 0; i < 40; ++i) {
            formHashTable.put(THE_SET0[i], FORMAT_TABLE[i]);
        }

        for (i = 0; i < inputlist.size(); ++i) {
            if (compare(inputlist.get(i), 0)) {
                boolean inSet1;
                boolean inSet2;
                int flag;
                if (i == 0) {
                    inSet1 = false;
                    inSet2 = compare(inputlist.get(i + 1), 2);
                    flag = Flag(inSet1, inSet2);
                } else if (i == inputlist.size() - 1) {
                    inSet1 = compare(inputlist.get(i - 1), 1);
                    inSet2 = false;
                    flag = Flag(inSet1, inSet2);
                } else {
                    inSet1 = compare(inputlist.get(i - 1), 1);
                    inSet2 = compare(inputlist.get(i + 1), 2);
                    flag = Flag(inSet1, inSet2);
                }
                Integer[] a = formHashTable.get(inputlist.get(i));
                outputlist.add(a[flag]);
            } else {
                outputlist.add(inputlist.get(i));
            }
        }
        return outputlist;
    }

    static void copy(char[] array, Integer[] originUnicode, int length) {
        for (int i = 0; i < length; ++i) {
            originUnicode[i] = (int) array[i];
        }

    }

    static List<Integer> Hyphen(List<Integer> list) {
        for (int i = 0; i < list.size(); ++i) {
            if (list.get(i) == 1604) {
                switch (list.get(i + 1)) {
                    case 1570:
                        list.set(i, 17442);
                        list.remove(i + 1);
                        break;
                    case 1571:
                        list.set(i, 17443);
                        list.remove(i + 1);
                        break;
                    case 1573:
                        list.set(i, 17445);
                        list.remove(i + 1);
                        break;
                    case 1575:
                        list.set(i, 17447);
                        list.remove(i + 1);
                        break;
                    case 1572:
                    case 1574:
                    default:
                        break;
                }
            }
        }
        return list;
    }

    public static Bitmap toGrayscale(Bitmap bmpOriginal) {
        int height = bmpOriginal.getHeight();
        int width = bmpOriginal.getWidth();
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0.0F);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0.0F, 0.0F, paint);
        return bmpGrayscale;
    }

    public static Bitmap resizeImage(Bitmap bitmap, int w, int h) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = (float) w / (float) width;
        float scaleHeight = (float) h / (float) height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public static byte[] bitmapToBWPix(Bitmap mBitmap) {
        int[] pixels = new int[mBitmap.getWidth() * mBitmap.getHeight()];
        byte[] data = new byte[mBitmap.getWidth() * mBitmap.getHeight()];
        Bitmap grayBitmap = toGrayscale(mBitmap);
        grayBitmap.getPixels(pixels, 0, mBitmap.getWidth(), 0, 0, mBitmap.getWidth(), mBitmap.getHeight());
        format_K_dither16x16(pixels, grayBitmap.getWidth(), grayBitmap.getHeight(), data);
        return data;
    }

    public static byte[] pixToEscRastBitImageCmd(byte[] src) {
        byte[] data = new byte[src.length / 8];
        int i = 0;

        for (int k = 0; i < data.length; ++i) {
            data[i] = (byte) (P0[src[k]] + P1[src[k + 1]] + P2[src[k + 2]] + P3[src[k + 3]] + P4[src[k + 4]] + P5[src[k + 5]] + P6[src[k + 6]] + src[k + 7]);
            k += 8;
        }

        return data;
    }

    public static byte[] printEscDraw(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        byte[] bitbuf = new byte[width / 8];
        byte[] imgbuf = new byte[width / 8 * height + 8];
        imgbuf[0] = 29;
        imgbuf[1] = 118;
        imgbuf[2] = 48;
        imgbuf[3] = 0;
        imgbuf[4] = (byte) (width / 8);
        imgbuf[5] = 0;
        imgbuf[6] = (byte) (height % 256);
        imgbuf[7] = (byte) (height / 256);
        int s = 7;

        for (int i = 0; i < height; ++i) {
            int k;
            for (k = 0; k < width / 8; ++k) {
                int c0 = bitmap.getPixel(k * 8, i);
                byte p0;
                if (c0 != -1 && c0 != 0) {
                    p0 = 1;
                } else {
                    p0 = 0;
                }

                int c1 = bitmap.getPixel(k * 8 + 1, i);
                byte p1;
                if (c1 == -1) {
                    p1 = 0;
                } else {
                    p1 = 1;
                }

                int c2 = bitmap.getPixel(k * 8 + 2, i);
                byte p2;
                if (c2 == -1) {
                    p2 = 0;
                } else {
                    p2 = 1;
                }

                int c3 = bitmap.getPixel(k * 8 + 3, i);
                byte p3;
                if (c3 == -1) {
                    p3 = 0;
                } else {
                    p3 = 1;
                }

                int c4 = bitmap.getPixel(k * 8 + 4, i);
                byte p4;
                if (c4 == -1) {
                    p4 = 0;
                } else {
                    p4 = 1;
                }

                int c5 = bitmap.getPixel(k * 8 + 5, i);
                byte p5;
                if (c5 == -1) {
                    p5 = 0;
                } else {
                    p5 = 1;
                }

                int c6 = bitmap.getPixel(k * 8 + 6, i);
                byte p6;
                if (c6 == -1) {
                    p6 = 0;
                } else {
                    p6 = 1;
                }

                int c7 = bitmap.getPixel(k * 8 + 7, i);
                byte p7;
                if (c7 == -1) {
                    p7 = 0;
                } else {
                    p7 = 1;
                }

                int value = p0 * 128 + p1 * 64 + p2 * 32 + p3 * 16 + p4 * 8 + p5 * 4 + p6 * 2 + p7;
                bitbuf[k] = (byte) value;
            }

            for (k = 0; k < width / 8; ++k) {
                ++s;
                imgbuf[s] = bitbuf[k];
            }
        }

        return imgbuf;
    }

    public static Bitmap filter(Bitmap nbm, int width, int height) {
        int[] inPixels = new int[width * height];
        nbm.getPixels(inPixels, 0, width, 0, 0, width, height);
        int[] outPixels = new int[inPixels.length];
        for (int row = 0; row < height; ++row) {
            for (int col = 0; col < width; ++col) {
                int index = row * width + col;
                int r1 = inPixels[index] >> 16 & 255;
                int g1 = inPixels[index] >> 8 & 255;
                int b1 = inPixels[index] & 255;
                int cIndex = getCloseColor(r1, g1, b1);
                outPixels[index] = -16777216 | COLOR_PALETTE[cIndex][0] << 16 | COLOR_PALETTE[cIndex][1] << 8 | COLOR_PALETTE[cIndex][2];
                int[] ergb = new int[]{r1 - COLOR_PALETTE[cIndex][0], g1 - COLOR_PALETTE[cIndex][1], b1 - COLOR_PALETTE[cIndex][2]};
                float e1;
                int[] rgb4;
                int[] rgb5;
                int[] rgb6;
                if (method == 1) {
                    e1 = 0.4375F;
                    float e2 = 0.3125F;
                    float e3 = 0.1875F;
                    float e4 = 0.0625F;
                    rgb4 = getPixel(inPixels, width, height, col + 1, row, e1, ergb);
                    rgb5 = getPixel(inPixels, width, height, col, row + 1, e2, ergb);
                    rgb6 = getPixel(inPixels, width, height, col - 1, row + 1, e3, ergb);
                    //int[] rgb4 = getPixel(inPixels, width, height, col + 1, row + 1, e4, ergb);???
                    int[] rgb7 = getPixel(inPixels, width, height, col + 1, row + 1, e4, ergb);
                    setPixel(inPixels, width, height, col + 1, row, rgb4);
                    setPixel(inPixels, width, height, col, row + 1, rgb5);
                    setPixel(inPixels, width, height, col - 1, row + 1, rgb6);
                    setPixel(inPixels, width, height, col + 1, row + 1, rgb7);
                } else {
                    if (method != 2) {
                        throw new IllegalArgumentException("Not Supported Dither Mothed!!");
                    }

                    e1 = 0.125F;
                    int[] rgb1 = getPixel(inPixels, width, height, col + 1, row, e1, ergb);
                    int[] rgb2 = getPixel(inPixels, width, height, col + 2, row, e1, ergb);
                    int[] rgb3 = getPixel(inPixels, width, height, col - 1, row + 1, e1, ergb);
                    rgb4 = getPixel(inPixels, width, height, col, row + 1, e1, ergb);
                    rgb5 = getPixel(inPixels, width, height, col + 1, row + 1, e1, ergb);
                    rgb6 = getPixel(inPixels, width, height, col, row + 2, e1, ergb);
                    setPixel(inPixels, width, height, col + 1, row, rgb1);
                    setPixel(inPixels, width, height, col + 2, row, rgb2);
                    setPixel(inPixels, width, height, col - 1, row + 1, rgb3);
                    setPixel(inPixels, width, height, col, row + 1, rgb4);
                    setPixel(inPixels, width, height, col + 1, row + 1, rgb5);
                    setPixel(inPixels, width, height, col, row + 2, rgb6);
                }
            }
        }

        return Bitmap.createBitmap(outPixels, 0, width, width, height, Bitmap.Config.RGB_565);
    }

    static byte[] pixToEscNvBitImageCmd(byte[] src, int width, int height) {
        byte[] data = new byte[src.length / 8 + 4];
        data[0] = (byte) (width / 8 % 256);
        data[1] = (byte) (width / 8 / 256);
        data[2] = (byte) (height / 8 % 256);
        data[3] = (byte) (height / 8 / 256);

        for (int i = 0; i < width; ++i) {
            int k = 0;
            for (int j = 0; j < height / 8; ++j) {
                data[4 + j + i * height / 8] = (byte) (P0[src[i + k]] + P1[src[i + k + width]] + P2[src[i + k + 2 * width]] + P3[src[i + k + 3 * width]] + P4[src[i + k + 4 * width]] + P5[src[i + k + 5 * width]] + P6[src[i + k + 6 * width]] + src[i + k + 7 * width]);
                k += 8 * width;
            }
        }

        return data;
    }

    public static void setPaperWidth(int paperWidth) {
        sPaperWidth = paperWidth;
    }

    static boolean compare(Integer input, int i) {
        List<Integer[]> list = new ArrayList<>();
        list.add(THE_SET0);
        list.add(THE_SET1);
        list.add(THE_SET2);
        return findInArray(list.get(i), input);
    }

    static boolean findInArray(Integer[] integer, int input) {
        for (Integer value : integer) {
            if (value == input) {
                return true;
            }
        }
        return false;
    }

    static int Flag(boolean set1, boolean set2) {
        if (set1 && set2) {
            return 3;
        } else if (!set1 && set2) {
            return 2;
        } else {
            return set1 ? 1 : 0;
        }
    }

    private static void format_K_dither16x16(int[] orgpixels, int xsize, int ysize, byte[] despixels) {
        int k = 0;

        for (int y = 0; y < ysize; ++y) {
            for (int x = 0; x < xsize; ++x) {
                if ((orgpixels[k] & 255) > FLOYD_16_X_16[x & 15][y & 15]) {
                    despixels[k] = 0;
                } else {
                    despixels[k] = 1;
                }

                ++k;
            }
        }

    }

    private static int getCloseColor(int tr, int tg, int tb) {
        int minDistanceSquared = 195076;
        int bestIndex = 0;

        for (int i = 0; i < COLOR_PALETTE.length; ++i) {
            int rdiff = tr - COLOR_PALETTE[i][0];
            int gdiff = tg - COLOR_PALETTE[i][1];
            int bdiff = tb - COLOR_PALETTE[i][2];
            int distanceSquared = rdiff * rdiff + gdiff * gdiff + bdiff * bdiff;
            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                bestIndex = i;
            }
        }

        return bestIndex;
    }

    private static void setPixel(int[] input, int width, int height, int col, int row, int[] p) {
        if (col < 0 || col >= width) {
            col = 0;
        }

        if (row < 0 || row >= height) {
            row = 0;
        }

        int index = row * width + col;
        input[index] = -16777216 | clamp(p[0]) << 16 | clamp(p[1]) << 8 | clamp(p[2]);
    }

    public static int clamp(int value) {
        return value > 255 ? 255 : Math.max(value, 0);
    }

    private static int[] getPixel(int[] input, int width, int height, int col, int row, float error, int[] ergb) {
        if (col < 0 || col >= width) {
            col = 0;
        }

        if (row < 0 || row >= height) {
            row = 0;
        }

        int index = row * width + col;
        int tr = input[index] >> 16 & 255;
        int tg = input[index] >> 8 & 255;
        int tb = input[index] & 255;
        tr = (int) ((float) tr + error * (float) ergb[0]);
        tg = (int) ((float) tg + error * (float) ergb[1]);
        tb = (int) ((float) tb + error * (float) ergb[2]);
        return new int[]{tr, tg, tb};
    }

    public int getMethod() {
        return method;
    }

    public static void setMethod(int method) {
        GpUtils.method = method;
    }
} 
