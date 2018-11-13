package android.icu.lang;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
public class UCharacterEnums {

    public interface ECharacterCategory {
        public static final byte CHAR_CATEGORY_COUNT = (byte) 30;
        public static final byte COMBINING_SPACING_MARK = (byte) 8;
        public static final byte CONNECTOR_PUNCTUATION = (byte) 22;
        public static final byte CONTROL = (byte) 15;
        public static final byte CURRENCY_SYMBOL = (byte) 25;
        public static final byte DASH_PUNCTUATION = (byte) 19;
        public static final byte DECIMAL_DIGIT_NUMBER = (byte) 9;
        public static final byte ENCLOSING_MARK = (byte) 7;
        public static final byte END_PUNCTUATION = (byte) 21;
        public static final byte FINAL_PUNCTUATION = (byte) 29;
        public static final byte FINAL_QUOTE_PUNCTUATION = (byte) 29;
        public static final byte FORMAT = (byte) 16;
        public static final byte GENERAL_OTHER_TYPES = (byte) 0;
        public static final byte INITIAL_PUNCTUATION = (byte) 28;
        public static final byte INITIAL_QUOTE_PUNCTUATION = (byte) 28;
        public static final byte LETTER_NUMBER = (byte) 10;
        public static final byte LINE_SEPARATOR = (byte) 13;
        public static final byte LOWERCASE_LETTER = (byte) 2;
        public static final byte MATH_SYMBOL = (byte) 24;
        public static final byte MODIFIER_LETTER = (byte) 4;
        public static final byte MODIFIER_SYMBOL = (byte) 26;
        public static final byte NON_SPACING_MARK = (byte) 6;
        public static final byte OTHER_LETTER = (byte) 5;
        public static final byte OTHER_NUMBER = (byte) 11;
        public static final byte OTHER_PUNCTUATION = (byte) 23;
        public static final byte OTHER_SYMBOL = (byte) 27;
        public static final byte PARAGRAPH_SEPARATOR = (byte) 14;
        public static final byte PRIVATE_USE = (byte) 17;
        public static final byte SPACE_SEPARATOR = (byte) 12;
        public static final byte START_PUNCTUATION = (byte) 20;
        public static final byte SURROGATE = (byte) 18;
        public static final byte TITLECASE_LETTER = (byte) 3;
        public static final byte UNASSIGNED = (byte) 0;
        public static final byte UPPERCASE_LETTER = (byte) 1;
    }

    public interface ECharacterDirection {
        public static final int ARABIC_NUMBER = 5;
        public static final int BLOCK_SEPARATOR = 7;
        public static final int BOUNDARY_NEUTRAL = 18;
        public static final int CHAR_DIRECTION_COUNT = 23;
        public static final int COMMON_NUMBER_SEPARATOR = 6;
        public static final byte DIRECTIONALITY_ARABIC_NUMBER = (byte) 5;
        public static final byte DIRECTIONALITY_BOUNDARY_NEUTRAL = (byte) 18;
        public static final byte DIRECTIONALITY_COMMON_NUMBER_SEPARATOR = (byte) 6;
        public static final byte DIRECTIONALITY_EUROPEAN_NUMBER = (byte) 2;
        public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR = (byte) 3;
        public static final byte DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR = (byte) 4;
        public static final byte DIRECTIONALITY_LEFT_TO_RIGHT = (byte) 0;
        public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING = (byte) 11;
        public static final byte DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE = (byte) 12;
        public static final byte DIRECTIONALITY_NONSPACING_MARK = (byte) 17;
        public static final byte DIRECTIONALITY_OTHER_NEUTRALS = (byte) 10;
        public static final byte DIRECTIONALITY_PARAGRAPH_SEPARATOR = (byte) 7;
        public static final byte DIRECTIONALITY_POP_DIRECTIONAL_FORMAT = (byte) 16;
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT = (byte) 1;
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC = (byte) 13;
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_EMBEDDING = (byte) 14;
        public static final byte DIRECTIONALITY_RIGHT_TO_LEFT_OVERRIDE = (byte) 15;
        public static final byte DIRECTIONALITY_SEGMENT_SEPARATOR = (byte) 8;
        public static final byte DIRECTIONALITY_UNDEFINED = (byte) -1;
        public static final byte DIRECTIONALITY_WHITESPACE = (byte) 9;
        public static final int DIR_NON_SPACING_MARK = 17;
        public static final int EUROPEAN_NUMBER = 2;
        public static final int EUROPEAN_NUMBER_SEPARATOR = 3;
        public static final int EUROPEAN_NUMBER_TERMINATOR = 4;
        public static final byte FIRST_STRONG_ISOLATE = (byte) 19;
        public static final int LEFT_TO_RIGHT = 0;
        public static final int LEFT_TO_RIGHT_EMBEDDING = 11;
        public static final byte LEFT_TO_RIGHT_ISOLATE = (byte) 20;
        public static final int LEFT_TO_RIGHT_OVERRIDE = 12;
        public static final int OTHER_NEUTRAL = 10;
        public static final int POP_DIRECTIONAL_FORMAT = 16;
        public static final byte POP_DIRECTIONAL_ISOLATE = (byte) 22;
        public static final int RIGHT_TO_LEFT = 1;
        public static final int RIGHT_TO_LEFT_ARABIC = 13;
        public static final int RIGHT_TO_LEFT_EMBEDDING = 14;
        public static final byte RIGHT_TO_LEFT_ISOLATE = (byte) 21;
        public static final int RIGHT_TO_LEFT_OVERRIDE = 15;
        public static final int SEGMENT_SEPARATOR = 8;
        public static final int WHITE_SPACE_NEUTRAL = 9;
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.lang.UCharacterEnums.<init>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    private UCharacterEnums() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.lang.UCharacterEnums.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.lang.UCharacterEnums.<init>():void");
    }
}
