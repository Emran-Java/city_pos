package acquire.base.utils.network;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import acquire.base.BaseApplication;
import acquire.base.R;
import acquire.base.utils.LoggerUtils;
import acquire.base.utils.network.exception.NetConnectException;
import acquire.base.utils.network.exception.NetHttpCodeException;
import acquire.base.utils.network.exception.NetReceiveException;
import acquire.base.utils.network.listener.HttpEventListener;
import acquire.base.utils.network.listener.ProcessListener;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Http utils by {@link OkHttpClient}.
 *
 * @author Janson
 * @date 2018/2/19
 */
public class HttpOkHelper {
    protected String mUrl;
    protected Call mCall;
    protected OkHttpClient mOkHttpClient;
    /**
     * HTTP event listener
     */
    protected HttpEventListener httpEventListener = new HttpEventListener();

    /**
     * @param url        server address
     * @param timeoutSec communication time out in seconds
     * @param cers       ssl certificate. If null,not certificate
     */
    public HttpOkHelper(String url, int timeoutSec, InputStream... cers) {
        this.mUrl = url;
        initHttpClient(url, timeoutSec, validCers(cers));
    }

    private InputStream[] validCers(InputStream... cers) {
        if (cers == null) {
            return null;
        }
        List<InputStream> validCers = new ArrayList<>();
        for (InputStream cer : cers) {
            if (cer != null) {
                validCers.add(cer);
            }
        }
        if (validCers.isEmpty()) {
            return null;
        } else {
            return validCers.toArray(new InputStream[0]);
        }
    }


    /**
     * Init configuration
     *
     * @param url        server address
     * @param timeoutSec communication time out in seconds
     * @param cers       ssl certificate. If null,not certificate
     */
    protected void initHttpClient(String url, int timeoutSec, InputStream[] cers) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (cers != null) {
            //SSL certificate
            builder.hostnameVerifier((hostname, session) -> true);
            TrustManager[] trustManagers = SslFactoryHelper.generateTrustManagers(cers);
            SSLSocketFactory sslSocketFactory = SslFactoryHelper.getSslSocketFactory(null, trustManagers);
            if (sslSocketFactory != null) {
                builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0]);
            }
        } else {
            if (url.startsWith("https:")) {
                //https
                builder.hostnameVerifier((hostname, session) -> true);
                TrustManager[] trustManagers = SslFactoryHelper.generateDefaultTrustManagers();
                SSLSocketFactory sslSocketFactory = SslFactoryHelper.getTrustAllSocketFactory();
                if (sslSocketFactory != null && trustManagers != null && trustManagers.length > 0) {
                    builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustManagers[0]);
                }
            }
            //http
        }
        // timeout
        builder.connectTimeout(timeoutSec, TimeUnit.SECONDS)
                .readTimeout(timeoutSec, TimeUnit.SECONDS)
                .writeTimeout(timeoutSec, TimeUnit.SECONDS)
                //http event listener
                .eventListener(httpEventListener)
                //print http log
                .addNetworkInterceptor(new HttpLogInterceptor(HttpLogInterceptor.Level.HEADERS))
                .cache(null);
        //If an exception occurs in SSLProtocolException, it indicates that the server ssl protocol version is too low,
        // please use the following code:
//        List<ConnectionSpec> connectionSpecList = new ArrayList<>();
//        connectionSpecList.add(ConnectionSpec.COMPATIBLE_TLS);
//        builder.connectionSpecs(connectionSpecList);
        this.mOkHttpClient = builder.build();
    }

    /**
     * Listener to data has been sent over
     */
    public void setProcessListener(ProcessListener processListener) {
        httpEventListener.attachProcessListener(processListener);
    }

    /**
     * Post form
     * <p><hr><b>e.g.</b></p>
     * <pre>
     *     a=2&b=3&c=4
     * </pre>
     *
     * @param map form data. key-value
     */
    public ResponseBody httpPostForm(@NonNull Map<String, String> map) throws NetHttpCodeException, NetReceiveException, NetConnectException {
        FormBody.Builder bodyBuilder = new FormBody.Builder();
        for (Entry<String, String> entry : map.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            bodyBuilder.add(key, value);
        }
        FormBody formBody = bodyBuilder.build();
        Request request = new Request.Builder()
                .url(mUrl)
                .post(formBody)
                .build();
        mCall = mOkHttpClient.newCall(request);
        return syncCallExe();
    }

    /**
     * Post xml
     * <p><hr><b>e.g.</b></p>
     * <pre>
     *     {@code <root> <a>1</a> <b>2</b> </root>}
     * </pre>
     *
     * @param xml xml data
     * @return response body
     */
    public String httpPostXml(String xml) throws NetHttpCodeException, NetReceiveException, NetConnectException {
        MediaType mediaType = MediaType.parse("application/xml");
        RequestBody requestBody = RequestBody.create(xml, mediaType);
        Request request = new Request.Builder()
                .url(mUrl)
                .post(requestBody)
                .build();
        mCall = mOkHttpClient.newCall(request);
        ResponseBody responseBody = syncCallExe();
        try {
            return responseBody.string();
        } catch (IOException e) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.base_http_response_body_parse_fail));
        }
    }

    /**
     * Post JSON
     * <p><hr><b>e.g.</b></p>
     * <pre>
     *     {
     *         "a"="2",
     *         "b"="4",
     *         "c"="5"
     *      }
     * </pre>
     * @param json json string
     * @return response body
     */
    public String httpPostJson(String json, Map<String,String> headers) throws NetHttpCodeException, NetReceiveException, NetConnectException {
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = RequestBody.create(json, mediaType);
        Request.Builder requestBuilder = new Request.Builder()
                .url(mUrl)
                .post(requestBody);
        if (headers != null){
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                requestBuilder.addHeader(entry.getKey(), entry.getValue());
            }
        }
        Request request = requestBuilder.build();
//        Request request = new Request.Builder()
//                .url(mUrl)
//                .post(requestBody)
//                .build();
        mCall = mOkHttpClient.newCall(request);
        ResponseBody responseBody = syncCallExe();
        try {
            return responseBody.string();
        } catch (IOException e) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.base_http_response_body_parse_fail));
        }
    }

    /**
     * Post bytes
     *
     * @param data bytes data
     * @return response byte data
     */
    public byte[] httpPostByte(byte[] data) throws NetHttpCodeException, NetReceiveException, NetConnectException {
        MediaType mediaType = MediaType.parse("application/octet-stream; charset=utf-8");
        RequestBody requestBody = RequestBody.create(data, mediaType);
        Request request = new Request.Builder()
                .url(mUrl)
                .post(requestBody)
                .build();
        mCall = mOkHttpClient.newCall(request);
        ResponseBody responseBody = syncCallExe();
        try {
            return responseBody.bytes();
        } catch (IOException e) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.base_http_response_body_parse_fail));
        }
    }

    /**
     * Post file
     *
     * @param file file to be posted
     * @return response body
     */
    public ResponseBody httpPostFile(File file) throws NetHttpCodeException, NetReceiveException, NetConnectException {
        //[Mime type],see http://www.w3school.com.cn/media/media_mimeref.asp
        String mediaType = "application/octet-stream";
        RequestBody requestBody = RequestBody.create(file, MediaType.parse(mediaType));
        MultipartBody multBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", file.getName(), requestBody)
                .build();
        Request request = new Request.Builder()
                .url(mUrl)
                .post(multBody)
                .build();
        mCall = mOkHttpClient.newCall(request);
        return syncCallExe();
    }

    /**
     * Get (String)
     *
     * @return response body
     */
    public String httpGetString() throws NetHttpCodeException, NetReceiveException, NetConnectException {
        //url
        Request request = new Request.Builder()
                .url(mUrl)
                .build();
        mCall = mOkHttpClient.newCall(request);
        ResponseBody responseBody = syncCallExe();
        try {
            return responseBody.string();
        } catch (IOException e) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.base_http_response_body_parse_fail));
        }
    }

    /**
     * Get (byte[])
     *
     * @return response body
     */
    public byte[] httpGetByte() throws NetHttpCodeException, NetReceiveException, NetConnectException {
        //url
        Request request = new Request.Builder()
                .url(mUrl)
                .build();
        mCall = mOkHttpClient.newCall(request);
        ResponseBody responseBody = syncCallExe();
        try {
            return responseBody.bytes();
        } catch (IOException e) {
            throw new NetReceiveException(BaseApplication.getAppString(R.string.base_http_response_body_parse_fail));
        }
    }

    /**
     * Send data synchronously, blocking mode
     *
     * @return response body
     */
    protected ResponseBody syncCallExe() throws NetConnectException, NetReceiveException, NetHttpCodeException {
        Response response;
        long start = System.currentTimeMillis();
        try {
            response = mCall.execute();
        } catch (IOException e) {
            if (httpEventListener.isConnected()) {
                //Socket has been connected
                throw new NetReceiveException(e);
            } else {
                //Socket connected failed
                throw new NetConnectException(e);
            }
        } finally {
            LoggerUtils.i("Communication time: " + (System.currentTimeMillis() - start) + " ms.");
            //clear httpEventListener
            httpEventListener.reset();
        }
        if (response.code() == HttpURLConnection.HTTP_OK) {
            return response.body();
        } else {
            LoggerUtils.e("code: " + response.code() + ", message: " + response.message());
            throw new NetHttpCodeException(response.code(), response.message());
        }
    }

    /**
     * Abort http
     */
    public void abort() {
        if (mCall != null) {
            mCall.cancel();
        }
    }
}
