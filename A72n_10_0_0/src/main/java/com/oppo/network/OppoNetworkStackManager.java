package com.oppo.network;

import android.content.Context;
import android.net.Network;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import com.oppo.network.IOppoNetScoreChange;
import com.oppo.network.IOppoNetworkStack;
import java.util.ArrayList;
import java.util.Iterator;

public class OppoNetworkStackManager {
    public static final boolean DBG = true;
    public static final String LOG_TAG = "OppoNetworkStackManager";
    public static final String SRV_NAME = "opponetworkstack";
    private static ArrayList<INetworkScoreCallback> allCallbacks = new ArrayList<>();
    private static OppoNetworkStackManager sInstance;
    public Context mContext;
    private IOppoNetworkStack mNetworkStackService = IOppoNetworkStack.Stub.asInterface(ServiceManager.getService(SRV_NAME));

    public interface INetworkScoreCallback {
        void onNetworkQualityChange(boolean z, int i);
    }

    public static void registerTcpCallback(INetworkScoreCallback nsc) {
        synchronized (allCallbacks) {
            if (!allCallbacks.contains(nsc)) {
                allCallbacks.add(nsc);
            }
        }
    }

    public static void unregisterTcpCallback(INetworkScoreCallback nsc) {
        synchronized (allCallbacks) {
            if (allCallbacks.contains(nsc)) {
                allCallbacks.remove(nsc);
            }
        }
    }

    public void startTest() {
        this.mNetworkStackService = IOppoNetworkStack.Stub.asInterface(ServiceManager.getService(SRV_NAME));
        if (this.mNetworkStackService != null) {
            try {
                Log.e(LOG_TAG, "registerTcpScoreChange start!");
                this.mNetworkStackService.registerTcpScoreChange(new MyCallBack());
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "registerTcpScoreChange fail!");
            }
        } else {
            Log.e(LOG_TAG, "mNetworkStackService is null!");
        }
    }

    public String getOppoNetworkStackInfo() {
        this.mNetworkStackService = IOppoNetworkStack.Stub.asInterface(ServiceManager.getService(SRV_NAME));
        if (this.mNetworkStackService != null) {
            try {
                Log.e(LOG_TAG, "getOppoNetworkStackInfo start!");
                return this.mNetworkStackService.getOppoNetworkStackInfo();
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "getOppoNetworkStackInfo fail!");
                return "";
            }
        } else {
            Log.e(LOG_TAG, "getOppoNetworkStackInfo is null!");
            return "";
        }
    }

    public void setOppoNetworkStackConfig(String config) {
        this.mNetworkStackService = IOppoNetworkStack.Stub.asInterface(ServiceManager.getService(SRV_NAME));
        if (this.mNetworkStackService != null) {
            try {
                Log.e(LOG_TAG, "setOppoNetworkStackConfig start!");
                this.mNetworkStackService.setOppoNetworkStackConfig(config);
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "setOppoNetworkStackConfig fail!");
            }
        } else {
            Log.e(LOG_TAG, "setOppoNetworkStackConfig is null!");
        }
    }

    public int getFailRate(Network network) {
        this.mNetworkStackService = IOppoNetworkStack.Stub.asInterface(ServiceManager.getService(SRV_NAME));
        if (this.mNetworkStackService != null) {
            try {
                Log.e(LOG_TAG, "getFailRate start!");
                int uu = this.mNetworkStackService.getPortalResult(network, 3);
                Log.e(LOG_TAG, "getFailRate rate = " + uu);
                return uu;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "getFailRate fail!");
            }
        } else {
            Log.e(LOG_TAG, "getFailRate is null!");
            Log.e(LOG_TAG, "getFailRate is error rerurn 0!");
            return 0;
        }
    }

    public int getNetworkScore(Network network) {
        this.mNetworkStackService = IOppoNetworkStack.Stub.asInterface(ServiceManager.getService(SRV_NAME));
        if (this.mNetworkStackService != null) {
            try {
                Log.e(LOG_TAG, "getNetworkScore start!");
                int uu = this.mNetworkStackService.getNetworkScore(network);
                Log.e(LOG_TAG, "getNetworkScore score = " + uu);
                return uu;
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "getNetworkScore fail!");
            }
        } else {
            Log.e(LOG_TAG, "getNetworkScore is null!");
            Log.e(LOG_TAG, "getNetworkScore is error rerurn 0!");
            return 0;
        }
    }

    private class MyCallBack extends IOppoNetScoreChange.Stub {
        private MyCallBack() {
        }

        @Override // com.oppo.network.IOppoNetScoreChange
        public void networkScoreChange(boolean better, int score) throws RemoteException {
            synchronized (OppoNetworkStackManager.allCallbacks) {
                Log.e(OppoNetworkStackManager.LOG_TAG, "callback len = " + OppoNetworkStackManager.allCallbacks.size());
                Iterator it = OppoNetworkStackManager.allCallbacks.iterator();
                while (it.hasNext()) {
                    ((INetworkScoreCallback) it.next()).onNetworkQualityChange(better, score);
                }
            }
        }
    }

    public static OppoNetworkStackManager getInstance(Context c) {
        OppoNetworkStackManager oppoNetworkStackManager;
        synchronized (OppoNetworkStackManager.class) {
            if (sInstance == null) {
                sInstance = new OppoNetworkStackManager(c);
                Log.e(LOG_TAG, "OppoNetworkStackManager first new!");
            }
            oppoNetworkStackManager = sInstance;
        }
        return oppoNetworkStackManager;
    }

    protected OppoNetworkStackManager(Context context) {
        this.mContext = context;
        if (this.mNetworkStackService != null) {
            try {
                Log.e(LOG_TAG, "registerTcpScoreChange start!");
                this.mNetworkStackService.registerTcpScoreChange(new MyCallBack());
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "registerTcpScoreChange fail!");
            }
        } else {
            Log.e(LOG_TAG, "mNetworkStackService is null!");
        }
    }
}
