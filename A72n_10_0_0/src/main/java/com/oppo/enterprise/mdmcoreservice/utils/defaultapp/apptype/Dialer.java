package com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.telecom.TelecomManager;
import com.oppo.enterprise.mdmcoreservice.utils.AppTypeUtil;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp;
import java.util.ArrayList;
import java.util.List;

public class Dialer extends DefaultApp {
    public Dialer(Context context) {
        super(context);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getAppTypeKey() {
        return AppTypeUtil.KEY_DEFAULT_APP_DIAL;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Intent> getIntentList() {
        List<Intent> intentList = new ArrayList<>();
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.addCategory("android.intent.category.BROWSABLE");
        intent.setType("vnd.android.cursor.dir/calls");
        intentList.add(intent);
        Intent intent2 = new Intent("android.intent.action.DIAL");
        intent2.addCategory("android.intent.category.DEFAULT");
        intent2.addCategory("android.intent.category.BROWSABLE");
        intent2.setDataAndType(Uri.parse("voicemail://"), null);
        intentList.add(intent2);
        Intent intent3 = new Intent("android.intent.action.DIAL");
        intent3.addCategory("android.intent.category.DEFAULT");
        intent3.setDataAndType(Uri.parse("tel://"), null);
        intentList.add(intent3);
        Intent intent4 = new Intent("android.intent.action.DIAL");
        intent4.addCategory("android.intent.category.DEFAULT");
        intentList.add(intent4);
        Intent intent5 = new Intent("android.intent.action.VIEW");
        intent5.addCategory("android.intent.category.DEFAULT");
        intent5.addCategory("android.intent.category.BROWSABLE");
        intent5.setDataAndType(Uri.parse("tel://"), null);
        intentList.add(intent5);
        Intent intent6 = new Intent("android.intent.action.VIEW");
        intent6.addCategory("android.intent.category.DEFAULT");
        intent6.setDataAndType(Uri.parse("tel://"), null);
        intentList.add(intent6);
        return intentList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<IntentFilter> getFilterList() {
        List<IntentFilter> filterList = new ArrayList<>();
        IntentFilter filter = new IntentFilter("android.intent.action.VIEW");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addCategory("android.intent.category.BROWSABLE");
        try {
            filter.addDataType("vnd.android.cursor.dir/calls");
        } catch (Exception e) {
            e.printStackTrace();
        }
        filterList.add(filter);
        IntentFilter filter2 = new IntentFilter("android.intent.action.DIAL");
        filter2.addCategory("android.intent.category.DEFAULT");
        filter2.addCategory("android.intent.category.BROWSABLE");
        filter2.addDataScheme("voicemail");
        filterList.add(filter2);
        IntentFilter filter3 = new IntentFilter("android.intent.action.DIAL");
        filter3.addCategory("android.intent.category.DEFAULT");
        filter3.addDataScheme("tel");
        filterList.add(filter3);
        IntentFilter filter4 = new IntentFilter("android.intent.action.DIAL");
        filter4.addCategory("android.intent.category.DEFAULT");
        filterList.add(filter4);
        IntentFilter filter5 = new IntentFilter("android.intent.action.VIEW");
        filter5.addCategory("android.intent.category.DEFAULT");
        filter5.addCategory("android.intent.category.BROWSABLE");
        filter5.addDataScheme("tel");
        filterList.add(filter5);
        IntentFilter filter6 = new IntentFilter("android.intent.action.VIEW");
        filter6.addCategory("android.intent.category.DEFAULT");
        filter6.addDataScheme("tel");
        filterList.add(filter6);
        return filterList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Integer> getMatchList() {
        List<Integer> matchList = new ArrayList<>();
        matchList.add(6291456);
        matchList.add(2097152);
        matchList.add(2097152);
        matchList.add(1048576);
        matchList.add(2097152);
        matchList.add(2097152);
        return matchList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getDefaultPackage(PackageManager pm) {
        String dialerPkg = "com.android.contacts";
        Intent intent = new Intent("android.intent.action.DIAL");
        intent.addCategory("android.intent.category.DEFAULT");
        ResolveInfo info = pm.resolveActivity(intent, 65536);
        if (!(info == null || info.activityInfo == null)) {
            dialerPkg = info.activityInfo.packageName;
        }
        if (dialerPkg == null) {
            return "com.android.contacts";
        }
        return dialerPkg;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public boolean setDefaultApp(String targetPackage) {
        super.setDefaultApp(targetPackage);
        ((TelecomManager) this.mContext.getSystemService("telecom")).setDefaultDialer(targetPackage);
        return true;
    }
}
