package acquire.base.utils.crypto;


import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


/**
 * DES by android native
 *
 * @author Janson
 * @date 2019/3/28 14:04
 */
public class DesUtils {
    //PKCS5Padding:end filled with bytes count missing
    /**
     * DES mode
     */
    private final static String ALGORITHM_DES = "DES/ECB/NoPadding";
    /**
     * DES3 mode
     */
    private final static String ALGORITHM_DES3 = "DESede/ECB/NoPadding";

    /**
     * DES encrypt
     *
     * @param key  DES key
     * @param plainText data to be encrypted
     * @return cipher data
     */
    public static byte[] softDes(byte[] key, byte[] plainText) {
        if (plainText == null || plainText.length == 0) {
            System.err.println("des plainText is null");
            return null;
        }
        if (key == null){
            System.err.println("des key is null");
            return null;
        }
        //1.confirm whether to use DES or DES3
        String algorithm;
        if (key.length == 8) {
            algorithm = ALGORITHM_DES;
        } else {
            algorithm = ALGORITHM_DES3;
        }
        try {
            //2.create key
            SecretKey deskey = new SecretKeySpec(buildDesKey(key), algorithm);
            //3.init cipher
            Cipher c = Cipher.getInstance(algorithm);
            c.init(Cipher.ENCRYPT_MODE, deskey);
            //4.encrypt plainText
            return c.doFinal(plainText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * DES decrypt
     *
     * @param key  DES key
     * @param cipherText data to be decrypted
     * @return plain data
     */
    public static byte[] softUndes(byte[] key, byte[] cipherText) {
        if (cipherText == null || cipherText.length == 0) {
            System.err.println("des cipherText is null");
            return null;
        }
        if (key == null){
            System.err.println("des key is null");
            return null;
        }
        //1.confirm whether to use DES or DES3
        String algorithm;
        if (key.length == 8) {
            algorithm = ALGORITHM_DES;
        } else {
            algorithm = ALGORITHM_DES3;
        }
        try {
            //2.create key
            SecretKey deskey = new SecretKeySpec(buildDesKey(key), algorithm);
            //3.init cipher
            Cipher c = Cipher.getInstance(algorithm);
            c.init(Cipher.DECRYPT_MODE, deskey);
            //4.decrypt cipherText
            return c.doFinal(cipherText);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Create real des key.
     *
     * @param keyBytes input des key
     * @return real des key.
     */
    private static byte[] buildDesKey(byte[] keyBytes) {
        if (keyBytes.length == 8) {
            //DES
            return keyBytes;
        } else if (keyBytes.length == 16) {
            //3DES
            //Less than 24 bytes, to fill in to 24 bytes
            byte[] key = new byte[24];
            System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
            System.arraycopy(keyBytes, 0, key, 16, 8);
            return key;
        } else if (keyBytes.length == 24) {
            //3DES
            byte[] key = new byte[24];
            System.arraycopy(keyBytes, 0, key, 0, keyBytes.length);
            return key;
        } else {
            throw new IllegalArgumentException("Des3Encrypt only support keylength:8,16,24");
        }
    }


    /**
     * Pad the data using PKCS#7 padding.
     *
     * @param data      originalData（could be null）
     * @param blockSize  8 or 16
     * @return byte[]
     */
    public static byte[] pad(byte[] data, int blockSize) {
        if (blockSize <= 0 || blockSize > 255) {
            throw new IllegalArgumentException("Block size must be between 1 and 255");
        }
        int paddingSize = blockSize - (data.length % blockSize);
        byte[] padded = new byte[data.length + paddingSize];
        System.arraycopy(data, 0, padded, 0, data.length);
        Arrays.fill(padded, data.length, padded.length, (byte) paddingSize);
        return padded;
    }

    /**
     * remove PKCS#7 pad
     *
     * @param padded    padded data
     * @param blockSize size
     * @return originalData
     * @throws IllegalArgumentException Invalid padding
     */
    public static byte[] unpad(byte[] padded, int blockSize) {
        if (padded == null || padded.length == 0) {
            throw new IllegalArgumentException("Padded data cannot be null or empty");
        }
        if (padded.length % blockSize != 0) {
            throw new IllegalArgumentException("Padded data length is not a multiple of block size");
        }
        int paddingSize = padded[padded.length - 1] & 0xFF;
        if (paddingSize < 1 || paddingSize > blockSize) {
            throw new IllegalArgumentException("Invalid padding size: " + paddingSize);
        }
        for (int i = 1; i <= paddingSize; i++) {
            if ((padded[padded.length - i] & 0xFF) != paddingSize) {
                throw new IllegalArgumentException("Invalid padding bytes");
            }
        }
        byte[] original = new byte[padded.length - paddingSize];
        System.arraycopy(padded, 0, original, 0, original.length);
        return original;
    }
}
