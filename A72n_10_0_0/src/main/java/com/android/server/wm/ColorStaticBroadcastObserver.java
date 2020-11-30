package com.android.server.wm;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.os.UserHandle;
import android.util.Log;
import com.color.app.ColorAppEnterInfo;
import com.color.app.ColorAppExitInfo;
import com.color.app.IColorAppSwitchObserver;

public class ColorStaticBroadcastObserver extends IColorAppSwitchObserver.Stub {
    private static final String TAG = "StaticBroadcastObserver";
    private Context context;
    private String pkgName;

    public ColorStaticBroadcastObserver(String pkgName2, Context context2) {
        this.pkgName = pkgName2;
        this.context = context2;
    }

    public void onActivityEnter(ColorAppEnterInfo info) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Log.d(TAG, "onActivityEnter info = " + info);
        }
        Intent intent = new Intent("oppo.intent.action.APP_SWITCH");
        intent.putExtra("extra_notify_type", 3);
        intent.putExtra("extyra_switch_info", (Parcelable) info);
        intent.setPackage(this.pkgName);
        this.context.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    public void onActivityExit(ColorAppExitInfo info) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Log.d(TAG, "onActivityExit info = " + info);
        }
        Intent intent = new Intent("oppo.intent.action.APP_SWITCH");
        intent.putExtra("extra_notify_type", 4);
        intent.putExtra("extyra_switch_info", (Parcelable) info);
        intent.setPackage(this.pkgName);
        this.context.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    public void onAppEnter(ColorAppEnterInfo info) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Log.d(TAG, "onAppEnter info = " + info);
        }
        Intent intent = new Intent("oppo.intent.action.APP_SWITCH");
        intent.putExtra("extra_notify_type", 1);
        intent.putExtra("extyra_switch_info", (Parcelable) info);
        intent.setPackage(this.pkgName);
        this.context.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
    }

    public void onAppExit(ColorAppExitInfo info) {
        if (ActivityTaskManagerDebugConfig.DEBUG_TASKS) {
            Log.d(TAG, "onAppExit info = " + info);
        }
        Intent intent = new Intent("oppo.intent.action.APP_SWITCH");
        intent.putExtra("extra_notify_type", 2);
        intent.putExtra("extyra_switch_info", (Parcelable) info);
        intent.setPackage(this.pkgName);
        this.context.sendBroadcastAsUser(intent, UserHandle.ALL, "oppo.permission.OPPO_COMPONENT_SAFE");
    }
}
