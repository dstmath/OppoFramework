package android.transition;

import android.animation.AnimatorListenerAdapter;
import android.animation.RectEvaluator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;

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
public class ChangeBounds extends Transition {
    private static final Property<View, PointF> BOTTOM_RIGHT_ONLY_PROPERTY = null;
    private static final Property<ViewBounds, PointF> BOTTOM_RIGHT_PROPERTY = null;
    private static final Property<Drawable, PointF> DRAWABLE_ORIGIN_PROPERTY = null;
    private static final String LOG_TAG = "ChangeBounds";
    private static final Property<View, PointF> POSITION_PROPERTY = null;
    private static final String PROPNAME_BOUNDS = "android:changeBounds:bounds";
    private static final String PROPNAME_CLIP = "android:changeBounds:clip";
    private static final String PROPNAME_PARENT = "android:changeBounds:parent";
    private static final String PROPNAME_WINDOW_X = "android:changeBounds:windowX";
    private static final String PROPNAME_WINDOW_Y = "android:changeBounds:windowY";
    private static final Property<View, PointF> TOP_LEFT_ONLY_PROPERTY = null;
    private static final Property<ViewBounds, PointF> TOP_LEFT_PROPERTY = null;
    private static RectEvaluator sRectEvaluator;
    private static final String[] sTransitionProperties = null;
    boolean mReparent;
    boolean mResizeClip;
    int[] tempLocation;

    /* renamed from: android.transition.ChangeBounds$10 */
    class AnonymousClass10 extends AnimatorListenerAdapter {
        final /* synthetic */ ChangeBounds this$0;
        final /* synthetic */ BitmapDrawable val$drawable;
        final /* synthetic */ ViewGroup val$sceneRoot;
        final /* synthetic */ float val$transitionAlpha;
        final /* synthetic */ View val$view;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.transition.ChangeBounds.10.<init>(android.transition.ChangeBounds, android.view.ViewGroup, android.graphics.drawable.BitmapDrawable, android.view.View, float):void, dex: 
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
        AnonymousClass10(android.transition.ChangeBounds r1, android.view.ViewGroup r2, android.graphics.drawable.BitmapDrawable r3, android.view.View r4, float r5) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.transition.ChangeBounds.10.<init>(android.transition.ChangeBounds, android.view.ViewGroup, android.graphics.drawable.BitmapDrawable, android.view.View, float):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.10.<init>(android.transition.ChangeBounds, android.view.ViewGroup, android.graphics.drawable.BitmapDrawable, android.view.View, float):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.transition.ChangeBounds.10.onAnimationEnd(android.animation.Animator):void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.transition.ChangeBounds.10.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.10.onAnimationEnd(android.animation.Animator):void");
        }
    }

    /* renamed from: android.transition.ChangeBounds$1 */
    static class AnonymousClass1 extends Property<Drawable, PointF> {
        private Rect mBounds;

        AnonymousClass1(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
            this.mBounds = new Rect();
        }

        public /* bridge */ /* synthetic */ void set(Object object, Object value) {
            set((Drawable) object, (PointF) value);
        }

        public void set(Drawable object, PointF value) {
            object.copyBounds(this.mBounds);
            this.mBounds.offsetTo(Math.round(value.x), Math.round(value.y));
            object.setBounds(this.mBounds);
        }

        public /* bridge */ /* synthetic */ Object get(Object object) {
            return get((Drawable) object);
        }

        public PointF get(Drawable object) {
            object.copyBounds(this.mBounds);
            return new PointF((float) this.mBounds.left, (float) this.mBounds.top);
        }
    }

    /* renamed from: android.transition.ChangeBounds$2 */
    static class AnonymousClass2 extends Property<ViewBounds, PointF> {
        AnonymousClass2(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public /* bridge */ /* synthetic */ void set(Object viewBounds, Object topLeft) {
            set((ViewBounds) viewBounds, (PointF) topLeft);
        }

        public void set(ViewBounds viewBounds, PointF topLeft) {
            viewBounds.setTopLeft(topLeft);
        }

        public /* bridge */ /* synthetic */ Object get(Object viewBounds) {
            return get((ViewBounds) viewBounds);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds$3 */
    static class AnonymousClass3 extends Property<ViewBounds, PointF> {
        AnonymousClass3(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public /* bridge */ /* synthetic */ void set(Object viewBounds, Object bottomRight) {
            set((ViewBounds) viewBounds, (PointF) bottomRight);
        }

        public void set(ViewBounds viewBounds, PointF bottomRight) {
            viewBounds.setBottomRight(bottomRight);
        }

        public /* bridge */ /* synthetic */ Object get(Object viewBounds) {
            return get((ViewBounds) viewBounds);
        }

        public PointF get(ViewBounds viewBounds) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds$4 */
    static class AnonymousClass4 extends Property<View, PointF> {
        AnonymousClass4(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public /* bridge */ /* synthetic */ void set(Object view, Object bottomRight) {
            set((View) view, (PointF) bottomRight);
        }

        public void set(View view, PointF bottomRight) {
            view.setLeftTopRightBottom(view.getLeft(), view.getTop(), Math.round(bottomRight.x), Math.round(bottomRight.y));
        }

        public /* bridge */ /* synthetic */ Object get(Object view) {
            return get((View) view);
        }

        public PointF get(View view) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds$5 */
    static class AnonymousClass5 extends Property<View, PointF> {
        AnonymousClass5(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public /* bridge */ /* synthetic */ void set(Object view, Object topLeft) {
            set((View) view, (PointF) topLeft);
        }

        public void set(View view, PointF topLeft) {
            view.setLeftTopRightBottom(Math.round(topLeft.x), Math.round(topLeft.y), view.getRight(), view.getBottom());
        }

        public /* bridge */ /* synthetic */ Object get(Object view) {
            return get((View) view);
        }

        public PointF get(View view) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds$6 */
    static class AnonymousClass6 extends Property<View, PointF> {
        AnonymousClass6(Class $anonymous0, String $anonymous1) {
            super($anonymous0, $anonymous1);
        }

        public /* bridge */ /* synthetic */ void set(Object view, Object topLeft) {
            set((View) view, (PointF) topLeft);
        }

        public void set(View view, PointF topLeft) {
            int left = Math.round(topLeft.x);
            int top = Math.round(topLeft.y);
            view.setLeftTopRightBottom(left, top, left + view.getWidth(), top + view.getHeight());
        }

        public /* bridge */ /* synthetic */ Object get(Object view) {
            return get((View) view);
        }

        public PointF get(View view) {
            return null;
        }
    }

    /* renamed from: android.transition.ChangeBounds$8 */
    class AnonymousClass8 extends AnimatorListenerAdapter {
        private boolean mIsCanceled;
        final /* synthetic */ ChangeBounds this$0;
        final /* synthetic */ int val$endBottom;
        final /* synthetic */ int val$endLeft;
        final /* synthetic */ int val$endRight;
        final /* synthetic */ int val$endTop;
        final /* synthetic */ Rect val$finalClip;
        final /* synthetic */ View val$view;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.transition.ChangeBounds.8.<init>(android.transition.ChangeBounds, android.view.View, android.graphics.Rect, int, int, int, int):void, dex: 
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
        AnonymousClass8(android.transition.ChangeBounds r1, android.view.View r2, android.graphics.Rect r3, int r4, int r5, int r6, int r7) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.transition.ChangeBounds.8.<init>(android.transition.ChangeBounds, android.view.View, android.graphics.Rect, int, int, int, int):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.8.<init>(android.transition.ChangeBounds, android.view.View, android.graphics.Rect, int, int, int, int):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.transition.ChangeBounds.8.onAnimationCancel(android.animation.Animator):void, dex:  in method: android.transition.ChangeBounds.8.onAnimationCancel(android.animation.Animator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.transition.ChangeBounds.8.onAnimationCancel(android.animation.Animator):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void onAnimationCancel(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.transition.ChangeBounds.8.onAnimationCancel(android.animation.Animator):void, dex:  in method: android.transition.ChangeBounds.8.onAnimationCancel(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.8.onAnimationCancel(android.animation.Animator):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.transition.ChangeBounds.8.onAnimationEnd(android.animation.Animator):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 6 more
            */
        public void onAnimationEnd(android.animation.Animator r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.transition.ChangeBounds.8.onAnimationEnd(android.animation.Animator):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.8.onAnimationEnd(android.animation.Animator):void");
        }
    }

    private static class ViewBounds {
        private int mBottom;
        private boolean mIsBottomRightSet;
        private boolean mIsTopLeftSet;
        private int mLeft;
        private int mRight;
        private int mTop;
        private View mView;

        public ViewBounds(View view) {
            this.mView = view;
        }

        public void setTopLeft(PointF topLeft) {
            this.mLeft = Math.round(topLeft.x);
            this.mTop = Math.round(topLeft.y);
            this.mIsTopLeftSet = true;
            if (this.mIsBottomRightSet) {
                setLeftTopRightBottom();
            }
        }

        public void setBottomRight(PointF bottomRight) {
            this.mRight = Math.round(bottomRight.x);
            this.mBottom = Math.round(bottomRight.y);
            this.mIsBottomRightSet = true;
            if (this.mIsTopLeftSet) {
                setLeftTopRightBottom();
            }
        }

        private void setLeftTopRightBottom() {
            this.mView.setLeftTopRightBottom(this.mLeft, this.mTop, this.mRight, this.mBottom);
            this.mIsTopLeftSet = false;
            this.mIsBottomRightSet = false;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.transition.ChangeBounds.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.transition.ChangeBounds.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.<clinit>():void");
    }

    public ChangeBounds() {
        this.tempLocation = new int[2];
        this.mResizeClip = false;
        this.mReparent = false;
    }

    public ChangeBounds(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.tempLocation = new int[2];
        this.mResizeClip = false;
        this.mReparent = false;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ChangeBounds);
        boolean resizeClip = a.getBoolean(0, false);
        a.recycle();
        setResizeClip(resizeClip);
    }

    public String[] getTransitionProperties() {
        return sTransitionProperties;
    }

    public void setResizeClip(boolean resizeClip) {
        this.mResizeClip = resizeClip;
    }

    public boolean getResizeClip() {
        return this.mResizeClip;
    }

    public void setReparent(boolean reparent) {
        this.mReparent = reparent;
    }

    private void captureValues(TransitionValues values) {
        View view = values.view;
        if (view.isLaidOut() || view.getWidth() != 0 || view.getHeight() != 0) {
            values.values.put(PROPNAME_BOUNDS, new Rect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom()));
            values.values.put(PROPNAME_PARENT, values.view.getParent());
            if (this.mReparent) {
                values.view.getLocationInWindow(this.tempLocation);
                values.values.put(PROPNAME_WINDOW_X, Integer.valueOf(this.tempLocation[0]));
                values.values.put(PROPNAME_WINDOW_Y, Integer.valueOf(this.tempLocation[1]));
            }
            if (this.mResizeClip) {
                values.values.put(PROPNAME_CLIP, view.getClipBounds());
            }
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    public void captureEndValues(TransitionValues transitionValues) {
        captureValues(transitionValues);
    }

    private boolean parentMatches(View startParent, View endParent) {
        if (!this.mReparent) {
            return true;
        }
        TransitionValues endValues = getMatchedTransitionValues(startParent, true);
        return endValues == null ? startParent == endParent : endParent == endValues.view;
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
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public android.animation.Animator createAnimator(android.view.ViewGroup r61, android.transition.TransitionValues r62, android.transition.TransitionValues r63) {
        /*
        r60 = this;
        if (r62 == 0) goto L_0x0004;
    L_0x0002:
        if (r63 != 0) goto L_0x0006;
    L_0x0004:
        r4 = 0;
        return r4;
    L_0x0006:
        r0 = r62;
        r0 = r0.values;
        r49 = r0;
        r0 = r63;
        r0 = r0.values;
        r30 = r0;
        r4 = "android:changeBounds:parent";
        r0 = r49;
        r48 = r0.get(r4);
        r48 = (android.view.ViewGroup) r48;
        r4 = "android:changeBounds:parent";
        r0 = r30;
        r29 = r0.get(r4);
        r29 = (android.view.ViewGroup) r29;
        if (r48 == 0) goto L_0x002c;
    L_0x002a:
        if (r29 != 0) goto L_0x002e;
    L_0x002c:
        r4 = 0;
        return r4;
    L_0x002e:
        r0 = r63;
        r6 = r0.view;
        r0 = r60;
        r1 = r48;
        r2 = r29;
        r4 = r0.parentMatches(r1, r2);
        if (r4 == 0) goto L_0x026f;
    L_0x003e:
        r0 = r62;
        r4 = r0.values;
        r5 = "android:changeBounds:bounds";
        r44 = r4.get(r5);
        r44 = (android.graphics.Rect) r44;
        r0 = r63;
        r4 = r0.values;
        r5 = "android:changeBounds:bounds";
        r26 = r4.get(r5);
        r26 = (android.graphics.Rect) r26;
        r0 = r44;
        r0 = r0.left;
        r47 = r0;
        r0 = r26;
        r8 = r0.left;
        r0 = r44;
        r0 = r0.top;
        r51 = r0;
        r0 = r26;
        r9 = r0.top;
        r0 = r44;
        r0 = r0.right;
        r50 = r0;
        r0 = r26;
        r10 = r0.right;
        r0 = r44;
        r0 = r0.bottom;
        r43 = r0;
        r0 = r26;
        r11 = r0.bottom;
        r52 = r50 - r47;
        r46 = r43 - r51;
        r31 = r10 - r8;
        r28 = r11 - r9;
        r0 = r62;
        r4 = r0.values;
        r5 = "android:changeBounds:clip";
        r45 = r4.get(r5);
        r45 = (android.graphics.Rect) r45;
        r0 = r63;
        r4 = r0.values;
        r5 = "android:changeBounds:clip";
        r7 = r4.get(r5);
        r7 = (android.graphics.Rect) r7;
        r38 = 0;
        if (r52 == 0) goto L_0x0130;
    L_0x00a6:
        if (r46 == 0) goto L_0x0130;
    L_0x00a8:
        r0 = r47;
        if (r0 != r8) goto L_0x00b0;
    L_0x00ac:
        r0 = r51;
        if (r0 == r9) goto L_0x00b2;
    L_0x00b0:
        r38 = 1;
    L_0x00b2:
        r0 = r50;
        if (r0 != r10) goto L_0x00ba;
    L_0x00b6:
        r0 = r43;
        if (r0 == r11) goto L_0x00bc;
    L_0x00ba:
        r38 = r38 + 1;
    L_0x00bc:
        if (r45 == 0) goto L_0x00c6;
    L_0x00be:
        r0 = r45;
        r4 = r0.equals(r7);
        if (r4 == 0) goto L_0x00ca;
    L_0x00c6:
        if (r45 != 0) goto L_0x00cc;
    L_0x00c8:
        if (r7 == 0) goto L_0x00cc;
    L_0x00ca:
        r38 = r38 + 1;
    L_0x00cc:
        if (r38 <= 0) goto L_0x0365;
    L_0x00ce:
        r0 = r60;
        r4 = r0.mResizeClip;
        if (r4 != 0) goto L_0x01d6;
    L_0x00d4:
        r0 = r47;
        r1 = r51;
        r2 = r50;
        r3 = r43;
        r6.setLeftTopRightBottom(r0, r1, r2, r3);
        r4 = 2;
        r0 = r38;
        if (r0 != r4) goto L_0x0198;
    L_0x00e4:
        r0 = r52;
        r1 = r31;
        if (r0 != r1) goto L_0x0136;
    L_0x00ea:
        r0 = r46;
        r1 = r28;
        if (r0 != r1) goto L_0x0136;
    L_0x00f0:
        r4 = r60.getPathMotion();
        r0 = r47;
        r5 = (float) r0;
        r0 = r51;
        r12 = (float) r0;
        r13 = (float) r8;
        r14 = (float) r9;
        r56 = r4.getPath(r5, r12, r13, r14);
        r4 = POSITION_PROPERTY;
        r5 = 0;
        r0 = r56;
        r18 = android.animation.ObjectAnimator.ofObject(r6, r4, r5, r0);
    L_0x0109:
        r4 = r6.getParent();
        r4 = r4 instanceof android.view.ViewGroup;
        if (r4 == 0) goto L_0x012f;
    L_0x0111:
        r40 = r6.getParent();
        r40 = (android.view.ViewGroup) r40;
        r4 = 1;
        r0 = r40;
        r0.suppressLayout(r4);
        r57 = new android.transition.ChangeBounds$9;
        r0 = r57;
        r1 = r60;
        r2 = r40;
        r0.<init>(r1, r2);
        r0 = r60;
        r1 = r57;
        r0.addListener(r1);
    L_0x012f:
        return r18;
    L_0x0130:
        if (r31 == 0) goto L_0x00bc;
    L_0x0132:
        if (r28 == 0) goto L_0x00bc;
    L_0x0134:
        goto L_0x00a8;
    L_0x0136:
        r58 = new android.transition.ChangeBounds$ViewBounds;
        r0 = r58;
        r0.<init>(r6);
        r4 = r60.getPathMotion();
        r0 = r47;
        r5 = (float) r0;
        r0 = r51;
        r12 = (float) r0;
        r13 = (float) r8;
        r14 = (float) r9;
        r56 = r4.getPath(r5, r12, r13, r14);
        r4 = TOP_LEFT_PROPERTY;
        r5 = 0;
        r0 = r58;
        r1 = r56;
        r55 = android.animation.ObjectAnimator.ofObject(r0, r4, r5, r1);
        r4 = r60.getPathMotion();
        r0 = r50;
        r5 = (float) r0;
        r0 = r43;
        r12 = (float) r0;
        r13 = (float) r10;
        r14 = (float) r11;
        r23 = r4.getPath(r5, r12, r13, r14);
        r4 = BOTTOM_RIGHT_PROPERTY;
        r5 = 0;
        r0 = r58;
        r1 = r23;
        r22 = android.animation.ObjectAnimator.ofObject(r0, r4, r5, r1);
        r42 = new android.animation.AnimatorSet;
        r42.<init>();
        r4 = 2;
        r4 = new android.animation.Animator[r4];
        r5 = 0;
        r4[r5] = r55;
        r5 = 1;
        r4[r5] = r22;
        r0 = r42;
        r0.playTogether(r4);
        r18 = r42;
        r4 = new android.transition.ChangeBounds$7;
        r0 = r60;
        r1 = r58;
        r4.<init>(r0, r1);
        r0 = r42;
        r0.addListener(r4);
        goto L_0x0109;
    L_0x0198:
        r0 = r47;
        if (r0 != r8) goto L_0x01a0;
    L_0x019c:
        r0 = r51;
        if (r0 == r9) goto L_0x01bb;
    L_0x01a0:
        r4 = r60.getPathMotion();
        r0 = r47;
        r5 = (float) r0;
        r0 = r51;
        r12 = (float) r0;
        r13 = (float) r8;
        r14 = (float) r9;
        r56 = r4.getPath(r5, r12, r13, r14);
        r4 = TOP_LEFT_ONLY_PROPERTY;
        r5 = 0;
        r0 = r56;
        r18 = android.animation.ObjectAnimator.ofObject(r6, r4, r5, r0);
        goto L_0x0109;
    L_0x01bb:
        r4 = r60.getPathMotion();
        r0 = r50;
        r5 = (float) r0;
        r0 = r43;
        r12 = (float) r0;
        r13 = (float) r10;
        r14 = (float) r11;
        r21 = r4.getPath(r5, r12, r13, r14);
        r4 = BOTTOM_RIGHT_ONLY_PROPERTY;
        r5 = 0;
        r0 = r21;
        r18 = android.animation.ObjectAnimator.ofObject(r6, r4, r5, r0);
        goto L_0x0109;
    L_0x01d6:
        r0 = r52;
        r1 = r31;
        r37 = java.lang.Math.max(r0, r1);
        r0 = r46;
        r1 = r28;
        r36 = java.lang.Math.max(r0, r1);
        r4 = r47 + r37;
        r5 = r51 + r36;
        r0 = r47;
        r1 = r51;
        r6.setLeftTopRightBottom(r0, r1, r4, r5);
        r41 = 0;
        r0 = r47;
        if (r0 != r8) goto L_0x01fb;
    L_0x01f7:
        r0 = r51;
        if (r0 == r9) goto L_0x0214;
    L_0x01fb:
        r4 = r60.getPathMotion();
        r0 = r47;
        r5 = (float) r0;
        r0 = r51;
        r12 = (float) r0;
        r13 = (float) r8;
        r14 = (float) r9;
        r56 = r4.getPath(r5, r12, r13, r14);
        r4 = POSITION_PROPERTY;
        r5 = 0;
        r0 = r56;
        r41 = android.animation.ObjectAnimator.ofObject(r6, r4, r5, r0);
    L_0x0214:
        r34 = r7;
        if (r45 != 0) goto L_0x0225;
    L_0x0218:
        r45 = new android.graphics.Rect;
        r4 = 0;
        r5 = 0;
        r0 = r45;
        r1 = r52;
        r2 = r46;
        r0.<init>(r4, r5, r1, r2);
    L_0x0225:
        if (r7 != 0) goto L_0x0367;
    L_0x0227:
        r27 = new android.graphics.Rect;
        r4 = 0;
        r5 = 0;
        r0 = r27;
        r1 = r31;
        r2 = r28;
        r0.<init>(r4, r5, r1, r2);
    L_0x0234:
        r25 = 0;
        r0 = r45;
        r1 = r27;
        r4 = r0.equals(r1);
        if (r4 != 0) goto L_0x0263;
    L_0x0240:
        r0 = r45;
        r6.setClipBounds(r0);
        r4 = "clipBounds";
        r5 = sRectEvaluator;
        r12 = 2;
        r12 = new java.lang.Object[r12];
        r13 = 0;
        r12[r13] = r45;
        r13 = 1;
        r12[r13] = r27;
        r25 = android.animation.ObjectAnimator.ofObject(r6, r4, r5, r12);
        r4 = new android.transition.ChangeBounds$8;
        r5 = r60;
        r4.<init>(r5, r6, r7, r8, r9, r10, r11);
        r0 = r25;
        r0.addListener(r4);
    L_0x0263:
        r0 = r41;
        r1 = r25;
        r18 = android.transition.TransitionUtils.mergeAnimators(r0, r1);
        r7 = r27;
        goto L_0x0109;
    L_0x026f:
        r0 = r60;
        r4 = r0.tempLocation;
        r0 = r61;
        r0.getLocationInWindow(r4);
        r0 = r62;
        r4 = r0.values;
        r5 = "android:changeBounds:windowX";
        r4 = r4.get(r5);
        r4 = (java.lang.Integer) r4;
        r4 = r4.intValue();
        r0 = r60;
        r5 = r0.tempLocation;
        r12 = 0;
        r5 = r5[r12];
        r53 = r4 - r5;
        r0 = r62;
        r4 = r0.values;
        r5 = "android:changeBounds:windowY";
        r4 = r4.get(r5);
        r4 = (java.lang.Integer) r4;
        r4 = r4.intValue();
        r0 = r60;
        r5 = r0.tempLocation;
        r12 = 1;
        r5 = r5[r12];
        r54 = r4 - r5;
        r0 = r63;
        r4 = r0.values;
        r5 = "android:changeBounds:windowX";
        r4 = r4.get(r5);
        r4 = (java.lang.Integer) r4;
        r4 = r4.intValue();
        r0 = r60;
        r5 = r0.tempLocation;
        r12 = 0;
        r5 = r5[r12];
        r32 = r4 - r5;
        r0 = r63;
        r4 = r0.values;
        r5 = "android:changeBounds:windowY";
        r4 = r4.get(r5);
        r4 = (java.lang.Integer) r4;
        r4 = r4.intValue();
        r0 = r60;
        r5 = r0.tempLocation;
        r12 = 1;
        r5 = r5[r12];
        r33 = r4 - r5;
        r0 = r53;
        r1 = r32;
        if (r0 != r1) goto L_0x02ec;
    L_0x02e6:
        r0 = r54;
        r1 = r33;
        if (r0 == r1) goto L_0x0365;
    L_0x02ec:
        r59 = r6.getWidth();
        r35 = r6.getHeight();
        r4 = android.graphics.Bitmap.Config.ARGB_8888;
        r0 = r59;
        r1 = r35;
        r20 = android.graphics.Bitmap.createBitmap(r0, r1, r4);
        r24 = new android.graphics.Canvas;
        r0 = r24;
        r1 = r20;
        r0.<init>(r1);
        r0 = r24;
        r6.draw(r0);
        r15 = new android.graphics.drawable.BitmapDrawable;
        r0 = r20;
        r15.<init>(r0);
        r4 = r53 + r59;
        r5 = r54 + r35;
        r0 = r53;
        r1 = r54;
        r15.setBounds(r0, r1, r4, r5);
        r17 = r6.getTransitionAlpha();
        r4 = 0;
        r6.setTransitionAlpha(r4);
        r4 = r61.getOverlay();
        r4.add(r15);
        r4 = r60.getPathMotion();
        r0 = r53;
        r5 = (float) r0;
        r0 = r54;
        r12 = (float) r0;
        r0 = r32;
        r13 = (float) r0;
        r0 = r33;
        r14 = (float) r0;
        r56 = r4.getPath(r5, r12, r13, r14);
        r4 = DRAWABLE_ORIGIN_PROPERTY;
        r5 = 0;
        r0 = r56;
        r39 = android.animation.PropertyValuesHolder.ofObject(r4, r5, r0);
        r4 = 1;
        r4 = new android.animation.PropertyValuesHolder[r4];
        r5 = 0;
        r4[r5] = r39;
        r19 = android.animation.ObjectAnimator.ofPropertyValuesHolder(r15, r4);
        r12 = new android.transition.ChangeBounds$10;
        r13 = r60;
        r14 = r61;
        r16 = r6;
        r12.<init>(r13, r14, r15, r16, r17);
        r0 = r19;
        r0.addListener(r12);
        return r19;
    L_0x0365:
        r4 = 0;
        return r4;
    L_0x0367:
        r27 = r7;
        goto L_0x0234;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.transition.ChangeBounds.createAnimator(android.view.ViewGroup, android.transition.TransitionValues, android.transition.TransitionValues):android.animation.Animator");
    }
}
