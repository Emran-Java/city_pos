package acquire.sdk;

import android.content.Context;

import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.utils.LogLevel;
import com.newland.nsdk.core.api.common.utils.NativeDebugLevel;
import com.newland.nsdk.core.api.internal.NSDKModuleManager;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.emv.EmvProvider;

/**
 * Service Executor. As early as possible, it is recommended to initialize in the Application
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *     public class App extends Application {
 *           public void onCreate() {
 *              super.onCreate();
 *              //initialize when starting Application.
 *              ServiceHelper.getInstance().init(this);
 *          }
 *     }
 *  </pre>
 *
 * @author Janson
 * @date 2019/10/17 16:54
 */
public class ServiceHelper {
    /**
     * singleton instance
     */
    private static volatile ServiceHelper instance;

    private NSDKModuleManager nsdkModuleManager = null;

    private ServiceHelper() {
    }

    public static ServiceHelper getInstance() {
        if (instance == null) {
            synchronized (ServiceHelper.class) {
                if (instance == null) {
                    instance = new ServiceHelper();
                }
            }
        }
        return instance;
    }

    /**
     * Init NSDK module
     *
     * @return true if NSDK module inited successfully.
     */
    public boolean init(Context context) {
        if (!isInit()) {
            try {
                LoggerUtils.d("[NSDK ServiceHelper]--NSDK init start.");
                nsdkModuleManager = NSDKModuleManagerImpl.getInstance();
                nsdkModuleManager.init(context.getApplicationContext());
                LoggerUtils.d("[NSDK ServiceHelper]--NSDK init over.");
                nsdkModuleManager.setDebugMode(LogLevel.DEBUG);
            } catch (Exception e) {
                LoggerUtils.e("[NSDK ServiceHelper]--NSDK init failed.", e);
                nsdkModuleManager = null;
                return false;
            }
        }
        return true;
    }

    public void openNatvieLog(boolean open){
        if (nsdkModuleManager != null){
            try {
                nsdkModuleManager.setNativeDebugMode(open? NativeDebugLevel.ALL_ON:NativeDebugLevel.ALL_OFF);
            } catch (NSDKException e) {
                LoggerUtils.e("setNativeDebugMode in NSDK failed!",e);
            }
        }
    }
    /**
     * Return true if NSDK module is initialized.
     *
     * @return truen if nsdkModuleManager isn't null.
     */
    public boolean isInit() {
        return null != nsdkModuleManager;
    }

    /**
     * unbind service
     */
    public void destroy() {
        if (isInit()) {
            nsdkModuleManager.destroy();
            nsdkModuleManager = null;
            EmvProvider.release();
        } else {
            LoggerUtils.i("[NSDK ServiceHelper]--NSDK isn't initialized,so doesn't execute method[destroy]!");
        }
    }

}
