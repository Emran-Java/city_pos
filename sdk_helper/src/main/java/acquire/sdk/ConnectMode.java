package acquire.sdk;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * External device connect mode
 *
 * @author Janson
 * @date 2021/12/9 9:17
 */
public class ConnectMode {
    /**
     * RS232
     */
    public final static int SERIAL_PORT = 0;
    /**
     * USB
     */
    public final static int USB = 1;
    /**
     * Bluetooth
     */
    public final static int BLUETOOTH = 2;
    /**
     * Newland Smart Dock RS232
     */
    public final static int DOCK_SERIAL_PORT = 10;
    /**
     * Newland Smart Dock USB1
     */
    public final static int DOCK_USB1 = 11;
    /**
     * Newland Smart Dock USB2
     */
    public final static int DOCK_USB2 = 12;


    @IntDef(value = {SERIAL_PORT, USB,BLUETOOTH,DOCK_SERIAL_PORT, DOCK_USB1, DOCK_USB2})
    @Target({ElementType.PARAMETER, ElementType.METHOD, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectModeDef {
    }

}
