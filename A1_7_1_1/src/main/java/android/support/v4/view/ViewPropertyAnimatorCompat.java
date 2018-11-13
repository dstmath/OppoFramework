package android.support.v4.view;

import android.view.View;
import android.view.animation.Interpolator;
import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public class ViewPropertyAnimatorCompat {
    static final ViewPropertyAnimatorCompatImpl IMPL = null;
    static final int LISTENER_TAG_ID = 2113929216;
    private static final String TAG = "ViewAnimatorCompat";
    private Runnable mEndAction;
    private int mOldLayerType;
    private Runnable mStartAction;
    private WeakReference<View> mView;

    interface ViewPropertyAnimatorCompatImpl {
        void alpha(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void alphaBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void cancel(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view);

        long getDuration(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view);

        Interpolator getInterpolator(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view);

        long getStartDelay(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view);

        void rotation(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void rotationBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void rotationX(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void rotationXBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void rotationY(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void rotationYBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void scaleX(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void scaleXBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void scaleY(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void scaleYBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void setDuration(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, long j);

        void setInterpolator(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, Interpolator interpolator);

        void setListener(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, ViewPropertyAnimatorListener viewPropertyAnimatorListener);

        void setStartDelay(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, long j);

        void setUpdateListener(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, ViewPropertyAnimatorUpdateListener viewPropertyAnimatorUpdateListener);

        void start(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view);

        void translationX(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void translationXBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void translationY(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void translationYBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void withEndAction(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, Runnable runnable);

        void withLayer(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view);

        void withStartAction(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, Runnable runnable);

        void x(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void xBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void y(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);

        void yBy(ViewPropertyAnimatorCompat viewPropertyAnimatorCompat, View view, float f);
    }

    static class BaseViewPropertyAnimatorCompatImpl implements ViewPropertyAnimatorCompatImpl {
        WeakHashMap<View, Runnable> mStarterMap = null;

        class Starter implements Runnable {
            WeakReference<View> mViewRef;
            ViewPropertyAnimatorCompat mVpa;

            /* synthetic */ Starter(BaseViewPropertyAnimatorCompatImpl this$1, ViewPropertyAnimatorCompat vpa, View view, Starter starter) {
                this(vpa, view);
            }

            private Starter(ViewPropertyAnimatorCompat vpa, View view) {
                this.mViewRef = new WeakReference(view);
                this.mVpa = vpa;
            }

            public void run() {
                BaseViewPropertyAnimatorCompatImpl.this.startAnimation(this.mVpa, (View) this.mViewRef.get());
            }
        }

        BaseViewPropertyAnimatorCompatImpl() {
        }

        public void setDuration(ViewPropertyAnimatorCompat vpa, View view, long value) {
        }

        public void alpha(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void translationX(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void translationY(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void withEndAction(ViewPropertyAnimatorCompat vpa, View view, Runnable runnable) {
            vpa.mEndAction = runnable;
            postStartMessage(vpa, view);
        }

        public long getDuration(ViewPropertyAnimatorCompat vpa, View view) {
            return 0;
        }

        public void setInterpolator(ViewPropertyAnimatorCompat vpa, View view, Interpolator value) {
        }

        public Interpolator getInterpolator(ViewPropertyAnimatorCompat vpa, View view) {
            return null;
        }

        public void setStartDelay(ViewPropertyAnimatorCompat vpa, View view, long value) {
        }

        public long getStartDelay(ViewPropertyAnimatorCompat vpa, View view) {
            return 0;
        }

        public void alphaBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void rotation(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void rotationBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void rotationX(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void rotationXBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void rotationY(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void rotationYBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void scaleX(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void scaleXBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void scaleY(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void scaleYBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void cancel(ViewPropertyAnimatorCompat vpa, View view) {
            postStartMessage(vpa, view);
        }

        public void x(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void xBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void y(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void yBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void translationXBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void translationYBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            postStartMessage(vpa, view);
        }

        public void start(ViewPropertyAnimatorCompat vpa, View view) {
            removeStartMessage(view);
            startAnimation(vpa, view);
        }

        public void withLayer(ViewPropertyAnimatorCompat vpa, View view) {
        }

        public void withStartAction(ViewPropertyAnimatorCompat vpa, View view, Runnable runnable) {
            vpa.mStartAction = runnable;
            postStartMessage(vpa, view);
        }

        public void setListener(ViewPropertyAnimatorCompat vpa, View view, ViewPropertyAnimatorListener listener) {
            view.setTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID, listener);
        }

        public void setUpdateListener(ViewPropertyAnimatorCompat vpa, View view, ViewPropertyAnimatorUpdateListener listener) {
        }

        private void startAnimation(ViewPropertyAnimatorCompat vpa, View view) {
            ViewPropertyAnimatorListener listenerTag = view.getTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID);
            ViewPropertyAnimatorListener listener = null;
            if (listenerTag instanceof ViewPropertyAnimatorListener) {
                listener = listenerTag;
            }
            Runnable startAction = vpa.mStartAction;
            Runnable endAction = vpa.mEndAction;
            if (startAction != null) {
                startAction.run();
            }
            if (listener != null) {
                listener.onAnimationStart(view);
                listener.onAnimationEnd(view);
            }
            if (endAction != null) {
                endAction.run();
            }
            if (this.mStarterMap != null) {
                this.mStarterMap.remove(view);
            }
        }

        private void removeStartMessage(View view) {
            if (this.mStarterMap != null) {
                Runnable starter = (Runnable) this.mStarterMap.get(view);
                if (starter != null) {
                    view.removeCallbacks(starter);
                }
            }
        }

        private void postStartMessage(ViewPropertyAnimatorCompat vpa, View view) {
            Runnable starter = null;
            if (this.mStarterMap != null) {
                starter = (Runnable) this.mStarterMap.get(view);
            }
            if (starter == null) {
                starter = new Starter(this, vpa, view, null);
                if (this.mStarterMap == null) {
                    this.mStarterMap = new WeakHashMap();
                }
                this.mStarterMap.put(view, starter);
            }
            view.removeCallbacks(starter);
            view.post(starter);
        }
    }

    static class ICSViewPropertyAnimatorCompatImpl extends BaseViewPropertyAnimatorCompatImpl {
        WeakHashMap<View, Integer> mLayerMap = null;

        static class MyVpaListener implements ViewPropertyAnimatorListener {
            ViewPropertyAnimatorCompat mVpa;

            MyVpaListener(ViewPropertyAnimatorCompat vpa) {
                this.mVpa = vpa;
            }

            public void onAnimationStart(View view) {
                if (this.mVpa.mOldLayerType >= 0) {
                    ViewCompat.setLayerType(view, 2, null);
                }
                if (this.mVpa.mStartAction != null) {
                    this.mVpa.mStartAction.run();
                }
                ViewPropertyAnimatorListener listenerTag = view.getTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID);
                ViewPropertyAnimatorListener listener = null;
                if (listenerTag instanceof ViewPropertyAnimatorListener) {
                    listener = listenerTag;
                }
                if (listener != null) {
                    listener.onAnimationStart(view);
                }
            }

            public void onAnimationEnd(View view) {
                if (this.mVpa.mOldLayerType >= 0) {
                    ViewCompat.setLayerType(view, this.mVpa.mOldLayerType, null);
                    this.mVpa.mOldLayerType = -1;
                }
                if (this.mVpa.mEndAction != null) {
                    this.mVpa.mEndAction.run();
                }
                ViewPropertyAnimatorListener listenerTag = view.getTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID);
                ViewPropertyAnimatorListener listener = null;
                if (listenerTag instanceof ViewPropertyAnimatorListener) {
                    listener = listenerTag;
                }
                if (listener != null) {
                    listener.onAnimationEnd(view);
                }
            }

            public void onAnimationCancel(View view) {
                ViewPropertyAnimatorListener listenerTag = view.getTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID);
                ViewPropertyAnimatorListener listener = null;
                if (listenerTag instanceof ViewPropertyAnimatorListener) {
                    listener = listenerTag;
                }
                if (listener != null) {
                    listener.onAnimationCancel(view);
                }
            }
        }

        ICSViewPropertyAnimatorCompatImpl() {
        }

        public void setDuration(ViewPropertyAnimatorCompat vpa, View view, long value) {
            ViewPropertyAnimatorCompatICS.setDuration(view, value);
        }

        public void alpha(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.alpha(view, value);
        }

        public void translationX(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.translationX(view, value);
        }

        public void translationY(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.translationY(view, value);
        }

        public long getDuration(ViewPropertyAnimatorCompat vpa, View view) {
            return ViewPropertyAnimatorCompatICS.getDuration(view);
        }

        public void setInterpolator(ViewPropertyAnimatorCompat vpa, View view, Interpolator value) {
            ViewPropertyAnimatorCompatICS.setInterpolator(view, value);
        }

        public void setStartDelay(ViewPropertyAnimatorCompat vpa, View view, long value) {
            ViewPropertyAnimatorCompatICS.setStartDelay(view, value);
        }

        public long getStartDelay(ViewPropertyAnimatorCompat vpa, View view) {
            return ViewPropertyAnimatorCompatICS.getStartDelay(view);
        }

        public void alphaBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.alphaBy(view, value);
        }

        public void rotation(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.rotation(view, value);
        }

        public void rotationBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.rotationBy(view, value);
        }

        public void rotationX(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.rotationX(view, value);
        }

        public void rotationXBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.rotationXBy(view, value);
        }

        public void rotationY(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.rotationY(view, value);
        }

        public void rotationYBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.rotationYBy(view, value);
        }

        public void scaleX(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.scaleX(view, value);
        }

        public void scaleXBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.scaleXBy(view, value);
        }

        public void scaleY(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.scaleY(view, value);
        }

        public void scaleYBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.scaleYBy(view, value);
        }

        public void cancel(ViewPropertyAnimatorCompat vpa, View view) {
            ViewPropertyAnimatorCompatICS.cancel(view);
        }

        public void x(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.x(view, value);
        }

        public void xBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.xBy(view, value);
        }

        public void y(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.y(view, value);
        }

        public void yBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.yBy(view, value);
        }

        public void translationXBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.translationXBy(view, value);
        }

        public void translationYBy(ViewPropertyAnimatorCompat vpa, View view, float value) {
            ViewPropertyAnimatorCompatICS.translationYBy(view, value);
        }

        public void start(ViewPropertyAnimatorCompat vpa, View view) {
            ViewPropertyAnimatorCompatICS.start(view);
        }

        public void setListener(ViewPropertyAnimatorCompat vpa, View view, ViewPropertyAnimatorListener listener) {
            view.setTag(ViewPropertyAnimatorCompat.LISTENER_TAG_ID, listener);
            ViewPropertyAnimatorCompatICS.setListener(view, new MyVpaListener(vpa));
        }

        public void withEndAction(ViewPropertyAnimatorCompat vpa, View view, Runnable runnable) {
            ViewPropertyAnimatorCompatICS.setListener(view, new MyVpaListener(vpa));
            vpa.mEndAction = runnable;
        }

        public void withStartAction(ViewPropertyAnimatorCompat vpa, View view, Runnable runnable) {
            ViewPropertyAnimatorCompatICS.setListener(view, new MyVpaListener(vpa));
            vpa.mStartAction = runnable;
        }

        public void withLayer(ViewPropertyAnimatorCompat vpa, View view) {
            vpa.mOldLayerType = ViewCompat.getLayerType(view);
            ViewPropertyAnimatorCompatICS.setListener(view, new MyVpaListener(vpa));
        }
    }

    static class JBViewPropertyAnimatorCompatImpl extends ICSViewPropertyAnimatorCompatImpl {
        JBViewPropertyAnimatorCompatImpl() {
        }

        public void setListener(ViewPropertyAnimatorCompat vpa, View view, ViewPropertyAnimatorListener listener) {
            ViewPropertyAnimatorCompatJB.setListener(view, listener);
        }

        public void withStartAction(ViewPropertyAnimatorCompat vpa, View view, Runnable runnable) {
            ViewPropertyAnimatorCompatJB.withStartAction(view, runnable);
        }

        public void withEndAction(ViewPropertyAnimatorCompat vpa, View view, Runnable runnable) {
            ViewPropertyAnimatorCompatJB.withEndAction(view, runnable);
        }

        public void withLayer(ViewPropertyAnimatorCompat vpa, View view) {
            ViewPropertyAnimatorCompatJB.withLayer(view);
        }
    }

    static class JBMr2ViewPropertyAnimatorCompatImpl extends JBViewPropertyAnimatorCompatImpl {
        JBMr2ViewPropertyAnimatorCompatImpl() {
        }

        public Interpolator getInterpolator(ViewPropertyAnimatorCompat vpa, View view) {
            return ViewPropertyAnimatorCompatJellybeanMr2.getInterpolator(view);
        }
    }

    static class KitKatViewPropertyAnimatorCompatImpl extends JBMr2ViewPropertyAnimatorCompatImpl {
        KitKatViewPropertyAnimatorCompatImpl() {
        }

        public void setUpdateListener(ViewPropertyAnimatorCompat vpa, View view, ViewPropertyAnimatorUpdateListener listener) {
            ViewPropertyAnimatorCompatKK.setUpdateListener(view, listener);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.support.v4.view.ViewPropertyAnimatorCompat.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.support.v4.view.ViewPropertyAnimatorCompat.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.view.ViewPropertyAnimatorCompat.<clinit>():void");
    }

    ViewPropertyAnimatorCompat(View view) {
        this.mStartAction = null;
        this.mEndAction = null;
        this.mOldLayerType = -1;
        this.mView = new WeakReference(view);
    }

    public ViewPropertyAnimatorCompat setDuration(long value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.setDuration(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat alpha(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.alpha(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat alphaBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.alphaBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationX(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.translationX(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationY(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.translationY(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat withEndAction(Runnable runnable) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.withEndAction(this, view, runnable);
        }
        return this;
    }

    public long getDuration() {
        View view = (View) this.mView.get();
        if (view != null) {
            return IMPL.getDuration(this, view);
        }
        return 0;
    }

    public ViewPropertyAnimatorCompat setInterpolator(Interpolator value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.setInterpolator(this, view, value);
        }
        return this;
    }

    public Interpolator getInterpolator() {
        View view = (View) this.mView.get();
        if (view != null) {
            return IMPL.getInterpolator(this, view);
        }
        return null;
    }

    public ViewPropertyAnimatorCompat setStartDelay(long value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.setStartDelay(this, view, value);
        }
        return this;
    }

    public long getStartDelay() {
        View view = (View) this.mView.get();
        if (view != null) {
            return IMPL.getStartDelay(this, view);
        }
        return 0;
    }

    public ViewPropertyAnimatorCompat rotation(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.rotation(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.rotationBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationX(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.rotationX(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationXBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.rotationXBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationY(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.rotationY(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat rotationYBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.rotationYBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleX(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.scaleX(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleXBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.scaleXBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleY(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.scaleY(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat scaleYBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.scaleYBy(this, view, value);
        }
        return this;
    }

    public void cancel() {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.cancel(this, view);
        }
    }

    public ViewPropertyAnimatorCompat x(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.x(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat xBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.xBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat y(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.y(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat yBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.yBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationXBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.translationXBy(this, view, value);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat translationYBy(float value) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.translationYBy(this, view, value);
        }
        return this;
    }

    public void start() {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.start(this, view);
        }
    }

    public ViewPropertyAnimatorCompat withLayer() {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.withLayer(this, view);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat withStartAction(Runnable runnable) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.withStartAction(this, view, runnable);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat setListener(ViewPropertyAnimatorListener listener) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.setListener(this, view, listener);
        }
        return this;
    }

    public ViewPropertyAnimatorCompat setUpdateListener(ViewPropertyAnimatorUpdateListener listener) {
        View view = (View) this.mView.get();
        if (view != null) {
            IMPL.setUpdateListener(this, view, listener);
        }
        return this;
    }
}
