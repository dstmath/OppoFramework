package com.android.server.wm;

import android.app.AlertDialog;
import android.app.AppGlobals;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Slog;
import android.util.Xml;
import com.android.server.UiThread;
import com.android.server.am.BaseErrorDialog;
import com.android.server.oppo.OppoArmyService;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class OppoArmyController {
    private static final String CUSTOMIZE_LIST_PATH = "/system/etc/oppo_customize_whitelist.xml";
    private static final boolean DBG_CUSTOMIZE = false;
    private static final int DISMISS_DIALOG_UI_MSG = 1;
    private static final String PROTECT_APP_LIST_PATH = "/data/system/custom_protect_app.xml";
    private static final int SHOW_NOT_ALLOW_DIALOG_MSG = 0;
    private static final String TAG = "OppoArmyController";
    /* access modifiers changed from: private */
    public List<String> mCustomizeList = new ArrayList();
    /* access modifiers changed from: private */
    public AlertDialog mDialogForDisallow = null;
    /* access modifiers changed from: private */
    public OppoArmyService mOppoArmyService;
    /* access modifiers changed from: private */
    public List<String> mProtectAppList = new ArrayList();
    /* access modifiers changed from: private */
    public Context mUiContext;
    private UiHandler mUiHandler;

    final class UiHandler extends Handler {
        public UiHandler() {
            super(UiThread.get().getLooper(), null, true);
        }

        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                if (OppoArmyController.this.mDialogForDisallow == null) {
                    OppoArmyController oppoArmyController = OppoArmyController.this;
                    AlertDialog unused = oppoArmyController.mDialogForDisallow = new BaseErrorDialog(oppoArmyController.mUiContext);
                    OppoArmyController.this.mDialogForDisallow.setCancelable(false);
                    OppoArmyController.this.mDialogForDisallow.setMessage(OppoArmyController.this.mUiContext.getText(201653548));
                    OppoArmyController.this.mDialogForDisallow.setButton(-1, OppoArmyController.this.mUiContext.getText(17039370), obtainMessage(1, OppoArmyController.this.mDialogForDisallow));
                }
                if (!OppoArmyController.this.mDialogForDisallow.isShowing()) {
                    OppoArmyController.this.mDialogForDisallow.show();
                }
            } else if (i == 1) {
                ((Dialog) msg.obj).dismiss();
            }
        }
    }

    public OppoArmyController(Context context) {
        this.mUiContext = context;
        this.mOppoArmyService = new OppoArmyService();
        this.mOppoArmyService.publish(context);
    }

    public void systemReady() {
        new Thread(new Runnable() {
            /* class com.android.server.wm.OppoArmyController.AnonymousClass1 */

            public void run() {
                List unused = OppoArmyController.this.mCustomizeList = OppoArmyController.loadCustomizeWhiteList(OppoArmyController.CUSTOMIZE_LIST_PATH);
                List unused2 = OppoArmyController.this.mProtectAppList = OppoArmyController.loadCustomizeWhiteList(OppoArmyController.PROTECT_APP_LIST_PATH);
                OppoArmyController.this.mOppoArmyService.systemReady();
            }
        }).start();
    }

    private boolean checkWhiteList(String packageName) {
        List<String> list = this.mCustomizeList;
        if (list == null || list.size() <= 0 || packageName == null) {
            return false;
        }
        try {
            for (String pkg : this.mCustomizeList) {
                if (pkg.equalsIgnoreCase(packageName)) {
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            Slog.w(TAG, "check white list has exception! ", e);
            return false;
        }
    }

    private boolean checkProtectAppList(Context context, String packageName) {
        List<String> list;
        if (context.getPackageManager().hasSystemFeature("oppo.customize.function.addprotectapp") && (list = this.mProtectAppList) != null && !list.isEmpty() && this.mProtectAppList.contains(packageName)) {
            return true;
        }
        return false;
    }

    public boolean isRunningDisallowed(String pkgName) {
        return this.mOppoArmyService.isRunningDisallowed(pkgName);
    }

    public void showDisallowedRunningAppDialog() {
        Message msg = Message.obtain();
        msg.what = 0;
        this.mUiHandler.sendMessage(msg);
    }

    public boolean isAllowedForceStop(Context context, String packageName) {
        if (!context.getPackageManager().hasSystemFeature("oppo.customize.function.forbid_stop_app", 0)) {
            return true;
        }
        if (!checkWhiteList(packageName) && !checkProtectAppList(context, packageName)) {
            return true;
        }
        Slog.d(TAG, "current app is not allowed to force stop!");
        return false;
    }

    public boolean allowCallerKillProcess(Context context, int uid) {
        if (!context.getPackageManager().hasSystemFeature("oppo.customize.function.killprocess")) {
            return false;
        }
        try {
            String[] callerPkgs = AppGlobals.getPackageManager().getPackagesForUid(uid);
            if (callerPkgs == null || callerPkgs.length <= 0) {
                return false;
            }
            for (String pkgName : callerPkgs) {
                if (checkWhiteList(pkgName)) {
                    return true;
                }
            }
            return false;
        } catch (RemoteException e) {
            Slog.e(TAG, "getPackagesForUid failed for uid:" + uid, e);
            return false;
        }
    }

    /* access modifiers changed from: private */
    public static List<String> loadCustomizeWhiteList(String path) {
        String value;
        ArrayList<String> emptyList = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            Slog.w(TAG, path + " file don't exist!");
            return emptyList;
        }
        ArrayList<String> ret = new ArrayList<>();
        FileInputStream listFileInputStream = null;
        boolean success = false;
        try {
            listFileInputStream = new FileInputStream(file);
            XmlPullParser parser = Xml.newPullParser();
            String str = null;
            parser.setInput(listFileInputStream, null);
            while (true) {
                int type = parser.next();
                if (type == 2 && "p".equals(parser.getName()) && (value = parser.getAttributeValue(str, "att")) != null) {
                    ret.add(value);
                }
                if (type == 1) {
                    break;
                }
                str = null;
            }
            success = true;
            try {
                listFileInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e2) {
            Slog.w(TAG, "failed parsing ", e2);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (NumberFormatException e3) {
            Slog.w(TAG, "failed parsing ", e3);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (XmlPullParserException e4) {
            Slog.w(TAG, "failed parsing ", e4);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (IOException e5) {
            Slog.w(TAG, "failed parsing ", e5);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (IndexOutOfBoundsException e6) {
            Slog.w(TAG, "failed parsing ", e6);
            if (listFileInputStream != null) {
                listFileInputStream.close();
            }
        } catch (Throwable th) {
            if (listFileInputStream != null) {
                try {
                    listFileInputStream.close();
                } catch (IOException e7) {
                    e7.printStackTrace();
                }
            }
            throw th;
        }
        if (success) {
            return ret;
        }
        Slog.w(TAG, path + " file failed parsing!");
        return emptyList;
    }
}
