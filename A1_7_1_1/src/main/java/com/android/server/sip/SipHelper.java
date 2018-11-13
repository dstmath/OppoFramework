package com.android.server.sip;

import android.net.sip.SipProfile;
import java.text.ParseException;
import javax.sip.Dialog;
import javax.sip.ListeningPoint;
import javax.sip.SipException;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.Transaction;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.CallIdHeader;
import javax.sip.header.ContactHeader;
import javax.sip.header.HeaderFactory;
import javax.sip.header.ToHeader;
import javax.sip.message.Message;
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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class SipHelper {
    private static final boolean DBG = true;
    private static final boolean DBG_PING = false;
    private static final String TAG = null;
    private AddressFactory mAddressFactory;
    private HeaderFactory mHeaderFactory;
    private MessageFactory mMessageFactory;
    private SipProvider mSipProvider;
    private SipStack mSipStack;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipHelper.<init>(javax.sip.SipStack, javax.sip.SipProvider):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public SipHelper(javax.sip.SipStack r1, javax.sip.SipProvider r2) throws javax.sip.PeerUnavailableException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.server.sip.SipHelper.<init>(javax.sip.SipStack, javax.sip.SipProvider):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.<init>(javax.sip.SipStack, javax.sip.SipProvider):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createCSeqHeader(java.lang.String):javax.sip.header.CSeqHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.CSeqHeader createCSeqHeader(java.lang.String r1) throws java.text.ParseException, javax.sip.InvalidArgumentException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createCSeqHeader(java.lang.String):javax.sip.header.CSeqHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createCSeqHeader(java.lang.String):javax.sip.header.CSeqHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createCallIdHeader():javax.sip.header.CallIdHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.CallIdHeader createCallIdHeader() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createCallIdHeader():javax.sip.header.CallIdHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createCallIdHeader():javax.sip.header.CallIdHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.createContactHeader(android.net.sip.SipProfile, java.lang.String, int):javax.sip.header.ContactHeader, dex: 
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
    private javax.sip.header.ContactHeader createContactHeader(android.net.sip.SipProfile r1, java.lang.String r2, int r3) throws java.text.ParseException, javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.createContactHeader(android.net.sip.SipProfile, java.lang.String, int):javax.sip.header.ContactHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createContactHeader(android.net.sip.SipProfile, java.lang.String, int):javax.sip.header.ContactHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createFromHeader(android.net.sip.SipProfile, java.lang.String):javax.sip.header.FromHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.FromHeader createFromHeader(android.net.sip.SipProfile r1, java.lang.String r2) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createFromHeader(android.net.sip.SipProfile, java.lang.String):javax.sip.header.FromHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createFromHeader(android.net.sip.SipProfile, java.lang.String):javax.sip.header.FromHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createMaxForwardsHeader():javax.sip.header.MaxForwardsHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.MaxForwardsHeader createMaxForwardsHeader() throws javax.sip.InvalidArgumentException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createMaxForwardsHeader():javax.sip.header.MaxForwardsHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createMaxForwardsHeader():javax.sip.header.MaxForwardsHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createMaxForwardsHeader(int):javax.sip.header.MaxForwardsHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.MaxForwardsHeader createMaxForwardsHeader(int r1) throws javax.sip.InvalidArgumentException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createMaxForwardsHeader(int):javax.sip.header.MaxForwardsHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createMaxForwardsHeader(int):javax.sip.header.MaxForwardsHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.createRequest(java.lang.String, android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String):javax.sip.message.Request, dex: 
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
    private javax.sip.message.Request createRequest(java.lang.String r1, android.net.sip.SipProfile r2, android.net.sip.SipProfile r3, java.lang.String r4) throws java.text.ParseException, javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.createRequest(java.lang.String, android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String):javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createRequest(java.lang.String, android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String):javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.server.sip.SipHelper.createRequest(java.lang.String, android.net.sip.SipProfile, java.lang.String):javax.sip.message.Request, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ea
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private javax.sip.message.Request createRequest(java.lang.String r1, android.net.sip.SipProfile r2, java.lang.String r3) throws java.text.ParseException, javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.server.sip.SipHelper.createRequest(java.lang.String, android.net.sip.SipProfile, java.lang.String):javax.sip.message.Request, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createRequest(java.lang.String, android.net.sip.SipProfile, java.lang.String):javax.sip.message.Request");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createSipUri(java.lang.String, java.lang.String, java.lang.String, int):javax.sip.address.SipURI, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.address.SipURI createSipUri(java.lang.String r1, java.lang.String r2, java.lang.String r3, int r4) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createSipUri(java.lang.String, java.lang.String, java.lang.String, int):javax.sip.address.SipURI, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createSipUri(java.lang.String, java.lang.String, java.lang.String, int):javax.sip.address.SipURI");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createToHeader(android.net.sip.SipProfile, java.lang.String):javax.sip.header.ToHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.ToHeader createToHeader(android.net.sip.SipProfile r1, java.lang.String r2) throws java.text.ParseException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createToHeader(android.net.sip.SipProfile, java.lang.String):javax.sip.header.ToHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createToHeader(android.net.sip.SipProfile, java.lang.String):javax.sip.header.ToHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createViaHeaders():java.util.List<javax.sip.header.ViaHeader>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private java.util.List<javax.sip.header.ViaHeader> createViaHeaders() throws java.text.ParseException, javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createViaHeaders():java.util.List<javax.sip.header.ViaHeader>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createViaHeaders():java.util.List<javax.sip.header.ViaHeader>");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createWildcardContactHeader():javax.sip.header.ContactHeader, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.header.ContactHeader createWildcardContactHeader() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.createWildcardContactHeader():javax.sip.header.ContactHeader, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.createWildcardContactHeader():javax.sip.header.ContactHeader");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.getCallId(java.util.EventObject):java.lang.String, dex: 
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
    public static java.lang.String getCallId(java.util.EventObject r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.getCallId(java.util.EventObject):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.getCallId(java.util.EventObject):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.getListeningPoint():javax.sip.ListeningPoint, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    private javax.sip.ListeningPoint getListeningPoint() throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.getListeningPoint():javax.sip.ListeningPoint, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.getListeningPoint():javax.sip.ListeningPoint");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipHelper.log(java.lang.String):void, dex: 
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
    private void log(java.lang.String r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.sip.SipHelper.log(java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.log(java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.getServerTransaction(javax.sip.RequestEvent):javax.sip.ServerTransaction, dex: 
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
    public javax.sip.ServerTransaction getServerTransaction(javax.sip.RequestEvent r1) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.getServerTransaction(javax.sip.RequestEvent):javax.sip.ServerTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.getServerTransaction(javax.sip.RequestEvent):javax.sip.ServerTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.handleChallenge(javax.sip.ResponseEvent, gov.nist.javax.sip.clientauthutils.AccountManager):javax.sip.ClientTransaction, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public javax.sip.ClientTransaction handleChallenge(javax.sip.ResponseEvent r1, gov.nist.javax.sip.clientauthutils.AccountManager r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.handleChallenge(javax.sip.ResponseEvent, gov.nist.javax.sip.clientauthutils.AccountManager):javax.sip.ClientTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.handleChallenge(javax.sip.ResponseEvent, gov.nist.javax.sip.clientauthutils.AccountManager):javax.sip.ClientTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendBye(javax.sip.Dialog):void, dex: 
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
    public void sendBye(javax.sip.Dialog r1) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendBye(javax.sip.Dialog):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendBye(javax.sip.Dialog):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendCancel(javax.sip.ClientTransaction):void, dex: 
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
    public void sendCancel(javax.sip.ClientTransaction r1) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendCancel(javax.sip.ClientTransaction):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendCancel(javax.sip.ClientTransaction):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendInvite(android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String, java.lang.String, gov.nist.javax.sip.header.extensions.ReferredByHeader, java.lang.String):javax.sip.ClientTransaction, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public javax.sip.ClientTransaction sendInvite(android.net.sip.SipProfile r1, android.net.sip.SipProfile r2, java.lang.String r3, java.lang.String r4, gov.nist.javax.sip.header.extensions.ReferredByHeader r5, java.lang.String r6) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendInvite(android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String, java.lang.String, gov.nist.javax.sip.header.extensions.ReferredByHeader, java.lang.String):javax.sip.ClientTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendInvite(android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String, java.lang.String, gov.nist.javax.sip.header.extensions.ReferredByHeader, java.lang.String):javax.sip.ClientTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendInviteAck(javax.sip.ResponseEvent, javax.sip.Dialog):void, dex: 
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
    public void sendInviteAck(javax.sip.ResponseEvent r1, javax.sip.Dialog r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendInviteAck(javax.sip.ResponseEvent, javax.sip.Dialog):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendInviteAck(javax.sip.ResponseEvent, javax.sip.Dialog):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendInviteBusyHere(javax.sip.RequestEvent, javax.sip.ServerTransaction):void, dex: 
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
    public void sendInviteBusyHere(javax.sip.RequestEvent r1, javax.sip.ServerTransaction r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendInviteBusyHere(javax.sip.RequestEvent, javax.sip.ServerTransaction):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendInviteBusyHere(javax.sip.RequestEvent, javax.sip.ServerTransaction):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendInviteOk(javax.sip.RequestEvent, android.net.sip.SipProfile, java.lang.String, javax.sip.ServerTransaction, java.lang.String, int):javax.sip.ServerTransaction, dex: 
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
    public javax.sip.ServerTransaction sendInviteOk(javax.sip.RequestEvent r1, android.net.sip.SipProfile r2, java.lang.String r3, javax.sip.ServerTransaction r4, java.lang.String r5, int r6) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendInviteOk(javax.sip.RequestEvent, android.net.sip.SipProfile, java.lang.String, javax.sip.ServerTransaction, java.lang.String, int):javax.sip.ServerTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendInviteOk(javax.sip.RequestEvent, android.net.sip.SipProfile, java.lang.String, javax.sip.ServerTransaction, java.lang.String, int):javax.sip.ServerTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendInviteRequestTerminated(javax.sip.message.Request, javax.sip.ServerTransaction):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void sendInviteRequestTerminated(javax.sip.message.Request r1, javax.sip.ServerTransaction r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendInviteRequestTerminated(javax.sip.message.Request, javax.sip.ServerTransaction):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendInviteRequestTerminated(javax.sip.message.Request, javax.sip.ServerTransaction):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendOptions(android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String):javax.sip.ClientTransaction, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public javax.sip.ClientTransaction sendOptions(android.net.sip.SipProfile r1, android.net.sip.SipProfile r2, java.lang.String r3) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendOptions(android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String):javax.sip.ClientTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendOptions(android.net.sip.SipProfile, android.net.sip.SipProfile, java.lang.String):javax.sip.ClientTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendReferNotify(javax.sip.Dialog, java.lang.String):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public void sendReferNotify(javax.sip.Dialog r1, java.lang.String r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendReferNotify(javax.sip.Dialog, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendReferNotify(javax.sip.Dialog, java.lang.String):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendRegister(android.net.sip.SipProfile, java.lang.String, int):javax.sip.ClientTransaction, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public javax.sip.ClientTransaction sendRegister(android.net.sip.SipProfile r1, java.lang.String r2, int r3) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendRegister(android.net.sip.SipProfile, java.lang.String, int):javax.sip.ClientTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendRegister(android.net.sip.SipProfile, java.lang.String, int):javax.sip.ClientTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendReinvite(javax.sip.Dialog, java.lang.String):javax.sip.ClientTransaction, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    public javax.sip.ClientTransaction sendReinvite(javax.sip.Dialog r1, java.lang.String r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.server.sip.SipHelper.sendReinvite(javax.sip.Dialog, java.lang.String):javax.sip.ClientTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendReinvite(javax.sip.Dialog, java.lang.String):javax.sip.ClientTransaction");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendResponse(javax.sip.RequestEvent, int):void, dex: 
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
    public void sendResponse(javax.sip.RequestEvent r1, int r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendResponse(javax.sip.RequestEvent, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendResponse(javax.sip.RequestEvent, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendRinging(javax.sip.RequestEvent, java.lang.String):javax.sip.ServerTransaction, dex: 
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
    public javax.sip.ServerTransaction sendRinging(javax.sip.RequestEvent r1, java.lang.String r2) throws javax.sip.SipException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.sip.SipHelper.sendRinging(javax.sip.RequestEvent, java.lang.String):javax.sip.ServerTransaction, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.sip.SipHelper.sendRinging(javax.sip.RequestEvent, java.lang.String):javax.sip.ServerTransaction");
    }

    private ToHeader createToHeader(SipProfile profile) throws ParseException {
        return createToHeader(profile, null);
    }

    private ContactHeader createContactHeader(SipProfile profile) throws ParseException, SipException {
        return createContactHeader(profile, null, 0);
    }

    private SipURI createSipUri(String username, String transport, ListeningPoint lp) throws ParseException {
        return createSipUri(username, transport, lp.getIPAddress(), lp.getPort());
    }

    public static String getCallId(Transaction transaction) {
        if (transaction != null) {
            return getCallId(transaction.getRequest());
        }
        return "";
    }

    private static String getCallId(Message message) {
        return ((CallIdHeader) message.getHeader("Call-ID")).getCallId();
    }

    private static String getCallId(Dialog dialog) {
        return dialog.getCallId().getCallId();
    }
}
