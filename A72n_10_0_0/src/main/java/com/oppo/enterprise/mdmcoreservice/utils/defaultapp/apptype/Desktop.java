package com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.oppo.enterprise.mdmcoreservice.utils.AppTypeUtil;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp;
import java.util.ArrayList;
import java.util.List;

public class Desktop extends DefaultApp {
    public Desktop(Context context) {
        super(context);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getAppTypeKey() {
        return AppTypeUtil.KEY_DEFAULT_APP_DESKTOP;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Intent> getIntentList() {
        List<Intent> intentList = new ArrayList<>();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.addCategory("android.intent.category.DEFAULT");
        intentList.add(intent);
        return intentList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<IntentFilter> getFilterList() {
        List<IntentFilter> filterList = new ArrayList<>();
        IntentFilter filter = new IntentFilter("android.intent.action.MAIN");
        filter.addCategory("android.intent.category.HOME");
        filter.addCategory("android.intent.category.DEFAULT");
        filterList.add(filter);
        return filterList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Integer> getMatchList() {
        List<Integer> matchList = new ArrayList<>();
        matchList.add(1048576);
        return matchList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public void setPreferredActivity(PackageManager pm, IntentFilter filter, int match, ComponentName[] componentNames, ComponentName activity) {
        pm.replacePreferredActivity(filter, 1048576, componentNames, activity);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getDefaultPackage(PackageManager pm) {
        String homePkg = "com.oppo.launcher";
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        ResolveInfo info = pm.resolveActivity(intent, 65536);
        if (!(info == null || info.activityInfo == null)) {
            homePkg = info.activityInfo.packageName;
        }
        if (homePkg == null) {
            return "com.oppo.launcher";
        }
        return homePkg;
    }
}
