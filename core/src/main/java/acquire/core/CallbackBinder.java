package acquire.core;

import android.content.Intent;
import android.os.Binder;

/**
 * binder as a callback
 *
 * @author Janson
 * @date 2025/2/24 10:06
 */
public class CallbackBinder extends Binder {
    private final CallbackInterface callbackMethod;

    public CallbackBinder(CallbackInterface callback) {
        super();
        this.callbackMethod = callback;
    }
    public void onResult(Intent intent) {
        callbackMethod.invoke(intent);
    }

    public interface CallbackInterface {
        void invoke(Intent respData);
    }
}
