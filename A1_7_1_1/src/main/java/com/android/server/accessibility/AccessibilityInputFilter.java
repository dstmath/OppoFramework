package com.android.server.accessibility;

import android.content.Context;
import android.os.PowerManager;
import android.util.Pools.SimplePool;
import android.util.SparseBooleanArray;
import android.view.Choreographer;
import android.view.InputEvent;
import android.view.InputFilter;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.job.controllers.JobStatus;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class AccessibilityInputFilter extends InputFilter implements EventStreamTransformation {
    private static final boolean DEBUG = false;
    static final int FEATURES_AFFECTING_MOTION_EVENTS = 27;
    static final int FLAG_FEATURE_AUTOCLICK = 8;
    static final int FLAG_FEATURE_CONTROL_SCREEN_MAGNIFIER = 32;
    static final int FLAG_FEATURE_FILTER_KEY_EVENTS = 4;
    static final int FLAG_FEATURE_INJECT_MOTION_EVENTS = 16;
    static final int FLAG_FEATURE_SCREEN_MAGNIFIER = 1;
    static final int FLAG_FEATURE_TOUCH_EXPLORATION = 2;
    private static final String TAG = null;
    private final AccessibilityManagerService mAms;
    private AutoclickController mAutoclickController;
    private final Choreographer mChoreographer;
    private final Context mContext;
    private int mEnabledFeatures;
    private EventStreamTransformation mEventHandler;
    private MotionEventHolder mEventQueue;
    private boolean mInstalled;
    private KeyboardInterceptor mKeyboardInterceptor;
    private EventStreamState mKeyboardStreamState;
    private MagnificationGestureHandler mMagnificationGestureHandler;
    private MotionEventInjector mMotionEventInjector;
    private EventStreamState mMouseStreamState;
    private final PowerManager mPm;
    private final Runnable mProcessBatchedEventsRunnable;
    private TouchExplorer mTouchExplorer;
    private EventStreamState mTouchScreenStreamState;
    private int mUserId;

    private static class EventStreamState {
        private int mDeviceId = -1;

        EventStreamState() {
        }

        public boolean updateDeviceId(int deviceId) {
            if (this.mDeviceId == deviceId) {
                return false;
            }
            reset();
            this.mDeviceId = deviceId;
            return true;
        }

        public boolean deviceIdValid() {
            return this.mDeviceId >= 0;
        }

        public void reset() {
            this.mDeviceId = -1;
        }

        public boolean shouldProcessScroll() {
            return false;
        }

        public boolean shouldProcessMotionEvent(MotionEvent event) {
            return false;
        }

        public boolean shouldProcessKeyEvent(KeyEvent event) {
            return false;
        }
    }

    private static class KeyboardEventStreamState extends EventStreamState {
        private SparseBooleanArray mEventSequenceStartedMap = new SparseBooleanArray();

        public KeyboardEventStreamState() {
            reset();
        }

        public final void reset() {
            super.reset();
            this.mEventSequenceStartedMap.clear();
        }

        public boolean updateDeviceId(int deviceId) {
            return false;
        }

        public boolean deviceIdValid() {
            return true;
        }

        public final boolean shouldProcessKeyEvent(KeyEvent event) {
            int deviceId = event.getDeviceId();
            if (this.mEventSequenceStartedMap.get(deviceId, false)) {
                return true;
            }
            boolean shouldProcess = event.getAction() == 0;
            this.mEventSequenceStartedMap.put(deviceId, shouldProcess);
            return shouldProcess;
        }
    }

    private static class MotionEventHolder {
        private static final int MAX_POOL_SIZE = 32;
        private static final SimplePool<MotionEventHolder> sPool = null;
        public MotionEvent event;
        public MotionEventHolder next;
        public int policyFlags;
        public MotionEventHolder previous;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.accessibility.AccessibilityInputFilter.MotionEventHolder.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.accessibility.AccessibilityInputFilter.MotionEventHolder.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.AccessibilityInputFilter.MotionEventHolder.<clinit>():void");
        }

        private MotionEventHolder() {
        }

        public static MotionEventHolder obtain(MotionEvent event, int policyFlags) {
            MotionEventHolder holder = (MotionEventHolder) sPool.acquire();
            if (holder == null) {
                holder = new MotionEventHolder();
            }
            holder.event = MotionEvent.obtain(event);
            holder.policyFlags = policyFlags;
            return holder;
        }

        public void recycle() {
            this.event.recycle();
            this.event = null;
            this.policyFlags = 0;
            this.next = null;
            this.previous = null;
            sPool.release(this);
        }
    }

    private static class MouseEventStreamState extends EventStreamState {
        private boolean mMotionSequenceStarted;

        public MouseEventStreamState() {
            reset();
        }

        public final void reset() {
            super.reset();
            this.mMotionSequenceStarted = false;
        }

        public final boolean shouldProcessScroll() {
            return true;
        }

        public final boolean shouldProcessMotionEvent(MotionEvent event) {
            boolean z = true;
            if (this.mMotionSequenceStarted) {
                return true;
            }
            int action = event.getActionMasked();
            if (!(action == 0 || action == 7)) {
                z = false;
            }
            this.mMotionSequenceStarted = z;
            return this.mMotionSequenceStarted;
        }
    }

    private static class TouchScreenEventStreamState extends EventStreamState {
        private boolean mHoverSequenceStarted;
        private boolean mTouchSequenceStarted;

        public TouchScreenEventStreamState() {
            reset();
        }

        public final void reset() {
            super.reset();
            this.mTouchSequenceStarted = false;
            this.mHoverSequenceStarted = false;
        }

        public final boolean shouldProcessMotionEvent(MotionEvent event) {
            boolean z = true;
            if (event.isTouchEvent()) {
                if (this.mTouchSequenceStarted) {
                    return true;
                }
                if (event.getActionMasked() != 0) {
                    z = false;
                }
                this.mTouchSequenceStarted = z;
                return this.mTouchSequenceStarted;
            } else if (this.mHoverSequenceStarted) {
                return true;
            } else {
                if (event.getActionMasked() != 9) {
                    z = false;
                }
                this.mHoverSequenceStarted = z;
                return this.mHoverSequenceStarted;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.accessibility.AccessibilityInputFilter.<clinit>():void, dex: 
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
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.accessibility.AccessibilityInputFilter.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.AccessibilityInputFilter.<clinit>():void");
    }

    AccessibilityInputFilter(Context context, AccessibilityManagerService service) {
        super(context.getMainLooper());
        this.mProcessBatchedEventsRunnable = new Runnable() {
            public void run() {
                AccessibilityInputFilter.this.processBatchedEvents(AccessibilityInputFilter.this.mChoreographer.getFrameTimeNanos());
                if (AccessibilityInputFilter.this.mEventQueue != null) {
                    AccessibilityInputFilter.this.scheduleProcessBatchedEvents();
                }
            }
        };
        this.mContext = context;
        this.mAms = service;
        this.mPm = (PowerManager) context.getSystemService("power");
        this.mChoreographer = Choreographer.getInstance();
    }

    public void onInstalled() {
        this.mInstalled = true;
        disableFeatures();
        enableFeatures();
        super.onInstalled();
    }

    public void onUninstalled() {
        this.mInstalled = false;
        disableFeatures();
        super.onUninstalled();
    }

    public void onInputEvent(InputEvent event, int policyFlags) {
        if (this.mEventHandler == null) {
            super.onInputEvent(event, policyFlags);
            return;
        }
        EventStreamState state = getEventStreamState(event);
        if (state == null) {
            super.onInputEvent(event, policyFlags);
            return;
        }
        int eventSource = event.getSource();
        if ((1073741824 & policyFlags) == 0) {
            state.reset();
            this.mEventHandler.clearEvents(eventSource);
            super.onInputEvent(event, policyFlags);
            return;
        }
        if (state.updateDeviceId(event.getDeviceId())) {
            this.mEventHandler.clearEvents(eventSource);
        }
        if (state.deviceIdValid()) {
            if (event instanceof MotionEvent) {
                if ((this.mEnabledFeatures & 27) != 0) {
                    processMotionEvent(state, (MotionEvent) event, policyFlags);
                    return;
                }
                super.onInputEvent(event, policyFlags);
            } else if (event instanceof KeyEvent) {
                processKeyEvent(state, (KeyEvent) event, policyFlags);
            }
            return;
        }
        super.onInputEvent(event, policyFlags);
    }

    private EventStreamState getEventStreamState(InputEvent event) {
        if (event instanceof MotionEvent) {
            if (event.isFromSource(4098)) {
                if (this.mTouchScreenStreamState == null) {
                    this.mTouchScreenStreamState = new TouchScreenEventStreamState();
                }
                return this.mTouchScreenStreamState;
            } else if (event.isFromSource(8194)) {
                if (this.mMouseStreamState == null) {
                    this.mMouseStreamState = new MouseEventStreamState();
                }
                return this.mMouseStreamState;
            }
        } else if ((event instanceof KeyEvent) && event.isFromSource(257)) {
            if (this.mKeyboardStreamState == null) {
                this.mKeyboardStreamState = new KeyboardEventStreamState();
            }
            return this.mKeyboardStreamState;
        }
        return null;
    }

    private void processMotionEvent(EventStreamState state, MotionEvent event, int policyFlags) {
        if (!state.shouldProcessScroll() && event.getActionMasked() == 8) {
            super.onInputEvent(event, policyFlags);
        } else if (state.shouldProcessMotionEvent(event)) {
            batchMotionEvent(event, policyFlags);
        }
    }

    private void processKeyEvent(EventStreamState state, KeyEvent event, int policyFlags) {
        if (state.shouldProcessKeyEvent(event)) {
            this.mEventHandler.onKeyEvent(event, policyFlags);
        }
    }

    private void scheduleProcessBatchedEvents() {
        this.mChoreographer.postCallback(0, this.mProcessBatchedEventsRunnable, null);
    }

    private void batchMotionEvent(MotionEvent event, int policyFlags) {
        if (this.mEventQueue == null) {
            this.mEventQueue = MotionEventHolder.obtain(event, policyFlags);
            scheduleProcessBatchedEvents();
        } else if (!this.mEventQueue.event.addBatch(event)) {
            MotionEventHolder holder = MotionEventHolder.obtain(event, policyFlags);
            holder.next = this.mEventQueue;
            this.mEventQueue.previous = holder;
            this.mEventQueue = holder;
        }
    }

    private void processBatchedEvents(long frameNanos) {
        MotionEventHolder current = this.mEventQueue;
        if (current != null) {
            while (current.next != null) {
                current = current.next;
            }
            while (current != null) {
                if (current.event.getEventTimeNano() >= frameNanos) {
                    current.next = null;
                    break;
                }
                handleMotionEvent(current.event, current.policyFlags);
                MotionEventHolder prior = current;
                current = current.previous;
                prior.recycle();
            }
            this.mEventQueue = null;
        }
    }

    private void handleMotionEvent(MotionEvent event, int policyFlags) {
        if (this.mEventHandler != null) {
            this.mPm.userActivity(event.getEventTime(), false);
            MotionEvent transformedEvent = MotionEvent.obtain(event);
            this.mEventHandler.onMotionEvent(transformedEvent, event, policyFlags);
            transformedEvent.recycle();
        }
    }

    public void onMotionEvent(MotionEvent transformedEvent, MotionEvent rawEvent, int policyFlags) {
        sendInputEvent(transformedEvent, policyFlags);
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        sendInputEvent(event, policyFlags);
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
    }

    public void setNext(EventStreamTransformation sink) {
    }

    public void clearEvents(int inputSource) {
    }

    void setUserAndEnabledFeatures(int userId, int enabledFeatures) {
        if (this.mEnabledFeatures != enabledFeatures || this.mUserId != userId) {
            if (this.mInstalled) {
                disableFeatures();
            }
            this.mUserId = userId;
            this.mEnabledFeatures = enabledFeatures;
            if (this.mInstalled) {
                enableFeatures();
            }
        }
    }

    void notifyAccessibilityEvent(AccessibilityEvent event) {
        if (this.mEventHandler != null) {
            this.mEventHandler.onAccessibilityEvent(event);
        }
    }

    private void enableFeatures() {
        resetStreamState();
        if ((this.mEnabledFeatures & 8) != 0) {
            this.mAutoclickController = new AutoclickController(this.mContext, this.mUserId);
            addFirstEventHandler(this.mAutoclickController);
        }
        if ((this.mEnabledFeatures & 2) != 0) {
            this.mTouchExplorer = new TouchExplorer(this.mContext, this.mAms);
            addFirstEventHandler(this.mTouchExplorer);
        }
        if (!((this.mEnabledFeatures & 32) == 0 && (this.mEnabledFeatures & 1) == 0)) {
            this.mMagnificationGestureHandler = new MagnificationGestureHandler(this.mContext, this.mAms, (this.mEnabledFeatures & 1) != 0);
            addFirstEventHandler(this.mMagnificationGestureHandler);
        }
        if ((this.mEnabledFeatures & 16) != 0) {
            this.mMotionEventInjector = new MotionEventInjector(this.mContext.getMainLooper());
            addFirstEventHandler(this.mMotionEventInjector);
            this.mAms.setMotionEventInjector(this.mMotionEventInjector);
        }
        if ((this.mEnabledFeatures & 4) != 0) {
            this.mKeyboardInterceptor = new KeyboardInterceptor(this.mAms);
            addFirstEventHandler(this.mKeyboardInterceptor);
        }
    }

    private void addFirstEventHandler(EventStreamTransformation handler) {
        if (this.mEventHandler != null) {
            handler.setNext(this.mEventHandler);
        } else {
            handler.setNext(this);
        }
        this.mEventHandler = handler;
    }

    private void disableFeatures() {
        processBatchedEvents(JobStatus.NO_LATEST_RUNTIME);
        if (this.mMotionEventInjector != null) {
            this.mAms.setMotionEventInjector(null);
            this.mMotionEventInjector.onDestroy();
            this.mMotionEventInjector = null;
        }
        if (this.mAutoclickController != null) {
            this.mAutoclickController.onDestroy();
            this.mAutoclickController = null;
        }
        if (this.mTouchExplorer != null) {
            this.mTouchExplorer.onDestroy();
            this.mTouchExplorer = null;
        }
        if (this.mMagnificationGestureHandler != null) {
            this.mMagnificationGestureHandler.onDestroy();
            this.mMagnificationGestureHandler = null;
        }
        if (this.mKeyboardInterceptor != null) {
            this.mKeyboardInterceptor.onDestroy();
            this.mKeyboardInterceptor = null;
        }
        if (!((this.mEnabledFeatures & 1) == 0 || this.mEventHandler == null)) {
            this.mEventHandler.setNext(null);
        }
        this.mEventHandler = null;
        resetStreamState();
    }

    void resetStreamState() {
        if (this.mTouchScreenStreamState != null) {
            this.mTouchScreenStreamState.reset();
        }
        if (this.mMouseStreamState != null) {
            this.mMouseStreamState.reset();
        }
        if (this.mKeyboardStreamState != null) {
            this.mKeyboardStreamState.reset();
        }
    }

    public void onDestroy() {
    }
}
