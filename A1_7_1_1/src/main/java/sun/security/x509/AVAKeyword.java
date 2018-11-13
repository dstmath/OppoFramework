package sun.security.x509;

import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import sun.security.util.ObjectIdentifier;

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
/* compiled from: AVA */
class AVAKeyword {
    private static final Map<String, AVAKeyword> keywordMap = null;
    private static final Map<ObjectIdentifier, AVAKeyword> oidMap = null;
    private String keyword;
    private ObjectIdentifier oid;
    private boolean rfc1779Compliant;
    private boolean rfc2253Compliant;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.x509.AVAKeyword.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.x509.AVAKeyword.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.AVAKeyword.<clinit>():void");
    }

    private AVAKeyword(String keyword, ObjectIdentifier oid, boolean rfc1779Compliant, boolean rfc2253Compliant) {
        this.keyword = keyword;
        this.oid = oid;
        this.rfc1779Compliant = rfc1779Compliant;
        this.rfc2253Compliant = rfc2253Compliant;
        oidMap.put(oid, this);
        keywordMap.put(keyword, this);
    }

    private boolean isCompliant(int standard) {
        switch (standard) {
            case 1:
                return true;
            case 2:
                return this.rfc1779Compliant;
            case 3:
                return this.rfc2253Compliant;
            default:
                throw new IllegalArgumentException("Invalid standard " + standard);
        }
    }

    static ObjectIdentifier getOID(String keyword, int standard) throws IOException {
        return getOID(keyword, standard, Collections.emptyMap());
    }

    static ObjectIdentifier getOID(String keyword, int standard, Map<String, String> extraKeywordMap) throws IOException {
        keyword = keyword.toUpperCase(Locale.ENGLISH);
        if (standard != 3) {
            keyword = keyword.trim();
        } else if (keyword.startsWith(" ") || keyword.endsWith(" ")) {
            throw new IOException("Invalid leading or trailing space in keyword \"" + keyword + "\"");
        }
        String oidString = (String) extraKeywordMap.get(keyword);
        if (oidString != null) {
            return new ObjectIdentifier(oidString);
        }
        AVAKeyword ak = (AVAKeyword) keywordMap.get(keyword);
        if (ak != null && ak.isCompliant(standard)) {
            return ak.oid;
        }
        if (standard == 2) {
            if (keyword.startsWith("OID.")) {
                keyword = keyword.substring(4);
            } else {
                throw new IOException("Invalid RFC1779 keyword: " + keyword);
            }
        } else if (standard == 1 && keyword.startsWith("OID.")) {
            keyword = keyword.substring(4);
        }
        boolean number = false;
        if (keyword.length() != 0) {
            char ch = keyword.charAt(0);
            if (ch >= '0' && ch <= '9') {
                number = true;
            }
        }
        if (number) {
            return new ObjectIdentifier(keyword);
        }
        throw new IOException("Invalid keyword \"" + keyword + "\"");
    }

    static String getKeyword(ObjectIdentifier oid, int standard) {
        return getKeyword(oid, standard, Collections.emptyMap());
    }

    static String getKeyword(ObjectIdentifier oid, int standard, Map<String, String> extraOidMap) {
        String oidString = oid.toString();
        String keywordString = (String) extraOidMap.get(oidString);
        if (keywordString == null) {
            AVAKeyword ak = (AVAKeyword) oidMap.get(oid);
            if (ak != null && ak.isCompliant(standard)) {
                return ak.keyword;
            }
            if (standard == 3) {
                return oidString;
            }
            return "OID." + oidString;
        } else if (keywordString.length() == 0) {
            throw new IllegalArgumentException("keyword cannot be empty");
        } else {
            keywordString = keywordString.trim();
            char c = keywordString.charAt(0);
            if (c < 'A' || c > 'z' || (c > 'Z' && c < 'a')) {
                throw new IllegalArgumentException("keyword does not start with letter");
            }
            for (int i = 1; i < keywordString.length(); i++) {
                c = keywordString.charAt(i);
                if ((c < 'A' || c > 'z' || (c > 'Z' && c < 'a')) && ((c < '0' || c > '9') && c != '_')) {
                    throw new IllegalArgumentException("keyword character is not a letter, digit, or underscore");
                }
            }
            return keywordString;
        }
    }

    static boolean hasKeyword(ObjectIdentifier oid, int standard) {
        AVAKeyword ak = (AVAKeyword) oidMap.get(oid);
        if (ak == null) {
            return false;
        }
        return ak.isCompliant(standard);
    }
}
