package com.android.server.accessibility;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.Secure;
import android.util.MathUtils;
import android.util.TypedValue;
import android.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.OppoScreenDragUtil;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityEvent;
import com.android.server.display.OppoBrightUtils;
import com.oppo.app.OppoSecurityAlertDialog;
import com.oppo.app.OppoSecurityAlertDialog.OnSelectedListener;

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
class MagnificationGestureHandler implements EventStreamTransformation {
    private static final boolean DEBUG_DETECTING = false;
    private static final boolean DEBUG_PANNING = false;
    private static final boolean DEBUG_STATE_TRANSITIONS = false;
    private static final String LOG_TAG = "MagnificationEventHandler";
    private static final float MAX_SCALE = 5.0f;
    private static final float MIN_SCALE = 2.0f;
    private static final int STATE_DELEGATING = 1;
    private static final int STATE_DETECTING = 2;
    private static final int STATE_MAGNIFIED_INTERACTION = 4;
    private static final int STATE_VIEWPORT_DRAGGING = 3;
    private AccessibilityManagerService mAms;
    private int mCurrentState;
    private long mDelegatingStateDownTime;
    private final boolean mDetectControlGestures;
    private final DetectingStateHandler mDetectingStateHandler;
    private final MagnificationController mMagnificationController;
    private final MagnifiedContentInteractionStateHandler mMagnifiedContentInteractionStateHandler;
    private EventStreamTransformation mNext;
    private int mPreviousState;
    private final StateViewportDraggingHandler mStateViewportDraggingHandler;
    private PointerCoords[] mTempPointerCoords;
    private PointerProperties[] mTempPointerProperties;
    private boolean mTranslationEnabledBeforePan;

    private interface MotionEventHandler {
        void clear();

        void onMotionEvent(MotionEvent motionEvent, MotionEvent motionEvent2, int i);
    }

    private final class DetectingStateHandler implements MotionEventHandler {
        private static final int ACTION_TAP_COUNT = 3;
        private static final int MESSAGE_ON_ACTION_TAP_AND_HOLD = 1;
        private static final int MESSAGE_TRANSITION_TO_DELEGATING_STATE = 2;
        private boolean isShow = false;
        private Context mContext;
        private MotionEventInfo mDelayedEventQueue;
        private final Handler mHandler = new Handler() {
            public void handleMessage(Message message) {
                int type = message.what;
                switch (type) {
                    case 1:
                        DetectingStateHandler.this.onActionTapAndHold(message.obj, message.arg1);
                        return;
                    case 2:
                        MagnificationGestureHandler.this.transitionToState(1);
                        DetectingStateHandler.this.sendDelayedMotionEvents();
                        DetectingStateHandler.this.clear();
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message type: " + type);
                }
            }
        };
        private MotionEvent mLastDownEvent;
        private MotionEvent mLastTapUpEvent;
        private final int mMultiTapDistanceSlop;
        private final int mMultiTapTimeSlop;
        private int mTapCount;
        private final int mTapDistanceSlop;
        private final int mTapTimeSlop = ViewConfiguration.getJumpTapTimeout();

        public DetectingStateHandler(Context context) {
            this.mContext = context;
            this.mMultiTapTimeSlop = ViewConfiguration.getDoubleTapTimeout() + context.getResources().getInteger(17694873);
            this.mTapDistanceSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            this.mMultiTapDistanceSlop = ViewConfiguration.get(context).getScaledDoubleTapSlop();
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            cacheDelayedMotionEvent(event, rawEvent, policyFlags);
            switch (event.getActionMasked()) {
                case 0:
                    this.mHandler.removeMessages(2);
                    if (MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                        if (this.mTapCount == 2 && this.mLastDownEvent != null && GestureUtils.isMultiTap(this.mLastDownEvent, event, this.mMultiTapTimeSlop, this.mMultiTapDistanceSlop, 0)) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1, policyFlags, 0, event), (long) ViewConfiguration.getLongPressTimeout());
                        } else if (this.mTapCount < 3) {
                            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), (long) this.mMultiTapTimeSlop);
                        }
                        clearLastDownEvent();
                        this.mLastDownEvent = MotionEvent.obtain(event);
                        break;
                    }
                    transitionToDelegatingStateAndClear();
                    return;
                case 1:
                    if (this.mLastDownEvent != null) {
                        this.mHandler.removeMessages(1);
                        if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(event.getX(), event.getY())) {
                            transitionToDelegatingStateAndClear();
                            return;
                        } else if (!GestureUtils.isTap(this.mLastDownEvent, event, this.mTapTimeSlop, this.mTapDistanceSlop, 0)) {
                            transitionToDelegatingStateAndClear();
                            return;
                        } else if (this.mLastTapUpEvent == null || GestureUtils.isMultiTap(this.mLastTapUpEvent, event, this.mMultiTapTimeSlop, this.mMultiTapDistanceSlop, 0)) {
                            this.mTapCount++;
                            if (this.mTapCount != 3 || OppoScreenDragUtil.isOffsetState()) {
                                clearLastTapUpEvent();
                                this.mLastTapUpEvent = MotionEvent.obtain(event);
                                break;
                            }
                            if (!MagnificationGestureHandler.this.mAms.getMagnificationDialogEnable() || MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                                clear();
                                onActionTap(event, policyFlags);
                            } else {
                                displayDialog(event, policyFlags);
                            }
                            return;
                        } else {
                            transitionToDelegatingStateAndClear();
                            return;
                        }
                    }
                    return;
                    break;
                case 2:
                    if (this.mLastDownEvent != null && this.mTapCount < 2 && Math.abs(GestureUtils.computeDistance(this.mLastDownEvent, event, 0)) > ((double) this.mTapDistanceSlop)) {
                        transitionToDelegatingStateAndClear();
                        break;
                    }
                case 5:
                    if (!MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                        transitionToDelegatingStateAndClear();
                        break;
                    }
                    MagnificationGestureHandler.this.transitionToState(4);
                    clear();
                    break;
            }
        }

        public void clear() {
            this.mHandler.removeMessages(1);
            this.mHandler.removeMessages(2);
            clearTapDetectionState();
            clearDelayedMotionEvents();
        }

        private void clearTapDetectionState() {
            this.mTapCount = 0;
            clearLastTapUpEvent();
            clearLastDownEvent();
        }

        private void clearLastTapUpEvent() {
            if (this.mLastTapUpEvent != null) {
                this.mLastTapUpEvent.recycle();
                this.mLastTapUpEvent = null;
            }
        }

        private void clearLastDownEvent() {
            if (this.mLastDownEvent != null) {
                this.mLastDownEvent.recycle();
                this.mLastDownEvent = null;
            }
        }

        private void cacheDelayedMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            MotionEventInfo info = MotionEventInfo.obtain(event, rawEvent, policyFlags);
            if (this.mDelayedEventQueue == null) {
                this.mDelayedEventQueue = info;
                return;
            }
            MotionEventInfo tail = this.mDelayedEventQueue;
            while (tail.mNext != null) {
                tail = tail.mNext;
            }
            tail.mNext = info;
        }

        private void sendDelayedMotionEvents() {
            while (this.mDelayedEventQueue != null) {
                MotionEventInfo info = this.mDelayedEventQueue;
                this.mDelayedEventQueue = info.mNext;
                MagnificationGestureHandler.this.onMotionEvent(info.mEvent, info.mRawEvent, info.mPolicyFlags);
                info.recycle();
            }
        }

        private void clearDelayedMotionEvents() {
            while (this.mDelayedEventQueue != null) {
                MotionEventInfo info = this.mDelayedEventQueue;
                this.mDelayedEventQueue = info.mNext;
                info.recycle();
            }
        }

        private void transitionToDelegatingStateAndClear() {
            MagnificationGestureHandler.this.transitionToState(1);
            sendDelayedMotionEvents();
            clear();
        }

        private void onActionTap(MotionEvent up, int policyFlags) {
            if (MagnificationGestureHandler.this.mMagnificationController.isMagnifying()) {
                MagnificationGestureHandler.this.mMagnificationController.reset(true);
                return;
            }
            MagnificationGestureHandler.this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(MagnificationGestureHandler.this.mMagnificationController.getPersistedScale(), 2.0f, MagnificationGestureHandler.MAX_SCALE), up.getX(), up.getY(), true, 0);
        }

        private void onActionTapAndHold(MotionEvent down, int policyFlags) {
            clear();
            MagnificationGestureHandler.this.mTranslationEnabledBeforePan = MagnificationGestureHandler.this.mMagnificationController.isMagnifying();
            MagnificationGestureHandler.this.mMagnificationController.setScaleAndCenter(MathUtils.constrain(MagnificationGestureHandler.this.mMagnificationController.getPersistedScale(), 2.0f, MagnificationGestureHandler.MAX_SCALE), down.getX(), down.getY(), true, 0);
            MagnificationGestureHandler.this.transitionToState(3);
        }

        private void displayDialog(final MotionEvent event, final int policyFlags) {
            Context settingsContext = new ContextThemeWrapper(this.mContext, 201523202);
            clearDelayedMotionEvents();
            if (!this.isShow) {
                OppoSecurityAlertDialog manificationDialog = new OppoSecurityAlertDialog(settingsContext, 201590154, 201590155, true, false, 201590157, 201590156);
                final Dialog dialog = manificationDialog.getSecurityAlertDialog();
                dialog.getWindow().setType(2003);
                dialog.setOnKeyListener(new OnKeyListener() {
                    public boolean onKey(DialogInterface dialogInterface, int keyCode, KeyEvent event) {
                        if (keyCode == 4 && event.getAction() == 0 && dialog != null && dialog.isShowing()) {
                            DetectingStateHandler.this.isShow = false;
                            dialog.dismiss();
                        }
                        return false;
                    }
                });
                manificationDialog.setOnSelectedListener(new OnSelectedListener() {
                    public void onSelected(DialogInterface dialog, boolean isCheck, int whichButton) {
                        switch (whichButton) {
                            case -2:
                                Secure.putInt(DetectingStateHandler.this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0);
                                DetectingStateHandler.this.isShow = false;
                                return;
                            case -1:
                                if (isCheck) {
                                    MagnificationGestureHandler.this.mAms.setMagnificationDialogEnable(false);
                                }
                                DetectingStateHandler.this.clear();
                                DetectingStateHandler.this.onActionTap(event, policyFlags);
                                DetectingStateHandler.this.isShow = false;
                                return;
                            default:
                                return;
                        }
                    }
                });
                this.isShow = true;
                manificationDialog.show();
            }
        }
    }

    private final class MagnifiedContentInteractionStateHandler extends SimpleOnGestureListener implements OnScaleGestureListener, MotionEventHandler {
        private final GestureDetector mGestureDetector;
        private float mInitialScaleFactor = -1.0f;
        private final ScaleGestureDetector mScaleGestureDetector;
        private boolean mScaling;
        private final float mScalingThreshold;

        public MagnifiedContentInteractionStateHandler(Context context) {
            TypedValue scaleValue = new TypedValue();
            context.getResources().getValue(17104919, scaleValue, false);
            this.mScalingThreshold = scaleValue.getFloat();
            this.mScaleGestureDetector = new ScaleGestureDetector(context, this);
            this.mScaleGestureDetector.setQuickScaleEnabled(false);
            this.mGestureDetector = new GestureDetector(context, this);
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            this.mScaleGestureDetector.onTouchEvent(event);
            this.mGestureDetector.onTouchEvent(event);
            if (MagnificationGestureHandler.this.mCurrentState == 4 && event.getActionMasked() == 1) {
                clear();
                MagnificationGestureHandler.this.mMagnificationController.persistScale();
                if (MagnificationGestureHandler.this.mPreviousState == 3) {
                    MagnificationGestureHandler.this.transitionToState(3);
                } else {
                    MagnificationGestureHandler.this.transitionToState(2);
                }
            }
        }

        public boolean onScroll(MotionEvent first, MotionEvent second, float distanceX, float distanceY) {
            if (MagnificationGestureHandler.this.mCurrentState != 4) {
                return true;
            }
            MagnificationGestureHandler.this.mMagnificationController.offsetMagnifiedRegionCenter(distanceX, distanceY, 0);
            return true;
        }

        public boolean onScale(ScaleGestureDetector detector) {
            if (this.mScaling) {
                float scale;
                float initialScale = MagnificationGestureHandler.this.mMagnificationController.getScale();
                float targetScale = initialScale * detector.getScaleFactor();
                if (targetScale > MagnificationGestureHandler.MAX_SCALE && targetScale > initialScale) {
                    scale = MagnificationGestureHandler.MAX_SCALE;
                } else if (targetScale >= 2.0f || targetScale >= initialScale) {
                    scale = targetScale;
                } else {
                    scale = 2.0f;
                }
                MagnificationGestureHandler.this.mMagnificationController.setScale(scale, detector.getFocusX(), detector.getFocusY(), false, 0);
                return true;
            }
            if (this.mInitialScaleFactor < OppoBrightUtils.MIN_LUX_LIMITI) {
                this.mInitialScaleFactor = detector.getScaleFactor();
            } else if (Math.abs(detector.getScaleFactor() - this.mInitialScaleFactor) > this.mScalingThreshold) {
                this.mScaling = true;
                return true;
            }
            return false;
        }

        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return MagnificationGestureHandler.this.mCurrentState == 4;
        }

        public void onScaleEnd(ScaleGestureDetector detector) {
            clear();
        }

        public void clear() {
            this.mInitialScaleFactor = -1.0f;
            this.mScaling = false;
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
    private static final class MotionEventInfo {
        private static final int MAX_POOL_SIZE = 10;
        private static final Object sLock = null;
        private static MotionEventInfo sPool;
        private static int sPoolSize;
        public MotionEvent mEvent;
        private boolean mInPool;
        private MotionEventInfo mNext;
        public int mPolicyFlags;
        public MotionEvent mRawEvent;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.accessibility.MagnificationGestureHandler.MotionEventInfo.<clinit>():void, dex: 
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
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.accessibility.MagnificationGestureHandler.MotionEventInfo.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.MagnificationGestureHandler.MotionEventInfo.<clinit>():void");
        }

        private MotionEventInfo() {
        }

        public static MotionEventInfo obtain(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            MotionEventInfo info;
            synchronized (sLock) {
                if (sPoolSize > 0) {
                    sPoolSize--;
                    info = sPool;
                    sPool = info.mNext;
                    info.mNext = null;
                    info.mInPool = false;
                } else {
                    info = new MotionEventInfo();
                }
                info.initialize(event, rawEvent, policyFlags);
            }
            return info;
        }

        private void initialize(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            this.mEvent = MotionEvent.obtain(event);
            this.mRawEvent = MotionEvent.obtain(rawEvent);
            this.mPolicyFlags = policyFlags;
        }

        public void recycle() {
            synchronized (sLock) {
                if (this.mInPool) {
                    throw new IllegalStateException("Already recycled.");
                }
                clear();
                if (sPoolSize < 10) {
                    sPoolSize++;
                    this.mNext = sPool;
                    sPool = this;
                    this.mInPool = true;
                }
            }
        }

        private void clear() {
            this.mEvent.recycle();
            this.mEvent = null;
            this.mRawEvent.recycle();
            this.mRawEvent = null;
            this.mPolicyFlags = 0;
        }
    }

    private final class StateViewportDraggingHandler implements MotionEventHandler {
        private boolean mLastMoveOutsideMagnifiedRegion;

        /* synthetic */ StateViewportDraggingHandler(MagnificationGestureHandler this$0, StateViewportDraggingHandler stateViewportDraggingHandler) {
            this();
        }

        private StateViewportDraggingHandler() {
        }

        public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
            switch (event.getActionMasked()) {
                case 0:
                    throw new IllegalArgumentException("Unexpected event type: ACTION_DOWN");
                case 1:
                    if (!MagnificationGestureHandler.this.mTranslationEnabledBeforePan) {
                        MagnificationGestureHandler.this.mMagnificationController.reset(true);
                    }
                    clear();
                    MagnificationGestureHandler.this.transitionToState(2);
                    return;
                case 2:
                    if (event.getPointerCount() != 1) {
                        throw new IllegalStateException("Should have one pointer down.");
                    }
                    float eventX = event.getX();
                    float eventY = event.getY();
                    if (!MagnificationGestureHandler.this.mMagnificationController.magnificationRegionContains(eventX, eventY)) {
                        this.mLastMoveOutsideMagnifiedRegion = true;
                        return;
                    } else if (this.mLastMoveOutsideMagnifiedRegion) {
                        this.mLastMoveOutsideMagnifiedRegion = false;
                        MagnificationGestureHandler.this.mMagnificationController.setCenter(eventX, eventY, true, 0);
                        return;
                    } else {
                        MagnificationGestureHandler.this.mMagnificationController.setCenter(eventX, eventY, false, 0);
                        return;
                    }
                case 5:
                    clear();
                    MagnificationGestureHandler.this.transitionToState(4);
                    return;
                case 6:
                    throw new IllegalArgumentException("Unexpected event type: ACTION_POINTER_UP");
                default:
                    return;
            }
        }

        public void clear() {
            this.mLastMoveOutsideMagnifiedRegion = false;
        }
    }

    public MagnificationGestureHandler(Context context, AccessibilityManagerService ams, boolean detectControlGestures) {
        this.mAms = ams;
        this.mMagnificationController = ams.getMagnificationController();
        this.mDetectingStateHandler = new DetectingStateHandler(context);
        this.mStateViewportDraggingHandler = new StateViewportDraggingHandler(this, null);
        this.mMagnifiedContentInteractionStateHandler = new MagnifiedContentInteractionStateHandler(context);
        this.mDetectControlGestures = detectControlGestures;
        transitionToState(2);
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (!event.isFromSource(4098)) {
            if (this.mNext != null) {
                this.mNext.onMotionEvent(event, rawEvent, policyFlags);
            }
        } else if (this.mDetectControlGestures) {
            this.mMagnifiedContentInteractionStateHandler.onMotionEvent(event, rawEvent, policyFlags);
            switch (this.mCurrentState) {
                case 1:
                    handleMotionEventStateDelegating(event, rawEvent, policyFlags);
                    break;
                case 2:
                    this.mDetectingStateHandler.onMotionEvent(event, rawEvent, policyFlags);
                    break;
                case 3:
                    this.mStateViewportDraggingHandler.onMotionEvent(event, rawEvent, policyFlags);
                    break;
                case 4:
                    break;
                default:
                    throw new IllegalStateException("Unknown state: " + this.mCurrentState);
            }
        } else {
            if (this.mNext != null) {
                dispatchTransformedEvent(event, rawEvent, policyFlags);
            }
        }
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        if (this.mNext != null) {
            this.mNext.onKeyEvent(event, policyFlags);
        }
    }

    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (this.mNext != null) {
            this.mNext.onAccessibilityEvent(event);
        }
    }

    public void setNext(EventStreamTransformation next) {
        this.mNext = next;
    }

    public void clearEvents(int inputSource) {
        if (inputSource == 4098) {
            clear();
        }
        if (this.mNext != null) {
            this.mNext.clearEvents(inputSource);
        }
    }

    public void onDestroy() {
        clear();
    }

    private void clear() {
        this.mCurrentState = 2;
        this.mDetectingStateHandler.clear();
        this.mStateViewportDraggingHandler.clear();
        this.mMagnifiedContentInteractionStateHandler.clear();
    }

    private void handleMotionEventStateDelegating(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        switch (event.getActionMasked()) {
            case 0:
                this.mDelegatingStateDownTime = event.getDownTime();
                break;
            case 1:
                if (this.mDetectingStateHandler.mDelayedEventQueue == null) {
                    transitionToState(2);
                    break;
                }
                break;
        }
        if (this.mNext != null) {
            event.setDownTime(this.mDelegatingStateDownTime);
            dispatchTransformedEvent(event, rawEvent, policyFlags);
        }
    }

    private void dispatchTransformedEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        float eventX = event.getX();
        float eventY = event.getY();
        if (this.mMagnificationController.isMagnifying() && this.mMagnificationController.magnificationRegionContains(eventX, eventY)) {
            float scale = this.mMagnificationController.getScale();
            float scaledOffsetX = this.mMagnificationController.getOffsetX();
            float scaledOffsetY = this.mMagnificationController.getOffsetY();
            int pointerCount = event.getPointerCount();
            PointerCoords[] coords = getTempPointerCoordsWithMinSize(pointerCount);
            PointerProperties[] properties = getTempPointerPropertiesWithMinSize(pointerCount);
            for (int i = 0; i < pointerCount; i++) {
                event.getPointerCoords(i, coords[i]);
                coords[i].x = (coords[i].x - scaledOffsetX) / scale;
                coords[i].y = (coords[i].y - scaledOffsetY) / scale;
                event.getPointerProperties(i, properties[i]);
            }
            event = MotionEvent.obtain(event.getDownTime(), event.getEventTime(), event.getAction(), pointerCount, properties, coords, 0, 0, 1.0f, 1.0f, event.getDeviceId(), 0, event.getSource(), event.getFlags());
        }
        this.mNext.onMotionEvent(event, rawEvent, policyFlags);
    }

    private PointerCoords[] getTempPointerCoordsWithMinSize(int size) {
        int oldSize;
        if (this.mTempPointerCoords != null) {
            oldSize = this.mTempPointerCoords.length;
        } else {
            oldSize = 0;
        }
        if (oldSize < size) {
            PointerCoords[] oldTempPointerCoords = this.mTempPointerCoords;
            this.mTempPointerCoords = new PointerCoords[size];
            if (oldTempPointerCoords != null) {
                System.arraycopy(oldTempPointerCoords, 0, this.mTempPointerCoords, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerCoords[i] = new PointerCoords();
        }
        return this.mTempPointerCoords;
    }

    private PointerProperties[] getTempPointerPropertiesWithMinSize(int size) {
        int oldSize;
        if (this.mTempPointerProperties != null) {
            oldSize = this.mTempPointerProperties.length;
        } else {
            oldSize = 0;
        }
        if (oldSize < size) {
            PointerProperties[] oldTempPointerProperties = this.mTempPointerProperties;
            this.mTempPointerProperties = new PointerProperties[size];
            if (oldTempPointerProperties != null) {
                System.arraycopy(oldTempPointerProperties, 0, this.mTempPointerProperties, 0, oldSize);
            }
        }
        for (int i = oldSize; i < size; i++) {
            this.mTempPointerProperties[i] = new PointerProperties();
        }
        return this.mTempPointerProperties;
    }

    private void transitionToState(int state) {
        this.mPreviousState = this.mCurrentState;
        this.mCurrentState = state;
    }
}
