package acquire.base;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import acquire.base.utils.LoggerUtils;

/**
 * Activity Stack Manager
 *
 * @author Janson
 * @date 2021/3/23 17:07
 */
public class ActivityStackManager {

    /**
     * The list of all existing {@link Activity}
     */
    private final static List<Activity> ACTIVITIES = new LinkedList<>();

    /**
     * Bind application life
     *
     * @param application app
     */
    public static void bindApp(@NonNull Application application) {
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
                if (activity == getTopActivity()) {
                    return;
                }
                LoggerUtils.d("add Activity:"+activity);
                ACTIVITIES.add(activity);
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity,@NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
                //destroy an activity
                LoggerUtils.d("remove Activity:"+ activity);
                ACTIVITIES.remove(activity);
            }
        });
    }


    public static Activity getTopActivity() {
        Activity activity = null;
        if (!ACTIVITIES.isEmpty()) {
            activity = ACTIVITIES.get(ACTIVITIES.size() - 1);
        }
        return activity;
    }

    public static void finishAllActivity() {
        Iterator<Activity> it = ACTIVITIES.iterator();
        while (it.hasNext()) {
            Activity activity = it.next();
            if (!activity.isFinishing()) {
                LoggerUtils.d("finish: "+activity);
                activity.finish();
            }
            it.remove();
        }
    }

    public static boolean isExist(Class<? extends Activity> clazz) {
        return countActivity(clazz)>0;
    }
    public static int countActivity(Class<? extends Activity> clazz) {
        int count = 0;
        for (Activity activity : ACTIVITIES) {
            if (activity.getClass() == clazz) {
                count++;
            }
        }
        return count;
    }
    /**
     * The count of existing {@link Activity}
     */
    public static int size() {
        return ACTIVITIES.size();
    }

    /**
     * Get all existing {@link Activity}
     */
    @NonNull
    public static Activity[] getActivities() {
        return ACTIVITIES.toArray(new Activity[0]);
    }
}
