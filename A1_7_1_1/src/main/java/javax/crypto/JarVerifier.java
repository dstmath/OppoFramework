package javax.crypto;

import java.net.URL;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;

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
final class JarVerifier {
    private CryptoPermissions appPerms;
    private URL jarURL;
    private boolean savePerms;

    /* renamed from: javax.crypto.JarVerifier$1 */
    class AnonymousClass1 implements PrivilegedExceptionAction {
        final /* synthetic */ JarVerifier this$0;
        final /* synthetic */ URL val$url;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: javax.crypto.JarVerifier.1.<init>(javax.crypto.JarVerifier, java.net.URL):void, dex: 
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
        AnonymousClass1(javax.crypto.JarVerifier r1, java.net.URL r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: javax.crypto.JarVerifier.1.<init>(javax.crypto.JarVerifier, java.net.URL):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: javax.crypto.JarVerifier.1.<init>(javax.crypto.JarVerifier, java.net.URL):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: javax.crypto.JarVerifier.1.run():java.lang.Object, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public java.lang.Object run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: javax.crypto.JarVerifier.1.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: javax.crypto.JarVerifier.1.run():java.lang.Object");
        }
    }

    JarVerifier(URL jarURL, boolean savePerms) {
        this.appPerms = null;
        this.jarURL = jarURL;
        this.savePerms = savePerms;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    void verify() throws java.util.jar.JarException, java.io.IOException {
        /*
        r10 = this;
        r8 = r10.savePerms;
        if (r8 != 0) goto L_0x0005;
    L_0x0004:
        return;
    L_0x0005:
        r8 = r10.jarURL;
        r8 = r8.getProtocol();
        r9 = "jar";
        r8 = r8.equalsIgnoreCase(r9);
        if (r8 == 0) goto L_0x003f;
    L_0x0014:
        r7 = r10.jarURL;
    L_0x0016:
        r4 = 0;
        r8 = new javax.crypto.JarVerifier$1;	 Catch:{ PrivilegedActionException -> 0x0066 }
        r8.<init>(r10, r7);	 Catch:{ PrivilegedActionException -> 0x0066 }
        r8 = java.security.AccessController.doPrivileged(r8);	 Catch:{ PrivilegedActionException -> 0x0066 }
        r0 = r8;	 Catch:{ PrivilegedActionException -> 0x0066 }
        r0 = (java.util.jar.JarFile) r0;	 Catch:{ PrivilegedActionException -> 0x0066 }
        r4 = r0;	 Catch:{ PrivilegedActionException -> 0x0066 }
        if (r4 == 0) goto L_0x0098;
    L_0x0026:
        r8 = "cryptoPerms";	 Catch:{ all -> 0x0038 }
        r2 = r4.getJarEntry(r8);	 Catch:{ all -> 0x0038 }
        if (r2 != 0) goto L_0x0088;	 Catch:{ all -> 0x0038 }
    L_0x002f:
        r8 = new java.util.jar.JarException;	 Catch:{ all -> 0x0038 }
        r9 = "Can not find cryptoPerms";	 Catch:{ all -> 0x0038 }
        r8.<init>(r9);	 Catch:{ all -> 0x0038 }
        throw r8;	 Catch:{ all -> 0x0038 }
    L_0x0038:
        r8 = move-exception;
        if (r4 == 0) goto L_0x003e;
    L_0x003b:
        r4.close();
    L_0x003e:
        throw r8;
    L_0x003f:
        r7 = new java.net.URL;
        r8 = new java.lang.StringBuilder;
        r8.<init>();
        r9 = "jar:";
        r8 = r8.append(r9);
        r9 = r10.jarURL;
        r9 = r9.toString();
        r8 = r8.append(r9);
        r9 = "!/";
        r8 = r8.append(r9);
        r8 = r8.toString();
        r7.<init>(r8);
        goto L_0x0016;
    L_0x0066:
        r5 = move-exception;
        r6 = new java.lang.SecurityException;	 Catch:{ all -> 0x0038 }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0038 }
        r8.<init>();	 Catch:{ all -> 0x0038 }
        r9 = "Cannot load ";	 Catch:{ all -> 0x0038 }
        r8 = r8.append(r9);	 Catch:{ all -> 0x0038 }
        r9 = r7.toString();	 Catch:{ all -> 0x0038 }
        r8 = r8.append(r9);	 Catch:{ all -> 0x0038 }
        r8 = r8.toString();	 Catch:{ all -> 0x0038 }
        r6.<init>(r8);	 Catch:{ all -> 0x0038 }
        r6.initCause(r5);	 Catch:{ all -> 0x0038 }
        throw r6;	 Catch:{ all -> 0x0038 }
    L_0x0088:
        r8 = new javax.crypto.CryptoPermissions;	 Catch:{ Exception -> 0x009e }
        r8.<init>();	 Catch:{ Exception -> 0x009e }
        r10.appPerms = r8;	 Catch:{ Exception -> 0x009e }
        r8 = r10.appPerms;	 Catch:{ Exception -> 0x009e }
        r9 = r4.getInputStream(r2);	 Catch:{ Exception -> 0x009e }
        r8.load(r9);	 Catch:{ Exception -> 0x009e }
    L_0x0098:
        if (r4 == 0) goto L_0x009d;
    L_0x009a:
        r4.close();
    L_0x009d:
        return;
    L_0x009e:
        r1 = move-exception;
        r3 = new java.util.jar.JarException;	 Catch:{ all -> 0x0038 }
        r8 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0038 }
        r8.<init>();	 Catch:{ all -> 0x0038 }
        r9 = "Cannot load/parse";	 Catch:{ all -> 0x0038 }
        r8 = r8.append(r9);	 Catch:{ all -> 0x0038 }
        r9 = r10.jarURL;	 Catch:{ all -> 0x0038 }
        r9 = r9.toString();	 Catch:{ all -> 0x0038 }
        r8 = r8.append(r9);	 Catch:{ all -> 0x0038 }
        r8 = r8.toString();	 Catch:{ all -> 0x0038 }
        r3.<init>(r8);	 Catch:{ all -> 0x0038 }
        r3.initCause(r1);	 Catch:{ all -> 0x0038 }
        throw r3;	 Catch:{ all -> 0x0038 }
        */
        throw new UnsupportedOperationException("Method not decompiled: javax.crypto.JarVerifier.verify():void");
    }

    static void verifyPolicySigned(Certificate[] certs) throws Exception {
    }

    CryptoPermissions getPermissions() {
        return this.appPerms;
    }
}
