package com.android.ims;

import com.android.ims.internal.IImsExternalCallStateListener.Stub;
import com.android.ims.internal.IImsMultiEndpoint;

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
public class ImsMultiEndpoint {
    private static final boolean DBG = true;
    private static final String TAG = "ImsMultiEndpoint";
    private final IImsMultiEndpoint mImsMultiendpoint;

    private class ImsExternalCallStateListenerProxy extends Stub {
        private ImsExternalCallStateListener mListener;
        final /* synthetic */ ImsMultiEndpoint this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.<init>(com.android.ims.ImsMultiEndpoint, com.android.ims.ImsExternalCallStateListener):void, dex:  in method: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.<init>(com.android.ims.ImsMultiEndpoint, com.android.ims.ImsExternalCallStateListener):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.<init>(com.android.ims.ImsMultiEndpoint, com.android.ims.ImsExternalCallStateListener):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public ImsExternalCallStateListenerProxy(com.android.ims.ImsMultiEndpoint r1, com.android.ims.ImsExternalCallStateListener r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.<init>(com.android.ims.ImsMultiEndpoint, com.android.ims.ImsExternalCallStateListener):void, dex:  in method: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.<init>(com.android.ims.ImsMultiEndpoint, com.android.ims.ImsExternalCallStateListener):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.<init>(com.android.ims.ImsMultiEndpoint, com.android.ims.ImsExternalCallStateListener):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.onImsExternalCallStateUpdate(java.util.List):void, dex: 
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
        public void onImsExternalCallStateUpdate(java.util.List<com.android.ims.ImsExternalCallState> r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.onImsExternalCallStateUpdate(java.util.List):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsMultiEndpoint.ImsExternalCallStateListenerProxy.onImsExternalCallStateUpdate(java.util.List):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.ims.ImsMultiEndpoint.<init>(com.android.ims.internal.IImsMultiEndpoint):void, dex: 
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
    public ImsMultiEndpoint(com.android.ims.internal.IImsMultiEndpoint r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.ims.ImsMultiEndpoint.<init>(com.android.ims.internal.IImsMultiEndpoint):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsMultiEndpoint.<init>(com.android.ims.internal.IImsMultiEndpoint):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.ims.ImsMultiEndpoint.setExternalCallStateListener(com.android.ims.ImsExternalCallStateListener):void, dex: 
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
    public void setExternalCallStateListener(com.android.ims.ImsExternalCallStateListener r1) throws com.android.ims.ImsException {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.ims.ImsMultiEndpoint.setExternalCallStateListener(com.android.ims.ImsExternalCallStateListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.ims.ImsMultiEndpoint.setExternalCallStateListener(com.android.ims.ImsExternalCallStateListener):void");
    }
}
