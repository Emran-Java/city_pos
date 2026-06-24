package acquire.app.dev_rnd;

public class HexUtil {

    public static byte[] withLengthHeader(String isoBodyHex) {

        int byteLength = isoBodyHex.length() / 2;
        String lengthHex = String.format("%04X", byteLength);

        return HexUtil.hexToBytes(lengthHex + isoBodyHex);
    }
    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] =
                    (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                            + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}
