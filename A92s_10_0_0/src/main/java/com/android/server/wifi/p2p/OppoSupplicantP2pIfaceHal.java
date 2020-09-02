package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicant;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.net.wifi.p2p.WifiP2pConfig;
import android.os.IHwBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.server.wifi.OppoSoftapP2pBandControl;
import com.android.server.wifi.p2p.OppoSupplicantP2pIfaceHal;
import com.android.server.wifi.util.NativeUtil;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantP2pIface;

public class OppoSupplicantP2pIfaceHal {
    private static final int DEFAULT_5G_P2P_REG_CLASS = 124;
    private static final int DEFAULT_CFG_OPER_CHANNEL = 1;
    private static final int DEFAULT_GROUP_OWNER_INTENT = 6;
    private static final int DEFAULT_OPERATING_CLASS = 81;
    private static final int LISTEN_OPERATING_CHANNEL_NOT_VALID = -1;
    private static final String OPPO_P2P_HIDL_IFACE = "oppo_p2p";
    private static final int RESULT_NOT_VALID = -1;
    /* access modifiers changed from: private */
    public static final String TAG = OppoSupplicantP2pIfaceHal.class.getSimpleName();
    private ISupplicantIface mHidlSupplicantIface = null;
    private IOppoSupplicantP2pIface mIOppoSupplicantP2pIface = null;
    private ISupplicant mISupplicant = null;
    private ISupplicantP2pIface mISupplicantP2pIface = null;
    private Object mLock = new Object();
    private final IHwBinder.DeathRecipient mOppoP2pIfaceDeathRecipient = new IHwBinder.DeathRecipient() {
        /* class com.android.server.wifi.p2p.$$Lambda$OppoSupplicantP2pIfaceHal$IDRw9QyK_iYWKUKZEdM6kpE41V0 */

        public final void serviceDied(long j) {
            OppoSupplicantP2pIfaceHal.this.lambda$new$0$OppoSupplicantP2pIfaceHal(j);
        }
    };

    public /* synthetic */ void lambda$new$0$OppoSupplicantP2pIfaceHal(long cookie) {
        String str = TAG;
        Log.w(str, "OppoP2pIface died: cookie=" + cookie);
        supplicantServiceDiedHandler();
    }

    public boolean initialize() {
        boolean oppoP2pIface;
        synchronized (this.mLock) {
            try {
                this.mISupplicant = getSupplicantMockable();
                oppoP2pIface = getOppoP2pIface();
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "Exception while trying to register a listener for ISupplicant service: " + e);
                supplicantServiceDiedHandler();
                return false;
            } catch (Throwable th) {
                throw th;
            }
        }
        return oppoP2pIface;
    }

    /* access modifiers changed from: protected */
    public ISupplicant getSupplicantMockable() throws RemoteException {
        try {
            return ISupplicant.getService();
        } catch (NoSuchElementException e) {
            Log.e(TAG, "Failed to get ISupplicant", e);
            return null;
        }
    }

    private boolean getOppoP2pIface() {
        if (this.mISupplicant == null) {
            Log.e(TAG, "mISupplicant is null!");
            return false;
        }
        ISupplicant.IfaceInfo ifaceInfo = new ISupplicant.IfaceInfo();
        ifaceInfo.type = 1;
        ifaceInfo.name = OPPO_P2P_HIDL_IFACE;
        SupplicantResult<ISupplicantIface> supplicantIface = new SupplicantResult<>("getInterface()");
        try {
            this.mISupplicant.getInterface(ifaceInfo, new ISupplicant.getInterfaceCallback() {
                /* class com.android.server.wifi.p2p.$$Lambda$OppoSupplicantP2pIfaceHal$3JbSMrMMdNJGL87SsZzJL3Bys7A */

                @Override // android.hardware.wifi.supplicant.V1_0.ISupplicant.getInterfaceCallback
                public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
                    OppoSupplicantP2pIfaceHal.lambda$getOppoP2pIface$1(OppoSupplicantP2pIfaceHal.SupplicantResult.this, supplicantStatus, iSupplicantIface);
                }
            });
            ISupplicantIface ifaceHwBinder = supplicantIface.getResult();
            if (ifaceHwBinder != null) {
                this.mIOppoSupplicantP2pIface = IOppoSupplicantP2pIface.asInterface(ifaceHwBinder.asBinder());
                if (this.mIOppoSupplicantP2pIface == null) {
                    Log.wtf(TAG, "Error on get mIOppoSupplicantP2pIface");
                } else if (!this.mIOppoSupplicantP2pIface.linkToDeath(this.mOppoP2pIfaceDeathRecipient, 0)) {
                    Log.wtf(TAG, "Error on linkToDeath on OppoP2pIface");
                    supplicantServiceDiedHandler();
                    return false;
                }
            } else {
                Log.wtf(TAG, "Error on get ifaceHwBinder");
            }
            return true;
        } catch (RemoteException e) {
            String str = TAG;
            Log.e(str, "ISupplicant.getInterface exception: " + e);
            supplicantServiceDiedHandler();
            return false;
        }
    }

    static /* synthetic */ void lambda$getOppoP2pIface$1(SupplicantResult supplicantIface, SupplicantStatus status, ISupplicantIface iface) {
        if (status.code != 0) {
            String str = TAG;
            Log.e(str, "Failed to get ISupplicantIface " + status.code);
            return;
        }
        supplicantIface.setResult(status, iface);
    }

    public String connect(WifiP2pConfig config, boolean joinExistingGroup, int freq) {
        int groupOwnerIntent;
        SupplicantResult<String> result;
        if (config == null) {
            Log.e(TAG, "Could not connect: null config.");
            return null;
        }
        synchronized (this.mLock) {
            try {
                if (!checkOppoSupplicantP2pIfaceAndLogFailure("setSsidPostfix")) {
                    return null;
                }
                if (config.deviceAddress == null) {
                    Log.e(TAG, "Could not parse null mac address.");
                    return null;
                } else if (config.wps.setup != 0 || TextUtils.isEmpty(config.wps.pin)) {
                    try {
                        byte[] peerAddress = NativeUtil.macAddressToByteArray(config.deviceAddress);
                        int provisionMethod = wpsInfoToConfigMethod(config.wps.setup);
                        if (provisionMethod == -1) {
                            Log.e(TAG, "Invalid WPS config method: " + config.wps.setup);
                            return null;
                        }
                        String preSelectedPin = TextUtils.isEmpty(config.wps.pin) ? "" : config.wps.pin;
                        boolean persistent = config.netId == -2;
                        if (!joinExistingGroup) {
                            groupOwnerIntent = config.groupOwnerIntent;
                            if (groupOwnerIntent < 0 || groupOwnerIntent > 15) {
                                groupOwnerIntent = 6;
                            }
                        } else {
                            groupOwnerIntent = 0;
                        }
                        StringBuilder sb = new StringBuilder();
                        sb.append("connect(");
                        sb.append(config.deviceAddress);
                        sb.append(", ");
                        try {
                            sb.append(freq);
                            sb.append(")");
                            SupplicantResult<String> result2 = new SupplicantResult<>(sb.toString());
                            try {
                                result = result2;
                                try {
                                    this.mIOppoSupplicantP2pIface.p2pConnect(peerAddress, provisionMethod, preSelectedPin, joinExistingGroup, persistent, groupOwnerIntent, freq, new IOppoSupplicantP2pIface.p2pConnectCallback() {
                                        /* class com.android.server.wifi.p2p.$$Lambda$OppoSupplicantP2pIfaceHal$zgD2_uW1pGzT4Y27llcnnKzw61Q */

                                        @Override // vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantP2pIface.p2pConnectCallback
                                        public final void onValues(SupplicantStatus supplicantStatus, String str) {
                                            OppoSupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, str);
                                        }
                                    });
                                } catch (RemoteException e) {
                                    e = e;
                                }
                            } catch (RemoteException e2) {
                                e = e2;
                                result = result2;
                                Log.e(TAG, "ISupplicantP2pIface exception: " + e);
                                supplicantServiceDiedHandler();
                                String result3 = result.getResult();
                                return result3;
                            }
                            String result32 = result.getResult();
                            return result32;
                        } catch (Throwable th) {
                            e = th;
                            throw e;
                        }
                    } catch (Exception e3) {
                        Log.e(TAG, "Could not parse peer mac address.", e3);
                        return null;
                    }
                } else {
                    Log.e(TAG, "Expected empty pin for PBC.");
                    return null;
                }
            } catch (Throwable th2) {
                e = th2;
                throw e;
            }
        }
    }

    public boolean setNetworkVariable(int netId, String name, String value) {
        synchronized (this.mLock) {
            if (!checkOppoSupplicantP2pIfaceAndLogFailure("setNetworkVariable")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setNetworkVariable(" + netId + ", " + name + ", " + value + ")");
            try {
                result.setResult(this.mIOppoSupplicantP2pIface.setNetworkVariable(netId, name, value));
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public int addP2pNetwork() {
        synchronized (this.mLock) {
            if (!checkOppoSupplicantP2pIfaceAndLogFailure("addP2pNetwork")) {
                return -1;
            }
            SupplicantResult<Integer> result = new SupplicantResult<>("addP2pNetwork()");
            try {
                this.mIOppoSupplicantP2pIface.addP2pNetwork(new IOppoSupplicantP2pIface.addP2pNetworkCallback() {
                    /* class com.android.server.wifi.p2p.$$Lambda$OppoSupplicantP2pIfaceHal$PcC0Lo_m6uLF1BrXQKkrah5LoU */

                    @Override // vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantP2pIface.addP2pNetworkCallback
                    public final void onValues(SupplicantStatus supplicantStatus, int i) {
                        OppoSupplicantP2pIfaceHal.SupplicantResult.this.setResult(supplicantStatus, Integer.valueOf(i));
                    }
                });
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            if (!result.isSuccess()) {
                return -1;
            }
            int intValue = result.getResult().intValue();
            return intValue;
        }
    }

    public boolean find(int timeout, int freq) {
        synchronized (this.mLock) {
            if (this.mIOppoSupplicantP2pIface == null) {
                initialize();
            }
            if (!checkOppoSupplicantP2pIfaceAndLogFailure("find")) {
                return false;
            }
            if (timeout < 0) {
                String str = TAG;
                Log.e(str, "Invalid timeout value: " + timeout);
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("find(" + timeout + ", " + freq + ")");
            try {
                result.setResult(this.mIOppoSupplicantP2pIface.p2pFind(timeout, freq));
            } catch (RemoteException e) {
                String str2 = TAG;
                Log.e(str2, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    public void setP2pChannel(int listenChannel, int operatingChannel) {
        setListenChannel(listenChannel, -1);
        setOperChannel(operatingChannel, DEFAULT_5G_P2P_REG_CLASS, 1);
    }

    private boolean setOperChannel(int operChannel, int operRegClass, int cfgOperChannel) {
        synchronized (this.mLock) {
            if (!checkOppoSupplicantP2pIfaceAndLogFailure("setOperChannel")) {
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setOperChannel(" + operChannel + ", " + operRegClass + "," + cfgOperChannel + ")");
            try {
                result.setResult(this.mIOppoSupplicantP2pIface.setOperChannel(operChannel, operRegClass, cfgOperChannel));
            } catch (RemoteException e) {
                String str = TAG;
                Log.e(str, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }

    private boolean checkOppoSupplicantP2pIfaceAndLogFailure(String method) {
        if (this.mIOppoSupplicantP2pIface != null) {
            return true;
        }
        String str = TAG;
        Log.e(str, "Can't call " + method + ": IOppoSupplicantP2pIface is null");
        return false;
    }

    /* access modifiers changed from: private */
    public static class SupplicantResult<E> {
        private String mMethodName;
        private SupplicantStatus mStatus = null;
        private E mValue = null;

        SupplicantResult(String methodName) {
            this.mMethodName = methodName;
            String access$000 = OppoSupplicantP2pIfaceHal.TAG;
            Log.d(access$000, "entering " + this.mMethodName);
        }

        public void setResult(SupplicantStatus status, E value) {
            OppoSupplicantP2pIfaceHal.logCompletion(this.mMethodName, status);
            String access$000 = OppoSupplicantP2pIfaceHal.TAG;
            Log.d(access$000, "leaving " + this.mMethodName + " with result = " + ((Object) value));
            this.mStatus = status;
            this.mValue = value;
        }

        public void setResult(SupplicantStatus status) {
            OppoSupplicantP2pIfaceHal.logCompletion(this.mMethodName, status);
            String access$000 = OppoSupplicantP2pIfaceHal.TAG;
            Log.d(access$000, "leaving " + this.mMethodName);
            this.mStatus = status;
        }

        public boolean isSuccess() {
            SupplicantStatus supplicantStatus = this.mStatus;
            return supplicantStatus != null && (supplicantStatus.code == 0 || this.mStatus.code == 5);
        }

        public E getResult() {
            if (isSuccess()) {
                return this.mValue;
            }
            return null;
        }
    }

    protected static void logCompletion(String operation, SupplicantStatus status) {
        if (status == null) {
            String str = TAG;
            Log.w(str, operation + " failed: no status code returned.");
        } else if (status.code == 0) {
            String str2 = TAG;
            Log.d(str2, operation + " completed successfully.");
        } else {
            String str3 = TAG;
            Log.w(str3, operation + " failed: " + status.code + " (" + status.debugMessage + ")");
        }
    }

    private int wpsInfoToConfigMethod(int info) {
        if (info == 0) {
            return 0;
        }
        if (info == 1) {
            return 1;
        }
        if (info == 2 || info == 3) {
            return 2;
        }
        String str = TAG;
        Log.e(str, "Unsupported WPS provision method: " + info);
        return -1;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:44:0x00ed, code lost:
        return false;
     */
    private boolean setListenChannel(int listenChannel, int operatingChannel) {
        synchronized (this.mLock) {
            if (!checkOppoSupplicantP2pIfaceAndLogFailure("setListenChannel")) {
                return false;
            }
            if (listenChannel >= 1 && listenChannel <= 11) {
                SupplicantResult<Void> result = new SupplicantResult<>("setListenChannel(" + listenChannel + ", " + 81 + ")");
                try {
                    result.setResult(this.mISupplicantP2pIface.setListenChannel(listenChannel, 81));
                } catch (RemoteException e) {
                    String str = TAG;
                    Log.e(str, "ISupplicantP2pIface exception: " + e);
                    supplicantServiceDiedHandler();
                }
                if (!result.isSuccess()) {
                    return false;
                }
            } else if (listenChannel != 0) {
                return false;
            }
            if (operatingChannel >= 0 && operatingChannel <= 165) {
                ArrayList<ISupplicantP2pIface.FreqRange> ranges = new ArrayList<>();
                if (operatingChannel >= 1 && operatingChannel <= 165) {
                    int freq = (operatingChannel <= 14 ? 2407 : 5000) + (operatingChannel * 5);
                    ISupplicantP2pIface.FreqRange range1 = new ISupplicantP2pIface.FreqRange();
                    range1.min = 1000;
                    range1.max = freq - 5;
                    ISupplicantP2pIface.FreqRange range2 = new ISupplicantP2pIface.FreqRange();
                    range2.min = freq + 5;
                    range2.max = 6000;
                    ranges.add(range1);
                    ranges.add(range2);
                }
                SupplicantResult<Void> result2 = new SupplicantResult<>("setDisallowedFrequencies(" + ranges + ")");
                try {
                    result2.setResult(this.mISupplicantP2pIface.setDisallowedFrequencies(ranges));
                } catch (RemoteException e2) {
                    String str2 = TAG;
                    Log.e(str2, "ISupplicantP2pIface exception: " + e2);
                    supplicantServiceDiedHandler();
                }
                boolean isSuccess = result2.isSuccess();
                return isSuccess;
            }
        }
    }

    private void supplicantServiceDiedHandler() {
        synchronized (this.mLock) {
            this.mIOppoSupplicantP2pIface = null;
            this.mISupplicant = null;
            this.mHidlSupplicantIface = null;
        }
    }

    public boolean setP2pBandLIst(OppoSoftapP2pBandControl.BandType type) {
        synchronized (this.mLock) {
            ArrayList<ISupplicantP2pIface.FreqRange> ranges = new ArrayList<>();
            ISupplicantP2pIface.FreqRange range = new ISupplicantP2pIface.FreqRange();
            String str = TAG;
            Log.d(str, "setP2pBandLIst " + type);
            if (type == OppoSoftapP2pBandControl.BandType.BAND_2G_ONLY) {
                range.min = 5175;
                range.max = 5830;
                ranges.add(range);
            } else if (type == OppoSoftapP2pBandControl.BandType.BAND_5G_B1) {
                range.min = 5255;
                range.max = 5830;
                ranges.add(range);
            } else if (type == OppoSoftapP2pBandControl.BandType.BAND_5G_B4) {
                range.min = 5175;
                range.max = 5705;
                ranges.add(range);
            } else {
                Log.d(TAG, "setP2pBandLIst invalid type no need to limit band ");
                return false;
            }
            SupplicantResult<Void> result = new SupplicantResult<>("setDisallowedFrequencies(" + ranges + ")");
            try {
                result.setResult(this.mISupplicantP2pIface.setDisallowedFrequencies(ranges));
            } catch (RemoteException e) {
                String str2 = TAG;
                Log.e(str2, "ISupplicantP2pIface exception: " + e);
                supplicantServiceDiedHandler();
            }
            boolean isSuccess = result.isSuccess();
            return isSuccess;
        }
    }
}
