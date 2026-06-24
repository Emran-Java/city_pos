package acquire.sdk.device;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.internal.devicemanager.DeviceInfo;
import com.newland.nsdk.core.api.internal.devicemanager.DeviceManager;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import java.util.Date;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.sdk.device.constant.Model;
import acquire.sdk.device.constant.SubScreenType;
import acquire.sdk.hce.BCardEmulator;

/**
 * Device information
 *
 * @author Janson
 * @date 2018/3/1
 */
public class BDevice {

    /**
     * set POS system time
     * <p><hr><b>e.g.</b></p>
     * <pre>
     *        Calendar calendar = Calendar.getInstance();
     *        //change time:
     *        //calendar.set(xxx);
     *        Date date = calendar.getTime();
     *        BDevice.setSystemTime(data);
     * </pre>
     *
     * @param date date instant.
     */
    public static void setSystemTime(Date date) {
        try {
            DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
            deviceManager.setPOSDate(date);
        } catch (NSDKException e) {
            e.printStackTrace();
        }
    }

    /**
     * Return true if this device has security module.
     **/
    public static boolean isExistSecurityModule() {
        DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
        return deviceManager.isExistSecurityModule();
    }

    /**
     * Get sdk version
     *
     * @return sdk version
     **/
    public static String getSdkVersion() {
        DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
        return deviceManager.getSDKVersion();
    }


    /**
     * Get the device information
     */
    private static DeviceInfo getDeviceInfo()  {
        DeviceManager deviceManager = (DeviceManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.DEVICE_MANAGER);
        if (deviceManager == null) {
            return null;
        }
        try {
            return deviceManager.getDeviceInfo();
        } catch (NSDKException e) {
            LoggerUtils.e("get device info failed!",e);
            return null;
        }
    }

    /**
     * Get the sequence number
     *
     * @return device sequence number
     */
    public static String getSn() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return null;
        }
        return deviceInfo.getSN();
    }


    /**
     * Get the manufacturer serial number
     *
     * @return manufacturer serial number .
     */
    public static String getCsn() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return null;
        }
        return deviceInfo.getCustomerID();
    }


    /**
     * Get the device model.
     *
     * @return The device model
     */
    public static String getDeviceModel() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return null;
        }
        return deviceInfo.getDeviceModel();
    }

    /**
     * device is CPOS (cash register)
     */
    public static boolean isCpos() {
        String model = getDeviceModel();
        if (model == null) {
            return false;
        } else {
            return model.contains("CPOS");
        }
    }


    /**
     * Get the firmware version number
     *
     * @return Firmware version
     */
    public static String getFirmwareVersion() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return null;
        }
        return deviceInfo.getFirmwareVer();
    }


    /**
     * Return true if this device supports printing.
     */
    public static boolean supportPrint() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isSupportPrint();
    }


    /**
     * Return true if this device supports inserting card.
     */
    public static boolean supportInsert() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isSupportICCard();
    }


    /**
     * Return true if this device supports rfid card.
     */
    public static boolean supportRf() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isSupportQuickPass();
    }


    /**
     * Return true if this device supports mag card.
     */
    public static boolean supportMag() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isSupportMagCard();
    }


    /**
     * Return true if this device supports external scanner by USB.
     */
    public static boolean supportUsbHost() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isSupportUSB();
    }

    /**
     * Return true if this device supports external PIN pad
     */
    public static boolean supportExternalPinPad() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isSupportPinpadPort();
    }

    /**
     * Return true if this device supports RS232 serial port
     */
    public static boolean supportExternalRs232() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isSupport232Port();
    }

    public static int getLedConfig() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return 0;
        }
        return deviceInfo.getLEDConfig();
    }

    /**
     * Return true if this device supports hard scanner
     */
    public static boolean supportHardScanner() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.getScannerConfig().supportHardScanning();
    }

    /**
     * Return true if this device has physical keyboard
     */
    public static boolean supportPhysicalKeyboard() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return false;
        }
        return deviceInfo.isPhysicalKeyboard();
    }

    public static boolean supportTapLed() {
        String model = getDeviceModel();
        return Model.P300.equals(model);
    }

    public static boolean supportHCE(){
        return BCardEmulator.getInstance(BaseApplication.getAppContext()).init();
    }

    /**
     * @return a sub screen type, see {@link SubScreenType}
     */
    public static int getSubScreenType() {
        DeviceInfo deviceInfo = getDeviceInfo();
        if (deviceInfo == null) {
            return SubScreenType.NONE;
        }
        int type = deviceInfo.isSupportSubScreen();
        switch (type){
            case 0x01:
                return SubScreenType.CLICKABLE;
            case 0x02:
                return SubScreenType.VIEW_ONLY;
            default:
                return SubScreenType.NONE;
        }
    }
}
