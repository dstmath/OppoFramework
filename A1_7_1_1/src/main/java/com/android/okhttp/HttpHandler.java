package com.android.okhttp;

import com.android.okhttp.internal.URLFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.ResponseCache;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.List;
import java.util.concurrent.TimeUnit;
import libcore.net.NetworkSecurityPolicy;

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
public class HttpHandler extends URLStreamHandler {
    private static final CleartextURLFilter CLEARTEXT_FILTER = null;
    private static final List<ConnectionSpec> CLEARTEXT_ONLY = null;
    private final ConfigAwareConnectionPool configAwareConnectionPool;

    private static final class CleartextURLFilter implements URLFilter {
        /* synthetic */ CleartextURLFilter(CleartextURLFilter cleartextURLFilter) {
            this();
        }

        private CleartextURLFilter() {
        }

        public void checkURLPermitted(URL url) throws IOException {
            String host = url.getHost();
            if (!NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted(host)) {
                throw new IOException("Cleartext HTTP traffic to " + host + " not permitted");
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.okhttp.HttpHandler.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.okhttp.HttpHandler.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.HttpHandler.<clinit>():void");
    }

    public HttpHandler() {
        this.configAwareConnectionPool = ConfigAwareConnectionPool.getInstance();
    }

    protected URLConnection openConnection(URL url) throws IOException {
        try {
            Class[] clsArr = new Class[1];
            clsArr[0] = URL.class;
            Method method = Class.forName("android.security.NetworkSecurityPolicy").getMethod("checkUrl", clsArr);
            Object[] objArr = new Object[1];
            objArr[0] = url;
            method.invoke(null, objArr);
        } catch (Exception e) {
            System.out.println(e);
        }
        return newOkUrlFactory(null).open(url);
    }

    protected URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        if (url == null || proxy == null) {
            throw new IllegalArgumentException("url == null || proxy == null");
        }
        try {
            Class[] clsArr = new Class[1];
            clsArr[0] = URL.class;
            Method method = Class.forName("android.security.NetworkSecurityPolicy").getMethod("checkUrl", clsArr);
            Object[] objArr = new Object[1];
            objArr[0] = url;
            method.invoke(null, objArr);
        } catch (Exception e) {
            System.out.println(e);
        }
        return newOkUrlFactory(proxy).open(url);
    }

    protected int getDefaultPort() {
        return 80;
    }

    protected OkUrlFactory newOkUrlFactory(Proxy proxy) {
        OkUrlFactory okUrlFactory = createHttpOkUrlFactory(proxy);
        okUrlFactory.client().setConnectionPool(this.configAwareConnectionPool.get());
        return okUrlFactory;
    }

    public static OkUrlFactory createHttpOkUrlFactory(Proxy proxy) {
        OkHttpClient client = new OkHttpClient();
        client.setConnectTimeout(0, TimeUnit.MILLISECONDS);
        client.setReadTimeout(0, TimeUnit.MILLISECONDS);
        client.setWriteTimeout(0, TimeUnit.MILLISECONDS);
        client.setFollowRedirects(HttpURLConnection.getFollowRedirects());
        client.setFollowSslRedirects(false);
        client.setConnectionSpecs(CLEARTEXT_ONLY);
        if (proxy != null) {
            client.setProxy(proxy);
        }
        OkUrlFactory okUrlFactory = new OkUrlFactory(client);
        okUrlFactory.setUrlFilter(CLEARTEXT_FILTER);
        ResponseCache responseCache = ResponseCache.getDefault();
        if (responseCache != null) {
            AndroidInternal.setResponseCache(okUrlFactory, responseCache);
        }
        return okUrlFactory;
    }
}
