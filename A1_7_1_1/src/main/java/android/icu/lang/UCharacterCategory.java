package android.icu.lang;

import android.icu.lang.UCharacterEnums.ECharacterCategory;

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
public final class UCharacterCategory implements ECharacterCategory {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.lang.UCharacterCategory.<init>():void, dex: 
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
    private UCharacterCategory() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.lang.UCharacterCategory.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacterCategory.<init>():void");
    }

    public static String toString(int category) {
        switch (category) {
            case 1:
                return "Letter, Uppercase";
            case 2:
                return "Letter, Lowercase";
            case 3:
                return "Letter, Titlecase";
            case 4:
                return "Letter, Modifier";
            case 5:
                return "Letter, Other";
            case 6:
                return "Mark, Non-Spacing";
            case 7:
                return "Mark, Enclosing";
            case 8:
                return "Mark, Spacing Combining";
            case 9:
                return "Number, Decimal Digit";
            case 10:
                return "Number, Letter";
            case 11:
                return "Number, Other";
            case 12:
                return "Separator, Space";
            case 13:
                return "Separator, Line";
            case 14:
                return "Separator, Paragraph";
            case 15:
                return "Other, Control";
            case 16:
                return "Other, Format";
            case 17:
                return "Other, Private Use";
            case 18:
                return "Other, Surrogate";
            case 19:
                return "Punctuation, Dash";
            case 20:
                return "Punctuation, Open";
            case 21:
                return "Punctuation, Close";
            case 22:
                return "Punctuation, Connector";
            case 23:
                return "Punctuation, Other";
            case 24:
                return "Symbol, Math";
            case 25:
                return "Symbol, Currency";
            case 26:
                return "Symbol, Modifier";
            case 27:
                return "Symbol, Other";
            case 28:
                return "Punctuation, Initial quote";
            case 29:
                return "Punctuation, Final quote";
            default:
                return "Unassigned";
        }
    }
}
