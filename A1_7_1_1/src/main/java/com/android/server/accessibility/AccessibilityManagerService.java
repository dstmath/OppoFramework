package com.android.server.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription.MotionEventGenerator;
import android.accessibilityservice.IAccessibilityServiceClient;
import android.accessibilityservice.IAccessibilityServiceConnection;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.app.StatusBarManager;
import android.appwidget.AppWidgetManagerInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ParceledListSlice;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.hardware.display.DisplayManager;
import android.hardware.input.InputManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IBinder.DeathRecipient;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.text.TextUtils;
import android.text.TextUtils.SimpleStringSplitter;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.view.Display;
import android.view.IWindow;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MagnificationSpec;
import android.view.MotionEvent;
import android.view.WindowInfo;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerInternal;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityInteractionClient;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.accessibility.IAccessibilityInteractionConnection;
import android.view.accessibility.IAccessibilityInteractionConnectionCallback;
import android.view.accessibility.IAccessibilityManager.Stub;
import android.view.accessibility.IAccessibilityManagerClient;
import com.android.internal.content.PackageMonitor;
import com.android.internal.os.SomeArgs;
import com.android.internal.util.ArrayUtils;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.am.OppoMultiAppManager;
import com.android.server.display.OppoBrightUtils;
import com.android.server.statusbar.StatusBarManagerInternal;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import libcore.util.EmptyArray;

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
public class AccessibilityManagerService extends Stub {
    private static final char COMPONENT_NAME_SEPARATOR = ':';
    private static final boolean DEBUG = false;
    private static final String FUNCTION_DUMP = "dump";
    private static final String FUNCTION_REGISTER_UI_TEST_AUTOMATION_SERVICE = "registerUiTestAutomationService";
    private static final String GET_WINDOW_TOKEN = "getWindowToken";
    private static final boolean IS_ENG_BUILD = false;
    private static final String LOG_TAG = "AccessibilityManagerService";
    public static final int MAGNIFICATION_GESTURE_HANDLER_ID = 0;
    private static final int OWN_PROCESS_ID = 0;
    private static final String TEMPORARY_ENABLE_ACCESSIBILITY_UNTIL_KEYGUARD_REMOVED = "temporaryEnableAccessibilityStateUntilKeyguardRemoved";
    private static final int WAIT_FOR_USER_STATE_FULLY_INITIALIZED_MILLIS = 3000;
    private static final int WAIT_MOTION_INJECTOR_TIMEOUT_MILLIS = 1000;
    private static final int WAIT_RGB_MOUDLE_READY = 400;
    private static final int WAIT_WINDOWS_TIMEOUT_MILLIS = 5000;
    private static final int WINDOW_ID_UNKNOWN = -1;
    private static final ComponentName sFakeAccessibilityServiceComponentName = null;
    private static int sIdCounter;
    private static int sNextWindowId;
    private AppWidgetManagerInternal mAppWidgetService;
    private final Context mContext;
    private int mCurrentUserId;
    private AlertDialog mEnableTouchExplorationDialog;
    private final List<AccessibilityServiceInfo> mEnabledServicesForFeedbackTempList;
    private final RemoteCallbackList<IAccessibilityManagerClient> mGlobalClients;
    private final SparseArray<RemoteAccessibilityConnection> mGlobalInteractionConnections;
    private final SparseArray<IBinder> mGlobalWindowTokens;
    private boolean mHasInputFilter;
    private boolean mInitialized;
    private AccessibilityInputFilter mInputFilter;
    private InteractionBridge mInteractionBridge;
    private KeyEventDispatcher mKeyEventDispatcher;
    private final Object mLock;
    private MagnificationController mMagnificationController;
    private final MainHandler mMainHandler;
    private MotionEventInjector mMotionEventInjector;
    private boolean mNeedShowMagnificationDialog;
    private final PackageManager mPackageManager;
    private final PowerManager mPowerManager;
    private final SecurityPolicy mSecurityPolicy;
    private final SimpleStringSplitter mStringColonSplitter;
    private final List<AccessibilityServiceInfo> mTempAccessibilityServiceInfoList;
    private final Set<ComponentName> mTempComponentNameSet;
    private final Point mTempPoint;
    private final Rect mTempRect;
    private final Rect mTempRect1;
    private final UserManager mUserManager;
    private final SparseArray<UserState> mUserStates;
    private final WindowManagerInternal mWindowManagerService;
    private WindowsForAccessibilityCallback mWindowsForAccessibilityCallback;

    private final class AccessibilityContentObserver extends ContentObserver {
        private final Uri mAccessibilitySoftKeyboardModeUri = Secure.getUriFor("accessibility_soft_keyboard_mode");
        private final Uri mAutoclickEnabledUri = Secure.getUriFor("accessibility_autoclick_enabled");
        private final Uri mDisplayAdjustValuesUri = System.getUriFor("color_dispaly_adjust");
        private final Uri mDisplayDaltonizerEnabledUri = Secure.getUriFor("accessibility_display_daltonizer_enabled");
        private final Uri mDisplayDaltonizerUri = Secure.getUriFor("accessibility_display_daltonizer");
        private final Uri mDisplayInversionEnabledUri = Secure.getUriFor("accessibility_display_inversion_enabled");
        private final Uri mDisplayMagnificationEnabledUri = Secure.getUriFor("accessibility_display_magnification_enabled");
        private final Uri mEnabledAccessibilityServicesUri = Secure.getUriFor("enabled_accessibility_services");
        private final Uri mEnhancedWebAccessibilityUri = Secure.getUriFor("accessibility_script_injection");
        private final Uri mHighTextContrastUri = Secure.getUriFor("high_text_contrast_enabled");
        private final Uri mTouchExplorationEnabledUri = Secure.getUriFor("touch_exploration_enabled");
        private final Uri mTouchExplorationGrantedAccessibilityServicesUri = Secure.getUriFor("touch_exploration_granted_accessibility_services");

        public AccessibilityContentObserver(Handler handler) {
            super(handler);
        }

        public void register(ContentResolver contentResolver) {
            contentResolver.registerContentObserver(this.mTouchExplorationEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayMagnificationEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAutoclickEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mEnabledAccessibilityServicesUri, false, this, -1);
            contentResolver.registerContentObserver(this.mTouchExplorationGrantedAccessibilityServicesUri, false, this, -1);
            contentResolver.registerContentObserver(this.mEnhancedWebAccessibilityUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayInversionEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayDaltonizerEnabledUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayDaltonizerUri, false, this, -1);
            contentResolver.registerContentObserver(this.mHighTextContrastUri, false, this, -1);
            contentResolver.registerContentObserver(this.mAccessibilitySoftKeyboardModeUri, false, this, -1);
            contentResolver.registerContentObserver(this.mDisplayAdjustValuesUri, false, this, -1);
        }

        /* JADX WARNING: Missing block: B:14:0x002b, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onChange(boolean selfChange, Uri uri) {
            synchronized (AccessibilityManagerService.this.mLock) {
                UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                if (userState.isUiAutomationSuppressingOtherServices()) {
                } else if (this.mTouchExplorationEnabledUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readTouchExplorationEnabledSettingLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mDisplayMagnificationEnabledUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readDisplayMagnificationEnabledSettingLocked(userState)) {
                        if (userState.mIsDisplayMagnificationEnabled) {
                            AccessibilityManagerService.this.setMagnificationDialogEnable(true);
                        }
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mAutoclickEnabledUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readAutoclickEnabledSettingLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mEnabledAccessibilityServicesUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readEnabledAccessibilityServicesLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mTouchExplorationGrantedAccessibilityServicesUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readTouchExplorationGrantedAccessibilityServicesLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mEnhancedWebAccessibilityUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readEnhancedWebAccessibilityEnabledChangedLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mDisplayDaltonizerEnabledUri.equals(uri) || this.mDisplayDaltonizerUri.equals(uri)) {
                    AccessibilityManagerService.this.updateDisplayDaltonizerLocked(userState);
                } else if (this.mDisplayInversionEnabledUri.equals(uri)) {
                    AccessibilityManagerService.this.updateDisplayInversionLocked(userState);
                } else if (this.mHighTextContrastUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readHighTextContrastEnabledSettingLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mAccessibilitySoftKeyboardModeUri.equals(uri)) {
                    if (AccessibilityManagerService.this.readSoftKeyboardShowModeChangedLocked(userState)) {
                        AccessibilityManagerService.this.notifySoftKeyboardShowModeChangedLocked(userState.mSoftKeyboardShowMode);
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                } else if (this.mDisplayAdjustValuesUri.equals(uri)) {
                    Slog.e("DispalyAdjut", "display adjust the values is changed!!");
                    AccessibilityManagerService.this.updateDisplayAdjustValues(userState);
                }
            }
        }
    }

    private final class InteractionBridge {
        private final AccessibilityInteractionClient mClient = AccessibilityInteractionClient.getInstance();
        private final int mConnectionId;
        private final Display mDefaultDisplay;

        public InteractionBridge() {
            AccessibilityServiceInfo info = new AccessibilityServiceInfo();
            info.setCapabilities(1);
            info.flags |= 64;
            info.flags |= 2;
            Service service = new Service(-10000, AccessibilityManagerService.sFakeAccessibilityServiceComponentName, info);
            this.mConnectionId = service.mId;
            this.mClient.addConnection(this.mConnectionId, service);
            this.mDefaultDisplay = ((DisplayManager) AccessibilityManagerService.this.mContext.getSystemService("display")).getDisplay(0);
        }

        public void clearAccessibilityFocusNotLocked(int windowId) {
            AccessibilityNodeInfo focus = getAccessibilityFocusNotLocked(windowId);
            if (focus != null) {
                focus.performAction(128);
            }
        }

        public boolean getAccessibilityFocusClickPointInScreenNotLocked(Point outPoint) {
            AccessibilityNodeInfo focus = getAccessibilityFocusNotLocked();
            if (focus == null) {
                return false;
            }
            synchronized (AccessibilityManagerService.this.mLock) {
                Rect boundsInScreen = AccessibilityManagerService.this.mTempRect;
                focus.getBoundsInScreen(boundsInScreen);
                MagnificationSpec spec = AccessibilityManagerService.this.getCompatibleMagnificationSpecLocked(focus.getWindowId());
                if (!(spec == null || spec.isNop())) {
                    boundsInScreen.offset((int) (-spec.offsetX), (int) (-spec.offsetY));
                    boundsInScreen.scale(1.0f / spec.scale);
                }
                Rect windowBounds = AccessibilityManagerService.this.mTempRect1;
                AccessibilityManagerService.this.getWindowBounds(focus.getWindowId(), windowBounds);
                if (boundsInScreen.intersect(windowBounds)) {
                    Point screenSize = AccessibilityManagerService.this.mTempPoint;
                    this.mDefaultDisplay.getRealSize(screenSize);
                    if (boundsInScreen.intersect(0, 0, screenSize.x, screenSize.y)) {
                        outPoint.set(boundsInScreen.centerX(), boundsInScreen.centerY());
                        return true;
                    }
                    return false;
                }
                return false;
            }
        }

        private AccessibilityNodeInfo getAccessibilityFocusNotLocked() {
            synchronized (AccessibilityManagerService.this.mLock) {
                int focusedWindowId = AccessibilityManagerService.this.mSecurityPolicy.mAccessibilityFocusedWindowId;
                if (focusedWindowId == -1) {
                    return null;
                }
                return getAccessibilityFocusNotLocked(focusedWindowId);
            }
        }

        private AccessibilityNodeInfo getAccessibilityFocusNotLocked(int windowId) {
            return this.mClient.findFocus(this.mConnectionId, windowId, AccessibilityNodeInfo.ROOT_NODE_ID, 2);
        }
    }

    private final class MainHandler extends Handler {
        public static final int MSG_ANNOUNCE_NEW_USER_IF_NEEDED = 5;
        public static final int MSG_CLEAR_ACCESSIBILITY_FOCUS = 9;
        public static final int MSG_DISPLAY_ADJUST_TMP = 10;
        public static final int MSG_SEND_ACCESSIBILITY_EVENT_TO_INPUT_FILTER = 1;
        public static final int MSG_SEND_CLEARED_STATE_TO_CLIENTS_FOR_USER = 3;
        public static final int MSG_SEND_KEY_EVENT_TO_INPUT_FILTER = 8;
        public static final int MSG_SEND_STATE_TO_CLIENTS = 2;
        public static final int MSG_SHOW_ENABLED_TOUCH_EXPLORATION_DIALOG = 7;
        public static final int MSG_UPDATE_INPUT_FILTER = 6;

        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    AccessibilityEvent event = msg.obj;
                    synchronized (AccessibilityManagerService.this.mLock) {
                        if (AccessibilityManagerService.this.mHasInputFilter && AccessibilityManagerService.this.mInputFilter != null) {
                            AccessibilityManagerService.this.mInputFilter.notifyAccessibilityEvent(event);
                        }
                    }
                    event.recycle();
                    return;
                case 2:
                    int clientState = msg.arg1;
                    int userId = msg.arg2;
                    sendStateToClients(clientState, AccessibilityManagerService.this.mGlobalClients);
                    sendStateToClientsForUser(clientState, userId);
                    return;
                case 3:
                    sendStateToClientsForUser(0, msg.arg1);
                    return;
                case 5:
                    announceNewUserIfNeeded();
                    return;
                case 6:
                    AccessibilityManagerService.this.updateInputFilter(msg.obj);
                    return;
                case 7:
                    AccessibilityManagerService.this.showEnableTouchExplorationDialog(msg.obj);
                    return;
                case 8:
                    KeyEvent event2 = msg.obj;
                    int policyFlags = msg.arg1;
                    synchronized (AccessibilityManagerService.this.mLock) {
                        if (AccessibilityManagerService.this.mHasInputFilter && AccessibilityManagerService.this.mInputFilter != null) {
                            AccessibilityManagerService.this.mInputFilter.sendInputEvent(event2, policyFlags);
                        }
                    }
                    event2.recycle();
                    return;
                case 9:
                    InteractionBridge bridge;
                    int windowId = msg.arg1;
                    synchronized (AccessibilityManagerService.this.mLock) {
                        bridge = AccessibilityManagerService.this.getInteractionBridgeLocked();
                    }
                    bridge.clearAccessibilityFocusNotLocked(windowId);
                    return;
                case 10:
                    DisplayAdjustmentUtils.applyAdjustValuesRGB(AccessibilityManagerService.this.mContext, AccessibilityManagerService.this.getCurrentUserStateLocked().mUserId);
                    return;
                default:
                    return;
            }
        }

        private void announceNewUserIfNeeded() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (AccessibilityManagerService.this.getCurrentUserStateLocked().isHandlingAccessibilityEvents()) {
                    UserManager userManager = (UserManager) AccessibilityManagerService.this.mContext.getSystemService("user");
                    Context -get2 = AccessibilityManagerService.this.mContext;
                    Object[] objArr = new Object[1];
                    objArr[0] = userManager.getUserInfo(AccessibilityManagerService.this.mCurrentUserId).name;
                    String message = -get2.getString(17040701, objArr);
                    AccessibilityEvent event = AccessibilityEvent.obtain(16384);
                    event.getText().add(message);
                    AccessibilityManagerService.this.sendAccessibilityEvent(event, AccessibilityManagerService.this.mCurrentUserId);
                }
            }
        }

        private void sendStateToClientsForUser(int clientState, int userId) {
            UserState userState;
            synchronized (AccessibilityManagerService.this.mLock) {
                userState = AccessibilityManagerService.this.getUserStateLocked(userId);
            }
            sendStateToClients(clientState, userState.mClients);
        }

        private void sendStateToClients(int clientState, RemoteCallbackList<IAccessibilityManagerClient> clients) {
            try {
                int userClientCount = clients.beginBroadcast();
                for (int i = 0; i < userClientCount; i++) {
                    try {
                        ((IAccessibilityManagerClient) clients.getBroadcastItem(i)).setState(clientState);
                    } catch (RemoteException e) {
                    }
                }
            } finally {
                clients.finishBroadcast();
            }
        }
    }

    public class OppoGestureObserver extends ContentObserver {
        private static final String OPPO_GESTURE_SCREEN_HOVERING = "oppo_gesture_screen_hovering";

        public OppoGestureObserver(Handler handler) {
            super(handler);
        }

        void observe() {
            AccessibilityManagerService.this.mContext.getContentResolver().registerContentObserver(System.getUriFor(OPPO_GESTURE_SCREEN_HOVERING), false, this);
            updateService();
        }

        public void onChange(boolean selfChange) {
            updateService();
        }

        private void updateService() {
            boolean screenHoveringEnabled = true;
            if (System.getInt(AccessibilityManagerService.this.mContext.getContentResolver(), OPPO_GESTURE_SCREEN_HOVERING, 0) != 1) {
                screenHoveringEnabled = false;
            }
            if (screenHoveringEnabled) {
                Slog.d(AccessibilityManagerService.LOG_TAG, "----------------------- mInputFilter" + AccessibilityManagerService.this.mMagnificationController);
                AccessibilityManagerService.this.getMagnificationController().register();
            }
        }
    }

    class RemoteAccessibilityConnection implements DeathRecipient {
        private final IAccessibilityInteractionConnection mConnection;
        private final String mPackageName;
        private final int mUid;
        private final int mUserId;
        private final int mWindowId;

        RemoteAccessibilityConnection(int windowId, IAccessibilityInteractionConnection connection, String packageName, int uid, int userId) {
            this.mWindowId = windowId;
            this.mPackageName = packageName;
            this.mUid = uid;
            this.mUserId = userId;
            this.mConnection = connection;
        }

        public int getUid() {
            return this.mUid;
        }

        public String getPackageName() {
            return this.mPackageName;
        }

        public IAccessibilityInteractionConnection getRemote() {
            return this.mConnection;
        }

        public void linkToDeath() throws RemoteException {
            this.mConnection.asBinder().linkToDeath(this, 0);
        }

        public void unlinkToDeath() {
            this.mConnection.asBinder().unlinkToDeath(this, 0);
        }

        public void binderDied() {
            unlinkToDeath();
            synchronized (AccessibilityManagerService.this.mLock) {
                AccessibilityManagerService.this.removeAccessibilityInteractionConnectionLocked(this.mWindowId, this.mUserId);
            }
        }
    }

    final class SecurityPolicy {
        public static final int INVALID_WINDOW_ID = -1;
        private static final int RETRIEVAL_ALLOWING_EVENT_TYPES = 244159;
        public long mAccessibilityFocusNodeId = 2147483647L;
        public int mAccessibilityFocusedWindowId = -1;
        public int mActiveWindowId = -1;
        public int mFocusedWindowId = -1;
        private boolean mTouchInteractionInProgress;
        public List<AccessibilityWindowInfo> mWindows;

        SecurityPolicy() {
        }

        private boolean canDispatchAccessibilityEventLocked(AccessibilityEvent event) {
            switch (event.getEventType()) {
                case 32:
                case 64:
                case 128:
                case 256:
                case 512:
                case 1024:
                case 16384:
                case DumpState.DUMP_DOMAIN_PREFERRED /*262144*/:
                case DumpState.DUMP_FROZEN /*524288*/:
                case DumpState.DUMP_DEXOPT /*1048576*/:
                case DumpState.DUMP_COMPILER_STATS /*2097152*/:
                case 4194304:
                case 16777216:
                    return true;
                default:
                    return isRetrievalAllowingWindow(event.getWindowId());
            }
        }

        private boolean isValidPackageForUid(String packageName, int uid) {
            boolean z = false;
            long token = Binder.clearCallingIdentity();
            try {
                if (uid == AccessibilityManagerService.this.mPackageManager.getPackageUid(packageName, UserHandle.getUserId(uid))) {
                    z = true;
                }
                Binder.restoreCallingIdentity(token);
                return z;
            } catch (NameNotFoundException e) {
                Binder.restoreCallingIdentity(token);
                return false;
            } catch (Throwable th) {
                Binder.restoreCallingIdentity(token);
                throw th;
            }
        }

        String resolveValidReportedPackageLocked(CharSequence packageName, int appId, int userId) {
            if (packageName == null) {
                return null;
            }
            if (appId == 1000) {
                return packageName.toString();
            }
            String packageNameStr = packageName.toString();
            int resolvedUid = UserHandle.getUid(userId, appId);
            if (isValidPackageForUid(packageNameStr, resolvedUid)) {
                return packageName.toString();
            }
            AppWidgetManagerInternal appWidgetManager = AccessibilityManagerService.this.getAppWidgetManager();
            if (appWidgetManager != null && ArrayUtils.contains(appWidgetManager.getHostedWidgetPackages(resolvedUid), packageNameStr)) {
                return packageName.toString();
            }
            String[] packageNames = AccessibilityManagerService.this.mPackageManager.getPackagesForUid(resolvedUid);
            if (ArrayUtils.isEmpty(packageNames)) {
                return null;
            }
            return packageNames[0];
        }

        String[] computeValidReportedPackages(int callingUid, String targetPackage, int targetUid) {
            if (UserHandle.getAppId(callingUid) == 1000) {
                return EmptyArray.STRING;
            }
            String[] uidPackages = new String[1];
            uidPackages[0] = targetPackage;
            AppWidgetManagerInternal appWidgetManager = AccessibilityManagerService.this.getAppWidgetManager();
            if (appWidgetManager != null) {
                ArraySet<String> widgetPackages = appWidgetManager.getHostedWidgetPackages(targetUid);
                if (!(widgetPackages == null || widgetPackages.isEmpty())) {
                    String[] validPackages = new String[(uidPackages.length + widgetPackages.size())];
                    System.arraycopy(uidPackages, 0, validPackages, 0, uidPackages.length);
                    int widgetPackageCount = widgetPackages.size();
                    for (int i = 0; i < widgetPackageCount; i++) {
                        validPackages[uidPackages.length + i] = (String) widgetPackages.valueAt(i);
                    }
                    return validPackages;
                }
            }
            return uidPackages;
        }

        public void clearWindowsLocked() {
            List<AccessibilityWindowInfo> windows = Collections.emptyList();
            int activeWindowId = this.mActiveWindowId;
            updateWindowsLocked(windows);
            this.mActiveWindowId = activeWindowId;
            this.mWindows = null;
        }

        public void updateWindowsLocked(List<AccessibilityWindowInfo> windows) {
            int i;
            if (this.mWindows == null) {
                this.mWindows = new ArrayList();
            }
            for (i = this.mWindows.size() - 1; i >= 0; i--) {
                ((AccessibilityWindowInfo) this.mWindows.remove(i)).recycle();
            }
            this.mFocusedWindowId = -1;
            if (!this.mTouchInteractionInProgress) {
                this.mActiveWindowId = -1;
            }
            boolean activeWindowGone = true;
            int windowCount = windows.size();
            if (windowCount > 0) {
                AccessibilityWindowInfo window;
                for (i = 0; i < windowCount; i++) {
                    window = (AccessibilityWindowInfo) windows.get(i);
                    int windowId = window.getId();
                    if (window.isFocused()) {
                        this.mFocusedWindowId = windowId;
                        if (!this.mTouchInteractionInProgress) {
                            this.mActiveWindowId = windowId;
                            window.setActive(true);
                        } else if (windowId == this.mActiveWindowId) {
                            activeWindowGone = false;
                        }
                    }
                    this.mWindows.add(window);
                }
                if (this.mTouchInteractionInProgress && activeWindowGone) {
                    this.mActiveWindowId = this.mFocusedWindowId;
                }
                for (i = 0; i < windowCount; i++) {
                    window = (AccessibilityWindowInfo) this.mWindows.get(i);
                    if (window.getId() == this.mActiveWindowId) {
                        window.setActive(true);
                    }
                    if (window.getId() == this.mAccessibilityFocusedWindowId) {
                        window.setAccessibilityFocused(true);
                    }
                }
            }
            notifyWindowsChanged();
        }

        public boolean computePartialInteractiveRegionForWindowLocked(int windowId, Region outRegion) {
            if (this.mWindows == null) {
                return false;
            }
            Region windowInteractiveRegion = null;
            boolean windowInteractiveRegionChanged = false;
            for (int i = this.mWindows.size() - 1; i >= 0; i--) {
                AccessibilityWindowInfo currentWindow = (AccessibilityWindowInfo) this.mWindows.get(i);
                Rect currentWindowBounds;
                if (windowInteractiveRegion == null) {
                    if (currentWindow.getId() == windowId) {
                        currentWindowBounds = AccessibilityManagerService.this.mTempRect;
                        currentWindow.getBoundsInScreen(currentWindowBounds);
                        outRegion.set(currentWindowBounds);
                        windowInteractiveRegion = outRegion;
                    }
                } else if (currentWindow.getType() != 4) {
                    currentWindowBounds = AccessibilityManagerService.this.mTempRect;
                    currentWindow.getBoundsInScreen(currentWindowBounds);
                    if (windowInteractiveRegion.op(currentWindowBounds, Op.DIFFERENCE)) {
                        windowInteractiveRegionChanged = true;
                    }
                }
            }
            return windowInteractiveRegionChanged;
        }

        public void updateEventSourceLocked(AccessibilityEvent event) {
            if ((event.getEventType() & RETRIEVAL_ALLOWING_EVENT_TYPES) == 0) {
                event.setSource(null);
            }
        }

        public void updateActiveAndAccessibilityFocusedWindowLocked(int windowId, long nodeId, int eventType, int eventAction) {
            Object -get8;
            switch (eventType) {
                case 32:
                    -get8 = AccessibilityManagerService.this.mLock;
                    synchronized (-get8) {
                        if (AccessibilityManagerService.this.mWindowsForAccessibilityCallback == null) {
                            this.mFocusedWindowId = getFocusedWindowId();
                            if (windowId == this.mFocusedWindowId) {
                                this.mActiveWindowId = windowId;
                                break;
                            }
                        }
                    }
                    break;
                case 128:
                    -get8 = AccessibilityManagerService.this.mLock;
                    synchronized (-get8) {
                        if (this.mTouchInteractionInProgress && this.mActiveWindowId != windowId) {
                            setActiveWindowLocked(windowId);
                            break;
                        }
                    }
                case 32768:
                    -get8 = AccessibilityManagerService.this.mLock;
                    synchronized (-get8) {
                        if (this.mAccessibilityFocusedWindowId != windowId) {
                            AccessibilityManagerService.this.mMainHandler.obtainMessage(9, this.mAccessibilityFocusedWindowId, 0).sendToTarget();
                            AccessibilityManagerService.this.mSecurityPolicy.setAccessibilityFocusedWindowLocked(windowId);
                            this.mAccessibilityFocusNodeId = nodeId;
                            break;
                        }
                    }
                    break;
                case DumpState.DUMP_INSTALLS /*65536*/:
                    -get8 = AccessibilityManagerService.this.mLock;
                    synchronized (-get8) {
                        if (this.mAccessibilityFocusNodeId == nodeId) {
                            this.mAccessibilityFocusNodeId = 2147483647L;
                        }
                        if (this.mAccessibilityFocusNodeId == 2147483647L && this.mAccessibilityFocusedWindowId == windowId && eventAction != 64) {
                            this.mAccessibilityFocusedWindowId = -1;
                            break;
                        }
                    }
                default:
                    return;
            }
        }

        public void onTouchInteractionStart() {
            synchronized (AccessibilityManagerService.this.mLock) {
                this.mTouchInteractionInProgress = true;
            }
        }

        public void onTouchInteractionEnd() {
            synchronized (AccessibilityManagerService.this.mLock) {
                this.mTouchInteractionInProgress = false;
                int oldActiveWindow = AccessibilityManagerService.this.mSecurityPolicy.mActiveWindowId;
                setActiveWindowLocked(this.mFocusedWindowId);
                if (oldActiveWindow != AccessibilityManagerService.this.mSecurityPolicy.mActiveWindowId && this.mAccessibilityFocusedWindowId == oldActiveWindow && AccessibilityManagerService.this.getCurrentUserStateLocked().mAccessibilityFocusOnlyInActiveWindow) {
                    AccessibilityManagerService.this.mMainHandler.obtainMessage(9, oldActiveWindow, 0).sendToTarget();
                }
            }
        }

        public int getActiveWindowId() {
            if (this.mActiveWindowId == -1 && !this.mTouchInteractionInProgress) {
                this.mActiveWindowId = getFocusedWindowId();
            }
            return this.mActiveWindowId;
        }

        private void setActiveWindowLocked(int windowId) {
            if (this.mActiveWindowId != windowId) {
                this.mActiveWindowId = windowId;
                if (this.mWindows != null) {
                    int windowCount = this.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindows.get(i);
                        window.setActive(window.getId() == windowId);
                    }
                }
                notifyWindowsChanged();
            }
        }

        private void setAccessibilityFocusedWindowLocked(int windowId) {
            if (this.mAccessibilityFocusedWindowId != windowId) {
                this.mAccessibilityFocusedWindowId = windowId;
                if (this.mWindows != null) {
                    int windowCount = this.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindows.get(i);
                        window.setAccessibilityFocused(window.getId() == windowId);
                    }
                }
                notifyWindowsChanged();
            }
        }

        private void notifyWindowsChanged() {
            if (AccessibilityManagerService.this.mWindowsForAccessibilityCallback != null) {
                long identity = Binder.clearCallingIdentity();
                try {
                    AccessibilityEvent event = AccessibilityEvent.obtain(4194304);
                    event.setEventTime(SystemClock.uptimeMillis());
                    AccessibilityManagerService.this.sendAccessibilityEvent(event, AccessibilityManagerService.this.mCurrentUserId);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
        }

        public boolean canGetAccessibilityNodeInfoLocked(Service service, int windowId) {
            return canRetrieveWindowContentLocked(service) ? isRetrievalAllowingWindow(windowId) : false;
        }

        public boolean canRetrieveWindowsLocked(Service service) {
            return canRetrieveWindowContentLocked(service) ? service.mRetrieveInteractiveWindows : false;
        }

        public boolean canRetrieveWindowContentLocked(Service service) {
            return (service.mAccessibilityServiceInfo.getCapabilities() & 1) != 0;
        }

        public boolean canControlMagnification(Service service) {
            return (service.mAccessibilityServiceInfo.getCapabilities() & 16) != 0;
        }

        public boolean canPerformGestures(Service service) {
            return (service.mAccessibilityServiceInfo.getCapabilities() & 32) != 0;
        }

        private int resolveProfileParentLocked(int userId) {
            if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                long identity = Binder.clearCallingIdentity();
                try {
                    UserInfo parent = AccessibilityManagerService.this.mUserManager.getProfileParent(userId);
                    if (parent != null) {
                        int identifier = parent.getUserHandle().getIdentifier();
                        return identifier;
                    }
                    Binder.restoreCallingIdentity(identity);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            }
            return userId;
        }

        public int resolveCallingUserIdEnforcingPermissionsLocked(int userId) {
            int callingUid = Binder.getCallingUid();
            if (callingUid != 0 && callingUid != 1000 && callingUid != 2000) {
                int callingUserId = UserHandle.getUserId(callingUid);
                if (callingUserId == userId) {
                    return resolveProfileParentLocked(userId);
                }
                if (resolveProfileParentLocked(callingUserId) == AccessibilityManagerService.this.mCurrentUserId && (userId == -2 || userId == -3)) {
                    return AccessibilityManagerService.this.mCurrentUserId;
                }
                if (!hasPermission("android.permission.INTERACT_ACROSS_USERS") && !hasPermission("android.permission.INTERACT_ACROSS_USERS_FULL")) {
                    throw new SecurityException("Call from user " + callingUserId + " as user " + userId + " without permission INTERACT_ACROSS_USERS or " + "INTERACT_ACROSS_USERS_FULL not allowed.");
                } else if (userId == -2 || userId == -3) {
                    return AccessibilityManagerService.this.mCurrentUserId;
                } else {
                    throw new IllegalArgumentException("Calling user can be changed to only UserHandle.USER_CURRENT or UserHandle.USER_CURRENT_OR_SELF.");
                }
            } else if (userId == -2 || userId == -3) {
                return AccessibilityManagerService.this.mCurrentUserId;
            } else {
                return resolveProfileParentLocked(userId);
            }
        }

        public boolean isCallerInteractingAcrossUsers(int userId) {
            int callingUid = Binder.getCallingUid();
            if (Binder.getCallingPid() == Process.myPid() || callingUid == 2000 || userId == -2 || userId == -3) {
                return true;
            }
            return false;
        }

        private boolean isRetrievalAllowingWindow(int windowId) {
            boolean z = true;
            if (Binder.getCallingUid() == 1000 || windowId == this.mActiveWindowId) {
                return true;
            }
            if (findWindowById(windowId) == null) {
                z = false;
            }
            return z;
        }

        private AccessibilityWindowInfo findWindowById(int windowId) {
            if (this.mWindows != null) {
                int windowCount = this.mWindows.size();
                for (int i = 0; i < windowCount; i++) {
                    AccessibilityWindowInfo window = (AccessibilityWindowInfo) this.mWindows.get(i);
                    if (window.getId() == windowId) {
                        return window;
                    }
                }
            }
            return null;
        }

        private void enforceCallingPermission(String permission, String function) {
            if (AccessibilityManagerService.OWN_PROCESS_ID != Binder.getCallingPid() && !hasPermission(permission)) {
                throw new SecurityException("You do not have " + permission + " required to call " + function + " from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
            }
        }

        private boolean hasPermission(String permission) {
            return AccessibilityManagerService.this.mContext.checkCallingPermission(permission) == 0;
        }

        private int getFocusedWindowId() {
            int -wrap15;
            IBinder token = AccessibilityManagerService.this.mWindowManagerService.getFocusedWindowToken();
            synchronized (AccessibilityManagerService.this.mLock) {
                -wrap15 = AccessibilityManagerService.this.findWindowIdLocked(token);
            }
            return -wrap15;
        }

        public void updateActiveWindowLocked() {
            int windowId = AccessibilityManagerService.this.findWindowIdLocked(AccessibilityManagerService.this.mWindowManagerService.getFocusedWindowToken());
            Slog.i(AccessibilityManagerService.LOG_TAG, "updateActiveWindow, windowId = " + windowId + ", mActiveWindowId = " + this.mActiveWindowId);
            if (windowId != this.mActiveWindowId) {
                this.mActiveWindowId = windowId;
            } else {
                this.mActiveWindowId = -1;
            }
        }
    }

    class Service extends IAccessibilityServiceConnection.Stub implements ServiceConnection, DeathRecipient {
        AccessibilityServiceInfo mAccessibilityServiceInfo;
        ComponentName mComponentName;
        public Handler mEventDispatchHandler = new Handler(AccessibilityManagerService.this.mMainHandler.getLooper()) {
            public void handleMessage(Message message) {
                Service.this.notifyAccessibilityEventInternal(message.what, message.obj);
            }
        };
        int mEventTypes;
        int mFeedbackType;
        int mFetchFlags;
        int mId = 0;
        Intent mIntent;
        public final InvocationHandler mInvocationHandler = new InvocationHandler(AccessibilityManagerService.this.mMainHandler.getLooper());
        boolean mIsAutomation;
        boolean mIsDefault;
        long mNotificationTimeout;
        final IBinder mOverlayWindowToken = new Binder();
        Set<String> mPackageNames = new HashSet();
        final SparseArray<AccessibilityEvent> mPendingEvents = new SparseArray();
        boolean mRequestEnhancedWebAccessibility;
        boolean mRequestFilterKeyEvents;
        boolean mRequestTouchExplorationMode;
        final ResolveInfo mResolveInfo;
        boolean mRetrieveInteractiveWindows;
        IBinder mService;
        IAccessibilityServiceClient mServiceInterface;
        final int mUserId;
        boolean mWasConnectedAndDied;

        private final class InvocationHandler extends Handler {
            public static final int MSG_CLEAR_ACCESSIBILITY_CACHE = 2;
            public static final int MSG_ON_GESTURE = 1;
            private static final int MSG_ON_MAGNIFICATION_CHANGED = 5;
            private static final int MSG_ON_SOFT_KEYBOARD_STATE_CHANGED = 6;
            private boolean mIsMagnificationCallbackEnabled = false;
            private boolean mIsSoftKeyboardCallbackEnabled = false;

            public InvocationHandler(Looper looper) {
                super(looper, null, true);
            }

            public void handleMessage(Message message) {
                int type = message.what;
                switch (type) {
                    case 1:
                        Service.this.notifyGestureInternal(message.arg1);
                        return;
                    case 2:
                        Service.this.notifyClearAccessibilityCacheInternal();
                        return;
                    case 5:
                        SomeArgs args = message.obj;
                        Service.this.notifyMagnificationChangedInternal(args.arg1, ((Float) args.arg2).floatValue(), ((Float) args.arg3).floatValue(), ((Float) args.arg4).floatValue());
                        return;
                    case 6:
                        Service.this.notifySoftKeyboardShowModeChangedInternal(message.arg1);
                        return;
                    default:
                        throw new IllegalArgumentException("Unknown message: " + type);
                }
            }

            public void notifyMagnificationChangedLocked(Region region, float scale, float centerX, float centerY) {
                if (this.mIsMagnificationCallbackEnabled) {
                    SomeArgs args = SomeArgs.obtain();
                    args.arg1 = region;
                    args.arg2 = Float.valueOf(scale);
                    args.arg3 = Float.valueOf(centerX);
                    args.arg4 = Float.valueOf(centerY);
                    obtainMessage(5, args).sendToTarget();
                }
            }

            public void setMagnificationCallbackEnabled(boolean enabled) {
                this.mIsMagnificationCallbackEnabled = enabled;
            }

            public void notifySoftKeyboardShowModeChangedLocked(int showState) {
                if (this.mIsSoftKeyboardCallbackEnabled) {
                    obtainMessage(6, showState, 0).sendToTarget();
                }
            }

            public void setSoftKeyboardCallbackEnabled(boolean enabled) {
                this.mIsSoftKeyboardCallbackEnabled = enabled;
            }
        }

        public Service(int userId, ComponentName componentName, AccessibilityServiceInfo accessibilityServiceInfo) {
            this.mUserId = userId;
            this.mResolveInfo = accessibilityServiceInfo.getResolveInfo();
            int -get22 = AccessibilityManagerService.sIdCounter;
            AccessibilityManagerService.sIdCounter = -get22 + 1;
            this.mId = -get22;
            this.mComponentName = componentName;
            this.mAccessibilityServiceInfo = accessibilityServiceInfo;
            this.mIsAutomation = AccessibilityManagerService.sFakeAccessibilityServiceComponentName.equals(componentName);
            if (!this.mIsAutomation) {
                this.mIntent = new Intent().setComponent(this.mComponentName);
                this.mIntent.putExtra("android.intent.extra.client_label", 17040508);
                long idendtity = Binder.clearCallingIdentity();
                try {
                    this.mIntent.putExtra("android.intent.extra.client_intent", PendingIntent.getActivity(AccessibilityManagerService.this.mContext, 0, new Intent("android.settings.ACCESSIBILITY_SETTINGS"), 0));
                } finally {
                    Binder.restoreCallingIdentity(idendtity);
                }
            }
            setDynamicallyConfigurableProperties(accessibilityServiceInfo);
        }

        public void setDynamicallyConfigurableProperties(AccessibilityServiceInfo info) {
            boolean z;
            boolean z2 = true;
            this.mEventTypes = info.eventTypes;
            this.mFeedbackType = info.feedbackType;
            String[] packageNames = info.packageNames;
            if (packageNames != null) {
                this.mPackageNames.addAll(Arrays.asList(packageNames));
            }
            this.mNotificationTimeout = info.notificationTimeout;
            if ((info.flags & 1) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIsDefault = z;
            if (this.mIsAutomation || info.getResolveInfo().serviceInfo.applicationInfo.targetSdkVersion >= 16) {
                if ((info.flags & 2) != 0) {
                    this.mFetchFlags |= 8;
                } else {
                    this.mFetchFlags &= -9;
                }
            }
            if ((info.flags & 16) != 0) {
                this.mFetchFlags |= 16;
            } else {
                this.mFetchFlags &= -17;
            }
            if ((info.flags & 4) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mRequestTouchExplorationMode = z;
            if ((info.flags & 8) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mRequestEnhancedWebAccessibility = z;
            if ((info.flags & 32) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mRequestFilterKeyEvents = z;
            if ((info.flags & 64) == 0) {
                z2 = false;
            }
            this.mRetrieveInteractiveWindows = z2;
        }

        public boolean bindLocked() {
            final UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
            if (this.mIsAutomation) {
                userState.mBindingServices.add(this.mComponentName);
                AccessibilityManagerService.this.mMainHandler.post(new Runnable() {
                    public void run() {
                        Service.this.onServiceConnected(Service.this.mComponentName, userState.mUiAutomationServiceClient.asBinder());
                    }
                });
                userState.mUiAutomationService = this;
            } else {
                long identity = Binder.clearCallingIdentity();
                try {
                    if (this.mService == null && AccessibilityManagerService.this.mContext.bindServiceAsUser(this.mIntent, this, 33554433, new UserHandle(this.mUserId))) {
                        userState.mBindingServices.add(this.mComponentName);
                    }
                    Binder.restoreCallingIdentity(identity);
                } catch (Throwable th) {
                    Binder.restoreCallingIdentity(identity);
                }
            }
            return false;
        }

        public boolean unbindLocked() {
            UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
            AccessibilityManagerService.this.getKeyEventDispatcher().flush(this);
            if (this.mIsAutomation) {
                userState.destroyUiAutomationService();
            } else {
                AccessibilityManagerService.this.mContext.unbindService(this);
            }
            AccessibilityManagerService.this.removeServiceLocked(this, userState);
            resetLocked();
            return true;
        }

        public void disableSelf() {
            synchronized (AccessibilityManagerService.this.mLock) {
                UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
                if (userState.mEnabledServices.remove(this.mComponentName)) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, this.mUserId);
                        Binder.restoreCallingIdentity(identity);
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
                }
            }
        }

        public boolean canReceiveEventsLocked() {
            return (this.mEventTypes == 0 || this.mFeedbackType == 0 || this.mService == null) ? false : true;
        }

        public void setOnKeyEventResult(boolean handled, int sequence) {
            AccessibilityManagerService.this.getKeyEventDispatcher().setOnKeyEventResult(this, handled, sequence);
        }

        public AccessibilityServiceInfo getServiceInfo() {
            AccessibilityServiceInfo accessibilityServiceInfo;
            synchronized (AccessibilityManagerService.this.mLock) {
                accessibilityServiceInfo = this.mAccessibilityServiceInfo;
            }
            return accessibilityServiceInfo;
        }

        public boolean canRetrieveInteractiveWindowsLocked() {
            if (AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowContentLocked(this)) {
                return this.mRetrieveInteractiveWindows;
            }
            return false;
        }

        public void setServiceInfo(AccessibilityServiceInfo info) {
            long identity = Binder.clearCallingIdentity();
            try {
                synchronized (AccessibilityManagerService.this.mLock) {
                    AccessibilityServiceInfo oldInfo = this.mAccessibilityServiceInfo;
                    if (oldInfo != null) {
                        oldInfo.updateDynamicallyConfigurableProperties(info);
                        setDynamicallyConfigurableProperties(oldInfo);
                    } else {
                        setDynamicallyConfigurableProperties(info);
                    }
                    AccessibilityManagerService.this.onUserStateChangedLocked(AccessibilityManagerService.this.getUserStateLocked(this.mUserId));
                }
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (this.mService != service) {
                    if (this.mService != null) {
                        this.mService.unlinkToDeath(this, 0);
                    }
                    this.mService = service;
                    try {
                        this.mService.linkToDeath(this, 0);
                    } catch (RemoteException e) {
                        Slog.e(AccessibilityManagerService.LOG_TAG, "Failed registering death link");
                        binderDied();
                        return;
                    }
                }
                this.mServiceInterface = IAccessibilityServiceClient.Stub.asInterface(service);
                UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
                AccessibilityManagerService.this.addServiceLocked(this, userState);
                if (userState.mBindingServices.contains(this.mComponentName) || this.mWasConnectedAndDied) {
                    userState.mBindingServices.remove(this.mComponentName);
                    this.mWasConnectedAndDied = false;
                    try {
                        this.mServiceInterface.init(this, this.mId, this.mOverlayWindowToken);
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    } catch (RemoteException re) {
                        Slog.w(AccessibilityManagerService.LOG_TAG, "Error while setting connection for service: " + service, re);
                        binderDied();
                    }
                } else {
                    binderDied();
                }
            }
            return;
        }

        private boolean isCalledForCurrentUserLocked() {
            return AccessibilityManagerService.this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(-2) == AccessibilityManagerService.this.mCurrentUserId;
        }

        public List<AccessibilityWindowInfo> getWindows() {
            AccessibilityManagerService.this.ensureWindowsAvailableTimed();
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return null;
                } else if (!AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowsLocked(this)) {
                    return null;
                } else if (AccessibilityManagerService.this.mSecurityPolicy.mWindows == null) {
                    return null;
                } else {
                    List<AccessibilityWindowInfo> windows = new ArrayList();
                    int windowCount = AccessibilityManagerService.this.mSecurityPolicy.mWindows.size();
                    for (int i = 0; i < windowCount; i++) {
                        AccessibilityWindowInfo windowClone = AccessibilityWindowInfo.obtain((AccessibilityWindowInfo) AccessibilityManagerService.this.mSecurityPolicy.mWindows.get(i));
                        windowClone.setConnectionId(this.mId);
                        windows.add(windowClone);
                    }
                    return windows;
                }
            }
        }

        public AccessibilityWindowInfo getWindow(int windowId) {
            AccessibilityManagerService.this.ensureWindowsAvailableTimed();
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return null;
                } else if (AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowsLocked(this)) {
                    AccessibilityWindowInfo window = AccessibilityManagerService.this.mSecurityPolicy.findWindowById(windowId);
                    if (window != null) {
                        AccessibilityWindowInfo windowClone = AccessibilityWindowInfo.obtain(window);
                        windowClone.setConnectionId(this.mId);
                        return windowClone;
                    }
                    return null;
                } else {
                    return null;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x0052, code:
            r11 = android.os.Binder.getCallingPid();
            r2 = android.os.Binder.getCallingUid();
            r16 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r15.getRemote().findAccessibilityNodeInfosByViewId(r23, r25, r7, r26, r27, r21.mFetchFlags, r11, r28, com.android.server.accessibility.AccessibilityManagerService.-wrap1(r21.this$0, r20));
            r3 = com.android.server.accessibility.AccessibilityManagerService.-get14(r21.this$0).computeValidReportedPackages(r2, r15.getPackageName(), r15.getUid());
     */
        /* JADX WARNING: Missing block: B:29:0x0090, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:30:0x0094, code:
            if (r7 == null) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:32:0x009e, code:
            if (android.os.Binder.isProxy(r15.getRemote()) == false) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:33:0x00a0, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:34:0x00a3, code:
            return r3;
     */
        /* JADX WARNING: Missing block: B:39:0x00a8, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:43:0x00b7, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:45:0x00bb, code:
            return null;
     */
        /* JADX WARNING: Missing block: B:46:0x00bc, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:47:0x00bd, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:51:0x00cc, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:52:0x00cf, code:
            throw r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String[] findAccessibilityNodeInfosByViewId(int accessibilityWindowId, long accessibilityNodeId, String viewIdResName, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            RemoteAccessibilityConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return null;
                            } else if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                        } else {
                            return null;
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x0052, code:
            r11 = android.os.Binder.getCallingPid();
            r2 = android.os.Binder.getCallingUid();
            r16 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r15.getRemote().findAccessibilityNodeInfosByText(r23, r25, r7, r26, r27, r21.mFetchFlags, r11, r28, com.android.server.accessibility.AccessibilityManagerService.-wrap1(r21.this$0, r20));
            r3 = com.android.server.accessibility.AccessibilityManagerService.-get14(r21.this$0).computeValidReportedPackages(r2, r15.getPackageName(), r15.getUid());
     */
        /* JADX WARNING: Missing block: B:29:0x0090, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:30:0x0094, code:
            if (r7 == null) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:32:0x009e, code:
            if (android.os.Binder.isProxy(r15.getRemote()) == false) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:33:0x00a0, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:34:0x00a3, code:
            return r3;
     */
        /* JADX WARNING: Missing block: B:39:0x00a8, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:43:0x00b7, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:45:0x00bb, code:
            return null;
     */
        /* JADX WARNING: Missing block: B:46:0x00bc, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:47:0x00bd, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:51:0x00cc, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:52:0x00cf, code:
            throw r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String[] findAccessibilityNodeInfosByText(int accessibilityWindowId, long accessibilityNodeId, String text, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            RemoteAccessibilityConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return null;
                            } else if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                        } else {
                            return null;
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x0052, code:
            r9 = android.os.Binder.getCallingPid();
            r13 = android.os.Binder.getCallingUid();
            r16 = android.os.Binder.clearCallingIdentity();
            r12 = com.android.server.accessibility.AccessibilityManagerService.-wrap1(r20.this$0, r19);
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r14.getRemote().findAccessibilityNodeInfoByAccessibilityId(r22, r5, r24, r25, r20.mFetchFlags | r26, r9, r27, r12);
            r2 = com.android.server.accessibility.AccessibilityManagerService.-get14(r20.this$0).computeValidReportedPackages(r13, r14.getPackageName(), r14.getUid());
     */
        /* JADX WARNING: Missing block: B:29:0x0090, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:30:0x0094, code:
            if (r5 == null) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:32:0x009e, code:
            if (android.os.Binder.isProxy(r14.getRemote()) == false) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:33:0x00a0, code:
            r5.recycle();
     */
        /* JADX WARNING: Missing block: B:34:0x00a3, code:
            return r2;
     */
        /* JADX WARNING: Missing block: B:39:0x00a8, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:43:0x00b7, code:
            r5.recycle();
     */
        /* JADX WARNING: Missing block: B:45:0x00bb, code:
            return null;
     */
        /* JADX WARNING: Missing block: B:46:0x00bc, code:
            r2 = move-exception;
     */
        /* JADX WARNING: Missing block: B:47:0x00bd, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:51:0x00cc, code:
            r5.recycle();
     */
        /* JADX WARNING: Missing block: B:52:0x00cf, code:
            throw r2;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String[] findAccessibilityNodeInfoByAccessibilityId(int accessibilityWindowId, long accessibilityNodeId, int interactionId, IAccessibilityInteractionConnectionCallback callback, int flags, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            RemoteAccessibilityConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return null;
                            } else if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                        } else {
                            return null;
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x0059, code:
            r13 = android.os.Binder.getCallingPid();
            r4 = android.os.Binder.getCallingUid();
            r18 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r17.getRemote().findFocus(r25, r27, r9, r28, r29, r23.mFetchFlags, r13, r30, com.android.server.accessibility.AccessibilityManagerService.-wrap1(r23.this$0, r22));
            r5 = com.android.server.accessibility.AccessibilityManagerService.-get14(r23.this$0).computeValidReportedPackages(r4, r17.getPackageName(), r17.getUid());
     */
        /* JADX WARNING: Missing block: B:29:0x0097, code:
            android.os.Binder.restoreCallingIdentity(r18);
     */
        /* JADX WARNING: Missing block: B:30:0x009b, code:
            if (r9 == null) goto L_0x00aa;
     */
        /* JADX WARNING: Missing block: B:32:0x00a5, code:
            if (android.os.Binder.isProxy(r17.getRemote()) == false) goto L_0x00aa;
     */
        /* JADX WARNING: Missing block: B:33:0x00a7, code:
            r9.recycle();
     */
        /* JADX WARNING: Missing block: B:34:0x00aa, code:
            return r5;
     */
        /* JADX WARNING: Missing block: B:39:0x00af, code:
            android.os.Binder.restoreCallingIdentity(r18);
     */
        /* JADX WARNING: Missing block: B:43:0x00be, code:
            r9.recycle();
     */
        /* JADX WARNING: Missing block: B:45:0x00c2, code:
            return null;
     */
        /* JADX WARNING: Missing block: B:46:0x00c3, code:
            r5 = move-exception;
     */
        /* JADX WARNING: Missing block: B:47:0x00c4, code:
            android.os.Binder.restoreCallingIdentity(r18);
     */
        /* JADX WARNING: Missing block: B:51:0x00d3, code:
            r9.recycle();
     */
        /* JADX WARNING: Missing block: B:52:0x00d6, code:
            throw r5;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String[] findFocus(int accessibilityWindowId, long accessibilityNodeId, int focusType, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdForFindFocusLocked(accessibilityWindowId, focusType);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            RemoteAccessibilityConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return null;
                            } else if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                        } else {
                            return null;
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:26:0x0052, code:
            r11 = android.os.Binder.getCallingPid();
            r2 = android.os.Binder.getCallingUid();
            r16 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:28:?, code:
            r15.getRemote().focusSearch(r23, r25, r7, r26, r27, r21.mFetchFlags, r11, r28, com.android.server.accessibility.AccessibilityManagerService.-wrap1(r21.this$0, r20));
            r3 = com.android.server.accessibility.AccessibilityManagerService.-get14(r21.this$0).computeValidReportedPackages(r2, r15.getPackageName(), r15.getUid());
     */
        /* JADX WARNING: Missing block: B:29:0x0090, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:30:0x0094, code:
            if (r7 == null) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:32:0x009e, code:
            if (android.os.Binder.isProxy(r15.getRemote()) == false) goto L_0x00a3;
     */
        /* JADX WARNING: Missing block: B:33:0x00a0, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:34:0x00a3, code:
            return r3;
     */
        /* JADX WARNING: Missing block: B:39:0x00a8, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:43:0x00b7, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:45:0x00bb, code:
            return null;
     */
        /* JADX WARNING: Missing block: B:46:0x00bc, code:
            r3 = move-exception;
     */
        /* JADX WARNING: Missing block: B:47:0x00bd, code:
            android.os.Binder.restoreCallingIdentity(r16);
     */
        /* JADX WARNING: Missing block: B:51:0x00cc, code:
            r7.recycle();
     */
        /* JADX WARNING: Missing block: B:52:0x00cf, code:
            throw r3;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public String[] focusSearch(int accessibilityWindowId, long accessibilityNodeId, int direction, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            Region partialInteractiveRegion = Region.obtain();
            synchronized (AccessibilityManagerService.this.mLock) {
                try {
                    if (isCalledForCurrentUserLocked()) {
                        int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                        if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                            RemoteAccessibilityConnection connection = getConnectionLocked(resolvedWindowId);
                            if (connection == null) {
                                return null;
                            } else if (!AccessibilityManagerService.this.mSecurityPolicy.computePartialInteractiveRegionForWindowLocked(resolvedWindowId, partialInteractiveRegion)) {
                                partialInteractiveRegion.recycle();
                                partialInteractiveRegion = null;
                            }
                        } else {
                            return null;
                        }
                    }
                    return null;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:28:?, code:
            r12.mServiceInterface.onPerformGestureResult(r13, false);
     */
        /* JADX WARNING: Missing block: B:35:0x008b, code:
            r4 = move-exception;
     */
        /* JADX WARNING: Missing block: B:36:0x008c, code:
            android.util.Slog.e(com.android.server.accessibility.AccessibilityManagerService.LOG_TAG, "Error sending motion event injection failure to " + r12.mServiceInterface, r4);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void sendGesture(int sequence, ParceledListSlice gestureSteps) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (AccessibilityManagerService.this.mSecurityPolicy.canPerformGestures(this)) {
                    long endMillis = SystemClock.uptimeMillis() + 1000;
                    while (AccessibilityManagerService.this.mMotionEventInjector == null && SystemClock.uptimeMillis() < endMillis) {
                        try {
                            AccessibilityManagerService.this.mLock.wait(endMillis - SystemClock.uptimeMillis());
                        } catch (InterruptedException e) {
                        }
                    }
                    if (AccessibilityManagerService.this.mMotionEventInjector != null) {
                        List<MotionEvent> events = MotionEventGenerator.getMotionEventsFromGestureSteps(gestureSteps.getList());
                        if (((MotionEvent) events.get(events.size() - 1)).getAction() == 1) {
                            AccessibilityManagerService.this.mMotionEventInjector.injectEvents(events, this.mServiceInterface, sequence);
                            return;
                        }
                        Slog.e(AccessibilityManagerService.LOG_TAG, "Gesture is not well-formed");
                    } else {
                        Slog.e(AccessibilityManagerService.LOG_TAG, "MotionEventInjector installation timed out");
                    }
                }
            }
        }

        /* JADX WARNING: Missing block: B:21:0x0039, code:
            r11 = android.os.Binder.getCallingPid();
            r14 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:23:?, code:
            com.android.server.accessibility.AccessibilityManagerService.-get13(r20.this$0).userActivity(android.os.SystemClock.uptimeMillis(), 3, 0);
            com.android.server.accessibility.AccessibilityManagerService.RemoteAccessibilityConnection.-get0(r2).performAccessibilityAction(r22, r24, r25, r26, r27, r20.mFetchFlags, r11, r28);
     */
        /* JADX WARNING: Missing block: B:25:0x006d, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:31:0x0077, code:
            android.os.Binder.restoreCallingIdentity(r14);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean performAccessibilityAction(int accessibilityWindowId, long accessibilityNodeId, int action, Bundle arguments, int interactionId, IAccessibilityInteractionConnectionCallback callback, long interrogatingTid) throws RemoteException {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (isCalledForCurrentUserLocked()) {
                    int resolvedWindowId = resolveAccessibilityWindowIdLocked(accessibilityWindowId);
                    if (AccessibilityManagerService.this.mSecurityPolicy.canGetAccessibilityNodeInfoLocked(this, resolvedWindowId)) {
                        RemoteAccessibilityConnection connection = getConnectionLocked(resolvedWindowId);
                        if (connection == null) {
                            return false;
                        }
                    }
                    return false;
                }
                return false;
            }
        }

        /* JADX WARNING: Missing block: B:8:0x0012, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:10:?, code:
            com.android.server.accessibility.AccessibilityManagerService.-get13(r9.this$0).userActivity(android.os.SystemClock.uptimeMillis(), 3, 0);
     */
        /* JADX WARNING: Missing block: B:11:0x0025, code:
            switch(r10) {
                case 1: goto L_0x002f;
                case 2: goto L_0x0037;
                case 3: goto L_0x003f;
                case 4: goto L_0x0046;
                case 5: goto L_0x004d;
                case 6: goto L_0x0054;
                case 7: goto L_0x005b;
                default: goto L_0x0028;
            };
     */
        /* JADX WARNING: Missing block: B:18:?, code:
            sendDownAndUpKeyEvents(4);
     */
        /* JADX WARNING: Missing block: B:19:0x0033, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:20:0x0036, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:23:?, code:
            sendDownAndUpKeyEvents(3);
     */
        /* JADX WARNING: Missing block: B:24:0x003b, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:25:0x003e, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:27:?, code:
            openRecents();
     */
        /* JADX WARNING: Missing block: B:28:0x0042, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:29:0x0045, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:31:?, code:
            expandNotifications();
     */
        /* JADX WARNING: Missing block: B:32:0x0049, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:33:0x004c, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:35:?, code:
            expandQuickSettings();
     */
        /* JADX WARNING: Missing block: B:36:0x0050, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:37:0x0053, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:39:?, code:
            showGlobalActions();
     */
        /* JADX WARNING: Missing block: B:40:0x0057, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:41:0x005a, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:43:?, code:
            toggleSplitScreen();
     */
        /* JADX WARNING: Missing block: B:44:0x005e, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:45:0x0061, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:47:0x0063, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean performGlobalAction(int action) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return false;
                }
            }
            return false;
        }

        /* JADX WARNING: Missing block: B:9:0x0012, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:?, code:
            r2 = r4.this$0.getMagnificationController().getScale();
     */
        /* JADX WARNING: Missing block: B:17:0x0028, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public float getMagnificationScale() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return 1.0f;
                }
            }
            return r2;
        }

        /* JADX WARNING: Missing block: B:21:0x0040, code:
            return r5;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public Region getMagnificationRegion() {
            synchronized (AccessibilityManagerService.this.mLock) {
                Region region = Region.obtain();
                if (isCalledForCurrentUserLocked()) {
                    MagnificationController magnificationController = AccessibilityManagerService.this.getMagnificationController();
                    boolean forceRegistration = AccessibilityManagerService.this.mSecurityPolicy.canControlMagnification(this);
                    boolean initiallyRegistered = magnificationController.isRegisteredLocked();
                    if (!initiallyRegistered && forceRegistration) {
                        magnificationController.register();
                    }
                    long identity = Binder.clearCallingIdentity();
                    try {
                        magnificationController.getMagnificationRegion(region);
                        Binder.restoreCallingIdentity(identity);
                        if (!initiallyRegistered && forceRegistration) {
                            magnificationController.unregister();
                        }
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                        if (!initiallyRegistered && forceRegistration) {
                            magnificationController.unregister();
                        }
                    }
                } else {
                    return region;
                }
            }
        }

        /* JADX WARNING: Missing block: B:9:0x0011, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:?, code:
            r2 = r4.this$0.getMagnificationController().getCenterX();
     */
        /* JADX WARNING: Missing block: B:17:0x0027, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public float getMagnificationCenterX() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return OppoBrightUtils.MIN_LUX_LIMITI;
                }
            }
            return r2;
        }

        /* JADX WARNING: Missing block: B:9:0x0011, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:?, code:
            r2 = r4.this$0.getMagnificationController().getCenterY();
     */
        /* JADX WARNING: Missing block: B:17:0x0027, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public float getMagnificationCenterY() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return OppoBrightUtils.MIN_LUX_LIMITI;
                }
            }
            return r2;
        }

        /* JADX WARNING: Missing block: B:13:0x001f, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:15:?, code:
            r3 = r6.this$0.getMagnificationController().reset(r7);
     */
        /* JADX WARNING: Missing block: B:21:0x0035, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean resetMagnification(boolean animate) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return false;
                } else if (!AccessibilityManagerService.this.mSecurityPolicy.canControlMagnification(this)) {
                    return false;
                }
            }
            return r3;
        }

        public boolean setMagnificationScaleAndCenter(float scale, float centerX, float centerY, boolean animate) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (!isCalledForCurrentUserLocked()) {
                    return false;
                } else if (AccessibilityManagerService.this.mSecurityPolicy.canControlMagnification(this)) {
                    long identity = Binder.clearCallingIdentity();
                    try {
                        MagnificationController magnificationController = AccessibilityManagerService.this.getMagnificationController();
                        if (!magnificationController.isRegisteredLocked()) {
                            magnificationController.register();
                        }
                        boolean scaleAndCenter = magnificationController.setScaleAndCenter(scale, centerX, centerY, animate, this.mId);
                        Binder.restoreCallingIdentity(identity);
                        return scaleAndCenter;
                    } catch (Throwable th) {
                        Binder.restoreCallingIdentity(identity);
                    }
                } else {
                    return false;
                }
            }
        }

        public void setMagnificationCallbackEnabled(boolean enabled) {
            this.mInvocationHandler.setMagnificationCallbackEnabled(enabled);
        }

        /* JADX WARNING: Missing block: B:10:0x0017, code:
            r0 = android.os.Binder.clearCallingIdentity();
     */
        /* JADX WARNING: Missing block: B:11:0x001b, code:
            if (r7 != 0) goto L_0x003a;
     */
        /* JADX WARNING: Missing block: B:14:?, code:
            r2.mServiceChangingSoftKeyboardMode = null;
     */
        /* JADX WARNING: Missing block: B:15:0x0020, code:
            android.provider.Settings.Secure.putIntForUser(com.android.server.accessibility.AccessibilityManagerService.-get2(r6.this$0).getContentResolver(), "accessibility_soft_keyboard_mode", r7, r2.mUserId);
     */
        /* JADX WARNING: Missing block: B:16:0x0032, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* JADX WARNING: Missing block: B:17:0x0036, code:
            return true;
     */
        /* JADX WARNING: Missing block: B:22:?, code:
            r2.mServiceChangingSoftKeyboardMode = r6.mComponentName;
     */
        /* JADX WARNING: Missing block: B:24:0x0040, code:
            android.os.Binder.restoreCallingIdentity(r0);
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean setSoftKeyboardShowMode(int showMode) {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (isCalledForCurrentUserLocked()) {
                    UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                } else {
                    return false;
                }
            }
        }

        public void setSoftKeyboardCallbackEnabled(boolean enabled) {
            this.mInvocationHandler.setSoftKeyboardCallbackEnabled(enabled);
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            AccessibilityManagerService.this.mSecurityPolicy.enforceCallingPermission("android.permission.DUMP", "dump");
            synchronized (AccessibilityManagerService.this.mLock) {
                pw.append("Service[label=" + this.mAccessibilityServiceInfo.getResolveInfo().loadLabel(AccessibilityManagerService.this.mContext.getPackageManager()));
                pw.append(", feedbackType" + AccessibilityServiceInfo.feedbackTypeToString(this.mFeedbackType));
                pw.append(", capabilities=" + this.mAccessibilityServiceInfo.getCapabilities());
                pw.append(", eventTypes=" + AccessibilityEvent.eventTypeToString(this.mEventTypes));
                pw.append(", notificationTimeout=" + this.mNotificationTimeout);
                pw.append("]");
            }
        }

        public void onServiceDisconnected(ComponentName componentName) {
            binderDied();
        }

        public void onAdded() throws RemoteException {
            long identity = Binder.clearCallingIdentity();
            try {
                AccessibilityManagerService.this.mWindowManagerService.addWindowToken(this.mOverlayWindowToken, 2032);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void onRemoved() {
            long identity = Binder.clearCallingIdentity();
            try {
                AccessibilityManagerService.this.mWindowManagerService.removeWindowToken(this.mOverlayWindowToken, true);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }

        public void resetLocked() {
            if (AccessibilityManagerService.IS_ENG_BUILD) {
                Slog.d(AccessibilityManagerService.LOG_TAG, "resetLocked()", new Throwable("resetLocked()"));
            }
            try {
                if (this.mServiceInterface != null) {
                    this.mServiceInterface.init(null, this.mId, null);
                }
            } catch (RemoteException e) {
            }
            if (this.mService != null) {
                this.mService.unlinkToDeath(this, 0);
                this.mService = null;
            }
            this.mServiceInterface = null;
        }

        public boolean isConnectedLocked() {
            return this.mService != null;
        }

        public void binderDied() {
            synchronized (AccessibilityManagerService.this.mLock) {
                if (isConnectedLocked()) {
                    this.mWasConnectedAndDied = true;
                    AccessibilityManagerService.this.getKeyEventDispatcher().flush(this);
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(this.mUserId);
                    resetLocked();
                    if (this.mIsAutomation) {
                        AccessibilityManagerService.this.removeServiceLocked(this, userState);
                        userState.mInstalledServices.remove(this.mAccessibilityServiceInfo);
                        userState.mEnabledServices.remove(this.mComponentName);
                        userState.destroyUiAutomationService();
                        AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState);
                    }
                    if (this.mId == AccessibilityManagerService.this.getMagnificationController().getIdOfLastServiceToMagnify()) {
                        AccessibilityManagerService.this.getMagnificationController().resetIfNeeded(true);
                    }
                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    return;
                }
            }
        }

        public void notifyAccessibilityEvent(AccessibilityEvent event) {
            synchronized (AccessibilityManagerService.this.mLock) {
                Message message;
                int eventType = event.getEventType();
                AccessibilityEvent newEvent = AccessibilityEvent.obtain(event);
                if (this.mNotificationTimeout <= 0 || eventType == 2048) {
                    message = this.mEventDispatchHandler.obtainMessage(eventType, newEvent);
                } else {
                    AccessibilityEvent oldEvent = (AccessibilityEvent) this.mPendingEvents.get(eventType);
                    this.mPendingEvents.put(eventType, newEvent);
                    if (oldEvent != null) {
                        this.mEventDispatchHandler.removeMessages(eventType);
                        oldEvent.recycle();
                    }
                    message = this.mEventDispatchHandler.obtainMessage(eventType);
                }
                this.mEventDispatchHandler.sendMessageDelayed(message, this.mNotificationTimeout);
            }
        }

        /* JADX WARNING: Missing block: B:21:?, code:
            r1.onAccessibilityEvent(r8);
     */
        /* JADX WARNING: Missing block: B:22:0x003e, code:
            return;
     */
        /* JADX WARNING: Missing block: B:29:0x0047, code:
            r2 = move-exception;
     */
        /* JADX WARNING: Missing block: B:31:?, code:
            android.util.Slog.e(com.android.server.accessibility.AccessibilityManagerService.LOG_TAG, "Error during sending " + r8 + " to " + r1, r2);
     */
        /* JADX WARNING: Missing block: B:33:0x0072, code:
            r8.recycle();
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void notifyAccessibilityEventInternal(int eventType, AccessibilityEvent event) {
            synchronized (AccessibilityManagerService.this.mLock) {
                IAccessibilityServiceClient listener = this.mServiceInterface;
                if (listener == null) {
                    return;
                }
                if (event == null) {
                    event = (AccessibilityEvent) this.mPendingEvents.get(eventType);
                    if (event == null) {
                        return;
                    }
                    this.mPendingEvents.remove(eventType);
                }
                if (AccessibilityManagerService.this.mSecurityPolicy.canRetrieveWindowContentLocked(this)) {
                    event.setConnectionId(this.mId);
                } else {
                    event.setSource(null);
                }
                event.setSealed(true);
            }
        }

        public void notifyGesture(int gestureId) {
            this.mInvocationHandler.obtainMessage(1, gestureId, 0).sendToTarget();
        }

        public void notifyClearAccessibilityNodeInfoCache() {
            this.mInvocationHandler.sendEmptyMessage(2);
        }

        public void notifyMagnificationChangedLocked(Region region, float scale, float centerX, float centerY) {
            this.mInvocationHandler.notifyMagnificationChangedLocked(region, scale, centerX, centerY);
        }

        public void notifySoftKeyboardShowModeChangedLocked(int showState) {
            this.mInvocationHandler.notifySoftKeyboardShowModeChangedLocked(showState);
        }

        private void notifyMagnificationChangedInternal(Region region, float scale, float centerX, float centerY) {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.onMagnificationChanged(region, scale, centerX, centerY);
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error sending magnification changes to " + this.mService, re);
                }
            }
        }

        private void notifySoftKeyboardShowModeChangedInternal(int showState) {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.onSoftKeyboardShowModeChanged(showState);
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error sending soft keyboard show mode changes to " + this.mService, re);
                }
            }
        }

        private void notifyGestureInternal(int gestureId) {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.onGesture(gestureId);
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error during sending gesture " + gestureId + " to " + this.mService, re);
                }
            }
        }

        private void notifyClearAccessibilityCacheInternal() {
            IAccessibilityServiceClient listener;
            synchronized (AccessibilityManagerService.this.mLock) {
                listener = this.mServiceInterface;
            }
            if (listener != null) {
                try {
                    listener.clearAccessibilityCache();
                } catch (RemoteException re) {
                    Slog.e(AccessibilityManagerService.LOG_TAG, "Error during requesting accessibility info cache to be cleared.", re);
                }
            }
        }

        private void sendDownAndUpKeyEvents(int keyCode) {
            long token = Binder.clearCallingIdentity();
            long downTime = SystemClock.uptimeMillis();
            KeyEvent down = KeyEvent.obtain(downTime, downTime, 0, keyCode, 0, 0, -1, 0, 8, 257, null);
            InputManager.getInstance().injectInputEvent(down, 0);
            down.recycle();
            InputEvent up = KeyEvent.obtain(downTime, SystemClock.uptimeMillis(), 1, keyCode, 0, 0, -1, 0, 8, 257, null);
            InputManager.getInstance().injectInputEvent(up, 0);
            up.recycle();
            Binder.restoreCallingIdentity(token);
        }

        private void expandNotifications() {
            long token = Binder.clearCallingIdentity();
            ((StatusBarManager) AccessibilityManagerService.this.mContext.getSystemService("statusbar")).expandNotificationsPanel();
            Binder.restoreCallingIdentity(token);
        }

        private void expandQuickSettings() {
            long token = Binder.clearCallingIdentity();
            ((StatusBarManager) AccessibilityManagerService.this.mContext.getSystemService("statusbar")).expandSettingsPanel();
            Binder.restoreCallingIdentity(token);
        }

        private void openRecents() {
            long token = Binder.clearCallingIdentity();
            ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleRecentApps();
            Binder.restoreCallingIdentity(token);
        }

        private void showGlobalActions() {
            AccessibilityManagerService.this.mWindowManagerService.showGlobalActions();
        }

        private void toggleSplitScreen() {
            ((StatusBarManagerInternal) LocalServices.getService(StatusBarManagerInternal.class)).toggleSplitScreen();
        }

        private RemoteAccessibilityConnection getConnectionLocked(int windowId) {
            RemoteAccessibilityConnection wrapper = (RemoteAccessibilityConnection) AccessibilityManagerService.this.mGlobalInteractionConnections.get(windowId);
            if (wrapper == null) {
                wrapper = (RemoteAccessibilityConnection) AccessibilityManagerService.this.getCurrentUserStateLocked().mInteractionConnections.get(windowId);
            }
            if (wrapper == null || wrapper.mConnection == null) {
                return null;
            }
            return wrapper;
        }

        private int resolveAccessibilityWindowIdLocked(int accessibilityWindowId) {
            if (accessibilityWindowId == Integer.MAX_VALUE) {
                return AccessibilityManagerService.this.mSecurityPolicy.getActiveWindowId();
            }
            return accessibilityWindowId;
        }

        private int resolveAccessibilityWindowIdForFindFocusLocked(int windowId, int focusType) {
            if (windowId == Integer.MAX_VALUE) {
                return AccessibilityManagerService.this.mSecurityPolicy.mActiveWindowId;
            }
            if (windowId == -2) {
                if (focusType == 1) {
                    return AccessibilityManagerService.this.mSecurityPolicy.mFocusedWindowId;
                }
                if (focusType == 2) {
                    return AccessibilityManagerService.this.mSecurityPolicy.mAccessibilityFocusedWindowId;
                }
            }
            return windowId;
        }
    }

    private class SettingsStringHelper {
        private static final String SETTINGS_DELIMITER = ":";
        private ContentResolver mContentResolver;
        private Set<String> mServices = new HashSet();
        private final String mSettingsName;
        private final int mUserId;

        public SettingsStringHelper(String name, int userId) {
            this.mUserId = userId;
            this.mSettingsName = name;
            this.mContentResolver = AccessibilityManagerService.this.mContext.getContentResolver();
            String servicesString = Secure.getStringForUser(this.mContentResolver, this.mSettingsName, userId);
            if (!TextUtils.isEmpty(servicesString)) {
                SimpleStringSplitter colonSplitter = new SimpleStringSplitter(SETTINGS_DELIMITER.charAt(0));
                colonSplitter.setString(servicesString);
                while (colonSplitter.hasNext()) {
                    this.mServices.add(colonSplitter.next());
                }
            }
        }

        public void addService(ComponentName component) {
            this.mServices.add(component.flattenToString());
        }

        public void deleteService(ComponentName component) {
            this.mServices.remove(component.flattenToString());
        }

        public void writeToSettings() {
            Secure.putStringForUser(this.mContentResolver, this.mSettingsName, TextUtils.join(SETTINGS_DELIMITER, this.mServices), this.mUserId);
        }
    }

    private class UserState {
        public boolean mAccessibilityFocusOnlyInActiveWindow;
        public final Set<ComponentName> mBindingServices = new HashSet();
        public final CopyOnWriteArrayList<Service> mBoundServices = new CopyOnWriteArrayList();
        public final RemoteCallbackList<IAccessibilityManagerClient> mClients = new RemoteCallbackList();
        public final Map<ComponentName, Service> mComponentNameToServiceMap = new HashMap();
        public final Set<ComponentName> mEnabledServices = new HashSet();
        public final List<AccessibilityServiceInfo> mInstalledServices = new ArrayList();
        public final SparseArray<RemoteAccessibilityConnection> mInteractionConnections = new SparseArray();
        public boolean mIsAutoclickEnabled;
        public boolean mIsDisplayMagnificationEnabled;
        public boolean mIsEnhancedWebAccessibilityEnabled;
        public boolean mIsFilterKeyEventsEnabled;
        public boolean mIsPerformGesturesEnabled;
        public boolean mIsTextHighContrastEnabled;
        public boolean mIsTouchExplorationEnabled;
        public int mLastSentClientState = -1;
        public ComponentName mServiceChangingSoftKeyboardMode;
        public int mSoftKeyboardShowMode = 0;
        public final Set<ComponentName> mTouchExplorationGrantedServices = new HashSet();
        private int mUiAutomationFlags;
        private final DeathRecipient mUiAutomationSerivceOnwerDeathRecipient = new DeathRecipient() {
            public void binderDied() {
                UserState.this.mUiAutomationServiceOwner.unlinkToDeath(UserState.this.mUiAutomationSerivceOnwerDeathRecipient, 0);
                UserState.this.mUiAutomationServiceOwner = null;
                if (UserState.this.mUiAutomationService != null) {
                    UserState.this.mUiAutomationService.binderDied();
                }
            }
        };
        private Service mUiAutomationService;
        private IAccessibilityServiceClient mUiAutomationServiceClient;
        private IBinder mUiAutomationServiceOwner;
        public final int mUserId;
        public final SparseArray<IBinder> mWindowTokens = new SparseArray();

        public UserState(int userId) {
            this.mUserId = userId;
        }

        public int getClientState() {
            int clientState = 0;
            if (isHandlingAccessibilityEvents()) {
                clientState = 1;
            }
            if (isHandlingAccessibilityEvents() && this.mIsTouchExplorationEnabled) {
                clientState |= 2;
            }
            if (this.mIsTextHighContrastEnabled) {
                return clientState | 4;
            }
            return clientState;
        }

        public boolean isHandlingAccessibilityEvents() {
            return (this.mBoundServices.isEmpty() && this.mBindingServices.isEmpty()) ? false : true;
        }

        public void onSwitchToAnotherUser() {
            if (this.mUiAutomationService != null) {
                this.mUiAutomationService.binderDied();
            }
            AccessibilityManagerService.this.unbindAllServicesLocked(this);
            this.mBoundServices.clear();
            this.mBindingServices.clear();
            this.mLastSentClientState = -1;
            this.mEnabledServices.clear();
            this.mTouchExplorationGrantedServices.clear();
            this.mIsTouchExplorationEnabled = false;
            this.mIsEnhancedWebAccessibilityEnabled = false;
            this.mIsDisplayMagnificationEnabled = false;
            this.mIsAutoclickEnabled = false;
            this.mSoftKeyboardShowMode = 0;
        }

        public void destroyUiAutomationService() {
            this.mUiAutomationService = null;
            this.mUiAutomationFlags = 0;
            this.mUiAutomationServiceClient = null;
            if (this.mUiAutomationServiceOwner != null) {
                this.mUiAutomationServiceOwner.unlinkToDeath(this.mUiAutomationSerivceOnwerDeathRecipient, 0);
                this.mUiAutomationServiceOwner = null;
            }
        }

        boolean isUiAutomationSuppressingOtherServices() {
            return this.mUiAutomationService != null && (this.mUiAutomationFlags & 1) == 0;
        }
    }

    final class WindowsForAccessibilityCallback implements android.view.WindowManagerInternal.WindowsForAccessibilityCallback {
        WindowsForAccessibilityCallback() {
        }

        public void onWindowsForAccessibilityChanged(List<WindowInfo> windows) {
            synchronized (AccessibilityManagerService.this.mLock) {
                List<AccessibilityWindowInfo> reportedWindows = new ArrayList();
                int receivedWindowCount = windows.size();
                for (int i = 0; i < receivedWindowCount; i++) {
                    AccessibilityWindowInfo reportedWindow = populateReportedWindow((WindowInfo) windows.get(i));
                    if (reportedWindow != null) {
                        reportedWindows.add(reportedWindow);
                    }
                }
                AccessibilityManagerService.this.mSecurityPolicy.updateWindowsLocked(reportedWindows);
                AccessibilityManagerService.this.mLock.notifyAll();
            }
        }

        private AccessibilityWindowInfo populateReportedWindow(WindowInfo window) {
            int windowId = AccessibilityManagerService.this.findWindowIdLocked(window.token);
            if (windowId < 0) {
                return null;
            }
            AccessibilityWindowInfo reportedWindow = AccessibilityWindowInfo.obtain();
            reportedWindow.setId(windowId);
            reportedWindow.setType(getTypeForWindowManagerWindowType(window.type));
            reportedWindow.setLayer(window.layer);
            reportedWindow.setFocused(window.focused);
            reportedWindow.setBoundsInScreen(window.boundsInScreen);
            reportedWindow.setTitle(window.title);
            reportedWindow.setAnchorId(window.accessibilityIdOfAnchor);
            int parentId = AccessibilityManagerService.this.findWindowIdLocked(window.parentToken);
            if (parentId >= 0) {
                reportedWindow.setParentId(parentId);
            }
            if (window.childTokens != null) {
                int childCount = window.childTokens.size();
                for (int i = 0; i < childCount; i++) {
                    int childId = AccessibilityManagerService.this.findWindowIdLocked((IBinder) window.childTokens.get(i));
                    if (childId >= 0) {
                        reportedWindow.addChild(childId);
                    }
                }
            }
            return reportedWindow;
        }

        private int getTypeForWindowManagerWindowType(int windowType) {
            switch (windowType) {
                case 1:
                case 2:
                case 3:
                case 4:
                case 1000:
                case 1001:
                case 1002:
                case 1003:
                case 1005:
                case 2002:
                case 2005:
                case 2007:
                    return 1;
                case 2000:
                case 2001:
                case 2003:
                case 2006:
                case 2008:
                case 2009:
                case 2010:
                case 2014:
                case 2017:
                case 2019:
                case 2020:
                case 2024:
                case 2036:
                    return 3;
                case 2011:
                case 2012:
                    return 2;
                case 2032:
                    return 4;
                case 2034:
                    return 5;
                default:
                    return -1;
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.accessibility.AccessibilityManagerService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.accessibility.AccessibilityManagerService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.accessibility.AccessibilityManagerService.<clinit>():void");
    }

    private UserState getCurrentUserStateLocked() {
        return getUserStateLocked(this.mCurrentUserId);
    }

    public AccessibilityManagerService(Context context) {
        this.mLock = new Object();
        this.mStringColonSplitter = new SimpleStringSplitter(COMPONENT_NAME_SEPARATOR);
        this.mEnabledServicesForFeedbackTempList = new ArrayList();
        this.mTempRect = new Rect();
        this.mTempRect1 = new Rect();
        this.mTempPoint = new Point();
        this.mTempComponentNameSet = new HashSet();
        this.mTempAccessibilityServiceInfoList = new ArrayList();
        this.mGlobalClients = new RemoteCallbackList();
        this.mGlobalInteractionConnections = new SparseArray();
        this.mGlobalWindowTokens = new SparseArray();
        this.mUserStates = new SparseArray();
        this.mCurrentUserId = 0;
        this.mNeedShowMagnificationDialog = true;
        this.mContext = context;
        this.mPackageManager = this.mContext.getPackageManager();
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mWindowManagerService = (WindowManagerInternal) LocalServices.getService(WindowManagerInternal.class);
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mSecurityPolicy = new SecurityPolicy();
        this.mMainHandler = new MainHandler(this.mContext.getMainLooper());
        registerBroadcastReceivers();
        new AccessibilityContentObserver(this.mMainHandler).register(context.getContentResolver());
        getMagnificationController().register();
        if (SystemProperties.get("ro.mtk_ipo_support").equals(LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON)) {
            registerIPOReceiver(context);
        }
    }

    private UserState getUserStateLocked(int userId) {
        UserState state = (UserState) this.mUserStates.get(userId);
        if (state != null) {
            return state;
        }
        state = new UserState(userId);
        this.mUserStates.put(userId, state);
        return state;
    }

    public void setMagnificationDialogEnable(boolean enable) {
        this.mNeedShowMagnificationDialog = enable;
    }

    public boolean getMagnificationDialogEnable() {
        return this.mNeedShowMagnificationDialog;
    }

    private void registerBroadcastReceivers() {
        new PackageMonitor() {
            /* JADX WARNING: Missing block: B:14:0x0034, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onSomePackagesChanged() {
                synchronized (AccessibilityManagerService.this.mLock) {
                    if (getChangingUserId() != AccessibilityManagerService.this.mCurrentUserId) {
                        return;
                    }
                    UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                    userState.mInstalledServices.clear();
                    if (!userState.isUiAutomationSuppressingOtherServices() && AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState)) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                }
            }

            /* JADX WARNING: Missing block: B:17:0x0049, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPackageUpdateFinished(String packageName, int uid) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                        return;
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    boolean unboundAService = false;
                    for (int i = userState.mBoundServices.size() - 1; i >= 0; i--) {
                        Service boundService = (Service) userState.mBoundServices.get(i);
                        if (boundService.mComponentName.getPackageName().equals(packageName)) {
                            boundService.unbindLocked();
                            unboundAService = true;
                        }
                    }
                    if (unboundAService) {
                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                    }
                }
            }

            /* JADX WARNING: Missing block: B:17:0x005f, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onPackageRemoved(String packageName, int uid) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                        return;
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    Iterator<ComponentName> it = userState.mEnabledServices.iterator();
                    while (it.hasNext()) {
                        ComponentName comp = (ComponentName) it.next();
                        if (comp.getPackageName().equals(packageName)) {
                            it.remove();
                            AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userId);
                            userState.mTouchExplorationGrantedServices.remove(comp);
                            AccessibilityManagerService.this.persistComponentNamesToSettingLocked("touch_exploration_granted_accessibility_services", userState.mTouchExplorationGrantedServices, userId);
                            if (!userState.isUiAutomationSuppressingOtherServices()) {
                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                            }
                        }
                    }
                }
            }

            public boolean onHandleForceStop(Intent intent, String[] packages, int uid, boolean doit) {
                synchronized (AccessibilityManagerService.this.mLock) {
                    int userId = getChangingUserId();
                    if (userId != AccessibilityManagerService.this.mCurrentUserId) {
                        return false;
                    }
                    UserState userState = AccessibilityManagerService.this.getUserStateLocked(userId);
                    Iterator<ComponentName> it = userState.mEnabledServices.iterator();
                    while (it.hasNext()) {
                        String compPkg = ((ComponentName) it.next()).getPackageName();
                        for (String pkg : packages) {
                            if (compPkg.equals(pkg)) {
                                if (doit) {
                                    it.remove();
                                    AccessibilityManagerService.this.persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, userId);
                                    if (!userState.isUiAutomationSuppressingOtherServices()) {
                                        AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                                    }
                                } else {
                                    return true;
                                }
                            }
                        }
                    }
                    return false;
                }
            }
        }.register(this.mContext, null, UserHandle.ALL, true);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.USER_SWITCHED");
        intentFilter.addAction("android.intent.action.USER_UNLOCKED");
        intentFilter.addAction("android.intent.action.USER_REMOVED");
        intentFilter.addAction("android.intent.action.USER_PRESENT");
        intentFilter.addAction("android.os.action.SETTING_RESTORED");
        intentFilter.addAction("android.intent.action.SCREEN_ON");
        this.mContext.registerReceiverAsUser(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                if ("android.intent.action.USER_SWITCHED".equals(action)) {
                    AccessibilityManagerService.this.switchUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_UNLOCKED".equals(action)) {
                    AccessibilityManagerService.this.unlockUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else if ("android.intent.action.USER_REMOVED".equals(action)) {
                    AccessibilityManagerService.this.removeUser(intent.getIntExtra("android.intent.extra.user_handle", 0));
                } else {
                    Object -get8;
                    if ("android.intent.action.USER_PRESENT".equals(action)) {
                        -get8 = AccessibilityManagerService.this.mLock;
                        synchronized (-get8) {
                            UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                            if (!userState.isUiAutomationSuppressingOtherServices() && AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState)) {
                                AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                            }
                        }
                    } else if ("android.intent.action.SCREEN_ON".equals(action)) {
                        -get8 = AccessibilityManagerService.this.mLock;
                        synchronized (-get8) {
                            Slog.i("DisplayAdjustmentUtils", "action = " + action);
                            DisplayAdjustmentUtils.applyAdjustValuesRGB(AccessibilityManagerService.this.mContext, AccessibilityManagerService.this.getCurrentUserStateLocked().mUserId);
                            AccessibilityManagerService.this.mMainHandler.sendEmptyMessageDelayed(10, 400);
                        }
                    } else if ("android.os.action.SETTING_RESTORED".equals(action)) {
                        if ("enabled_accessibility_services".equals(intent.getStringExtra("setting_name"))) {
                            -get8 = AccessibilityManagerService.this.mLock;
                            synchronized (-get8) {
                                AccessibilityManagerService.this.restoreEnabledAccessibilityServicesLocked(intent.getStringExtra("previous_value"), intent.getStringExtra("new_value"));
                            }
                        } else {
                            return;
                        }
                    } else {
                        return;
                    }
                }
            }
        }, UserHandle.ALL, intentFilter, null, null);
    }

    /* JADX WARNING: Missing block: B:17:0x003a, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int addClient(IAccessibilityManagerClient client, int userId) {
        int i = 0;
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            UserState userState = getUserStateLocked(resolvedUserId);
            if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(userId)) {
                this.mGlobalClients.register(client);
                i = userState.getClientState();
                return i;
            }
            if (userId == OppoMultiAppManager.USER_ID) {
                userState = getUserStateLocked(0);
            }
            userState.mClients.register(client);
            if (resolvedUserId == this.mCurrentUserId || resolvedUserId == OppoMultiAppManager.USER_ID) {
                i = userState.getClientState();
            }
        }
    }

    /* JADX WARNING: Missing block: B:21:0x0070, code:
            if (OWN_PROCESS_ID == android.os.Binder.getCallingPid()) goto L_0x0077;
     */
    /* JADX WARNING: Missing block: B:22:0x0072, code:
            r0 = true;
     */
    /* JADX WARNING: Missing block: B:23:0x0073, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:27:0x0077, code:
            r0 = false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean sendAccessibilityEvent(AccessibilityEvent event, int userId) {
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            event.setPackageName(this.mSecurityPolicy.resolveValidReportedPackageLocked(event.getPackageName(), UserHandle.getCallingAppId(), resolvedUserId));
            if (resolvedUserId == this.mCurrentUserId || resolvedUserId == OppoMultiAppManager.USER_ID) {
                if (this.mSecurityPolicy.canDispatchAccessibilityEventLocked(event)) {
                    this.mSecurityPolicy.updateActiveAndAccessibilityFocusedWindowLocked(event.getWindowId(), event.getSourceNodeId(), event.getEventType(), event.getAction());
                    this.mSecurityPolicy.updateEventSourceLocked(event);
                    notifyAccessibilityServicesDelayedLocked(event, false);
                    notifyAccessibilityServicesDelayedLocked(event, true);
                }
                if (this.mHasInputFilter && this.mInputFilter != null) {
                    this.mMainHandler.obtainMessage(1, AccessibilityEvent.obtain(event)).sendToTarget();
                }
                event.recycle();
            } else {
                return true;
            }
        }
    }

    public List<AccessibilityServiceInfo> getInstalledAccessibilityServiceList(int userId) {
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId));
            if (userState.mUiAutomationService != null) {
                List<AccessibilityServiceInfo> installedServices = new ArrayList();
                installedServices.addAll(userState.mInstalledServices);
                installedServices.remove(userState.mUiAutomationService.mAccessibilityServiceInfo);
                return installedServices;
            }
            List<AccessibilityServiceInfo> list = userState.mInstalledServices;
            return list;
        }
    }

    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackType, int userId) {
        synchronized (this.mLock) {
            UserState userState = getUserStateLocked(this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId));
            if (userState.isUiAutomationSuppressingOtherServices()) {
                List<AccessibilityServiceInfo> emptyList = Collections.emptyList();
                return emptyList;
            }
            List<AccessibilityServiceInfo> result = this.mEnabledServicesForFeedbackTempList;
            result.clear();
            List<Service> services = userState.mBoundServices;
            while (feedbackType != 0) {
                int feedbackTypeBit = 1 << Integer.numberOfTrailingZeros(feedbackType);
                feedbackType &= ~feedbackTypeBit;
                int serviceCount = services.size();
                for (int i = 0; i < serviceCount; i++) {
                    Service service = (Service) services.get(i);
                    if (!(sFakeAccessibilityServiceComponentName.equals(service.mComponentName) || (service.mFeedbackType & feedbackTypeBit) == 0)) {
                        result.add(service.mAccessibilityServiceInfo);
                    }
                }
            }
            return result;
        }
    }

    /* JADX WARNING: Missing block: B:10:0x0016, code:
            r1 = 0;
            r0 = r5.size();
     */
    /* JADX WARNING: Missing block: B:11:0x001b, code:
            if (r1 >= r0) goto L_0x004c;
     */
    /* JADX WARNING: Missing block: B:12:0x001d, code:
            r4 = (com.android.server.accessibility.AccessibilityManagerService.Service) r5.get(r1);
     */
    /* JADX WARNING: Missing block: B:14:?, code:
            r4.mServiceInterface.onInterrupt();
     */
    /* JADX WARNING: Missing block: B:19:0x002e, code:
            r2 = move-exception;
     */
    /* JADX WARNING: Missing block: B:20:0x002f, code:
            android.util.Slog.e(LOG_TAG, "Error during sending interrupt request to " + r4.mService, r2);
     */
    /* JADX WARNING: Missing block: B:21:0x004c, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void interrupt(int userId) {
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            if (resolvedUserId != this.mCurrentUserId) {
                return;
            }
            CopyOnWriteArrayList<Service> services = getUserStateLocked(resolvedUserId).mBoundServices;
        }
        int i++;
    }

    public int addAccessibilityInteractionConnection(IWindow windowToken, IAccessibilityInteractionConnection connection, String packageName, int userId) throws RemoteException {
        int windowId;
        synchronized (this.mLock) {
            int resolvedUserId = this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId);
            int resolvedUid = UserHandle.getUid(resolvedUserId, UserHandle.getCallingAppId());
            packageName = this.mSecurityPolicy.resolveValidReportedPackageLocked(packageName, UserHandle.getCallingAppId(), resolvedUserId);
            windowId = sNextWindowId;
            sNextWindowId = windowId + 1;
            RemoteAccessibilityConnection wrapper;
            if (this.mSecurityPolicy.isCallerInteractingAcrossUsers(userId)) {
                wrapper = new RemoteAccessibilityConnection(windowId, connection, packageName, resolvedUid, -1);
                wrapper.linkToDeath();
                this.mGlobalInteractionConnections.put(windowId, wrapper);
                this.mGlobalWindowTokens.put(windowId, windowToken.asBinder());
            } else {
                wrapper = new RemoteAccessibilityConnection(windowId, connection, packageName, resolvedUid, resolvedUserId);
                wrapper.linkToDeath();
                UserState userState = getUserStateLocked(resolvedUserId);
                if (userId == OppoMultiAppManager.USER_ID) {
                    userState = getUserStateLocked(0);
                }
                userState.mInteractionConnections.put(windowId, wrapper);
                userState.mWindowTokens.put(windowId, windowToken.asBinder());
            }
        }
        return windowId;
    }

    public void removeAccessibilityInteractionConnection(IWindow window) {
        synchronized (this.mLock) {
            this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(UserHandle.getCallingUserId());
            IBinder token = window.asBinder();
            if (removeAccessibilityInteractionConnectionInternalLocked(token, this.mGlobalWindowTokens, this.mGlobalInteractionConnections) >= 0) {
                return;
            }
            int userCount = this.mUserStates.size();
            for (int i = 0; i < userCount; i++) {
                UserState userState = (UserState) this.mUserStates.valueAt(i);
                if (removeAccessibilityInteractionConnectionInternalLocked(token, userState.mWindowTokens, userState.mInteractionConnections) >= 0) {
                    return;
                }
            }
        }
    }

    private int removeAccessibilityInteractionConnectionInternalLocked(IBinder windowToken, SparseArray<IBinder> windowTokens, SparseArray<RemoteAccessibilityConnection> interactionConnections) {
        int count = windowTokens.size();
        for (int i = 0; i < count; i++) {
            if (windowTokens.valueAt(i) == windowToken) {
                int windowId = windowTokens.keyAt(i);
                windowTokens.removeAt(i);
                ((RemoteAccessibilityConnection) interactionConnections.get(windowId)).unlinkToDeath();
                interactionConnections.remove(windowId);
                return windowId;
            }
        }
        return -1;
    }

    public void registerUiTestAutomationService(IBinder owner, IAccessibilityServiceClient serviceClient, AccessibilityServiceInfo accessibilityServiceInfo, int flags) {
        if (IS_ENG_BUILD) {
            Slog.d(LOG_TAG, "registerUiTestAutomationService begins");
        }
        this.mSecurityPolicy.enforceCallingPermission("android.permission.RETRIEVE_WINDOW_CONTENT", FUNCTION_REGISTER_UI_TEST_AUTOMATION_SERVICE);
        accessibilityServiceInfo.setComponentName(sFakeAccessibilityServiceComponentName);
        synchronized (this.mLock) {
            UserState userState = getCurrentUserStateLocked();
            if (userState.mUiAutomationService != null) {
                throw new IllegalStateException("UiAutomationService " + serviceClient + "already registered!");
            }
            try {
                owner.linkToDeath(userState.mUiAutomationSerivceOnwerDeathRecipient, 0);
                userState.mUiAutomationServiceOwner = owner;
                userState.mUiAutomationServiceClient = serviceClient;
                userState.mUiAutomationFlags = flags;
                userState.mInstalledServices.add(accessibilityServiceInfo);
                if ((flags & 1) == 0) {
                    userState.mIsTouchExplorationEnabled = false;
                    userState.mIsEnhancedWebAccessibilityEnabled = false;
                    userState.mIsDisplayMagnificationEnabled = false;
                    userState.mIsAutoclickEnabled = false;
                    userState.mEnabledServices.clear();
                }
                userState.mEnabledServices.add(sFakeAccessibilityServiceComponentName);
                userState.mTouchExplorationGrantedServices.add(sFakeAccessibilityServiceComponentName);
                onUserStateChangedLocked(userState);
            } catch (RemoteException re) {
                Slog.e(LOG_TAG, "Couldn't register for the death of a UiTestAutomationService!", re);
                return;
            }
        }
        if (IS_ENG_BUILD) {
            Slog.d(LOG_TAG, "registerUiTestAutomationService ends");
        }
    }

    public void unregisterUiTestAutomationService(IAccessibilityServiceClient serviceClient) {
        synchronized (this.mLock) {
            UserState userState = getCurrentUserStateLocked();
            if (userState.mUiAutomationService == null || serviceClient == null || userState.mUiAutomationService.mServiceInterface == null || userState.mUiAutomationService.mServiceInterface.asBinder() != serviceClient.asBinder()) {
                throw new IllegalStateException("UiAutomationService " + serviceClient + " not registered!");
            }
            userState.mUiAutomationService.binderDied();
        }
    }

    public void temporaryEnableAccessibilityStateUntilKeyguardRemoved(ComponentName service, boolean touchExplorationEnabled) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.TEMPORARY_ENABLE_ACCESSIBILITY", TEMPORARY_ENABLE_ACCESSIBILITY_UNTIL_KEYGUARD_REMOVED);
        if (this.mWindowManagerService.isKeyguardLocked()) {
            synchronized (this.mLock) {
                UserState userState = getCurrentUserStateLocked();
                if (userState.isUiAutomationSuppressingOtherServices()) {
                    return;
                }
                userState.mIsTouchExplorationEnabled = touchExplorationEnabled;
                userState.mIsEnhancedWebAccessibilityEnabled = false;
                userState.mIsDisplayMagnificationEnabled = false;
                userState.mIsAutoclickEnabled = false;
                userState.mEnabledServices.clear();
                userState.mEnabledServices.add(service);
                userState.mBindingServices.clear();
                userState.mTouchExplorationGrantedServices.clear();
                userState.mTouchExplorationGrantedServices.add(service);
                onUserStateChangedLocked(userState);
            }
        }
    }

    public IBinder getWindowToken(int windowId, int userId) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.RETRIEVE_WINDOW_TOKEN", GET_WINDOW_TOKEN);
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.resolveCallingUserIdEnforcingPermissionsLocked(userId) != this.mCurrentUserId) {
                return null;
            } else if (this.mSecurityPolicy.findWindowById(windowId) == null) {
                return null;
            } else {
                IBinder token = (IBinder) this.mGlobalWindowTokens.get(windowId);
                if (token != null) {
                    return token;
                }
                IBinder iBinder = (IBinder) getCurrentUserStateLocked().mWindowTokens.get(windowId);
                return iBinder;
            }
        }
    }

    boolean onGesture(int gestureId) {
        boolean handled;
        synchronized (this.mLock) {
            handled = notifyGestureLocked(gestureId, false);
            if (!handled) {
                handled = notifyGestureLocked(gestureId, true);
            }
        }
        return handled;
    }

    boolean notifyKeyEvent(KeyEvent event, int policyFlags) {
        synchronized (this.mLock) {
            List<Service> boundServices = getCurrentUserStateLocked().mBoundServices;
            if (boundServices.isEmpty()) {
                return false;
            }
            boolean notifyKeyEventLocked = getKeyEventDispatcher().notifyKeyEventLocked(event, policyFlags, boundServices);
            return notifyKeyEventLocked;
        }
    }

    void notifyMagnificationChanged(Region region, float scale, float centerX, float centerY) {
        synchronized (this.mLock) {
            notifyMagnificationChangedLocked(region, scale, centerX, centerY);
        }
    }

    void setMotionEventInjector(MotionEventInjector motionEventInjector) {
        synchronized (this.mLock) {
            this.mMotionEventInjector = motionEventInjector;
            this.mLock.notifyAll();
        }
    }

    boolean getAccessibilityFocusClickPointInScreen(Point outPoint) {
        return getInteractionBridgeLocked().getAccessibilityFocusClickPointInScreenNotLocked(outPoint);
    }

    boolean getWindowBounds(int windowId, Rect outBounds) {
        IBinder token;
        synchronized (this.mLock) {
            token = (IBinder) this.mGlobalWindowTokens.get(windowId);
            if (token == null) {
                token = (IBinder) getCurrentUserStateLocked().mWindowTokens.get(windowId);
            }
        }
        this.mWindowManagerService.getWindowFrame(token, outBounds);
        if (outBounds.isEmpty()) {
            return false;
        }
        return true;
    }

    boolean accessibilityFocusOnlyInActiveWindow() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mWindowsForAccessibilityCallback == null;
        }
        return z;
    }

    int getActiveWindowId() {
        return this.mSecurityPolicy.getActiveWindowId();
    }

    void onTouchInteractionStart() {
        this.mSecurityPolicy.onTouchInteractionStart();
    }

    void onTouchInteractionEnd() {
        this.mSecurityPolicy.onTouchInteractionEnd();
    }

    void onMagnificationStateChanged() {
        notifyClearAccessibilityCacheLocked();
    }

    /* JADX WARNING: Missing block: B:23:0x0064, code:
            return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void switchUser(int userId) {
        synchronized (this.mLock) {
            if (this.mCurrentUserId == userId && this.mInitialized) {
                return;
            }
            UserState oldUserState = getCurrentUserStateLocked();
            oldUserState.onSwitchToAnotherUser();
            if (oldUserState.mClients.getRegisteredCallbackCount() > 0) {
                this.mMainHandler.obtainMessage(3, oldUserState.mUserId, 0).sendToTarget();
            }
            boolean announceNewUser = ((UserManager) this.mContext.getSystemService("user")).getUsers().size() > 1;
            this.mCurrentUserId = userId;
            UserState userState = getCurrentUserStateLocked();
            if (userState.mUiAutomationService != null) {
                userState.mUiAutomationService.binderDied();
            }
            readConfigurationForUserStateLocked(userState);
            onUserStateChangedLocked(userState);
            if (announceNewUser) {
                this.mMainHandler.sendEmptyMessageDelayed(5, 3000);
            }
        }
    }

    private void unlockUser(int userId) {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.resolveProfileParentLocked(userId) == this.mCurrentUserId) {
                onUserStateChangedLocked(getUserStateLocked(this.mCurrentUserId));
            }
        }
    }

    private void removeUser(int userId) {
        synchronized (this.mLock) {
            this.mUserStates.remove(userId);
        }
    }

    void restoreEnabledAccessibilityServicesLocked(String oldSetting, String newSetting) {
        readComponentNamesFromStringLocked(oldSetting, this.mTempComponentNameSet, false);
        readComponentNamesFromStringLocked(newSetting, this.mTempComponentNameSet, true);
        UserState userState = getUserStateLocked(0);
        userState.mEnabledServices.clear();
        userState.mEnabledServices.addAll(this.mTempComponentNameSet);
        persistComponentNamesToSettingLocked("enabled_accessibility_services", userState.mEnabledServices, 0);
        onUserStateChangedLocked(userState);
    }

    private InteractionBridge getInteractionBridgeLocked() {
        if (this.mInteractionBridge == null) {
            this.mInteractionBridge = new InteractionBridge();
        }
        return this.mInteractionBridge;
    }

    private boolean notifyGestureLocked(int gestureId, boolean isDefault) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            Service service = (Service) state.mBoundServices.get(i);
            if (service.mRequestTouchExplorationMode && service.mIsDefault == isDefault) {
                service.notifyGesture(gestureId);
                return true;
            }
        }
        return false;
    }

    private void notifyClearAccessibilityCacheLocked() {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            ((Service) state.mBoundServices.get(i)).notifyClearAccessibilityNodeInfoCache();
        }
    }

    private void notifyMagnificationChangedLocked(Region region, float scale, float centerX, float centerY) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            ((Service) state.mBoundServices.get(i)).notifyMagnificationChangedLocked(region, scale, centerX, centerY);
        }
    }

    private void notifySoftKeyboardShowModeChangedLocked(int showMode) {
        UserState state = getCurrentUserStateLocked();
        for (int i = state.mBoundServices.size() - 1; i >= 0; i--) {
            ((Service) state.mBoundServices.get(i)).notifySoftKeyboardShowModeChangedLocked(showMode);
        }
    }

    private void removeAccessibilityInteractionConnectionLocked(int windowId, int userId) {
        if (userId == -1) {
            this.mGlobalWindowTokens.remove(windowId);
            this.mGlobalInteractionConnections.remove(windowId);
            return;
        }
        UserState userState = getCurrentUserStateLocked();
        userState.mWindowTokens.remove(windowId);
        userState.mInteractionConnections.remove(windowId);
    }

    /* JADX WARNING: Removed duplicated region for block: B:8:0x0077 A:{Splitter: B:6:0x006a, ExcHandler: org.xmlpull.v1.XmlPullParserException (r6_0 'xppe' java.lang.Exception)} */
    /* JADX WARNING: Missing block: B:8:0x0077, code:
            r6 = move-exception;
     */
    /* JADX WARNING: Missing block: B:9:0x0078, code:
            android.util.Slog.e(LOG_TAG, "Error while initializing AccessibilityServiceInfo", r6);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean readInstalledAccessibilityServiceLocked(UserState userState) {
        this.mTempAccessibilityServiceInfoList.clear();
        List<ResolveInfo> installedServices = this.mPackageManager.queryIntentServicesAsUser(new Intent("android.accessibilityservice.AccessibilityService"), 819332, this.mCurrentUserId);
        int count = installedServices.size();
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) installedServices.get(i);
            ServiceInfo serviceInfo = resolveInfo.serviceInfo;
            if ("android.permission.BIND_ACCESSIBILITY_SERVICE".equals(serviceInfo.permission)) {
                try {
                    this.mTempAccessibilityServiceInfoList.add(new AccessibilityServiceInfo(resolveInfo, this.mContext));
                } catch (Exception xppe) {
                }
            } else {
                Slog.w(LOG_TAG, "Skipping accessibilty service " + new ComponentName(serviceInfo.packageName, serviceInfo.name).flattenToShortString() + ": it does not require the permission " + "android.permission.BIND_ACCESSIBILITY_SERVICE");
            }
        }
        if (this.mTempAccessibilityServiceInfoList.equals(userState.mInstalledServices)) {
            this.mTempAccessibilityServiceInfoList.clear();
            return false;
        }
        userState.mInstalledServices.clear();
        userState.mInstalledServices.addAll(this.mTempAccessibilityServiceInfoList);
        this.mTempAccessibilityServiceInfoList.clear();
        return true;
    }

    private boolean readEnabledAccessibilityServicesLocked(UserState userState) {
        this.mTempComponentNameSet.clear();
        readComponentNamesFromSettingLocked("enabled_accessibility_services", userState.mUserId, this.mTempComponentNameSet);
        if (this.mTempComponentNameSet.equals(userState.mEnabledServices)) {
            this.mTempComponentNameSet.clear();
            return false;
        }
        userState.mEnabledServices.clear();
        userState.mEnabledServices.addAll(this.mTempComponentNameSet);
        if (userState.mUiAutomationService != null) {
            userState.mEnabledServices.add(sFakeAccessibilityServiceComponentName);
        }
        this.mTempComponentNameSet.clear();
        return true;
    }

    private boolean readTouchExplorationGrantedAccessibilityServicesLocked(UserState userState) {
        this.mTempComponentNameSet.clear();
        readComponentNamesFromSettingLocked("touch_exploration_granted_accessibility_services", userState.mUserId, this.mTempComponentNameSet);
        if (this.mTempComponentNameSet.equals(userState.mTouchExplorationGrantedServices)) {
            this.mTempComponentNameSet.clear();
            return false;
        }
        userState.mTouchExplorationGrantedServices.clear();
        userState.mTouchExplorationGrantedServices.addAll(this.mTempComponentNameSet);
        this.mTempComponentNameSet.clear();
        return true;
    }

    private void notifyAccessibilityServicesDelayedLocked(AccessibilityEvent event, boolean isDefault) {
        try {
            UserState state = getCurrentUserStateLocked();
            int count = state.mBoundServices.size();
            for (int i = 0; i < count; i++) {
                Service service = (Service) state.mBoundServices.get(i);
                if (service.mIsDefault == isDefault && canDispatchEventToServiceLocked(service, event)) {
                    service.notifyAccessibilityEvent(event);
                }
            }
        } catch (IndexOutOfBoundsException e) {
        }
    }

    private void addServiceLocked(Service service, UserState userState) {
        try {
            if (!userState.mBoundServices.contains(service)) {
                service.onAdded();
                userState.mBoundServices.add(service);
                userState.mComponentNameToServiceMap.put(service.mComponentName, service);
            }
        } catch (RemoteException e) {
        }
    }

    private void removeServiceLocked(Service service, UserState userState) {
        userState.mBoundServices.remove(service);
        service.onRemoved();
        userState.mComponentNameToServiceMap.clear();
        for (int i = 0; i < userState.mBoundServices.size(); i++) {
            Service boundService = (Service) userState.mBoundServices.get(i);
            userState.mComponentNameToServiceMap.put(boundService.mComponentName, boundService);
        }
    }

    private boolean canDispatchEventToServiceLocked(Service service, AccessibilityEvent event) {
        if (!service.canReceiveEventsLocked()) {
            return false;
        }
        if (event.getWindowId() != -1 && !event.isImportantForAccessibility() && (service.mFetchFlags & 8) == 0) {
            return false;
        }
        int eventType = event.getEventType();
        if ((service.mEventTypes & eventType) != eventType) {
            return false;
        }
        Set<String> packageNames = service.mPackageNames;
        return !packageNames.isEmpty() ? packageNames.contains(event.getPackageName() != null ? event.getPackageName().toString() : null) : true;
    }

    private void unbindAllServicesLocked(UserState userState) {
        List<Service> services = userState.mBoundServices;
        int i = 0;
        int count = services.size();
        while (i < count) {
            if (((Service) services.get(i)).unbindLocked()) {
                i--;
                count--;
            }
            i++;
        }
    }

    private void readComponentNamesFromSettingLocked(String settingName, int userId, Set<ComponentName> outComponentNames) {
        readComponentNamesFromStringLocked(Secure.getStringForUser(this.mContext.getContentResolver(), settingName, userId), outComponentNames, false);
    }

    private void readComponentNamesFromStringLocked(String names, Set<ComponentName> outComponentNames, boolean doMerge) {
        if (!doMerge) {
            outComponentNames.clear();
        }
        if (names != null) {
            SimpleStringSplitter splitter = this.mStringColonSplitter;
            splitter.setString(names);
            while (splitter.hasNext()) {
                String str = splitter.next();
                if (str != null && str.length() > 0) {
                    ComponentName enabledService = ComponentName.unflattenFromString(str);
                    if (enabledService != null) {
                        outComponentNames.add(enabledService);
                    }
                }
            }
        }
    }

    private void persistComponentNamesToSettingLocked(String settingName, Set<ComponentName> componentNames, int userId) {
        StringBuilder builder = new StringBuilder();
        for (ComponentName componentName : componentNames) {
            if (builder.length() > 0) {
                builder.append(COMPONENT_NAME_SEPARATOR);
            }
            builder.append(componentName.flattenToShortString());
        }
        long identity = Binder.clearCallingIdentity();
        try {
            Secure.putStringForUser(this.mContext.getContentResolver(), settingName, builder.toString(), userId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private void updateServicesLocked(UserState userState) {
        Map<ComponentName, Service> componentNameToServiceMap = userState.mComponentNameToServiceMap;
        boolean isUnlockingOrUnlocked = ((UserManager) this.mContext.getSystemService(UserManager.class)).isUserUnlockingOrUnlocked(userState.mUserId);
        int count = userState.mInstalledServices.size();
        for (int i = 0; i < count; i++) {
            AccessibilityServiceInfo installedService = (AccessibilityServiceInfo) userState.mInstalledServices.get(i);
            ComponentName componentName = ComponentName.unflattenFromString(installedService.getId());
            Service service = (Service) componentNameToServiceMap.get(componentName);
            if (!isUnlockingOrUnlocked && !installedService.isDirectBootAware()) {
                Slog.d(LOG_TAG, "Ignoring non-encryption-aware service " + componentName);
            } else if (!userState.mBindingServices.contains(componentName)) {
                if (userState.mEnabledServices.contains(componentName)) {
                    if (service == null) {
                        service = new Service(userState.mUserId, componentName, installedService);
                    } else if (userState.mBoundServices.contains(service)) {
                    }
                    service.bindLocked();
                } else if (service != null) {
                    service.unbindLocked();
                }
            }
        }
        updateAccessibilityEnabledSetting(userState);
    }

    private void scheduleUpdateClientsIfNeededLocked(UserState userState) {
        int clientState = userState.getClientState();
        if (userState.mLastSentClientState == clientState) {
            return;
        }
        if (this.mGlobalClients.getRegisteredCallbackCount() > 0 || userState.mClients.getRegisteredCallbackCount() > 0) {
            userState.mLastSentClientState = clientState;
            this.mMainHandler.obtainMessage(2, clientState, userState.mUserId).sendToTarget();
        }
    }

    private void scheduleUpdateInputFilter(UserState userState) {
        this.mMainHandler.obtainMessage(6, userState).sendToTarget();
    }

    private void updateInputFilter(UserState userState) {
        boolean setInputFilter = false;
        AccessibilityInputFilter inputFilter = null;
        synchronized (this.mLock) {
            int flags = 0;
            if (userState.mIsDisplayMagnificationEnabled) {
                flags = 1;
            }
            if (userHasMagnificationServicesLocked(userState)) {
                flags |= 32;
            }
            if (userState.isHandlingAccessibilityEvents() && userState.mIsTouchExplorationEnabled) {
                flags |= 2;
            }
            if (userState.mIsFilterKeyEventsEnabled) {
                flags |= 4;
            }
            if (userState.mIsAutoclickEnabled) {
                flags |= 8;
            }
            if (userState.mIsPerformGesturesEnabled) {
                flags |= 16;
            }
            if (flags != 0) {
                if (!this.mHasInputFilter) {
                    this.mHasInputFilter = true;
                    if (this.mInputFilter == null) {
                        this.mInputFilter = new AccessibilityInputFilter(this.mContext, this);
                    }
                    inputFilter = this.mInputFilter;
                    setInputFilter = true;
                }
                this.mInputFilter.setUserAndEnabledFeatures(userState.mUserId, flags);
            } else if (this.mHasInputFilter) {
                this.mHasInputFilter = false;
                this.mInputFilter.setUserAndEnabledFeatures(userState.mUserId, 0);
                inputFilter = null;
                setInputFilter = true;
            }
        }
        if (setInputFilter) {
            this.mWindowManagerService.setInputFilter(inputFilter);
        }
    }

    private void showEnableTouchExplorationDialog(final Service service) {
        synchronized (this.mLock) {
            String label = service.mResolveInfo.loadLabel(this.mContext.getPackageManager()).toString();
            final UserState state = getCurrentUserStateLocked();
            if (state.mIsTouchExplorationEnabled) {
            } else if (this.mEnableTouchExplorationDialog == null || !this.mEnableTouchExplorationDialog.isShowing()) {
                Builder title = new Builder(this.mContext).setIconAttribute(16843605).setPositiveButton(17039370, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        state.mTouchExplorationGrantedServices.add(service.mComponentName);
                        AccessibilityManagerService.this.persistComponentNamesToSettingLocked("touch_exploration_granted_accessibility_services", state.mTouchExplorationGrantedServices, state.mUserId);
                        UserState userState = AccessibilityManagerService.this.getUserStateLocked(service.mUserId);
                        userState.mIsTouchExplorationEnabled = true;
                        long identity = Binder.clearCallingIdentity();
                        try {
                            Secure.putIntForUser(AccessibilityManagerService.this.mContext.getContentResolver(), "touch_exploration_enabled", 1, service.mUserId);
                            AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                        } finally {
                            Binder.restoreCallingIdentity(identity);
                        }
                    }
                }).setNegativeButton(17039360, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).setTitle(17040207);
                Context context = this.mContext;
                Object[] objArr = new Object[1];
                objArr[0] = label;
                this.mEnableTouchExplorationDialog = title.setMessage(context.getString(17040208, objArr)).create();
                this.mEnableTouchExplorationDialog.getWindow().setType(2003);
                LayoutParams attributes = this.mEnableTouchExplorationDialog.getWindow().getAttributes();
                attributes.privateFlags |= 16;
                this.mEnableTouchExplorationDialog.setCanceledOnTouchOutside(true);
                this.mEnableTouchExplorationDialog.show();
            }
        }
    }

    private void onUserStateChangedLocked(UserState userState) {
        this.mInitialized = true;
        updateLegacyCapabilitiesLocked(userState);
        updateServicesLocked(userState);
        updateWindowsForAccessibilityCallbackLocked(userState);
        updateAccessibilityFocusBehaviorLocked(userState);
        updateFilterKeyEventsLocked(userState);
        updateTouchExplorationLocked(userState);
        updatePerformGesturesLocked(userState);
        updateEnhancedWebAccessibilityLocked(userState);
        updateDisplayDaltonizerLocked(userState);
        updateDisplayInversionLocked(userState);
        updateMagnificationLocked(userState);
        updateSoftKeyboardShowModeLocked(userState);
        scheduleUpdateInputFilter(userState);
        scheduleUpdateClientsIfNeededLocked(userState);
    }

    private void updateAccessibilityFocusBehaviorLocked(UserState userState) {
        List<Service> boundServices = userState.mBoundServices;
        int boundServiceCount = boundServices.size();
        for (int i = 0; i < boundServiceCount; i++) {
            if (((Service) boundServices.get(i)).canRetrieveInteractiveWindowsLocked()) {
                userState.mAccessibilityFocusOnlyInActiveWindow = false;
                return;
            }
        }
        userState.mAccessibilityFocusOnlyInActiveWindow = true;
    }

    private void updateWindowsForAccessibilityCallbackLocked(UserState userState) {
        List<Service> boundServices = userState.mBoundServices;
        int boundServiceCount = boundServices.size();
        for (int i = 0; i < boundServiceCount; i++) {
            if (((Service) boundServices.get(i)).canRetrieveInteractiveWindowsLocked()) {
                if (this.mWindowsForAccessibilityCallback == null) {
                    this.mWindowsForAccessibilityCallback = new WindowsForAccessibilityCallback();
                    this.mWindowManagerService.setWindowsForAccessibilityCallback(this.mWindowsForAccessibilityCallback);
                }
                return;
            }
        }
        if (this.mWindowsForAccessibilityCallback != null) {
            this.mWindowsForAccessibilityCallback = null;
            this.mWindowManagerService.setWindowsForAccessibilityCallback(null);
            this.mSecurityPolicy.clearWindowsLocked();
        }
    }

    private void updateLegacyCapabilitiesLocked(UserState userState) {
        int installedServiceCount = userState.mInstalledServices.size();
        for (int i = 0; i < installedServiceCount; i++) {
            AccessibilityServiceInfo serviceInfo = (AccessibilityServiceInfo) userState.mInstalledServices.get(i);
            ResolveInfo resolveInfo = serviceInfo.getResolveInfo();
            if ((serviceInfo.getCapabilities() & 2) == 0 && resolveInfo.serviceInfo.applicationInfo.targetSdkVersion <= 17) {
                if (userState.mTouchExplorationGrantedServices.contains(new ComponentName(resolveInfo.serviceInfo.packageName, resolveInfo.serviceInfo.name))) {
                    serviceInfo.setCapabilities(serviceInfo.getCapabilities() | 2);
                }
            }
        }
    }

    private void updatePerformGesturesLocked(UserState userState) {
        int serviceCount = userState.mBoundServices.size();
        for (int i = 0; i < serviceCount; i++) {
            if ((((Service) userState.mBoundServices.get(i)).mAccessibilityServiceInfo.getCapabilities() & 32) != 0) {
                userState.mIsPerformGesturesEnabled = true;
                return;
            }
        }
        userState.mIsPerformGesturesEnabled = false;
    }

    private void updateFilterKeyEventsLocked(UserState userState) {
        int serviceCount = userState.mBoundServices.size();
        int i = 0;
        while (i < serviceCount) {
            Service service = (Service) userState.mBoundServices.get(i);
            if (!service.mRequestFilterKeyEvents || (service.mAccessibilityServiceInfo.getCapabilities() & 8) == 0) {
                i++;
            } else {
                userState.mIsFilterKeyEventsEnabled = true;
                return;
            }
        }
        userState.mIsFilterKeyEventsEnabled = false;
    }

    private boolean readConfigurationForUserStateLocked(UserState userState) {
        return ((((((readInstalledAccessibilityServiceLocked(userState) | readEnabledAccessibilityServicesLocked(userState)) | readTouchExplorationGrantedAccessibilityServicesLocked(userState)) | readTouchExplorationEnabledSettingLocked(userState)) | readHighTextContrastEnabledSettingLocked(userState)) | readEnhancedWebAccessibilityEnabledChangedLocked(userState)) | readDisplayMagnificationEnabledSettingLocked(userState)) | readAutoclickEnabledSettingLocked(userState);
    }

    private void updateAccessibilityEnabledSetting(UserState userState) {
        long identity = Binder.clearCallingIdentity();
        try {
            Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_enabled", userState.isHandlingAccessibilityEvents() ? 1 : 0, userState.mUserId);
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
    }

    private boolean readTouchExplorationEnabledSettingLocked(UserState userState) {
        boolean touchExplorationEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "touch_exploration_enabled", 0, userState.mUserId) == 1;
        if (touchExplorationEnabled == userState.mIsTouchExplorationEnabled) {
            return false;
        }
        userState.mIsTouchExplorationEnabled = touchExplorationEnabled;
        return true;
    }

    private boolean readDisplayMagnificationEnabledSettingLocked(UserState userState) {
        boolean displayMagnificationEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_display_magnification_enabled", 0, userState.mUserId) == 1;
        if (displayMagnificationEnabled == userState.mIsDisplayMagnificationEnabled) {
            return false;
        }
        userState.mIsDisplayMagnificationEnabled = displayMagnificationEnabled;
        return true;
    }

    private boolean readAutoclickEnabledSettingLocked(UserState userState) {
        boolean autoclickEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_autoclick_enabled", 0, userState.mUserId) == 1;
        if (autoclickEnabled == userState.mIsAutoclickEnabled) {
            return false;
        }
        userState.mIsAutoclickEnabled = autoclickEnabled;
        return true;
    }

    private boolean readEnhancedWebAccessibilityEnabledChangedLocked(UserState userState) {
        boolean enhancedWeAccessibilityEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_script_injection", 0, userState.mUserId) == 1;
        if (enhancedWeAccessibilityEnabled == userState.mIsEnhancedWebAccessibilityEnabled) {
            return false;
        }
        userState.mIsEnhancedWebAccessibilityEnabled = enhancedWeAccessibilityEnabled;
        return true;
    }

    private boolean readHighTextContrastEnabledSettingLocked(UserState userState) {
        boolean highTextContrastEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "high_text_contrast_enabled", 0, userState.mUserId) == 1;
        if (highTextContrastEnabled == userState.mIsTextHighContrastEnabled) {
            return false;
        }
        userState.mIsTextHighContrastEnabled = highTextContrastEnabled;
        return true;
    }

    private boolean readSoftKeyboardShowModeChangedLocked(UserState userState) {
        int softKeyboardShowMode = Secure.getIntForUser(this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, userState.mUserId);
        if (softKeyboardShowMode == userState.mSoftKeyboardShowMode) {
            return false;
        }
        userState.mSoftKeyboardShowMode = softKeyboardShowMode;
        return true;
    }

    private void updateTouchExplorationLocked(UserState userState) {
        boolean enabled = false;
        int serviceCount = userState.mBoundServices.size();
        for (int i = 0; i < serviceCount; i++) {
            if (canRequestAndRequestsTouchExplorationLocked((Service) userState.mBoundServices.get(i))) {
                enabled = true;
                break;
            }
        }
        if (enabled != userState.mIsTouchExplorationEnabled) {
            userState.mIsTouchExplorationEnabled = enabled;
            long identity = Binder.clearCallingIdentity();
            try {
                Secure.putIntForUser(this.mContext.getContentResolver(), "touch_exploration_enabled", enabled ? 1 : 0, userState.mUserId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private boolean canRequestAndRequestsTouchExplorationLocked(Service service) {
        if (!service.canReceiveEventsLocked() || !service.mRequestTouchExplorationMode) {
            return false;
        }
        if (service.mIsAutomation) {
            return true;
        }
        if (service.mResolveInfo.serviceInfo.applicationInfo.targetSdkVersion <= 17) {
            if (getUserStateLocked(service.mUserId).mTouchExplorationGrantedServices.contains(service.mComponentName)) {
                return true;
            }
            if (this.mEnableTouchExplorationDialog == null || !this.mEnableTouchExplorationDialog.isShowing()) {
                this.mMainHandler.obtainMessage(7, service).sendToTarget();
            }
        } else if ((service.mAccessibilityServiceInfo.getCapabilities() & 2) != 0) {
            return true;
        }
        return false;
    }

    private void updateEnhancedWebAccessibilityLocked(UserState userState) {
        boolean enabled = false;
        int serviceCount = userState.mBoundServices.size();
        for (int i = 0; i < serviceCount; i++) {
            if (canRequestAndRequestsEnhancedWebAccessibilityLocked((Service) userState.mBoundServices.get(i))) {
                enabled = true;
                break;
            }
        }
        if (enabled != userState.mIsEnhancedWebAccessibilityEnabled) {
            userState.mIsEnhancedWebAccessibilityEnabled = enabled;
            long identity = Binder.clearCallingIdentity();
            try {
                Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_script_injection", enabled ? 1 : 0, userState.mUserId);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private boolean canRequestAndRequestsEnhancedWebAccessibilityLocked(Service service) {
        if (!service.canReceiveEventsLocked() || !service.mRequestEnhancedWebAccessibility) {
            return false;
        }
        if (service.mIsAutomation || (service.mAccessibilityServiceInfo.getCapabilities() & 4) != 0) {
            return true;
        }
        return false;
    }

    private void updateDisplayDaltonizerLocked(UserState userState) {
        DisplayAdjustmentUtils.applyDaltonizerSetting(this.mContext, userState.mUserId);
    }

    private void updateDisplayInversionLocked(UserState userState) {
        DisplayAdjustmentUtils.applyInversionSetting(this.mContext, userState.mUserId);
    }

    private void updateDisplayAdjustValues(UserState userState) {
        DisplayAdjustmentUtils.applyAdjustValues(this.mContext, userState.mUserId);
    }

    private void updateMagnificationLocked(UserState userState) {
        if (userState.mUserId == this.mCurrentUserId) {
            if (userState.mIsDisplayMagnificationEnabled || userHasListeningMagnificationServicesLocked(userState)) {
                getMagnificationController();
                this.mMagnificationController.register();
            } else if (this.mMagnificationController != null) {
                this.mMagnificationController.unregister();
            }
        }
    }

    private boolean userHasMagnificationServicesLocked(UserState userState) {
        List<Service> services = userState.mBoundServices;
        int count = services.size();
        for (int i = 0; i < count; i++) {
            if (this.mSecurityPolicy.canControlMagnification((Service) services.get(i))) {
                return true;
            }
        }
        return false;
    }

    private boolean userHasListeningMagnificationServicesLocked(UserState userState) {
        List<Service> services = userState.mBoundServices;
        int count = services.size();
        for (int i = 0; i < count; i++) {
            Service service = (Service) services.get(i);
            if (this.mSecurityPolicy.canControlMagnification(service) && service.mInvocationHandler.mIsMagnificationCallbackEnabled) {
                return true;
            }
        }
        return false;
    }

    private void updateSoftKeyboardShowModeLocked(UserState userState) {
        if (userState.mUserId == this.mCurrentUserId && userState.mSoftKeyboardShowMode != 0 && !userState.mEnabledServices.contains(userState.mServiceChangingSoftKeyboardMode)) {
            long identity = Binder.clearCallingIdentity();
            try {
                Secure.putIntForUser(this.mContext.getContentResolver(), "accessibility_soft_keyboard_mode", 0, userState.mUserId);
                userState.mSoftKeyboardShowMode = 0;
                userState.mServiceChangingSoftKeyboardMode = null;
                notifySoftKeyboardShowModeChangedLocked(userState.mSoftKeyboardShowMode);
            } finally {
                Binder.restoreCallingIdentity(identity);
            }
        }
    }

    private MagnificationSpec getCompatibleMagnificationSpecLocked(int windowId) {
        IBinder windowToken = (IBinder) this.mGlobalWindowTokens.get(windowId);
        if (windowToken == null) {
            windowToken = (IBinder) getCurrentUserStateLocked().mWindowTokens.get(windowId);
        }
        if (windowToken != null) {
            return this.mWindowManagerService.getCompatibleMagnificationSpecForWindow(windowToken);
        }
        return null;
    }

    private KeyEventDispatcher getKeyEventDispatcher() {
        if (this.mKeyEventDispatcher == null) {
            this.mKeyEventDispatcher = new KeyEventDispatcher(this.mMainHandler, 8, this.mLock, this.mPowerManager);
        }
        return this.mKeyEventDispatcher;
    }

    public void enableAccessibilityService(ComponentName componentName, int userId) {
        synchronized (this.mLock) {
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException("only SYSTEM can call enableAccessibilityService.");
            }
            SettingsStringHelper settingsHelper = new SettingsStringHelper("enabled_accessibility_services", userId);
            settingsHelper.addService(componentName);
            settingsHelper.writeToSettings();
            UserState userState = getUserStateLocked(userId);
            if (userState.mEnabledServices.add(componentName)) {
                onUserStateChangedLocked(userState);
            }
        }
    }

    public void disableAccessibilityService(ComponentName componentName, int userId) {
        synchronized (this.mLock) {
            if (Binder.getCallingUid() != 1000) {
                throw new SecurityException("only SYSTEM can call disableAccessibility");
            }
            SettingsStringHelper settingsHelper = new SettingsStringHelper("enabled_accessibility_services", userId);
            settingsHelper.deleteService(componentName);
            settingsHelper.writeToSettings();
            UserState userState = getUserStateLocked(userId);
            if (userState.mEnabledServices.remove(componentName)) {
                onUserStateChangedLocked(userState);
            }
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mSecurityPolicy.enforceCallingPermission("android.permission.DUMP", "dump");
        synchronized (this.mLock) {
            int j;
            pw.println("ACCESSIBILITY MANAGER (dumpsys accessibility)");
            pw.println();
            int userCount = this.mUserStates.size();
            for (int i = 0; i < userCount; i++) {
                UserState userState = (UserState) this.mUserStates.valueAt(i);
                pw.append("User state[attributes:{id=" + userState.mUserId);
                pw.append(", currentUser=" + (userState.mUserId == this.mCurrentUserId));
                pw.append(", touchExplorationEnabled=" + userState.mIsTouchExplorationEnabled);
                pw.append(", displayMagnificationEnabled=" + userState.mIsDisplayMagnificationEnabled);
                pw.append(", autoclickEnabled=" + userState.mIsAutoclickEnabled);
                if (userState.mUiAutomationService != null) {
                    pw.append(", ");
                    userState.mUiAutomationService.dump(fd, pw, args);
                    pw.println();
                }
                pw.append("}");
                pw.println();
                pw.append("           services:{");
                int serviceCount = userState.mBoundServices.size();
                for (j = 0; j < serviceCount; j++) {
                    if (j > 0) {
                        pw.append(", ");
                        pw.println();
                        pw.append("                     ");
                    }
                    ((Service) userState.mBoundServices.get(j)).dump(fd, pw, args);
                }
                pw.println("}]");
                pw.println();
            }
            if (this.mSecurityPolicy.mWindows != null) {
                int windowCount = this.mSecurityPolicy.mWindows.size();
                for (j = 0; j < windowCount; j++) {
                    if (j > 0) {
                        pw.append(',');
                        pw.println();
                    }
                    pw.append("Window[");
                    pw.append(((AccessibilityWindowInfo) this.mSecurityPolicy.mWindows.get(j)).toString());
                    pw.append(']');
                }
            }
        }
    }

    private int findWindowIdLocked(IBinder token) {
        int globalIndex = this.mGlobalWindowTokens.indexOfValue(token);
        if (globalIndex >= 0) {
            return this.mGlobalWindowTokens.keyAt(globalIndex);
        }
        UserState userState = getCurrentUserStateLocked();
        int userIndex = userState.mWindowTokens.indexOfValue(token);
        if (userIndex >= 0) {
            return userState.mWindowTokens.keyAt(userIndex);
        }
        return -1;
    }

    private void ensureWindowsAvailableTimed() {
        synchronized (this.mLock) {
            if (this.mSecurityPolicy.mWindows != null) {
                return;
            }
            if (this.mWindowsForAccessibilityCallback == null) {
                onUserStateChangedLocked(getCurrentUserStateLocked());
            }
            if (this.mWindowsForAccessibilityCallback == null) {
                return;
            }
            long startMillis = SystemClock.uptimeMillis();
            while (this.mSecurityPolicy.mWindows == null) {
                long remainMillis = 5000 - (SystemClock.uptimeMillis() - startMillis);
                if (remainMillis <= 0) {
                    return;
                }
                try {
                    this.mLock.wait(remainMillis);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    MagnificationController getMagnificationController() {
        MagnificationController magnificationController;
        synchronized (this.mLock) {
            if (this.mMagnificationController == null) {
                this.mMagnificationController = new MagnificationController(this.mContext, this, this.mLock);
                this.mMagnificationController.setUserId(this.mCurrentUserId);
            }
            magnificationController = this.mMagnificationController;
        }
        return magnificationController;
    }

    private AppWidgetManagerInternal getAppWidgetManager() {
        AppWidgetManagerInternal appWidgetManagerInternal;
        synchronized (this.mLock) {
            if (this.mAppWidgetService == null && this.mPackageManager.hasSystemFeature("android.software.app_widgets")) {
                this.mAppWidgetService = (AppWidgetManagerInternal) LocalServices.getService(AppWidgetManagerInternal.class);
            }
            appWidgetManagerInternal = this.mAppWidgetService;
        }
        return appWidgetManagerInternal;
    }

    private void manageAccessibilityServices() {
        UserState userState = getCurrentUserStateLocked();
        synchronized (this.mLock) {
            unbindAllServicesLocked(userState);
            scheduleUpdateClientsIfNeededLocked(userState);
        }
    }

    private void registerIPOReceiver(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.ACTION_BOOT_IPO");
        filter.addAction("android.intent.action.ACTION_SHUTDOWN_IPO");
        context.registerReceiver(new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                UserState userState = AccessibilityManagerService.this.getCurrentUserStateLocked();
                if ("android.intent.action.ACTION_BOOT_IPO".equals(intent.getAction())) {
                    AccessibilityManagerService.this.readConfigurationForUserStateLocked(userState);
                    AccessibilityManagerService.this.onUserStateChangedLocked(userState);
                } else if ("android.intent.action.ACTION_SHUTDOWN_IPO".equals(intent.getAction())) {
                    AccessibilityManagerService.this.manageAccessibilityServices();
                }
            }
        }, filter);
    }
}
