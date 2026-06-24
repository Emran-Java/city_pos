package acquire.base.utils.crypto;


import android.util.Base64;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 * RSA by android native
 *
 * @author Janson
 * @date 2019/11/29 16:22
 */
public class RsaUtils {
    private final static String CIPHER_TRANSFORMATION = "RSA/ECB/PKCS1Padding";
    private final static String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private final static String RSA_ALGORITHM = "RSA";

    /**
     * RSA encrypt by public key
     *
     * @param key       public key
     * @param plainText data to be encrypted
     * @return cipher data
     */
    public static byte[] encryptByPubkey(byte[] key, byte[] plainText) {
        try {
            PublicKey publicKey = getPublicKey(key);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }

    }

    /**
     * RSA decrypt by public key
     *
     * @param pubKey     public key
     * @param cipherText data to be decrypted
     * @return plain data
     */
    public static byte[] decryptByPubkey(byte[] pubKey, byte[] cipherText) {
        try {
            PublicKey publicKey = getPublicKey(pubKey);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * RSA encrypt by private key
     *
     * @param priKey    private key
     * @param plainText data to be encrypted
     * @return cipher data
     */
    public static byte[] encryptByPrivate(byte[] priKey, byte[] plainText) {
        try {
            PrivateKey privateKey = getPrivateKey(priKey);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, privateKey);
            return cipher.doFinal(plainText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }


    /**
     * RSA decrypt by private key
     *
     * @param priKey     private key
     * @param cipherText data to be decrypted
     * @return plain data
     */
    public static byte[] decryptByPrivate(byte[] priKey, byte[] cipherText) {
        try {
            PrivateKey privateKey = getPrivateKey(priKey);
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            if (cipherText.length > 128) {
                int blockSize = cipher.getBlockSize();
                int blockCount = (int) Math.ceil((double) cipherText.length / blockSize);
                byte[] decryptedData = new byte[0];
                for (int i = 0; i < blockCount; i++) {
                    int offset = i * blockSize;
                    int length = Math.min(blockSize, cipherText.length - offset);
                    byte[] block = cipher.doFinal(cipherText, offset, length);
                    byte[] result = new byte[decryptedData.length + block.length];
                    System.arraycopy(decryptedData, 0, result, 0, decryptedData.length);
                    System.arraycopy(block, 0, result, decryptedData.length, block.length);
                    decryptedData = result;
                }
                return decryptedData;
            } else {
                return cipher.doFinal(cipherText);
            }
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException |
                 IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sha256WithRsa sign data
     *
     * @param priKey rsa private key
     * @param data   data to be signed
     * @return data signature
     */
    public static byte[] signBySha256WithRsa(byte[] priKey, byte[] data) {
        try {
            PrivateKey privateKey = getPrivateKey(priKey);
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initSign(privateKey);
            sign.update(data);
            return sign.sign();
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Sha256WithRsa verify data
     *
     * @param pubKey     rsa public key
     * @param data       original data
     * @param signedData data signature
     * @return If true,right.
     */
    public static boolean verifySignBySha256WithRsa(byte[] pubKey, byte[] data, byte[] signedData) {
        try {
            PublicKey publicKey = getPublicKey(pubKey);
            Signature sign = Signature.getInstance(SIGNATURE_ALGORITHM);
            sign.initVerify(publicKey);
            sign.update(data);
            return sign.verify(signedData);
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get {@link PublicKey}.
     * It's a  {@link RSAPublicKey}.
     *
     * @param pubKey public key bytes
     * @return {@link PublicKey}
     */
    private static PublicKey getPublicKey(byte[] pubKey) {
        try {
            X509EncodedKeySpec bobPubKeySpec = new X509EncodedKeySpec(pubKey);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePublic(bobPubKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get {@link PrivateKey}.
     * It's a  {@link RSAPrivateKey}.
     *
     * @param priKey private key bytes
     * @return {@link PrivateKey}
     */
    private static PrivateKey getPrivateKey(byte[] priKey) {

        try {
            PKCS8EncodedKeySpec bobPriKeySpec = new PKCS8EncodedKeySpec(priKey);
            KeyFactory keyFactory = KeyFactory.getInstance(RSA_ALGORITHM);
            return keyFactory.generatePrivate(bobPriKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }

    }


    /**
     * Create rsa key pair
     *
     * @return [Public key,Private key]
     */
    public static byte[][] getKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
            keyPairGenerator.initialize(1024);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            //public key
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            byte[] bPubKey = publicKey.getEncoded();
            //private key
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
            byte[] bPriKey = privateKey.getEncoded();
            return new byte[][]{bPubKey, bPriKey};
        } catch (NoSuchAlgorithmException e1) {
            e1.printStackTrace();
            return null;
        }
    }

    /**
     * Get rsa key pair from pem file
     *
     * @param publicPem       public key PEM file
     * @param privatePkcs8Pem private key PEM file(PKCS8)
     * @return [Public key,Private key]
     */
    public static byte[][] getKeyPairByPem(InputStream publicPem, InputStream privatePkcs8Pem) {
        return new byte[][]{getKeyByPem(publicPem), getKeyByPem(privatePkcs8Pem)};
    }

    /**
     * Get rsa public key according to a modulus and exponent.
     *
     * @param modulus  public key modulus
     * @param exponent public key exponent
     * @return Public key bytes
     */
    public static byte[] getPubKeyByModExp(String modulus, String exponent) {
        RSAPublicKeySpec keySpec = new RSAPublicKeySpec(new BigInteger(modulus, 16), new BigInteger(exponent, 16));
        try {
            KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM);
            PublicKey rsaPublicKey = factory.generatePublic(keySpec);
            return rsaPublicKey.getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get rsa private key according to a modulus and exponent.
     *
     * @param modulus  private key modulus
     * @param exponent private key exponent
     * @return Private key bytes
     */
    public static byte[] getPriKeyByModExp(String modulus, String exponent) {
        RSAPrivateKeySpec keySpec = new RSAPrivateKeySpec(new BigInteger(modulus, 16), new BigInteger(exponent, 16));
        try {
            KeyFactory factory = KeyFactory.getInstance(RSA_ALGORITHM);
            PrivateKey rsaPrivateKey = factory.generatePrivate(keySpec);
            return rsaPrivateKey.getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Get rsa public key modulus and exponent from public key bytes.
     *
     * @param pubKey public key bytes
     * @return [Modulus, Exponent]
     */
    public static byte[][] getPublicModExp(byte[] pubKey) {
        PublicKey publicKey = getPublicKey(pubKey);
        if (publicKey instanceof RSAPublicKey) {
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            return new byte[][]{rsaPublicKey.getModulus().toByteArray(), rsaPublicKey.getPublicExponent().toByteArray()};
        } else {
            return null;
        }
    }

    /**
     * Get rsa private key modulus and exponent from private key bytes.
     *
     * @param priKey private key bytes
     * @return [Modulus, Exponent]
     */
    public static byte[][] getPrivateModExp(byte[] priKey) {
        PrivateKey privateKey = getPrivateKey(priKey);
        if (privateKey instanceof RSAPrivateKey) {
            RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
            return new byte[][]{rsaPrivateKey.getModulus().toByteArray(), rsaPrivateKey.getPrivateExponent().toByteArray()};
        } else {
            return null;
        }
    }

    /**
     * Get a rsa key from pem file
     *
     * @param pemIs PEM file {@link InputStream}
     * @return key bytes
     */
    public static byte[] getKeyByPem(InputStream pemIs) {
        try {
            BufferedReader stringBuffer = new BufferedReader(new InputStreamReader(pemIs));
            StringBuilder keyBuilder = new StringBuilder();
            String buffer;
            while ((buffer = stringBuffer.readLine()) != null) {
                if (buffer.startsWith("--")) {
                    continue;
                }
                keyBuilder.append(buffer);
            }
            return Base64.decode(keyBuilder.toString(), Base64.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
