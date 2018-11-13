package sun.security.ec;

import java.io.IOException;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.regex.Pattern;
import sun.security.util.DerOutputStream;
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
public final class NamedCurve extends ECParameterSpec {
    private static final int B = 2;
    private static final int BD = 6;
    private static final int P = 1;
    private static final int PD = 5;
    private static Pattern SPLIT_PATTERN;
    private static final Map<Integer, NamedCurve> lengthMap = null;
    private static final Map<String, NamedCurve> nameMap = null;
    private static final Map<String, NamedCurve> oidMap = null;
    private final byte[] encoded;
    private final String name;
    private final ObjectIdentifier oid;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.ec.NamedCurve.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.ec.NamedCurve.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.ec.NamedCurve.<clinit>():void");
    }

    private NamedCurve(String name, ObjectIdentifier oid, EllipticCurve curve, ECPoint g, BigInteger n, int h) throws IOException {
        super(curve, g, n, h);
        this.name = name;
        this.oid = oid;
        DerOutputStream out = new DerOutputStream();
        out.putOID(oid);
        this.encoded = out.toByteArray();
    }

    public static ECParameterSpec getECParameterSpec(String name) {
        NamedCurve spec = (NamedCurve) oidMap.get(name);
        return spec != null ? spec : (ECParameterSpec) nameMap.get(name);
    }

    static ECParameterSpec getECParameterSpec(ObjectIdentifier oid) {
        return getECParameterSpec(oid.toString());
    }

    public static ECParameterSpec getECParameterSpec(int length) {
        return (ECParameterSpec) lengthMap.get(Integer.valueOf(length));
    }

    public static Collection<? extends ECParameterSpec> knownECParameterSpecs() {
        return Collections.unmodifiableCollection(oidMap.values());
    }

    byte[] getEncoded() {
        return (byte[]) this.encoded.clone();
    }

    ObjectIdentifier getObjectIdentifier() {
        return this.oid;
    }

    public String toString() {
        return this.name + " (" + this.oid + ")";
    }

    private static BigInteger bi(String s) {
        return new BigInteger(s, 16);
    }

    private static void add(String name, String soid, int type, String sfield, String a, String b, String x, String y, String n, int h) {
        ECField field;
        BigInteger p = bi(sfield);
        if (type == 1 || type == 5) {
            field = new ECFieldFp(p);
        } else if (type == 2 || type == 6) {
            field = new ECFieldF2m(p.bitLength() - 1, p);
        } else {
            throw new RuntimeException("Invalid type: " + type);
        }
        try {
            NamedCurve params = new NamedCurve(name, new ObjectIdentifier(soid), new EllipticCurve(field, bi(a), bi(b)), new ECPoint(bi(x), bi(y)), bi(n), h);
            if (oidMap.put(soid, params) != null) {
                throw new RuntimeException("Duplication oid: " + soid);
            }
            for (String commonName : SPLIT_PATTERN.split(name)) {
                if (nameMap.put(commonName.trim(), params) != null) {
                    throw new RuntimeException("Duplication name: " + commonName);
                }
            }
            int len = field.getFieldSize();
            if (type == 5 || type == 6 || lengthMap.get(Integer.valueOf(len)) == null) {
                lengthMap.put(Integer.valueOf(len), params);
            }
        } catch (IOException e) {
            throw new RuntimeException("Internal error", e);
        }
    }
}
