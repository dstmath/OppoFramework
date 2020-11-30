package android.app;

import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import java.util.ArrayList;

public class OppoWhiteListManager {
    private static final boolean DEBUG = false;
    private static final long MAX_PROTECT_SELF_TIMEOUT = 10800000;
    private static final long MIN_PROTECT_SELF_TIMEOUT = 60000;
    private static final String TAG = "OppoWhiteListManager";
    public static final int TYPE_FILTER_CLEAR = 1;
    public static final int TYPE_FILTER_PERMISSION = 2;
    public static final int TYPE_FLOATWINDOW = 5;
    public static final int TYPE_FLOATWINDOW_DEFAULT_GRANT_BUILDIN = 4;
    public static final int TYPE_SPECIFIG_PKG_PROTECT = 3;
    private Context mContext;

    public OppoWhiteListManager(Context context) {
        this.mContext = context;
    }

    public ArrayList<String> getGlobalWhiteList() {
        return getGlobalWhiteList(1);
    }

    public ArrayList<String> getGlobalWhiteList(int type) {
        ArrayList<String> list = new ArrayList<>();
        try {
            list = new OppoActivityManager().getGlobalPkgWhiteList(type);
        } catch (RemoteException e) {
        }
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public ArrayList<String> getStageProtectListFromPkg(String pkg, int type) {
        ArrayList<String> list = new ArrayList<>();
        try {
            list = new OppoActivityManager().getStageProtectListFromPkg(pkg, type);
        } catch (RemoteException e) {
        }
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public ArrayList<String> getGlobalProcessWhiteList() {
        ArrayList<String> list = new ArrayList<>();
        this.mContext.getPackageManager();
        try {
            list = new OppoActivityManager().getGlobalProcessWhiteList();
        } catch (RemoteException e) {
        }
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    public void addStageProtectInfo(String pkg, long timeout) {
        if (pkg == null || pkg.isEmpty()) {
            Log.e(TAG, "can't add empty info to protect infos");
        } else if (timeout < 60000) {
            Log.e(TAG, "timeout should be longer than 1 minute in milliseconds, return.");
        } else {
            String self = this.mContext.getPackageName();
            if (!pkg.equals(self)) {
                Log.w(TAG, self + " try to add " + pkg + " to protect info, are you sure?");
            }
            try {
                new OppoActivityManager().addStageProtectInfo(pkg, self, timeout);
            } catch (RemoteException e) {
            }
        }
    }

    public void removeStageProtectInfo(String pkg) {
        if (pkg == null || pkg.isEmpty()) {
            Log.e(TAG, "can't add empty info to protect infos");
            return;
        }
        try {
            new OppoActivityManager().removeStageProtectInfo(pkg, this.mContext.getPackageName());
        } catch (RemoteException e) {
        }
    }
}
