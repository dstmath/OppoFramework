package android.view.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import android.view.IWindow;
import android.view.accessibility.IAccessibilityManagerClient.Stub;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
public final class AccessibilityManager {
    public static final int AUTOCLICK_DELAY_DEFAULT = 600;
    public static final int DALTONIZER_CORRECT_DEUTERANOMALY = 12;
    public static final int DALTONIZER_DISABLED = -1;
    public static final int DALTONIZER_SIMULATE_MONOCHROMACY = 0;
    private static final boolean DEBUG = false;
    private static final String LOG_TAG = "AccessibilityManager";
    public static final int STATE_FLAG_ACCESSIBILITY_ENABLED = 1;
    public static final int STATE_FLAG_HIGH_TEXT_CONTRAST_ENABLED = 4;
    public static final int STATE_FLAG_TOUCH_EXPLORATION_ENABLED = 2;
    private static AccessibilityManager sInstance;
    static final Object sInstanceSync = null;
    private final CopyOnWriteArrayList<AccessibilityStateChangeListener> mAccessibilityStateChangeListeners;
    private final Stub mClient;
    final Handler mHandler;
    private final CopyOnWriteArrayList<HighTextContrastChangeListener> mHighTextContrastStateChangeListeners;
    boolean mIsEnabled;
    boolean mIsHighTextContrastEnabled;
    private boolean mIsSystemserver;
    boolean mIsTouchExplorationEnabled;
    private final Object mLock;
    private IAccessibilityManager mService;
    private final CopyOnWriteArrayList<TouchExplorationStateChangeListener> mTouchExplorationStateChangeListeners;
    final int mUserId;

    public interface AccessibilityStateChangeListener {
        void onAccessibilityStateChanged(boolean z);
    }

    public interface HighTextContrastChangeListener {
        void onHighTextContrastStateChanged(boolean z);
    }

    private final class MyHandler extends Handler {
        public static final int MSG_NOTIFY_ACCESSIBILITY_STATE_CHANGED = 1;
        public static final int MSG_NOTIFY_EXPLORATION_STATE_CHANGED = 2;
        public static final int MSG_NOTIFY_HIGH_TEXT_CONTRAST_STATE_CHANGED = 3;
        public static final int MSG_SET_STATE = 4;

        public MyHandler(Looper looper) {
            super(looper, null, false);
        }

        public void handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    AccessibilityManager.this.handleNotifyAccessibilityStateChanged();
                    return;
                case 2:
                    AccessibilityManager.this.handleNotifyTouchExplorationStateChanged();
                    return;
                case 3:
                    AccessibilityManager.this.handleNotifyHighTextContrastStateChanged();
                    return;
                case 4:
                    int state = message.arg1;
                    synchronized (AccessibilityManager.this.mLock) {
                        AccessibilityManager.this.setStateLocked(state);
                    }
                    return;
                default:
                    return;
            }
        }
    }

    public interface TouchExplorationStateChangeListener {
        void onTouchExplorationStateChanged(boolean z);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.view.accessibility.AccessibilityManager.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.view.accessibility.AccessibilityManager.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.accessibility.AccessibilityManager.<clinit>():void");
    }

    public static AccessibilityManager getInstance(Context context) {
        synchronized (sInstanceSync) {
            if (sInstance == null) {
                int userId;
                if (!(Binder.getCallingUid() == 1000 || context.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS") == 0)) {
                    if (context.checkCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS_FULL") != 0) {
                        userId = UserHandle.myUserId();
                        sInstance = new AccessibilityManager(context, null, userId);
                    }
                }
                userId = -2;
                sInstance = new AccessibilityManager(context, null, userId);
            }
        }
        return sInstance;
    }

    private boolean isSpecialProcess(String strProcName) {
        String strPidName = Process.getProcessNameByPid(Process.myPid());
        return strPidName != null ? strPidName.equals(strProcName) : false;
    }

    private boolean isSystemserverProcess() {
        return isSpecialProcess("system_server");
    }

    public AccessibilityManager(Context context, IAccessibilityManager service, int userId) {
        this.mLock = new Object();
        this.mAccessibilityStateChangeListeners = new CopyOnWriteArrayList();
        this.mTouchExplorationStateChangeListeners = new CopyOnWriteArrayList();
        this.mHighTextContrastStateChangeListeners = new CopyOnWriteArrayList();
        this.mClient = new Stub() {
            public void setState(int state) {
                AccessibilityManager.this.mHandler.obtainMessage(4, state, 0).sendToTarget();
            }
        };
        this.mHandler = new MyHandler(context.getMainLooper());
        this.mUserId = userId;
        this.mIsSystemserver = isSystemserverProcess();
        synchronized (this.mLock) {
            tryConnectToServiceLocked(service);
        }
    }

    public IAccessibilityManagerClient getClient() {
        return this.mClient;
    }

    public boolean isEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return false;
            }
            boolean z = this.mIsEnabled;
            return z;
        }
    }

    public boolean isTouchExplorationEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return false;
            }
            boolean z = this.mIsTouchExplorationEnabled;
            return z;
        }
    }

    public boolean isHighTextContrastEnabled() {
        synchronized (this.mLock) {
            if (getServiceLocked() == null) {
                return false;
            }
            boolean z = this.mIsHighTextContrastEnabled;
            return z;
        }
    }

    /* JADX WARNING: Missing block: B:30:0x0042, code:
            r0 = false;
     */
    /* JADX WARNING: Missing block: B:32:?, code:
            r11.setEventTime(android.os.SystemClock.uptimeMillis());
            r2 = android.os.Binder.clearCallingIdentity();
            r0 = r5.sendAccessibilityEvent(r11, r6);
            android.os.Binder.restoreCallingIdentity(r2);
     */
    /* JADX WARNING: Missing block: B:33:0x0055, code:
            if (r0 == false) goto L_0x005a;
     */
    /* JADX WARNING: Missing block: B:34:0x0057, code:
            r11.recycle();
     */
    /* JADX WARNING: Missing block: B:35:0x005a, code:
            return;
     */
    /* JADX WARNING: Missing block: B:36:0x005b, code:
            r4 = move-exception;
     */
    /* JADX WARNING: Missing block: B:38:?, code:
            android.util.Log.e(LOG_TAG, "Error during sending " + r11 + " ", r4);
     */
    /* JADX WARNING: Missing block: B:39:0x007d, code:
            if (r0 != false) goto L_0x007f;
     */
    /* JADX WARNING: Missing block: B:40:0x007f, code:
            r11.recycle();
     */
    /* JADX WARNING: Missing block: B:42:0x0084, code:
            if (r0 != false) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:43:0x0086, code:
            r11.recycle();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void sendAccessibilityEvent(AccessibilityEvent event) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
            } else if (this.mIsEnabled) {
                int userId = this.mUserId;
            } else if (Looper.myLooper() != Looper.getMainLooper()) {
                Log.e(LOG_TAG, "AccessibilityEvent sent with accessibility disabled");
            } else if (this.mIsSystemserver) {
                Log.w(LOG_TAG, "sendAccessibilityEvent, Accessibility off. Did you forget to check that? Becasue systemserver process, we skip throw exception");
            } else {
                throw new IllegalStateException("Accessibility off. Did you forget to check that?");
            }
        }
    }

    /* JADX WARNING: Missing block: B:25:?, code:
            r1.interrupt(r2);
     */
    /* JADX WARNING: Missing block: B:27:0x0031, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:28:0x0032, code:
            android.util.Log.e(LOG_TAG, "Error while requesting interrupt from all services. ", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interrupt() {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
            } else if (this.mIsEnabled) {
                int userId = this.mUserId;
            } else if (this.mIsSystemserver) {
                Log.w(LOG_TAG, "interrupt, Accessibility off. Did you forget to check that? Becasue systemserver process, we skip throw exception");
            } else {
                throw new IllegalStateException("Accessibility off. Did you forget to check that?");
            }
        }
    }

    @Deprecated
    public List<ServiceInfo> getAccessibilityServiceList() {
        List<AccessibilityServiceInfo> infos = getInstalledAccessibilityServiceList();
        List<ServiceInfo> services = new ArrayList();
        int infoCount = infos.size();
        for (int i = 0; i < infoCount; i++) {
            services.add(((AccessibilityServiceInfo) infos.get(i)).getResolveInfo().serviceInfo);
        }
        return Collections.unmodifiableList(services);
    }

    /* JADX WARNING: Missing block: B:11:0x0012, code:
            r2 = null;
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r2 = r1.getInstalledAccessibilityServiceList(r3);
     */
    /* JADX WARNING: Missing block: B:20:0x0021, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x0022, code:
            android.util.Log.e(LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList() {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                List<AccessibilityServiceInfo> emptyList = Collections.emptyList();
                return emptyList;
            }
            int userId = this.mUserId;
        }
        if (services != null) {
            return Collections.unmodifiableList(services);
        }
        return Collections.emptyList();
    }

    /* JADX WARNING: Missing block: B:11:0x0012, code:
            r2 = null;
     */
    /* JADX WARNING: Missing block: B:13:?, code:
            r2 = r1.getEnabledAccessibilityServiceList(r7, r3);
     */
    /* JADX WARNING: Missing block: B:20:0x0021, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:21:0x0022, code:
            android.util.Log.e(LOG_TAG, "Error while obtaining the installed AccessibilityServices. ", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackTypeFlags) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                List<AccessibilityServiceInfo> emptyList = Collections.emptyList();
                return emptyList;
            }
            int userId = this.mUserId;
        }
        if (services != null) {
            return Collections.unmodifiableList(services);
        }
        return Collections.emptyList();
    }

    public boolean addAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        return this.mAccessibilityStateChangeListeners.add(listener);
    }

    public boolean removeAccessibilityStateChangeListener(AccessibilityStateChangeListener listener) {
        return this.mAccessibilityStateChangeListeners.remove(listener);
    }

    public boolean addTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        return this.mTouchExplorationStateChangeListeners.add(listener);
    }

    public boolean removeTouchExplorationStateChangeListener(TouchExplorationStateChangeListener listener) {
        return this.mTouchExplorationStateChangeListeners.remove(listener);
    }

    public boolean addHighTextContrastStateChangeListener(HighTextContrastChangeListener listener) {
        return this.mHighTextContrastStateChangeListeners.add(listener);
    }

    public boolean removeHighTextContrastStateChangeListener(HighTextContrastChangeListener listener) {
        return this.mHighTextContrastStateChangeListeners.remove(listener);
    }

    private void setStateLocked(int stateFlags) {
        boolean enabled = (stateFlags & 1) != 0;
        boolean touchExplorationEnabled = (stateFlags & 2) != 0;
        boolean highTextContrastEnabled = (stateFlags & 4) != 0;
        boolean wasEnabled = this.mIsEnabled;
        boolean wasTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
        boolean wasHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
        this.mIsEnabled = enabled;
        this.mIsTouchExplorationEnabled = touchExplorationEnabled;
        this.mIsHighTextContrastEnabled = highTextContrastEnabled;
        if (wasEnabled != enabled) {
            this.mHandler.sendEmptyMessage(1);
        }
        if (wasTouchExplorationEnabled != touchExplorationEnabled) {
            this.mHandler.sendEmptyMessage(2);
        }
        if (wasHighTextContrastEnabled != highTextContrastEnabled) {
            this.mHandler.sendEmptyMessage(3);
        }
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, String packageName, IAccessibilityInteractionConnection connection) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
                return -1;
            }
            int userId = this.mUserId;
            try {
                return service.addAccessibilityInteractionConnection(windowToken, connection, packageName, userId);
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while adding an accessibility interaction connection. ", re);
                return -1;
            }
        }
    }

    /* JADX WARNING: Missing block: B:9:?, code:
            r1.removeAccessibilityInteractionConnection(r5);
     */
    /* JADX WARNING: Missing block: B:14:0x0013, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:15:0x0014, code:
            android.util.Log.e(LOG_TAG, "Error while removing an accessibility interaction connection. ", r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void removeAccessibilityInteractionConnection(IWindow windowToken) {
        synchronized (this.mLock) {
            IAccessibilityManager service = getServiceLocked();
            if (service == null) {
            }
        }
    }

    private IAccessibilityManager getServiceLocked() {
        if (this.mService == null) {
            tryConnectToServiceLocked(null);
        }
        return this.mService;
    }

    private void tryConnectToServiceLocked(IAccessibilityManager service) {
        if (service == null) {
            IBinder iBinder = ServiceManager.getService("accessibility");
            if (iBinder != null) {
                service = IAccessibilityManager.Stub.asInterface(iBinder);
            } else {
                return;
            }
        }
        try {
            setStateLocked(service.addClient(this.mClient, this.mUserId));
            this.mService = service;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "AccessibilityManagerService is dead", re);
        }
    }

    private void handleNotifyAccessibilityStateChanged() {
        boolean isEnabled;
        synchronized (this.mLock) {
            isEnabled = this.mIsEnabled;
        }
        for (AccessibilityStateChangeListener listener : this.mAccessibilityStateChangeListeners) {
            listener.onAccessibilityStateChanged(isEnabled);
        }
    }

    private void handleNotifyTouchExplorationStateChanged() {
        boolean isTouchExplorationEnabled;
        synchronized (this.mLock) {
            isTouchExplorationEnabled = this.mIsTouchExplorationEnabled;
        }
        for (TouchExplorationStateChangeListener listener : this.mTouchExplorationStateChangeListeners) {
            listener.onTouchExplorationStateChanged(isTouchExplorationEnabled);
        }
    }

    private void handleNotifyHighTextContrastStateChanged() {
        boolean isHighTextContrastEnabled;
        synchronized (this.mLock) {
            isHighTextContrastEnabled = this.mIsHighTextContrastEnabled;
        }
        for (HighTextContrastChangeListener listener : this.mHighTextContrastStateChangeListeners) {
            listener.onHighTextContrastStateChanged(isHighTextContrastEnabled);
        }
    }
}
