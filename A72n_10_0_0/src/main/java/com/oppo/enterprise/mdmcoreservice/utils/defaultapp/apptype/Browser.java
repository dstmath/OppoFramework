package com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.Log;
import com.oppo.enterprise.mdmcoreservice.utils.AppTypeUtil;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Browser extends DefaultApp {
    private static final List<Integer> DEFAULT_MATCH_TYPE_LIST = Collections.unmodifiableList(Arrays.asList(2097152));
    private static final List<String> DEFAULT_SCHEME_LIST = Collections.unmodifiableList(Arrays.asList("http", "https"));
    private static final List<String> DEFAULT_URI_DATA_TYPE_LIST = Collections.unmodifiableList(Arrays.asList("dn1.dn2.dn3", "dn1.dn2.dn3/path"));
    private static final Set<String> OPPO_BROWSER_PACKAGES = new ArraySet();
    private static final String REALME_BROWSER_PACKAGE_NAME = "com.nearme.browser";
    private static final String SYSTEM_DEFAULT_APP_BROWSER_NEW = "com.coloros.browser";
    private static final String SYSTEM_DEFAULT_APP_BROWSER_NEW_UNIQUE = "com.heytap.browser";

    static {
        OPPO_BROWSER_PACKAGES.add(SYSTEM_DEFAULT_APP_BROWSER_NEW);
        OPPO_BROWSER_PACKAGES.add(REALME_BROWSER_PACKAGE_NAME);
        OPPO_BROWSER_PACKAGES.add(SYSTEM_DEFAULT_APP_BROWSER_NEW_UNIQUE);
    }

    public Browser(Context context) {
        super(context);
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Intent> getIntentList() {
        List<Intent> intentList = new ArrayList<>();
        for (int i = 0; i < DEFAULT_MATCH_TYPE_LIST.size(); i++) {
            for (int j = 0; j < DEFAULT_SCHEME_LIST.size(); j++) {
                for (int k = 0; k < DEFAULT_URI_DATA_TYPE_LIST.size(); k++) {
                    Intent intent = new Intent("android.intent.action.VIEW");
                    intent.addCategory("android.intent.category.DEFAULT");
                    StringBuffer sb = new StringBuffer();
                    sb.append(DEFAULT_SCHEME_LIST.get(j));
                    sb.append("://");
                    sb.append(DEFAULT_URI_DATA_TYPE_LIST.get(k));
                    intent.setDataAndType(Uri.parse(sb.toString()), null);
                    intentList.add(intent);
                }
            }
        }
        Intent intent2 = new Intent("android.intent.action.VIEW");
        intent2.addCategory("android.intent.category.DEFAULT");
        intent2.setDataAndType(Uri.parse("http://dn1.dn2.dn3"), null);
        intentList.add(intent2);
        Intent intent3 = new Intent("android.intent.action.VIEW");
        intent3.addCategory("android.intent.category.DEFAULT");
        intent3.setDataAndType(Uri.parse("http://dn1.dn2.dn3/..."), null);
        intentList.add(intent3);
        Intent intent4 = new Intent("android.intent.action.VIEW");
        intent4.addCategory("android.intent.category.DEFAULT");
        intent4.setDataAndType(Uri.parse("https://dn1.dn2.dn3/..."), null);
        intentList.add(intent4);
        return intentList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<IntentFilter> getFilterList() {
        List<IntentFilter> filterList = new ArrayList<>();
        for (int i = 0; i < DEFAULT_MATCH_TYPE_LIST.size(); i++) {
            for (int j = 0; j < DEFAULT_SCHEME_LIST.size(); j++) {
                for (int k = 0; k < DEFAULT_URI_DATA_TYPE_LIST.size(); k++) {
                    IntentFilter filter = new IntentFilter("android.intent.action.VIEW");
                    filter.addCategory("android.intent.category.DEFAULT");
                    filter.addDataScheme(DEFAULT_SCHEME_LIST.get(j));
                    filterList.add(filter);
                }
            }
        }
        IntentFilter filter2 = new IntentFilter("android.intent.action.VIEW");
        filter2.addCategory("android.intent.category.DEFAULT");
        filter2.addDataScheme("http");
        filterList.add(filter2);
        IntentFilter filter3 = new IntentFilter("android.intent.action.VIEW");
        filter3.addCategory("android.intent.category.DEFAULT");
        filter3.addDataScheme("http");
        filterList.add(filter3);
        IntentFilter filter4 = new IntentFilter("android.intent.action.VIEW");
        filter4.addCategory("android.intent.category.DEFAULT");
        filter4.addDataScheme("https");
        filterList.add(filter4);
        return filterList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Integer> getMatchList() {
        List<Integer> matchList = new ArrayList<>();
        for (int i = 0; i < DEFAULT_MATCH_TYPE_LIST.size(); i++) {
            for (int j = 0; j < DEFAULT_SCHEME_LIST.size(); j++) {
                for (int k = 0; k < DEFAULT_URI_DATA_TYPE_LIST.size(); k++) {
                    matchList.add(DEFAULT_MATCH_TYPE_LIST.get(i));
                }
            }
        }
        matchList.add(5242880);
        matchList.add(3145728);
        matchList.add(3145728);
        return matchList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getDefaultPackage(PackageManager pm) {
        String browserPkg = getCurrentSystemBrowserName(this.mContext);
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("http://dn1.dn2.dn3"), null);
        ResolveInfo info = pm.resolveActivity(intent, 65536);
        if (!(info == null || info.activityInfo == null)) {
            browserPkg = info.activityInfo.packageName;
        }
        if (browserPkg == null) {
            return getCurrentSystemBrowserName(this.mContext);
        }
        return browserPkg;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getAppTypeKey() {
        return AppTypeUtil.KEY_DEFAULT_APP_BROWSER;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<ResolveInfo> getResolveInfoList(Intent intent, PackageManager pm) {
        try {
            return pm.queryIntentActivities(intent, 131072);
        } catch (Exception e) {
            List<ResolveInfo> resolveInfoList = new ArrayList<>();
            e.printStackTrace();
            return resolveInfoList;
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public void setPreferredActivity(PackageManager pm, IntentFilter filter, int match, ComponentName[] componentNames, ComponentName activity) {
        super.setPreferredActivity(pm, filter, match, componentNames, activity);
        if (Build.VERSION.SDK_INT >= 23) {
            setDefaultBrowserPackageName(pm, activity.getPackageName(), UserHandle.myUserId());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public void clearPreferredActivity(PackageManager pm, String currentDefault) {
        super.clearPreferredActivity(pm, currentDefault);
        if (Build.VERSION.SDK_INT >= 23) {
            setDefaultBrowserPackageName(pm, null, UserHandle.myUserId());
        }
    }

    private void setDefaultBrowserPackageName(PackageManager pm, String pkgName, int userId) {
        try {
            Class clazz = pm.getClass();
            if (Build.VERSION.SDK_INT == 23) {
                clazz.getDeclaredMethod("setDefaultBrowserPackageName", String.class, Integer.TYPE).invoke(pm, pkgName, Integer.valueOf(userId));
            } else if (Build.VERSION.SDK_INT >= 24) {
                clazz.getDeclaredMethod("setDefaultBrowserPackageNameAsUser", String.class, Integer.TYPE).invoke(pm, pkgName, Integer.valueOf(userId));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCurrentSystemBrowserName(Context context) {
        for (String browserName : OPPO_BROWSER_PACKAGES) {
            if (isPackageInstalled(context, browserName)) {
                return browserName;
            }
        }
        return null;
    }

    private static boolean isPackageInstalled(Context context, String packageName) {
        PackageInfo packageInfo;
        if (context == null) {
            return false;
        }
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            Log.d("DefaultApp", " isPackageInstalled not found");
            packageInfo = null;
        }
        if (packageInfo != null) {
            return true;
        }
        return false;
    }
}
