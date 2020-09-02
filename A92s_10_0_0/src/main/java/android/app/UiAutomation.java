package android.app;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.accessibilityservice.IAccessibilityServiceConnection;
import android.annotation.UnsupportedAppUsage;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.hardware.display.DisplayManagerGlobal;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.Log;
import android.view.Display;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.WindowAnimationFrameStats;
import android.view.WindowContentFrameStats;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import com.android.internal.util.function.pooled.PooledLambda;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import libcore.io.IoUtils;

public final class UiAutomation {
    private static final int CONNECTION_ID_UNDEFINED = -1;
    private static final long CONNECT_TIMEOUT_MILLIS = 5000;
    private static final boolean DEBUG = false;
    public static final int FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES = 1;
    private static final String LOG_TAG = UiAutomation.class.getSimpleName();
    public static final int ROTATION_FREEZE_0 = 0;
    public static final int ROTATION_FREEZE_180 = 2;
    public static final int ROTATION_FREEZE_270 = 3;
    public static final int ROTATION_FREEZE_90 = 1;
    public static final int ROTATION_FREEZE_CURRENT = -1;
    public static final int ROTATION_UNFREEZE = -2;
    private IAccessibilityServiceClient mClient;
    /* access modifiers changed from: private */
    public int mConnectionId = -1;
    /* access modifiers changed from: private */
    public final ArrayList<AccessibilityEvent> mEventQueue = new ArrayList<>();
    private int mFlags;
    private boolean mIsConnecting;
    private boolean mIsDestroyed;
    /* access modifiers changed from: private */
    public long mLastEventTimeMillis;
    /* access modifiers changed from: private */
    public final Handler mLocalCallbackHandler;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();
    /* access modifiers changed from: private */
    public OnAccessibilityEventListener mOnAccessibilityEventListener;
    private HandlerThread mRemoteCallbackThread;
    private final IUiAutomationConnection mUiAutomationConnection;
    /* access modifiers changed from: private */
    public boolean mWaitingForEventDelivery;

    public interface AccessibilityEventFilter {
        boolean accept(AccessibilityEvent accessibilityEvent);
    }

    public interface OnAccessibilityEventListener {
        void onAccessibilityEvent(AccessibilityEvent accessibilityEvent);
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public UiAutomation(Looper looper, IUiAutomationConnection connection) {
        if (looper == null) {
            throw new IllegalArgumentException("Looper cannot be null!");
        } else if (connection != null) {
            this.mLocalCallbackHandler = new Handler(looper);
            this.mUiAutomationConnection = connection;
        } else {
            throw new IllegalArgumentException("Connection cannot be null!");
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void connect() {
        connect(0);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:?, code lost:
        r10.mUiAutomationConnection.connect(r10.mClient, r11);
        r10.mFlags = r11;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0034, code lost:
        r0 = r10.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0037, code lost:
        monitor-enter(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:?, code lost:
        r1 = android.os.SystemClock.uptimeMillis();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:0x0041, code lost:
        if (isConnectedLocked() == false) goto L_0x0049;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        monitor-exit(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0048, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0049, code lost:
        r6 = 5000 - (android.os.SystemClock.uptimeMillis() - r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x0055, code lost:
        if (r6 <= 0) goto L_0x005f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r10.mLock.wait(r6);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0066, code lost:
        throw new java.lang.RuntimeException("Error while connecting UiAutomation");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0067, code lost:
        r4 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0068, code lost:
        r10.mIsConnecting = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x006a, code lost:
        throw r4;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x006e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0076, code lost:
        throw new java.lang.RuntimeException("Error while connecting UiAutomation", r0);
     */
    public void connect(int flags) {
        synchronized (this.mLock) {
            throwIfConnectedLocked();
            if (!this.mIsConnecting) {
                this.mIsConnecting = true;
                this.mRemoteCallbackThread = new HandlerThread("UiAutomation");
                this.mRemoteCallbackThread.start();
                this.mClient = new IAccessibilityServiceClientImpl(this.mRemoteCallbackThread.getLooper());
            }
        }
    }

    public int getFlags() {
        return this.mFlags;
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public void disconnect() {
        synchronized (this.mLock) {
            if (!this.mIsConnecting) {
                throwIfNotConnectedLocked();
                this.mConnectionId = -1;
            } else {
                throw new IllegalStateException("Cannot call disconnect() while connecting!");
            }
        }
        try {
            this.mUiAutomationConnection.disconnect();
            this.mRemoteCallbackThread.quit();
            this.mRemoteCallbackThread = null;
        } catch (RemoteException re) {
            throw new RuntimeException("Error while disconnecting UiAutomation", re);
        } catch (Throwable th) {
            this.mRemoteCallbackThread.quit();
            this.mRemoteCallbackThread = null;
            throw th;
        }
    }

    public int getConnectionId() {
        int i;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            i = this.mConnectionId;
        }
        return i;
    }

    public boolean isDestroyed() {
        return this.mIsDestroyed;
    }

    public void setOnAccessibilityEventListener(OnAccessibilityEventListener listener) {
        synchronized (this.mLock) {
            this.mOnAccessibilityEventListener = listener;
        }
    }

    public void destroy() {
        disconnect();
        this.mIsDestroyed = true;
    }

    public void adoptShellPermissionIdentity() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.adoptShellPermissionIdentity(Process.myUid(), null);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing adopting shell permission identity!", re);
        }
    }

    public void adoptShellPermissionIdentity(String... permissions) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.adoptShellPermissionIdentity(Process.myUid(), permissions);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing adopting shell permission identity!", re);
        }
    }

    public void dropShellPermissionIdentity() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.dropShellPermissionIdentity();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing dropping shell permission identity!", re);
        }
    }

    public final boolean performGlobalAction(int action) {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            AccessibilityInteractionClient.getInstance();
            connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        }
        if (connection == null) {
            return false;
        }
        try {
            return connection.performGlobalAction(action);
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling performGlobalAction", re);
            return false;
        }
    }

    public AccessibilityNodeInfo findFocus(int focus) {
        return AccessibilityInteractionClient.getInstance().findFocus(this.mConnectionId, -2, AccessibilityNodeInfo.ROOT_NODE_ID, focus);
    }

    public final AccessibilityServiceInfo getServiceInfo() {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            AccessibilityInteractionClient.getInstance();
            connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        }
        if (connection == null) {
            return null;
        }
        try {
            return connection.getServiceInfo();
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while getting AccessibilityServiceInfo", re);
            return null;
        }
    }

    public final void setServiceInfo(AccessibilityServiceInfo info) {
        IAccessibilityServiceConnection connection;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            AccessibilityInteractionClient.getInstance().clearCache();
            AccessibilityInteractionClient.getInstance();
            connection = AccessibilityInteractionClient.getConnection(this.mConnectionId);
        }
        if (connection != null) {
            try {
                connection.setServiceInfo(info);
            } catch (RemoteException re) {
                Log.w(LOG_TAG, "Error while setting AccessibilityServiceInfo", re);
            }
        }
    }

    public List<AccessibilityWindowInfo> getWindows() {
        int connectionId;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connectionId = this.mConnectionId;
        }
        return AccessibilityInteractionClient.getInstance().getWindows(connectionId);
    }

    public AccessibilityNodeInfo getRootInActiveWindow() {
        int connectionId;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            connectionId = this.mConnectionId;
        }
        return AccessibilityInteractionClient.getInstance().getRootInActiveWindow(connectionId);
    }

    public boolean injectInputEvent(InputEvent event, boolean sync) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.injectInputEvent(event, sync);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while injecting input event!", re);
            return false;
        }
    }

    public void syncInputTransactions() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.syncInputTransactions();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while syncing input transactions!", re);
        }
    }

    public boolean setRotation(int rotation) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        if (rotation == -2 || rotation == -1 || rotation == 0 || rotation == 1 || rotation == 2 || rotation == 3) {
            try {
                this.mUiAutomationConnection.setRotation(rotation);
                return true;
            } catch (RemoteException re) {
                Log.e(LOG_TAG, "Error while setting rotation!", re);
                return false;
            }
        } else {
            throw new IllegalArgumentException("Invalid rotation.");
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v10, resolved type: java.util.ArrayList} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v1 */
    /* JADX WARN: Type inference failed for: r4v8, types: [boolean, int] */
    /* JADX WARN: Type inference failed for: r4v10 */
    /* JADX WARNING: Code restructure failed: missing block: B:68:0x00de, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:90:0x0111, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x00eb A[LOOP:4: B:73:0x00e9->B:74:0x00eb, LOOP_END] */
    /* JADX WARNING: Removed duplicated region for block: B:77:0x00fa  */
    public AccessibilityEvent executeAndWaitForEvent(Runnable command, AccessibilityEventFilter filter, long timeoutMillis) throws TimeoutException {
        int size;
        int i;
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            this.mEventQueue.clear();
            this.mWaitingForEventDelivery = true;
        }
        long executionStartTimeMillis = SystemClock.uptimeMillis();
        command.run();
        List<AccessibilityEvent> receivedEvents = new ArrayList<>();
        ? r4 = 0;
        try {
            long startTimeMillis = SystemClock.uptimeMillis();
            while (true) {
                List<AccessibilityEvent> localEvents = new ArrayList<>();
                synchronized (this.mLock) {
                    localEvents.addAll(this.mEventQueue);
                    this.mEventQueue.clear();
                }
                while (!localEvents.isEmpty()) {
                    try {
                        AccessibilityEvent event = localEvents.remove((int) r4);
                        if (event.getEventTime() >= executionStartTimeMillis) {
                            try {
                                if (filter.accept(event)) {
                                    int size2 = receivedEvents.size();
                                    for (int i2 = 0; i2 < size2; i2++) {
                                        receivedEvents.get(i2).recycle();
                                    }
                                    synchronized (this.mLock) {
                                        this.mWaitingForEventDelivery = r4;
                                        this.mEventQueue.clear();
                                        this.mLock.notifyAll();
                                    }
                                    return event;
                                }
                                receivedEvents.add(event);
                            } catch (Throwable th) {
                                th = th;
                                size = receivedEvents.size();
                                while (i < size) {
                                }
                                synchronized (this.mLock) {
                                }
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        size = receivedEvents.size();
                        while (i < size) {
                        }
                        synchronized (this.mLock) {
                        }
                    }
                }
                try {
                    long remainingTimeMillis = timeoutMillis - (SystemClock.uptimeMillis() - startTimeMillis);
                    if (remainingTimeMillis > 0) {
                        try {
                            synchronized (this.mLock) {
                                if (this.mEventQueue.isEmpty()) {
                                    try {
                                        this.mLock.wait(remainingTimeMillis);
                                    } catch (InterruptedException e) {
                                    }
                                }
                            }
                            executionStartTimeMillis = executionStartTimeMillis;
                            r4 = 0;
                        } catch (Throwable th3) {
                            th = th3;
                            size = receivedEvents.size();
                            while (i < size) {
                            }
                            synchronized (this.mLock) {
                            }
                        }
                    } else {
                        throw new TimeoutException("Expected event not received within: " + timeoutMillis + " ms among: " + receivedEvents);
                    }
                } catch (Throwable th4) {
                    th = th4;
                    size = receivedEvents.size();
                    while (i < size) {
                    }
                    synchronized (this.mLock) {
                    }
                }
            }
        } catch (Throwable th5) {
            th = th5;
            size = receivedEvents.size();
            for (i = 0; i < size; i++) {
                receivedEvents.get(i).recycle();
            }
            synchronized (this.mLock) {
                this.mWaitingForEventDelivery = false;
                this.mEventQueue.clear();
                this.mLock.notifyAll();
            }
            throw th;
        }
        while (true) {
        }
        while (true) {
        }
    }

    /* JADX INFO: Multiple debug info for r7v4 long: [D('startTimeMillis' long), D('remainingIdleTimeMillis' long)] */
    public void waitForIdle(long idleTimeoutMillis, long globalTimeoutMillis) throws TimeoutException {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
            long startTimeMillis = SystemClock.uptimeMillis();
            long elapsedIdleTimeMillis = 0;
            if (this.mLastEventTimeMillis <= 0) {
                this.mLastEventTimeMillis = startTimeMillis;
            }
            while (true) {
                long currentTimeMillis = SystemClock.uptimeMillis();
                if (globalTimeoutMillis - (currentTimeMillis - startTimeMillis) > elapsedIdleTimeMillis) {
                    long remainingIdleTimeMillis = idleTimeoutMillis - (currentTimeMillis - this.mLastEventTimeMillis);
                    if (remainingIdleTimeMillis > 0) {
                        try {
                            this.mLock.wait(remainingIdleTimeMillis);
                        } catch (InterruptedException e) {
                        }
                        elapsedIdleTimeMillis = 0;
                        startTimeMillis = startTimeMillis;
                    }
                } else {
                    throw new TimeoutException("No idle state with idle timeout: " + idleTimeoutMillis + " within global timeout: " + globalTimeoutMillis);
                }
            }
        }
    }

    public Bitmap takeScreenshot() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        Display display = DisplayManagerGlobal.getInstance().getRealDisplay(0);
        Point displaySize = new Point();
        display.getRealSize(displaySize);
        try {
            Bitmap screenShot = this.mUiAutomationConnection.takeScreenshot(new Rect(0, 0, displaySize.x, displaySize.y), display.getRotation());
            if (screenShot == null) {
                return null;
            }
            screenShot.setHasAlpha(false);
            return screenShot;
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while taking screnshot!", re);
            return null;
        }
    }

    public void setRunAsMonkey(boolean enable) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            ActivityManager.getService().setUserIsMonkey(enable);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while setting run as monkey!", re);
        }
    }

    public boolean clearWindowContentFrameStats(int windowId) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.clearWindowContentFrameStats(windowId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error clearing window content frame stats!", re);
            return false;
        }
    }

    public WindowContentFrameStats getWindowContentFrameStats(int windowId) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.getWindowContentFrameStats(windowId);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error getting window content frame stats!", re);
            return null;
        }
    }

    public void clearWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.clearWindowAnimationFrameStats();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error clearing window animation frame stats!", re);
        }
    }

    public WindowAnimationFrameStats getWindowAnimationFrameStats() {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            return this.mUiAutomationConnection.getWindowAnimationFrameStats();
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error getting window animation frame stats!", re);
            return null;
        }
    }

    public void grantRuntimePermission(String packageName, String permission) {
        grantRuntimePermissionAsUser(packageName, permission, Process.myUserHandle());
    }

    @Deprecated
    public boolean grantRuntimePermission(String packageName, String permission, UserHandle userHandle) {
        grantRuntimePermissionAsUser(packageName, permission, userHandle);
        return true;
    }

    public void grantRuntimePermissionAsUser(String packageName, String permission, UserHandle userHandle) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.grantRuntimePermission(packageName, permission, userHandle.getIdentifier());
        } catch (Exception e) {
            throw new SecurityException("Error granting runtime permission", e);
        }
    }

    public void revokeRuntimePermission(String packageName, String permission) {
        revokeRuntimePermissionAsUser(packageName, permission, Process.myUserHandle());
    }

    @Deprecated
    public boolean revokeRuntimePermission(String packageName, String permission, UserHandle userHandle) {
        revokeRuntimePermissionAsUser(packageName, permission, userHandle);
        return true;
    }

    public void revokeRuntimePermissionAsUser(String packageName, String permission, UserHandle userHandle) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        try {
            this.mUiAutomationConnection.revokeRuntimePermission(packageName, permission, userHandle.getIdentifier());
        } catch (Exception e) {
            throw new SecurityException("Error granting runtime permission", e);
        }
    }

    public ParcelFileDescriptor executeShellCommand(String command) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        warnIfBetterCommand(command);
        ParcelFileDescriptor source = null;
        ParcelFileDescriptor sink = null;
        try {
            ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            source = pipe[0];
            sink = pipe[1];
            this.mUiAutomationConnection.executeShellCommand(command, sink, null);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error executing shell command!", ioe);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing shell command!", re);
        } catch (Throwable th) {
            IoUtils.closeQuietly(sink);
            throw th;
        }
        IoUtils.closeQuietly(sink);
        return source;
    }

    public ParcelFileDescriptor[] executeShellCommandRw(String command) {
        synchronized (this.mLock) {
            throwIfNotConnectedLocked();
        }
        warnIfBetterCommand(command);
        ParcelFileDescriptor source_read = null;
        ParcelFileDescriptor sink_read = null;
        ParcelFileDescriptor source_write = null;
        ParcelFileDescriptor sink_write = null;
        try {
            ParcelFileDescriptor[] pipe_read = ParcelFileDescriptor.createPipe();
            source_read = pipe_read[0];
            sink_read = pipe_read[1];
            ParcelFileDescriptor[] pipe_write = ParcelFileDescriptor.createPipe();
            source_write = pipe_write[0];
            sink_write = pipe_write[1];
            this.mUiAutomationConnection.executeShellCommand(command, sink_read, source_write);
        } catch (IOException ioe) {
            Log.e(LOG_TAG, "Error executing shell command!", ioe);
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error executing shell command!", re);
        } catch (Throwable th) {
            IoUtils.closeQuietly(sink_read);
            IoUtils.closeQuietly(source_write);
            throw th;
        }
        IoUtils.closeQuietly(sink_read);
        IoUtils.closeQuietly(source_write);
        return new ParcelFileDescriptor[]{source_read, sink_write};
    }

    private boolean isConnectedLocked() {
        return this.mConnectionId != -1;
    }

    private void throwIfConnectedLocked() {
        if (this.mConnectionId != -1) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }

    private void throwIfNotConnectedLocked() {
        if (!isConnectedLocked()) {
            throw new IllegalStateException("UiAutomation not connected!");
        }
    }

    private void warnIfBetterCommand(String cmd) {
        if (cmd.startsWith("pm grant ")) {
            Log.w(LOG_TAG, "UiAutomation.grantRuntimePermission() is more robust and should be used instead of 'pm grant'");
        } else if (cmd.startsWith("pm revoke ")) {
            Log.w(LOG_TAG, "UiAutomation.revokeRuntimePermission() is more robust and should be used instead of 'pm revoke'");
        }
    }

    private class IAccessibilityServiceClientImpl extends AccessibilityService.IAccessibilityServiceClientWrapper {
        public IAccessibilityServiceClientImpl(Looper looper) {
            super(null, looper, new AccessibilityService.Callbacks() {
                /* class android.app.UiAutomation.IAccessibilityServiceClientImpl.AnonymousClass1 */

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void init(int connectionId, IBinder windowToken) {
                    synchronized (UiAutomation.this.mLock) {
                        int unused = UiAutomation.this.mConnectionId = connectionId;
                        UiAutomation.this.mLock.notifyAll();
                    }
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onServiceConnected() {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onInterrupt() {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public boolean onGesture(int gestureId) {
                    return false;
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onAccessibilityEvent(AccessibilityEvent event) {
                    OnAccessibilityEventListener listener;
                    synchronized (UiAutomation.this.mLock) {
                        long unused = UiAutomation.this.mLastEventTimeMillis = event.getEventTime();
                        if (UiAutomation.this.mWaitingForEventDelivery) {
                            UiAutomation.this.mEventQueue.add(AccessibilityEvent.obtain(event));
                        }
                        UiAutomation.this.mLock.notifyAll();
                        listener = UiAutomation.this.mOnAccessibilityEventListener;
                    }
                    if (listener != null) {
                        UiAutomation.this.mLocalCallbackHandler.sendMessage(PooledLambda.obtainMessage($$Lambda$GnVtsLTLDH5bZdtLeTd6cfwpgcs.INSTANCE, listener, AccessibilityEvent.obtain(event)));
                    }
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public boolean onKeyEvent(KeyEvent event) {
                    return false;
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onMagnificationChanged(int displayId, Region region, float scale, float centerX, float centerY) {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onSoftKeyboardShowModeChanged(int showMode) {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onPerformGestureResult(int sequence, boolean completedSuccessfully) {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onFingerprintCapturingGesturesChanged(boolean active) {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onFingerprintGesture(int gesture) {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onAccessibilityButtonClicked() {
                }

                @Override // android.accessibilityservice.AccessibilityService.Callbacks
                public void onAccessibilityButtonAvailabilityChanged(boolean available) {
                }
            });
        }
    }
}
