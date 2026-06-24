package acquire.sdk.dock;


import android.os.Looper;
import android.os.RemoteException;

import com.newland.nsdk.dock.Dock;
import com.newland.nsdk.dock.DockException;
import com.newland.nsdk.dock.DockManager;
import com.newland.nsdk.dock.ServiceCallback;
import com.newland.nsdk.dock.model.USBInfo;

import acquire.base.BaseApplication;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.thread.Locker;

/**
 * A dock basic class.
 * <p>Note: Don't perform read and write operations on the main thread</p>
 *
 * @author Janson
 * @date 2023/4/12 10:02
 */
public class BaseDock {
    protected Dock dock;
    private static boolean isConnected;
    public BaseDock() {
        dock = DockManager.getDock(BaseApplication.getAppContext());
    }

    protected void checkThread(){
        if (Looper.getMainLooper() == Looper.myLooper()){
            throw new RuntimeException("Do not allow Dock API to execute on the main thread");
        }
    }
    /**
     * init dock.Don't execute on main thread !
     */
    public boolean init() {
        if (!dock.isSupported()) {
            LoggerUtils.e("[Dock]--Dock Service doesn't exist.");
            return false;
        }
        if (!isInited()) {
            LoggerUtils.i("[Dock]--Dock init start.");
            checkThread();
            try {
                Locker<Boolean> locker = new Locker<>(false);
                dock.init(new ServiceCallback.a() {
                    @Override
                    public void onStatusChanged(int status) {
                        LoggerUtils.d("[Dock]--status: "+ status);
                    }

                    @Override
                    public void onConnectionChanged(boolean connected) {
                        LoggerUtils.d("[Dock]--onConnectionChanged: "+ connected);
                        isConnected = connected;
                        if (connected) {
                            LoggerUtils.d("[Dock]--Dock init over.");
                            locker.setResult(true);
                            locker.wakeUp();
                        }
                    }

                    @Override
                    public void onUsbDeviceChanged(int i, USBInfo usbInfo, boolean b) throws RemoteException {

                    }
                });
                locker.waiting(6000);
                boolean result = locker.getResult();
                if (!result){
                    LoggerUtils.e("[Dock]--Dock init timeout.");
                }
                return result;
            } catch (DockException e) {
                LoggerUtils.e("[Dock]--Dock init failed.", e);
                return false;
            }
        }
        return true;
    }

    public boolean isInited() {
        return isConnected;
//        return DockStatus.isStateOf(dock.getState(), DockStatus.STATUS_CONNECTED);
    }


    public void release() {
        if (isInited()) {
            try {
                dock.release();
            } catch (DockException e) {
                LoggerUtils.e("release dock failed",e);
            }
        } else {
            LoggerUtils.i("[Dock]--Dock isn't initialized,so doesn't execute method[destroy]!");
        }
    }

    public byte[] getDockType() {
        if (init()){
            try {
                checkThread();
                return dock.getDockInfo(0);
            } catch (DockException e) {
                LoggerUtils.e("get dock type failed",e);
            }
        }
        return null;
    }

    public byte[] getDockBiosVersion() {
        if (init()){
            try {
                checkThread();
                return dock.getDockInfo(2);
            } catch (DockException e) {
                LoggerUtils.e("get dock BIOS version failed",e);
            }
        }
        return null;
    }

    public byte[] getDockSn() {
        if (init()){
            try {
                checkThread();
                return dock.getDockInfo(3);
            } catch (DockException e) {
                LoggerUtils.e("get dock SN failed",e);
            }
        }
        return null;
    }

    public byte[] getDockMachineId() {
        if (init()){
            try {
                checkThread();
                return dock.getDockInfo(4);
            } catch (DockException e) {
                LoggerUtils.e("get dock machine ID failed",e);
            }
        }
        return null;
    }

    public boolean startSettings() {
        if (!dock.isSupported()) {
            return false;
        }
        dock.startSettings();
        return true;
    }
}
