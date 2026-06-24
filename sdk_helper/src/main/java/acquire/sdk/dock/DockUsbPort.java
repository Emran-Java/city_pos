package acquire.sdk.dock;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.newland.nsdk.dock.DockException;
import com.newland.nsdk.dock.DockUSB;
import com.newland.nsdk.dock.DockUSBManager;

import acquire.base.utils.LoggerUtils;


/**
 * The usb port of the dock.
 *
 * @author Janson
 * @date 2023/4/12 16:17
 */
public class DockUsbPort extends BaseDock{
    private final DockUSB usb;


    public DockUsbPort(@IntRange(from = 1,to = 2) int id) {
        DockUSBManager usbManager = dock.getUsbManager();
        usb = usbManager.getUSB(id);
    }

    /**
     * get the usb count of this dock
     */
    public static int getUsbCount(){
        try {
            return new BaseDock().dock.getUsbManager().getUSBCount();
        } catch (DockException e) {
            LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            return 0;
        }
    }


    /**
     * open USB port.
     */
    public boolean open() {
        if (init()){
            try {
                LoggerUtils.d("open Dock usb");
                usb.open();
                return true;
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return false;
    }

    /**
     * close USB port
     */
    public void close() {
        if (init()){
            try {
                LoggerUtils.d("close Dock usb");
                usb.close();
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
    }

    /**
     * read data
     */
    public byte[] read(int lengthMax, int timeoutMillis) {
        if (init()){
            // <=0, read data immediately;
            // >0,It does not end until the maximum length data is read or the readTimeout occurs
            try {
                return usb.read(lengthMax, timeoutMillis);
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return null;
    }

    /**
     * write data
     */
    public boolean write(@NonNull byte[] data) {
        if (init()){
            try {
                usb.write(data);
                return true;
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return false;
    }
    /**
     * flush read cache
     */
    public void flush(){
        if (init()){
            try {
                usb.flush();
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }

    }
    /**
     * get the read cache length
     */
    public int getCacheLength(){
        if (isConnected()){
            try {
                return usb.readLen();
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return 0;
    }

    public boolean isConnected(){
        if (init()){
            try {
                return usb.isOnline();
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return false;
    }

    /**
     * get the usb factory id
     */
    public String getUsbFactoryId(){
        try {
            return usb.getInfo().factoryID;
        } catch (DockException e) {
            LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            return null;
        }
    }
}
