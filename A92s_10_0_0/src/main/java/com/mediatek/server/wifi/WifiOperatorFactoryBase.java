package com.mediatek.server.wifi;

import android.content.Context;
import android.os.Message;
import com.android.internal.util.State;
import com.android.server.wifi.ScanDetail;
import java.util.List;

public class WifiOperatorFactoryBase {

    public interface IMtkWifiServiceExt {
        void handleScanResults(List<ScanDetail> list, List<ScanDetail> list2);

        void init();

        boolean needCustomEvaluator();

        boolean postProcessMessage(State state, Message message, Object... objArr);

        boolean preProcessMessage(State state, Message message);

        void triggerNetworkEvaluatorCallBack();

        void updateRSSI(Integer num, int i, int i2);
    }

    public IMtkWifiServiceExt createWifiFwkExt(Context context) {
        return new DefaultMtkWifiServiceExt(context);
    }

    public static class DefaultMtkWifiServiceExt implements IMtkWifiServiceExt {
        protected Context mContext;

        public DefaultMtkWifiServiceExt(Context context) {
            this.mContext = context;
        }

        @Override // com.mediatek.server.wifi.WifiOperatorFactoryBase.IMtkWifiServiceExt
        public void init() {
        }

        @Override // com.mediatek.server.wifi.WifiOperatorFactoryBase.IMtkWifiServiceExt
        public void handleScanResults(List<ScanDetail> list, List<ScanDetail> list2) {
        }

        @Override // com.mediatek.server.wifi.WifiOperatorFactoryBase.IMtkWifiServiceExt
        public void updateRSSI(Integer newRssi, int ipAddr, int lastNetworkId) {
        }

        @Override // com.mediatek.server.wifi.WifiOperatorFactoryBase.IMtkWifiServiceExt
        public boolean preProcessMessage(State state, Message msg) {
            return false;
        }

        @Override // com.mediatek.server.wifi.WifiOperatorFactoryBase.IMtkWifiServiceExt
        public boolean postProcessMessage(State state, Message msg, Object... args) {
            return false;
        }

        @Override // com.mediatek.server.wifi.WifiOperatorFactoryBase.IMtkWifiServiceExt
        public void triggerNetworkEvaluatorCallBack() {
        }

        @Override // com.mediatek.server.wifi.WifiOperatorFactoryBase.IMtkWifiServiceExt
        public boolean needCustomEvaluator() {
            return false;
        }
    }
}
