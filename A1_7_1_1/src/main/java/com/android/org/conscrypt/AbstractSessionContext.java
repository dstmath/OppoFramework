package com.android.org.conscrypt;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSessionContext;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
abstract class AbstractSessionContext implements SSLSessionContext {
    private static final int DEFAULT_SESSION_TIMEOUT_SECONDS = 28800;
    static final int OPEN_SSL = 1;
    volatile int maximumSize;
    private final Map<ByteArray, SSLSession> sessions;
    final long sslCtxNativePointer;
    volatile int timeout;

    /* renamed from: com.android.org.conscrypt.AbstractSessionContext$2 */
    class AnonymousClass2 implements Enumeration<byte[]> {
        private SSLSession next;
        final /* synthetic */ AbstractSessionContext this$0;
        final /* synthetic */ Iterator val$i;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.org.conscrypt.AbstractSessionContext.2.<init>(com.android.org.conscrypt.AbstractSessionContext, java.util.Iterator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        AnonymousClass2(com.android.org.conscrypt.AbstractSessionContext r1, java.util.Iterator r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.org.conscrypt.AbstractSessionContext.2.<init>(com.android.org.conscrypt.AbstractSessionContext, java.util.Iterator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.AbstractSessionContext.2.<init>(com.android.org.conscrypt.AbstractSessionContext, java.util.Iterator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.AbstractSessionContext.2.hasMoreElements():boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public boolean hasMoreElements() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.org.conscrypt.AbstractSessionContext.2.hasMoreElements():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.AbstractSessionContext.2.hasMoreElements():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.AbstractSessionContext.2.nextElement():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.AbstractSessionContext.2.nextElement():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.AbstractSessionContext.2.nextElement():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.AbstractSessionContext.2.nextElement():byte[], dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public byte[] nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.AbstractSessionContext.2.nextElement():byte[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.AbstractSessionContext.2.nextElement():byte[]");
        }
    }

    protected abstract void sessionRemoved(SSLSession sSLSession);

    AbstractSessionContext(int maximumSize) {
        this.timeout = DEFAULT_SESSION_TIMEOUT_SECONDS;
        this.sslCtxNativePointer = NativeCrypto.SSL_CTX_new();
        this.sessions = new LinkedHashMap<ByteArray, SSLSession>() {
            protected boolean removeEldestEntry(Entry<ByteArray, SSLSession> eldest) {
                boolean remove;
                if (AbstractSessionContext.this.maximumSize <= 0 || size() <= AbstractSessionContext.this.maximumSize) {
                    remove = false;
                } else {
                    remove = true;
                }
                if (remove) {
                    remove(eldest.getKey());
                    AbstractSessionContext.this.sessionRemoved((SSLSession) eldest.getValue());
                }
                return false;
            }
        };
        this.maximumSize = maximumSize;
    }

    private Iterator<SSLSession> sessionIterator() {
        Iterator<SSLSession> it;
        synchronized (this.sessions) {
            it = Arrays.asList((SSLSession[]) this.sessions.values().toArray(new SSLSession[this.sessions.size()])).iterator();
        }
        return it;
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public final java.util.Enumeration<byte[]> getIds() {
        /*
        r2 = this;
        r0 = r2.sessionIterator();
        r1 = new com.android.org.conscrypt.AbstractSessionContext$2;
        r1.<init>(r2, r0);
        return r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.AbstractSessionContext.getIds():java.util.Enumeration<byte[]>");
    }

    public final int getSessionCacheSize() {
        return this.maximumSize;
    }

    public final int getSessionTimeout() {
        return this.timeout;
    }

    protected void trimToSize() {
        synchronized (this.sessions) {
            int size = this.sessions.size();
            if (size > this.maximumSize) {
                int removals = size - this.maximumSize;
                Iterator<SSLSession> i = this.sessions.values().iterator();
                do {
                    SSLSession session = (SSLSession) i.next();
                    i.remove();
                    sessionRemoved(session);
                    removals--;
                } while (removals > 0);
            }
        }
    }

    public void setSessionTimeout(int seconds) throws IllegalArgumentException {
        if (seconds < 0) {
            throw new IllegalArgumentException("seconds < 0");
        }
        this.timeout = seconds;
        synchronized (this.sessions) {
            Iterator<SSLSession> i = this.sessions.values().iterator();
            while (i.hasNext()) {
                SSLSession session = (SSLSession) i.next();
                if (!session.isValid()) {
                    i.remove();
                    sessionRemoved(session);
                }
            }
        }
    }

    public final void setSessionCacheSize(int size) throws IllegalArgumentException {
        if (size < 0) {
            throw new IllegalArgumentException("size < 0");
        }
        int oldMaximum = this.maximumSize;
        this.maximumSize = size;
        if (size < oldMaximum) {
            trimToSize();
        }
    }

    byte[] toBytes(SSLSession session) {
        if (!(session instanceof OpenSSLSessionImpl)) {
            return null;
        }
        OpenSSLSessionImpl sslSession = (OpenSSLSessionImpl) session;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream daos = new DataOutputStream(baos);
            daos.writeInt(1);
            byte[] data = sslSession.getEncoded();
            daos.writeInt(data.length);
            daos.write(data);
            Certificate[] certs = session.getPeerCertificates();
            daos.writeInt(certs.length);
            for (Certificate cert : certs) {
                data = cert.getEncoded();
                daos.writeInt(data.length);
                daos.write(data);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            System.err.println("Failed to convert saved SSL Session: " + e.getMessage());
            return null;
        } catch (CertificateEncodingException e2) {
            log(e2);
            return null;
        }
    }

    OpenSSLSessionImpl toSession(byte[] data, String host, int port) {
        DataInputStream dais = new DataInputStream(new ByteArrayInputStream(data));
        try {
            int type = dais.readInt();
            if (type != 1) {
                log(new AssertionError("Unexpected type ID: " + type));
                return null;
            }
            byte[] sessionData = new byte[dais.readInt()];
            dais.readFully(sessionData);
            int count = dais.readInt();
            X509Certificate[] certs = new X509Certificate[count];
            for (int i = 0; i < count; i++) {
                byte[] certData = new byte[dais.readInt()];
                dais.readFully(certData);
                certs[i] = OpenSSLX509Certificate.fromX509Der(certData);
            }
            return new OpenSSLSessionImpl(sessionData, host, port, certs, this);
        } catch (IOException e) {
            log(e);
            return null;
        }
    }

    protected SSLSession wrapSSLSessionIfNeeded(SSLSession session) {
        if (session instanceof OpenSSLSessionImpl) {
            return Platform.wrapSSLSession((OpenSSLSessionImpl) session);
        }
        return session;
    }

    public SSLSession getSession(byte[] sessionId) {
        if (sessionId == null) {
            throw new NullPointerException("sessionId == null");
        }
        SSLSession session;
        ByteArray key = new ByteArray(sessionId);
        synchronized (this.sessions) {
            session = (SSLSession) this.sessions.get(key);
        }
        if (session == null || !session.isValid()) {
            return null;
        }
        if (session instanceof OpenSSLSessionImpl) {
            return Platform.wrapSSLSession((OpenSSLSessionImpl) session);
        }
        return session;
    }

    void putSession(SSLSession session) {
        byte[] id = session.getId();
        if (id.length != 0) {
            ByteArray key = new ByteArray(id);
            synchronized (this.sessions) {
                this.sessions.put(key, session);
            }
        }
    }

    static void log(Throwable t) {
        new Exception("Error converting session", t).printStackTrace();
    }

    protected void finalize() throws Throwable {
        try {
            NativeCrypto.SSL_CTX_free(this.sslCtxNativePointer);
        } finally {
            super.finalize();
        }
    }
}
