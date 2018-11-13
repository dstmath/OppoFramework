package com.android.org.conscrypt;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;
import dalvik.system.BlockGuard;
import dalvik.system.CloseGuard;
import java.io.FileDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketImpl;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECParameterSpec;
import java.util.Collections;
import java.util.List;
import javax.crypto.spec.GCMParameterSpec;
import javax.net.ssl.SNIHostName;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509ExtendedTrustManager;
import javax.net.ssl.X509TrustManager;
import sun.security.x509.AlgorithmId;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
class Platform {

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private static class NoPreloadHolder {
        public static final Platform MAPPER = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.Platform.NoPreloadHolder.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.Platform.NoPreloadHolder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.Platform.NoPreloadHolder.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.Platform.NoPreloadHolder.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private NoPreloadHolder() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.Platform.NoPreloadHolder.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.Platform.NoPreloadHolder.<init>():void");
        }
    }

    /* synthetic */ Platform(Platform platform) {
        this();
    }

    public static void setup() {
        NoPreloadHolder.MAPPER.ping();
    }

    private void ping() {
    }

    private Platform() {
    }

    public static FileDescriptor getFileDescriptor(Socket s) {
        return s.getFileDescriptor$();
    }

    public static FileDescriptor getFileDescriptorFromSSLSocket(OpenSSLSocketImpl openSSLSocketImpl) {
        try {
            Field f_impl = Socket.class.getDeclaredField("impl");
            f_impl.setAccessible(true);
            Object socketImpl = f_impl.get(openSSLSocketImpl);
            Field f_fd = SocketImpl.class.getDeclaredField("fd");
            f_fd.setAccessible(true);
            return (FileDescriptor) f_fd.get(socketImpl);
        } catch (Exception e) {
            throw new RuntimeException("Can't get FileDescriptor from socket", e);
        }
    }

    public static String getCurveName(ECParameterSpec spec) {
        return spec.getCurveName();
    }

    public static void setCurveName(ECParameterSpec spec, String curveName) {
        spec.setCurveName(curveName);
    }

    public static void setSocketWriteTimeout(Socket s, long timeoutMillis) throws SocketException {
        try {
            Os.setsockoptTimeval(s.getFileDescriptor$(), OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO, StructTimeval.fromMillis(timeoutMillis));
        } catch (ErrnoException errnoException) {
            throw errnoException.rethrowAsSocketException();
        }
    }

    public static void setSSLParameters(SSLParameters params, SSLParametersImpl impl, OpenSSLSocketImpl socket) {
        impl.setEndpointIdentificationAlgorithm(params.getEndpointIdentificationAlgorithm());
        impl.setUseCipherSuitesOrder(params.getUseCipherSuitesOrder());
        List<SNIServerName> serverNames = params.getServerNames();
        if (serverNames != null) {
            for (SNIServerName serverName : serverNames) {
                if (serverName.getType() == 0) {
                    socket.setHostname(((SNIHostName) serverName).getAsciiName());
                    return;
                }
            }
        }
    }

    public static void getSSLParameters(SSLParameters params, SSLParametersImpl impl, OpenSSLSocketImpl socket) {
        params.setEndpointIdentificationAlgorithm(impl.getEndpointIdentificationAlgorithm());
        params.setUseCipherSuitesOrder(impl.getUseCipherSuitesOrder());
        if (impl.getUseSni() && AddressUtils.isValidSniHostname(socket.getHostname())) {
            params.setServerNames(Collections.singletonList(new SNIHostName(socket.getHostname())));
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x0044 A:{Splitter: B:1:0x0002, ExcHandler: java.lang.NoSuchMethodException (e java.lang.NoSuchMethodException)} */
    /* JADX WARNING: Missing block: B:12:0x0045, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean checkTrusted(String methodName, X509TrustManager tm, X509Certificate[] chain, String authType, Class<?> argumentClass, Object argumentInstance) throws CertificateException {
        try {
            tm.getClass().getMethod(methodName, new Class[]{X509Certificate[].class, String.class, argumentClass}).invoke(tm, new Object[]{chain, authType, argumentInstance});
            return true;
        } catch (NoSuchMethodException e) {
        } catch (InvocationTargetException e2) {
            if (e2.getCause() instanceof CertificateException) {
                throw ((CertificateException) e2.getCause());
            }
            throw new RuntimeException(e2.getCause());
        }
    }

    public static void checkClientTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLSocketImpl socket) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkClientTrusted(chain, authType, socket);
            return;
        }
        if (!checkTrusted("checkClientTrusted", tm, chain, authType, Socket.class, socket)) {
            if (!checkTrusted("checkClientTrusted", tm, chain, authType, String.class, socket.getHandshakeSession().getPeerHost())) {
                tm.checkClientTrusted(chain, authType);
            }
        }
    }

    public static void checkServerTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLSocketImpl socket) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkServerTrusted(chain, authType, socket);
            return;
        }
        if (!checkTrusted("checkServerTrusted", tm, chain, authType, Socket.class, socket)) {
            if (!checkTrusted("checkServerTrusted", tm, chain, authType, String.class, socket.getHandshakeSession().getPeerHost())) {
                tm.checkServerTrusted(chain, authType);
            }
        }
    }

    public static void checkClientTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLEngineImpl engine) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkClientTrusted(chain, authType, engine);
            return;
        }
        if (!checkTrusted("checkClientTrusted", tm, chain, authType, SSLEngine.class, engine)) {
            if (!checkTrusted("checkClientTrusted", tm, chain, authType, String.class, engine.getHandshakeSession().getPeerHost())) {
                tm.checkClientTrusted(chain, authType);
            }
        }
    }

    public static void checkServerTrusted(X509TrustManager tm, X509Certificate[] chain, String authType, OpenSSLEngineImpl engine) throws CertificateException {
        if (tm instanceof X509ExtendedTrustManager) {
            ((X509ExtendedTrustManager) tm).checkServerTrusted(chain, authType, engine);
            return;
        }
        if (!checkTrusted("checkServerTrusted", tm, chain, authType, SSLEngine.class, engine)) {
            if (!checkTrusted("checkServerTrusted", tm, chain, authType, String.class, engine.getHandshakeSession().getPeerHost())) {
                tm.checkServerTrusted(chain, authType);
            }
        }
    }

    public static OpenSSLKey wrapRsaKey(PrivateKey key) {
        return null;
    }

    public static void logEvent(String message) {
        try {
            Class processClass = Class.forName("android.os.Process");
            int uid = ((Integer) processClass.getMethod("myUid", (Class[]) null).invoke(processClass.newInstance(), new Object[0])).intValue();
            Class eventLogClass = Class.forName("android.util.EventLog");
            Object eventLogInstance = eventLogClass.newInstance();
            Method writeEventMethod = eventLogClass.getMethod("writeEvent", new Class[]{Integer.TYPE, Object[].class});
            Object[] objArr = new Object[2];
            objArr[0] = Integer.valueOf(1397638484);
            objArr[1] = new Object[]{"conscrypt", Integer.valueOf(uid), message};
            writeEventMethod.invoke(eventLogInstance, objArr);
        } catch (Exception e) {
        }
    }

    public static boolean isLiteralIpAddress(String hostname) {
        return InetAddress.isNumeric(hostname);
    }

    public static SSLSocketFactory wrapSocketFactoryIfNeeded(OpenSSLSocketFactoryImpl factory) {
        return factory;
    }

    public static GCMParameters fromGCMParameterSpec(AlgorithmParameterSpec params) {
        if (!(params instanceof GCMParameterSpec)) {
            return null;
        }
        GCMParameterSpec gcmParams = (GCMParameterSpec) params;
        return new GCMParameters(gcmParams.getTLen(), gcmParams.getIV());
    }

    public static AlgorithmParameterSpec toGCMParameterSpec(int tagLenInBits, byte[] iv) {
        return new GCMParameterSpec(tagLenInBits, iv);
    }

    public static CloseGuard closeGuardGet() {
        return CloseGuard.get();
    }

    public static void closeGuardOpen(Object guardObj, String message) {
        ((CloseGuard) guardObj).open(message);
    }

    public static void closeGuardClose(Object guardObj) {
        ((CloseGuard) guardObj).close();
    }

    public static void closeGuardWarnIfOpen(Object guardObj) {
        ((CloseGuard) guardObj).warnIfOpen();
    }

    public static void blockGuardOnNetwork() {
        BlockGuard.getThreadPolicy().onNetwork();
    }

    public static String oidToAlgorithmName(String oid) {
        try {
            return AlgorithmId.get(oid).getName();
        } catch (NoSuchAlgorithmException e) {
            return oid;
        }
    }

    public static SSLSession wrapSSLSession(OpenSSLSessionImpl sslSession) {
        return new OpenSSLExtendedSessionImpl(sslSession);
    }

    public static String getHostStringFromInetSocketAddress(InetSocketAddress addr) {
        return addr.getHostString();
    }
}
