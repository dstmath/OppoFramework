package android.media;

import android.content.IntentFilter;
import android.media.IMediaHTTPConnection.Stub;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NoRouteToHostException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.URL;
import java.net.UnknownServiceException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class MediaHTTPConnection extends Stub {
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 20;
    private static final int MEDIA_ERROR_HTTP_PROTOCOL_ERROR = -214741;
    private static final String TAG = "MediaHTTPConnection";
    private static final boolean VERBOSE = false;
    private boolean mAllowCrossDomainRedirect = true;
    private boolean mAllowCrossProtocolRedirect = true;
    private HttpURLConnection mConnection = null;
    private long mCurrentOffset = -1;
    private Map<String, String> mHeaders = null;
    private InputStream mInputStream = null;
    private long mNativeContext;
    private long mTotalSize = -1;
    private URL mURL = null;

    private final native void native_finalize();

    private final native IBinder native_getIMemory();

    private static final native void native_init();

    private final native int native_readAt(long j, int i);

    private final native void native_setup();

    public MediaHTTPConnection() {
        if (CookieHandler.getDefault() == null) {
            Log.w(TAG, "MediaHTTPConnection: Unexpected. No CookieHandler found.");
        }
        native_setup();
    }

    public IBinder connect(String uri, String headers) {
        try {
            disconnect();
            this.mAllowCrossDomainRedirect = true;
            this.mURL = new URL(uri);
            this.mHeaders = convertHeaderStringToMap(headers);
            return native_getIMemory();
        } catch (MalformedURLException e) {
            return null;
        }
    }

    private boolean parseBoolean(String val) {
        boolean z = true;
        try {
            if (Long.parseLong(val) == 0) {
                z = false;
            }
            return z;
        } catch (NumberFormatException e) {
            if (!"true".equalsIgnoreCase(val)) {
                z = "yes".equalsIgnoreCase(val);
            }
            return z;
        }
    }

    private boolean filterOutInternalHeaders(String key, String val) {
        if (!"android-allow-cross-domain-redirect".equalsIgnoreCase(key)) {
            return false;
        }
        this.mAllowCrossDomainRedirect = parseBoolean(val);
        this.mAllowCrossProtocolRedirect = this.mAllowCrossDomainRedirect;
        return true;
    }

    private Map<String, String> convertHeaderStringToMap(String headers) {
        HashMap<String, String> map = new HashMap();
        for (String pair : headers.split("\r\n")) {
            int colonPos = pair.indexOf(":");
            if (colonPos >= 0) {
                String key = pair.substring(0, colonPos);
                String val = pair.substring(colonPos + 1);
                if (!filterOutInternalHeaders(key, val)) {
                    map.put(key, val);
                }
            }
        }
        return map;
    }

    public void disconnect() {
        teardownConnection();
        this.mHeaders = null;
        this.mURL = null;
    }

    private void teardownConnection() {
        if (this.mConnection != null) {
            if (this.mInputStream != null) {
                try {
                    this.mInputStream.close();
                } catch (IOException e) {
                } catch (AssertionError e2) {
                    Log.w(TAG, "teardown connection unknown exception " + e2);
                }
                this.mInputStream = null;
            }
            this.mConnection.disconnect();
            this.mConnection = null;
            this.mCurrentOffset = -1;
        }
    }

    private static final boolean isLocalHost(URL url) {
        if (url == null) {
            return false;
        }
        String host = url.getHost();
        if (host == null) {
            return false;
        }
        try {
            return host.equalsIgnoreCase(ProxyInfo.LOCAL_HOST) || NetworkUtils.numericToInetAddress(host).isLoopbackAddress();
        } catch (IllegalArgumentException e) {
        }
    }

    /* JADX WARNING: Missing block: B:31:0x00f7, code:
            if (r23.mAllowCrossDomainRedirect == false) goto L_0x0109;
     */
    /* JADX WARNING: Missing block: B:32:0x00f9, code:
            r23.mURL = r23.mConnection.getURL();
     */
    /* JADX WARNING: Missing block: B:34:0x010d, code:
            if (r14 != 206) goto L_0x0253;
     */
    /* JADX WARNING: Missing block: B:35:0x010f, code:
            r4 = r23.mConnection.getHeaderField("Content-Range");
            r23.mTotalSize = -1;
     */
    /* JADX WARNING: Missing block: B:36:0x0124, code:
            if (r4 == null) goto L_0x0142;
     */
    /* JADX WARNING: Missing block: B:37:0x0126, code:
            r9 = r4.lastIndexOf(47);
     */
    /* JADX WARNING: Missing block: B:38:0x012e, code:
            if (r9 < 0) goto L_0x0142;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            r23.mTotalSize = java.lang.Long.parseLong(r4.substring(r9 + 1));
     */
    /* JADX WARNING: Missing block: B:87:0x0257, code:
            if (r14 == 200) goto L_0x025f;
     */
    /* JADX WARNING: Missing block: B:89:0x025e, code:
            throw new java.io.IOException();
     */
    /* JADX WARNING: Missing block: B:90:0x025f, code:
            r23.mTotalSize = (long) r23.mConnection.getContentLength();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void seekTo(long offset) throws IOException {
        teardownConnection();
        int redirectCount = 0;
        try {
            URL url = this.mURL;
            boolean noProxy = isLocalHost(url);
            while (true) {
                if (noProxy) {
                    this.mConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                } else {
                    this.mConnection = (HttpURLConnection) url.openConnection();
                }
                this.mConnection.setConnectTimeout(30000);
                this.mConnection.setInstanceFollowRedirects(this.mAllowCrossDomainRedirect);
                if (this.mHeaders != null) {
                    for (Entry<String, String> entry : this.mHeaders.entrySet()) {
                        this.mConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                if (offset > 0) {
                    this.mConnection.setRequestProperty("Range", "bytes=" + offset + "-");
                }
                int response = this.mConnection.getResponseCode();
                if (response != 300 && response != 301 && response != 302 && response != 303 && response != 307) {
                    break;
                }
                redirectCount++;
                if (redirectCount > 20) {
                    throw new NoRouteToHostException("Too many redirects: " + redirectCount);
                }
                String method = this.mConnection.getRequestMethod();
                if (response != 307 || (method.equals("GET") ^ 1) == 0 || (method.equals("HEAD") ^ 1) == 0) {
                    String location = this.mConnection.getHeaderField("Location");
                    if (location == null) {
                        throw new NoRouteToHostException("Invalid redirect");
                    }
                    URL url2 = new URL(this.mURL, location);
                    if (url2.getProtocol().equals(IntentFilter.SCHEME_HTTPS) || (url2.getProtocol().equals(IntentFilter.SCHEME_HTTP) ^ 1) == 0) {
                        boolean sameProtocol = this.mURL.getProtocol().equals(url2.getProtocol());
                        if (this.mAllowCrossProtocolRedirect || (sameProtocol ^ 1) == 0) {
                            boolean sameHost = this.mURL.getHost().equals(url2.getHost());
                            if (!this.mAllowCrossDomainRedirect && (sameHost ^ 1) != 0) {
                                throw new NoRouteToHostException("Cross-domain redirects are disallowed");
                            } else if (response != 307) {
                                this.mURL = url2;
                            }
                        } else {
                            throw new NoRouteToHostException("Cross-protocol redirects are disallowed");
                        }
                    }
                    throw new NoRouteToHostException("Unsupported protocol redirect");
                }
                throw new NoRouteToHostException("Invalid redirect");
            }
            if (offset <= 0 || response == 206) {
                this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
                this.mCurrentOffset = offset;
                return;
            }
            throw new ProtocolException();
        } catch (IOException e) {
            this.mTotalSize = -1;
            teardownConnection();
            this.mCurrentOffset = -1;
            throw e;
        }
    }

    public int readAt(long offset, int size) {
        return native_readAt(offset, size);
    }

    private int readAt(long offset, byte[] data, int size) {
        int ret = readAt_internal(offset, data, size, false);
        if (ret == MEDIA_ERROR_HTTP_PROTOCOL_ERROR) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + "protocol error, retry");
            ret = readAt_internal(offset, data, size, true);
        }
        if (ret != MEDIA_ERROR_HTTP_PROTOCOL_ERROR) {
            return ret;
        }
        Log.w(TAG, "readAt " + offset + " / " + size + " => " + "error, convert error");
        return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
    }

    private int readAt_internal(long offset, byte[] data, int size, boolean forceSeek) {
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        try {
            if (offset != this.mCurrentOffset || forceSeek) {
                seekTo(offset);
            }
            int n = this.mInputStream.read(data, 0, size);
            if (n == -1) {
                n = 0;
            }
            this.mCurrentOffset += (long) n;
            return n;
        } catch (ProtocolException e) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e);
            return MEDIA_ERROR_HTTP_PROTOCOL_ERROR;
        } catch (NoRouteToHostException e2) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e2);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (UnknownServiceException e3) {
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e3);
            return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
        } catch (IOException e4) {
            return -1;
        } catch (Exception e5) {
            return -1;
        }
    }

    public long getSize() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return -1;
            }
        }
        return this.mTotalSize;
    }

    public String getMIMEType() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return "application/octet-stream";
            }
        }
        return this.mConnection.getContentType();
    }

    public String getUri() {
        return this.mURL.toString();
    }

    protected void finalize() {
        native_finalize();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
