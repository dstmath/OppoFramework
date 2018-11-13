package java.lang;

import android.icu.text.Transliterator;
import java.util.Locale;
import libcore.icu.ICU;

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
class CaseMapper {
    private static final ThreadLocal<Transliterator> EL_UPPER = null;
    private static final char GREEK_CAPITAL_SIGMA = 'Σ';
    private static final char GREEK_SMALL_FINAL_SIGMA = 'ς';
    private static final char LATIN_CAPITAL_I_WITH_DOT = 'İ';
    private static final char[] upperValues = null;
    private static final char[] upperValues2 = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.lang.CaseMapper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.lang.CaseMapper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.CaseMapper.<clinit>():void");
    }

    private CaseMapper() {
    }

    public static String toLowerCase(Locale locale, String s) {
        String languageCode = locale.getLanguage();
        if (languageCode.equals("tr") || languageCode.equals("az") || languageCode.equals("lt")) {
            return ICU.toLowerCase(s, locale);
        }
        String newString = null;
        int i = 0;
        int end = s.length();
        while (i < end) {
            char ch = s.charAt(i);
            if (ch == 304 || Character.isHighSurrogate(ch)) {
                return ICU.toLowerCase(s, locale);
            }
            char newCh;
            if (ch == GREEK_CAPITAL_SIGMA && isFinalSigma(s, i)) {
                newCh = GREEK_SMALL_FINAL_SIGMA;
            } else {
                newCh = Character.toLowerCase(ch);
            }
            if (ch != newCh) {
                if (newString == null) {
                    newString = StringFactory.newStringFromString(s);
                }
                newString.setCharAt(i, newCh);
            }
            i++;
        }
        if (newString == null) {
            newString = s;
        }
        return newString;
    }

    private static boolean isFinalSigma(String s, int index) {
        if (index <= 0) {
            return false;
        }
        boolean z;
        char previous = s.charAt(index - 1);
        if (Character.isLowerCase(previous) || Character.isUpperCase(previous)) {
            z = true;
        } else {
            z = Character.isTitleCase(previous);
        }
        if (!z) {
            return false;
        }
        if (index + 1 >= s.length()) {
            return true;
        }
        char next = s.charAt(index + 1);
        return (Character.isLowerCase(next) || Character.isUpperCase(next) || Character.isTitleCase(next)) ? false : true;
    }

    private static int upperIndex(int ch) {
        int index = -1;
        if (ch >= 223) {
            if (ch <= 1415) {
                switch (ch) {
                    case 223:
                        return 0;
                    case 329:
                        return 1;
                    case 496:
                        return 2;
                    case 912:
                        return 3;
                    case 944:
                        return 4;
                    case 1415:
                        return 5;
                }
            } else if (ch >= 7830) {
                if (ch <= 7834) {
                    index = (ch + 6) - 7830;
                } else if (ch >= 8016 && ch <= 8188) {
                    index = upperValues2[ch - 8016];
                    if (index == 0) {
                        index = -1;
                    }
                } else if (ch >= 64256) {
                    if (ch <= 64262) {
                        index = (ch + 90) - 64256;
                    } else if (ch >= 64275 && ch <= 64279) {
                        index = (ch + 97) - 64275;
                    }
                }
            }
        }
        return index;
    }

    public static String toUpperCase(Locale locale, String s, int count) {
        String languageCode = locale.getLanguage();
        if (languageCode.equals("tr") || languageCode.equals("az") || languageCode.equals("lt")) {
            return ICU.toUpperCase(s, locale);
        }
        if (languageCode.equals("el")) {
            return ((Transliterator) EL_UPPER.get()).transliterate(s);
        }
        char[] output = null;
        String newString = null;
        int i = 0;
        int o = 0;
        int end = count;
        while (true) {
            int i2 = i;
            if (o < count) {
                char ch = s.charAt(o);
                if (Character.isHighSurrogate(ch)) {
                    return ICU.toUpperCase(s, locale);
                }
                int index = upperIndex(ch);
                char[] newoutput;
                if (index == -1) {
                    if (output != null && i2 >= output.length) {
                        newoutput = new char[((output.length + (count / 6)) + 2)];
                        System.arraycopy(output, 0, newoutput, 0, output.length);
                        output = newoutput;
                    }
                    char upch = Character.toUpperCase(ch);
                    if (output != null) {
                        i = i2 + 1;
                        output[i2] = upch;
                    } else if (ch != upch) {
                        if (newString == null) {
                            newString = StringFactory.newStringFromString(s);
                        }
                        newString.setCharAt(o, upch);
                        i = i2;
                    } else {
                        i = i2;
                    }
                } else {
                    int target = index * 3;
                    char val3 = upperValues[target + 2];
                    if (output == null) {
                        output = new char[(((count / 6) + count) + 2)];
                        i = o;
                        if (newString != null) {
                            System.arraycopy(newString.toCharArray(), 0, output, 0, i);
                        } else {
                            System.arraycopy(s.toCharArray(), 0, output, 0, i);
                        }
                    } else {
                        if ((val3 == 0 ? 1 : 2) + i2 >= output.length) {
                            newoutput = new char[((output.length + (count / 6)) + 3)];
                            System.arraycopy(output, 0, newoutput, 0, output.length);
                            output = newoutput;
                            i = i2;
                        } else {
                            i = i2;
                        }
                    }
                    i2 = i + 1;
                    output[i] = upperValues[target];
                    i = i2 + 1;
                    output[i2] = upperValues[target + 1];
                    if (val3 != 0) {
                        i2 = i + 1;
                        output[i] = val3;
                        i = i2;
                    }
                }
                o++;
            } else if (output != null) {
                String str;
                if (output.length == i2 || output.length - i2 < 8) {
                    str = new String(0, i2, output);
                } else {
                    str = new String(output, 0, i2);
                }
                return r17;
            } else if (newString != null) {
                return newString;
            } else {
                return s;
            }
        }
    }
}
