package android.icu.lang;

import android.icu.text.UTF16;
import dalvik.bytecode.Opcodes;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
@Deprecated
public class CharSequences {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.lang.CharSequences.<init>():void, dex: 
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
    private CharSequences() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.lang.CharSequences.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.CharSequences.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.lang.CharSequences.equals(java.lang.Object, java.lang.Object):boolean, dex: 
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
    @java.lang.Deprecated
    public static final <T> boolean equals(T r1, T r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.lang.CharSequences.equals(java.lang.Object, java.lang.Object):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.CharSequences.equals(java.lang.Object, java.lang.Object):boolean");
    }

    @Deprecated
    public static int matchAfter(CharSequence a, CharSequence b, int aIndex, int bIndex) {
        int i = aIndex;
        int j = bIndex;
        int alen = a.length();
        int blen = b.length();
        while (i < alen && j < blen && a.charAt(i) == b.charAt(j)) {
            i++;
            j++;
        }
        int result = i - aIndex;
        if (result == 0 || onCharacterBoundary(a, i) || onCharacterBoundary(b, j)) {
            return result;
        }
        return result - 1;
    }

    @Deprecated
    public int codePointLength(CharSequence s) {
        return Character.codePointCount(s, 0, s.length());
    }

    @Deprecated
    public static final boolean equals(int codepoint, CharSequence other) {
        boolean z = true;
        boolean z2 = false;
        if (other == null) {
            return false;
        }
        switch (other.length()) {
            case 1:
                if (codepoint != other.charAt(0)) {
                    z = false;
                }
                return z;
            case 2:
                if (codepoint > 65535 && codepoint == Character.codePointAt(other, 0)) {
                    z2 = true;
                }
                return z2;
            default:
                return false;
        }
    }

    @Deprecated
    public static final boolean equals(CharSequence other, int codepoint) {
        return equals(codepoint, other);
    }

    @Deprecated
    public static int compare(CharSequence string, int codePoint) {
        if (codePoint < 0 || codePoint > 1114111) {
            throw new IllegalArgumentException();
        }
        int stringLength = string.length();
        if (stringLength == 0) {
            return -1;
        }
        char firstChar = string.charAt(0);
        int offset = codePoint - 65536;
        int result;
        if (offset < 0) {
            result = firstChar - codePoint;
            if (result != 0) {
                return result;
            }
            return stringLength - 1;
        }
        result = firstChar - ((char) ((offset >>> 10) + 55296));
        if (result != 0) {
            return result;
        }
        if (stringLength > 1) {
            result = string.charAt(1) - ((char) ((offset & Opcodes.OP_NEW_INSTANCE_JUMBO) + UTF16.TRAIL_SURROGATE_MIN_VALUE));
            if (result != 0) {
                return result;
            }
        }
        return stringLength - 2;
    }

    @Deprecated
    public static int compare(int codepoint, CharSequence a) {
        return -compare(a, codepoint);
    }

    @Deprecated
    public static int getSingleCodePoint(CharSequence s) {
        int i = 1;
        int length = s.length();
        if (length < 1 || length > 2) {
            return Integer.MAX_VALUE;
        }
        int result = Character.codePointAt(s, 0);
        int i2 = result < 65536 ? 1 : 0;
        if (length != 1) {
            i = 0;
        }
        if (i2 != i) {
            result = Integer.MAX_VALUE;
        }
        return result;
    }

    @Deprecated
    public static int compare(CharSequence a, CharSequence b) {
        int alength = a.length();
        int blength = b.length();
        int min = alength <= blength ? alength : blength;
        for (int i = 0; i < min; i++) {
            int diff = a.charAt(i) - b.charAt(i);
            if (diff != 0) {
                return diff;
            }
        }
        return alength - blength;
    }

    @Deprecated
    public static boolean equalsChars(CharSequence a, CharSequence b) {
        return a.length() == b.length() && compare(a, b) == 0;
    }

    @Deprecated
    public static boolean onCharacterBoundary(CharSequence s, int i) {
        if (i <= 0 || i >= s.length() || !Character.isHighSurrogate(s.charAt(i - 1))) {
            return true;
        }
        if (Character.isLowSurrogate(s.charAt(i))) {
            return false;
        }
        return true;
    }

    @Deprecated
    public static int indexOf(CharSequence s, int codePoint) {
        int i = 0;
        while (i < s.length()) {
            int cp = Character.codePointAt(s, i);
            if (cp == codePoint) {
                return i;
            }
            i += Character.charCount(cp);
        }
        return -1;
    }

    @Deprecated
    public static int[] codePoints(CharSequence s) {
        int[] result = new int[s.length()];
        int j = 0;
        int i = 0;
        while (i < s.length()) {
            char cp = s.charAt(i);
            if (cp >= UCharacter.MIN_LOW_SURROGATE && cp <= 57343 && i != 0) {
                char last = (char) result[j - 1];
                if (last >= 55296 && last <= UCharacter.MAX_HIGH_SURROGATE) {
                    result[j - 1] = Character.toCodePoint(last, cp);
                    i++;
                }
            }
            int j2 = j + 1;
            result[j] = cp;
            j = j2;
            i++;
        }
        if (j == result.length) {
            return result;
        }
        int[] shortResult = new int[j];
        System.arraycopy(result, 0, shortResult, 0, j);
        return shortResult;
    }
}
