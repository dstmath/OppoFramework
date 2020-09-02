package com.android.server.engineer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.database.ContentObserver;
import android.debug.AdbManagerInternal;
import android.debug.IAdbTransport;
import android.engineer.IOppoEngineerManager;
import android.engineer.OppoEngineerInternal;
import android.net.Uri;
import android.os.Binder;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.ServiceThread;
import com.android.server.SystemService;
import com.android.server.content.SyncStorageEngine;
import com.android.server.engineer.OppoEngineerConfig;
import com.android.server.engineer.util.SecrecyServiceHelper;
import com.android.server.lights.Light;
import com.android.server.lights.LightsManager;
import com.android.server.oppo.TemperatureProvider;
import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public class OppoEngineerService extends SystemService {
    private static final String TAG = "OppoEngineerService";
    private BinderService mBinderService;
    /* access modifiers changed from: private */
    public Light mBreathLight;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        /* class com.android.server.engineer.OppoEngineerService.AnonymousClass1 */

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.ACTION_SHUTDOWN".equals(intent.getAction()) && OppoEngineerUtils.isMtkPlatform()) {
                Slog.d(OppoEngineerService.TAG, "auto reset md mode while shutdown");
                try {
                    SystemProperties.set("vendor.oppo.quit.atm", Boolean.toString(true));
                } catch (Exception e) {
                    Slog.i(OppoEngineerService.TAG, "set reset atm property caught exception : " + e.getMessage());
                }
            }
        }
    };
    /* access modifiers changed from: private */
    public Light mButtonLight;
    /* access modifiers changed from: private */
    public final Context mContext;
    private final OppoEngineerHandler mHandler;
    private LightsManager mLightsManager;
    /* access modifiers changed from: private */
    public OppoEngineerConfig mOppoEngineerConfig;
    /* access modifiers changed from: private */
    public int mRpmbEnableState = -1;
    private final ServiceThread mServiceThread;
    private SettingsObserver mSettingsObserver;
    private OppoEngineerConfig.ISwitchUpdateListener mSwitchUpdateListener = new OppoEngineerConfig.ISwitchUpdateListener() {
        /* class com.android.server.engineer.OppoEngineerService.AnonymousClass2 */

        @Override // com.android.server.engineer.OppoEngineerConfig.ISwitchUpdateListener
        public void onEngineerConfigUpdate() {
        }

        @Override // com.android.server.engineer.OppoEngineerConfig.ISwitchUpdateListener
        public void onEngineerConfigUpdateFromProvider() {
            boolean needResetAtm = OppoEngineerService.this.mOppoEngineerConfig.needResetAtm();
            if (OppoEngineerUtils.isMtkPlatform() && needResetAtm) {
                if (OppoEngineerService.this.resetWriteProtectStateInternal()) {
                    Slog.i(OppoEngineerService.TAG, "reset wp state success");
                    try {
                        SystemProperties.set("sys.usb.config.meta", Boolean.toString(false));
                    } catch (Exception e) {
                        Slog.i(OppoEngineerService.TAG, "set usb charge switch caught exception : " + e.getMessage());
                    }
                    OppoEngineerUtils.writeLogToPartition(102, "WriteProtectResetDone", "ANDROID", "WriteProtectIssue", "WriteProtectResetDone");
                    return;
                }
                Slog.i(OppoEngineerService.TAG, "reset wp state fail");
                OppoEngineerService.this.writeWpIssueToCriticalLog();
            }
        }
    };

    public OppoEngineerService(Context context) {
        super(context);
        this.mContext = context;
        this.mServiceThread = new ServiceThread(TAG, -2, true);
        this.mServiceThread.start();
        this.mHandler = new OppoEngineerHandler(this.mServiceThread.getLooper());
        this.mLightsManager = (LightsManager) LocalServices.getService(LightsManager.class);
        this.mButtonLight = this.mLightsManager.getLight(2);
        this.mBreathLight = this.mLightsManager.getLight(4);
        this.mSettingsObserver = new SettingsObserver(this.mHandler);
        this.mOppoEngineerConfig = new OppoEngineerConfig(this.mContext, this.mSwitchUpdateListener);
        ((AdbManagerInternal) LocalServices.getService(AdbManagerInternal.class)).registerTransport(new AdbTransport(this.mHandler));
    }

    private final class LocalService extends OppoEngineerInternal {
        private LocalService() {
        }

        public boolean handleStartActivity(ActivityInfo activityInfo, String callingPackage, int callingUid, int callingPid) {
            if (activityInfo == null) {
                return false;
            }
            if (OppoEngineerService.this.isActivityEncrypt(activityInfo)) {
                return true;
            }
            return OppoEngineerService.this.isActivityInBlackListInternal(new ComponentName(activityInfo.packageName, activityInfo.name));
        }

        public boolean handleStartServiceOrBindService(Intent intent) {
            if (intent == null || intent.getComponent() == null) {
                return false;
            }
            return OppoEngineerService.this.isServiceInBlackListInternal(intent.getComponent());
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isActivityEncrypt(ActivityInfo activityInfo) {
        if (activityInfo == null || !SecrecyServiceHelper.isSecrecySupported() || !SecrecyServiceHelper.isInEncryptedAppList(activityInfo, null, -1, -1)) {
            return false;
        }
        Slog.e(TAG, activityInfo + " is isInEncryptedAppList ");
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isActivityInBlackListInternal(ComponentName componentName) {
        OppoEngineerConfig oppoEngineerConfig;
        if (componentName == null || (oppoEngineerConfig = this.mOppoEngineerConfig) == null || !oppoEngineerConfig.isActivityInBlackList(componentName)) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean isServiceInBlackListInternal(ComponentName componentName) {
        OppoEngineerConfig oppoEngineerConfig = this.mOppoEngineerConfig;
        return oppoEngineerConfig != null && oppoEngineerConfig.isServiceInBlackList(componentName);
    }

    /* access modifiers changed from: package-private */
    public boolean isShellCommandInBlackListInternal(String command) {
        OppoEngineerConfig oppoEngineerConfig = this.mOppoEngineerConfig;
        return oppoEngineerConfig != null && oppoEngineerConfig.isShellCommandInBlackList(command);
    }

    /* access modifiers changed from: package-private */
    public boolean isEngineerOrderInBlackListInternal(String order) {
        OppoEngineerConfig oppoEngineerConfig = this.mOppoEngineerConfig;
        return oppoEngineerConfig != null && oppoEngineerConfig.isEngineerOrderInBlackList(order);
    }

    private class OppoEngineerHandler extends Handler {
        public OppoEngineerHandler(Looper looper) {
            super(looper, null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
        }
    }

    /* access modifiers changed from: private */
    public boolean checkPermission() {
        int uid = Binder.getCallingUid();
        if (UserHandle.getAppId(uid) == 1000 || UserHandle.getAppId(uid) == 1001) {
            return true;
        }
        Slog.d(TAG, "Permission Denial : can't access from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        return false;
    }

    private final class BinderService extends IOppoEngineerManager.Stub {
        private static final String TORCH_SWITCH_FILE_PATH = "/proc/qcom_flash";

        private BinderService() {
        }

        public void turnButtonLightOn(int brightness) {
            if (OppoEngineerService.this.checkPermission() && OppoEngineerService.this.mButtonLight != null) {
                OppoEngineerService.this.mButtonLight.setBrightness(brightness);
            }
        }

        public void turnButtonLightOff() {
            if (OppoEngineerService.this.checkPermission() && OppoEngineerService.this.mButtonLight != null) {
                OppoEngineerService.this.mButtonLight.turnOff();
            }
        }

        public void turnBreathLightOn(int color) {
            if (OppoEngineerService.this.checkPermission() && OppoEngineerService.this.mButtonLight != null) {
                OppoEngineerService.this.mBreathLight.setColor(color);
            }
        }

        public void turnBreathLightFlashOn(int color) {
            if (OppoEngineerService.this.checkPermission()) {
                int defaultLightOn = OppoEngineerService.this.mContext.getResources().getInteger(17694777);
                int defaultLightOff = OppoEngineerService.this.mContext.getResources().getInteger(17694776);
                if (OppoEngineerService.this.mButtonLight != null) {
                    OppoEngineerService.this.mBreathLight.setFlashing(color, 1, defaultLightOn, defaultLightOff);
                }
            }
        }

        public void turnBreathLightOff() {
            if (OppoEngineerService.this.checkPermission() && OppoEngineerService.this.mButtonLight != null) {
                OppoEngineerService.this.mBreathLight.turnOff();
            }
        }

        public void setTorchState(String state) {
            if (OppoEngineerService.this.checkPermission()) {
                try {
                    FileUtils.stringToFile(TORCH_SWITCH_FILE_PATH, state);
                } catch (IOException e) {
                    Slog.i(OppoEngineerService.TAG, "setTorchState state=" + state + " failed : " + e.getMessage());
                }
            }
        }

        public String getDownloadStatus() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerService.this.getDownloadStatusInternal();
        }

        public boolean isSerialPortEnabled() {
            String serialPort;
            if (OppoEngineerService.this.checkPermission() && (serialPort = OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(100002))) != null && serialPort.equals("ENABLE_UART:TRUE")) {
                return true;
            }
            return false;
        }

        public boolean setSerialPortState(boolean enable) {
            byte[] status;
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            if (enable) {
                status = "ENABLE_UART:TRUE".getBytes(StandardCharsets.UTF_8);
            } else {
                status = "ENABLE_UART:FALSE".getBytes(StandardCharsets.UTF_8);
            }
            return OppoEngineerNative.nativeSaveEngineerData(100003, status, status.length);
        }

        public byte[] getEmmcHealthInfo() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerNative.nativeReadEngineerData(100004);
        }

        public boolean isPartionWriteProtectDisabled() {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeGetPartionWriteProtectState();
        }

        public boolean disablePartionWriteProtect(boolean disable) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeSetPartionWriteProtectState(disable);
        }

        public boolean resetWriteProtectState() {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerService.this.resetWriteProtectStateInternal();
        }

        public String getBackCoverColorId() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerService.this.getBackCoverColorIdInternal();
        }

        public boolean setBackCoverColorId(String colorId) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerService.this.setBackCoverColorIdInternal(colorId);
        }

        public String getCarrierVersion() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000008));
        }

        public boolean setCarrierVersion(String version) {
            if (!OppoEngineerService.this.checkPermission() || version == null) {
                return false;
            }
            byte[] versionData = version.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000009, versionData, versionData.length);
        }

        public String getRegionNetlockStatus() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000010));
        }

        public boolean setRegionNetlock(String lock) {
            if (!OppoEngineerService.this.checkPermission() || lock == null) {
                return false;
            }
            byte[] lockData = lock.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000011, lockData, lockData.length);
        }

        public String getTelcelSimlockStatus() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000012));
        }

        public boolean setTelcelSimlock(String lock) {
            if (!OppoEngineerService.this.checkPermission() || lock == null) {
                return false;
            }
            byte[] lockData = lock.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000013, lockData, lockData.length);
        }

        public String getTelcelSimlockUnlockTimes() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000014));
        }

        public boolean setTelcelSimlockUnlockTimes(String times) {
            if (!OppoEngineerService.this.checkPermission() || times == null) {
                return false;
            }
            byte[] timesData = times.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000015, timesData, timesData.length);
        }

        public String getSingleDoubleCardStatus() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000016));
        }

        public boolean setSingleDoubleCard(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000017, stateData, stateData.length);
        }

        public byte[] getBadBatteryConfig(int offset, int size) {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerNative.nativeGetBadBatteryConfig(offset, size);
        }

        public int setBatteryBatteryConfig(int offset, int size, byte[] data) {
            if (!OppoEngineerService.this.checkPermission()) {
                return -1;
            }
            return OppoEngineerNative.nativeSetBatteryBatteryConfig(offset, size, data);
        }

        public byte[] getProductLineTestResult() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerNative.nativeGetProductLineTestResult();
        }

        public boolean setProductLineTestResult(int position, int result) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeSetProductLineTestResult(position, result);
        }

        public boolean resetProductLineTestResult() {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeResetProductLineTestResult();
        }

        public byte[] getCarrierVersionFromNvram() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerNative.nativeReadEngineerData(1000023);
        }

        public boolean saveCarrierVersionToNvram(byte[] version) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeSaveEngineerData(1000024, version, version.length);
        }

        public byte[] getCalibrationStatusFromNvram() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerNative.nativeReadEngineerData(1000025);
        }

        public String getSimOperatorSwitchStatus() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000027));
        }

        public boolean setSimOperatorSwitch(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000028, stateData, stateData.length);
        }

        public String getDeviceLockStatus() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000033));
        }

        public boolean setDeviceLockStatus(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000034, stateData, stateData.length);
        }

        public String getDeviceLockIMSI() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000035));
        }

        public boolean setDeviceLockIMSI(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000036, stateData, stateData.length);
        }

        public String getDeviceLockDays() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000037));
        }

        public boolean setDeviceLockDays(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000038, stateData, stateData.length);
        }

        public String getDeviceLockLastBindTime() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000039));
        }

        public boolean setDeviceLockLastBindTime(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000040, stateData, stateData.length);
        }

        public String getDeviceLockICCID() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000041));
        }

        public boolean setDeviceLockICCID(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000042, stateData, stateData.length);
        }

        public String getDeviceLockFirstBindTime() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000043));
        }

        public boolean setDeviceLockFirstBindTime(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000044, stateData, stateData.length);
        }

        public String getDeviceLockUnlockTime() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000045));
        }

        public boolean setDeviceLockUnlockTime(String state) {
            if (!OppoEngineerService.this.checkPermission() || state == null) {
                return false;
            }
            byte[] stateData = state.getBytes(StandardCharsets.UTF_8);
            return OppoEngineerNative.nativeSaveEngineerData(1000046, stateData, stateData.length);
        }

        public String getBootImgWaterMark() {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000029));
        }

        public byte[] readEngineerData(int type) {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerNative.nativeReadEngineerData(type);
        }

        public boolean saveEngineerData(int type, byte[] engineerData, int length) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeSaveEngineerData(type, engineerData, length);
        }

        public boolean fastbootUnlock(byte[] data, int length) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeSaveEngineerData(1000055, data, length);
        }

        public void setSystemProperties(String key, String val) {
            if (OppoEngineerService.this.checkPermission()) {
                try {
                    SystemProperties.set(key, val);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        public String getSystemProperties(String key, String val) {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            try {
                return SystemProperties.get(key, val);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        public boolean saveOppoUsageRecords(String path, String usageRecord, boolean isSingleRecord) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            return OppoEngineerNative.nativeSaveOppoUsageRecords(path, usageRecord, isSingleRecord);
        }

        public boolean isEngineerItemInBlackList(int type, String item) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            if (type == 1) {
                return OppoEngineerService.this.isActivityInBlackListInternal(ComponentName.unflattenFromString(item));
            }
            if (type == 2) {
                return OppoEngineerService.this.isServiceInBlackListInternal(ComponentName.unflattenFromString(item));
            }
            if (type == 3) {
                return OppoEngineerService.this.isShellCommandInBlackListInternal(item);
            }
            if (type != 4) {
                return false;
            }
            return OppoEngineerService.this.isEngineerOrderInBlackListInternal(item);
        }

        public String getOppoUsageRecords(String path) {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadOppoUsageRecords(path));
        }

        private boolean saveHeytapIDDUID(String duid) {
            if (!TextUtils.isEmpty(duid)) {
                byte[] duidData = duid.getBytes(StandardCharsets.UTF_8);
                return OppoEngineerNative.nativeSaveEngineerData(1000066, duidData, duidData.length);
            }
            Slog.e(OppoEngineerService.TAG, "invalid duid");
            return false;
        }

        public boolean saveHeytapID(int type, String id) {
            if (!OppoEngineerService.this.checkPermission()) {
                return false;
            }
            if (type == 0) {
                return saveHeytapIDDUID(id);
            }
            Slog.e(OppoEngineerService.TAG, "invalid type");
            return false;
        }

        private String getHeytapIDUDID() {
            return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000067));
        }

        public String getHeytapID(int type) {
            if (!OppoEngineerService.this.checkPermission()) {
                return null;
            }
            if (type == 0) {
                return getHeytapIDUDID();
            }
            Slog.e(OppoEngineerService.TAG, "invalid type");
            return null;
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            int uid = Binder.getCallingUid();
            if (UserHandle.getAppId(uid) == 1000 || UserHandle.getAppId(uid) == 1017 || UserHandle.getAppId(uid) == 2000 || OppoEngineerService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
                OppoEngineerService.this.dumpInternal(fd, pw, args);
                return;
            }
            pw.println("Permission Denial: can't dump engineer from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
        }
    }

    /* JADX WARN: Type inference failed for: r0v2, types: [com.android.server.engineer.OppoEngineerService$BinderService, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        Slog.i(TAG, "publishBinderService ENGINEER_SERVICE");
        this.mBinderService = new BinderService();
        publishBinderService("engineer", this.mBinderService);
        publishLocalService(OppoEngineerInternal.class, new LocalService());
    }

    @Override // com.android.server.SystemService
    public void onBootPhase(int phase) {
        if (phase == 1000) {
            Slog.i(TAG, "on PHASE_BOOT_COMPLETED");
            OppoEngineerConfig oppoEngineerConfig = this.mOppoEngineerConfig;
            if (oppoEngineerConfig != null) {
                oppoEngineerConfig.initUpdateBroadcastReceiver();
            }
            if (SystemProperties.getBoolean("persist.sys.oppo.bootenable", false)) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.engineer.OppoEngineerService.AnonymousClass3 */

                    public void run() {
                        OppoEngineerService.this.startWifiAdbHelper();
                    }
                });
            }
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.ACTION_SHUTDOWN");
            this.mContext.registerReceiver(this.mBroadcastReceiver, filter, "oppo.permission.OPPO_COMPONENT_SAFE", this.mHandler);
            if (OppoEngineerUtils.isMtkPlatform() && OppoEngineerNative.nativeGetPartionWriteProtectState()) {
                this.mHandler.post(new Runnable() {
                    /* class com.android.server.engineer.OppoEngineerService.AnonymousClass4 */

                    public void run() {
                        OppoEngineerService oppoEngineerService = OppoEngineerService.this;
                        oppoEngineerService.addPartionProtectNotification(oppoEngineerService.mContext);
                        if (SystemProperties.getBoolean("ro.build.release_type", false) && SecrecyServiceHelper.isSecrecySupported() && SecrecyServiceHelper.getSecrecyState(4)) {
                            OppoEngineerService.this.writeWpIssueToCriticalLog();
                        }
                    }
                });
            }
            this.mHandler.post(new Runnable() {
                /* class com.android.server.engineer.OppoEngineerService.AnonymousClass5 */

                public void run() {
                    if (OppoEngineerUtils.isMtkPlatform()) {
                        String serialFromProperty = SystemProperties.get("ro.serialno", "");
                        if (!TextUtils.isEmpty(serialFromProperty)) {
                            String serialFromNv = OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000018));
                            Slog.d(OppoEngineerService.TAG, "getSerialNoFromNvram serialFromNv=" + serialFromNv);
                            if (!serialFromProperty.equals(serialFromNv)) {
                                byte[] serialData = serialFromProperty.getBytes(StandardCharsets.UTF_8);
                                boolean ret = OppoEngineerNative.nativeSaveEngineerData(1000019, serialData, serialData.length);
                                StringBuilder sb = new StringBuilder();
                                sb.append("update serial to nvram ");
                                sb.append(ret ? SyncStorageEngine.MESG_SUCCESS : "fail");
                                Slog.d(OppoEngineerService.TAG, sb.toString());
                            }
                        }
                        byte[] oppoProductinfo = OppoEngineerNative.nativeReadEngineerData(1000026);
                        if (oppoProductinfo == null || oppoProductinfo.length != 128) {
                            Slog.d(OppoEngineerService.TAG, "OPPO_PRODUCT_INFO empty");
                            return;
                        }
                        int numberLength = 0;
                        int i = 0;
                        while (true) {
                            if (i >= 16) {
                                break;
                            } else if (oppoProductinfo[i] == 0) {
                                numberLength = i;
                                break;
                            } else {
                                if (i == 15) {
                                    numberLength = 16;
                                }
                                i++;
                            }
                        }
                        if (numberLength > 0) {
                            String factoryNumber = new String(oppoProductinfo, 0, numberLength);
                            Slog.i(OppoEngineerService.TAG, "factoryNumber:" + factoryNumber);
                            try {
                                SystemProperties.set("oppo.eng.factory.no", factoryNumber);
                            } catch (Exception e) {
                                Slog.i(OppoEngineerService.TAG, "set factory number caught exception : " + e.getMessage());
                            }
                        }
                    }
                }
            });
            new Thread() {
                /* class com.android.server.engineer.OppoEngineerService.AnonymousClass6 */

                public void run() {
                    if (new File("/proc/ldmp_wdt_test").exists()) {
                        Slog.d(OppoEngineerService.TAG, "WATCH_DOG_IC_TEST_PATH found");
                        String result = OppoEngineerUtils.readLineFromFile("/proc/ldmp_wdt_test");
                        if ("PASS".equals(result)) {
                            OppoEngineerNative.nativeSetProductLineTestResult(83, 1);
                        } else if ("FAILED".equals(result)) {
                            OppoEngineerNative.nativeSetProductLineTestResult(83, 2);
                        } else {
                            OppoEngineerNative.nativeSetProductLineTestResult(83, 0);
                        }
                    }
                    Object fps = OppoEngineerUtils.getFingerprintPaySerice();
                    if (fps != null) {
                        int unused = OppoEngineerService.this.mRpmbEnableState = OppoEngineerUtils.getRpmbEnableState(fps);
                    } else {
                        Slog.e(OppoEngineerService.TAG, "get fps failed");
                    }
                    if ("3".equals(SystemProperties.get("vendor.oppo.engineer.adb.flag"))) {
                        String address = SystemProperties.get("vendor.oppo.engineer.adb.address");
                        Slog.i(OppoEngineerService.TAG, "address = " + address);
                        if (OppoEngineerUtils.isServerLinkAvailable(address)) {
                            Intent intent = new Intent();
                            intent.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.wireless.WifiAdbHelper"));
                            intent.addFlags(268435456);
                            intent.putExtra("server_address", address);
                            try {
                                if (OppoEngineerService.this.mContext.getPackageManager().resolveActivity(intent, 0) != null) {
                                    OppoEngineerService.this.mContext.startActivity(intent);
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }.start();
            if (!isProvisioned(this.mContext)) {
                this.mContext.getContentResolver().registerContentObserver(Settings.Global.getUriFor("device_provisioned"), false, this.mSettingsObserver, -1);
            }
        } else if (480 == phase) {
            String colorId = getBackCoverColorIdInternal();
            if (colorId != null) {
                try {
                    SystemProperties.set("ro.hw.phone.color", colorId);
                } catch (Exception e) {
                    Slog.i(TAG, "set color id property caught exception : " + e.getMessage());
                }
            }
            String downloadMode = getDownloadModeInternal();
            if (!TextUtils.isEmpty(downloadMode) && downloadMode.contains("\"login_mode\":\t\"production\"")) {
                try {
                    SystemProperties.set("sys.oppo.production.login.mode", Boolean.toString(true));
                } catch (Exception e2) {
                    Slog.i(TAG, "set download mode property caught exception : " + e2.getMessage());
                }
            }
            String serial = getOppoSerialNoInternal();
            if (serial != null) {
                try {
                    SystemProperties.set("persist.sys.oppo.serialno", serial);
                } catch (Exception e3) {
                    Slog.i(TAG, "set oppo serial no caught exception : " + e3.getMessage());
                }
            }
        }
    }

    private static class AdbTransport extends IAdbTransport.Stub {
        private final Handler mHandler;

        AdbTransport(Handler handler) {
            this.mHandler = handler;
        }

        public void onAdbEnabled(final boolean enabled) {
            this.mHandler.post(new Runnable() {
                /* class com.android.server.engineer.OppoEngineerService.AdbTransport.AnonymousClass1 */

                public void run() {
                    if (!enabled && SystemProperties.getInt("persist.sys.adb.engineermode", 1) == 0) {
                        Slog.i(OppoEngineerService.TAG, "reset engineer mode adb property");
                        SystemProperties.set("persist.sys.allcommode", TemperatureProvider.SWITCH_OFF);
                        SystemProperties.set("persist.sys.oppo.usbactive", TemperatureProvider.SWITCH_OFF);
                        SystemProperties.set("persist.sys.adb.engineermode", "1");
                        SystemProperties.set("vendor.oppo.engineer.usb.config", "midi");
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    public void startWifiAdbHelper() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.wireless.WifiAdbHelper"));
        intent.addFlags(268435456);
        try {
            if (this.mContext.getPackageManager().resolveActivity(intent, 0) != null) {
                this.mContext.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getOppoSerialNoInternal() {
        return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000059));
    }

    private final class SettingsObserver extends ContentObserver {
        SettingsObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange, Uri uri) {
            OppoEngineerService oppoEngineerService = OppoEngineerService.this;
            if (oppoEngineerService.isProvisioned(oppoEngineerService.mContext)) {
                boolean ret = OppoEngineerNative.nativeSaveEngineerData(1000056, new byte[512], 512);
                try {
                    SystemProperties.set("sys.oppo.production.login.mode", "");
                } catch (Exception e) {
                    Slog.i(OppoEngineerService.TAG, "set download mode property caught exception : " + e.getMessage());
                }
                Slog.i(OppoEngineerService.TAG, "reset download model flag result = " + ret);
            }
        }
    }

    /* access modifiers changed from: private */
    public void writeWpIssueToCriticalLog() {
        Slog.i(TAG, "writeWpIssueToCriticalLog");
        String pcba = SystemProperties.get("gsm.serial", "UNKNOWN");
        StringBuilder criticalLogSb = new StringBuilder();
        criticalLogSb.append("[");
        criticalLogSb.append(pcba);
        criticalLogSb.append("]");
        criticalLogSb.append("[WP OFF ISSUE]");
        if (SystemProperties.getBoolean("sys.usb.config.meta", false)) {
            criticalLogSb.append("[USB Charge Disable]");
        }
        if (!"normal".equals(SystemProperties.get("persist.vendor.atm.mdmode", "normal"))) {
            criticalLogSb.append("[MODEM META MODE]");
        }
        OppoEngineerUtils.writeLogToPartition(101, criticalLogSb.toString(), "ANDROID", "WriteProtectIssue", "WriteProtectNeedReset");
    }

    private void clearWpIssue() {
        OppoEngineerUtils.cleanItem(101);
        OppoEngineerUtils.cleanItem(1125);
        OppoEngineerUtils.syncCacheToEmmc();
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v0, resolved type: com.android.server.engineer.OppoEngineerShell} */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r4v0, types: [android.os.Binder, com.android.server.engineer.OppoEngineerService$BinderService] */
    /* access modifiers changed from: private */
    public void dumpInternal(FileDescriptor fd, PrintWriter pw, String[] args) {
        if (args != null && args.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (String str : args) {
                sb.append(str);
                sb.append("    ");
            }
            Slog.i(TAG, "dumpInternal args is : " + sb.toString().trim());
        }
        new OppoEngineerShell(this, this.mContext).exec(this.mBinderService, null, fd, null, args, null, new ResultReceiver(null));
    }

    /* access modifiers changed from: private */
    public void addPartionProtectNotification(Context context) {
        Slog.i(TAG, "addPartionProtectNotification");
        NotificationManager notificationManager = (NotificationManager) context.getSystemService("notification");
        if (notificationManager == null) {
            Slog.i(TAG, "system server not ready");
            return;
        }
        Object titleResId = OppoEngineerUtils.getDeclaredField(null, "com.oppo.internal.R$string", "zzz_engineeringmode_warning_title");
        Object messageResId = OppoEngineerUtils.getDeclaredField(null, "com.oppo.internal.R$string", "zzz_engineeringmode_warning_message");
        if (titleResId == null || messageResId == null) {
            Slog.e(TAG, "res not found");
            return;
        }
        notificationManager.createNotificationChannel(new NotificationChannel("com.oppo.engineermode", this.mContext.getString(((Integer) titleResId).intValue()), 3));
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(17301651);
        builder.setAutoCancel(false);
        builder.setContentTitle(this.mContext.getString(((Integer) titleResId).intValue()));
        builder.setContentText(this.mContext.getString(((Integer) messageResId).intValue()));
        builder.setShowWhen(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setChannelId("com.oppo.engineermode");
        Notification status = builder.build();
        status.flags = 34;
        status.priority = 2;
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.oppo.engineermode", "com.oppo.engineermode.wireless.NormalModeWarningPage"));
        intent.addFlags(268435456);
        status.contentIntent = PendingIntent.getActivityAsUser(context, 0, intent, 0, null, UserHandle.CURRENT);
        notificationManager.notify(10000, status);
    }

    /* access modifiers changed from: private */
    public boolean isProvisioned(Context context) {
        return Settings.Global.getInt(context.getContentResolver(), "device_provisioned", 0) == 1;
    }

    /* access modifiers changed from: package-private */
    public String getBackCoverColorIdInternal() {
        String colorString = OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(100005));
        if (OppoEngineerUtils.isBackCoverColorIdValid(colorString)) {
            return colorString;
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public String getDownloadStatusInternal() {
        return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(100001));
    }

    /* access modifiers changed from: package-private */
    public boolean setBackCoverColorIdInternal(String colorId) {
        if (colorId == null) {
            byte[] nullColorId = new byte[8];
            return OppoEngineerNative.nativeSaveEngineerData(100006, nullColorId, nullColorId.length);
        } else if (!OppoEngineerUtils.isBackCoverColorIdValid(colorId)) {
            return false;
        } else {
            byte[] validColorId = colorId.getBytes(StandardCharsets.UTF_8);
            boolean result = OppoEngineerNative.nativeSaveEngineerData(100006, validColorId, validColorId.length);
            if (result) {
                try {
                    SystemProperties.set("ro.hw.phone.color", colorId);
                } catch (Exception e) {
                    Slog.i(TAG, "set back cover color id caught exception : " + e.getMessage());
                }
            }
            return result;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean resetWriteProtectStateInternal() {
        if (!OppoEngineerNative.nativeGetPartionWriteProtectState()) {
            return true;
        }
        if (!OppoEngineerNative.nativeSetPartionWriteProtectState(false) || OppoEngineerNative.nativeGetPartionWriteProtectState()) {
            return false;
        }
        try {
            SystemProperties.set("vendor.oppo.quit.atm", TemperatureProvider.SWITCH_ON);
            SystemProperties.set("vendor.oppo.engineer.usb.config", "adb");
        } catch (Exception e) {
            Slog.i(TAG, "set reset atm property caught exception : " + e.getMessage());
        }
        ((NotificationManager) this.mContext.getSystemService("notification")).cancel(10000);
        clearWpIssue();
        return true;
    }

    /* access modifiers changed from: package-private */
    public int getRpmbEnableState() {
        return this.mRpmbEnableState;
    }

    /* access modifiers changed from: package-private */
    public String getDownloadModeInternal() {
        return OppoEngineerUtils.transferByteArrayToString(OppoEngineerNative.nativeReadEngineerData(1000057));
    }
}
