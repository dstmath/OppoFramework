package com.android.server.pm;

import android.common.OppoFeatureCache;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.OppoPackageManagerInternal;
import android.os.Binder;
import android.util.Slog;
import com.android.server.LocalServices;
import com.android.server.pm.RomUpdateHelper;
import com.color.util.ColorTypeCastingHelper;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class AppFrozenWhiteListHelper extends RomUpdateHelper {
    private static final String DATA_FILE_DIR = "data/system/appfrozen_config.xml";
    private static boolean DEBUG_SHOW_INFO = false;
    private static final ArrayList<String> DEFAULT_APP_FROZEN_COMP_WHITE_LIST = new ArrayList<>(Arrays.asList("com.tencent.mm.sdk.plugin.provider,com.tencent.mm"));
    private static final String FILTER_NAME = "sys_startupmanager_config_list";
    private static final String SYS_FILE_DIR = "system/etc/appfrozen_config.xml";
    private static final String TAG = "PackageManager";
    private static AppFrozenWhiteListHelper sAppFrozenWhiteListHelper = null;
    /* access modifiers changed from: private */
    public ArrayList<String> mActionWhiteList = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<String> mProviderWhiteList = new ArrayList<>();
    /* access modifiers changed from: private */
    public ArrayList<String> mServiceWhiteList = new ArrayList<>();

    public static AppFrozenWhiteListHelper getInstance(Context context) {
        if (sAppFrozenWhiteListHelper == null) {
            sAppFrozenWhiteListHelper = new AppFrozenWhiteListHelper(context);
        }
        return sAppFrozenWhiteListHelper;
    }

    private class AppFrozenUpdateInfo extends RomUpdateHelper.UpdateInfo {
        private static final String ACTION_WHITE_LIST = "actionfrozen";
        private static final String PROVIDER_WHITE_LIST = "providerfrozen";
        private static final String SERVICE_WHITE_LIST = "sevicefrozen";

        public AppFrozenUpdateInfo() {
            super();
        }

        @Override // com.android.server.pm.RomUpdateHelper.UpdateInfo
        public void parseContentFromXML(String content) {
            int type;
            String pkgTmp;
            if (content != null && !content.isEmpty()) {
                AppFrozenWhiteListHelper.this.mProviderWhiteList.clear();
                AppFrozenWhiteListHelper.this.mServiceWhiteList.clear();
                AppFrozenWhiteListHelper.this.mActionWhiteList.clear();
                try {
                    XmlPullParser parser = XmlPullParserFactory.newInstance().newPullParser();
                    parser.setInput(new StringReader(content));
                    parser.nextTag();
                    do {
                        type = parser.next();
                        if (type == 2) {
                            String tag = parser.getName();
                            if (PROVIDER_WHITE_LIST.equals(tag)) {
                                String pkgTmp2 = parser.nextText().trim();
                                if (pkgTmp2 != null && pkgTmp2.length() > 0) {
                                    AppFrozenWhiteListHelper.this.mProviderWhiteList.add(pkgTmp2);
                                }
                            } else if (SERVICE_WHITE_LIST.equals(tag)) {
                                String pkgTmp3 = parser.nextText().trim();
                                if (pkgTmp3 != null && pkgTmp3.length() > 0) {
                                    AppFrozenWhiteListHelper.this.mServiceWhiteList.add(pkgTmp3);
                                }
                            } else if (ACTION_WHITE_LIST.equals(tag) && (pkgTmp = parser.nextText().trim()) != null && pkgTmp.length() > 0) {
                                AppFrozenWhiteListHelper.this.mActionWhiteList.add(pkgTmp);
                            }
                        }
                    } while (type != 1);
                } catch (Exception e) {
                    Slog.e(AppFrozenWhiteListHelper.TAG, e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public AppFrozenWhiteListHelper(Context context) {
        super(context, FILTER_NAME, SYS_FILE_DIR, DATA_FILE_DIR);
        setUpdateInfo(new AppFrozenUpdateInfo(), new AppFrozenUpdateInfo());
        try {
            init();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processWhiteFrozenComponent(PackageManagerService pms, String componentName, int userId) {
        ArrayList<String> oppoWhiteFrozenCompList;
        if (typeCasting(pms) != null && componentName != 0) {
            if (DEBUG_SHOW_INFO) {
                Slog.d(TAG, "processWhiteFrozenComponent componentName == " + componentName);
            }
            ArrayList<String> arrayList = this.mProviderWhiteList;
            if (arrayList == null || arrayList.size() == 0) {
                oppoWhiteFrozenCompList = DEFAULT_APP_FROZEN_COMP_WHITE_LIST;
            } else {
                oppoWhiteFrozenCompList = this.mProviderWhiteList;
            }
            Iterator<String> it = oppoWhiteFrozenCompList.iterator();
            while (it.hasNext()) {
                String whiteComp = it.next();
                String[] component = whiteComp.split(",");
                if (whiteComp.length() >= 2 && componentName.equals(component[0]) && 2 == OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).getOppoFreezePackageState(component[1], userId)) {
                    long callingId = Binder.clearCallingIdentity();
                    try {
                        Slog.d(TAG, "processWhiteFrozenComponent unFreezePackage:" + whiteComp);
                        OppoFeatureCache.get(IColorAppQuickFreezeManager.DEFAULT).oppoUnFreezePackageInternal(component[1], userId, 1, 0, PackageManagerService.PLATFORM_PACKAGE_NAME);
                        return;
                    } finally {
                        Binder.restoreCallingIdentity(callingId);
                    }
                }
            }
        }
    }

    public void handleBindOrStartService(Intent service, int userId) {
        if (service != null) {
            ComponentName cpn = service.getComponent();
            if (cpn != null) {
                String cpnPkgName = cpn.getPackageName();
                String cpnClassName = cpn.getClassName();
                if (DEBUG_SHOW_INFO) {
                    Slog.d(TAG, "handleBindService cpn == " + cpn.toString());
                }
                long callingId = Binder.clearCallingIdentity();
                try {
                    OppoPackageManagerInternal packageManagerInternal = (OppoPackageManagerInternal) LocalServices.getService(OppoPackageManagerInternal.class);
                    if (this.mServiceWhiteList.contains(cpnClassName) && cpnPkgName != null) {
                        packageManagerInternal.autoUnfreezePackage(cpnPkgName, userId, "handleBindService component");
                    }
                } finally {
                    Binder.restoreCallingIdentity(callingId);
                }
            } else {
                String action = service.getAction();
                if (action != null) {
                    String pkgName = service.getPackage();
                    if (DEBUG_SHOW_INFO) {
                        Slog.v(TAG, "handleBindService action == " + action + ",pkgName == " + pkgName);
                    }
                    if (pkgName != null) {
                        long callingId2 = Binder.clearCallingIdentity();
                        try {
                            OppoPackageManagerInternal packageManagerInternal2 = (OppoPackageManagerInternal) LocalServices.getService(OppoPackageManagerInternal.class);
                            if (this.mActionWhiteList.contains(action)) {
                                packageManagerInternal2.autoUnfreezePackage(pkgName, userId, "handleBindService action");
                            }
                        } finally {
                            Binder.restoreCallingIdentity(callingId2);
                        }
                    }
                }
            }
        }
    }

    private static OppoBasePackageManagerService typeCasting(PackageManagerService pms) {
        return (OppoBasePackageManagerService) ColorTypeCastingHelper.typeCasting(OppoBasePackageManagerService.class, pms);
    }
}
