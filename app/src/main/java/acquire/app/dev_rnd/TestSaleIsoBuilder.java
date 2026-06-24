package acquire.app.dev_rnd;

public class TestSaleIsoBuilder {
    public static byte[] buildTestSaleRequest() {

        String isoHex =

                "6000000000" +                 // TPDU
                        "0200" +                       // MTI
                        "3020058020C08204" +           // Bitmap

                        "000000" +                     // DE3 // sale for fix
                        "000000000100" +               // DE4 // amount
                        "000007" +                     // DE11 // stand number unique generate by pos
                        "0071" +                       // DE22 // NFC/WAV card read mode 0071
                        "0001" +                       // DE24 // NII (network interface identifier)
                        "00" +                         // DE25 // POS condition code, only for EMI 53
                        "325262383022931622D290662610027153" + // DE35 // CARD read data TRACK 2
                        "3630303131333636" +           // DE41  // TID
                        "313030303030303030303837363030" + // DE42  MID
                        "303530" +                     // DE49 //currency code

                        // DE55 (LLLVAR: 0174)
                        "0174" +                        // 0174  → total length = 0x0174 = 372 hex chars = 186 bytes
                        "5F2A020050" +      // Transaction Currency Code 0050
                        "5F340100" +        // Application PAN Sequence Number
                        "82021980" +        //Application Interchange Profile (AIP)
                        "8407A0000000041010" + // Dedicated File (DF) Name / AID
                        "95050000008801" +      //Terminal Verification Results (TVR)
                        "9A03260111" +
                        "9C01009F0206000000000100" +
                        "9F03060000000000009F0607A0000000041010" +
                        "9F10120114A04301A200000000FFFFFFFFFFFFFFFF" + //Issuer Application Data (IAD).
                        "9F1A0200509F1E083030303030303031" +
                        "9F21031424109F26081D1069E868774E7D" +
                        "9F2701809F3303E008C8" +
                        "9F34031F0302" +
                        "9F350122" +
                        "9F360202E6" +
                        "9F37047FAEEC2D" +
                        "9F6E0700500000323000" +
                        "9F410400000001" +
                        "9F5D03000000" +

                        // DE62 (LLLVAR: 0006)
                        "0006" +
                        "303030303037";

        return HexUtil.withLengthHeader(isoHex);
    }


}