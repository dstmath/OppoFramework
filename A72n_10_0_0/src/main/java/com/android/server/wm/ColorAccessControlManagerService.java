package com.android.server.wm;

import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteCallbackList;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.os.AtomicFile;
import com.android.internal.util.DumpUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.server.am.ColorMultiAppManagerService;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.color.app.ColorAccessControlInfo;
import com.color.app.IColorAccessControlManager;
import com.color.app.IColorAccessControlObserver;
import com.color.zoomwindow.ColorZoomWindowManager;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class ColorAccessControlManagerService extends IColorAccessControlManager.Stub implements IColorAccessControlLocalManager {
    private static boolean DEBUG = false;
    private static final int MSG_WRITE = 1;
    private static String OPPO_SAFECENTER_PASSWORD = "com.coloros.safecenter/.privacy.view.password.AppUnlockPasswordActivity";
    private static final String PACKAGE_SECURITYCENTER = "com.coloros.safecenter";
    private static final String PASSWORD_ACTION = "oppo.intent.action.UNLOCK_APP_PASSWORD";
    static final String TAG = "ColorAccessControlManagerService";
    private static final String UPDATE_VERSION = "1.0";
    private static final int WINDOWING_MODE_PRELOAD = 7;
    private static final int WINDOWING_MODE_ZOOM = ColorZoomWindowManager.WINDOWING_MODE_ZOOM;
    private static final int WRITE_SETTINGS_DELAY = 1000;
    private AccessControlHandler mAccessControlHandler;
    private ColorAccessController mAccessController;
    private String[] mArgs;
    private ActivityTaskManagerService mAtms;
    private final Context mContext;
    private final RemoteCallbackList<IColorAccessControlObserver> mIColorAccessControlObservers = new RemoteCallbackList<>();
    private int mNextArg;
    private AtomicFile mSettingsFile;
    private SettingsObserver mSettingsObserver;
    private boolean mSystemReday;
    private Object mUserStateLock = new Object();
    final SparseArray<UserState> mUserStates = new SparseArray<>();

    public ColorAccessControlManagerService(ActivityTaskManagerService atms) {
        this.mContext = atms.mContext;
        this.mAtms = atms;
        this.mSettingsFile = new AtomicFile(new File(new File(Environment.getDataDirectory(), "system"), "oppo-encryption-packages.xml"));
        HandlerThread accessControlHandlerThread = new HandlerThread("AccessControlHandlerThread");
        accessControlHandlerThread.start();
        Looper looper = accessControlHandlerThread.getLooper();
        this.mAccessControlHandler = new AccessControlHandler(looper);
        readState();
        this.mAccessController = new ColorAccessController(this.mContext, looper);
    }

    public void publish() {
        ServiceManager.addService("color_accesscontrol", asBinder());
        Slog.i(TAG, "ColorAccessControlManagerService published");
    }

    public void onSystemReady() {
        Slog.i(TAG, "ColorAccessControlManagerService*****onSystemReady");
        this.mSystemReday = true;
        this.mSettingsObserver = new SettingsObserver(this.mAccessControlHandler, this.mContext);
        synchronized (this) {
            initAccessControlSettingsLocked(getUserStateLocked(0));
        }
    }

    public Intent checkStartActivityForAppLock(ActivityStackSupervisor supervisor, ActivityRecord sourceRecord, ActivityInfo aInfo, Intent intent, int requestCode, int realCallingUid, ActivityOptions options) {
        UserState userState = getUserStateLocked(UserHandle.getCallingUserId());
        boolean isPreLoad = false;
        boolean isSkip = this.mAccessController.isSkipCheckActivity(intent) || this.mAccessController.isFilterAction(intent);
        boolean isAccessControlEnable = getAccessControlEnabledLocked(userState);
        if (options != null && options.getLaunchWindowingMode() == 7) {
            isPreLoad = true;
        }
        boolean isKeyguardLock = supervisor.getKeyguardController().isKeyguardLocked();
        if (DEBUG) {
            Slog.d(TAG, "checkStartActivityForAppLock: start intent=" + intent + "  requestCode=" + requestCode + "  sourceRecord=" + sourceRecord + "  aInfo.applicationInfo.uid=" + aInfo.applicationInfo.uid + "  realCallingUid=" + realCallingUid + "  isSkip=" + isSkip + "  isAccessControlEnable=" + isAccessControlEnable + "  isPreLoad=" + isPreLoad + "  isKeyguardLock=" + isKeyguardLock);
        }
        if (!isAccessControlEnable || isSkip || isPreLoad || isKeyguardLock || aInfo == null || aInfo.applicationInfo == null) {
            return null;
        }
        Intent checkIntent = getCheckAccessControlIntent(aInfo, intent, sourceRecord, requestCode, UserHandle.getUserId(aInfo.applicationInfo.uid), realCallingUid, options);
        if (DEBUG) {
            Slog.d(TAG, "checkStartActivityForAppLock: checkIntent=" + checkIntent + "  oriInten=" + intent + "  sourceRecord=" + sourceRecord + "  aInfo.applicationInfo.uid=" + aInfo.applicationInfo.uid + "  realCallingUid=" + realCallingUid);
        }
        if (checkIntent != null) {
            return checkIntent;
        }
        return null;
    }

    private Intent getCheckAccessControlIntent(ActivityInfo aInfo, Intent intent, ActivityRecord sourceRecord, int requestCode, int userId, int realCallingUid, ActivityOptions options) {
        if (aInfo == null || !checkAccessControlPassAsUser(aInfo.packageName, intent, userId)) {
            return getCheckAccessIntent(aInfo, intent, requestCode, sourceRecord, userId, options);
        }
        Slog.w(TAG, "getCheckAccessControlIntent: aInfo=" + aInfo + " has pass   intent=" + intent);
        return null;
    }

    private Intent getCheckAccessIntent(ActivityInfo aInfo, Intent intent, int requestCode, ActivityRecord sourceRecord, int userId, ActivityOptions options) {
        Intent result = new Intent(PASSWORD_ACTION);
        result.addFlags(8388608);
        result.setPackage(PACKAGE_SECURITYCENTER);
        if (intent != null) {
            Intent original = new Intent(intent);
            if ((original.getFlags() & 33554432) != 0) {
                result.addFlags(33554432);
            }
            if (sourceRecord != null) {
                if (requestCode >= 0) {
                    original.addFlags(33554432);
                }
                if ((original.getFlags() & 268435456) == 0) {
                    result.addFlags(536870912);
                } else {
                    result.addFlags(268435456);
                    result.addFlags(ColorMultiAppManagerService.GET_MULTI_APP);
                }
            } else {
                original.addFlags(268435456);
                result.addFlags(ColorMultiAppManagerService.GET_MULTI_APP);
            }
            result.putExtra("Access_Control_Package_UserId", userId);
            result.putExtra("Access_Control_Package_Name", aInfo.packageName);
            if (options != null && (options.getLaunchWindowingMode() == WINDOWING_MODE_ZOOM || options.getLaunchWindowingMode() == 5)) {
                result.putExtra("Launch_Windowing_Mode", options.getLaunchWindowingMode());
            } else if (this.mAtms.mRootActivityContainer.getTopDisplayFocusedStack() == null || !this.mAtms.mRootActivityContainer.getTopDisplayFocusedStack().inSplitScreenWindowingMode()) {
                result.putExtra("Launch_Windowing_Mode", 1);
            } else if (sourceRecord != null && (original.getFlags() & 268435456) == 0 && (requestCode >= 0 || sourceRecord.launchMode != 3 || !isLaunchModeNeedNewTask(aInfo.launchMode))) {
                result.putExtra("Launch_Windowing_Mode", sourceRecord.getWindowingMode());
            } else if (!ActivityInfo.isResizeableMode(aInfo.resizeMode)) {
                result.putExtra("Launch_Windowing_Mode", 1);
            } else {
                result.putExtra("Launch_Windowing_Mode", 4);
            }
            result.putExtra("android.intent.extra.INTENT", original);
        } else {
            result.addFlags(536870912);
        }
        return result;
    }

    private boolean isLaunchModeNeedNewTask(int mode) {
        return mode == 3 || mode == 2;
    }

    private ActivityRecord findAccessControlActivityLocked(ActivityRecord top, int windowMode) {
        for (int displayNdx = this.mAtms.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display2 = this.mAtms.mRootActivityContainer.getChildAt(displayNdx);
            for (int stackNdx = display2.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display2.getChildAt(stackNdx);
                for (int taskNdx = stack.getChildCount() - 1; taskNdx >= 0; taskNdx--) {
                    ArrayList<ActivityRecord> activities = stack.getChildAt(taskNdx).mActivities;
                    for (int activityNdx = activities.size() - 1; activityNdx >= 0; activityNdx--) {
                        ActivityRecord r = activities.get(activityNdx);
                        if (r != top && Objects.equals(r.mActivityComponent, top.mActivityComponent) && r.getWindowingMode() == windowMode) {
                            return r;
                        }
                    }
                }
            }
        }
        return null;
    }

    private ActivityStack findFreeformStackLocked() {
        for (int displayNdx = this.mAtms.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
            ActivityDisplay display2 = this.mAtms.mRootActivityContainer.getChildAt(displayNdx);
            for (int stackNdx = display2.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                ActivityStack stack = display2.getChildAt(stackNdx);
                if (stack.getWindowingMode() == 5 || stack.getWindowingMode() == WINDOWING_MODE_ZOOM) {
                    return stack;
                }
            }
        }
        return null;
    }

    private void finishOldAccessControlActivityLocked(ActivityRecord r) {
        synchronized (this.mAtms.mGlobalLock) {
            if (r != null) {
                ActivityStack stack = r.getActivityStack();
                if (stack != null) {
                    stack.finishActivityLocked(r, 0, (Intent) null, "finish-old-lock", false);
                }
            }
        }
    }

    private boolean checkAccessControlPassAsUser(String packageName, Intent intent, int userId) {
        boolean checkAccessControlPassLocked;
        synchronized (this) {
            checkAccessControlPassLocked = checkAccessControlPassLocked(packageName, intent, userId);
        }
        return checkAccessControlPassLocked;
    }

    private boolean checkAccessControlPassLocked(String packageName, Intent intent, int userId) {
        UserState userState = getUserStateLocked(userId);
        if (userState.mAccessControlPackages.get(packageName) != null) {
            return userState.mAccessControlPassPackages.containsKey(packageName);
        }
        return true;
    }

    private void readState() {
        if (this.mSettingsFile.getBaseFile().exists()) {
            FileInputStream fis = null;
            try {
                fis = this.mSettingsFile.openRead();
                readAccessControlPackage(fis);
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e) {
                    }
                }
            } catch (Exception e2) {
                Log.w(TAG, "Error reading package state", e2);
                if (fis != null) {
                    fis.close();
                }
            } catch (Throwable th) {
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        }
    }

    private void readAccessControlPackage(FileInputStream fis) throws Exception {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(fis, null);
        int eventType = parser.getEventType();
        while (eventType != 2 && eventType != 1) {
            eventType = parser.next();
        }
        if ("packages".equals(parser.getName())) {
            int eventType2 = parser.next();
            do {
                if (eventType2 == 2 && parser.getDepth() == 2 && BrightnessConstants.AppSplineXml.TAG_PACKAGE.equals(parser.getName())) {
                    String name = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                    AccessControlPackage ps = new AccessControlPackage(name);
                    int userHandle = 0;
                    String userHandleStr = parser.getAttributeValue(null, "u");
                    if (!TextUtils.isEmpty(userHandleStr)) {
                        userHandle = Integer.parseInt(userHandleStr);
                    }
                    ps.mFlags = Integer.parseInt(parser.getAttributeValue(null, "flags"));
                    ps.isHideIcon = Boolean.parseBoolean(parser.getAttributeValue(null, "isHideIcon"));
                    ps.isHideInRecent = Boolean.parseBoolean(parser.getAttributeValue(null, "isHideInRecent"));
                    ps.isHideNotice = Boolean.parseBoolean(parser.getAttributeValue(null, "isHideNotice"));
                    synchronized (this) {
                        getUserStateLocked(userHandle).mAccessControlPackages.put(name, ps);
                    }
                }
                eventType2 = parser.next();
            } while (eventType2 != 1);
        }
    }

    public void addAccessControlPassForUser(String packageName, int windowMode, int userId) {
        checkPermission();
        synchronized (this) {
            getUserStateLocked(userId).mAccessControlPassPackages.put(packageName, Integer.valueOf(windowMode));
            if (DEBUG) {
                Slog.d(TAG, "addAccessControlPassForUser: add pkgName=" + packageName + "  windowMode=" + windowMode + "  userId=" + userId + "  to mAccessControlPassPackages");
            }
        }
    }

    public void updateRusList(int type, List<String> addList, List<String> deleteList) {
        checkPermission();
        synchronized (this) {
            this.mAccessController.updateRusList(type, addList, deleteList);
        }
    }

    public Map getPrivacyAppInfo(int userId) {
        HashMap<String, Integer> privacyInfo;
        checkPermission();
        synchronized (this) {
            privacyInfo = new HashMap<>();
            HashMap<String, AccessControlPackage> packages = getUserStateLocked(userId).mAccessControlPackages;
            for (String pkgName : packages.keySet()) {
                try {
                    privacyInfo.put(pkgName, Integer.valueOf(packages.get(pkgName).mFlags));
                } catch (Exception e) {
                    Log.e(TAG, "getPrivacyAppInfo error", e);
                }
            }
        }
        return privacyInfo;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAccessControlEnabledLocked(UserState userState) {
        boolean z = true;
        if (Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "access_control_lock_enabled", 0, userState.mUserHandle) != 1) {
            z = false;
        }
        userState.mAccessControlEnabled = z;
        if (userState.mAccessControlEnabled) {
            clearPassPackagesByUser(userState.mUserHandle);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void updateAccessControlLockModeLocked(UserState userState) {
        userState.mAccessControlLockMode = Settings.Secure.getIntForUser(this.mContext.getContentResolver(), "access_control_lock_mode", 0, userState.mUserHandle);
    }

    private void initAccessControlSettingsLocked(UserState userState) {
        updateAccessControlEnabledLocked(userState);
        updateAccessControlLockModeLocked(userState);
        userState.mAccessControlSettingInit = true;
    }

    private boolean getAccessControlEnabledLocked(UserState userState) {
        UserState transferUserState = changeUserState(userState);
        if (!transferUserState.mAccessControlSettingInit) {
            initAccessControlSettingsLocked(transferUserState);
        }
        return transferUserState.mAccessControlEnabled;
    }

    private int getAccessControlLockMode(UserState userState) {
        UserState transferUserState = changeUserState(userState);
        if (!transferUserState.mAccessControlSettingInit) {
            initAccessControlSettingsLocked(transferUserState);
        }
        return transferUserState.mAccessControlLockMode;
    }

    private UserState changeUserState(UserState userState) {
        return getUserStateLocked(userState.mUserHandle == 999 ? 0 : userState.mUserHandle);
    }

    public void removeAccessControlPassAsUser(String packageName, int userId, boolean allUser) {
        checkPermission();
        synchronized (this) {
            int size = this.mUserStates.size();
            if (allUser) {
                for (int i = 0; i < size; i++) {
                    this.mUserStates.valueAt(i).mAccessControlPassPackages.clear();
                }
            } else {
                UserState userState = this.mUserStates.get(userId);
                if (userState != null && userState.mAccessControlPassPackages.containsKey(packageName)) {
                    userState.mAccessControlPassPackages.remove(packageName);
                }
            }
        }
    }

    private void clearPassPackagesByUser(int userId) {
        if (userId == 0 || 999 == userId) {
            getUserStateLocked(0).mAccessControlPassPackages.clear();
        } else {
            getUserStateLocked(userId).mAccessControlPassPackages.clear();
        }
    }

    private void clearPassPackages(int windowMode, int userId) {
        if (userId == 0 || 999 == userId) {
            UserState userStateOwner = getUserStateLocked(0);
            UserState userStateXSpace = getUserStateLocked(ColorMultiAppManagerService.USER_ID);
            removePassPackagesByWindowMode(userStateOwner, windowMode);
            removePassPackagesByWindowMode(userStateXSpace, windowMode);
            return;
        }
        removePassPackagesByWindowMode(getUserStateLocked(userId), windowMode);
    }

    private void removePassPackagesByWindowMode(UserState us, int windowMode) {
        HashMap<String, Integer> passPackages = us.mAccessControlPassPackages;
        passPackages.keySet();
        if (!passPackages.isEmpty()) {
            Iterator<Map.Entry<String, Integer>> it = passPackages.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, Integer> item = it.next();
                if (item.getValue().intValue() == windowMode) {
                    it.remove();
                    if (DEBUG) {
                        Slog.d(TAG, "removePassPackagesByWindowMode: remove pkgName=" + item.getKey() + "  windowMode=" + windowMode + "  userId=" + us.mUserHandle);
                    }
                }
            }
        }
    }

    public void checkGoToSleep(ActivityRecord activity, int userId) {
        if (activity == null) {
            for (int displayNdx = this.mAtms.mRootActivityContainer.getChildCount() - 1; displayNdx >= 0; displayNdx--) {
                ActivityDisplay display2 = this.mAtms.mRootActivityContainer.getChildAt(displayNdx);
                for (int stackNdx = display2.getChildCount() - 1; stackNdx >= 0; stackNdx--) {
                    ActivityStack stack = display2.getChildAt(stackNdx);
                    if (stack.mResumedActivity != null) {
                        goingToSleepLock(stack.mResumedActivity, stack.mResumedActivity.mUserId);
                    }
                }
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0097, code lost:
        if (r0 == false) goto L_0x00a5;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0099, code lost:
        startAccessControlActivity(r10, r13.intent, r13.info, r14, false, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:?, code lost:
        return;
     */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0035 A[Catch:{ all -> 0x00ad }] */
    private void goingToSleepLock(ActivityRecord activity, int userId) {
        Throwable th;
        boolean isSkip;
        int taskId;
        ActivityOptions options;
        boolean needIntercept = false;
        if (activity != null && activity.packageName != null) {
            activity.getDisplay().hasSplitScreenPrimaryStack();
            synchronized (this) {
                try {
                    UserState userState = getUserStateLocked(userId);
                    boolean enabled = getAccessControlEnabledLocked(userState);
                    if (!this.mAccessController.isSkipCheckActivity(activity.intent)) {
                        if (!this.mAccessController.isFilterAction(activity.intent)) {
                            isSkip = false;
                            if (enabled) {
                                if (!isSkip) {
                                    if (userState.mAccessControlPackages.get(activity.packageName) != null) {
                                        removeAccessControlPassAsUser(activity.packageName, userId, true);
                                        TaskRecord task = activity.getTaskRecord();
                                        if (task != null) {
                                            int taskId2 = task.taskId;
                                            ActivityOptions options2 = ActivityOptions.makeBasic();
                                            options2.setLaunchTaskId(taskId2);
                                            options2.setLaunchWindowingMode(activity.getWindowingMode());
                                            if (DEBUG) {
                                                Slog.d(TAG, "checkGoToSleep: activity=" + activity + "  windowMode=" + activity.getWindowingMode() + "  userId=" + userId);
                                            }
                                            needIntercept = true;
                                            taskId = taskId2;
                                            options = options2;
                                        }
                                    }
                                    taskId = -1;
                                    options = null;
                                    try {
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                }
                            }
                        }
                    }
                    isSkip = true;
                    if (enabled) {
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }
    }

    public void notifyZoomWindowExit(ActivityStack stack, boolean toFullScreen) {
        if (stack != null) {
            ActivityRecord activity = stack.getTopActivity();
            Slog.w(TAG, "notifyZoomWindowExit: stack=" + stack + "  windowMode=" + stack.getWindowingMode() + "  topActivity=" + activity + "  toFullScreen = " + toFullScreen);
            synchronized (this) {
                UserState userState = getUserStateLocked(stack.mCurrentUser);
                HashMap<String, Integer> passPackages = userState.mAccessControlPassPackages;
                if (getAccessControlLockMode(userState) == 0) {
                    if (activity != null && activity.packageName != null && toFullScreen && passPackages.containsKey(activity.packageName)) {
                        passPackages.put(activity.packageName, 1);
                    }
                    clearPassPackages(WINDOWING_MODE_ZOOM, stack.mCurrentUser);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x006c  */
    public void notifyInSplitScreenMode(ActivityStack stack) {
        boolean isSkip;
        ActivityRecord activity = null;
        if (stack != null) {
            activity = stack.getTopActivity();
        }
        if (activity != null) {
            if (DEBUG) {
                Slog.d(TAG, "notifyInSplitScreenMode: stack=" + stack + "  activity=" + activity + "  windowMode=" + stack.getWindowingMode());
            }
            activity.intent.getComponent().getClassName();
            String packageName = activity.packageName;
            if (packageName != null) {
                synchronized (this) {
                    UserState userState = getUserStateLocked(activity.mUserId);
                    boolean enabled = getAccessControlEnabledLocked(userState);
                    if (!this.mAccessController.isSkipCheckActivity(activity.intent)) {
                        if (!this.mAccessController.isFilterAction(activity.intent)) {
                            isSkip = false;
                            if (enabled) {
                                if (!isSkip) {
                                    int lockMode = getAccessControlLockMode(userState);
                                    HashMap<String, Integer> passPackages = userState.mAccessControlPassPackages;
                                    if (passPackages.containsKey(packageName) && lockMode == 0) {
                                        clearPassPackages(passPackages.get(packageName).intValue(), activity.mUserId);
                                        passPackages.put(packageName, Integer.valueOf(stack.getWindowingMode()));
                                    }
                                    return;
                                }
                            }
                        }
                    }
                    isSkip = true;
                    if (enabled) {
                    }
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:0x004f  */
    public void onSplitScreenModeDismissed(ActivityStack topFullscreenStack) {
        boolean isSkip;
        if (DEBUG) {
            Slog.d(TAG, "onSplitScreenModeDismissed: topFullscreenStack=" + topFullscreenStack);
        }
        synchronized (this) {
            if (topFullscreenStack != null) {
                ActivityRecord activity = topFullscreenStack.getTopActivity();
                if (activity != null) {
                    String packageName = activity.packageName;
                    if (packageName != null) {
                        UserState userState = getUserStateLocked(activity.mUserId);
                        boolean enabled = getAccessControlEnabledLocked(userState);
                        if (!this.mAccessController.isSkipCheckActivity(activity.intent)) {
                            if (!this.mAccessController.isFilterAction(activity.intent)) {
                                isSkip = false;
                                if (enabled) {
                                    if (!isSkip) {
                                        int lockMode = getAccessControlLockMode(userState);
                                        HashMap<String, Integer> passPackages = userState.mAccessControlPassPackages;
                                        if (lockMode == 0) {
                                            if (passPackages.containsKey(packageName)) {
                                                clearPassPackages(passPackages.get(packageName).intValue(), activity.mUserId);
                                                passPackages.put(packageName, Integer.valueOf(topFullscreenStack.getWindowingMode()));
                                            } else {
                                                Slog.i(TAG, "onSplitScreenModeDismissed: clear split screen passPackages");
                                                clearPassPackages(3, activity.mUserId);
                                                clearPassPackages(4, activity.mUserId);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        isSkip = true;
                        if (enabled) {
                        }
                    }
                }
            }
        }
    }

    public boolean isAccessControlPassForUser(String packageName, int userId) {
        boolean containsKey;
        synchronized (this) {
            try {
                containsKey = getUserStateLocked(userId).mAccessControlPassPackages.containsKey(packageName);
            } catch (Exception e) {
                return false;
            }
        }
        return containsKey;
    }

    public boolean isAppUnlockPasswordActivity(ActivityRecord r) {
        if (r == null || r.mActivityComponent == null || !OPPO_SAFECENTER_PASSWORD.equals(r.mActivityComponent.flattenToShortString())) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:39:0x008a, code lost:
        if (r15 == false) goto L_0x00bb;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x008c, code lost:
        android.util.Slog.w(com.android.server.wm.ColorAccessControlManagerService.TAG, "shouldAbortMoveTaskToFront: task=" + r18 + "  topActivity=" + r10);
        startAccessControlActivity(r18.taskId, r10.intent, r10.info, r10.mUserId, false, r16);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00ba, code lost:
        return true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00bb, code lost:
        return false;
     */
    /* JADX WARNING: Removed duplicated region for block: B:20:0x004e A[Catch:{ all -> 0x00c5 }] */
    public boolean shouldAbortMoveTaskToFront(TaskRecord task) {
        ActivityRecord topActivity;
        Throwable th;
        boolean isSkip;
        ActivityOptions options;
        boolean needIntercept;
        if (task == null || (topActivity = task.getTopActivity()) == null) {
            return false;
        }
        topActivity.mActivityComponent.getClassName();
        String packageName = topActivity.packageName;
        if (packageName == null) {
            return false;
        }
        boolean isKeyguardLock = this.mAtms.mStackSupervisor.getKeyguardController().isKeyguardLocked();
        synchronized (this) {
            try {
                UserState userState = getUserStateLocked(topActivity.mUserId);
                boolean enabled = getAccessControlEnabledLocked(userState);
                if (!this.mAccessController.isSkipCheckActivity(topActivity.intent)) {
                    if (!this.mAccessController.isFilterAction(topActivity.intent)) {
                        isSkip = false;
                        if (enabled) {
                            if (!isSkip) {
                                HashMap<String, Integer> passPackages = userState.mAccessControlPassPackages;
                                AccessControlPackage ap = userState.mAccessControlPackages.get(packageName);
                                if (!isKeyguardLock) {
                                    if (ap == null || passPackages.containsKey(packageName)) {
                                        needIntercept = false;
                                        options = null;
                                    } else {
                                        ActivityOptions options2 = ActivityOptions.makeBasic();
                                        options2.setLaunchTaskId(task.taskId);
                                        needIntercept = true;
                                        options = options2;
                                    }
                                    try {
                                    } catch (Throwable th2) {
                                        th = th2;
                                        throw th;
                                    }
                                } else if (ap == null || !this.mAccessController.isHideActivity(topActivity.intent)) {
                                    return false;
                                } else {
                                    return true;
                                }
                            }
                        }
                        return false;
                    }
                }
                isSkip = true;
                if (enabled) {
                }
                return false;
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:50:0x00de, code lost:
        r7 = r20.getTaskRecord();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x00e4, code lost:
        if (r7 == null) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x00e6, code lost:
        r1 = r7.taskId;
        r0 = android.app.ActivityOptions.makeBasic();
        r0.setLaunchTaskId(r1);
        r0.setLaunchWindowingMode(r20.getWindowingMode());
        r17 = r0;
        r0 = r1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x00fa, code lost:
        r17 = null;
        r0 = -1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00ff, code lost:
        if (com.android.server.wm.ColorAccessControlManagerService.DEBUG == false) goto L_0x011f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x0101, code lost:
        android.util.Slog.d(com.android.server.wm.ColorAccessControlManagerService.TAG, "interceptResumeActivity: taskId=" + r0 + "  activity=" + r20);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:0x011f, code lost:
        startAccessControlActivity(r0, r20.intent, r20.info, r21, r15, r17);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x0130, code lost:
        return true;
     */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00d1  */
    public boolean interceptResumeActivity(ActivityRecord activity, int userId) {
        boolean isSkip;
        if (activity == null) {
            return false;
        }
        if (Objects.equals(PASSWORD_ACTION, activity.intent.getAction()) && Objects.equals(PACKAGE_SECURITYCENTER, activity.packageName)) {
            finishOldAccessControlActivityLocked(findAccessControlActivityLocked(activity, activity.getWindowingMode()));
        }
        ActivityStack freeFormStack = findFreeformStackLocked();
        boolean inSplitScreenMode = activity.getDisplay().hasSplitScreenPrimaryStack();
        String cpName = activity.mActivityComponent.getClassName();
        String packageName = activity.packageName;
        if (packageName == null) {
            return false;
        }
        boolean isKeyguardLock = this.mAtms.mStackSupervisor.getKeyguardController().isKeyguardLocked();
        synchronized (this) {
            UserState userState = getUserStateLocked(userId);
            if (!getAccessControlEnabledLocked(userState)) {
                return false;
            }
            int lockMode = getAccessControlLockMode(userState);
            HashMap<String, Integer> passPackages = userState.mAccessControlPassPackages;
            AccessControlPackage ap = userState.mAccessControlPackages.get(packageName);
            if (!isKeyguardLock) {
                if (freeFormStack == null && lockMode == 0) {
                    clearPassPackages(5, userId);
                    clearPassPackages(WINDOWING_MODE_ZOOM, userId);
                }
                if (passPackages.containsKey(packageName)) {
                    if (lockMode == 0) {
                        clearPassPackages(passPackages.get(packageName).intValue(), userId);
                        passPackages.put(packageName, Integer.valueOf(activity.getWindowingMode()));
                    }
                    return false;
                }
                if (!this.mAccessController.isSkipCheckActivity(activity.intent)) {
                    if (!this.mAccessController.isFilterAction(activity.intent)) {
                        isSkip = false;
                        boolean isIgnoreAppSwitch = this.mAccessController.isIgnoreAppSwitchActivity(activity.intent);
                        if (ap != null) {
                            if (!isSkip) {
                                if (lockMode == 0) {
                                    clearPassPackages(activity.getWindowingMode(), userId);
                                }
                            }
                        }
                        if (lockMode == 0 && activity.fullscreen && !cpName.contains("PipMenuActivity") && !isIgnoreAppSwitch) {
                            clearPassPackages(activity.getWindowingMode(), userId);
                        }
                        if (inSplitScreenMode && lockMode == 0) {
                            if (DEBUG) {
                                Slog.d(TAG, "interceptResumeActivity: clear split screen passPackages");
                            }
                            clearPassPackages(3, userId);
                            clearPassPackages(4, userId);
                        }
                        return false;
                    }
                }
                isSkip = true;
                boolean isIgnoreAppSwitch2 = this.mAccessController.isIgnoreAppSwitchActivity(activity.intent);
                if (ap != null) {
                }
                clearPassPackages(activity.getWindowingMode(), userId);
                if (inSplitScreenMode) {
                }
                return false;
            } else if (ap == null || !this.mAccessController.isHideActivity(activity.intent)) {
                return false;
            } else {
                return true;
            }
        }
    }

    private void checkSplitScreenVisible(ActivityStack stack) {
        ActivityRecord top;
        boolean isSkip;
        if (stack != null && stack.inSplitScreenPrimaryWindowingMode() && stack != this.mAtms.mRootActivityContainer.getTopDisplayFocusedStack() && (top = stack.getTopActivity()) != null && !top.finishing) {
            String packageName = top.packageName;
            synchronized (this) {
                UserState userState = getUserStateLocked(top.mUserId);
                int lockMode = getAccessControlLockMode(userState);
                HashMap<String, Integer> passPackages = userState.mAccessControlPassPackages;
                AccessControlPackage ap = userState.mAccessControlPackages.get(packageName);
                if (!this.mAccessController.isSkipCheckActivity(top.intent)) {
                    if (!this.mAccessController.isFilterAction(top.intent)) {
                        isSkip = false;
                        if (lockMode == 0 && ap != null && !isSkip && !passPackages.containsKey(packageName)) {
                            passPackages.put(packageName, 3);
                        }
                    }
                }
                isSkip = true;
                passPackages.put(packageName, 3);
            }
        }
    }

    private void startAccessControlActivity(final int taskId, Intent oriIntent, ActivityInfo aInfo, int userId, boolean showWhenLock, final ActivityOptions options) {
        final Intent intent = getCheckAccessIntent(aInfo, oriIntent, -1, null, userId, null);
        intent.putExtra("task_id", taskId);
        intent.putExtra("show_when_lock", showWhenLock);
        if (options != null) {
            intent.putExtra("Launch_Windowing_Mode", options.getLaunchWindowingMode());
        }
        this.mAtms.mH.postAtFrontOfQueue(new Runnable() {
            /* class com.android.server.wm.ColorAccessControlManagerService.AnonymousClass1 */

            public void run() {
                try {
                    if (ColorAccessControlManagerService.this.isAccessControlActivityInTopTask(taskId)) {
                        return;
                    }
                    if (options != null) {
                        ColorAccessControlManagerService.this.mContext.startActivityAsUser(intent, options.toBundle(), UserHandle.CURRENT);
                    } else {
                        ColorAccessControlManagerService.this.mContext.startActivityAsUser(intent, UserHandle.CURRENT);
                    }
                } catch (Exception e) {
                    Log.e(ColorAccessControlManagerService.TAG, "startAccessControlActivit", e);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean isAccessControlActivityInTopTask(int taskId) {
        try {
            synchronized (this.mAtms.mGlobalLock) {
                TaskRecord task = this.mAtms.mRootActivityContainer.anyTaskForId(taskId);
                if (!(task == null || task.getTopActivity() == null)) {
                    ActivityRecord top = task.getTopActivity();
                    if (isAppUnlockPasswordActivity(top)) {
                        Slog.w(TAG, "isAccessControlActivityInTopTask:  accessControl Activity =" + top + "  had in top task =" + task);
                        return true;
                    }
                }
                return false;
            }
        } catch (Exception e) {
            Log.e(TAG, "isAccessControlActivityInTopTask  exception", e);
            return false;
        }
    }

    public void notifyAccessControlStateChanged(ColorAccessControlInfo info) {
        try {
            int size = this.mIColorAccessControlObservers.beginBroadcast();
            for (int i = 0; i < size; i++) {
                IColorAccessControlObserver listener = this.mIColorAccessControlObservers.getBroadcastItem(i);
                try {
                    Slog.v(TAG, "notifyAccessControlStateChanged: ");
                    listener.onAccessControlStateChange(info);
                } catch (Exception e) {
                    Log.e(TAG, "Error notifyAccessControlStateChanged changed event.", e);
                }
            }
            this.mIColorAccessControlObservers.finishBroadcast();
        } catch (Exception e2) {
            Log.e(TAG, "Exception notifyAccessControlStateChanged changed event.", e2);
        }
    }

    public ColorAccessControlInfo getAccessControlInfo(AccessControlPackage ps) {
        ColorAccessControlInfo info = new ColorAccessControlInfo();
        info.mName = ps.mName;
        info.isEncrypted = true;
        info.isHideIcon = ps.isHideIcon;
        info.isHideInRecent = ps.isHideInRecent;
        info.isHideNotice = ps.isHideNotice;
        return info;
    }

    public ColorAccessControlInfo getAccessControlInfoIfEncryptCancel(String packageName) {
        ColorAccessControlInfo info = new ColorAccessControlInfo();
        info.mName = packageName;
        info.isEncrypted = false;
        info.isHideIcon = false;
        info.isHideInRecent = false;
        info.isHideNotice = false;
        return info;
    }

    public void setPrivacyAppsInfoForUser(Map privacyInfo, boolean enabled, int userId) {
        checkPermission();
        synchronized (this) {
            try {
                HashMap<String, Integer> tempInfo = (HashMap) privacyInfo;
                if (tempInfo != null && !tempInfo.isEmpty()) {
                    UserState userStateLocked = getUserStateLocked(userId);
                    for (Map.Entry<String, Integer> privacyEntry : tempInfo.entrySet()) {
                        String packageName = privacyEntry.getKey();
                        int flags = privacyEntry.getValue().intValue();
                        if (!enabled) {
                            userStateLocked.mAccessControlPackages.remove(packageName);
                            removeAccessControlPassAsUser(packageName, userId, false);
                            notifyAccessControlStateChanged(getAccessControlInfoIfEncryptCancel(packageName));
                        } else if (packageName != null) {
                            updateAccessControlPackage(userStateLocked.mAccessControlPackages, packageName, flags);
                        }
                    }
                    scheduleWriteLocked();
                }
            } catch (Exception e) {
                Slog.w(TAG, "setPrivacyAppsInfoForUser exception");
                e.getStackTrace();
            }
        }
    }

    public boolean getApplicationAccessControlEnabledAsUser(String packageName, int userId) {
        boolean applicationAccessControlEnabledLocked;
        synchronized (this) {
            applicationAccessControlEnabledLocked = getApplicationAccessControlEnabledLocked(packageName, userId);
        }
        return applicationAccessControlEnabledLocked;
    }

    private boolean getApplicationAccessControlEnabledLocked(String packageName, int userId) {
        try {
            if (getUserStateLocked(userId).mAccessControlPackages.get(packageName) != null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private void checkPermission() {
        int permission = this.mContext.checkCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE");
        int privatePermission = this.mContext.checkCallingOrSelfPermission("com.oppo.permission.safe.PRIVATE");
        if (permission != 0 && privatePermission != 0) {
            throw new SecurityException("Permission Denial: attempt to change application state from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid() + "  in ColorAccessControlManagerService");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private UserState getUserStateLocked(int userHandle) {
        UserState userState = this.mUserStates.get(userHandle);
        if (userState != null) {
            return userState;
        }
        UserState userState2 = new UserState();
        userState2.mUserHandle = userHandle;
        synchronized (this.mUserStateLock) {
            this.mUserStates.put(userHandle, userState2);
        }
        return userState2;
    }

    private void updateAccessControlPackage(HashMap<String, AccessControlPackage> packages, String packageName, int flags) {
        AccessControlPackage ps = packages.get(packageName);
        if (ps == null) {
            ps = new AccessControlPackage(packageName);
            packages.put(packageName, ps);
        }
        ps.mFlags = flags;
        boolean z = false;
        ps.isHideIcon = (flags & 1) != 0;
        ps.isHideInRecent = (flags & 2) != 0;
        if ((flags & 4) != 0) {
            z = true;
        }
        ps.isHideNotice = z;
        notifyAccessControlStateChanged(getAccessControlInfo(ps));
    }

    public boolean registerAccessControlObserver(IColorAccessControlObserver observer) {
        checkPermission();
        if (observer == null) {
            return false;
        }
        return this.mIColorAccessControlObservers.register(observer);
    }

    public boolean unregisterAccessControlObserver(IColorAccessControlObserver observer) {
        checkPermission();
        return this.mIColorAccessControlObservers.unregister(observer);
    }

    private void scheduleWriteLocked() {
        if (!this.mAccessControlHandler.hasMessages(1)) {
            this.mAccessControlHandler.sendEmptyMessageDelayed(1, 1000);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writeState() {
        try {
            ArrayList<UserState> userStates = new ArrayList<>();
            synchronized (this) {
                int size = this.mUserStates.size();
                for (int i = 0; i < size; i++) {
                    UserState state = this.mUserStates.valueAt(i);
                    UserState userState = new UserState();
                    userState.mUserHandle = state.mUserHandle;
                    userState.mAccessControlPackages.putAll(new HashMap(state.mAccessControlPackages));
                    userStates.add(userState);
                }
            }
            FileOutputStream fos = this.mSettingsFile.startWrite();
            XmlSerializer out = new FastXmlSerializer();
            out.setOutput(fos, "utf-8");
            out.startDocument(null, true);
            out.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            out.startTag(null, "packages");
            out.attribute(null, "updateVersion", UPDATE_VERSION);
            Iterator<UserState> it = userStates.iterator();
            while (it.hasNext()) {
                UserState userState2 = it.next();
                for (AccessControlPackage ps : userState2.mAccessControlPackages.values()) {
                    out.startTag(null, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                    out.attribute(null, BrightnessConstants.AppSplineXml.TAG_NAME, ps.mName);
                    out.attribute(null, "flags", String.valueOf(ps.mFlags));
                    out.attribute(null, "isHideIcon", String.valueOf(ps.isHideIcon));
                    out.attribute(null, "isHideInRecent", String.valueOf(ps.isHideInRecent));
                    out.attribute(null, "isHideNotice", String.valueOf(ps.isHideNotice));
                    out.attribute(null, "u", String.valueOf(userState2.mUserHandle));
                    out.endTag(null, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                }
            }
            out.endTag(null, "packages");
            out.endDocument();
            this.mSettingsFile.finishWrite(fos);
        } catch (IOException e1) {
            Log.w(TAG, "Error writing package settings file", e1);
            if (0 != 0) {
                this.mSettingsFile.failWrite((FileOutputStream) null);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (DumpUtils.checkDumpPermission(this.mContext, TAG, pw)) {
            boolean isKeyguardLock = this.mAtms.mStackSupervisor.getKeyguardController().isKeyguardLocked();
            String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
            pw.println("Dump time : " + currentDateTimeString);
            pw.println("AccessControl Dump Info");
            pw.println("isKeyguardLock =" + isKeyguardLock);
            int opti = 0;
            while (opti < args.length) {
                this.mArgs = args;
                this.mNextArg = 1;
                String opt = args[opti];
                if (opt == null || opt.length() <= 0 || opt.charAt(0) != '-') {
                    break;
                }
                opti++;
                if ("-h".equals(opt)) {
                    showUsage(pw);
                    return;
                } else if ("-d".equals(opt)) {
                    runDebug(fd, pw);
                    return;
                } else {
                    pw.println("Unknow argument: " + opt + "; user -h for help");
                }
            }
            runDebugList(pw);
        }
    }

    private String nextArg() {
        int i = this.mNextArg;
        String[] strArr = this.mArgs;
        if (i >= strArr.length) {
            return null;
        }
        String arg = strArr[i];
        this.mNextArg = i + 1;
        return arg;
    }

    private void showUsage(PrintWriter pw) {
        pw.println("Access Control Manager dump options:");
        pw.println("  [-h][-d]]");
        pw.println("  -d list          list the all of debug zones");
        pw.println("  -d enable <zone zone>");
        pw.println("  -d disable <zone zone>");
        pw.println("zone usage:");
        pw.println("    a  : ALL DEBUG");
    }

    private void runDebug(FileDescriptor fd, PrintWriter pw) {
        String type = nextArg();
        if (type == null) {
            showUsage(pw);
        } else if ("list".equals(type)) {
            runDebugList(pw);
        } else if ("enable".equals(type)) {
            runDebugEnable(pw, true);
        } else if ("disable".equals(type)) {
            runDebugEnable(pw, false);
        }
    }

    private void runDebugList(PrintWriter pw) {
        runDebugAccessControlInfo(pw);
        this.mAccessController.dump(pw);
    }

    private void runDebugAccessControlInfo(PrintWriter pw) {
        pw.println();
        int size = this.mUserStates.size();
        if (size > 0) {
            for (int i = 0; i < size; i++) {
                UserState userState = this.mUserStates.valueAt(i);
                if (!userState.mAccessControlPackages.isEmpty()) {
                    int N = userState.mAccessControlPackages.size();
                    pw.print("Now " + N + " packages in accessControl state , userId=" + userState.mUserHandle);
                    if (userState.mUserHandle != 999) {
                        pw.print("  ");
                        pw.print("Enabled=");
                        pw.print(userState.mAccessControlEnabled);
                        pw.print("  ");
                        pw.print("LockMode=");
                        pw.print(userState.mAccessControlLockMode);
                    }
                    pw.println();
                    for (String pkgName : userState.mAccessControlPackages.keySet()) {
                        AccessControlPackage ps = userState.mAccessControlPackages.get(pkgName);
                        pw.print("  ");
                        pw.print(pkgName);
                        pw.print("  ");
                        pw.print("isHideIcon=");
                        pw.print(ps.isHideIcon);
                        pw.print("  ");
                        pw.print("isHideInRecent=");
                        pw.print(ps.isHideInRecent);
                        pw.print("  ");
                        pw.print("isHideNotice=");
                        pw.print(ps.isHideNotice);
                        pw.println();
                    }
                } else {
                    pw.println("0 AccessControl package in user =" + userState.mUserHandle);
                }
                if (!userState.mAccessControlPassPackages.isEmpty()) {
                    int N2 = userState.mAccessControlPassPackages.size();
                    pw.println("Now " + N2 + " Pass packages , userId=" + userState.mUserHandle);
                    for (String pkgName2 : userState.mAccessControlPassPackages.keySet()) {
                        pw.print("  ");
                        pw.println(pkgName2 + "   windowMode=" + userState.mAccessControlPassPackages.get(pkgName2));
                    }
                } else {
                    pw.println("0 Pass package in user =" + userState.mUserHandle);
                }
            }
            return;
        }
        pw.println("0 UserState");
    }

    private void runDebugEnable(PrintWriter pw, boolean enable) {
        String nextArg;
        String type = nextArg();
        if (type == null) {
            showUsage(pw);
            return;
        }
        do {
            if ("a".equals(type)) {
                DEBUG = enable;
            } else if ("0".equals(type)) {
                DEBUG = enable;
            }
            nextArg = nextArg();
            type = nextArg;
        } while (nextArg != null);
    }

    private class SettingsObserver extends ContentObserver {
        private final Uri mAccessControlLockEnabledUri = Settings.Secure.getUriFor("access_control_lock_enabled");
        private final Uri mAccessControlLockModedUri = Settings.Secure.getUriFor("access_control_lock_mode");

        public SettingsObserver(Handler handler, Context context) {
            super(handler);
            ContentResolver resolver = context.getContentResolver();
            resolver.registerContentObserver(this.mAccessControlLockEnabledUri, false, this, -1);
            resolver.registerContentObserver(this.mAccessControlLockModedUri, false, this, -1);
        }

        @Override // android.database.ContentObserver
        public void onChange(boolean selfChange, Uri uri, int userId) {
            if (ColorAccessControlManagerService.DEBUG) {
                Slog.d(ColorAccessControlManagerService.TAG, "onChange:  uri : " + uri + " userId : " + userId);
            }
            synchronized (ColorAccessControlManagerService.this) {
                UserState userState = ColorAccessControlManagerService.this.getUserStateLocked(userId);
                if (this.mAccessControlLockEnabledUri.equals(uri)) {
                    ColorAccessControlManagerService.this.updateAccessControlEnabledLocked(userState);
                } else if (this.mAccessControlLockModedUri.equals(uri)) {
                    ColorAccessControlManagerService.this.updateAccessControlLockModeLocked(userState);
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public class AccessControlHandler extends Handler {
        AccessControlHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                Process.setThreadPriority(0);
                synchronized (ColorAccessControlManagerService.this.mSettingsFile) {
                    removeMessages(1);
                    ColorAccessControlManagerService.this.writeState();
                }
                Process.setThreadPriority(10);
            }
        }
    }

    /* access modifiers changed from: private */
    public static final class UserState {
        boolean mAccessControlEnabled;
        int mAccessControlLockMode;
        final HashMap<String, AccessControlPackage> mAccessControlPackages;
        final HashMap<String, Integer> mAccessControlPassPackages;
        boolean mAccessControlSettingInit;
        int mUserHandle;

        private UserState() {
            this.mAccessControlPackages = new HashMap<>();
            this.mAccessControlPassPackages = new HashMap<>();
            this.mAccessControlLockMode = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public class AccessControlPackage {
        boolean isHideIcon = false;
        boolean isHideInRecent;
        boolean isHideNotice;
        int mFlags;
        String mName;

        AccessControlPackage(String name) {
            this.mName = name;
        }
    }
}
