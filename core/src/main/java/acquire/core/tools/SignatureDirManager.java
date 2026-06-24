package acquire.core.tools;

import java.io.File;

import acquire.base.utils.file.FileUtils;
import acquire.core.constant.FileDir;

/**
 * Signature directory manager
 *
 * @author Janson
 * @date 2021/9/14 17:22
 */
public class SignatureDirManager {
    /**
     * get the directory signature of the merchant.
     *
     * @param mid merchant id
     * @param tid terminal id
     * @return signature directory path.
     */
    public static String getSignatureDir(String mid, String tid) {
        return FileDir.SIGNATURE_DIR + File.separator + mid + "-" + tid;
    }
    /**
     * get the directory signature of the merchant.
     *
     * @param mid merchant id
     * @param tid terminal id
     * @return signature directory path.
     */
    public static String getSignatureFile(String mid, String tid,String trace) {
        return getSignatureDir(mid,tid) +File.separator+trace+".bmp";
    }


    /**
     * clear the signature files of the merchant.
     *
     * @param mid merchant id
     * @param tid terminal id
     */
    public static void clearSignatureDir(String mid, String tid) {
        String dir = getSignatureDir(mid, tid);
        FileUtils.clearDir(new File(dir));
    }

    /**
     * clear all signature files.
     */
    public static void clearAllSignature() {
        FileUtils.clearDir(new File(FileDir.SIGNATURE_DIR));
    }
} 
