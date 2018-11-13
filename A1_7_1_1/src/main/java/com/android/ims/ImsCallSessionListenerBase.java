package com.android.ims;

import com.android.ims.internal.IImsCallSessionListener.Stub;

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
public abstract class ImsCallSessionListenerBase extends Stub {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.<init>():void, dex: 
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
    public ImsCallSessionListenerBase() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtendFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionConferenceExtendFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtendFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtendFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtendReceived(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionConferenceExtendReceived(com.android.ims.internal.IImsCallSession r1, com.android.ims.internal.IImsCallSession r2, com.android.ims.ImsCallProfile r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtendReceived(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtendReceived(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtended(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionConferenceExtended(com.android.ims.internal.IImsCallSession r1, com.android.ims.internal.IImsCallSession r2, com.android.ims.ImsCallProfile r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtended(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceExtended(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceStateUpdated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsConferenceState):void, dex: 
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
    public void callSessionConferenceStateUpdated(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsConferenceState r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceStateUpdated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsConferenceState):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionConferenceStateUpdated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsConferenceState):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHandover(com.android.ims.internal.IImsCallSession, int, int, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionHandover(com.android.ims.internal.IImsCallSession r1, int r2, int r3, com.android.ims.ImsReasonInfo r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHandover(com.android.ims.internal.IImsCallSession, int, int, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionHandover(com.android.ims.internal.IImsCallSession, int, int, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHandoverFailed(com.android.ims.internal.IImsCallSession, int, int, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionHandoverFailed(com.android.ims.internal.IImsCallSession r1, int r2, int r3, com.android.ims.ImsReasonInfo r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHandoverFailed(com.android.ims.internal.IImsCallSession, int, int, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionHandoverFailed(com.android.ims.internal.IImsCallSession, int, int, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHeld(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionHeld(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsCallProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHeld(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionHeld(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHoldFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionHoldFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHoldFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionHoldFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHoldReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionHoldReceived(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsCallProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionHoldReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionHoldReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionInviteParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession):void, dex: 
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
    public void callSessionInviteParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionInviteParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionInviteParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionInviteParticipantsRequestFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionInviteParticipantsRequestFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionInviteParticipantsRequestFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionInviteParticipantsRequestFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMergeComplete(com.android.ims.internal.IImsCallSession):void, dex: 
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
    public void callSessionMergeComplete(com.android.ims.internal.IImsCallSession r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMergeComplete(com.android.ims.internal.IImsCallSession):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionMergeComplete(com.android.ims.internal.IImsCallSession):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMergeFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionMergeFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMergeFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionMergeFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMergeStarted(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionMergeStarted(com.android.ims.internal.IImsCallSession r1, com.android.ims.internal.IImsCallSession r2, com.android.ims.ImsCallProfile r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMergeStarted(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionMergeStarted(com.android.ims.internal.IImsCallSession, com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMultipartyStateChanged(com.android.ims.internal.IImsCallSession, boolean):void, dex: 
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
    public void callSessionMultipartyStateChanged(com.android.ims.internal.IImsCallSession r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionMultipartyStateChanged(com.android.ims.internal.IImsCallSession, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionMultipartyStateChanged(com.android.ims.internal.IImsCallSession, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionProgressing(com.android.ims.internal.IImsCallSession, com.android.ims.ImsStreamMediaProfile):void, dex: 
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
    public void callSessionProgressing(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsStreamMediaProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionProgressing(com.android.ims.internal.IImsCallSession, com.android.ims.ImsStreamMediaProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionProgressing(com.android.ims.internal.IImsCallSession, com.android.ims.ImsStreamMediaProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionRemoveParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession):void, dex: 
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
    public void callSessionRemoveParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionRemoveParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionRemoveParticipantsRequestDelivered(com.android.ims.internal.IImsCallSession):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionRemoveParticipantsRequestFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionRemoveParticipantsRequestFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionRemoveParticipantsRequestFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionRemoveParticipantsRequestFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionResumeFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionResumeFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionResumeFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionResumeFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionResumeReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionResumeReceived(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsCallProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionResumeReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionResumeReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionResumed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionResumed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsCallProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionResumed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionResumed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionStartFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionStartFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionStartFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionStartFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionStarted(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionStarted(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsCallProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionStarted(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionStarted(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionSuppServiceReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsSuppServiceNotification):void, dex: 
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
    public void callSessionSuppServiceReceived(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsSuppServiceNotification r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionSuppServiceReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsSuppServiceNotification):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionSuppServiceReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsSuppServiceNotification):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionTerminated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionTerminated(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionTerminated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionTerminated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionTtyModeReceived(com.android.ims.internal.IImsCallSession, int):void, dex: 
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
    public void callSessionTtyModeReceived(com.android.ims.internal.IImsCallSession r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionTtyModeReceived(com.android.ims.internal.IImsCallSession, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionTtyModeReceived(com.android.ims.internal.IImsCallSession, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUpdateFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
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
    public void callSessionUpdateFailed(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsReasonInfo r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUpdateFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionUpdateFailed(com.android.ims.internal.IImsCallSession, com.android.ims.ImsReasonInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUpdateReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionUpdateReceived(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsCallProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUpdateReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionUpdateReceived(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUpdated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
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
    public void callSessionUpdated(com.android.ims.internal.IImsCallSession r1, com.android.ims.ImsCallProfile r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUpdated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionUpdated(com.android.ims.internal.IImsCallSession, com.android.ims.ImsCallProfile):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUssdMessageReceived(com.android.ims.internal.IImsCallSession, int, java.lang.String):void, dex: 
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
    public void callSessionUssdMessageReceived(com.android.ims.internal.IImsCallSession r1, int r2, java.lang.String r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.ims.ImsCallSessionListenerBase.callSessionUssdMessageReceived(com.android.ims.internal.IImsCallSession, int, java.lang.String):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsCallSessionListenerBase.callSessionUssdMessageReceived(com.android.ims.internal.IImsCallSession, int, java.lang.String):void");
    }
}
