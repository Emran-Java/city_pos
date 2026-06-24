package acquire.core.esc;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;

import acquire.base.BaseApplication;
import acquire.base.utils.BitmapUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.CommonPoolExecutor;
import acquire.core.R;
import acquire.core.bluetooth.BluetoothCore;
import acquire.core.native_usb.NativeUsbCore;
import acquire.sdk.ConnectMode;
import acquire.sdk.dock.DockSerialPort;
import acquire.sdk.dock.DockUsbPort;
import acquire.sdk.printer.IPrinter;
import acquire.sdk.serial.BPort;

/**
 * Esc Printer
 * <pre>
 *     EscPrinter printer = new EscPrinter(ConnectMode.SERIAL_PORT, baudRate);
 *     printer.print(bitmap,new IPrinter.PrintCallback() {
 *             public void onFinish() {
 *             }
 *
 *             public void onError(String message) {
 *             }
 *
 *             public void onOutOfPaper() {
 *             }
 *     })
 * </pre>
 *
 * @author Janson
 * @date 2021/10/9 11:28
 */
public class EscPrinter implements IPrinter {
    public final static Executor ESC_EXECUTOR = CommonPoolExecutor.newSinglePool("EscPrinter");
    private final int baudRate;
    private final @ConnectMode.ConnectModeDef int mode;

    /**
     * Esc printer
     *
     * @param mode connect mode
     */
    public EscPrinter(@ConnectMode.ConnectModeDef int mode) {
        this.mode = mode;
        this.baudRate = 115200;
        LoggerUtils.i("[Esc Printer]--create a ESC printer," + mode + "[115200]");
    }

    /**
     * Esc printer
     *
     * @param mode     connect mode
     * @param baudRate serial port baud rate. Only for serial port.
     */
    public EscPrinter(@ConnectMode.ConnectModeDef int mode, int baudRate) {
        this.mode = mode;
        this.baudRate = baudRate;
        LoggerUtils.i("[Esc Printer]--create a ESC printer," + mode + "[" + baudRate + "]");
    }

    @Override
    public void print(@NonNull Bitmap receipt, boolean endFeed, IPrinter.PrintCallback callback) {
        EscDraw draw = new EscDraw();
        if (endFeed){
            Bitmap tailBitmap = Bitmap.createBitmap(receipt.getWidth(), 50, receipt.getConfig());
            Canvas canvas = new Canvas(tailBitmap);
            canvas.drawColor(Color.WHITE);
            receipt =  BitmapUtils.mergeVertical(receipt, tailBitmap);
        }
        draw.image(receipt);
        byte[] esc = draw.getEscCommand();
        LoggerUtils.i("[Esc Printer]--start to print esc");
        ESC_EXECUTOR.execute(() -> {
            try {
                sendEsc(esc);
                if (callback != null) {
                    callback.onFinish();
                }
            } catch (Exception e) {
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }


        });
    }

    private void sendEsc(byte[] esc) throws Exception {
        boolean result;
        Context context = BaseApplication.getAppContext();
        switch (mode) {
            case ConnectMode.USB:
                NativeUsbCore nativeUsbCore = new NativeUsbCore();
                result = nativeUsbCore.init();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_init_usb_failed));
                }
                result = nativeUsbCore.open();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_open_usb_failed));
                }
                result = nativeUsbCore.write(esc);
                nativeUsbCore.close();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_send_usb_failed));
                }
                break;
            case ConnectMode.SERIAL_PORT:
                BPort port = BPort.newRs232(baudRate);
                result = port.open();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_open_serial_port_failed));
                }
                result = port.write(esc);
                port.close();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_send_serial_port_failed));
                }
                break;
            case ConnectMode.DOCK_USB1:
                DockUsbPort dockUsbPort1 = new DockUsbPort(1);
                result = dockUsbPort1.open();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_open_dock_usb1_failed));
                }
                result = dockUsbPort1.write(esc);
                dockUsbPort1.close();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_send_dock_usb1_failed));
                }
                break;
            case ConnectMode.DOCK_USB2:
                DockUsbPort dockUsbPort2 = new DockUsbPort(2);
                result = dockUsbPort2.open();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_open_dock_usb2_failed));
                }
                result = dockUsbPort2.write(esc);
                dockUsbPort2.close();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_send_dock_usb2_failed));
                }
                break;
            case ConnectMode.DOCK_SERIAL_PORT:
                DockSerialPort dockSerialPort = new DockSerialPort();
                result = dockSerialPort.open(baudRate);
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_open_dock_serial_port_failed));
                }
                result = dockSerialPort.write(esc);
                dockSerialPort.close();
                if (!result) {
                    throw new Exception(context.getString(R.string.core_esc_send_dock_serial_port_failed));
                }
                break;
            case ConnectMode.BLUETOOTH:
                BluetoothCore core = new BluetoothCore();
                core.connectLast();
                result = core.send(esc);
                if (!result) {
                    //failed
                    throw new Exception(context.getString(R.string.core_esc_send_bluetooth_failed));
                }
                break;
            default:
                throw new Exception(context.getString(R.string.core_esc_wrong_type));

        }
    }

    @Override
    public void cutPaper() {
        EscDraw draw = new EscDraw();
        draw.cutPaper();
        byte[] esc = draw.getEscCommand();
        ESC_EXECUTOR.execute(() -> {
            try {
                sendEsc(esc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
