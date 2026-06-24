package acquire.base.activity.listener;

import android.view.KeyEvent;

/**
 * Listen for {@link android.app.Activity#onKeyDown(int, KeyEvent)}
 * @author Janson
 * @date 2023/3/14 14:20
 */
public interface OnKeyDownListener {
    boolean onKeyDown(int keyCode, KeyEvent event);
} 
