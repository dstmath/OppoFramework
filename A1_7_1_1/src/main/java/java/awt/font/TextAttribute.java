package java.awt.font;

import java.text.AttributedCharacterIterator.Attribute;
import java.util.Map;

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
public final class TextAttribute extends Attribute {
    public static final TextAttribute BACKGROUND = null;
    public static final TextAttribute BIDI_EMBEDDING = null;
    public static final TextAttribute CHAR_REPLACEMENT = null;
    public static final TextAttribute FAMILY = null;
    public static final TextAttribute FONT = null;
    public static final TextAttribute FOREGROUND = null;
    public static final TextAttribute INPUT_METHOD_HIGHLIGHT = null;
    public static final TextAttribute INPUT_METHOD_UNDERLINE = null;
    public static final TextAttribute JUSTIFICATION = null;
    public static final Float JUSTIFICATION_FULL = null;
    public static final Float JUSTIFICATION_NONE = null;
    public static final TextAttribute KERNING = null;
    public static final Integer KERNING_ON = null;
    public static final TextAttribute LIGATURES = null;
    public static final Integer LIGATURES_ON = null;
    public static final TextAttribute NUMERIC_SHAPING = null;
    public static final TextAttribute POSTURE = null;
    public static final Float POSTURE_OBLIQUE = null;
    public static final Float POSTURE_REGULAR = null;
    public static final TextAttribute RUN_DIRECTION = null;
    public static final Boolean RUN_DIRECTION_LTR = null;
    public static final Boolean RUN_DIRECTION_RTL = null;
    public static final TextAttribute SIZE = null;
    public static final TextAttribute STRIKETHROUGH = null;
    public static final Boolean STRIKETHROUGH_ON = null;
    public static final TextAttribute SUPERSCRIPT = null;
    public static final Integer SUPERSCRIPT_SUB = null;
    public static final Integer SUPERSCRIPT_SUPER = null;
    public static final TextAttribute SWAP_COLORS = null;
    public static final Boolean SWAP_COLORS_ON = null;
    public static final TextAttribute TRACKING = null;
    public static final Float TRACKING_LOOSE = null;
    public static final Float TRACKING_TIGHT = null;
    public static final TextAttribute TRANSFORM = null;
    public static final TextAttribute UNDERLINE = null;
    public static final Integer UNDERLINE_LOW_DASHED = null;
    public static final Integer UNDERLINE_LOW_DOTTED = null;
    public static final Integer UNDERLINE_LOW_GRAY = null;
    public static final Integer UNDERLINE_LOW_ONE_PIXEL = null;
    public static final Integer UNDERLINE_LOW_TWO_PIXEL = null;
    public static final Integer UNDERLINE_ON = null;
    public static final TextAttribute WEIGHT = null;
    public static final Float WEIGHT_BOLD = null;
    public static final Float WEIGHT_DEMIBOLD = null;
    public static final Float WEIGHT_DEMILIGHT = null;
    public static final Float WEIGHT_EXTRABOLD = null;
    public static final Float WEIGHT_EXTRA_LIGHT = null;
    public static final Float WEIGHT_HEAVY = null;
    public static final Float WEIGHT_LIGHT = null;
    public static final Float WEIGHT_MEDIUM = null;
    public static final Float WEIGHT_REGULAR = null;
    public static final Float WEIGHT_SEMIBOLD = null;
    public static final Float WEIGHT_ULTRABOLD = null;
    public static final TextAttribute WIDTH = null;
    public static final Float WIDTH_CONDENSED = null;
    public static final Float WIDTH_EXTENDED = null;
    public static final Float WIDTH_REGULAR = null;
    public static final Float WIDTH_SEMI_CONDENSED = null;
    public static final Float WIDTH_SEMI_EXTENDED = null;
    private static final Map instanceMap = null;
    static final long serialVersionUID = 7744112784117861702L;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.awt.font.TextAttribute.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.awt.font.TextAttribute.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.awt.font.TextAttribute.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.awt.font.TextAttribute.<init>(java.lang.String):void, dex: 
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
    protected TextAttribute(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.awt.font.TextAttribute.<init>(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.awt.font.TextAttribute.<init>(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.awt.font.TextAttribute.readResolve():java.lang.Object, dex: 
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
    protected java.lang.Object readResolve() throws java.io.InvalidObjectException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.awt.font.TextAttribute.readResolve():java.lang.Object, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.awt.font.TextAttribute.readResolve():java.lang.Object");
    }
}
