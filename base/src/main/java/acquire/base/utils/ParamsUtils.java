package acquire.base.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;

import java.util.Map;
import java.util.Set;


/**
 * Params utils for {@link SharedPreferences}
 *
 * @author Janson
 * @date 2020/5/7 16:14
 */
public class ParamsUtils {

    private static SharedPreferences sharedPreferences;

    /**
     * Init
     */
    public static void init(Context context) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    /**
     * register shared preference change listener
     */
    public static void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }
    public static void unregisterOnSharedPreferenceChangeListener (SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }
    /**
     * Sava params map
     *
     * @param paramsMap params map
     */
    public static boolean save(@NonNull Map<String, String> paramsMap) {
        if (sharedPreferences == null) {
            LoggerUtils.e("ParamsUtils isn't initialized!");
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Set<String> keys = paramsMap.keySet();
        for (String key : keys) {
            editor.putString(key, paramsMap.get(key));
        }
        return editor.commit();
    }

    /**
     * Clear all params
     */
    public static boolean clear() {
        if (sharedPreferences == null) {
            LoggerUtils.e("ParamsUtils isn't initialized!");
            return false;
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        return editor.clear().commit();
    }

    /**
     * Get all params
     */
    public static Map<String, String> get() {
        if (sharedPreferences == null) {
            LoggerUtils.e("ParamsUtils isn't initialized!");
            return null;
        }
        return (Map<String, String>) sharedPreferences.getAll();
    }

    /**
     * Get String
     *
     * @param key          param key
     * @param defaultValue default value
     * @return param string value
     */
    private static String _getString(String key, String defaultValue) {
        if (sharedPreferences == null) {
            LoggerUtils.e("ParamsUtils isn't initialized!");
            return defaultValue;
        }
        if (key == null || "".equals(key)) {
            return defaultValue;
        }
        String value = sharedPreferences.getString(key, null);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    /**
     * Set String
     *
     * @param key   param key
     * @param value param value
     */
    private static boolean _setString(String key, String value) {
        if (sharedPreferences == null) {
            LoggerUtils.e("ParamsUtils isn't initialized!");
            return false;
        }
        if (key == null || "".equals(key)) {
            return false;
        }
        return sharedPreferences.edit().putString(key, value).commit();
    }


    public static String getString(String key) {
        return getString(key, null);
    }


    public static String getString(String key, String defaultValue) {
        String value = _getString(key, defaultValue);
        LoggerUtils.d("Preferences get->key:" + key + ", value:" + value);
        return value;
    }


    public static boolean setObject(String key, Object value) {
        if (value == null){
            LoggerUtils.e("Preferences set->key:" + key + ", value:null");
            return false;
        }
        if (value instanceof Boolean){
            return setBoolean(key, (Boolean) value);
        }else if (value instanceof Integer){
            return setInt(key, (Integer) value);
        }else if (value instanceof Long){
            return setLong(key, (Long) value);
        }else {
            return setString(key, value.toString());
        }
    }


    public static boolean setString(String key, String value) {
        LoggerUtils.d("Preferences set->key:" + key + ", value:" + value);
        return _setString(key, value);
    }

    public static boolean setBoolean(String key, boolean value) {
        try {
            if (key != null && !"".equals(key)) {
                LoggerUtils.d("Preferences set->key:" + key + ", value:" + value);
                String vString = value ? "1" : "0";
                return _setString(key, vString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }


    public static boolean getBoolean(String key, boolean defaultValue) {
        try {
            boolean value = defaultValue;
            if (key != null && !"".equals(key)) {
                String strValue = _getString(key, null);
                if ("1".equals(strValue)) {
                    value = true;
                } else if ("0".equals(strValue)) {
                    value = false;
                }
                LoggerUtils.d("Preferences get->key:" + key + ", value:" + value);
            }
            return value;
        } catch (Exception e) {
            return false;
        }
    }

    public static long getLong(String key) {
        return getLong(key, 0);
    }


    public static long getLong(String key, long defaultValue) {
        try {
            long value = defaultValue;
            if (key != null && !"".equals(key)) {
                String strValue = _getString(key, null);
                value = Long.parseLong(strValue);
            }
            LoggerUtils.d("Preferences get->key:" + key + ", value:" + value);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean setLong(String key, long value) {
        try {
            if (key != null && !"".equals(key)) {
                String strValue = String.valueOf(value);
                LoggerUtils.d("Preferences set->key:" + key + ", value:" + value);
                return _setString(key, strValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int defaultValue) {
        try {
            int value = defaultValue;
            if (key != null && !"".equals(key)) {
                String strValue = _getString(key, null);
                value = Integer.parseInt(strValue);
            }
            LoggerUtils.d("Preferences get->key:" + key + ", value:" + value);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean setInt(String key, int value) {
        try {
            if (key != null && !"".equals(key)) {
                String strValue = String.valueOf(value);
                LoggerUtils.d("Preferences set->key:" + key + ", value:" + value);
                return _setString(key, strValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
    public static boolean setFloat(String key, float value) {
        try {
            if (!TextUtils.isEmpty(key)) {
                String strValue = String.valueOf(value);
                LoggerUtils.d("Preferences set->key:" + key + ", value:" + value);
                return _setString(key, strValue);
            }
        } catch (Exception e) {
            LoggerUtils.e("setFloat("+key+","+value+") error!",e);
        }
        return false;
    }
    public static float getFloat(String key) {
        return getFloat(key,0);
    }
    public static float getFloat(String key, float defaultValue) {
        try {
            float value = defaultValue;
            if (!TextUtils.isEmpty(key)) {
                String strValue = _getString(key, null);
                value = Float.parseFloat(strValue);
            }
            LoggerUtils.d("Preferences get->key:" + key + ", value:" + value);
            return value;
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static boolean remove(String key) {
        try {
            if (sharedPreferences == null) {
                LoggerUtils.e("ParamsUtils isn't initialized!");
                return false;
            }
            if (key != null && !"".equals(key)) {
                LoggerUtils.i("Preferences remove->key:" + key);
                return sharedPreferences.edit().remove(key).commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
