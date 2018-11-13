package gov.nist.javax.sip.message;

import gov.nist.javax.sip.parser.ParseExceptionListener;
import javax.sip.header.ServerHeader;
import javax.sip.header.UserAgentHeader;
import javax.sip.message.MessageFactory;

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
public class MessageFactoryImpl implements MessageFactory, MessageFactoryExt {
    private static String defaultContentEncodingCharset;
    private static ServerHeader server;
    private static UserAgentHeader userAgent;
    private boolean strict;
    private boolean testing;

    /* renamed from: gov.nist.javax.sip.message.MessageFactoryImpl$1 */
    class AnonymousClass1 implements ParseExceptionListener {
        final /* synthetic */ MessageFactoryImpl this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.message.MessageFactoryImpl.1.<init>(gov.nist.javax.sip.message.MessageFactoryImpl):void, dex: 
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
        AnonymousClass1(gov.nist.javax.sip.message.MessageFactoryImpl r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: gov.nist.javax.sip.message.MessageFactoryImpl.1.<init>(gov.nist.javax.sip.message.MessageFactoryImpl):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.1.<init>(gov.nist.javax.sip.message.MessageFactoryImpl):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.message.MessageFactoryImpl.1.handleException(java.text.ParseException, gov.nist.javax.sip.message.SIPMessage, java.lang.Class, java.lang.String, java.lang.String):void, dex: 
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
        public void handleException(java.text.ParseException r1, gov.nist.javax.sip.message.SIPMessage r2, java.lang.Class r3, java.lang.String r4, java.lang.String r5) throws java.text.ParseException {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: gov.nist.javax.sip.message.MessageFactoryImpl.1.handleException(java.text.ParseException, gov.nist.javax.sip.message.SIPMessage, java.lang.Class, java.lang.String, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.1.handleException(java.text.ParseException, gov.nist.javax.sip.message.SIPMessage, java.lang.Class, java.lang.String, java.lang.String):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: gov.nist.javax.sip.message.MessageFactoryImpl.-get0(gov.nist.javax.sip.message.MessageFactoryImpl):boolean, dex:  in method: gov.nist.javax.sip.message.MessageFactoryImpl.-get0(gov.nist.javax.sip.message.MessageFactoryImpl):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: gov.nist.javax.sip.message.MessageFactoryImpl.-get0(gov.nist.javax.sip.message.MessageFactoryImpl):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get0 */
    static /* synthetic */ boolean m19-get0(gov.nist.javax.sip.message.MessageFactoryImpl r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: gov.nist.javax.sip.message.MessageFactoryImpl.-get0(gov.nist.javax.sip.message.MessageFactoryImpl):boolean, dex:  in method: gov.nist.javax.sip.message.MessageFactoryImpl.-get0(gov.nist.javax.sip.message.MessageFactoryImpl):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.-get0(gov.nist.javax.sip.message.MessageFactoryImpl):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: gov.nist.javax.sip.message.MessageFactoryImpl.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public MessageFactoryImpl() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: gov.nist.javax.sip.message.MessageFactoryImpl.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createMultipartMimeContent(javax.sip.header.ContentTypeHeader, java.lang.String[], java.lang.String[], java.lang.String[]):gov.nist.javax.sip.message.MultipartMimeContent, dex: 
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
    public gov.nist.javax.sip.message.MultipartMimeContent createMultipartMimeContent(javax.sip.header.ContentTypeHeader r1, java.lang.String[] r2, java.lang.String[] r3, java.lang.String[] r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createMultipartMimeContent(javax.sip.header.ContentTypeHeader, java.lang.String[], java.lang.String[], java.lang.String[]):gov.nist.javax.sip.message.MultipartMimeContent, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createMultipartMimeContent(javax.sip.header.ContentTypeHeader, java.lang.String[], java.lang.String[], java.lang.String[]):gov.nist.javax.sip.message.MultipartMimeContent");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(java.lang.String):javax.sip.message.Request, dex: 
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
    public javax.sip.message.Request createRequest(java.lang.String r1) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(java.lang.String):javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(java.lang.String):javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader):javax.sip.message.Request, dex: 
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
    public javax.sip.message.Request createRequest(javax.sip.address.URI r1, java.lang.String r2, javax.sip.header.CallIdHeader r3, javax.sip.header.CSeqHeader r4, javax.sip.header.FromHeader r5, javax.sip.header.ToHeader r6, java.util.List r7, javax.sip.header.MaxForwardsHeader r8) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader):javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader):javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Request, dex: 
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
    public javax.sip.message.Request createRequest(javax.sip.address.URI r1, java.lang.String r2, javax.sip.header.CallIdHeader r3, javax.sip.header.CSeqHeader r4, javax.sip.header.FromHeader r5, javax.sip.header.ToHeader r6, java.util.List r7, javax.sip.header.MaxForwardsHeader r8, javax.sip.header.ContentTypeHeader r9, java.lang.Object r10) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Request, dex: 
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
    public javax.sip.message.Request createRequest(javax.sip.address.URI r1, java.lang.String r2, javax.sip.header.CallIdHeader r3, javax.sip.header.CSeqHeader r4, javax.sip.header.FromHeader r5, javax.sip.header.ToHeader r6, java.util.List r7, javax.sip.header.MaxForwardsHeader r8, javax.sip.header.ContentTypeHeader r9, byte[] r10) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, byte[], javax.sip.header.ContentTypeHeader):javax.sip.message.Request, dex: 
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
    public javax.sip.message.Request createRequest(javax.sip.address.URI r1, java.lang.String r2, javax.sip.header.CallIdHeader r3, javax.sip.header.CSeqHeader r4, javax.sip.header.FromHeader r5, javax.sip.header.ToHeader r6, java.util.List r7, javax.sip.header.MaxForwardsHeader r8, byte[] r9, javax.sip.header.ContentTypeHeader r10) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, byte[], javax.sip.header.ContentTypeHeader):javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createRequest(javax.sip.address.URI, java.lang.String, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, byte[], javax.sip.header.ContentTypeHeader):javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.header.CallIdHeader r2, javax.sip.header.CSeqHeader r3, javax.sip.header.FromHeader r4, javax.sip.header.ToHeader r5, java.util.List r6, javax.sip.header.MaxForwardsHeader r7) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, java.lang.Object, javax.sip.header.ContentTypeHeader):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.header.CallIdHeader r2, javax.sip.header.CSeqHeader r3, javax.sip.header.FromHeader r4, javax.sip.header.ToHeader r5, java.util.List r6, javax.sip.header.MaxForwardsHeader r7, java.lang.Object r8, javax.sip.header.ContentTypeHeader r9) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, java.lang.Object, javax.sip.header.ContentTypeHeader):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, java.lang.Object, javax.sip.header.ContentTypeHeader):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.header.CallIdHeader r2, javax.sip.header.CSeqHeader r3, javax.sip.header.FromHeader r4, javax.sip.header.ToHeader r5, java.util.List r6, javax.sip.header.MaxForwardsHeader r7, javax.sip.header.ContentTypeHeader r8, java.lang.Object r9) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.header.CallIdHeader r2, javax.sip.header.CSeqHeader r3, javax.sip.header.FromHeader r4, javax.sip.header.ToHeader r5, java.util.List r6, javax.sip.header.MaxForwardsHeader r7, javax.sip.header.ContentTypeHeader r8, byte[] r9) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, byte[], javax.sip.header.ContentTypeHeader):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.header.CallIdHeader r2, javax.sip.header.CSeqHeader r3, javax.sip.header.FromHeader r4, javax.sip.header.ToHeader r5, java.util.List r6, javax.sip.header.MaxForwardsHeader r7, byte[] r8, javax.sip.header.ContentTypeHeader r9) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, byte[], javax.sip.header.ContentTypeHeader):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.header.CallIdHeader, javax.sip.header.CSeqHeader, javax.sip.header.FromHeader, javax.sip.header.ToHeader, java.util.List, javax.sip.header.MaxForwardsHeader, byte[], javax.sip.header.ContentTypeHeader):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.message.Request r2) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.message.Request r2, javax.sip.header.ContentTypeHeader r3, java.lang.Object r4) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request, javax.sip.header.ContentTypeHeader, java.lang.Object):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(int r1, javax.sip.message.Request r2, javax.sip.header.ContentTypeHeader r3, byte[] r4) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(int, javax.sip.message.Request, javax.sip.header.ContentTypeHeader, byte[]):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(java.lang.String):javax.sip.message.Response, dex: 
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
    public javax.sip.message.Response createResponse(java.lang.String r1) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(java.lang.String):javax.sip.message.Response, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.createResponse(java.lang.String):javax.sip.message.Response");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultContentEncodingCharset(java.lang.String):void, dex: 
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
    public void setDefaultContentEncodingCharset(java.lang.String r1) throws java.lang.NullPointerException, java.lang.IllegalArgumentException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultContentEncodingCharset(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultContentEncodingCharset(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultServerHeader(javax.sip.header.ServerHeader):void, dex: 
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
    public void setDefaultServerHeader(javax.sip.header.ServerHeader r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultServerHeader(javax.sip.header.ServerHeader):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultServerHeader(javax.sip.header.ServerHeader):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultUserAgentHeader(javax.sip.header.UserAgentHeader):void, dex: 
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
    public void setDefaultUserAgentHeader(javax.sip.header.UserAgentHeader r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultUserAgentHeader(javax.sip.header.UserAgentHeader):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.setDefaultUserAgentHeader(javax.sip.header.UserAgentHeader):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: gov.nist.javax.sip.message.MessageFactoryImpl.setStrict(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00eb
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setStrict(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: gov.nist.javax.sip.message.MessageFactoryImpl.setStrict(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.setStrict(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: gov.nist.javax.sip.message.MessageFactoryImpl.setTest(boolean):void, dex:  in method: gov.nist.javax.sip.message.MessageFactoryImpl.setTest(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: gov.nist.javax.sip.message.MessageFactoryImpl.setTest(boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void setTest(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: gov.nist.javax.sip.message.MessageFactoryImpl.setTest(boolean):void, dex:  in method: gov.nist.javax.sip.message.MessageFactoryImpl.setTest(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: gov.nist.javax.sip.message.MessageFactoryImpl.setTest(boolean):void");
    }

    public static UserAgentHeader getDefaultUserAgentHeader() {
        return userAgent;
    }

    public static ServerHeader getDefaultServerHeader() {
        return server;
    }

    public static String getDefaultContentEncodingCharset() {
        return defaultContentEncodingCharset;
    }
}
