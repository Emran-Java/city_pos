package acquire.base.utils.crypto;

import java.util.Arrays;

/**
 * DUKPT encryption
 *
 * @author Janson
 * @date 2022/10/10 17:04
 */
public class DukptUtils {
    private final static byte[] VETCOR = new byte[]{(byte) 0xC0, (byte) 0xC0, (byte) 0xC0, (byte) 0xC0,
            0x00,0x00,0x00,0x00,
            (byte) 0xC0, (byte) 0xC0, (byte) 0xC0, (byte) 0xC0,
            0x00,0x00,0x00,0x00};

    /**
     * Create IPEK(initial PEK)
     *
     * @param bdk root key. It must be destroyed after using for safety.
     * @param ksn data from the BDK provider.
     * @return IPEK
     */
    public static byte[] generateIpekByBdk(byte[] bdk, byte[] ksn) {
        byte[] ipekLeft = DesUtils.softDes(bdk, Arrays.copyOfRange(ksn, 0, 8));
        byte[] ipekRight = DesUtils.softDes(xor(bdk, VETCOR), Arrays.copyOfRange(ksn, 0, 8));
        return merge(ipekLeft, ipekRight);
    }

    /**
     * Create PEK. PEK will generate PIN\MAC\TDK key.
     *
     * @param ipek initial PEK
     * @param ksn  data that should increase with trade
     * @return New PEK
     */
    public static byte[] generatePek(byte[] ipek, byte[] ksn) {
        return calculatePek(ipek, ksn);
    }

    /**
     * Calculate the new PEK by the previous pek and ksn
     *
     * @param prePek the previous pek
     * @param ksn    data that should increase with trade
     * @return New PEK
     */
    private static byte[] calculatePek(byte[] prePek, byte[] ksn) {

        //the previous ksn
        byte[] preKsn = getPreKsn(ksn);
        if (preKsn != null) {
            //calculate new parentPek by the previous ksn
            prePek = calculatePek(prePek, preKsn);
        }
        //ksn right 8 bytes
        byte[] ksnRigth8 = Arrays.copyOfRange(ksn, 2, 10);
        //1.calculate the new PEK right 8 bytes
        byte[] resultRight8 = calculatePekHalf(prePek, ksnRigth8);
        //2.calculate the new PEK left 8 bytes
        byte[] resultLeft8 = calculatePekHalf(xor(prePek, VETCOR), ksnRigth8);
        //3.merget result
        return merge(resultLeft8, resultRight8);
    }

    /**
     * Calculate the half of the new PEK
     */
    private static byte[] calculatePekHalf(byte[] pek, byte[] ksnRight8) {
        //pek left 8 bytes
        byte[] pekLeft8 = Arrays.copyOfRange(pek, 0, 8);
        //pek right 8 bytes
        byte[] pekRight8 = Arrays.copyOfRange(pek, 8, 16);

        byte[] xorData = xor(ksnRight8, pekRight8);
        byte[] desData = DesUtils.softDes(pekLeft8, xorData);
        return xor(pekRight8, desData);
    }

    /**
     * Create work key key by pek
     *
     * @return Work key {PIN key、MAC key、Data key}
     */
    public static byte[][] generateWorkKey(byte[] pek) {
        byte[] pinKey = xor(pek,new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,0x00, (byte) 0xFF,
                0x00,0x00,0x00,0x00,0x00,0x00,0x00, (byte) 0xFF});
        byte[] macKey =  xor(pek,new byte[]{0x00,0x00,0x00,0x00,0x00,0x00,(byte) 0xFF,0x00,
                0x00,0x00,0x00,0x00,0x00,0x00, (byte) 0xFF,0x00});
        byte[] dataKey =  xor(pek,new byte[]{0x00,0x00,0x00,0x00,0x00,(byte) 0xFF,0x00,0x00,
                0x00,0x00,0x00,0x00,0x00, (byte) 0xFF,0x00,0x00});
        return new byte[][]{pinKey,macKey,dataKey};
    }

    /**
     * Get the previous ksn.
     *
     * @param ksn current ksn
     * @return previous ksn
     */
    private static byte[] getPreKsn(final byte[] ksn) {
        int count = 0;
        //copy ksn
        byte[] parentKsn = new byte[ksn.length];
        System.arraycopy(ksn, 0, parentKsn, 0, ksn.length);
        // Look forward from the end to find 1
        for (int i = 0; i < 21; i++) {
            byte index = (byte) (0x01 << (i % 8));
            if ((parentKsn[parentKsn.length - 1 - i / 8] & index) != 0) {
                count++;
                if (count == 1) {
                    parentKsn[parentKsn.length - 1 - i / 8] &= ~index;
                } else if (count == 2) {
                    break;
                }
            }
        }
        if (count == 2) {
            return parentKsn;
        } else {
            return null;
        }
    }


    /**
     * A byte array  xor another a byte array
     */
    private static byte[] xor(byte[] a, byte[] b) {
        if (a == null || a.length == 0 || b == null || a.length != b.length) {
            return null;
        }
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }
    /**
     * Merge two byte[]
     * <p>e.g. src: [0x01,0x03], bs: [0x04,0x05], return [0x01,0x03,0x04,0x05]
     */
    private static byte[] merge(byte[] src, byte[]... adds) {
        byte[] result = src;
        for (byte[] add : adds) {
            int start = result.length;
            result = Arrays.copyOf(result, result.length + add.length);
            System.arraycopy(add, 0, result, start, add.length);
        }
        return result;
    }
} 
