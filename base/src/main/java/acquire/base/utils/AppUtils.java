package acquire.base.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;

import androidx.core.content.FileProvider;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;


/**
 * App Utils
 *
 * @author Janson
 * @date 2018/9/27 9:35
 */
public class AppUtils {

    public static String getAppName(Context context) {
        String packageName = context.getPackageName();
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (NameNotFoundException e) {
            LoggerUtils.e("Not found "+packageName+"!",e);
            return null;
        }
    }


    public static String getAppVersionName(Context context) {
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(packageName, 0);
            return packInfo.versionName;
        } catch (NameNotFoundException e) {
            LoggerUtils.e("Not found "+packageName+"!",e);
            return null;
        }
    }

    public static Drawable getAppIcon(Context context) {
        String packageName = context.getPackageName();
        try {
            PackageManager pm = context.getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return info.loadIcon(pm);
        } catch (Exception e) {
            LoggerUtils.e("Not found "+packageName+"!",e);
        }

        return null;
    }

    public static long getAppVersionCode(Context context) {
        String packageName = context.getPackageName();
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packInfo = packageManager.getPackageInfo(packageName, 0);
            return packInfo.versionCode;
        } catch (NameNotFoundException e) {
            LoggerUtils.e("Not found "+packageName+"!",e);
            return -1;
        }
    }


    public static void reStartApp(Context context) {
        reStartApp(context, 100);
    }


    public static void reStartApp(Context context, long restartDelay) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        int flag;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        } else {
            flag = PendingIntent.FLAG_CANCEL_CURRENT;
        }
        PendingIntent restartIntent = PendingIntent.getActivity(context.getApplicationContext(), 123456, intent, flag);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (mgr != null) {
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + restartDelay, restartIntent);
        }
        System.exit(0);
    }

    public static String getMainActivity(Context context, String packageName) {
        Intent intent = new Intent("android.intent.action.MAIN", null);
        intent.addCategory("android.intent.category.LAUNCHER");
        List<ResolveInfo> appList = context.getPackageManager().queryIntentActivities(intent, 0);
        for (int i = 0; i < appList.size(); i++) {
            ResolveInfo resolveInfo = appList.get(i);
            String packageStr = resolveInfo.activityInfo.packageName;
            if (packageStr.equals(packageName)) {
                return resolveInfo.activityInfo.name;
            }
        }
        return null;
    }

    /**
     * set app local.
     * After setLanguage, the activity should be restart.
     * e.g.
     * '<pre>
     *      Locale locale = new Locale("en");
     *      AppUtils.setLocale(context,locale)
     *      Intent intent = new Intent(this, YourMainActivity.class);
     *      startActivity(intent);
     *      finish();
     * </pre>
     */
    public static void setLocale(Context context, Locale locale){
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        Configuration configuration = context.getResources().getConfiguration();
        context.createConfigurationContext(configuration);
    }
    /**
     * Get sha1 of app signatures
     */
    public static byte[] getSha1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            MessageDigest md = MessageDigest.getInstance("SHA1");
            return md.digest(cert);
        } catch (NameNotFoundException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Boolean DEBUG;

    /**
     * Get the application debug mode.
     *
     * @return true if application is in debug mode.
     */
    public static boolean isDebug(Context context) {
        if (DEBUG == null) {
            try {
                ApplicationInfo info = context.getApplicationInfo();
                DEBUG = (info.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
            } catch (Exception e) {
                e.printStackTrace();
                DEBUG = false;
            }
        }
        return DEBUG;
    }

    /**
     * install a apk
     */
    public static void installApk(Context context,File apkFile) {
        try {
            Intent intent = new Intent("android.intent.action.INSTALL_APP_HIDE");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            Uri apkUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileProvider", apkFile);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * uninstall an application
     *
     * @param packageName application name to be uninstalled
     */
    public static void uninstallApp(Context context,String packageName) {
        try {
            Uri uri = Uri.parse("package:" + packageName);
            Intent intent = new Intent("android.intent.action.DELETE.HIDE", uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
