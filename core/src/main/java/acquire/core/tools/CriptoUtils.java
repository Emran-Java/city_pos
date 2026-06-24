package acquire.core.tools;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CriptoUtils {

    public static String generateKCV(String hexKey) throws Exception {

        byte[] keyBytes = hexStringToByteArray(hexKey);

        // If double-length (16 bytes), convert to 24 bytes (K1 K2 K1)
        if (keyBytes.length == 16) {
            byte[] temp = new byte[24];
            System.arraycopy(keyBytes, 0, temp, 0, 16);
            System.arraycopy(keyBytes, 0, temp, 16, 8); // copy first 8 bytes again
            keyBytes = temp;
        }

        byte[] zeroBlock = new byte[8]; // 8 bytes of zero

        Cipher cipher = Cipher.getInstance("DESede/ECB/NoPadding");
        SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "DESede");

        cipher.init(Cipher.ENCRYPT_MODE, keySpec);
        byte[] encrypted = cipher.doFinal(zeroBlock);

        return bytesToHex(encrypted).substring(0, 6); // First 3 bytes
    }

    private static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] =
                    (byte) ((Character.digit(s.charAt(i), 16) << 4)
                            + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    /*public static void main(String[] args) throws Exception {

        String key = "CF2833E25F0366B8A68B5F06A914DE27";
        System.out.println("3DES KCV: " + generateKCV(key));
    }*/
}
