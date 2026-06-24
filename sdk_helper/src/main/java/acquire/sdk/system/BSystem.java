package acquire.sdk.system;


import android.app.Activity;
import android.app.Dialog;
import android.view.Window;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.internal.setting.Settings;
import com.newland.nsdk.core.api.internal.setting.SettingsManager;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.ToastUtils;

/**
 * POS System settings
 *
 * @author Xulf
 * @date 2021/3/10 14:58
 */
public class BSystem {

    /**
     * Set device task button.
     *
     * @param enable true if task button can be clicked.
     */
    public static void setTaskButton(boolean enable) {
        SettingsManager settingsManager = (SettingsManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SETTINGS);
        if (settingsManager != null) {
            try {
                if (enable) {
                    settingsManager.set(Settings.SETTING_DISABLE_APP_SWITCH_KEY, "1");
                } else {
                    settingsManager.set(Settings.SETTING_DISABLE_APP_SWITCH_KEY, "0");
                }
            } catch (Exception e) {
                LoggerUtils.e("BSystem setTaskButton["+enable+"] failed!",e);
            }
        } else {
            LoggerUtils.e("SettingsManager is null.");
        }
    }

    /**
     * Set device home button.
     *
     * @param enable true if home button can be clicked.
     */
    public static void setHomeButton(boolean enable) {
        SettingsManager settingsManager = (SettingsManager)NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SETTINGS);
        if (settingsManager != null) {
            try {
                if (enable) {
                    settingsManager.set(Settings.SETTING_DISABLE_HOME_KEY, "0");
                } else {
                    settingsManager.set(Settings.SETTING_DISABLE_HOME_KEY, "1" );
                }
            } catch (Exception e) {
                LoggerUtils.e("BSystem setHomeButton["+enable+"] failed!",e);
            }
        } else {
            LoggerUtils.e("SettingsManager is null.");
        }
    }

    /**
     * Set Screen Brightness
     *
     * @param brightness brightness level
     */
    public static void setScreenBrightness(int brightness) {
        SettingsManager settingsManager = (SettingsManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SETTINGS);
        if (settingsManager != null) {
            try {
                settingsManager.set(Settings.SCREEN_BRIGHTNESS, "" + brightness);
            } catch (Exception e) {
                LoggerUtils.e("BSystem setScreenBrightness["+brightness+"] failed!",e);
            }
        } else {
            LoggerUtils.e("SettingsManager is null.");
        }
    }

    /**
     * Set screen timeout.
     *
     * @param timeout The max time to wait in seconds.
     */
    public static void setScreenTimeout(int timeout) {
        SettingsManager settingsManager = (SettingsManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SETTINGS);
        if (settingsManager != null) {
            try {
                settingsManager.set(Settings.SCREEN_OFF_TIMEOUT, "" + timeout);
            } catch (Exception e) {
                LoggerUtils.e("BSystem setScreenTimeout["+timeout+"] failed!",e);
            }
        } else {
            LoggerUtils.e("SettingsManager is null.");
        }
    }


    /**
     * Observe task button of a window.
     * <p><hr><b>e.g. in {@link Activity}</b></p>
     * <pre>
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         BSystem.observeTaskButton(getWindow());
     *         ...
     *     }
     *     //listener event
     *     public boolean onKeyDown(int keyCode, KeyEvent event) {
     *         if (keyCode == KeyEvent.KEYCODE_MENU) {
     *              ...
     *         }
     *         return super.onKeyDown(keyCode, event);
     *     }
     * </pre>
     *
     * @param window any window. e.g. {@link Activity} or {@link Dialog}
     */
    public static void observeTaskButton(Window window) {
        window.addFlags(5);
    }

    /**
     * Observe home button of a window.
     * <p><hr><b>e.g. in {@link Activity}</b></p>
     * <pre>
     *     protected void onCreate(Bundle savedInstanceState) {
     *         super.onCreate(savedInstanceState);
     *         BSystem.observeHomeButton(getWindow());
     *         ...
     *     }
     *     //listener event
     *     public boolean onKeyDown(int keyCode, KeyEvent event) {
     *         if (keyCode == KeyEvent.KEYCODE_HOME) {
     *             ...
     *         }
     *         return super.onKeyDown(keyCode, event);
     *     }
     * </pre>
     *
     * @param window any window. e.g. {@link Activity} or {@link Dialog}
     */
    public static void observeHomeButton(Window window) {
        window.addFlags(3);

    }
    public static void setRfidLogo(boolean open) {
        SettingsManager settingsManager = (SettingsManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SETTINGS);
        if (settingsManager != null) {
            try {
                settingsManager.set("setting_display_rfid_logo_enable", open?"1":"0");
            } catch (Exception e) {
                LoggerUtils.e("close Rfid Logo failed!",e);
            }
        } else {
            LoggerUtils.e("SettingsManager is null.");
        }
    }
    public static void disableHid(boolean disable) {
        SettingsManager settingsManager = (SettingsManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SETTINGS);
        if (settingsManager != null) {
            try {
                settingsManager.set("setting_hid_disabled", disable?"1":"0");
                ToastUtils.showToast("disableHid:"+disable);
            } catch (Exception e) {
                LoggerUtils.e("set Hid failed!",e);
            }
        } else {
            LoggerUtils.e("SettingsManager is null.");
        }
    }

}
