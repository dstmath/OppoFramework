package com.android.server.pm;

import android.app.OppoActivityManager;
import android.common.OppoFeatureCache;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManagerInternal;
import android.content.pm.PackageParser;
import android.content.pm.UserInfo;
import android.os.Binder;
import android.os.Build;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseArray;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.server.LocalServices;
import com.android.server.am.ColorHansManager;
import com.android.server.am.OppoPermissionConstants;
import com.android.server.coloros.OppoListManager;
import com.android.server.display.ai.utils.BrightnessConstants;
import com.android.server.display.ai.utils.ColorAILog;
import com.android.server.pm.permission.BasePermission;
import com.android.server.pm.permission.IColorDefaultPermissionGrantPolicyInner;
import com.android.server.pm.permission.PermissionManagerService;
import com.android.server.pm.permission.PermissionsState;
import com.android.server.wm.ColorFreeformManagerService;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import com.color.util.ColorTypeCastingHelper;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ColorRuntimePermGrantPolicyManager implements IColorRuntimePermGrantPolicyManager {
    private static final Set<String> ACTIVITY_RECOGNITION_PERMISSIONS = new ArraySet();
    private static final Set<String> ALWAYS_LOCATION_PERMISSIONS = new ArraySet();
    private static final Set<String> CALENDAR_PERMISSIONS = new ArraySet();
    private static final Set<String> CAMERA_PERMISSIONS = new ArraySet();
    private static final Set<String> CONTACTS_PERMISSIONS = new ArraySet();
    private static boolean DEBUG_GRANT_ALL_SYSTEM_PERM = SystemProperties.getBoolean("persist.sys.debug.allsystem", true);
    private static final int DEFAULT_PACKAGE_INFO_QUERY_FLAGS = 12288;
    private static final boolean EXP_VERSION = SystemProperties.get("ro.oppo.version", "CN").equalsIgnoreCase("US");
    public static final List<String> FILTER_RUNTIME_PERM_GROUPS = Arrays.asList("CALENDAR", "CAMERA", "CONTACTS", "LOCATION", "MICROPHONE", "PHONE", "SENSORS", "SMS", "STORAGE", "ACTIVITY_RECOGNITION");
    private static final Set<String> LOCATION_PERMISSIONS = new ArraySet();
    private static final Set<String> MICROPHONE_PERMISSIONS = new ArraySet();
    private static final Set<String> MX_TElCEL_CONTENEDOR_PERMS = new ArraySet();
    private static final Set<String> MX_TElCEL_WIRE_PERMS = new ArraySet();
    private static final ArrayList<String> OPPO_FIXED_PERM_LIST = new ArrayList<>();
    private static final ArrayList<String> OPPO_OVERSEA_FIXED_PERM_LIST = new ArrayList<>();
    private static final ArrayList<String> OPPO_OVERSEA_NONFIXED_PERM_LIST = new ArrayList<>();
    private static final ArrayList<String> OPPO_RUNTIME_PERM_BLACK_LIST = new ArrayList<>();
    private static final String OPPO_RUNTIME_PERM_FILTER_FILE = "/data/system/config/sys_pms_runtimeperm_filter_list.xml";
    private static final Set<String> PHONE_PERMISSIONS = new ArraySet();
    private static final Set<String> SENSORS_PERMISSIONS = new ArraySet();
    private static final Set<String> SMS_PERMISSIONS = new ArraySet();
    private static final Set<String> STORAGE_PERMISSIONS = new ArraySet();
    private static final String SYS_PERMISSIONS_GRANTED_BY_DEFAULT_FILTER_FILE = "/system/oppo/default_grant_permissions_list.xml";
    public static final String TAG = "ColorRuntimePermGrantPolicyManager";
    private static final String TAG_FIXED_RUNTIME_PERM = "FixedRuntimePermFilter";
    private static final String TAG_OVERSEA_FIXED_RUNTIME_PERM = "OverseaFixedRuntimePermFilter";
    private static final int USER_FIXED_OR_SET = 3;
    private static final String mOperator = SystemProperties.get("ro.oppo.operator", "");
    /* access modifiers changed from: private */
    public static ArrayMap<String, defaultPermissionInfo> mPermissionsGrantedByDefault = new ArrayMap<>();
    private static final String mSimOperatorProp = SystemProperties.get("persist.sys.oppo_opta", "");
    private static ColorRuntimePermGrantPolicyManager sColorRuntimePermGrantPolicyManager = null;
    public static boolean sDebugfDetail = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) EXP_VERSION);
    private static ArrayList<RuntimePermFilterInfo> sOppoFixedPermInfos = new ArrayList<>();
    private static ArrayList<RuntimePermFilterInfo> sOppoOverseaFixedPermInfos = new ArrayList<>();
    private static ArrayList<RuntimePermFilterInfo> sOppoOverseaNonFixedPermInfos = new ArrayList<>();
    boolean DEBUG_SWITCH = (sDebugfDetail | this.mDynamicDebug);
    private IColorDefaultPermissionGrantPolicyInner mColorDefaultPermissionGrantPolicyInner = null;
    private IColorPackageManagerServiceEx mColorPmsEx = null;
    boolean mDynamicDebug = EXP_VERSION;
    private boolean mForceGrantPermission = true;
    private final Object mLock = new Object();
    PermissionManagerService mPermissionManager = null;
    private PackageManagerService mPms = null;
    PackageManagerInternal mServiceInternal = null;
    boolean mSupportRuntimeAlert = EXP_VERSION;

    public static class RuntimePermFilterInfo {
        public boolean mAddAll;
        public int mFixType;
        public ArrayList<String> mGroups;
        public boolean mOverSea;
        public String mPackageName;
    }

    static {
        PHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE);
        PHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_CALL_PHONE);
        PHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_READ_CALL_LOG);
        PHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_WRITE_CALL_LOG);
        PHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_ADD_VOICEMAIL);
        PHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_USE_SIP);
        PHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_PROCESS_OUTGOING_CALLS);
        CONTACTS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_READ_CONTACTS);
        CONTACTS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_WRITE_CONTACTS);
        CONTACTS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_GET_ACCOUNTS);
        LOCATION_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_ACCESS);
        LOCATION_PERMISSIONS.add("android.permission.ACCESS_COARSE_LOCATION");
        ALWAYS_LOCATION_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_ACCESS);
        ALWAYS_LOCATION_PERMISSIONS.add("android.permission.ACCESS_COARSE_LOCATION");
        ALWAYS_LOCATION_PERMISSIONS.add("android.permission.ACCESS_BACKGROUND_LOCATION");
        ACTIVITY_RECOGNITION_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_ACTIVITY_RECOGNITION);
        CALENDAR_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_READ_CALENDAR);
        CALENDAR_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_WRITE_CALENDAR);
        SMS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_SEND_SMS);
        SMS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_RECEIVE_SMS);
        SMS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_READ_SMS);
        SMS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_RECEIVE_WAP_PUSH);
        SMS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_RECEIVE_MMS);
        SMS_PERMISSIONS.add("android.permission.READ_CELL_BROADCASTS");
        MICROPHONE_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_RECORD_AUDIO);
        CAMERA_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_CAMERA);
        SENSORS_PERMISSIONS.add(OppoPermissionConstants.PERMISSION_SENSORS);
        STORAGE_PERMISSIONS.add("android.permission.READ_EXTERNAL_STORAGE");
        STORAGE_PERMISSIONS.add("android.permission.WRITE_EXTERNAL_STORAGE");
        OPPO_FIXED_PERM_LIST.add("com.iflytek.speechcloud");
        OPPO_FIXED_PERM_LIST.add("com.coloros.fingerprint");
        OPPO_FIXED_PERM_LIST.add("com.coloros.activation");
        OPPO_FIXED_PERM_LIST.add("com.oppo.c2u");
        OPPO_FIXED_PERM_LIST.add("com.coloros.speechassist.engine");
        OPPO_FIXED_PERM_LIST.add("com.android.dlna.service");
        OPPO_FIXED_PERM_LIST.add("com.coloros.phonenoareainquire");
        OPPO_FIXED_PERM_LIST.add("com.android.mms.service");
        OPPO_FIXED_PERM_LIST.add("com.android.incallui");
        OPPO_FIXED_PERM_LIST.add("com.ted.number");
        OPPO_FIXED_PERM_LIST.add("com.coloros.number");
        OPPO_FIXED_PERM_LIST.add("com.nearme.romupdate");
        OPPO_FIXED_PERM_LIST.add("com.coloros.safesdkproxy");
        OPPO_FIXED_PERM_LIST.add("com.coloros.pictorial");
        OPPO_FIXED_PERM_LIST.add("com.oppo.ota");
        OPPO_FIXED_PERM_LIST.add("com.mediatek.omacp");
        OPPO_FIXED_PERM_LIST.add("com.heytap.pictorial");
        OPPO_FIXED_PERM_LIST.add("com.heytap.speechassist.engine");
        OPPO_FIXED_PERM_LIST.add("com.oppo.market#STORAGE");
        OPPO_FIXED_PERM_LIST.add("com.heytap.market#STORAGE");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.coloros.phonenoareainquire");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.ted.number");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.coloros.number");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.coloros.safesdkproxy");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.oppo.market#STORAGE");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.oppo.smartvolume");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.baidu.map.location");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.amap.android.location");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.amap.android.ams");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.mediatek.omacp");
        OPPO_OVERSEA_FIXED_PERM_LIST.add("com.heytap.market#STORAGE");
        OPPO_OVERSEA_NONFIXED_PERM_LIST.add("com.LogiaGroup.LogiaDeck");
        OPPO_OVERSEA_NONFIXED_PERM_LIST.add("com.heytap.market#PHONE");
        OPPO_OVERSEA_NONFIXED_PERM_LIST.add("com.oppo.market#PHONE");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.gms.policy_sidecar_aps");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.configupdater");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.backuptransport");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.ext.services");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.ext.shared");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.onetimeinitializer");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.partnersetup");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.printservice.recommendation");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.gsf");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.gms");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.android.vending");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.webview");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.google.android.marvin.talkback");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.nativeapp.rcsapp");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.baidu.input_oppo");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.sohu.inputmethod.sogouoem");
        OPPO_RUNTIME_PERM_BLACK_LIST.add("com.iflytek.speechsuite");
        MX_TElCEL_CONTENEDOR_PERMS.add(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE);
        MX_TElCEL_WIRE_PERMS.add(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE);
        MX_TElCEL_WIRE_PERMS.add(OppoPermissionConstants.PERMISSION_CALL_PHONE);
        MX_TElCEL_WIRE_PERMS.add(OppoPermissionConstants.PERMISSION_RECEIVE_SMS);
        MX_TElCEL_WIRE_PERMS.add(OppoPermissionConstants.PERMISSION_PROCESS_OUTGOING_CALLS);
    }

    public static ColorRuntimePermGrantPolicyManager getInstance() {
        if (sColorRuntimePermGrantPolicyManager == null) {
            sColorRuntimePermGrantPolicyManager = new ColorRuntimePermGrantPolicyManager();
        }
        return sColorRuntimePermGrantPolicyManager;
    }

    private ColorRuntimePermGrantPolicyManager() {
    }

    public void init(IColorPackageManagerServiceEx pmsEx) {
        if (pmsEx != null) {
            this.mColorPmsEx = pmsEx;
            this.mPms = pmsEx.getPackageManagerService();
        }
        initRuntimeFilterInfos();
        registerLogModule();
    }

    public void systemReady() {
        Slog.d(TAG, "start systemReady");
        this.mForceGrantPermission = SystemProperties.getInt("oppo.device.firstboot", 1) == 1 ? true : EXP_VERSION;
        Slog.d(TAG, "isFirstBoot: " + this.mForceGrantPermission);
        OppoFeatureCache.get(IColorRuntimePermGrantPolicyManager.DEFAULT).grantDefaultRuntimePermission();
        this.mForceGrantPermission = true;
        Slog.d(TAG, "end systemReady");
    }

    public void grantNonFixedPermToOtherSystemApps(int userId) {
        PermissionsState ps;
        boolean z = DEBUG_GRANT_ALL_SYSTEM_PERM;
        if (z) {
            if (z) {
                Slog.d(TAG, "grantNonFixedPermToOtherSystemApps");
            }
            synchronized (this.mLock) {
                for (String packageName : this.mServiceInternal.getPackageList().getPackageNames()) {
                    PackageParser.Package pkg = this.mServiceInternal.getPackage(packageName);
                    if (pkg.applicationInfo.isSystemApp() && doesPackageSupportRuntimePermissions(pkg)) {
                        if (!pkg.requestedPermissions.isEmpty()) {
                            if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                                Slog.d(TAG, "debug: oppo grant runtime-all to " + pkg.packageName + ", systemFix=false");
                            }
                            Set<String> permissions = new ArraySet<>();
                            int permissionCount = pkg.requestedPermissions.size();
                            PackageSetting packageSetting = (PackageSetting) pkg.mExtras;
                            if (packageSetting == null) {
                                ps = null;
                            } else {
                                ps = packageSetting.getPermissionsState();
                            }
                            for (int i = 0; i < permissionCount; i++) {
                                String permission = (String) pkg.requestedPermissions.get(i);
                                BasePermission bp = this.mColorDefaultPermissionGrantPolicyInner.getPermission(permission);
                                if (bp != null && bp.isRuntime() && !isRuntimePermissionInBlackList(pkg.packageName, permission) && shouldSystemAppPermissionsGrantByDefault(pkg, ps, permission, userId)) {
                                    permissions.add(permission);
                                }
                            }
                            if (!permissions.isEmpty()) {
                                repairPermFlagIfNeeded(ps, permissions, pkg.packageName, userId);
                                PackageInfo pkgInfo = this.mServiceInternal.getPackageInfo(pkg.packageName, (int) DEFAULT_PACKAGE_INFO_QUERY_FLAGS, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID, userId);
                                if (pkgInfo != null) {
                                    this.mColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgInfo, permissions, (boolean) EXP_VERSION, userId);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean includeInPermInfoList(ArrayList<RuntimePermFilterInfo> list, String pkg) {
        if (list == null || pkg == null) {
            return EXP_VERSION;
        }
        Iterator<RuntimePermFilterInfo> it = list.iterator();
        while (it.hasNext()) {
            RuntimePermFilterInfo info = it.next();
            if (info != null && info.mPackageName.equals(pkg)) {
                return true;
            }
        }
        return EXP_VERSION;
    }

    public void grantOppoFixRuntimePermssion(int userId, boolean supportRuntimeAlert) {
        PermissionsState ps;
        PackageParser.Package pkg;
        String str;
        String str2;
        Set<String> permissions;
        PermissionsState pkg2;
        PackageParser.Package pkg3;
        String str3;
        ArrayList<RuntimePermFilterInfo> globalPermList;
        ColorRuntimePermGrantPolicyManager colorRuntimePermGrantPolicyManager = this;
        if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
            Slog.d(TAG, "grantOppoFixRuntimePermssion");
        }
        ArrayList<RuntimePermFilterInfo> permInfoList = getFixedRuntimePermInfos(supportRuntimeAlert);
        ArrayList<String> globalList = OppoListManager.getInstance().getGlobalWhiteList(colorRuntimePermGrantPolicyManager.mPms.mContext, 2);
        if (!(globalList == null || (globalPermList = getDefaultPermFilterInfosFromStr(globalList)) == null)) {
            permInfoList.addAll(globalPermList);
        }
        if (permInfoList == null) {
            return;
        }
        if (!permInfoList.isEmpty()) {
            Iterator<RuntimePermFilterInfo> it = permInfoList.iterator();
            while (it.hasNext()) {
                RuntimePermFilterInfo info = it.next();
                if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                    Slog.d(TAG, "grantOppoFixRuntimePermssion supportRuntimeAlert=" + supportRuntimeAlert + ", info=" + info.mPackageName);
                }
                boolean addAll = info.mAddAll;
                PackageInfo pkgInfo = colorRuntimePermGrantPolicyManager.mServiceInternal.getPackageInfo(info.mPackageName, (int) DEFAULT_PACKAGE_INFO_QUERY_FLAGS, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID, userId);
                PermissionsState ps2 = null;
                PackageParser.Package ps3 = info.mPackageName == null ? null : colorRuntimePermGrantPolicyManager.mServiceInternal.getPackage(info.mPackageName);
                PackageSetting packageSetting = ps3 == null ? null : (PackageSetting) ps3.mExtras;
                if (packageSetting != null) {
                    ps2 = packageSetting.getPermissionsState();
                }
                String str4 = ", systemFix=true";
                if (!addAll) {
                    PermissionsState ps4 = ps2;
                    String str5 = "fixed per error, skip.";
                    if (ps3 != null) {
                        Iterator<String> it2 = info.mGroups.iterator();
                        while (it2.hasNext()) {
                            String group = it2.next();
                            if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                                Slog.d(TAG, "oppo grant runtime-" + group + " to " + info.mPackageName + str4);
                            }
                            Set<String> permissions2 = colorRuntimePermGrantPolicyManager.getPermsForType(group);
                            if (permissions2 == null || pkgInfo == null) {
                                str = str4;
                                pkg = ps3;
                                str2 = str5;
                                ps = ps4;
                            } else {
                                if (!colorRuntimePermGrantPolicyManager.mForceGrantPermission) {
                                    Set<String> arraySet = new ArraySet<>();
                                    for (String perm : permissions2) {
                                        if (colorRuntimePermGrantPolicyManager.shouldPermissionGrant(ps4, perm, userId)) {
                                            arraySet.add(perm);
                                        }
                                        ps4 = ps4;
                                        ps3 = ps3;
                                    }
                                    pkg = ps3;
                                    pkg2 = ps4;
                                    permissions = arraySet;
                                } else {
                                    pkg = ps3;
                                    pkg2 = ps4;
                                    permissions = permissions2;
                                }
                                if (!permissions.isEmpty()) {
                                    try {
                                        IColorDefaultPermissionGrantPolicyInner iColorDefaultPermissionGrantPolicyInner = colorRuntimePermGrantPolicyManager.mColorDefaultPermissionGrantPolicyInner;
                                        str = str4;
                                        ps = pkg2;
                                        str2 = str5;
                                        try {
                                            iColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgInfo, permissions, true, true, true, userId);
                                        } catch (Exception e) {
                                        }
                                    } catch (Exception e2) {
                                        str = str4;
                                        ps = pkg2;
                                        str2 = str5;
                                        Slog.d(TAG, str2);
                                        str5 = str2;
                                        str4 = str;
                                        ps3 = pkg;
                                        ps4 = ps;
                                        colorRuntimePermGrantPolicyManager = this;
                                    }
                                } else {
                                    str = str4;
                                    ps = pkg2;
                                    str2 = str5;
                                }
                            }
                            str5 = str2;
                            str4 = str;
                            ps3 = pkg;
                            ps4 = ps;
                            colorRuntimePermGrantPolicyManager = this;
                        }
                    }
                } else if (info.mPackageName != null) {
                    if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                        Slog.d(TAG, "oppo grant runtime-all to " + info.mPackageName + str4);
                    }
                    if (ps3 != null) {
                        if (!doesPackageSupportRuntimePermissions(ps3)) {
                            pkg3 = ps3;
                        } else if (ps3.requestedPermissions.isEmpty()) {
                            pkg3 = ps3;
                        } else {
                            Set<String> permissions3 = new ArraySet<>();
                            int i = 0;
                            for (int permissionCount = ps3.requestedPermissions.size(); i < permissionCount; permissionCount = permissionCount) {
                                String permission = (String) ps3.requestedPermissions.get(i);
                                BasePermission bp = colorRuntimePermGrantPolicyManager.mColorDefaultPermissionGrantPolicyInner.getPermission(permission);
                                if (bp != null && bp.isRuntime()) {
                                    if (colorRuntimePermGrantPolicyManager.mForceGrantPermission || colorRuntimePermGrantPolicyManager.shouldPermissionGrant(ps2, permission, userId)) {
                                        permissions3.add(permission);
                                    }
                                }
                                i++;
                            }
                            if (!permissions3.isEmpty() && pkgInfo != null) {
                                try {
                                    str3 = "fixed per error, skip.";
                                    try {
                                        colorRuntimePermGrantPolicyManager.mColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgInfo, permissions3, true, true, true, userId);
                                    } catch (Exception e3) {
                                    }
                                } catch (Exception e4) {
                                    str3 = "fixed per error, skip.";
                                    Slog.d(TAG, str3);
                                    colorRuntimePermGrantPolicyManager = this;
                                }
                            }
                        }
                        if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                            Slog.d(TAG, pkg3 + " do not support runtime, oppo skip");
                        }
                    }
                }
                colorRuntimePermGrantPolicyManager = this;
            }
        }
    }

    public void grantOppoNonFixRuntimePermssion(int userId, boolean supportRuntimeAlert) {
        ArrayList<RuntimePermFilterInfo> permInfoList;
        Iterator<String> it;
        ArrayList<RuntimePermFilterInfo> permInfoList2;
        if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
            Slog.d(TAG, "grantOppoNonFixRuntimePermssion");
        }
        ArrayList<RuntimePermFilterInfo> permInfoList3 = getNonFixedRuntimePermInfos(supportRuntimeAlert);
        if (permInfoList3 == null) {
            return;
        }
        if (!permInfoList3.isEmpty()) {
            Iterator<RuntimePermFilterInfo> it2 = permInfoList3.iterator();
            while (it2.hasNext()) {
                RuntimePermFilterInfo info = it2.next();
                if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                    Slog.d(TAG, "grantOppoNonFixRuntimePermssion supportRuntimeAlert=" + supportRuntimeAlert + ", info=" + info.mPackageName);
                }
                PackageInfo pkgInfo = this.mServiceInternal.getPackageInfo(info.mPackageName, (int) DEFAULT_PACKAGE_INFO_QUERY_FLAGS, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID, userId);
                boolean addAll = info.mAddAll;
                PermissionsState ps = null;
                PackageParser.Package pkg = info.mPackageName == null ? null : this.mServiceInternal.getPackage(info.mPackageName);
                PackageSetting packageSetting = pkg == null ? null : (PackageSetting) pkg.mExtras;
                if (packageSetting != null) {
                    ps = packageSetting.getPermissionsState();
                }
                if (!addAll) {
                    permInfoList = permInfoList3;
                    if (pkg != null) {
                        Iterator<String> it3 = info.mGroups.iterator();
                        while (it3.hasNext()) {
                            String group = it3.next();
                            if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                                Slog.d(TAG, "oppo grant runtime-" + group + " to " + info.mPackageName + ", systemFix=false");
                            }
                            Set<String> permissions = getPermsForType(group);
                            if (permissions == null || pkgInfo == null) {
                                it = it3;
                            } else {
                                if (!this.mForceGrantPermission) {
                                    Set<String> newPermissions = new ArraySet<>();
                                    for (String perm : permissions) {
                                        if (shouldPermissionGrant(ps, perm, userId)) {
                                            newPermissions.add(perm);
                                        }
                                        it3 = it3;
                                    }
                                    it = it3;
                                    permissions = newPermissions;
                                } else {
                                    it = it3;
                                }
                                if (!permissions.isEmpty()) {
                                    this.mColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgInfo, permissions, (boolean) EXP_VERSION, userId);
                                }
                            }
                            it3 = it;
                        }
                    }
                } else if (info.mPackageName != null) {
                    if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                        Slog.d(TAG, "oppo grant runtime-all to " + info.mPackageName + ", systemFix=false");
                    }
                    if (pkg != null) {
                        if (!doesPackageSupportRuntimePermissions(pkg)) {
                            permInfoList2 = permInfoList3;
                        } else if (pkg.requestedPermissions.isEmpty()) {
                            permInfoList2 = permInfoList3;
                        } else {
                            Set<String> permissions2 = new ArraySet<>();
                            int permissionCount = pkg.requestedPermissions.size();
                            int i = 0;
                            while (i < permissionCount) {
                                String permission = (String) pkg.requestedPermissions.get(i);
                                BasePermission bp = this.mColorDefaultPermissionGrantPolicyInner.getPermission(permission);
                                if (bp != null && bp.isRuntime()) {
                                    if (this.mForceGrantPermission || shouldPermissionGrant(ps, permission, userId)) {
                                        permissions2.add(permission);
                                    }
                                }
                                i++;
                                permInfoList3 = permInfoList3;
                            }
                            permInfoList = permInfoList3;
                            if (!permissions2.isEmpty() && pkgInfo != null) {
                                this.mColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgInfo, permissions2, (boolean) EXP_VERSION, userId);
                            }
                        }
                        if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
                            Slog.d(TAG, pkg + " do not support runtime, oppo skip");
                        }
                        permInfoList3 = permInfoList2;
                    } else {
                        permInfoList = permInfoList3;
                    }
                }
                permInfoList3 = permInfoList;
            }
            if (supportRuntimeAlert && isOperatorVersion()) {
                grantMXTelcelPackagesPerms(userId);
            }
            if ("YMOBILE".equals(mOperator)) {
                grantSoftBankPackagesPerms(userId);
            }
        }
    }

    private Set<String> getPermsForType(String type) {
        if (type == null || type.isEmpty()) {
            return null;
        }
        if (type.equalsIgnoreCase("PHONE")) {
            return PHONE_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("CONTACTS")) {
            return CONTACTS_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("LOCATION")) {
            return LOCATION_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("CALENDAR")) {
            return CALENDAR_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("SMS")) {
            return SMS_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("MICROPHONE")) {
            return MICROPHONE_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("CAMERA")) {
            return CAMERA_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("SENSORS")) {
            return SENSORS_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("STORAGE")) {
            return STORAGE_PERMISSIONS;
        }
        if (type.equalsIgnoreCase("ACTIVITY_RECOGNITION")) {
            return ACTIVITY_RECOGNITION_PERMISSIONS;
        }
        return null;
    }

    private boolean isRuntimePermissionInBlackList(String packageName, String permission) {
        ArrayList<RuntimePermFilterInfo> globalRuntimePermBlackList = getRuntimePermInBlackListInfos();
        if (globalRuntimePermBlackList == null || globalRuntimePermBlackList.isEmpty()) {
            return EXP_VERSION;
        }
        Iterator<RuntimePermFilterInfo> it = globalRuntimePermBlackList.iterator();
        while (it.hasNext()) {
            RuntimePermFilterInfo info = it.next();
            if (info != null && info.mPackageName != null && packageName != null && info.mPackageName.equals(packageName)) {
                if (info.mAddAll) {
                    Slog.i(TAG, "packageName is in OPPO_RUNTIME_PERM_BLACK_LIST : " + packageName + " permission : " + permission);
                    return true;
                }
                String group = getGroupForPerm(permission);
                if (info.mGroups == null || !info.mGroups.contains(group)) {
                    return EXP_VERSION;
                }
                Slog.i(TAG, "packageName is in OPPO_RUNTIME_PERM_BLACK_LIST : " + packageName + " permission : " + permission);
                return true;
            }
        }
        return EXP_VERSION;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private String getGroupForPerm(String permission) {
        char c;
        switch (permission.hashCode()) {
            case -2062386608:
                if (permission.equals(OppoPermissionConstants.PERMISSION_READ_SMS)) {
                    c = 16;
                    break;
                }
                c = 65535;
                break;
            case -1928411001:
                if (permission.equals(OppoPermissionConstants.PERMISSION_READ_CALENDAR)) {
                    c = 12;
                    break;
                }
                c = 65535;
                break;
            case -1921431796:
                if (permission.equals(OppoPermissionConstants.PERMISSION_READ_CALL_LOG)) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case -1888586689:
                if (permission.equals(OppoPermissionConstants.PERMISSION_ACCESS)) {
                    c = 10;
                    break;
                }
                c = 65535;
                break;
            case -1479758289:
                if (permission.equals(OppoPermissionConstants.PERMISSION_RECEIVE_WAP_PUSH)) {
                    c = 17;
                    break;
                }
                c = 65535;
                break;
            case -1238066820:
                if (permission.equals(OppoPermissionConstants.PERMISSION_SENSORS)) {
                    c = 22;
                    break;
                }
                c = 65535;
                break;
            case -895679497:
                if (permission.equals(OppoPermissionConstants.PERMISSION_RECEIVE_MMS)) {
                    c = 18;
                    break;
                }
                c = 65535;
                break;
            case -895673731:
                if (permission.equals(OppoPermissionConstants.PERMISSION_RECEIVE_SMS)) {
                    c = 15;
                    break;
                }
                c = 65535;
                break;
            case -406040016:
                if (permission.equals("android.permission.READ_EXTERNAL_STORAGE")) {
                    c = 23;
                    break;
                }
                c = 65535;
                break;
            case -63024214:
                if (permission.equals("android.permission.ACCESS_COARSE_LOCATION")) {
                    c = 11;
                    break;
                }
                c = 65535;
                break;
            case -5573545:
                if (permission.equals(OppoPermissionConstants.PERMISSION_READ_PHONE_STATE)) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 52602690:
                if (permission.equals(OppoPermissionConstants.PERMISSION_SEND_SMS)) {
                    c = 14;
                    break;
                }
                c = 65535;
                break;
            case 112197485:
                if (permission.equals(OppoPermissionConstants.PERMISSION_CALL_PHONE)) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 214526995:
                if (permission.equals(OppoPermissionConstants.PERMISSION_WRITE_CONTACTS)) {
                    c = 8;
                    break;
                }
                c = 65535;
                break;
            case 463403621:
                if (permission.equals(OppoPermissionConstants.PERMISSION_CAMERA)) {
                    c = 21;
                    break;
                }
                c = 65535;
                break;
            case 603653886:
                if (permission.equals(OppoPermissionConstants.PERMISSION_WRITE_CALENDAR)) {
                    c = 13;
                    break;
                }
                c = 65535;
                break;
            case 610633091:
                if (permission.equals(OppoPermissionConstants.PERMISSION_WRITE_CALL_LOG)) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 784519842:
                if (permission.equals(OppoPermissionConstants.PERMISSION_USE_SIP)) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 952819282:
                if (permission.equals(OppoPermissionConstants.PERMISSION_PROCESS_OUTGOING_CALLS)) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 958655846:
                if (permission.equals("android.permission.READ_CELL_BROADCASTS")) {
                    c = 19;
                    break;
                }
                c = 65535;
                break;
            case 1271781903:
                if (permission.equals(OppoPermissionConstants.PERMISSION_GET_ACCOUNTS)) {
                    c = 9;
                    break;
                }
                c = 65535;
                break;
            case 1365911975:
                if (permission.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
                    c = 24;
                    break;
                }
                c = 65535;
                break;
            case 1780337063:
                if (permission.equals(OppoPermissionConstants.PERMISSION_ACTIVITY_RECOGNITION)) {
                    c = 25;
                    break;
                }
                c = 65535;
                break;
            case 1831139720:
                if (permission.equals(OppoPermissionConstants.PERMISSION_RECORD_AUDIO)) {
                    c = 20;
                    break;
                }
                c = 65535;
                break;
            case 1977429404:
                if (permission.equals(OppoPermissionConstants.PERMISSION_READ_CONTACTS)) {
                    c = 7;
                    break;
                }
                c = 65535;
                break;
            case 2133799037:
                if (permission.equals(OppoPermissionConstants.PERMISSION_ADD_VOICEMAIL)) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
                return "PHONE";
            case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /*{ENCODED_INT: 7}*/:
            case ColorStartingWindowRUSHelper.SPLASH_SNAPSHOT_WHITE_THIRD_PARTY_APP /*{ENCODED_INT: 8}*/:
            case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /*{ENCODED_INT: 9}*/:
                return "CONTACTS";
            case ColorStartingWindowRUSHelper.STARTING_WINDOW_EXIT_LONG_DURATION_PACKAGE /*{ENCODED_INT: 10}*/:
            case ColorStartingWindowRUSHelper.SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION /*{ENCODED_INT: 11}*/:
                return "LOCATION";
            case 12:
            case 13:
                return "CALENDAR";
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
                return "SMS";
            case 20:
                return "MICROPHONE";
            case 21:
                return "CAMERA";
            case ColorHansManager.HansMainHandler.HANS_MSG_FREEZE_REPEAT_ACTION:
                return "SENSORS";
            case 23:
            case 24:
                return "STORAGE";
            case 25:
                return "ACTIVITY_RECOGNITION";
            default:
                return "OTHER";
        }
    }

    public void grantDefaultRuntimePermission() {
        this.mServiceInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mColorDefaultPermissionGrantPolicyInner = this.mColorPmsEx.getColorRuntimePermGrantPolicyManagerInner(this.mPms.mDefaultPermissionPolicy);
        this.mPermissionManager = this.mColorDefaultPermissionGrantPolicyInner.getPermissionManagerService();
        int i = 0;
        boolean supportRuntimeAlert = this.mPms.hasSystemFeature("oppo.runtime.permission.alert.support", 0);
        int[] userIds = UserManagerService.getInstance().getUserIds();
        if (userIds == null || userIds.length == 0) {
            userIds = new int[]{0};
        }
        int length = userIds.length;
        while (i < length) {
            int userId = userIds[i];
            UserInfo info = null;
            if (userId != 0) {
                try {
                    PackageManagerService packageManagerService = this.mPms;
                    info = PackageManagerService.sUserManager.getUserInfo(userId);
                } catch (Exception e) {
                    Slog.e(TAG, "PMS fails to grant runtime for user " + userId + " : " + e.getMessage());
                }
            }
            if (info != null && info.isManagedProfile()) {
                if (supportRuntimeAlert) {
                    Slog.d(TAG, "PMS start grant runtime for managed profile user " + userId);
                    grantOppoFixRuntimePermssion(userId, supportRuntimeAlert);
                    grantOppoNonFixRuntimePermssion(userId, supportRuntimeAlert);
                }
                i++;
            } else if (userId == 0 || info != null) {
                Slog.d(TAG, "PMS start grant runtime for user " + userId);
                grantOppoFixRuntimePermssion(userId, supportRuntimeAlert);
                if (supportRuntimeAlert) {
                    grantOppoNonFixRuntimePermssion(userId, supportRuntimeAlert);
                } else {
                    grantNonFixedPermToOtherSystemApps(userId);
                }
                i++;
            } else {
                i++;
            }
        }
    }

    public void grantDefaultRuntimePermissionNewUser(int userId) {
        this.mServiceInternal = (PackageManagerInternal) LocalServices.getService(PackageManagerInternal.class);
        this.mColorDefaultPermissionGrantPolicyInner = this.mColorPmsEx.getColorRuntimePermGrantPolicyManagerInner(this.mPms.mDefaultPermissionPolicy);
        this.mPermissionManager = this.mColorDefaultPermissionGrantPolicyInner.getPermissionManagerService();
        boolean supportRuntimeAlert = this.mPms.hasSystemFeature("oppo.runtime.permission.alert.support", 0);
        try {
            PackageManagerService packageManagerService = this.mPms;
            UserInfo createdInfo = PackageManagerService.sUserManager.getUserInfo(userId);
            if (createdInfo == null || !createdInfo.isManagedProfile()) {
                if (createdInfo != null) {
                    Slog.d(TAG, "pm.onNewUserCreated user " + userId);
                    grantOppoFixRuntimePermssion(userId, supportRuntimeAlert);
                    if (supportRuntimeAlert) {
                        grantOppoNonFixRuntimePermssion(userId, supportRuntimeAlert);
                    } else {
                        grantNonFixedPermToOtherSystemApps(userId);
                    }
                }
            } else if (supportRuntimeAlert) {
                Slog.d(TAG, "pm.onNewUserCreated isManagedProfile " + userId);
                grantOppoFixRuntimePermssion(userId, supportRuntimeAlert);
                grantOppoNonFixRuntimePermssion(userId, supportRuntimeAlert);
            }
        } catch (Exception e) {
            Slog.e(TAG, "pm.onNewUserCreated fails to grant runtime for user " + userId + " : " + e.getMessage());
        }
    }

    public static void initRuntimeFilterInfos() {
        File systemConfigPah = new File("/data/system/config");
        File runtimeFilterFilePath = new File(OPPO_RUNTIME_PERM_FILTER_FILE);
        try {
            if (!systemConfigPah.exists()) {
                systemConfigPah.mkdirs();
            }
            if (!runtimeFilterFilePath.exists()) {
                runtimeFilterFilePath.createNewFile();
            }
        } catch (IOException e) {
            Slog.e(TAG, "init runtimeFilterFilePath Dir failed!!!");
        }
        parseRuntimePermFilterInfos();
        parseSysPermissionsGrantByDefault();
    }

    public static ArrayList<RuntimePermFilterInfo> getDefaultPermFilterInfosFromStr(ArrayList<String> list) {
        if (list == null) {
            return null;
        }
        ArrayList<RuntimePermFilterInfo> tempList = new ArrayList<>();
        Iterator<String> it = list.iterator();
        while (it.hasNext()) {
            String value = it.next();
            RuntimePermFilterInfo info = new RuntimePermFilterInfo();
            if (!value.contains("#")) {
                info.mPackageName = value;
                info.mAddAll = true;
            } else {
                String[] splits = value.split("#");
                if (splits.length >= 2) {
                    info.mPackageName = splits[0];
                    info.mAddAll = EXP_VERSION;
                    ArrayList<String> groups = new ArrayList<>();
                    for (int i = 1; i < splits.length; i++) {
                        String group = splits[i];
                        if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                            groups.add(group);
                        }
                    }
                    info.mGroups = groups;
                } else {
                    info.mPackageName = splits[0];
                    info.mAddAll = true;
                }
            }
            tempList.add(info);
        }
        return tempList;
    }

    public static ArrayList<RuntimePermFilterInfo> getRuntimePermInBlackListInfos() {
        ArrayList<String> arrayList = OPPO_RUNTIME_PERM_BLACK_LIST;
        if (arrayList == null || arrayList.isEmpty()) {
            return null;
        }
        return getDefaultPermFilterInfosFromStr(OPPO_RUNTIME_PERM_BLACK_LIST);
    }

    public static ArrayList<RuntimePermFilterInfo> getFixedRuntimePermInfos(boolean overSea) {
        if (overSea) {
            ArrayList<RuntimePermFilterInfo> arrayList = sOppoOverseaFixedPermInfos;
            if (arrayList == null || arrayList.isEmpty()) {
                return getDefaultPermFilterInfosFromStr(OPPO_OVERSEA_FIXED_PERM_LIST);
            }
            return sOppoOverseaFixedPermInfos;
        }
        ArrayList<RuntimePermFilterInfo> defaultList = sOppoFixedPermInfos;
        if (defaultList == null || defaultList.isEmpty()) {
            return getDefaultPermFilterInfosFromStr(OPPO_FIXED_PERM_LIST);
        }
        return sOppoFixedPermInfos;
    }

    public static ArrayList<RuntimePermFilterInfo> getNonFixedRuntimePermInfos(boolean overSea) {
        if (overSea) {
            return getDefaultPermFilterInfosFromStr(OPPO_OVERSEA_NONFIXED_PERM_LIST);
        }
        return null;
    }

    public static void parseRuntimePermFilterInfos() {
        String value;
        File xmlFile = new File(OPPO_RUNTIME_PERM_FILTER_FILE);
        if (!xmlFile.exists()) {
            Slog.d(TAG, "sys_pms_runtimeperm_filter_list.xml not exist");
            return;
        }
        FileReader xmlReader = null;
        StringReader strReader = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            try {
                FileReader xmlReader2 = new FileReader(xmlFile);
                parser.setInput(xmlReader2);
                int eventType = parser.getEventType();
                while (true) {
                    int i = 1;
                    if (eventType != 1) {
                        if (eventType != 0 && eventType == 2) {
                            if (parser.getName().equals(TAG_FIXED_RUNTIME_PERM)) {
                                String value2 = parser.nextText();
                                if (value2 != null) {
                                    RuntimePermFilterInfo info = new RuntimePermFilterInfo();
                                    info.mOverSea = EXP_VERSION;
                                    if (!value2.contains("#")) {
                                        info.mPackageName = value2;
                                        info.mAddAll = true;
                                    } else {
                                        String[] splits = value2.split("#");
                                        if (splits.length >= 2) {
                                            info.mPackageName = splits[0];
                                            info.mAddAll = EXP_VERSION;
                                            ArrayList<String> groups = new ArrayList<>();
                                            while (i < splits.length) {
                                                String group = splits[i];
                                                if (FILTER_RUNTIME_PERM_GROUPS.contains(group)) {
                                                    groups.add(group);
                                                }
                                                i++;
                                            }
                                            info.mGroups = groups;
                                        } else {
                                            info.mPackageName = splits[0];
                                            info.mAddAll = true;
                                        }
                                    }
                                    sOppoFixedPermInfos.add(info);
                                }
                            } else if (parser.getName().equals(TAG_OVERSEA_FIXED_RUNTIME_PERM) && (value = parser.nextText()) != null) {
                                RuntimePermFilterInfo info2 = new RuntimePermFilterInfo();
                                info2.mOverSea = true;
                                if (!value.contains("#")) {
                                    info2.mPackageName = value;
                                    info2.mAddAll = true;
                                } else {
                                    String[] splits2 = value.split("#");
                                    if (splits2.length >= 2) {
                                        info2.mPackageName = splits2[0];
                                        info2.mAddAll = EXP_VERSION;
                                        ArrayList<String> groups2 = new ArrayList<>();
                                        while (i < splits2.length) {
                                            String group2 = splits2[i];
                                            if (FILTER_RUNTIME_PERM_GROUPS.contains(group2)) {
                                                groups2.add(group2);
                                            }
                                            i++;
                                        }
                                        info2.mGroups = groups2;
                                    } else {
                                        info2.mPackageName = splits2[0];
                                        info2.mAddAll = true;
                                    }
                                }
                                sOppoOverseaFixedPermInfos.add(info2);
                            }
                        }
                        eventType = parser.next();
                    } else {
                        try {
                            break;
                        } catch (IOException e) {
                            Slog.w(TAG, "Got execption close permReader.", e);
                            return;
                        }
                    }
                }
                xmlReader2.close();
                if (strReader != null) {
                    strReader.close();
                }
            } catch (FileNotFoundException e2) {
                Slog.w(TAG, "Couldn't find or open sys_pms_runtimeperm_filter_list file ");
                if (xmlReader != null) {
                    try {
                        xmlReader.close();
                    } catch (IOException e3) {
                        Slog.w(TAG, "Got execption close permReader.", e3);
                        return;
                    }
                }
                if (strReader != null) {
                    strReader.close();
                }
            }
        } catch (Exception e4) {
            Slog.w(TAG, "Got execption parsing permissions.", e4);
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e5) {
                    Slog.w(TAG, "Got execption close permReader.", e5);
                    return;
                }
            }
            if (strReader != null) {
                strReader.close();
            }
        } catch (Throwable th) {
            if (xmlReader != null) {
                try {
                    xmlReader.close();
                } catch (IOException e6) {
                    Slog.w(TAG, "Got execption close permReader.", e6);
                    throw th;
                }
            }
            if (strReader != null) {
                strReader.close();
            }
            throw th;
        }
    }

    public static void parseSysPermissionsGrantByDefault() {
        int type;
        int i;
        File permFile = new File(SYS_PERMISSIONS_GRANTED_BY_DEFAULT_FILTER_FILE);
        if (!permFile.exists()) {
            Slog.w(TAG, "Couldn't find permissions file " + permFile);
            return;
        }
        try {
            FileReader permReader = new FileReader(permFile);
            try {
                XmlPullParser parser = Xml.newPullParser();
                parser.setInput(permReader);
                do {
                    type = parser.next();
                    i = 1;
                    if (type == 2) {
                        break;
                    }
                } while (type != 1);
                if (type != 2) {
                    Slog.w("TAG", "No start tag found");
                    IoUtils.closeQuietly(permReader);
                } else if (!parser.getName().equals("packages")) {
                    Slog.w("TAG", "Unexpected start tag in " + permFile + ": found " + parser.getName() + ", expected 'packages'");
                    IoUtils.closeQuietly(permReader);
                } else {
                    while (true) {
                        XmlUtils.nextElement(parser);
                        if (parser.getEventType() == i) {
                            break;
                        }
                        if ("default-grant-permissions".equals(parser.getName())) {
                            String packageName = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_PACKAGE);
                            if (TextUtils.isEmpty(packageName)) {
                                Slog.d("TAG", "skip package");
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                boolean sharedOrPersistent = XmlUtils.readBooleanAttribute(parser, "sys_shared_or_persistent");
                                defaultPermissionInfo permInfo = mPermissionsGrantedByDefault.get(packageName);
                                if (permInfo == null) {
                                    permInfo = new defaultPermissionInfo();
                                    permInfo.setPkgName(packageName, sharedOrPersistent);
                                    mPermissionsGrantedByDefault.put(packageName, permInfo);
                                }
                                int depth = parser.getDepth();
                                while (XmlUtils.nextElementWithin(parser, depth)) {
                                    if ("permission".equals(parser.getName())) {
                                        String permission = parser.getAttributeValue(null, BrightnessConstants.AppSplineXml.TAG_NAME);
                                        if (TextUtils.isEmpty(permission)) {
                                            Slog.d(TAG, "skip package: " + packageName + " permission");
                                        } else {
                                            permInfo.addPermission(permission, XmlUtils.readBooleanAttribute(parser, "fixed"));
                                        }
                                    }
                                }
                            }
                        }
                        i = 1;
                    }
                    IoUtils.closeQuietly(permReader);
                }
            } catch (XmlPullParserException e) {
                Slog.w(TAG, "Got exception parsing default grant permissions: " + e);
            } catch (IOException e2) {
                Slog.w(TAG, "Got exception parsing default grant permissions: " + e2);
            } catch (Throwable th) {
                IoUtils.closeQuietly(permReader);
                throw th;
            }
        } catch (FileNotFoundException e3) {
            Slog.w(TAG, "Couldn't open permissions file " + permFile);
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(PackageParser.Package pkg) {
        if (pkg.applicationInfo.targetSdkVersion > 22) {
            return true;
        }
        return EXP_VERSION;
    }

    public void grantOppoPermissionByGroup(final PackageParser.Package pkg, final String permName, final String packageName, final int callingUid) {
        this.mSupportRuntimeAlert = this.mPms.hasSystemFeature("oppo.runtime.permission.alert.support", 0);
        if (this.mSupportRuntimeAlert) {
            final int callingPid = Binder.getCallingPid();
            this.mPms.mHandler.post(new Runnable() {
                /* class com.android.server.pm.ColorRuntimePermGrantPolicyManager.AnonymousClass1 */

                public void run() {
                    boolean isSystem = ColorRuntimePermGrantPolicyManager.EXP_VERSION;
                    if (pkg.applicationInfo != null) {
                        isSystem = (!pkg.applicationInfo.isSystemApp() || ColorRuntimePermGrantPolicyManager.mPermissionsGrantedByDefault.get(packageName) != null) ? ColorRuntimePermGrantPolicyManager.EXP_VERSION : true;
                    }
                    String callingPackage = OppoPackageManagerHelper.getProcessNameByPid(callingPid);
                    if (!isSystem && !"com.android.packageinstaller".equals(callingPackage) && !"com.coloros.securitypermission".equals(callingPackage) && !"com.coloros.securitypermission:ui".equals(callingPackage) && !"com.coloros.persist.system".equals(callingPackage)) {
                        OppoActivityManager oAm = new OppoActivityManager();
                        try {
                            int userId = UserHandle.getUserId(callingUid);
                            String packageNameWithUserId = packageName;
                            if (userId != 0) {
                                packageNameWithUserId = packageName + "#" + userId;
                            }
                            oAm.grantOppoPermissionByGroup(packageNameWithUserId, permName);
                        } catch (RemoteException e) {
                            Slog.w(ColorRuntimePermGrantPolicyManager.TAG, "failed to grantOppoPermissionByGroup");
                        }
                    }
                }
            });
        }
    }

    public void revokeOppoPermissionByGroup(final PackageParser.Package pkg, final String permName, final String packageName, final int callingUid) {
        this.mSupportRuntimeAlert = this.mPms.hasSystemFeature("oppo.runtime.permission.alert.support", 0);
        if (this.mSupportRuntimeAlert) {
            final int callingPid = Binder.getCallingPid();
            this.mPms.mHandler.post(new Runnable() {
                /* class com.android.server.pm.ColorRuntimePermGrantPolicyManager.AnonymousClass2 */

                public void run() {
                    boolean isSystem = ColorRuntimePermGrantPolicyManager.EXP_VERSION;
                    if (pkg.applicationInfo != null) {
                        isSystem = (!pkg.applicationInfo.isSystemApp() || ColorRuntimePermGrantPolicyManager.mPermissionsGrantedByDefault.get(packageName) != null) ? ColorRuntimePermGrantPolicyManager.EXP_VERSION : true;
                    }
                    String callingPackage = OppoPackageManagerHelper.getProcessNameByPid(callingPid);
                    if (!isSystem && !"com.android.packageinstaller".equals(callingPackage) && !"com.coloros.securitypermission".equals(callingPackage) && !"com.coloros.securitypermission:ui".equals(callingPackage) && !"com.coloros.persist.system".equals(callingPackage)) {
                        OppoActivityManager oAm = new OppoActivityManager();
                        try {
                            int userId = UserHandle.getUserId(callingUid);
                            String packageNameWithUserId = packageName;
                            if (userId != 0) {
                                packageNameWithUserId = packageName + "#" + userId;
                            }
                            oAm.revokeOppoPermissionByGroup(packageNameWithUserId, permName);
                        } catch (RemoteException e) {
                            Slog.w(ColorRuntimePermGrantPolicyManager.TAG, "failed to revokeOppoPermissionByGroup");
                        }
                    }
                }
            });
        }
    }

    public void grantOppoPermissionByGroupAsUser(final PackageParser.Package pkg, final String permName, final String packageName, int callingUid, final int userId) {
        this.mSupportRuntimeAlert = this.mPms.hasSystemFeature("oppo.runtime.permission.alert.support", 0);
        if (this.mSupportRuntimeAlert) {
            final int callingPid = Binder.getCallingPid();
            this.mPms.mHandler.post(new Runnable() {
                /* class com.android.server.pm.ColorRuntimePermGrantPolicyManager.AnonymousClass3 */

                public void run() {
                    boolean isSystem = ColorRuntimePermGrantPolicyManager.EXP_VERSION;
                    if (pkg.applicationInfo != null) {
                        isSystem = (!pkg.applicationInfo.isSystemApp() || ColorRuntimePermGrantPolicyManager.mPermissionsGrantedByDefault.get(packageName) != null) ? ColorRuntimePermGrantPolicyManager.EXP_VERSION : true;
                    }
                    String callingPackage = OppoPackageManagerHelper.getProcessNameByPid(callingPid);
                    if (!isSystem && !"com.android.packageinstaller".equals(callingPackage) && !"com.coloros.securitypermission".equals(callingPackage) && !"com.coloros.securitypermission:ui".equals(callingPackage) && !"com.coloros.persist.system".equals(callingPackage)) {
                        OppoActivityManager oAm = new OppoActivityManager();
                        try {
                            String packageNameWithUserId = packageName;
                            if (userId != 0) {
                                packageNameWithUserId = packageName + "#" + userId;
                            }
                            oAm.grantOppoPermissionByGroup(packageNameWithUserId, permName);
                        } catch (RemoteException e) {
                            Slog.w(ColorRuntimePermGrantPolicyManager.TAG, "failed to grantOppoPermissionByGroup");
                        }
                    }
                }
            });
        }
    }

    public void revokeOppoPermissionByGroupAsUser(final PackageParser.Package pkg, final String permName, final String packageName, int callingUid, final int userId) {
        this.mSupportRuntimeAlert = this.mPms.hasSystemFeature("oppo.runtime.permission.alert.support", 0);
        if (this.mSupportRuntimeAlert) {
            final int callingPid = Binder.getCallingPid();
            this.mPms.mHandler.post(new Runnable() {
                /* class com.android.server.pm.ColorRuntimePermGrantPolicyManager.AnonymousClass4 */

                public void run() {
                    boolean isSystem = ColorRuntimePermGrantPolicyManager.EXP_VERSION;
                    if (pkg.applicationInfo != null) {
                        isSystem = (!pkg.applicationInfo.isSystemApp() || ColorRuntimePermGrantPolicyManager.mPermissionsGrantedByDefault.get(packageName) != null) ? ColorRuntimePermGrantPolicyManager.EXP_VERSION : true;
                    }
                    String callingPackage = OppoPackageManagerHelper.getProcessNameByPid(callingPid);
                    if (!isSystem && !"com.android.packageinstaller".equals(callingPackage) && !"com.coloros.securitypermission".equals(callingPackage) && !"com.coloros.securitypermission:ui".equals(callingPackage) && !"com.coloros.persist.system".equals(callingPackage)) {
                        OppoActivityManager oAm = new OppoActivityManager();
                        try {
                            String packageNameWithUserId = packageName;
                            if (userId != 0) {
                                packageNameWithUserId = packageName + "#" + userId;
                            }
                            oAm.revokeOppoPermissionByGroup(packageNameWithUserId, permName);
                        } catch (RemoteException e) {
                            Slog.w(ColorRuntimePermGrantPolicyManager.TAG, "failed to revokeOppoPermissionByGroup");
                        }
                    }
                }
            });
        }
    }

    public ArrayList<String> getIgnoreAppList() {
        return ColorPackageManagerHelper.getIgnoreAppList();
    }

    public boolean onPermissionRevoked(ApplicationInfo applicationInfo, int userId) {
        return EXP_VERSION;
    }

    public boolean isRuntimePermissionFingerprintNew(int userId) {
        PackageManagerService packageManagerService = this.mPms;
        if (packageManagerService != null) {
            Settings settings = packageManagerService.mSettings;
            try {
                Field runtimePermissionsPersistenceField = Settings.class.getDeclaredField("mRuntimePermissionsPersistence");
                runtimePermissionsPersistenceField.setAccessible(true);
                Object runtimePermissionsPersistence = runtimePermissionsPersistenceField.get(settings);
                Class[] classes = Settings.class.getDeclaredClasses();
                for (Class c : classes) {
                    if (c.getSimpleName().equals("RuntimePermissionPersistence")) {
                        Field fingerprintsField = c.getDeclaredField("mFingerprints");
                        fingerprintsField.setAccessible(true);
                        boolean res = Build.FINGERPRINT.equals(((SparseArray) fingerprintsField.get(runtimePermissionsPersistence)).get(userId));
                        Slog.d(TAG, "isRuntimePermissionFingerprintNew for user" + userId + ": " + res);
                        return res;
                    }
                }
            } catch (Exception e) {
                Slog.e(TAG, "fail to get fingerprint for user" + userId + ": " + e.getMessage());
            }
        }
        Slog.i(TAG, "isRuntimePermissionFingerprintNew return default value for user" + userId);
        return EXP_VERSION;
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
            m.invoke(cls.newInstance(), ColorRuntimePermGrantPolicyManager.class.getName());
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

    private boolean shouldPermissionGrant(PermissionsState permissionsState, String permissionName, int userId) {
        if (permissionsState == null || !permissionsState.hasPermission(permissionName, userId)) {
            return true;
        }
        return EXP_VERSION;
    }

    private static OppoBasePackageManagerService typeCasting(PackageManagerService pms) {
        if (pms != null) {
            return (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, pms);
        }
        return null;
    }

    private void repairPermFlagIfNeeded(PermissionsState ps, Set<String> permissions, String pkgName, int userId) {
        if (ps != null) {
            for (String perm : permissions) {
                if ((ps.getPermissionFlags(perm, userId) & 3) != 0) {
                    Slog.i(TAG, "repairPermFlag: " + perm + " for" + pkgName + " in user" + userId);
                    this.mPms.updatePermissionFlags(perm, pkgName, 3, 0, (boolean) EXP_VERSION, userId);
                }
            }
        }
    }

    private boolean isUserSetOrFixed(PermissionsState ps, String permission, int userId) {
        if (ps == null || (ps.getPermissionFlags(permission, userId) & 3) == 0) {
            return EXP_VERSION;
        }
        return true;
    }

    private boolean shouldSystemAppPermissionsGrantByDefault(PackageParser.Package pkg, PermissionsState ps, String perm, int userId) {
        boolean hasGranted = !shouldPermissionGrant(ps, perm, userId);
        String pkgName = pkg.packageName;
        if (isPkgInGrantByDefaultList(pkgName)) {
            if (isPermInGrantByDefaultList(pkgName, perm)) {
                Slog.d(TAG, "package: " + pkgName + " and its perm: " + perm + " is in whitelist and has granted: " + hasGranted);
                if (hasGranted || isUserSetOrFixed(ps, perm, userId)) {
                    return EXP_VERSION;
                }
                return true;
            }
            Slog.d(TAG, "package: " + pkgName + " is in whitelist but its " + perm + " is not and has granted: " + hasGranted);
            repairDefaultPermFlagIfNeeded(ps, perm, pkgName, hasGranted, userId);
            return EXP_VERSION;
        } else if (this.mForceGrantPermission) {
            return true;
        } else {
            if (hasGranted || skipGrantToUidShared(pkg, perm)) {
                return EXP_VERSION;
            }
            return true;
        }
    }

    private boolean skipGrantToUidShared(PackageParser.Package pkg, String permission) {
        PackageParser.Package pkgOfContacts;
        if (pkg == null) {
            return EXP_VERSION;
        }
        String pkgName = pkg.packageName;
        if (!"android.uid.shared".equals(pkg.mSharedUserId) || "com.android.contacts".equals(pkgName) || (pkgOfContacts = this.mServiceInternal.getPackage("com.android.contacts")) == null) {
            return EXP_VERSION;
        }
        return pkgOfContacts.requestedPermissions.contains(permission);
    }

    private void repairDefaultPermFlagIfNeeded(PermissionsState ps, String perm, String pkgName, boolean granted, int userId) {
        if (ps != null) {
            int flag = ps.getPermissionFlags(perm, userId);
            defaultPermissionInfo permInfo = mPermissionsGrantedByDefault.get(pkgName);
            if (permInfo != null && permInfo.isSysSharedOrPersistent && granted && (flag & 16) == 0) {
                Slog.d(TAG, "package: " + pkgName + " is sys shared or persistent app, its permission should be changed to system fixed");
                this.mPms.updatePermissionFlags(perm, pkgName, 16, 16, (boolean) EXP_VERSION, userId);
            }
            if ((flag & 32) != 0) {
                Slog.d(TAG, "repairDefaultPermFlag: " + perm + " for " + pkgName + " in user" + userId);
                this.mPms.updatePermissionFlags(perm, pkgName, 32, 0, (boolean) EXP_VERSION, userId);
            }
        }
    }

    private static boolean isPkgInGrantByDefaultList(String pkgName) {
        if (mPermissionsGrantedByDefault.get(pkgName) != null) {
            return true;
        }
        return EXP_VERSION;
    }

    private static boolean isPermInGrantByDefaultList(String pkgName, String permission) {
        defaultPermissionInfo permInfo = mPermissionsGrantedByDefault.get(pkgName);
        return permInfo == null ? EXP_VERSION : permInfo.permissions.containsKey(permission);
    }

    /* access modifiers changed from: private */
    public static class defaultPermissionInfo {
        boolean isSysSharedOrPersistent;
        ArrayMap<String, Boolean> permissions;
        String pkgName;

        private defaultPermissionInfo() {
            this.permissions = new ArrayMap<>();
        }

        public void setPkgName(String name, boolean sharedOrPersistent) {
            this.pkgName = name;
            this.isSysSharedOrPersistent = sharedOrPersistent;
        }

        public void addPermission(String permission, boolean fixed) {
            if (!TextUtils.isEmpty(permission)) {
                this.permissions.put(permission, Boolean.valueOf(fixed));
            }
        }
    }

    private void grantMXTelcelPackagesPerms(int userid) {
        PackageInfo pkgWireInfo;
        PackageInfo pkgContenedorInfo;
        PackageParser.Package pkgContenedor = this.mServiceInternal.getPackage("com.telcel.contenedor");
        if (!(pkgContenedor == null || (pkgContenedorInfo = this.mServiceInternal.getPackageInfo(pkgContenedor.packageName, (int) DEFAULT_PACKAGE_INFO_QUERY_FLAGS, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID, userid)) == null)) {
            this.mColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgContenedorInfo, MX_TElCEL_CONTENEDOR_PERMS, (boolean) EXP_VERSION, userid);
        }
        PackageParser.Package pkgWire = this.mServiceInternal.getPackage("com.speedymovil.wire");
        if (pkgWire != null && (pkgWireInfo = this.mServiceInternal.getPackageInfo(pkgWire.packageName, (int) DEFAULT_PACKAGE_INFO_QUERY_FLAGS, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID, userid)) != null) {
            this.mColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgWireInfo, MX_TElCEL_WIRE_PERMS, (boolean) EXP_VERSION, userid);
        }
    }

    private void grantSoftBankPackagesPerms(int userid) {
        PackageInfo pkgInfo;
        HashMap<String, Boolean> softBankPackagePermMap = new HashMap<>();
        softBankPackagePermMap.put("jp.softbank.mb.flcrlap", Boolean.valueOf((boolean) EXP_VERSION));
        softBankPackagePermMap.put("com.android.cellbroadcastreceiver", true);
        if (softBankPackagePermMap.entrySet() != null) {
            for (Map.Entry<String, Boolean> singleEntry : softBankPackagePermMap.entrySet()) {
                PackageParser.Package singlePackageParser = this.mServiceInternal.getPackage(singleEntry.getKey());
                if (singlePackageParser != null && doesPackageSupportRuntimePermissions(singlePackageParser) && !singlePackageParser.requestedPermissions.isEmpty() && (pkgInfo = this.mServiceInternal.getPackageInfo(singlePackageParser.packageName, (int) DEFAULT_PACKAGE_INFO_QUERY_FLAGS, (int) ColorFreeformManagerService.FREEFORM_CALLER_UID, userid)) != null) {
                    Set<String> permissions = new ArraySet<>();
                    Iterator it = singlePackageParser.requestedPermissions.iterator();
                    while (it.hasNext()) {
                        String permission = (String) it.next();
                        BasePermission bp = this.mColorDefaultPermissionGrantPolicyInner.getPermission(permission);
                        if (bp != null && bp.isRuntime()) {
                            permissions.add(permission);
                        }
                    }
                    if (!permissions.isEmpty()) {
                        this.mColorDefaultPermissionGrantPolicyInner.grantRuntimePermissions(pkgInfo, permissions, singleEntry.getValue().booleanValue(), userid);
                    }
                }
            }
        }
    }

    static boolean isOperatorVersion() {
        if (!TextUtils.isEmpty(mOperator) || !TextUtils.isEmpty(mSimOperatorProp)) {
            return true;
        }
        return EXP_VERSION;
    }
}
