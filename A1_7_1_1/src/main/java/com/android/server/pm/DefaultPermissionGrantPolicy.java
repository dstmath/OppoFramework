package com.android.server.pm;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManagerInternal.PackagesProvider;
import android.content.pm.PackageManagerInternal.SyncAdapterPackagesProvider;
import android.content.pm.PackageParser.Package;
import android.content.pm.ProviderInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.android.server.am.OppoProcessManager;
import com.android.server.coloros.OppoListManager;
import com.android.server.pm.ColorPackageManagerHelper.RuntimePermFilterInfo;
import com.mediatek.cta.CtaUtils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

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
final class DefaultPermissionGrantPolicy {
    private static final String ATTR_FIXED = "fixed";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PACKAGE = "package";
    private static final String AUDIO_MIME_TYPE = "audio/mpeg";
    private static final Set<String> CALENDAR_PERMISSIONS = null;
    private static final Set<String> CAMERA_PERMISSIONS = null;
    private static final Set<String> CONTACTS_PERMISSIONS = null;
    private static final boolean DEBUG = false;
    private static boolean DEBUG_GRANT_ALL_SYSTEM_PERM = false;
    private static final int DEFAULT_FLAGS = 786432;
    private static final Set<String> LOCATION_PERMISSIONS = null;
    private static final Set<String> MICROPHONE_PERMISSIONS = null;
    private static final int MSG_READ_DEFAULT_PERMISSION_EXCEPTIONS = 1;
    private static final Set<String> PHONE_PERMISSIONS = null;
    private static final Set<String> SENSORS_PERMISSIONS = null;
    private static final Set<String> SMS_PERMISSIONS = null;
    private static final Set<String> STORAGE_PERMISSIONS = null;
    private static final String TAG = "DefaultPermGrantPolicy";
    private static final String TAG_EXCEPTION = "exception";
    private static final String TAG_EXCEPTIONS = "exceptions";
    private static final String TAG_PERMISSION = "permission";
    private static HashSet<String> mForcePermReviewPkgs;
    private PackagesProvider mDialerAppPackagesProvider;
    private ArrayMap<String, List<DefaultPermissionGrant>> mGrantExceptions;
    private final Handler mHandler;
    private PackagesProvider mLocationPackagesProvider;
    private final PackageManagerService mService;
    private PackagesProvider mSimCallManagerPackagesProvider;
    private PackagesProvider mSmsAppPackagesProvider;
    private SyncAdapterPackagesProvider mSyncAdapterPackagesProvider;
    private PackagesProvider mVoiceInteractionPackagesProvider;

    private static final class DefaultPermissionGrant {
        final boolean fixed;
        final String name;

        public DefaultPermissionGrant(String name, boolean fixed) {
            this.name = name;
            this.fixed = fixed;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.DefaultPermissionGrantPolicy.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.pm.DefaultPermissionGrantPolicy.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.DefaultPermissionGrantPolicy.<clinit>():void");
    }

    public DefaultPermissionGrantPolicy(PackageManagerService service) {
        this.mService = service;
        this.mHandler = new Handler(this.mService.mHandlerThread.getLooper()) {
            public void handleMessage(Message msg) {
                if (msg.what == 1) {
                    synchronized (DefaultPermissionGrantPolicy.this.mService.mPackages) {
                        if (DefaultPermissionGrantPolicy.this.mGrantExceptions == null) {
                            DefaultPermissionGrantPolicy.this.mGrantExceptions = DefaultPermissionGrantPolicy.this.readDefaultPermissionExceptionsLPw();
                        }
                    }
                }
            }
        };
    }

    public void setLocationPackagesProviderLPw(PackagesProvider provider) {
        this.mLocationPackagesProvider = provider;
    }

    public void setVoiceInteractionPackagesProviderLPw(PackagesProvider provider) {
        this.mVoiceInteractionPackagesProvider = provider;
    }

    public void setSmsAppPackagesProviderLPw(PackagesProvider provider) {
        this.mSmsAppPackagesProvider = provider;
    }

    public void setDialerAppPackagesProviderLPw(PackagesProvider provider) {
        this.mDialerAppPackagesProvider = provider;
    }

    public void setSimCallManagerPackagesProviderLPw(PackagesProvider provider) {
        this.mSimCallManagerPackagesProvider = provider;
    }

    public void setSyncAdapterPackagesProviderLPw(SyncAdapterPackagesProvider provider) {
        this.mSyncAdapterPackagesProvider = provider;
    }

    public void grantDefaultPermissions(int userId) {
        grantPermissionsToSysComponentsAndPrivApps(userId);
        grantDefaultSystemHandlerPermissions(userId);
        grantDefaultPermissionExceptions(userId);
        if (CtaUtils.isCtaSupported()) {
            grantCtaPermToPreInstalledPackage(userId);
        }
    }

    public void scheduleReadDefaultPermissionExceptions() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void grantNonFixedPermToOtherSystemApps(int userId) {
        if (DEBUG_GRANT_ALL_SYSTEM_PERM) {
            PackageManagerService packageManagerService = this.mService;
            if (PackageManagerService.DEBUG_SHOW_INFO) {
                Slog.d(TAG, "grantNonFixedPermToOtherSystemApps");
            }
            synchronized (this.mService.mPackages) {
                for (Package pkg : this.mService.mPackages.values()) {
                    if (pkg.isSystemApp() && doesPackageSupportRuntimePermissions(pkg) && !pkg.requestedPermissions.isEmpty()) {
                        packageManagerService = this.mService;
                        if (PackageManagerService.DEBUG_SHOW_INFO) {
                            Slog.d(TAG, "debug: oppo grant runtime-all to " + pkg.packageName + ", systemFix=false");
                        }
                        Set<String> permissions = new ArraySet();
                        int permissionCount = pkg.requestedPermissions.size();
                        for (int i = 0; i < permissionCount; i++) {
                            String permission = (String) pkg.requestedPermissions.get(i);
                            BasePermission bp = (BasePermission) this.mService.mSettings.mPermissions.get(permission);
                            if (bp != null && bp.isRuntime()) {
                                permissions.add(permission);
                            }
                        }
                        if (!permissions.isEmpty()) {
                            grantRuntimePermissionsLPw(pkg, permissions, false, userId);
                        }
                    }
                }
            }
        }
    }

    private boolean includeInPermInfoList(ArrayList<RuntimePermFilterInfo> list, String pkg) {
        if (list == null || pkg == null) {
            return false;
        }
        for (RuntimePermFilterInfo info : list) {
            if (info != null && info.packageName.equals(pkg)) {
                return true;
            }
        }
        return false;
    }

    public void grantOppoFixRuntimePermssion(int userId, boolean supportRuntimeAlert) {
        PackageManagerService packageManagerService = this.mService;
        if (PackageManagerService.DEBUG_SHOW_INFO) {
            Slog.d(TAG, "grantOppoFixRuntimePermssion");
        }
        ArrayList<RuntimePermFilterInfo> permInfoList = ColorPackageManagerHelper.getFixedRuntimePermInfos(supportRuntimeAlert);
        ArrayList<String> globalList = OppoListManager.getInstance().getGlobalWhiteList(this.mService.mContext, 2);
        if (globalList != null) {
            ArrayList<RuntimePermFilterInfo> globalPermList = ColorPackageManagerHelper.getDefaultPermFilterInfosFromStr(globalList);
            if (globalPermList != null) {
                permInfoList.addAll(globalPermList);
            }
        }
        if (permInfoList != null && !permInfoList.isEmpty()) {
            for (RuntimePermFilterInfo info : permInfoList) {
                packageManagerService = this.mService;
                if (PackageManagerService.DEBUG_SHOW_INFO) {
                    Slog.d(TAG, "grantOppoFixRuntimePermssion supportRuntimeAlert=" + supportRuntimeAlert + ", info=" + info.packageName);
                }
                Package pkg;
                Set<String> permissions;
                if (!info.addAll) {
                    pkg = getPackageLPr(info.packageName);
                    if (pkg != null) {
                        for (String group : info.groups) {
                            packageManagerService = this.mService;
                            if (PackageManagerService.DEBUG_SHOW_INFO) {
                                Slog.d(TAG, "oppo grant runtime-" + group + " to " + info.packageName + ", systemFix=true");
                            }
                            permissions = getPermsForType(group);
                            if (permissions != null) {
                                grantRuntimePermissionsLPw(pkg, permissions, true, true, userId);
                            }
                        }
                    }
                } else if (info.packageName != null) {
                    packageManagerService = this.mService;
                    if (PackageManagerService.DEBUG_SHOW_INFO) {
                        Slog.d(TAG, "oppo grant runtime-all to " + info.packageName + ", systemFix=true");
                    }
                    pkg = getPackageLPr(info.packageName);
                    if (pkg != null) {
                        if (!doesPackageSupportRuntimePermissions(pkg) || pkg.requestedPermissions.isEmpty()) {
                            packageManagerService = this.mService;
                            if (PackageManagerService.DEBUG_SHOW_INFO) {
                                Slog.d(TAG, pkg + " do not support runtime, oppo skip");
                            }
                        } else {
                            permissions = new ArraySet();
                            int permissionCount = pkg.requestedPermissions.size();
                            for (int i = 0; i < permissionCount; i++) {
                                String permission = (String) pkg.requestedPermissions.get(i);
                                BasePermission bp = (BasePermission) this.mService.mSettings.mPermissions.get(permission);
                                if (bp != null && bp.isRuntime()) {
                                    permissions.add(permission);
                                }
                            }
                            if (!permissions.isEmpty()) {
                                grantRuntimePermissionsLPw(pkg, permissions, true, true, userId);
                            }
                        }
                    }
                }
            }
        }
    }

    public void grantOppoNonFixRuntimePermssion(int userId, boolean supportRuntimeAlert) {
        PackageManagerService packageManagerService = this.mService;
        if (PackageManagerService.DEBUG_SHOW_INFO) {
            Slog.d(TAG, "grantOppoNonFixRuntimePermssion");
        }
        ArrayList<RuntimePermFilterInfo> permInfoList = ColorPackageManagerHelper.getNonFixedRuntimePermInfos(supportRuntimeAlert);
        if (permInfoList != null && !permInfoList.isEmpty()) {
            for (RuntimePermFilterInfo info : permInfoList) {
                packageManagerService = this.mService;
                if (PackageManagerService.DEBUG_SHOW_INFO) {
                    Slog.d(TAG, "grantOppoNonFixRuntimePermssion supportRuntimeAlert=" + supportRuntimeAlert + ", info=" + info.packageName);
                }
                Package pkg;
                Set<String> permissions;
                if (!info.addAll) {
                    pkg = getPackageLPr(info.packageName);
                    if (pkg != null) {
                        for (String group : info.groups) {
                            packageManagerService = this.mService;
                            if (PackageManagerService.DEBUG_SHOW_INFO) {
                                Slog.d(TAG, "oppo grant runtime-" + group + " to " + info.packageName + ", systemFix=false");
                            }
                            permissions = getPermsForType(group);
                            if (permissions != null) {
                                grantRuntimePermissionsLPw(pkg, permissions, false, userId);
                            }
                        }
                    }
                } else if (info.packageName != null) {
                    packageManagerService = this.mService;
                    if (PackageManagerService.DEBUG_SHOW_INFO) {
                        Slog.d(TAG, "oppo grant runtime-all to " + info.packageName + ", systemFix=false");
                    }
                    pkg = getPackageLPr(info.packageName);
                    if (pkg != null) {
                        if (!doesPackageSupportRuntimePermissions(pkg) || pkg.requestedPermissions.isEmpty()) {
                            packageManagerService = this.mService;
                            if (PackageManagerService.DEBUG_SHOW_INFO) {
                                Slog.d(TAG, pkg + " do not support runtime, oppo skip");
                            }
                        } else {
                            permissions = new ArraySet();
                            int permissionCount = pkg.requestedPermissions.size();
                            for (int i = 0; i < permissionCount; i++) {
                                String permission = (String) pkg.requestedPermissions.get(i);
                                BasePermission bp = (BasePermission) this.mService.mSettings.mPermissions.get(permission);
                                if (bp != null && bp.isRuntime()) {
                                    permissions.add(permission);
                                }
                            }
                            if (!permissions.isEmpty()) {
                                grantRuntimePermissionsLPw(pkg, permissions, false, userId);
                            }
                        }
                    }
                }
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
        return null;
    }

    private void grantPermissionsToSysComponentsAndPrivApps(int userId) {
        Log.i(TAG, "Granting permissions to platform components for user " + userId);
        synchronized (this.mService.mPackages) {
            for (Package pkg : this.mService.mPackages.values()) {
                if (isSysComponentOrPersistentPlatformSignedPrivAppLPr(pkg) && doesPackageSupportRuntimePermissions(pkg) && !pkg.requestedPermissions.isEmpty()) {
                    Set<String> permissions = new ArraySet();
                    int permissionCount = pkg.requestedPermissions.size();
                    for (int i = 0; i < permissionCount; i++) {
                        String permission = (String) pkg.requestedPermissions.get(i);
                        BasePermission bp = (BasePermission) this.mService.mSettings.mPermissions.get(permission);
                        if (bp != null && bp.isRuntime()) {
                            permissions.add(permission);
                        }
                    }
                    if (!permissions.isEmpty()) {
                        grantRuntimePermissionsLPw(pkg, permissions, true, userId);
                    }
                }
            }
        }
    }

    private void grantDefaultSystemHandlerPermissions(int userId) {
        PackagesProvider locationPackagesProvider;
        PackagesProvider voiceInteractionPackagesProvider;
        PackagesProvider smsAppPackagesProvider;
        PackagesProvider dialerAppPackagesProvider;
        PackagesProvider simCallManagerPackagesProvider;
        SyncAdapterPackagesProvider syncAdapterPackagesProvider;
        Log.i(TAG, "Granting permissions to default platform handlers for user " + userId);
        synchronized (this.mService.mPackages) {
            locationPackagesProvider = this.mLocationPackagesProvider;
            voiceInteractionPackagesProvider = this.mVoiceInteractionPackagesProvider;
            smsAppPackagesProvider = this.mSmsAppPackagesProvider;
            dialerAppPackagesProvider = this.mDialerAppPackagesProvider;
            simCallManagerPackagesProvider = this.mSimCallManagerPackagesProvider;
            syncAdapterPackagesProvider = this.mSyncAdapterPackagesProvider;
        }
        String[] voiceInteractPackageNames = voiceInteractionPackagesProvider != null ? voiceInteractionPackagesProvider.getPackages(userId) : null;
        String[] locationPackageNames = locationPackagesProvider != null ? locationPackagesProvider.getPackages(userId) : null;
        String[] smsAppPackageNames = smsAppPackagesProvider != null ? smsAppPackagesProvider.getPackages(userId) : null;
        String[] dialerAppPackageNames = dialerAppPackagesProvider != null ? dialerAppPackagesProvider.getPackages(userId) : null;
        String[] simCallManagerPackageNames = simCallManagerPackagesProvider != null ? simCallManagerPackagesProvider.getPackages(userId) : null;
        String[] contactsSyncAdapterPackages = syncAdapterPackagesProvider != null ? syncAdapterPackagesProvider.getPackages("com.android.contacts", userId) : null;
        String[] calendarSyncAdapterPackages = syncAdapterPackagesProvider != null ? syncAdapterPackagesProvider.getPackages("com.android.calendar", userId) : null;
        synchronized (this.mService.mPackages) {
            Intent intent;
            int i;
            Package installerPackage = getSystemPackageLPr(this.mService.mRequiredInstallerPackage);
            if (installerPackage != null && doesPackageSupportRuntimePermissions(installerPackage)) {
                grantRuntimePermissionsLPw(installerPackage, STORAGE_PERMISSIONS, true, userId);
            }
            Package verifierPackage = getSystemPackageLPr(this.mService.mRequiredVerifierPackage);
            if (verifierPackage != null && doesPackageSupportRuntimePermissions(verifierPackage)) {
                grantRuntimePermissionsLPw(verifierPackage, STORAGE_PERMISSIONS, true, userId);
                grantRuntimePermissionsLPw(verifierPackage, PHONE_PERMISSIONS, false, userId);
                grantRuntimePermissionsLPw(verifierPackage, SMS_PERMISSIONS, false, userId);
            }
            Package setupPackage = getSystemPackageLPr(this.mService.mSetupWizardPackage);
            if (setupPackage != null && doesPackageSupportRuntimePermissions(setupPackage)) {
                grantRuntimePermissionsLPw(setupPackage, PHONE_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(setupPackage, CONTACTS_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(setupPackage, LOCATION_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(setupPackage, CAMERA_PERMISSIONS, userId);
            }
            Package cameraPackage = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.media.action.IMAGE_CAPTURE"), userId);
            if (cameraPackage != null && doesPackageSupportRuntimePermissions(cameraPackage)) {
                grantRuntimePermissionsLPw(cameraPackage, CAMERA_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(cameraPackage, MICROPHONE_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(cameraPackage, STORAGE_PERMISSIONS, userId);
            }
            Package mediaStorePackage = getDefaultProviderAuthorityPackageLPr(OppoProcessManager.RESUME_REASON_MEDIA_STR, userId);
            if (mediaStorePackage != null) {
                grantRuntimePermissionsLPw(mediaStorePackage, STORAGE_PERMISSIONS, true, userId);
            }
            Package downloadsPackage = getDefaultProviderAuthorityPackageLPr("downloads", userId);
            if (downloadsPackage != null) {
                grantRuntimePermissionsLPw(downloadsPackage, STORAGE_PERMISSIONS, true, userId);
            }
            Package downloadsUiPackage = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.intent.action.VIEW_DOWNLOADS"), userId);
            if (downloadsUiPackage != null && doesPackageSupportRuntimePermissions(downloadsUiPackage)) {
                grantRuntimePermissionsLPw(downloadsUiPackage, STORAGE_PERMISSIONS, true, userId);
            }
            Package storagePackage = getDefaultProviderAuthorityPackageLPr("com.android.externalstorage.documents", userId);
            if (storagePackage != null) {
                grantRuntimePermissionsLPw(storagePackage, STORAGE_PERMISSIONS, true, userId);
            }
            Package certInstallerPackage = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.credentials.INSTALL"), userId);
            if (certInstallerPackage != null && doesPackageSupportRuntimePermissions(certInstallerPackage)) {
                grantRuntimePermissionsLPw(certInstallerPackage, STORAGE_PERMISSIONS, true, userId);
            }
            Package dialerPackage;
            if (dialerAppPackageNames == null) {
                dialerPackage = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.intent.action.DIAL"), userId);
                if (dialerPackage != null) {
                    grantDefaultPermissionsToDefaultSystemDialerAppLPr(dialerPackage, userId);
                }
            } else {
                for (String dialerAppPackageName : dialerAppPackageNames) {
                    dialerPackage = getSystemPackageLPr(dialerAppPackageName);
                    if (dialerPackage != null) {
                        grantDefaultPermissionsToDefaultSystemDialerAppLPr(dialerPackage, userId);
                    }
                }
            }
            if (simCallManagerPackageNames != null) {
                for (String simCallManagerPackageName : simCallManagerPackageNames) {
                    Package simCallManagerPackage = getSystemPackageLPr(simCallManagerPackageName);
                    if (simCallManagerPackage != null) {
                        grantDefaultPermissionsToDefaultSimCallManagerLPr(simCallManagerPackage, userId);
                    }
                }
            }
            Package smsPackage;
            if (smsAppPackageNames == null) {
                intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.APP_MESSAGING");
                smsPackage = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
                if (smsPackage != null) {
                    grantDefaultPermissionsToDefaultSystemSmsAppLPr(smsPackage, userId);
                }
            } else {
                for (String smsPackageName : smsAppPackageNames) {
                    smsPackage = getSystemPackageLPr(smsPackageName);
                    if (smsPackage != null) {
                        grantDefaultPermissionsToDefaultSystemSmsAppLPr(smsPackage, userId);
                    }
                }
            }
            Package cbrPackage = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.provider.Telephony.SMS_CB_RECEIVED"), userId);
            if (cbrPackage != null && doesPackageSupportRuntimePermissions(cbrPackage)) {
                grantRuntimePermissionsLPw(cbrPackage, SMS_PERMISSIONS, userId);
            }
            Package carrierProvPackage = getDefaultSystemHandlerServicePackageLPr(new Intent("android.provider.Telephony.SMS_CARRIER_PROVISION"), userId);
            if (carrierProvPackage != null && doesPackageSupportRuntimePermissions(carrierProvPackage)) {
                grantRuntimePermissionsLPw(carrierProvPackage, SMS_PERMISSIONS, false, userId);
            }
            Intent calendarIntent = new Intent("android.intent.action.MAIN");
            calendarIntent.addCategory("android.intent.category.APP_CALENDAR");
            Package calendarPackage = getDefaultSystemHandlerActivityPackageLPr(calendarIntent, userId);
            if (calendarPackage != null && doesPackageSupportRuntimePermissions(calendarPackage)) {
                grantRuntimePermissionsLPw(calendarPackage, CALENDAR_PERMISSIONS, userId);
                if (!CtaUtils.isCtaSupported()) {
                    grantRuntimePermissionsLPw(calendarPackage, CONTACTS_PERMISSIONS, userId);
                }
            }
            Package calendarProviderPackage = getDefaultProviderAuthorityPackageLPr("com.android.calendar", userId);
            if (calendarProviderPackage != null) {
                grantRuntimePermissionsLPw(calendarProviderPackage, CONTACTS_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(calendarProviderPackage, CALENDAR_PERMISSIONS, true, userId);
                grantRuntimePermissionsLPw(calendarProviderPackage, STORAGE_PERMISSIONS, userId);
            }
            List<Package> calendarSyncAdapters = getHeadlessSyncAdapterPackagesLPr(calendarSyncAdapterPackages, userId);
            int calendarSyncAdapterCount = calendarSyncAdapters.size();
            for (i = 0; i < calendarSyncAdapterCount; i++) {
                Package calendarSyncAdapter = (Package) calendarSyncAdapters.get(i);
                if (doesPackageSupportRuntimePermissions(calendarSyncAdapter)) {
                    grantRuntimePermissionsLPw(calendarSyncAdapter, CALENDAR_PERMISSIONS, userId);
                }
            }
            intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.APP_CONTACTS");
            Package contactsPackage = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
            if (contactsPackage != null && doesPackageSupportRuntimePermissions(contactsPackage)) {
                grantRuntimePermissionsLPw(contactsPackage, CONTACTS_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(contactsPackage, PHONE_PERMISSIONS, userId);
            }
            List<Package> contactsSyncAdapters = getHeadlessSyncAdapterPackagesLPr(contactsSyncAdapterPackages, userId);
            int contactsSyncAdapterCount = contactsSyncAdapters.size();
            for (i = 0; i < contactsSyncAdapterCount; i++) {
                Package contactsSyncAdapter = (Package) contactsSyncAdapters.get(i);
                if (doesPackageSupportRuntimePermissions(contactsSyncAdapter)) {
                    grantRuntimePermissionsLPw(contactsSyncAdapter, CONTACTS_PERMISSIONS, userId);
                }
            }
            Package contactsProviderPackage = getDefaultProviderAuthorityPackageLPr("com.android.contacts", userId);
            if (contactsProviderPackage != null) {
                grantRuntimePermissionsLPw(contactsProviderPackage, CONTACTS_PERMISSIONS, true, userId);
                grantRuntimePermissionsLPw(contactsProviderPackage, PHONE_PERMISSIONS, true, userId);
                grantRuntimePermissionsLPw(contactsProviderPackage, STORAGE_PERMISSIONS, userId);
            }
            Package deviceProvisionPackage = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.app.action.PROVISION_MANAGED_DEVICE"), userId);
            if (deviceProvisionPackage != null && doesPackageSupportRuntimePermissions(deviceProvisionPackage)) {
                grantRuntimePermissionsLPw(deviceProvisionPackage, CONTACTS_PERMISSIONS, userId);
            }
            intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.APP_MAPS");
            Package mapsPackage = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
            if (mapsPackage != null && doesPackageSupportRuntimePermissions(mapsPackage)) {
                grantRuntimePermissionsLPw(mapsPackage, LOCATION_PERMISSIONS, userId);
            }
            intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.APP_GALLERY");
            Package galleryPackage = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
            if (galleryPackage != null && doesPackageSupportRuntimePermissions(galleryPackage)) {
                grantRuntimePermissionsLPw(galleryPackage, STORAGE_PERMISSIONS, userId);
            }
            intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.APP_EMAIL");
            Package emailPackage = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
            if (emailPackage != null && doesPackageSupportRuntimePermissions(emailPackage)) {
                grantRuntimePermissionsLPw(emailPackage, CONTACTS_PERMISSIONS, userId);
                grantRuntimePermissionsLPw(emailPackage, CALENDAR_PERMISSIONS, userId);
            }
            Package browserPackage = null;
            String defaultBrowserPackage = this.mService.getDefaultBrowserPackageName(userId);
            if (defaultBrowserPackage != null) {
                browserPackage = getPackageLPr(defaultBrowserPackage);
            }
            if (browserPackage == null) {
                Intent browserIntent = new Intent("android.intent.action.MAIN");
                browserIntent.addCategory("android.intent.category.APP_BROWSER");
                browserPackage = getDefaultSystemHandlerActivityPackageLPr(browserIntent, userId);
            }
            if (browserPackage != null && doesPackageSupportRuntimePermissions(browserPackage)) {
                grantRuntimePermissionsLPw(browserPackage, LOCATION_PERMISSIONS, userId);
            }
            if (voiceInteractPackageNames != null) {
                for (String voiceInteractPackageName : voiceInteractPackageNames) {
                    Package voiceInteractPackage = getSystemPackageLPr(voiceInteractPackageName);
                    if (voiceInteractPackage != null && doesPackageSupportRuntimePermissions(voiceInteractPackage)) {
                        grantRuntimePermissionsLPw(voiceInteractPackage, CONTACTS_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(voiceInteractPackage, CALENDAR_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(voiceInteractPackage, MICROPHONE_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(voiceInteractPackage, PHONE_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(voiceInteractPackage, SMS_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(voiceInteractPackage, LOCATION_PERMISSIONS, userId);
                    }
                }
            }
            intent = new Intent("android.speech.RecognitionService");
            intent.addCategory("android.intent.category.DEFAULT");
            Package voiceRecoPackage = getDefaultSystemHandlerServicePackageLPr(intent, userId);
            if (voiceRecoPackage != null && doesPackageSupportRuntimePermissions(voiceRecoPackage)) {
                grantRuntimePermissionsLPw(voiceRecoPackage, MICROPHONE_PERMISSIONS, userId);
            }
            if (locationPackageNames != null) {
                for (String packageName : locationPackageNames) {
                    Package locationPackage = getSystemPackageLPr(packageName);
                    if (locationPackage != null && doesPackageSupportRuntimePermissions(locationPackage)) {
                        grantRuntimePermissionsLPw(locationPackage, CONTACTS_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(locationPackage, CALENDAR_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(locationPackage, MICROPHONE_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(locationPackage, PHONE_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(locationPackage, SMS_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(locationPackage, LOCATION_PERMISSIONS, true, userId);
                        grantRuntimePermissionsLPw(locationPackage, CAMERA_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(locationPackage, SENSORS_PERMISSIONS, userId);
                        grantRuntimePermissionsLPw(locationPackage, STORAGE_PERMISSIONS, userId);
                    }
                }
            }
            intent = new Intent("android.intent.action.VIEW");
            intent.addCategory("android.intent.category.DEFAULT");
            intent.setDataAndType(Uri.fromFile(new File("foo.mp3")), AUDIO_MIME_TYPE);
            Package musicPackage = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
            if (musicPackage != null && doesPackageSupportRuntimePermissions(musicPackage)) {
                grantRuntimePermissionsLPw(musicPackage, STORAGE_PERMISSIONS, userId);
            }
            if (this.mService.hasSystemFeature("android.hardware.type.watch", 0)) {
                intent = new Intent("android.intent.action.MAIN");
                intent.addCategory("android.intent.category.HOME_MAIN");
                Package wearHomePackage = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
                if (wearHomePackage != null && doesPackageSupportRuntimePermissions(wearHomePackage)) {
                    grantRuntimePermissionsLPw(wearHomePackage, CONTACTS_PERMISSIONS, false, userId);
                    grantRuntimePermissionsLPw(wearHomePackage, PHONE_PERMISSIONS, true, userId);
                    grantRuntimePermissionsLPw(wearHomePackage, MICROPHONE_PERMISSIONS, false, userId);
                    grantRuntimePermissionsLPw(wearHomePackage, LOCATION_PERMISSIONS, false, userId);
                }
            }
            Package printSpoolerPackage = getSystemPackageLPr("com.android.printspooler");
            if (printSpoolerPackage != null && doesPackageSupportRuntimePermissions(printSpoolerPackage)) {
                grantRuntimePermissionsLPw(printSpoolerPackage, LOCATION_PERMISSIONS, true, userId);
            }
            Package emergencyInfoPckg = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.telephony.action.EMERGENCY_ASSISTANCE"), userId);
            if (emergencyInfoPckg != null && doesPackageSupportRuntimePermissions(emergencyInfoPckg)) {
                grantRuntimePermissionsLPw(emergencyInfoPckg, CONTACTS_PERMISSIONS, true, userId);
                grantRuntimePermissionsLPw(emergencyInfoPckg, PHONE_PERMISSIONS, true, userId);
            }
            intent = new Intent("android.intent.action.VIEW");
            intent.setType("vnd.android.cursor.item/ndef_msg");
            Package nfcTagPkg = getDefaultSystemHandlerActivityPackageLPr(intent, userId);
            if (nfcTagPkg != null && doesPackageSupportRuntimePermissions(nfcTagPkg)) {
                grantRuntimePermissionsLPw(nfcTagPkg, CONTACTS_PERMISSIONS, false, userId);
                grantRuntimePermissionsLPw(nfcTagPkg, PHONE_PERMISSIONS, false, userId);
            }
            Package storageManagerPckg = getDefaultSystemHandlerActivityPackageLPr(new Intent("android.os.storage.action.MANAGE_STORAGE"), userId);
            if (storageManagerPckg != null && doesPackageSupportRuntimePermissions(storageManagerPckg)) {
                grantRuntimePermissionsLPw(storageManagerPckg, STORAGE_PERMISSIONS, true, userId);
            }
            this.mService.mSettings.onDefaultRuntimePermissionsGrantedLPr(userId);
        }
    }

    private void grantDefaultPermissionsToDefaultSystemDialerAppLPr(Package dialerPackage, int userId) {
        if (doesPackageSupportRuntimePermissions(dialerPackage)) {
            grantRuntimePermissionsLPw(dialerPackage, PHONE_PERMISSIONS, this.mService.hasSystemFeature("android.hardware.type.watch", 0), userId);
            grantRuntimePermissionsLPw(dialerPackage, CONTACTS_PERMISSIONS, userId);
            grantRuntimePermissionsLPw(dialerPackage, SMS_PERMISSIONS, userId);
            grantRuntimePermissionsLPw(dialerPackage, MICROPHONE_PERMISSIONS, userId);
            grantRuntimePermissionsLPw(dialerPackage, CAMERA_PERMISSIONS, userId);
        }
    }

    private void grantDefaultPermissionsToDefaultSystemSmsAppLPr(Package smsPackage, int userId) {
        if (doesPackageSupportRuntimePermissions(smsPackage)) {
            grantRuntimePermissionsLPw(smsPackage, PHONE_PERMISSIONS, userId);
            grantRuntimePermissionsLPw(smsPackage, CONTACTS_PERMISSIONS, userId);
            grantRuntimePermissionsLPw(smsPackage, SMS_PERMISSIONS, userId);
        }
    }

    public void grantDefaultPermissionsToDefaultSmsAppLPr(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default sms app for user:" + userId);
        if (packageName != null) {
            Package smsPackage = getPackageLPr(packageName);
            if (smsPackage != null && doesPackageSupportRuntimePermissions(smsPackage)) {
                grantRuntimePermissionsLPw(smsPackage, PHONE_PERMISSIONS, false, true, userId);
                grantRuntimePermissionsLPw(smsPackage, CONTACTS_PERMISSIONS, false, true, userId);
                grantRuntimePermissionsLPw(smsPackage, SMS_PERMISSIONS, false, true, userId);
            }
        }
    }

    public void grantDefaultPermissionsToDefaultDialerAppLPr(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default dialer app for user:" + userId);
        if (packageName != null) {
            Package dialerPackage = getPackageLPr(packageName);
            if (dialerPackage != null && doesPackageSupportRuntimePermissions(dialerPackage)) {
                grantRuntimePermissionsLPw(dialerPackage, PHONE_PERMISSIONS, false, true, userId);
                grantRuntimePermissionsLPw(dialerPackage, CONTACTS_PERMISSIONS, false, true, userId);
                grantRuntimePermissionsLPw(dialerPackage, SMS_PERMISSIONS, false, true, userId);
                grantRuntimePermissionsLPw(dialerPackage, MICROPHONE_PERMISSIONS, false, true, userId);
                grantRuntimePermissionsLPw(dialerPackage, CAMERA_PERMISSIONS, false, true, userId);
            }
        }
    }

    private void grantDefaultPermissionsToDefaultSimCallManagerLPr(Package simCallManagerPackage, int userId) {
        Log.i(TAG, "Granting permissions to sim call manager for user:" + userId);
        if (doesPackageSupportRuntimePermissions(simCallManagerPackage)) {
            grantRuntimePermissionsLPw(simCallManagerPackage, PHONE_PERMISSIONS, userId);
            grantRuntimePermissionsLPw(simCallManagerPackage, MICROPHONE_PERMISSIONS, userId);
        }
    }

    public void grantDefaultPermissionsToDefaultSimCallManagerLPr(String packageName, int userId) {
        if (packageName != null) {
            Package simCallManagerPackage = getPackageLPr(packageName);
            if (simCallManagerPackage != null) {
                grantDefaultPermissionsToDefaultSimCallManagerLPr(simCallManagerPackage, userId);
            }
        }
    }

    public void grantDefaultPermissionsToEnabledCarrierAppsLPr(String[] packageNames, int userId) {
        Log.i(TAG, "Granting permissions to enabled carrier apps for user:" + userId);
        if (packageNames != null) {
            for (String packageName : packageNames) {
                Package carrierPackage = getSystemPackageLPr(packageName);
                if (carrierPackage != null && doesPackageSupportRuntimePermissions(carrierPackage)) {
                    grantRuntimePermissionsLPw(carrierPackage, PHONE_PERMISSIONS, userId);
                    grantRuntimePermissionsLPw(carrierPackage, LOCATION_PERMISSIONS, userId);
                    grantRuntimePermissionsLPw(carrierPackage, SMS_PERMISSIONS, userId);
                }
            }
        }
    }

    public void grantDefaultPermissionsToDefaultBrowserLPr(String packageName, int userId) {
        Log.i(TAG, "Granting permissions to default browser for user:" + userId);
        if (packageName != null) {
            Package browserPackage = getSystemPackageLPr(packageName);
            if (browserPackage != null && doesPackageSupportRuntimePermissions(browserPackage)) {
                grantRuntimePermissionsLPw(browserPackage, LOCATION_PERMISSIONS, false, false, userId);
            }
        }
    }

    private Package getDefaultSystemHandlerActivityPackageLPr(Intent intent, int userId) {
        ResolveInfo handler = this.mService.resolveIntent(intent, intent.resolveType(this.mService.mContext.getContentResolver()), DEFAULT_FLAGS, userId);
        if (handler == null || handler.activityInfo == null) {
            return null;
        }
        ActivityInfo activityInfo = handler.activityInfo;
        if (activityInfo.packageName.equals(this.mService.mResolveActivity.packageName) && activityInfo.name.equals(this.mService.mResolveActivity.name)) {
            return null;
        }
        return getSystemPackageLPr(handler.activityInfo.packageName);
    }

    private Package getDefaultSystemHandlerServicePackageLPr(Intent intent, int userId) {
        List<ResolveInfo> handlers = this.mService.queryIntentServices(intent, intent.resolveType(this.mService.mContext.getContentResolver()), DEFAULT_FLAGS, userId).getList();
        if (handlers == null) {
            return null;
        }
        int handlerCount = handlers.size();
        for (int i = 0; i < handlerCount; i++) {
            Package handlerPackage = getSystemPackageLPr(((ResolveInfo) handlers.get(i)).serviceInfo.packageName);
            if (handlerPackage != null) {
                return handlerPackage;
            }
        }
        return null;
    }

    private List<Package> getHeadlessSyncAdapterPackagesLPr(String[] syncAdapterPackageNames, int userId) {
        List<Package> syncAdapterPackages = new ArrayList();
        Intent homeIntent = new Intent("android.intent.action.MAIN");
        homeIntent.addCategory("android.intent.category.LAUNCHER");
        for (String syncAdapterPackageName : syncAdapterPackageNames) {
            homeIntent.setPackage(syncAdapterPackageName);
            if (this.mService.resolveIntent(homeIntent, homeIntent.resolveType(this.mService.mContext.getContentResolver()), DEFAULT_FLAGS, userId) == null) {
                Package syncAdapterPackage = getSystemPackageLPr(syncAdapterPackageName);
                if (syncAdapterPackage != null) {
                    syncAdapterPackages.add(syncAdapterPackage);
                }
            }
        }
        return syncAdapterPackages;
    }

    private Package getDefaultProviderAuthorityPackageLPr(String authority, int userId) {
        ProviderInfo provider = this.mService.resolveContentProvider(authority, DEFAULT_FLAGS, userId);
        if (provider != null) {
            return getSystemPackageLPr(provider.packageName);
        }
        return null;
    }

    private Package getPackageLPr(String packageName) {
        return (Package) this.mService.mPackages.get(packageName);
    }

    private Package getSystemPackageLPr(String packageName) {
        Package pkg = getPackageLPr(packageName);
        if (pkg == null || !pkg.isSystemApp()) {
            return null;
        }
        if (isSysComponentOrPersistentPlatformSignedPrivAppLPr(pkg)) {
            pkg = null;
        }
        return pkg;
    }

    private void grantRuntimePermissionsLPw(Package pkg, Set<String> permissions, int userId) {
        grantRuntimePermissionsLPw(pkg, permissions, false, false, userId);
    }

    private void grantRuntimePermissionsLPw(Package pkg, Set<String> permissions, boolean systemFixed, int userId) {
        grantRuntimePermissionsLPw(pkg, permissions, systemFixed, false, userId);
    }

    /* JADX WARNING: Missing block: B:37:0x00b0, code:
            if (isNeedToGrantAppOfForceShowList(r21.packageName, r13) != false) goto L_0x00b2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void grantRuntimePermissionsLPw(Package pkg, Set<String> permissions, boolean systemFixed, boolean isDefaultPhoneOrSms, int userId) {
        if (!pkg.requestedPermissions.isEmpty()) {
            List<String> requestedPermissions = pkg.requestedPermissions;
            Set<String> grantablePermissions = null;
            if (!isDefaultPhoneOrSms && pkg.isUpdatedSystemApp()) {
                PackageSetting sysPs = this.mService.mSettings.getDisabledSystemPkgLPr(pkg.packageName);
                if (!(sysPs == null || sysPs.pkg == null)) {
                    if (!sysPs.pkg.requestedPermissions.isEmpty()) {
                        if (!requestedPermissions.equals(sysPs.pkg.requestedPermissions)) {
                            Set<String> arraySet = new ArraySet(requestedPermissions);
                            requestedPermissions = sysPs.pkg.requestedPermissions;
                        }
                    } else {
                        return;
                    }
                }
            }
            int grantablePermissionCount = requestedPermissions.size();
            for (int i = 0; i < grantablePermissionCount; i++) {
                String permission = (String) requestedPermissions.get(i);
                if ((grantablePermissions == null || grantablePermissions.contains(permission)) && permissions.contains(permission)) {
                    int flags = this.mService.getPermissionFlags(permission, pkg.packageName, userId);
                    boolean forceGrantCtaPerm;
                    if (!CtaUtils.isCtaOnlyPermission(permission) || (flags & 64) == 0) {
                        forceGrantCtaPerm = false;
                    } else if ((flags & 1) == 0) {
                        forceGrantCtaPerm = true;
                    } else {
                        forceGrantCtaPerm = false;
                    }
                    if (!(flags == 0 || isDefaultPhoneOrSms || forceGrantCtaPerm)) {
                    }
                    if ((flags & 20) == 0) {
                        this.mService.grantRuntimePermission(pkg.packageName, permission, userId);
                        int newFlags = 32;
                        if (systemFixed) {
                            newFlags = 48;
                        }
                        this.mService.updatePermissionFlags(permission, pkg.packageName, newFlags, newFlags, userId);
                        if (!((flags & 32) == 0 || (flags & 16) == 0 || systemFixed)) {
                            this.mService.updatePermissionFlags(permission, pkg.packageName, 16, 0, userId);
                        }
                    }
                }
            }
        }
    }

    private boolean isListedInForceShow(String pkgName) {
        if (mForcePermReviewPkgs == null) {
            mForcePermReviewPkgs = new HashSet(Arrays.asList(this.mService.mContext.getResources().getStringArray(134479885)));
        }
        return mForcePermReviewPkgs.contains(pkgName);
    }

    private boolean isNeedToGrantAppOfForceShowList(String pkg, int flags) {
        if (!CtaUtils.isCtaSupported()) {
            return false;
        }
        boolean isReviewNeeded = (flags & 64) != 0;
        boolean isNotUserSet = (flags & 1) == 0;
        boolean isListed = isListedInForceShow(pkg);
        if (!(isReviewNeeded && isNotUserSet)) {
            isListed = false;
        }
        return isListed;
    }

    private boolean isSysComponentOrPersistentPlatformSignedPrivAppLPr(Package pkg) {
        boolean z = true;
        if (UserHandle.getAppId(pkg.applicationInfo.uid) < 10000) {
            return true;
        }
        if (!pkg.isPrivilegedApp()) {
            return false;
        }
        PackageSetting sysPkg = this.mService.mSettings.getDisabledSystemPkgLPr(pkg.packageName);
        if (sysPkg == null || sysPkg.pkg == null) {
            if ((pkg.applicationInfo.flags & 8) == 0) {
                return false;
            }
        } else if ((sysPkg.pkg.applicationInfo.flags & 8) == 0) {
            return false;
        }
        if (PackageManagerService.compareSignatures(this.mService.mPlatformPackage.mSignatures, pkg.mSignatures) != 0) {
            z = false;
        }
        return z;
    }

    private void grantDefaultPermissionExceptions(int userId) {
        synchronized (this.mService.mPackages) {
            this.mHandler.removeMessages(1);
            if (this.mGrantExceptions == null) {
                this.mGrantExceptions = readDefaultPermissionExceptionsLPw();
            }
            Set<String> permissions = null;
            int exceptionCount = this.mGrantExceptions.size();
            for (int i = 0; i < exceptionCount; i++) {
                Package pkg = getSystemPackageLPr((String) this.mGrantExceptions.keyAt(i));
                List<DefaultPermissionGrant> permissionGrants = (List) this.mGrantExceptions.valueAt(i);
                int permissionGrantCount = permissionGrants.size();
                for (int j = 0; j < permissionGrantCount; j++) {
                    DefaultPermissionGrant permissionGrant = (DefaultPermissionGrant) permissionGrants.get(j);
                    if (permissions == null) {
                        permissions = new ArraySet();
                    } else {
                        permissions.clear();
                    }
                    permissions.add(permissionGrant.name);
                    grantRuntimePermissionsLPw(pkg, permissions, false, permissionGrant.fixed, userId);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:33:0x00c1 A:{Splitter: B:31:0x00c0, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException)} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00ee A:{Splitter: B:46:0x00ed, ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Catch:{ XmlPullParserException -> 0x00ee, XmlPullParserException -> 0x00ee }} */
    /* JADX WARNING: Removed duplicated region for block: B:43:0x00e8 A:{SYNTHETIC, Splitter: B:43:0x00e8} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x00fb A:{SYNTHETIC} */
    /* JADX WARNING: Removed duplicated region for block: B:46:0x00ed A:{SYNTHETIC, Splitter: B:46:0x00ed} */
    /* JADX WARNING: Missing block: B:33:0x00c1, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:34:0x00c2, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:35:0x00c3, code:
            android.util.Slog.w(TAG, "Error reading default permissions file " + r2, r1);
     */
    /* JADX WARNING: Missing block: B:48:0x00ee, code:
            r1 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayMap<String, List<DefaultPermissionGrant>> readDefaultPermissionExceptionsLPw() {
        Throwable th;
        File dir = new File(Environment.getRootDirectory(), "etc/default-permissions");
        if (!dir.exists() || !dir.isDirectory() || !dir.canRead()) {
            return new ArrayMap(0);
        }
        File[] files = dir.listFiles();
        if (files == null) {
            return new ArrayMap(0);
        }
        ArrayMap<String, List<DefaultPermissionGrant>> grantExceptions = new ArrayMap();
        for (File file : files) {
            if (!file.getPath().endsWith(".xml")) {
                Slog.i(TAG, "Non-xml file " + file + " in " + dir + " directory, ignoring");
            } else if (file.canRead()) {
                InputStream str = null;
                Throwable th2;
                try {
                    InputStream str2 = new BufferedInputStream(new FileInputStream(file));
                    try {
                        XmlPullParser parser = Xml.newPullParser();
                        parser.setInput(str2, null);
                        parse(parser, grantExceptions);
                        if (str2 != null) {
                            try {
                                str2.close();
                            } catch (Throwable th3) {
                                th2 = th3;
                            }
                        }
                        th2 = null;
                        if (th2 != null) {
                            try {
                                throw th2;
                            } catch (XmlPullParserException e) {
                            }
                        }
                    } catch (Throwable th4) {
                        th2 = th4;
                        th = null;
                        str = str2;
                        if (str != null) {
                        }
                        if (th == null) {
                        }
                    }
                } catch (Throwable th5) {
                    th2 = th5;
                    th = null;
                    if (str != null) {
                        try {
                            str.close();
                        } catch (Throwable th6) {
                            if (th == null) {
                                th = th6;
                            } else if (th != th6) {
                                th.addSuppressed(th6);
                            }
                        }
                    }
                    if (th == null) {
                        try {
                            throw th;
                        } catch (XmlPullParserException e2) {
                        }
                    } else {
                        throw th2;
                    }
                }
            } else {
                Slog.w(TAG, "Default permissions file " + file + " cannot be read");
            }
        }
        return grantExceptions;
    }

    private void parse(XmlPullParser parser, Map<String, List<DefaultPermissionGrant>> outGrantExceptions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (TAG_EXCEPTIONS.equals(parser.getName())) {
                    parseExceptions(parser, outGrantExceptions);
                } else {
                    Log.e(TAG, "Unknown tag " + parser.getName());
                }
            }
        }
    }

    private void parseExceptions(XmlPullParser parser, Map<String, List<DefaultPermissionGrant>> outGrantExceptions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (TAG_EXCEPTION.equals(parser.getName())) {
                    String packageName = parser.getAttributeValue(null, ATTR_PACKAGE);
                    List<DefaultPermissionGrant> packageExceptions = (List) outGrantExceptions.get(packageName);
                    if (packageExceptions == null) {
                        Package pkg = getSystemPackageLPr(packageName);
                        if (pkg == null) {
                            Log.w(TAG, "Unknown package:" + packageName);
                            XmlUtils.skipCurrentTag(parser);
                        } else if (doesPackageSupportRuntimePermissions(pkg)) {
                            packageExceptions = new ArrayList();
                            outGrantExceptions.put(packageName, packageExceptions);
                        } else {
                            Log.w(TAG, "Skipping non supporting runtime permissions package:" + packageName);
                            XmlUtils.skipCurrentTag(parser);
                        }
                    }
                    parsePermission(parser, packageExceptions);
                } else {
                    Log.e(TAG, "Unknown tag " + parser.getName() + "under <exceptions>");
                }
            }
        }
    }

    private void parsePermission(XmlPullParser parser, List<DefaultPermissionGrant> outPackageExceptions) throws IOException, XmlPullParserException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1) {
                return;
            }
            if (type == 3 && parser.getDepth() <= outerDepth) {
                return;
            }
            if (!(type == 3 || type == 4)) {
                if (TAG_PERMISSION.contains(parser.getName())) {
                    String name = parser.getAttributeValue(null, ATTR_NAME);
                    if (name == null) {
                        Log.w(TAG, "Mandatory name attribute missing for permission tag");
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        outPackageExceptions.add(new DefaultPermissionGrant(name, XmlUtils.readBooleanAttribute(parser, ATTR_FIXED)));
                    }
                } else {
                    Log.e(TAG, "Unknown tag " + parser.getName() + "under <exception>");
                }
            }
        }
    }

    private static boolean doesPackageSupportRuntimePermissions(Package pkg) {
        return pkg.applicationInfo.targetSdkVersion > 22;
    }

    public void grantCtaPermToPreInstalledPackage(int userId) {
        Log.d(TAG, "grantCtaPermToPreInstalledPackage userId = " + userId);
        synchronized (this.mService.mPackages) {
            for (Package pkg : this.mService.mPackages.values()) {
                if (doesPackageSupportRuntimePermissions(pkg) && !pkg.requestedPermissions.isEmpty()) {
                    Set<String> permissions = new ArraySet();
                    int permissionCount = pkg.requestedPermissions.size();
                    for (int i = 0; i < permissionCount; i++) {
                        String permission = (String) pkg.requestedPermissions.get(i);
                        BasePermission bp = (BasePermission) this.mService.mSettings.mPermissions.get(permission);
                        if (bp != null && bp.isRuntime() && CtaUtils.isCtaOnlyPermission(bp.name)) {
                            Log.d(TAG, "grantCtaPermToPreInstalledPackage grant " + permission + " to pre-installed package: " + pkg.packageName);
                            permissions.add(permission);
                        }
                    }
                    if (!permissions.isEmpty()) {
                        grantRuntimePermissionsLPw(pkg, permissions, isSysComponentOrPersistentPlatformSignedPrivAppLPr(pkg), false, userId);
                    }
                }
            }
        }
    }
}
