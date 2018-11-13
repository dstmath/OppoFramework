package android.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.app.SharedElementCallback.OnSharedElementsReadyListener;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.transition.Transition;
import android.transition.Transition.TransitionListenerAdapter;
import android.transition.TransitionManager;
import android.util.ArrayMap;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import java.util.ArrayList;

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
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ClassModifier.removeFieldUsageFromConstructor(ClassModifier.java:100)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticFields(ClassModifier.java:75)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:48)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
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
class EnterTransitionCoordinator extends ActivityTransitionCoordinator {
    private static final int MIN_ANIMATION_FRAMES = 2;
    private static final String TAG = "EnterTransitionCoordinator";
    private Activity mActivity;
    private boolean mAreViewsReady;
    private ObjectAnimator mBackgroundAnimator;
    private Transition mEnterViewsTransition;
    private boolean mHasStopped;
    private boolean mIsCanceled;
    private final boolean mIsCrossTask;
    private boolean mIsExitTransitionComplete;
    private boolean mIsReadyForTransition;
    private boolean mIsViewsTransitionStarted;
    private boolean mSharedElementTransitionStarted;
    private Bundle mSharedElementsBundle;
    private OnPreDrawListener mViewsReadyListener;
    private boolean mWasOpaque;

    /* renamed from: android.app.EnterTransitionCoordinator$10 */
    class AnonymousClass10 extends AnimatorListenerAdapter {
        final /* synthetic */ EnterTransitionCoordinator this$0;
        final /* synthetic */ ViewGroup val$decorView;
        final /* synthetic */ ArrayList val$rejectedSnapshots;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.10.<init>(android.app.EnterTransitionCoordinator, android.view.ViewGroup, java.util.ArrayList):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass10(android.app.EnterTransitionCoordinator r1, android.view.ViewGroup r2, java.util.ArrayList r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.10.<init>(android.app.EnterTransitionCoordinator, android.view.ViewGroup, java.util.ArrayList):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.10.<init>(android.app.EnterTransitionCoordinator, android.view.ViewGroup, java.util.ArrayList):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.app.EnterTransitionCoordinator.10.onAnimationEnd(android.animation.Animator):void, dex:  in method: android.app.EnterTransitionCoordinator.10.onAnimationEnd(android.animation.Animator):void, dex: 
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
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.app.EnterTransitionCoordinator.10.onAnimationEnd(android.animation.Animator):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$34.decode(InstructionCodec.java:752)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.app.EnterTransitionCoordinator.10.onAnimationEnd(android.animation.Animator):void, dex:  in method: android.app.EnterTransitionCoordinator.10.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.10.onAnimationEnd(android.animation.Animator):void");
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator$11 */
    class AnonymousClass11 implements Runnable {
        final /* synthetic */ EnterTransitionCoordinator this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.11.<init>(android.app.EnterTransitionCoordinator):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass11(android.app.EnterTransitionCoordinator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.11.<init>(android.app.EnterTransitionCoordinator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.11.<init>(android.app.EnterTransitionCoordinator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.EnterTransitionCoordinator.11.run():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void run() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.EnterTransitionCoordinator.11.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.11.run():void");
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator$2 */
    class AnonymousClass2 implements OnPreDrawListener {
        final /* synthetic */ EnterTransitionCoordinator this$0;
        final /* synthetic */ ViewGroup val$decor;
        final /* synthetic */ ArrayMap val$sharedElements;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.2.<init>(android.app.EnterTransitionCoordinator, android.view.ViewGroup, android.util.ArrayMap):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass2(android.app.EnterTransitionCoordinator r1, android.view.ViewGroup r2, android.util.ArrayMap r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.2.<init>(android.app.EnterTransitionCoordinator, android.view.ViewGroup, android.util.ArrayMap):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.2.<init>(android.app.EnterTransitionCoordinator, android.view.ViewGroup, android.util.ArrayMap):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.EnterTransitionCoordinator.2.onPreDraw():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean onPreDraw() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.EnterTransitionCoordinator.2.onPreDraw():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.2.onPreDraw():boolean");
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator$3 */
    class AnonymousClass3 implements OnPreDrawListener {
        final /* synthetic */ EnterTransitionCoordinator this$0;
        final /* synthetic */ View val$decorView;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.3.<init>(android.app.EnterTransitionCoordinator, android.view.View):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass3(android.app.EnterTransitionCoordinator r1, android.view.View r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.3.<init>(android.app.EnterTransitionCoordinator, android.view.View):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.3.<init>(android.app.EnterTransitionCoordinator, android.view.View):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.EnterTransitionCoordinator.3.onPreDraw():boolean, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean onPreDraw() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.EnterTransitionCoordinator.3.onPreDraw():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.3.onPreDraw():boolean");
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator$4 */
    class AnonymousClass4 implements Runnable {
        int mAnimations;
        final /* synthetic */ EnterTransitionCoordinator this$0;

        AnonymousClass4(EnterTransitionCoordinator this$0) {
            this.this$0 = this$0;
        }

        public void run() {
            int i = this.mAnimations;
            this.mAnimations = i + 1;
            if (i < 2) {
                View decorView = this.this$0.getDecor();
                if (decorView != null) {
                    decorView.postOnAnimation(this);
                }
            } else if (this.this$0.mResultReceiver != null) {
                this.this$0.mResultReceiver.send(101, null);
                this.this$0.mResultReceiver = null;
            }
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator$6 */
    class AnonymousClass6 extends TransitionListenerAdapter {
        final /* synthetic */ EnterTransitionCoordinator this$0;

        AnonymousClass6(EnterTransitionCoordinator this$0) {
            this.this$0 = this$0;
        }

        public void onTransitionStart(Transition transition) {
            this.this$0.sharedElementTransitionStarted();
        }

        public void onTransitionEnd(Transition transition) {
            transition.removeListener(this);
            this.this$0.sharedElementTransitionComplete();
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator$8 */
    class AnonymousClass8 extends AnimatorListenerAdapter {
        final /* synthetic */ EnterTransitionCoordinator this$0;

        AnonymousClass8(EnterTransitionCoordinator this$0) {
            this.this$0 = this$0;
        }

        public void onAnimationEnd(Animator animation) {
            this.this$0.makeOpaque();
        }
    }

    /* renamed from: android.app.EnterTransitionCoordinator$9 */
    class AnonymousClass9 extends TransitionListenerAdapter {
        final /* synthetic */ EnterTransitionCoordinator this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.9.<init>(android.app.EnterTransitionCoordinator):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        AnonymousClass9(android.app.EnterTransitionCoordinator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.EnterTransitionCoordinator.9.<init>(android.app.EnterTransitionCoordinator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.9.<init>(android.app.EnterTransitionCoordinator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.app.EnterTransitionCoordinator.9.onTransitionEnd(android.transition.Transition):void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public void onTransitionEnd(android.transition.Transition r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.app.EnterTransitionCoordinator.9.onTransitionEnd(android.transition.Transition):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.9.onTransitionEnd(android.transition.Transition):void");
        }
    }

    public EnterTransitionCoordinator(Activity activity, ResultReceiver resultReceiver, ArrayList<String> sharedElementNames, boolean isReturning, boolean isCrossTask) {
        boolean z = false;
        Window window = activity.getWindow();
        if (isReturning && !isCrossTask) {
            z = true;
        }
        super(window, sharedElementNames, getListener(activity, z), isReturning);
        this.mActivity = activity;
        this.mIsCrossTask = isCrossTask;
        setResultReceiver(resultReceiver);
        prepareEnter();
        Bundle resultReceiverBundle = new Bundle();
        resultReceiverBundle.putParcelable("android:remoteReceiver", this);
        this.mResultReceiver.send(100, resultReceiverBundle);
        final View decorView = getDecor();
        if (decorView != null) {
            decorView.getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener(this) {
                final /* synthetic */ EnterTransitionCoordinator this$0;

                public boolean onPreDraw() {
                    if (this.this$0.mIsReadyForTransition) {
                        decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                    }
                    return this.this$0.mIsReadyForTransition;
                }
            });
        }
    }

    boolean isCrossTask() {
        return this.mIsCrossTask;
    }

    public void viewInstancesReady(ArrayList<String> accepted, ArrayList<String> localNames, ArrayList<View> localViews) {
        boolean remap = false;
        for (int i = 0; i < localViews.size(); i++) {
            View view = (View) localViews.get(i);
            if (!TextUtils.equals(view.getTransitionName(), (CharSequence) localNames.get(i)) || !view.isAttachedToWindow()) {
                remap = true;
                break;
            }
        }
        if (remap) {
            triggerViewsReady(mapNamedElements(accepted, localNames));
        } else {
            triggerViewsReady(mapSharedElements(accepted, localViews));
        }
    }

    public void namedViewsReady(ArrayList<String> accepted, ArrayList<String> localNames) {
        triggerViewsReady(mapNamedElements(accepted, localNames));
    }

    public Transition getEnterViewsTransition() {
        return this.mEnterViewsTransition;
    }

    protected void viewsReady(ArrayMap<String, View> sharedElements) {
        super.viewsReady(sharedElements);
        this.mIsReadyForTransition = true;
        hideViews(this.mSharedElements);
        if (!(getViewsTransition() == null || this.mTransitioningViews == null)) {
            hideViews(this.mTransitioningViews);
        }
        if (this.mIsReturning) {
            sendSharedElementDestination();
        } else {
            moveSharedElementsToOverlay();
        }
        if (this.mSharedElementsBundle != null) {
            onTakeSharedElements();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void triggerViewsReady(android.util.ArrayMap<java.lang.String, android.view.View> r4) {
        /*
        r3 = this;
        r1 = r3.mAreViewsReady;
        if (r1 == 0) goto L_0x0005;
    L_0x0004:
        return;
    L_0x0005:
        r1 = 1;
        r3.mAreViewsReady = r1;
        r0 = r3.getDecor();
        if (r0 == 0) goto L_0x003b;
    L_0x000e:
        r1 = r0.isAttachedToWindow();
        if (r1 == 0) goto L_0x0027;
    L_0x0014:
        r1 = r4.isEmpty();
        if (r1 != 0) goto L_0x003b;
    L_0x001a:
        r1 = 0;
        r1 = r4.valueAt(r1);
        r1 = (android.view.View) r1;
        r1 = r1.isLayoutRequested();
        if (r1 == 0) goto L_0x003b;
    L_0x0027:
        r1 = new android.app.EnterTransitionCoordinator$2;
        r1.<init>(r3, r0, r4);
        r3.mViewsReadyListener = r1;
        r1 = r0.getViewTreeObserver();
        r2 = r3.mViewsReadyListener;
        r1.addOnPreDrawListener(r2);
        r0.invalidate();
    L_0x003a:
        return;
    L_0x003b:
        r3.viewsReady(r4);
        goto L_0x003a;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.triggerViewsReady(android.util.ArrayMap):void");
    }

    private ArrayMap<String, View> mapNamedElements(ArrayList<String> accepted, ArrayList<String> localNames) {
        ArrayMap<String, View> sharedElements = new ArrayMap();
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.findNamedViews(sharedElements);
        }
        if (accepted != null) {
            for (int i = 0; i < localNames.size(); i++) {
                String localName = (String) localNames.get(i);
                String acceptedName = (String) accepted.get(i);
                if (!(localName == null || localName.equals(acceptedName))) {
                    View view = (View) sharedElements.remove(localName);
                    if (view != null) {
                        sharedElements.put(acceptedName, view);
                    }
                }
            }
        }
        return sharedElements;
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void sendSharedElementDestination() {
        /*
        r6 = this;
        r1 = r6.getDecor();
        r4 = r6.allowOverlappingTransitions();
        if (r4 == 0) goto L_0x002b;
    L_0x000a:
        r4 = r6.getEnterViewsTransition();
        if (r4 == 0) goto L_0x002b;
    L_0x0010:
        r0 = 0;
    L_0x0011:
        if (r0 == 0) goto L_0x0056;
    L_0x0013:
        r3 = r6.captureSharedElementState();
        r6.moveSharedElementsToOverlay();
        r4 = r6.mResultReceiver;
        r5 = 107; // 0x6b float:1.5E-43 double:5.3E-322;
        r4.send(r5, r3);
    L_0x0021:
        r4 = r6.allowOverlappingTransitions();
        if (r4 == 0) goto L_0x002a;
    L_0x0027:
        r6.startEnterTransitionOnly();
    L_0x002a:
        return;
    L_0x002b:
        if (r1 != 0) goto L_0x002f;
    L_0x002d:
        r0 = 1;
        goto L_0x0011;
    L_0x002f:
        r4 = r1.isLayoutRequested();
        if (r4 == 0) goto L_0x0051;
    L_0x0035:
        r0 = 0;
    L_0x0036:
        if (r0 == 0) goto L_0x0011;
    L_0x0038:
        r2 = 0;
    L_0x0039:
        r4 = r6.mSharedElements;
        r4 = r4.size();
        if (r2 >= r4) goto L_0x0011;
    L_0x0041:
        r4 = r6.mSharedElements;
        r4 = r4.get(r2);
        r4 = (android.view.View) r4;
        r4 = r4.isLayoutRequested();
        if (r4 == 0) goto L_0x0053;
    L_0x004f:
        r0 = 0;
        goto L_0x0011;
    L_0x0051:
        r0 = 1;
        goto L_0x0036;
    L_0x0053:
        r2 = r2 + 1;
        goto L_0x0039;
    L_0x0056:
        if (r1 == 0) goto L_0x0021;
    L_0x0058:
        r4 = r1.getViewTreeObserver();
        r5 = new android.app.EnterTransitionCoordinator$3;
        r5.<init>(r6, r1);
        r4.addOnPreDrawListener(r5);
        goto L_0x0021;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.sendSharedElementDestination():void");
    }

    private static SharedElementCallback getListener(Activity activity, boolean isReturning) {
        return isReturning ? activity.mExitTransitionListener : activity.mEnterTransitionListener;
    }

    protected void onReceiveResult(int resultCode, Bundle resultData) {
        switch (resultCode) {
            case 103:
                if (!this.mIsCanceled) {
                    this.mSharedElementsBundle = resultData;
                    onTakeSharedElements();
                    return;
                }
                return;
            case 104:
                if (!this.mIsCanceled) {
                    this.mIsExitTransitionComplete = true;
                    if (this.mSharedElementTransitionStarted) {
                        onRemoteExitTransitionComplete();
                        return;
                    }
                    return;
                }
                return;
            case 106:
                cancel();
                return;
            default:
                return;
        }
    }

    public boolean isWaitingForRemoteExit() {
        return this.mIsReturning && this.mResultReceiver != null;
    }

    public void forceViewsToAppear() {
        if (this.mIsReturning) {
            if (this.mIsReadyForTransition) {
                if (!this.mSharedElementTransitionStarted) {
                    moveSharedElementsFromOverlay();
                    this.mSharedElementTransitionStarted = true;
                    showViews(this.mSharedElements, true);
                    this.mSharedElements.clear();
                    sharedElementTransitionComplete();
                }
                if (!this.mIsViewsTransitionStarted) {
                    this.mIsViewsTransitionStarted = true;
                    showViews(this.mTransitioningViews, true);
                    setTransitioningViewsVisiblity(0, true);
                    this.mTransitioningViews.clear();
                    viewsTransitionComplete();
                }
                cancelPendingTransitions();
            } else {
                this.mIsReadyForTransition = true;
                ViewGroup decor = getDecor();
                if (!(decor == null || this.mViewsReadyListener == null)) {
                    decor.getViewTreeObserver().removeOnPreDrawListener(this.mViewsReadyListener);
                    this.mViewsReadyListener = null;
                }
                showViews(this.mTransitioningViews, true);
                setTransitioningViewsVisiblity(0, true);
                this.mSharedElements.clear();
                this.mAllSharedElementNames.clear();
                this.mTransitioningViews.clear();
                this.mIsReadyForTransition = true;
                viewsTransitionComplete();
                sharedElementTransitionComplete();
            }
            this.mAreViewsReady = true;
            if (this.mResultReceiver != null) {
                this.mResultReceiver.send(106, null);
                this.mResultReceiver = null;
            }
        }
    }

    private void cancel() {
        if (!this.mIsCanceled) {
            this.mIsCanceled = true;
            if (getViewsTransition() == null || this.mIsViewsTransitionStarted) {
                showViews(this.mSharedElements, true);
            } else if (this.mTransitioningViews != null) {
                this.mTransitioningViews.addAll(this.mSharedElements);
            }
            moveSharedElementsFromOverlay();
            this.mSharedElementNames.clear();
            this.mSharedElements.clear();
            this.mAllSharedElementNames.clear();
            startSharedElementTransition(null);
            onRemoteExitTransitionComplete();
        }
    }

    public boolean isReturning() {
        return this.mIsReturning;
    }

    protected void prepareEnter() {
        ViewGroup decorView = getDecor();
        if (this.mActivity != null && decorView != null) {
            if (!isCrossTask()) {
                this.mActivity.overridePendingTransition(0, 0);
            }
            if (this.mIsReturning) {
                this.mActivity = null;
            } else {
                this.mWasOpaque = this.mActivity.convertToTranslucent(null, null);
                Drawable background = decorView.getBackground();
                if (background != null) {
                    getWindow().setBackgroundDrawable(null);
                    background = background.mutate();
                    background.setAlpha(0);
                    getWindow().setBackgroundDrawable(background);
                }
            }
        }
    }

    protected Transition getViewsTransition() {
        Window window = getWindow();
        if (window == null) {
            return null;
        }
        if (this.mIsReturning) {
            return window.getReenterTransition();
        }
        return window.getEnterTransition();
    }

    protected Transition getSharedElementTransition() {
        Window window = getWindow();
        if (window == null) {
            return null;
        }
        if (this.mIsReturning) {
            return window.getSharedElementReenterTransition();
        }
        return window.getSharedElementEnterTransition();
    }

    private void startSharedElementTransition(Bundle sharedElementState) {
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            ArrayList<String> rejectedNames = new ArrayList(this.mAllSharedElementNames);
            rejectedNames.removeAll(this.mSharedElementNames);
            ArrayList<View> rejectedSnapshots = createSnapshots(sharedElementState, rejectedNames);
            if (this.mListener != null) {
                this.mListener.onRejectSharedElements(rejectedSnapshots);
            }
            removeNullViews(rejectedSnapshots);
            startRejectedAnimations(rejectedSnapshots);
            ArrayList<View> sharedElementSnapshots = createSnapshots(sharedElementState, this.mSharedElementNames);
            showViews(this.mSharedElements, true);
            scheduleSetSharedElementEnd(sharedElementSnapshots);
            ArrayList<SharedElementOriginalState> originalImageViewState = setSharedElementState(sharedElementState, sharedElementSnapshots);
            requestLayoutForSharedElements();
            boolean startEnterTransition = allowOverlappingTransitions() && !this.mIsReturning;
            setGhostVisibility(4);
            scheduleGhostVisibilityChange(4);
            pauseInput();
            Transition transition = beginTransition(decorView, startEnterTransition, true);
            scheduleGhostVisibilityChange(0);
            setGhostVisibility(0);
            if (startEnterTransition) {
                startEnterTransition(transition);
            }
            ActivityTransitionCoordinator.setOriginalSharedElementState(this.mSharedElements, originalImageViewState);
            if (this.mResultReceiver != null) {
                decorView.postOnAnimation(new AnonymousClass4(this));
            }
        }
    }

    private static void removeNullViews(ArrayList<View> views) {
        if (views != null) {
            for (int i = views.size() - 1; i >= 0; i--) {
                if (views.get(i) == null) {
                    views.remove(i);
                }
            }
        }
    }

    private void onTakeSharedElements() {
        if (this.mIsReadyForTransition && this.mSharedElementsBundle != null) {
            final Bundle sharedElementState = this.mSharedElementsBundle;
            this.mSharedElementsBundle = null;
            OnSharedElementsReadyListener listener = new OnSharedElementsReadyListener(this) {
                final /* synthetic */ EnterTransitionCoordinator this$0;

                public void onSharedElementsReady() {
                    final View decorView = this.this$0.getDecor();
                    if (decorView != null) {
                        ViewTreeObserver viewTreeObserver = decorView.getViewTreeObserver();
                        final Bundle bundle = sharedElementState;
                        viewTreeObserver.addOnPreDrawListener(new OnPreDrawListener(this) {
                            final /* synthetic */ AnonymousClass5 this$1;

                            public boolean onPreDraw() {
                                decorView.getViewTreeObserver().removeOnPreDrawListener(this);
                                EnterTransitionCoordinator enterTransitionCoordinator = this.this$1.this$0;
                                final Bundle bundle = bundle;
                                enterTransitionCoordinator.startTransition(new Runnable(this) {
                                    final /* synthetic */ AnonymousClass1 this$2;

                                    public void run() {
                                        this.this$2.this$1.this$0.startSharedElementTransition(bundle);
                                    }
                                });
                                return false;
                            }
                        });
                        decorView.invalidate();
                    }
                }
            };
            if (this.mListener == null) {
                listener.onSharedElementsReady();
            } else {
                this.mListener.onSharedElementsArrived(this.mSharedElementNames, this.mSharedElements, listener);
            }
        }
    }

    private void requestLayoutForSharedElements() {
        int numSharedElements = this.mSharedElements.size();
        for (int i = 0; i < numSharedElements; i++) {
            ((View) this.mSharedElements.get(i)).requestLayout();
        }
    }

    private Transition beginTransition(ViewGroup decorView, boolean startEnterTransition, boolean startSharedElementTransition) {
        Transition sharedElementTransition = null;
        if (startSharedElementTransition) {
            if (!this.mSharedElementNames.isEmpty()) {
                sharedElementTransition = configureTransition(getSharedElementTransition(), false);
            }
            if (sharedElementTransition == null) {
                sharedElementTransitionStarted();
                sharedElementTransitionComplete();
            } else {
                sharedElementTransition.addListener(new AnonymousClass6(this));
            }
        }
        Transition viewsTransition = null;
        if (startEnterTransition) {
            this.mIsViewsTransitionStarted = true;
            if (!(this.mTransitioningViews == null || this.mTransitioningViews.isEmpty())) {
                viewsTransition = configureTransition(getViewsTransition(), true);
                if (!(viewsTransition == null || this.mIsReturning)) {
                    stripOffscreenViews();
                }
            }
            if (viewsTransition == null) {
                viewsTransitionComplete();
            } else {
                final ArrayList<View> transitioningViews = this.mTransitioningViews;
                viewsTransition.addListener(new ContinueTransitionListener(this, this) {
                    final /* synthetic */ EnterTransitionCoordinator this$0;

                    public void onTransitionStart(Transition transition) {
                        this.this$0.mEnterViewsTransition = transition;
                        if (transitioningViews != null) {
                            this.this$0.showViews(transitioningViews, false);
                        }
                        super.onTransitionStart(transition);
                    }

                    public void onTransitionEnd(Transition transition) {
                        this.this$0.mEnterViewsTransition = null;
                        transition.removeListener(this);
                        this.this$0.viewsTransitionComplete();
                        super.onTransitionEnd(transition);
                    }
                });
            }
        }
        Transition transition = ActivityTransitionCoordinator.mergeTransitions(sharedElementTransition, viewsTransition);
        if (transition != null) {
            transition.addListener(new ContinueTransitionListener());
            if (startEnterTransition) {
                setTransitioningViewsVisiblity(4, false);
            }
            TransitionManager.beginDelayedTransition(decorView, transition);
            if (startEnterTransition) {
                setTransitioningViewsVisiblity(0, false);
            }
            decorView.invalidate();
        } else {
            transitionStarted();
        }
        return transition;
    }

    protected void onTransitionsComplete() {
        moveSharedElementsFromOverlay();
        ViewGroup decorView = getDecor();
        if (decorView != null) {
            decorView.sendAccessibilityEvent(2048);
        }
    }

    private void sharedElementTransitionStarted() {
        this.mSharedElementTransitionStarted = true;
        if (this.mIsExitTransitionComplete) {
            send(104, null);
        }
    }

    private void startEnterTransition(Transition transition) {
        ViewGroup decorView = getDecor();
        if (!this.mIsReturning && decorView != null) {
            Drawable background = decorView.getBackground();
            if (background != null) {
                Object background2 = background.mutate();
                getWindow().setBackgroundDrawable(background2);
                this.mBackgroundAnimator = ObjectAnimator.ofInt(background2, "alpha", 255);
                this.mBackgroundAnimator.setDuration(getFadeDuration());
                this.mBackgroundAnimator.addListener(new AnonymousClass8(this));
                this.mBackgroundAnimator.start();
            } else if (transition != null) {
                transition.addListener(new AnonymousClass9(this));
            } else {
                makeOpaque();
            }
        }
    }

    public void stop() {
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.end();
            this.mBackgroundAnimator = null;
        } else if (this.mWasOpaque) {
            ViewGroup decorView = getDecor();
            if (decorView != null) {
                Drawable drawable = decorView.getBackground();
                if (drawable != null) {
                    drawable.setAlpha(1);
                }
            }
        }
        makeOpaque();
        this.mIsCanceled = true;
        this.mResultReceiver = null;
        this.mActivity = null;
        moveSharedElementsFromOverlay();
        if (this.mTransitioningViews != null) {
            showViews(this.mTransitioningViews, true);
            setTransitioningViewsVisiblity(0, true);
        }
        showViews(this.mSharedElements, true);
        clearState();
    }

    public boolean cancelEnter() {
        setGhostVisibility(4);
        this.mHasStopped = true;
        this.mIsCanceled = true;
        clearState();
        return super.cancelPendingTransitions();
    }

    protected void clearState() {
        this.mSharedElementsBundle = null;
        this.mEnterViewsTransition = null;
        this.mResultReceiver = null;
        if (this.mBackgroundAnimator != null) {
            this.mBackgroundAnimator.cancel();
            this.mBackgroundAnimator = null;
        }
        super.clearState();
    }

    private void makeOpaque() {
        if (!this.mHasStopped && this.mActivity != null) {
            if (this.mWasOpaque) {
                this.mActivity.convertFromTranslucent();
            }
            this.mActivity = null;
        }
    }

    private boolean allowOverlappingTransitions() {
        if (this.mIsReturning) {
            return getWindow().getAllowReturnTransitionOverlap();
        }
        return getWindow().getAllowEnterTransitionOverlap();
    }

    /*  JADX ERROR: NullPointerException in pass: ModVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ModVisitor.getParentInsnSkipMove(ModVisitor.java:320)
        	at jadx.core.dex.visitors.ModVisitor.getArgsToFieldsMapping(ModVisitor.java:294)
        	at jadx.core.dex.visitors.ModVisitor.processAnonymousConstructor(ModVisitor.java:253)
        	at jadx.core.dex.visitors.ModVisitor.processInvoke(ModVisitor.java:235)
        	at jadx.core.dex.visitors.ModVisitor.replaceStep(ModVisitor.java:83)
        	at jadx.core.dex.visitors.ModVisitor.visit(ModVisitor.java:68)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    private void startRejectedAnimations(java.util.ArrayList<android.view.View> r11) {
        /*
        r10 = this;
        if (r11 == 0) goto L_0x0008;
    L_0x0002:
        r6 = r11.isEmpty();
        if (r6 == 0) goto L_0x0009;
    L_0x0008:
        return;
    L_0x0009:
        r1 = r10.getDecor();
        if (r1 == 0) goto L_0x0044;
    L_0x000f:
        r4 = r1.getOverlay();
        r0 = 0;
        r3 = r11.size();
        r2 = 0;
    L_0x0019:
        if (r2 >= r3) goto L_0x003c;
    L_0x001b:
        r5 = r11.get(r2);
        r5 = (android.view.View) r5;
        r4.add(r5);
        r6 = android.view.View.ALPHA;
        r7 = 2;
        r7 = new float[r7];
        r8 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
        r9 = 0;
        r7[r9] = r8;
        r8 = 0;
        r9 = 1;
        r7[r9] = r8;
        r0 = android.animation.ObjectAnimator.ofFloat(r5, r6, r7);
        r0.start();
        r2 = r2 + 1;
        goto L_0x0019;
    L_0x003c:
        r6 = new android.app.EnterTransitionCoordinator$10;
        r6.<init>(r10, r1, r11);
        r0.addListener(r6);
    L_0x0044:
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.EnterTransitionCoordinator.startRejectedAnimations(java.util.ArrayList):void");
    }

    protected void onRemoteExitTransitionComplete() {
        if (!allowOverlappingTransitions()) {
            startEnterTransitionOnly();
        }
    }

    private void startEnterTransitionOnly() {
        startTransition(new AnonymousClass11(this));
    }
}
