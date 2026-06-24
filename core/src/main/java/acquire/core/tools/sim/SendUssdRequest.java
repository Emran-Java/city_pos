package acquire.core.tools.sim;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.TelephonyManager;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import acquire.base.utils.LoggerUtils;


public class SendUssdRequest {

    private static ListenUssdResponse mListenUssdResponse;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static void sendUssdRequest(Context context, String ussdCode, ListenUssdResponse listenUssdResponse) {

        mListenUssdResponse = listenUssdResponse;
        mListenUssdResponse.hideLoader(false);

        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            LoggerUtils.e("newCall USSD_ERROR:" + "CALL_PHONE permission missing");
            mListenUssdResponse.getUssdPermissionException("CALL_PHONE permission missing");
            mListenUssdResponse.hideLoader(true);
            return;
        }

        if (telephonyManager != null) {
            telephonyManager.sendUssdRequest(ussdCode, new TelephonyManager.UssdResponseCallback() {

                @Override
                public void onReceiveUssdResponse(TelephonyManager telephonyManager, String request, CharSequence response) {
                    super.onReceiveUssdResponse(telephonyManager, request, response);
                    LoggerUtils.d("newCall USSD_SUCCESS" + "Request: " + request + ", Response: " + response);

                    String ussdResult = response.toString();
                    mListenUssdResponse.getUssdResponse(ussdResult);
                    mListenUssdResponse.hideLoader(true);
                }

                @Override
                public void onReceiveUssdResponseFailed(TelephonyManager telephonyManager, String request, int failureCode) {
                    super.onReceiveUssdResponseFailed(telephonyManager, request, failureCode);
                    LoggerUtils.e("newCall USSD_FAILED" + "Request: " + request + ", Failure Code: " + failureCode);
                    mListenUssdResponse.getUssdErrorResponse("Request: " + request + ", Failure Code: " + failureCode);
                    mListenUssdResponse.hideLoader(true);
                }
            }, new Handler(Looper.getMainLooper()));
        }
    }

    public interface ListenUssdResponse {
        public void hideLoader(boolean isHideLoader);
        public void getUssdResponse(String response);

        public void getUssdErrorResponse(String errorResponse);

        public void getUssdPermissionException(String permissionResponse);
    }
}

