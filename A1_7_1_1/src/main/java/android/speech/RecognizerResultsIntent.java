package android.speech;

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
public class RecognizerResultsIntent {
    public static final String ACTION_VOICE_SEARCH_RESULTS = "android.speech.action.VOICE_SEARCH_RESULTS";
    public static final String EXTRA_VOICE_SEARCH_RESULT_HTML = "android.speech.extras.VOICE_SEARCH_RESULT_HTML";
    public static final String EXTRA_VOICE_SEARCH_RESULT_HTML_BASE_URLS = "android.speech.extras.VOICE_SEARCH_RESULT_HTML_BASE_URLS";
    public static final String EXTRA_VOICE_SEARCH_RESULT_HTTP_HEADERS = "android.speech.extras.EXTRA_VOICE_SEARCH_RESULT_HTTP_HEADERS";
    public static final String EXTRA_VOICE_SEARCH_RESULT_STRINGS = "android.speech.extras.VOICE_SEARCH_RESULT_STRINGS";
    public static final String EXTRA_VOICE_SEARCH_RESULT_URLS = "android.speech.extras.VOICE_SEARCH_RESULT_URLS";
    public static final String URI_SCHEME_INLINE = "inline";

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.speech.RecognizerResultsIntent.<init>():void, dex: 
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
    private RecognizerResultsIntent() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.speech.RecognizerResultsIntent.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.speech.RecognizerResultsIntent.<init>():void");
    }
}
