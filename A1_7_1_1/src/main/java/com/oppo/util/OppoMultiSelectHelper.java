package com.oppo.util;

import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.View;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import java.util.List;

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
@Deprecated
public class OppoMultiSelectHelper implements Callback, AnimationListener, ActivityLifecycleCallbacks {
    private static final long DEFAULT_DURATION_OFFSET = 0;
    private static final int DEFAULT_FINAL_VISIBILITY = -1;
    private static final int DEFAULT_FLOW_LAYOUT = 201917494;
    private static final String TAG_BOTTOM_IN = "BottomIn";
    private static final String TAG_BOTTOM_OUT = "BottomOut";
    private static final String TAG_FADE_IN = "FadeIn";
    private static final String TAG_FADE_OUT = "FadeOut";
    private static final String TAG_RIGHT_IN = "RightIn";
    private static final String TAG_RIGHT_OUT = "RightOut";
    private static List<OppoAnimationHelper> mBottomInList;
    private static List<OppoAnimationHelper> mBottomOutList;
    private static List<OppoAnimationHelper> mFadeInList;
    private static List<OppoAnimationHelper> mFadeOutList;
    private static List<OppoAnimationHelper> mLeftInList;
    private static List<OppoAnimationHelper> mLeftOutList;
    private static List<OppoAnimationHelper> mRightInList;
    private static List<OppoAnimationHelper> mRightOutList;
    private boolean mActionBarAnimating;
    private boolean mActionBarShow;
    private ActionMode mActionMode;
    private Activity mActivity;
    private View mBottomExtra;
    private OppoAnimationHelper mBottomIn;
    private View mBottomMenu;
    private OppoAnimationHelper mBottomOut;
    private Callback mCallback;
    private boolean mClearing;
    private OppoAnimationHelper mExtraIn;
    private OppoAnimationHelper mExtraOut;
    private boolean mFinishing;
    private OppoAnimationHelper mFlowIn;
    private View mFlowMenu;
    private OppoAnimationHelper mFlowOut;
    private OnAnimationsEndListener mListener;
    private int mMajorVisibility;
    private List<OppoAnimationHelper> mRunningList;
    private boolean mStarting;
    protected final Class<?> mTagClass;

    /* renamed from: com.oppo.util.OppoMultiSelectHelper$1 */
    class AnonymousClass1 extends AnimatorListenerAdapter {
        final /* synthetic */ OppoMultiSelectHelper this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.1.<init>(com.oppo.util.OppoMultiSelectHelper):void, dex: 
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
        AnonymousClass1(com.oppo.util.OppoMultiSelectHelper r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.1.<init>(com.oppo.util.OppoMultiSelectHelper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.1.<init>(com.oppo.util.OppoMultiSelectHelper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.1.onAnimationEnd(android.animation.Animator):void, dex: 
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
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.1.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.1.onAnimationEnd(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.1.onAnimationStart(android.animation.Animator):void, dex: 
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
        public void onAnimationStart(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.1.onAnimationStart(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.1.onAnimationStart(android.animation.Animator):void");
        }
    }

    /* renamed from: com.oppo.util.OppoMultiSelectHelper$2 */
    class AnonymousClass2 extends AnimatorListenerAdapter {
        final /* synthetic */ OppoMultiSelectHelper this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.2.<init>(com.oppo.util.OppoMultiSelectHelper):void, dex: 
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
        AnonymousClass2(com.oppo.util.OppoMultiSelectHelper r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.2.<init>(com.oppo.util.OppoMultiSelectHelper):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.2.<init>(com.oppo.util.OppoMultiSelectHelper):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.2.onAnimationEnd(android.animation.Animator):void, dex: 
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
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.2.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.2.onAnimationEnd(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.2.onAnimationStart(android.animation.Animator):void, dex: 
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
        public void onAnimationStart(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.2.onAnimationStart(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.2.onAnimationStart(android.animation.Animator):void");
        }
    }

    public interface OnAnimationsEndListener {
        void onAnimationsEnd();
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.-get0(com.oppo.util.OppoMultiSelectHelper):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get0 */
    static /* synthetic */ boolean m302-get0(com.oppo.util.OppoMultiSelectHelper r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.-get0(com.oppo.util.OppoMultiSelectHelper):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.-get0(com.oppo.util.OppoMultiSelectHelper):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.-get1(com.oppo.util.OppoMultiSelectHelper):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -get1 */
    static /* synthetic */ boolean m303-get1(com.oppo.util.OppoMultiSelectHelper r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.-get1(com.oppo.util.OppoMultiSelectHelper):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.-get1(com.oppo.util.OppoMultiSelectHelper):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.oppo.util.OppoMultiSelectHelper.-set0(com.oppo.util.OppoMultiSelectHelper, boolean):boolean, dex: 
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
    /* renamed from: -set0 */
    static /* synthetic */ boolean m304-set0(com.oppo.util.OppoMultiSelectHelper r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.oppo.util.OppoMultiSelectHelper.-set0(com.oppo.util.OppoMultiSelectHelper, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.-set0(com.oppo.util.OppoMultiSelectHelper, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.-wrap0(com.oppo.util.OppoMultiSelectHelper):void, dex: 
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
    /* renamed from: -wrap0 */
    static /* synthetic */ void m305-wrap0(com.oppo.util.OppoMultiSelectHelper r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.-wrap0(com.oppo.util.OppoMultiSelectHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.-wrap0(com.oppo.util.OppoMultiSelectHelper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.<clinit>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.<init>(android.app.Activity, android.view.View, int, long):void, dex: 
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
    public OppoMultiSelectHelper(android.app.Activity r1, android.view.View r2, int r3, long r4) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.<init>(android.app.Activity, android.view.View, int, long):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.<init>(android.app.Activity, android.view.View, int, long):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.clearAnimations():void, dex: 
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
    private void clearAnimations() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.clearAnimations():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.clearAnimations():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.createAnimation(android.content.Context, android.view.View, int, boolean, boolean, android.view.animation.Animation, boolean, long, java.lang.String):com.oppo.util.OppoAnimationHelper, dex: 
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
    private static com.oppo.util.OppoAnimationHelper createAnimation(android.content.Context r1, android.view.View r2, int r3, boolean r4, boolean r5, android.view.animation.Animation r6, boolean r7, long r8, java.lang.String r10) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.createAnimation(android.content.Context, android.view.View, int, boolean, boolean, android.view.animation.Animation, boolean, long, java.lang.String):com.oppo.util.OppoAnimationHelper, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.createAnimation(android.content.Context, android.view.View, int, boolean, boolean, android.view.animation.Animation, boolean, long, java.lang.String):com.oppo.util.OppoAnimationHelper");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.findHelper(android.view.View, java.util.List):com.oppo.util.OppoAnimationHelper, dex: 
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
    private static com.oppo.util.OppoAnimationHelper findHelper(android.view.View r1, java.util.List<com.oppo.util.OppoAnimationHelper> r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.findHelper(android.view.View, java.util.List):com.oppo.util.OppoAnimationHelper, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.findHelper(android.view.View, java.util.List):com.oppo.util.OppoAnimationHelper");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.getActionBarShow():boolean, dex: 
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
    private boolean getActionBarShow() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.getActionBarShow():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.getActionBarShow():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.getBottomInfo():void, dex: 
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
    private void getBottomInfo() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.getBottomInfo():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.getBottomInfo():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.initActionBar():void, dex: 
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
    private void initActionBar() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.initActionBar():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.initActionBar():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.initActivity(android.app.Activity):android.app.Activity, dex: 
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
    private android.app.Activity initActivity(android.app.Activity r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.initActivity(android.app.Activity):android.app.Activity, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.initActivity(android.app.Activity):android.app.Activity");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.isAnimationMatch(android.view.animation.Animation):boolean, dex: 
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
    private boolean isAnimationMatch(android.view.animation.Animation r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.isAnimationMatch(android.view.animation.Animation):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.isAnimationMatch(android.view.animation.Animation):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.makeTag(android.view.View, boolean, java.lang.String):java.lang.String, dex: 
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
    private static java.lang.String makeTag(android.view.View r1, boolean r2, java.lang.String r3) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.makeTag(android.view.View, boolean, java.lang.String):java.lang.String, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.makeTag(android.view.View, boolean, java.lang.String):java.lang.String");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.needBottomAnimation():boolean, dex: 
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
    private boolean needBottomAnimation() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.needBottomAnimation():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.needBottomAnimation():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.needCancelAnimation(com.oppo.util.OppoAnimationHelper):boolean, dex: 
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
    private boolean needCancelAnimation(com.oppo.util.OppoAnimationHelper r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.needCancelAnimation(com.oppo.util.OppoAnimationHelper):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.needCancelAnimation(com.oppo.util.OppoAnimationHelper):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.needClearAnimations():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    private boolean needClearAnimations() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.needClearAnimations():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.needClearAnimations():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onFinishActionMode():void, dex: 
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
    private void onFinishActionMode() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onFinishActionMode():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onFinishActionMode():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onStartActionMode():android.view.ActionMode, dex: 
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
    private android.view.ActionMode onStartActionMode() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onStartActionMode():android.view.ActionMode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onStartActionMode():android.view.ActionMode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.showBottomExtra(android.view.View, boolean):void, dex: 
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
    private void showBottomExtra(android.view.View r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.showBottomExtra(android.view.View, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.showBottomExtra(android.view.View, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.showMajorGroup(android.view.View, boolean):void, dex: 
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
    private void showMajorGroup(android.view.View r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.showMajorGroup(android.view.View, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.showMajorGroup(android.view.View, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoMultiSelectHelper.startBottomAnimation(com.oppo.util.OppoAnimationHelper):void, dex:  in method: com.oppo.util.OppoMultiSelectHelper.startBottomAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoMultiSelectHelper.startBottomAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:72)
        	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void startBottomAnimation(com.oppo.util.OppoAnimationHelper r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoMultiSelectHelper.startBottomAnimation(com.oppo.util.OppoAnimationHelper):void, dex:  in method: com.oppo.util.OppoMultiSelectHelper.startBottomAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.startBottomAnimation(com.oppo.util.OppoAnimationHelper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.tryClearAnimations():void, dex: 
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
    private void tryClearAnimations() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.oppo.util.OppoMultiSelectHelper.tryClearAnimations():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.tryClearAnimations():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.updateMenuBar(android.view.View, boolean):void, dex: 
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
    private void updateMenuBar(android.view.View r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.updateMenuBar(android.view.View, boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.updateMenuBar(android.view.View, boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.doClearAnimations():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void doClearAnimations() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.doClearAnimations():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.doClearAnimations():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.finishActionMode():void, dex: 
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
    public void finishActionMode() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.finishActionMode():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.finishActionMode():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.getActionMode():android.view.ActionMode, dex: 
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
    public android.view.ActionMode getActionMode() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.getActionMode():android.view.ActionMode, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.getActionMode():android.view.ActionMode");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.isActionBarShow():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean isActionBarShow() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.isActionBarShow():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.isActionBarShow():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.isAnimationsRunning():boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public boolean isAnimationsRunning() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.oppo.util.OppoMultiSelectHelper.isAnimationsRunning():boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.isAnimationsRunning():boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onActionItemClicked(android.view.ActionMode, android.view.MenuItem):boolean, dex: 
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
    public boolean onActionItemClicked(android.view.ActionMode r1, android.view.MenuItem r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onActionItemClicked(android.view.ActionMode, android.view.MenuItem):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActionItemClicked(android.view.ActionMode, android.view.MenuItem):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityCreated(android.app.Activity, android.os.Bundle):void, dex: 
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
    public void onActivityCreated(android.app.Activity r1, android.os.Bundle r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityCreated(android.app.Activity, android.os.Bundle):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActivityCreated(android.app.Activity, android.os.Bundle):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onActivityDestroyed(android.app.Activity):void, dex: 
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
    public void onActivityDestroyed(android.app.Activity r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onActivityDestroyed(android.app.Activity):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActivityDestroyed(android.app.Activity):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onActivityPaused(android.app.Activity):void, dex: 
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
    public void onActivityPaused(android.app.Activity r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onActivityPaused(android.app.Activity):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActivityPaused(android.app.Activity):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityResumed(android.app.Activity):void, dex: 
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
    public void onActivityResumed(android.app.Activity r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityResumed(android.app.Activity):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActivityResumed(android.app.Activity):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivitySaveInstanceState(android.app.Activity, android.os.Bundle):void, dex: 
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
    public void onActivitySaveInstanceState(android.app.Activity r1, android.os.Bundle r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivitySaveInstanceState(android.app.Activity, android.os.Bundle):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActivitySaveInstanceState(android.app.Activity, android.os.Bundle):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityStarted(android.app.Activity):void, dex: 
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
    public void onActivityStarted(android.app.Activity r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityStarted(android.app.Activity):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActivityStarted(android.app.Activity):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityStopped(android.app.Activity):void, dex: 
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
    public void onActivityStopped(android.app.Activity r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onActivityStopped(android.app.Activity):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onActivityStopped(android.app.Activity):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onAnimationEnd(android.view.animation.Animation):void, dex: 
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
    public void onAnimationEnd(android.view.animation.Animation r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onAnimationEnd(android.view.animation.Animation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onAnimationEnd(android.view.animation.Animation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onAnimationRepeat(android.view.animation.Animation):void, dex: 
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
    public void onAnimationRepeat(android.view.animation.Animation r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onAnimationRepeat(android.view.animation.Animation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onAnimationRepeat(android.view.animation.Animation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onAnimationStart(android.view.animation.Animation):void, dex: 
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
    public void onAnimationStart(android.view.animation.Animation r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.onAnimationStart(android.view.animation.Animation):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onAnimationStart(android.view.animation.Animation):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onCreateActionMode(android.view.ActionMode, android.view.Menu):boolean, dex: 
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
    public boolean onCreateActionMode(android.view.ActionMode r1, android.view.Menu r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onCreateActionMode(android.view.ActionMode, android.view.Menu):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onCreateActionMode(android.view.ActionMode, android.view.Menu):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onDestroyActionMode(android.view.ActionMode):void, dex: 
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
    public void onDestroyActionMode(android.view.ActionMode r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onDestroyActionMode(android.view.ActionMode):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onDestroyActionMode(android.view.ActionMode):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onPrepareActionMode(android.view.ActionMode, android.view.Menu):boolean, dex: 
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
    public boolean onPrepareActionMode(android.view.ActionMode r1, android.view.Menu r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.onPrepareActionMode(android.view.ActionMode, android.view.Menu):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.onPrepareActionMode(android.view.ActionMode, android.view.Menu):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.recycle():void, dex: 
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
    public void recycle() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.oppo.util.OppoMultiSelectHelper.recycle():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.recycle():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.recycleAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
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
    public void recycleAnimation(com.oppo.util.OppoAnimationHelper r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.oppo.util.OppoMultiSelectHelper.recycleAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.recycleAnimation(com.oppo.util.OppoAnimationHelper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.oppo.util.OppoMultiSelectHelper.setActionBarShow(boolean):void, dex: 
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
    public void setActionBarShow(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.oppo.util.OppoMultiSelectHelper.setActionBarShow(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.setActionBarShow(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.setExtraBottomView(android.view.View):void, dex: 
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
    public void setExtraBottomView(android.view.View r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.setExtraBottomView(android.view.View):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.setExtraBottomView(android.view.View):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.setOnAnimationsEndListener(com.oppo.util.OppoMultiSelectHelper$OnAnimationsEndListener):void, dex: 
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
    public void setOnAnimationsEndListener(com.oppo.util.OppoMultiSelectHelper.OnAnimationsEndListener r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.setOnAnimationsEndListener(com.oppo.util.OppoMultiSelectHelper$OnAnimationsEndListener):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.setOnAnimationsEndListener(com.oppo.util.OppoMultiSelectHelper$OnAnimationsEndListener):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.startActionMode(android.view.ActionMode$Callback):void, dex: 
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
    public void startActionMode(android.view.ActionMode.Callback r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.oppo.util.OppoMultiSelectHelper.startActionMode(android.view.ActionMode$Callback):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.startActionMode(android.view.ActionMode$Callback):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoMultiSelectHelper.startLeftAnimation(com.oppo.util.OppoAnimationHelper):void, dex:  in method: com.oppo.util.OppoMultiSelectHelper.startLeftAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoMultiSelectHelper.startLeftAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
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
    public void startLeftAnimation(com.oppo.util.OppoAnimationHelper r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoMultiSelectHelper.startLeftAnimation(com.oppo.util.OppoAnimationHelper):void, dex:  in method: com.oppo.util.OppoMultiSelectHelper.startLeftAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.startLeftAnimation(com.oppo.util.OppoAnimationHelper):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.oppo.util.OppoMultiSelectHelper.startRightAnimation(com.oppo.util.OppoAnimationHelper):void, dex:  in method: com.oppo.util.OppoMultiSelectHelper.startRightAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.oppo.util.OppoMultiSelectHelper.startRightAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
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
    public void startRightAnimation(com.oppo.util.OppoAnimationHelper r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.oppo.util.OppoMultiSelectHelper.startRightAnimation(com.oppo.util.OppoAnimationHelper):void, dex:  in method: com.oppo.util.OppoMultiSelectHelper.startRightAnimation(com.oppo.util.OppoAnimationHelper):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.oppo.util.OppoMultiSelectHelper.startRightAnimation(com.oppo.util.OppoAnimationHelper):void");
    }

    public OppoMultiSelectHelper(Activity activity, View view) {
        this(activity, view, (int) DEFAULT_FLOW_LAYOUT);
    }

    public OppoMultiSelectHelper(Activity activity, View view, int layout) {
        this(activity, view, layout, DEFAULT_DURATION_OFFSET);
    }

    public OppoMultiSelectHelper(Activity activity, View view, long offset) {
        this(activity, view, DEFAULT_FLOW_LAYOUT, offset);
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, long offset) {
        return makeBottomIn(context, view, visibility, false, offset, makeTag(view, false, TAG_BOTTOM_IN));
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view, long offset) {
        return makeBottomIn(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility) {
        return makeBottomIn(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeBottomIn(Context context, View view) {
        return makeBottomIn(context, view, -1);
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, long offset) {
        return makeBottomOut(context, view, visibility, false, offset, makeTag(view, false, TAG_BOTTOM_OUT));
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view, long offset) {
        return makeBottomOut(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility) {
        return makeBottomOut(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeBottomOut(Context context, View view) {
        return makeBottomOut(context, view, -1);
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view, int visibility, long offset) {
        return makeLeftIn(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_IN));
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view, long offset) {
        return makeLeftIn(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view, int visibility) {
        return makeLeftIn(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeLeftIn(Context context, View view) {
        return makeLeftIn(context, view, -1);
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view, int visibility, long offset) {
        return makeLeftOut(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_OUT));
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view, long offset) {
        return makeLeftOut(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view, int visibility) {
        return makeLeftOut(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeLeftOut(Context context, View view) {
        return makeLeftOut(context, view, -1);
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view, int visibility, long offset) {
        return makeRightIn(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_IN));
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view, long offset) {
        return makeRightIn(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view, int visibility) {
        return makeRightIn(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeRightIn(Context context, View view) {
        return makeRightIn(context, view, -1);
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view, int visibility, long offset) {
        return makeRightOut(context, view, visibility, false, offset, makeTag(view, false, TAG_RIGHT_OUT));
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view, long offset) {
        return makeRightOut(context, view, -1, offset);
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view, int visibility) {
        return makeRightOut(context, view, visibility, DEFAULT_DURATION_OFFSET);
    }

    public static OppoAnimationHelper makeRightOut(Context context, View view) {
        return makeRightOut(context, view, -1);
    }

    public static OppoAnimationHelper makeFadeIn(Context context, View view) {
        return makeFadeIn(context, view, -1, true, DEFAULT_DURATION_OFFSET, makeTag(view, false, TAG_FADE_IN));
    }

    public static OppoAnimationHelper makeFadeOut(Context context, View view) {
        return makeFadeOut(context, view, -1, false, DEFAULT_DURATION_OFFSET, makeTag(view, false, TAG_FADE_OUT));
    }

    public static OppoAnimationHelper makeItemUp(Context context, View view) {
        return null;
    }

    public static OppoAnimationHelper makeItemLeft(Context context, View view) {
        return null;
    }

    private static View getBottomView(View view, boolean forceMenuBar) {
        return view;
    }

    private static OppoAnimationHelper makeLeftIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mLeftInList);
        if (helper != null) {
            return helper;
        }
        helper = createLeftIn(context, view, visibility, forceMenuBar, offset, tag);
        mLeftInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeLeftOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mLeftOutList);
        if (helper != null) {
            return helper;
        }
        helper = createLeftOut(context, view, visibility, forceMenuBar, offset, tag);
        mLeftOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeRightIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mRightInList);
        if (helper != null) {
            return helper;
        }
        helper = createRightIn(context, view, visibility, forceMenuBar, offset, tag);
        mRightInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeRightOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mRightOutList);
        if (helper != null) {
            return helper;
        }
        helper = createRightOut(context, view, visibility, forceMenuBar, offset, tag);
        mRightOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, boolean forceMenuBar, long offset) {
        return makeBottomIn(context, view, visibility, forceMenuBar, offset, makeTag(view, forceMenuBar, TAG_BOTTOM_IN));
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, boolean forceMenuBar, String tag) {
        return makeBottomIn(context, view, visibility, forceMenuBar, DEFAULT_DURATION_OFFSET, tag);
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, long offset, String tag) {
        return makeBottomIn(context, view, visibility, false, offset, tag);
    }

    private static OppoAnimationHelper makeBottomIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mBottomInList);
        if (helper != null) {
            return helper;
        }
        helper = createBottomIn(context, view, visibility, forceMenuBar, offset, tag);
        mBottomInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, boolean forceMenuBar, long offset) {
        return makeBottomOut(context, view, visibility, forceMenuBar, offset, makeTag(view, forceMenuBar, TAG_BOTTOM_OUT));
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, boolean forceMenuBar, String tag) {
        return makeBottomOut(context, view, visibility, forceMenuBar, DEFAULT_DURATION_OFFSET, tag);
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, long offset, String tag) {
        return makeBottomOut(context, view, visibility, false, offset, tag);
    }

    private static OppoAnimationHelper makeBottomOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mBottomOutList);
        if (helper != null) {
            return helper;
        }
        helper = createBottomOut(context, view, visibility, forceMenuBar, offset, tag);
        mBottomOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeFadeIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mFadeInList);
        if (helper != null) {
            return helper;
        }
        helper = createFadeIn(context, view, visibility, forceMenuBar, offset, tag);
        mFadeInList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper makeFadeOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        OppoAnimationHelper helper = findHelper(view, mFadeOutList);
        if (helper != null) {
            return helper;
        }
        helper = createFadeOut(context, view, visibility, forceMenuBar, offset, tag);
        mFadeOutList.add(helper);
        return helper;
    }

    private static OppoAnimationHelper createAnimation(Context context, View view, int visibility, boolean forceMenuBar, boolean fillAfter, int res, boolean in, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, fillAfter, AnimationUtils.loadAnimation(context, res), in, offset, tag);
    }

    private static OppoAnimationHelper createBottomIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, true, 201982976, true, offset, tag);
    }

    private static OppoAnimationHelper createBottomOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982977, false, offset, tag);
    }

    private static OppoAnimationHelper createLeftIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982988, true, offset, tag);
    }

    private static OppoAnimationHelper createLeftOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982989, false, offset, tag);
    }

    private static OppoAnimationHelper createRightIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982978, true, offset, tag);
    }

    private static OppoAnimationHelper createRightOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982979, false, offset, tag);
    }

    private static OppoAnimationHelper createFadeIn(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982980, true, offset, tag);
    }

    private static OppoAnimationHelper createFadeOut(Context context, View view, int visibility, boolean forceMenuBar, long offset, String tag) {
        return createAnimation(context, view, visibility, forceMenuBar, false, 201982981, false, offset, tag);
    }

    private View createFlowMenu(int layout) {
        return null;
    }

    private View initBottomMenu(View view) {
        return view;
    }

    private boolean isMoreGroupExpanded() {
        return false;
    }

    private int getBottomInVisibility() {
        return 0;
    }

    private int getBottomOutVisibility() {
        return 8;
    }

    private int getFlowInVisibility() {
        return 0;
    }

    private int getFlowOutVisibility() {
        return 8;
    }
}
