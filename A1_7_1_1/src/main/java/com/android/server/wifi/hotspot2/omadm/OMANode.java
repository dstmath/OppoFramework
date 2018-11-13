package com.android.server.wifi.hotspot2.omadm;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public abstract class OMANode {
    private static final Map<Character, String> sEscapes = null;
    private final Map<String, String> mAttributes;
    private final String mContext;
    private final String mName;
    private final OMAConstructed mParent;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.omadm.OMANode.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.omadm.OMANode.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.omadm.OMANode.<clinit>():void");
    }

    public abstract OMANode addChild(String str, String str2, String str3, String str4) throws IOException;

    public abstract void fillPayload(StringBuilder stringBuilder);

    public abstract OMANode getChild(String str) throws OMAException;

    public abstract Collection<OMANode> getChildren();

    public abstract OMANode getListValue(Iterator<String> it) throws OMAException;

    public abstract String getScalarValue(Iterator<String> it) throws OMAException;

    public abstract String getValue();

    public abstract boolean isLeaf();

    public abstract void marshal(OutputStream outputStream, int i) throws IOException;

    public abstract OMANode reparent(OMAConstructed oMAConstructed);

    public abstract void toString(StringBuilder stringBuilder, int i);

    protected OMANode(OMAConstructed parent, String name, String context, Map<String, String> avps) {
        this.mParent = parent;
        this.mName = name;
        this.mContext = context;
        this.mAttributes = avps;
    }

    protected static Map<String, String> buildAttributes(String[] avps) {
        if (avps == null) {
            return null;
        }
        Map<String, String> attributes = new HashMap();
        for (int n = 0; n < avps.length; n += 2) {
            attributes.put(avps[n], avps[n + 1]);
        }
        return attributes;
    }

    protected Map<String, String> getAttributes() {
        return this.mAttributes;
    }

    public OMAConstructed getParent() {
        return this.mParent;
    }

    public String getName() {
        return this.mName;
    }

    public String getContext() {
        return this.mContext;
    }

    public List<String> getPath() {
        LinkedList<String> path = new LinkedList();
        for (OMANode node = this; node != null; node = node.getParent()) {
            path.addFirst(node.getName());
        }
        return path;
    }

    public String getPathString() {
        StringBuilder sb = new StringBuilder();
        for (String element : getPath()) {
            sb.append('/').append(element);
        }
        return sb.toString();
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length());
        for (int n = 0; n < s.length(); n++) {
            char ch = s.charAt(n);
            String escape = (String) sEscapes.get(Character.valueOf(ch));
            if (escape != null) {
                sb.append(escape);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    public void toXml(StringBuilder sb) {
        sb.append('<').append(MOTree.NodeTag);
        if (!(this.mAttributes == null || this.mAttributes.isEmpty())) {
            for (Entry<String, String> avp : this.mAttributes.entrySet()) {
                sb.append(' ').append((String) avp.getKey()).append("=\"").append(escape((String) avp.getValue())).append('\"');
            }
        }
        sb.append(">\n");
        sb.append('<').append(MOTree.NodeNameTag).append('>');
        sb.append(getName());
        sb.append("</").append(MOTree.NodeNameTag).append(">\n");
        fillPayload(sb);
        sb.append("</").append(MOTree.NodeTag).append(">\n");
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        toString(sb, 0);
        return sb.toString();
    }

    public static OMAConstructed unmarshal(InputStream in) throws IOException {
        OMANode node = buildNode(in, null);
        if (node == null || node.isLeaf()) {
            throw new IOException("Bad OMA tree");
        }
        unmarshal(in, (OMAConstructed) node);
        return (OMAConstructed) node;
    }

    private static void unmarshal(InputStream in, OMAConstructed parent) throws IOException {
        while (true) {
            OMANode node = buildNode(in, parent);
            if (node != null) {
                if (!node.isLeaf()) {
                    unmarshal(in, (OMAConstructed) node);
                }
            } else {
                return;
            }
        }
    }

    private static OMANode buildNode(InputStream in, OMAConstructed parent) throws IOException {
        String name = OMAConstants.deserializeString(in);
        if (name == null) {
            return null;
        }
        String urn = null;
        int next = in.read();
        if (next == 40) {
            urn = OMAConstants.readURN(in);
            next = in.read();
        }
        if (next == 61) {
            return parent.addChild(name, urn, OMAConstants.deserializeString(in), null);
        }
        if (next != 43) {
            throw new IOException("Parse error: expected = or + after node name");
        } else if (parent != null) {
            return parent.addChild(name, urn, null, null);
        } else {
            return new OMAConstructed(null, name, urn, new String[0]);
        }
    }
}
