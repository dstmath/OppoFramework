package android.app;

import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import java.lang.ref.WeakReference;
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
class ActivityTransitionState {
    private static final String ENTERING_SHARED_ELEMENTS = "android:enteringSharedElements";
    private static final String EXITING_MAPPED_FROM = "android:exitingMappedFrom";
    private static final String EXITING_MAPPED_TO = "android:exitingMappedTo";
    private ExitTransitionCoordinator mCalledExitCoordinator;
    private ActivityOptions mEnterActivityOptions;
    private EnterTransitionCoordinator mEnterTransitionCoordinator;
    private ArrayList<String> mEnteringNames;
    private SparseArray<WeakReference<ExitTransitionCoordinator>> mExitTransitionCoordinators;
    private int mExitTransitionCoordinatorsKey;
    private ArrayList<String> mExitingFrom;
    private ArrayList<String> mExitingTo;
    private ArrayList<View> mExitingToView;
    private boolean mHasExited;
    private boolean mIsEnterPostponed;
    private boolean mIsEnterTriggered;
    private ExitTransitionCoordinator mReturnExitCoordinator;

    /* renamed from: android.app.ActivityTransitionState$1 */
    class AnonymousClass1 implements Runnable {
        final /* synthetic */ ActivityTransitionState this$0;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.ActivityTransitionState.1.<init>(android.app.ActivityTransitionState):void, dex: 
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
        AnonymousClass1(android.app.ActivityTransitionState r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.ActivityTransitionState.1.<init>(android.app.ActivityTransitionState):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityTransitionState.1.<init>(android.app.ActivityTransitionState):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.ActivityTransitionState.1.run():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.ActivityTransitionState.1.run():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityTransitionState.1.run():void");
        }
    }

    /* renamed from: android.app.ActivityTransitionState$2 */
    class AnonymousClass2 implements OnPreDrawListener {
        final /* synthetic */ ActivityTransitionState this$0;
        final /* synthetic */ Activity val$activity;
        final /* synthetic */ ViewGroup val$finalDecor;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.app.ActivityTransitionState.2.<init>(android.app.ActivityTransitionState, android.view.ViewGroup, android.app.Activity):void, dex: 
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
        AnonymousClass2(android.app.ActivityTransitionState r1, android.view.ViewGroup r2, android.app.Activity r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.app.ActivityTransitionState.2.<init>(android.app.ActivityTransitionState, android.view.ViewGroup, android.app.Activity):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityTransitionState.2.<init>(android.app.ActivityTransitionState, android.view.ViewGroup, android.app.Activity):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.app.ActivityTransitionState.2.onPreDraw():boolean, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.app.ActivityTransitionState.2.onPreDraw():boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityTransitionState.2.onPreDraw():boolean");
        }
    }

    public ActivityTransitionState() {
        this.mExitTransitionCoordinatorsKey = 1;
    }

    public int addExitTransitionCoordinator(ExitTransitionCoordinator exitTransitionCoordinator) {
        if (this.mExitTransitionCoordinators == null) {
            this.mExitTransitionCoordinators = new SparseArray();
        }
        WeakReference<ExitTransitionCoordinator> ref = new WeakReference(exitTransitionCoordinator);
        for (int i = this.mExitTransitionCoordinators.size() - 1; i >= 0; i--) {
            if (((WeakReference) this.mExitTransitionCoordinators.valueAt(i)).get() == null) {
                this.mExitTransitionCoordinators.removeAt(i);
            }
        }
        int newKey = this.mExitTransitionCoordinatorsKey;
        this.mExitTransitionCoordinatorsKey = newKey + 1;
        this.mExitTransitionCoordinators.append(newKey, ref);
        return newKey;
    }

    public void readState(Bundle bundle) {
        if (bundle != null) {
            if (this.mEnterTransitionCoordinator == null || this.mEnterTransitionCoordinator.isReturning()) {
                this.mEnteringNames = bundle.getStringArrayList(ENTERING_SHARED_ELEMENTS);
            }
            if (this.mEnterTransitionCoordinator == null) {
                this.mExitingFrom = bundle.getStringArrayList(EXITING_MAPPED_FROM);
                this.mExitingTo = bundle.getStringArrayList(EXITING_MAPPED_TO);
            }
        }
    }

    public void saveState(Bundle bundle) {
        if (this.mEnteringNames != null) {
            bundle.putStringArrayList(ENTERING_SHARED_ELEMENTS, this.mEnteringNames);
        }
        if (this.mExitingFrom != null) {
            bundle.putStringArrayList(EXITING_MAPPED_FROM, this.mExitingFrom);
            bundle.putStringArrayList(EXITING_MAPPED_TO, this.mExitingTo);
        }
    }

    public void setEnterActivityOptions(Activity activity, ActivityOptions options) {
        Window window = activity.getWindow();
        if (window != null) {
            window.getDecorView();
            if (window.hasFeature(13) && options != null && this.mEnterActivityOptions == null && this.mEnterTransitionCoordinator == null && options.getAnimationType() == 5) {
                this.mEnterActivityOptions = options;
                this.mIsEnterTriggered = false;
                if (this.mEnterActivityOptions.isReturning()) {
                    restoreExitedViews();
                    int result = this.mEnterActivityOptions.getResultCode();
                    if (result != 0) {
                        activity.onActivityReenter(result, this.mEnterActivityOptions.getResultData());
                    }
                }
            }
        }
    }

    public void enterReady(Activity activity) {
        if (this.mEnterActivityOptions != null && !this.mIsEnterTriggered) {
            this.mIsEnterTriggered = true;
            this.mHasExited = false;
            ArrayList<String> sharedElementNames = this.mEnterActivityOptions.getSharedElementNames();
            ResultReceiver resultReceiver = this.mEnterActivityOptions.getResultReceiver();
            if (this.mEnterActivityOptions.isReturning()) {
                restoreExitedViews();
                activity.getWindow().getDecorView().setVisibility(0);
            }
            this.mEnterTransitionCoordinator = new EnterTransitionCoordinator(activity, resultReceiver, sharedElementNames, this.mEnterActivityOptions.isReturning(), this.mEnterActivityOptions.isCrossTask());
            if (this.mEnterActivityOptions.isCrossTask()) {
                this.mExitingFrom = new ArrayList(this.mEnterActivityOptions.getSharedElementNames());
                this.mExitingTo = new ArrayList(this.mEnterActivityOptions.getSharedElementNames());
            }
            if (!this.mIsEnterPostponed) {
                startEnter();
            }
        }
    }

    public void postponeEnterTransition() {
        this.mIsEnterPostponed = true;
    }

    public void startPostponedEnterTransition() {
        if (this.mIsEnterPostponed) {
            this.mIsEnterPostponed = false;
            if (this.mEnterTransitionCoordinator != null) {
                startEnter();
            }
        }
    }

    private void startEnter() {
        if (!this.mEnterTransitionCoordinator.isReturning()) {
            this.mEnterTransitionCoordinator.namedViewsReady(null, null);
            this.mEnteringNames = this.mEnterTransitionCoordinator.getAllSharedElementNames();
        } else if (this.mExitingToView != null) {
            this.mEnterTransitionCoordinator.viewInstancesReady(this.mExitingFrom, this.mExitingTo, this.mExitingToView);
        } else {
            this.mEnterTransitionCoordinator.namedViewsReady(this.mExitingFrom, this.mExitingTo);
        }
        this.mExitingFrom = null;
        this.mExitingTo = null;
        this.mExitingToView = null;
        this.mEnterActivityOptions = null;
    }

    public void onStop() {
        restoreExitedViews();
        if (this.mEnterTransitionCoordinator != null) {
            this.mEnterTransitionCoordinator.stop();
            this.mEnterTransitionCoordinator = null;
        }
        if (this.mReturnExitCoordinator != null) {
            this.mReturnExitCoordinator.stop();
            this.mReturnExitCoordinator = null;
        }
    }

    public void onResume(Activity activity, boolean isTopOfTask) {
        if (isTopOfTask || this.mEnterTransitionCoordinator == null) {
            restoreExitedViews();
            restoreReenteringViews();
            return;
        }
        activity.mHandler.postDelayed(new AnonymousClass1(this), 1000);
    }

    public void clear() {
        this.mEnteringNames = null;
        this.mExitingFrom = null;
        this.mExitingTo = null;
        this.mExitingToView = null;
        this.mCalledExitCoordinator = null;
        this.mEnterTransitionCoordinator = null;
        this.mEnterActivityOptions = null;
        this.mExitTransitionCoordinators = null;
    }

    private void restoreExitedViews() {
        if (this.mCalledExitCoordinator != null) {
            this.mCalledExitCoordinator.resetViews();
            this.mCalledExitCoordinator = null;
        }
    }

    private void restoreReenteringViews() {
        if (this.mEnterTransitionCoordinator != null && this.mEnterTransitionCoordinator.isReturning() && !this.mEnterTransitionCoordinator.isCrossTask()) {
            this.mEnterTransitionCoordinator.forceViewsToAppear();
            this.mExitingFrom = null;
            this.mExitingTo = null;
            this.mExitingToView = null;
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
    public boolean startExitBackTransition(android.app.Activity r13) {
        /*
        r12 = this;
        r7 = 1;
        r5 = 0;
        r0 = r12.mEnteringNames;
        if (r0 == 0) goto L_0x000a;
    L_0x0006:
        r0 = r12.mCalledExitCoordinator;
        if (r0 == 0) goto L_0x000c;
    L_0x000a:
        r0 = 0;
        return r0;
    L_0x000c:
        r0 = r12.mHasExited;
        if (r0 != 0) goto L_0x005d;
    L_0x0010:
        r12.mHasExited = r7;
        r10 = 0;
        r8 = 0;
        r9 = 0;
        r0 = r12.mEnterTransitionCoordinator;
        if (r0 == 0) goto L_0x0034;
    L_0x0019:
        r0 = r12.mEnterTransitionCoordinator;
        r10 = r0.getEnterViewsTransition();
        r0 = r12.mEnterTransitionCoordinator;
        r8 = r0.getDecor();
        r0 = r12.mEnterTransitionCoordinator;
        r9 = r0.cancelEnter();
        r12.mEnterTransitionCoordinator = r5;
        if (r10 == 0) goto L_0x0034;
    L_0x002f:
        if (r8 == 0) goto L_0x0034;
    L_0x0031:
        r10.pause(r8);
    L_0x0034:
        r0 = new android.app.ExitTransitionCoordinator;
        r2 = r13.getWindow();
        r3 = r13.mEnterTransitionListener;
        r4 = r12.mEnteringNames;
        r1 = r13;
        r6 = r5;
        r0.<init>(r1, r2, r3, r4, r5, r6, r7);
        r12.mReturnExitCoordinator = r0;
        if (r10 == 0) goto L_0x004c;
    L_0x0047:
        if (r8 == 0) goto L_0x004c;
    L_0x0049:
        r10.resume(r8);
    L_0x004c:
        if (r9 == 0) goto L_0x005e;
    L_0x004e:
        if (r8 == 0) goto L_0x005e;
    L_0x0050:
        r11 = r8;
        r0 = r8.getViewTreeObserver();
        r1 = new android.app.ActivityTransitionState$2;
        r1.<init>(r12, r11, r13);
        r0.addOnPreDrawListener(r1);
    L_0x005d:
        return r7;
    L_0x005e:
        r0 = r12.mReturnExitCoordinator;
        r1 = r13.mResultCode;
        r2 = r13.mResultData;
        r0.startExit(r1, r2);
        goto L_0x005d;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.app.ActivityTransitionState.startExitBackTransition(android.app.Activity):boolean");
    }

    public void startExitOutTransition(Activity activity, Bundle options) {
        this.mEnterTransitionCoordinator = null;
        if (activity.getWindow().hasFeature(13) && this.mExitTransitionCoordinators != null) {
            ActivityOptions activityOptions = new ActivityOptions(options);
            if (activityOptions.getAnimationType() == 5) {
                int index = this.mExitTransitionCoordinators.indexOfKey(activityOptions.getExitCoordinatorKey());
                if (index >= 0) {
                    this.mCalledExitCoordinator = (ExitTransitionCoordinator) ((WeakReference) this.mExitTransitionCoordinators.valueAt(index)).get();
                    this.mExitTransitionCoordinators.removeAt(index);
                    if (this.mCalledExitCoordinator != null) {
                        this.mExitingFrom = this.mCalledExitCoordinator.getAcceptedNames();
                        this.mExitingTo = this.mCalledExitCoordinator.getMappedNames();
                        this.mExitingToView = this.mCalledExitCoordinator.copyMappedViews();
                        this.mCalledExitCoordinator.startExit();
                    }
                }
            }
        }
    }
}
