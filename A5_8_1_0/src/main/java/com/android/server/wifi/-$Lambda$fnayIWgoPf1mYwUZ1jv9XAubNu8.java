package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicant.getInterfaceCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicant.listInterfacesCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.addNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.getNetworkCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantIface.listNetworksCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.getMacAddressCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaIface.startWpsPinDisplayCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import java.util.ArrayList;

final /* synthetic */ class -$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8 implements getInterfaceCallback {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f48-$f0;

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$1 */
    final /* synthetic */ class AnonymousClass1 implements listInterfacesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f49-$f0;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            SupplicantStaIfaceHal.m48lambda$-com_android_server_wifi_SupplicantStaIfaceHal_13715((ArrayList) this.f49-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f49-$f0 = obj;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$2 */
    final /* synthetic */ class AnonymousClass2 implements addNetworkCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f50-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f51-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantNetwork arg1) {
            ((SupplicantStaIfaceHal) this.f50-$f0).m50lambda$-com_android_server_wifi_SupplicantStaIfaceHal_34821((Mutable) this.f51-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.f50-$f0 = obj;
            this.f51-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
            $m$0(supplicantStatus, iSupplicantNetwork);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$3 */
    final /* synthetic */ class AnonymousClass3 implements getNetworkCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f52-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f53-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantNetwork arg1) {
            ((SupplicantStaIfaceHal) this.f52-$f0).m51lambda$-com_android_server_wifi_SupplicantStaIfaceHal_39168((Mutable) this.f53-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.f52-$f0 = obj;
            this.f53-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ISupplicantNetwork iSupplicantNetwork) {
            $m$0(supplicantStatus, iSupplicantNetwork);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$4 */
    final /* synthetic */ class AnonymousClass4 implements listNetworksCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f54-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f55-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaIfaceHal) this.f54-$f0).m52lambda$-com_android_server_wifi_SupplicantStaIfaceHal_41615((Mutable) this.f55-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.f54-$f0 = obj;
            this.f55-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$5 */
    final /* synthetic */ class AnonymousClass5 implements getMacAddressCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f56-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f57-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            ((SupplicantStaIfaceHal) this.f56-$f0).m53lambda$-com_android_server_wifi_SupplicantStaIfaceHal_59398((Mutable) this.f57-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.f56-$f0 = obj;
            this.f57-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8$6 */
    final /* synthetic */ class AnonymousClass6 implements startWpsPinDisplayCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f58-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f59-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaIfaceHal) this.f58-$f0).m56lambda$-com_android_server_wifi_SupplicantStaIfaceHal_74445((Mutable) this.f59-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.f58-$f0 = obj;
            this.f59-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    private final /* synthetic */ void $m$0(SupplicantStatus arg0, ISupplicantIface arg1) {
        SupplicantStaIfaceHal.m49lambda$-com_android_server_wifi_SupplicantStaIfaceHal_14826((Mutable) this.f48-$f0, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$fnayIWgoPf1mYwUZ1jv9XAubNu8(Object obj) {
        this.f48-$f0 = obj;
    }

    public final void onValues(SupplicantStatus supplicantStatus, ISupplicantIface iSupplicantIface) {
        $m$0(supplicantStatus, iSupplicantIface);
    }
}
