package acquire.sdk;

import android.content.Context;
import android.os.ParcelFileDescriptor;

import com.newland.modules.flyparameterparser.core.handler.ParameterFileHandler;
import com.newland.toms.client.api.IAppParameterFileListener;
import com.newland.toms.client.api.IServiceConnectListener;
import com.newland.toms.client.api.TOMSClientAPIManager;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.file.FileUtils;
import acquire.base.utils.thread.Locker;

/**
 * A tool for Fly Parameter Service. The application can receive the Fly Parameter Service notification.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *     //step 1.initialize
 *     public class App extends Application {
 *            public void onCreate() {
 *               super.onCreate();
 *               //As early as possible,initialize in the Application.
 *                FlyParameterHelper.getInstance().bind(this);
 *           }
 *      }
 *
 *     ...
 *     //step 2. fetch TOMS information
 *     private void receiveToms(){
 *           FlyParameterHelper.getInstance().fetchParameters(FileDir.EXTERNAL_ROOT + File.separator + "tmp",new FlyParameterCallback() {
 *               private final static String CONFIG_FILE_TYPE = ".CFG";
 *               public void onReceive(File downloadFolder) {
 *                   //... handle folder
 *               }
 *
 *               public void onError(int errorCode, String message) {
 *               }
 *           });
 *     }
 * </pre>
 *
 * @author Janson
 * @date 2022/1/27 14:58
 */
public class FlyParameterHelper {
    private static volatile FlyParameterHelper instance;
    private Context appContext;
    private boolean updating;
    private boolean hasConnectedParameter;

    private FlyParameterHelper() {
    }

    public static FlyParameterHelper getInstance() {
        if (instance == null) {
            synchronized (FlyParameterHelper.class) {
                if (instance == null) {
                    instance = new FlyParameterHelper();
                }
            }
        }
        return instance;
    }

    /**
     * bind Fly Parameter service
     */
    public boolean bind(Context context) {
        appContext = context.getApplicationContext();
        if (hasConnectedParameter) {
            return true;
        }
        Locker<Boolean> locker = new Locker<>();
        TOMSClientAPIManager.getInstance().connectAppParameterService(appContext, new IServiceConnectListener() {
            @Override
            public void onConnected() {
                LoggerUtils.d("connected Fly Parameter service success.");
                hasConnectedParameter = true;
                locker.setResult(true);
                locker.wakeUp();
            }

            @Override
            public void onFailed(String error) {
                LoggerUtils.e("connected Fly Parameter service failed.");
                hasConnectedParameter = false;
                locker.setResult(false);
                locker.wakeUp();
            }
        });
        locker.waiting();
        return locker.getResult();
    }

    /**
     * unbind Fly parameter
     */
    public void unbind() {
        if (hasConnectedParameter && appContext != null) {
            TOMSClientAPIManager.getInstance().disconnectAppParameterService(appContext);
            hasConnectedParameter = false;
        }
    }

    /**
     * set the watcher of Fly parameter
     */
    public void setParameterWatcher(Context context, Runnable watcher) {
        TOMSClientAPIManager.getInstance().addAppParameterUpdateListener(context, () -> {
            if (updating) {
                return;
            }
            watcher.run();
        });
    }

    /**
     * Whether to bind Fly parameter
     */
    public boolean isBind() {
        return hasConnectedParameter;
    }

    /**
     * Fetch parameters from Fly parameter
     *
     * @param flyParameterCallback download result
     */
    public void fetchParameters(FlyParameterCallback flyParameterCallback) {
        if (!hasConnectedParameter) {
            flyParameterCallback.onError(0xFF, BaseApplication.getAppString(R.string.sdk_helper_fly_parameter_not_init));
            return;
        }
        if (updating) {
            flyParameterCallback.onError(0xFF, BaseApplication.getAppString(R.string.sdk_helper_fly_parameter_updating));
            return;
        }
        updating = true;
        try {
            TOMSClientAPIManager.getInstance().getAppParameterFile(appContext, new IAppParameterFileListener() {
                @Override
                public void onGetParameterFileSuccess(ParcelFileDescriptor parcelFileDescriptor) {
                    String tempDir = BaseApplication.getAppContext().getExternalFilesDir(null).getPath() + File.separator + "FlyParameterTemp";
                    FileUtils.delete(new File(tempDir),true);

                    try (ParcelFileDescriptor.AutoCloseInputStream autoCloseInputStream = new ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor)) {
                        String zip = tempDir + File.separator + "FlyParameter.zip";
                        FileUtils.createFile(zip);
                        FileUtils.copyFileByInputStream(autoCloseInputStream, new File(zip));
                        ParameterFileHandler handler = new ParameterFileHandler(appContext.getPackageName(),zip);
                        Map<String, Object> map = handler.getBody(HashMap.class);
                        flyParameterCallback.onReceive(map);
                    } catch (Exception e) {
                        LoggerUtils.e("TOMSClientAPIManager parcelFileDescriptor error!",e);
                        flyParameterCallback.onError(0xFF, e.toString());
                    } finally {
                        updating = false;
                    }
                }

                @Override
                public void onError(int errorCode, String message) {
                    updating = false;
                    flyParameterCallback.onError(errorCode, message);
                }
            });
        } catch (Exception e) {
            LoggerUtils.e("TOMSClientAPIManager.getInstance().getAppParameterFile failed!",e);
            updating = false;
        }
    }


    public interface FlyParameterCallback {

        void onReceive(Map<String, Object> map);

        void onError(int errorCode, String message);
    }

}
