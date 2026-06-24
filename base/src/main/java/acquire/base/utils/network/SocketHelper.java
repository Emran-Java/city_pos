package acquire.base.utils.network;


import android.util.Log;

import androidx.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import acquire.base.BaseApplication;
import acquire.base.R;
import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.network.exception.NetConnectException;
import acquire.base.utils.network.exception.NetReceiveException;
import acquire.base.utils.network.listener.ProcessListener;


/**
 * Socket utils
 *
 * @author Janson
 * @date 2021/3/24 14:59
 */
public class SocketHelper {

    protected Socket mSocket;
    /**
     * Server ip
     */
    protected String mIp;
    /**
     * Server port
     */
    protected int mPort;
    /**
     * Communication timeout in seconds
     */
    protected int mTimeOutSec;

    /**
     * True if the socket is cancelled
     */
    protected boolean mIsCancel;
    /**
     * ssl certificate.
     */
    protected SSLSocketFactory sslSocketFactory;
    /**
     * Packet length bytes
     */
    private final static int SOCKET_LENGTH_LEN = 2;

    protected ProcessListener processListener;


    /**
     * create a socket tool
     *
     * @param ip         server ip
     * @param port       server port
     * @param timeoutSec socket communication timeout
     * @param cers       ssl certificate. If none, SSL is not applicable
     */
    public SocketHelper(String ip, int port, int timeoutSec, InputStream... cers) {
        this.mIp = ip;
        this.mPort = port;
        this.mTimeOutSec = timeoutSec;
        if (cers != null && cers.length > 0) {
            TrustManager[] trustManagers = SslFactoryHelper.generateTrustManagers(cers);
            sslSocketFactory = SslFactoryHelper.getSslSocketFactory(null, trustManagers);
        }
    }

    /**
     * Send and receive data
     *
     * @param data request data
     * @return response
     * @throws NetConnectException Net Connect Failed
     * @throws NetReceiveException Receive Failed
     */
    public byte[] commSendRecv(@NonNull byte[] data) throws NetConnectException, NetReceiveException {
        if (mIsCancel) {
            //socket was cancelled
            throw new NetConnectException(BaseApplication.getAppString(R.string.base_socket_cancel_connect));
        }

        //System.exit(0);

        long start = System.currentTimeMillis();
        //connect server
        connect();
        if (mIsCancel) {
            LoggerUtils.i("Communication time: " + (System.currentTimeMillis() - start) + " ms.");
            //socket was cancelled
            throw new NetConnectException(BaseApplication.getAppString(R.string.base_socket_cancel_send));
        }
        //send data
        send(data);
        if (mIsCancel) {
            LoggerUtils.i("Communication time: " + (System.currentTimeMillis() - start) + " ms.");
            //socket was cancelled
            throw new NetReceiveException(BaseApplication.getAppString(R.string.base_socket_cancel_receive));
        }
        if (processListener != null) {
            processListener.sendFinish();
        }
        //receive data complete, transaction success
        byte[] response = receive();
        //communicate over,release socket
        closeComm();
        LoggerUtils.d("Communication time: " + (System.currentTimeMillis() - start) + "ms");

        Log.d("NewCall","response: "+response);
        if (response == null) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.base_socket_response_data_is_empty));
        }
        return response;
    }

    /**
     * Connect server
     *
     * @throws NetConnectException connect failed
     */
    protected void connect() throws NetConnectException {
        if (mSocket != null && mSocket.isConnected()) {
            //socket has been connected
            return;
        }
        mSocket = createSocket();
        if (mSocket == null) {
            throw new NetConnectException("create socket failed");
        }
        SocketAddress mAddress = new InetSocketAddress(mIp, mPort);
        LoggerUtils.d("connect server [" + mIp + ":" + mPort + "]");
        LoggerUtils.d("set time out: " + mTimeOutSec + "s");
        try {
            mSocket.connect(mAddress, mTimeOutSec * 1000);
            LoggerUtils.d("connect success!");
        } catch (Exception e) {
            LoggerUtils.e("connect failed!");
            release();
            throw new NetConnectException(e);
        }
    }

    /**
     * Send data
     *
     * @throws NetReceiveException send failed
     */
    protected void send(@NonNull byte[] data) throws NetReceiveException, NetConnectException {
//        LoggerUtils.d("send[" + data.length + "]: " + BytesUtils.bcdToString(data));
        LoggerUtils.d("send[" + data.length + "bytes ]" );
        DataOutputStream output;
        try {
            LoggerUtils.d("LogOn: mTimeOutSec: "+mTimeOutSec);
            mSocket.setSoTimeout(mTimeOutSec * 1000);
            output = new DataOutputStream(new BufferedOutputStream(mSocket.getOutputStream()));
        } catch (Exception e) {
            LoggerUtils.e("hand shake failed!");
            LoggerUtils.e("LogOn: ex: "+e.getMessage());
            release();
            throw new NetConnectException(e);
        }
        try {
            output.write(data);
            output.flush();
            LoggerUtils.i("send success!");
            LoggerUtils.d("LogOn: success ");
        } catch (Exception e) {
            LoggerUtils.e("send failed!");
            LoggerUtils.d("LogOn: failed! ex: "+e.getMessage());
            release();
            throw new NetReceiveException(e);
        }
    }

    /**
     * Receive data
     *
     * @return response data
     * @throws NetReceiveException receive failed
     */
    protected byte[] receive() throws NetReceiveException {
        LoggerUtils.d("ready to receive!");
        try {
            DataInputStream input = new DataInputStream(new BufferedInputStream(mSocket.getInputStream()));

            /*
             *length bytes[2 bytes]+ body[len bytes]
             */
            //length bytes size
            byte[] socketLen = new byte[2];
            int retLen = input.read(socketLen);
            if (retLen != SOCKET_LENGTH_LEN) {
                LoggerUtils.e("receive failed，error length: " + retLen);
                return null;
            }
            LoggerUtils.d("Socket Received read 2 bytes to len:"+retLen);
            int msgLen = (socketLen[0] & 0xFF) * 256 + (socketLen[1] & 0xFF);
            byte[] response = new byte[msgLen + 2];
            System.arraycopy(socketLen, 0, response, 0, 2);
            long start = System.currentTimeMillis();
            int totalRead = 0;
            while (totalRead < msgLen) {
                int bytesRead = input.read(response, 2 + totalRead, msgLen - totalRead);
                if (bytesRead == -1) {
                    LoggerUtils.e("Socket closed unexpectedly while reading data");
                    throw new NetReceiveException(BaseApplication.getAppString(R.string.base_socket_close_error));
                }
                totalRead += bytesRead;
                // Timeout check
                if (System.currentTimeMillis() - start > 3000) {
                    LoggerUtils.e("Response timeout. Expected: " + msgLen + ", Received: " + totalRead);
                    throw new NetReceiveException(BaseApplication.getAppString(R.string.base_socket_receive_fail));
                }
                LoggerUtils.d("Socket Received cumulative read length "+totalRead+"bytes");
            }
//            LoggerUtils.d("receive [" + response.length + "]: " + BytesUtils.bcdToString(response));
            LoggerUtils.d("receive [" + response.length + "bytes  ]" );
            LoggerUtils.i("receive success!");
            return response;
        } catch (Exception e) {
            LoggerUtils.e("receive failed!");
            release();
            throw new NetReceiveException(e);
        }
    }

    /**
     * Close communication
     */
    public void closeComm() {
        mIsCancel = true;
        //release socket
        release();
    }


    public void setProcessListener(ProcessListener processListener) {
        this.processListener = processListener;
    }

    /**
     * Create {@link Socket}
     */
    protected Socket createSocket() {
        if (sslSocketFactory != null ) {
            try {
                return sslSocketFactory.createSocket();
            } catch (IOException e) {
                LoggerUtils.e("create a socket failed!",e);
                return null;
            }
        }
        return new Socket();
    }

    /**
     * Release socket
     */
    protected void release() {
        LoggerUtils.d("release socket.");
        if (mSocket != null) {
            try {
                if (!mSocket.isClosed()) {
                    OutputStream outputStream = null;
                    InputStream inputStream = null;
                    if (mSocket.isConnected()) {
                        if (!mSocket.isOutputShutdown()) {
                            outputStream = mSocket.getOutputStream();
                        }
                        if (!mSocket.isInputShutdown()) {
                            inputStream = mSocket.getInputStream();
                        }
                    }
                    mSocket.close();
                    if (outputStream != null) {
                        outputStream.close();
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch (Exception e) {
                LoggerUtils.e("release socket failed!",e);
            }
            mSocket = null;
        }
    }
}
