package com.android.server.display;

import android.content.Context;
import android.hardware.SensorManager;
import android.hardware.display.DisplayManagerInternal;
import android.hardware.display.DisplayManagerInternal.DisplayPowerCallbacks;
import android.hardware.display.DisplayManagerInternal.DisplayPowerRequest;
import android.hardware.display.DisplayManagerInternal.DisplayTransactionListener;
import android.hardware.display.DisplayViewport;
import android.hardware.display.IDisplayManager.Stub;
import android.hardware.display.IDisplayManagerCallback;
import android.hardware.display.IVirtualDisplayCallback;
import android.hardware.display.WifiDisplayStatus;
import android.hardware.input.InputManagerInternal;
import android.media.projection.IMediaProjection;
import android.media.projection.IMediaProjectionManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.DisplayInfo;
import android.view.Surface;
import android.view.WindowManagerInternal;
import com.android.internal.util.IndentingPrintWriter;
import com.android.server.DisplayThread;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.OppoBPMHelper;
import com.android.server.SystemService;
import com.android.server.UiThread;
import com.android.server.am.OppoGameSpaceManagerUtils;
import com.android.server.display.DisplayAdapter.Listener;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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
public final class DisplayManagerService extends SystemService {
    private static boolean DEBUG = false;
    private static final String FORCE_WIFI_DISPLAY_ENABLE = "persist.debug.wfd.enable";
    private static final int MSG_DELIVER_DISPLAY_EVENT = 3;
    private static final int MSG_REGISTER_ADDITIONAL_DISPLAY_ADAPTERS = 2;
    private static final int MSG_REGISTER_DEFAULT_DISPLAY_ADAPTER = 1;
    private static final int MSG_REQUEST_TRAVERSAL = 4;
    private static final int MSG_UPDATE_VIEWPORT = 5;
    private static final String TAG = "DisplayManagerService";
    private static final long WAIT_FOR_DEFAULT_DISPLAY_TIMEOUT = 10000;
    public final SparseArray<CallbackRecord> mCallbacks;
    private final Context mContext;
    private final DisplayViewport mDefaultViewport;
    private final DisplayAdapterListener mDisplayAdapterListener;
    private final ArrayList<DisplayAdapter> mDisplayAdapters;
    private final ArrayList<DisplayDevice> mDisplayDevices;
    private DisplayPowerController mDisplayPowerController;
    private final CopyOnWriteArrayList<DisplayTransactionListener> mDisplayTransactionListeners;
    private final DisplayViewport mExternalTouchViewport;
    private int mGlobalDisplayBrightness;
    private int mGlobalDisplayState;
    private final DisplayManagerHandler mHandler;
    private InputManagerInternal mInputManagerInternal;
    private final SparseArray<LogicalDisplay> mLogicalDisplays;
    private int mNextNonDefaultDisplayId;
    public boolean mOnlyCore;
    private boolean mPendingTraversal;
    private final PersistentDataStore mPersistentDataStore;
    private IMediaProjectionManager mProjectionService;
    public boolean mSafeMode;
    private final boolean mSingleDisplayDemoMode;
    private final SyncRoot mSyncRoot;
    private final ArrayList<CallbackRecord> mTempCallbacks;
    private final DisplayViewport mTempDefaultViewport;
    private final DisplayInfo mTempDisplayInfo;
    private final ArrayList<Runnable> mTempDisplayStateWorkQueue;
    private final DisplayViewport mTempExternalTouchViewport;
    private final Handler mUiHandler;
    private VirtualDisplayAdapter mVirtualDisplayAdapter;
    private WifiDisplayAdapter mWifiDisplayAdapter;
    private int mWifiDisplayScanRequestCount;
    private WindowManagerInternal mWindowManagerInternal;

    private final class BinderService extends Stub {
        /* synthetic */ BinderService(DisplayManagerService this$0, BinderService binderService) {
            this();
        }

        private BinderService() {
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayInfo -wrap2 = DisplayManagerService.this.getDisplayInfoInternal(displayId, callingUid);
                return -wrap2;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public int[] getDisplayIds() {
            int callingUid = Binder.getCallingUid();
            long token = Binder.clearCallingIdentity();
            try {
                int[] -wrap4 = DisplayManagerService.this.getDisplayIdsInternal(callingUid);
                return -wrap4;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void registerCallback(IDisplayManagerCallback callback) {
            if (callback == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            int callingPid = Binder.getCallingPid();
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.registerCallbackInternal(callback, callingPid);
            } finally {
                Binder.restoreCallingIdentity(token);
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
            if (address == null) {
                throw new IllegalArgumentException("address must not be null");
            }
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to connect to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.connectWifiDisplayInternal(address);
            } finally {
                Binder.restoreCallingIdentity(token);
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
            if (address == null) {
                throw new IllegalArgumentException("address must not be null");
            }
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to rename to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.renameWifiDisplayInternal(address, alias);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void forgetWifiDisplay(String address) {
            if (address == null) {
                throw new IllegalArgumentException("address must not be null");
            }
            DisplayManagerService.this.mContext.enforceCallingOrSelfPermission("android.permission.CONFIGURE_WIFI_DISPLAY", "Permission required to forget to a wifi display");
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.forgetWifiDisplayInternal(address);
            } finally {
                Binder.restoreCallingIdentity(token);
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
                WifiDisplayStatus -wrap0 = DisplayManagerService.this.getWifiDisplayStatusInternal();
                return -wrap0;
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

        public int createVirtualDisplay(IVirtualDisplayCallback callback, IMediaProjection projection, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags) {
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
                    flags |= 16;
                }
                if ((flags & 8) != 0) {
                    flags &= -17;
                }
                if (projection != null) {
                    try {
                        if (DisplayManagerService.this.getProjectionService().isValidMediaProjection(projection)) {
                            flags = projection.applyVirtualDisplayFlags(flags);
                        } else {
                            throw new SecurityException("Invalid media projection");
                        }
                    } catch (RemoteException e) {
                        throw new SecurityException("unable to validate media projection or flags");
                    }
                }
                if (callingUid != 1000 && (flags & 16) != 0 && !canProjectVideo(projection)) {
                    throw new SecurityException("Requires CAPTURE_VIDEO_OUTPUT or CAPTURE_SECURE_VIDEO_OUTPUT permission, or an appropriate MediaProjection token in order to create a screen sharing virtual display.");
                } else if ((flags & 4) == 0 || canProjectSecureVideo(projection)) {
                    long token = Binder.clearCallingIdentity();
                    try {
                        int -wrap5 = DisplayManagerService.this.createVirtualDisplayInternal(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags);
                        return -wrap5;
                    } finally {
                        Binder.restoreCallingIdentity(token);
                    }
                } else {
                    throw new SecurityException("Requires CAPTURE_SECURE_VIDEO_OUTPUT or an appropriate MediaProjection token to create a secure virtual display.");
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

        protected boolean dynamicallyConfigDisplayLogTag(PrintWriter pw, String[] args) {
            if (args.length < 1) {
                return false;
            }
            if (!"log".equals(args[0])) {
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
            boolean on = LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[2]);
            pw.println("dynamicallyConfigDisplayLogTag, logCategoryTag:" + logCategoryTag + ", on:" + on);
            if ("all".equals(logCategoryTag)) {
                DisplayManagerService.DEBUG = on;
                DisplayPowerController.DEBUG = on;
                DisplayPowerController.DEBUG_PANIC = on;
                AutomaticBrightnessController.DEBUG = on;
                OppoBrightUtils.DEBUG = on;
            } else if ("panic".equals(logCategoryTag)) {
                DisplayPowerController.DEBUG_PANIC = on;
            } else if ("state".equals(logCategoryTag)) {
                DisplayPowerState.DEBUG = on;
            } else {
                pw.println("Invalid log tag argument! Get detail help as bellow:");
                logOutDisplayLogTagHelp(pw);
            }
            return true;
        }

        protected void logOutDisplayLogTagHelp(PrintWriter pw) {
            pw.println("********************** Help begin:**********************");
            pw.println("1 All display log:DEBUG | DisplayPowerController");
            pw.println("cmd: dumpsys display log all 0/1");
            pw.println("----------------------------------");
            pw.println("2 lightWeight power log: DisplayPowerController");
            pw.println("cmd: dumpsys display log panic 0/1");
            pw.println("----------------------------------");
            pw.println("********************** Help end.  **********************");
        }

        protected boolean dynamicallyConfigDisplaySensorTag(PrintWriter pw, String[] args) {
            if (args.length < 1) {
                return false;
            }
            if (!"psensor".equals(args[0])) {
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
            } else if (LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON.equals(args[1])) {
                OppoBrightUtils.DEBUG_PRETEND_PROX_SENSOR_ABSENT = false;
                pw.println("dynamicallyConfigDisplaySensorTag: enable psensor");
            } else {
                pw.println("Invalid psenor tag argument! input adb shell dumpsys display psensor 0");
            }
            return true;
        }

        public void releaseVirtualDisplay(IVirtualDisplayCallback callback) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.releaseVirtualDisplayInternal(callback.asBinder());
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (DisplayManagerService.this.mContext == null || DisplayManagerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump DisplayManager from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            } else if (!dynamicallyConfigDisplayLogTag(pw, args) && !dynamicallyConfigDisplaySensorTag(pw, args)) {
                long token = Binder.clearCallingIdentity();
                try {
                    DisplayManagerService.this.dumpInternal(pw);
                } finally {
                    Binder.restoreCallingIdentity(token);
                }
            }
        }

        private boolean validatePackageName(int uid, String packageName) {
            if (packageName != null) {
                String[] packageNames = DisplayManagerService.this.mContext.getPackageManager().getPackagesForUid(uid);
                if (packageNames != null) {
                    for (String n : packageNames) {
                        if (n.equals(packageName)) {
                            return true;
                        }
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
            if (DisplayManagerService.this.mContext.checkCallingPermission("android.permission.CAPTURE_VIDEO_OUTPUT") == 0) {
                return true;
            }
            return canProjectSecureVideo(projection);
        }

        private boolean canProjectSecureVideo(IMediaProjection projection) {
            boolean z = true;
            if (projection != null) {
                try {
                    if (projection.canProjectSecureVideo()) {
                        return true;
                    }
                } catch (RemoteException e) {
                    Slog.e(DisplayManagerService.TAG, "Unable to query projection service for permissions", e);
                }
            }
            if (DisplayManagerService.this.mContext.checkCallingPermission("android.permission.CAPTURE_SECURE_VIDEO_OUTPUT") != 0) {
                z = false;
            }
            return z;
        }

        public boolean isSinkEnabled() {
            long token = Binder.clearCallingIdentity();
            try {
                boolean -wrap3 = DisplayManagerService.this.isSinkEnabledInternal();
                return -wrap3;
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void enableSink(boolean enable) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.enableSinkInternal(enable);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void waitWifiDisplayConnection(Surface surface) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.waitWifiDisplayConnectionInternal(surface);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void suspendWifiDisplay(boolean suspend, Surface surface) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.suspendWifiDisplayInternal(suspend, surface);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }

        public void sendUibcInputEvent(String input) {
            long token = Binder.clearCallingIdentity();
            try {
                DisplayManagerService.this.sendUibcInputEventInternal(input);
            } finally {
                Binder.restoreCallingIdentity(token);
            }
        }
    }

    private final class CallbackRecord implements DeathRecipient {
        private final IDisplayManagerCallback mCallback;
        public final int mPid;
        public boolean mWifiDisplayScanRequested;

        public CallbackRecord(int pid, IDisplayManagerCallback callback) {
            this.mPid = pid;
            this.mCallback = callback;
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

    private final class DisplayAdapterListener implements Listener {
        /* synthetic */ DisplayAdapterListener(DisplayManagerService this$0, DisplayAdapterListener displayAdapterListener) {
            this();
        }

        private DisplayAdapterListener() {
        }

        public void onDisplayDeviceEvent(DisplayDevice device, int event) {
            switch (event) {
                case 1:
                    DisplayManagerService.this.handleDisplayDeviceAdded(device);
                    return;
                case 2:
                    DisplayManagerService.this.handleDisplayDeviceChanged(device);
                    return;
                case 3:
                    DisplayManagerService.this.handleDisplayDeviceRemoved(device);
                    return;
                default:
                    return;
            }
        }

        public void onTraversalRequested() {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.scheduleTraversalLocked(false);
            }
        }
    }

    private final class DisplayManagerHandler extends Handler {
        public DisplayManagerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    DisplayManagerService.this.registerDefaultDisplayAdapter();
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
                        DisplayManagerService.this.mTempDefaultViewport.copyFrom(DisplayManagerService.this.mDefaultViewport);
                        DisplayManagerService.this.mTempExternalTouchViewport.copyFrom(DisplayManagerService.this.mExternalTouchViewport);
                    }
                    DisplayManagerService.this.mInputManagerInternal.setDisplayViewports(DisplayManagerService.this.mTempDefaultViewport, DisplayManagerService.this.mTempExternalTouchViewport);
                    return;
                default:
                    return;
            }
        }
    }

    private final class LocalService extends DisplayManagerInternal {
        /* synthetic */ LocalService(DisplayManagerService this$0, LocalService localService) {
            this();
        }

        private LocalService() {
        }

        public void initPowerManagement(final DisplayPowerCallbacks callbacks, Handler handler, SensorManager sensorManager) {
            synchronized (DisplayManagerService.this.mSyncRoot) {
                DisplayManagerService.this.mDisplayPowerController = new DisplayPowerController(DisplayManagerService.this.mContext, callbacks, handler, sensorManager, new DisplayBlanker() {
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
            }
        }

        public boolean requestPowerState(DisplayPowerRequest request, boolean waitForNegativeProximity) {
            return DisplayManagerService.this.mDisplayPowerController.requestPowerState(request, waitForNegativeProximity);
        }

        public boolean isProximitySensorAvailable() {
            return DisplayManagerService.this.mDisplayPowerController.isProximitySensorAvailable();
        }

        public void setIPOScreenOnDelay(int msec) {
            DisplayManagerService.this.mDisplayPowerController.setIPOScreenOnDelay(msec);
        }

        public void setUseProximityForceSuspend(boolean enable) {
            DisplayManagerService.this.mDisplayPowerController.setUseProximityForceSuspend(enable);
        }

        public void blockScreenOnByFingerPrint() {
            DisplayManagerService.this.mDisplayPowerController.blockScreenOnByFingerPrint();
        }

        public void unblockScreenOnByFingerPrint(boolean type) {
            DisplayManagerService.this.mDisplayPowerController.unblockScreenOnByFingerPrint(type);
        }

        public boolean isBlockScreenOnByFingerPrint() {
            return DisplayManagerService.this.mDisplayPowerController.isBlockScreenOnByFingerPrint();
        }

        public boolean isBlockDisplayByFingerPrint() {
            return DisplayManagerService.this.mDisplayPowerController.isBlockDisplayByFingerPrint();
        }

        public int getScreenState() {
            return DisplayManagerService.this.mDisplayPowerController.getScreenState();
        }

        public void setOutdoorMode(boolean enable) {
            DisplayManagerService.this.mDisplayPowerController.setOutdoorMode(enable);
        }

        public void updateScreenOnBlockedState(boolean isBlockedScreenOn) {
            DisplayManagerService.this.mDisplayPowerController.updateScreenOnBlockedState(isBlockedScreenOn);
        }

        public DisplayInfo getDisplayInfo(int displayId) {
            return DisplayManagerService.this.getDisplayInfoInternal(displayId, Process.myUid());
        }

        public void registerDisplayTransactionListener(DisplayTransactionListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            DisplayManagerService.this.registerDisplayTransactionListenerInternal(listener);
        }

        public void unregisterDisplayTransactionListener(DisplayTransactionListener listener) {
            if (listener == null) {
                throw new IllegalArgumentException("listener must not be null");
            }
            DisplayManagerService.this.unregisterDisplayTransactionListenerInternal(listener);
        }

        public void setDisplayInfoOverrideFromWindowManager(int displayId, DisplayInfo info) {
            DisplayManagerService.this.setDisplayInfoOverrideFromWindowManagerInternal(displayId, info);
        }

        public void performTraversalInTransactionFromWindowManager() {
            DisplayManagerService.this.performTraversalInTransactionFromWindowManagerInternal();
        }

        public void setDisplayProperties(int displayId, boolean hasContent, float requestedRefreshRate, int requestedMode, boolean inTraversal) {
            DisplayManagerService.this.setDisplayPropertiesInternal(displayId, hasContent, requestedRefreshRate, requestedMode, inTraversal);
        }

        public void setDisplayOffsets(int displayId, int x, int y) {
            DisplayManagerService.this.setDisplayOffsetsInternal(displayId, x, y);
        }
    }

    public static final class SyncRoot {
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.display.DisplayManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.display.DisplayManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.DisplayManagerService.<clinit>():void");
    }

    public DisplayManagerService(Context context) {
        super(context);
        this.mSyncRoot = new SyncRoot();
        this.mCallbacks = new SparseArray();
        this.mDisplayAdapters = new ArrayList();
        this.mDisplayDevices = new ArrayList();
        this.mLogicalDisplays = new SparseArray();
        this.mNextNonDefaultDisplayId = 1;
        this.mDisplayTransactionListeners = new CopyOnWriteArrayList();
        this.mGlobalDisplayState = 2;
        this.mGlobalDisplayBrightness = -1;
        this.mDefaultViewport = new DisplayViewport();
        this.mExternalTouchViewport = new DisplayViewport();
        this.mPersistentDataStore = new PersistentDataStore();
        this.mTempCallbacks = new ArrayList();
        this.mTempDisplayInfo = new DisplayInfo();
        this.mTempDefaultViewport = new DisplayViewport();
        this.mTempExternalTouchViewport = new DisplayViewport();
        this.mTempDisplayStateWorkQueue = new ArrayList();
        this.mContext = context;
        this.mHandler = new DisplayManagerHandler(DisplayThread.get().getLooper());
        this.mUiHandler = UiThread.getHandler();
        this.mDisplayAdapterListener = new DisplayAdapterListener(this, null);
        this.mSingleDisplayDemoMode = SystemProperties.getBoolean("persist.demo.singledisplay", false);
        PowerManager pm = (PowerManager) this.mContext.getSystemService("power");
        this.mGlobalDisplayBrightness = OppoBrightUtils.getInstance().getBootupBrightness();
    }

    public void onStart() {
        this.mPersistentDataStore.loadIfNeeded();
        this.mHandler.sendEmptyMessage(1);
        publishBinderService("display", new BinderService(this, null), true);
        publishLocalService(DisplayManagerInternal.class, new LocalService(this, null));
        publishLocalService(DisplayTransformManager.class, new DisplayTransformManager());
    }

    public void onBootPhase(int phase) {
        if (phase == 100) {
            synchronized (this.mSyncRoot) {
                long timeout = SystemClock.uptimeMillis() + 10000;
                while (this.mLogicalDisplays.get(0) == null) {
                    long delay = timeout - SystemClock.uptimeMillis();
                    if (delay <= 0) {
                        throw new RuntimeException("Timeout waiting for default display to be initialized.");
                    }
                    if (DEBUG) {
                        Slog.d(TAG, "waitForDefaultDisplay: waiting, timeout=" + delay);
                    }
                    try {
                        this.mSyncRoot.wait(delay);
                    } catch (InterruptedException e) {
                    }
                }
            }
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
        }
        this.mHandler.sendEmptyMessage(2);
    }

    private void registerDisplayTransactionListenerInternal(DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.add(listener);
    }

    private void unregisterDisplayTransactionListenerInternal(DisplayTransactionListener listener) {
        this.mDisplayTransactionListeners.remove(listener);
    }

    private void setDisplayInfoOverrideFromWindowManagerInternal(int displayId, DisplayInfo info) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display != null && display.setDisplayInfoOverrideFromWindowManagerLocked(info)) {
                sendDisplayEventLocked(displayId, 2);
                scheduleTraversalLocked(false);
            }
        }
    }

    /* JADX WARNING: Missing block: B:11:0x0010, code:
            r1 = r4.mDisplayTransactionListeners.iterator();
     */
    /* JADX WARNING: Missing block: B:13:0x001a, code:
            if (r1.hasNext() == false) goto L_0x0029;
     */
    /* JADX WARNING: Missing block: B:14:0x001c, code:
            ((android.hardware.display.DisplayManagerInternal.DisplayTransactionListener) r1.next()).onDisplayTransaction();
     */
    /* JADX WARNING: Missing block: B:18:0x0029, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void performTraversalInTransactionFromWindowManagerInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mPendingTraversal) {
                this.mPendingTraversal = false;
                performTraversalInTransactionLocked();
            }
        }
    }

    /* JADX WARNING: Missing block: B:29:0x0062, code:
            r0 = 0;
     */
    /* JADX WARNING: Missing block: B:31:0x0069, code:
            if (r0 >= r6.mTempDisplayStateWorkQueue.size()) goto L_0x0086;
     */
    /* JADX WARNING: Missing block: B:32:0x006b, code:
            ((java.lang.Runnable) r6.mTempDisplayStateWorkQueue.get(r0)).run();
            r0 = r0 + 1;
     */
    /* JADX WARNING: Missing block: B:41:?, code:
            android.os.Trace.traceEnd(524288);
     */
    /* JADX WARNING: Missing block: B:43:?, code:
            r6.mTempDisplayStateWorkQueue.clear();
     */
    /* JADX WARNING: Missing block: B:45:0x0092, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void requestGlobalDisplayStateInternal(int state, int brightness) {
        if (state == 0) {
            state = 2;
        }
        if (state == 1) {
            brightness = 0;
        } else if (brightness < 0) {
            brightness = -1;
        } else if (brightness > PowerManager.BRIGHTNESS_MULTIBITS_ON) {
            brightness = PowerManager.BRIGHTNESS_MULTIBITS_ON;
        }
        synchronized (this.mTempDisplayStateWorkQueue) {
            try {
                synchronized (this.mSyncRoot) {
                    if (this.mGlobalDisplayState != state || this.mGlobalDisplayBrightness != brightness) {
                        Trace.traceBegin(524288, "requestGlobalDisplayState(" + Display.stateToString(state) + ", brightness=" + brightness + ")");
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

    /* JADX WARNING: Missing block: B:10:0x0023, code:
            return null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private DisplayInfo getDisplayInfoInternal(int displayId, int callingUid) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display != null) {
                DisplayInfo displayInfo = display.getDisplayInfoLocked();
                DisplayInfo info = new DisplayInfo();
                info.copyFrom(displayInfo);
                if (info.hasAccess(callingUid)) {
                    return info;
                }
            }
        }
    }

    private int[] getDisplayIdsInternal(int callingUid) {
        int[] displayIds;
        synchronized (this.mSyncRoot) {
            int count = this.mLogicalDisplays.size();
            displayIds = new int[count];
            int i = 0;
            int n = 0;
            while (i < count) {
                int n2;
                if (((LogicalDisplay) this.mLogicalDisplays.valueAt(i)).getDisplayInfoLocked().hasAccess(callingUid)) {
                    n2 = n + 1;
                    displayIds[n] = this.mLogicalDisplays.keyAt(i);
                } else {
                    n2 = n;
                }
                i++;
                n = n2;
            }
            if (n != count) {
                displayIds = Arrays.copyOfRange(displayIds, 0, n);
            }
        }
        return displayIds;
    }

    private void registerCallbackInternal(IDisplayManagerCallback callback, int callingPid) {
        synchronized (this.mSyncRoot) {
            if (this.mCallbacks.get(callingPid) != null) {
                throw new SecurityException("The calling process has already registered an IDisplayManagerCallback.");
            }
            CallbackRecord record = new CallbackRecord(callingPid, callback);
            try {
                callback.asBinder().linkToDeath(record, 0);
                this.mCallbacks.put(callingPid, record);
            } catch (RemoteException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    private void onCallbackDied(CallbackRecord record) {
        synchronized (this.mSyncRoot) {
            this.mCallbacks.remove(record.mPid);
            stopWifiDisplayScanLocked(record);
        }
    }

    private void startWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = (CallbackRecord) this.mCallbacks.get(callingPid);
            if (record == null) {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
            startWifiDisplayScanLocked(record);
        }
    }

    private void startWifiDisplayScanLocked(CallbackRecord record) {
        if (!record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = true;
            int i = this.mWifiDisplayScanRequestCount;
            this.mWifiDisplayScanRequestCount = i + 1;
            if (i == 0 && this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestStartScanLocked();
            }
        }
    }

    private void stopWifiDisplayScanInternal(int callingPid) {
        synchronized (this.mSyncRoot) {
            CallbackRecord record = (CallbackRecord) this.mCallbacks.get(callingPid);
            if (record == null) {
                throw new IllegalStateException("The calling process has not registered an IDisplayManagerCallback.");
            }
            stopWifiDisplayScanLocked(record);
        }
    }

    private void stopWifiDisplayScanLocked(CallbackRecord record) {
        if (record.mWifiDisplayScanRequested) {
            record.mWifiDisplayScanRequested = false;
            int i = this.mWifiDisplayScanRequestCount - 1;
            this.mWifiDisplayScanRequestCount = i;
            if (i == 0) {
                if (this.mWifiDisplayAdapter != null) {
                    this.mWifiDisplayAdapter.requestStopScanLocked();
                }
            } else if (this.mWifiDisplayScanRequestCount < 0) {
                Slog.wtf(TAG, "mWifiDisplayScanRequestCount became negative: " + this.mWifiDisplayScanRequestCount);
                this.mWifiDisplayScanRequestCount = 0;
            }
        }
    }

    private void connectWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestConnectLocked(address);
            }
        }
    }

    private void pauseWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestPauseLocked();
            }
        }
    }

    private void resumeWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestResumeLocked();
            }
        }
    }

    private void disconnectWifiDisplayInternal() {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestDisconnectLocked();
            }
        }
    }

    private void renameWifiDisplayInternal(String address, String alias) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestRenameLocked(address, alias);
            }
        }
    }

    private void forgetWifiDisplayInternal(String address) {
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                this.mWifiDisplayAdapter.requestForgetLocked(address);
            }
        }
    }

    private WifiDisplayStatus getWifiDisplayStatusInternal() {
        synchronized (this.mSyncRoot) {
            WifiDisplayStatus wifiDisplayStatusLocked;
            if (this.mWifiDisplayAdapter != null) {
                wifiDisplayStatusLocked = this.mWifiDisplayAdapter.getWifiDisplayStatusLocked();
                return wifiDisplayStatusLocked;
            }
            wifiDisplayStatusLocked = new WifiDisplayStatus();
            return wifiDisplayStatusLocked;
        }
    }

    private void requestColorModeInternal(int displayId, int colorMode) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (!(display == null || display.getRequestedColorModeLocked() == colorMode)) {
                display.setRequestedColorModeLocked(colorMode);
                scheduleTraversalLocked(false);
            }
        }
    }

    private int createVirtualDisplayInternal(IVirtualDisplayCallback callback, IMediaProjection projection, int callingUid, String packageName, String name, int width, int height, int densityDpi, Surface surface, int flags) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                Slog.w(TAG, "Rejecting request to create private virtual display because the virtual display adapter is not available.");
                return -1;
            }
            DisplayDevice device = this.mVirtualDisplayAdapter.createVirtualDisplayLocked(callback, projection, callingUid, packageName, name, width, height, densityDpi, surface, flags);
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

    private void resizeVirtualDisplayInternal(IBinder appToken, int width, int height, int densityDpi) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                return;
            }
            this.mVirtualDisplayAdapter.resizeVirtualDisplayLocked(appToken, width, height, densityDpi);
        }
    }

    private void setVirtualDisplaySurfaceInternal(IBinder appToken, Surface surface) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                return;
            }
            this.mVirtualDisplayAdapter.setVirtualDisplaySurfaceLocked(appToken, surface);
        }
    }

    /* JADX WARNING: Missing block: B:12:0x0015, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void releaseVirtualDisplayInternal(IBinder appToken) {
        synchronized (this.mSyncRoot) {
            if (this.mVirtualDisplayAdapter == null) {
                return;
            }
            DisplayDevice device = this.mVirtualDisplayAdapter.releaseVirtualDisplayLocked(appToken);
            if (device != null) {
                handleDisplayDeviceRemovedLocked(device);
            }
        }
    }

    private void registerDefaultDisplayAdapter() {
        synchronized (this.mSyncRoot) {
            registerDisplayAdapterLocked(new LocalDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener));
        }
    }

    private void registerAdditionalDisplayAdapters() {
        synchronized (this.mSyncRoot) {
            if (shouldRegisterNonEssentialDisplayAdaptersLocked()) {
                registerOverlayDisplayAdapterLocked();
                registerWifiDisplayAdapterLocked();
                registerVirtualDisplayAdapterLocked();
            }
        }
    }

    private void registerOverlayDisplayAdapterLocked() {
        registerDisplayAdapterLocked(new OverlayDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mUiHandler));
    }

    private void registerWifiDisplayAdapterLocked() {
        if (this.mContext.getResources().getBoolean(17956984) || SystemProperties.getInt(FORCE_WIFI_DISPLAY_ENABLE, -1) == 1 || SystemProperties.get("ro.mtk_wfd_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            this.mWifiDisplayAdapter = new WifiDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener, this.mPersistentDataStore);
            registerDisplayAdapterLocked(this.mWifiDisplayAdapter);
        }
    }

    private void registerVirtualDisplayAdapterLocked() {
        this.mVirtualDisplayAdapter = new VirtualDisplayAdapter(this.mSyncRoot, this.mContext, this.mHandler, this.mDisplayAdapterListener);
        registerDisplayAdapterLocked(this.mVirtualDisplayAdapter);
    }

    private boolean shouldRegisterNonEssentialDisplayAdaptersLocked() {
        return (this.mSafeMode || this.mOnlyCore) ? false : true;
    }

    private void registerDisplayAdapterLocked(DisplayAdapter adapter) {
        this.mDisplayAdapters.add(adapter);
        adapter.registerLocked();
    }

    private void handleDisplayDeviceAdded(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            handleDisplayDeviceAddedLocked(device);
        }
    }

    private void handleDisplayDeviceAddedLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (this.mDisplayDevices.contains(device)) {
            Slog.w(TAG, "Attempted to add already added display device: " + info);
            return;
        }
        Slog.i(TAG, "Display device added: " + info);
        if (info != null) {
            OppoBPMHelper.addPkgToDisplayDeviceList(info.ownerPackageName);
            OppoGameSpaceManagerUtils.getInstance().addPkgToDisplayDeviceList(info.ownerPackageName);
        }
        device.mDebugLastLoggedDeviceInfo = info;
        this.mDisplayDevices.add(device);
        LogicalDisplay display = addLogicalDisplayLocked(device);
        Runnable work = updateDisplayStateLocked(device);
        if (work != null) {
            work.run();
        }
        if (display != null && display.getPrimaryDisplayDeviceLocked() == device) {
            display.setRequestedColorModeLocked(this.mPersistentDataStore.getColorMode(device));
        }
        scheduleTraversalLocked(false);
    }

    /* JADX WARNING: Missing block: B:21:0x0081, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleDisplayDeviceChanged(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
            if (this.mDisplayDevices.contains(device)) {
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
            } else {
                Slog.w(TAG, "Attempted to change non-existent display device: " + info);
            }
        }
    }

    private void handleDisplayDeviceRemoved(DisplayDevice device) {
        synchronized (this.mSyncRoot) {
            handleDisplayDeviceRemovedLocked(device);
        }
    }

    private void handleDisplayDeviceRemovedLocked(DisplayDevice device) {
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        if (this.mDisplayDevices.remove(device)) {
            Slog.i(TAG, "Display device removed: " + info);
            if (info != null) {
                OppoBPMHelper.removePkgFromDisplayDeviceList(info.ownerPackageName);
                OppoGameSpaceManagerUtils.getInstance().removePkgFromDisplayDeviceList(info.ownerPackageName);
            }
            device.mDebugLastLoggedDeviceInfo = info;
            updateLogicalDisplaysLocked();
            scheduleTraversalLocked(false);
            return;
        }
        Slog.w(TAG, "Attempted to remove non-existent display device: " + info);
    }

    private void applyGlobalDisplayStateLocked(List<Runnable> workQueue) {
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i++) {
            DisplayDevice device = (DisplayDevice) this.mDisplayDevices.get(i);
            if (i == 0 && DEBUG) {
                Slog.d(TAG, "Update global display state (" + Display.stateToString(this.mGlobalDisplayState) + ", " + this.mGlobalDisplayBrightness + ") for " + device.getDisplayDeviceInfoLocked());
            }
            Runnable runnable = updateDisplayStateLocked(device);
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
            if (display.isValidLocked()) {
                this.mLogicalDisplays.put(displayId, display);
                if (isDefault) {
                    this.mSyncRoot.notifyAll();
                }
                sendDisplayEventLocked(displayId, 1);
                return display;
            }
            Slog.w(TAG, "Ignoring display device because the logical display created from it was not considered valid: " + deviceInfo);
            return null;
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

    private boolean updateLogicalDisplaysLocked() {
        boolean changed = false;
        int i = this.mLogicalDisplays.size();
        while (true) {
            int i2 = i;
            i = i2 - 1;
            if (i2 <= 0) {
                return changed;
            }
            int displayId = this.mLogicalDisplays.keyAt(i);
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.valueAt(i);
            this.mTempDisplayInfo.copyFrom(display.getDisplayInfoLocked());
            display.updateLocked(this.mDisplayDevices);
            if (!display.isValidLocked()) {
                this.mLogicalDisplays.removeAt(i);
                sendDisplayEventLocked(displayId, 3);
                changed = true;
            } else if (!this.mTempDisplayInfo.equals(display.getDisplayInfoLocked())) {
                sendDisplayEventLocked(displayId, 2);
                changed = true;
            }
        }
    }

    private void performTraversalInTransactionLocked() {
        clearViewportsLocked();
        int count = this.mDisplayDevices.size();
        for (int i = 0; i < count; i++) {
            DisplayDevice device = (DisplayDevice) this.mDisplayDevices.get(i);
            configureDisplayInTransactionLocked(device);
            device.performTraversalInTransactionLocked();
        }
        if (this.mInputManagerInternal != null) {
            this.mHandler.sendEmptyMessage(5);
        }
    }

    /* JADX WARNING: Missing block: B:25:0x009b, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setDisplayPropertiesInternal(int displayId, boolean hasContent, float requestedRefreshRate, int requestedModeId, boolean inTraversal) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display == null) {
                return;
            }
            if (display.hasContentLocked() != hasContent) {
                if (DEBUG) {
                    Slog.d(TAG, "Display " + displayId + " hasContent flag changed: " + "hasContent=" + hasContent + ", inTraversal=" + inTraversal);
                }
                display.setHasContentLocked(hasContent);
                scheduleTraversalLocked(inTraversal);
            }
            if (requestedModeId == 0 && requestedRefreshRate != OppoBrightUtils.MIN_LUX_LIMITI) {
                requestedModeId = display.getDisplayInfoLocked().findDefaultModeByRefreshRate(requestedRefreshRate);
            }
            if (display.getRequestedModeIdLocked() != requestedModeId) {
                if (DEBUG) {
                    Slog.d(TAG, "Display " + displayId + " switching to mode " + requestedModeId);
                }
                display.setRequestedModeIdLocked(requestedModeId);
                scheduleTraversalLocked(inTraversal);
            }
        }
    }

    /* JADX WARNING: Missing block: B:17:0x005e, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void setDisplayOffsetsInternal(int displayId, int x, int y) {
        synchronized (this.mSyncRoot) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.get(displayId);
            if (display == null) {
            } else if (!(display.getDisplayOffsetXLocked() == x && display.getDisplayOffsetYLocked() == y)) {
                if (DEBUG) {
                    Slog.d(TAG, "Display " + displayId + " burn-in offset set to (" + x + ", " + y + ")");
                }
                display.setDisplayOffsetsLocked(x, y);
                scheduleTraversalLocked(false);
            }
        }
    }

    private void clearViewportsLocked() {
        this.mDefaultViewport.valid = false;
        this.mExternalTouchViewport.valid = false;
    }

    private void configureDisplayInTransactionLocked(DisplayDevice device) {
        boolean z = true;
        DisplayDeviceInfo info = device.getDisplayDeviceInfoLocked();
        boolean ownContent = (info.flags & 128) != 0;
        LogicalDisplay display = findLogicalDisplayForDeviceLocked(device);
        if (!ownContent) {
            if (!(display == null || display.hasContentLocked())) {
                display = null;
            }
            if (display == null) {
                display = (LogicalDisplay) this.mLogicalDisplays.get(0);
            }
        }
        if (display == null) {
            Slog.w(TAG, "Missing logical display to use for physical display device: " + device.getDisplayDeviceInfoLocked());
            return;
        }
        if (info.state != 1) {
            z = false;
        }
        display.configureDisplayInTransactionLocked(device, z);
        if (!(this.mDefaultViewport.valid || (info.flags & 1) == 0)) {
            setViewportLocked(this.mDefaultViewport, display, device);
        }
        if (!this.mExternalTouchViewport.valid && info.touch == 2) {
            setViewportLocked(this.mExternalTouchViewport, display, device);
        }
    }

    private static void setViewportLocked(DisplayViewport viewport, LogicalDisplay display, DisplayDevice device) {
        viewport.valid = true;
        viewport.displayId = display.getDisplayIdLocked();
        device.populateViewportLocked(viewport);
    }

    private LogicalDisplay findLogicalDisplayForDeviceLocked(DisplayDevice device) {
        int count = this.mLogicalDisplays.size();
        for (int i = 0; i < count; i++) {
            LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.valueAt(i);
            if (display.getPrimaryDisplayDeviceLocked() == device) {
                return display;
            }
        }
        return null;
    }

    private void sendDisplayEventLocked(int displayId, int event) {
        this.mHandler.sendMessage(this.mHandler.obtainMessage(3, displayId, event));
    }

    private void scheduleTraversalLocked(boolean inTraversal) {
        if (!this.mPendingTraversal && this.mWindowManagerInternal != null) {
            this.mPendingTraversal = true;
            if (!inTraversal) {
                this.mHandler.sendEmptyMessage(4);
            }
        }
    }

    private void deliverDisplayEvent(int displayId, int event) {
        int count;
        int i;
        if (DEBUG) {
            Slog.d(TAG, "Delivering display event: displayId=" + displayId + ", event=" + event);
        }
        synchronized (this.mSyncRoot) {
            count = this.mCallbacks.size();
            this.mTempCallbacks.clear();
            for (i = 0; i < count; i++) {
                this.mTempCallbacks.add((CallbackRecord) this.mCallbacks.valueAt(i));
            }
        }
        for (i = 0; i < count; i++) {
            ((CallbackRecord) this.mTempCallbacks.get(i)).notifyDisplayEventAsync(displayId, event);
        }
        this.mTempCallbacks.clear();
    }

    private IMediaProjectionManager getProjectionService() {
        if (this.mProjectionService == null) {
            this.mProjectionService = IMediaProjectionManager.Stub.asInterface(ServiceManager.getService("media_projection"));
        }
        return this.mProjectionService;
    }

    private void dumpInternal(PrintWriter pw) {
        pw.println("DISPLAY MANAGER (dumpsys display)");
        synchronized (this.mSyncRoot) {
            int i;
            pw.println("  mOnlyCode=" + this.mOnlyCore);
            pw.println("  mSafeMode=" + this.mSafeMode);
            pw.println("  mPendingTraversal=" + this.mPendingTraversal);
            pw.println("  mGlobalDisplayState=" + Display.stateToString(this.mGlobalDisplayState));
            pw.println("  mNextNonDefaultDisplayId=" + this.mNextNonDefaultDisplayId);
            pw.println("  mDefaultViewport=" + this.mDefaultViewport);
            pw.println("  mExternalTouchViewport=" + this.mExternalTouchViewport);
            pw.println("  mSingleDisplayDemoMode=" + this.mSingleDisplayDemoMode);
            pw.println("  mWifiDisplayScanRequestCount=" + this.mWifiDisplayScanRequestCount);
            IndentingPrintWriter ipw = new IndentingPrintWriter(pw, "    ");
            ipw.increaseIndent();
            pw.println();
            pw.println("Display Adapters: size=" + this.mDisplayAdapters.size());
            for (DisplayAdapter adapter : this.mDisplayAdapters) {
                pw.println("  " + adapter.getName());
                adapter.dumpLocked(ipw);
            }
            pw.println();
            pw.println("Display Devices: size=" + this.mDisplayDevices.size());
            for (DisplayDevice device : this.mDisplayDevices) {
                pw.println("  " + device.getDisplayDeviceInfoLocked());
                device.dumpLocked(ipw);
            }
            int logicalDisplayCount = this.mLogicalDisplays.size();
            pw.println();
            pw.println("Logical Displays: size=" + logicalDisplayCount);
            for (i = 0; i < logicalDisplayCount; i++) {
                LogicalDisplay display = (LogicalDisplay) this.mLogicalDisplays.valueAt(i);
                pw.println("  Display " + this.mLogicalDisplays.keyAt(i) + ":");
                display.dumpLocked(ipw);
            }
            int callbackCount = this.mCallbacks.size();
            pw.println();
            pw.println("Callbacks: size=" + callbackCount);
            for (i = 0; i < callbackCount; i++) {
                CallbackRecord callback = (CallbackRecord) this.mCallbacks.valueAt(i);
                pw.println("  " + i + ": mPid=" + callback.mPid + ", mWifiDisplayScanRequested=" + callback.mWifiDisplayScanRequested);
            }
            if (this.mDisplayPowerController != null) {
                this.mDisplayPowerController.dump(pw);
            }
            pw.println();
            this.mPersistentDataStore.dump(pw);
        }
    }

    private boolean isSinkEnabledInternal() {
        if (!SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            return false;
        }
        boolean enabled = false;
        synchronized (this.mSyncRoot) {
            if (this.mWifiDisplayAdapter != null) {
                enabled = this.mWifiDisplayAdapter.getIfSinkEnabledLocked();
            }
        }
        return enabled;
    }

    private void enableSinkInternal(boolean enable) {
        if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            synchronized (this.mSyncRoot) {
                if (this.mWifiDisplayAdapter != null) {
                    this.mWifiDisplayAdapter.requestEnableSinkLocked(enable);
                }
            }
        }
    }

    private void waitWifiDisplayConnectionInternal(Surface surface) {
        if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            synchronized (this.mSyncRoot) {
                if (this.mWifiDisplayAdapter != null) {
                    this.mWifiDisplayAdapter.requestWaitConnectionLocked(surface);
                }
            }
        }
    }

    private void suspendWifiDisplayInternal(boolean suspend, Surface surface) {
        if (SystemProperties.get("ro.mtk_wfd_sink_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            synchronized (this.mSyncRoot) {
                if (this.mWifiDisplayAdapter != null) {
                    this.mWifiDisplayAdapter.requestSuspendDisplayLocked(suspend, surface);
                }
            }
        }
    }

    private void sendUibcInputEventInternal(String input) {
        if (SystemProperties.get("ro.mtk_wfd_sink_uibc_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            synchronized (this.mSyncRoot) {
                if (this.mWifiDisplayAdapter != null) {
                    this.mWifiDisplayAdapter.sendUibcInputEventLocked(input);
                }
            }
        }
    }
}
