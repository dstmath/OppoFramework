package com.oppo.media;

import android.media.MediaHTTPConnection;
import android.media.MediaPlayer;
import android.os.StrictMode;
import android.util.Log;
import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.UnknownServiceException;

public class OppoMediaHTTPConnection {
    private static final int MEDIA_ERROR_HTTP_PROTOCOL_ERROR = -214741;
    private static final String TAG = "MediaHTTPConnection";
    private static final boolean VERBOSE = false;
    MediaHTTPConnection mMediaHTTPConnection;

    public OppoMediaHTTPConnection(MediaHTTPConnection mMediaHTTPConnection2) {
        this.mMediaHTTPConnection = mMediaHTTPConnection2;
    }

    public synchronized int readAt(long offset, byte[] data, int size) {
        int ret;
        ret = readAt_internal(offset, data, size, false);
        if (ret == MEDIA_ERROR_HTTP_PROTOCOL_ERROR) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => protocol error, retry");
            ret = readAt_internal(offset, data, size, true);
        }
        if (ret == MEDIA_ERROR_HTTP_PROTOCOL_ERROR) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => error, convert error");
            ret = MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        }
        return ret;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:21:0x003f, code lost:
        return -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0043, code lost:
        r2 = e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x006d, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x006e, code lost:
        android.util.Log.w(com.oppo.media.OppoMediaHTTPConnection.TAG, "readAt " + r10 + " / " + r13 + " => " + r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0096, code lost:
        return android.media.MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0097, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0098, code lost:
        android.util.Log.w(com.oppo.media.OppoMediaHTTPConnection.TAG, "readAt " + r10 + " / " + r13 + " => " + r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x00c3, code lost:
        return com.oppo.media.OppoMediaHTTPConnection.MEDIA_ERROR_HTTP_PROTOCOL_ERROR;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x003d A[ExcHandler: Exception (e java.lang.Exception), Splitter:B:4:0x0014] */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x006d A[ExcHandler: NoRouteToHostException (r2v3 'e' java.net.NoRouteToHostException A[CUSTOM_DECLARE]), Splitter:B:4:0x0014] */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x0097 A[ExcHandler: ProtocolException (r1v1 'e' java.net.ProtocolException A[CUSTOM_DECLARE]), Splitter:B:4:0x0014] */
    private synchronized int readAt_internal(long offset, byte[] data, int size, boolean forceSeek) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        try {
            if (offset != this.mMediaHTTPConnection.mCurrentOffset || forceSeek) {
                this.mMediaHTTPConnection.seekTo(offset);
            }
            int n = this.mMediaHTTPConnection.mInputStream.read(data, 0, size);
            if (n == -1) {
                n = 0;
            }
            this.mMediaHTTPConnection.mCurrentOffset += (long) n;
            return n;
        } catch (ProtocolException e) {
        } catch (NoRouteToHostException e2) {
        } catch (UnknownServiceException e3) {
            e = e3;
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (IOException e4) {
            return -1;
        } catch (Exception e5) {
        }
    }
}
