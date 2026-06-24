package acquire.base.utils.network.listener;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.EventListener;
import okhttp3.Handshake;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

/**
 * HTTP event listener of {@link okhttp3.OkHttp}
 *
 * @author Janson
 * @date 2019/6/16 22:55
 */
public class HttpEventListener extends EventListener {
    private boolean connected;
    private boolean hasSendData;
    private ProcessListener processListener;


    public void reset(){
        connected = false;
        hasSendData = false;
        processListener = null;
    }
    /**
     * Set net process listener
     */
    public void attachProcessListener(ProcessListener processListener) {
        this.processListener = processListener;
    }

    public boolean isConnected() {
        return connected;
    }

    /**
     * Get send status.
     * @return true if the data has been sent.
     */
    public boolean hasSendData() {
        return hasSendData;
    }

    /**
     * 1. start http process
     */
    @Override
    public void callStart(@NonNull Call call) {
    }

    /**
     * 2. DNS start to parse
     */
    @Override
    public void dnsStart(@NonNull Call call,@NonNull String domainName) {
    }

    /**
     * 3. DNS parse over
     */
    @Override
    public void dnsEnd(@NonNull Call call,@NonNull String domainName,@NonNull List<InetAddress> inetAddressList) {
    }

    /**
     * 4. socket start to connect
     */
    @Override
    public void connectStart(@NonNull Call call,@NonNull InetSocketAddress inetSocketAddress,@NonNull Proxy proxy) {
    }

    /**
     * 5.SSL start to connect
     */
    @Override
    public void secureConnectStart(@NonNull Call call) {
    }

    /**
     * 6.SSL connect over
     */
    @Override
    public void secureConnectEnd(@NonNull Call call, Handshake handshake) {
    }

    /**
     * 7. socket connect over
     */
    @Override
    public void connectEnd(@NonNull Call call,@NonNull InetSocketAddress inetSocketAddress,@NonNull Proxy proxy, Protocol protocol) {
        connected = true;
    }

    /**
     * 7. socket connect failed
     */
    @Override
    public void connectFailed(@NonNull Call call,@NonNull InetSocketAddress inetSocketAddress,@NonNull Proxy proxy, Protocol protocol,@NonNull IOException ioe) {
    }

    /**
     * 8. apply for multiplex connection(Long connection)
     */
    @Override
    public void connectionAcquired(@NonNull Call call,@NonNull Connection connection) {
    }

    /**
     * 9. release multiplex connection(Long connection)
     */
    @Override
    public void connectionReleased(@NonNull Call call,@NonNull Connection connection) {
    }

    /**
     * 10. start to send request headers
     */
    @Override
    public void requestHeadersStart(@NonNull Call call) {
    }

    /**
     * 11. send request headers over
     */
    @Override
    public void requestHeadersEnd(@NonNull Call call,@NonNull Request request) {
    }

    /**
     * 12. start to send request body
     */
    @Override
    public void requestBodyStart(@NonNull Call call) {
        if (processListener != null) {
            processListener.sendFinish();
        }
        hasSendData = true;
    }

    /**
     * 13. send request body over
     */
    @Override
    public void requestBodyEnd(@NonNull Call call, long byteCount) {
    }

    /**
     * 14. start to read response headers
     */
    @Override
    public void responseHeadersStart(@NonNull Call call) {
    }

    /**
     * 15. read response headers over
     */
    @Override
    public void responseHeadersEnd(@NonNull Call call,@NonNull Response response) {
    }

    /**
     * 16. start to read response body
     */
    @Override
    public void responseBodyStart(@NonNull Call call) {
    }

    /**
     * 17. read response body over
     */
    @Override
    public void responseBodyEnd(@NonNull Call call, long byteCount) {
    }

    /**
     *  http complete. It happens in two cases
     * <p>
     *   1.socket close.
     *   2.release connection(cancel)
     */
    @Override
    public void callEnd(@NonNull Call call) {
    }

    /**
     *  http  failed.It happens in two cases
     * <p>
     *     1.error when request.
     *     2.close I/O error after request
     */
    @Override
    public void callFailed(@NonNull Call call,@NonNull IOException ioe) {
    }

}
