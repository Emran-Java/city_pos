package acquire.sdk.dock;


import com.newland.nsdk.dock.DockException;
import com.newland.nsdk.dock.DockPort;

import acquire.base.utils.LoggerUtils;

/**
 * The RS232 port of the dock.
 *
 * @author Janson
 * @date 2023/4/12 16:17
 */
public class DockSerialPort extends BaseDock{
    private DockPort dockPort;

    /**
     * Open the serial port
     *
     * @param baudRate port baud rate. e.g. 9600、115200
     * @return true: open success; false: open failed
     */
    public boolean open(final int baudRate) {
        if (init()){
            try {
                LoggerUtils.d("open Dock serial port:"+baudRate);
                DockPort temp = dock.getPortManager().getPort(0,baudRate+",8,N,1");
                temp.open();
                temp.flush();
                dockPort = temp;
                return true;
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return false;
    }

    /**
     * open type-C port.
     * <p>Note: When type C is opened, other port will be closed.</p>
     */
    public boolean openTypeC() {
        if (init()){
            try {
                LoggerUtils.d("open Dock type-C");
                DockPort temp = dock.getPortManager().getPort(8,"115200,8,8,N,1");
                temp.open();
                temp.flush();
                dockPort = temp;
                return true;
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return false;
    }

    /**
     * Read the serial port
     *
     * @param lengthMax    Expect read length
     * @param timeoutMillis Reading time out, unit: ms. If value <= 0, read the existing serial port data immediately without waiting.
     * @return data of reading serial port
     */
    public byte[] read(int lengthMax, int timeoutMillis) {
        if (isInited() && dockPort != null){
            try {
                return dockPort.read(lengthMax,timeoutMillis);
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return null;

    }

    /**
     * Write data
     *
     * @param data         Writes the data buffer corresponding to the source address
     * @return true: write success; false: write failed
     */
    public boolean write(byte[] data) {
        if (isInited() && dockPort != null){
            try {
                dockPort.write(data);
                return true;
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return false;
    }

    /**
     * Close the serial port
     *
     * @return true: close success; false: close failed.
     */
    public boolean close() {
        if (isInited() && dockPort != null){
            try {
                LoggerUtils.d("close Dock serial port");
                dockPort.close();
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
    public void flush() {
        if (isInited() && dockPort != null){
            try {
                dockPort.flush();
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
    }

    /**
     * get the read cache length
     */
    public int getCacheLength(){
        if (isInited() && dockPort!= null){
            try {
                return dockPort.readLen();
            } catch (DockException e) {
                LoggerUtils.e(e.getMessage()+",code="+e.getCode());
            }
        }
        return 0;
    }

    public boolean isConnected() {
        return dockPort != null;
    }
}
