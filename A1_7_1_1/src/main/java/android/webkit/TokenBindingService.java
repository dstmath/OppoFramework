package android.webkit;

import android.net.Uri;
import java.security.KeyPair;

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
public abstract class TokenBindingService {
    public static final String KEY_ALGORITHM_ECDSAP256 = "ECDSAP256";
    public static final String KEY_ALGORITHM_RSA2048_PKCS_1_5 = "RSA2048_PKCS_1.5";
    public static final String KEY_ALGORITHM_RSA2048_PSS = "RSA2048PSS";

    public static abstract class TokenBindingKey {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.webkit.TokenBindingService.TokenBindingKey.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public TokenBindingKey() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.webkit.TokenBindingService.TokenBindingKey.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.webkit.TokenBindingService.TokenBindingKey.<init>():void");
        }

        public abstract String getAlgorithm();

        public abstract KeyPair getKeyPair();
    }

    public abstract void deleteAllKeys(ValueCallback<Boolean> valueCallback);

    public abstract void deleteKey(Uri uri, ValueCallback<Boolean> valueCallback);

    public abstract void enableTokenBinding();

    public abstract void getKey(Uri uri, String[] strArr, ValueCallback<TokenBindingKey> valueCallback);

    public static TokenBindingService getInstance() {
        return WebViewFactory.getProvider().getTokenBindingService();
    }
}
