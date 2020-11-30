package com.android.server.pm;

import android.content.pm.OppoBasePackageUserState;
import android.content.pm.PackageUserState;
import android.os.Handler;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.Xml;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.color.util.ColorTypeCastingHelper;
import com.oppo.reflect.RefMethod;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorAppQuickFreezeManager implements IColorAppQuickFreezeManager {
    public static final String OLD_FREEZE_INFO_PATH = "/data/oppo/coloros/freeze/freeze-packages.xml";
    public static final String TAG = "ColorAppQuickFreezeManager";
    public static ArrayMap<Integer, ArrayMap<String, OppoFreezeInfo>> mOppoFreezeInfoUserMap = new ArrayMap<>();
    private static ColorAppQuickFreezeManager sColorAppQuickFreezeManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, false);
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    boolean mDynamicDebug = false;
    private PackageManagerService mPms = null;

    public static ColorAppQuickFreezeManager getInstance() {
        if (sColorAppQuickFreezeManager == null) {
            sColorAppQuickFreezeManager = new ColorAppQuickFreezeManager();
        }
        return sColorAppQuickFreezeManager;
    }

    private ColorAppQuickFreezeManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
        }
        registerLogModule();
    }

    public int oppoFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) {
        return oppoFreezePackageInternal(pkgName, userId, freezeFlag, flag, callingPkg);
    }

    public int oppoUnFreezePackage(String pkgName, int userId, int freezeFlag, int flag, String callingPkg) {
        return oppoUnFreezePackageInternal(pkgName, userId, freezeFlag, flag, callingPkg);
    }

    public int getOppoFreezePackageState(String pkgName, int userId) {
        PackageSetting pkgSetting = (PackageSetting) this.mPms.mSettings.mPackages.get(pkgName);
        if (pkgSetting == null || OppoMirrorPackageSettingBase.getOppoFreezeState == null) {
            return 0;
        }
        return ((Integer) OppoMirrorPackageSettingBase.getOppoFreezeState.call(pkgSetting, new Object[]{Integer.valueOf(userId)})).intValue();
    }

    public boolean inOppoFreezePackageList(String pkgName, int userId) {
        return getOppoFreezePackageState(pkgName, userId) == 2;
    }

    public List<String> getOppoFreezedPackageList(int userId) {
        List<String> freezeList = new ArrayList<>();
        new ArrayList();
        for (PackageSetting ps : this.mPms.mSettings.mPackages.values()) {
            if (ps != null && !ps.isSystem() && getOppoFreezeState(ps, userId) == 2) {
                freezeList.add(ps.name);
            }
        }
        return freezeList;
    }

    public int getOppoPackageFreezeFlag(String pkgName, int userId) {
        PackageSetting pkgSetting = (PackageSetting) this.mPms.mSettings.mPackages.get(pkgName);
        if (pkgSetting == null || OppoMirrorPackageSettingBase.getOppoFreezeFlag == null) {
            return 0;
        }
        return ((Integer) OppoMirrorPackageSettingBase.getOppoFreezeFlag.call(pkgSetting, new Object[]{Integer.valueOf(userId)})).intValue();
    }

    public void dumpOppoFreezeInfo(PrintWriter pw, String[] args) {
        PackageManagerService packageManagerService = this.mPms;
        int[] allUserIds = PackageManagerService.sUserManager.getUserIds();
        for (int userId : allUserIds) {
            List<String> freezeList = getOppoFreezedPackageList(userId);
            if (!freezeList.isEmpty()) {
                for (String pkg : freezeList) {
                    if (!TextUtils.isEmpty(pkg)) {
                        pw.println("-- " + pkg + "  userId=" + userId + "  state=2, flag=" + getOppoPackageFreezeFlag(pkg, userId));
                    }
                }
            }
        }
    }

    public void deleteOldFreezeInfo() {
        PackageSetting pkgSetting;
        File oldFreezeFile = new File(OLD_FREEZE_INFO_PATH);
        if (oldFreezeFile.exists()) {
            initMapFromDisk();
            if (mOppoFreezeInfoUserMap != null) {
                for (int i = 0; i < mOppoFreezeInfoUserMap.size(); i++) {
                    int userId = mOppoFreezeInfoUserMap.keyAt(i).intValue();
                    ArrayMap<String, OppoFreezeInfo> freezeInfoMap = mOppoFreezeInfoUserMap.valueAt(i);
                    if (freezeInfoMap != null) {
                        for (int j = 0; j < freezeInfoMap.size(); j++) {
                            OppoFreezeInfo info = freezeInfoMap.valueAt(j);
                            if (info != null && !TextUtils.isEmpty(info.mPackageName) && info.mState == 2 && (pkgSetting = (PackageSetting) this.mPms.mSettings.mPackages.get(info.mPackageName)) != null) {
                                Slog.w(TAG, "reset pkgSetting " + info.mPackageName + " freeze state at boot");
                                pkgSetting.setEnabled(0, userId, "android");
                                if (OppoMirrorPackageSettingBase.setOppoFreezeState != null) {
                                    OppoMirrorPackageSettingBase.setOppoFreezeState.call(pkgSetting, new Object[]{0, Integer.valueOf(userId)});
                                }
                            }
                        }
                    }
                }
            }
            oldFreezeFile.delete();
            return;
        }
        Slog.d(TAG, "deleteOldFreezeInfo find no old freeze file");
    }

    public int oppoFreezePackageInternal(String pkgName, int userId, int freezeFlag, int flags, String callingPackage) {
        PackageSetting pkgSetting;
        this.mPms.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "oppoFreezePackage");
        Slog.i(TAG, "oppo-dis-package " + pkgName + "/" + userId + ", freezeFlag=" + freezeFlag + ", callingPackage=" + callingPackage);
        if (TextUtils.isEmpty(pkgName) || (pkgSetting = (PackageSetting) this.mPms.mSettings.mPackages.get(pkgName)) == null) {
            return -1;
        }
        if (pkgSetting.isSystem()) {
            Slog.w(TAG, "can't oppo-dis system package " + pkgName);
            return -1;
        } else if (this.mPms.mProtectedPackages.isPackageStateProtected(userId, pkgName)) {
            Slog.w(TAG, "can't oppo-disable a protected package " + pkgName);
            return -1;
        } else {
            PackageUserState userState = pkgSetting.readUserState(userId);
            OppoBasePackageUserState baseUserState = typeCasting(userState);
            int currentFreezeState = baseUserState != null ? baseUserState.oppoFreezeState : 0;
            int currentFreezeFlag = baseUserState != null ? baseUserState.oppoFreezeFlag : 0;
            int currentEnableState = userState.enabled;
            if (currentFreezeState == 2) {
                Slog.w(TAG, "can't oppo-dis already freezed app:" + pkgName);
                return -2;
            } else if (currentEnableState == 2) {
                Slog.w(TAG, "can't oppo-dis already disabled app:" + pkgName);
                return -2;
            } else {
                if (OppoMirrorPackageSettingBase.setOppoFreezeState != null) {
                    OppoMirrorPackageSettingBase.setOppoFreezeState.call(pkgSetting, new Object[]{2, Integer.valueOf(userId)});
                }
                if (OppoMirrorPackageSettingBase.setOppoFreezeFlag != null) {
                    OppoMirrorPackageSettingBase.setOppoFreezeFlag.call(pkgSetting, new Object[]{Integer.valueOf(freezeFlag), Integer.valueOf(userId)});
                }
                this.mPms.setApplicationEnabledSetting(pkgName, 2, flags, userId, callingPackage);
                int afterEnableState = userState.enabled;
                if (afterEnableState == 2) {
                    return 0;
                }
                Slog.w(TAG, "failed to change " + pkgName + " to disable, result=" + afterEnableState);
                if (OppoMirrorPackageSettingBase.setOppoFreezeState != null) {
                    OppoMirrorPackageSettingBase.setOppoFreezeState.call(pkgSetting, new Object[]{0, Integer.valueOf(userId)});
                }
                if (OppoMirrorPackageSettingBase.setOppoFreezeFlag == null) {
                    return -3;
                }
                OppoMirrorPackageSettingBase.setOppoFreezeFlag.call(pkgSetting, new Object[]{Integer.valueOf(currentFreezeFlag), Integer.valueOf(userId)});
                return -3;
            }
        }
    }

    public int oppoUnFreezePackageInternal(String pkgName, int userId, int freezeFlag, int flags, String callingPackage) {
        PackageSetting pkgSetting;
        int i;
        this.mPms.mContext.enforceCallingOrSelfPermission("oppo.permission.OPPO_COMPONENT_SAFE", "oppoFreezePackage");
        Slog.i(TAG, "oppo-enable  " + pkgName + "/" + userId + ", freezeFlag=" + freezeFlag + ", callingPackage=" + callingPackage);
        if (TextUtils.isEmpty(pkgName) || (pkgSetting = (PackageSetting) this.mPms.mSettings.mPackages.get(pkgName)) == null) {
            return -1;
        }
        if (pkgSetting.isSystem()) {
            Slog.w(TAG, "can't oppo-enable system package " + pkgName);
            return -1;
        }
        PackageUserState userState = pkgSetting.readUserState(userId);
        OppoBasePackageUserState baseUserState = typeCasting(userState);
        int currentFreezeState = baseUserState != null ? baseUserState.oppoFreezeState : 0;
        int currentFreezeFlag = baseUserState != null ? baseUserState.oppoFreezeFlag : 0;
        int i2 = userState.enabled;
        if (currentFreezeState != 2) {
            Slog.w(TAG, "can't oppo-enable app " + pkgName + " from normal state");
            return -2;
        }
        if (OppoMirrorPackageSettingBase.setOppoFreezeState != null) {
            OppoMirrorPackageSettingBase.setOppoFreezeState.call(pkgSetting, new Object[]{0, Integer.valueOf(userId)});
        }
        if (OppoMirrorPackageSettingBase.setOppoFreezeFlag != null) {
            OppoMirrorPackageSettingBase.setOppoFreezeFlag.call(pkgSetting, new Object[]{Integer.valueOf(freezeFlag), Integer.valueOf(userId)});
        }
        this.mPms.setApplicationEnabledSetting(pkgName, 1, flags | 268435456 | 1, userId, callingPackage);
        int afterEnableState = userState.enabled;
        if (afterEnableState == 1) {
            return 0;
        }
        Slog.w(TAG, "failed to change " + pkgName + " to enable, result=" + afterEnableState);
        if (OppoMirrorPackageSettingBase.setOppoFreezeState != null) {
            i = 2;
            OppoMirrorPackageSettingBase.setOppoFreezeState.call(pkgSetting, new Object[]{2, Integer.valueOf(userId)});
        } else {
            i = 2;
        }
        if (OppoMirrorPackageSettingBase.setOppoFreezeFlag == null) {
            return -3;
        }
        RefMethod refMethod = OppoMirrorPackageSettingBase.setOppoFreezeFlag;
        Object[] objArr = new Object[i];
        objArr[0] = Integer.valueOf(currentFreezeFlag);
        objArr[1] = Integer.valueOf(userId);
        refMethod.call(pkgSetting, objArr);
        return -3;
    }

    public void autoUnfreezePackage(String pkgName, int userId, String reason) {
        if (!TextUtils.isEmpty(pkgName)) {
            try {
                int oppoFreezState = getOppoFreezePackageState(pkgName, userId);
                if (oppoFreezState == 2 || oppoFreezState == 1) {
                    if (this.mDynamicDebug) {
                        Slog.w(TAG, "oppo-enable  autoUnfreezePackage " + pkgName + ",reason:" + reason);
                    }
                    oppoUnFreezePackageInternal(pkgName, userId, 1, 0, "android");
                }
            } catch (Exception e) {
            }
        }
    }

    public int adjustFreezeAppFlags(int flags) {
        if ((flags & 1073741824) == 0) {
            return flags | 1073741824;
        }
        return flags;
    }

    public boolean adjustSendNow(boolean prevVaule, int flags) {
        return prevVaule;
    }

    public void setApplicationEnabledSetting(boolean update, int[] updateUserIds, String packageName) {
        if (update) {
            for (int userId : updateUserIds) {
                if (getOppoFreezePackageState(packageName, userId) == 2) {
                    Slog.i(TAG, "re-freeze package " + packageName + "/" + userId + " after replaced");
                    this.mPms.setApplicationEnabledSetting(packageName, 2, 0, userId, "com.coloros.safecenter");
                }
            }
        }
    }

    public boolean customizeSendEmptyMessage(String className, Handler mHandler) {
        if (!"com.coloros.safecenter.appfrozen.activity.AppFrozenLauncherActivity".equals(className)) {
            return false;
        }
        mHandler.sendEmptyMessage(1);
        return true;
    }

    public static class OppoFreezeInfo {
        int mFreezeFlag;
        String mPackageName;
        int mState;
        int mUserId;

        public OppoFreezeInfo(String packageName, int userId, int state, int flags) {
            this.mPackageName = packageName;
            this.mUserId = userId;
            this.mState = state;
            this.mFreezeFlag = flags;
        }

        public String toString() {
            return this.mPackageName + "/" + this.mUserId + "/" + this.mState + "/" + this.mFreezeFlag;
        }
    }

    public static void initMapFromDisk() {
        Slog.i(TAG, "init oppo-dis map from disk");
        if (mOppoFreezeInfoUserMap == null) {
            mOppoFreezeInfoUserMap = new ArrayMap<>();
        }
        File file = new File(OLD_FREEZE_INFO_PATH);
        if (!file.exists()) {
            Slog.e(TAG, "initMapFromDisk can't find file");
            return;
        }
        try {
            FileReader fileReader = new FileReader(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(fileReader);
                for (int eventType = parser.getEventType(); eventType != 1; eventType = parser.next()) {
                    if (eventType != 0) {
                        if (eventType == 2) {
                            if (parser.getName().equals(BrightnessConstants.AppSplineXml.TAG_PACKAGE)) {
                                String userIdText = parser.getAttributeValue(null, "userId");
                                int userId = -1;
                                if (!TextUtils.isEmpty(userIdText)) {
                                    try {
                                        userId = Integer.parseInt(userIdText);
                                    } catch (NumberFormatException e) {
                                        userId = -1;
                                    }
                                }
                                if (userId != -1) {
                                    ArrayMap<String, OppoFreezeInfo> freezeInfoMap = mOppoFreezeInfoUserMap.get(Integer.valueOf(userId));
                                    if (freezeInfoMap == null) {
                                        freezeInfoMap = new ArrayMap<>();
                                        mOppoFreezeInfoUserMap.put(Integer.valueOf(userId), freezeInfoMap);
                                    }
                                    String name = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                                    int state = -1;
                                    try {
                                        state = Integer.parseInt(parser.getAttributeValue(null, "state"));
                                    } catch (Exception e2) {
                                    }
                                    int flag = -1;
                                    try {
                                        flag = Integer.parseInt(parser.getAttributeValue(null, "flag"));
                                    } catch (Exception e3) {
                                    }
                                    if (!(TextUtils.isEmpty(name) || state == -1 || flag == -1)) {
                                        freezeInfoMap.put(name, new OppoFreezeInfo(name, userId, state, flag));
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (XmlPullParserException e4) {
                Slog.w(TAG, "Got execption parsing permissions.", e4);
            } catch (IOException e5) {
                Slog.w(TAG, "Got execption parsing permissions.", e5);
            }
            try {
                fileReader.close();
            } catch (IOException e6) {
                e6.printStackTrace();
            }
        } catch (FileNotFoundException e7) {
            Slog.w(TAG, "Couldn't find or open oppo_cts_list file " + file);
        }
    }

    public void setDynamicDebugSwitch(boolean on) {
        this.mDynamicDebug = on;
        this.DEBUG_SWITCH = sDebugfDetail | this.mDynamicDebug;
    }

    public void openLog(boolean on) {
        Slog.i(TAG, "#####openlog####");
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
        getInstance().setDynamicDebugSwitch(on);
        Slog.i(TAG, "mDynamicDebug = " + getInstance().mDynamicDebug);
    }

    public void registerLogModule() {
        try {
            Slog.i(TAG, "registerLogModule!");
            Class<?> cls = Class.forName("com.android.server.OppoDynamicLogManager");
            Slog.i(TAG, "invoke " + cls);
            Method m = cls.getDeclaredMethod("invokeRegisterLogModule", String.class);
            Slog.i(TAG, "invoke " + m);
            m.invoke(cls.newInstance(), ColorAppQuickFreezeManager.class.getName());
            Slog.i(TAG, "invoke end!");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e2) {
            e2.printStackTrace();
        } catch (IllegalArgumentException e3) {
            e3.printStackTrace();
        } catch (IllegalAccessException e4) {
            e4.printStackTrace();
        } catch (InvocationTargetException e5) {
            e5.printStackTrace();
        } catch (InstantiationException e6) {
            e6.printStackTrace();
        }
    }

    private static OppoBasePackageUserState typeCasting(PackageUserState ps) {
        if (ps != null) {
            return (OppoBasePackageUserState) ColorTypeCastingHelper.typeCasting(OppoBasePackageUserState.class, ps);
        }
        return null;
    }

    private int getOppoFreezeState(PackageSettingBase ps, int userId) {
        if (ps == null || OppoMirrorPackageSettingBase.getOppoFreezeState == null) {
            return 0;
        }
        return ((Integer) OppoMirrorPackageSettingBase.getOppoFreezeState.call(ps, new Object[]{Integer.valueOf(userId)})).intValue();
    }
}
