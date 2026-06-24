package acquire.app.dev_rnd;

public class TestTxnResponseParser {

    public static boolean isApproved(byte[] response) {

        String hex = HexUtil.bytesToHex(response);

        // TPDU (10 hex chars) + MTI (4 chars)
        String mti = hex.substring(14, 18);

        return "0810".equals(mti);
    }
}
