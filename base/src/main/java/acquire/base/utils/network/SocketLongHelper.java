package acquire.base.utils.network;


import androidx.annotation.NonNull;

import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;

import acquire.base.utils.LoggerUtils;
import acquire.base.utils.network.exception.NetConnectException;
import acquire.base.utils.network.exception.NetReceiveException;


/**
 * Long Connection Socket Utils
 *
 * @author Janson
 * @date 2021/3/24 15:34
 */
public class SocketLongHelper extends SocketHelper{

    /**
     * create a long-connected socket tool
     *
     * @param ip         server ip
     * @param port       server port
     * @param timeoutSec socket communication timeout
     * @param cers       ssl certificate. If none, SSL is not applicable
     */
    public SocketLongHelper(String ip, int port, int timeoutSec, InputStream... cers) {
        super(ip, port, timeoutSec,cers);
        mIsCancel = false;
        LoggerUtils.d("Long connection mode.");
    }

    @Override
    protected void connect() throws NetConnectException {
        mSocket = LongHolder.getInstance().socket;
        if (LongHolder.getInstance().needReset(mIp,mPort)){
            //reset
            LoggerUtils.d("Long connection reset.");
            //re-connect socket
            release();
            super.connect();
            try {
                mSocket.setKeepAlive(true);
            } catch (SocketException e) {
                throw new NetConnectException(e);
            }
            // save socket configuration
            LongHolder.getInstance().socket = mSocket;
            LongHolder.getInstance().ip = mIp;
            LongHolder.getInstance().port = mPort;
        }
    }



    @Override
    protected void send(@NonNull byte[] data) throws NetReceiveException, NetConnectException {
        //reset long connection time
        if (mSocket != LongHolder.getInstance().socket){
            LongHolder.getInstance().resetTime();
        }
        super.send(data);
    }

    @Override
    protected byte[] receive() throws NetReceiveException {
        //reset long connection time
        if (mSocket != LongHolder.getInstance().socket){
            LongHolder.getInstance().resetTime();
        }
        return super.receive();
    }

    @Override
    public void closeComm() {
        mIsCancel = true;
        try {
            if (mSocket != LongHolder.getInstance().socket){
                release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * Long connection configuration
     *
     * @author Janson
     * @date 2021/3/24 15:36
     */
    private static class LongHolder{

        private long startTime;
        /**
         * keep time
         */
        private static final long KEEP_TIME = 60 * 1000;

        protected Socket socket;

        protected String ip;

        protected int port;

        private static volatile LongHolder instance;
        private LongHolder() {}
        public static LongHolder getInstance() {
            if (instance == null){
                synchronized (LongHolder.class){
                    if (instance == null){
                        instance = new LongHolder();
                    }
                }
            }
            return instance;
        }

        boolean needReset(String ip,int port){
            if (socket == null
                    || socket.isOutputShutdown()
                    || socket.isInputShutdown()
                    || !socket.isConnected()
                    || socket.isClosed()
                    || !ip.equals(this.ip) || port != this.port) {
                return true;
            }
            if (System.currentTimeMillis() >= startTime + KEEP_TIME){
                LoggerUtils.d("Long connection time out!");
                return true;
            }
            return false;
        }


        void resetTime(){
            startTime = System.currentTimeMillis();
        }
    }

}
