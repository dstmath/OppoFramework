package java.security;

import java.io.Serializable;
import java.security.cert.Certificate;

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
public final class UnresolvedPermission extends Permission implements Serializable {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.security.UnresolvedPermission.<init>(java.lang.String, java.lang.String, java.lang.String, java.security.cert.Certificate[]):void, dex: 
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
    public UnresolvedPermission(java.lang.String r1, java.lang.String r2, java.lang.String r3, java.security.cert.Certificate[] r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.security.UnresolvedPermission.<init>(java.lang.String, java.lang.String, java.lang.String, java.security.cert.Certificate[]):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.security.UnresolvedPermission.<init>(java.lang.String, java.lang.String, java.lang.String, java.security.cert.Certificate[]):void");
    }

    public boolean implies(Permission p) {
        return false;
    }

    public String getActions() {
        return null;
    }

    public String getUnresolvedType() {
        return null;
    }

    public String getUnresolvedName() {
        return null;
    }

    public String getUnresolvedActions() {
        return null;
    }

    public Certificate[] getUnresolvedCerts() {
        return null;
    }
}
