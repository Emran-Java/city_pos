package acquire.sdk.scan;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.annotation.IntRange;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.ThreadPool;
import acquire.sdk.R;
import acquire.sdk.dock.DockSerialPort;
import acquire.sdk.dock.DockUsbPort;
import acquire.sdk.serial.BPort;

/**
 * External scan code box
 *
 * @author Janson
 * @date 2020/9/18 15:19
 */
public class BExternScanner {

    private BPort serialComm;
    private boolean isScanning;
    private DockUsbPort dockUsbPort;
    private DockSerialPort dockSerialPort;

    /**
     * Start scanning code (USB peripheral mode connection)
     *
     * @param delayMillis Waiting time for USB receipting completion in millis.
     * @param editText    Edit box for receiving USB data
     * @param callback    scan code results
     */
    public void doScanUsbHost(EditText editText, final int delayMillis, final ScanExternCallback callback) {
        editText.addTextChangedListener(new TextWatcher() {
            volatile boolean isWaiting = false;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(final Editable editable) {
                String editString = editable.toString();
                LoggerUtils.d("[NSDK ExternScanner]--usb host input data: " + editString);
                if (!isWaiting) {
                    isWaiting = true;
                    ThreadPool.postDelayOnMain(() -> {
                        if (TextUtils.isEmpty(editString)) {
                            isWaiting = false;
                            return;
                        }
                        LoggerUtils.d("[NSDK ExternScanner]--usb QR code: " + editString);
                        isWaiting = false;
                        callback.onSuccess(editString);
                    }, delayMillis);
                }
            }
        });
    }

    /**
     * Start scanning code (serial connection)
     *
     * @param baudRate Serial port scanner baudrate
     * @param callback Scanning result callback
     */
    public void doScanSerial(final int baudRate, final ScanExternCallback callback) {
        ThreadPool.execute(() -> {
            synchronized (BExternScanner.class) {
                if (isScanning) {
                    return;
                }
                isScanning = true;
            }
            serialComm = BPort.newRs232(baudRate);
            if (!serialComm.open()){
                isScanning = false;
                callback.onFailed(BaseApplication.getAppString(R.string.sdk_helper_serial_open_failed));
                return;
            }

            byte[] buff;
            // clear port
            while (isScanning) {
                buff = serialComm.read(512, 0);
                if (buff == null || buff.length < 512) {
                    break;
                }
            }
            // read scanner
            while (isScanning) {
                buff = serialComm.read(512, 0);
                if (buff != null && buff.length > 0 && isScanning) {
                    String result = new String(buff);
                    LoggerUtils.d("[NSDK ExternScanner]--read serial scanner data: " + result);
                    serialComm.close();
                    isScanning = false;
                    callback.onSuccess(result);
                    break;
                }
            }
        });
    }

    /**
     * Start scanning code with dock RS232
     *
     * @param baudRate Serial port scanner baudrate
     * @param callback Scanning result callback
     */
    public void doScanDockSerial(final int baudRate,final BExternScanner.ScanExternCallback callback) {
        ThreadPool.execute(() -> {
            synchronized (BExternScanner.class) {
                if (isScanning) {
                    return;
                }
                isScanning = true;
            }
            if (dockSerialPort == null){
                dockSerialPort = new DockSerialPort();
            }
            if (!dockSerialPort.open(baudRate)){
                isScanning = false;
                callback.onFailed(BaseApplication.getAppString(R.string.sdk_helper_serial_open_failed));
                return ;
            }
            // clear port
            dockSerialPort.flush();
            // read scanner
            while (isScanning) {
                int length = dockSerialPort.getCacheLength();
                if (length > 0 && isScanning) {
                    byte[] buff = dockSerialPort.read(length, 0);
                    String result = new String(buff);
                    LoggerUtils.d("[DockScanner]--read serial scanner data: " + result);
                    dockSerialPort.close();
                    isScanning = false;
                    callback.onSuccess(result);
                }
            }
        });
    }
    /**
     * Start scanning code with dock USB
     *
     * @param usbId USB id
     * @param callback Scanning result callback
     */
    public void doScanDockUsb(@IntRange(from = 1,to = 2) final int usbId, final BExternScanner.ScanExternCallback callback) {
        ThreadPool.execute(() -> {
            synchronized (BExternScanner.class) {
                if (isScanning) {
                    return;
                }
                isScanning = true;
            }
            dockUsbPort = new DockUsbPort(usbId);
            if (!dockUsbPort.open()){
                isScanning = false;
                if (usbId == 1){
                    callback.onFailed(BaseApplication.getAppString(R.string.sdk_helper_usb1_open_failed));
                }else{
                    callback.onFailed(BaseApplication.getAppString(R.string.sdk_helper_usb2_open_failed));
                }
                return ;
            }
            // clear port
            dockUsbPort.flush();
            // read scanner
            while (isScanning) {
                int length = dockUsbPort.getCacheLength();
                if (length > 0 && isScanning) {
                    byte[] buff = dockUsbPort.read(1024,0);
                    String result = new String(buff);
                    LoggerUtils.d("[DockScanner]--read usb"+usbId+" scanner data: " + result);
                    dockUsbPort.close();
                    isScanning = false;
                    callback.onSuccess(result);
                }
            }
        });
    }

    /**
     * End and code
     */
    public void close() {
        synchronized (BExternScanner.class) {
            if (!isScanning) {
                return;
            }
            isScanning = false;
        }
        try {
            if (serialComm != null) {
                boolean result = serialComm.close();
                if (result) {
                    LoggerUtils.d("[NSDK ExternScanner]--close serial success.");
                } else {
                    LoggerUtils.e("[NSDK ExternScanner]--close serial failed.");
                }
            }
            if (dockSerialPort != null) {
                boolean result = dockSerialPort.close();
                if (result) {
                    LoggerUtils.d("[DockScanner]--close serial success.");
                } else {
                    LoggerUtils.e("[DockScanner]--close serial failed.");
                }
            }

            if (dockUsbPort != null) {
                dockUsbPort.close();
                LoggerUtils.d("[DockScanner]--close usb success.");
            }
        }catch (Exception e){
            LoggerUtils.e("[NSDK ExternScanner]--close serial failed.",e);
        }
    }

    public interface ScanExternCallback {
        void onSuccess(String result);

        void onFailed(String error);
    }
}
