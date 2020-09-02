package cm.android.mdm.util.defaultapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.util.Log;
import cm.android.mdm.util.defaultapp.entity.DefaultAppActivityInfo;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultApp {
    protected static final String TAG = "DefaultApp";
    private List<DefaultAppActivityInfo> mAppActivityInfoList;
    protected Context mContext;

    /* access modifiers changed from: protected */
    public abstract String getDefaultPackage(PackageManager packageManager);

    /* access modifiers changed from: protected */
    public abstract List<IntentFilter> getFilterList();

    /* access modifiers changed from: protected */
    public abstract List<Intent> getIntentList();

    /* access modifiers changed from: protected */
    public abstract List<Integer> getMatchList();

    public DefaultApp(Context context) {
        this.mContext = context;
    }

    public List<DefaultAppActivityInfo> getAppInfoList() {
        List<DefaultAppActivityInfo> list = this.mAppActivityInfoList;
        if (list != null) {
            return list;
        }
        this.mAppActivityInfoList = new ArrayList();
        PackageManager pm = this.mContext.getPackageManager();
        for (Intent intent : getIntentList()) {
            DefaultAppActivityInfo appActivityInfo = new DefaultAppActivityInfo();
            for (ResolveInfo ri : getResolveInfoList(intent, pm)) {
                appActivityInfo.addActivityInfo(ri.activityInfo);
                appActivityInfo.addPriority(ri.priority);
            }
            this.mAppActivityInfoList.add(appActivityInfo);
        }
        return this.mAppActivityInfoList;
    }

    /* access modifiers changed from: protected */
    public List<ResolveInfo> getResolveInfoList(Intent intent, PackageManager pm) {
        try {
            return pm.queryIntentActivities(intent, 65536);
        } catch (Exception e) {
            List<ResolveInfo> resolveInfoList = new ArrayList<>();
            e.printStackTrace();
            return resolveInfoList;
        }
    }

    /* JADX INFO: Multiple debug info for r5v4 java.lang.String: [D('activityInfoList' java.util.List<android.content.pm.ActivityInfo>), D('packageName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r12v7 java.lang.String: [D('className' java.lang.String), D('matchList' java.util.List<java.lang.Integer>)] */
    public boolean setDefaultApp(String targetPackage) {
        List<Integer> matchList;
        boolean changed;
        boolean hasCleared;
        ComponentName[] componentNames;
        PackageManager pm = this.mContext.getPackageManager();
        String currentDefault = getDefaultPackage(pm);
        Log.d(TAG, "setDefaultApp, currentDefault=" + currentDefault + ", targetPackage=" + targetPackage);
        if (currentDefault.equals(targetPackage)) {
            return true;
        }
        List<IntentFilter> filterList = getFilterList();
        List<Integer> matchList2 = getMatchList();
        List<DefaultAppActivityInfo> appInfoList = getAppInfoList();
        boolean hasCleared2 = false;
        boolean changed2 = false;
        int i = 0;
        while (i < filterList.size()) {
            DefaultAppActivityInfo appActivityInfo = appInfoList.get(i);
            List<ActivityInfo> activityInfoList = appActivityInfo.getActivityInfo();
            List<Integer> priorityList = appActivityInfo.getPriorityList();
            ComponentName[] componentNames2 = new ComponentName[activityInfoList.size()];
            ComponentName bestActivity = null;
            int j = 0;
            int maxPrioity = Integer.MIN_VALUE;
            while (j < activityInfoList.size()) {
                ActivityInfo activityInfo = activityInfoList.get(j);
                String packageName = activityInfo.packageName;
                String className = activityInfo.name;
                ComponentName componentName = new ComponentName(packageName, className);
                componentNames2[j] = componentName;
                if (targetPackage.equals(packageName)) {
                    componentNames = componentNames2;
                    if (priorityList.get(j).intValue() > maxPrioity) {
                        bestActivity = componentName;
                        maxPrioity = priorityList.get(j).intValue();
                    }
                } else {
                    componentNames = componentNames2;
                }
                Log.d(TAG, "setDefaultApp, index=" + j + ",packageName=" + packageName + ", className=" + className + ", priority=" + priorityList.get(j));
                j++;
                changed2 = changed2;
                activityInfoList = activityInfoList;
                matchList2 = matchList2;
                bestActivity = bestActivity;
                componentNames2 = componentNames;
            }
            Log.d(TAG, "setDefaultApp, bestActivity=" + bestActivity + ", maxPrioity=" + maxPrioity);
            if (bestActivity != null) {
                if (!hasCleared2) {
                    clearPreferredActivity(pm, currentDefault);
                    hasCleared = true;
                } else {
                    hasCleared = hasCleared2;
                }
                matchList = matchList2;
                setPreferredActivity(pm, filterList.get(i), matchList.get(i).intValue(), componentNames2, bestActivity);
                changed = true;
                hasCleared2 = hasCleared;
            } else {
                matchList = matchList2;
                changed = changed2;
            }
            i++;
            matchList2 = matchList;
            appInfoList = appInfoList;
            changed2 = changed;
        }
        return changed2;
    }

    /* JADX INFO: Multiple debug info for r5v3 java.lang.String: [D('activityInfoList' java.util.List<android.content.pm.ActivityInfo>), D('packageName' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r1v11 java.lang.String: [D('className' java.lang.String), D('changed' boolean)] */
    public boolean setDefaultApp(ComponentName home) {
        boolean hasCleared;
        ComponentName componentName = home;
        if (componentName == null) {
            return false;
        }
        String targetPackage = home.getPackageName();
        PackageManager pm = this.mContext.getPackageManager();
        String currentDefault = getDefaultPackage(pm);
        Log.d(TAG, "setDefaultApp, currentDefault=" + currentDefault + ", home=" + componentName);
        List<IntentFilter> filterList = getFilterList();
        List<Integer> matchList = getMatchList();
        List<DefaultAppActivityInfo> appInfoList = getAppInfoList();
        boolean hasCleared2 = false;
        boolean changed = false;
        int i = 0;
        while (i < filterList.size()) {
            DefaultAppActivityInfo appActivityInfo = appInfoList.get(i);
            List<ActivityInfo> activityInfoList = appActivityInfo.getActivityInfo();
            List<Integer> priorityList = appActivityInfo.getPriorityList();
            ComponentName[] componentNames = new ComponentName[activityInfoList.size()];
            boolean ComponentNameMatched = false;
            int j = 0;
            while (j < activityInfoList.size()) {
                ActivityInfo activityInfo = activityInfoList.get(j);
                String packageName = activityInfo.packageName;
                String className = activityInfo.name;
                ComponentName componentName2 = new ComponentName(packageName, className);
                componentNames[j] = componentName2;
                if (componentName.equals(componentName2)) {
                    ComponentNameMatched = true;
                }
                Log.d(TAG, "setDefaultApp, index=" + j + ",packageName=" + packageName + ", className=" + className + ", priority=" + priorityList.get(j));
                j++;
                componentName = home;
                appInfoList = appInfoList;
                activityInfoList = activityInfoList;
                changed = changed;
                componentNames = componentNames;
            }
            Log.d(TAG, "setDefaultApp, ComponentNameMatched=" + ComponentNameMatched);
            if (ComponentNameMatched) {
                if (!hasCleared2) {
                    clearPreferredActivity(pm, currentDefault);
                    hasCleared = true;
                } else {
                    hasCleared = hasCleared2;
                }
                setPreferredActivity(pm, filterList.get(i), matchList.get(i).intValue(), componentNames, home);
                changed = true;
                hasCleared2 = hasCleared;
            } else {
                changed = changed;
            }
            i++;
            componentName = home;
            targetPackage = targetPackage;
            appInfoList = appInfoList;
        }
        return changed;
    }

    /* access modifiers changed from: protected */
    public void setPreferredActivity(PackageManager pm, IntentFilter filter, int match, ComponentName[] componentNames, ComponentName activity) {
        pm.addPreferredActivity(filter, match, componentNames, activity);
    }

    /* access modifiers changed from: protected */
    public void clearPreferredActivity(PackageManager pm, String currentDefault) {
        pm.clearPackagePreferredActivities(currentDefault);
        Log.d(TAG, "clearPreferredActivity, pkg=" + currentDefault);
    }
}
