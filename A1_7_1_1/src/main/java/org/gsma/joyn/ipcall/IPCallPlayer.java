package org.gsma.joyn.ipcall;

import java.util.Set;
import org.gsma.joyn.ipcall.IIPCallPlayer.Stub;

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
public abstract class IPCallPlayer extends Stub {
    private Set<IPCallPlayerListener> listeners;

    public static class Error {
        public static final int INTERNAL_ERROR = 0;
        public static final int NETWORK_FAILURE = 1;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.ipcall.IPCallPlayer.Error.<init>():void, dex: 
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
        private Error() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: org.gsma.joyn.ipcall.IPCallPlayer.Error.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ipcall.IPCallPlayer.Error.<init>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.gsma.joyn.ipcall.IPCallPlayer.<init>():void, dex:  in method: org.gsma.joyn.ipcall.IPCallPlayer.<init>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.gsma.joyn.ipcall.IPCallPlayer.<init>():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public IPCallPlayer() {
        /*
        // Can't load method instructions: Load method exception: null in method: org.gsma.joyn.ipcall.IPCallPlayer.<init>():void, dex:  in method: org.gsma.joyn.ipcall.IPCallPlayer.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ipcall.IPCallPlayer.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ipcall.IPCallPlayer.addEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener):void, dex: 
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
    public void addEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ipcall.IPCallPlayer.addEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ipcall.IPCallPlayer.addEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener):void");
    }

    public abstract void close();

    public abstract AudioCodec getAudioCodec();

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: org.gsma.joyn.ipcall.IPCallPlayer.getEventListeners():java.util.Set<org.gsma.joyn.ipcall.IPCallPlayerListener>, dex:  in method: org.gsma.joyn.ipcall.IPCallPlayer.getEventListeners():java.util.Set<org.gsma.joyn.ipcall.IPCallPlayerListener>, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: org.gsma.joyn.ipcall.IPCallPlayer.getEventListeners():java.util.Set<org.gsma.joyn.ipcall.IPCallPlayerListener>, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public java.util.Set<org.gsma.joyn.ipcall.IPCallPlayerListener> getEventListeners() {
        /*
        // Can't load method instructions: Load method exception: null in method: org.gsma.joyn.ipcall.IPCallPlayer.getEventListeners():java.util.Set<org.gsma.joyn.ipcall.IPCallPlayerListener>, dex:  in method: org.gsma.joyn.ipcall.IPCallPlayer.getEventListeners():java.util.Set<org.gsma.joyn.ipcall.IPCallPlayerListener>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ipcall.IPCallPlayer.getEventListeners():java.util.Set<org.gsma.joyn.ipcall.IPCallPlayerListener>");
    }

    public abstract int getLocalAudioRtpPort();

    public abstract int getLocalVideoRtpPort();

    public abstract AudioCodec[] getSupportedAudioCodecs();

    public abstract VideoCodec[] getSupportedVideoCodecs();

    public abstract VideoCodec getVideoCodec();

    public abstract void open(AudioCodec audioCodec, VideoCodec videoCodec, String str, int i, int i2);

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ipcall.IPCallPlayer.removeAllEventListeners():void, dex: 
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
    public void removeAllEventListeners() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ipcall.IPCallPlayer.removeAllEventListeners():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ipcall.IPCallPlayer.removeAllEventListeners():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ipcall.IPCallPlayer.removeEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener):void, dex: 
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
    public void removeEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: org.gsma.joyn.ipcall.IPCallPlayer.removeEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: org.gsma.joyn.ipcall.IPCallPlayer.removeEventListener(org.gsma.joyn.ipcall.IPCallPlayerListener):void");
    }

    public abstract void start();

    public abstract void stop();
}
