package android.media;

import android.content.IntentFilter;
import android.media.IMediaHTTPConnection.Stub;
import android.net.NetworkUtils;
import android.net.ProxyInfo;
import android.os.IBinder;
import android.os.StrictMode;
import android.os.StrictMode.ThreadPolicy.Builder;
import android.provider.ContactsContract.Aas;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
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

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class MediaHTTPConnection extends Stub {
    private static final int CONNECT_TIMEOUT_MS = 30000;
    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int MAX_REDIRECTS = 20;
    private static final String TAG = "MediaHTTPConnection";
    private static final boolean VERBOSE = false;
    private boolean mAllowCrossDomainRedirect;
    private boolean mAllowCrossProtocolRedirect;
    private HttpURLConnection mConnection;
    private long mCurrentOffset;
    private Map<String, String> mHeaders;
    private InputStream mInputStream;
    private long mNativeContext;
    private long mTotalSize;
    private URL mURL;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.media.MediaHTTPConnection.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.media.MediaHTTPConnection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.media.MediaHTTPConnection.<clinit>():void");
    }

    private final native void native_finalize();

    private final native IBinder native_getIMemory();

    private static final native void native_init();

    private final native int native_readAt(long j, int i);

    private final native void native_setup();

    public MediaHTTPConnection() {
        this.mCurrentOffset = -1;
        this.mURL = null;
        this.mHeaders = null;
        this.mConnection = null;
        this.mTotalSize = -1;
        this.mInputStream = null;
        this.mAllowCrossDomainRedirect = true;
        this.mAllowCrossProtocolRedirect = true;
        if (CookieHandler.getDefault() == null) {
            CookieHandler.setDefault(new CookieManager());
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
        Log.d(TAG, "disconnect finish");
    }

    private void teardownConnection() {
        if (this.mConnection != null) {
            Log.d(TAG, "teardownConnection");
            if (this.mInputStream != null) {
                try {
                    this.mInputStream.close();
                } catch (IOException e) {
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

    /* JADX WARNING: Missing block: B:31:0x0104, code:
            if (r25.mAllowCrossDomainRedirect == false) goto L_0x0116;
     */
    /* JADX WARNING: Missing block: B:32:0x0106, code:
            r25.mURL = r25.mConnection.getURL();
     */
    /* JADX WARNING: Missing block: B:34:0x011a, code:
            if (r14 != 206) goto L_0x0256;
     */
    /* JADX WARNING: Missing block: B:35:0x011c, code:
            r4 = r25.mConnection.getHeaderField("Content-Range");
            r25.mTotalSize = -1;
     */
    /* JADX WARNING: Missing block: B:36:0x0131, code:
            if (r4 == null) goto L_0x014f;
     */
    /* JADX WARNING: Missing block: B:37:0x0133, code:
            r9 = r4.lastIndexOf(47);
     */
    /* JADX WARNING: Missing block: B:38:0x013b, code:
            if (r9 < 0) goto L_0x014f;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            r25.mTotalSize = java.lang.Long.parseLong(r4.substring(r9 + 1));
     */
    /* JADX WARNING: Missing block: B:85:0x025a, code:
            if (r14 == 200) goto L_0x0262;
     */
    /* JADX WARNING: Missing block: B:87:0x0261, code:
            throw new java.io.IOException();
     */
    /* JADX WARNING: Missing block: B:89:?, code:
            r25.mTotalSize = java.lang.Long.parseLong(r25.mConnection.getHeaderField("Content-Length"));
     */
    /* JADX WARNING: Missing block: B:93:0x02a0, code:
            r25.mTotalSize = -1;
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
                this.mConnection.setReadTimeout(60000);
                this.mConnection.setInstanceFollowRedirects(this.mAllowCrossDomainRedirect);
                if (this.mHeaders != null) {
                    for (Entry<String, String> entry : this.mHeaders.entrySet()) {
                        this.mConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
                    }
                }
                if (offset > 0) {
                    this.mConnection.setRequestProperty("Range", "bytes=" + offset + Aas.ENCODE_SYMBOL);
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
                if (response != 307 || method.equals("GET") || method.equals("HEAD")) {
                    String location = this.mConnection.getHeaderField("Location");
                    if (location == null) {
                        throw new NoRouteToHostException("Invalid redirect");
                    }
                    URL url2 = new URL(this.mURL, location);
                    if (url2.getProtocol().equals(IntentFilter.SCHEME_HTTPS) || url2.getProtocol().equals(IntentFilter.SCHEME_HTTP)) {
                        boolean sameProtocol = this.mURL.getProtocol().equals(url2.getProtocol());
                        if (this.mAllowCrossProtocolRedirect || sameProtocol) {
                            boolean sameHost = this.mURL.getHost().equals(url2.getHost());
                            if (!this.mAllowCrossDomainRedirect && !sameHost) {
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
            Log.d(TAG, "mTotalSize=" + this.mTotalSize);
            if (offset > 0 || response == 206) {
                this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
                this.mCurrentOffset = offset;
            }
            throw new ProtocolException();
            if (offset > 0) {
            }
            this.mInputStream = new BufferedInputStream(this.mConnection.getInputStream());
            this.mCurrentOffset = offset;
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
        StrictMode.setThreadPolicy(new Builder().permitAll().build());
        try {
            if (offset != this.mCurrentOffset) {
                seekTo(offset);
            }
            if (this.mInputStream == null) {
                return -1;
            }
            int n = this.mInputStream.read(data, 0, size);
            if (n == -1) {
                n = 0;
            }
            this.mCurrentOffset += (long) n;
            return n;
        } catch (ProtocolException e) {
            String msg = e.getMessage();
            Log.w(TAG, "readAt " + offset + " / " + size + " => " + e);
            if (msg == null || msg.indexOf("unexpected end of stream") == -1) {
                return MediaPlayer.MEDIA_ERROR_UNSUPPORTED;
            }
            return -1;
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
}
