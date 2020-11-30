package android.net;

import android.content.Context;
import android.net.IOppoNetworkingControlManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.security.keystore.KeyProperties;
import android.util.DebugUtils;
import java.util.Map;

public class OppoNetworkingControlManager {
    public static final String ACTION_APP_NETWORK_NOT_ALLOWED = "oppo.intent.action.APP_NETWORK_NOT_ALLOWED";
    public static final String ACTION_MULTI_PACKAGE_ADDED = "oppo.intent.action.MULTI_APP_PACKAGE_ADDED";
    public static final String ACTION_MULTI_PACKAGE_REMOVED = "oppo.intent.action.MULTI_APP_PACKAGE_REMOVED";
    public static final String ACTION_ROM_APP_CHAGNE = "oppo.intent.action.ROM_APP_CHANGE";
    public static final int ALLOW = 1;
    public static final int DENY = 2;
    public static final String EXTRA_NETWORK_TYPE = "networkType";
    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final int INVALID_UID = -1;
    public static final int POLICY_AllOW_MOBILEDATA_REJECT_WIFI = 2;
    public static final int POLICY_NONE = 0;
    public static final int POLICY_REJECT_ALL = 4;
    public static final int POLICY_REJECT_MOBILEDATA_AllOW_WIFI = 1;
    public static final int REJECT_MOBILEDATA = 5;
    public static final int REJECT_WIFI = 6;
    public static final int TYPE_MOBILEDATA = 0;
    public static final int TYPE_MOBILEDATA_MTK = 1;
    public static final int TYPE_MOBILEDATA_QCOM = 2;
    public static final int TYPE_WIFI = 3;
    private static OppoNetworkingControlManager mInstance = null;
    private IOppoNetworkingControlManager mService;

    private OppoNetworkingControlManager() {
        this.mService = IOppoNetworkingControlManager.Stub.asInterface(ServiceManager.getService("networking_control"));
    }

    public static OppoNetworkingControlManager getOppoNetworkingControlManager() {
        if (mInstance == null) {
            mInstance = new OppoNetworkingControlManager();
        }
        return mInstance;
    }

    public OppoNetworkingControlManager(Context context, IOppoNetworkingControlManager service) {
        if (service != null) {
            this.mService = service;
            return;
        }
        throw new IllegalArgumentException("missing IOppoNetworkingControlManager");
    }

    public static OppoNetworkingControlManager from(Context context) {
        return (OppoNetworkingControlManager) context.getSystemService("networking_control");
    }

    public void setUidPolicy(int uid, int policy) {
        IOppoNetworkingControlManager iOppoNetworkingControlManager = this.mService;
        if (iOppoNetworkingControlManager != null) {
            try {
                iOppoNetworkingControlManager.setUidPolicy(uid, policy);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    public int getUidPolicy(int uid) {
        IOppoNetworkingControlManager iOppoNetworkingControlManager = this.mService;
        if (iOppoNetworkingControlManager == null) {
            return 0;
        }
        try {
            return iOppoNetworkingControlManager.getUidPolicy(uid);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public int[] getUidsWithPolicy(int policy) {
        IOppoNetworkingControlManager iOppoNetworkingControlManager = this.mService;
        if (iOppoNetworkingControlManager == null) {
            return null;
        }
        try {
            return iOppoNetworkingControlManager.getUidsWithPolicy(policy);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public Map<Integer, Integer> getPolicyList() {
        IOppoNetworkingControlManager iOppoNetworkingControlManager = this.mService;
        if (iOppoNetworkingControlManager == null) {
            return null;
        }
        try {
            return iOppoNetworkingControlManager.getPolicyList();
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    public static String uidPoliciesToString(int uidPolicies) {
        StringBuilder sb = new StringBuilder();
        sb.append(uidPolicies);
        StringBuilder string = sb.append(" (");
        if (uidPolicies == 0) {
            string.append(KeyProperties.DIGEST_NONE);
        } else {
            string.append(DebugUtils.flagsToString(OppoNetworkingControlManager.class, "POLICY_", uidPolicies));
        }
        string.append(")");
        return string.toString();
    }

    public void factoryReset() {
        IOppoNetworkingControlManager iOppoNetworkingControlManager = this.mService;
        if (iOppoNetworkingControlManager != null) {
            try {
                iOppoNetworkingControlManager.factoryReset();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
}
