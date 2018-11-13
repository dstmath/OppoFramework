package android.icu.impl;

import android.icu.text.PluralRules;
import java.util.List;

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
/*  JADX ERROR: NullPointerException in pass: EnumVisitor
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public enum StandardPlural {
    ;
    
    public static final int COUNT = 0;
    public static final int OTHER_INDEX = 0;
    public static final List<StandardPlural> VALUES = null;
    private final String keyword;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.StandardPlural.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.icu.impl.StandardPlural.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.impl.StandardPlural.<clinit>():void");
    }

    private StandardPlural(String kw) {
        this.keyword = kw;
    }

    public final String getKeyword() {
        return this.keyword;
    }

    public static final StandardPlural orNullFromString(CharSequence keyword) {
        switch (keyword.length()) {
            case 3:
                if (PluralRules.KEYWORD_ONE.contentEquals(keyword)) {
                    return ONE;
                }
                if (PluralRules.KEYWORD_TWO.contentEquals(keyword)) {
                    return TWO;
                }
                if (PluralRules.KEYWORD_FEW.contentEquals(keyword)) {
                    return FEW;
                }
                break;
            case 4:
                if (PluralRules.KEYWORD_MANY.contentEquals(keyword)) {
                    return MANY;
                }
                if (PluralRules.KEYWORD_ZERO.contentEquals(keyword)) {
                    return ZERO;
                }
                break;
            case 5:
                if (PluralRules.KEYWORD_OTHER.contentEquals(keyword)) {
                    return OTHER;
                }
                break;
        }
        return null;
    }

    public static final StandardPlural orOtherFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p : OTHER;
    }

    public static final StandardPlural fromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p;
        }
        throw new IllegalArgumentException(keyword.toString());
    }

    public static final int indexOrNegativeFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : -1;
    }

    public static final int indexOrOtherIndexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        return p != null ? p.ordinal() : OTHER.ordinal();
    }

    public static final int indexFromString(CharSequence keyword) {
        StandardPlural p = orNullFromString(keyword);
        if (p != null) {
            return p.ordinal();
        }
        throw new IllegalArgumentException(keyword.toString());
    }
}
