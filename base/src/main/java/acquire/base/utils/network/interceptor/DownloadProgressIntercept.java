package acquire.base.utils.network.interceptor;


import androidx.annotation.NonNull;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;

/**
 * download progress interceptor.
 * <pre>
 *        DownloadProgressIntercept downloadProgressIntercept = new DownloadProgressIntercept(){
 *              protected void onDownloadProgress(long readSize,long totalSize){
 *                  //do your things
 *                  ...
 *              }
 *        }
 *        OkHttpClient.Builder builder = new OkHttpClient.Builder();
 *        builder.addInterceptor(downloadProgressIntercept);
 *        ....
 *        OkHttpClient okHttpClient = builder.build();
 * </pre>
 *
 * @author Janson
 * @date 2022/7/8 9:28
 */
public abstract class DownloadProgressIntercept implements Interceptor{
    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Response responseResponse = chain.proceed(chain.request());
        return responseResponse.newBuilder()
                .body(new ProgressResponseBody(responseResponse.body()))
                .build();
    }
    protected abstract void onDownloadProgress(long readSize,long totalSize);

    public class ProgressResponseBody extends ResponseBody {
        private final ResponseBody responseBody;
        private BufferedSource bufferedSource;
        private long readSize;
        public ProgressResponseBody( ResponseBody responseBody) {
            this.responseBody = responseBody;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @NonNull
        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(new ProgressBufferedSource(responseBody.source()));
            }
            return bufferedSource;
        }

        @Override
        public long contentLength() {
            //if response http head dosen't has Content-length, responseBody.contentLength() will be -1.
            // Force content-length, you can write 'request.builder.addHeader("Accept-Encoding", "identity")'
            return responseBody.contentLength();
        }

        class ProgressBufferedSource extends ForwardingSource {

            ProgressBufferedSource(BufferedSource delegate) {
                super(delegate);
            }

            @Override
            public long read(@NonNull Buffer sink, long byteCount) throws IOException {
                long readLength = super.read(sink, byteCount);
                readSize += readLength > 0 ? readLength : 0;
                onDownloadProgress(readSize, contentLength());
                return readLength;
            }
        }
    }
} 
