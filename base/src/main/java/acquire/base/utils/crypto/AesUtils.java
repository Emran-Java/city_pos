package acquire.base.utils.crypto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import acquire.base.utils.LoggerUtils;

/**
 * AES
 *
 * @author Janson
 * @date 2019/5/25 0:54
 */
public class AesUtils {
    private final static String ALGORITHM = "AES";

    /**
     * AES encrypt
     *
     * @param password  AES password
     * @param plainText data to be encrypted
     * @return cipher data
     */
    public static byte[] encrypt(byte[] password, byte[] plainText) {
        try {
            //1.create key generator by password
            KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
            kgen.init(128, new SecureRandom(password));
            //2.create key
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, ALGORITHM);
            //3.init cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            //4.encrypt plainText
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            LoggerUtils.e("Aes encrypt failed!",e);
            return null;
        }
    }

    /**
     * AES encrypt
     *
     * @param password       AES password
     * @param plainText      data to be encrypted
     * @param alogAndPadding padding mode. such as "AES" 、"AES/CBC/PKCS5Padding"
     * @param zeroIv         inital vector
     * @return cipher data
     */
    public static byte[] encrypt(byte[] password, byte[] plainText, String alogAndPadding, byte[] zeroIv) {
        try {
            //1.create key by password
            SecretKeySpec key = new SecretKeySpec(password, ALGORITHM);
            //2.init cipher
            Cipher cipher = Cipher.getInstance(alogAndPadding);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(zeroIv));
            //3.encrypt plainText
            return cipher.doFinal(plainText);
        } catch (Exception e) {
            LoggerUtils.e("Aes encrypt with padding failed!",e);
            return null;
        }
    }

    /**
     * AES decrypt
     *
     * @param password   AES password
     * @param cipherText data to be decrypted
     * @return plain data
     */
    public static byte[] decrypt(byte[] password, byte[] cipherText) {
        try {
            //1.create key generator by password
            KeyGenerator kgen = KeyGenerator.getInstance(ALGORITHM);
            kgen.init(128, new SecureRandom(password));
            //2.create key
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec key = new SecretKeySpec(enCodeFormat, ALGORITHM);
            //3.init cipher
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key);
            //4.decrypt plainText
            return cipher.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException e) {
            LoggerUtils.e("Aes decrypt failed!",e);
            return null;
        }

    }

    /**
     * AES decrypt
     *
     * @param password       AES password
     * @param cipherText     data to be decrypted
     * @param alogAndPadding padding mode. such as "AES" 、"AES/CBC/PKCS5Padding"
     * @param zeroIv         inital vector
     * @return plain data
     */
    public static byte[] decrypt(byte[] password, byte[] cipherText, String alogAndPadding, byte[] zeroIv) {
        try {
            //1.create key by password
            SecretKeySpec key = new SecretKeySpec(password, ALGORITHM);
            //2.init cipher
            Cipher cipher = Cipher.getInstance(alogAndPadding);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(zeroIv));
            //3.decrypt plainText
            return cipher.doFinal(cipherText);
        } catch (Exception e) {
            LoggerUtils.e("Aes decrypt with padding failed!",e);
            return null;
        }
    }
}
