package android.net.http;

import java.security.cert.X509Certificate;

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
public class SslError {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f25-assertionsDisabled = false;
    public static final int SSL_DATE_INVALID = 4;
    public static final int SSL_EXPIRED = 1;
    public static final int SSL_IDMISMATCH = 2;
    public static final int SSL_INVALID = 5;
    @Deprecated
    public static final int SSL_MAX_ERROR = 6;
    public static final int SSL_NOTYETVALID = 0;
    public static final int SSL_UNTRUSTED = 3;
    final SslCertificate mCertificate;
    int mErrors;
    final String mUrl;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.http.SslError.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.http.SslError.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.http.SslError.<clinit>():void");
    }

    @Deprecated
    public SslError(int error, SslCertificate certificate) {
        this(error, certificate, "");
    }

    @Deprecated
    public SslError(int error, X509Certificate certificate) {
        this(error, certificate, "");
    }

    public SslError(int error, SslCertificate certificate, String url) {
        Object obj = 1;
        if (!f25-assertionsDisabled) {
            if ((certificate != null ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!f25-assertionsDisabled) {
            if (url == null) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        addError(error);
        this.mCertificate = certificate;
        this.mUrl = url;
    }

    public SslError(int error, X509Certificate certificate, String url) {
        this(error, new SslCertificate(certificate), url);
    }

    public static SslError SslErrorFromChromiumErrorCode(int error, SslCertificate cert, String url) {
        Object obj = null;
        if (!f25-assertionsDisabled) {
            if (error >= -299 && error <= -200) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        if (error == -200) {
            return new SslError(2, cert, url);
        }
        if (error == -201) {
            return new SslError(4, cert, url);
        }
        if (error == -202) {
            return new SslError(3, cert, url);
        }
        return new SslError(5, cert, url);
    }

    public SslCertificate getCertificate() {
        return this.mCertificate;
    }

    public String getUrl() {
        return this.mUrl;
    }

    public boolean addError(int error) {
        boolean rval = false;
        if (error >= 0 && error < 6) {
            rval = true;
        }
        if (rval) {
            this.mErrors = (1 << error) | this.mErrors;
        }
        return rval;
    }

    public boolean hasError(int error) {
        boolean rval = false;
        if (error >= 0 && error < 6) {
            rval = true;
        }
        if (!rval) {
            return rval;
        }
        return ((1 << error) & this.mErrors) != 0;
    }

    public int getPrimaryError() {
        if (this.mErrors != 0) {
            for (int error = 5; error >= 0; error--) {
                if ((this.mErrors & (1 << error)) != 0) {
                    return error;
                }
            }
            if (!f25-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        return -1;
    }

    public String toString() {
        return "primary error: " + getPrimaryError() + " certificate: " + getCertificate() + " on URL: " + getUrl();
    }
}
