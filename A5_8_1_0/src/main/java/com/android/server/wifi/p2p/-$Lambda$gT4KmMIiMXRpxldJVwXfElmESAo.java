package com.android.server.wifi.p2p;

import android.hardware.wifi.supplicant.V1_0.ISupplicant.getInterfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.getNameCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.getNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.listNetworksCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.connectCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.createNfcHandoverRequestMessageCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.createNfcHandoverSelectMessageCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.getDeviceAddressCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.getGroupCapabilityCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.getSsidCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.requestServiceDiscoveryCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pIface.startWpsPinDisplayCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.getBssidCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.getClientListCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.isCurrentCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantP2pNetwork.isGoCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.os.IHwBinder.DeathRecipient;
import java.util.ArrayList;
import java.util.function.Function;
import vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantP2pIface.addP2pNetworkCallback;
import vendor.oppo.hardware.wifi.supplicant.V1_0.IOppoSupplicantP2pIface.p2pConnectCallback;

final /* synthetic */ class -$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo implements Function {
    public static final /* synthetic */ -$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo $INST$0 = new -$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo();

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$10 */
    final /* synthetic */ class AnonymousClass10 implements getGroupCapabilityCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f190-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantResult) this.f190-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, Integer.valueOf(arg1));
        }

        public /* synthetic */ AnonymousClass10(Object obj) {
            this.f190-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$11 */
    final /* synthetic */ class AnonymousClass11 implements getSsidCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f191-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            SupplicantP2pIfaceHal.m103lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_45853((SupplicantResult) this.f191-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj) {
            this.f191-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$12 */
    final /* synthetic */ class AnonymousClass12 implements requestServiceDiscoveryCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f192-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, long arg1) {
            ((SupplicantResult) this.f192-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, new Long(arg1));
        }

        public /* synthetic */ AnonymousClass12(Object obj) {
            this.f192-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, long j) {
            $m$0(supplicantStatus, j);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$13 */
    final /* synthetic */ class AnonymousClass13 implements startWpsPinDisplayCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f193-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantResult) this.f193-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj) {
            this.f193-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$14 */
    final /* synthetic */ class AnonymousClass14 implements getBssidCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f194-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            ((SupplicantResult) this.f194-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj) {
            this.f194-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$15 */
    final /* synthetic */ class AnonymousClass15 implements getClientListCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f195-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.f195-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj) {
            this.f195-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$16 */
    final /* synthetic */ class AnonymousClass16 implements ISupplicantP2pNetwork.getSsidCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f196-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.f196-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass16(Object obj) {
            this.f196-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$17 */
    final /* synthetic */ class AnonymousClass17 implements isCurrentCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f197-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, boolean arg1) {
            ((SupplicantResult) this.f197-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, Boolean.valueOf(arg1));
        }

        public /* synthetic */ AnonymousClass17(Object obj) {
            this.f197-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
            $m$0(supplicantStatus, z);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$18 */
    final /* synthetic */ class AnonymousClass18 implements isGoCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f198-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, boolean arg1) {
            ((SupplicantResult) this.f198-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, Boolean.valueOf(arg1));
        }

        public /* synthetic */ AnonymousClass18(Object obj) {
            this.f198-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
            $m$0(supplicantStatus, z);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$19 */
    final /* synthetic */ class AnonymousClass19 implements DeathRecipient {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f199-$f0;

        private final /* synthetic */ void $m$0(long arg0) {
            ((SupplicantP2pIfaceHal) this.f199-$f0).m104lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_5055(arg0);
        }

        private final /* synthetic */ void $m$1(long arg0) {
            ((SupplicantP2pIfaceHal) this.f199-$f0).m105lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_5445(arg0);
        }

        public /* synthetic */ AnonymousClass19(byte b, Object obj) {
            this.$id = b;
            this.f199-$f0 = obj;
        }

        public final void serviceDied(long j) {
            switch (this.$id) {
                case (byte) 0:
                    $m$0(j);
                    return;
                case (byte) 1:
                    $m$1(j);
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$1 */
    final /* synthetic */ class AnonymousClass1 implements getInterfaceCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f200-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantIface arg1) {
            SupplicantP2pIfaceHal.m101lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_11664((SupplicantResult) this.f200-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f200-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
            $m$0(supplicantStatus, iSupplicantIface);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$20 */
    final /* synthetic */ class AnonymousClass20 implements addP2pNetworkCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f201-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantResult) this.f201-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, Integer.valueOf(arg1));
        }

        public /* synthetic */ AnonymousClass20(Object obj) {
            this.f201-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$21 */
    final /* synthetic */ class AnonymousClass21 implements p2pConnectCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f202-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantResult) this.f202-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass21(Object obj) {
            this.f202-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$2 */
    final /* synthetic */ class AnonymousClass2 implements listInterfacesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f203-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            SupplicantP2pIfaceHal.m100lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_10558((ArrayList) this.f203-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f203-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$3 */
    final /* synthetic */ class AnonymousClass3 implements getNameCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f204-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantResult) this.f204-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.f204-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$4 */
    final /* synthetic */ class AnonymousClass4 implements getNetworkCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f205-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantNetwork arg1) {
            ((SupplicantResult) this.f205-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.f205-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
            $m$0(supplicantStatus, iSupplicantNetwork);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$5 */
    final /* synthetic */ class AnonymousClass5 implements listNetworksCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f206-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.f206-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.f206-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$6 */
    final /* synthetic */ class AnonymousClass6 implements connectCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f207-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantResult) this.f207-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.f207-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$7 */
    final /* synthetic */ class AnonymousClass7 implements createNfcHandoverRequestMessageCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f208-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.f208-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj) {
            this.f208-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$8 */
    final /* synthetic */ class AnonymousClass8 implements createNfcHandoverSelectMessageCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f209-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantResult) this.f209-$f0).lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_93462(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj) {
            this.f209-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.p2p.-$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo$9 */
    final /* synthetic */ class AnonymousClass9 implements getDeviceAddressCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f210-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            SupplicantP2pIfaceHal.m102lambda$-com_android_server_wifi_p2p_SupplicantP2pIfaceHal_44221((SupplicantResult) this.f210-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj) {
            this.f210-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    private /* synthetic */ -$Lambda$gT4KmMIiMXRpxldJVwXfElmESAo() {
    }

    public final Object apply(Object obj) {
        return $m$0(obj);
    }
}
