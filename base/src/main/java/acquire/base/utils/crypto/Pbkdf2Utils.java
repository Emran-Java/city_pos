package acquire.base.utils.crypto;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Pbkdf2
 *
 * @author Janson
 * @date 2019/7/1 12:02
 */
public class Pbkdf2Utils {

    /**
     * Pdkdf encrypt with password and salt
     *
     * @return pbkdf2 result
     */
    public static byte[] pbkdf2(char[] password, byte[] salt, int interations, int keyLen) {
        KeySpec spec = new PBEKeySpec(password, salt, interations, keyLen * 4);
        try {
            SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            return secretKeyFactory.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }
}
