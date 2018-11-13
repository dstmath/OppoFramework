package com.android.location.provider;

import android.location.FusedBatchOptions;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class GmsFusedBatchOptions {
    private FusedBatchOptions mOptions;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static final class BatchFlags {
        public static int CALLBACK_ON_LOCATION_FIX;
        public static int WAKEUP_ON_FIFO_FULL;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.location.provider.GmsFusedBatchOptions.BatchFlags.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.location.provider.GmsFusedBatchOptions.BatchFlags.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.location.provider.GmsFusedBatchOptions.BatchFlags.<clinit>():void");
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public static final class SourceTechnologies {
        public static int BLUETOOTH;
        public static int CELL;
        public static int GNSS;
        public static int SENSORS;
        public static int WIFI;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.location.provider.GmsFusedBatchOptions.SourceTechnologies.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.location.provider.GmsFusedBatchOptions.SourceTechnologies.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.location.provider.GmsFusedBatchOptions.SourceTechnologies.<clinit>():void");
        }
    }

    public GmsFusedBatchOptions() {
        this.mOptions = new FusedBatchOptions();
    }

    public void setMaxPowerAllocationInMW(double value) {
        this.mOptions.setMaxPowerAllocationInMW(value);
    }

    public double getMaxPowerAllocationInMW() {
        return this.mOptions.getMaxPowerAllocationInMW();
    }

    public void setPeriodInNS(long value) {
        this.mOptions.setPeriodInNS(value);
    }

    public long getPeriodInNS() {
        return this.mOptions.getPeriodInNS();
    }

    public void setSmallestDisplacementMeters(float value) {
        this.mOptions.setSmallestDisplacementMeters(value);
    }

    public float getSmallestDisplacementMeters() {
        return this.mOptions.getSmallestDisplacementMeters();
    }

    public void setSourceToUse(int source) {
        this.mOptions.setSourceToUse(source);
    }

    public void resetSourceToUse(int source) {
        this.mOptions.resetSourceToUse(source);
    }

    public boolean isSourceToUseSet(int source) {
        return this.mOptions.isSourceToUseSet(source);
    }

    public int getSourcesToUse() {
        return this.mOptions.getSourcesToUse();
    }

    public void setFlag(int flag) {
        this.mOptions.setFlag(flag);
    }

    public void resetFlag(int flag) {
        this.mOptions.resetFlag(flag);
    }

    public boolean isFlagSet(int flag) {
        return this.mOptions.isFlagSet(flag);
    }

    public int getFlags() {
        return this.mOptions.getFlags();
    }

    public FusedBatchOptions getParcelableOptions() {
        return this.mOptions;
    }
}
