package acquire.app;


import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.core.tools.SelfCheckHelper;

/**
 *  The class maintaining global application state.
 *  Does some initialization in this {@link App} when the application is cold booted .
 *
 * @author Janson
 * @date 2018/9/25 14:11
 */
public class App extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        //catches global exceptions that are not caught
        Thread.setDefaultUncaughtExceptionHandler((thread, e) -> {
            LoggerUtils.e("catch a global uncaught exception!!!",e);
            //0: normal exit, non-0: error exit
            System.exit(1);
            android.os.Process.killProcess(android.os.Process.myPid());
        });
        //self check
        BaseApplication.SINGLE_EXECUTOR.execute(() -> SelfCheckHelper.initAppConfig(this));
    }
}
