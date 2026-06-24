package acquire.sdk.cashdrawer;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.cashbox.CashBox;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;

/**
 * Cash Drawer
 *
 * @author Janson
 * @date 2022/10/13 15:56
 * @since 3.6
 */
public class BCashDrawer {
    private final CashBox cashBox;

    public BCashDrawer() {
        cashBox = (CashBox) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.CASH_BOX);
    }

    /**
     * Opens cash drawer with default voltage(12V) and delay time(500ms).
     */
    public void open() {
        try {
            cashBox.open();
        } catch (NSDKException e) {
            LoggerUtils.e("cashBox open failed!",e);
        }
    }

    /**
     * Opens cash drawer with specified voltage and delay time.
     *
     * @param voltage     drawer voltage
     * @param delayMillis The delay (in milliseconds) until the drawer opens.
     */
    public void open(int voltage, long delayMillis) {
        try {
            cashBox.open(voltage, delayMillis);
        } catch (NSDKException e) {
            LoggerUtils.e("cashBox open failed! [voltage ="+voltage+"]",e);
        }
    }
}
