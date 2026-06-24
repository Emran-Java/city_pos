package acquire.sdk.sound;

import androidx.annotation.IntRange;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.beeper.Beeper;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;

/**
 * POS beeper
 *
 * @author Janson
 * @date 2021/6/10 16:30
 */
public class BBeeper {

    /**
     * beeps.
     * <p>If it's 960K or 950K,frequency 750 is 2500,frequency 1500 is 2730. </p>
     */
    public static void beep() {
        beep(750, 200);
    }
    /**
     * beeps
     *
     * @param frequency Set beep frequency in Hz
     * @param duration  Set how long to beep in ms.
     */
    public static void beep(@IntRange(from = 1, to = 4000) int frequency, @IntRange(from = 1) int duration) {
        try {
            Beeper beeper = (Beeper) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.BEEPER);
            beeper.beep(frequency, duration);
        } catch (NSDKException e) {
            LoggerUtils.e("Beeper beep failed![frequency = "+frequency+"]",e);
        }
    }

} 
