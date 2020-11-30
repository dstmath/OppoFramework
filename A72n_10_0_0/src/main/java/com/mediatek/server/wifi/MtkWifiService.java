package com.mediatek.server.wifi;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import com.android.internal.util.State;
import com.android.server.wifi.ScanDetail;
import com.mediatek.common.util.OperatorCustomizationFactoryLoader;
import com.mediatek.server.wifi.MtkWifiServiceAdapter;
import com.mediatek.server.wifi.WifiOperatorFactoryBase;
import java.util.ArrayList;
import java.util.List;

public final class MtkWifiService implements MtkWifiServiceAdapter.IMtkWifiService {
    private static final String TAG = "MtkWifiService";
    private static final List<OperatorCustomizationFactoryLoader.OperatorFactoryInfo> sFactoryInfoList = new ArrayList();
    private Context mContext;
    private WifiOperatorFactoryBase.IMtkWifiServiceExt mExt = null;

    static {
        sFactoryInfoList.add(new OperatorCustomizationFactoryLoader.OperatorFactoryInfo("Op01WifiService.apk", "com.mediatek.op.wifi.Op01WifiOperatorFactory", "com.mediatek.server.wifi.op01", "OP01"));
    }

    private synchronized WifiOperatorFactoryBase.IMtkWifiServiceExt getOpExt() {
        if (this.mExt == null) {
            WifiOperatorFactoryBase factory = (WifiOperatorFactoryBase) OperatorCustomizationFactoryLoader.loadFactory(this.mContext, sFactoryInfoList);
            if (factory == null) {
                factory = new WifiOperatorFactoryBase();
            }
            Log.i(TAG, "Factory is : " + factory.getClass());
            this.mExt = factory.createWifiFwkExt(this.mContext);
            this.mExt.init();
        }
        return this.mExt;
    }

    public MtkWifiService(Context context) {
        Log.i(TAG, "[MtkWifiService] " + context);
        this.mContext = context;
    }

    public void initialize() {
        Log.i(TAG, "[initialize]");
        getOpExt();
    }

    public void handleScanResults(List<ScanDetail> full, List<ScanDetail> unsaved) {
        this.mExt.handleScanResults(full, unsaved);
    }

    public void updateRSSI(Integer newRssi, int ipAddr, int lastNetworkId) {
        this.mExt.updateRSSI(newRssi, ipAddr, lastNetworkId);
    }

    public boolean preProcessMessage(State state, Message msg) {
        return this.mExt.preProcessMessage(state, msg);
    }

    public boolean postProcessMessage(State state, Message msg, Object... args) {
        return this.mExt.postProcessMessage(state, msg, args);
    }

    public boolean needCustomEvaluator() {
        return this.mExt.needCustomEvaluator();
    }

    public void triggerNetworkEvaluatorCallBack() {
        this.mExt.triggerNetworkEvaluatorCallBack();
    }
}
