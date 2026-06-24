package acquire.base.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Base64;

import androidx.annotation.NonNull;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import acquire.base.utils.file.FileUtils;


/**
 * Bitmap utils
 *
 * @author Janson
 */
public class BitmapUtils {
    /**
     * save the picture to the file
     *
     * @param bitmap   bitmap data
     * @param filePath file path
     */
    public static void saveBmp(Bitmap bitmap, String filePath) {
        File file = new File(filePath);
        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
            bos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get bitmap from file
     */
    public static Bitmap readBmp(String filePath) {
        return BitmapFactory.decodeFile(filePath);
    }
    /**
     * Get bitmap from byte array
     */
    public static Bitmap readBmp(byte[] data) {
        return BitmapFactory.decodeByteArray(data, 0, data.length);
    }
    /**
     * Get the lattice data of monochrome bitmap
     */
    public static byte[] readBmpLattice(File file) {
        byte[] tempResult = FileUtils.read(file);
        if (tempResult == null) {
            return null;
        }
        byte[] result = new byte[tempResult.length - 14 - 40 - 8];
        System.arraycopy(tempResult, 14 + 40 + 8 - 1, result, 0, result.length);
        return result;
    }

    /**
     * read bmp bytes
     */
    public static byte[] readBmpData(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }


    /**
     * Copies the specified range of the specified bitmap into a new bitmap
     *
     * @param originalBitmap the bitmap from which a range is to be copied
     * @param widthFrom      the initial x coordinate of the bitmap to be copied
     * @param widthTo        the final x coordinate of the bitmap to be copied
     * @param heightFrom     the initial y coordinate of the bitmap to be copied
     * @param heightTo       the final y coordinate of the bitmap to be copied
     * @return a new bitmap containing the specified range from the original bitmap
     */
    public static Bitmap copyOfRange(@NonNull Bitmap originalBitmap, int widthFrom, int widthTo, int heightFrom, int heightTo) {
        int newWidth = widthTo - widthFrom;
        int newHeight = heightTo - heightFrom;
        if (newWidth > originalBitmap.getWidth() || newHeight > originalBitmap.getHeight()) {
            return null;
        }
        Bitmap newBitmap = Bitmap.createBitmap(newWidth, newHeight, originalBitmap.getConfig());
        Canvas canvas = new Canvas(newBitmap);
        Rect srcRect = new Rect(widthFrom, heightFrom, widthTo, heightTo);
        Rect destRect = new Rect(0, 0, newWidth, newHeight);
        canvas.drawBitmap(originalBitmap, srcRect, destRect, null);
        return newBitmap;
    }

    /**
     * merge 2 bitmaps vertically
     */
    public static Bitmap mergeVertical(@NonNull Bitmap bitmap1, @NonNull Bitmap bitmap2) {
        int width = Math.max(bitmap1.getWidth(), bitmap2.getWidth());
        int height = bitmap1.getHeight() + bitmap2.getHeight();
        Bitmap mergedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergedBitmap);
        canvas.drawBitmap(bitmap1, 0, 0, null);
        canvas.drawBitmap(bitmap2, 0, bitmap1.getHeight(), null);
        return mergedBitmap;
    }

    /**
     * merge 2 bitmaps horizontally
     */
    public static Bitmap mergeHorizontal(@NonNull Bitmap bitmap1, @NonNull Bitmap bitmap2) {
        int width = bitmap1.getWidth() + bitmap2.getWidth();
        int height = Math.max(bitmap1.getHeight(), bitmap2.getHeight());
        Bitmap mergedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mergedBitmap);
        canvas.drawBitmap(bitmap1, 0, 0, null);
        canvas.drawBitmap(bitmap2, bitmap1.getWidth(), 0, null);
        return mergedBitmap;
    }

    /**
     * bitmap to YUV(420)
     */
    public static byte[] bitmapToYuv420(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        // Bitmap ->ARGB8888
        int[] argbPixels = new int[width * height];
        bitmap.getPixels(argbPixels, 0, width, 0, 0, width, height);

        // ARGB8888 -> YUV
        byte[] yuvData = new byte[width * height * 3 / 2];
        int inputOffset = 0;
        int outputOffset = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                int argb = argbPixels[inputOffset++];
                int alpha = (argb >> 24) & 0xFF;
                int red = (argb >> 16) & 0xFF;
                int green = (argb >> 8) & 0xFF;
                int blue = argb & 0xFF;

                // RGB to YUV conversion
                byte y = (byte) (16 + ((66 * red + 129 * green + 25 * blue + 128) >> 8));
                byte u = (byte) (128 + ((-38 * red - 74 * green + 112 * blue + 128) >> 8));
                byte v = (byte) (128 + ((112 * red - 94 * green - 18 * blue + 128) >> 8));

                yuvData[outputOffset++] = y;

                // Take a value every 2 pixels for U and V
                if (i % 2 == 0 && j % 2 == 0) {
                    yuvData[outputOffset++] = u;
                    yuvData[outputOffset++] = v;
                }
            }
        }
        return yuvData;
    }

    /**
     * YUV(420) to bitmap
     */
    public static Bitmap yuv420ToBitmap(byte[] yuvData, int width, int height) {
        // RGB data
        int[] rgbData = new int[width * height];
        int i, j;
        int y, u, v;
        int yp, up, vp;
        int r, g, b;
        int index = 0;
        for (i = 0; i < height; i++) {
            yp = i * width;
            up = (i / 2) * (width / 2);
            vp = (i / 2) * (width / 2);

            for (j = 0; j < width; j++) {
                y = yuvData[yp + j] & 0xff;
                u = yuvData[width * height + up + (j / 2)] & 0xff;
                v = yuvData[width * height + width * height / 4 + vp + (j / 2)] & 0xff;

                // YUV to RGB conversion
                r = (int) (y + 1.402 * (v - 128));
                g = (int) (y - 0.344136 * (u - 128) - 0.714136 * (v - 128));
                b = (int) (y + 1.772 * (u - 128));

                // Clipping RGB values to be within 0-255
                r = Math.max(0, Math.min(255, r));
                g = Math.max(0, Math.min(255, g));
                b = Math.max(0, Math.min(255, b));

                // Combining R, G, B values into a single pixel
                rgbData[index++] = Color.rgb(r, g, b);
            }
        }
        return Bitmap.createBitmap(rgbData, width, height, Bitmap.Config.ARGB_8888);
    }



    public static String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);
    }
}
