package acquire.database.livedata;

import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import acquire.base.utils.ParamsUtils;


/**
 * LiveData for the {@link  acquire.base.utils.ParamsUtils}
 *
 * @author jians
 * @date 2024/9/15 21:02
 */
public class SharedPreferencesLiveData<T> extends LiveData<T> {
    private final static Map<String, List<SharedPreferences.OnSharedPreferenceChangeListener>> WAREHOUSE = new ConcurrentHashMap<>();
    private final static Map<String, SharedPreferences.OnSharedPreferenceChangeListener> WAREHOUSE_FACADE = new ConcurrentHashMap<>();

    private final String key;
    private final T defaultValue;
    private final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String changeKey) {
            if (key.equals(changeKey)) {
                setValue(getValueFromPreferences(key, defaultValue));
            }
        }
    };
    public SharedPreferencesLiveData(String key, @NonNull T defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
        setValue(getValueFromPreferences(key, defaultValue));

    }

    @Override
    protected void onActive() {
        List<SharedPreferences.OnSharedPreferenceChangeListener> storeList = WAREHOUSE.get(key);
        if (storeList == null){
            storeList = new ArrayList<>();
            WAREHOUSE.put(key,storeList);
            storeList.add(listener);
            if (!WAREHOUSE_FACADE.containsKey(key)){
                WAREHOUSE_FACADE.put(key, new FacadeListener());
                ParamsUtils.registerOnSharedPreferenceChangeListener(WAREHOUSE_FACADE.get(key));
            }
        }else{
            if (!storeList.contains(listener)){
                storeList.add(listener);
            }
        }
    }

    private static class FacadeListener implements SharedPreferences.OnSharedPreferenceChangeListener{

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
            List<SharedPreferences.OnSharedPreferenceChangeListener> listeners = WAREHOUSE.get(key);
            if (listeners == null){
                return;
            }
            SharedPreferences.OnSharedPreferenceChangeListener [] listenerArray = listeners.toArray(new SharedPreferences.OnSharedPreferenceChangeListener[0]);
            for (SharedPreferences.OnSharedPreferenceChangeListener value : listenerArray) {
                value.onSharedPreferenceChanged(sharedPreferences,key);
            }
        }
    }

    @Override
    protected void onInactive() {
        List<SharedPreferences.OnSharedPreferenceChangeListener> storeList = WAREHOUSE.get(key);
        if (storeList != null ){
            storeList.remove(listener);
        }
        if (storeList == null || storeList.isEmpty()){
            WAREHOUSE.remove(key);
            SharedPreferences.OnSharedPreferenceChangeListener facadeListener = WAREHOUSE_FACADE.remove(key);
            if (facadeListener != null){
                ParamsUtils.unregisterOnSharedPreferenceChangeListener(facadeListener);
            }
        }
    }

    private T getValueFromPreferences(String key, T defaultValue) {
        if (defaultValue instanceof Boolean) {
            return (T) Boolean.valueOf(ParamsUtils.getBoolean(key, (Boolean) defaultValue));
        } else if (defaultValue instanceof String) {
            return (T) ParamsUtils.getString(key, (String) defaultValue);
        } else if (defaultValue instanceof Integer) {
            return (T) Integer.valueOf(ParamsUtils.getInt(key, (Integer) defaultValue));
        } else if (defaultValue instanceof Long) {
            return (T) Long.valueOf(ParamsUtils.getLong(key, (Long) defaultValue));
        } else if (defaultValue instanceof Float) {
            return (T) Float.valueOf(ParamsUtils.getFloat(key, (Long) defaultValue));
        } else {
            throw new IllegalArgumentException("Unsupported type");
        }
    }
    @Override
    public void postValue(T value) {
        super.postValue(value);
    }

    @Override
    public void setValue(T value) {
        super.setValue(value);
    }
}
