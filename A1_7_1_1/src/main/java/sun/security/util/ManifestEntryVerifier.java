package sun.security.util;

import java.io.IOException;
import java.security.CodeSigner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarException;
import java.util.jar.Manifest;
import sun.misc.BASE64Decoder;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public class ManifestEntryVerifier {
    private static final Debug debug = null;
    private static final char[] hexc = null;
    HashMap<String, MessageDigest> createdDigests;
    private BASE64Decoder decoder;
    ArrayList<MessageDigest> digests;
    private JarEntry entry;
    private Manifest man;
    ArrayList<byte[]> manifestHashes;
    private String name;
    private CodeSigner[] signers;
    private boolean skip;

    private static class SunProviderHolder {
        private static final Provider instance = null;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.util.ManifestEntryVerifier.SunProviderHolder.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.util.ManifestEntryVerifier.SunProviderHolder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: sun.security.util.ManifestEntryVerifier.SunProviderHolder.<clinit>():void");
        }

        private SunProviderHolder() {
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: sun.security.util.ManifestEntryVerifier.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: sun.security.util.ManifestEntryVerifier.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.util.ManifestEntryVerifier.<clinit>():void");
    }

    public ManifestEntryVerifier(Manifest man) {
        this.decoder = null;
        this.name = null;
        this.skip = true;
        this.signers = null;
        this.createdDigests = new HashMap(11);
        this.digests = new ArrayList();
        this.manifestHashes = new ArrayList();
        this.decoder = new BASE64Decoder();
        this.man = man;
    }

    public void setEntry(String name, JarEntry entry) throws IOException {
        this.digests.clear();
        this.manifestHashes.clear();
        this.name = name;
        this.entry = entry;
        this.skip = true;
        this.signers = null;
        if (this.man != null && name != null) {
            Attributes attr = this.man.getAttributes(name);
            if (attr == null) {
                attr = this.man.getAttributes("./" + name);
                if (attr == null) {
                    attr = this.man.getAttributes("/" + name);
                    if (attr == null) {
                        return;
                    }
                }
            }
            for (Entry<Object, Object> se : attr.entrySet()) {
                String key = se.getKey().toString();
                if (key.toUpperCase(Locale.ENGLISH).endsWith("-DIGEST")) {
                    String algorithm = key.substring(0, key.length() - 7);
                    MessageDigest digest = (MessageDigest) this.createdDigests.get(algorithm);
                    if (digest == null) {
                        try {
                            digest = MessageDigest.getInstance(algorithm, SunProviderHolder.instance);
                            this.createdDigests.put(algorithm, digest);
                        } catch (NoSuchAlgorithmException e) {
                        }
                    }
                    if (digest != null) {
                        this.skip = false;
                        digest.reset();
                        this.digests.add(digest);
                        this.manifestHashes.add(this.decoder.decodeBuffer((String) se.getValue()));
                    }
                }
            }
        }
    }

    public void update(byte buffer) {
        if (!this.skip) {
            for (int i = 0; i < this.digests.size(); i++) {
                ((MessageDigest) this.digests.get(i)).update(buffer);
            }
        }
    }

    public void update(byte[] buffer, int off, int len) {
        if (!this.skip) {
            for (int i = 0; i < this.digests.size(); i++) {
                ((MessageDigest) this.digests.get(i)).update(buffer, off, len);
            }
        }
    }

    public JarEntry getEntry() {
        return this.entry;
    }

    public CodeSigner[] verify(Hashtable<String, CodeSigner[]> verifiedSigners, Hashtable<String, CodeSigner[]> sigFileSigners) throws JarException {
        if (this.skip) {
            return null;
        }
        if (this.signers != null) {
            return this.signers;
        }
        int i = 0;
        while (i < this.digests.size()) {
            MessageDigest digest = (MessageDigest) this.digests.get(i);
            byte[] manHash = (byte[]) this.manifestHashes.get(i);
            byte[] theHash = digest.digest();
            if (debug != null) {
                debug.println("Manifest Entry: " + this.name + " digest=" + digest.getAlgorithm());
                debug.println("  manifest " + toHex(manHash));
                debug.println("  computed " + toHex(theHash));
                debug.println();
            }
            if (MessageDigest.isEqual(theHash, manHash)) {
                i++;
            } else {
                throw new SecurityException(digest.getAlgorithm() + " digest error for " + this.name);
            }
        }
        this.signers = (CodeSigner[]) sigFileSigners.remove(this.name);
        if (this.signers != null) {
            verifiedSigners.put(this.name, this.signers);
        }
        return this.signers;
    }

    static String toHex(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(hexc[(data[i] >> 4) & 15]);
            sb.append(hexc[data[i] & 15]);
        }
        return sb.toString();
    }
}
