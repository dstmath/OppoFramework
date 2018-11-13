package android.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class UrlQuerySanitizer {
    private static final ValueSanitizer sAllButNulAndAngleBracketsLegal = null;
    private static final ValueSanitizer sAllButNulLegal = null;
    private static final ValueSanitizer sAllButWhitespaceLegal = null;
    private static final ValueSanitizer sAllIllegal = null;
    private static final ValueSanitizer sAmpAndSpaceLegal = null;
    private static final ValueSanitizer sAmpLegal = null;
    private static final ValueSanitizer sSpaceLegal = null;
    private static final ValueSanitizer sURLLegal = null;
    private static final ValueSanitizer sUrlAndSpaceLegal = null;
    private boolean mAllowUnregisteredParamaters;
    private final HashMap<String, String> mEntries;
    private final ArrayList<ParameterValuePair> mEntriesList;
    private boolean mPreferFirstRepeatedParameter;
    private final HashMap<String, ValueSanitizer> mSanitizers;
    private ValueSanitizer mUnregisteredParameterValueSanitizer;

    public interface ValueSanitizer {
        String sanitize(String str);
    }

    public static class IllegalCharacterValueSanitizer implements ValueSanitizer {
        public static final int ALL_BUT_NUL_AND_ANGLE_BRACKETS_LEGAL = 1439;
        public static final int ALL_BUT_NUL_LEGAL = 1535;
        public static final int ALL_BUT_WHITESPACE_LEGAL = 1532;
        public static final int ALL_ILLEGAL = 0;
        public static final int ALL_OK = 2047;
        public static final int ALL_WHITESPACE_OK = 3;
        public static final int AMP_AND_SPACE_LEGAL = 129;
        public static final int AMP_LEGAL = 128;
        public static final int AMP_OK = 128;
        public static final int DQUOTE_OK = 8;
        public static final int GT_OK = 64;
        private static final String JAVASCRIPT_PREFIX = "javascript:";
        public static final int LT_OK = 32;
        private static final int MIN_SCRIPT_PREFIX_LENGTH = 0;
        public static final int NON_7_BIT_ASCII_OK = 4;
        public static final int NUL_OK = 512;
        public static final int OTHER_WHITESPACE_OK = 2;
        public static final int PCT_OK = 256;
        public static final int SCRIPT_URL_OK = 1024;
        public static final int SPACE_LEGAL = 1;
        public static final int SPACE_OK = 1;
        public static final int SQUOTE_OK = 16;
        public static final int URL_AND_SPACE_LEGAL = 405;
        public static final int URL_LEGAL = 404;
        private static final String VBSCRIPT_PREFIX = "vbscript:";
        private int mFlags;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<clinit>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<init>(int):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public IllegalCharacterValueSanitizer(int r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<init>(int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.<init>(int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.characterIsLegal(char):boolean, dex:  in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.characterIsLegal(char):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.characterIsLegal(char):boolean, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:756)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        private boolean characterIsLegal(char r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.characterIsLegal(char):boolean, dex:  in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.characterIsLegal(char):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.characterIsLegal(char):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.trimWhitespace(java.lang.String):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        private java.lang.String trimWhitespace(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.trimWhitespace(java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.trimWhitespace(java.lang.String):java.lang.String");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.sanitize(java.lang.String):java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public java.lang.String sanitize(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.sanitize(java.lang.String):java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.IllegalCharacterValueSanitizer.sanitize(java.lang.String):java.lang.String");
        }

        private boolean isWhitespace(char c) {
            switch (c) {
                case 9:
                case 10:
                case 11:
                case 12:
                case 13:
                case ' ':
                    return true;
                default:
                    return false;
            }
        }
    }

    public class ParameterValuePair {
        public String mParameter;
        public String mValue;
        final /* synthetic */ UrlQuerySanitizer this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.net.UrlQuerySanitizer.ParameterValuePair.<init>(android.net.UrlQuerySanitizer, java.lang.String, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public ParameterValuePair(android.net.UrlQuerySanitizer r1, java.lang.String r2, java.lang.String r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.net.UrlQuerySanitizer.ParameterValuePair.<init>(android.net.UrlQuerySanitizer, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.ParameterValuePair.<init>(android.net.UrlQuerySanitizer, java.lang.String, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.UrlQuerySanitizer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.UrlQuerySanitizer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.UrlQuerySanitizer.<clinit>():void");
    }

    public ValueSanitizer getUnregisteredParameterValueSanitizer() {
        return this.mUnregisteredParameterValueSanitizer;
    }

    public void setUnregisteredParameterValueSanitizer(ValueSanitizer sanitizer) {
        this.mUnregisteredParameterValueSanitizer = sanitizer;
    }

    public static final ValueSanitizer getAllIllegal() {
        return sAllIllegal;
    }

    public static final ValueSanitizer getAllButNulLegal() {
        return sAllButNulLegal;
    }

    public static final ValueSanitizer getAllButWhitespaceLegal() {
        return sAllButWhitespaceLegal;
    }

    public static final ValueSanitizer getUrlLegal() {
        return sURLLegal;
    }

    public static final ValueSanitizer getUrlAndSpaceLegal() {
        return sUrlAndSpaceLegal;
    }

    public static final ValueSanitizer getAmpLegal() {
        return sAmpLegal;
    }

    public static final ValueSanitizer getAmpAndSpaceLegal() {
        return sAmpAndSpaceLegal;
    }

    public static final ValueSanitizer getSpaceLegal() {
        return sSpaceLegal;
    }

    public static final ValueSanitizer getAllButNulAndAngleBracketsLegal() {
        return sAllButNulAndAngleBracketsLegal;
    }

    public UrlQuerySanitizer() {
        this.mSanitizers = new HashMap();
        this.mEntries = new HashMap();
        this.mEntriesList = new ArrayList();
        this.mUnregisteredParameterValueSanitizer = getAllIllegal();
    }

    public UrlQuerySanitizer(String url) {
        this.mSanitizers = new HashMap();
        this.mEntries = new HashMap();
        this.mEntriesList = new ArrayList();
        this.mUnregisteredParameterValueSanitizer = getAllIllegal();
        setAllowUnregisteredParamaters(true);
        parseUrl(url);
    }

    public void parseUrl(String url) {
        String query;
        int queryIndex = url.indexOf(63);
        if (queryIndex >= 0) {
            query = url.substring(queryIndex + 1);
        } else {
            query = "";
        }
        parseQuery(query);
    }

    public void parseQuery(String query) {
        clear();
        StringTokenizer tokenizer = new StringTokenizer(query, "&");
        while (tokenizer.hasMoreElements()) {
            String attributeValuePair = tokenizer.nextToken();
            if (attributeValuePair.length() > 0) {
                int assignmentIndex = attributeValuePair.indexOf(61);
                if (assignmentIndex < 0) {
                    parseEntry(attributeValuePair, "");
                } else {
                    parseEntry(attributeValuePair.substring(0, assignmentIndex), attributeValuePair.substring(assignmentIndex + 1));
                }
            }
        }
    }

    public Set<String> getParameterSet() {
        return this.mEntries.keySet();
    }

    public List<ParameterValuePair> getParameterList() {
        return this.mEntriesList;
    }

    public boolean hasParameter(String parameter) {
        return this.mEntries.containsKey(parameter);
    }

    public String getValue(String parameter) {
        return (String) this.mEntries.get(parameter);
    }

    public void registerParameter(String parameter, ValueSanitizer valueSanitizer) {
        if (valueSanitizer == null) {
            this.mSanitizers.remove(parameter);
        }
        this.mSanitizers.put(parameter, valueSanitizer);
    }

    public void registerParameters(String[] parameters, ValueSanitizer valueSanitizer) {
        for (Object put : parameters) {
            this.mSanitizers.put(put, valueSanitizer);
        }
    }

    public void setAllowUnregisteredParamaters(boolean allowUnregisteredParamaters) {
        this.mAllowUnregisteredParamaters = allowUnregisteredParamaters;
    }

    public boolean getAllowUnregisteredParamaters() {
        return this.mAllowUnregisteredParamaters;
    }

    public void setPreferFirstRepeatedParameter(boolean preferFirstRepeatedParameter) {
        this.mPreferFirstRepeatedParameter = preferFirstRepeatedParameter;
    }

    public boolean getPreferFirstRepeatedParameter() {
        return this.mPreferFirstRepeatedParameter;
    }

    protected void parseEntry(String parameter, String value) {
        String unescapedParameter = unescape(parameter);
        ValueSanitizer valueSanitizer = getEffectiveValueSanitizer(unescapedParameter);
        if (valueSanitizer != null) {
            addSanitizedEntry(unescapedParameter, valueSanitizer.sanitize(unescape(value)));
        }
    }

    protected void addSanitizedEntry(String parameter, String value) {
        this.mEntriesList.add(new ParameterValuePair(this, parameter, value));
        if (!this.mPreferFirstRepeatedParameter || !this.mEntries.containsKey(parameter)) {
            this.mEntries.put(parameter, value);
        }
    }

    public ValueSanitizer getValueSanitizer(String parameter) {
        return (ValueSanitizer) this.mSanitizers.get(parameter);
    }

    public ValueSanitizer getEffectiveValueSanitizer(String parameter) {
        ValueSanitizer sanitizer = getValueSanitizer(parameter);
        if (sanitizer == null && this.mAllowUnregisteredParamaters) {
            return getUnregisteredParameterValueSanitizer();
        }
        return sanitizer;
    }

    public String unescape(String string) {
        int firstEscape = string.indexOf(37);
        if (firstEscape < 0) {
            firstEscape = string.indexOf(43);
            if (firstEscape < 0) {
                return string;
            }
        }
        int length = string.length();
        StringBuilder stringBuilder = new StringBuilder(length);
        stringBuilder.append(string.substring(0, firstEscape));
        int i = firstEscape;
        while (i < length) {
            char c = string.charAt(i);
            if (c == '+') {
                c = ' ';
            } else if (c == '%' && i + 2 < length) {
                char c1 = string.charAt(i + 1);
                char c2 = string.charAt(i + 2);
                if (isHexDigit(c1) && isHexDigit(c2)) {
                    c = (char) ((decodeHexDigit(c1) * 16) + decodeHexDigit(c2));
                    i += 2;
                }
            }
            stringBuilder.append(c);
            i++;
        }
        return stringBuilder.toString();
    }

    protected boolean isHexDigit(char c) {
        return decodeHexDigit(c) >= 0;
    }

    protected int decodeHexDigit(char c) {
        if (c >= '0' && c <= '9') {
            return c - 48;
        }
        if (c >= 'A' && c <= 'F') {
            return (c - 65) + 10;
        }
        if (c < 'a' || c > 'f') {
            return -1;
        }
        return (c - 97) + 10;
    }

    protected void clear() {
        this.mEntries.clear();
        this.mEntriesList.clear();
    }
}
