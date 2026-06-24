package acquire.base.activity;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import acquire.base.R;
import acquire.base.lifecycle.LogLife;
import acquire.base.utils.LoggerUtils;

/**
 * A basic class extended {@link AppCompatActivity}. Its Features:
 * <ul>
 *     <li>
 *         <p><b>manage the fragment</b></p>
 *         <pre>
 *          //open fragment
 *          mSupportDelegate.switchContent(fragment);
 *          //Back to the first fragment in the back stack.
 *          mSupportDelegate.switchFirstContent(fragment);
 *          //pop count fragments
 *          mSupportDelegate.popBackFragment(count);
 *         </pre>
 *     </li>
 *     <li>
 *         <p><b>start an Activity with a callback</b></p>
 *         <pre>
 *          Intent intent = new Intent(this, TransActivity.class);
 *          intent.putExtra(TransTag.TRANS_TYPE, TransType.TRANS_SALE);
 *          mSupportDelegate.startActivityForResult(intent, null, result -> {
 *              //activity result
 *              if (result.getResultCode() == Activity.RESULT_OK){
 *              }
 *              else{
 *              }
 *          });
 *         </pre>
 *     </li>
 * </ul>
 *
 * @author Janson
 * @date 2018/9/12 23:11
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * Support Delegate.
     */
    public SupportDelegate mSupportDelegate;
    /**
     * Print {@link Activity} life log
     */
    private LogLife lifecycleObserver;


    /**
     * Get a layout id that show {@link Fragment}。If 0,it means to not use {@link Fragment}
     *
     * @return fragment layout {@link android.R.id}
     */
    public abstract @IdRes int attachFragmentResId();
    private static Boolean isSquare;

    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    private void screenRotate(){
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                //X direction
                float x = event.values[0];
                //Y direction
                float y = event.values[1];
                //Z direction
                float z = event.values[2];

                int newOrientation ;
                if (isSquare){
                    if (y > 7) {
                        //0°
                        newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    } else if (y < -7) {
                        //180°
                        newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    }else if (x > 7) {
                        // 270°
                        newOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    } else if (x < -7) {
                        // 90°
                        newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
                    } else {
                        // ignore
                        return;
                    }
                }else{
                    if (y > 7) {
                        //0°
                        newOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    } else if (y < -7) {
                        //180°
                        newOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
                    } else {
                        // ignore
                        return;
                    }
                }

                // Orientation Changed
                if (getRequestedOrientation() != newOrientation) {
                    setRequestedOrientation(newOrientation);
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                LoggerUtils.d("onKeyDown Home");
                break;
            case KeyEvent.KEYCODE_BACK:
                LoggerUtils.d("onKeyDown BACK");
                break;
            case KeyEvent.KEYCODE_MENU:
                LoggerUtils.d("onKeyDown MENU");
                break;
            case KeyEvent.KEYCODE_DEL:
                LoggerUtils.d("onKeyDown DEL");
                break;
            case KeyEvent.KEYCODE_ENTER:
                LoggerUtils.d("onKeyDown ENTER");
                break;
            default:
                LoggerUtils.d("onKeyDown " + keyCode);
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //print activity life log
        if (lifecycleObserver == null) {
            lifecycleObserver = new LogLife();
            getLifecycle().addObserver(lifecycleObserver);
        }
        if (isSquare == null){
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            float widthInches = metrics.widthPixels / metrics.xdpi;
            float heightInches = metrics.heightPixels / metrics.ydpi;
            isSquare = Math.abs(widthInches - heightInches)/Math.min(widthInches,heightInches) < 0.2;
        }
        screenRotate();
        //Support Delegate
        mSupportDelegate = new SupportDelegate(getSupportFragmentManager(), attachFragmentResId());
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(sensorEventListener);
    }
    public static void setNaveAndStatusBarColor(@NonNull Window window) {

//        window.setStatusBarColor(ContextCompat.getColor(window.getContext(), acquire.base.R.color.status_bar_color));
        window.setStatusBarColor(ContextCompat.getColor(window.getContext(), R.color.status_bar_color));

        View decor = window.getDecorView();
        decor.setSystemUiVisibility(0);

        window.setNavigationBarColor(
                ContextCompat.getColor(window.getContext(), R.color.nav_bar_color)
        );
    }

}
