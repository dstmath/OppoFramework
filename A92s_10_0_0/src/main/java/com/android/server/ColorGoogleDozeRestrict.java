package com.android.server;

import android.app.ActivityManagerInternal;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Handler;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Slog;
import android.util.SparseBooleanArray;
import com.android.server.am.ColorAppCrashClearManager;
import java.io.IOException;
import org.xmlpull.v1.XmlSerializer;

public class ColorGoogleDozeRestrict implements IColorGoogleDozeRestrict, IColorGoogleRestrictCallback {
    private static final String TAG = "ColorGoogleDozeRestrict";
    ColorGoogleRestrictionHelper mColorGoogleRestrictionHelper = null;
    private DeviceIdleController mController = null;
    private Handler mHandler = null;
    private IColorGoogleDozeRestrictInner mInner;
    private ArrayMap<String, Integer> sRestrictedExceptIdleMap = new ArrayMap<>();
    private ArrayMap<String, Integer> sRestrictedIdleSystemMap = new ArrayMap<>();
    private ArrayMap<String, Integer> sRestrictedIdleUserMap = new ArrayMap<>();

    public void initArgs(Context context, Handler handler, DeviceIdleController controller, IColorGoogleDozeRestrictInner inner) {
        this.mColorGoogleRestrictionHelper = ColorGoogleRestrictionHelper.getInstance(context);
        this.mColorGoogleRestrictionHelper.addCallback(this);
        this.mHandler = handler;
        this.mController = controller;
        this.mInner = inner;
    }

    public void updateWhitelistApps(ArrayMap<String, Integer> whitelistApps, boolean isSystem, boolean isExceptIdle) {
        updateRestrictedList(whitelistApps, isExceptIdle ? this.sRestrictedExceptIdleMap : isSystem ? this.sRestrictedIdleSystemMap : this.sRestrictedIdleUserMap);
    }

    public void reportWhitelistForAms(ActivityManagerInternal localAms, SparseBooleanArray allAppIds, SparseBooleanArray allExceptIdleAppIds) {
        localAms.setDeviceIdleWhitelist(buildAppIdArrayForOppo(this.sRestrictedIdleSystemMap, this.sRestrictedIdleUserMap, allAppIds.clone()), buildAppIdArrayForOppo(this.sRestrictedExceptIdleMap, null, allExceptIdleAppIds.clone()));
    }

    public void restoreConfigFile(ArrayMap<String, Integer> whitelistApps, XmlSerializer out) throws IOException {
        for (int i = 0; i < this.sRestrictedIdleUserMap.size(); i++) {
            if (!whitelistApps.containsKey(this.sRestrictedIdleUserMap.keyAt(i))) {
                out.startTag(null, "wl");
                out.attribute(null, ColorAppCrashClearManager.CRASH_COUNT, this.sRestrictedIdleUserMap.keyAt(i));
                out.endTag(null, "wl");
            }
        }
    }

    public boolean interceptWhitelistOperation(ApplicationInfo ai, String name, boolean isSystem, boolean isExceptIdle, boolean add) {
        if (!matchGoogleRestrictRule(name)) {
            return false;
        }
        Slog.d(TAG, "add power save whitelist app : " + name + ", but it's in restrict map");
        ArrayMap<String, Integer> list = isExceptIdle ? this.sRestrictedExceptIdleMap : isSystem ? this.sRestrictedIdleSystemMap : this.sRestrictedIdleUserMap;
        if (add) {
            if (ai == null) {
                return false;
            }
            list.put(name, Integer.valueOf(UserHandle.getAppId(ai.uid)));
            Slog.d(TAG, "add power save whitelist app : " + name + ", but it's in restrict map");
            return true;
        } else if (list.remove(name) == null) {
            return false;
        } else {
            Slog.d(TAG, "remove power save whitelist app : " + name + ", but it's in restrict map");
            return true;
        }
    }

    public void interceptWhitelistReset(boolean isExceptIdle, ArraySet<String> list) {
        this.sRestrictedExceptIdleMap.removeAll(list);
    }

    public void restrictChange() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.ColorGoogleDozeRestrict.AnonymousClass1 */

            public void run() {
                ColorGoogleDozeRestrict.this.oppoUpdateWhitelist();
            }
        });
    }

    public void restrictListChange() {
        this.mHandler.post(new Runnable() {
            /* class com.android.server.ColorGoogleDozeRestrict.AnonymousClass2 */

            public void run() {
                ColorGoogleDozeRestrict.this.oppoUpdateWhitelist();
            }
        });
    }

    private ArrayMap<String, Integer> updateRestrictedList(ArrayMap<String, Integer> inputApps, ArrayMap<String, Integer> outputApps) {
        for (int i = 0; i < outputApps.size(); i++) {
            inputApps.put(outputApps.keyAt(i), outputApps.valueAt(i));
        }
        outputApps.clear();
        for (int i2 = inputApps.size() - 1; i2 >= 0; i2--) {
            if (matchGoogleRestrictRule(inputApps.keyAt(i2))) {
                outputApps.put(inputApps.keyAt(i2), inputApps.valueAt(i2));
                inputApps.removeAt(i2);
            }
        }
        return outputApps;
    }

    private static int[] buildAppIdArrayForOppo(ArrayMap<String, Integer> systemApps, ArrayMap<String, Integer> userApps, SparseBooleanArray outAppIds) {
        if (systemApps != null) {
            for (int i = 0; i < systemApps.size(); i++) {
                outAppIds.put(systemApps.valueAt(i).intValue(), true);
            }
        }
        if (userApps != null) {
            for (int i2 = 0; i2 < userApps.size(); i2++) {
                outAppIds.put(userApps.valueAt(i2).intValue(), true);
            }
        }
        int size = outAppIds.size();
        int[] appids = new int[size];
        for (int i3 = 0; i3 < size; i3++) {
            appids[i3] = outAppIds.keyAt(i3);
        }
        return appids;
    }

    private boolean matchGoogleRestrictRule(String name) {
        return this.mColorGoogleRestrictionHelper.isGoogleRestrct() && this.mColorGoogleRestrictionHelper.getGoogleRestrictList().contains(name);
    }

    public void oppoUpdateWhitelist() {
        synchronized (this.mController) {
            if (this.mInner != null) {
                this.mInner.reportPowerSaveWhitelistChangedLocked();
                this.mInner.updateWhitelistAppIdsLocked();
            }
        }
    }
}
