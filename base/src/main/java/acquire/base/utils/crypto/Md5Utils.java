package acquire.base.utils.crypto;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * MD5
 *
 * @author Janson
 * @date 2019/7/1 11:25
 */
public class Md5Utils {
    /**
     * MD5 digest
     *
     * @param data data to be digested
     * @return MD5 result
     */
    public static byte[] md5(byte[] data) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            return md.digest(data);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sign data by HMAC-MD5
     *
     * @param data data to be signed
     * @param key  MD5 key
     * @return MD5 result
     */
    public static byte[] hmacMd5(byte[] key, byte[] data) {
        try {
            SecretKeySpec signinKey = new SecretKeySpec(key, "HmacMD5");
            Mac mac = Mac.getInstance("HmacMD5");
            mac.init(signinKey);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }
}
