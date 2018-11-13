package com.android.okhttp.internal.http;

import com.android.okhttp.Address;
import com.android.okhttp.CertificatePinner;
import com.android.okhttp.Connection;
import com.android.okhttp.ConnectionPool;
import com.android.okhttp.Headers;
import com.android.okhttp.HttpUrl;
import com.android.okhttp.Interceptor.Chain;
import com.android.okhttp.MediaType;
import com.android.okhttp.OkHttpClient;
import com.android.okhttp.Protocol;
import com.android.okhttp.Request;
import com.android.okhttp.Response;
import com.android.okhttp.Response.Builder;
import com.android.okhttp.ResponseBody;
import com.android.okhttp.Route;
import com.android.okhttp.internal.Internal;
import com.android.okhttp.internal.InternalCache;
import com.android.okhttp.internal.Util;
import com.android.okhttp.internal.Version;
import com.android.okhttp.internal.http.CacheStrategy.Factory;
import com.android.okhttp.okio.Buffer;
import com.android.okhttp.okio.BufferedSink;
import com.android.okhttp.okio.BufferedSource;
import com.android.okhttp.okio.GzipSource;
import com.android.okhttp.okio.Okio;
import com.android.okhttp.okio.Sink;
import com.android.okhttp.okio.Source;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.lang.reflect.Method;
import java.net.CookieHandler;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import java.util.Date;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class HttpEngine {
    private static final ResponseBody EMPTY_BODY = null;
    public static final int MAX_AUTHENTICTORS = 10;
    public static final int MAX_FOLLOW_UPS = 20;
    private static Method enforceCheckPermissionMethod;
    private Address address;
    private int authenticateCount;
    public final boolean bufferRequestBody;
    private BufferedSink bufferedRequestBody;
    private Response cacheResponse;
    private CacheStrategy cacheStrategy;
    private final boolean callerWritesRequestBody;
    final OkHttpClient client;
    private Connection connection;
    private final boolean forWebSocket;
    private boolean momsPermitted;
    private Request networkRequest;
    private final Response priorResponse;
    private Sink requestBodyOut;
    private Route route;
    private RouteSelector routeSelector;
    long sentRequestMillis;
    private CacheRequest storeRequest;
    private boolean transparentGzip;
    private Transport transport;
    private final Request userRequest;
    private Response userResponse;

    /* renamed from: com.android.okhttp.internal.http.HttpEngine$2 */
    class AnonymousClass2 implements Source {
        boolean cacheRequestClosed;
        final /* synthetic */ HttpEngine this$0;
        final /* synthetic */ BufferedSink val$cacheBody;
        final /* synthetic */ CacheRequest val$cacheRequest;
        final /* synthetic */ BufferedSource val$source;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.okhttp.internal.http.HttpEngine.2.<init>(com.android.okhttp.internal.http.HttpEngine, com.android.okhttp.okio.BufferedSource, com.android.okhttp.internal.http.CacheRequest, com.android.okhttp.okio.BufferedSink):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass2(com.android.okhttp.internal.http.HttpEngine r1, com.android.okhttp.okio.BufferedSource r2, com.android.okhttp.internal.http.CacheRequest r3, com.android.okhttp.okio.BufferedSink r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.okhttp.internal.http.HttpEngine.2.<init>(com.android.okhttp.internal.http.HttpEngine, com.android.okhttp.okio.BufferedSource, com.android.okhttp.internal.http.CacheRequest, com.android.okhttp.okio.BufferedSink):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.2.<init>(com.android.okhttp.internal.http.HttpEngine, com.android.okhttp.okio.BufferedSource, com.android.okhttp.internal.http.CacheRequest, com.android.okhttp.okio.BufferedSink):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.android.okhttp.internal.http.HttpEngine.2.close():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void close() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.android.okhttp.internal.http.HttpEngine.2.close():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.2.close():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.2.read(com.android.okhttp.okio.Buffer, long):long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public long read(com.android.okhttp.okio.Buffer r1, long r2) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.2.read(com.android.okhttp.okio.Buffer, long):long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.2.read(com.android.okhttp.okio.Buffer, long):long");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.2.timeout():com.android.okhttp.okio.Timeout, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public com.android.okhttp.okio.Timeout timeout() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.2.timeout():com.android.okhttp.okio.Timeout, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.2.timeout():com.android.okhttp.okio.Timeout");
        }
    }

    /* renamed from: com.android.okhttp.internal.http.HttpEngine$3 */
    class AnonymousClass3 extends ResponseBody {
        final /* synthetic */ HttpEngine this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.okhttp.internal.http.HttpEngine.3.<init>(com.android.okhttp.internal.http.HttpEngine):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(com.android.okhttp.internal.http.HttpEngine r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.okhttp.internal.http.HttpEngine.3.<init>(com.android.okhttp.internal.http.HttpEngine):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.3.<init>(com.android.okhttp.internal.http.HttpEngine):void");
        }

        public MediaType contentType() {
            return null;
        }

        public long contentLength() {
            return 0;
        }

        public BufferedSource source() {
            return new Buffer();
        }
    }

    class NetworkInterceptorChain implements Chain {
        private int calls;
        private final int index;
        private final Request request;
        final /* synthetic */ HttpEngine this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.<init>(com.android.okhttp.internal.http.HttpEngine, int, com.android.okhttp.Request):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        NetworkInterceptorChain(com.android.okhttp.internal.http.HttpEngine r1, int r2, com.android.okhttp.Request r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.<init>(com.android.okhttp.internal.http.HttpEngine, int, com.android.okhttp.Request):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.<init>(com.android.okhttp.internal.http.HttpEngine, int, com.android.okhttp.Request):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.connection():com.android.okhttp.Connection, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public com.android.okhttp.Connection connection() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.connection():com.android.okhttp.Connection, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.connection():com.android.okhttp.Connection");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.proceed(com.android.okhttp.Request):com.android.okhttp.Response, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public com.android.okhttp.Response proceed(com.android.okhttp.Request r1) throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.proceed(com.android.okhttp.Request):com.android.okhttp.Response, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.proceed(com.android.okhttp.Request):com.android.okhttp.Response");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.request():com.android.okhttp.Request, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public com.android.okhttp.Request request() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.request():com.android.okhttp.Request, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.NetworkInterceptorChain.request():com.android.okhttp.Request");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.okhttp.internal.http.HttpEngine.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.okhttp.internal.http.HttpEngine.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.<clinit>():void");
    }

    public HttpEngine(OkHttpClient client, Request request, boolean bufferRequestBody, boolean callerWritesRequestBody, boolean forWebSocket, Connection connection, RouteSelector routeSelector, RetryableSink requestBodyOut, Response priorResponse) {
        this.sentRequestMillis = -1;
        this.momsPermitted = true;
        this.client = client;
        this.userRequest = request;
        this.bufferRequestBody = bufferRequestBody;
        this.callerWritesRequestBody = callerWritesRequestBody;
        this.forWebSocket = forWebSocket;
        this.connection = connection;
        this.routeSelector = routeSelector;
        this.requestBodyOut = requestBodyOut;
        this.priorResponse = priorResponse;
        if (connection != null) {
            Internal.instance.setOwner(connection, this);
            this.route = connection.getRoute();
            return;
        }
        this.route = null;
    }

    public void sendRequest() throws RequestException, RouteException, IOException {
        if (this.cacheStrategy == null) {
            if (this.transport != null) {
                throw new IllegalStateException();
            }
            Response cacheCandidate;
            Request request = networkRequest(this.userRequest);
            InternalCache responseCache = Internal.instance.internalCache(this.client);
            if (responseCache != null) {
                cacheCandidate = responseCache.get(request);
            } else {
                cacheCandidate = null;
            }
            this.cacheStrategy = new Factory(System.currentTimeMillis(), request, cacheCandidate).get();
            this.networkRequest = this.cacheStrategy.networkRequest;
            this.cacheResponse = this.cacheStrategy.cacheResponse;
            if (responseCache != null) {
                responseCache.trackResponse(this.cacheStrategy);
            }
            if (cacheCandidate != null && this.cacheResponse == null) {
                Util.closeQuietly(cacheCandidate.body());
            }
            if (this.networkRequest == null) {
                if (this.connection != null) {
                    Internal.instance.recycle(this.client.getConnectionPool(), this.connection);
                    this.connection = null;
                }
                if (this.cacheResponse != null) {
                    this.userResponse = this.cacheResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).cacheResponse(stripBody(this.cacheResponse)).build();
                } else {
                    this.userResponse = new Builder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).protocol(Protocol.HTTP_1_1).code(504).message("Unsatisfiable Request (only-if-cached)").body(EMPTY_BODY).build();
                }
                this.userResponse = unzip(this.userResponse);
            } else if (isMmsAndEmailSendingPermitted(this.userRequest)) {
                if (this.connection == null) {
                    connect();
                }
                if (this.connection != null && isMoMMS(request)) {
                    try {
                        Socket s = this.connection.getSocket();
                        if (s != null) {
                            System.out.println("Configure MMS buffer size");
                            s.setSendBufferSize(8192);
                            s.setReceiveBufferSize(16384);
                        }
                    } catch (Exception e) {
                        System.out.println("Socket Buffer size:" + e);
                    }
                }
                this.transport = Internal.instance.newTransport(this.connection, this);
                if (this.callerWritesRequestBody && permitsRequestBody() && this.requestBodyOut == null) {
                    long contentLength = OkHeaders.contentLength(request);
                    if (!this.bufferRequestBody) {
                        this.transport.writeRequestHeaders(this.networkRequest);
                        this.requestBodyOut = this.transport.createRequestBody(this.networkRequest, contentLength);
                    } else if (contentLength > 2147483647L) {
                        throw new IllegalStateException("Use setFixedLengthStreamingMode() or setChunkedStreamingMode() for requests larger than 2 GiB.");
                    } else if (contentLength != -1) {
                        this.transport.writeRequestHeaders(this.networkRequest);
                        this.requestBodyOut = new RetryableSink((int) contentLength);
                    } else {
                        this.requestBodyOut = new RetryableSink();
                    }
                }
            } else {
                this.momsPermitted = false;
                System.out.println("isMmsAndEmailSendingPermitted ? no");
            }
        }
    }

    private static Response stripBody(Response response) {
        if (response == null || response.body() == null) {
            return response;
        }
        return response.newBuilder().body(null).build();
    }

    private void connect() throws RequestException, RouteException {
        if (this.connection != null) {
            throw new IllegalStateException();
        }
        if (this.routeSelector == null) {
            this.address = createAddress(this.client, this.networkRequest);
            try {
                this.routeSelector = RouteSelector.get(this.address, this.networkRequest, this.client);
            } catch (IOException e) {
                throw new RequestException(e);
            }
        }
        this.connection = createNextConnection();
        Internal.instance.connectAndSetOwner(this.client, this.connection, this, this.networkRequest);
        this.route = this.connection.getRoute();
    }

    private Connection createNextConnection() throws RouteException {
        Connection pooled;
        ConnectionPool pool = this.client.getConnectionPool();
        while (true) {
            pooled = pool.get(this.address);
            if (pooled == null) {
                try {
                    return new Connection(pool, this.routeSelector.next());
                } catch (IOException e) {
                    throw new RouteException(e);
                }
            } else if (this.networkRequest.method().equals("GET") || Internal.instance.isReadable(pooled)) {
                return pooled;
            } else {
                Util.closeQuietly(pooled.getSocket());
            }
        }
        return pooled;
    }

    public void writingRequestHeaders() {
        if (this.sentRequestMillis != -1) {
            throw new IllegalStateException();
        }
        this.sentRequestMillis = System.currentTimeMillis();
    }

    boolean permitsRequestBody() {
        return HttpMethod.permitsRequestBody(this.userRequest.method());
    }

    public Sink getRequestBody() {
        if (this.cacheStrategy != null) {
            return this.requestBodyOut;
        }
        throw new IllegalStateException();
    }

    public BufferedSink getBufferedRequestBody() {
        BufferedSink bufferedSink = null;
        BufferedSink result = this.bufferedRequestBody;
        if (result != null) {
            return result;
        }
        Sink requestBody = getRequestBody();
        if (requestBody != null) {
            bufferedSink = Okio.buffer(requestBody);
            this.bufferedRequestBody = bufferedSink;
        }
        return bufferedSink;
    }

    public boolean hasResponse() {
        return this.userResponse != null;
    }

    public Request getRequest() {
        return this.userRequest;
    }

    public Response getResponse() {
        if (this.userResponse != null) {
            return this.userResponse;
        }
        throw new IllegalStateException();
    }

    public Connection getConnection() {
        return this.connection;
    }

    public HttpEngine recover(RouteException e) {
        if (!(this.routeSelector == null || this.connection == null)) {
            connectFailed(this.routeSelector, e.getLastConnectException());
        }
        if ((this.routeSelector == null && this.connection == null) || ((this.routeSelector != null && !this.routeSelector.hasNext()) || !isRecoverable(e))) {
            return null;
        }
        return new HttpEngine(this.client, this.userRequest, this.bufferRequestBody, this.callerWritesRequestBody, this.forWebSocket, close(), this.routeSelector, (RetryableSink) this.requestBodyOut, this.priorResponse);
    }

    private boolean isRecoverable(RouteException e) {
        if (!this.client.getRetryOnConnectionFailure()) {
            return false;
        }
        IOException ioe = e.getLastConnectException();
        if (ioe instanceof ProtocolException) {
            return false;
        }
        if (ioe instanceof InterruptedIOException) {
            return ioe instanceof SocketTimeoutException;
        }
        if (((ioe instanceof SSLHandshakeException) && (ioe.getCause() instanceof CertificateException)) || (ioe instanceof SSLPeerUnverifiedException)) {
            return false;
        }
        return true;
    }

    public HttpEngine recover(IOException e, Sink requestBodyOut) {
        if (!(this.routeSelector == null || this.connection == null)) {
            connectFailed(this.routeSelector, e);
        }
        boolean canRetryRequestBody = requestBodyOut != null ? requestBodyOut instanceof RetryableSink : true;
        if ((this.routeSelector == null && this.connection == null) || ((this.routeSelector != null && !this.routeSelector.hasNext()) || !isRecoverable(e) || !canRetryRequestBody)) {
            return null;
        }
        return new HttpEngine(this.client, this.userRequest, this.bufferRequestBody, this.callerWritesRequestBody, this.forWebSocket, close(), this.routeSelector, (RetryableSink) requestBodyOut, this.priorResponse);
    }

    private void connectFailed(RouteSelector routeSelector, IOException e) {
        if (Internal.instance.recycleCount(this.connection) <= 0) {
            routeSelector.connectFailed(this.connection.getRoute(), e);
        }
    }

    public HttpEngine recover(IOException e) {
        return recover(e, this.requestBodyOut);
    }

    private boolean isRecoverable(IOException e) {
        if (!this.client.getRetryOnConnectionFailure() || (e instanceof ProtocolException) || (e instanceof InterruptedIOException)) {
            return false;
        }
        return true;
    }

    public Route getRoute() {
        return this.route;
    }

    private void maybeCache() throws IOException {
        InternalCache responseCache = Internal.instance.internalCache(this.client);
        if (responseCache != null) {
            if (CacheStrategy.isCacheable(this.userResponse, this.networkRequest)) {
                this.storeRequest = responseCache.put(stripBody(this.userResponse));
                return;
            }
            if (HttpMethod.invalidatesCache(this.networkRequest.method())) {
                try {
                    responseCache.remove(this.networkRequest);
                } catch (IOException e) {
                }
            }
        }
    }

    public void releaseConnection() throws IOException {
        if (!(this.transport == null || this.connection == null)) {
            this.transport.releaseConnectionOnIdle();
        }
        this.connection = null;
    }

    public void disconnect() {
        try {
            if (this.transport != null) {
                this.transport.disconnect(this);
                return;
            }
            Connection connection = this.connection;
            if (connection != null) {
                Internal.instance.closeIfOwnedBy(connection, this);
            }
        } catch (IOException e) {
        }
    }

    public Connection close() {
        if (this.bufferedRequestBody != null) {
            Util.closeQuietly(this.bufferedRequestBody);
        } else if (this.requestBodyOut != null) {
            Util.closeQuietly(this.requestBodyOut);
        }
        if (this.userResponse == null) {
            if (this.connection != null) {
                Util.closeQuietly(this.connection.getSocket());
            }
            this.connection = null;
            return null;
        }
        Util.closeQuietly(this.userResponse.body());
        if (this.transport == null || this.connection == null || this.transport.canReuseConnection()) {
            if (!(this.connection == null || Internal.instance.clearOwner(this.connection))) {
                this.connection = null;
            }
            Connection result = this.connection;
            this.connection = null;
            return result;
        }
        Util.closeQuietly(this.connection.getSocket());
        this.connection = null;
        return null;
    }

    /* JADX WARNING: Missing block: B:7:0x001d, code:
            return r6;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Response unzip(Response response) throws IOException {
        if (!this.transparentGzip || !"gzip".equalsIgnoreCase(this.userResponse.header("Content-Encoding")) || response.body() == null) {
            return response;
        }
        Source responseBody = new GzipSource(response.body().source());
        Headers strippedHeaders = response.headers().newBuilder().removeAll("Content-Encoding").removeAll("Content-Length").build();
        return response.newBuilder().headers(strippedHeaders).body(new RealResponseBody(strippedHeaders, Okio.buffer(responseBody))).build();
    }

    public static boolean hasBody(Response response) {
        if (response.request().method().equals("HEAD")) {
            return false;
        }
        int responseCode = response.code();
        return (((responseCode >= 100 && responseCode < 200) || responseCode == 204 || responseCode == 304) && OkHeaders.contentLength(response) == -1 && !"chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) ? false : true;
    }

    private Request networkRequest(Request request) throws IOException {
        Request.Builder result = request.newBuilder();
        if (request.header("Host") == null) {
            result.header("Host", Util.hostHeader(request.httpUrl()));
        }
        if ((this.connection == null || this.connection.getProtocol() != Protocol.HTTP_1_0) && request.header("Connection") == null) {
            result.header("Connection", "Keep-Alive");
        }
        if (request.header("Accept-Encoding") == null && !"true".equals(System.getProperty("xcap.req"))) {
            this.transparentGzip = true;
            result.header("Accept-Encoding", "gzip");
        }
        CookieHandler cookieHandler = this.client.getCookieHandler();
        if (cookieHandler != null) {
            OkHeaders.addCookies(result, cookieHandler.get(request.uri(), OkHeaders.toMultimap(result.build().headers(), null)));
        }
        if (request.header("User-Agent") == null) {
            result.header("User-Agent", Version.userAgent());
        }
        return result.build();
    }

    public void readResponse() throws IOException {
        if (this.userResponse == null) {
            if (this.networkRequest == null && this.cacheResponse == null) {
                throw new IllegalStateException("call sendRequest() first!");
            } else if (this.networkRequest != null) {
                if (this.momsPermitted) {
                    Response networkResponse;
                    if (this.forWebSocket) {
                        this.transport.writeRequestHeaders(this.networkRequest);
                        networkResponse = readNetworkResponse();
                    } else if (this.callerWritesRequestBody) {
                        if (this.bufferedRequestBody != null && this.bufferedRequestBody.buffer().size() > 0) {
                            this.bufferedRequestBody.emit();
                        }
                        if (this.sentRequestMillis == -1) {
                            if (OkHeaders.contentLength(this.networkRequest) == -1 && (this.requestBodyOut instanceof RetryableSink)) {
                                this.networkRequest = this.networkRequest.newBuilder().header("Content-Length", Long.toString(((RetryableSink) this.requestBodyOut).contentLength())).build();
                            }
                            System.out.println("[OkHttp] sendRequest>>");
                            this.transport.writeRequestHeaders(this.networkRequest);
                        }
                        if (this.requestBodyOut != null) {
                            if (this.bufferedRequestBody != null) {
                                this.bufferedRequestBody.close();
                            } else {
                                this.requestBodyOut.close();
                            }
                            if (this.requestBodyOut instanceof RetryableSink) {
                                this.transport.writeRequestBody((RetryableSink) this.requestBodyOut);
                            }
                        }
                        System.out.println("[OkHttp] sendRequest<<");
                        networkResponse = readNetworkResponse();
                    } else {
                        networkResponse = new NetworkInterceptorChain(this, 0, this.networkRequest).proceed(this.networkRequest);
                    }
                    receiveHeaders(networkResponse.headers());
                    if (this.cacheResponse != null) {
                        if (validate(this.cacheResponse, networkResponse)) {
                            this.userResponse = this.cacheResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).headers(combine(this.cacheResponse.headers(), networkResponse.headers())).cacheResponse(stripBody(this.cacheResponse)).networkResponse(stripBody(networkResponse)).build();
                            networkResponse.body().close();
                            releaseConnection();
                            InternalCache responseCache = Internal.instance.internalCache(this.client);
                            responseCache.trackConditionalCacheHit();
                            responseCache.update(this.cacheResponse, stripBody(this.userResponse));
                            this.userResponse = unzip(this.userResponse);
                            return;
                        }
                        Util.closeQuietly(this.cacheResponse.body());
                    }
                    this.userResponse = networkResponse.newBuilder().request(this.userRequest).priorResponse(stripBody(this.priorResponse)).cacheResponse(stripBody(this.cacheResponse)).networkResponse(stripBody(networkResponse)).build();
                    if (hasBody(this.userResponse)) {
                        maybeCache();
                        this.userResponse = unzip(cacheWritingResponse(this.storeRequest, this.userResponse));
                    }
                    return;
                }
                this.userResponse = getBadHttpResponse();
                System.out.println("Mms or Email Sending is not Permitted");
            }
        }
    }

    private Response readNetworkResponse() throws IOException {
        this.transport.finishRequest();
        Response networkResponse = this.transport.readResponseHeaders().request(this.networkRequest).handshake(this.connection.getHandshake()).header(OkHeaders.SENT_MILLIS, Long.toString(this.sentRequestMillis)).header(OkHeaders.RECEIVED_MILLIS, Long.toString(System.currentTimeMillis())).build();
        if (!this.forWebSocket) {
            networkResponse = networkResponse.newBuilder().body(this.transport.openResponseBody(networkResponse)).build();
        }
        Internal.instance.setProtocol(this.connection, networkResponse.protocol());
        return networkResponse;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private com.android.okhttp.Response cacheWritingResponse(com.android.okhttp.internal.http.CacheRequest r9, com.android.okhttp.Response r10) throws java.io.IOException {
        /*
        r8 = this;
        if (r9 != 0) goto L_0x0003;
    L_0x0002:
        return r10;
    L_0x0003:
        r1 = r9.body();
        if (r1 != 0) goto L_0x000a;
    L_0x0009:
        return r10;
    L_0x000a:
        r4 = r10.body();
        r3 = r4.source();
        r0 = com.android.okhttp.okio.Okio.buffer(r1);
        r2 = new com.android.okhttp.internal.http.HttpEngine$2;
        r2.<init>(r8, r3, r9, r0);
        r4 = r10.newBuilder();
        r5 = new com.android.okhttp.internal.http.RealResponseBody;
        r6 = r10.headers();
        r7 = com.android.okhttp.okio.Okio.buffer(r2);
        r5.<init>(r6, r7);
        r4 = r4.body(r5);
        r4 = r4.build();
        return r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.HttpEngine.cacheWritingResponse(com.android.okhttp.internal.http.CacheRequest, com.android.okhttp.Response):com.android.okhttp.Response");
    }

    private static boolean validate(Response cached, Response network) {
        if (network.code() == 304) {
            return true;
        }
        Date lastModified = cached.headers().getDate("Last-Modified");
        if (lastModified != null) {
            Date networkLastModified = network.headers().getDate("Last-Modified");
            if (networkLastModified != null && networkLastModified.getTime() < lastModified.getTime()) {
                return true;
            }
        }
        return false;
    }

    private static Headers combine(Headers cachedHeaders, Headers networkHeaders) throws IOException {
        int i;
        String fieldName;
        Headers.Builder result = new Headers.Builder();
        int size = cachedHeaders.size();
        for (i = 0; i < size; i++) {
            fieldName = cachedHeaders.name(i);
            String value = cachedHeaders.value(i);
            if (!("Warning".equalsIgnoreCase(fieldName) && value.startsWith("1")) && (!OkHeaders.isEndToEnd(fieldName) || networkHeaders.get(fieldName) == null)) {
                result.add(fieldName, value);
            }
        }
        size = networkHeaders.size();
        for (i = 0; i < size; i++) {
            fieldName = networkHeaders.name(i);
            if (!"Content-Length".equalsIgnoreCase(fieldName) && OkHeaders.isEndToEnd(fieldName)) {
                result.add(fieldName, networkHeaders.value(i));
            }
        }
        return result.build();
    }

    public void receiveHeaders(Headers headers) throws IOException {
        CookieHandler cookieHandler = this.client.getCookieHandler();
        if (cookieHandler != null) {
            cookieHandler.put(this.userRequest.uri(), OkHeaders.toMultimap(headers, null));
        }
    }

    /* JADX WARNING: Missing block: B:15:0x003b, code:
            r9 = r13.authenticateCount + 1;
            r13.authenticateCount = r9;
     */
    /* JADX WARNING: Missing block: B:16:0x0043, code:
            if (r9 <= 10) goto L_0x0061;
     */
    /* JADX WARNING: Missing block: B:18:0x0060, code:
            throw new java.net.ProtocolException("Too many authentication: " + r13.authenticateCount);
     */
    /* JADX WARNING: Missing block: B:19:0x0061, code:
            java.lang.System.setProperty("http.method", r13.networkRequest.method());
            r8 = r13.networkRequest.url().getFile();
     */
    /* JADX WARNING: Missing block: B:20:0x0077, code:
            if (r8 == null) goto L_0x007f;
     */
    /* JADX WARNING: Missing block: B:22:0x007d, code:
            if (r8.isEmpty() == false) goto L_0x0092;
     */
    /* JADX WARNING: Missing block: B:23:0x007f, code:
            java.lang.System.setProperty("http.urlpath", r8);
     */
    /* JADX WARNING: Missing block: B:24:0x0091, code:
            return com.android.okhttp.internal.http.OkHeaders.processAuthHeader(r13.client.getAuthenticator(), r13.userResponse, r5);
     */
    /* JADX WARNING: Missing block: B:26:0x009f, code:
            if (r8.substring(0, 1).equals("/") != false) goto L_0x007f;
     */
    /* JADX WARNING: Missing block: B:27:0x00a1, code:
            r8 = "/" + r8;
     */
    /* JADX WARNING: Missing block: B:33:0x00da, code:
            if (r13.client.getFollowRedirects() != false) goto L_0x00de;
     */
    /* JADX WARNING: Missing block: B:34:0x00dc, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:36:0x00de, code:
            r1 = r13.userResponse.header("Location");
     */
    /* JADX WARNING: Missing block: B:37:0x00e7, code:
            if (r1 != null) goto L_0x00ea;
     */
    /* JADX WARNING: Missing block: B:38:0x00e9, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:39:0x00ea, code:
            r7 = r13.userRequest.httpUrl().resolve(r1);
     */
    /* JADX WARNING: Missing block: B:40:0x00f4, code:
            if (r7 != null) goto L_0x00f7;
     */
    /* JADX WARNING: Missing block: B:41:0x00f6, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:43:0x0109, code:
            if (r7.scheme().equals(r13.userRequest.httpUrl().scheme()) != false) goto L_0x0113;
     */
    /* JADX WARNING: Missing block: B:45:0x0111, code:
            if (r13.client.getFollowSslRedirects() == false) goto L_0x0163;
     */
    /* JADX WARNING: Missing block: B:46:0x0113, code:
            java.lang.System.out.println("tmpLocation");
     */
    /* JADX WARNING: Missing block: B:48:?, code:
            r6 = r13.userResponse.header("Location");
     */
    /* JADX WARNING: Missing block: B:57:0x0163, code:
            return null;
     */
    /* JADX WARNING: Missing block: B:58:0x0164, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:59:0x0165, code:
            java.lang.System.out.println("exception:" + r0.getMessage());
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Request followUpRequest() throws IOException {
        if (this.userResponse == null) {
            throw new IllegalStateException();
        }
        Proxy selectedProxy;
        if (getRoute() != null) {
            selectedProxy = getRoute().getProxy();
        } else {
            selectedProxy = this.client.getProxy();
        }
        switch (this.userResponse.code()) {
            case 300:
            case 301:
            case 302:
            case 303:
                break;
            case StatusLine.HTTP_TEMP_REDIRECT /*307*/:
            case StatusLine.HTTP_PERM_REDIRECT /*308*/:
                if (!(this.userRequest.method().equals("GET") || this.userRequest.method().equals("HEAD"))) {
                    return null;
                }
            case 401:
                break;
            case 407:
                if (selectedProxy.type() != Type.HTTP) {
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                }
                break;
            default:
                return null;
        }
        Request.Builder requestBuilder = this.userRequest.newBuilder();
        if (HttpMethod.permitsRequestBody(this.userRequest.method())) {
            requestBuilder.method("GET", null);
            requestBuilder.removeHeader("Transfer-Encoding");
            requestBuilder.removeHeader("Content-Length");
            requestBuilder.removeHeader("Content-Type");
        }
        if (!sameConnection(url)) {
            requestBuilder.removeHeader("Authorization");
        }
        return requestBuilder.url(url).build();
    }

    public boolean sameConnection(HttpUrl followUp) {
        HttpUrl url = this.userRequest.httpUrl();
        if (url.host().equals(followUp.host()) && url.port() == followUp.port()) {
            return url.scheme().equals(followUp.scheme());
        }
        return false;
    }

    private static Address createAddress(OkHttpClient client, Request request) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (request.isHttps()) {
            sslSocketFactory = client.getSslSocketFactory();
            hostnameVerifier = client.getHostnameVerifier();
            certificatePinner = client.getCertificatePinner();
        }
        return new Address(request.httpUrl().rfc2732host(), request.httpUrl().port(), client.getSocketFactory(), sslSocketFactory, hostnameVerifier, certificatePinner, client.getAuthenticator(), client.getProxy(), client.getProtocols(), client.getConnectionSpecs(), client.getProxySelector());
    }

    private boolean isMmsAndEmailSendingPermitted(Request request) {
        if (isMoMMS(request)) {
            if (!enforceCheckPermission("com.mediatek.permission.CTA_SEND_MMS", "Send MMS")) {
                System.out.println("Fail to send due to user permission");
                return false;
            }
        } else if (isEmailSend(request) && !enforceCheckPermission("com.mediatek.permission.CTA_SEND_EMAIL", "Send emails")) {
            System.out.println("Fail to send due to user permission");
            return false;
        }
        return true;
    }

    private boolean isMoMMS(Request request) {
        String mimetype = "application/vnd.wap.mms-message";
        if ("POST".equals(request.method())) {
            String userAgent = request.header("User-Agent");
            if (userAgent != null && userAgent.indexOf("MMS") != -1) {
                return true;
            }
            String contentType = request.header("Content-Type");
            if (contentType != null && contentType.indexOf("application/vnd.wap.mms-message") != -1) {
                return true;
            }
            String acceptType = request.header("Accept");
            if (acceptType != null && acceptType.indexOf("application/vnd.wap.mms-message") != -1) {
                return true;
            }
            for (String value : request.headers().values("Content-Type")) {
                if (value.indexOf("application/vnd.wap.mms-message") != -1) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isEmailSend(Request request) {
        String mimetype = "application/vnd.ms-sync.wbxml";
        if ("POST".equals(request.method()) || "PUT".equals(request.method())) {
            String contentType = request.header("Content-Type");
            if (contentType != null && contentType.startsWith("message/rfc822")) {
                return true;
            }
            for (String value : request.headers().values("Content-Type")) {
                if (value.startsWith("message/rfc822")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean enforceCheckPermission(String permission, String action) {
        try {
            Method method;
            synchronized (HttpEngine.class) {
                if (enforceCheckPermissionMethod == null) {
                    Class[] clsArr = new Class[2];
                    clsArr[0] = String.class;
                    clsArr[1] = String.class;
                    enforceCheckPermissionMethod = Class.forName("com.mediatek.cta.CtaUtils").getMethod("enforceCheckPermission", clsArr);
                }
                method = enforceCheckPermissionMethod;
            }
            Object[] objArr = new Object[2];
            objArr[0] = permission;
            objArr[1] = action;
            return ((Boolean) method.invoke(null, objArr)).booleanValue();
        } catch (ReflectiveOperationException e) {
            if (!(e.getCause() instanceof SecurityException)) {
                return true;
            }
            throw new SecurityException(e.getCause());
        }
    }

    private Response getBadHttpResponse() {
        return new Builder().protocol(Protocol.HTTP_1_1).code(400).message("User Permission is denied").body(new AnonymousClass3(this)).build();
    }
}
