package acquire.base.utils.network;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Network utils
 * <p><hr><b>Features:</b></p>
 * <p>1.get network state</p>
 * <pre>
 *       NetworkCapabilities networkCapabilities = NetworkUtils.getNetworkCapabilities(context)
 *       boolean wifi = NetworkUtils.wifiAvailable(networkCapabilities);
 *       boolean sim = NetworkUtils.simAvailable(networkCapabilities);
 * </pre>
 * <p>2.listen network state</p>
 * <pre>
 *    NetworkUtils.registerNetCallback(context,new ConnectivityManager.NetworkCallback(){
 *           public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities networkCapabilities) {
 *               super.onCapabilitiesChanged(network, networkCapabilities);
 *               //state change
 *               boolean wifi = NetworkUtils.wifiAvailable(networkCapabilities);
 *               boolean sim = NetworkUtils.simAvailable(networkCapabilities);
 *           }
 *    })
 * </pre>
 *
 * @author Janson
 * @date 2022/9/19 11:36
 */
public class NetworkUtils {


    /**
     * get sim name
     *
     * @return return "No Permission","Unknown" SIM is not connected/Other issue.
     *         Else ,return default SIM Name and its network is working.
     */
    public static String getOperatorName(Context context) {

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return "";
        }

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return "Permission not granted";
        }

        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (telephonyManager != null) {
            return telephonyManager.getSimOperatorName();
        }

        return "Unknown";

        /*SubscriptionManager subscriptionManager = (SubscriptionManager) context
                .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);

        List<SubscriptionInfo> activeSubInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubInfoList != null) {
            for (SubscriptionInfo subscriptionInfo : activeSubInfoList) {
                if(subscriptionInfo.getSubscriptionId() == SubscriptionManager.getDefaultDataSubscriptionId()){
                    return subscriptionInfo.getSimSlotIndex();
                }
            }
        }*/

    }


    /**
     * register a callback to listen network state
     */
    public static void registerNetCallback(Context context, ConnectivityManager.NetworkCallback callback) {
        ConnectivityManager networkService = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        networkService.registerDefaultNetworkCallback(callback);
    }

    /**
     * Check whether wifi is available
     */
    public static boolean wifiAvailable(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities == null) {
            return false;
        }
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
    }

    /**
     * Check whether wifi is available
     */
    public static boolean simAvailable(NetworkCapabilities networkCapabilities) {
        if (networkCapabilities == null) {
            return false;
        }
        return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR);
    }

    /**
     * get {@link NetworkCapabilities}
     */
    public static @Nullable NetworkCapabilities getNetworkCapabilities(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return manager.getNetworkCapabilities(manager.getActiveNetwork());
    }

    /**
     * check sim slot data
     *
     * @return return -1, sim is not connected.
     *         Else ,return default sim slot and its network is working.
     */
    public static int getDefaultDataSlotId(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return -1;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) context
                .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> activeSubInfoList = subscriptionManager.getActiveSubscriptionInfoList();
        if (activeSubInfoList != null) {
            for (SubscriptionInfo subscriptionInfo : activeSubInfoList) {
                if(subscriptionInfo.getSubscriptionId() == SubscriptionManager.getDefaultDataSubscriptionId()){
                    return subscriptionInfo.getSimSlotIndex();
                }
            }
        }
        return -1;
    }

    /**
     * set sim data slot
     */
    public static boolean setDefaultDataSub(int slotIndex, Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        SubscriptionManager subscriptionManager = (SubscriptionManager) context
                .getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subInfos = subscriptionManager.getActiveSubscriptionInfoList();
        if (subInfos == null || subInfos.size() < 2) {
            return false;
        }
        SubscriptionInfo mSubscriptionInfo = null;
        for (int i = 0; i < subInfos.size(); i++) {
            if (subInfos.get(i).getSimSlotIndex() != slotIndex) {
                mSubscriptionInfo = subInfos.get(i);
                break;
            }
        }
        if (mSubscriptionInfo == null) {
            return false;
        }
        try {
            Method method = SubscriptionManager.class.getMethod("setDefaultDataSubId", int.class);
            method.invoke(subscriptionManager, mSubscriptionInfo.getSubscriptionId());
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return true;
    }

    /**
     * open/close wifi
     */
    public static void setWifiEnable(Context context, boolean enabled) {
        WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wm.setWifiEnabled(enabled);
    }


} 
