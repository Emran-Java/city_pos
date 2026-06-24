package acquire.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;

import androidx.annotation.StringRes;

import java.util.concurrent.Executor;

import acquire.base.utils.ParamsUtils;
import acquire.base.utils.thread.CommonPoolExecutor;


/**
 * Base class extends {@link Application}.
 *
 * @author Janson
 * @date 2018/9/25 14:06
 */
public abstract class BaseApplication extends Application {
    public final static Executor SINGLE_EXECUTOR = CommonPoolExecutor.newSinglePool("Application");

    /**
     * A global application context
     */
    @SuppressLint("StaticFieldLeak")
    private static Application application;

    @Override
    public void onCreate() {
        application = this;
        //ActivityManager bind this application
        ActivityStackManager.bindApp(this);
        ParamsUtils.init(this);
        super.onCreate();
    }

    /**
     * Get application context
     */
    public static Context getAppContext() {
        return application;
    }

    /**
     * Get String by resource id
     */
    public static String getAppString(@StringRes int resId, Object... formatArgs) {
        return application.getString(resId,formatArgs);
    }

}
