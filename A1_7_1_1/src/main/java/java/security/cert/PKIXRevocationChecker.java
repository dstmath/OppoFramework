package java.security.cert;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public abstract class PKIXRevocationChecker extends PKIXCertPathChecker {
    private List<Extension> ocspExtensions = Collections.emptyList();
    private URI ocspResponder;
    private X509Certificate ocspResponderCert;
    private Map<X509Certificate, byte[]> ocspResponses = Collections.emptyMap();
    private Set<Option> options = Collections.emptySet();

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Option {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.security.cert.PKIXRevocationChecker.Option.<clinit>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.security.cert.PKIXRevocationChecker.Option.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.security.cert.PKIXRevocationChecker.Option.<clinit>():void");
        }
    }

    public abstract List<CertPathValidatorException> getSoftFailExceptions();

    protected PKIXRevocationChecker() {
    }

    public void setOcspResponder(URI uri) {
        this.ocspResponder = uri;
    }

    public URI getOcspResponder() {
        return this.ocspResponder;
    }

    public void setOcspResponderCert(X509Certificate cert) {
        this.ocspResponderCert = cert;
    }

    public X509Certificate getOcspResponderCert() {
        return this.ocspResponderCert;
    }

    public void setOcspExtensions(List<Extension> extensions) {
        List emptyList;
        if (extensions == null) {
            emptyList = Collections.emptyList();
        } else {
            emptyList = new ArrayList((Collection) extensions);
        }
        this.ocspExtensions = emptyList;
    }

    public List<Extension> getOcspExtensions() {
        return Collections.unmodifiableList(this.ocspExtensions);
    }

    public void setOcspResponses(Map<X509Certificate, byte[]> responses) {
        if (responses == null) {
            this.ocspResponses = Collections.emptyMap();
            return;
        }
        Map<X509Certificate, byte[]> copy = new HashMap(responses.size());
        for (Entry<X509Certificate, byte[]> e : responses.entrySet()) {
            copy.put((X509Certificate) e.getKey(), (byte[]) ((byte[]) e.getValue()).clone());
        }
        this.ocspResponses = copy;
    }

    public Map<X509Certificate, byte[]> getOcspResponses() {
        Map<X509Certificate, byte[]> copy = new HashMap(this.ocspResponses.size());
        for (Entry<X509Certificate, byte[]> e : this.ocspResponses.entrySet()) {
            copy.put((X509Certificate) e.getKey(), (byte[]) ((byte[]) e.getValue()).clone());
        }
        return copy;
    }

    public void setOptions(Set<Option> options) {
        Set emptySet;
        if (options == null) {
            emptySet = Collections.emptySet();
        } else {
            emptySet = new HashSet((Collection) options);
        }
        this.options = emptySet;
    }

    public Set<Option> getOptions() {
        return Collections.unmodifiableSet(this.options);
    }

    public PKIXRevocationChecker clone() {
        PKIXRevocationChecker copy = (PKIXRevocationChecker) super.clone();
        copy.ocspExtensions = new ArrayList(this.ocspExtensions);
        copy.ocspResponses = new HashMap(this.ocspResponses);
        for (Entry<X509Certificate, byte[]> entry : copy.ocspResponses.entrySet()) {
            entry.setValue((byte[]) ((byte[]) entry.getValue()).clone());
        }
        copy.options = new HashSet(this.options);
        return copy;
    }
}
