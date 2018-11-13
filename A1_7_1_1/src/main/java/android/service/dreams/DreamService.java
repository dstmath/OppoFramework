package android.service.dreams;

import android.app.Service;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.IBinder;
import android.os.IRemoteCallback;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.service.dreams.IDreamService.Stub;
import android.util.MathUtils;
import android.util.Slog;
import android.view.ActionMode;
import android.view.Display;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SearchEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.Window.Callback;
import android.view.WindowManager;
import android.view.WindowManager.BadTokenException;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.policy.PhoneWindow;
import com.android.internal.util.DumpUtils.Dump;
import java.io.FileDescriptor;
import java.io.PrintWriter;

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
public class DreamService extends Service implements Callback {
    public static final String DREAM_META_DATA = "android.service.dream";
    public static final String DREAM_SERVICE = "dreams";
    public static final String SERVICE_INTERFACE = "android.service.dreams.DreamService";
    private final String TAG;
    private boolean mCanDoze;
    private boolean mDebug;
    private int mDozeScreenBrightness;
    private int mDozeScreenState;
    private boolean mDozing;
    private boolean mFinished;
    private boolean mFullscreen;
    private final Handler mHandler;
    private boolean mInteractive;
    private boolean mLowProfile;
    private final IDreamManager mSandman;
    private boolean mScreenBright;
    private boolean mStarted;
    private boolean mWaking;
    private Window mWindow;
    private IBinder mWindowToken;
    private boolean mWindowless;

    /* renamed from: android.service.dreams.DreamService$2 */
    class AnonymousClass2 implements Dump {
        final /* synthetic */ DreamService this$0;
        final /* synthetic */ String[] val$args;
        final /* synthetic */ FileDescriptor val$fd;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.service.dreams.DreamService.2.<init>(android.service.dreams.DreamService, java.io.FileDescriptor, java.lang.String[]):void, dex: 
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
        AnonymousClass2(android.service.dreams.DreamService r1, java.io.FileDescriptor r2, java.lang.String[] r3) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.service.dreams.DreamService.2.<init>(android.service.dreams.DreamService, java.io.FileDescriptor, java.lang.String[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.dreams.DreamService.2.<init>(android.service.dreams.DreamService, java.io.FileDescriptor, java.lang.String[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.service.dreams.DreamService.2.dump(java.io.PrintWriter, java.lang.String):void, dex: 
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
        public void dump(java.io.PrintWriter r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.service.dreams.DreamService.2.dump(java.io.PrintWriter, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.service.dreams.DreamService.2.dump(java.io.PrintWriter, java.lang.String):void");
        }
    }

    private final class DreamServiceWrapper extends Stub {
        final /* synthetic */ DreamService this$0;

        /* renamed from: android.service.dreams.DreamService$DreamServiceWrapper$2 */
        class AnonymousClass2 implements Runnable {
            final /* synthetic */ DreamServiceWrapper this$1;

            AnonymousClass2(DreamServiceWrapper this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.this$0.detach();
            }
        }

        /* renamed from: android.service.dreams.DreamService$DreamServiceWrapper$3 */
        class AnonymousClass3 implements Runnable {
            final /* synthetic */ DreamServiceWrapper this$1;

            AnonymousClass3(DreamServiceWrapper this$1) {
                this.this$1 = this$1;
            }

            public void run() {
                this.this$1.this$0.wakeUp(true);
            }
        }

        /* synthetic */ DreamServiceWrapper(DreamService this$0, DreamServiceWrapper dreamServiceWrapper) {
            this(this$0);
        }

        private DreamServiceWrapper(DreamService this$0) {
            this.this$0 = this$0;
        }

        public void attach(final IBinder windowToken, final boolean canDoze, final IRemoteCallback started) {
            this.this$0.mHandler.post(new Runnable(this) {
                final /* synthetic */ DreamServiceWrapper this$1;

                public void run() {
                    this.this$1.this$0.attach(windowToken, canDoze, started);
                }
            });
        }

        public void detach() {
            this.this$0.mHandler.post(new AnonymousClass2(this));
        }

        public void wakeUp() {
            this.this$0.mHandler.post(new AnonymousClass3(this));
        }
    }

    public DreamService() {
        this.TAG = DreamService.class.getSimpleName() + "[" + getClass().getSimpleName() + "]";
        this.mHandler = new Handler();
        this.mLowProfile = true;
        this.mScreenBright = true;
        this.mDozeScreenState = 0;
        this.mDozeScreenBrightness = -1;
        this.mDebug = false;
        this.mSandman = IDreamManager.Stub.asInterface(ServiceManager.getService(DREAM_SERVICE));
    }

    public void setDebug(boolean dbg) {
        this.mDebug = dbg;
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        if (!this.mInteractive) {
            if (this.mDebug) {
                Slog.v(this.TAG, "Waking up on keyEvent");
            }
            wakeUp();
            return true;
        } else if (event.getKeyCode() != 4) {
            return this.mWindow.superDispatchKeyEvent(event);
        } else {
            if (this.mDebug) {
                Slog.v(this.TAG, "Waking up on back key");
            }
            wakeUp();
            return true;
        }
    }

    public boolean dispatchKeyShortcutEvent(KeyEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchKeyShortcutEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on keyShortcutEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchTouchEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on touchEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchTrackballEvent(MotionEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchTrackballEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on trackballEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event) {
        if (this.mInteractive) {
            return this.mWindow.superDispatchGenericMotionEvent(event);
        }
        if (this.mDebug) {
            Slog.v(this.TAG, "Waking up on genericMotionEvent");
        }
        wakeUp();
        return true;
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
        return false;
    }

    public View onCreatePanelView(int featureId) {
        return null;
    }

    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        return false;
    }

    public boolean onPreparePanel(int featureId, View view, Menu menu) {
        return false;
    }

    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        return false;
    }

    public void onWindowAttributesChanged(LayoutParams attrs) {
    }

    public void onContentChanged() {
    }

    public void onWindowFocusChanged(boolean hasFocus) {
    }

    public void onAttachedToWindow() {
    }

    public void onDetachedFromWindow() {
    }

    public void onPanelClosed(int featureId, Menu menu) {
    }

    public boolean onSearchRequested(SearchEvent event) {
        return onSearchRequested();
    }

    public boolean onSearchRequested() {
        return false;
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback) {
        return null;
    }

    public ActionMode onWindowStartingActionMode(ActionMode.Callback callback, int type) {
        return null;
    }

    public void onActionModeStarted(ActionMode mode) {
    }

    public void onActionModeFinished(ActionMode mode) {
    }

    public WindowManager getWindowManager() {
        return this.mWindow != null ? this.mWindow.getWindowManager() : null;
    }

    public Window getWindow() {
        return this.mWindow;
    }

    public void setContentView(int layoutResID) {
        getWindow().setContentView(layoutResID);
    }

    public void setContentView(View view) {
        getWindow().setContentView(view);
    }

    public void setContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().setContentView(view, params);
    }

    public void addContentView(View view, ViewGroup.LayoutParams params) {
        getWindow().addContentView(view, params);
    }

    public View findViewById(int id) {
        return getWindow().findViewById(id);
    }

    public void setInteractive(boolean interactive) {
        this.mInteractive = interactive;
    }

    public boolean isInteractive() {
        return this.mInteractive;
    }

    public void setLowProfile(boolean lowProfile) {
        if (this.mLowProfile != lowProfile) {
            int i;
            this.mLowProfile = lowProfile;
            if (this.mLowProfile) {
                i = 1;
            } else {
                i = 0;
            }
            applySystemUiVisibilityFlags(i, 1);
        }
    }

    public boolean isLowProfile() {
        return getSystemUiVisibilityFlagValue(1, this.mLowProfile);
    }

    public void setFullscreen(boolean fullscreen) {
        if (this.mFullscreen != fullscreen) {
            int i;
            this.mFullscreen = fullscreen;
            if (this.mFullscreen) {
                i = 1024;
            } else {
                i = 0;
            }
            applyWindowFlags(i, 1024);
        }
    }

    public boolean isFullscreen() {
        return this.mFullscreen;
    }

    public void setScreenBright(boolean screenBright) {
        if (this.mScreenBright != screenBright) {
            int i;
            this.mScreenBright = screenBright;
            if (this.mScreenBright) {
                i = 128;
            } else {
                i = 0;
            }
            applyWindowFlags(i, 128);
        }
    }

    public boolean isScreenBright() {
        return getWindowFlagValue(128, this.mScreenBright);
    }

    public void setWindowless(boolean windowless) {
        this.mWindowless = windowless;
    }

    public boolean isWindowless() {
        return this.mWindowless;
    }

    public boolean canDoze() {
        return this.mCanDoze;
    }

    public void startDozing() {
        if (this.mCanDoze && !this.mDozing) {
            this.mDozing = true;
            updateDoze();
        }
    }

    private void updateDoze() {
        if (this.mDozing) {
            try {
                this.mSandman.startDozing(this.mWindowToken, this.mDozeScreenState, this.mDozeScreenBrightness);
            } catch (RemoteException e) {
            }
        }
    }

    public void stopDozing() {
        if (this.mDozing) {
            this.mDozing = false;
            try {
                this.mSandman.stopDozing(this.mWindowToken);
            } catch (RemoteException e) {
            }
        }
    }

    public boolean isDozing() {
        return this.mDozing;
    }

    public int getDozeScreenState() {
        return this.mDozeScreenState;
    }

    public void setDozeScreenState(int state) {
        if (this.mDozeScreenState != state) {
            this.mDozeScreenState = state;
            updateDoze();
        }
    }

    public int getDozeScreenBrightness() {
        return this.mDozeScreenBrightness;
    }

    public void setDozeScreenBrightness(int brightness) {
        if (brightness != -1) {
            brightness = clampAbsoluteBrightness(brightness);
        }
        if (this.mDozeScreenBrightness != brightness) {
            this.mDozeScreenBrightness = brightness;
            updateDoze();
        }
    }

    public void onCreate() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onCreate()");
        }
        super.onCreate();
    }

    public void onDreamingStarted() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onDreamingStarted()");
        }
    }

    public void onDreamingStopped() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onDreamingStopped()");
        }
    }

    public void onWakeUp() {
        finish();
    }

    public final IBinder onBind(Intent intent) {
        if (this.mDebug) {
            Slog.v(this.TAG, "onBind() intent = " + intent);
        }
        return new DreamServiceWrapper(this, null);
    }

    public final void finish() {
        if (this.mDebug) {
            Slog.v(this.TAG, "finish(): mFinished=" + this.mFinished);
        }
        if (!this.mFinished) {
            this.mFinished = true;
            if (this.mWindowToken == null) {
                Slog.w(this.TAG, "Finish was called before the dream was attached.");
            } else {
                try {
                    this.mSandman.finishSelf(this.mWindowToken, true);
                } catch (RemoteException e) {
                }
            }
            stopSelf();
        }
    }

    public final void wakeUp() {
        wakeUp(false);
    }

    private void wakeUp(boolean fromSystem) {
        if (this.mDebug) {
            Slog.v(this.TAG, "wakeUp(): fromSystem=" + fromSystem + ", mWaking=" + this.mWaking + ", mFinished=" + this.mFinished);
        }
        if (!this.mWaking && !this.mFinished) {
            this.mWaking = true;
            onWakeUp();
            if (!fromSystem && !this.mFinished) {
                if (this.mWindowToken == null) {
                    Slog.w(this.TAG, "WakeUp was called before the dream was attached.");
                    return;
                }
                try {
                    this.mSandman.finishSelf(this.mWindowToken, false);
                } catch (RemoteException e) {
                }
            }
        }
    }

    public void onDestroy() {
        if (this.mDebug) {
            Slog.v(this.TAG, "onDestroy()");
        }
        detach();
        super.onDestroy();
    }

    private final void detach() {
        if (this.mStarted) {
            if (this.mDebug) {
                Slog.v(this.TAG, "detach(): Calling onDreamingStopped()");
            }
            this.mStarted = false;
            onDreamingStopped();
        }
        if (this.mWindow != null) {
            if (this.mDebug) {
                Slog.v(this.TAG, "detach(): Removing window from window manager");
            }
            this.mWindow.getWindowManager().removeViewImmediate(this.mWindow.getDecorView());
            this.mWindow = null;
        }
        if (this.mWindowToken != null) {
            WindowManagerGlobal.getInstance().closeAll(this.mWindowToken, getClass().getName(), "Dream");
            this.mWindowToken = null;
            this.mCanDoze = false;
        }
    }

    private final void attach(IBinder windowToken, boolean canDoze, final IRemoteCallback started) {
        int i = 0;
        if (this.mWindowToken != null) {
            Slog.e(this.TAG, "attach() called when already attached with token=" + this.mWindowToken);
        } else if (this.mFinished || this.mWaking) {
            Slog.w(this.TAG, "attach() called after dream already finished");
            try {
                this.mSandman.finishSelf(windowToken, true);
            } catch (RemoteException e) {
            }
        } else {
            this.mWindowToken = windowToken;
            this.mCanDoze = canDoze;
            if (!this.mWindowless || this.mCanDoze) {
                if (!this.mWindowless) {
                    int i2;
                    this.mWindow = new PhoneWindow(this);
                    this.mWindow.setCallback(this);
                    this.mWindow.requestFeature(1);
                    this.mWindow.setBackgroundDrawable(new ColorDrawable(-16777216));
                    this.mWindow.setFormat(-1);
                    if (this.mDebug) {
                        Slog.v(this.TAG, String.format("Attaching window token: %s to window of type %s", new Object[]{windowToken, Integer.valueOf(2023)}));
                    }
                    LayoutParams lp = this.mWindow.getAttributes();
                    lp.type = 2023;
                    lp.token = windowToken;
                    lp.windowAnimations = 16974584;
                    int i3 = lp.flags;
                    if (this.mFullscreen) {
                        i2 = 1024;
                    } else {
                        i2 = 0;
                    }
                    int i4 = 4784385 | i2;
                    if (this.mScreenBright) {
                        i2 = 128;
                    } else {
                        i2 = 0;
                    }
                    lp.flags = (i2 | i4) | i3;
                    this.mWindow.setAttributes(lp);
                    this.mWindow.clearFlags(Integer.MIN_VALUE);
                    this.mWindow.setWindowManager(null, windowToken, "dream", true);
                    if (this.mLowProfile) {
                        i = 1;
                    }
                    applySystemUiVisibilityFlags(i, 1);
                    try {
                        getWindowManager().addView(this.mWindow.getDecorView(), this.mWindow.getAttributes());
                    } catch (BadTokenException e2) {
                        Slog.i(this.TAG, "attach() called after window token already removed, dream will finish soon");
                        this.mWindow = null;
                        return;
                    }
                }
                this.mHandler.post(new Runnable() {
                    public void run() {
                        if (DreamService.this.mWindow != null || DreamService.this.mWindowless) {
                            if (DreamService.this.mDebug) {
                                Slog.v(DreamService.this.TAG, "Calling onDreamingStarted()");
                            }
                            DreamService.this.mStarted = true;
                            try {
                                DreamService.this.onDreamingStarted();
                            } finally {
                                try {
                                    started.sendResult(null);
                                } catch (RemoteException e) {
                                    throw e.rethrowFromSystemServer();
                                }
                            }
                        }
                    }
                });
                return;
            }
            throw new IllegalStateException("Only doze dreams can be windowless");
        }
    }

    private boolean getWindowFlagValue(int flag, boolean defaultValue) {
        if (this.mWindow == null) {
            return defaultValue;
        }
        return (this.mWindow.getAttributes().flags & flag) != 0;
    }

    private void applyWindowFlags(int flags, int mask) {
        if (this.mWindow != null) {
            LayoutParams lp = this.mWindow.getAttributes();
            lp.flags = applyFlags(lp.flags, flags, mask);
            this.mWindow.setAttributes(lp);
            this.mWindow.getWindowManager().updateViewLayout(this.mWindow.getDecorView(), lp);
        }
    }

    private boolean getSystemUiVisibilityFlagValue(int flag, boolean defaultValue) {
        View v = null;
        if (this.mWindow != null) {
            v = this.mWindow.getDecorView();
        }
        if (v == null) {
            return defaultValue;
        }
        return (v.getSystemUiVisibility() & flag) != 0;
    }

    private void applySystemUiVisibilityFlags(int flags, int mask) {
        View v = null;
        if (this.mWindow != null) {
            v = this.mWindow.getDecorView();
        }
        if (v != null) {
            v.setSystemUiVisibility(applyFlags(v.getSystemUiVisibility(), flags, mask));
        }
    }

    private int applyFlags(int oldFlags, int flags, int mask) {
        return ((~mask) & oldFlags) | (flags & mask);
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
    protected void dump(java.io.FileDescriptor r7, java.io.PrintWriter r8, java.lang.String[] r9) {
        /*
        r6 = this;
        r0 = r6.mHandler;
        r1 = new android.service.dreams.DreamService$2;
        r1.<init>(r6, r7, r9);
        r3 = "";
        r4 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r2 = r8;
        com.android.internal.util.DumpUtils.dumpAsync(r0, r1, r2, r3, r4);
        return;
        */
        throw new UnsupportedOperationException("Method not decompiled: android.service.dreams.DreamService.dump(java.io.FileDescriptor, java.io.PrintWriter, java.lang.String[]):void");
    }

    protected void dumpOnHandler(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print(this.TAG + ": ");
        if (this.mWindowToken == null) {
            pw.println("stopped");
        } else {
            pw.println("running (token=" + this.mWindowToken + ")");
        }
        pw.println("  window: " + this.mWindow);
        pw.print("  flags:");
        if (isInteractive()) {
            pw.print(" interactive");
        }
        if (isLowProfile()) {
            pw.print(" lowprofile");
        }
        if (isFullscreen()) {
            pw.print(" fullscreen");
        }
        if (isScreenBright()) {
            pw.print(" bright");
        }
        if (isWindowless()) {
            pw.print(" windowless");
        }
        if (isDozing()) {
            pw.print(" dozing");
        } else if (canDoze()) {
            pw.print(" candoze");
        }
        pw.println();
        if (canDoze()) {
            pw.println("  doze screen state: " + Display.stateToString(this.mDozeScreenState));
            pw.println("  doze screen brightness: " + this.mDozeScreenBrightness);
        }
    }

    private static int clampAbsoluteBrightness(int value) {
        return MathUtils.constrain(value, 0, PowerManager.BRIGHTNESS_MULTIBITS_ON);
    }
}
