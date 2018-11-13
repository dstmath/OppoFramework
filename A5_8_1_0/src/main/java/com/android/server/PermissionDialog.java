package com.android.server;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.TextView;

public class PermissionDialog extends BasePermissionDialog {
    private static final int ALLOWED_REQ = 2;
    private static final int IGNORED_REQ = 4;
    private static final int IGNORED_REQ_TIMEOUT = 8;
    private static final long TIMEOUT_WAIT = 15000;
    private final Context contId;
    private final int inputId;
    private final String inputPackage;
    private final int mDef;
    private final CharSequence[] mOpLabels;
    private final Handler myHandle = new Handler() {
        public void handleMessage(Message mess) {
            int runSet;
            switch (mess.what) {
                case 2:
                    runSet = 0;
                    break;
                case 4:
                    runSet = 1;
                    break;
                default:
                    runSet = 1;
                    break;
            }
            PermissionDialog.this.opsServ.notifyOperation(PermissionDialog.this.mDef, PermissionDialog.this.inputId, PermissionDialog.this.inputPackage, runSet);
            PermissionDialog.this.dismiss();
        }
    };
    private final AppOpsService opsServ;
    private final View viewId;

    public PermissionDialog(Context contextId, AppOpsService opsService, int defInf, int idInfo, String packageName) {
        super(contextId);
        this.opsServ = opsService;
        this.inputPackage = packageName;
        this.contId = contextId;
        this.mDef = defInf;
        Resources rId = contextId.getResources();
        this.inputId = idInfo;
        this.mOpLabels = rId.getTextArray(17235974);
        setCancelable(false);
        setButton(-1, rId.getString(17039477), this.myHandle.obtainMessage(2));
        setButton(-2, rId.getString(17039792), this.myHandle.obtainMessage(4));
        setTitle(" ");
        LayoutParams paraDef = getWindow().getAttributes();
        paraDef.setTitle("PermissionXXX: " + getAppName(this.inputPackage));
        paraDef.privateFlags |= 272;
        getWindow().setAttributes(paraDef);
        this.viewId = getLayoutInflater().inflate(17367200, null);
        TextView textId = (TextView) this.viewId.findViewById(16909147);
        String appName = getAppName(this.inputPackage);
        if (appName == null) {
            appName = this.inputPackage;
        }
        textId.setText(appName + ": " + this.mOpLabels[this.mDef - 70]);
        setView(this.viewId);
        this.myHandle.sendMessageDelayed(this.myHandle.obtainMessage(8), TIMEOUT_WAIT);
    }

    private String getAppName(String inputName) {
        PackageManager packMan = this.contId.getPackageManager();
        try {
            ApplicationInfo runInfo = packMan.getApplicationInfo(inputName, 8704);
            if (runInfo != null) {
                return (String) packMan.getApplicationLabel(runInfo);
            }
            return null;
        } catch (NameNotFoundException e) {
            return null;
        }
    }
}
