package acquire.core.tools.sim;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;

import androidx.core.app.ActivityCompat;

import java.util.List;

public class SimUtility {
    public static String getICCID(Context context) {
        // Requires Manifest.permission.READ_PHONE_STATE
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            return "";
        }

        SubscriptionManager subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        List<SubscriptionInfo> subscriptionInfoList = subscriptionManager.getActiveSubscriptionInfoList();

        if (subscriptionInfoList != null && !subscriptionInfoList.isEmpty()) {
            SubscriptionInfo subscriptionInfo = subscriptionInfoList.get(0);
            // Get the ICCID (SIM Serial Number)
            return subscriptionInfo.getIccId();
        }

        return "";
    }
}
