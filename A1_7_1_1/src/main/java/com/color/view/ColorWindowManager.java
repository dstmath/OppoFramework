package com.color.view;

import android.os.RemoteException;
import android.view.IOppoWindowManagerImpl;
import android.view.WindowManagerGlobal;

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
public final class ColorWindowManager {
    public static final int NAVIGATION_BAR_HIDE_AUTO = 2;
    public static final int NAVIGATION_BAR_HIDE_MANUAL = 1;
    public static final int NAVIGATION_BAR_HIDE_PERMANENT = 3;
    public static final int NAVIGATION_BAR_SHOW_TRANSIENT = 0;
    public static final String TAG = "ColorWindowManager";

    public static class LayoutParams extends android.view.WindowManager.LayoutParams {
        public static final int COLOROS_FIRST_SYSTEM_WINDOW = 300;
        private static final int MAX_FORCE_FULLSCREEN_OFFSET = 9;
        public static final int TYPE_DRAG_SCREEN_BACKGROUND = 2301;
        public static final int TYPE_DRAG_SCREEN_FOREGROUND = 2302;
        public static final int TYPE_POWERVIEW = 2300;
        public static final int TYPE_SYSTEM_LONGSHOT = 2303;
        public static final int TYPE_SYSTEM_LONGSHOT_EDIT = 2307;
        public static final int TYPE_SYSTEM_LONGSHOT_GUIDE = 2306;
        public static final int TYPE_SYSTEM_LONGSHOT_MASK = 2308;
        public static final int TYPE_SYSTEM_LONGSHOT_SCROLL = 2304;
        public static final int TYPE_SYSTEM_LONGSHOT_TOAST = 2305;
        public static final int TYPE_SYSTEM_LONGSHOT_VIEW = 2309;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.LayoutParams.<init>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public LayoutParams() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.LayoutParams.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.view.ColorWindowManager.LayoutParams.<init>():void");
        }

        public static boolean isForceFullScreen(int type) {
            int offset = (type - 2000) - 300;
            if (offset < 0 || offset > 9) {
                return false;
            }
            return true;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.<init>():void, dex: 
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
    public ColorWindowManager() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.view.ColorWindowManager.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.getFocusedWindowFrame(android.graphics.Rect):void, dex: 
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
    @java.lang.Deprecated
    public static void getFocusedWindowFrame(android.graphics.Rect r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.getFocusedWindowFrame(android.graphics.Rect):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.view.ColorWindowManager.getFocusedWindowFrame(android.graphics.Rect):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo):void, dex: 
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
    @java.lang.Deprecated
    public static void getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.view.ColorWindowManager.getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.view.ColorWindowManager.getLongshotViewInfo(com.color.screenshot.ColorLongshotViewInfo):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.view.ColorWindowManager.setNoMoveAnimation(android.view.WindowManager$LayoutParams, boolean):void, dex:  in method: com.color.view.ColorWindowManager.setNoMoveAnimation(android.view.WindowManager$LayoutParams, boolean):void, dex: 
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
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.view.ColorWindowManager.setNoMoveAnimation(android.view.WindowManager$LayoutParams, boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 9 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 10 more
        */
    public static void setNoMoveAnimation(android.view.WindowManager.LayoutParams r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.view.ColorWindowManager.setNoMoveAnimation(android.view.WindowManager$LayoutParams, boolean):void, dex:  in method: com.color.view.ColorWindowManager.setNoMoveAnimation(android.view.WindowManager$LayoutParams, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.view.ColorWindowManager.setNoMoveAnimation(android.view.WindowManager$LayoutParams, boolean):void");
    }

    @Deprecated
    public static boolean isInputShow() {
        boolean result = false;
        try {
            return new IOppoWindowManagerImpl().isInputShow();
        } catch (RemoteException e) {
            return result;
        }
    }

    @Deprecated
    public static int getLongshotSurfaceLayer() {
        int layer = 0;
        try {
            return new IOppoWindowManagerImpl().getLongshotSurfaceLayer();
        } catch (RemoteException e) {
            return layer;
        }
    }

    @Deprecated
    public static int getLongshotSurfaceLayerByType(int type) {
        int layer = 0;
        try {
            return new IOppoWindowManagerImpl().getLongshotSurfaceLayerByType(type);
        } catch (RemoteException e) {
            return layer;
        }
    }

    @Deprecated
    public static boolean isInMultiWindowMode() {
        int dockSide = -1;
        try {
            dockSide = WindowManagerGlobal.getWindowManagerService().getDockedStackSide();
        } catch (RemoteException e) {
        }
        return -1 != dockSide;
    }
}
