package acquire.sdk.dock;


import com.newland.nsdk.dock.DockCashBox;
import com.newland.nsdk.dock.DockException;

import acquire.base.utils.LoggerUtils;

/**
 * A cash drawer of the dock.
 *
 * @author Janson
 * @date 2023/4/12 16:17
 */
public class DockCashDrawer extends BaseDock{
    private final DockCashBox cashBox;

    public DockCashDrawer() {
        cashBox = dock.getCashBox();
    }

    /**
     * Opens cash drawer with default voltage(12V) and delay time(500ms).
     */
    public void open() {
        if (init()){
            try {
                cashBox.open();
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
    }

    /**
     * Opens cash drawer with specified voltage and delay time.
     *
     * @param voltage     drawer voltage.0 - 12V 1 - 24V
     * @param delayMillis The delay (in milliseconds) until the drawer opens.
     */
    public void open(int voltage, int delayMillis) {
        if (init()){
            try {
                cashBox.open(voltage, delayMillis);
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
    }
}
