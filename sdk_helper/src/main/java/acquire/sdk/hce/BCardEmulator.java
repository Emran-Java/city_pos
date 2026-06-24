package acquire.sdk.hce;

import android.content.Context;

import com.newland.nsdk.cardemulation.CardDetector;
import com.newland.nsdk.cardemulation.CardDetectorImpl;
import com.newland.nsdk.cardemulation.CardEmulator;
import com.newland.nsdk.cardemulation.CardEmulatorImpl;
import com.newland.nsdk.cardemulation.utils.CardEmulatorState;
import com.newland.nsdk.cardemulation.utils.DebugMode;
import com.newland.nsdk.cardemulation.utils.EmulateCardConfig;
import com.newland.nsdk.cardemulation.utils.EmulateCardFileType;
import com.newland.nsdk.cardemulation.utils.EmulateCardType;
import com.newland.nsdk.cardemulation.utils.MagCardResult;
import com.newland.nsdk.cardemulation.utils.NSDKCardEmulatorException;
import com.newland.nsdk.cardemulation.utils.PollingCardsListener;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.device.BDevice;
import acquire.sdk.led.BLed;

public class BCardEmulator {
    private CardEmulator cardEmulator ;
    private CardDetector cardDetector ;

    private static BCardEmulator instance ;
    public static BCardEmulator getInstance(Context context){
        if (instance == null){
            instance = new BCardEmulator(context);
        }
        return instance ;
    }

    private BCardEmulator(Context context){
        cardEmulator = CardEmulatorImpl.getInstance(context);
        cardDetector = CardDetectorImpl.getInstance();
        init();
    }

    public boolean init(){
        try {
            cardEmulator.init();
            cardEmulator.setDebugMode(DebugMode.ALL_ON);
            CardEmulatorState cardEmulatorState = cardEmulator.getStatus(EmulateCardType.T4T);
            LoggerUtils.d("HCE init: cardEmulatorState="+cardEmulatorState);
            return true ;
        }catch (NSDKCardEmulatorException e){
            LoggerUtils.d("HCE init error:"+e.getErrorMessage());
            return false ;
        }
    }

    public void startHCEForUrl(String url){
        try {
            cardEmulator.init();
            EmulateCardConfig cardConfig = new EmulateCardConfig();
            cardEmulator.writeConfig(EmulateCardType.T4T,cardConfig);
            cardEmulator.writeData(EmulateCardFileType.T4T_NDEF,
                    HCECommon.spliceDataForConfiguration(EmulateCardType.T4T,url,(byte)0x00));
            cardEmulator.start(EmulateCardType.T4T, () -> {
                LoggerUtils.d("HCE onDetect");
            });
        } catch (NSDKCardEmulatorException e) {
            LoggerUtils.d("NSDKCardEmulatorException:"+e.getErrorMessage());
        }
    }


//    ---------------------------------------------------only HCE------------------------------------


    public boolean hasStarted(){
        try {
            CardEmulatorState cardEmulatorState = cardEmulator.getStatus(EmulateCardType.T4T);
            return cardEmulatorState == CardEmulatorState.ACTIVE ;
        } catch (NSDKCardEmulatorException e) {
            LoggerUtils.d("NSDKCardEmulatorException:"+e.getErrorMessage());
            return false ;
        }
    }

    public void startDetectForUrl(String url , DeviceReadListener listener){
        try {
            LoggerUtils.d("HCE writeData:"+url);
            cardEmulator.writeData(EmulateCardFileType.T4T_NDEF,
                    HCECommon.spliceDataForConfiguration(EmulateCardType.T4T,url,(byte)0x00));
            cardEmulator.start(EmulateCardType.T4T, () -> {
                LoggerUtils.d("HCE onDetect");
                listener.onDetected();
            });
            LoggerUtils.d("HCE startDetect success");
        } catch (NSDKCardEmulatorException e) {
            LoggerUtils.d("HCE startDetect error:"+e.getErrorMessage());
            listener.onError(e.getErrorMessage());
        }
    }


    public void stopDetect(){
        if (!hasStarted()){
            return;
        }
        try {
            cardEmulator.stop();
            LoggerUtils.d("HCE: cardEmulator stop");
        } catch (NSDKCardEmulatorException e) {
            LoggerUtils.d("HCE: Stop fail:"+e.getErrorMessage());
        }
    }

    public interface DeviceReadListener {
        /**
         * Called when device reads.
         */
        void onDetected();

        /**
         *  error
         *
         * @param msg error message
         */
        void onError(String msg);
    }




//    ---------------------------------------------------polling hce&card ------------------------------------

    MagCardResult bMagCardResult ;
    private boolean foundCard ;

    public void pollingCards(String url,PollingListener listener){
//        if (!init()) {
//            listener.onError("HCE init error");
//            return;
//        }
        if (BDevice.supportPhysicalKeyboard() && BDevice.supportRf()){
            BLed.cardLightTurnOn();
        }
        foundCard = false ;
        bMagCardResult = null ;

        PollingCardsListener pollingCardsListener = new PollingCardsListener() {
            @Override
            public void onFindMagCard(MagCardResult magCardResult) {
                LoggerUtils.d("HCE pollingCards: onFindMagCard");
                foundCard = true ;
                bMagCardResult = magCardResult ;
//                bReadCardType = new byte[]{0x01};
            }

            @Override
            public void onFindContactlessCard() {
                LoggerUtils.d("HCE pollingCards: onFindContactlessCard");
                foundCard = true ;
//                bReadCardType = new byte[]{0x04};
            }

            @Override
            public void onFindContactCard() {
                LoggerUtils.d("HCE pollingCards: onFindContactCard");
                foundCard = true ;
//                bReadCardType = new byte[]{0x02};
            }

            @Override
            public void onFindHCE() {
                LoggerUtils.d("HCE pollingCards: onFindHCE");
                listener.onFindHCE();
            }

            @Override
            public void onTimeout() {
                LoggerUtils.d("HCE pollingCards: onTimeout");
                listener.onError("Timeout");
            }

            @Override
            public void onCompletePolling() {
                LoggerUtils.d("HCE pollingCards: onCompletePolling");
                if (BDevice.supportPhysicalKeyboard() && BDevice.supportRf()){
                    BLed.cardLightTurnOff();
                }
                if (foundCard){
                    listener.onFindCard(bMagCardResult);
                }
            }
        };

        try {
            LoggerUtils.d("HCE writeData:"+url);
            cardEmulator.writeData(EmulateCardFileType.T4T_NDEF,
                    HCECommon.spliceDataForConfiguration(EmulateCardType.T4T,url,(byte)0x00));
            cardDetector.pollingCards(60, EmulateCardType.T4T, pollingCardsListener);
            LoggerUtils.d("HCE startPolling success");
        } catch (NSDKCardEmulatorException e) {
            LoggerUtils.d("HCE startPolling error:"+e.getErrorMessage());
            listener.onError(e.getErrorMessage());
        }

    }

    public void stopPolling() {
        try {
            if (BDevice.supportPhysicalKeyboard() && BDevice.supportRf()){
                BLed.cardLightTurnOff();
            }
            cardDetector.stopPolling();
            LoggerUtils.d("HCE stopPolling");
        } catch (NSDKCardEmulatorException e) {
            LoggerUtils.d("HCE stopPolling error:"+e.getErrorMessage());
            e.printStackTrace();
        }
    }


    public interface PollingListener {
        void onFindCard(MagCardResult magCardResult);
        void onFindHCE();

        void onError(String msg);
    }
}
