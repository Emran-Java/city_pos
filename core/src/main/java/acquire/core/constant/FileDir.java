package acquire.core.constant;

import android.content.Context;
import android.os.Environment;

import java.io.File;

import acquire.base.BaseApplication;


/**
 * Directory path
 *
 * @author Janson
 * @date 2018/3/26
 */
public class FileDir {
    /**
     * External root directory of the application.
     * <p>below Android 7: /storage/emulated/0  will not be removed with app uninstall.</p>
     * <p>above Android 7(excluding 7): /storage/emulated/0/Android/data/xxx(your PackageName)/files， When the application is unloaded, it will be deleted,
     * but there is no need to apply for read-write permission. If you still want to use the path below Android 9, please use {@link Environment#getExternalStorageDirectory()}</p>
     */
    public final static String APP_EXTERNAL = BaseApplication.getAppContext().getExternalFilesDir(null).getPath()+File.separator;

    public final static String SD_CARD = Environment.getExternalStorageDirectory()+File.separator;


    /**
     * The directory  of PC serial download file.   /Share/EpayParameter/
     */
    public final static String SHARE_PATH = File.separator + "Share" + File.separator + "EpayParameter" + File.separator;

    /**
     * The directory of signature bmp files.  /data/data/your package name/app_signatures/
     */
    public final static String SIGNATURE_DIR = BaseApplication.getAppContext().getDir("signatures", Context.MODE_PRIVATE) + File.separator;


    /**
     * This folder is the decompressed zip downloaded from PC
     */
    public final static String PC_DOWNLOAD = SD_CARD+"custom_acquire"+File.separator;
}
