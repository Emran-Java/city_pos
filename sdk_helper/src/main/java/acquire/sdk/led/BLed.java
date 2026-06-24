package acquire.sdk.led;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.led.LEDColor;
import com.newland.nsdk.core.api.common.led.LEDState;
import com.newland.nsdk.core.api.internal.led.LED;
import com.newland.nsdk.core.api.internal.led.LEDLight;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.CommonPoolExecutor;
import acquire.base.utils.thread.ThreadPool;
import acquire.sdk.device.BDevice;

/**
 * Led
 *
 * @author Janson
 * @date 2021/6/10 16:29
 */
public class BLed {
    private final static Executor LED_EXECUTOR = CommonPoolExecutor.newSinglePool("LED");
    
    /**
     * set the light. true to turn on, false to no change.
     *
     * @param blue   Blue light.
     * @param yellow Yellow light.
     * @param green  Green light.
     * @param red    Red light.
     * @param state  light state
     * @return true on success, false on failure.
     */
    private static boolean setLight(final boolean blue, final boolean yellow, final boolean green, final boolean red, LEDState state) {
        List<LEDColor> list = new ArrayList<>();
        if (blue) {
            list.add(LEDColor.BLUE);
        }
        if (yellow) {
            list.add(LEDColor.YELLOW);
        }
        if (green) {
            list.add(LEDColor.GREEN);
        }
        if (red) {
            list.add(LEDColor.RED);
        }
        if (!list.isEmpty()) {
            LED led = (LED) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.LED);
            try {
                led.setState(list.toArray(new LEDColor[0]), state);
            } catch (NSDKException e) {
                LoggerUtils.e("led.setState failed!",e);
                return false;
            }
        }
        return true;
    }

    /**
     * Turn on the light. true to turn on, false to no change.
     *
     * @param blue   Blue light.
     * @param yellow Yellow light.
     * @param green  Green light.
     * @param red    Red light.
     */
    public static void lightTurnOn(final boolean blue, final boolean yellow, final boolean green, final boolean red) {
        LED_EXECUTOR.execute(() -> {
            if (!setLight(blue, yellow, green, red, LEDState.ON)) {
                LoggerUtils.e("[NSDK Led]--open led failed.");
            }
        });
    }

    /**
     * Turn off the lights. true to turn off, false to no change.
     *
     * @param blue   Blue light.
     * @param yellow Yellow light.
     * @param green  Green light.
     * @param red    Red light.
     */
    public static void lightTurnOff(final boolean blue, final boolean yellow, final boolean green, final boolean red) {
        LED_EXECUTOR.execute(() -> {
            if (!setLight(blue, yellow, green, red, LEDState.OFF)) {
                LoggerUtils.e("[NSDK Led]--close led failed.");
            }
        });
    }


    /**
     * Blink. true to blink, false to no change.
     *
     * @param blue   Blue light.
     * @param yellow Yellow light.
     * @param green  Green light.
     * @param red    Red light.
     */
    public static void lightBlink(final boolean blue, final boolean yellow, final boolean green, final boolean red) {
        LED_EXECUTOR.execute(() -> {
            if (!setLight(blue, yellow, green, red, LEDState.BLINK)) {
                LoggerUtils.e("[NSDK Led]--blink led failed.");
            }
        });
    }

    /**
     * Set card reader light，currently only P300 supports.
     * <P>Support to display multiple colors in one light at a time
     *
     * @param leftTop     left top light of card reader
     * @param rightTop    right top light of card reader
     * @param leftBottom  left bottom light of card reader
     * @param rightBottom right bottom light of card reader
     * @param center      center light of card reader
     * @param state       light state
     * @return true on success, false on failure.
     */
    private static boolean setCardLight(final LEDColor[] leftTop, final LEDColor[] rightTop, final LEDColor[] leftBottom, final LEDColor[] rightBottom, final LEDColor[] center, LEDState state) {
        if (!BDevice.supportTapLed()) {
            return true;
        }
        List<LEDLight> lights = new ArrayList<>();
        for (LEDColor ledColor : leftTop) {
            lights.add(new LEDLight(1, ledColor, state));
        }
        for (LEDColor ledColor : rightTop) {
            lights.add(new LEDLight(2, ledColor, state));
        }
        for (LEDColor ledColor : leftBottom) {
            lights.add(new LEDLight(3, ledColor, state));
        }
        for (LEDColor ledColor : rightBottom) {
            lights.add(new LEDLight(4, ledColor, state));
        }
        for (LEDColor ledColor : center) {
            lights.add(new LEDLight(5, ledColor, state));
        }
        if (!lights.isEmpty()) {
            LED led = (LED) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.LED);
            try {
                led.setState(lights.toArray(new LEDLight[0]));
            } catch (NSDKException e) {
                LoggerUtils.e("led.setState failed!",e);
                return false;
            }
        }
        return true;
    }

    public static void cardLightTurnOn() {
        ThreadPool.execute(() -> {
            if (!setCardLight(new LEDColor[]{LEDColor.RED,LEDColor.BLUE,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.RED,LEDColor.BLUE,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.RED,LEDColor.BLUE,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.RED,LEDColor.BLUE,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.RED,LEDColor.BLUE,LEDColor.GREEN,LEDColor.YELLOW}
                    , LEDState.ON)) {
                LoggerUtils.e("[NSDK Led]--open card reader led failed.");
            }
        });
    }

    public static void cardLightTurnOff() {
        ThreadPool.execute(() -> {
            if (!setCardLight(new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , LEDState.OFF)) {
                LoggerUtils.e("[NSDK Led]--close card reader led failed.");
            }
        });
    }

    public static void cardLightBlink() {
        ThreadPool.execute(() -> {
            if (!setCardLight(new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , new LEDColor[]{LEDColor.BLUE,LEDColor.RED,LEDColor.GREEN,LEDColor.YELLOW}
                    , LEDState.BLINK)) {
                LoggerUtils.e("[NSDK Led]--blink card reader led failed.");
            }
        });
    }
} 
