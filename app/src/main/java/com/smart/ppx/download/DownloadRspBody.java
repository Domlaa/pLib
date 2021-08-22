package com.smart.ppx.download;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.ForwardingSource;
import okio.Okio;


/**
 *  重写下载体 监听下载进度
 */
public class DownloadRspBody extends ResponseBody {

    private static final String TAG = "Down--->>>";

    private Response originalResponse;
    private DownloadListener downloadListener;
    private long oldPoint = 0;

    public DownloadRspBody(Response originalResponse, long startsPoint, DownloadListener downloadListener){
        this.originalResponse = originalResponse;
        this.downloadListener = downloadListener;
        this.oldPoint = startsPoint;
    }

    @Override
    public MediaType contentType() {
        return originalResponse.body().contentType();
    }

    @Override
    public long contentLength() {
        return originalResponse.body().contentLength();
    }

    @Override
    public BufferedSource source() {
        return Okio.buffer(new ForwardingSource(originalResponse.body().source()) {
            private long bytesReaded = 0;
            @Override
            public long read(Buffer sink, long byteCount) throws IOException {
                long length = contentLength();  //kb
                long bytesRead = super.read(sink, byteCount);
                bytesReaded += bytesRead == -1 ? 0 : bytesRead;
                if (downloadListener != null) {
                    long progress = 100*(bytesReaded+oldPoint)/length;
                    downloadListener.loading((int) progress);
                }
                return bytesRead;
            }
        });
    }

}
