package com.android.server.display;

import android.app.AppOpsManager;
import android.common.OppoFeatureCache;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ParceledListSlice;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.ColorSpace;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.SensorManager;
import android.hardware.display.AmbientBrightnessDayStats;
import android.hardware.display.BrightnessChangeEvent;
import android.hardware.display.BrightnessConfiguration;
import android.hardware.display.Curve;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayViewport;
import android.hardware.display.DisplayedContentSample;
import android.hardware.display.DisplayedContentSamplingAttributes;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.IVirtualDisplayCallback;
import android.hardware.display.WifiDisplayStatus;
import android.hardware.input.InputManagerInternal;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ResultReceiver;
import android.os.ServiceManager;
import android.os.ShellCallback;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.OppoDisplayPerformanceHelper;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.IntArray;
import android.util.Pair;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Spline;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.SurfaceControl;
import android.widget.Toast;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.AnimationThread;
import com.android.server.DisplayThread;
import com.android.server.LocalServices;
import com.android.server.UiThread;
import com.android.server.am.IColorCommonListManager;
import com.android.server.am.IColorGameSpaceManager;
import com.android.server.display.DisplayAdapter;
import com.android.server.display.DisplayModeDirector;
import com.android.server.display.OppoBaseDisplayManagerService;
import com.android.server.util.ColorZoomWindowManagerHelper;
import com.android.server.wm.SurfaceAnimationThread;
import com.android.server.wm.WindowManagerInternal;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import oppo.util.OppoStatistics;

public class DisplayManagerService extends OppoBaseDisplayManagerService {
    private static final String DCS_LOG_EVENT_ID = "display_msg";
    private static final String DCS_LOG_TAG = "20120";
    /* access modifiers changed from: private */
    public static boolean DEBUG = SystemProperties.getBoolean("dbg.dms.dms", false);
    private static final String FORCE_60HZ_TOAST_COUNT = "force_60Hz_toast_count";
    private static final String FORCE_WIFI_DISPLAY_ENABLE = "persist.debug.wfd.enable";
    private static final boolean IS_SUPPORT_DCS = false;
    private static final String KEY_DISPLAY_EVENT_REGISTER_CALLBACK = "register_callback";
    private static final String LOCAL_PREFIX = "local:";
    private static final int MSG_DELIVER_DISPLAY_EVENT = 3;
    private static final int MSG_LOAD_BRIGHTNESS_CONFIGURATION = 6;
    private static final int MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS = 2;
    private static final int MSG_REGISTER_DEFAULT_DISPLAY_ADAPTERS = 1;
    private static final int MSG_REQUEST_TRAVERSAL = 4;
    private static final int MSG_SHOW_TOAST = 7;
    private static final int MSG_UPDATE_VIEWPORT = 5;
    private static final String OVERLAY_PREFIX = "overlay:";
    private static final String PROP_DEFAULT_DISPLAY_TOP_INSET = "persist.sys.displayinset.top";
    private static final int REFRESH_RATE_120HZ = 3;
    private static final int REFRESH_RATE_60HZ = 2;
    private static final int REFRESH_RATE_90HZ = 1;
    private static final int REFRESH_RATE_AUTO = 0;
    private static final String SYS_FORCE_60HZ = "sys_force_60Hz";
    private static final String TAG = "DisplayManagerService";
    private static final String VIRTUAL_PREFIX = "virtual:";
    private static final long WAIT_FOR_DEFAULT_DISPLAY_TIMEOUT = 10000;
    private static final String WIFI_PREFIX = "wifi:";
    private static int mStateCached = -1;
    public final SparseArray<CallbackRecord> mCallbacks;
    /* access modifiers changed from: private */
    public final Context mContext;
    private int mCurrentUserId;
    private final int mDefaultDisplayDefaultColorMode;
    private int mDefaultDisplayTopInset;
    private final SparseArray<IntArray> mDisplayAccessUIDs;
    private final DisplayAdapterListener mDisplayAdapterListener;
    private final ArrayList<DisplayAdapter> mDisplayAdapters;
    /* access modifiers changed from: private */
    public final ArrayList<DisplayDevice> mDisplayDevices;
    private final DisplayModeDirector mDisplayModeDirector;
    private OppoDisplayPerformanceHelper mDisplayPerformanceHelper;
    /* access modifiers changed from: private */
    public DisplayPowerController mDisplayPowerController;
    private final CopyOnWriteArrayList<DisplayManagerInternal.DisplayTransactionListener> mDisplayTransactionListeners;
    private int mGlobalDisplayBrightness;
    private int mGlobalDisplayState;
    /* access modifiers changed from: private */
    public final DisplayManagerHandler mHandler;
    private final Injector mInjector;
    /* access modifiers changed from: private */
    public InputManagerInternal mInputManagerInternal;
    private final SparseArray<LogicalDisplay> mLogicalDisplays;
    private final Curve mMinimumBrightnessCurve;
    private final Spline mMinimumBrightnessSpline;
    private int mNextNonDefaultDisplayId;
    public boolean mOnlyCore;
    private int mOomDevices;
    private boolean mPendingTraversal;
    /* access modifiers changed from: private */
    public final PersistentDataStore mPersistentDataStore;
    private IMediaProjectionManager mProjectionService;
    public boolean mSafeMode;
    private final boolean mSingleDisplayDemoMode;
    private Point mStableDisplaySize;
    /* access modifiers changed from: private */
    public final SyncRoot mSyncRoot;
    private boolean mSystemReady;
    private final ArrayList<CallbackRecord> mTempCallbacks;
    private final DisplayInfo mTempDisplayInfo;
    private final ArrayList<Runnable> mTempDisplayStateWorkQueue;
    /* access modifiers changed from: private */
    public final ArrayList<DisplayViewport> mTempViewports;
    private int mToastCount;
    private final Handler mUiHandler;
    /* access modifiers changed from: private */
    @GuardedBy({"mSyncRoot"})
    public final ArrayList<DisplayViewport> mViewports;
    private VirtualDisplayAdapter mVirtualDisplayAdapter;
    private final ColorSpace mWideColorSpace;
    private WifiDisplayAdapter mWifiDisplayAdapter;
    private int mWifiDisplayScanRequestCount;
    /* access modifiers changed from: private */
    public WindowManagerInternal mWindowManagerInternal;

    public static final class SyncRoot {
    }

    public DisplayManagerService(Context context) {
        this(context, new Injector());
    }

    @VisibleForTesting
    DisplayManagerService(Context context, Injector injector) {
        super(context);
        this.mSyncRoot = new SyncRoot();
        this.mCallbacks = new SparseArray<>();
        this.mDisplayAdapters = new ArrayList<>();
        this.mDisplayDevices = new ArrayList<>();
        this.mLogicalDisplays = new SparseArray<>();
        this.mNextNonDefaultDisplayId = 1;
        this.mDisplayTransactionListeners = new CopyOnWriteArrayList<>();
        this.mGlobalDisplayState = 2;
        this.mGlobalDisplayBrightness = -1;
        this.mStableDisplaySize = new Point();
        this.mViewports = new ArrayList<>();
        this.mPersistentDataStore = new PersistentDataStore();
        this.mTempCallbacks = new ArrayList<>();
        this.mTempDisplayInfo = new DisplayInfo();
        this.mTempViewports = new ArrayList<>();
        this.mTempDisplayStateWorkQueue = new ArrayList<>();
        this.mDisplayAccessUIDs = new SparseArray<>();
        this.mDisplayPerformanceHelper = null;
        this.mInjector = injector;
        this.mContext = context;
        this.mHandler = new DisplayManagerHandler(DisplayThread.get().getLooper());
        this.mUiHandler = UiThread.getHandler();
        this.mDisplayAdapterListener = new DisplayAdapterListener();
        this.mDisplayModeDirector = new DisplayModeDirector(context, this.mHandler);
        this.mSingleDisplayDemoMode = SystemProperties.getBoolean("persist.demo.singledisplay", false);
        Resources resources = this.mContext.getResources();
        this.mDefaultDisplayDefaultColorMode = this.mContext.getResources().getInteger(17694770);
        this.mDefaultDisplayTopInset = SystemProperties.getInt(PROP_DEFAULT_DISPLAY_TOP_INSET, -1);
        float[] lux = getFloatArray(resources.obtainTypedArray(17236044));
        float[] nits = getFloatArray(resources.obtainTypedArray(17236045));
        this.mMinimumBrightnessCurve = new Curve(lux, nits);
        this.mMinimumBrightnessSpline = Spline.createSpline(lux, nits);
        PowerManager powerManager = (PowerManager) this.mContext.getSystemService(PowerManager.class);
        this.mGlobalDisplayBrightness = OppoBrightUtils.getInstance().getBootupBrightness();
        this.mCurrentUserId = 0;
        this.mWideColorSpace = SurfaceControl.getCompositionColorSpaces()[1];
        this.mSystemReady = false;
    }

    public void setupSchedulerPolicies() {
        Process.setThreadGroupAndCpuset(DisplayThread.get().getThreadId(), 5);
        Process.setThreadGroupAndCpuset(AnimationThread.get().getThreadId(), 5);
        Process.setThreadGroupAndCpuset(SurfaceAnimationThread.get().getThreadId(), 5);
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.display.DisplayManagerService$BinderService, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        synchronized (this.mSyncRoot) {
            this.mPersistentDataStore.loadIfNeeded();
            loadStableDisplayValuesLocked();
        }
        this.mHandler.sendEmptyMessage(1);
        publishBinderService("display", new BinderService(), true);
        publishLocalService(DisplayManagerInternal.class, new LocalService());
        onOppoStart();
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 100) {
            synchronized (this.mSyncRoot) {
                long timeout = SystemClock.uptimeMillis() + this.mInjector.getDefaultDisplayDelayTimeout();
                while (true) {
                    if (this.mLogicalDisplays.get(0) != null) {
                        if (this.mVirtualDisplayAdapter == null) {
                        }
                    }
                    long delay = timeout - SystemClock.uptimeMillis();
                    if (delay > 0) {
                        if (DEBUG) {
                            Slog.d(TAG, "waitForDefaultDisplay: waiting, timeout=" + delay);
                        }
                        try {
                            this.mSyncRoot.wait(delay);
                        } catch (InterruptedException e) {
                        }
                    } else {
                        throw new RuntimeException("Timeout waiting for default display to be initialized. DefaultDisplay=" + this.mLogicalDisplays.get(0) + ", mVirtualDisplayAdapter=" + this.mVirtualDisplayAdapter);
                    }
                }
            }
        }
    }

    @Override // com.android.server.SystemService
    public void onSwitchUser(int newUserId) {
        int userSerial = getUserManager().getUserSerialNumber(newUserId);
        synchronized (this.mSyncRoot) {
            if (this.mCurrentUserId != newUserId) {
                this.mCurrentUserId = newUserId;
                this.mPersistentDataStore.getBrightnessConfiguration(userSerial);
            }
            this.mDisplayPowerController.onSwitchUser(newUserId);
        }
    }

    public void windowManagerAndInputReady() {
        synchronized (this.mSyncRoot) {
            this.mWindowManagerInternal = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
            this.mInputManagerInternal = (InputManagerInternal) LocalServices.getService(InputManagerInternal.class);
            scheduleTraversalLocked(false);
        }
    }

    public void systemReady(boolean safeMode, boolean onlyCore) {
        synchronized (this.mSyncRoot) {
            this.mSafeMode = safeMode;
            this.mOnlyCore = onlyCore;
            this.mSystemReady = true;
            recordTopInsetLocked(this.mLogicalDisplays.get(0));
        }
        this.mDisplayModeDirector.setListener(new AllowedDisplayModeObserver());
        this.mDisplayModeDirector.start();
        this.mHandler.sendEmptyMessage(2);
        this.mDisplayPerformanceHelper = new OppoDisplayPerformanceHelper(this.mContext);
        if (hasSystemFeature("oppo.multi.device.decrease.refresh.rate")) {
            Settings.Secure.putInt(this.mContext.getContentResolver(), SYS_FORCE_60HZ, 0);
        }
        this.mOomDevices = 0;
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Handler getDisplayHandler() {
        return this.mHandler;
    }

    private void loadStableDisplayValuesLocked() {
        Point size = this.mPersistentDataStore.getStableDisplaySize();
        if (size.x <= 0 || size.y <= 0) {
            Resources res = this.mContext.getResources();
            int width = res.getInteger(17694897);
            int height = res.getInteger(17694896);
            if (width > 0 && height > 0) {
                setStableDisplaySizeLocked(width, height);
                return;
            }
            return;
        }
        this.mStableDisplaySize.set(size.x, size.y);
    }

    /* access modifiers changed from: private */
    public Point getStableDisplaySizeInternal() {
        Point r = new Point();
        synchronized (this.mSyncRoot) {
            if (this.mStableDisplaySize.x > 0 && this.mStableDisplaySize.y > 0) {
                r.set(this.mStableDisplaySize.x, this.mStableDisplaySize.y);
            }
        }
        return r;
    }

    /* access modifiers changed from: private */
    public void registerDisplayTransactionListenerInternal(DisplayManagerInternal.DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.add(listener);
    }

    /* access modifiers changed from: private */
    public void unregisterDisplayTransactionListenerInternal(DisplayManagerInternal.DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.remove(listener);
    }

    /* access modifiers changed from: private */
    public void setDisplayInfoOverrideFromWindowManagerInternal(int displayId, DisplayInfo info) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null && display.setDisplayInfoOverrideFromWindowManagerLocked(info)) {
                handleLogicalDisplayChanged(displayId, display);
                scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public void getNonOverrideDisplayInfoInternal(int displayId, DisplayInfo outInfo) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                display.getNonOverrideDisplayInfoLocked(outInfo);
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        if (r0.hasNext() == false) goto L_0x0026;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x001c, code lost:
        r0.next().onDisplayTransaction(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0026, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0010, code lost:
        r0 = r2.mDisplayTransactionListeners.iterator();
     */
    @VisibleForTesting
    public void performTraversalInternal(SurfaceControl.Transaction t) {
        synchronized (this.mSyncRoot) {
            if (this.mPendingTraversal) {
                this.mPendingTraversal = false;
                performTraversalLocked(t);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x006a, code lost:
        r1 = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0071, code lost:
        if (r1 >= r5.mTempDisplayStateWorkQueue.size()) goto L_0x0081;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0073, code lost:
        r5.mTempDisplayStateWorkQueue.get(r1).run();
        r1 = r1 + 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0081, code lost:
        android.os.Trace.traceEnd(131072);
        r5.mTempDisplayStateWorkQueue.clear();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008b, code lost:
        return;
     */
    public void requestGlobalDisplayStateInternal(int state, int brightness) {
        if (state == 0) {
            state = 2;
        }
        if (state == 1) {
            brightness = 0;
        } else if (brightness < 0) {
            brightness = -1;
        } else if (OppoBrightUtils.mScreenGlobalHBMSupport && brightness > OppoBrightUtils.HBM_EXTEND_MAXBRIGHTNESS) {
            brightness = OppoBrightUtils.HBM_EXTEND_MAXBRIGHTNESS;
        } else if (!OppoBrightUtils.mScreenGlobalHBMSupport && brightness > PowerManager.BRIGHTNESS_MULTIBITS_ON) {
            brightness = PowerManager.BRIGHTNESS_MULTIBITS_ON;
        }
        synchronized (this.mTempDisplayStateWorkQueue) {
            try {
                synchronized (this.mSyncRoot) {
                    if (this.mGlobalDisplayState != state || this.mGlobalDisplayBrightness != brightness) {
                        Trace.traceBegin(131072, "requestGlobalDisplayState(" + Display.stateToString(state) + ", brightness=" + brightness + ")");
                        this.mGlobalDisplayState = state;
                        this.mGlobalDisplayBrightness = brightness;
                        applyGlobalDisplayStateLocked(this.mTempDisplayStateWorkQueue);
                    }
                }
            } finally {
                this.mTempDisplayStateWorkQueue.clear();
            }
        }
    }

    /* access modifiers changed from: private */
    public DisplayInfo getDisplayInfoInternal(int displayId, int callingUid) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                DisplayInfo info = display.getDisplayInfoLocked();
                if (info.hasAccess(callingUid) || isUidPresentOnDisplayInternal(callingUid, displayId)) {
                    ColorZoomWindowManagerHelper.getInstance();
                    DisplayInfo info2 = ColorZoomWindowManagerHelper.getZoomWindowManager().getZoomModeDisplayInfo(info, displayId, callingUid);
                    return info2;
                }
            }
            return null;
        }
    }

    /* access modifiers changed from: private */
    public int[] getDisplayIdsInternal(int callingUid) {
        int[] displayIds;
        synchronized (this.mSyncRoot) {
            int count = this.mLogicalDisplays.size();
            displayIds = new int[count];
            int n = 0;
            for (int i = 0; i < count; i++) {
                if (this.mLogicalDisplays.valueAt(i).getDisplayInfoLocked().hasAccess(callingUid)) {
                    displayIds[n] = this.mLogicalDisplays.keyAt(i);
                    n++;
                }
            }
            if (n != count) {
                displayIds = Arrays.copyOfRange(displayIds, 0, n);
            }
        }
        return displayIds;
    }

    /* access modifiers changed from: private */
    public void registerCallbackInternal(IDisplayManagerCallback callback, int callingPid, int callingUid) {
        synchronized (this.mSyncRoot) {
            if (this.mCallbacks.get(callingPid) != null) {
                CallbackRecord existCallBack = this.mCallbacks.get(callingPid);
                boolean isCallbackAlive = existCallBack.isCallbackAlive();
                boolean isCallbackFromDifferentUid = callingUid != existCallBack.mUid;
                if (isCallbackAlive) {
                    if (!isCallbackFromDifferentUid) {
                        throw new SecurityException("The calling process has already registered an IDisplayManagerCallback.");
                    }
                }
                onCallbackDied(existCallBack);
                Slog.w(TAG, "register error detect:" + ("regFail:new-" + callingUid + ", " + "old-" + existCallBack.mUid + ", " + "ali-" + isCallbackAlive));
            }
            CallbackRecord record = new CallbackRecord(callingPid, callback, callingUid);
            try {
                callback.asBinder().linkToDeath(record, 0);
                this.mCallbacks.put(callingPid, record);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /* access modifiers changed from: private */
    public void onCallbackDied(CallbackRecord record) {
        synchronized (this.mSyncRoot) {
            this.mCallbacks.remove(record.mPid);
            stopWifiDisplayScanLocked(record);
        }
    }

    /* access modifiers changed from: private */
    public void startWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = this.mCallbacks.get(callingPid);
            if (record != null) {
                startWifiDisplayScanLocked(record);
            } else {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
        }
    }

    private void startWifiDisplayScanLocked(CallbackRecord record) {
        WifiDisplayAdapter wifiDisplayAdapter;
        if (!record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = true;
            int i = this.mWifiDisplayScanRequestCount;
            this.mWifiDisplayScanRequestCount = i + 1;
            if (i == 0 && (wifiDisplayAdapter = this.mWifiDisplayAdapter) != null) {
                wifiDisplayAdapter.requestStartScanLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void stopWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = this.mCallbacks.get(callingPid);
            if (record != null) {
                stopWifiDisplayScanLocked(record);
            } else {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
        }
    }

    private void stopWifiDisplayScanLocked(CallbackRecord record) {
        if (record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = false;
            int i = this.mWifiDisplayScanRequestCount - 1;
            this.mWifiDisplayScanRequestCount = i;
            if (i == 0) {
                WifiDisplayAdapter wifiDisplayAdapter = this.mWifiDisplayAdapter;
                if (wifiDisplayAdapter != null) {
                    wifiDisplayAdapter.requestStopScanLocked();
                }
            } else if (this.mWifiDisplayScanRequestCount < 0) {
                Slog.wtf(TAG, "mWifiDisplayScanRequestCount became negative: " + this.mWifiDisplayScanRequestCount);
                this.mWifiDisplayScanRequestCount = 0;
            }
        }
    }

    /* access modifiers changed from: private */
    public void connectWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestConnectLocked(address);
            }
        }
    }

    /* access modifiers changed from: private */
    public void pauseWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestPauseLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void resumeWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestResumeLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void disconnectWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestDisconnectLocked();
            }
        }
    }

    /* access modifiers changed from: private */
    public void renameWifiDisplayInternal(String address, String alias) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestRenameLocked(address, alias);
            }
        }
    }

    /* access modifiers changed from: private */
    public void forgetWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestForgetLocked(address);
            }
        }
    }

    /* access modifiers changed from: private */
    public WifiDisplayStatus getWifiDisplayStatusInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                WifiDisplayStatus wifiDisplayStatusLocked = this.mWifiDisplayAdapter.getWifiDisplayStatusLocked();
                return wifiDisplayStatusLocked;
            }
            WifiDisplayStatus wifiDisplayStatus = new WifiDisplayStatus();
            return wifiDisplayStatus;
        }
    }

    /* access modifiers changed from: private */
    public void requestColorModeInternal(int displayId, int colorMode) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (!(display == null || display.getRequestedColorModeLocked() == colorMode)) {
                display.setRequestedColorModeLocked(colorMode);
                scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public int createVirtualDisplayInternal(IVirtualDisplayCallback callback, IMediaProjection projection, int callingUid, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                Slog.w(TAG, "Rejecting request to create private virtual display because the virtual display adapter is not available.");
                return -1;
            }
            DisplayDevice device = this.mVirtualDisplayAdapter.createVirtualDisplayLocked(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags, uniqueId);
            if (device == null) {
                return -1;
            }
            handleDisplayDeviceAddedLocked(device);
            LogicalDisplay display = findLogicalDisplayForDeviceLocked(device);
            if (display != null) {
                int displayIdLocked = display.getDisplayIdLocked();
                return displayIdLocked;
            }
            Slog.w(TAG, "Rejecting request to create virtual display because the logical display was not created.");
            this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(callback.asBinder());
            handleDisplayDeviceRemovedLocked(device);
            return -1;
        }
    }

    /* access modifiers changed from: private */
    public void resizeVirtualDisplayInternal(IBinder appToken, int width, int height, int densityDpi) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.resizeVirtualDisplayLocked(appToken, width, height, densityDpi);
            }
        }
    }

    /* access modifiers changed from: private */
    public void setVirtualDisplaySurfaceInternal(IBinder appToken, Surface surface) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.setVirtualDisplaySurfaceLocked(appToken, surface);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x00ba, code lost:
        return;
     */
    public void releaseVirtualDisplayInternal(IBinder appToken) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                DisplayDevice device = this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(appToken);
                if (device != null) {
                    handleDisplayDeviceRemovedLocked(device);
                } else {
                    Slog.e(TAG, "device is null, mOomDevices = " + this.mOomDevices);
                    int count = this.mDisplayDevices.size();
                    boolean hasLocalDisplay = false;
                    boolean hasVirtualDisplay = false;
                    if (count == this.mOomDevices + 2) {
                        for (int i = 0; i < count; i++) {
                            DisplayDevice exist = this.mDisplayDevices.get(i);
                            if (exist.getUniqueId().contains(LOCAL_PREFIX)) {
                                hasLocalDisplay = true;
                            } else if (exist.getUniqueId().contains(VIRTUAL_PREFIX)) {
                                hasVirtualDisplay = true;
                            }
                        }
                        if (hasLocalDisplay && hasVirtualDisplay && hasSystemFeature("oppo.multi.device.decrease.refresh.rate")) {
                            Slog.i(TAG, "releaseVirtualDisplayInternal:enable display refresh rate switch");
                            Settings.Secure.putInt(this.mContext.getContentResolver(), SYS_FORCE_60HZ, 0);
                            if (mStateCached != 2) {
                                Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", mStateCached);
                                Slog.i(TAG, "releaseVirtualDisplayInternal:recover display refresh state to " + mStateCached);
                            }
                        }
                    }
                    this.mOomDevices++;
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setVirtualDisplayStateInternal(IBinder appToken, boolean isOn) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter != null) {
                this.mVirtualDisplayAdapter.setVirtualDisplayStateLocked(appToken, isOn);
            }
        }
    }

    /* access modifiers changed from: private */
    public void registerDefaultDisplayAdapters() {
        synchronized (this.mSyncRoot) {
            registerDisplayAdapterLocked(new LocalDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener));
            this.mVirtualDisplayAdapter = this.mInjector.getVirtualDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener);
            if (this.mVirtualDisplayAdapter != null) {
                registerDisplayAdapterLocked(this.mVirtualDisplayAdapter);
            }
        }
    }

    /* access modifiers changed from: private */
    public void registerAdditionalDisplayAdapters() {
        synchronized (this.mSyncRoot) {
            if (shouldRegisterNonEssentialDisplayAdaptersLocked()) {
                registerOverlayDisplayAdapterLocked();
                registerWifiDisplayAdapterLocked();
            }
        }
    }

    private void registerOverlayDisplayAdapterLocked() {
        registerDisplayAdapterLocked(new OverlayDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mUiHandler));
    }

    private void registerWifiDisplayAdapterLocked() {
        if (this.mContext.getResources().getBoolean(17891452) || SystemProperties.getInt(FORCE_WIFI_DISPLAY_ENABLE, -1) == 1 || SystemProperties.get("ro.vendor.mtk_wfd_support").equals("1")) {
            this.mWifiDisplayAdapter = new WifiDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mPersistentDataStore);
            registerDisplayAdapterLocked(this.mWifiDisplayAdapter);
        }
    }

    private boolean shouldRegisterNonEssentialDisplayAdaptersLocked() {
        return !this.mSafeMode && !this.mOnlyCore;
    }

    private void registerDisplayAdapterLocked(DisplayAdapter adapter) {
        this.mDisplayAdapters.add(adapter);
        adapter.registerLocked();
    }

    /* access modifiers changed from: private */
    public void handleDisplayDeviceAdded(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            handleDisplayDeviceAddedLocked(device);
        }
    }

    private boolean hasSystemFeature(String sysFeatureName) {
        if (TextUtils.isEmpty(sysFeatureName)) {
            return false;
        }
        return this.mContext.getPackageManager().hasSystemFeature(sysFeatureName);
    }

    private int getRefreshRateMode() {
        int defaultMode = 1;
        if (hasSystemFeature("oppo.display.screen.defaultsmartmode")) {
            defaultMode = 0;
        } else if (hasSystemFeature("oppo.display.screen.120hz.support")) {
            defaultMode = 3;
        }
        Slog.i(TAG, "getRefreshRateMode defaultMode = " + defaultMode);
        return Settings.Secure.getInt(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", defaultMode);
    }

    private void handleDisplayDeviceAddedLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (this.mDisplayDevices.contains(device)) {
            Slog.w(TAG, "Attempted to add already added display device: " + info);
            return;
        }
        Slog.i(TAG, "Display device added: " + info);
        int count = this.mDisplayDevices.size();
        boolean hasLocalDisplay = false;
        boolean hasVirtualDisplay = false;
        for (int i = 0; i < count; i++) {
            DisplayDevice exist = this.mDisplayDevices.get(i);
            if (exist.getUniqueId().contains(LOCAL_PREFIX)) {
                hasLocalDisplay = true;
            } else if (exist.getUniqueId().contains(VIRTUAL_PREFIX)) {
                hasVirtualDisplay = true;
            }
            if (count == 1 && hasLocalDisplay && !device.getUniqueId().contains(LOCAL_PREFIX) && hasSystemFeature("oppo.multi.device.decrease.refresh.rate")) {
                mStateCached = getRefreshRateMode();
                Slog.i(TAG, "primary display refresh rate is " + mStateCached);
                Slog.i(TAG, "disable display refresh rate switch");
                Settings.Secure.putInt(this.mContext.getContentResolver(), SYS_FORCE_60HZ, 1);
                if (mStateCached != 2) {
                    Slog.i(TAG, "force 60Hz");
                    this.mToastCount = Settings.Secure.getInt(this.mContext.getContentResolver(), FORCE_60HZ_TOAST_COUNT, 0);
                    Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", 2);
                    if (!hasVirtualDisplay && device.getUniqueId().contains(VIRTUAL_PREFIX) && this.mToastCount < 3) {
                        this.mHandler.sendEmptyMessage(7);
                        ContentResolver contentResolver = this.mContext.getContentResolver();
                        int i2 = this.mToastCount + 1;
                        this.mToastCount = i2;
                        Settings.Secure.putInt(contentResolver, FORCE_60HZ_TOAST_COUNT, i2);
                    }
                }
            }
        }
        if (info != null) {
            try {
                OppoFeatureCache.get(IColorCommonListManager.DEFAULT).putAppInfo(info.ownerPackageName, info.ownerUid, "recorder");
                OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).addPkgToDisplayDeviceList(info.ownerPackageName);
            } catch (Exception e) {
            }
        }
        device.mDebugLastLoggedDeviceInfo = info;
        this.mDisplayDevices.add(device);
        addLogicalDisplayLocked(device);
        Runnable work = updateDisplayStateLocked(device);
        if (work != null) {
            work.run();
        }
        scheduleTraversalLocked(false);
        if (info != null) {
            try {
                OppoFeatureCache.get(IColorCommonListManager.DEFAULT).putAppInfo(info.ownerPackageName, info.ownerUid, "recorder");
                OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).addPkgToDisplayDeviceList(info.ownerPackageName);
            } catch (Exception e2) {
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDisplayDeviceChanged(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
            if (!this.mDisplayDevices.contains(device)) {
                Slog.w(TAG, "Attempted to change non-existent display device: " + info);
                return;
            }
            int diff = device.mDebugLastLoggedDeviceInfo.diff(info);
            if (diff == 1) {
                Slog.i(TAG, "Display device changed state: \"" + info.name + "\", " + Display.stateToString(info.state));
            } else if (diff != 0) {
                Slog.i(TAG, "Display device changed: " + info);
            }
            if ((diff & 4) != 0) {
                try {
                    this.mPersistentDataStore.setColorMode(device, info.colorMode);
                } finally {
                    this.mPersistentDataStore.saveIfNeeded();
                }
            }
            device.mDebugLastLoggedDeviceInfo = info;
            device.applyPendingDisplayDeviceInfoChangesLocked();
            if (updateLogicalDisplaysLocked()) {
                scheduleTraversalLocked(false);
            }
            if (info != null && info.state == 1) {
                try {
                    OppoFeatureCache.get(IColorCommonListManager.DEFAULT).removeAppInfo(info.ownerPackageName, info.ownerUid, "recorder");
                } catch (Exception e) {
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void handleDisplayDeviceRemoved(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            handleDisplayDeviceRemovedLocked(device);
        }
    }

    private void handleDisplayDeviceRemovedLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (!this.mDisplayDevices.remove(device)) {
            Slog.w(TAG, "Attempted to remove non-existent display device: " + info);
            return;
        }
        Slog.i(TAG, "Display device removed: " + info);
        boolean onlyLocalDisplay = false;
        if (this.mDisplayDevices.size() == 1) {
            if (this.mDisplayDevices.get(0).getUniqueId().contains(LOCAL_PREFIX)) {
                onlyLocalDisplay = true;
            }
            if (onlyLocalDisplay && hasSystemFeature("oppo.multi.device.decrease.refresh.rate")) {
                Slog.i(TAG, "enable display refresh rate switch");
                Settings.Secure.putInt(this.mContext.getContentResolver(), SYS_FORCE_60HZ, 0);
                if (mStateCached != 2) {
                    Settings.Secure.putInt(this.mContext.getContentResolver(), "coloros_screen_refresh_rate", mStateCached);
                    Slog.i(TAG, "recover display refresh state to " + mStateCached);
                }
            }
        }
        if (info != null) {
            try {
                OppoFeatureCache.get(IColorCommonListManager.DEFAULT).removeAppInfo(info.ownerPackageName, info.ownerUid, "recorder");
                OppoFeatureCache.get(IColorGameSpaceManager.DEFAULT).removePkgFromDisplayDeviceList(info.ownerPackageName);
            } catch (Exception e) {
            }
        }
        device.mDebugLastLoggedDeviceInfo = info;
        updateLogicalDisplaysLocked();
        scheduleTraversalLocked(false);
    }

    private void handleLogicalDisplayChanged(int displayId, LogicalDisplay display) {
        if (displayId == 0) {
            recordTopInsetLocked(display);
        }
        sendDisplayEventLocked(displayId, 2);
    }

    private void applyGlobalDisplayStateLocked(List<Runnable> workQueue) {
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i++) {
            Runnable runnable = updateDisplayStateLocked(this.mDisplayDevices.get(i));
            if (runnable != null) {
                workQueue.add(runnable);
            }
        }
    }

    private Runnable updateDisplayStateLocked(DisplayDevice device) {
        if ((device.getDisplayDeviceInfoLocked().flags & 32) == 0) {
            return device.requestDisplayStateLocked(this.mGlobalDisplayState, this.mGlobalDisplayBrightness);
        }
        return null;
    }

    private LogicalDisplay addLogicalDisplayLocked(DisplayDevice device) {
        DisplayDeviceInfo deviceInfo = device.getDisplayDeviceInfoLocked();
        boolean isDefault = (deviceInfo.flags & 1) != 0;
        if (isDefault && this.mLogicalDisplays.get(0) != null) {
            Slog.w(TAG, "Ignoring attempt to add a second default display: " + deviceInfo);
            isDefault = false;
        }
        if (isDefault || !this.mSingleDisplayDemoMode) {
            int displayId = assignDisplayIdLocked(isDefault);
            LogicalDisplay display = new LogicalDisplay(displayId, assignLayerStackLocked(displayId), device);
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                Slog.w(TAG, "Ignoring display device because the logical display created from it was not considered valid: " + deviceInfo);
                return null;
            }
            configureColorModeLocked(display, device);
            if (isDefault) {
                recordStableDisplayStatsIfNeededLocked(display);
                recordTopInsetLocked(display);
            }
            this.mLogicalDisplays.put(displayId, display);
            if (isDefault) {
                this.mSyncRoot.notifyAll();
            }
            sendDisplayEventLocked(displayId, 1);
            return display;
        }
        Slog.i(TAG, "Not creating a logical display for a secondary display  because single display demo mode is enabled: " + deviceInfo);
        return null;
    }

    private int assignDisplayIdLocked(boolean isDefault) {
        if (isDefault) {
            return 0;
        }
        int i = this.mNextNonDefaultDisplayId;
        this.mNextNonDefaultDisplayId = i + 1;
        return i;
    }

    private int assignLayerStackLocked(int displayId) {
        return displayId;
    }

    private void configureColorModeLocked(LogicalDisplay display, DisplayDevice device) {
        if (display.getPrimaryDisplayDeviceLocked() == device) {
            int colorMode = this.mPersistentDataStore.getColorMode(device);
            if (colorMode == -1) {
                if ((device.getDisplayDeviceInfoLocked().flags & 1) != 0) {
                    colorMode = this.mDefaultDisplayDefaultColorMode;
                } else {
                    colorMode = 0;
                }
            }
            display.setRequestedColorModeLocked(colorMode);
        }
    }

    private void recordStableDisplayStatsIfNeededLocked(LogicalDisplay d) {
        if (this.mStableDisplaySize.x <= 0 && this.mStableDisplaySize.y <= 0) {
            DisplayInfo info = d.getDisplayInfoLocked();
            setStableDisplaySizeLocked(info.getNaturalWidth(), info.getNaturalHeight());
        }
    }

    private void recordTopInsetLocked(LogicalDisplay d) {
        int topInset;
        if (this.mSystemReady && d != null && (topInset = d.getInsets().top) != this.mDefaultDisplayTopInset) {
            this.mDefaultDisplayTopInset = topInset;
            SystemProperties.set(PROP_DEFAULT_DISPLAY_TOP_INSET, Integer.toString(topInset));
        }
    }

    private void setStableDisplaySizeLocked(int width, int height) {
        this.mStableDisplaySize = new Point(width, height);
        try {
            this.mPersistentDataStore.setStableDisplaySize(this.mStableDisplaySize);
        } finally {
            this.mPersistentDataStore.saveIfNeeded();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public Curve getMinimumBrightnessCurveInternal() {
        return this.mMinimumBrightnessCurve;
    }

    /* access modifiers changed from: package-private */
    public int getPreferredWideGamutColorSpaceIdInternal() {
        return this.mWideColorSpace.getId();
    }

    /* access modifiers changed from: private */
    public void setBrightnessConfigurationForUserInternal(BrightnessConfiguration c, int userId, String packageName) {
        validateBrightnessConfiguration(c);
        int userSerial = getUserManager().getUserSerialNumber(userId);
        synchronized (this.mSyncRoot) {
            try {
                this.mPersistentDataStore.setBrightnessConfigurationForUser(c, userSerial, packageName);
                this.mPersistentDataStore.saveIfNeeded();
                if (userId == this.mCurrentUserId) {
                    this.mDisplayPowerController.setBrightnessConfiguration(c);
                }
            } catch (Throwable th) {
                this.mPersistentDataStore.saveIfNeeded();
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void validateBrightnessConfiguration(BrightnessConfiguration config) {
        if (config != null && isBrightnessConfigurationTooDark(config)) {
            throw new IllegalArgumentException("brightness curve is too dark");
        }
    }

    private boolean isBrightnessConfigurationTooDark(BrightnessConfiguration config) {
        Pair<float[], float[]> curve = config.getCurve();
        float[] lux = (float[]) curve.first;
        float[] nits = (float[]) curve.second;
        for (int i = 0; i < lux.length; i++) {
            if (nits[i] < this.mMinimumBrightnessSpline.interpolate(lux[i])) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: private */
    public void loadBrightnessConfiguration() {
        synchronized (this.mSyncRoot) {
            this.mDisplayPowerController.setBrightnessConfiguration(this.mPersistentDataStore.getBrightnessConfiguration(getUserManager().getUserSerialNumber(this.mCurrentUserId)));
        }
    }

    private boolean updateLogicalDisplaysLocked() {
        boolean changed = false;
        int displayId = this.mLogicalDisplays.size();
        while (true) {
            int i = displayId - 1;
            if (displayId <= 0) {
                return changed;
            }
            int displayId2 = this.mLogicalDisplays.keyAt(i);
            LogicalDisplay display = this.mLogicalDisplays.valueAt(i);
            this.mTempDisplayInfo.copyFrom(display.getDisplayInfoLocked());
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                this.mLogicalDisplays.removeAt(i);
                sendDisplayEventLocked(displayId2, 3);
                changed = true;
            } else if (!this.mTempDisplayInfo.equals(display.getDisplayInfoLocked())) {
                handleLogicalDisplayChanged(displayId2, display);
                changed = true;
            }
            displayId = i;
        }
    }

    private void performTraversalLocked(SurfaceControl.Transaction t) {
        clearViewportsLocked();
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i++) {
            DisplayDevice device = this.mDisplayDevices.get(i);
            configureDisplayLocked(t, device);
            device.performTraversalLocked(t);
        }
        if (this.mInputManagerInternal != null) {
            this.mHandler.sendEmptyMessage(5);
        }
    }

    /* access modifiers changed from: private */
    public void setDisplayPropertiesInternal(int displayId, boolean hasContent, float requestedRefreshRate, int requestedModeId, boolean inTraversal) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                if (display.hasContentLocked() != hasContent) {
                    if (DEBUG) {
                        Slog.d(TAG, "Display " + displayId + " hasContent flag changed: hasContent=" + hasContent + ", inTraversal=" + inTraversal);
                    }
                    display.setHasContentLocked(hasContent);
                    scheduleTraversalLocked(inTraversal);
                }
                if (requestedModeId == 0 && requestedRefreshRate != OppoBrightUtils.MIN_LUX_LIMITI) {
                    requestedModeId = display.getDisplayInfoLocked().findDefaultModeByRefreshRate(requestedRefreshRate);
                }
                this.mDisplayModeDirector.getAppRequestObserver().setAppRequestedMode(displayId, requestedModeId);
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0052, code lost:
        return;
     */
    public void setDisplayOffsetsInternal(int displayId, int x, int y) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                if (!(display.getDisplayOffsetXLocked() == x && display.getDisplayOffsetYLocked() == y)) {
                    if (DEBUG) {
                        Slog.d(TAG, "Display " + displayId + " burn-in offset set to (" + x + ", " + y + ")");
                    }
                    display.setDisplayOffsetsLocked(x, y);
                    scheduleTraversalLocked(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x003f, code lost:
        return;
     */
    public void setDisplayScalingDisabledInternal(int displayId, boolean disable) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null) {
                if (display.isDisplayScalingDisabled() != disable) {
                    if (DEBUG) {
                        Slog.d(TAG, "Display " + displayId + " content scaling disabled = " + disable);
                    }
                    display.setDisplayScalingDisabledLocked(disable);
                    scheduleTraversalLocked(false);
                }
            }
        }
    }

    /* access modifiers changed from: private */
    public void setDisplayAccessUIDsInternal(SparseArray<IntArray> newDisplayAccessUIDs) {
        synchronized (this.mSyncRoot) {
            this.mDisplayAccessUIDs.clear();
            for (int i = newDisplayAccessUIDs.size() - 1; i >= 0; i--) {
                this.mDisplayAccessUIDs.append(newDisplayAccessUIDs.keyAt(i), newDisplayAccessUIDs.valueAt(i));
            }
        }
    }

    /* access modifiers changed from: private */
    public boolean isUidPresentOnDisplayInternal(int uid, int displayId) {
        boolean z;
        synchronized (this.mSyncRoot) {
            IntArray displayUIDs = this.mDisplayAccessUIDs.get(displayId);
            z = (displayUIDs == null || displayUIDs.indexOf(uid) == -1) ? false : true;
        }
        return z;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001a, code lost:
        return null;
     */
    private IBinder getDisplayToken(int displayId) {
        DisplayDevice device;
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display != null && (device = display.getPrimaryDisplayDeviceLocked()) != null) {
                IBinder displayTokenLocked = device.getDisplayTokenLocked();
                return displayTokenLocked;
            }
        }
    }

    /* access modifiers changed from: private */
    public SurfaceControl.ScreenshotGraphicBuffer screenshotInternal(int displayId) {
        IBinder token = getDisplayToken(displayId);
        if (token == null) {
            return null;
        }
        return SurfaceControl.screenshotToBufferWithSecureLayersUnsafe(token, new Rect(), 0, 0, false, 0);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public DisplayedContentSamplingAttributes getDisplayedContentSamplingAttributesInternal(int displayId) {
        IBinder token = getDisplayToken(displayId);
        if (token == null) {
            return null;
        }
        return SurfaceControl.getDisplayedContentSamplingAttributes(token);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public boolean setDisplayedContentSamplingEnabledInternal(int displayId, boolean enable, int componentMask, int maxFrames) {
        IBinder token = getDisplayToken(displayId);
        if (token == null) {
            return false;
        }
        return SurfaceControl.setDisplayedContentSamplingEnabled(token, enable, componentMask, maxFrames);
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public DisplayedContentSample getDisplayedContentSampleInternal(int displayId, long maxFrames, long timestamp) {
        IBinder token = getDisplayToken(displayId);
        if (token == null) {
            return null;
        }
        return SurfaceControl.getDisplayedContentSample(token, maxFrames, timestamp);
    }

    /* access modifiers changed from: private */
    public void onAllowedDisplayModesChangedInternal() {
        boolean changed = false;
        synchronized (this.mSyncRoot) {
            int count = this.mLogicalDisplays.size();
            for (int i = 0; i < count; i++) {
                LogicalDisplay display = this.mLogicalDisplays.valueAt(i);
                int[] allowedModes = this.mDisplayModeDirector.getAllowedModes(this.mLogicalDisplays.keyAt(i));
                if (!Arrays.equals(allowedModes, display.getAllowedDisplayModesLocked())) {
                    display.setAllowedDisplayModesLocked(allowedModes);
                    changed = true;
                }
            }
            if (changed) {
                scheduleTraversalLocked(false);
            }
        }
    }

    private void clearViewportsLocked() {
        this.mViewports.clear();
    }

    private void configureDisplayLocked(SurfaceControl.Transaction t, DisplayDevice device) {
        int viewportType;
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        boolean z = false;
        boolean ownContent = (info.flags & 128) != 0;
        LogicalDisplay display = findLogicalDisplayForDeviceLocked(device);
        if (!ownContent) {
            if (display != null && !display.hasContentLocked()) {
                display = null;
            }
            if (display == null) {
                display = this.mLogicalDisplays.get(0);
            }
        }
        if (display == null) {
            Slog.w(TAG, "Missing logical display to use for physical display device: " + device.getDisplayDeviceInfoLocked());
            return;
        }
        if (info.state == 1) {
            z = true;
        }
        display.configureDisplayLocked(t, device, z);
        if ((info.flags & 1) != 0) {
            viewportType = 1;
        } else if (info.touch == 2) {
            viewportType = 2;
        } else if (info.touch != 3 || TextUtils.isEmpty(info.uniqueId)) {
            Slog.i(TAG, "Display " + info + " does not support input device matching.");
            return;
        } else {
            viewportType = 3;
        }
        populateViewportLocked(viewportType, display.getDisplayIdLocked(), device, info.uniqueId);
    }

    private DisplayViewport getViewportLocked(int viewportType, String uniqueId) {
        if (viewportType == 1 || viewportType == 2 || viewportType == 3) {
            if (viewportType != 3) {
                uniqueId = "";
            }
            int count = this.mViewports.size();
            for (int i = 0; i < count; i++) {
                DisplayViewport viewport = this.mViewports.get(i);
                if (viewport.type == viewportType && uniqueId.equals(viewport.uniqueId)) {
                    return viewport;
                }
            }
            DisplayViewport viewport2 = new DisplayViewport();
            viewport2.type = viewportType;
            viewport2.uniqueId = uniqueId;
            this.mViewports.add(viewport2);
            return viewport2;
        }
        Slog.wtf(TAG, "Cannot call getViewportByTypeLocked for type " + DisplayViewport.typeToString(viewportType));
        return null;
    }

    private void populateViewportLocked(int viewportType, int displayId, DisplayDevice device, String uniqueId) {
        DisplayViewport viewport = getViewportLocked(viewportType, uniqueId);
        device.populateViewportLocked(viewport);
        viewport.valid = true;
        viewport.displayId = displayId;
    }

    private LogicalDisplay findLogicalDisplayForDeviceLocked(DisplayDevice device) {
        int count = this.mLogicalDisplays.size();
        for (int i = 0; i < count; i++) {
            LogicalDisplay display = this.mLogicalDisplays.valueAt(i);
            if (display.getPrimaryDisplayDeviceLocked() == device) {
                return display;
            }
        }
        return null;
    }

    private void sendDisplayEventLocked(int displayId, int event) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, displayId, event));
    }

    /* access modifiers changed from: private */
    public void scheduleTraversalLocked(boolean inTraversal) {
        if (!this.mPendingTraversal && this.mWindowManagerInternal != null) {
            this.mPendingTraversal = true;
            if (!inTraversal) {
                this.mHandler.sendEmptyMessage(4);
            }
        }
    }

    /* access modifiers changed from: private */
    public void deliverDisplayEvent(int displayId, int event) {
        int count;
        if (DEBUG) {
            Slog.d(TAG, "Delivering display event: displayId=" + displayId + ", event=" + event);
        }
        synchronized (this.mSyncRoot) {
            count = this.mCallbacks.size();
            this.mTempCallbacks.clear();
            for (int i = 0; i < count; i++) {
                this.mTempCallbacks.add(this.mCallbacks.valueAt(i));
            }
        }
        for (int i2 = 0; i2 < count; i2++) {
            this.mTempCallbacks.get(i2).notifyDisplayEventAsync(displayId, event);
        }
        this.mTempCallbacks.clear();
    }

    /* access modifiers changed from: private */
    public IMediaProjectionManager getProjectionService() {
        if (this.mProjectionService == null) {
            this.mProjectionService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        }
        return this.mProjectionService;
    }

    /* access modifiers changed from: private */
    public UserManager getUserManager() {
        return (UserManager) this.mContext.getSystemService(UserManager.class);
    }

    /* access modifiers changed from: private */
    public void dumpInternal(PrintWriter pw) {
        pw.println("DISPLAY MANAGER (dumpsys display)");
        synchronized (this.mSyncRoot) {
            pw.println("  mOnlyCode=" + this.mOnlyCore);
            pw.println("  mSafeMode=" + this.mSafeMode);
            pw.println("  mPendingTraversal=" + this.mPendingTraversal);
            pw.println("  mGlobalDisplayState=" + Display.stateToString(this.mGlobalDisplayState));
            pw.println("  mNextNonDefaultDisplayId=" + this.mNextNonDefaultDisplayId);
            pw.println("  mViewports=" + this.mViewports);
            pw.println("  mDefaultDisplayDefaultColorMode=" + this.mDefaultDisplayDefaultColorMode);
            pw.println("  mSingleDisplayDemoMode=" + this.mSingleDisplayDemoMode);
            pw.println("  mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
            pw.println("  mStableDisplaySize=" + this.mStableDisplaySize);
            pw.println("  mMinimumBrightnessCurve=" + this.mMinimumBrightnessCurve);
            IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "    ");
            ipw.increaseIndent();
            pw.println();
            pw.println("Display Adapters: size=" + this.mDisplayAdapters.size());
            Iterator<DisplayAdapter> it = this.mDisplayAdapters.iterator();
            while (it.hasNext()) {
                DisplayAdapter adapter = it.next();
                pw.println("  " + adapter.getName());
                adapter.dumpLocked(ipw);
            }
            pw.println();
            pw.println("Display Devices: size=" + this.mDisplayDevices.size());
            Iterator<DisplayDevice> it2 = this.mDisplayDevices.iterator();
            while (it2.hasNext()) {
                DisplayDevice device = it2.next();
                pw.println("  " + device.getDisplayDeviceInfoLocked());
                device.dumpLocked(ipw);
            }
            int logicalDisplayCount = this.mLogicalDisplays.size();
            pw.println();
            pw.println("Logical Displays: size=" + logicalDisplayCount);
            for (int i = 0; i < logicalDisplayCount; i++) {
                int displayId = this.mLogicalDisplays.keyAt(i);
                pw.println("  Display " + displayId + ":");
                this.mLogicalDisplays.valueAt(i).dumpLocked(ipw);
            }
            pw.println();
            this.mDisplayModeDirector.dump(pw);
            int callbackCount = this.mCallbacks.size();
            pw.println();
            pw.println("Callbacks: size=" + callbackCount);
            for (int i2 = 0; i2 < callbackCount; i2++) {
                CallbackRecord callback = this.mCallbacks.valueAt(i2);
                pw.println("  " + i2 + ": mPid=" + callback.mPid + ", mWifiDisplayScanRequested=" + callback.mWifiDisplayScanRequested);
            }
            if (this.mDisplayPowerController != null) {
                this.mDisplayPowerController.dump(pw);
            }
            pw.println();
            this.mPersistentDataStore.dump(pw);
        }
    }

    private void sendDisplayDcsMsg(String strMsg) {
        try {
            Map<String, String> logMap = new HashMap<>();
            logMap.put(KEY_DISPLAY_EVENT_REGISTER_CALLBACK, strMsg);
            OppoStatistics.onCommon(this.mContext, "20120", DCS_LOG_EVENT_ID, logMap, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static float[] getFloatArray(TypedArray array) {
        int length = array.length();
        float[] floatArray = new float[length];
        for (int i = 0; i < length; i++) {
            floatArray[i] = array.getFloat(i, Float.NaN);
        }
        array.recycle();
        return floatArray;
    }

    @VisibleForTesting
    static class Injector {
        Injector() {
        }

        /* access modifiers changed from: package-private */
        public VirtualDisplayAdapter getVirtualDisplayAdapter(SyncRoot syncRoot, Context context, Handler handler, DisplayAdapter.Listener displayAdapterListener) {
            return new VirtualDisplayAdapter(syncRoot, context, handler, displayAdapterListener);
        }

        /* access modifiers changed from: package-private */
        public long getDefaultDisplayDelayTimeout() {
            return 10000;
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public DisplayDeviceInfo getDisplayDeviceInfoInternal(int displayId) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = this.mLogicalDisplays.get(displayId);
            if (display == null) {
                return null;
            }
            DisplayDeviceInfo displayDeviceInfoLocked = display.getPrimaryDisplayDeviceLocked().getDisplayDeviceInfoLocked();
            return displayDeviceInfoLocked;
        }
    }

    /* access modifiers changed from: private */
    public final class DisplayManagerHandler extends Handler {
        public DisplayManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            boolean changed;
            switch (msg.what) {
                case 1:
                    DisplayManagerService.this.registerDefaultDisplayAdapters();
                    return;
                case 2:
                    DisplayManagerService.this.registerAdditionalDisplayAdapters();
                    return;
                case 3:
                    DisplayManagerService.this.deliverDisplayEvent(msg.arg1, msg.arg2);
                    return;
                case 4:
                    DisplayManagerService.this.mWindowManagerInternal.requestTraversalFromDisplayManager();
                    return;
                case 5:
                    synchronized (DisplayManagerService.this.mSyncRoot) {
                        changed = !DisplayManagerService.this.mTempViewports.equals(DisplayManagerService.this.mViewports);
                        if (changed) {
                            DisplayManagerService.this.mTempViewports.clear();
                            Iterator it = DisplayManagerService.this.mViewports.iterator();
                            while (it.hasNext()) {
                                DisplayManagerService.this.mTempViewports.add(((DisplayViewport) it.next()).makeCopy());
                            }
                        }
                    }
                    if (changed) {
                        DisplayManagerService.this.mInputManagerInternal.setDisplayViewports(DisplayManagerService.this.mTempViewports);
                        return;
                    }
                    return;
                case 6:
                    DisplayManagerService.this.loadBrightnessConfiguration();
                    return;
                case 7:
                    DisplayManagerService.this.showToast(DisplayManagerService.this.mContext.getResources().getString(201653646));
                    return;
                default:
                    return;
            }
        }
    }

    /* access modifiers changed from: private */
    public void showToast(String message) {
        Toast.makeText(this.mContext, message, 0).show();
    }

    private final class DisplayAdapterListener implements DisplayAdapter.Listener {
        private DisplayAdapterListener() {
        }

        @Override // com.android.server.display.DisplayAdapter.Listener
        public void onDisplayDeviceEvent(DisplayDevice device, int event) {
            if (event == 1) {
                DisplayManagerService.this.handleDisplayDeviceAdded(device);
            } else if (event == 2) {
                DisplayManagerService.this.handleDisplayDeviceChanged(device);
            } else if (event == 3) {
                DisplayManagerService.this.handleDisplayDeviceRemoved(device);
            }
        }

        @Override // com.android.server.display.DisplayAdapter.Listener
        public void onTraversalRequested() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.scheduleTraversalLocked(false);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class CallbackRecord implements IBinder.DeathRecipient {
        private final IDisplayManagerCallback mCallback;
        public final int mPid;
        public final int mUid;
        public boolean mWifiDisplayScanRequested;

        public CallbackRecord(int pid, IDisplayManagerCallback callback, int callingUid) {
            this.mPid = pid;
            this.mCallback = callback;
            this.mUid = callingUid;
        }

        public boolean isCallbackAlive() {
            IDisplayManagerCallback iDisplayManagerCallback = this.mCallback;
            if (iDisplayManagerCallback != null) {
                return iDisplayManagerCallback.asBinder().isBinderAlive();
            }
            return false;
        }

        public CallbackRecord(int pid, IDisplayManagerCallback callback) {
            this.mPid = pid;
            this.mCallback = callback;
            this.mUid = 0;
        }

        public void binderDied() {
            if (DisplayManagerService.DEBUG) {
                Slog.d(DisplayManagerService.TAG, "Display listener for pid " + this.mPid + " died.");
            }
            DisplayManagerService.this.onCallbackDied(this);
        }

        public void notifyDisplayEventAsync(int displayId, int event) {
            try {
                this.mCallback.onDisplayEvent(displayId, event);
            } catch (RemoteException ex) {
                Slog.w(DisplayManagerService.TAG, "Failed to notify process " + this.mPid + " that displays changed, assuming it died.", ex);
                binderDied();
            }
        }
    }

    @VisibleForTesting
    protected final class BinderService extends OppoBaseDisplayManagerService.OppoBaseBinderService {
        protected BinderService() {
            super();
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getDisplayInfoInternal(displayId, callingUid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public int[] getDisplayIds() {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getDisplayIdsInternal(callingUid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public boolean isUidPresentOnDisplay(int uid, int displayId) {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.isUidPresentOnDisplayInternal(uid, displayId);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public Point getStableDisplaySize() {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getStableDisplaySizeInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void registerCallback(IDisplayManagerCallback callback) {
            if (callback != null) {
                int callingUid = Binder.getCallingUid();
                int callingPid = Binder.getCallingPid();
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.registerCallbackInternal(callback, callingPid, callingUid);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("listener must not be null");
            }
        }

        public void startWifiDisplayScan() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to start wifi display scans");
            int callingPid = Binder.getCallingPid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.startWifiDisplayScanInternal(callingPid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void stopWifiDisplayScan() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to stop wifi display scans");
            int callingPid = Binder.getCallingPid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.stopWifiDisplayScanInternal(callingPid);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void connectWifiDisplay(String address) {
            if (address != null) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to connect to a wifi display");
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.connectWifiDisplayInternal(address);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("address must not be null");
            }
        }

        public void disconnectWifiDisplay() {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.disconnectWifiDisplayInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void renameWifiDisplay(String address, String alias) {
            if (address != null) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to rename to a wifi display");
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.renameWifiDisplayInternal(address, alias);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("address must not be null");
            }
        }

        public void forgetWifiDisplay(String address) {
            if (address != null) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to forget to a wifi display");
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.forgetWifiDisplayInternal(address);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("address must not be null");
            }
        }

        public void pauseWifiDisplay() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to pause a wifi display session");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.pauseWifiDisplayInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void resumeWifiDisplay() {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to resume a wifi display session");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.resumeWifiDisplayInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public WifiDisplayStatus getWifiDisplayStatus() {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getWifiDisplayStatusInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void requestColorMode(int displayId, int colorMode) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_COLOR_MODE", "Permission required to change the display color mode");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.requestColorModeInternal(displayId, colorMode);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public int createVirtualDisplay(IVirtualDisplayCallback callback, IMediaProjection projection, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags, String uniqueId) {
            int flags2;
            int flags3;
            int flags4;
            int callingUid = Binder.getCallingUid();
            if (!validatePackageName(callingUid, packageName)) {
                throw new SecurityException("packageName must match the calling uid");
            } else if (callback == null) {
                throw new IllegalArgumentException("appToken must not be null");
            } else if (TextUtils.isEmpty(name)) {
                throw new IllegalArgumentException("name must be non-null and non-empty");
            } else if (width <= 0 || height <= 0 || densityDpi <= 0) {
                throw new IllegalArgumentException("width, height, and densityDpi must be greater than 0");
            } else if (surface == null || !surface.isSingleBuffered()) {
                if ((flags & 1) != 0) {
                    flags2 = flags | 16;
                    if ((flags2 & 32) != 0) {
                        throw new IllegalArgumentException("Public display must not be marked as SHOW_WHEN_LOCKED_INSECURE");
                    }
                } else {
                    flags2 = flags;
                }
                if ((flags2 & 8) != 0) {
                    flags3 = flags2 & -17;
                } else {
                    flags3 = flags2;
                }
                if (projection != null) {
                    try {
                        if (DisplayManagerService.this.getProjectionService().isValidMediaProjection(projection)) {
                            flags4 = projection.applyVirtualDisplayFlags(flags3);
                        } else {
                            throw new SecurityException("Invalid media projection");
                        }
                    } catch (RemoteException e) {
                        throw new SecurityException("unable to validate media projection or flags");
                    }
                } else {
                    flags4 = flags3;
                }
                if (callingUid != 1000 && (flags4 & 16) != 0 && !canProjectVideo(projection)) {
                    throw new SecurityException("Requires CAPTURE_VIDEO_OUTPUT or CAPTURE_SECURE_VIDEO_OUTPUT permission, or an appropriate MediaProjection token in order to create a screen sharing virtual display.");
                } else if (callingUid != 1000 && (flags4 & 4) != 0 && !canProjectSecureVideo(projection)) {
                    throw new SecurityException("Requires CAPTURE_SECURE_VIDEO_OUTPUT or an appropriate MediaProjection token to create a secure virtual display.");
                } else if (callingUid == 1000 || (flags4 & 512) == 0 || checkCallingPermission("android.permission.INTERNAL_SYSTEM_WINDOW", "createVirtualDisplay()")) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        try {
                            int access$3500 = DisplayManagerService.this.createVirtualDisplayInternal(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags4, uniqueId);
                            Binder.restoreCallingIdentity(token);
                            return access$3500;
                        } catch (Throwable th) {
                            th = th;
                            Binder.restoreCallingIdentity(token);
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        Binder.restoreCallingIdentity(token);
                        throw th;
                    }
                } else {
                    throw new SecurityException("Requires INTERNAL_SYSTEM_WINDOW permission");
                }
            } else {
                throw new IllegalArgumentException("Surface can't be single-buffered");
            }
        }

        public void resizeVirtualDisplay(IVirtualDisplayCallback callback, int width, int height, int densityDpi) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.resizeVirtualDisplayInternal(callback.asBinder(), width, height, densityDpi);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setVirtualDisplaySurface(IVirtualDisplayCallback callback, Surface surface) {
            if (surface == null || !surface.isSingleBuffered()) {
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.setVirtualDisplaySurfaceInternal(callback.asBinder(), surface);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            } else {
                throw new IllegalArgumentException("Surface can't be single-buffered");
            }
        }

        public void releaseVirtualDisplay(IVirtualDisplayCallback callback) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.releaseVirtualDisplayInternal(callback.asBinder());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setVirtualDisplayState(IVirtualDisplayCallback callback, boolean isOn) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.setVirtualDisplayStateInternal(callback.asBinder(), isOn);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* JADX INFO: Multiple debug info for r0v6 java.lang.String: [D('logCategoryTag' java.lang.String), D('index' int)] */
        /* access modifiers changed from: protected */
        public boolean dynamicallyConfigDisplayLogTag(PrintWriter pw, String[] args) {
            if (args.length < 1 || !"log".equals(args[0])) {
                return false;
            }
            if (args.length != 3) {
                pw.println("Invalid argument! Get detail help as bellow:");
                logOutDisplayLogTagHelp(pw);
                return true;
            }
            pw.println("dynamicallyConfigDisplayLogTag, args.length:" + args.length);
            for (int index = 0; index < args.length; index++) {
                pw.println("dynamicallyConfigPowerLogTag, args[" + index + "]:" + args[index]);
            }
            String logCategoryTag = args[1];
            boolean on = "1".equals(args[2]);
            pw.println("dynamicallyConfigDisplayLogTag, logCategoryTag:" + logCategoryTag + ", on:" + on);
            if ("all".equals(logCategoryTag)) {
                boolean unused = DisplayManagerService.DEBUG = on;
                DisplayPowerController.DEBUG = on;
                DisplayPowerController.DEBUG_PANIC = on;
                OppoAutomaticBrightnessController.DEBUG = on;
                DisplayPowerState.DEBUG = on;
                OppoBrightUtils.DEBUG = on;
            } else if ("panic".equals(logCategoryTag)) {
                DisplayPowerController.DEBUG_PANIC = on;
            } else if ("state".equals(logCategoryTag)) {
                DisplayPowerState.DEBUG = on;
            } else if ("report".equals(logCategoryTag)) {
                OppoBrightUtils.REPORT_FEATURE_ON = on;
            } else if (!"report_time".equals(logCategoryTag)) {
                pw.println("Invalid log tag argument! Get detail help as bellow:");
                logOutDisplayLogTagHelp(pw);
            }
            return true;
        }

        /* access modifiers changed from: protected */
        public void logOutDisplayLogTagHelp(PrintWriter pw) {
            pw.println("********************** Help begin:**********************");
            pw.println("1 All display log:DEBUG | DisplayPowerController");
            pw.println("cmd: dumpsys display log all 0/1");
            pw.println("----------------------------------");
            pw.println("2 lightWeight power log: DisplayPowerController");
            pw.println("cmd: dumpsys display log panic 0/1");
            pw.println("----------------------------------");
            pw.println("********************** Help end.  **********************");
        }

        /* access modifiers changed from: protected */
        public boolean dynamicallyConfigDisplaySensorTag(PrintWriter pw, String[] args) {
            if (args.length < 1 || !"psensor".equals(args[0])) {
                return false;
            }
            if (args.length != 2) {
                pw.println("Invalid argument! input adb shell dumpsys display psensor 0");
                return true;
            }
            for (int index = 0; index < args.length; index++) {
                pw.println("dynamicallyConfigsensorTag, args[" + index + "]:" + args[index]);
            }
            if ("0".equals(args[1])) {
                OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT = true;
                pw.println("dynamicallyConfigDisplaySensorTag: disable psensor");
            } else if ("1".equals(args[1])) {
                OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT = false;
                pw.println("dynamicallyConfigDisplaySensorTag: enable psensor");
            } else {
                pw.println("Invalid psenor tag argument! input adb shell dumpsys display psensor 0");
            }
            return true;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DumpUtils.checkDumpPermission(DisplayManagerService.this.mContext, DisplayManagerService.TAG, pw) && !dynamicallyConfigDisplayLogTag(pw, args) && !dynamicallyConfigDisplaySensorTag(pw, args)) {
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.dumpInternal(pw);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        public ParceledListSlice<BrightnessChangeEvent> getBrightnessEvents(String callingPackage) {
            ParceledListSlice<BrightnessChangeEvent> brightnessEvents;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.BRIGHTNESS_SLIDER_USAGE", "Permission to read brightness events.");
            int callingUid = Binder.getCallingUid();
            int mode = ((AppOpsManager) DisplayManagerService.this.mContext.getSystemService(AppOpsManager.class)).noteOp(43, callingUid, callingPackage);
            boolean hasUsageStats = true;
            if (mode == 3) {
                if (DisplayManagerService.this.mContext.checkCallingPermission("android.permission.PACKAGE_USAGE_STATS") != 0) {
                    hasUsageStats = false;
                }
            } else if (mode != 0) {
                hasUsageStats = false;
            }
            int userId = UserHandle.getUserId(callingUid);
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    brightnessEvents = DisplayManagerService.this.mDisplayPowerController.getBrightnessEvents(userId, hasUsageStats);
                }
                return brightnessEvents;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public ParceledListSlice<AmbientBrightnessDayStats> getAmbientBrightnessStats() {
            ParceledListSlice<AmbientBrightnessDayStats> ambientBrightnessStats;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.ACCESS_AMBIENT_LIGHT_STATS", "Permission required to to access ambient light stats.");
            int userId = UserHandle.getUserId(Binder.getCallingUid());
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    ambientBrightnessStats = DisplayManagerService.this.mDisplayPowerController.getAmbientBrightnessStats(userId);
                }
                return ambientBrightnessStats;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setBrightnessConfigurationForUser(BrightnessConfiguration c, int userId, String packageName) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_BRIGHTNESS", "Permission required to change the display's brightness configuration");
            if (userId != UserHandle.getCallingUserId()) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Permission required to change the display brightness configuration of another user");
            }
            if (packageName != null && !validatePackageName(getCallingUid(), packageName)) {
                packageName = null;
            }
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.setBrightnessConfigurationForUserInternal(c, userId, packageName);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public BrightnessConfiguration getBrightnessConfigurationForUser(int userId) {
            BrightnessConfiguration config;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_BRIGHTNESS", "Permission required to read the display's brightness configuration");
            if (userId != UserHandle.getCallingUserId()) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.INTERACT_ACROSS_USERS", "Permission required to read the display brightness configuration of another user");
            }
            long token = Binder.clearCallingIdentity();
            try {
                int userSerial = DisplayManagerService.this.getUserManager().getUserSerialNumber(userId);
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    config = DisplayManagerService.this.mPersistentDataStore.getBrightnessConfiguration(userSerial);
                    if (config == null) {
                        config = DisplayManagerService.this.mDisplayPowerController.getDefaultBrightnessConfiguration();
                    }
                }
                return config;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public BrightnessConfiguration getDefaultBrightnessConfiguration() {
            BrightnessConfiguration defaultBrightnessConfiguration;
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_DISPLAY_BRIGHTNESS", "Permission required to read the display's default brightness configuration");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    defaultBrightnessConfiguration = DisplayManagerService.this.mDisplayPowerController.getDefaultBrightnessConfiguration();
                }
                return defaultBrightnessConfiguration;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setRotateState(boolean isStart) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("oppo.permission.DISPLAY_ROTATE_STATE", "Permission required to set the display's rotate state");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setRotateState(isStart);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setTemporaryBrightness(int brightness) {
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_DISPLAY_BRIGHTNESS", "Permission required to set the display's brightness");
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setTemporaryBrightness(brightness);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void setTemporaryAutoBrightnessAdjustment(float adjustment) {
            HashSet<Integer> targetUids = new HashSet<>();
            try {
                PackageManager pm = DisplayManagerService.this.mContext.getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo("com.coloros.gallery3d", 1);
                if (ai != null) {
                    targetUids.add(Integer.valueOf(ai.uid));
                }
                ApplicationInfo ai2 = pm.getApplicationInfo("com.coloros.video", 1);
                if (ai2 != null) {
                    targetUids.add(Integer.valueOf(ai2.uid));
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            int uid = Binder.getCallingUid();
            Integer uid_integer = new Integer(uid);
            if (DisplayManagerService.DEBUG) {
                Slog.d(DisplayManagerService.TAG, "setTemporaryAutoBrightnessAdjustment: targetUids = " + targetUids + ", uid = " + uid);
            }
            if (targetUids.size() <= 0 || !targetUids.contains(uid_integer)) {
                DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONTROL_DISPLAY_BRIGHTNESS", "Permission required to set the display's auto brightness adjustment");
            }
            long token = Binder.clearCallingIdentity();
            try {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setTemporaryAutoBrightnessAdjustment(adjustment);
                }
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void onShellCommand(FileDescriptor in, FileDescriptor out, FileDescriptor err, String[] args, ShellCallback callback, ResultReceiver resultReceiver) {
            long token = Binder.clearCallingIdentity();
            try {
                try {
                    new DisplayManagerShellCommand(this).exec(this, in, out, err, args, callback, resultReceiver);
                    Binder.restoreCallingIdentity(token);
                } catch (Throwable th) {
                    th = th;
                    Binder.restoreCallingIdentity(token);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        public Curve getMinimumBrightnessCurve() {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getMinimumBrightnessCurveInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public int getPreferredWideGamutColorSpaceId() {
            long token = Binder.clearCallingIdentity();
            try {
                return DisplayManagerService.this.getPreferredWideGamutColorSpaceIdInternal();
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        /* access modifiers changed from: package-private */
        public void setBrightness(int brightness) {
            Settings.System.putIntForUser(DisplayManagerService.this.mContext.getContentResolver(), "screen_brightness", brightness, -2);
        }

        /* access modifiers changed from: package-private */
        public void resetBrightnessConfiguration() {
            DisplayManagerService displayManagerService = DisplayManagerService.this;
            displayManagerService.setBrightnessConfigurationForUserInternal(null, displayManagerService.mContext.getUserId(), DisplayManagerService.this.mContext.getPackageName());
        }

        /* access modifiers changed from: package-private */
        public void setAutoBrightnessLoggingEnabled(boolean enabled) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setAutoBrightnessLoggingEnabled(enabled);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setDisplayWhiteBalanceLoggingEnabled(boolean enabled) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setDisplayWhiteBalanceLoggingEnabled(enabled);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void setAmbientColorTemperatureOverride(float cct) {
            if (DisplayManagerService.this.mDisplayPowerController != null) {
                synchronized (DisplayManagerService.this.mSyncRoot) {
                    DisplayManagerService.this.mDisplayPowerController.setAmbientColorTemperatureOverride(cct);
                }
            }
        }

        private boolean validatePackageName(int uid, String packageName) {
            String[] packageNames;
            if (!(packageName == null || (packageNames = DisplayManagerService.this.mContext.getPackageManager().getPackagesForUid(uid)) == null)) {
                for (String n : packageNames) {
                    if (n.equals(packageName)) {
                        return true;
                    }
                }
            }
            return false;
        }

        private boolean canProjectVideo(IMediaProjection projection) {
            if (projection != null) {
                try {
                    if (projection.canProjectVideo()) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Slog.e(DisplayManagerService.TAG, "Unable to query projection service for permissions", e);
                }
            }
            if (checkCallingPermission("android.permission.CAPTURE_VIDEO_OUTPUT", "canProjectVideo()")) {
                return true;
            }
            return canProjectSecureVideo(projection);
        }

        private boolean canProjectSecureVideo(IMediaProjection projection) {
            if (projection != null) {
                try {
                    if (projection.canProjectSecureVideo()) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Slog.e(DisplayManagerService.TAG, "Unable to query projection service for permissions", e);
                }
            }
            return checkCallingPermission("android.permission.CAPTURE_SECURE_VIDEO_OUTPUT", "canProjectSecureVideo()");
        }

        private boolean checkCallingPermission(String permission, String func) {
            if (DisplayManagerService.this.mContext.checkCallingPermission(permission) == 0) {
                return true;
            }
            Slog.w(DisplayManagerService.TAG, "Permission Denial: " + func + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + " requires " + permission);
            return false;
        }
    }

    private final class LocalService extends DisplayManagerInternal {
        private LocalService() {
        }

        public void initPowerManagement(final DisplayManagerInternal.DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager) {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayPowerController unused = DisplayManagerService.this.mDisplayPowerController = new DisplayPowerController(DisplayManagerService.this.mContext, callbacks, handler, sensorManager, new DisplayBlanker() {
                    /* class com.android.server.display.DisplayManagerService.LocalService.AnonymousClass1 */

                    @Override // com.android.server.display.DisplayBlanker
                    public void requestDisplayState(int state, int brightness) {
                        if (state == 1) {
                            DisplayManagerService.this.requestGlobalDisplayStateInternal(state, brightness);
                        }
                        callbacks.onDisplayStateChange(state);
                        if (state != 1) {
                            DisplayManagerService.this.requestGlobalDisplayStateInternal(state, brightness);
                        }
                    }
                });
                DisplayManagerService.this.initPowerManagement(DisplayManagerService.this.mDisplayPowerController);
            }
            DisplayManagerService.this.mHandler.sendEmptyMessage(6);
        }

        public boolean requestPowerState(DisplayManagerInternal.DisplayPowerRequest request, boolean waitForNegativeProximity) {
            boolean requestPowerState;
            synchronized (DisplayManagerService.this.mSyncRoot) {
                requestPowerState = DisplayManagerService.this.mDisplayPowerController.requestPowerState(request, waitForNegativeProximity);
            }
            return requestPowerState;
        }

        public boolean isProximitySensorAvailable() {
            boolean isProximitySensorAvailable;
            synchronized (DisplayManagerService.this.mSyncRoot) {
                isProximitySensorAvailable = DisplayManagerService.this.mDisplayPowerController.isProximitySensorAvailable();
            }
            return isProximitySensorAvailable;
        }

        public void updateFadeOffDuration(long duration) {
            DisplayManagerService.this.mDisplayPowerController.updateFadeOffDuration(duration);
        }

        public SurfaceControl.ScreenshotGraphicBuffer screenshot(int displayId) {
            return DisplayManagerService.this.screenshotInternal(displayId);
        }

        public void blockScreenOnByBiometrics(String reason) {
            DisplayManagerService.this.mDisplayPowerController.blockScreenOnByBiometrics(reason);
        }

        public void unblockScreenOnByBiometrics(String reason) {
            DisplayManagerService.this.mDisplayPowerController.unblockScreenOnByBiometrics(reason);
        }

        public boolean isBlockScreenOnByBiometrics() {
            return DisplayManagerService.this.mDisplayPowerController.isBlockScreenOnByBiometrics();
        }

        public void removeFaceBlockReasonFromBlockReasonList() {
            DisplayManagerService.this.mDisplayPowerController.removeFaceBlockReasonFromBlockReasonList();
        }

        public boolean isBlockDisplayByBiometrics() {
            return DisplayManagerService.this.mDisplayPowerController.isBlockDisplayByBiometrics();
        }

        public int getScreenState() {
            return DisplayManagerService.this.mDisplayPowerController.getScreenState();
        }

        public boolean hasBiometricsBlockedReason(String reason) {
            return DisplayManagerService.this.mDisplayPowerController.hasBiometricsBlockedReason(reason);
        }

        public void setUseProximityForceSuspend(boolean enable) {
            DisplayManagerService.this.mDisplayPowerController.setUseProximityForceSuspend(enable);
        }

        public void updateScreenOnBlockedState(boolean isBlockedScreenOn) {
            DisplayManagerService.this.mDisplayPowerController.updateScreenOnBlockedState(isBlockedScreenOn);
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            return DisplayManagerService.this.getDisplayInfoInternal(displayId, Process.myUid());
        }

        public void registerDisplayTransactionListener(DisplayManagerInternal.DisplayTransactionListener listener) {
            if (listener != null) {
                DisplayManagerService.this.registerDisplayTransactionListenerInternal(listener);
                return;
            }
            throw new IllegalArgumentException("listener must not be null");
        }

        public void unregisterDisplayTransactionListener(DisplayManagerInternal.DisplayTransactionListener listener) {
            if (listener != null) {
                DisplayManagerService.this.unregisterDisplayTransactionListenerInternal(listener);
                return;
            }
            throw new IllegalArgumentException("listener must not be null");
        }

        public void setDisplayInfoOverrideFromWindowManager(int displayId, DisplayInfo info) {
            DisplayManagerService.this.setDisplayInfoOverrideFromWindowManagerInternal(displayId, info);
        }

        public void getNonOverrideDisplayInfo(int displayId, DisplayInfo outInfo) {
            DisplayManagerService.this.getNonOverrideDisplayInfoInternal(displayId, outInfo);
        }

        public void performTraversal(SurfaceControl.Transaction t) {
            DisplayManagerService.this.performTraversalInternal(t);
        }

        public void setDisplayProperties(int displayId, boolean hasContent, float requestedRefreshRate, int requestedMode, boolean inTraversal) {
            DisplayManagerService.this.setDisplayPropertiesInternal(displayId, hasContent, requestedRefreshRate, requestedMode, inTraversal);
        }

        public void setDisplayOffsets(int displayId, int x, int y) {
            DisplayManagerService.this.setDisplayOffsetsInternal(displayId, x, y);
        }

        public void setDisplayScalingDisabled(int displayId, boolean disableScaling) {
            DisplayManagerService.this.setDisplayScalingDisabledInternal(displayId, disableScaling);
        }

        public void setDisplayAccessUIDs(SparseArray<IntArray> newDisplayAccessUIDs) {
            DisplayManagerService.this.setDisplayAccessUIDsInternal(newDisplayAccessUIDs);
        }

        public void persistBrightnessTrackerState() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.mDisplayPowerController.persistBrightnessTrackerState();
            }
        }

        public void onOverlayChanged() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                for (int i = 0; i < DisplayManagerService.this.mDisplayDevices.size(); i++) {
                    ((DisplayDevice) DisplayManagerService.this.mDisplayDevices.get(i)).onOverlayChangedLocked();
                }
            }
        }

        public DisplayedContentSamplingAttributes getDisplayedContentSamplingAttributes(int displayId) {
            return DisplayManagerService.this.getDisplayedContentSamplingAttributesInternal(displayId);
        }

        public boolean setDisplayedContentSamplingEnabled(int displayId, boolean enable, int componentMask, int maxFrames) {
            return DisplayManagerService.this.setDisplayedContentSamplingEnabledInternal(displayId, enable, componentMask, maxFrames);
        }

        public DisplayedContentSample getDisplayedContentSample(int displayId, long maxFrames, long timestamp) {
            return DisplayManagerService.this.getDisplayedContentSampleInternal(displayId, maxFrames, timestamp);
        }

        public void setOutdoorMode(boolean enable) {
            DisplayManagerService.this.mDisplayPowerController.setOutdoorModeInBase(enable);
        }
    }

    class AllowedDisplayModeObserver implements DisplayModeDirector.Listener {
        AllowedDisplayModeObserver() {
        }

        @Override // com.android.server.display.DisplayModeDirector.Listener
        public void onAllowedDisplayModesChanged() {
            DisplayManagerService.this.onAllowedDisplayModesChangedInternal();
        }
    }
}
