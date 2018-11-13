package java.nio.charset;

import java.lang.ref.WeakReference;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.util.HashMap;
import java.util.Map;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class CoderResult {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f36-assertionsDisabled = false;
    private static final int CR_ERROR_MIN = 2;
    private static final int CR_MALFORMED = 2;
    private static final int CR_OVERFLOW = 1;
    private static final int CR_UNDERFLOW = 0;
    private static final int CR_UNMAPPABLE = 3;
    public static final CoderResult OVERFLOW = null;
    public static final CoderResult UNDERFLOW = null;
    private static Cache malformedCache;
    private static final String[] names = null;
    private static Cache unmappableCache;
    private final int length;
    private final int type;

    private static abstract class Cache {
        private Map<Integer, WeakReference<CoderResult>> cache;

        /* synthetic */ Cache(Cache cache) {
            this();
        }

        protected abstract CoderResult create(int i);

        private Cache() {
            this.cache = null;
        }

        private synchronized CoderResult get(int len) {
            CoderResult e;
            if (len <= 0) {
                throw new IllegalArgumentException("Non-positive length");
            }
            Integer k = new Integer(len);
            e = null;
            if (this.cache == null) {
                this.cache = new HashMap();
            } else {
                WeakReference<CoderResult> w = (WeakReference) this.cache.get(k);
                if (w != null) {
                    e = (CoderResult) w.get();
                }
            }
            if (e == null) {
                e = create(len);
                this.cache.put(k, new WeakReference(e));
            }
            return e;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.nio.charset.CoderResult.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.nio.charset.CoderResult.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.nio.charset.CoderResult.<clinit>():void");
    }

    /* synthetic */ CoderResult(int type, int length, CoderResult coderResult) {
        this(type, length);
    }

    private CoderResult(int type, int length) {
        this.type = type;
        this.length = length;
    }

    public String toString() {
        String nm = names[this.type];
        return isError() ? nm + "[" + this.length + "]" : nm;
    }

    public boolean isUnderflow() {
        return this.type == 0 ? true : f36-assertionsDisabled;
    }

    public boolean isOverflow() {
        return this.type == 1 ? true : f36-assertionsDisabled;
    }

    public boolean isError() {
        return this.type >= 2 ? true : f36-assertionsDisabled;
    }

    public boolean isMalformed() {
        return this.type == 2 ? true : f36-assertionsDisabled;
    }

    public boolean isUnmappable() {
        return this.type == 3 ? true : f36-assertionsDisabled;
    }

    public int length() {
        if (isError()) {
            return this.length;
        }
        throw new UnsupportedOperationException();
    }

    public static CoderResult malformedForLength(int length) {
        return malformedCache.get(length);
    }

    public static CoderResult unmappableForLength(int length) {
        return unmappableCache.get(length);
    }

    public void throwException() throws CharacterCodingException {
        switch (this.type) {
            case 0:
                throw new BufferUnderflowException();
            case 1:
                throw new BufferOverflowException();
            case 2:
                throw new MalformedInputException(this.length);
            case 3:
                throw new UnmappableCharacterException(this.length);
            default:
                if (!f36-assertionsDisabled) {
                    throw new AssertionError();
                }
                return;
        }
    }
}
