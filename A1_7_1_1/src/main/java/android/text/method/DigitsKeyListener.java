package android.text.method;

import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.InputDevice;
import com.android.internal.telephony.PhoneConstants;

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
public class DigitsKeyListener extends NumberKeyListener {
    private static final char[][] CHARACTERS = null;
    private static final int DECIMAL = 2;
    private static final int SIGN = 1;
    private static DigitsKeyListener[] sInstance;
    private char[] mAccepted;
    private boolean mDecimal;
    private boolean mSign;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.text.method.DigitsKeyListener.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.text.method.DigitsKeyListener.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.text.method.DigitsKeyListener.<clinit>():void");
    }

    protected char[] getAcceptedChars() {
        return this.mAccepted;
    }

    private static boolean isSignChar(char c) {
        return c == '-' || c == '+';
    }

    private static boolean isDecimalPointChar(char c) {
        return c == '.';
    }

    public DigitsKeyListener() {
        this(false, false);
    }

    public DigitsKeyListener(boolean sign, boolean decimal) {
        int i;
        int i2 = 0;
        this.mSign = sign;
        this.mDecimal = decimal;
        if (sign) {
            i = 1;
        } else {
            i = 0;
        }
        if (decimal) {
            i2 = 2;
        }
        this.mAccepted = CHARACTERS[i | i2];
    }

    public static DigitsKeyListener getInstance() {
        return getInstance(false, false);
    }

    public static DigitsKeyListener getInstance(boolean sign, boolean decimal) {
        int i;
        int i2 = 0;
        if (sign) {
            i = 1;
        } else {
            i = 0;
        }
        if (decimal) {
            i2 = 2;
        }
        int kind = i | i2;
        if (sInstance[kind] != null) {
            return sInstance[kind];
        }
        sInstance[kind] = new DigitsKeyListener(sign, decimal);
        return sInstance[kind];
    }

    public static DigitsKeyListener getInstance(String accepted) {
        DigitsKeyListener dim = new DigitsKeyListener();
        dim.mAccepted = new char[accepted.length()];
        accepted.getChars(0, accepted.length(), dim.mAccepted, 0);
        return dim;
    }

    public int getInputType() {
        int contentType = 2;
        if (this.mSign) {
            contentType = InputDevice.SOURCE_TOUCHSCREEN;
        }
        if (this.mDecimal) {
            return contentType | 8192;
        }
        return contentType;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        CharSequence out = super.filter(source, start, end, dest, dstart, dend);
        if (!this.mSign && !this.mDecimal) {
            return out;
        }
        int i;
        char c;
        if (out != null) {
            source = out;
            start = 0;
            end = out.length();
        }
        int sign = -1;
        int decimal = -1;
        int dlen = dest.length();
        for (i = 0; i < dstart; i++) {
            c = dest.charAt(i);
            if (isSignChar(c)) {
                sign = i;
            } else if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        for (i = dend; i < dlen; i++) {
            c = dest.charAt(i);
            if (isSignChar(c)) {
                return PhoneConstants.MVNO_TYPE_NONE;
            }
            if (isDecimalPointChar(c)) {
                decimal = i;
            }
        }
        CharSequence stripped = null;
        for (i = end - 1; i >= start; i--) {
            c = source.charAt(i);
            boolean strip = false;
            if (isSignChar(c)) {
                if (i != start || dstart != 0) {
                    strip = true;
                } else if (sign >= 0) {
                    strip = true;
                } else {
                    sign = i;
                }
            } else if (isDecimalPointChar(c)) {
                if (decimal >= 0) {
                    strip = true;
                } else {
                    decimal = i;
                }
            }
            if (strip) {
                if (end == start + 1) {
                    return PhoneConstants.MVNO_TYPE_NONE;
                }
                if (stripped == null) {
                    stripped = new SpannableStringBuilder(source, start, end);
                }
                stripped.delete(i - start, (i + 1) - start);
            }
        }
        if (stripped != null) {
            return stripped;
        }
        if (out != null) {
            return out;
        }
        return null;
    }
}
