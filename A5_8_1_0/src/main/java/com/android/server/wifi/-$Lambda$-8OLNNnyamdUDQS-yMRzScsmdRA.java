package com.android.server.wifi;

import android.hardware.wifi.V1_0.IWifiApIface;
import android.hardware.wifi.V1_0.IWifiChip;
import android.hardware.wifi.V1_0.IWifiChip.ChipDebugInfo;
import android.hardware.wifi.V1_0.IWifiChip.getDebugHostWakeReasonStatsCallback;
import android.hardware.wifi.V1_0.IWifiChip.getDebugRingBuffersStatusCallback;
import android.hardware.wifi.V1_0.IWifiChip.requestChipDebugInfoCallback;
import android.hardware.wifi.V1_0.IWifiChip.requestDriverDebugDumpCallback;
import android.hardware.wifi.V1_0.IWifiChip.requestFirmwareDebugDumpCallback;
import android.hardware.wifi.V1_0.IWifiRttController;
import android.hardware.wifi.V1_0.IWifiRttController.getResponderInfoCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getApfPacketFilterCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getBackgroundScanCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getDebugRxPacketFatesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getDebugTxPacketFatesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getLinkLayerStatsCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getRoamingCapabilitiesCallback;
import android.hardware.wifi.V1_0.IWifiStaIface.getValidFrequenciesForBandCallback;
import android.hardware.wifi.V1_0.RttCapabilities;
import android.hardware.wifi.V1_0.RttResponder;
import android.hardware.wifi.V1_0.StaApfPacketFilterCapabilities;
import android.hardware.wifi.V1_0.StaBackgroundScanCapabilities;
import android.hardware.wifi.V1_0.StaLinkLayerStats;
import android.hardware.wifi.V1_0.StaRoamingCapabilities;
import android.hardware.wifi.V1_0.WifiDebugHostWakeReasonStats;
import android.hardware.wifi.V1_0.WifiDebugRingBufferStatus;
import android.hardware.wifi.V1_0.WifiStatus;
import android.util.MutableBoolean;
import android.util.MutableInt;
import com.android.server.wifi.WifiNative.RoamingCapabilities;
import com.android.server.wifi.WifiNative.RxFateReport;
import com.android.server.wifi.WifiNative.ScanCapabilities;
import com.android.server.wifi.WifiNative.TxFateReport;
import com.android.server.wifi.WifiVendorHal.AnonymousClass1AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass2AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass3AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass4AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass5AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass6AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass7AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass8AnswerBox;
import com.android.server.wifi.WifiVendorHal.AnonymousClass9AnswerBox;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA implements requestChipDebugInfoCallback {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f5-$f0;

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$10 */
    final /* synthetic */ class AnonymousClass10 implements getCapabilitiesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f6-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f7-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            ((WifiVendorHal) this.f6-$f0).m32lambda$-com_android_server_wifi_WifiVendorHal_37253((MutableInt) this.f7-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass10(Object obj, Object obj2) {
            this.f6-$f0 = obj;
            this.f7-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$11 */
    final /* synthetic */ class AnonymousClass11 implements getLinkLayerStatsCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f8-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f9-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaLinkLayerStats arg1) {
            ((WifiVendorHal) this.f8-$f0).m30lambda$-com_android_server_wifi_WifiVendorHal_29156((AnonymousClass1AnswerBox) this.f9-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2) {
            this.f8-$f0 = obj;
            this.f9-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, StaLinkLayerStats staLinkLayerStats) {
            $m$0(wifiStatus, staLinkLayerStats);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$12 */
    final /* synthetic */ class AnonymousClass12 implements getValidFrequenciesForBandCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f10-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f11-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.f10-$f0).m35lambda$-com_android_server_wifi_WifiVendorHal_59881((AnonymousClass4AnswerBox) this.f11-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass12(Object obj, Object obj2) {
            this.f10-$f0 = obj;
            this.f11-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$13 */
    final /* synthetic */ class AnonymousClass13 implements getBackgroundScanCapabilitiesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f12-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f13-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f14-$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaBackgroundScanCapabilities arg1) {
            ((WifiVendorHal) this.f12-$f0).m29lambda$-com_android_server_wifi_WifiVendorHal_19593((ScanCapabilities) this.f13-$f1, (MutableBoolean) this.f14-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj, Object obj2, Object obj3) {
            this.f12-$f0 = obj;
            this.f13-$f1 = obj2;
            this.f14-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, StaBackgroundScanCapabilities staBackgroundScanCapabilities) {
            $m$0(wifiStatus, staBackgroundScanCapabilities);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$14 */
    final /* synthetic */ class AnonymousClass14 implements getDebugRxPacketFatesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f15-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f16-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f17-$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.f15-$f0).m43lambda$-com_android_server_wifi_WifiVendorHal_80760((RxFateReport[]) this.f16-$f1, (MutableBoolean) this.f17-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj, Object obj2, Object obj3) {
            this.f15-$f0 = obj;
            this.f16-$f1 = obj2;
            this.f17-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$15 */
    final /* synthetic */ class AnonymousClass15 implements getDebugTxPacketFatesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f18-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f19-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f20-$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.f18-$f0).m42lambda$-com_android_server_wifi_WifiVendorHal_78973((TxFateReport[]) this.f19-$f1, (MutableBoolean) this.f20-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj, Object obj2, Object obj3) {
            this.f18-$f0 = obj;
            this.f19-$f1 = obj2;
            this.f20-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$16 */
    final /* synthetic */ class AnonymousClass16 implements getRoamingCapabilitiesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f21-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f22-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f23-$f2;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaRoamingCapabilities arg1) {
            ((WifiVendorHal) this.f21-$f0).m45lambda$-com_android_server_wifi_WifiVendorHal_89828((RoamingCapabilities) this.f22-$f1, (MutableBoolean) this.f23-$f2, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass16(Object obj, Object obj2, Object obj3) {
            this.f21-$f0 = obj;
            this.f22-$f1 = obj2;
            this.f23-$f2 = obj3;
        }

        public final void onValues(WifiStatus wifiStatus, StaRoamingCapabilities staRoamingCapabilities) {
            $m$0(wifiStatus, staRoamingCapabilities);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$17 */
    final /* synthetic */ class AnonymousClass17 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f24-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f25-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f26-$f2;

        private final /* synthetic */ void $m$0() {
            ((ChipEventCallback) this.f24-$f0).m46x8c745a64((WifiDebugRingBufferStatus) this.f25-$f1, (ArrayList) this.f26-$f2);
        }

        public /* synthetic */ AnonymousClass17(Object obj, Object obj2, Object obj3) {
            this.f24-$f0 = obj;
            this.f25-$f1 = obj2;
            this.f26-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$1 */
    final /* synthetic */ class AnonymousClass1 implements IWifiApIface.getValidFrequenciesForBandCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f27-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f28-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.f27-$f0).m36lambda$-com_android_server_wifi_WifiVendorHal_60394((AnonymousClass4AnswerBox) this.f28-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.f27-$f0 = obj;
            this.f28-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$2 */
    final /* synthetic */ class AnonymousClass2 implements IWifiChip.getCapabilitiesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f29-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f30-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, int arg1) {
            ((WifiVendorHal) this.f29-$f0).m31lambda$-com_android_server_wifi_WifiVendorHal_36949((MutableInt) this.f30-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.f29-$f0 = obj;
            this.f30-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, int i) {
            $m$0(wifiStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$3 */
    final /* synthetic */ class AnonymousClass3 implements getDebugHostWakeReasonStatsCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f31-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f32-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, WifiDebugHostWakeReasonStats arg1) {
            ((WifiVendorHal) this.f31-$f0).m44lambda$-com_android_server_wifi_WifiVendorHal_88201((AnonymousClass9AnswerBox) this.f32-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.f31-$f0 = obj;
            this.f32-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, WifiDebugHostWakeReasonStats wifiDebugHostWakeReasonStats) {
            $m$0(wifiStatus, wifiDebugHostWakeReasonStats);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$4 */
    final /* synthetic */ class AnonymousClass4 implements getDebugRingBuffersStatusCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f33-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f34-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.f33-$f0).m39lambda$-com_android_server_wifi_WifiVendorHal_72141((AnonymousClass6AnswerBox) this.f34-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.f33-$f0 = obj;
            this.f34-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$5 */
    final /* synthetic */ class AnonymousClass5 implements requestDriverDebugDumpCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f35-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f36-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.f35-$f0).m41lambda$-com_android_server_wifi_WifiVendorHal_74106((AnonymousClass8AnswerBox) this.f36-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.f35-$f0 = obj;
            this.f36-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$6 */
    final /* synthetic */ class AnonymousClass6 implements requestFirmwareDebugDumpCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f37-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f38-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, ArrayList arg1) {
            ((WifiVendorHal) this.f37-$f0).m40lambda$-com_android_server_wifi_WifiVendorHal_73412((AnonymousClass7AnswerBox) this.f38-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.f37-$f0 = obj;
            this.f38-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, ArrayList arrayList) {
            $m$0(wifiStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$7 */
    final /* synthetic */ class AnonymousClass7 implements IWifiRttController.getCapabilitiesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f39-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f40-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, RttCapabilities arg1) {
            ((WifiVendorHal) this.f39-$f0).m33lambda$-com_android_server_wifi_WifiVendorHal_38745((AnonymousClass2AnswerBox) this.f40-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj, Object obj2) {
            this.f39-$f0 = obj;
            this.f40-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, RttCapabilities rttCapabilities) {
            $m$0(wifiStatus, rttCapabilities);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$8 */
    final /* synthetic */ class AnonymousClass8 implements getResponderInfoCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f41-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f42-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, RttResponder arg1) {
            ((WifiVendorHal) this.f41-$f0).m34lambda$-com_android_server_wifi_WifiVendorHal_55127((AnonymousClass3AnswerBox) this.f42-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.f41-$f0 = obj;
            this.f42-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, RttResponder rttResponder) {
            $m$0(wifiStatus, rttResponder);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA$9 */
    final /* synthetic */ class AnonymousClass9 implements getApfPacketFilterCapabilitiesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f43-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f44-$f1;

        private final /* synthetic */ void $m$0(WifiStatus arg0, StaApfPacketFilterCapabilities arg1) {
            ((WifiVendorHal) this.f43-$f0).m37lambda$-com_android_server_wifi_WifiVendorHal_62281((AnonymousClass5AnswerBox) this.f44-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj, Object obj2) {
            this.f43-$f0 = obj;
            this.f44-$f1 = obj2;
        }

        public final void onValues(WifiStatus wifiStatus, StaApfPacketFilterCapabilities staApfPacketFilterCapabilities) {
            $m$0(wifiStatus, staApfPacketFilterCapabilities);
        }
    }

    private final /* synthetic */ void $m$0(WifiStatus arg0, ChipDebugInfo arg1) {
        ((WifiVendorHal) this.f5-$f0).m38lambda$-com_android_server_wifi_WifiVendorHal_69198(arg0, arg1);
    }

    public /* synthetic */ -$Lambda$-8OLNNnyamdUDQS-yMRzScsmdRA(Object obj) {
        this.f5-$f0 = obj;
    }

    public final void onValues(WifiStatus wifiStatus, ChipDebugInfo chipDebugInfo) {
        $m$0(wifiStatus, chipDebugInfo);
    }
}
