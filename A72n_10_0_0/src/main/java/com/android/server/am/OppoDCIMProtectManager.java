package com.android.server.am;

import android.app.AppGlobals;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UEventObserver;
import android.util.Log;
import android.util.Slog;
import com.android.server.display.ai.utils.ColorAILog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OppoDCIMProtectManager {
    private static final String DCIMPROTECT_SERVICE = "coloros.safecenter.permission.PERMISSION_DIALOG_SERVICE";
    private static final boolean DEBUG = SystemProperties.getBoolean(ColorAILog.OPPO_LOG_KEY, (boolean) DEBUG);
    private static final int MSG_DCIM_PROTECT_RENAME = 10002;
    private static final int MSG_DCIM_PROTECT_UNLINK = 10001;
    private static final String PERMISSION_PACKGE_NAME = "com.coloros.securitypermission";
    private static final String TAG = "OppoDCIMProtectManager";
    private static final String UEVENT_DP_RENAME = "RENAME_STAT";
    private static final String UEVENT_DP_UNLINK = "UNLINK_STAT";
    private static boolean sDCIMProtectEnabled = true;
    private static List<String> sDefaultSkipPackages = Arrays.asList("com.oppo.camera", "com.android.providers.media", "com.coloros.filemanager", "com.coloros.cloud", "com.heytap.cloud", "com.coloros.gallery3d", "com.coloros.video", "com.google.android.apps.photos", "com.coloros.encryption");
    private static ArrayList<Integer> sDefaultSkipUids = new ArrayList<>();
    private static OppoDCIMProtectManager sInstance;
    private static Bundle sRecordedData = null;
    private static int sRecordedMsgWhat = 0;
    private Context mContext;
    private UEventObserver mDCIMRenameEventObserver = new UEventObserver() {
        /* class com.android.server.am.OppoDCIMProtectManager.AnonymousClass2 */

        public void onUEvent(UEventObserver.UEvent event) {
            OppoDCIMProtectManager.this.mH.sendMessage(OppoDCIMProtectManager.this.mH.obtainMessage(3, event));
        }
    };
    private UEventObserver mDCIMUnlinkEventObserver = new UEventObserver() {
        /* class com.android.server.am.OppoDCIMProtectManager.AnonymousClass1 */

        public void onUEvent(UEventObserver.UEvent event) {
            OppoDCIMProtectManager.this.mH.sendMessage(OppoDCIMProtectManager.this.mH.obtainMessage(2, event));
        }
    };
    private final H mH = new H();
    private Messenger mPermissioinDialogService;
    private ServiceConnection mServConnection = new ServiceConnection() {
        /* class com.android.server.am.OppoDCIMProtectManager.AnonymousClass3 */

        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(OppoDCIMProtectManager.TAG, "onServiceConnected");
            OppoDCIMProtectManager.this.mPermissioinDialogService = new Messenger(service);
            if (OppoDCIMProtectManager.sRecordedMsgWhat != 0 && OppoDCIMProtectManager.sRecordedData != null) {
                OppoDCIMProtectManager.this.sendMsgToPermissionDialogService(OppoDCIMProtectManager.sRecordedMsgWhat, OppoDCIMProtectManager.sRecordedData);
                int unused = OppoDCIMProtectManager.sRecordedMsgWhat = 0;
                Bundle unused2 = OppoDCIMProtectManager.sRecordedData = null;
            }
        }

        public void onServiceDisconnected(ComponentName name) {
            Log.d(OppoDCIMProtectManager.TAG, "onServiceDisconnected");
            OppoDCIMProtectManager.this.mPermissioinDialogService = null;
            OppoDCIMProtectManager.this.bindPermissionDialogService();
        }
    };

    public static OppoDCIMProtectManager getInstance() {
        if (sInstance == null) {
            sInstance = new OppoDCIMProtectManager();
        }
        return sInstance;
    }

    private OppoDCIMProtectManager() {
        initDCIMProtectWhiteList();
    }

    public boolean isDCIMProtectEnabled() {
        return sDCIMProtectEnabled;
    }

    public boolean isSkippedUid(int uid) {
        return sDefaultSkipUids.contains(Integer.valueOf(uid));
    }

    public void setDCIMProtectEnabled(boolean enable) {
        sDCIMProtectEnabled = enable;
    }

    private void initDCIMProtectWhiteList() {
        for (String packageName : sDefaultSkipPackages) {
            try {
                ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
                if (appInfo != null) {
                    sDefaultSkipUids.add(Integer.valueOf(appInfo.uid));
                }
            } catch (Exception e) {
                Log.e(TAG, "getApplicationInfo Exception: " + e.toString());
            }
        }
    }

    public void addToDCIMProtectWhiteList(String packageName) {
        try {
            ApplicationInfo appInfo = AppGlobals.getPackageManager().getApplicationInfo(packageName, 0, 0);
            if (appInfo != null) {
                sDefaultSkipUids.add(Integer.valueOf(appInfo.uid));
            }
        } catch (Exception e) {
            Log.e(TAG, "getApplicationInfo Exception: " + e.toString());
        }
    }

    public void startDCIMProtectEventObserving(Context context) {
        this.mContext = context;
        bindPermissionDialogService();
        this.mDCIMUnlinkEventObserver.startObserving(UEVENT_DP_UNLINK);
        this.mDCIMRenameEventObserver.startObserving(UEVENT_DP_RENAME);
    }

    /* access modifiers changed from: package-private */
    public final class H extends Handler {
        public static final int NOTIFY_DCIM_DELETE_EVENT = 2;
        public static final int NOTIFY_DCIM_RENAME_EVENT = 3;

        H() {
        }

        public void handleMessage(Message msg) {
            if (OppoDCIMProtectManager.DEBUG) {
                Slog.v(OppoDCIMProtectManager.TAG, "DCIM Protection Event Handler Message: what=" + msg.what);
            }
            int i = msg.what;
            if (i == 2) {
                UEventObserver.UEvent event = (UEventObserver.UEvent) msg.obj;
                String uid = event.get("UID");
                String pid = event.get("PID");
                String path = event.get("PATH");
                String mask = event.get("MASK");
                if (OppoDCIMProtectManager.DEBUG) {
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_DELETE_EVENT uid " + uid);
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_DELETE_EVENT pid " + pid);
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_DELETE_EVENT path " + path);
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_DELETE_EVENT mask " + mask);
                }
                try {
                    path = new String(path.getBytes("ISO-8859-1"), "UTF-8");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Bundle bundle = new Bundle();
                if (uid != null) {
                    bundle.putInt("UID", Integer.valueOf(uid).intValue());
                }
                if (pid != null) {
                    bundle.putInt("PID", Integer.valueOf(pid).intValue());
                }
                if (mask != null) {
                    bundle.putInt("MASK", Integer.valueOf(mask).intValue());
                }
                bundle.putString("PATH", path);
                OppoDCIMProtectManager.this.sendMsgToPermissionDialogService(OppoDCIMProtectManager.MSG_DCIM_PROTECT_UNLINK, bundle);
            } else if (i == 3) {
                UEventObserver.UEvent event2 = (UEventObserver.UEvent) msg.obj;
                String uid2 = event2.get("UID");
                String pid2 = event2.get("PID");
                String mask2 = event2.get("MASK");
                String oldPath = event2.get("OLD_PATH");
                String newPath = event2.get("NEW_PATH");
                if (OppoDCIMProtectManager.DEBUG) {
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_RENAME_EVENT uid " + uid2);
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_RENAME_EVENT pid " + pid2);
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_RENAME_EVENT mask " + mask2);
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_RENAME_EVENT oldPath " + oldPath);
                    Slog.d(OppoDCIMProtectManager.TAG, "NOTIFY_DCIM_RENAME_EVENT newPath " + newPath);
                }
                try {
                    oldPath = new String(oldPath.getBytes("ISO-8859-1"), "UTF-8");
                    newPath = new String(newPath.getBytes("ISO-8859-1"), "UTF-8");
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                Bundle bundle2 = new Bundle();
                if (uid2 != null) {
                    bundle2.putInt("UID", Integer.valueOf(uid2).intValue());
                }
                if (pid2 != null) {
                    bundle2.putInt("PID", Integer.valueOf(pid2).intValue());
                }
                if (mask2 != null) {
                    bundle2.putInt("MASK", Integer.valueOf(mask2).intValue());
                }
                bundle2.putString("OLD_PATH", oldPath);
                bundle2.putString("NEW_PATH", newPath);
                OppoDCIMProtectManager.this.sendMsgToPermissionDialogService(OppoDCIMProtectManager.MSG_DCIM_PROTECT_RENAME, bundle2);
            }
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void sendMsgToPermissionDialogService(int what, Bundle data) {
        if (this.mPermissioinDialogService == null) {
            if (DEBUG) {
                Log.e(TAG, "dialog Service is null");
            }
            sRecordedMsgWhat = what;
            sRecordedData = data;
            if (!bindPermissionDialogService()) {
                if (DEBUG) {
                    Log.e(TAG, "bind false");
                }
                sRecordedMsgWhat = 0;
                sRecordedData = null;
                return;
            }
            return;
        }
        if (DEBUG) {
            Log.e(TAG, "dialog Service is not null, what = " + what);
        }
        try {
            Message msg = Message.obtain((Handler) null, what);
            if (data != null) {
                msg.setData(data);
            }
            this.mPermissioinDialogService.send(msg);
        } catch (RemoteException e) {
            Log.e(TAG, "");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean bindPermissionDialogService() {
        try {
            Intent intent = new Intent(DCIMPROTECT_SERVICE);
            intent.setPackage(PERMISSION_PACKGE_NAME);
            return this.mContext.bindService(intent, this.mServConnection, 1);
        } catch (Exception e) {
            Log.e(TAG, "bind service err !");
            return DEBUG;
        }
    }
}
