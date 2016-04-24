package com.lchli.loaderlibrary.okhttpWraper;

import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingSink;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

/**
 * Created by lchli on 2016/3/20.
 */
public class OkHttpFileTransportUtils {

    private static final String tag = OkHttpFileTransportUtils.class.getSimpleName();


    public interface FileTransportCallback {
        void onFailure(Request request, IOException e);

        void onResponse(Response response);

        void onProgress(long currentBytes, long contentLength, boolean done);
    }


    private static void checkMustInUiThread() {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            throw new IllegalAccessError("this method must  call in ui thread!!");
        }
    }

    public static Call upload(final String url, Map<String, String> textParams, Map<String, File> fileParams, final FileTransportCallback callback) {
        checkMustInUiThread();
        final MultipartBody.Builder multipartBuilder = new MultipartBody.Builder();
        multipartBuilder.setType(MultipartBody.FORM);

        Map<String, String> totalParams = OkhttpAppConfig.filterParams(textParams);

        Iterator<Map.Entry<String, String>> textParamsIter = totalParams.entrySet().iterator();
        while (textParamsIter.hasNext()) {
            Map.Entry<String, String> entry = textParamsIter.next();
            multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue());
        }

        if (fileParams != null && !fileParams.isEmpty()) {
            Iterator<Map.Entry<String, File>> fileParamsIter = fileParams.entrySet().iterator();
            while (fileParamsIter.hasNext()) {
                Map.Entry<String, File> entry = fileParamsIter.next();
                multipartBuilder.addFormDataPart(entry.getKey(), entry.getValue().getName(), RequestBody.create(MediaType.parse("application/octet-stream"), entry.getValue()));
            }
        }
        final RequestBody requestBody = multipartBuilder.build();
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                String log = String.format("[%s]upload file progress:%d%% done\n", url, (100 * bytesRead) / contentLength);
                OkhttpAppConfig.e(tag, log);
                callback.onProgress(bytesRead, contentLength, done);
            }
        };
        final Request request = OkhttpAppConfig.newCommonRequestBuilder().url(url).post(new ProgressRequestBody(requestBody, progressListener)).build();
        OkHttpClient client = OkhttpAppConfig.newOkHttpClientBuilder().build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(request, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(response);
            }

        });
        return call;
    }


    public static Call download(final String url, final FileTransportCallback callback, final File saveFile) {
        checkMustInUiThread();
        final Request request = OkhttpAppConfig.newCommonRequestBuilder()
                .url(url)
                .build();
        final ProgressListener progressListener = new ProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
                OkhttpAppConfig.e(tag, "download file progress:" + (100 * bytesRead) / contentLength);
                callback.onProgress(bytesRead, contentLength, done);
            }
        };
        OkHttpClient client = OkhttpAppConfig.newOkHttpClientBuilder()
                .addNetworkInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        return originalResponse.newBuilder()
                                .body(new ProgressResponseBody(originalResponse.body(), progressListener, saveFile))
                                .build();
                    }
                })
                .build();
        Call call = client.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call.request(), e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(response);
            }
        });
        return call;
    }


    private static class ProgressResponseBody extends ResponseBody {

        private final ResponseBody responseBody;
        private final ProgressListener progressListener;
        private final File saveFile;
        private BufferedSource bufferedSource;

        public ProgressResponseBody(ResponseBody responseBody, ProgressListener progressListener, File saveFile) {
            this.responseBody = responseBody;
            this.progressListener = progressListener;
            this.saveFile = saveFile;
        }

        @Override
        public MediaType contentType() {
            return responseBody.contentType();
        }

        @Override
        public long contentLength() {
            return responseBody.contentLength();
        }

        @Override
        public BufferedSource source() {
            if (bufferedSource == null) {
                bufferedSource = Okio.buffer(source(responseBody.source()));
            }
            return bufferedSource;
        }

        private Source source(final Source source) {

            return new ForwardingSource(source) {
                long totalBytesRead = 0L;
                FileOutputStream fos;

                @Override
                public long read(Buffer sink, long byteCount) throws IOException {
                    if (fos == null) {
                        fos = new FileOutputStream(saveFile);
                    }
                    long bytesRead = super.read(sink, byteCount);
                    boolean isdone = bytesRead == -1;
                    if (!isdone) {
                        sink.writeTo(fos, bytesRead);
                        // read() returns the number of bytes read, or -1 if this source is exhausted.write file here.
                        totalBytesRead += bytesRead;
                    } else {
                        fos.close();
                    }
                    progressListener.update(totalBytesRead, responseBody.contentLength(), isdone);
                    return bytesRead;
                }
            };
        }
    }


    private static class ProgressRequestBody extends RequestBody {
        //实际的待包装请求体
        private final RequestBody requestBody;
        //进度回调接口
        private final ProgressListener progressListener;
        //包装完成的BufferedSink
        private BufferedSink bufferedSink;

        /**
         * 构造函数，赋值
         *
         * @param requestBody      待包装的请求体
         * @param progressListener 回调接口
         */
        public ProgressRequestBody(RequestBody requestBody, ProgressListener progressListener) {
            this.requestBody = requestBody;
            this.progressListener = progressListener;
        }

        /**
         * 重写调用实际的响应体的contentType
         *
         * @return MediaType
         */
        @Override
        public MediaType contentType() {
            return requestBody.contentType();
        }

        /**
         * 重写调用实际的响应体的contentLength
         *
         * @return contentLength
         * @throws IOException 异常
         */
        @Override
        public long contentLength() throws IOException {
            return requestBody.contentLength();
        }

        /**
         * 重写进行写入
         *
         * @param sink BufferedSink
         * @throws IOException 异常
         */
        @Override
        public void writeTo(BufferedSink sink) throws IOException {
            if (bufferedSink == null) {
                //包装
                bufferedSink = Okio.buffer(sink(sink));
            }
            //写入
            requestBody.writeTo(bufferedSink);
            //必须调用flush，否则最后一部分数据可能不会被写入
            bufferedSink.flush();

        }

        /**
         * 写入，回调进度接口
         *
         * @param sink Sink
         * @return Sink
         */
        private Sink sink(Sink sink) {
            return new ForwardingSink(sink) {
                //当前写入字节数
                long bytesWritten = 0L;
                //总字节长度，避免多次调用contentLength()方法
                long contentLength = 0L;

                @Override
                public void write(Buffer source, long byteCount) throws IOException {
                    super.write(source, byteCount);
                    if (contentLength == 0) {
                        //获得contentLength的值，后续不再调用
                        contentLength = contentLength();
                    }
                    //增加当前写入的字节数
                    bytesWritten += byteCount;
                    //回调
                    if (progressListener != null) {
                        progressListener.update(bytesWritten, contentLength, bytesWritten == contentLength);
                    }
                }
            };
        }
    }


    interface ProgressListener {
        void update(long bytesRead, long contentLength, boolean done);
    }
}
