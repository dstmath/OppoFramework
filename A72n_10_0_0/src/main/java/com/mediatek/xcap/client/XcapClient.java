package com.mediatek.xcap.client;

import android.content.Context;
import android.net.Network;
import android.os.Build;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import com.android.okhttp.CertificatePinner;
import com.android.okhttp.Dns;
import com.android.okhttp.Headers;
import com.android.okhttp.Interceptor;
import com.android.okhttp.MediaType;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.Request;
import com.android.okhttp.RequestBody;
import com.android.okhttp.Response;
import com.mediatek.gba.GbaManager;
import com.mediatek.gba.NafSessionKey;
import com.mediatek.internal.telephony.MtkSubscriptionManager;
import com.mediatek.xcap.auth.AkaDigestAuth;
import com.mediatek.xcap.header.WwwAuthHeader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class XcapClient {
    private static final String AUTH_HDR = "WWW-Authenticate";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_GET = "GET";
    public static final String METHOD_PUT = "PUT";
    private static final String[] PROPERTY_RIL_TEST_SIM = {"vendor.gsm.sim.ril.testsim", "vendor.gsm.sim.ril.testsim.2", "vendor.gsm.sim.ril.testsim.3", "vendor.gsm.sim.ril.testsim.4"};
    private static final String PROP_FORCE_DEBUG_KEY = "persist.vendor.log.tag.tel_dbg";
    private static final boolean SENLOG = TextUtils.equals(Build.TYPE, "user");
    private static final int SOCKET_OPERATION_TIMEOUT = 30;
    private static final int SOCKET_READ_OPERATION_TIMEOUT = 30;
    private static final String TAG = "XcapClient";
    private static final boolean TELDBG;
    protected static final char[] hexArray = "0123456789abcdef".toCharArray();
    private static Map<Integer, String> mNafFqdnCache = new HashMap();
    private static int mRequestCount = 0;
    private Context mContext;
    private XcapDebugParam mDebugParam;
    private GbaManager mGbaManager;
    private HostnameVerifier mHostnameVerifier;
    private Network mNetwork;
    private OkHttpClient mOkHttpClient;
    private int mPhoneId;
    private Request mRequest;
    private Response mResponse;
    private TrustManager[] mTrustAllCerts;
    private String mUserAgent;

    static {
        boolean z = true;
        if (SystemProperties.getInt(PROP_FORCE_DEBUG_KEY, 0) != 1) {
            z = false;
        }
        TELDBG = z;
    }

    public XcapClient(Context context, int phoneId) {
        this.mDebugParam = XcapDebugParam.getInstance();
        this.mTrustAllCerts = new TrustManager[]{new X509TrustManager() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass1 */

            @Override // javax.net.ssl.X509TrustManager
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        this.mHostnameVerifier = new HostnameVerifier() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass2 */

            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        this.mContext = context;
        this.mPhoneId = phoneId;
        composeUserAgent();
        Log.i(TAG, "XcapClient context: " + context + " phoneId:" + phoneId);
        initialize();
    }

    public XcapClient(Context context, String userAgent, int phoneId) {
        this.mDebugParam = XcapDebugParam.getInstance();
        this.mTrustAllCerts = new TrustManager[]{new X509TrustManager() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass1 */

            @Override // javax.net.ssl.X509TrustManager
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        this.mHostnameVerifier = new HostnameVerifier() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass2 */

            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mUserAgent = userAgent;
        Log.i(TAG, "XcapClient context: " + context + " phoneId:" + phoneId);
        initialize();
    }

    public XcapClient(Context context, Network network, int phoneId) {
        this.mDebugParam = XcapDebugParam.getInstance();
        this.mTrustAllCerts = new TrustManager[]{new X509TrustManager() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass1 */

            @Override // javax.net.ssl.X509TrustManager
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        this.mHostnameVerifier = new HostnameVerifier() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass2 */

            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        this.mContext = context;
        this.mPhoneId = phoneId;
        composeUserAgent();
        Log.i(TAG, "XcapClient context: " + context + " phoneId:" + phoneId);
        if (network != null) {
            this.mNetwork = network;
        }
        initialize();
    }

    public XcapClient(Context context, String userAgent, Network network, int phoneId) {
        this.mDebugParam = XcapDebugParam.getInstance();
        this.mTrustAllCerts = new TrustManager[]{new X509TrustManager() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass1 */

            @Override // javax.net.ssl.X509TrustManager
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            @Override // javax.net.ssl.X509TrustManager
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }

            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        this.mHostnameVerifier = new HostnameVerifier() {
            /* class com.mediatek.xcap.client.XcapClient.AnonymousClass2 */

            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        this.mContext = context;
        this.mPhoneId = phoneId;
        this.mUserAgent = userAgent;
        Log.i(TAG, "XcapClient context: " + context + " phoneId:" + phoneId);
        if (network != null) {
            this.mNetwork = network;
        }
        initialize();
    }

    private void composeUserAgent() {
        boolean isGbaEnabled = SENLOG;
        if (ServiceManager.getService("GbaService") != null) {
            Log.i(TAG, "GbaService Enabled");
            isGbaEnabled = true;
        }
        if (this.mDebugParam.getXcapUserAgent() == null || this.mDebugParam.getXcapUserAgent().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("XCAP Client");
            sb.append(isGbaEnabled ? " 3gpp-gba" : "");
            this.mUserAgent = sb.toString();
            return;
        }
        this.mUserAgent = this.mDebugParam.getXcapUserAgent();
    }

    private void initialize() {
        this.mGbaManager = GbaManager.getDefaultGbaManager(this.mContext);
        this.mDebugParam.load();
    }

    public void shutdown() {
    }

    private Request addExtraHeaders(Request request, Headers rawHeaders) {
        if (rawHeaders == null) {
            return request;
        }
        for (String name : rawHeaders.names()) {
            Iterator<String> it = rawHeaders.values(name).iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                String value = it.next();
                if (!(name.isEmpty() || value.isEmpty())) {
                    Log.d(TAG, "name: " + name + ", value: " + encryptString(value));
                    request = request.newBuilder().addHeader(name, value).build();
                    break;
                }
            }
        }
        return request;
    }

    private void logRequestHeaders(Request request) {
        Map<String, List<String>> headerFields = request.headers().toMultimap();
        Log.d(TAG, "Request Headers:");
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (values != null) {
                for (String value : values) {
                    if (!SENLOG) {
                        Log.d(TAG, key + ": " + encryptString(value));
                    }
                }
            }
        }
    }

    private void logResponseHeaders(Response response) {
        Map<String, List<String>> headerFields = response.headers().toMultimap();
        Log.d(TAG, "Response Headers:");
        for (Map.Entry<String, List<String>> entry : headerFields.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            if (values != null) {
                for (String value : values) {
                    if (!SENLOG) {
                        Log.d(TAG, key + ": " + value);
                    }
                }
            }
        }
    }

    private byte[] getNafSecureProtocolId(boolean isTlsEnabled, String cipher) {
        StringBuilder sb = new StringBuilder();
        sb.append("getNafSecureProtocolId: protocol=");
        sb.append(isTlsEnabled ? "https" : "http");
        sb.append(", isTlsEnabled=");
        sb.append(isTlsEnabled);
        sb.append(", cipher = ");
        sb.append(cipher);
        Log.d(TAG, sb.toString());
        return this.mGbaManager.getNafSecureProtocolId(isTlsEnabled, cipher);
    }

    private void handle401Exception(Response response, boolean isHttps, String cipher) throws IOException {
        String auth_header = response.header("WWW-Authenticate");
        mRequestCount = 1;
        if (auth_header != null) {
            WwwAuthHeader wwwAuthHeader = WwwAuthHeader.parse(auth_header);
            Log.d(TAG, "handle401Exception: wwwAuthHeader=" + wwwAuthHeader);
            byte[] uaId = getNafSecureProtocolId(isHttps, cipher);
            if (!SENLOG) {
                for (int j = 0; j < uaId.length; j++) {
                    Log.d(TAG, "uaId[" + j + "] = " + String.format("0x%02x", Byte.valueOf(uaId[j])));
                }
            }
            String realm = wwwAuthHeader.getRealm();
            if (realm.length() > 0) {
                String[] segments = realm.split(";");
                String nafFqdn = segments[0].substring(segments[0].indexOf("@") + 1);
                Log.d(TAG, "handle401Exception: nafFqdn=" + nafFqdn + ", mPhoneId=" + this.mPhoneId);
                mNafFqdnCache.put(Integer.valueOf(this.mPhoneId), nafFqdn);
                int subId = MtkSubscriptionManager.getSubIdUsingPhoneId(this.mPhoneId);
                NafSessionKey nafSessionKey = this.mGbaManager.runGbaAuthentication(nafFqdn, uaId, true, subId);
                if (nafSessionKey == null || nafSessionKey.getKey() == null) {
                    Log.e(TAG, "handle401Exception: nafSessionKey Error!");
                    if (nafSessionKey != null && nafSessionKey.getException() != null && (nafSessionKey.getException() instanceof IllegalStateException)) {
                        String msg = ((IllegalStateException) nafSessionKey.getException()).getMessage();
                        if ("HTTP 403 Forbidden".equals(msg)) {
                            Log.i(TAG, "GBA hit 403");
                            throw new IOException("HTTP 403 Forbidden");
                        } else if ("HTTP 400 Bad Request".equals(msg)) {
                            Log.i(TAG, "GBA hit 400");
                            throw new IOException("HTTP 400 Bad Request");
                        }
                    }
                } else {
                    nafSessionKey.setAuthHeader(auth_header);
                    this.mGbaManager.updateCachedKey(nafFqdn, uaId, subId, nafSessionKey);
                    Log.d(TAG, "handle401Exception: nafSessionKey=" + nafSessionKey);
                }
            } else {
                Log.e(TAG, "handle401Exception: realm is empty string !!!");
            }
        } else {
            Log.e(TAG, "handle401Exception: authentication header has something wrong");
        }
    }

    private String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[(bytes.length * 2)];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 255;
            char[] cArr = hexArray;
            hexChars[j * 2] = cArr[v >>> 4];
            hexChars[(j * 2) + 1] = cArr[v & 15];
        }
        return new String(hexChars);
    }

    private AkaDigestAuth getAkaDigestAuth(Request request, String method, String content) {
        NafSessionKey nafSessionKey = null;
        WwwAuthHeader wwwAuthHeader = null;
        String nafFqdn = mNafFqdnCache.get(Integer.valueOf(this.mPhoneId));
        byte[] uaId = getNafSecureProtocolId(request.isHttps(), "");
        if (!SENLOG) {
            for (int j = 0; j < uaId.length; j++) {
                Log.d(TAG, "uaId[" + j + "] = " + String.format("0x%02x", Byte.valueOf(uaId[j])));
            }
        }
        int subId = MtkSubscriptionManager.getSubIdUsingPhoneId(this.mPhoneId);
        if (!"1".equals(SystemProperties.get(PROPERTY_RIL_TEST_SIM[this.mPhoneId], "0")) || mRequestCount <= 1) {
            if (nafFqdn != null) {
                nafSessionKey = this.mGbaManager.getCachedKey(nafFqdn, uaId, subId);
                Log.d(TAG, "getAkaDigestAuth: nafFqdn=" + nafFqdn + ", mPhoneId=" + this.mPhoneId + "nafSessionKey=" + nafSessionKey);
                if (!(nafSessionKey == null || nafSessionKey.getAuthHeader() == null)) {
                    wwwAuthHeader = WwwAuthHeader.parse(nafSessionKey.getAuthHeader());
                    Log.d(TAG, "getAkaDigestAuth: wwwAuthHeader=" + wwwAuthHeader);
                }
            }
            if (nafSessionKey == null || wwwAuthHeader == null) {
                return null;
            }
            String password = bytesToHex(Base64.encode(nafSessionKey.getKey(), 2));
            String nc = String.format("%08x", Integer.valueOf(mRequestCount));
            StringBuilder sb = new StringBuilder();
            sb.append("getAkaDigestAuth: password=");
            sb.append(password);
            sb.append(", nc=");
            sb.append(nc);
            sb.append(", url=");
            sb.append(!SENLOG ? request.url() : "[hidden]");
            Log.d(TAG, sb.toString());
            return new AkaDigestAuth(wwwAuthHeader, nafSessionKey.getBtid(), null, password, request.url().getPath(), nc, method, content);
        }
        Log.d(TAG, "getAkaDigestAuth: force to run gba");
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:84:0x02b2, code lost:
        if (1 != 0) goto L_0x03ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:86:0x02b6, code lost:
        if ((r6 - 1) <= 0) goto L_0x03ac;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:87:0x02b8, code lost:
        java.lang.Thread.sleep(5000);
        android.util.Log.d(com.mediatek.xcap.client.XcapClient.TAG, "retry once");
     */
    /* JADX WARNING: Removed duplicated region for block: B:113:0x02fa A[Catch:{ all -> 0x0378 }] */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x0307 A[Catch:{ all -> 0x0378 }] */
    private Response execute(URL url, String method, byte[] xml, Headers additionalRequestHeaders, String mimetype) throws IOException {
        MalformedURLException e;
        MalformedURLException e2;
        ProtocolException e3;
        boolean isTrustAll;
        boolean z;
        IOException e4;
        MalformedURLException e5;
        ProtocolException e6;
        IOException e7;
        MalformedURLException th;
        String str = method;
        byte[] bArr = xml;
        int tryCount = 3;
        boolean isTrustAll2 = this.mDebugParam.getEnableXcapTrustAll();
        this.mOkHttpClient = new OkHttpClient();
        this.mOkHttpClient.networkInterceptors().add(new RequestInterceptor());
        this.mOkHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);
        this.mOkHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        Network network = this.mNetwork;
        if (network != null) {
            this.mOkHttpClient.setSocketFactory(network.getSocketFactory());
            Log.d(TAG, "mOkHttpClient using dedicated network = " + this.mNetwork);
            NetworkDns.getInstance().setNetwork(this.mNetwork);
            this.mOkHttpClient.setDns(NetworkDns.getInstance());
        }
        if (isTrustAll2) {
            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, this.mTrustAllCerts, new SecureRandom());
                this.mOkHttpClient.setSslSocketFactory(sc.getSocketFactory());
                this.mOkHttpClient.setHostnameVerifier(this.mHostnameVerifier);
                Log.d(TAG, "mOkHttpClient set SSL");
            } catch (GeneralSecurityException se) {
                Log.e(TAG, "Execute TrustAll exception");
                se.printStackTrace();
            }
        }
        Log.d(TAG, "mOkHttpClient = " + this.mOkHttpClient);
        mRequestCount = mRequestCount + 1;
        while (true) {
            if (tryCount > 0 && 0 == 0) {
                try {
                    Log.d(TAG, str + " :" + encryptString(url.toString()));
                    try {
                        this.mRequest = new Request.Builder().url(url).header("Accept", "*/*").header("User-Agent", this.mUserAgent).build();
                        this.mRequest = addExtraHeaders(this.mRequest, additionalRequestHeaders);
                        this.mRequest = this.mRequest.newBuilder().removeHeader("Accept-Encoding").build();
                        AkaDigestAuth akaDigestAuth = getAkaDigestAuth(this.mRequest, str, bArr == null ? "" : new String(bArr));
                        if (akaDigestAuth != null) {
                            akaDigestAuth.calculateRequestDigest();
                            isTrustAll = isTrustAll2;
                            try {
                                this.mRequest = this.mRequest.newBuilder().addHeader(XcapConstants.HDR_KEY_AUTHORIZATION, akaDigestAuth.createAuthorHeaderValue()).build();
                            } catch (MalformedURLException e8) {
                                e2 = e8;
                                Log.e(TAG, "MalformedURLException");
                                e2.printStackTrace();
                                throw e2;
                            } catch (ProtocolException e9) {
                                e3 = e9;
                                Log.e(TAG, "ProtocolException");
                                e3.printStackTrace();
                                throw e3;
                            } catch (IOException e10) {
                                e4 = e10;
                                try {
                                    e4.printStackTrace();
                                    if (e4 instanceof SocketTimeoutException) {
                                    }
                                    try {
                                        Thread.sleep(5000);
                                        Log.d(TAG, "retry once");
                                    } catch (InterruptedException e11) {
                                        Log.d(TAG, "InterruptedException");
                                        e11.printStackTrace();
                                    }
                                    str = method;
                                    bArr = xml;
                                    isTrustAll2 = isTrustAll;
                                } catch (Throwable th2) {
                                    e = th2;
                                    try {
                                        Thread.sleep(5000);
                                        Log.d(TAG, "retry once");
                                    } catch (InterruptedException e12) {
                                        Log.d(TAG, "InterruptedException");
                                        e12.printStackTrace();
                                    }
                                    throw e;
                                }
                            }
                        } else {
                            isTrustAll = isTrustAll2;
                        }
                        logRequestHeaders(this.mRequest);
                        if (METHOD_PUT.equals(str)) {
                            Log.d(TAG, "METHOD_PUT");
                            this.mRequest = this.mRequest.newBuilder().put(RequestBody.create(MediaType.parse(mimetype), bArr)).build();
                        } else if (METHOD_GET.equals(str)) {
                            Log.d(TAG, "METHOD_GET");
                            this.mRequest = this.mRequest.newBuilder().build();
                        }
                        Log.d(TAG, "newCall execute");
                        this.mResponse = this.mOkHttpClient.newCall(this.mRequest).execute();
                        int responseCode = this.mResponse.code();
                        Log.d(TAG, "HTTP: " + responseCode + " " + this.mResponse.message());
                        String cipher = "";
                        try {
                            cipher = this.mResponse.handshake().cipherSuite();
                            Log.d(TAG, "cipherSuite: " + cipher);
                        } catch (Exception e13) {
                            Log.e(TAG, "No handshake stage");
                            cipher = cipher;
                        }
                        try {
                            for (Iterator it = this.mResponse.handshake().peerCertificates().iterator(); it.hasNext(); it = it) {
                                Log.d(TAG, "certificate: " + CertificatePinner.pin((Certificate) it.next()));
                            }
                        } catch (Exception e14) {
                            Log.e(TAG, "No certificate stage");
                        }
                        logResponseHeaders(this.mResponse);
                        if (responseCode != 200 && responseCode != 403 && responseCode != 304 && responseCode != 412 && responseCode != 201) {
                            if (responseCode != 409) {
                                if (responseCode == 401) {
                                    System.setProperty("gba.auth", "401");
                                    Log.d(TAG, "HTTP status code is 401. Force to run GBA");
                                    handle401Exception(this.mResponse, this.mRequest.isHttps(), cipher);
                                } else {
                                    Log.d(TAG, "HTTP status code is not 200 or 403 or 409");
                                }
                                if (0 == 0) {
                                    tryCount--;
                                    if (tryCount > 0) {
                                        try {
                                            Thread.sleep(5000);
                                            Log.d(TAG, "retry once");
                                        } catch (InterruptedException e15) {
                                            Log.d(TAG, "InterruptedException");
                                            e15.printStackTrace();
                                        }
                                    }
                                    z = SENLOG;
                                } else {
                                    z = SENLOG;
                                }
                                str = method;
                                bArr = xml;
                                isTrustAll2 = isTrustAll;
                            } else if (1 == 0 && tryCount - 1 > 0) {
                                try {
                                    Thread.sleep(5000);
                                    Log.d(TAG, "retry once");
                                } catch (InterruptedException e16) {
                                    Log.d(TAG, "InterruptedException");
                                    e16.printStackTrace();
                                }
                            }
                        }
                    } catch (MalformedURLException e17) {
                        e5 = e17;
                        e2 = e5;
                        Log.e(TAG, "MalformedURLException");
                        e2.printStackTrace();
                        throw e2;
                    } catch (ProtocolException e18) {
                        e6 = e18;
                        e3 = e6;
                        Log.e(TAG, "ProtocolException");
                        e3.printStackTrace();
                        throw e3;
                    } catch (IOException e19) {
                        e7 = e19;
                        isTrustAll = isTrustAll2;
                        e4 = e7;
                        e4.printStackTrace();
                        if (e4 instanceof SocketTimeoutException) {
                        }
                        Thread.sleep(5000);
                        Log.d(TAG, "retry once");
                        str = method;
                        bArr = xml;
                        isTrustAll2 = isTrustAll;
                    } catch (Throwable th3) {
                        th = th3;
                        e = th;
                        Thread.sleep(5000);
                        Log.d(TAG, "retry once");
                        throw e;
                    }
                } catch (MalformedURLException e20) {
                    e5 = e20;
                    e2 = e5;
                    Log.e(TAG, "MalformedURLException");
                    e2.printStackTrace();
                    throw e2;
                } catch (ProtocolException e21) {
                    e6 = e21;
                    e3 = e6;
                    Log.e(TAG, "ProtocolException");
                    e3.printStackTrace();
                    throw e3;
                } catch (IOException e22) {
                    e7 = e22;
                    isTrustAll = isTrustAll2;
                    e4 = e7;
                    e4.printStackTrace();
                    if (e4 instanceof SocketTimeoutException) {
                        if (tryCount - 1 > 0) {
                            Log.d(TAG, "SocketTimeoutException: wait for retry.");
                            z = SENLOG;
                        } else {
                            throw e4;
                        }
                    } else if (e4 instanceof UnknownHostException) {
                        if (tryCount - 1 > 0) {
                            Log.d(TAG, "Trying to use default system DNS");
                            NetworkDns instance = NetworkDns.getInstance();
                            z = SENLOG;
                            instance.setNetwork(null);
                            this.mOkHttpClient.setDns(NetworkDns.getInstance());
                        } else {
                            Log.e(TAG, "Throw UnknownHostException");
                            throw e4;
                        }
                    } else if ("HTTP 403 Forbidden".equals(e4.getMessage())) {
                        throw new IOException("GBA hit HTTP 403 Forbidden");
                    } else if ("HTTP 400 Bad Request".equals(e4.getMessage())) {
                        throw new IOException("GBA hit HTTP 400 Bad Request");
                    } else {
                        throw e4;
                    }
                    if (0 == 0 && tryCount - 1 > 0) {
                        Thread.sleep(5000);
                        Log.d(TAG, "retry once");
                    }
                    str = method;
                    bArr = xml;
                    isTrustAll2 = isTrustAll;
                } catch (Throwable th4) {
                    th = th4;
                    e = th;
                    if (0 == 0 && tryCount - 1 > 0) {
                        Thread.sleep(5000);
                        Log.d(TAG, "retry once");
                    }
                    throw e;
                }
            }
        }
        return this.mResponse;
    }

    public Response get(URI uri, Headers additionalRequestHeaders) throws IOException {
        return execute(uri.toURL(), METHOD_GET, null, additionalRequestHeaders, null);
    }

    public Response put(URI uri, String mimetype, String content) throws IOException {
        Log.d(TAG, "PUT: " + encryptString(content));
        return put(uri, mimetype, content.getBytes("UTF-8"), (Headers) null, (String) null, (String) null);
    }

    public Response put(URI uri, String mimetype, String content, Headers additionalRequestHeaders) throws IOException {
        Log.d(TAG, "PUT: " + encryptString(content));
        return put(uri, mimetype, content.getBytes("UTF-8"), additionalRequestHeaders, (String) null, (String) null);
    }

    public Response put(URI uri, String mimetype, String content, Headers additionalRequestHeaders, String eTag, String condition) throws IOException {
        Log.d(TAG, "PUT: " + encryptString(content));
        return put(uri, mimetype, content.getBytes("UTF-8"), additionalRequestHeaders, eTag, condition);
    }

    public Response put(URI uri, String mimetype, byte[] content, Headers additionalRequestHeaders, String eTag, String condition) throws IOException {
        return execute(uri.toURL(), METHOD_PUT, content, additionalRequestHeaders, mimetype);
    }

    public Response delete(URI uri) throws IOException {
        return delete(uri, null, null, null);
    }

    public Response delete(URI uri, Headers additionalRequestHeaders) throws IOException {
        return delete(uri, additionalRequestHeaders, null, null);
    }

    public Response delete(URI uri, Headers additionalRequestHeaders, String eTag, String condition) throws IOException {
        return execute(uri.toURL(), METHOD_DELETE, null, additionalRequestHeaders, null);
    }

    public static String encryptString(String message) {
        byte[] textByte;
        Base64.Encoder encoder = java.util.Base64.getEncoder();
        try {
            textByte = message.getBytes("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
            textByte = null;
        }
        if (textByte == null) {
            return "";
        }
        return encoder.encodeToString(textByte);
    }

    /* access modifiers changed from: package-private */
    public class RequestInterceptor implements Interceptor {
        RequestInterceptor() {
        }

        public Response intercept(Interceptor.Chain chain) throws IOException {
            Log.d(XcapClient.TAG, "okhttp intercepting ...");
            Request request = chain.request();
            String requestEncoding = request.header("Accept-Encoding");
            if (!TextUtils.isEmpty(requestEncoding)) {
                Log.i(XcapClient.TAG, "found Accept-Encoding, remove it:" + requestEncoding);
                request = request.newBuilder().removeHeader("Accept-Encoding").build();
            }
            return chain.proceed(request);
        }
    }

    public static class NetworkDns implements Dns {
        private static NetworkDns sInstance;
        private Network mNetwork;

        public static NetworkDns getInstance() {
            if (sInstance == null) {
                sInstance = new NetworkDns();
            }
            return sInstance;
        }

        public void setNetwork(Network network) {
            this.mNetwork = network;
        }

        public List<InetAddress> lookup(String hostname) throws UnknownHostException {
            if (this.mNetwork == null) {
                return Dns.SYSTEM.lookup(hostname);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("lookup hostname:");
            sb.append(!XcapClient.SENLOG ? hostname : "[hidden]");
            Log.d(XcapClient.TAG, sb.toString());
            List<InetAddress> list = Arrays.asList(this.mNetwork.getAllByName(hostname));
            if (list == null || list.size() <= 0) {
                Log.d(XcapClient.TAG, "list is null, using SYSTEM Dns to lookup");
                return Dns.SYSTEM.lookup(hostname);
            }
            Log.d(XcapClient.TAG, "list size:" + list.size());
            for (int i = 0; i < list.size(); i++) {
                if (!XcapClient.SENLOG) {
                    Log.d(XcapClient.TAG, "InetAddress :" + list.get(i).toString());
                }
            }
            return list;
        }

        private NetworkDns() {
        }
    }
}
