package acquire.sdk;

import android.bluetooth.BluetoothDevice;
import android.content.Context;

import androidx.annotation.NonNull;

import com.newland.nsdk.core.api.common.exception.NSDKException;
import com.newland.nsdk.core.api.common.serialport.BaudRate;
import com.newland.nsdk.core.api.common.serialport.DataBits;
import com.newland.nsdk.core.api.common.serialport.ParityBit;
import com.newland.nsdk.core.api.common.serialport.StopBits;
import com.newland.nsdk.core.api.common.uart3.UART3Config;
import com.newland.nsdk.core.api.common.uart3.UART3Type;
import com.newland.nsdk.core.api.common.utils.LogLevel;
import com.newland.nsdk.core.api.external.ExtNSDKModuleManager;
import com.newland.nsdk.core.api.external.communication.CommunicatorListener;
import com.newland.nsdk.core.api.external.communication.ExternalCommunicatorState;
import com.newland.nsdk.core.api.external.communication.ExternalCommunicatorType;
import com.newland.nsdk.core.api.external.communication.NSDKCommunicator;
import com.newland.nsdk.core.external.ExtNSDKModuleManagerImpl;
import com.newland.nsdk.core.external.command.communication.ExternalCommunicationManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acquire.base.utils.LoggerUtils;
import acquire.sdk.device.BDevice;
import acquire.sdk.dock.DockSerialPort;
import acquire.sdk.dock.DockUsbPort;

/**
 * External PIN pad Service Executor.It is commonly used in the cash register.
 * <p><hr><b>e.g.</b></p>
 * <pre>
 *     //connect and initialize the external PIN pad.(USB/Serial)
 *     boolean result = ExtServiceHelper.getInstance().init(context, isUsb);
 *     //note: it is a blocking process。 So it is recommended to execute in a thread.
 * </pre>
 *
 * @author Janson
 * @date 2021/11/23 14:30
 */
public class ExtServiceHelper {
    /**
     * singleton instance
     */
    private static volatile ExtServiceHelper instance;

    private ExtNSDKModuleManager extNsdkModuleManager;
    private NSDKCommunicator communicator;

    private int lastConnectMode;

    private ExtServiceHelper() {
    }

    public static ExtServiceHelper getInstance() {
        if (instance == null) {
            synchronized (ExtServiceHelper.class) {
                if (instance == null) {
                    instance = new ExtServiceHelper();
                }
            }
        }
        return instance;
    }

    public boolean init(@NonNull Context context, @ConnectMode.ConnectModeDef int connectMode) {
        if (!isInit() || connectMode != lastConnectMode) {
            LoggerUtils.i("[NSDK ExtServiceHelper]--Start to connect external Pinpad");
            extNsdkModuleManager = ExtNSDKModuleManagerImpl.getInstance();
            extNsdkModuleManager.setDebugMode(LogLevel.DEBUG);
            boolean result = connectExternal(context.getApplicationContext(), 0, extNsdkModuleManager, connectMode);
            if (!result) {
                extNsdkModuleManager = null;
            }
            lastConnectMode = connectMode;
            return result;
        }
        return true;
    }

    public void notifyConnect(@ConnectMode.ConnectModeDef int connectMode) {
        if (connectMode != lastConnectMode) {
            destroy();
        }
    }


    /**
     * Connect external PIN pad
     */
    private boolean connectExternal(Context context, int index, ExtNSDKModuleManager moduleManager, @ConnectMode.ConnectModeDef int connectMode) {
        if (communicator != null && communicator.isConnected()) {
            try {
                //close time out 2000ms
                communicator.close(2000);
            } catch (NSDKException e) {
                LoggerUtils.e("ExtServiceHelper communicator.close failed!",e);
            }
        }
        List<BaudRate> baudRates = new ArrayList<>();
        baudRates.add(BaudRate.BPS115200);
        baudRates.add(BaudRate.BPS57600);
        DockUsbPort dockUsbPort;
        switch (connectMode) {
            case ConnectMode.DOCK_USB1:
                //DOCK USB1
                LoggerUtils.d("[NSDK ExtServiceHelper]--Set dock Pinpad usb1");
                dockUsbPort = new DockUsbPort(1);
                ExternalCommunicationManager.getInstance().setSendTimeout(1000);
                ExternalCommunicationManager.getInstance().setReceiveTimeout(1000);
                communicator = new DockNSDKCommunicator(dockUsbPort);
                moduleManager.setCommunicator(communicator);
                boolean ret2= openAndPing(moduleManager);
                ExternalCommunicationManager.getInstance().setSendTimeout(10*1000);
                ExternalCommunicationManager.getInstance().setReceiveTimeout(10*1000);
                return  ret2;
            case ConnectMode.DOCK_USB2:
                //DOCK USB2
                LoggerUtils.d("[NSDK ExtServiceHelper]--Set dock Pinpad usb1");
                dockUsbPort = new DockUsbPort(2);
                ExternalCommunicationManager.getInstance().setSendTimeout(1000);
                ExternalCommunicationManager.getInstance().setReceiveTimeout(1000);
                communicator = new DockNSDKCommunicator(dockUsbPort);
                moduleManager.setCommunicator(communicator);
                boolean ret= openAndPing(moduleManager);
                ExternalCommunicationManager.getInstance().setSendTimeout(10*1000);
                ExternalCommunicationManager.getInstance().setReceiveTimeout(10*1000);
                return  ret;
            case ConnectMode.DOCK_SERIAL_PORT:
                //DOCK serial port
                if (index >= baudRates.size()) {
                    return false;
                }
                int baudrate = baudRates.get(index).toValue();
                LoggerUtils.d("[NSDK ExtServiceHelper]--Set dock Pinpad serial port (" + baudrate + ").");
                ExternalCommunicationManager.getInstance().setSendTimeout(1000);
                ExternalCommunicationManager.getInstance().setReceiveTimeout(1000);
                communicator = new DockNSDKCommunicator(new DockSerialPort(), baudrate);
                moduleManager.setCommunicator(communicator);
                if (!openAndPing(moduleManager)) {
                    //connect again
                    LoggerUtils.e("re-connect!!");
                    return connectExternal(context, index + 1, moduleManager, connectMode);
                }
                ExternalCommunicationManager.getInstance().setSendTimeout(10*1000);
                ExternalCommunicationManager.getInstance().setReceiveTimeout(10*1000);
                return true;
            case ConnectMode.USB:
                //USB
                LoggerUtils.d("[NSDK ExtServiceHelper]--Set Pinpad usb");
                try {
                    communicator = moduleManager.getNSDKCommunicator(context, ExternalCommunicatorType.USB, communicatorListenerImpl);
                } catch (NSDKException e) {
                    LoggerUtils.e("ExtServiceHelper ConnectMode.USB failed!",e);
                    communicator = null;
                    return false;
                }
                return openAndPing(moduleManager);
            case ConnectMode.SERIAL_PORT:
                //Serial Port
                if (index >= baudRates.size()) {
                    return false;
                }
                try {
                    communicator = moduleManager.getNSDKCommunicator(context, ExternalCommunicatorType.UART3PORT, communicatorListenerImpl);
                    UART3Type uart3Type;
                    if (BDevice.isCpos()) {
                        uart3Type = UART3Type.PINPAD_CPOS;
                    }else {
                        uart3Type = UART3Type.PINPAD_A7;
                    }
                    BaudRate baudRate = baudRates.get(index);
                    LoggerUtils.d("[NSDK ExtServiceHelper]--Set Pinpad serial port " + uart3Type + "(" + baudRate.toValue() + ").");
                    UART3Config config = new UART3Config(baudRate, DataBits.DATA_BIT_8, ParityBit.NO_CHECK, StopBits.STOP_BIT_ONE);
                    moduleManager.setUART3Config(uart3Type, config);
                    if (!openAndPing(moduleManager)) {
                        //connect again
                        LoggerUtils.e("re-connect!!");
                        return connectExternal(context, index + 1, moduleManager, connectMode);
                    }
                    return true;
                } catch (NSDKException e) {
                    LoggerUtils.e("ExtServiceHelper ConnectMode.SERIAL_PORT failed!",e);
                    communicator = null;
                    return false;
                }
            default:
                LoggerUtils.e("[NSDK ExtServiceHelper]--Set Pinpad error");
                return false;
        }
    }

    private boolean openAndPing(ExtNSDKModuleManager moduleManager) {
        try {
            communicator.open(2000);
            LoggerUtils.i("[NSDK ExtServiceHelper]--start to Ping.");
            if (moduleManager.ping()) {
                LoggerUtils.i("[NSDK ExtServiceHelper]--Ping external Pinpad success.");
                //init external PIN pad
                LoggerUtils.i("[NSDK ExtServiceHelper]--External NSDK init start.");
                moduleManager.initExternalModules();
                LoggerUtils.i("[NSDK ExtServiceHelper]--External NSDK init over.");
                return true;
            }
            LoggerUtils.e("[NSDK ExtServiceHelper]--Ping external Pinpad failed");
            communicator.close(2000);
        } catch (NSDKException e) {
            LoggerUtils.e("[NSDK ExtServiceHelper]--External Pinpad failed.", e);
        }
        communicator = null;
        return false;
    }

    private final CommunicatorListener communicatorListenerImpl = new CommunicatorListener() {
        @Override
        public BluetoothDevice onBluetoothList(ArrayList<BluetoothDevice> devices) {
            return null;
        }

        @Override
        public void onConnectedStateChange(ExternalCommunicatorState state) {
        }
    };

    private static class DockNSDKCommunicator implements NSDKCommunicator {
        private DockUsbPort dockUsbPort;
        private DockSerialPort dockSerialPort;
        private int baudRate;
        private byte[] lastErrorSendData;

        public DockNSDKCommunicator(DockUsbPort dockUsbPort) {
            this.dockUsbPort = dockUsbPort;
        }

        public DockNSDKCommunicator(DockSerialPort dockSerialPort, int baudRate) {
            this.dockSerialPort = dockSerialPort;
            this.baudRate = baudRate;
        }


        @Override
        public void send(byte[] data, int timeout) {
            if (dockUsbPort != null) {
                if (!dockUsbPort.write(data)) {
                    if (!Arrays.equals(lastErrorSendData, data)) {
                        lastErrorSendData = data;
                        dockUsbPort.open();
                        dockUsbPort.write(data);
                    }
                }
            } else {
                dockSerialPort.write(data);
            }
        }

        @Override
        public byte[] receive(int timeout) {
            if (dockUsbPort != null) {
                int length = dockUsbPort.getCacheLength();
                if (length <= 0) {
                    return null;
                }
                return dockUsbPort.read(length, 0);
            } else {
                int length = dockSerialPort.getCacheLength();
                if (length <= 0) {
                    return null;
                }
                return dockSerialPort.read(length, 0);
            }
        }

        @Override
        public void setCommunicationTimeout(int sendTimeout, int receiveTimeout) {
        }

        @Override
        public void clear() throws NSDKException {
            if (dockUsbPort != null) {
                dockUsbPort.flush();
            } else {
                dockSerialPort.flush();
            }
        }

        @Override
        public void open(int timeout) throws NSDKException {
            if (dockUsbPort != null) {
                boolean result = dockUsbPort.open();
                if (!result) {
                    throw new NSDKException("open USB failed");
                }
            } else {
                boolean result = dockSerialPort.open(baudRate);
                if (!result) {
                    throw new NSDKException("open Serial failed");
                }
            }

        }

        @Override
        public void close(int timeout) {
            if (dockUsbPort != null) {
                dockUsbPort.close();
            } else {
                dockSerialPort.close();
            }
        }

        @Override
        public boolean isConnected() {
            if (dockUsbPort != null) {
                return dockUsbPort.isConnected();
            } else {
                return dockSerialPort.isConnected();
            }
        }
    }

    /**
     * Return true if External NSDK module is initialized.
     *
     * @return true if extNSDKModuleManager isn't null.
     */
    public boolean isInit() {
        return extNsdkModuleManager != null && communicator != null;
    }

    /**
     * unbind service
     */
    public void destroy() {
        if (isInit()) {
            if (communicator != null && communicator.isConnected()) {
                try {
                    communicator.close(2000);
                } catch (NSDKException e) {
                    LoggerUtils.e("ExtServiceHelper communicator.close failed!",e);
                } finally {
                    communicator = null;
                }
            }
            extNsdkModuleManager.destroy();
            extNsdkModuleManager = null;
        } else {
            LoggerUtils.e("[NSDK ExtServiceHelper]--External Pinpad isn't initialized,so doesn't execute method[destroy]!");
        }
    }


}
