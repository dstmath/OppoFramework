package android.animation;

import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.Choreographer;
import android.view.Choreographer.FrameCallback;
import java.util.ArrayList;

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
public class AnimationHandler {
    public static final ThreadLocal<AnimationHandler> sAnimatorHandler = null;
    private final ArrayList<AnimationFrameCallback> mAnimationCallbacks;
    private final ArrayList<AnimationFrameCallback> mCommitCallbacks;
    private final ArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime;
    private final FrameCallback mFrameCallback;
    private boolean mListDirty;
    private AnimationFrameCallbackProvider mProvider;

    interface AnimationFrameCallback {
        void commitAnimationFrame(long j);

        void doAnimationFrame(long j);
    }

    public interface AnimationFrameCallbackProvider {
        long getFrameDelay();

        long getFrameTime();

        void postCommitCallback(Runnable runnable);

        void postFrameCallback(FrameCallback frameCallback);

        void setFrameDelay(long j);
    }

    private class MyFrameCallbackProvider implements AnimationFrameCallbackProvider {
        final Choreographer mChoreographer;

        /* synthetic */ MyFrameCallbackProvider(AnimationHandler this$0, MyFrameCallbackProvider myFrameCallbackProvider) {
            this();
        }

        private MyFrameCallbackProvider() {
            this.mChoreographer = Choreographer.getInstance();
        }

        public void postFrameCallback(FrameCallback callback) {
            this.mChoreographer.postFrameCallback(callback);
        }

        public void postCommitCallback(Runnable runnable) {
            this.mChoreographer.postCallback(3, runnable, null);
        }

        public long getFrameTime() {
            return this.mChoreographer.getFrameTime();
        }

        public long getFrameDelay() {
            return Choreographer.getFrameDelay();
        }

        public void setFrameDelay(long delay) {
            Choreographer.setFrameDelay(delay);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.animation.AnimationHandler.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.animation.AnimationHandler.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.animation.AnimationHandler.<clinit>():void");
    }

    public AnimationHandler() {
        this.mDelayedCallbackStartTime = new ArrayMap();
        this.mAnimationCallbacks = new ArrayList();
        this.mCommitCallbacks = new ArrayList();
        this.mFrameCallback = new FrameCallback() {
            public void doFrame(long frameTimeNanos) {
                AnimationHandler.this.doAnimationFrame(AnimationHandler.this.getProvider().getFrameTime());
                if (AnimationHandler.this.mAnimationCallbacks.size() > 0) {
                    AnimationHandler.this.getProvider().postFrameCallback(this);
                }
            }
        };
        this.mListDirty = false;
    }

    public static AnimationHandler getInstance() {
        if (sAnimatorHandler.get() == null) {
            sAnimatorHandler.set(new AnimationHandler());
        }
        return (AnimationHandler) sAnimatorHandler.get();
    }

    public void setProvider(AnimationFrameCallbackProvider provider) {
        if (provider == null) {
            this.mProvider = new MyFrameCallbackProvider(this, null);
        } else {
            this.mProvider = provider;
        }
    }

    private AnimationFrameCallbackProvider getProvider() {
        if (this.mProvider == null) {
            this.mProvider = new MyFrameCallbackProvider(this, null);
        }
        return this.mProvider;
    }

    public void addAnimationFrameCallback(AnimationFrameCallback callback, long delay) {
        if (this.mAnimationCallbacks.size() == 0) {
            getProvider().postFrameCallback(this.mFrameCallback);
        }
        if (!this.mAnimationCallbacks.contains(callback)) {
            this.mAnimationCallbacks.add(callback);
        }
        if (delay > 0) {
            this.mDelayedCallbackStartTime.put(callback, Long.valueOf(SystemClock.uptimeMillis() + delay));
        }
    }

    public void addOneShotCommitCallback(AnimationFrameCallback callback) {
        if (!this.mCommitCallbacks.contains(callback)) {
            this.mCommitCallbacks.add(callback);
        }
    }

    public void removeCallback(AnimationFrameCallback callback) {
        this.mCommitCallbacks.remove(callback);
        this.mDelayedCallbackStartTime.remove(callback);
        int id = this.mAnimationCallbacks.indexOf(callback);
        if (id >= 0) {
            this.mAnimationCallbacks.set(id, null);
            this.mListDirty = true;
        }
    }

    private void doAnimationFrame(long frameTime) {
        int size = this.mAnimationCallbacks.size();
        long currentTime = SystemClock.uptimeMillis();
        for (int i = 0; i < size; i++) {
            final AnimationFrameCallback callback = (AnimationFrameCallback) this.mAnimationCallbacks.get(i);
            if (callback != null && isCallbackDue(callback, currentTime)) {
                callback.doAnimationFrame(frameTime);
                if (this.mCommitCallbacks.contains(callback)) {
                    getProvider().postCommitCallback(new Runnable() {
                        public void run() {
                            AnimationHandler.this.commitAnimationFrame(callback, AnimationHandler.this.getProvider().getFrameTime());
                        }
                    });
                }
            }
        }
        cleanUpList();
    }

    private void commitAnimationFrame(AnimationFrameCallback callback, long frameTime) {
        if (!this.mDelayedCallbackStartTime.containsKey(callback) && this.mCommitCallbacks.contains(callback)) {
            callback.commitAnimationFrame(frameTime);
            this.mCommitCallbacks.remove(callback);
        }
    }

    private boolean isCallbackDue(AnimationFrameCallback callback, long currentTime) {
        Long startTime = (Long) this.mDelayedCallbackStartTime.get(callback);
        if (startTime == null) {
            return true;
        }
        if (startTime.longValue() >= currentTime) {
            return false;
        }
        this.mDelayedCallbackStartTime.remove(callback);
        return true;
    }

    public static int getAnimationCount() {
        AnimationHandler handler = (AnimationHandler) sAnimatorHandler.get();
        if (handler == null) {
            return 0;
        }
        return handler.getCallbackSize();
    }

    public static void setFrameDelay(long delay) {
        getInstance().getProvider().setFrameDelay(delay);
    }

    public static long getFrameDelay() {
        return getInstance().getProvider().getFrameDelay();
    }

    void autoCancelBasedOn(ObjectAnimator objectAnimator) {
        for (int i = this.mAnimationCallbacks.size() - 1; i >= 0; i--) {
            AnimationFrameCallback cb = (AnimationFrameCallback) this.mAnimationCallbacks.get(i);
            if (cb != null && objectAnimator.shouldAutoCancel(cb)) {
                ((Animator) this.mAnimationCallbacks.get(i)).cancel();
            }
        }
    }

    private void cleanUpList() {
        if (this.mListDirty) {
            for (int i = this.mAnimationCallbacks.size() - 1; i >= 0; i--) {
                if (this.mAnimationCallbacks.get(i) == null) {
                    this.mAnimationCallbacks.remove(i);
                }
            }
            this.mListDirty = false;
        }
    }

    private int getCallbackSize() {
        int count = 0;
        for (int i = this.mAnimationCallbacks.size() - 1; i >= 0; i--) {
            if (this.mAnimationCallbacks.get(i) != null) {
                count++;
            }
        }
        return count;
    }
}
