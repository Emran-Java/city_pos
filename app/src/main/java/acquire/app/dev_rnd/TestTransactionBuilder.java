package acquire.app.dev_rnd;

public class TestTransactionBuilder {

    public static byte[] buildTestTxn() {

        /*
        Request
            TPDU: 6000000000
            MTI: 0800 (Network Management Request)
            Processing Code (DE3): 990000 → Test Transaction
            DE11: STAN
            DE24: NII
            DE41: Terminal ID
            Bitmap: 2020010000800000

            Response
                MTI: 0810
                Approved:
                    MTI valid
                    Processing Code same
                    Responsed
        * */

        String hadder   = "001f";
        String tpdu   = "6000000000";
        String mti    = "0800";
        String bitmap = "2020010000800000";

        String de3  = "990000";
        String de11 = "000004";
        String de24 = "0001";
        String de41 = "3630303131333636"; // ASCII hex of "60011366"

        String isoHex =
                hadder+tpdu +
                        mti +
                        bitmap +
                        de3 +
                        de11 +
                        de24 +
                        de41;

        return HexUtil.hexToBytes(isoHex);
    }
}
