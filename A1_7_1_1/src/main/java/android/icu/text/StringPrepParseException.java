package android.icu.text;

import java.text.ParseException;

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
public class StringPrepParseException extends ParseException {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f23-assertionsDisabled = false;
    public static final int ACE_PREFIX_ERROR = 6;
    public static final int BUFFER_OVERFLOW_ERROR = 9;
    public static final int CHECK_BIDI_ERROR = 4;
    public static final int DOMAIN_NAME_TOO_LONG_ERROR = 11;
    public static final int ILLEGAL_CHAR_FOUND = 1;
    public static final int INVALID_CHAR_FOUND = 0;
    public static final int LABEL_TOO_LONG_ERROR = 8;
    private static final int PARSE_CONTEXT_LEN = 16;
    public static final int PROHIBITED_ERROR = 2;
    public static final int STD3_ASCII_RULES_ERROR = 5;
    public static final int UNASSIGNED_ERROR = 3;
    public static final int VERIFICATION_ERROR = 7;
    public static final int ZERO_LENGTH_LABEL = 10;
    static final long serialVersionUID = 7160264827701651255L;
    private int error;
    private int line;
    private StringBuffer postContext;
    private StringBuffer preContext;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.text.StringPrepParseException.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.text.StringPrepParseException.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.StringPrepParseException.<clinit>():void");
    }

    public StringPrepParseException(String message, int error) {
        super(message, -1);
        this.preContext = new StringBuffer();
        this.postContext = new StringBuffer();
        this.error = error;
        this.line = 0;
    }

    public StringPrepParseException(String message, int error, String rules, int pos) {
        super(message, -1);
        this.preContext = new StringBuffer();
        this.postContext = new StringBuffer();
        this.error = error;
        setContext(rules, pos);
        this.line = 0;
    }

    public StringPrepParseException(String message, int error, String rules, int pos, int lineNumber) {
        super(message, -1);
        this.preContext = new StringBuffer();
        this.postContext = new StringBuffer();
        this.error = error;
        setContext(rules, pos);
        this.line = lineNumber;
    }

    public boolean equals(Object other) {
        boolean z = false;
        if (!(other instanceof StringPrepParseException)) {
            return false;
        }
        if (((StringPrepParseException) other).error == this.error) {
            z = true;
        }
        return z;
    }

    @Deprecated
    public int hashCode() {
        if (f23-assertionsDisabled) {
            return 42;
        }
        throw new AssertionError("hashCode not designed");
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(super.getMessage());
        buf.append(". line:  ");
        buf.append(this.line);
        buf.append(". preContext:  ");
        buf.append(this.preContext);
        buf.append(". postContext: ");
        buf.append(this.postContext);
        buf.append("\n");
        return buf.toString();
    }

    private void setPreContext(String str, int pos) {
        setPreContext(str.toCharArray(), pos);
    }

    private void setPreContext(char[] str, int pos) {
        int start = pos <= 16 ? 0 : pos - 15;
        this.preContext.append(str, start, start <= 16 ? start : 16);
    }

    private void setPostContext(String str, int pos) {
        setPostContext(str.toCharArray(), pos);
    }

    private void setPostContext(char[] str, int pos) {
        int start = pos;
        this.postContext.append(str, pos, str.length - pos);
    }

    private void setContext(String str, int pos) {
        setPreContext(str, pos);
        setPostContext(str, pos);
    }

    public int getError() {
        return this.error;
    }
}
