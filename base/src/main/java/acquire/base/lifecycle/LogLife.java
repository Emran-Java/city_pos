package acquire.base.lifecycle;


import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.util.Locale;

import acquire.base.utils.LoggerUtils;


/**
 * Print life log
 *
 * @author Janson
 * @date 2018/11/19 21:03
 */
public class LogLife implements DefaultLifecycleObserver {

    @Override
    public void onCreate(@NonNull LifecycleOwner owner) {
        LoggerUtils.v( tag(owner)+"->onCreate");
    }
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        LoggerUtils.v( tag(owner)+"->onStart");
    }
    @Override
    public void onResume(@NonNull LifecycleOwner owner) {
        LoggerUtils.v( tag(owner)+"->onResume");
    }
    @Override
    public void onPause(@NonNull LifecycleOwner owner) {
        LoggerUtils.v( tag(owner)+"->onPause");
    }
    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        LoggerUtils.v( tag(owner)+"->onStop");
    }
    @Override
    public void onDestroy(@NonNull LifecycleOwner owner) {
        LoggerUtils.v( tag(owner)+"->onDestroy");
    }
    
    private String tag(Object o){
        return String.format(Locale.getDefault(),"%s{%02x}",o.getClass().getSimpleName(),System.identityHashCode(o));
    }
}
