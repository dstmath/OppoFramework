package android.icu.text;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class RBBIRuleParseTable {
    static final short doCheckVarDef = (short) 1;
    static final short doDotAny = (short) 2;
    static final short doEndAssign = (short) 3;
    static final short doEndOfRule = (short) 4;
    static final short doEndVariableName = (short) 5;
    static final short doExit = (short) 6;
    static final short doExprCatOperator = (short) 7;
    static final short doExprFinished = (short) 8;
    static final short doExprOrOperator = (short) 9;
    static final short doExprRParen = (short) 10;
    static final short doExprStart = (short) 11;
    static final short doLParen = (short) 12;
    static final short doNOP = (short) 13;
    static final short doOptionEnd = (short) 14;
    static final short doOptionStart = (short) 15;
    static final short doReverseDir = (short) 16;
    static final short doRuleChar = (short) 17;
    static final short doRuleError = (short) 18;
    static final short doRuleErrorAssignExpr = (short) 19;
    static final short doScanUnicodeSet = (short) 20;
    static final short doSlash = (short) 21;
    static final short doStartAssign = (short) 22;
    static final short doStartTagValue = (short) 23;
    static final short doStartVariableName = (short) 24;
    static final short doTagDigit = (short) 25;
    static final short doTagExpectedError = (short) 26;
    static final short doTagValue = (short) 27;
    static final short doUnaryOpPlus = (short) 28;
    static final short doUnaryOpQuestion = (short) 29;
    static final short doUnaryOpStar = (short) 30;
    static final short doVariableNameExpectedErr = (short) 31;
    static RBBIRuleTableElement[] gRuleParseStateTable = null;
    static final short kRuleSet_default = (short) 255;
    static final short kRuleSet_digit_char = (short) 128;
    static final short kRuleSet_eof = (short) 252;
    static final short kRuleSet_escaped = (short) 254;
    static final short kRuleSet_name_char = (short) 129;
    static final short kRuleSet_name_start_char = (short) 130;
    static final short kRuleSet_rule_char = (short) 131;
    static final short kRuleSet_white_space = (short) 132;

    static class RBBIRuleTableElement {
        short fAction;
        short fCharClass;
        boolean fNextChar;
        short fNextState;
        short fPushState;
        String fStateName;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ee in method: android.icu.text.RBBIRuleParseTable.RBBIRuleTableElement.<init>(short, int, int, int, boolean, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ee
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        RBBIRuleTableElement(short r1, int r2, int r3, int r4, boolean r5, java.lang.String r6) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ee in method: android.icu.text.RBBIRuleParseTable.RBBIRuleTableElement.<init>(short, int, int, int, boolean, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RBBIRuleParseTable.RBBIRuleTableElement.<init>(short, int, int, int, boolean, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.RBBIRuleParseTable.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.RBBIRuleParseTable.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RBBIRuleParseTable.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.icu.text.RBBIRuleParseTable.<init>():void, dex: 
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
    RBBIRuleParseTable() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.icu.text.RBBIRuleParseTable.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.icu.text.RBBIRuleParseTable.<init>():void");
    }
}
