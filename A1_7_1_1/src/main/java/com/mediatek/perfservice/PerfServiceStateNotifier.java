package com.mediatek.perfservice;

import java.util.HashSet;
import java.util.Iterator;

public class PerfServiceStateNotifier {
    /* renamed from: -com-mediatek-perfservice-PerfServiceStateNotifier$ActivityStateSwitchesValues */
    private static final /* synthetic */ int[] f18x78558e6c = null;
    static final String TAG = "PerfServiceStateNotifier";
    IPerfServiceWrapper mPerfService = new PerfServiceWrapper(null);

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
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum ActivityState {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.mediatek.perfservice.PerfServiceStateNotifier.ActivityState.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.mediatek.perfservice.PerfServiceStateNotifier.ActivityState.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.mediatek.perfservice.PerfServiceStateNotifier.ActivityState.<clinit>():void");
        }
    }

    /* renamed from: -getcom-mediatek-perfservice-PerfServiceStateNotifier$ActivityStateSwitchesValues */
    private static /* synthetic */ int[] m49xfd169010() {
        if (f18x78558e6c != null) {
            return f18x78558e6c;
        }
        int[] iArr = new int[ActivityState.values().length];
        try {
            iArr[ActivityState.Destroyed.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[ActivityState.Paused.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[ActivityState.Resumed.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[ActivityState.Stopped.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        f18x78558e6c = iArr;
        return iArr;
    }

    public void notifyActivityState(String packageName, int pid, String className, ActivityState actState) {
        int state;
        switch (m49xfd169010()[actState.ordinal()]) {
            case 1:
                state = 2;
                break;
            case 2:
                state = 0;
                break;
            case 3:
                state = 1;
                break;
            case 4:
                state = 4;
                break;
            default:
                return;
        }
        this.mPerfService.notifyAppState(packageName, className, state, pid);
    }

    public void notifyAppDied(int pid, HashSet<String> packageList) {
        Iterator i = packageList.iterator();
        while (i.hasNext()) {
            this.mPerfService.notifyAppState((String) i.next(), null, 3, pid);
        }
    }
}
