package com.android.internal.os;

import android.os.BatteryStats;

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
public class ScreenPowerCalculator extends PowerCalculator {
    private static final boolean DEBUG = false;
    private static final boolean DEBUG_OPPO = false;
    private static final String TAG = "ScreenPowerCalculator";
    private static final String TAG_OPPO = "OppoBatteryStats_ScreenPowerCalculator";
    private final double[] mPowerScreenFull;
    private final double mPowerScreenOn;
    private BatteryStats mStats;
    private long mTotalForgActivieyTime;
    private long mTotalScreenTime;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e7 in method: com.android.internal.os.ScreenPowerCalculator.<init>(com.android.internal.os.PowerProfile, android.os.BatteryStats):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e7
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public ScreenPowerCalculator(com.android.internal.os.PowerProfile r1, android.os.BatteryStats r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e7 in method: com.android.internal.os.ScreenPowerCalculator.<init>(com.android.internal.os.PowerProfile, android.os.BatteryStats):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ScreenPowerCalculator.<init>(com.android.internal.os.PowerProfile, android.os.BatteryStats):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ea in method: com.android.internal.os.ScreenPowerCalculator.systemUidApkScreenPower(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, int):void, dex: 
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
    private void systemUidApkScreenPower(com.android.internal.os.BatterySipper r1, android.os.BatteryStats.Uid r2, long r3, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ea in method: com.android.internal.os.ScreenPowerCalculator.systemUidApkScreenPower(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ScreenPowerCalculator.systemUidApkScreenPower(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.os.ScreenPowerCalculator.calculateApp(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, long, int):void, dex:  in method: com.android.internal.os.ScreenPowerCalculator.calculateApp(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, long, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.os.ScreenPowerCalculator.calculateApp(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, long, int):void, dex: 
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
    public void calculateApp(com.android.internal.os.BatterySipper r1, android.os.BatteryStats.Uid r2, long r3, long r5, int r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.os.ScreenPowerCalculator.calculateApp(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, long, int):void, dex:  in method: com.android.internal.os.ScreenPowerCalculator.calculateApp(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, long, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ScreenPowerCalculator.calculateApp(com.android.internal.os.BatterySipper, android.os.BatteryStats$Uid, long, long, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.ScreenPowerCalculator.calculateRemaining(com.android.internal.os.BatterySipper, android.os.BatteryStats, long, long, int):void, dex: 
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
    public void calculateRemaining(com.android.internal.os.BatterySipper r1, android.os.BatteryStats r2, long r3, long r5, int r7) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.ScreenPowerCalculator.calculateRemaining(com.android.internal.os.BatterySipper, android.os.BatteryStats, long, long, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ScreenPowerCalculator.calculateRemaining(com.android.internal.os.BatterySipper, android.os.BatteryStats, long, long, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.internal.os.ScreenPowerCalculator.reset():void, dex:  in method: com.android.internal.os.ScreenPowerCalculator.reset():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.internal.os.ScreenPowerCalculator.reset():void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void reset() {
        /*
        // Can't load method instructions: Load method exception: null in method: com.android.internal.os.ScreenPowerCalculator.reset():void, dex:  in method: com.android.internal.os.ScreenPowerCalculator.reset():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ScreenPowerCalculator.reset():void");
    }
}
