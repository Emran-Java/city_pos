package acquire.base.utils.network.request;

import androidx.annotation.NonNull;

import java.io.IOException;

import acquire.base.utils.LoggerUtils;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * Upload progress request body.
 * <pre>
 *     RequestBody requestBody;
 *     ...
 *     Request request = new Request.Builder()
 *                  .url(mUrl)
 *                  .post(new UploadProgressRequestBody(requestBody))
 *                  .build();
 * </pre>
 *
 * @author Janson
 * @date 2022/7/8 17:18
 */
public abstract class AbstractUploadProgressRequestBody extends RequestBody {
    private final RequestBody requestBody;
    private BufferedSink bufferedSink;

    private long writtenSize = 0;
    private long totalSize = -1;

    private AbstractUploadProgressRequestBody(RequestBody requestBody){
        this.requestBody = requestBody;
    }

    public abstract void onProgress(long currentBytes, long contentLength);
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() {
        if (totalSize == -1){
            try {
                totalSize = requestBody.contentLength();
            } catch (IOException e) {
                LoggerUtils.e("contentLength failed !",e);
            }
        }
        return totalSize;
    }

    @Override
    public void writeTo(@NonNull BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            bufferedSink = Okio.buffer(new ProgressBufferSink(sink));
        }
        requestBody.writeTo(bufferedSink);
        bufferedSink.flush();
    }

    class ProgressBufferSink extends ForwardingSink {

        ProgressBufferSink(Sink delegate) {
            super(delegate);
        }

        @Override
        public void write(@NonNull Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            writtenSize += byteCount;
            onProgress(writtenSize, contentLength());
        }

    }

}
