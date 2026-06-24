package acquire.base.utils.test;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.crypto.DukptUtils;
import acquire.base.utils.crypto.MacUtils;
import acquire.base.utils.crypto.PinUtils;

/**
 * Test mac or pin.
 *
 * @author Janson
 * @date 2022/4/2 15:42
 */
public class KeyTest {
    public static String testDukptMacByBdk(String bdk, String ksn, String data){
        byte[] ipek = DukptUtils.generateIpekByBdk(BytesUtils.hexToBytes(bdk.trim()),BytesUtils.hexToBytes(ksn));
        return testDukptMacByIpek(BytesUtils.bcdToString(ipek),ksn,data);
    }
    public static String testDukptMacByIpek(String ipek, String ksn, String data){
        byte[] pek = DukptUtils.generatePek(BytesUtils.hexToBytes(ipek.trim()),BytesUtils.hexToBytes(ksn));
        byte[] macKey = DukptUtils.generateWorkKey(pek)[1];
        byte[] mac = MacUtils.softMacX919(macKey,BytesUtils.hexToBytes(data));
        return BytesUtils.bcdToString(mac);
    }

    public static String testDukptPinByBdk(String bdk, String ksn, String pin, String cardNo){
        byte[] ipek = DukptUtils.generateIpekByBdk(BytesUtils.hexToBytes(bdk.trim()),BytesUtils.hexToBytes(ksn));
        return testDukptPinByIpek(BytesUtils.bcdToString(ipek),ksn,pin,cardNo);
    }
    public static String testDukptPinByIpek(String ipek, String ksn, String pin, String cardNo){
        byte[] pek = DukptUtils.generatePek(BytesUtils.hexToBytes(ipek.trim()),BytesUtils.hexToBytes(ksn));
        byte[] pinKey = DukptUtils.generateWorkKey(pek)[0];
        byte[] pinBlock = PinUtils.softPinBlock(pinKey,pin,cardNo);
        return BytesUtils.bcdToString(pinBlock);
    }


}
