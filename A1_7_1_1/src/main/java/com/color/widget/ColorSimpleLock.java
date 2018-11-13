package com.color.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

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
public class ColorSimpleLock extends View {
    private final int ADD_ANIMATION;
    private final int CLEAR_ALL_ANIMATION;
    private final int DELETE_ANIMATION;
    private final int DRAW_ALL_ANIMATION;
    private final int FAILED_ANIMATION;
    private final int MORPHING_CIRCLE_TO_LINE_TIME;
    private final int MORPHING_LINE_TO_CIRCLE_TIME;
    private final int TRANSLATION_X_TIME;
    private int animationMode;
    private boolean ctl_lastDraw;
    private boolean fail_lastDraw;
    private boolean isFingerprintMode;
    private boolean ltc_lastDraw;
    private ValueAnimator mAddAnimator;
    private Drawable mCircleDrawable;
    private int mCirclePadding;
    private int mCirclesNumber;
    private int mCirclesWidth;
    private int mCodeImageStart;
    public int mCodeNumber;
    private int mContentWidth;
    private ValueAnimator mDeleteAnimator;
    private boolean mDrawFailedAnimation;
    private Drawable mDrawable;
    private int mDrawableHeight;
    private int mDrawableWidth;
    private Animator mFailedAnimator;
    private Drawable mLineDrawable;
    private float mScaleX;
    private float mScaleY;
    private float mTransitionX;
    private final int mTranslationX_far;
    private final int mTranslationX_near;

    /* renamed from: com.color.widget.ColorSimpleLock$1 */
    class AnonymousClass1 implements AnimatorUpdateListener {
        final /* synthetic */ ColorSimpleLock this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.1.<init>(com.color.widget.ColorSimpleLock):void, dex: 
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
        AnonymousClass1(com.color.widget.ColorSimpleLock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.1.<init>(com.color.widget.ColorSimpleLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.1.<init>(com.color.widget.ColorSimpleLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.1.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.1.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorSimpleLock$2 */
    class AnonymousClass2 implements AnimatorListener {
        final /* synthetic */ ColorSimpleLock this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.2.<init>(com.color.widget.ColorSimpleLock):void, dex: 
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
        AnonymousClass2(com.color.widget.ColorSimpleLock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.2.<init>(com.color.widget.ColorSimpleLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.2.<init>(com.color.widget.ColorSimpleLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.2.onAnimationCancel(android.animation.Animator):void, dex: 
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
        public void onAnimationCancel(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.2.onAnimationCancel(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.2.onAnimationCancel(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.2.onAnimationEnd(android.animation.Animator):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.2.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.2.onAnimationEnd(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.2.onAnimationRepeat(android.animation.Animator):void, dex: 
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
        public void onAnimationRepeat(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.2.onAnimationRepeat(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.2.onAnimationRepeat(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.2.onAnimationStart(android.animation.Animator):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.2.onAnimationStart(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.2.onAnimationStart(android.animation.Animator):void");
        }
    }

    /* renamed from: com.color.widget.ColorSimpleLock$3 */
    class AnonymousClass3 implements AnimatorUpdateListener {
        final /* synthetic */ ColorSimpleLock this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.3.<init>(com.color.widget.ColorSimpleLock):void, dex: 
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
        AnonymousClass3(com.color.widget.ColorSimpleLock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.3.<init>(com.color.widget.ColorSimpleLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.3.<init>(com.color.widget.ColorSimpleLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.3.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.3.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.3.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorSimpleLock$4 */
    class AnonymousClass4 implements AnimatorListener {
        final /* synthetic */ ColorSimpleLock this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.4.<init>(com.color.widget.ColorSimpleLock):void, dex: 
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
        AnonymousClass4(com.color.widget.ColorSimpleLock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.4.<init>(com.color.widget.ColorSimpleLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.4.<init>(com.color.widget.ColorSimpleLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.4.onAnimationCancel(android.animation.Animator):void, dex: 
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
        public void onAnimationCancel(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.4.onAnimationCancel(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.4.onAnimationCancel(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.4.onAnimationEnd(android.animation.Animator):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.4.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.4.onAnimationEnd(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.4.onAnimationRepeat(android.animation.Animator):void, dex: 
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
        public void onAnimationRepeat(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.4.onAnimationRepeat(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.4.onAnimationRepeat(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.4.onAnimationStart(android.animation.Animator):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.4.onAnimationStart(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.4.onAnimationStart(android.animation.Animator):void");
        }
    }

    /* renamed from: com.color.widget.ColorSimpleLock$5 */
    class AnonymousClass5 implements AnimatorUpdateListener {
        final /* synthetic */ ColorSimpleLock this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.5.<init>(com.color.widget.ColorSimpleLock):void, dex: 
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
        AnonymousClass5(com.color.widget.ColorSimpleLock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.5.<init>(com.color.widget.ColorSimpleLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.5.<init>(com.color.widget.ColorSimpleLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.5.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onAnimationUpdate(android.animation.ValueAnimator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.5.onAnimationUpdate(android.animation.ValueAnimator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.5.onAnimationUpdate(android.animation.ValueAnimator):void");
        }
    }

    /* renamed from: com.color.widget.ColorSimpleLock$6 */
    class AnonymousClass6 implements AnimatorListener {
        final /* synthetic */ ColorSimpleLock this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.6.<init>(com.color.widget.ColorSimpleLock):void, dex: 
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
        AnonymousClass6(com.color.widget.ColorSimpleLock r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.6.<init>(com.color.widget.ColorSimpleLock):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.6.<init>(com.color.widget.ColorSimpleLock):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.6.onAnimationCancel(android.animation.Animator):void, dex: 
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
        public void onAnimationCancel(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.6.onAnimationCancel(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.6.onAnimationCancel(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.6.onAnimationEnd(android.animation.Animator):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.6.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.6.onAnimationEnd(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.6.onAnimationRepeat(android.animation.Animator):void, dex: 
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
        public void onAnimationRepeat(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.color.widget.ColorSimpleLock.6.onAnimationRepeat(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.6.onAnimationRepeat(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.6.onAnimationStart(android.animation.Animator):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.6.onAnimationStart(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.6.onAnimationStart(android.animation.Animator):void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleLock.-get0(com.color.widget.ColorSimpleLock):boolean, dex: 
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
    static /* synthetic */ boolean m223-get0(com.color.widget.ColorSimpleLock r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleLock.-get0(com.color.widget.ColorSimpleLock):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-get0(com.color.widget.ColorSimpleLock):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.-get1(com.color.widget.ColorSimpleLock):boolean, dex:  in method: com.color.widget.ColorSimpleLock.-get1(com.color.widget.ColorSimpleLock):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.-get1(com.color.widget.ColorSimpleLock):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$20.decode(InstructionCodec.java:450)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -get1 */
    static /* synthetic */ boolean m224-get1(com.color.widget.ColorSimpleLock r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.-get1(com.color.widget.ColorSimpleLock):boolean, dex:  in method: com.color.widget.ColorSimpleLock.-get1(com.color.widget.ColorSimpleLock):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-get1(com.color.widget.ColorSimpleLock):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.-get2(com.color.widget.ColorSimpleLock):android.animation.Animator, dex: 
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
    /* renamed from: -get2 */
    static /* synthetic */ android.animation.Animator m225-get2(com.color.widget.ColorSimpleLock r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.-get2(com.color.widget.ColorSimpleLock):android.animation.Animator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-get2(com.color.widget.ColorSimpleLock):android.animation.Animator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorSimpleLock.-set0(com.color.widget.ColorSimpleLock, int):int, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    /* renamed from: -set0 */
    static /* synthetic */ int m226-set0(com.color.widget.ColorSimpleLock r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorSimpleLock.-set0(com.color.widget.ColorSimpleLock, int):int, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-set0(com.color.widget.ColorSimpleLock, int):int");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set1(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
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
    /* renamed from: -set1 */
    static /* synthetic */ boolean m227-set1(com.color.widget.ColorSimpleLock r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set1(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-set1(com.color.widget.ColorSimpleLock, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set2(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
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
    /* renamed from: -set2 */
    static /* synthetic */ boolean m228-set2(com.color.widget.ColorSimpleLock r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set2(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-set2(com.color.widget.ColorSimpleLock, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set3(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
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
    /* renamed from: -set3 */
    static /* synthetic */ boolean m229-set3(com.color.widget.ColorSimpleLock r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set3(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-set3(com.color.widget.ColorSimpleLock, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set4(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
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
    /* renamed from: -set4 */
    static /* synthetic */ boolean m230-set4(com.color.widget.ColorSimpleLock r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.-set4(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-set4(com.color.widget.ColorSimpleLock, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.-set5(com.color.widget.ColorSimpleLock, boolean):boolean, dex:  in method: com.color.widget.ColorSimpleLock.-set5(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.-set5(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$20.decode(InstructionCodec.java:450)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    /* renamed from: -set5 */
    static /* synthetic */ boolean m231-set5(com.color.widget.ColorSimpleLock r1, boolean r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.-set5(com.color.widget.ColorSimpleLock, boolean):boolean, dex:  in method: com.color.widget.ColorSimpleLock.-set5(com.color.widget.ColorSimpleLock, boolean):boolean, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-set5(com.color.widget.ColorSimpleLock, boolean):boolean");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.-set6(com.color.widget.ColorSimpleLock, android.animation.Animator):android.animation.Animator, dex: 
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
    /* renamed from: -set6 */
    static /* synthetic */ android.animation.Animator m232-set6(com.color.widget.ColorSimpleLock r1, android.animation.Animator r2) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.color.widget.ColorSimpleLock.-set6(com.color.widget.ColorSimpleLock, android.animation.Animator):android.animation.Animator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.-set6(com.color.widget.ColorSimpleLock, android.animation.Animator):android.animation.Animator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.<init>(android.content.Context, android.util.AttributeSet, int):void, dex:  in method: com.color.widget.ColorSimpleLock.<init>(android.content.Context, android.util.AttributeSet, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.<init>(android.content.Context, android.util.AttributeSet, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public ColorSimpleLock(android.content.Context r1, android.util.AttributeSet r2, int r3) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.<init>(android.content.Context, android.util.AttributeSet, int):void, dex:  in method: com.color.widget.ColorSimpleLock.<init>(android.content.Context, android.util.AttributeSet, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.<init>(android.content.Context, android.util.AttributeSet, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.createMorphingAnimationCircleToLine():android.animation.ValueAnimator, dex: 
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
    private android.animation.ValueAnimator createMorphingAnimationCircleToLine() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.createMorphingAnimationCircleToLine():android.animation.ValueAnimator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.createMorphingAnimationCircleToLine():android.animation.ValueAnimator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.createMorphingAnimationLineToCircle():android.animation.ValueAnimator, dex: 
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
    private android.animation.ValueAnimator createMorphingAnimationLineToCircle() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.createMorphingAnimationLineToCircle():android.animation.ValueAnimator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.createMorphingAnimationLineToCircle():android.animation.ValueAnimator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawAllCodeAnimation(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawAllCodeAnimation(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.drawAllCodeAnimation(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawAllCodeAnimation(android.graphics.Canvas r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawAllCodeAnimation(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawAllCodeAnimation(android.graphics.Canvas, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawAllCodeAnimation(android.graphics.Canvas, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.drawCircle(android.graphics.Canvas, int, int, int, int):void, dex: 
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
    private void drawCircle(android.graphics.Canvas r1, int r2, int r3, int r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.drawCircle(android.graphics.Canvas, int, int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawCircle(android.graphics.Canvas, int, int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawCircleToLine(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawCircleToLine(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.drawCircleToLine(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawCircleToLine(android.graphics.Canvas r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawCircleToLine(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawCircleToLine(android.graphics.Canvas, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawCircleToLine(android.graphics.Canvas, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawClearAllAnimation(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawClearAllAnimation(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.drawClearAllAnimation(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawClearAllAnimation(android.graphics.Canvas r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawClearAllAnimation(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawClearAllAnimation(android.graphics.Canvas, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawClearAllAnimation(android.graphics.Canvas, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawFailedAnimation(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawFailedAnimation(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.drawFailedAnimation(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:61)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawFailedAnimation(android.graphics.Canvas r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawFailedAnimation(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawFailedAnimation(android.graphics.Canvas, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawFailedAnimation(android.graphics.Canvas, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.drawLine(android.graphics.Canvas, int, int, int, int):void, dex: 
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
    private void drawLine(android.graphics.Canvas r1, int r2, int r3, int r4, int r5) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.drawLine(android.graphics.Canvas, int, int, int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawLine(android.graphics.Canvas, int, int, int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawLineToCircle(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawLineToCircle(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.drawLineToCircle(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawLineToCircle(android.graphics.Canvas r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawLineToCircle(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawLineToCircle(android.graphics.Canvas, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawLineToCircle(android.graphics.Canvas, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawPreviousState(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawPreviousState(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.drawPreviousState(android.graphics.Canvas, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawPreviousState(android.graphics.Canvas r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawPreviousState(android.graphics.Canvas, int):void, dex:  in method: com.color.widget.ColorSimpleLock.drawPreviousState(android.graphics.Canvas, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawPreviousState(android.graphics.Canvas, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawScaleCircle(android.graphics.Canvas, int, int, int, int, float, float):void, dex:  in method: com.color.widget.ColorSimpleLock.drawScaleCircle(android.graphics.Canvas, int, int, int, int, float, float):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.drawScaleCircle(android.graphics.Canvas, int, int, int, int, float, float):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterRange(InstructionCodec.java:985)
        	at com.android.dx.io.instructions.InstructionCodec.access$1100(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$28.decode(InstructionCodec.java:611)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    private void drawScaleCircle(android.graphics.Canvas r1, int r2, int r3, int r4, int r5, float r6, float r7) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.drawScaleCircle(android.graphics.Canvas, int, int, int, int, float, float):void, dex:  in method: com.color.widget.ColorSimpleLock.drawScaleCircle(android.graphics.Canvas, int, int, int, int, float, float):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.drawScaleCircle(android.graphics.Canvas, int, int, int, int, float, float):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.createFailedAnimator():android.animation.Animator, dex: 
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
    public android.animation.Animator createFailedAnimator() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.color.widget.ColorSimpleLock.createFailedAnimator():android.animation.Animator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.createFailedAnimator():android.animation.Animator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.getFailedAnimator():android.animation.Animator, dex: 
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
    public android.animation.Animator getFailedAnimator() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.getFailedAnimator():android.animation.Animator, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.getFailedAnimator():android.animation.Animator");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleLock.onDraw(android.graphics.Canvas):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    protected void onDraw(android.graphics.Canvas r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleLock.onDraw(android.graphics.Canvas):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.onDraw(android.graphics.Canvas):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.onMeasure(int, int):void, dex:  in method: com.color.widget.ColorSimpleLock.onMeasure(int, int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.onMeasure(int, int):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$35.decode(InstructionCodec.java:790)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    protected void onMeasure(int r1, int r2) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.onMeasure(int, int):void, dex:  in method: com.color.widget.ColorSimpleLock.onMeasure(int, int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.onMeasure(int, int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleLock.setAllCode(boolean):void, dex: 
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
    public void setAllCode(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: com.color.widget.ColorSimpleLock.setAllCode(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setAllCode(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleLock.setClearAll(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setClearAll(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleLock.setClearAll(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setClearAll(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.setDeleteLast(boolean):void, dex: 
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
    public void setDeleteLast(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.color.widget.ColorSimpleLock.setDeleteLast(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setDeleteLast(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.setFailed(boolean):void, dex:  in method: com.color.widget.ColorSimpleLock.setFailed(boolean):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.setFailed(boolean):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
        	at com.android.dx.io.instructions.InstructionCodec$20.decode(InstructionCodec.java:450)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void setFailed(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.setFailed(boolean):void, dex:  in method: com.color.widget.ColorSimpleLock.setFailed(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setFailed(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.setFingerprintRecognition(boolean):void, dex: 
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
    public void setFingerprintRecognition(boolean r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00eb in method: com.color.widget.ColorSimpleLock.setFingerprintRecognition(boolean):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setFingerprintRecognition(boolean):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleLock.setOneCode(int):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setOneCode(int r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.color.widget.ColorSimpleLock.setOneCode(int):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setOneCode(int):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorSimpleLock.setScaleX(float):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setScaleX(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorSimpleLock.setScaleX(float):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setScaleX(float):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorSimpleLock.setScaleY(float):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    public void setScaleY(float r1) {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: com.color.widget.ColorSimpleLock.setScaleY(float):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setScaleY(float):void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.color.widget.ColorSimpleLock.setTranslationX(float):void, dex:  in method: com.color.widget.ColorSimpleLock.setTranslationX(float):void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.color.widget.ColorSimpleLock.setTranslationX(float):void, dex: 
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
        	... 5 more
        Caused by: java.io.EOFException
        	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
        	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
        	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
        	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
        	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
        	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
        	... 6 more
        */
    public void setTranslationX(float r1) {
        /*
        // Can't load method instructions: Load method exception: null in method: com.color.widget.ColorSimpleLock.setTranslationX(float):void, dex:  in method: com.color.widget.ColorSimpleLock.setTranslationX(float):void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.color.widget.ColorSimpleLock.setTranslationX(float):void");
    }

    public ColorSimpleLock(Context context) {
        this(context, null);
    }

    public ColorSimpleLock(Context context, AttributeSet attrs) {
        this(context, attrs, 201393298);
    }

    public Animator getAddAnimator() {
        return createMorphingAnimationLineToCircle();
    }

    public Animator getDeleteAnimator() {
        return createMorphingAnimationCircleToLine();
    }

    public float translationValueForTranslant(float input) {
        if (input < 0.5f) {
            return (((input - 0.5f) * -2.0f) * (input - 0.5f)) + 0.5f;
        }
        if (input > 0.8f) {
            return (input - 1.3f) * input;
        }
        return ((input - 0.6f) * (input - 3.7f)) + 0.18f;
    }

    public float translationValueForScale(float input) {
        if (input < 0.5f) {
            return ((-(input - 0.5f)) * (input - 0.5f)) + 0.25f;
        }
        if (input > 0.8f) {
            return (((input - 0.8f) * 3.12f) * (input - 0.8f)) - 0.08f;
        }
        return ((input - 0.6f) * (input - 1.8f)) + 0.12f;
    }
}
