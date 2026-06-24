package acquire.core;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.text.TextUtils;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import acquire.base.ActivityStackManager;
import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ParamsUtils;
import acquire.core.constant.ParamsConst;
import acquire.core.constant.TransTag;
import acquire.core.constant.TransType;

/**
 * A auto schedule scheduler at a fixed time.
 * <p>e.g.</p>
 * <pre>
 *     public class SelfCheckHelper {
 *          public static void initDevice(Context context) {
 *                  ...
 *                  SchedulerService.scheduleAutoSettle();
 *          }
 *     }
 * </pre>
 *
 * @author Janson
 * @date 2023/12/8 9:06
 */
public class SchedulerService extends Service {
    private AlarmManager mAlarmManager;
    private PendingIntent mPendingIntent;
    private boolean running;
    private final static String ACTION = "schedule.service";

    private final BroadcastReceiver mAlarmReceiver = new BroadcastReceiver() {
        private int retry = 0;

        @Override
        public void onReceive(Context context, Intent intent) {
            LoggerUtils.d("receive Alarm receiver.");
            mAlarmManager.cancel(mPendingIntent);
            if (TextUtils.isEmpty(getAlarmTime())){
                LoggerUtils.e("SchedulerService time is invalid." );
                return;
            }
            Activity topActivity = ActivityStackManager.getTopActivity();
            if (retry < 3) {
                if ((topActivity instanceof TransActivity)) {
                    LoggerUtils.e("A transaction is being executed, delay Scheduler task 5 minutes.");
                    long delayMillis = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
                    mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, delayMillis, mPendingIntent);
                    retry++;
                    return;
                }
                retry = 0;
                doTask();
                setAlarm();
            }else{
                //failed 3 times, continue next day.
                setAlarm();
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LoggerUtils.d("onStartCommand");
        mAlarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (running && mPendingIntent != null) {
            LoggerUtils.e("cancel last Scheduler.");
            unregisterReceiver(mAlarmReceiver);
            mAlarmManager.cancel(mPendingIntent);
        }
        running = true;
        registerReceiver(mAlarmReceiver, new IntentFilter(ACTION));
        Intent intentAlarm = new Intent(ACTION);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intentAlarm, PendingIntent.FLAG_MUTABLE);
        LoggerUtils.d("start Scheduler");
        setAlarm();
        return super.onStartCommand(intent, flags, startId);
    }
    private void setAlarm() {
        String time = getAlarmTime();
        if (TextUtils.isEmpty(time) || time.length() < 4) {
            LoggerUtils.e("SchedulerService invalid time: " + time);
            return;
        }
        String hour = time.substring(0, 2);
        String min = time.substring(2, 4);
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hour));
        calendar.set(Calendar.MINUTE, Integer.parseInt(min));
        calendar.set(Calendar.SECOND, 0);
        if (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }
        LoggerUtils.d("SchedulerService next task: " + calendar.getTime());
        long timeInMillis = calendar.getTimeInMillis();
        mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, mPendingIntent);
    }

    public static void scheduleAutoSettle() {
        LoggerUtils.d("schedule SchedulerService!!!");
        Context context = BaseApplication.getAppContext();
        Intent intent = new Intent(SchedulerService.ACTION);
        intent.putExtra(TransTag.SETTLE_ALL,true);
        intent.setPackage(context.getPackageName());
        context.startService(intent);
    }


    private void doTask() {
        //write your task.
        //true is continue, false is close scheduler.
        Intent intent = new Intent(this, TransActivity.class);
        intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SETTLE);
        intent.putExtra(TransTag.SETTLE_ALL,true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ActivityCompat.startActivity(this, intent, null);
    }

    /**
     *
     * @return alarm time in hhmm format
     */
    private String getAlarmTime() {
        if (!ParamsUtils.getBoolean(ParamsConst.PARAMS_KEY_AUTO_SETTLE_OPEN)){
            LoggerUtils.e("Auto Settle is closed.");
            return null;
        }
        //set your alarm time by HHmm (hour+minute)
        return ParamsUtils.getString(ParamsConst.PARAMS_KEY_AUTO_SETTLE_TIME,"2300");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onDestroy() {
        LoggerUtils.d("onDestroy");
        super.onDestroy();
        unregisterReceiver(mAlarmReceiver);
        mAlarmManager.cancel(mPendingIntent);
    }
}
