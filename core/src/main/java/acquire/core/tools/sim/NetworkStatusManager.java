package acquire.core.tools.sim;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.telephony.TelephonyManager;

public class NetworkStatusManager {

    private static volatile NetworkStatusManager instance;

    private NetworkStatusManager() {
    }

    public static NetworkStatusManager getInstance() {
        if (instance == null) {
            synchronized (NetworkStatusManager.class) {
                if (instance == null) {
                    instance = new NetworkStatusManager();
                }
            }
        }
        return instance;
    }

    /**
     * SIM inserted and ready
     */
    public boolean isSimReady(Context context) {
        try {
            TelephonyManager telephonyManager =
                    (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

            return telephonyManager != null
                    && telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Cellular Network Connected
     */
    public boolean isCellularConnected(Context context) {
        try {
            ConnectivityManager connectivityManager =
                    (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (connectivityManager == null) {
                return false;
            }

            Network network = connectivityManager.getActiveNetwork();

            if (network == null) {
                return false;
            }

            NetworkCapabilities capabilities =
                    connectivityManager.getNetworkCapabilities(network);

            return capabilities != null
                    && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                    && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * SIM + Mobile Data Available
     */
    public boolean isSimDataAvailable(Context context) {
        return isSimReady(context) && isCellularConnected(context);
    }

}