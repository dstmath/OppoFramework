package com.android.server.accessibility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerCoords;
import android.view.MotionEvent.PointerProperties;
import android.view.accessibility.AccessibilityEvent;

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
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AutoclickController implements EventStreamTransformation {
    private static final String LOG_TAG = null;
    private ClickDelayObserver mClickDelayObserver;
    private ClickScheduler mClickScheduler;
    private final Context mContext;
    private EventStreamTransformation mNext;
    private final int mUserId;

    private static final class ClickDelayObserver extends ContentObserver {
        private final Uri mAutoclickDelaySettingUri = Secure.getUriFor("accessibility_autoclick_delay");
        private ClickScheduler mClickScheduler;
        private ContentResolver mContentResolver;
        private final int mUserId;

        public ClickDelayObserver(int userId, Handler handler) {
            super(handler);
            this.mUserId = userId;
        }

        public void start(ContentResolver contentResolver, ClickScheduler clickScheduler) {
            if (this.mContentResolver != null || this.mClickScheduler != null) {
                throw new IllegalStateException("Observer already started.");
            } else if (contentResolver == null) {
                throw new NullPointerException("contentResolver not set.");
            } else if (clickScheduler == null) {
                throw new NullPointerException("clickScheduler not set.");
            } else {
                this.mContentResolver = contentResolver;
                this.mClickScheduler = clickScheduler;
                this.mContentResolver.registerContentObserver(this.mAutoclickDelaySettingUri, false, this, this.mUserId);
                onChange(true, this.mAutoclickDelaySettingUri);
            }
        }

        public void stop() {
            if (this.mContentResolver == null || this.mClickScheduler == null) {
                throw new IllegalStateException("ClickDelayObserver not started.");
            }
            this.mContentResolver.unregisterContentObserver(this);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.mAutoclickDelaySettingUri.equals(uri)) {
                this.mClickScheduler.updateDelay(Secure.getIntForUser(this.mContentResolver, "accessibility_autoclick_delay", 600, this.mUserId));
            }
        }
    }

    private final class ClickScheduler implements Runnable {
        private static final double MOVEMENT_SLOPE = 20.0d;
        private boolean mActive;
        private PointerCoords mAnchorCoords;
        private int mDelay;
        private int mEventPolicyFlags;
        private Handler mHandler;
        private MotionEvent mLastMotionEvent = null;
        private int mMetaState;
        private long mScheduledClickTime;
        private PointerCoords[] mTempPointerCoords;
        private PointerProperties[] mTempPointerProperties;

        public ClickScheduler(Handler handler, int delay) {
            this.mHandler = handler;
            resetInternalState();
            this.mDelay = delay;
            this.mAnchorCoords = new PointerCoords();
        }

        public void run() {
            long now = SystemClock.uptimeMillis();
            if (now < this.mScheduledClickTime) {
                this.mHandler.postDelayed(this, this.mScheduledClickTime - now);
                return;
            }
            sendClick();
            resetInternalState();
        }

        public void update(MotionEvent event, int policyFlags) {
            this.mMetaState = event.getMetaState();
            boolean moved = detectMovement(event);
            cacheLastEvent(event, policyFlags, this.mLastMotionEvent != null ? moved : true);
            if (moved) {
                rescheduleClick(this.mDelay);
            }
        }

        public void cancel() {
            if (this.mActive) {
                resetInternalState();
                this.mHandler.removeCallbacks(this);
            }
        }

        public void updateMetaState(int state) {
            this.mMetaState = state;
        }

        public void updateDelay(int delay) {
            this.mDelay = delay;
        }

        private void rescheduleClick(int delay) {
            long clickTime = SystemClock.uptimeMillis() + ((long) delay);
            if (!this.mActive || clickTime <= this.mScheduledClickTime) {
                if (this.mActive) {
                    this.mHandler.removeCallbacks(this);
                }
                this.mActive = true;
                this.mScheduledClickTime = clickTime;
                this.mHandler.postDelayed(this, (long) delay);
                return;
            }
            this.mScheduledClickTime = clickTime;
        }

        private void cacheLastEvent(MotionEvent event, int policyFlags, boolean useAsAnchor) {
            if (this.mLastMotionEvent != null) {
                this.mLastMotionEvent.recycle();
            }
            this.mLastMotionEvent = MotionEvent.obtain(event);
            this.mEventPolicyFlags = policyFlags;
            if (useAsAnchor) {
                this.mLastMotionEvent.getPointerCoords(this.mLastMotionEvent.getActionIndex(), this.mAnchorCoords);
            }
        }

        private void resetInternalState() {
            this.mActive = false;
            if (this.mLastMotionEvent != null) {
                this.mLastMotionEvent.recycle();
                this.mLastMotionEvent = null;
            }
            this.mScheduledClickTime = -1;
        }

        private boolean detectMovement(MotionEvent event) {
            boolean z = false;
            if (this.mLastMotionEvent == null) {
                return false;
            }
            int pointerIndex = event.getActionIndex();
            if (Math.hypot((double) (this.mAnchorCoords.x - event.getX(pointerIndex)), (double) (this.mAnchorCoords.y - event.getY(pointerIndex))) > MOVEMENT_SLOPE) {
                z = true;
            }
            return z;
        }

        private void sendClick() {
            if (this.mLastMotionEvent != null && AutoclickController.this.mNext != null) {
                int pointerIndex = this.mLastMotionEvent.getActionIndex();
                if (this.mTempPointerProperties == null) {
                    this.mTempPointerProperties = new PointerProperties[1];
                    this.mTempPointerProperties[0] = new PointerProperties();
                }
                this.mLastMotionEvent.getPointerProperties(pointerIndex, this.mTempPointerProperties[0]);
                if (this.mTempPointerCoords == null) {
                    this.mTempPointerCoords = new PointerCoords[1];
                    this.mTempPointerCoords[0] = new PointerCoords();
                }
                this.mLastMotionEvent.getPointerCoords(pointerIndex, this.mTempPointerCoords[0]);
                long now = SystemClock.uptimeMillis();
                MotionEvent downEvent = MotionEvent.obtain(now, now, 0, 1, this.mTempPointerProperties, this.mTempPointerCoords, this.mMetaState, 1, 1.0f, 1.0f, this.mLastMotionEvent.getDeviceId(), 0, this.mLastMotionEvent.getSource(), this.mLastMotionEvent.getFlags());
                MotionEvent upEvent = MotionEvent.obtain(downEvent);
                upEvent.setAction(1);
                AutoclickController.this.mNext.onMotionEvent(downEvent, downEvent, this.mEventPolicyFlags);
                downEvent.recycle();
                AutoclickController.this.mNext.onMotionEvent(upEvent, upEvent, this.mEventPolicyFlags);
                upEvent.recycle();
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("ClickScheduler: { active=").append(this.mActive);
            builder.append(", delay=").append(this.mDelay);
            builder.append(", scheduledClickTime=").append(this.mScheduledClickTime);
            builder.append(", anchor={x:").append(this.mAnchorCoords.x);
            builder.append(", y:").append(this.mAnchorCoords.y).append("}");
            builder.append(", metastate=").append(this.mMetaState);
            builder.append(", policyFlags=").append(this.mEventPolicyFlags);
            builder.append(", lastMotionEvent=").append(this.mLastMotionEvent);
            builder.append(" }");
            return builder.toString();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.accessibility.AutoclickController.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.accessibility.AutoclickController.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.AutoclickController.<clinit>():void");
    }

    public AutoclickController(Context context, int userId) {
        this.mContext = context;
        this.mUserId = userId;
    }

    public void onMotionEvent(MotionEvent event, MotionEvent rawEvent, int policyFlags) {
        if (event.isFromSource(8194)) {
            if (this.mClickScheduler == null) {
                Handler handler = new Handler(this.mContext.getMainLooper());
                this.mClickScheduler = new ClickScheduler(handler, 600);
                this.mClickDelayObserver = new ClickDelayObserver(this.mUserId, handler);
                this.mClickDelayObserver.start(this.mContext.getContentResolver(), this.mClickScheduler);
            }
            handleMouseMotion(event, policyFlags);
        } else if (this.mClickScheduler != null) {
            this.mClickScheduler.cancel();
        }
        if (this.mNext != null) {
            this.mNext.onMotionEvent(event, rawEvent, policyFlags);
        }
    }

    public void onKeyEvent(KeyEvent event, int policyFlags) {
        if (this.mClickScheduler != null) {
            if (KeyEvent.isModifierKey(event.getKeyCode())) {
                this.mClickScheduler.updateMetaState(event.getMetaState());
            } else {
                this.mClickScheduler.cancel();
            }
        }
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
        if (inputSource == 8194 && this.mClickScheduler != null) {
            this.mClickScheduler.cancel();
        }
        if (this.mNext != null) {
            this.mNext.clearEvents(inputSource);
        }
    }

    public void onDestroy() {
        if (this.mClickDelayObserver != null) {
            this.mClickDelayObserver.stop();
            this.mClickDelayObserver = null;
        }
        if (this.mClickScheduler != null) {
            this.mClickScheduler.cancel();
            this.mClickScheduler = null;
        }
    }

    private void handleMouseMotion(MotionEvent event, int policyFlags) {
        switch (event.getActionMasked()) {
            case 7:
                if (event.getPointerCount() == 1) {
                    this.mClickScheduler.update(event, policyFlags);
                    return;
                } else {
                    this.mClickScheduler.cancel();
                    return;
                }
            case 9:
            case 10:
                return;
            default:
                this.mClickScheduler.cancel();
                return;
        }
    }
}
