package acquire.base.utils.test;

import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Time test
 *
 * @author Janson
 * @date 2018/8/9 16:05
 */
public class TimeTest {
    public static void log(Object flag) {
        Log.e("TimeTest", flag+"->" + System.currentTimeMillis());
    }

    private static long last = 0;

    public static void start() {
        last = System.currentTimeMillis();
    }

    public static void consume(Object flag) {
        long now = System.currentTimeMillis();
        Log.e("TimeTest" , flag+"->use time:" + (now - last));
        last = now;
    }

    public static void traceStart() {
        File file = new File(Environment.getExternalStorageDirectory(), "timeTest");
        //mnt/shell/emulated/0/time.trace
        Debug.startMethodTracing(file.getAbsolutePath());
    }

    public static void traceEnd() {
        Debug.stopMethodTracing();
    }
}
