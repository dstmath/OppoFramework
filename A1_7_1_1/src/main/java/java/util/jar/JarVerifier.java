package java.util.jar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSigner;
import java.security.CodeSource;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import sun.security.util.Debug;
import sun.security.util.ManifestDigester;
import sun.security.util.ManifestEntryVerifier;
import sun.security.util.SignatureFileVerifier;

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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
class JarVerifier {
    static final Debug debug = null;
    private boolean anyToVerify;
    private ByteArrayOutputStream baos;
    private Object csdomain;
    boolean eagerValidation;
    private Enumeration emptyEnumeration;
    private CodeSigner[] emptySigner;
    private List jarCodeSigners;
    private URL lastURL;
    private Map lastURLMap;
    private volatile ManifestDigester manDig;
    private List manifestDigests;
    byte[] manifestRawBytes;
    private boolean parsingBlockOrSF;
    private boolean parsingMeta;
    private ArrayList pendingBlocks;
    private Hashtable sigFileData;
    private Hashtable sigFileSigners;
    private ArrayList signerCache;
    private Map signerMap;
    private Map signerToCodeSource;
    private Map urlToCodeSourceMap;
    private Hashtable verifiedSigners;

    /* renamed from: java.util.jar.JarVerifier$2 */
    class AnonymousClass2 implements Enumeration<String> {
        String name;
        final /* synthetic */ JarVerifier this$0;
        final /* synthetic */ Enumeration val$enum2;
        final /* synthetic */ Iterator val$itor;
        final /* synthetic */ List val$signersReq;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.2.<init>(java.util.jar.JarVerifier, java.util.Iterator, java.util.List, java.util.Enumeration):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass2(java.util.jar.JarVerifier r1, java.util.Iterator r2, java.util.List r3, java.util.Enumeration r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.2.<init>(java.util.jar.JarVerifier, java.util.Iterator, java.util.List, java.util.Enumeration):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.2.<init>(java.util.jar.JarVerifier, java.util.Iterator, java.util.List, java.util.Enumeration):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.2.hasMoreElements():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean hasMoreElements() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.2.hasMoreElements():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.2.hasMoreElements():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.2.nextElement():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.2.nextElement():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.2.nextElement():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.2.nextElement():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.2.nextElement():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.2.nextElement():java.lang.String");
        }
    }

    /* renamed from: java.util.jar.JarVerifier$3 */
    class AnonymousClass3 implements Enumeration<JarEntry> {
        JarEntry entry;
        Enumeration signers;
        final /* synthetic */ JarVerifier this$0;
        final /* synthetic */ Enumeration val$enum_;
        final /* synthetic */ JarFile val$jar;
        final /* synthetic */ Map val$map;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.3.<init>(java.util.jar.JarVerifier, java.util.Enumeration, java.util.jar.JarFile, java.util.Map):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(java.util.jar.JarVerifier r1, java.util.Enumeration r2, java.util.jar.JarFile r3, java.util.Map r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.3.<init>(java.util.jar.JarVerifier, java.util.Enumeration, java.util.jar.JarFile, java.util.Map):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.3.<init>(java.util.jar.JarVerifier, java.util.Enumeration, java.util.jar.JarFile, java.util.Map):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.3.hasMoreElements():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean hasMoreElements() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.3.hasMoreElements():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.3.hasMoreElements():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.3.nextElement():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.3.nextElement():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.3.nextElement():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.3.nextElement():java.util.jar.JarEntry, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.util.jar.JarEntry nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.3.nextElement():java.util.jar.JarEntry, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.3.nextElement():java.util.jar.JarEntry");
        }
    }

    /* renamed from: java.util.jar.JarVerifier$4 */
    class AnonymousClass4 implements Enumeration<String> {
        String name;
        final /* synthetic */ JarVerifier this$0;
        final /* synthetic */ Enumeration val$entries;
        final /* synthetic */ Map val$map;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.4.<init>(java.util.jar.JarVerifier, java.util.Enumeration, java.util.Map):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass4(java.util.jar.JarVerifier r1, java.util.Enumeration r2, java.util.Map r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.4.<init>(java.util.jar.JarVerifier, java.util.Enumeration, java.util.Map):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.4.<init>(java.util.jar.JarVerifier, java.util.Enumeration, java.util.Map):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.4.hasMoreElements():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean hasMoreElements() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.4.hasMoreElements():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.4.hasMoreElements():boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.4.nextElement():java.lang.Object, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public /* bridge */ /* synthetic */ java.lang.Object nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.4.nextElement():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.4.nextElement():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.4.nextElement():java.lang.String, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.String nextElement() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.util.jar.JarVerifier.4.nextElement():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.4.nextElement():java.lang.String");
        }
    }

    private static class VerifierCodeSource extends CodeSource {
        Object csdomain;
        Certificate[] vcerts;
        URL vlocation;
        CodeSigner[] vsigners;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.CodeSigner[]):void, dex:  in method: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.CodeSigner[]):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.CodeSigner[]):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        VerifierCodeSource(java.lang.Object r1, java.net.URL r2, java.security.CodeSigner[] r3) {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.CodeSigner[]):void, dex:  in method: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.CodeSigner[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.CodeSigner[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.cert.Certificate[]):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        VerifierCodeSource(java.lang.Object r1, java.net.URL r2, java.security.cert.Certificate[] r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.cert.Certificate[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.VerifierCodeSource.<init>(java.lang.Object, java.net.URL, java.security.cert.Certificate[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateCertificates():java.security.cert.Certificate[], dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        private java.security.cert.Certificate[] getPrivateCertificates() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateCertificates():java.security.cert.Certificate[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateCertificates():java.security.cert.Certificate[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateSigners():java.security.CodeSigner[], dex:  in method: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateSigners():java.security.CodeSigner[], dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateSigners():java.security.CodeSigner[], dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        private java.security.CodeSigner[] getPrivateSigners() {
            /*
            // Can't load method instructions: Load method exception: null in method: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateSigners():java.security.CodeSigner[], dex:  in method: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateSigners():java.security.CodeSigner[], dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.VerifierCodeSource.getPrivateSigners():java.security.CodeSigner[]");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.VerifierCodeSource.equals(java.lang.Object):boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean equals(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.VerifierCodeSource.equals(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.VerifierCodeSource.equals(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.VerifierCodeSource.isSameDomain(java.lang.Object):boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        boolean isSameDomain(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.util.jar.JarVerifier.VerifierCodeSource.isSameDomain(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.VerifierCodeSource.isSameDomain(java.lang.Object):boolean");
        }
    }

    static class VerifierStream extends InputStream {
        private InputStream is;
        private JarVerifier jv;
        private ManifestEntryVerifier mev;
        private long numLeft;

        VerifierStream(Manifest man, JarEntry je, InputStream is, JarVerifier jv) throws IOException {
            if (is == null) {
                throw new NullPointerException("is == null");
            }
            this.is = is;
            this.jv = jv;
            this.mev = new ManifestEntryVerifier(man);
            this.jv.beginEntry(je, this.mev);
            this.numLeft = je.getSize();
            if (this.numLeft == 0) {
                this.jv.update(-1, this.mev);
            }
        }

        public int read() throws IOException {
            if (this.is == null) {
                throw new IOException("stream closed");
            } else if (this.numLeft <= 0) {
                return -1;
            } else {
                int b = this.is.read();
                this.jv.update(b, this.mev);
                this.numLeft--;
                if (this.numLeft == 0) {
                    this.jv.update(-1, this.mev);
                }
                return b;
            }
        }

        public int read(byte[] b, int off, int len) throws IOException {
            if (this.is == null) {
                throw new IOException("stream closed");
            }
            if (this.numLeft > 0 && this.numLeft < ((long) len)) {
                len = (int) this.numLeft;
            }
            if (this.numLeft <= 0) {
                return -1;
            }
            int n = this.is.read(b, off, len);
            this.jv.update(n, b, off, len, this.mev);
            this.numLeft -= (long) n;
            if (this.numLeft == 0) {
                this.jv.update(-1, b, off, len, this.mev);
            }
            return n;
        }

        public void close() throws IOException {
            if (this.is != null) {
                this.is.close();
            }
            this.is = null;
            this.mev = null;
            this.jv = null;
        }

        public int available() throws IOException {
            if (this.is != null) {
                return this.is.available();
            }
            throw new IOException("stream closed");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: java.util.jar.JarVerifier.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: java.util.jar.JarVerifier.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.<clinit>():void");
    }

    public JarVerifier(byte[] rawBytes) {
        this.parsingBlockOrSF = false;
        this.parsingMeta = true;
        this.anyToVerify = true;
        this.manifestRawBytes = null;
        this.csdomain = new Object();
        this.urlToCodeSourceMap = new HashMap();
        this.signerToCodeSource = new HashMap();
        this.emptySigner = new CodeSigner[0];
        this.emptyEnumeration = new Enumeration<String>() {
            public boolean hasMoreElements() {
                return false;
            }

            public String nextElement() {
                throw new NoSuchElementException();
            }
        };
        this.manifestRawBytes = rawBytes;
        this.sigFileSigners = new Hashtable();
        this.verifiedSigners = new Hashtable();
        this.sigFileData = new Hashtable(11);
        this.pendingBlocks = new ArrayList();
        this.baos = new ByteArrayOutputStream();
        this.manifestDigests = new ArrayList();
    }

    public void beginEntry(JarEntry je, ManifestEntryVerifier mev) throws IOException {
        if (je != null) {
            if (debug != null) {
                debug.println("beginEntry " + je.getName());
            }
            String name = je.getName();
            if (this.parsingMeta) {
                String uname = name.toUpperCase(Locale.ENGLISH);
                if (uname.startsWith("META-INF/") || uname.startsWith("/META-INF/")) {
                    if (je.isDirectory()) {
                        mev.setEntry(null, je);
                        return;
                    }
                    if (SignatureFileVerifier.isBlockOrSF(uname)) {
                        this.parsingBlockOrSF = true;
                        this.baos.reset();
                        mev.setEntry(null, je);
                    }
                    return;
                }
            }
            if (this.parsingMeta) {
                doneWithMeta();
            }
            if (je.isDirectory()) {
                mev.setEntry(null, je);
                return;
            }
            if (name.startsWith("./")) {
                name = name.substring(2);
            }
            if (name.startsWith("/")) {
                name = name.substring(1);
            }
            if (this.sigFileSigners.get(name) != null) {
                mev.setEntry(name, je);
            } else {
                mev.setEntry(null, je);
            }
        }
    }

    public void update(int b, ManifestEntryVerifier mev) throws IOException {
        if (b == -1) {
            processEntry(mev);
        } else if (this.parsingBlockOrSF) {
            this.baos.write(b);
        } else {
            mev.update((byte) b);
        }
    }

    public void update(int n, byte[] b, int off, int len, ManifestEntryVerifier mev) throws IOException {
        if (n == -1) {
            processEntry(mev);
        } else if (this.parsingBlockOrSF) {
            this.baos.write(b, off, n);
        } else {
            mev.update(b, off, n);
        }
    }

    private void processEntry(ManifestEntryVerifier mev) throws IOException {
        if (this.parsingBlockOrSF) {
            try {
                this.parsingBlockOrSF = false;
                if (debug != null) {
                    debug.println("processEntry: processing block");
                }
                String uname = mev.getEntry().getName().toUpperCase(Locale.ENGLISH);
                String key;
                byte[] bytes;
                SignatureFileVerifier sfv;
                if (uname.endsWith(".SF")) {
                    key = uname.substring(0, uname.length() - 3);
                    bytes = this.baos.toByteArray();
                    this.sigFileData.put(key, bytes);
                    Iterator it = this.pendingBlocks.iterator();
                    while (it.hasNext()) {
                        sfv = (SignatureFileVerifier) it.next();
                        if (sfv.needSignatureFile(key)) {
                            if (debug != null) {
                                debug.println("processEntry: processing pending block");
                            }
                            sfv.setSignatureFile(bytes);
                            sfv.process(this.sigFileSigners, this.manifestDigests);
                        }
                    }
                    return;
                }
                key = uname.substring(0, uname.lastIndexOf("."));
                if (this.signerCache == null) {
                    this.signerCache = new ArrayList();
                }
                if (this.manDig == null) {
                    synchronized (this.manifestRawBytes) {
                        if (this.manDig == null) {
                            this.manDig = new ManifestDigester(this.manifestRawBytes);
                            this.manifestRawBytes = null;
                        }
                    }
                }
                sfv = new SignatureFileVerifier(this.signerCache, this.manDig, uname, this.baos.toByteArray());
                if (sfv.needSignatureFileBytes()) {
                    bytes = (byte[]) this.sigFileData.get(key);
                    if (bytes == null) {
                        if (debug != null) {
                            debug.println("adding pending block");
                        }
                        this.pendingBlocks.add(sfv);
                        return;
                    }
                    sfv.setSignatureFile(bytes);
                }
                sfv.process(this.sigFileSigners, this.manifestDigests);
            } catch (Object ioe) {
                if (debug != null) {
                    debug.println("processEntry caught: " + ioe);
                }
            } catch (Object se) {
                if (debug != null) {
                    debug.println("processEntry caught: " + se);
                }
            } catch (Object nsae) {
                if (debug != null) {
                    debug.println("processEntry caught: " + nsae);
                }
            } catch (Object ce) {
                if (debug != null) {
                    debug.println("processEntry caught: " + ce);
                }
            }
        } else {
            JarEntry je = mev.getEntry();
            if (je != null && je.signers == null) {
                je.signers = mev.verify(this.verifiedSigners, this.sigFileSigners);
                je.certs = mapSignersToCertArray(je.signers);
            }
        }
    }

    @Deprecated
    public Certificate[] getCerts(String name) {
        return mapSignersToCertArray(getCodeSigners(name));
    }

    public Certificate[] getCerts(JarFile jar, JarEntry entry) {
        return mapSignersToCertArray(getCodeSigners(jar, entry));
    }

    public CodeSigner[] getCodeSigners(String name) {
        return (CodeSigner[]) this.verifiedSigners.get(name);
    }

    public CodeSigner[] getCodeSigners(JarFile jar, JarEntry entry) {
        String name = entry.getName();
        if (this.eagerValidation && this.sigFileSigners.get(name) != null) {
            try {
                InputStream s = jar.getInputStream(entry);
                byte[] buffer = new byte[1024];
                for (int n = buffer.length; n != -1; n = s.read(buffer, 0, buffer.length)) {
                }
                s.close();
            } catch (IOException e) {
            }
        }
        return getCodeSigners(name);
    }

    private static Certificate[] mapSignersToCertArray(CodeSigner[] signers) {
        if (signers == null) {
            return null;
        }
        ArrayList certChains = new ArrayList();
        for (CodeSigner signerCertPath : signers) {
            certChains.addAll(signerCertPath.getSignerCertPath().getCertificates());
        }
        return (Certificate[]) certChains.toArray(new Certificate[certChains.size()]);
    }

    boolean nothingToVerify() {
        return !this.anyToVerify;
    }

    void doneWithMeta() {
        boolean z = false;
        this.parsingMeta = false;
        if (!this.sigFileSigners.isEmpty()) {
            z = true;
        }
        this.anyToVerify = z;
        this.baos = null;
        this.sigFileData = null;
        this.pendingBlocks = null;
        this.signerCache = null;
        this.manDig = null;
        if (this.sigFileSigners.containsKey(JarFile.MANIFEST_NAME)) {
            this.verifiedSigners.put(JarFile.MANIFEST_NAME, this.sigFileSigners.remove(JarFile.MANIFEST_NAME));
        }
    }

    private synchronized CodeSource mapSignersToCodeSource(URL url, CodeSigner[] signers) {
        CodeSource cs;
        Map map;
        if (url == this.lastURL) {
            map = this.lastURLMap;
        } else {
            map = (Map) this.urlToCodeSourceMap.get(url);
            if (map == null) {
                map = new HashMap();
                this.urlToCodeSourceMap.put(url, map);
            }
            this.lastURLMap = map;
            this.lastURL = url;
        }
        cs = (CodeSource) map.get(signers);
        if (cs == null) {
            cs = new VerifierCodeSource(this.csdomain, url, signers);
            this.signerToCodeSource.put(signers, cs);
        }
        return cs;
    }

    private CodeSource[] mapSignersToCodeSources(URL url, List signers, boolean unsigned) {
        List sources = new ArrayList();
        for (int i = 0; i < signers.size(); i++) {
            sources.add(mapSignersToCodeSource(url, (CodeSigner[]) signers.get(i)));
        }
        if (unsigned) {
            sources.add(mapSignersToCodeSource(url, null));
        }
        return (CodeSource[]) sources.toArray(new CodeSource[sources.size()]);
    }

    private CodeSigner[] findMatchingSigners(CodeSource cs) {
        if ((cs instanceof VerifierCodeSource) && ((VerifierCodeSource) cs).isSameDomain(this.csdomain)) {
            return ((VerifierCodeSource) cs).getPrivateSigners();
        }
        CodeSource[] sources = mapSignersToCodeSources(cs.getLocation(), getJarCodeSigners(), true);
        List sourceList = new ArrayList();
        for (Object add : sources) {
            sourceList.add(add);
        }
        int j = sourceList.indexOf(cs);
        if (j == -1) {
            return null;
        }
        CodeSigner[] match = ((VerifierCodeSource) sourceList.get(j)).getPrivateSigners();
        if (match == null) {
            match = this.emptySigner;
        }
        return match;
    }

    private synchronized Map signerMap() {
        if (this.signerMap == null) {
            this.signerMap = new HashMap(this.verifiedSigners.size() + this.sigFileSigners.size());
            this.signerMap.putAll(this.verifiedSigners);
            this.signerMap.putAll(this.sigFileSigners);
        }
        return this.signerMap;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public synchronized java.util.Enumeration<java.lang.String> entryNames(java.util.jar.JarFile r10, java.security.CodeSource[] r11) {
        /*
        r9 = this;
        monitor-enter(r9);
        r3 = r9.signerMap();	 Catch:{ all -> 0x003c }
        r8 = r3.entrySet();	 Catch:{ all -> 0x003c }
        r2 = r8.iterator();	 Catch:{ all -> 0x003c }
        r5 = 0;	 Catch:{ all -> 0x003c }
        r6 = new java.util.ArrayList;	 Catch:{ all -> 0x003c }
        r8 = r11.length;	 Catch:{ all -> 0x003c }
        r6.<init>(r8);	 Catch:{ all -> 0x003c }
        r1 = 0;	 Catch:{ all -> 0x003c }
    L_0x0015:
        r8 = r11.length;	 Catch:{ all -> 0x003c }
        if (r1 >= r8) goto L_0x002b;	 Catch:{ all -> 0x003c }
    L_0x0018:
        r8 = r11[r1];	 Catch:{ all -> 0x003c }
        r4 = r9.findMatchingSigners(r8);	 Catch:{ all -> 0x003c }
        if (r4 == 0) goto L_0x0026;	 Catch:{ all -> 0x003c }
    L_0x0020:
        r8 = r4.length;	 Catch:{ all -> 0x003c }
        if (r8 <= 0) goto L_0x0029;	 Catch:{ all -> 0x003c }
    L_0x0023:
        r6.add(r4);	 Catch:{ all -> 0x003c }
    L_0x0026:
        r1 = r1 + 1;	 Catch:{ all -> 0x003c }
        goto L_0x0015;	 Catch:{ all -> 0x003c }
    L_0x0029:
        r5 = 1;	 Catch:{ all -> 0x003c }
        goto L_0x0026;	 Catch:{ all -> 0x003c }
    L_0x002b:
        r7 = r6;	 Catch:{ all -> 0x003c }
        if (r5 == 0) goto L_0x0039;	 Catch:{ all -> 0x003c }
    L_0x002e:
        r0 = r9.unsignedEntryNames(r10);	 Catch:{ all -> 0x003c }
    L_0x0032:
        r8 = new java.util.jar.JarVerifier$2;	 Catch:{ all -> 0x003c }
        r8.<init>(r9, r2, r6, r0);	 Catch:{ all -> 0x003c }
        monitor-exit(r9);
        return r8;
    L_0x0039:
        r0 = r9.emptyEnumeration;	 Catch:{ all -> 0x003c }
        goto L_0x0032;
    L_0x003c:
        r8 = move-exception;
        monitor-exit(r9);
        throw r8;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.entryNames(java.util.jar.JarFile, java.security.CodeSource[]):java.util.Enumeration<java.lang.String>");
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public java.util.Enumeration<java.util.jar.JarEntry> entries2(java.util.jar.JarFile r4, java.util.Enumeration r5) {
        /*
        r3 = this;
        r1 = new java.util.HashMap;
        r1.<init>();
        r2 = r3.signerMap();
        r1.putAll(r2);
        r0 = r5;
        r2 = new java.util.jar.JarVerifier$3;
        r2.<init>(r3, r5, r4, r1);
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.entries2(java.util.jar.JarFile, java.util.Enumeration):java.util.Enumeration<java.util.jar.JarEntry>");
    }

    static boolean isSigningRelated(String name) {
        name = name.toUpperCase(Locale.ENGLISH);
        if (!name.startsWith("META-INF/")) {
            return false;
        }
        name = name.substring(9);
        if (name.indexOf(47) != -1) {
            return false;
        }
        if (name.endsWith(".DSA") || name.endsWith(".RSA") || name.endsWith(".SF") || name.endsWith(".EC") || name.startsWith("SIG-") || name.equals("MANIFEST.MF")) {
            return true;
        }
        return false;
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
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private java.util.Enumeration<java.lang.String> unsignedEntryNames(java.util.jar.JarFile r4) {
        /*
        r3 = this;
        r1 = r3.signerMap();
        r0 = r4.entries();
        r2 = new java.util.jar.JarVerifier$4;
        r2.<init>(r3, r0, r1);
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.jar.JarVerifier.unsignedEntryNames(java.util.jar.JarFile):java.util.Enumeration<java.lang.String>");
    }

    private synchronized List getJarCodeSigners() {
        if (this.jarCodeSigners == null) {
            HashSet set = new HashSet();
            set.addAll(signerMap().values());
            this.jarCodeSigners = new ArrayList();
            this.jarCodeSigners.addAll(set);
        }
        return this.jarCodeSigners;
    }

    public synchronized CodeSource[] getCodeSources(JarFile jar, URL url) {
        return mapSignersToCodeSources(url, getJarCodeSigners(), unsignedEntryNames(jar).hasMoreElements());
    }

    public CodeSource getCodeSource(URL url, String name) {
        return mapSignersToCodeSource(url, (CodeSigner[]) signerMap().get(name));
    }

    public CodeSource getCodeSource(URL url, JarFile jar, JarEntry je) {
        return mapSignersToCodeSource(url, getCodeSigners(jar, je));
    }

    public void setEagerValidation(boolean eager) {
        this.eagerValidation = eager;
    }

    public synchronized List getManifestDigests() {
        return Collections.unmodifiableList(this.manifestDigests);
    }

    static CodeSource getUnsignedCS(URL url) {
        return new VerifierCodeSource(null, url, (Certificate[]) null);
    }
}
