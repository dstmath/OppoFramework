package android.text.method;

import android.text.Spannable;
import android.view.KeyCharacterMap.KeyData;
import android.view.KeyEvent;

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
public class DialerKeyListener extends NumberKeyListener {
    public static final char[] CHARACTERS = null;
    private static DialerKeyListener sInstance;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.text.method.DialerKeyListener.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.text.method.DialerKeyListener.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.DialerKeyListener.<clinit>():void");
    }

    protected char[] getAcceptedChars() {
        return CHARACTERS;
    }

    public static DialerKeyListener getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        sInstance = new DialerKeyListener();
        return sInstance;
    }

    public int getInputType() {
        return 3;
    }

    protected int lookup(KeyEvent event, Spannable content) {
        int meta = MetaKeyKeyListener.getMetaState((CharSequence) content, event);
        int number = event.getNumber();
        if ((meta & 3) == 0 && number != 0) {
            return number;
        }
        int match = super.lookup(event, content);
        if (match != 0) {
            return match;
        }
        if (meta != 0) {
            KeyData kd = new KeyData();
            char[] accepted = getAcceptedChars();
            if (event.getKeyData(kd)) {
                for (int i = 1; i < kd.meta.length; i++) {
                    if (NumberKeyListener.ok(accepted, kd.meta[i])) {
                        return kd.meta[i];
                    }
                }
            }
        }
        return number;
    }
}
