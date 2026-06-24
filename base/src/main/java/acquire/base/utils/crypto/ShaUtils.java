package acquire.base.utils.crypto;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * SHA1
 *
 * @author Janson
 * @date 2019/7/1 11:03
 */
public class ShaUtils {
    /**
     * Get sha1
     *
     * @param data data to be digested
     * @return SHA1 value
     */
    public static byte[] sha1(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get SHA256
     *
     * @param data data to be digested
     * @return SHA256 value
     */
    public static byte[] sha256(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get SHA512
     *
     * @param data data to be digested
     * @return SHA512 value
     */
    public static byte[] sha512(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * HMAC-SHA1 sign data
     *
     * @param data data to be signed
     * @param key  HMAC-SHA1 key
     * @return HMAC-SHA1 signature
     */
    public static byte[] hmacSha1(byte[] key, byte[] data) {
        try {
            SecretKeySpec signinKey = new SecretKeySpec(key, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signinKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * HMAC-SHA256 sign data
     *
     * @param data data to be signed
     * @param key  HMAC-SHA256 key
     * @return HMAC-SHA256 signature
     */
    public static byte[] hmacSha256(byte[] key, byte[] data) {
        try {
            SecretKeySpec signinKey = new SecretKeySpec(key, "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signinKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * HMAC-SHA512 sign data
     *
     * @param data data to be signed
     * @param key  HMAC-SHA512 key
     * @return HMAC-SHA512 signature
     */
    public static byte[] hmacSha512(byte[] key, byte[] data) {
        try {
            SecretKeySpec signinKey = new SecretKeySpec(key, "HmacSHA512");
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(signinKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
    }
}
