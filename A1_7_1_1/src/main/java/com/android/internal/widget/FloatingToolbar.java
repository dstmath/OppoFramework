package com.android.internal.widget;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Property;
import android.util.Size;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.InternalInsetsInfo;
import android.view.ViewTreeObserver.OnComputeInternalInsetsListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.color.util.ColorContextUtil;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public final class FloatingToolbar {
    public static final String FLOATING_TOOLBAR_TAG = "floating_toolbar";
    private static final OnMenuItemClickListener NO_OP_MENUITEM_CLICK_LISTENER = null;
    private final Rect mContentRect;
    private final Context mContext;
    private Menu mMenu;
    private OnMenuItemClickListener mMenuItemClickListener;
    private final OnLayoutChangeListener mOrientationChangeHandler;
    private final ColorFloatingToolbarPopup mPopup;
    private final Rect mPreviousContentRect;
    private List<Object> mShowingMenuItems;
    private int mSuggestedWidth;
    private boolean mWidthChanged;
    private final Window mWindow;

    private static final class FloatingToolbarPopup {
        private static final int MAX_OVERFLOW_SIZE = 4;
        private static final int MIN_OVERFLOW_SIZE = 2;
        private final Drawable mArrow;
        private final AnimationSet mCloseOverflowAnimation;
        private final ViewGroup mContentContainer;
        private final Context mContext;
        private final Point mCoordsOnWindow;
        private final AnimatorSet mDismissAnimation;
        private boolean mDismissed;
        private final Interpolator mFastOutLinearInInterpolator;
        private final Interpolator mFastOutSlowInInterpolator;
        private boolean mHidden;
        private final AnimatorSet mHideAnimation;
        private final OnComputeInternalInsetsListener mInsetsComputer;
        private boolean mIsOverflowOpen;
        private final Interpolator mLinearOutSlowInInterpolator;
        private final Interpolator mLogAccelerateInterpolator;
        private final ViewGroup mMainPanel;
        private Size mMainPanelSize;
        private final int mMarginHorizontal;
        private final int mMarginVertical;
        private final OnClickListener mMenuItemButtonOnClickListener;
        private OnMenuItemClickListener mOnMenuItemClickListener;
        private final AnimationSet mOpenOverflowAnimation;
        private boolean mOpenOverflowUpwards;
        private final Drawable mOverflow;
        private final AnimationListener mOverflowAnimationListener;
        private final ImageButton mOverflowButton;
        private final Size mOverflowButtonSize;
        private final OverflowPanel mOverflowPanel;
        private Size mOverflowPanelSize;
        private final OverflowPanelViewHelper mOverflowPanelViewHelper;
        private final View mParent;
        private final PopupWindow mPopupWindow;
        private final Runnable mPreparePopupContentRTLHelper;
        private final AnimatorSet mShowAnimation;
        private final int[] mTmpCoords;
        private final Rect mTmpRect;
        private final AnimatedVectorDrawable mToArrow;
        private final AnimatedVectorDrawable mToOverflow;
        private final Region mTouchableRegion;
        private int mTransitionDurationScale;
        private final Rect mViewPortOnScreen;

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$10 */
        class AnonymousClass10 extends Animation {
            final /* synthetic */ FloatingToolbarPopup this$1;
            final /* synthetic */ float val$bottom;
            final /* synthetic */ int val$startHeight;
            final /* synthetic */ int val$targetHeight;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.10.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass10(com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup r1, int r2, int r3, float r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.10.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.10.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.10.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            protected void applyTransformation(float r1, android.view.animation.Transformation r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.10.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.10.applyTransformation(float, android.view.animation.Transformation):void");
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$11 */
        class AnonymousClass11 extends Animation {
            final /* synthetic */ FloatingToolbarPopup this$1;
            final /* synthetic */ float val$overflowButtonStartX;
            final /* synthetic */ float val$overflowButtonTargetX;
            final /* synthetic */ int val$startWidth;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.11.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, float, float, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass11(com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup r1, float r2, float r3, int r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.11.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, float, float, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.11.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, float, float, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.11.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            protected void applyTransformation(float r1, android.view.animation.Transformation r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.11.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.11.applyTransformation(float, android.view.animation.Transformation):void");
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$16 */
        class AnonymousClass16 implements AnimationListener {
            final /* synthetic */ FloatingToolbarPopup this$1;

            /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$16$1 */
            class AnonymousClass1 implements Runnable {
                final /* synthetic */ AnonymousClass16 this$2;

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.16.1.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$16):void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 12 more
                    */
                AnonymousClass1(com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.AnonymousClass16 r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.16.1.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$16):void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.16.1.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$16):void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.16.1.run():void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                    	... 12 more
                    */
                public void run() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.16.1.run():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.16.1.run():void");
                }
            }

            AnonymousClass16(FloatingToolbarPopup this$1) {
                this.this$1 = this$1;
            }

            public void onAnimationStart(Animation animation) {
                this.this$1.mOverflowButton.setEnabled(false);
                this.this$1.mMainPanel.setVisibility(0);
                this.this$1.mOverflowPanel.setVisibility(0);
            }

            public void onAnimationEnd(Animation animation) {
                this.this$1.mContentContainer.post(new AnonymousClass1(this));
            }

            public void onAnimationRepeat(Animation animation) {
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$1 */
        class AnonymousClass1 implements OnComputeInternalInsetsListener {
            final /* synthetic */ FloatingToolbarPopup this$1;

            AnonymousClass1(FloatingToolbarPopup this$1) {
                this.this$1 = this$1;
            }

            public void onComputeInternalInsets(InternalInsetsInfo info) {
                info.contentInsets.setEmpty();
                info.visibleInsets.setEmpty();
                info.touchableRegion.set(this.this$1.mTouchableRegion);
                info.setTouchableInsets(3);
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ FloatingToolbarPopup this$1;

            AnonymousClass2(FloatingToolbarPopup this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.setPanelsStatesAtRestingPosition();
                this.this$1.setContentAreaAsTouchableSurface();
                this.this$1.mContentContainer.setAlpha(1.0f);
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$3 */
        class AnonymousClass3 implements OnClickListener {
            final /* synthetic */ FloatingToolbarPopup this$1;

            AnonymousClass3(FloatingToolbarPopup this$1) {
                this.this$1 = this$1;
            }

            public void onClick(View v) {
                if ((v.getTag() instanceof MenuItem) && this.this$1.mOnMenuItemClickListener != null) {
                    this.this$1.mOnMenuItemClickListener.onMenuItemClick((MenuItem) v.getTag());
                }
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$4 */
        class AnonymousClass4 extends AnimatorListenerAdapter {
            final /* synthetic */ FloatingToolbarPopup this$1;

            AnonymousClass4(FloatingToolbarPopup this$1) {
                this.this$1 = this$1;
            }

            public void onAnimationEnd(Animator animation) {
                this.this$1.mPopupWindow.dismiss();
                this.this$1.mContentContainer.removeAllViews();
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$5 */
        class AnonymousClass5 extends AnimatorListenerAdapter {
            final /* synthetic */ FloatingToolbarPopup this$1;

            AnonymousClass5(FloatingToolbarPopup this$1) {
                this.this$1 = this$1;
            }

            public void onAnimationEnd(Animator animation) {
                this.this$1.mPopupWindow.dismiss();
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$6 */
        class AnonymousClass6 extends Animation {
            final /* synthetic */ FloatingToolbarPopup this$1;
            final /* synthetic */ float val$left;
            final /* synthetic */ float val$right;
            final /* synthetic */ int val$startWidth;
            final /* synthetic */ int val$targetWidth;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.6.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float, float):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass6(com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup r1, int r2, int r3, float r4, float r5) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.6.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float, float):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.6.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float, float):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.6.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            protected void applyTransformation(float r1, android.view.animation.Transformation r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.6.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.6.applyTransformation(float, android.view.animation.Transformation):void");
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$7 */
        class AnonymousClass7 extends Animation {
            final /* synthetic */ FloatingToolbarPopup this$1;
            final /* synthetic */ int val$startHeight;
            final /* synthetic */ float val$startY;
            final /* synthetic */ int val$targetHeight;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.7.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass7(com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup r1, int r2, int r3, float r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.7.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.7.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.7.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            protected void applyTransformation(float r1, android.view.animation.Transformation r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.7.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.7.applyTransformation(float, android.view.animation.Transformation):void");
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$8 */
        class AnonymousClass8 extends Animation {
            final /* synthetic */ FloatingToolbarPopup this$1;
            final /* synthetic */ float val$overflowButtonStartX;
            final /* synthetic */ float val$overflowButtonTargetX;
            final /* synthetic */ int val$startWidth;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.8.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, float, float, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass8(com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup r1, float r2, float r3, int r4) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.8.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, float, float, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.8.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, float, float, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.8.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            protected void applyTransformation(float r1, android.view.animation.Transformation r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.8.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.8.applyTransformation(float, android.view.animation.Transformation):void");
            }
        }

        /* renamed from: com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$9 */
        class AnonymousClass9 extends Animation {
            final /* synthetic */ FloatingToolbarPopup this$1;
            final /* synthetic */ float val$left;
            final /* synthetic */ float val$right;
            final /* synthetic */ int val$startWidth;
            final /* synthetic */ int val$targetWidth;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.9.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float, float):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            AnonymousClass9(com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup r1, int r2, int r3, float r4, float r5) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.9.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float, float):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.9.<init>(com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup, int, int, float, float):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.9.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            protected void applyTransformation(float r1, android.view.animation.Transformation r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.9.applyTransformation(float, android.view.animation.Transformation):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.9.applyTransformation(float, android.view.animation.Transformation):void");
            }
        }

        private static final class LogAccelerateInterpolator implements Interpolator {
            private static final int BASE = 100;
            private static final float LOGS_SCALE = 0.0f;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.LogAccelerateInterpolator.<clinit>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            static {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.LogAccelerateInterpolator.<clinit>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.LogAccelerateInterpolator.<clinit>():void");
            }

            /* synthetic */ LogAccelerateInterpolator(LogAccelerateInterpolator logAccelerateInterpolator) {
                this();
            }

            private LogAccelerateInterpolator() {
            }

            private static float computeLog(float t, int base) {
                return (float) (1.0d - Math.pow((double) base, (double) (-t)));
            }

            public float getInterpolation(float t) {
                return 1.0f - (computeLog(1.0f - t, 100) * LOGS_SCALE);
            }
        }

        private static final class OverflowPanel extends ListView {
            private final FloatingToolbarPopup mPopup;

            OverflowPanel(FloatingToolbarPopup popup) {
                super(((FloatingToolbarPopup) Preconditions.checkNotNull(popup)).mContext);
                this.mPopup = popup;
                setScrollBarDefaultDelayBeforeFade(ViewConfiguration.getScrollDefaultDelay() * 3);
                setScrollIndicators(3);
            }

            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(this.mPopup.mOverflowPanelSize.getHeight() - this.mPopup.mOverflowButtonSize.getHeight(), 1073741824));
            }

            public boolean dispatchTouchEvent(MotionEvent ev) {
                if (this.mPopup.isOverflowAnimating()) {
                    return true;
                }
                return super.dispatchTouchEvent(ev);
            }

            protected boolean awakenScrollBars() {
                return super.awakenScrollBars();
            }
        }

        private static final class OverflowPanelViewHelper {
            private static final int NUM_OF_VIEW_TYPES = 2;
            private static final int VIEW_TYPE_ICON_ONLY = 1;
            private static final int VIEW_TYPE_STRING_TITLE = 0;
            private final Context mContext;
            private final View mIconOnlyViewCalculator;
            private final TextView mStringTitleViewCalculator;

            public OverflowPanelViewHelper(Context context) {
                this.mContext = (Context) Preconditions.checkNotNull(context);
                this.mStringTitleViewCalculator = getStringTitleView(null, 0, null);
                this.mIconOnlyViewCalculator = getIconOnlyView(null, 0, null);
            }

            public int getViewTypeCount() {
                return 2;
            }

            public View getView(MenuItem menuItem, int minimumWidth, View convertView) {
                Preconditions.checkNotNull(menuItem);
                if (getItemViewType(menuItem) == 1) {
                    return getIconOnlyView(menuItem, minimumWidth, convertView);
                }
                return getStringTitleView(menuItem, minimumWidth, convertView);
            }

            public int getItemViewType(MenuItem menuItem) {
                Preconditions.checkNotNull(menuItem);
                if (FloatingToolbar.isIconOnlyMenuItem(menuItem)) {
                    return 1;
                }
                return 0;
            }

            public int calculateWidth(MenuItem menuItem) {
                View calculator;
                if (FloatingToolbar.isIconOnlyMenuItem(menuItem)) {
                    ((ImageView) this.mIconOnlyViewCalculator.findViewById(R.id.floating_toolbar_menu_item_image_button)).setImageDrawable(menuItem.getIcon());
                    calculator = this.mIconOnlyViewCalculator;
                } else {
                    this.mStringTitleViewCalculator.setText(menuItem.getTitle());
                    calculator = this.mStringTitleViewCalculator;
                }
                calculator.measure(0, 0);
                return calculator.getMeasuredWidth();
            }

            private TextView getStringTitleView(MenuItem menuItem, int minimumWidth, View convertView) {
                TextView menuButton;
                if (convertView != null) {
                    menuButton = (TextView) convertView;
                } else {
                    menuButton = (TextView) LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_list_item, null);
                    menuButton.setLayoutParams(new LayoutParams(-1, -2));
                }
                if (menuItem != null) {
                    menuButton.setText(menuItem.getTitle());
                    menuButton.setContentDescription(menuItem.getTitle());
                    menuButton.setMinimumWidth(minimumWidth);
                }
                return menuButton;
            }

            private View getIconOnlyView(MenuItem menuItem, int minimumWidth, View convertView) {
                View menuButton;
                if (convertView != null) {
                    menuButton = convertView;
                } else {
                    menuButton = LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_image_list_item, null);
                    menuButton.setLayoutParams(new LayoutParams(-2, -2));
                }
                if (menuItem != null) {
                    ((ImageView) menuButton.findViewById(R.id.floating_toolbar_menu_item_image_button)).setImageDrawable(menuItem.getIcon());
                    menuButton.setMinimumWidth(minimumWidth);
                }
                return menuButton;
            }
        }

        public FloatingToolbarPopup(Context context, View parent) {
            this.mViewPortOnScreen = new Rect();
            this.mCoordsOnWindow = new Point();
            this.mTmpCoords = new int[2];
            this.mTmpRect = new Rect();
            this.mTouchableRegion = new Region();
            this.mInsetsComputer = new AnonymousClass1(this);
            this.mPreparePopupContentRTLHelper = new AnonymousClass2(this);
            this.mDismissed = true;
            this.mMenuItemButtonOnClickListener = new AnonymousClass3(this);
            this.mParent = (View) Preconditions.checkNotNull(parent);
            this.mContext = (Context) Preconditions.checkNotNull(context);
            this.mContentContainer = FloatingToolbar.createContentContainer(context);
            this.mPopupWindow = FloatingToolbar.createPopupWindow(this.mContentContainer);
            this.mMarginHorizontal = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin);
            this.mMarginVertical = parent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_vertical_margin);
            this.mLogAccelerateInterpolator = new LogAccelerateInterpolator();
            this.mFastOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_slow_in);
            this.mLinearOutSlowInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.linear_out_slow_in);
            this.mFastOutLinearInInterpolator = AnimationUtils.loadInterpolator(this.mContext, R.interpolator.fast_out_linear_in);
            this.mArrow = this.mContext.getResources().getDrawable(R.drawable.ft_avd_tooverflow, this.mContext.getTheme());
            this.mArrow.setAutoMirrored(true);
            this.mOverflow = this.mContext.getResources().getDrawable(R.drawable.ft_avd_toarrow, this.mContext.getTheme());
            this.mOverflow.setAutoMirrored(true);
            this.mToArrow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_toarrow_animation, this.mContext.getTheme());
            this.mToArrow.setAutoMirrored(true);
            this.mToOverflow = (AnimatedVectorDrawable) this.mContext.getResources().getDrawable(R.drawable.ft_avd_tooverflow_animation, this.mContext.getTheme());
            this.mToOverflow.setAutoMirrored(true);
            this.mOverflowButton = createOverflowButton();
            this.mOverflowButtonSize = measure(this.mOverflowButton);
            this.mMainPanel = createMainPanel();
            this.mOverflowPanelViewHelper = new OverflowPanelViewHelper(this.mContext);
            this.mOverflowPanel = createOverflowPanel();
            this.mOverflowAnimationListener = createOverflowAnimationListener();
            this.mOpenOverflowAnimation = new AnimationSet(true);
            this.mOpenOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mCloseOverflowAnimation = new AnimationSet(true);
            this.mCloseOverflowAnimation.setAnimationListener(this.mOverflowAnimationListener);
            this.mShowAnimation = FloatingToolbar.createEnterAnimation(this.mContentContainer);
            this.mDismissAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 150, new AnonymousClass4(this));
            this.mHideAnimation = FloatingToolbar.createExitAnimation(this.mContentContainer, 0, new AnonymousClass5(this));
        }

        public void layoutMenuItems(List<MenuItem> menuItems, OnMenuItemClickListener menuItemClickListener, int suggestedWidth) {
            this.mOnMenuItemClickListener = menuItemClickListener;
            cancelOverflowAnimations();
            clearPanels();
            menuItems = layoutMainPanelItems(menuItems, getAdjustedToolbarWidth(suggestedWidth));
            if (!menuItems.isEmpty()) {
                layoutOverflowPanelItems(menuItems);
            }
            updatePopupSize();
        }

        public void show(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (!isShowing()) {
                this.mHidden = false;
                this.mDismissed = false;
                cancelDismissAndHideAnimations();
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.showAtLocation(this.mParent, 0, this.mCoordsOnWindow.x, this.mCoordsOnWindow.y);
                setTouchableSurfaceInsetsComputer();
                runShowAnimation();
            }
        }

        public void dismiss() {
            if (!this.mDismissed) {
                this.mHidden = false;
                this.mDismissed = true;
                this.mHideAnimation.cancel();
                runDismissAnimation();
                setZeroTouchableSurface();
            }
        }

        public void hide() {
            if (isShowing()) {
                this.mHidden = true;
                runHideAnimation();
                setZeroTouchableSurface();
            }
        }

        public boolean isShowing() {
            return (this.mDismissed || this.mHidden) ? false : true;
        }

        public boolean isHidden() {
            return this.mHidden;
        }

        public void updateCoordinates(Rect contentRectOnScreen) {
            Preconditions.checkNotNull(contentRectOnScreen);
            if (isShowing() && this.mPopupWindow.isShowing()) {
                cancelOverflowAnimations();
                refreshCoordinatesAndOverflowDirection(contentRectOnScreen);
                preparePopupContent();
                this.mPopupWindow.update(this.mCoordsOnWindow.x, this.mCoordsOnWindow.y, this.mPopupWindow.getWidth(), this.mPopupWindow.getHeight());
            }
        }

        private void refreshCoordinatesAndOverflowDirection(Rect contentRectOnScreen) {
            int y;
            refreshViewPort();
            int x = Math.min(contentRectOnScreen.centerX() - (this.mPopupWindow.getWidth() / 2), this.mViewPortOnScreen.right - this.mPopupWindow.getWidth());
            int availableHeightAboveContent = contentRectOnScreen.top - this.mViewPortOnScreen.top;
            int availableHeightBelowContent = this.mViewPortOnScreen.bottom - contentRectOnScreen.bottom;
            int margin = this.mMarginVertical * 2;
            int toolbarHeightWithVerticalMargin = getLineHeight(this.mContext) + margin;
            if (hasOverflow()) {
                int minimumOverflowHeightWithMargin = calculateOverflowHeight(2) + margin;
                int availableHeightThroughContentDown = (this.mViewPortOnScreen.bottom - contentRectOnScreen.top) + toolbarHeightWithVerticalMargin;
                int availableHeightThroughContentUp = (contentRectOnScreen.bottom - this.mViewPortOnScreen.top) + toolbarHeightWithVerticalMargin;
                if (availableHeightAboveContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightAboveContent - margin);
                    y = contentRectOnScreen.top - this.mPopupWindow.getHeight();
                    this.mOpenOverflowUpwards = true;
                } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin && availableHeightThroughContentDown >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightThroughContentDown - margin);
                    y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
                    this.mOpenOverflowUpwards = false;
                } else if (availableHeightBelowContent >= minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(availableHeightBelowContent - margin);
                    y = contentRectOnScreen.bottom;
                    this.mOpenOverflowUpwards = false;
                } else if (availableHeightBelowContent < toolbarHeightWithVerticalMargin || this.mViewPortOnScreen.height() < minimumOverflowHeightWithMargin) {
                    updateOverflowHeight(this.mViewPortOnScreen.height() - margin);
                    y = this.mViewPortOnScreen.top;
                    this.mOpenOverflowUpwards = false;
                } else {
                    updateOverflowHeight(availableHeightThroughContentUp - margin);
                    y = (contentRectOnScreen.bottom + toolbarHeightWithVerticalMargin) - this.mPopupWindow.getHeight();
                    this.mOpenOverflowUpwards = true;
                }
            } else if (availableHeightAboveContent >= toolbarHeightWithVerticalMargin) {
                y = contentRectOnScreen.top - toolbarHeightWithVerticalMargin;
            } else if (availableHeightBelowContent >= toolbarHeightWithVerticalMargin) {
                y = contentRectOnScreen.bottom;
            } else if (availableHeightBelowContent >= getLineHeight(this.mContext)) {
                y = contentRectOnScreen.bottom - this.mMarginVertical;
            } else {
                y = Math.max(this.mViewPortOnScreen.top, contentRectOnScreen.top - toolbarHeightWithVerticalMargin);
            }
            this.mParent.getRootView().getLocationOnScreen(this.mTmpCoords);
            int rootViewLeftOnScreen = this.mTmpCoords[0];
            int rootViewTopOnScreen = this.mTmpCoords[1];
            this.mParent.getRootView().getLocationInWindow(this.mTmpCoords);
            this.mCoordsOnWindow.set(Math.max(0, x - (rootViewLeftOnScreen - this.mTmpCoords[0])), Math.max(0, y - (rootViewTopOnScreen - this.mTmpCoords[1])));
        }

        private void runShowAnimation() {
            this.mShowAnimation.start();
        }

        private void runDismissAnimation() {
            this.mDismissAnimation.start();
        }

        private void runHideAnimation() {
            this.mHideAnimation.start();
        }

        private void cancelDismissAndHideAnimations() {
            this.mDismissAnimation.cancel();
            this.mHideAnimation.cancel();
        }

        private void cancelOverflowAnimations() {
            this.mContentContainer.clearAnimation();
            this.mMainPanel.animate().cancel();
            this.mOverflowPanel.animate().cancel();
            this.mToArrow.stop();
            this.mToOverflow.stop();
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        private void openOverflow() {
            /*
            r18 = this;
            r0 = r18;
            r3 = r0.mOverflowPanelSize;
            r4 = r3.getWidth();
            r0 = r18;
            r3 = r0.mOverflowPanelSize;
            r14 = r3.getHeight();
            r0 = r18;
            r3 = r0.mContentContainer;
            r5 = r3.getWidth();
            r0 = r18;
            r3 = r0.mContentContainer;
            r12 = r3.getHeight();
            r0 = r18;
            r3 = r0.mContentContainer;
            r13 = r3.getY();
            r0 = r18;
            r3 = r0.mContentContainer;
            r6 = r3.getX();
            r0 = r18;
            r3 = r0.mContentContainer;
            r3 = r3.getWidth();
            r3 = (float) r3;
            r7 = r6 + r3;
            r2 = new com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$6;
            r3 = r18;
            r2.<init>(r3, r4, r5, r6, r7);
            r8 = new com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$7;
            r0 = r18;
            r8.<init>(r0, r14, r12, r13);
            r0 = r18;
            r3 = r0.mOverflowButton;
            r10 = r3.getX();
            r3 = r18.isInRTLMode();
            if (r3 == 0) goto L_0x0119;
        L_0x0057:
            r3 = (float) r4;
            r3 = r3 + r10;
            r0 = r18;
            r15 = r0.mOverflowButton;
            r15 = r15.getWidth();
            r15 = (float) r15;
            r11 = r3 - r15;
        L_0x0064:
            r9 = new com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$8;
            r0 = r18;
            r9.<init>(r0, r10, r11, r5);
            r0 = r18;
            r3 = r0.mLogAccelerateInterpolator;
            r2.setInterpolator(r3);
            r3 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
            r0 = r18;
            r3 = r0.getAdjustedDuration(r3);
            r0 = (long) r3;
            r16 = r0;
            r0 = r16;
            r2.setDuration(r0);
            r0 = r18;
            r3 = r0.mFastOutSlowInInterpolator;
            r8.setInterpolator(r3);
            r3 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
            r0 = r18;
            r3 = r0.getAdjustedDuration(r3);
            r0 = (long) r3;
            r16 = r0;
            r0 = r16;
            r8.setDuration(r0);
            r0 = r18;
            r3 = r0.mFastOutSlowInInterpolator;
            r9.setInterpolator(r3);
            r3 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
            r0 = r18;
            r3 = r0.getAdjustedDuration(r3);
            r0 = (long) r3;
            r16 = r0;
            r0 = r16;
            r9.setDuration(r0);
            r0 = r18;
            r3 = r0.mOpenOverflowAnimation;
            r3 = r3.getAnimations();
            r3.clear();
            r0 = r18;
            r3 = r0.mOpenOverflowAnimation;
            r3 = r3.getAnimations();
            r3.clear();
            r0 = r18;
            r3 = r0.mOpenOverflowAnimation;
            r3.addAnimation(r2);
            r0 = r18;
            r3 = r0.mOpenOverflowAnimation;
            r3.addAnimation(r8);
            r0 = r18;
            r3 = r0.mOpenOverflowAnimation;
            r3.addAnimation(r9);
            r0 = r18;
            r3 = r0.mContentContainer;
            r0 = r18;
            r15 = r0.mOpenOverflowAnimation;
            r3.startAnimation(r15);
            r3 = 1;
            r0 = r18;
            r0.mIsOverflowOpen = r3;
            r0 = r18;
            r3 = r0.mMainPanel;
            r3 = r3.animate();
            r15 = 0;
            r3 = r3.alpha(r15);
            r3 = r3.withLayer();
            r0 = r18;
            r15 = r0.mLinearOutSlowInInterpolator;
            r3 = r3.setInterpolator(r15);
            r16 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
            r0 = r16;
            r3 = r3.setDuration(r0);
            r3.start();
            r0 = r18;
            r3 = r0.mOverflowPanel;
            r15 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
            r3.setAlpha(r15);
            return;
        L_0x0119:
            r3 = (float) r4;
            r3 = r10 - r3;
            r0 = r18;
            r15 = r0.mOverflowButton;
            r15 = r15.getWidth();
            r15 = (float) r15;
            r11 = r3 + r15;
            goto L_0x0064;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.openOverflow():void");
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
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        private void closeOverflow() {
            /*
            r18 = this;
            r0 = r18;
            r3 = r0.mMainPanelSize;
            r4 = r3.getWidth();
            r0 = r18;
            r3 = r0.mContentContainer;
            r5 = r3.getWidth();
            r0 = r18;
            r3 = r0.mContentContainer;
            r6 = r3.getX();
            r0 = r18;
            r3 = r0.mContentContainer;
            r3 = r3.getWidth();
            r3 = (float) r3;
            r7 = r6 + r3;
            r2 = new com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$9;
            r3 = r18;
            r2.<init>(r3, r4, r5, r6, r7);
            r0 = r18;
            r3 = r0.mMainPanelSize;
            r14 = r3.getHeight();
            r0 = r18;
            r3 = r0.mContentContainer;
            r13 = r3.getHeight();
            r0 = r18;
            r3 = r0.mContentContainer;
            r3 = r3.getY();
            r0 = r18;
            r15 = r0.mContentContainer;
            r15 = r15.getHeight();
            r15 = (float) r15;
            r8 = r3 + r15;
            r9 = new com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$10;
            r0 = r18;
            r9.<init>(r0, r14, r13, r8);
            r0 = r18;
            r3 = r0.mOverflowButton;
            r11 = r3.getX();
            r3 = r18.isInRTLMode();
            if (r3 == 0) goto L_0x0136;
        L_0x0062:
            r3 = (float) r5;
            r3 = r11 - r3;
            r0 = r18;
            r15 = r0.mOverflowButton;
            r15 = r15.getWidth();
            r15 = (float) r15;
            r12 = r3 + r15;
        L_0x0070:
            r10 = new com.android.internal.widget.FloatingToolbar$FloatingToolbarPopup$11;
            r0 = r18;
            r10.<init>(r0, r11, r12, r5);
            r0 = r18;
            r3 = r0.mFastOutSlowInInterpolator;
            r2.setInterpolator(r3);
            r3 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
            r0 = r18;
            r3 = r0.getAdjustedDuration(r3);
            r0 = (long) r3;
            r16 = r0;
            r0 = r16;
            r2.setDuration(r0);
            r0 = r18;
            r3 = r0.mLogAccelerateInterpolator;
            r9.setInterpolator(r3);
            r3 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
            r0 = r18;
            r3 = r0.getAdjustedDuration(r3);
            r0 = (long) r3;
            r16 = r0;
            r0 = r16;
            r9.setDuration(r0);
            r0 = r18;
            r3 = r0.mFastOutSlowInInterpolator;
            r10.setInterpolator(r3);
            r3 = 250; // 0xfa float:3.5E-43 double:1.235E-321;
            r0 = r18;
            r3 = r0.getAdjustedDuration(r3);
            r0 = (long) r3;
            r16 = r0;
            r0 = r16;
            r10.setDuration(r0);
            r0 = r18;
            r3 = r0.mCloseOverflowAnimation;
            r3 = r3.getAnimations();
            r3.clear();
            r0 = r18;
            r3 = r0.mCloseOverflowAnimation;
            r3.addAnimation(r2);
            r0 = r18;
            r3 = r0.mCloseOverflowAnimation;
            r3.addAnimation(r9);
            r0 = r18;
            r3 = r0.mCloseOverflowAnimation;
            r3.addAnimation(r10);
            r0 = r18;
            r3 = r0.mContentContainer;
            r0 = r18;
            r15 = r0.mCloseOverflowAnimation;
            r3.startAnimation(r15);
            r3 = 0;
            r0 = r18;
            r0.mIsOverflowOpen = r3;
            r0 = r18;
            r3 = r0.mMainPanel;
            r3 = r3.animate();
            r15 = 1065353216; // 0x3f800000 float:1.0 double:5.263544247E-315;
            r3 = r3.alpha(r15);
            r3 = r3.withLayer();
            r0 = r18;
            r15 = r0.mFastOutLinearInInterpolator;
            r3 = r3.setInterpolator(r15);
            r16 = 100;
            r0 = r16;
            r3 = r3.setDuration(r0);
            r3.start();
            r0 = r18;
            r3 = r0.mOverflowPanel;
            r3 = r3.animate();
            r15 = 0;
            r3 = r3.alpha(r15);
            r3 = r3.withLayer();
            r0 = r18;
            r15 = r0.mLinearOutSlowInInterpolator;
            r3 = r3.setInterpolator(r15);
            r16 = 150; // 0x96 float:2.1E-43 double:7.4E-322;
            r0 = r16;
            r3 = r3.setDuration(r0);
            r3.start();
            return;
        L_0x0136:
            r3 = (float) r5;
            r3 = r3 + r11;
            r0 = r18;
            r15 = r0.mOverflowButton;
            r15 = r15.getWidth();
            r15 = (float) r15;
            r12 = r3 - r15;
            goto L_0x0070;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.FloatingToolbarPopup.closeOverflow():void");
        }

        private void setPanelsStatesAtRestingPosition() {
            this.mOverflowButton.setEnabled(true);
            this.mOverflowPanel.awakenScrollBars();
            Size containerSize;
            if (this.mIsOverflowOpen) {
                containerSize = this.mOverflowPanelSize;
                setSize(this.mContentContainer, containerSize);
                this.mMainPanel.setAlpha(0.0f);
                this.mMainPanel.setVisibility(4);
                this.mOverflowPanel.setAlpha(1.0f);
                this.mOverflowPanel.setVisibility(0);
                this.mOverflowButton.setImageDrawable(this.mArrow);
                this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_close_overflow_description));
                if (isInRTLMode()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(-this.mContentContainer.getX());
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) this.mMarginVertical);
                    this.mMainPanel.setY((float) (containerSize.getHeight() - this.mContentContainer.getHeight()));
                    this.mOverflowButton.setY((float) (containerSize.getHeight() - this.mOverflowButtonSize.getHeight()));
                    this.mOverflowPanel.setY(0.0f);
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            containerSize = this.mMainPanelSize;
            setSize(this.mContentContainer, containerSize);
            this.mMainPanel.setAlpha(1.0f);
            this.mMainPanel.setVisibility(0);
            this.mOverflowPanel.setAlpha(0.0f);
            this.mOverflowPanel.setVisibility(4);
            this.mOverflowButton.setImageDrawable(this.mOverflow);
            this.mOverflowButton.setContentDescription(this.mContext.getString(R.string.floating_toolbar_open_overflow_description));
            if (hasOverflow()) {
                if (isInRTLMode()) {
                    this.mContentContainer.setX((float) this.mMarginHorizontal);
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX(0.0f);
                    this.mOverflowPanel.setX(0.0f);
                } else {
                    this.mContentContainer.setX((float) ((this.mPopupWindow.getWidth() - containerSize.getWidth()) - this.mMarginHorizontal));
                    this.mMainPanel.setX(0.0f);
                    this.mOverflowButton.setX((float) (containerSize.getWidth() - this.mOverflowButtonSize.getWidth()));
                    this.mOverflowPanel.setX((float) (containerSize.getWidth() - this.mOverflowPanelSize.getWidth()));
                }
                if (this.mOpenOverflowUpwards) {
                    this.mContentContainer.setY((float) ((this.mMarginVertical + this.mOverflowPanelSize.getHeight()) - containerSize.getHeight()));
                    this.mMainPanel.setY(0.0f);
                    this.mOverflowButton.setY(0.0f);
                    this.mOverflowPanel.setY((float) (containerSize.getHeight() - this.mOverflowPanelSize.getHeight()));
                    return;
                }
                this.mContentContainer.setY((float) this.mMarginVertical);
                this.mMainPanel.setY(0.0f);
                this.mOverflowButton.setY(0.0f);
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
                return;
            }
            this.mContentContainer.setX((float) this.mMarginHorizontal);
            this.mContentContainer.setY((float) this.mMarginVertical);
            this.mMainPanel.setX(0.0f);
            this.mMainPanel.setY(0.0f);
        }

        private void updateOverflowHeight(int suggestedHeight) {
            if (hasOverflow()) {
                int newHeight = calculateOverflowHeight((suggestedHeight - this.mOverflowButtonSize.getHeight()) / getLineHeight(this.mContext));
                if (this.mOverflowPanelSize.getHeight() != newHeight) {
                    this.mOverflowPanelSize = new Size(this.mOverflowPanelSize.getWidth(), newHeight);
                }
                setSize(this.mOverflowPanel, this.mOverflowPanelSize);
                if (this.mIsOverflowOpen) {
                    setSize(this.mContentContainer, this.mOverflowPanelSize);
                    if (this.mOpenOverflowUpwards) {
                        int deltaHeight = this.mOverflowPanelSize.getHeight() - newHeight;
                        this.mContentContainer.setY(this.mContentContainer.getY() + ((float) deltaHeight));
                        this.mOverflowButton.setY(this.mOverflowButton.getY() - ((float) deltaHeight));
                    }
                } else {
                    setSize(this.mContentContainer, this.mMainPanelSize);
                }
                updatePopupSize();
            }
        }

        private void updatePopupSize() {
            int width = 0;
            int height = 0;
            if (this.mMainPanelSize != null) {
                width = Math.max(0, this.mMainPanelSize.getWidth());
                height = Math.max(0, this.mMainPanelSize.getHeight());
            }
            if (this.mOverflowPanelSize != null) {
                width = Math.max(width, this.mOverflowPanelSize.getWidth());
                height = Math.max(height, this.mOverflowPanelSize.getHeight());
            }
            this.mPopupWindow.setWidth((this.mMarginHorizontal * 2) + width);
            this.mPopupWindow.setHeight((this.mMarginVertical * 2) + height);
            maybeComputeTransitionDurationScale();
        }

        private void refreshViewPort() {
            this.mParent.getWindowVisibleDisplayFrame(this.mViewPortOnScreen);
        }

        private int getAdjustedToolbarWidth(int suggestedWidth) {
            int width = suggestedWidth;
            refreshViewPort();
            int maximumWidth = this.mViewPortOnScreen.width() - (this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_horizontal_margin) * 2);
            if (suggestedWidth <= 0) {
                width = this.mParent.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_preferred_width);
            }
            return Math.min(width, maximumWidth);
        }

        private void setZeroTouchableSurface() {
            this.mTouchableRegion.setEmpty();
        }

        private void setContentAreaAsTouchableSurface() {
            int width;
            int height;
            Preconditions.checkNotNull(this.mMainPanelSize);
            if (this.mIsOverflowOpen) {
                Preconditions.checkNotNull(this.mOverflowPanelSize);
                width = this.mOverflowPanelSize.getWidth();
                height = this.mOverflowPanelSize.getHeight();
            } else {
                width = this.mMainPanelSize.getWidth();
                height = this.mMainPanelSize.getHeight();
            }
            this.mTouchableRegion.set((int) this.mContentContainer.getX(), (int) this.mContentContainer.getY(), ((int) this.mContentContainer.getX()) + width, ((int) this.mContentContainer.getY()) + height);
        }

        private void setTouchableSurfaceInsetsComputer() {
            ViewTreeObserver viewTreeObserver = this.mPopupWindow.getContentView().getRootView().getViewTreeObserver();
            viewTreeObserver.removeOnComputeInternalInsetsListener(this.mInsetsComputer);
            viewTreeObserver.addOnComputeInternalInsetsListener(this.mInsetsComputer);
        }

        private boolean isInRTLMode() {
            if (this.mContext.getApplicationInfo().hasRtlSupport()) {
                return this.mContext.getResources().getConfiguration().getLayoutDirection() == 1;
            } else {
                return false;
            }
        }

        private boolean hasOverflow() {
            return this.mOverflowPanelSize != null;
        }

        public List<MenuItem> layoutMainPanelItems(List<MenuItem> menuItems, int toolbarWidth) {
            Preconditions.checkNotNull(menuItems);
            int availableWidth = toolbarWidth;
            LinkedList<MenuItem> remainingMenuItems = new LinkedList(menuItems);
            this.mMainPanel.removeAllViews();
            this.mMainPanel.setPaddingRelative(0, 0, 0, 0);
            boolean isFirstItem = true;
            while (!remainingMenuItems.isEmpty()) {
                MenuItem menuItem = (MenuItem) remainingMenuItems.peek();
                View menuItemButton = FloatingToolbar.createMenuItemButton(this.mContext, menuItem);
                if (isFirstItem) {
                    menuItemButton.setPaddingRelative((int) (((double) menuItemButton.getPaddingStart()) * 1.5d), menuItemButton.getPaddingTop(), menuItemButton.getPaddingEnd(), menuItemButton.getPaddingBottom());
                    isFirstItem = false;
                }
                if (remainingMenuItems.size() == 1) {
                    menuItemButton.setPaddingRelative(menuItemButton.getPaddingStart(), menuItemButton.getPaddingTop(), (int) (((double) menuItemButton.getPaddingEnd()) * 1.5d), menuItemButton.getPaddingBottom());
                }
                menuItemButton.measure(0, 0);
                int menuItemButtonWidth = Math.min(menuItemButton.getMeasuredWidth(), toolbarWidth);
                boolean canFitWithOverflow = menuItemButtonWidth <= availableWidth - this.mOverflowButtonSize.getWidth();
                boolean canFitNoOverflow = remainingMenuItems.size() == 1 && menuItemButtonWidth <= availableWidth;
                if (!canFitWithOverflow && !canFitNoOverflow) {
                    this.mMainPanel.setPaddingRelative(0, 0, this.mOverflowButtonSize.getWidth(), 0);
                    break;
                }
                setButtonTagAndClickListener(menuItemButton, menuItem);
                this.mMainPanel.addView(menuItemButton);
                LayoutParams params = menuItemButton.getLayoutParams();
                params.width = menuItemButtonWidth;
                menuItemButton.setLayoutParams(params);
                availableWidth -= menuItemButtonWidth;
                remainingMenuItems.pop();
            }
            this.mMainPanelSize = measure(this.mMainPanel);
            return remainingMenuItems;
        }

        private void layoutOverflowPanelItems(List<MenuItem> menuItems) {
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            int size = menuItems.size();
            for (int i = 0; i < size; i++) {
                overflowPanelAdapter.add((MenuItem) menuItems.get(i));
            }
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            if (this.mOpenOverflowUpwards) {
                this.mOverflowPanel.setY(0.0f);
            } else {
                this.mOverflowPanel.setY((float) this.mOverflowButtonSize.getHeight());
            }
            this.mOverflowPanelSize = new Size(Math.max(getOverflowWidth(), this.mOverflowButtonSize.getWidth()), calculateOverflowHeight(4));
            setSize(this.mOverflowPanel, this.mOverflowPanelSize);
        }

        private void preparePopupContent() {
            this.mContentContainer.removeAllViews();
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowPanel);
            }
            this.mContentContainer.addView(this.mMainPanel);
            if (hasOverflow()) {
                this.mContentContainer.addView(this.mOverflowButton);
            }
            setPanelsStatesAtRestingPosition();
            setContentAreaAsTouchableSurface();
            if (isInRTLMode()) {
                this.mContentContainer.setAlpha(0.0f);
                this.mContentContainer.post(this.mPreparePopupContentRTLHelper);
            }
        }

        private void clearPanels() {
            this.mOverflowPanelSize = null;
            this.mMainPanelSize = null;
            this.mIsOverflowOpen = false;
            this.mMainPanel.removeAllViews();
            ArrayAdapter<MenuItem> overflowPanelAdapter = (ArrayAdapter) this.mOverflowPanel.getAdapter();
            overflowPanelAdapter.clear();
            this.mOverflowPanel.setAdapter(overflowPanelAdapter);
            this.mContentContainer.removeAllViews();
        }

        private void positionContentYCoordinatesIfOpeningOverflowUpwards() {
            if (this.mOpenOverflowUpwards) {
                this.mMainPanel.setY((float) (this.mContentContainer.getHeight() - this.mMainPanelSize.getHeight()));
                this.mOverflowButton.setY((float) (this.mContentContainer.getHeight() - this.mOverflowButton.getHeight()));
                this.mOverflowPanel.setY((float) (this.mContentContainer.getHeight() - this.mOverflowPanelSize.getHeight()));
            }
        }

        private int getOverflowWidth() {
            int overflowWidth = 0;
            int count = this.mOverflowPanel.getAdapter().getCount();
            for (int i = 0; i < count; i++) {
                overflowWidth = Math.max(this.mOverflowPanelViewHelper.calculateWidth((MenuItem) this.mOverflowPanel.getAdapter().getItem(i)), overflowWidth);
            }
            return overflowWidth;
        }

        private int calculateOverflowHeight(int maxItemSize) {
            int actualSize = Math.min(4, Math.min(Math.max(2, maxItemSize), this.mOverflowPanel.getCount()));
            int extension = 0;
            if (actualSize < this.mOverflowPanel.getCount()) {
                extension = (int) (((float) getLineHeight(this.mContext)) * 0.5f);
            }
            return ((getLineHeight(this.mContext) * actualSize) + this.mOverflowButtonSize.getHeight()) + extension;
        }

        private void setButtonTagAndClickListener(View menuItemButton, MenuItem menuItem) {
            View button = menuItemButton;
            if (FloatingToolbar.isIconOnlyMenuItem(menuItem)) {
                button = menuItemButton.findViewById(R.id.floating_toolbar_menu_item_image_button);
            }
            button.setTag(menuItem);
            button.setOnClickListener(this.mMenuItemButtonOnClickListener);
        }

        private int getAdjustedDuration(int originalDuration) {
            if (this.mTransitionDurationScale < 150) {
                return Math.max(originalDuration - 50, 0);
            }
            if (this.mTransitionDurationScale > 300) {
                return originalDuration + 50;
            }
            return (int) (((float) originalDuration) * ValueAnimator.getDurationScale());
        }

        private void maybeComputeTransitionDurationScale() {
            if (this.mMainPanelSize != null && this.mOverflowPanelSize != null) {
                int w = this.mMainPanelSize.getWidth() - this.mOverflowPanelSize.getWidth();
                int h = this.mOverflowPanelSize.getHeight() - this.mMainPanelSize.getHeight();
                this.mTransitionDurationScale = (int) (Math.sqrt((double) ((w * w) + (h * h))) / ((double) this.mContentContainer.getContext().getResources().getDisplayMetrics().density));
            }
        }

        private ViewGroup createMainPanel() {
            return new LinearLayout(this, this.mContext) {
                final /* synthetic */ FloatingToolbarPopup this$1;

                protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                    if (this.this$1.isOverflowAnimating()) {
                        widthMeasureSpec = MeasureSpec.makeMeasureSpec(this.this$1.mMainPanelSize.getWidth(), 1073741824);
                    }
                    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
                }

                public boolean onInterceptTouchEvent(MotionEvent ev) {
                    return this.this$1.isOverflowAnimating();
                }
            };
        }

        private ImageButton createOverflowButton() {
            final ImageButton overflowButton = (ImageButton) LayoutInflater.from(this.mContext).inflate((int) R.layout.floating_popup_overflow_button, null);
            overflowButton.setImageDrawable(this.mOverflow);
            overflowButton.setOnClickListener(new OnClickListener(this) {
                final /* synthetic */ FloatingToolbarPopup this$1;

                public void onClick(View v) {
                    if (this.this$1.mIsOverflowOpen) {
                        overflowButton.setImageDrawable(this.this$1.mToOverflow);
                        this.this$1.mToOverflow.start();
                        this.this$1.closeOverflow();
                        return;
                    }
                    overflowButton.setImageDrawable(this.this$1.mToArrow);
                    this.this$1.mToArrow.start();
                    this.this$1.openOverflow();
                }
            });
            return overflowButton;
        }

        private OverflowPanel createOverflowPanel() {
            final OverflowPanel overflowPanel = new OverflowPanel(this);
            overflowPanel.setLayoutParams(new LayoutParams(-1, -1));
            overflowPanel.setDivider(null);
            overflowPanel.setDividerHeight(0);
            overflowPanel.setAdapter(new ArrayAdapter<MenuItem>(this, this.mContext, 0) {
                final /* synthetic */ FloatingToolbarPopup this$1;

                public int getViewTypeCount() {
                    return this.this$1.mOverflowPanelViewHelper.getViewTypeCount();
                }

                public int getItemViewType(int position) {
                    return this.this$1.mOverflowPanelViewHelper.getItemViewType((MenuItem) getItem(position));
                }

                public View getView(int position, View convertView, ViewGroup parent) {
                    return this.this$1.mOverflowPanelViewHelper.getView((MenuItem) getItem(position), this.this$1.mOverflowPanelSize.getWidth(), convertView);
                }
            });
            overflowPanel.setOnItemClickListener(new OnItemClickListener(this) {
                final /* synthetic */ FloatingToolbarPopup this$1;

                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    MenuItem menuItem = (MenuItem) overflowPanel.getAdapter().getItem(position);
                    if (this.this$1.mOnMenuItemClickListener != null) {
                        this.this$1.mOnMenuItemClickListener.onMenuItemClick(menuItem);
                    }
                }
            });
            return overflowPanel;
        }

        private boolean isOverflowAnimating() {
            boolean overflowOpening = this.mOpenOverflowAnimation.hasStarted() ? !this.mOpenOverflowAnimation.hasEnded() : false;
            boolean overflowClosing = this.mCloseOverflowAnimation.hasStarted() ? !this.mCloseOverflowAnimation.hasEnded() : false;
            return !overflowOpening ? overflowClosing : true;
        }

        private AnimationListener createOverflowAnimationListener() {
            return new AnonymousClass16(this);
        }

        private static Size measure(View view) {
            boolean z;
            if (view.getParent() == null) {
                z = true;
            } else {
                z = false;
            }
            Preconditions.checkState(z);
            view.measure(0, 0);
            return new Size(view.getMeasuredWidth(), view.getMeasuredHeight());
        }

        private static void setSize(View view, int width, int height) {
            view.setMinimumWidth(width);
            view.setMinimumHeight(height);
            LayoutParams params = view.getLayoutParams();
            if (params == null) {
                params = new LayoutParams(0, 0);
            }
            params.width = width;
            params.height = height;
            view.setLayoutParams(params);
        }

        private static void setSize(View view, Size size) {
            setSize(view, size.getWidth(), size.getHeight());
        }

        private static void setWidth(View view, int width) {
            setSize(view, width, view.getLayoutParams().height);
        }

        private static void setHeight(View view, int height) {
            setSize(view, view.getLayoutParams().width, height);
        }

        private static int getLineHeight(Context context) {
            return context.getResources().getDimensionPixelSize(R.dimen.floating_toolbar_height);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.FloatingToolbar.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.widget.FloatingToolbar.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.widget.FloatingToolbar.<clinit>():void");
    }

    public FloatingToolbar(Context context, Window window) {
        this.mContentRect = new Rect();
        this.mPreviousContentRect = new Rect();
        this.mShowingMenuItems = new ArrayList();
        this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        this.mWidthChanged = true;
        this.mOrientationChangeHandler = new OnLayoutChangeListener() {
            private final Rect mNewRect = new Rect();
            private final Rect mOldRect = new Rect();

            public void onLayoutChange(View view, int newLeft, int newRight, int newTop, int newBottom, int oldLeft, int oldRight, int oldTop, int oldBottom) {
                this.mNewRect.set(newLeft, newRight, newTop, newBottom);
                this.mOldRect.set(oldLeft, oldRight, oldTop, oldBottom);
                if (FloatingToolbar.this.mPopup.isShowing() && !this.mNewRect.equals(this.mOldRect)) {
                    FloatingToolbar.this.mWidthChanged = true;
                    FloatingToolbar.this.updateLayout();
                }
            }
        };
        this.mContext = applyDefaultTheme((Context) Preconditions.checkNotNull(context));
        this.mWindow = (Window) Preconditions.checkNotNull(window);
        this.mPopup = new ColorFloatingToolbarPopup(this.mContext, window.getDecorView());
    }

    public FloatingToolbar setMenu(Menu menu) {
        this.mMenu = (Menu) Preconditions.checkNotNull(menu);
        return this;
    }

    public FloatingToolbar setOnMenuItemClickListener(OnMenuItemClickListener menuItemClickListener) {
        if (menuItemClickListener != null) {
            this.mMenuItemClickListener = menuItemClickListener;
        } else {
            this.mMenuItemClickListener = NO_OP_MENUITEM_CLICK_LISTENER;
        }
        return this;
    }

    public FloatingToolbar setContentRect(Rect rect) {
        this.mContentRect.set((Rect) Preconditions.checkNotNull(rect));
        return this;
    }

    public FloatingToolbar setSuggestedWidth(int suggestedWidth) {
        this.mWidthChanged = ((double) Math.abs(suggestedWidth - this.mSuggestedWidth)) > ((double) this.mSuggestedWidth) * 0.2d;
        this.mSuggestedWidth = suggestedWidth;
        return this;
    }

    public FloatingToolbar show() {
        registerOrientationHandler();
        doShow();
        return this;
    }

    public FloatingToolbar updateLayout() {
        if (this.mPopup.isShowing()) {
            doShow();
        }
        return this;
    }

    public void dismiss() {
        unregisterOrientationHandler();
        this.mPopup.dismiss();
    }

    public void hide() {
        this.mPopup.hide();
    }

    public boolean isShowing() {
        return this.mPopup.isShowing();
    }

    public boolean isHidden() {
        return this.mPopup.isHidden();
    }

    private void doShow() {
        List<MenuItem> menuItems = getVisibleAndEnabledMenuItems(this.mMenu);
        if (!isCurrentlyShowing(menuItems) || this.mWidthChanged) {
            this.mPopup.dismiss();
            this.mPopup.layoutMenuItems(menuItems, this.mMenuItemClickListener, this.mSuggestedWidth);
            this.mShowingMenuItems = getShowingMenuItemsReferences(menuItems);
        }
        if (!this.mPopup.isShowing()) {
            this.mPopup.show(this.mContentRect);
        } else if (!this.mPreviousContentRect.equals(this.mContentRect)) {
            this.mPopup.updateCoordinates(this.mContentRect);
        }
        this.mWidthChanged = false;
        this.mPreviousContentRect.set(this.mContentRect);
    }

    private boolean isCurrentlyShowing(List<MenuItem> menuItems) {
        return this.mShowingMenuItems.equals(getShowingMenuItemsReferences(menuItems));
    }

    private List<MenuItem> getVisibleAndEnabledMenuItems(Menu menu) {
        List<MenuItem> menuItems = new ArrayList();
        int i = 0;
        while (menu != null && i < menu.size()) {
            MenuItem menuItem = menu.getItem(i);
            if (menuItem.isVisible() && menuItem.isEnabled()) {
                Menu subMenu = menuItem.getSubMenu();
                if (subMenu != null) {
                    menuItems.addAll(getVisibleAndEnabledMenuItems(subMenu));
                } else {
                    menuItems.add(menuItem);
                }
            }
            i++;
        }
        return menuItems;
    }

    private List<Object> getShowingMenuItemsReferences(List<MenuItem> menuItems) {
        List<Object> references = new ArrayList();
        for (MenuItem menuItem : menuItems) {
            if (isIconOnlyMenuItem(menuItem)) {
                references.add(menuItem.getIcon());
            } else {
                references.add(menuItem.getTitle());
            }
        }
        return references;
    }

    private void registerOrientationHandler() {
        unregisterOrientationHandler();
        this.mWindow.getDecorView().addOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private void unregisterOrientationHandler() {
        this.mWindow.getDecorView().removeOnLayoutChangeListener(this.mOrientationChangeHandler);
    }

    private static boolean isIconOnlyMenuItem(MenuItem menuItem) {
        if (!TextUtils.isEmpty(menuItem.getTitle()) || menuItem.getIcon() == null) {
            return false;
        }
        return true;
    }

    private static View createMenuItemButton(Context context, MenuItem menuItem) {
        if (isIconOnlyMenuItem(menuItem)) {
            View imageMenuItemButton = LayoutInflater.from(context).inflate((int) R.layout.floating_popup_menu_image_button, null);
            ((ImageButton) imageMenuItemButton.findViewById(R.id.floating_toolbar_menu_item_image_button)).setImageDrawable(menuItem.getIcon());
            return imageMenuItemButton;
        }
        Button menuItemButton = (Button) LayoutInflater.from(context).inflate((int) R.layout.floating_popup_menu_button, null);
        menuItemButton.setText(menuItem.getTitle());
        menuItemButton.setContentDescription(menuItem.getTitle());
        return menuItemButton;
    }

    private static ViewGroup createContentContainer(Context context) {
        ViewGroup contentContainer = (ViewGroup) LayoutInflater.from(context).inflate((int) R.layout.floating_popup_container, null);
        contentContainer.setLayoutParams(new LayoutParams(-2, -2));
        contentContainer.setTag("floating_toolbar");
        return contentContainer;
    }

    private static PopupWindow createPopupWindow(ViewGroup content) {
        View popupContentHolder = new LinearLayout(content.getContext());
        PopupWindow popupWindow = new PopupWindow(popupContentHolder);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWindowLayoutType(1005);
        popupWindow.setAnimationStyle(0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(0));
        content.setLayoutParams(new LayoutParams(-2, -2));
        popupContentHolder.addView(content);
        return popupWindow;
    }

    private static AnimatorSet createEnterAnimation(View view) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        Property property = View.ALPHA;
        float[] fArr = new float[2];
        fArr[0] = 0.0f;
        fArr[1] = 1.0f;
        animatorArr[0] = ObjectAnimator.ofFloat(view, property, fArr).setDuration(150);
        animation.playTogether(animatorArr);
        return animation;
    }

    private static AnimatorSet createExitAnimation(View view, int startDelay, AnimatorListener listener) {
        AnimatorSet animation = new AnimatorSet();
        Animator[] animatorArr = new Animator[1];
        Property property = View.ALPHA;
        float[] fArr = new float[2];
        fArr[0] = 1.0f;
        fArr[1] = 0.0f;
        animatorArr[0] = ObjectAnimator.ofFloat(view, property, fArr).setDuration(100);
        animation.playTogether(animatorArr);
        animation.setStartDelay((long) startDelay);
        animation.addListener(listener);
        return animation;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "Jianhua.Lin@Plf.SDK : Modify for color style", property = OppoRomType.ROM)
    private static Context applyDefaultTheme(Context originalContext) {
        int[] iArr = new int[1];
        iArr[0] = R.attr.isLightTheme;
        TypedArray a = originalContext.obtainStyledAttributes(iArr);
        if (a.getBoolean(0, true)) {
        }
        a.recycle();
        return new ContextThemeWrapper(originalContext, getThemeId(originalContext));
    }

    @OppoHook(level = OppoHookType.NEW_METHOD, note = "YuJun.Feng@Plf.SDK : Modify for color style", property = OppoRomType.ROM)
    private static int getThemeId(Context context) {
        if (ColorContextUtil.isOppoStyle(context)) {
            return context.getThemeResId();
        }
        return 201523202;
    }
}
