package com.android.server.retaildemo;

import android.app.ActivityManagerInternal;
import android.app.ActivityManagerNative;
import android.app.AppGlobals;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.RetailDemoModeServiceInternal;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.RemoteException;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.CallLog.Calls;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.KeyValueListParser;
import android.util.Slog;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.os.BackgroundThread;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.LocalServices;
import com.android.server.LocationManagerService;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.OppoPermissionConstants;
import java.io.File;
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
public class RetailDemoModeService extends SystemService {
    private static final String ACTION_RESET_DEMO = "com.android.server.retaildemo.ACTION_RESET_DEMO";
    private static final boolean DEBUG = false;
    private static final String DEMO_SESSION_COUNT = "retail_demo_session_count";
    private static final String DEMO_SESSION_DURATION = "retail_demo_session_duration";
    private static final String DEMO_USER_NAME = "Demo";
    private static final long MILLIS_PER_SECOND = 1000;
    private static final int MSG_INACTIVITY_TIME_OUT = 1;
    private static final int MSG_START_NEW_SESSION = 2;
    private static final int MSG_TURN_SCREEN_ON = 0;
    private static final long SCREEN_WAKEUP_DELAY = 2500;
    private static final String SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED = "sys.retaildemo.enabled";
    private static final String TAG = null;
    private static final long USER_INACTIVITY_TIMEOUT_DEFAULT = 90000;
    private static final long USER_INACTIVITY_TIMEOUT_MIN = 10000;
    private static final int[] VOLUME_STREAMS_TO_MUTE = null;
    private static final long WARNING_DIALOG_TIMEOUT_DEFAULT = 0;
    final Object mActivityLock;
    private ActivityManagerInternal mAmi;
    private ActivityManagerService mAms;
    private AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver;
    private String[] mCameraIdsWithFlash;
    private CameraManager mCameraManager;
    int mCurrentUserId;
    boolean mDeviceInDemoMode;
    @GuardedBy("mActivityLock")
    long mFirstUserActivityTime;
    Handler mHandler;
    private ServiceThread mHandlerThread;
    @GuardedBy("mActivityLock")
    long mLastUserActivityTime;
    private RetailDemoModeServiceInternal mLocalService;
    private NotificationManager mNm;
    private PowerManager mPm;
    private PreloadAppsInstaller mPreloadAppsInstaller;
    private PendingIntent mResetDemoPendingIntent;
    private Configuration mSystemUserConfiguration;
    private UserManager mUm;
    long mUserInactivityTimeout;
    @GuardedBy("mActivityLock")
    boolean mUserUntouched;
    private WakeLock mWakeLock;
    long mWarningDialogTimeout;
    private WifiManager mWifiManager;

    final class MainHandler extends Handler {
        MainHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (RetailDemoModeService.this.mWakeLock.isHeld()) {
                        RetailDemoModeService.this.mWakeLock.release();
                    }
                    RetailDemoModeService.this.mWakeLock.acquire();
                    return;
                case 1:
                    if (RetailDemoModeService.this.isDemoLauncherDisabled()) {
                        Slog.i(RetailDemoModeService.TAG, "User inactivity timeout reached");
                        RetailDemoModeService.this.showInactivityCountdownDialog();
                        return;
                    }
                    return;
                case 2:
                    removeMessages(2);
                    removeMessages(1);
                    if (RetailDemoModeService.this.mCurrentUserId != 0) {
                        RetailDemoModeService.this.logSessionDuration();
                    }
                    UserInfo demoUser = RetailDemoModeService.this.getUserManager().createUser(RetailDemoModeService.DEMO_USER_NAME, 768);
                    if (demoUser != null) {
                        RetailDemoModeService.this.setupDemoUser(demoUser);
                        RetailDemoModeService.this.getActivityManager().switchUser(demoUser.id);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class SettingsObserver extends ContentObserver {
        private static final String KEY_USER_INACTIVITY_TIMEOUT = "user_inactivity_timeout_ms";
        private static final String KEY_WARNING_DIALOG_TIMEOUT = "warning_dialog_timeout_ms";
        private final Uri mDeviceDemoModeUri = Global.getUriFor("device_demo_mode");
        private final Uri mDeviceProvisionedUri = Global.getUriFor("device_provisioned");
        private final KeyValueListParser mParser = new KeyValueListParser(',');
        private final Uri mRetailDemoConstantsUri = Global.getUriFor("retail_demo_mode_constants");

        public SettingsObserver(Handler handler) {
            super(handler);
        }

        public void register() {
            ContentResolver cr = RetailDemoModeService.this.getContext().getContentResolver();
            cr.registerContentObserver(this.mDeviceDemoModeUri, false, this, 0);
            cr.registerContentObserver(this.mDeviceProvisionedUri, false, this, 0);
            cr.registerContentObserver(this.mRetailDemoConstantsUri, false, this, 0);
        }

        public void onChange(boolean selfChange, Uri uri) {
            if (this.mRetailDemoConstantsUri.equals(uri)) {
                refreshTimeoutConstants();
                return;
            }
            if (this.mDeviceDemoModeUri.equals(uri)) {
                RetailDemoModeService.this.mDeviceInDemoMode = UserManager.isDeviceInDemoMode(RetailDemoModeService.this.getContext());
                if (RetailDemoModeService.this.mDeviceInDemoMode) {
                    RetailDemoModeService.this.putDeviceInDemoMode();
                } else {
                    SystemProperties.set(RetailDemoModeService.SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED, "0");
                    if (RetailDemoModeService.this.mWakeLock.isHeld()) {
                        RetailDemoModeService.this.mWakeLock.release();
                    }
                }
            }
            if (!RetailDemoModeService.this.mDeviceInDemoMode && RetailDemoModeService.this.isDeviceProvisioned()) {
                BackgroundThread.getHandler().post(new Runnable() {
                    public void run() {
                        if (!RetailDemoModeService.this.deletePreloadsFolderContents()) {
                            Slog.w(RetailDemoModeService.TAG, "Failed to delete preloads folder contents");
                        }
                    }
                });
            }
        }

        private void refreshTimeoutConstants() {
            try {
                this.mParser.setString(Global.getString(RetailDemoModeService.this.getContext().getContentResolver(), "retail_demo_mode_constants"));
            } catch (IllegalArgumentException e) {
                Slog.e(RetailDemoModeService.TAG, "Invalid string passed to KeyValueListParser");
            }
            RetailDemoModeService.this.mWarningDialogTimeout = this.mParser.getLong(KEY_WARNING_DIALOG_TIMEOUT, 0);
            RetailDemoModeService.this.mUserInactivityTimeout = this.mParser.getLong(KEY_USER_INACTIVITY_TIMEOUT, RetailDemoModeService.USER_INACTIVITY_TIMEOUT_DEFAULT);
            RetailDemoModeService.this.mUserInactivityTimeout = Math.max(RetailDemoModeService.this.mUserInactivityTimeout, 10000);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.retaildemo.RetailDemoModeService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.retaildemo.RetailDemoModeService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.retaildemo.RetailDemoModeService.<clinit>():void");
    }

    private void showInactivityCountdownDialog() {
        UserInactivityCountdownDialog dialog = new UserInactivityCountdownDialog(getContext(), this.mWarningDialogTimeout, 1000);
        dialog.setNegativeButtonClickListener(null);
        dialog.setPositiveButtonClickListener(new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RetailDemoModeService.this.mHandler.sendEmptyMessage(2);
            }
        });
        dialog.setOnCountDownExpiredListener(new OnCountDownExpiredListener() {
            public void onCountDownExpired() {
                RetailDemoModeService.this.mHandler.sendEmptyMessage(2);
            }
        });
        dialog.show();
    }

    public RetailDemoModeService(Context context) {
        super(context);
        this.mDeviceInDemoMode = false;
        this.mCurrentUserId = 0;
        this.mActivityLock = new Object();
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (RetailDemoModeService.this.mDeviceInDemoMode) {
                    String action = intent.getAction();
                    if (action.equals("android.intent.action.SCREEN_OFF")) {
                        RetailDemoModeService.this.mHandler.removeMessages(0);
                        RetailDemoModeService.this.mHandler.sendEmptyMessageDelayed(0, RetailDemoModeService.SCREEN_WAKEUP_DELAY);
                    } else if (action.equals(RetailDemoModeService.ACTION_RESET_DEMO)) {
                        RetailDemoModeService.this.mHandler.sendEmptyMessage(2);
                    }
                }
            }
        };
        this.mLocalService = new RetailDemoModeServiceInternal() {
            private static final long USER_ACTIVITY_DEBOUNCE_TIME = 2000;

            /* JADX WARNING: Missing block: B:18:0x0044, code:
            r9.this$0.mHandler.removeMessages(1);
            r9.this$0.mHandler.sendEmptyMessageDelayed(1, r9.this$0.mUserInactivityTimeout);
     */
            /* JADX WARNING: Missing block: B:19:0x0056, code:
            return;
     */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void onUserActivity() {
                if (RetailDemoModeService.this.mDeviceInDemoMode) {
                    long timeOfActivity = SystemClock.uptimeMillis();
                    synchronized (RetailDemoModeService.this.mActivityLock) {
                        if (timeOfActivity < RetailDemoModeService.this.mLastUserActivityTime + USER_ACTIVITY_DEBOUNCE_TIME) {
                            return;
                        }
                        RetailDemoModeService.this.mLastUserActivityTime = timeOfActivity;
                        if (RetailDemoModeService.this.mUserUntouched && RetailDemoModeService.this.isDemoLauncherDisabled()) {
                            Slog.d(RetailDemoModeService.TAG, "retail_demo first touch");
                            RetailDemoModeService.this.mUserUntouched = false;
                            RetailDemoModeService.this.mFirstUserActivityTime = timeOfActivity;
                        }
                    }
                }
            }
        };
        synchronized (this.mActivityLock) {
            long uptimeMillis = SystemClock.uptimeMillis();
            this.mLastUserActivityTime = uptimeMillis;
            this.mFirstUserActivityTime = uptimeMillis;
        }
    }

    private Notification createResetNotification() {
        return new Builder(getContext()).setContentTitle(getContext().getString(17040899)).setContentText(getContext().getString(17040900)).setOngoing(true).setSmallIcon(17302876).setShowWhen(false).setVisibility(1).setContentIntent(getResetDemoPendingIntent()).setColor(getContext().getColor(17170523)).build();
    }

    private PendingIntent getResetDemoPendingIntent() {
        if (this.mResetDemoPendingIntent == null) {
            this.mResetDemoPendingIntent = PendingIntent.getBroadcast(getContext(), 0, new Intent(ACTION_RESET_DEMO), 0);
        }
        return this.mResetDemoPendingIntent;
    }

    boolean isDemoLauncherDisabled() {
        int enabledState = 0;
        try {
            enabledState = AppGlobals.getPackageManager().getComponentEnabledSetting(ComponentName.unflattenFromString(getContext().getResources().getString(17039476)), this.mCurrentUserId);
        } catch (RemoteException exc) {
            Slog.e(TAG, "Unable to talk to Package Manager", exc);
        }
        return enabledState == 2;
    }

    private void setupDemoUser(UserInfo userInfo) {
        UserManager um = getUserManager();
        UserHandle user = UserHandle.of(userInfo.id);
        um.setUserRestriction("no_config_wifi", true, user);
        um.setUserRestriction("no_install_unknown_sources", true, user);
        um.setUserRestriction("no_config_mobile_networks", true, user);
        um.setUserRestriction("no_usb_file_transfer", true, user);
        um.setUserRestriction("no_modify_accounts", true, user);
        um.setUserRestriction("no_config_bluetooth", true, user);
        um.setUserRestriction("no_outgoing_calls", false, user);
        getUserManager().setUserRestriction("no_safe_boot", true, UserHandle.SYSTEM);
        Secure.putIntForUser(getContext().getContentResolver(), "skip_first_use_hints", 1, userInfo.id);
        Global.putInt(getContext().getContentResolver(), "package_verifier_enable", 0);
        grantRuntimePermissionToCamera(user);
        clearPrimaryCallLog();
    }

    private void grantRuntimePermissionToCamera(UserHandle user) {
        Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        PackageManager pm = getContext().getPackageManager();
        ResolveInfo handler = pm.resolveActivityAsUser(cameraIntent, 786432, user.getIdentifier());
        if (handler != null && handler.activityInfo != null) {
            try {
                pm.grantRuntimePermission(handler.activityInfo.packageName, OppoPermissionConstants.PERMISSION_ACCESS, user);
            } catch (Exception e) {
            }
        }
    }

    private void clearPrimaryCallLog() {
        try {
            getContext().getContentResolver().delete(Calls.CONTENT_URI, null, null);
        } catch (Exception e) {
            Slog.w(TAG, "Deleting call log failed: " + e);
        }
    }

    void logSessionDuration() {
        int sessionDuration;
        synchronized (this.mActivityLock) {
            sessionDuration = (int) ((this.mLastUserActivityTime - this.mFirstUserActivityTime) / 1000);
        }
        MetricsLogger.histogram(getContext(), DEMO_SESSION_DURATION, sessionDuration);
    }

    private ActivityManagerService getActivityManager() {
        if (this.mAms == null) {
            this.mAms = (ActivityManagerService) ActivityManagerNative.getDefault();
        }
        return this.mAms;
    }

    private UserManager getUserManager() {
        if (this.mUm == null) {
            this.mUm = (UserManager) getContext().getSystemService(UserManager.class);
        }
        return this.mUm;
    }

    private AudioManager getAudioManager() {
        if (this.mAudioManager == null) {
            this.mAudioManager = (AudioManager) getContext().getSystemService(AudioManager.class);
        }
        return this.mAudioManager;
    }

    private boolean isDeviceProvisioned() {
        if (Global.getInt(getContext().getContentResolver(), "device_provisioned", 0) != 0) {
            return true;
        }
        return false;
    }

    private boolean deletePreloadsFolderContents() {
        File dir = Environment.getDataPreloadsDirectory();
        Slog.i(TAG, "Deleting contents of " + dir);
        return FileUtils.deleteContents(dir);
    }

    private void registerBroadcastReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction(ACTION_RESET_DEMO);
        getContext().registerReceiver(this.mBroadcastReceiver, filter);
    }

    private String[] getCameraIdsWithFlash() {
        ArrayList<String> cameraIdsList = new ArrayList();
        try {
            for (String cameraId : this.mCameraManager.getCameraIdList()) {
                if (Boolean.TRUE.equals(this.mCameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.FLASH_INFO_AVAILABLE))) {
                    cameraIdsList.add(cameraId);
                }
            }
        } catch (CameraAccessException e) {
            Slog.e(TAG, "Unable to access camera while getting camera id list", e);
        }
        return (String[]) cameraIdsList.toArray(new String[cameraIdsList.size()]);
    }

    private void turnOffAllFlashLights() {
        for (String cameraId : this.mCameraIdsWithFlash) {
            try {
                this.mCameraManager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                Slog.e(TAG, "Unable to access camera " + cameraId + " while turning off flash", e);
            }
        }
    }

    private void muteVolumeStreams() {
        for (int stream : VOLUME_STREAMS_TO_MUTE) {
            getAudioManager().setStreamVolume(stream, getAudioManager().getStreamMinVolume(stream), 0);
        }
    }

    private Configuration getSystemUsersConfiguration() {
        if (this.mSystemUserConfiguration == null) {
            ContentResolver contentResolver = getContext().getContentResolver();
            Configuration configuration = new Configuration();
            this.mSystemUserConfiguration = configuration;
            System.getConfiguration(contentResolver, configuration);
        }
        return this.mSystemUserConfiguration;
    }

    private void putDeviceInDemoMode() {
        SystemProperties.set(SYSTEM_PROPERTY_RETAIL_DEMO_ENABLED, LocationManagerService.OPPO_FAKE_LOCATOIN_SWITCH_ON);
        this.mHandler.sendEmptyMessage(2);
    }

    public void onStart() {
        this.mHandlerThread = new ServiceThread(TAG, -2, false);
        this.mHandlerThread.start();
        this.mHandler = new MainHandler(this.mHandlerThread.getLooper());
        publishLocalService(RetailDemoModeServiceInternal.class, this.mLocalService);
    }

    public void onBootPhase(int bootPhase) {
        switch (bootPhase) {
            case 600:
                this.mPreloadAppsInstaller = new PreloadAppsInstaller(getContext());
                this.mPm = (PowerManager) getContext().getSystemService("power");
                this.mAmi = (ActivityManagerInternal) LocalServices.getService(ActivityManagerInternal.class);
                this.mWakeLock = this.mPm.newWakeLock(268435482, TAG);
                this.mNm = NotificationManager.from(getContext());
                this.mWifiManager = (WifiManager) getContext().getSystemService("wifi");
                this.mCameraManager = (CameraManager) getContext().getSystemService("camera");
                this.mCameraIdsWithFlash = getCameraIdsWithFlash();
                SettingsObserver settingsObserver = new SettingsObserver(this.mHandler);
                settingsObserver.register();
                settingsObserver.refreshTimeoutConstants();
                registerBroadcastReceiver();
                return;
            case 1000:
                if (UserManager.isDeviceInDemoMode(getContext())) {
                    this.mDeviceInDemoMode = true;
                    putDeviceInDemoMode();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void onSwitchUser(final int userId) {
        if (!this.mDeviceInDemoMode) {
            return;
        }
        if (getUserManager().getUserInfo(userId).isDemo()) {
            if (!this.mWakeLock.isHeld()) {
                this.mWakeLock.acquire();
            }
            this.mCurrentUserId = userId;
            this.mAmi.updatePersistentConfigurationForUser(getSystemUsersConfiguration(), userId);
            turnOffAllFlashLights();
            muteVolumeStreams();
            if (!this.mWifiManager.isWifiEnabled()) {
                this.mWifiManager.setWifiEnabled(true);
            }
            new LockPatternUtils(getContext()).setLockScreenDisabled(true, userId);
            this.mNm.notifyAsUser(TAG, 1, createResetNotification(), UserHandle.of(userId));
            synchronized (this.mActivityLock) {
                this.mUserUntouched = true;
            }
            MetricsLogger.count(getContext(), DEMO_SESSION_COUNT, 1);
            this.mHandler.removeMessages(1);
            this.mHandler.post(new Runnable() {
                public void run() {
                    RetailDemoModeService.this.mPreloadAppsInstaller.installApps(userId);
                }
            });
            return;
        }
        Slog.wtf(TAG, "Should not allow switch to non-demo user in demo mode");
    }
}
