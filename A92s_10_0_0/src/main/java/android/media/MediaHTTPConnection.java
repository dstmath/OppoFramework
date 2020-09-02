package android.media;

import android.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.IntentFilter;
import android.media.IMediaHTTPConnection;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.provider.SettingsStringUtil;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.NativeLibraryHelper;
import com.oppo.media.OppoMediaHTTPConnection;
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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MediaHTTPConnection extends IMediaHTTPConnection.Stub {
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 20;
    private static final String TAG = "MediaHTTPConnection";
    private static final boolean VERBOSE = false;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    private boolean mAllowCrossDomainRedirect = true;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    private boolean mAllowCrossProtocolRedirect = true;
    @UnsupportedAppUsage
    private volatile HttpURLConnection mConnection = null;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    public long mCurrentOffset = -1;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    private Map<String, String> mHeaders = null;
    @GuardedBy({"this"})
    public InputStream mInputStream = null;
    private long mNativeContext;
    private final AtomicInteger mNumDisconnectingThreads = new AtomicInteger(0);
    public OppoMediaHTTPConnection mOppoMediaHTTPConnection = new OppoMediaHTTPConnection(this);
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    private long mTotalSize = -1;
    @UnsupportedAppUsage
    @GuardedBy({"this"})
    private URL mURL = null;

    private final native void native_finalize();

    private final native IBinder native_getIMemory();

    private static final native void native_init();

    private final native int native_readAt(long j, int i);

    private final native void native_setup();

    @UnsupportedAppUsage
    public MediaHTTPConnection() {
        if (CookieHandler.getDefault() == null) {
            Log.w(TAG, "MediaHTTPConnection: Unexpected. No CookieHandler found.");
        }
        native_setup();
    }

    @Override // android.media.IMediaHTTPConnection
    @UnsupportedAppUsage
    public synchronized IBinder connect(String uri, String headers) {
        try {
            disconnect();
            try {
                this.mAllowCrossDomainRedirect = true;
                this.mURL = new URL(uri);
                this.mHeaders = convertHeaderStringToMap(headers);
                return native_getIMemory();
            } catch (MalformedURLException e) {
            }
        } catch (MalformedURLException e2) {
            return null;
        }
    }

    private static boolean parseBoolean(String val) {
        try {
            return Long.parseLong(val) != 0;
        } catch (NumberFormatException e) {
            return "true".equalsIgnoreCase(val) || "yes".equalsIgnoreCase(val);
        }
    }

    private synchronized boolean filterOutInternalHeaders(String key, String val) {
        if (!"android-allow-cross-domain-redirect".equalsIgnoreCase(key)) {
            return false;
        }
        this.mAllowCrossDomainRedirect = parseBoolean(val);
        this.mAllowCrossProtocolRedirect = this.mAllowCrossDomainRedirect;
        return true;
    }

    private synchronized Map<String, String> convertHeaderStringToMap(String headers) {
        HashMap<String, String> map;
        map = new HashMap<>();
        String[] pairs = headers.split("\r\n");
        for (String pair : pairs) {
            int colonPos = pair.indexOf(SettingsStringUtil.DELIMITER);
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

    @Override // android.media.IMediaHTTPConnection
    @UnsupportedAppUsage
    public void disconnect() {
        this.mNumDisconnectingThreads.incrementAndGet();
        try {
            HttpURLConnection connectionToDisconnect = this.mConnection;
            if (connectionToDisconnect != null) {
                connectionToDisconnect.disconnect();
            }
            synchronized (this) {
                teardownConnection();
                this.mHeaders = null;
                this.mURL = null;
            }
        } finally {
            this.mNumDisconnectingThreads.decrementAndGet();
        }
    }

    private synchronized void teardownConnection() {
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
        String host;
        if (url == null || (host = url.getHost()) == null) {
            return false;
        }
        try {
            if (!host.equalsIgnoreCase(ProxyInfo.LOCAL_HOST) && !NetworkUtils.numericToInetAddress(host).isLoopbackAddress()) {
                return false;
            }
            return true;
        } catch (IllegalArgumentException e) {
        }
    }

    public synchronized void seekTo(long offset) throws IOException {
        int lastSlashPos;
        teardownConnection();
        long j = -1;
        try {
            URL url = this.mURL;
            boolean noProxy = isLocalHost(url);
            int redirectCount = 0;
            while (this.mNumDisconnectingThreads.get() <= 0) {
                if (noProxy) {
                    this.mConnection = (HttpURLConnection) url.openConnection(Proxy.NO_PROXY);
                } else {
                    this.mConnection = (HttpURLConnection) url.openConnection();
                }
                if (this.mNumDisconnectingThreads.get() <= 0) {
                    this.mConnection.setConnectTimeout(30000);
                    this.mConnection.setInstanceFollowRedirects(this.mAllowCrossDomainRedirect);
                    if (this.mHeaders != null) {
                        for (Map.Entry<String, String> entry : this.mHeaders.entrySet()) {
                            this.mConnection.setRequestProperty(entry.getKey(), entry.getValue());
                        }
                    }
                    if (offset > 0) {
                        this.mConnection.setRequestProperty("Range", "bytes=" + offset + NativeLibraryHelper.CLEAR_ABI_OVERRIDE);
                    }
                    int response = this.mConnection.getResponseCode();
                    if (response == 300 || response == 301 || response == 302 || response == 303 || response == 307) {
                        redirectCount++;
                        if (redirectCount <= 20) {
                            String method = this.mConnection.getRequestMethod();
                            if (response != 307 || method.equals("GET") || method.equals("HEAD")) {
                                String location = this.mConnection.getHeaderField("Location");
                                if (location != null) {
                                    url = new URL(this.mURL, location);
                                    if (url.getProtocol().equals(IntentFilter.SCHEME_HTTPS) || url.getProtocol().equals(IntentFilter.SCHEME_HTTP)) {
                                        boolean sameProtocol = this.mURL.getProtocol().equals(url.getProtocol());
                                        if (this.mAllowCrossProtocolRedirect || sameProtocol) {
                                            boolean sameHost = this.mURL.getHost().equals(url.getHost());
                                            if (this.mAllowCrossDomainRedirect || sameHost) {
                                                if (response != 307) {
                                                    this.mURL = url;
                                                }
                                                j = -1;
                                            } else {
                                                throw new NoRouteToHostException("Cross-domain redirects are disallowed");
                                            }
                                        } else {
                                            throw new NoRouteToHostException("Cross-protocol redirects are disallowed");
                                        }
                                    } else {
                                        throw new NoRouteToHostException("Unsupported protocol redirect");
                                    }
                                } else {
                                    throw new NoRouteToHostException("Invalid redirect");
                                }
                            } else {
                                throw new NoRouteToHostException("Invalid redirect");
                            }
                        } else {
                            throw new NoRouteToHostException("Too many redirects: " + redirectCount);
                        }
                    } else {
                        if (this.mAllowCrossDomainRedirect) {
                            this.mURL = this.mConnection.getURL();
                        }
                        if (response == 206) {
                            String contentRange = this.mConnection.getHeaderField("Content-Range");
                            this.mTotalSize = j;
                            if (contentRange != null && (lastSlashPos = contentRange.lastIndexOf(47)) >= 0) {
                                try {
                                    this.mTotalSize = Long.parseLong(contentRange.substring(lastSlashPos + 1));
                                } catch (NumberFormatException e) {
                                }
                            }
                        } else if (response == 200) {
                            this.mTotalSize = (long) this.mConnection.getContentLength();
                        } else {
                            throw new IOException();
                        }
                        if (offset <= 0 || response == 206) {
                            this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
                            this.mCurrentOffset = offset;
                        } else {
                            throw new ProtocolException();
                        }
                    }
                } else {
                    throw new IOException("concurrently disconnecting");
                }
            }
            throw new IOException("concurrently disconnecting");
        } catch (IOException e2) {
            this.mTotalSize = -1;
            teardownConnection();
            this.mCurrentOffset = -1;
            throw e2;
        }
    }

    @Override // android.media.IMediaHTTPConnection
    @UnsupportedAppUsage
    public synchronized int readAt(long offset, int size) {
        return native_readAt(offset, size);
    }

    private synchronized int readAt(long offset, byte[] data, int size) {
        return this.mOppoMediaHTTPConnection.readAt(offset, data, size);
    }

    @Override // android.media.IMediaHTTPConnection
    public synchronized long getSize() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return -1;
            }
        }
        return this.mTotalSize;
    }

    @Override // android.media.IMediaHTTPConnection
    @UnsupportedAppUsage
    public synchronized String getMIMEType() {
        if (this.mConnection == null) {
            try {
                seekTo(0);
            } catch (IOException e) {
                return ContentResolver.MIME_TYPE_DEFAULT;
            }
        }
        return this.mConnection.getContentType();
    }

    @Override // android.media.IMediaHTTPConnection
    @UnsupportedAppUsage
    public synchronized String getUri() {
        return this.mURL.toString();
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        native_finalize();
    }

    static {
        System.loadLibrary("media_jni");
        native_init();
    }
}
