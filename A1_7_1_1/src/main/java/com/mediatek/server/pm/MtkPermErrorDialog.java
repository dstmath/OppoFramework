package com.mediatek.server.pm;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import com.android.server.am.ActivityManagerService;
import com.android.server.am.AppErrorDialog.Data;
import com.android.server.am.MtkAppErrorDialog;
import com.android.server.oppo.IElsaManager;

public class MtkPermErrorDialog extends MtkAppErrorDialog implements OnClickListener {
    private final Context mContext;
    private String mPermission;
    private String mPkgName;
    private String mProcessName;

    public MtkPermErrorDialog(Context context, ActivityManagerService service, Data data, String permission, String processName, String pkgName) {
        super(context, service, data);
        this.mContext = context;
        this.mPermission = permission;
        this.mProcessName = processName;
        this.mPkgName = pkgName;
        setupUiComponents();
    }

    private void setupUiComponents() {
        String message;
        Resources res = this.mContext.getResources();
        setTitle(null);
        CharSequence permissionName = IElsaManager.EMPTY_PACKAGE;
        CharSequence applicationName = IElsaManager.EMPTY_PACKAGE;
        try {
            permissionName = this.mContext.getPackageManager().getPermissionInfo(this.mPermission, 0).loadLabel(this.mContext.getPackageManager());
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        try {
            applicationName = this.mContext.getPackageManager().getApplicationLabel(this.mContext.getPackageManager().getApplicationInfo(this.mPkgName, 0));
        } catch (NameNotFoundException e2) {
            applicationName = this.mProcessName;
        }
        final boolean isWlanPerm = "com.mediatek.permission.CTA_ENABLE_WIFI".equals(this.mPermission);
        final boolean isBtPerm = "com.mediatek.permission.CTA_ENABLE_BT".equals(this.mPermission);
        if (TextUtils.isEmpty(permissionName)) {
            message = res.getString(134545642, new Object[]{applicationName});
        } else if (isWlanPerm || isBtPerm) {
            message = res.getString(134545641, new Object[]{applicationName, permissionName});
        } else {
            message = res.getString(134545640, new Object[]{applicationName, permissionName});
        }
        setMessage(message);
        setButton(-1, res.getText(134545649), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String actionName;
                if (isWlanPerm) {
                    actionName = "android.settings.WIFI_SETTINGS";
                } else if (isBtPerm) {
                    actionName = "android.settings.BLUETOOTH_SETTINGS";
                } else {
                    actionName = "android.intent.action.MANAGE_APP_DETAILED_PERMISSIONS";
                }
                Intent intent = new Intent(actionName);
                intent.putExtra("android.intent.extra.PACKAGE_NAME", MtkPermErrorDialog.this.mPkgName);
                intent.putExtra("android.intent.extra.PERMISSION_NAME", MtkPermErrorDialog.this.mPermission);
                intent.setFlags(268435456);
                MtkPermErrorDialog.this.mContext.startActivityAsUser(intent, new UserHandle(-2));
                MtkPermErrorDialog.this.clickButtonForResult(1);
            }
        });
        setButton(-3, res.getText(17039360), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                MtkPermErrorDialog.this.clickButtonForResult(1);
            }
        });
    }

    public void initLayout(FrameLayout frame) {
    }
}
