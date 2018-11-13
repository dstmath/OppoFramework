package java.net;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;
import java.sql.Types;
import sun.security.action.GetPropertyAction;

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
class SocksSocketImpl extends PlainSocketImpl implements SocksConsts {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f54-assertionsDisabled = false;
    private boolean applicationSetProxy;
    private InputStream cmdIn;
    private OutputStream cmdOut;
    private Socket cmdsock;
    private InetSocketAddress external_address;
    private String server;
    private int serverPort;
    private boolean useV4;

    /* renamed from: java.net.SocksSocketImpl$1 */
    class AnonymousClass1 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ SocksSocketImpl this$0;
        final /* synthetic */ String val$host;
        final /* synthetic */ int val$port;
        final /* synthetic */ int val$timeout;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.1.<init>(java.net.SocksSocketImpl, java.lang.String, int, int):void, dex: 
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
        AnonymousClass1(java.net.SocksSocketImpl r1, java.lang.String r2, int r3, int r4) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.1.<init>(java.net.SocksSocketImpl, java.lang.String, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.1.<init>(java.net.SocksSocketImpl, java.lang.String, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.1.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.1.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.1.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.1.run():java.lang.Void, dex: 
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
        public java.lang.Void run() throws java.io.IOException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.1.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.1.run():java.lang.Void");
        }
    }

    /* renamed from: java.net.SocksSocketImpl$2 */
    class AnonymousClass2 implements PrivilegedAction<PasswordAuthentication> {
        final /* synthetic */ SocksSocketImpl this$0;
        final /* synthetic */ InetAddress val$addr;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.2.<init>(java.net.SocksSocketImpl, java.net.InetAddress):void, dex: 
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
        AnonymousClass2(java.net.SocksSocketImpl r1, java.net.InetAddress r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.2.<init>(java.net.SocksSocketImpl, java.net.InetAddress):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.2.<init>(java.net.SocksSocketImpl, java.net.InetAddress):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.2.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.2.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.2.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.2.run():java.net.PasswordAuthentication, dex: 
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
        public java.net.PasswordAuthentication run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.2.run():java.net.PasswordAuthentication, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.2.run():java.net.PasswordAuthentication");
        }
    }

    /* renamed from: java.net.SocksSocketImpl$3 */
    class AnonymousClass3 implements PrivilegedAction<InetAddress> {
        final /* synthetic */ SocksSocketImpl this$0;

        AnonymousClass3(SocksSocketImpl this$0) {
            this.this$0 = this$0;
        }

        public /* bridge */ /* synthetic */ Object run() {
            return run();
        }

        public InetAddress run() {
            return this.this$0.cmdsock.getLocalAddress();
        }
    }

    /* renamed from: java.net.SocksSocketImpl$4 */
    class AnonymousClass4 implements PrivilegedAction<ProxySelector> {
        final /* synthetic */ SocksSocketImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.4.<init>(java.net.SocksSocketImpl):void, dex: 
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
        AnonymousClass4(java.net.SocksSocketImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.4.<init>(java.net.SocksSocketImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.4.<init>(java.net.SocksSocketImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.4.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.4.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.4.run():java.lang.Object");
        }

        public ProxySelector run() {
            return ProxySelector.getDefault();
        }
    }

    /* renamed from: java.net.SocksSocketImpl$5 */
    class AnonymousClass5 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ SocksSocketImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.5.<init>(java.net.SocksSocketImpl):void, dex: 
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
        AnonymousClass5(java.net.SocksSocketImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.5.<init>(java.net.SocksSocketImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.5.<init>(java.net.SocksSocketImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.5.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.5.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.5.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.5.run():java.lang.Void, dex: 
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
        public java.lang.Void run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.5.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.5.run():java.lang.Void");
        }
    }

    /* renamed from: java.net.SocksSocketImpl$6 */
    class AnonymousClass6 implements PrivilegedExceptionAction<Void> {
        final /* synthetic */ SocksSocketImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.6.<init>(java.net.SocksSocketImpl):void, dex: 
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
        AnonymousClass6(java.net.SocksSocketImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: java.net.SocksSocketImpl.6.<init>(java.net.SocksSocketImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.6.<init>(java.net.SocksSocketImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.6.run():java.lang.Object, dex: 
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
        public /* bridge */ /* synthetic */ java.lang.Object run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.6.run():java.lang.Object, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.6.run():java.lang.Object");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.6.run():java.lang.Void, dex: 
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
        public java.lang.Void run() throws java.lang.Exception {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: java.net.SocksSocketImpl.6.run():java.lang.Void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.6.run():java.lang.Void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.net.SocksSocketImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.<clinit>():void");
    }

    SocksSocketImpl() {
        this.server = null;
        this.serverPort = SocksConsts.DEFAULT_PORT;
        this.useV4 = false;
        this.cmdsock = null;
        this.cmdIn = null;
        this.cmdOut = null;
    }

    SocksSocketImpl(String server, int port) {
        this.server = null;
        this.serverPort = SocksConsts.DEFAULT_PORT;
        this.useV4 = false;
        this.cmdsock = null;
        this.cmdIn = null;
        this.cmdOut = null;
        this.server = server;
        if (port == -1) {
            port = SocksConsts.DEFAULT_PORT;
        }
        this.serverPort = port;
    }

    SocksSocketImpl(Proxy proxy) {
        this.server = null;
        this.serverPort = SocksConsts.DEFAULT_PORT;
        this.useV4 = false;
        this.cmdsock = null;
        this.cmdIn = null;
        this.cmdOut = null;
        SocketAddress a = proxy.address();
        if (a instanceof InetSocketAddress) {
            InetSocketAddress ad = (InetSocketAddress) a;
            this.server = ad.getHostString();
            this.serverPort = ad.getPort();
        }
    }

    void setV4() {
        this.useV4 = true;
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
    private synchronized void privilegedConnect(java.lang.String r3, int r4, int r5) throws java.io.IOException {
        /*
        r2 = this;
        monitor-enter(r2);
        r1 = new java.net.SocksSocketImpl$1;	 Catch:{ PrivilegedActionException -> 0x000b }
        r1.<init>(r2, r3, r4, r5);	 Catch:{ PrivilegedActionException -> 0x000b }
        java.security.AccessController.doPrivileged(r1);	 Catch:{ PrivilegedActionException -> 0x000b }
        monitor-exit(r2);
        return;
    L_0x000b:
        r0 = move-exception;
        r1 = r0.getException();	 Catch:{ all -> 0x0013 }
        r1 = (java.io.IOException) r1;	 Catch:{ all -> 0x0013 }
        throw r1;	 Catch:{ all -> 0x0013 }
    L_0x0013:
        r1 = move-exception;
        monitor-exit(r2);
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.privilegedConnect(java.lang.String, int, int):void");
    }

    private void superConnectServer(String host, int port, int timeout) throws IOException {
        super.connect(new InetSocketAddress(host, port), timeout);
    }

    private static int remainingMillis(long deadlineMillis) throws IOException {
        if (deadlineMillis == 0) {
            return 0;
        }
        long remaining = deadlineMillis - System.currentTimeMillis();
        if (remaining > 0) {
            return (int) remaining;
        }
        throw new SocketTimeoutException();
    }

    private int readSocksReply(InputStream in, byte[] data) throws IOException {
        return readSocksReply(in, data, 0);
    }

    private int readSocksReply(InputStream in, byte[] data, long deadlineMillis) throws IOException {
        int len = data.length;
        int received = 0;
        int attempts = 0;
        while (received < len && attempts < 3) {
            try {
                int count = ((SocketInputStream) in).read(data, received, len - received, remainingMillis(deadlineMillis));
                if (count < 0) {
                    throw new SocketException("Malformed reply from SOCKS server");
                }
                received += count;
                attempts++;
            } catch (SocketTimeoutException e) {
                throw new SocketTimeoutException("Connect timed out");
            }
        }
        return received;
    }

    private boolean authenticate(byte method, InputStream in, BufferedOutputStream out) throws IOException {
        return authenticate(method, in, out, 0);
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
    private boolean authenticate(byte r11, java.io.InputStream r12, java.io.BufferedOutputStream r13, long r14) throws java.io.IOException {
        /*
        r10 = this;
        if (r11 != 0) goto L_0x0004;
    L_0x0002:
        r7 = 1;
        return r7;
    L_0x0004:
        r7 = 2;
        if (r11 != r7) goto L_0x009a;
    L_0x0007:
        r3 = 0;
        r7 = r10.server;
        r0 = java.net.InetAddress.getByName(r7);
        r7 = new java.net.SocksSocketImpl$2;
        r7.<init>(r10, r0);
        r4 = java.security.AccessController.doPrivileged(r7);
        r4 = (java.net.PasswordAuthentication) r4;
        if (r4 == 0) goto L_0x002c;
    L_0x001b:
        r6 = r4.getUserName();
        r3 = new java.lang.String;
        r7 = r4.getPassword();
        r3.<init>(r7);
    L_0x0028:
        if (r6 != 0) goto L_0x003b;
    L_0x002a:
        r7 = 0;
        return r7;
    L_0x002c:
        r7 = new sun.security.action.GetPropertyAction;
        r8 = "user.name";
        r7.<init>(r8);
        r6 = java.security.AccessController.doPrivileged(r7);
        r6 = (java.lang.String) r6;
        goto L_0x0028;
    L_0x003b:
        r7 = 1;
        r13.write(r7);
        r7 = r6.length();
        r13.write(r7);
        r7 = "ISO-8859-1";	 Catch:{ UnsupportedEncodingException -> 0x007d }
        r7 = r6.getBytes(r7);	 Catch:{ UnsupportedEncodingException -> 0x007d }
        r13.write(r7);	 Catch:{ UnsupportedEncodingException -> 0x007d }
    L_0x0050:
        if (r3 == 0) goto L_0x0093;
    L_0x0052:
        r7 = r3.length();
        r13.write(r7);
        r7 = "ISO-8859-1";	 Catch:{ UnsupportedEncodingException -> 0x0088 }
        r7 = r3.getBytes(r7);	 Catch:{ UnsupportedEncodingException -> 0x0088 }
        r13.write(r7);	 Catch:{ UnsupportedEncodingException -> 0x0088 }
    L_0x0063:
        r13.flush();
        r7 = 2;
        r1 = new byte[r7];
        r2 = r10.readSocksReply(r12, r1, r14);
        r7 = 2;
        if (r2 != r7) goto L_0x0075;
    L_0x0070:
        r7 = 1;
        r7 = r1[r7];
        if (r7 == 0) goto L_0x0098;
    L_0x0075:
        r13.close();
        r12.close();
        r7 = 0;
        return r7;
    L_0x007d:
        r5 = move-exception;
        r7 = f54-assertionsDisabled;
        if (r7 != 0) goto L_0x0050;
    L_0x0082:
        r7 = new java.lang.AssertionError;
        r7.<init>();
        throw r7;
    L_0x0088:
        r5 = move-exception;
        r7 = f54-assertionsDisabled;
        if (r7 != 0) goto L_0x0063;
    L_0x008d:
        r7 = new java.lang.AssertionError;
        r7.<init>();
        throw r7;
    L_0x0093:
        r7 = 0;
        r13.write(r7);
        goto L_0x0063;
    L_0x0098:
        r7 = 1;
        return r7;
    L_0x009a:
        r7 = 0;
        return r7;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.authenticate(byte, java.io.InputStream, java.io.BufferedOutputStream, long):boolean");
    }

    private void connectV4(InputStream in, OutputStream out, InetSocketAddress endpoint, long deadlineMillis) throws IOException {
        if (endpoint.getAddress() instanceof Inet4Address) {
            out.write(4);
            out.write(1);
            out.write((endpoint.getPort() >> 8) & 255);
            out.write((endpoint.getPort() >> 0) & 255);
            out.write(endpoint.getAddress().getAddress());
            try {
                out.write(getUserName().getBytes("ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                if (!f54-assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            out.write(0);
            out.flush();
            byte[] data = new byte[8];
            int n = readSocksReply(in, data, deadlineMillis);
            if (n != 8) {
                throw new SocketException("Reply from SOCKS server has bad length: " + n);
            } else if (data[0] == (byte) 0 || data[0] == (byte) 4) {
                SocketException ex = null;
                switch (data[1]) {
                    case (byte) 90:
                        this.external_address = endpoint;
                        break;
                    case Types.DATE /*91*/:
                        ex = new SocketException("SOCKS request rejected");
                        break;
                    case Types.TIME /*92*/:
                        ex = new SocketException("SOCKS server couldn't reach destination");
                        break;
                    case Types.TIMESTAMP /*93*/:
                        ex = new SocketException("SOCKS authentication failed");
                        break;
                    default:
                        ex = new SocketException("Reply from SOCKS server contains bad status");
                        break;
                }
                if (ex != null) {
                    in.close();
                    out.close();
                    throw ex;
                }
                return;
            } else {
                throw new SocketException("Reply from SOCKS server has bad version");
            }
        }
        throw new SocketException("SOCKS V4 requires IPv4 only addresses");
    }

    protected void connect(SocketAddress endpoint, int timeout) throws IOException {
        long deadlineMillis;
        if (timeout == 0) {
            deadlineMillis = 0;
        } else {
            long finish = System.currentTimeMillis() + ((long) timeout);
            deadlineMillis = finish < 0 ? Long.MAX_VALUE : finish;
        }
        SecurityManager security = System.getSecurityManager();
        if (endpoint == null || !(endpoint instanceof InetSocketAddress)) {
            throw new IllegalArgumentException("Unsupported address type");
        }
        InetSocketAddress epoint = (InetSocketAddress) endpoint;
        if (security != null) {
            if (epoint.isUnresolved()) {
                security.checkConnect(epoint.getHostName(), epoint.getPort());
            } else {
                security.checkConnect(epoint.getAddress().getHostAddress(), epoint.getPort());
            }
        }
        if (this.server == null) {
            super.connect((SocketAddress) epoint, remainingMillis(deadlineMillis));
            return;
        }
        try {
            privilegedConnect(this.server, this.serverPort, remainingMillis(deadlineMillis));
            BufferedOutputStream out = new BufferedOutputStream(this.cmdOut, 512);
            InputStream in = this.cmdIn;
            if (!this.useV4) {
                out.write(5);
                out.write(2);
                out.write(0);
                out.write(2);
                out.flush();
                byte[] data = new byte[2];
                if (readSocksReply(in, data, deadlineMillis) == 2 && data[0] == (byte) 5) {
                    if (data[1] == (byte) -1) {
                        throw new SocketException("SOCKS : No acceptable methods");
                    }
                    if (authenticate(data[1], in, out, deadlineMillis)) {
                        out.write(5);
                        out.write(1);
                        out.write(0);
                        if (epoint.isUnresolved()) {
                            out.write(3);
                            out.write(epoint.getHostName().length());
                            try {
                                out.write(epoint.getHostName().getBytes("ISO-8859-1"));
                            } catch (UnsupportedEncodingException e) {
                                if (!f54-assertionsDisabled) {
                                    throw new AssertionError();
                                }
                            }
                            out.write((epoint.getPort() >> 8) & 255);
                            out.write((epoint.getPort() >> 0) & 255);
                        } else if (epoint.getAddress() instanceof Inet6Address) {
                            out.write(4);
                            out.write(epoint.getAddress().getAddress());
                            out.write((epoint.getPort() >> 8) & 255);
                            out.write((epoint.getPort() >> 0) & 255);
                        } else {
                            out.write(1);
                            out.write(epoint.getAddress().getAddress());
                            out.write((epoint.getPort() >> 8) & 255);
                            out.write((epoint.getPort() >> 0) & 255);
                        }
                        out.flush();
                        data = new byte[4];
                        if (readSocksReply(in, data, deadlineMillis) != 4) {
                            throw new SocketException("Reply from SOCKS server has bad length");
                        }
                        SocketException ex = null;
                        SocketException socketException;
                        switch (data[1]) {
                            case (byte) 0:
                                int len;
                                switch (data[3]) {
                                    case (byte) 1:
                                        if (readSocksReply(in, new byte[4], deadlineMillis) != 4) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        if (readSocksReply(in, new byte[2], deadlineMillis) != 2) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        break;
                                    case (byte) 3:
                                        len = data[1];
                                        if (readSocksReply(in, new byte[len], deadlineMillis) != len) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        if (readSocksReply(in, new byte[2], deadlineMillis) != 2) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        break;
                                    case (byte) 4:
                                        len = data[1];
                                        if (readSocksReply(in, new byte[len], deadlineMillis) != len) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        if (readSocksReply(in, new byte[2], deadlineMillis) != 2) {
                                            throw new SocketException("Reply from SOCKS server badly formatted");
                                        }
                                        break;
                                    default:
                                        socketException = new SocketException("Reply from SOCKS server contains wrong code");
                                        break;
                                }
                            case (byte) 1:
                                socketException = new SocketException("SOCKS server general failure");
                                break;
                            case (byte) 2:
                                socketException = new SocketException("SOCKS: Connection not allowed by ruleset");
                                break;
                            case (byte) 3:
                                socketException = new SocketException("SOCKS: Network unreachable");
                                break;
                            case (byte) 4:
                                socketException = new SocketException("SOCKS: Host unreachable");
                                break;
                            case (byte) 5:
                                socketException = new SocketException("SOCKS: Connection refused");
                                break;
                            case (byte) 6:
                                socketException = new SocketException("SOCKS: TTL expired");
                                break;
                            case (byte) 7:
                                socketException = new SocketException("SOCKS: Command not supported");
                                break;
                            case (byte) 8:
                                socketException = new SocketException("SOCKS: address type not supported");
                                break;
                        }
                        if (ex != null) {
                            in.close();
                            out.close();
                            throw ex;
                        }
                        this.external_address = epoint;
                        return;
                    }
                    throw new SocketException("SOCKS : authentication failed");
                } else if (epoint.isUnresolved()) {
                    throw new UnknownHostException(epoint.toString());
                } else {
                    connectV4(in, out, epoint, deadlineMillis);
                }
            } else if (epoint.isUnresolved()) {
                throw new UnknownHostException(epoint.toString());
            } else {
                connectV4(in, out, epoint, deadlineMillis);
            }
        } catch (IOException e2) {
            throw new SocketException(e2.getMessage());
        }
    }

    private void bindV4(InputStream in, OutputStream out, InetAddress baddr, int lport) throws IOException {
        if (baddr instanceof Inet4Address) {
            super.bind(baddr, lport);
            byte[] addr1 = baddr.getAddress();
            InetAddress naddr = baddr;
            if (baddr.isAnyLocalAddress()) {
                addr1 = ((InetAddress) AccessController.doPrivileged(new AnonymousClass3(this))).getAddress();
            }
            out.write(4);
            out.write(2);
            out.write((super.getLocalPort() >> 8) & 255);
            out.write((super.getLocalPort() >> 0) & 255);
            out.write(addr1);
            try {
                out.write(getUserName().getBytes("ISO-8859-1"));
            } catch (UnsupportedEncodingException e) {
                if (!f54-assertionsDisabled) {
                    throw new AssertionError();
                }
            }
            out.write(0);
            out.flush();
            byte[] data = new byte[8];
            int n = readSocksReply(in, data);
            if (n != 8) {
                throw new SocketException("Reply from SOCKS server has bad length: " + n);
            } else if (data[0] == (byte) 0 || data[0] == (byte) 4) {
                SocketException ex = null;
                switch (data[1]) {
                    case (byte) 90:
                        this.external_address = new InetSocketAddress(baddr, lport);
                        break;
                    case Types.DATE /*91*/:
                        ex = new SocketException("SOCKS request rejected");
                        break;
                    case Types.TIME /*92*/:
                        ex = new SocketException("SOCKS server couldn't reach destination");
                        break;
                    case Types.TIMESTAMP /*93*/:
                        ex = new SocketException("SOCKS authentication failed");
                        break;
                    default:
                        ex = new SocketException("Reply from SOCKS server contains bad status");
                        break;
                }
                if (ex != null) {
                    in.close();
                    out.close();
                    throw ex;
                }
                return;
            } else {
                throw new SocketException("Reply from SOCKS server has bad version");
            }
        }
        throw new SocketException("SOCKS V4 requires IPv4 only addresses");
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:java.net.SocksSocketImpl.socksBind(java.net.InetSocketAddress):void, dom blocks: [B:18:0x0041, B:76:0x01e4, B:108:0x02f4]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    protected synchronized void socksBind(java.net.InetSocketAddress r28) throws java.io.IOException {
        /*
        r27 = this;
        monitor-enter(r27);
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.socket;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x000b;
    L_0x0009:
        monitor-exit(r27);
        return;
    L_0x000b:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.server;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x01e4;	 Catch:{ all -> 0x00d5 }
    L_0x0013:
        r24 = new java.net.SocksSocketImpl$4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1);	 Catch:{ all -> 0x00d5 }
        r21 = java.security.AccessController.doPrivileged(r24);	 Catch:{ all -> 0x00d5 }
        r21 = (java.net.ProxySelector) r21;	 Catch:{ all -> 0x00d5 }
        if (r21 != 0) goto L_0x0026;
    L_0x0024:
        monitor-exit(r27);
        return;
    L_0x0026:
        r10 = r28.getHostString();	 Catch:{ all -> 0x00d5 }
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.Inet6Address;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x0041;	 Catch:{ all -> 0x00d5 }
    L_0x0036:
        r24 = "[";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r24 = r10.startsWith(r0);	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x009e;
    L_0x0041:
        r23 = new java.net.URI;	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = new java.lang.StringBuilder;	 Catch:{ URISyntaxException -> 0x00c8 }
        r24.<init>();	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = "serversocket://";	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = sun.net.www.ParseUtil.encodePath(r10);	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = ":";	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r25 = r28.getPort();	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.append(r25);	 Catch:{ URISyntaxException -> 0x00c8 }
        r24 = r24.toString();	 Catch:{ URISyntaxException -> 0x00c8 }
        r23.<init>(r24);	 Catch:{ URISyntaxException -> 0x00c8 }
    L_0x006d:
        r19 = 0;
        r20 = 0;
        r13 = 0;
        r0 = r21;	 Catch:{ all -> 0x00d5 }
        r1 = r23;	 Catch:{ all -> 0x00d5 }
        r24 = r0.select(r1);	 Catch:{ all -> 0x00d5 }
        r13 = r24.iterator();	 Catch:{ all -> 0x00d5 }
        if (r13 == 0) goto L_0x00db;	 Catch:{ all -> 0x00d5 }
    L_0x0080:
        r24 = r13.hasNext();	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x00db;	 Catch:{ all -> 0x00d5 }
    L_0x0086:
        r24 = r13.hasNext();	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x01b6;	 Catch:{ all -> 0x00d5 }
    L_0x008c:
        r19 = r13.next();	 Catch:{ all -> 0x00d5 }
        r19 = (java.net.Proxy) r19;	 Catch:{ all -> 0x00d5 }
        if (r19 == 0) goto L_0x009c;	 Catch:{ all -> 0x00d5 }
    L_0x0094:
        r24 = java.net.Proxy.NO_PROXY;	 Catch:{ all -> 0x00d5 }
        r0 = r19;
        r1 = r24;
        if (r0 != r1) goto L_0x00dd;
    L_0x009c:
        monitor-exit(r27);
        return;
    L_0x009e:
        r24 = ":";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r24 = r10.indexOf(r0);	 Catch:{ all -> 0x00d5 }
        if (r24 < 0) goto L_0x0041;	 Catch:{ all -> 0x00d5 }
    L_0x00a9:
        r24 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r24.<init>();	 Catch:{ all -> 0x00d5 }
        r25 = "[";	 Catch:{ all -> 0x00d5 }
        r24 = r24.append(r25);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r24 = r0.append(r10);	 Catch:{ all -> 0x00d5 }
        r25 = "]";	 Catch:{ all -> 0x00d5 }
        r24 = r24.append(r25);	 Catch:{ all -> 0x00d5 }
        r10 = r24.toString();	 Catch:{ all -> 0x00d5 }
        goto L_0x0041;	 Catch:{ all -> 0x00d5 }
    L_0x00c8:
        r8 = move-exception;	 Catch:{ all -> 0x00d5 }
        r24 = f54-assertionsDisabled;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x00d8;	 Catch:{ all -> 0x00d5 }
    L_0x00cd:
        r24 = new java.lang.AssertionError;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r8);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x00d5:
        r24 = move-exception;
        monitor-exit(r27);
        throw r24;
    L_0x00d8:
        r23 = 0;
        goto L_0x006d;
    L_0x00db:
        monitor-exit(r27);
        return;
    L_0x00dd:
        r24 = r19.type();	 Catch:{ all -> 0x00d5 }
        r25 = java.net.Proxy.Type.SOCKS;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 == r1) goto L_0x0107;	 Catch:{ all -> 0x00d5 }
    L_0x00e9:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "Unknown proxy type : ";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r26 = r19.type();	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0107:
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x0131;	 Catch:{ all -> 0x00d5 }
    L_0x0113:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "Unknow address type for proxy: ";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r19;	 Catch:{ all -> 0x00d5 }
        r25 = r0.append(r1);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0131:
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r24 = (java.net.InetSocketAddress) r24;	 Catch:{ all -> 0x00d5 }
        r24 = r24.getHostString();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.server = r0;	 Catch:{ all -> 0x00d5 }
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r24 = (java.net.InetSocketAddress) r24;	 Catch:{ all -> 0x00d5 }
        r24 = r24.getPort();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.serverPort = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r19;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof sun.net.SocksProxy;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x0173;	 Catch:{ all -> 0x00d5 }
    L_0x0159:
        r0 = r19;	 Catch:{ all -> 0x00d5 }
        r0 = (sun.net.SocksProxy) r0;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r24 = r24.protocolVersion();	 Catch:{ all -> 0x00d5 }
        r25 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 != r1) goto L_0x0173;	 Catch:{ all -> 0x00d5 }
    L_0x016b:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.useV4 = r0;	 Catch:{ all -> 0x00d5 }
    L_0x0173:
        r24 = new java.net.SocksSocketImpl$5;	 Catch:{ Exception -> 0x0181 }
        r0 = r24;	 Catch:{ Exception -> 0x0181 }
        r1 = r27;	 Catch:{ Exception -> 0x0181 }
        r0.<init>(r1);	 Catch:{ Exception -> 0x0181 }
        java.security.AccessController.doPrivileged(r24);	 Catch:{ Exception -> 0x0181 }
        goto L_0x0086;
    L_0x0181:
        r7 = move-exception;
        r24 = r19.address();	 Catch:{ all -> 0x00d5 }
        r25 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r26 = r7.getMessage();	 Catch:{ all -> 0x00d5 }
        r25.<init>(r26);	 Catch:{ all -> 0x00d5 }
        r0 = r21;	 Catch:{ all -> 0x00d5 }
        r1 = r23;	 Catch:{ all -> 0x00d5 }
        r2 = r24;	 Catch:{ all -> 0x00d5 }
        r3 = r25;	 Catch:{ all -> 0x00d5 }
        r0.connectFailed(r1, r2, r3);	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.server = r0;	 Catch:{ all -> 0x00d5 }
        r24 = -1;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.serverPort = r0;	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.cmdsock = r0;	 Catch:{ all -> 0x00d5 }
        r20 = r7;	 Catch:{ all -> 0x00d5 }
        goto L_0x0086;	 Catch:{ all -> 0x00d5 }
    L_0x01b6:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.server;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x01c6;	 Catch:{ all -> 0x00d5 }
    L_0x01be:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdsock;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x01f0;	 Catch:{ all -> 0x00d5 }
    L_0x01c6:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "Can't connect to SOCKS proxy:";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r26 = r20.getMessage();	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x01e4:
        r24 = new java.net.SocksSocketImpl$6;	 Catch:{ Exception -> 0x0224 }
        r0 = r24;	 Catch:{ Exception -> 0x0224 }
        r1 = r27;	 Catch:{ Exception -> 0x0224 }
        r0.<init>(r1);	 Catch:{ Exception -> 0x0224 }
        java.security.AccessController.doPrivileged(r24);	 Catch:{ Exception -> 0x0224 }
    L_0x01f0:
        r18 = new java.io.BufferedOutputStream;	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdOut;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r25 = 512; // 0x200 float:7.175E-43 double:2.53E-321;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r2 = r25;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r14 = r0.cmdIn;	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.useV4;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x022f;	 Catch:{ all -> 0x00d5 }
    L_0x020f:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r25 = r28.getPort();	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r1 = r18;	 Catch:{ all -> 0x00d5 }
        r2 = r24;	 Catch:{ all -> 0x00d5 }
        r3 = r25;	 Catch:{ all -> 0x00d5 }
        r0.bindV4(r14, r1, r2, r3);	 Catch:{ all -> 0x00d5 }
        monitor-exit(r27);
        return;
    L_0x0224:
        r7 = move-exception;
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = r7.getMessage();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x022f:
        r24 = 5;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r18.flush();	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 != r0) goto L_0x0274;	 Catch:{ all -> 0x00d5 }
    L_0x0268:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r25 = 5;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 == r1) goto L_0x0289;	 Catch:{ all -> 0x00d5 }
    L_0x0274:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r25 = r28.getPort();	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r1 = r18;	 Catch:{ all -> 0x00d5 }
        r2 = r24;	 Catch:{ all -> 0x00d5 }
        r3 = r25;	 Catch:{ all -> 0x00d5 }
        r0.bindV4(r14, r1, r2, r3);	 Catch:{ all -> 0x00d5 }
        monitor-exit(r27);
        return;
    L_0x0289:
        r24 = 1;
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r25 = -1;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        if (r0 != r1) goto L_0x029e;	 Catch:{ all -> 0x00d5 }
    L_0x0295:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "SOCKS : No acceptable methods";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x029e:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r2 = r18;	 Catch:{ all -> 0x00d5 }
        r24 = r0.authenticate(r1, r14, r2);	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x02b7;	 Catch:{ all -> 0x00d5 }
    L_0x02ae:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "SOCKS : authentication failed";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x02b7:
        r24 = 5;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r16 = r28.getPort();	 Catch:{ all -> 0x00d5 }
        r24 = r28.isUnresolved();	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x035d;	 Catch:{ all -> 0x00d5 }
    L_0x02dc:
        r24 = 3;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r28.getHostName();	 Catch:{ all -> 0x00d5 }
        r24 = r24.length();	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r28.getHostName();	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r25 = "ISO-8859-1";	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r24 = r24.getBytes(r25);	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r0 = r18;	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r1 = r24;	 Catch:{ UnsupportedEncodingException -> 0x0352 }
        r0.write(r1);	 Catch:{ UnsupportedEncodingException -> 0x0352 }
    L_0x0306:
        r24 = r16 >> 8;
        r0 = r24;
        r0 = r0 & 255;
        r24 = r0;
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
    L_0x0324:
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r9 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        switch(r24) {
            case 0: goto L_0x040d;
            case 1: goto L_0x0550;
            case 2: goto L_0x055c;
            case 3: goto L_0x0568;
            case 4: goto L_0x0574;
            case 5: goto L_0x0580;
            case 6: goto L_0x058c;
            case 7: goto L_0x0598;
            case 8: goto L_0x05a4;
            default: goto L_0x0338;
        };	 Catch:{ all -> 0x00d5 }
    L_0x0338:
        if (r9 == 0) goto L_0x05b0;	 Catch:{ all -> 0x00d5 }
    L_0x033a:
        r14.close();	 Catch:{ all -> 0x00d5 }
        r18.close();	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdsock;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r24.close();	 Catch:{ all -> 0x00d5 }
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.cmdsock = r0;	 Catch:{ all -> 0x00d5 }
        throw r9;	 Catch:{ all -> 0x00d5 }
    L_0x0352:
        r22 = move-exception;	 Catch:{ all -> 0x00d5 }
        r24 = f54-assertionsDisabled;	 Catch:{ all -> 0x00d5 }
        if (r24 != 0) goto L_0x0306;	 Catch:{ all -> 0x00d5 }
    L_0x0357:
        r24 = new java.lang.AssertionError;	 Catch:{ all -> 0x00d5 }
        r24.<init>();	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x035d:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.Inet4Address;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x03a1;	 Catch:{ all -> 0x00d5 }
    L_0x0369:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r5 = r24.getAddress();	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r0.write(r5);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 8;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r18.flush();	 Catch:{ all -> 0x00d5 }
        goto L_0x0324;	 Catch:{ all -> 0x00d5 }
    L_0x03a1:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 instanceof java.net.Inet6Address;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        if (r24 == 0) goto L_0x03e6;	 Catch:{ all -> 0x00d5 }
    L_0x03ad:
        r24 = r28.getAddress();	 Catch:{ all -> 0x00d5 }
        r5 = r24.getAddress();	 Catch:{ all -> 0x00d5 }
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r0.write(r5);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 8;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r24 = r16 >> 0;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r24;	 Catch:{ all -> 0x00d5 }
        r0.write(r1);	 Catch:{ all -> 0x00d5 }
        r18.flush();	 Catch:{ all -> 0x00d5 }
        goto L_0x0324;	 Catch:{ all -> 0x00d5 }
    L_0x03e6:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0 = r0.cmdsock;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r24.close();	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00d5 }
        r25.<init>();	 Catch:{ all -> 0x00d5 }
        r26 = "unsupported address type : ";	 Catch:{ all -> 0x00d5 }
        r25 = r25.append(r26);	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r28;	 Catch:{ all -> 0x00d5 }
        r25 = r0.append(r1);	 Catch:{ all -> 0x00d5 }
        r25 = r25.toString();	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x040d:
        r24 = 3;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        switch(r24) {
            case 1: goto L_0x0416;
            case 2: goto L_0x0414;
            case 3: goto L_0x0483;
            case 4: goto L_0x04e7;
            default: goto L_0x0414;
        };	 Catch:{ all -> 0x00d5 }
    L_0x0414:
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0416:
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r4 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r4);	 Catch:{ all -> 0x00d5 }
        r24 = 4;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x0431;	 Catch:{ all -> 0x00d5 }
    L_0x0428:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0431:
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x044c;	 Catch:{ all -> 0x00d5 }
    L_0x0443:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x044c:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r24 << 8;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r17 + r24;	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r25 = new java.net.Inet4Address;	 Catch:{ all -> 0x00d5 }
        r26 = "";	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r26;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r4);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        r2 = r17;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.external_address = r0;	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0483:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r15 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r11 = new byte[r15];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r11);	 Catch:{ all -> 0x00d5 }
        if (r12 == r15) goto L_0x049a;	 Catch:{ all -> 0x00d5 }
    L_0x0491:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x049a:
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x04b5;	 Catch:{ all -> 0x00d5 }
    L_0x04ac:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x04b5:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r24 << 8;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r17 + r24;	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r25 = new java.lang.String;	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r11);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        r2 = r17;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.external_address = r0;	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x04e7:
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r15 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r4 = new byte[r15];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r4);	 Catch:{ all -> 0x00d5 }
        if (r12 == r15) goto L_0x04fe;	 Catch:{ all -> 0x00d5 }
    L_0x04f5:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x04fe:
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r6 = new byte[r0];	 Catch:{ all -> 0x00d5 }
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r12 = r0.readSocksReply(r14, r6);	 Catch:{ all -> 0x00d5 }
        r24 = 2;	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        if (r12 == r0) goto L_0x0519;	 Catch:{ all -> 0x00d5 }
    L_0x0510:
        r24 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r25 = "Reply from SOCKS server badly formatted";	 Catch:{ all -> 0x00d5 }
        r24.<init>(r25);	 Catch:{ all -> 0x00d5 }
        throw r24;	 Catch:{ all -> 0x00d5 }
    L_0x0519:
        r24 = 0;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r24 << 8;	 Catch:{ all -> 0x00d5 }
        r24 = 1;	 Catch:{ all -> 0x00d5 }
        r24 = r6[r24];	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r0 = r0 & 255;	 Catch:{ all -> 0x00d5 }
        r24 = r0;	 Catch:{ all -> 0x00d5 }
        r17 = r17 + r24;	 Catch:{ all -> 0x00d5 }
        r24 = new java.net.InetSocketAddress;	 Catch:{ all -> 0x00d5 }
        r25 = new java.net.Inet6Address;	 Catch:{ all -> 0x00d5 }
        r26 = "";	 Catch:{ all -> 0x00d5 }
        r0 = r25;	 Catch:{ all -> 0x00d5 }
        r1 = r26;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r4);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r25;	 Catch:{ all -> 0x00d5 }
        r2 = r17;	 Catch:{ all -> 0x00d5 }
        r0.<init>(r1, r2);	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.external_address = r0;	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0550:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS server general failure";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x055c:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Bind not allowed by ruleset";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0568:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Network unreachable";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0574:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Host unreachable";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0580:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Connection refused";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x058c:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: TTL expired";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x0598:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: Command not supported";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x05a4:
        r9 = new java.net.SocketException;	 Catch:{ all -> 0x00d5 }
        r24 = "SOCKS: address type not supported";	 Catch:{ all -> 0x00d5 }
        r0 = r24;	 Catch:{ all -> 0x00d5 }
        r9.<init>(r0);	 Catch:{ all -> 0x00d5 }
        goto L_0x0338;	 Catch:{ all -> 0x00d5 }
    L_0x05b0:
        r0 = r27;	 Catch:{ all -> 0x00d5 }
        r0.cmdIn = r14;	 Catch:{ all -> 0x00d5 }
        r0 = r18;	 Catch:{ all -> 0x00d5 }
        r1 = r27;	 Catch:{ all -> 0x00d5 }
        r1.cmdOut = r0;	 Catch:{ all -> 0x00d5 }
        monitor-exit(r27);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: java.net.SocksSocketImpl.socksBind(java.net.InetSocketAddress):void");
    }

    protected InetAddress getInetAddress() {
        if (this.external_address != null) {
            return this.external_address.getAddress();
        }
        return super.getInetAddress();
    }

    protected int getPort() {
        if (this.external_address != null) {
            return this.external_address.getPort();
        }
        return super.getPort();
    }

    protected int getLocalPort() {
        if (this.socket != null) {
            return super.getLocalPort();
        }
        if (this.external_address != null) {
            return this.external_address.getPort();
        }
        return super.getLocalPort();
    }

    protected void close() throws IOException {
        if (this.cmdsock != null) {
            this.cmdsock.close();
        }
        this.cmdsock = null;
        super.close();
    }

    private String getUserName() {
        String userName = "";
        if (!this.applicationSetProxy) {
            return (String) AccessController.doPrivileged(new GetPropertyAction("user.name"));
        }
        try {
            return System.getProperty("user.name");
        } catch (SecurityException e) {
            return userName;
        }
    }
}
