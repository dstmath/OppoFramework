package com.oppo.enterprise.mdmcoreservice.utils.defaultapp.apptype;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import com.oppo.enterprise.mdmcoreservice.utils.AppTypeUtil;
import com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp;
import java.util.ArrayList;
import java.util.List;

public class Message extends DefaultApp {
    public Message(Context context) {
        super(context);
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getAppTypeKey() {
        return AppTypeUtil.KEY_DEFAULT_APP_MESSAGE;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Intent> getIntentList() {
        List<Intent> intentList = new ArrayList<>();
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("smsto://"), null);
        intentList.add(intent);
        Intent intent2 = new Intent("android.intent.action.SENDTO");
        intent2.addCategory("android.intent.category.DEFAULT");
        intent2.setDataAndType(Uri.parse("mmsto://"), null);
        intentList.add(intent2);
        Intent intent3 = new Intent("android.intent.action.SENDTO");
        intent3.addCategory("android.intent.category.DEFAULT");
        intent3.setDataAndType(Uri.parse("sms://"), null);
        intentList.add(intent3);
        Intent intent4 = new Intent("android.intent.action.SENDTO");
        intent4.addCategory("android.intent.category.DEFAULT");
        intent4.setDataAndType(Uri.parse("mms://"), null);
        intentList.add(intent4);
        return intentList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<IntentFilter> getFilterList() {
        List<IntentFilter> filterList = new ArrayList<>();
        IntentFilter filter = new IntentFilter("android.intent.action.SENDTO");
        filter.addCategory("android.intent.category.DEFAULT");
        filter.addDataScheme("smsto");
        filterList.add(filter);
        IntentFilter filter2 = new IntentFilter("android.intent.action.SENDTO");
        filter2.addCategory("android.intent.category.DEFAULT");
        filter2.addDataScheme("mmsto");
        filterList.add(filter2);
        IntentFilter filter3 = new IntentFilter("android.intent.action.SENDTO");
        filter3.addCategory("android.intent.category.DEFAULT");
        filter3.addDataScheme("sms");
        filterList.add(filter3);
        IntentFilter filter4 = new IntentFilter("android.intent.action.SENDTO");
        filter4.addCategory("android.intent.category.DEFAULT");
        filter4.addDataScheme("mms");
        filterList.add(filter4);
        return filterList;
    }

    /* access modifiers changed from: protected */
    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public List<Integer> getMatchList() {
        List<Integer> matchList = new ArrayList<>();
        matchList.add(2097152);
        matchList.add(2097152);
        matchList.add(2097152);
        matchList.add(2097152);
        return matchList;
    }

    @Override // com.oppo.enterprise.mdmcoreservice.utils.defaultapp.DefaultApp
    public String getDefaultPackage(PackageManager pm) {
        String messagePkg = "com.android.mms";
        Intent intent = new Intent("android.intent.action.SENDTO");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.parse("smsto://"), null);
        ResolveInfo info = pm.resolveActivity(intent, 65536);
        if (!(info == null || info.activityInfo == null)) {
            messagePkg = info.activityInfo.packageName;
        }
        if (messagePkg == null) {
            return "com.android.mms";
        }
        return messagePkg;
    }
}
