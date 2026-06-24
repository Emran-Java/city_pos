package acquire.sdk.device;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.external.devicemanager.ExtDeviceManager;
import com.newland.nsdk.core.external.ExtNSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;

/**
 * External Device information
 *
 * @author Janson
 * @date 2021/12/14 10:34
 */
public class BExtDevice {

    /**
     * Return external PIN pad version.
     */
    public static String getVersion() {
        try {
            ExtDeviceManager deviceManager = (ExtDeviceManager) ExtNSDKModuleManagerImpl.getInstance().getModule(ModuleType.EXT_DEVICE_MANAGER);
            return deviceManager.getVersionNumber();
        } catch (NSDKException e) {
            LoggerUtils.e("ExtDeviceManager getVersionNumber failed!",e);
            return null;
        }
    }


    /**
     * Return external PIN pad serial number.
     **/
    public static String getSn() {
        try {
            ExtDeviceManager deviceManager = (ExtDeviceManager) ExtNSDKModuleManagerImpl.getInstance().getModule(ModuleType.EXT_DEVICE_MANAGER);
            return deviceManager.getSerialNumber();
        } catch (NSDKException e) {
            LoggerUtils.e("ExtDeviceManager getSerialNumber failed!",e);
            return null;
        }
    }

    /**
     * Return external PIN pad battery percentage.
     * <p>0: Charging</p>
     * <p>1 - 100: Battery percentage</p>
     **/
    public static int getBatteryPercentage() {
        try {
            ExtDeviceManager deviceManager = (ExtDeviceManager) ExtNSDKModuleManagerImpl.getInstance().getModule(ModuleType.EXT_DEVICE_MANAGER);
            return deviceManager.getBatteryPercentage();
        } catch (NSDKException e) {
            LoggerUtils.e("ExtDeviceManager getBatteryPercentage failed!",e);
            return -1;
        }
    }


    /**
     * Return external PIN pad baud rate mode.
     */
    public static String getBaudRateMode() {
        try {
            ExtDeviceManager deviceManager = (ExtDeviceManager) ExtNSDKModuleManagerImpl.getInstance().getModule(ModuleType.EXT_DEVICE_MANAGER);
            return deviceManager.getDeviceConfiguration().getBaudRateMode().toString();
        } catch (NSDKException e) {
            LoggerUtils.e("ExtDeviceManager getBaudRateMode failed!",e);
            return null;
        }
    }


}
