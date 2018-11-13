package android.icu.impl;

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
public final class PatternProps {
    private static final byte[] index2000 = null;
    private static final byte[] latin1 = null;
    private static final int[] syntax2000 = null;
    private static final int[] syntaxOrWhiteSpace2000 = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.impl.PatternProps.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.impl.PatternProps.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.PatternProps.<clinit>():void");
    }

    public static boolean isSyntax(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] != (byte) 3) {
                z = false;
            }
            return z;
        } else if (c < 8208) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntax2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) == 0) {
                    z = false;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c > 64831 && 65093 > c) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static boolean isSyntaxOrWhiteSpace(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] == (byte) 0) {
                z = false;
            }
            return z;
        } else if (c < 8206) {
            return false;
        } else {
            if (c <= 12336) {
                if (((syntaxOrWhiteSpace2000[index2000[(c - 8192) >> 5]] >> (c & 31)) & 1) == 0) {
                    z = false;
                }
                return z;
            } else if (64830 > c || c > 65094) {
                return false;
            } else {
                if (c > 64831 && 65093 > c) {
                    z = false;
                }
                return z;
            }
        }
    }

    public static boolean isWhiteSpace(int c) {
        boolean z = true;
        if (c < 0) {
            return false;
        }
        if (c <= 255) {
            if (latin1[c] != (byte) 5) {
                z = false;
            }
            return z;
        } else if (8206 > c || c > 8233) {
            return false;
        } else {
            if (c > 8207 && 8232 > c) {
                z = false;
            }
            return z;
        }
    }

    public static int skipWhiteSpace(CharSequence s, int i) {
        while (i < s.length() && isWhiteSpace(s.charAt(i))) {
            i++;
        }
        return i;
    }

    public static String trimWhiteSpace(String s) {
        if (s.length() == 0 || (!isWhiteSpace(s.charAt(0)) && !isWhiteSpace(s.charAt(s.length() - 1)))) {
            return s;
        }
        int start = 0;
        int limit = s.length();
        while (start < limit && isWhiteSpace(s.charAt(start))) {
            start++;
        }
        if (start < limit) {
            while (isWhiteSpace(s.charAt(limit - 1))) {
                limit--;
            }
        }
        return s.substring(start, limit);
    }

    public static boolean isIdentifier(CharSequence s) {
        int limit = s.length();
        if (limit == 0) {
            return false;
        }
        int start = 0;
        while (true) {
            int start2 = start + 1;
            if (isSyntaxOrWhiteSpace(s.charAt(start))) {
                return false;
            }
            if (start2 >= limit) {
                return true;
            }
            start = start2;
        }
    }

    public static boolean isIdentifier(CharSequence s, int start, int limit) {
        if (start >= limit) {
            return false;
        }
        while (true) {
            int start2 = start + 1;
            if (isSyntaxOrWhiteSpace(s.charAt(start))) {
                return false;
            }
            if (start2 >= limit) {
                return true;
            }
            start = start2;
        }
    }

    public static int skipIdentifier(CharSequence s, int i) {
        while (i < s.length() && !isSyntaxOrWhiteSpace(s.charAt(i))) {
            i++;
        }
        return i;
    }
}
