/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * Distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * Limitations under the License.
 */
package acquire.base.utils.network;

import static java.net.HttpURLConnection.HTTP_NOT_MODIFIED;
import static java.net.HttpURLConnection.HTTP_NO_CONTENT;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import java.io.EOFException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import acquire.base.utils.BytesUtils;
import acquire.base.utils.LoggerUtils;
import okhttp3.Connection;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.GzipSource;


/**
 * An OkHttp interceptor which logs request and response information. Can be applied as an
 * {@linkplain OkHttpClient#interceptors() application interceptor} or as a {@linkplain
 * OkHttpClient#networkInterceptors() network interceptor}.
 * <p> The format of the logs created by his class should not be considered stable and may
 * change slightly between releases. If you need a stable logging format, use your own interceptor.
 *
 * @author Janson
 */
public final class HttpLogInterceptor implements Interceptor {
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    public enum Level {
        /**
         * No logs.
         */
        NONE,
        /**
         * Logs request and response lines.
         * <p>
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1 (3-byte body)
         *
         * <-- 200 OK (22ms, 6-byte body)
         * }</pre>
         */
        BASIC,
        /**
         * Logs request and response lines and their respective headers.
         * <p>
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         * <-- END HTTP
         * }</pre>
         */
        HEADERS,
        /**
         * Logs request and response lines and their respective headers and bodies (if present).
         * <p>
         * <p>Example:
         * <pre>{@code
         * --> POST /greeting http/1.1
         * Host: example.com
         * Content-Type: plain/text
         * Content-Length: 3
         *
         * Hi?
         * --> END POST
         *
         * <-- 200 OK (22ms)
         * Content-Type: plain/text
         * Content-Length: 6
         *
         * Hello!
         * <-- END HTTP
         * }</pre>
         */
        BODY
    }

    private final Level level;

    public HttpLogInterceptor(Level level) {
        this.level = level;
    }

    private final static int MAX_LOG_LIMIT = 20 * 1024;
    private final static int MAX_LINE_WORDS = 3000;
    private StringBuilder buffer = new StringBuilder();

    private void addLog(String log) {
        buffer.append(log);
        buffer.append("\r\n");
    }

    private void printLog() {
        String log = buffer.toString();
        if (log.length() < MAX_LINE_WORDS) {
            LoggerUtils.i(log);
        } else {
            int n = log.length() / MAX_LINE_WORDS;
            if (log.length() % MAX_LINE_WORDS != 0) {
                n++;
            }
            for (int i = 0; i < n; i++) {
                if (i != n - 1) {
                    LoggerUtils.i(log.substring(i * MAX_LINE_WORDS, (i + 1) * MAX_LINE_WORDS));
                } else {
                    LoggerUtils.i(log.substring(i * MAX_LINE_WORDS));
                }
            }
        }
    }

    private void clearLog() {
        buffer = new StringBuilder();
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        if (level == Level.NONE) {
            return chain.proceed(request);
        }
        //print body
        boolean logBody = level == Level.BODY;
        //print header
        boolean logHeaders = logBody || level == Level.HEADERS;

        RequestBody requestBody = request.body();
        boolean hasRequestBody = requestBody != null;

        /*
         *1.request：connect status
         */
        Connection connection = chain.connection();
        String requestStartMessage = "--> [REQUEST] START\r\n" + request.method() + (connection != null ? " " + connection.protocol() : "");
        if (!logHeaders && hasRequestBody) {
            requestStartMessage += " (" + requestBody.contentLength() + "-byte body)";
        }
        addLog(requestStartMessage);

        /*
         *2.request：headers
         */
        if (logHeaders) {
            if (hasRequestBody) {
                // Request body headers are only present when installed as a network interceptor. Force
                // them to be included (when available) so there values are known.
                if (requestBody.contentType() != null) {
                    addLog("Content-Type: " + requestBody.contentType());
                }
                if (requestBody.contentLength() != -1) {
                    addLog("Content-Length: " + requestBody.contentLength());
                }
            }

            Headers headers = request.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                String name = headers.name(i);
                // Skip headers from the request body as they are explicitly logged above.
                if (!"Content-Type".equalsIgnoreCase(name) && !"Content-Length".equalsIgnoreCase(name)) {
                    addLog(name + ": " + headers.value(i));
                }
            }

            if (!logBody || !hasRequestBody) {
                addLog("--> [REQUEST] END " + request.method());
            } else if (bodyHasUnknownEncoding(request.headers())) {
                addLog("--> [REQUEST] END " + request.method() + " (encoded body omitted)");
            } else {
                if (requestBody.contentLength() <= MAX_LOG_LIMIT) {
                    Buffer buffer = new Buffer();
                    requestBody.writeTo(buffer);
                    if (isPlaintext(buffer)) {
                        Charset charset = DEFAULT_CHARSET;
                        MediaType contentType = requestBody.contentType();
                        if (contentType != null) {
                            charset = contentType.charset(DEFAULT_CHARSET);
                        }
                        if (charset != null) {
                            addLog(buffer.readString(charset));
                        }
                        addLog("--> [REQUEST] END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
                    } else {
                        addLog("(HEX)" + BytesUtils.bcdToString(buffer.readByteArray()));
                        addLog("--> [REQUEST] END " + request.method() + " (binary " + requestBody.contentLength() + "-byte body omitted)");
                    }
                } else {
                    addLog("\n*********Request body is too big, so not print log.*********\n");
                    addLog("--> [REQUEST] END " + request.method() + " (" + requestBody.contentLength() + "-byte body)");
                }
            }
        }
        printLog();
        clearLog();
        long startNs = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (Exception e) {
            addLog("<-- [RESPONSE] HTTP FAILED: " + e.getMessage());
            printLog();
            clearLog();
            throw e;
        }
        long tookMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

        ResponseBody responseBody = response.body();
        /*
         *3.response：status
         */
        long contentLength = responseBody.contentLength();
        String bodySize = contentLength != -1 ? contentLength + "-byte" : "unknown-length";
        addLog("<-- [RESPONSE] " + response.code() + (response.message().isEmpty() ? "" : ' ' + response.message()) + ' ' + response.request().url() + " (" + tookMs + "ms" + (!logHeaders ? ", " + bodySize + " body" : "") + ')');
        /*
         *4.response：headers
         */
        if (logHeaders) {
            Headers headers = response.headers();
            for (int i = 0, count = headers.size(); i < count; i++) {
                addLog(headers.name(i) + ": " + headers.value(i));
            }
            if (!logBody || !hasBody(response)) {
                addLog("<-- [RESPONSE] END HTTP");
            } else if (bodyHasUnknownEncoding(response.headers())) {
                addLog("<-- [RESPONSE] END HTTP (encoded body omitted)");
            } else {
                BufferedSource source = responseBody.source();
                // Buffer the entire body.
                source.request(MAX_LOG_LIMIT + 1);
                Buffer buffer = source.getBuffer();
                if (buffer.size() <= MAX_LOG_LIMIT) {
                    Long gzippedLength = null;
                    if ("gzip".equalsIgnoreCase(headers.get("Content-Encoding"))) {
                        gzippedLength = buffer.size();
                        try (GzipSource gzippedResponseBody = new GzipSource(buffer.clone())) {
                            buffer = new Buffer();
                            buffer.writeAll(gzippedResponseBody);
                        }
                    }
                    if (isPlaintext(buffer)) {
                        if (contentLength != 0) {
                            Charset charset = DEFAULT_CHARSET;
                            MediaType contentType = responseBody.contentType();
                            if (contentType != null) {
                                charset = contentType.charset(DEFAULT_CHARSET);
                            }
                            addLog(buffer.clone().readString(charset == null ? DEFAULT_CHARSET : charset));
                        }
                        if (gzippedLength != null) {
                            addLog("<-- [RESPONSE] END HTTP (" + buffer.size() + "-byte, " + gzippedLength + "-gzipped-byte body)");
                        } else {
                            addLog("<-- [RESPONSE] END HTTP (" + buffer.size() + "-byte body)");
                        }
                    } else {
                        addLog("(HEX)" + BytesUtils.bcdToString(buffer.clone().readByteArray()));
                        addLog("<-- [RESPONSE] END HTTP (binary " + buffer.size() + "-byte body omitted)");

                    }
                } else {
                    addLog("\n*********Response body is too big, so not print log.*********\n");
                    addLog("<-- [RESPONSE] END HTTP (" + buffer.size() + "-byte body)");
                }
            }
        }
        printLog();
        clearLog();
        return response;
    }

    /**
     * Returns true if the body in question probably contains human readable text. Uses a small sample
     * of code points to detect unicode control characters commonly used in binary file signatures.
     */
    private boolean isPlaintext(@NonNull Buffer buffer) {
        try {
            Buffer prefix = new Buffer();
            long byteCount = buffer.size() < 64 ? buffer.size() : 64;
            buffer.copyTo(prefix, 0, byteCount);
            for (int i = 0; i < 16; i++) {
                if (prefix.exhausted()) {
                    break;
                }
                int codePoint = prefix.readUtf8CodePoint();
                if (Character.isISOControl(codePoint) && !Character.isWhitespace(codePoint)) {
                    return false;
                }
            }
            return true;
        } catch (EOFException e) {
            // Truncated UTF-8 sequence
            return false;
        }
    }

    private boolean bodyHasUnknownEncoding(@NonNull Headers headers) {
        String contentEncoding = headers.get("Content-Encoding");
        return contentEncoding != null && !"identity".equalsIgnoreCase(contentEncoding) && !"gzip".equalsIgnoreCase(contentEncoding);
    }

    private boolean hasBody(@NonNull Response response) {
        // HEAD requests never yield a body regardless of the response headers.
        if ("HEAD".equals(response.request().method())) {
            return false;
        }

        int responseCode = response.code();
        boolean success = (responseCode < 100 || responseCode >= 200) && responseCode != HTTP_NO_CONTENT && responseCode != HTTP_NOT_MODIFIED;
        if (success) {
            return true;
        }

        // If the Content-Length or Transfer-Encoding headers disagree with the response code, the
        // response is malformed. For best compatibility, we honor the headers.
        String contentLength = response.headers().get("Content-Length");
        if (!TextUtils.isEmpty(contentLength) && contentLength.matches("[0-9]*")) {
            //digital content length
            return true;
        }
        return "chunked".equalsIgnoreCase(response.header("Transfer-Encoding"));
    }

}
