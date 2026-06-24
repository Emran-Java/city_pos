package acquire.sdk.serial;

import androidx.annotation.IntRange;

import com.newland.nsdk.core.api.common.ModuleType;
import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.serialport.BaudRate;
import com.newland.nsdk.core.api.common.serialport.DataBits;
import com.newland.nsdk.core.api.common.serialport.ParityBit;
import com.newland.nsdk.core.api.common.serialport.SerialPortSettings;
import com.newland.nsdk.core.api.common.serialport.StopBits;
import com.newland.nsdk.core.api.internal.serialportmanager.SerialPort;
import com.newland.nsdk.core.api.internal.serialportmanager.SerialPortManager;
import com.newland.nsdk.core.api.internal.serialportmanager.SerialPortType;
import com.newland.nsdk.core.internal.NSDKModuleManagerImpl;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.device.BDevice;
import acquire.sdk.device.constant.Model;

/**
 * A serial port communication tool.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *   //Cash register's serial port is different from portable POS.
 *   BSerialPort port = new BSerialPort();
 *   //1. open by baud rate.
 *   boolean isOpen = port.open(baudRate);
 *   //2. read/write data.
 *   byte[] readBytes = port.read(maxLen,100);
 *   port.write(data,maxLen,100);
 *   //3. finish
 *   port.close();
 * </pre>
 *
 * @author Janson
 * @date 2021/11/15 10:14
 */
public class BPort {
    private SerialPort serialPort;


    private static BaudRate convertBaudRate(int baudRate){
        BaudRate rate = BaudRate.BPS115200;
        for (BaudRate value : BaudRate.values()) {
            if (value.toValue() == baudRate) {
                rate = value;
                break;
            }
        }
        return rate;
    }

    /**
     * @param baudRate port baud rate. e.g. 9600、115200
     *
     * Construct a communication tool.
     */
    public static BPort newRs232(final int baudRate) {
        BPort port = new BPort();
        SerialPortManager serialPortManager = (SerialPortManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SERIAL_PORT_MANAGER);
        BaudRate rate = convertBaudRate(baudRate);
        try {
            SerialPortType serialPortType;
            if (Model.U2000.equals(BDevice.getDeviceModel())){
                serialPortType = SerialPortType.RS232B;
            }else if (Model.N950S_C.equals(BDevice.getDeviceModel())){
                serialPortType = SerialPortType.PINPAD;
            }else {
                serialPortType = SerialPortType.RS232;
            }
            port.serialPort = serialPortManager.createInstance(serialPortType, new SerialPortSettings(rate, DataBits.DATA_BIT_8, ParityBit.NO_CHECK, StopBits.STOP_BIT_ONE, false));
            return port;
        } catch (NSDKException e) {
            LoggerUtils.e("newRs232 BPort failed!",e);
            return null;
        }
    }

    public static BPort newPinpad(final int baudRate) {
        BPort port = new BPort();
        SerialPortManager serialPortManager = (SerialPortManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SERIAL_PORT_MANAGER);
        BaudRate rate = convertBaudRate(baudRate);
        try {
            SerialPortType serialPortType = SerialPortType.PINPAD;
            port.serialPort = serialPortManager.createInstance(serialPortType, new SerialPortSettings(rate, DataBits.DATA_BIT_8, ParityBit.NO_CHECK, StopBits.STOP_BIT_ONE, false));
            return port;
        } catch (NSDKException e) {
            LoggerUtils.e("newPinpad BPort failed!",e);
            return null;
        }
    }
    public static BPort newUsb() {
        BPort port = new BPort();
        SerialPortManager serialPortManager = (SerialPortManager) NSDKModuleManagerImpl.getInstance().getModule(ModuleType.SERIAL_PORT_MANAGER);
        try {
            SerialPortType serialPortType = SerialPortType.USB;
            port.serialPort = serialPortManager.createInstance(serialPortType, new SerialPortSettings(BaudRate.BPS9600, DataBits.DATA_BIT_8, ParityBit.NO_CHECK, StopBits.STOP_BIT_ONE, false));
            return port;
        } catch (NSDKException e) {
            LoggerUtils.e("newUsb BPort failed!",e);
            return null;
        }
    }

    private BPort() {
    }

    /**
     * Open the serial port
     *
     * @return true: open success; false: open failed
     */
    public boolean open() {
        try {
            serialPort.open();
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("BPort open failed!",e);
            return false;
        }
    }

    /**
     * Read the serial port
     *
     * @param lengthMax    Expect read length
     * @param timeoutMillis Reading time out, unit: ms. If value <= 0, read the existing serial port data immediately without waiting.
     * @return data of reading serial port
     */
    public byte[] read(@IntRange(from = 1,to = 512) int lengthMax, int timeoutMillis) {
        try {
            return serialPort.read(lengthMax, timeoutMillis);
        } catch (NSDKException e) {
            LoggerUtils.e("BPort read failed!",e);
            return null;
        }
    }

    /**
     * Write data
     *
     * @param data         Writes the data buffer corresponding to the source address
     * @return true: write success; false: write failed
     */
    public boolean write(byte[] data) {
        try {
            int timeoutMillis = 0;
            serialPort.write(data, timeoutMillis);
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("BPort write failed!",e);
            return false;
        }
    }

    /**
     * Close the serial port
     *
     * @return true: close success; false: close failed.
     */
    public boolean close() {
        try {
            serialPort.close();
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("BPort close failed!",e);
            return false;
        }
    }
    public boolean flush() {
        try {
            serialPort.flush();
            return true;
        } catch (NSDKException e) {
            LoggerUtils.e("BPort flush failed!",e);
            return false;
        }
    }

}
