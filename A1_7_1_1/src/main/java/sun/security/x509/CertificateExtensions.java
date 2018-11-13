package sun.security.x509;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.cert.CertificateException;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;
import sun.misc.HexDumpEncoder;
import sun.security.util.Debug;
import sun.security.util.DerInputStream;
import sun.security.util.DerOutputStream;
import sun.security.util.DerValue;
import sun.security.util.ObjectIdentifier;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class CertificateExtensions implements CertAttrSet<Extension> {
    public static final String IDENT = "x509.info.extensions";
    public static final String NAME = "extensions";
    private static Class[] PARAMS;
    private static final Debug debug = null;
    private Map<String, Extension> map;
    private Map<String, Extension> unparseableExtensions;
    private boolean unsupportedCritExt;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.x509.CertificateExtensions.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.x509.CertificateExtensions.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.x509.CertificateExtensions.<clinit>():void");
    }

    public CertificateExtensions() {
        this.map = Collections.synchronizedMap(new TreeMap());
        this.unsupportedCritExt = false;
    }

    public CertificateExtensions(DerInputStream in) throws IOException {
        this.map = Collections.synchronizedMap(new TreeMap());
        this.unsupportedCritExt = false;
        init(in);
    }

    private void init(DerInputStream in) throws IOException {
        DerValue[] exts = in.getSequence(5);
        for (DerValue extension : exts) {
            parseExtension(new Extension(extension));
        }
    }

    private void parseExtension(Extension ext) throws IOException {
        try {
            Class extClass = OIDMap.getClass(ext.getExtensionId());
            if (extClass == null) {
                if (ext.isCritical()) {
                    this.unsupportedCritExt = true;
                }
                if (this.map.put(ext.getExtensionId().toString(), ext) != null) {
                    throw new IOException("Duplicate extensions not allowed");
                }
                return;
            }
            Constructor cons = extClass.getConstructor(PARAMS);
            Object[] passed = new Object[2];
            passed[0] = Boolean.valueOf(ext.isCritical());
            passed[1] = ext.getExtensionValue();
            CertAttrSet certExt = (CertAttrSet) cons.newInstance(passed);
            if (this.map.put(certExt.getName(), (Extension) certExt) != null) {
                throw new IOException("Duplicate extensions not allowed");
            }
        } catch (InvocationTargetException invk) {
            Throwable e = invk.getTargetException();
            if (!ext.isCritical()) {
                if (this.unparseableExtensions == null) {
                    this.unparseableExtensions = new TreeMap();
                }
                this.unparseableExtensions.put(ext.getExtensionId().toString(), new UnparseableExtension(ext, e));
                if (debug != null) {
                    debug.println("Error parsing extension: " + ext);
                    e.printStackTrace();
                    System.err.println(new HexDumpEncoder().encodeBuffer(ext.getExtensionValue()));
                }
            } else if (e instanceof IOException) {
                throw ((IOException) e);
            } else {
                throw ((IOException) new IOException(e.toString()).initCause(e));
            }
        } catch (IOException e2) {
            throw e2;
        } catch (Exception e3) {
            throw ((IOException) new IOException(e3.toString()).initCause(e3));
        }
    }

    public void encode(OutputStream out) throws CertificateException, IOException {
        encode(out, false);
    }

    public void encode(OutputStream out, boolean isCertReq) throws CertificateException, IOException {
        DerOutputStream tmp;
        DerOutputStream extOut = new DerOutputStream();
        Object[] objs = this.map.values().toArray();
        for (int i = 0; i < objs.length; i++) {
            if (objs[i] instanceof CertAttrSet) {
                ((CertAttrSet) objs[i]).encode(extOut);
            } else if (objs[i] instanceof Extension) {
                ((Extension) objs[i]).encode(extOut);
            } else {
                throw new CertificateException("Illegal extension object");
            }
        }
        DerOutputStream seq = new DerOutputStream();
        seq.write((byte) 48, extOut);
        if (isCertReq) {
            tmp = seq;
        } else {
            tmp = new DerOutputStream();
            tmp.write(DerValue.createTag(Byte.MIN_VALUE, true, (byte) 3), seq);
        }
        out.write(tmp.toByteArray());
    }

    public void set(String name, Object obj) throws IOException {
        if (obj instanceof Extension) {
            this.map.put(name, (Extension) obj);
            return;
        }
        throw new IOException("Unknown extension type.");
    }

    public Object get(String name) throws IOException {
        Object obj = this.map.get(name);
        if (obj != null) {
            return obj;
        }
        throw new IOException("No extension found with name " + name);
    }

    Extension getExtension(String name) {
        return (Extension) this.map.get(name);
    }

    public void delete(String name) throws IOException {
        if (this.map.get(name) == null) {
            throw new IOException("No extension found with name " + name);
        }
        this.map.remove(name);
    }

    public String getNameByOid(ObjectIdentifier oid) throws IOException {
        for (String name : this.map.keySet()) {
            if (((Extension) this.map.get(name)).getExtensionId().equals(oid)) {
                return name;
            }
        }
        return null;
    }

    public Enumeration<Extension> getElements() {
        return Collections.enumeration(this.map.values());
    }

    public Collection<Extension> getAllExtensions() {
        return this.map.values();
    }

    public Map<String, Extension> getUnparseableExtensions() {
        if (this.unparseableExtensions == null) {
            return Collections.emptyMap();
        }
        return this.unparseableExtensions;
    }

    public String getName() {
        return "extensions";
    }

    public boolean hasUnsupportedCriticalExtension() {
        return this.unsupportedCritExt;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CertificateExtensions)) {
            return false;
        }
        Object[] objs = ((CertificateExtensions) other).getAllExtensions().toArray();
        int len = objs.length;
        if (len != this.map.size()) {
            return false;
        }
        String key = null;
        for (int i = 0; i < len; i++) {
            if (objs[i] instanceof CertAttrSet) {
                key = ((CertAttrSet) objs[i]).getName();
            }
            Extension otherExt = objs[i];
            if (key == null) {
                key = otherExt.getExtensionId().toString();
            }
            Extension thisExt = (Extension) this.map.get(key);
            if (thisExt == null || !thisExt.equals(otherExt)) {
                return false;
            }
        }
        return getUnparseableExtensions().equals(((CertificateExtensions) other).getUnparseableExtensions());
    }

    public int hashCode() {
        return this.map.hashCode() + getUnparseableExtensions().hashCode();
    }

    public String toString() {
        return this.map.toString();
    }
}
