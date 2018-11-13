package com.android.server.wifi;

import android.hardware.wifi.supplicant.V1_0.ISupplicantNetwork.getIdCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getAuthAlgCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getBssidCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapAltSubjectMatchCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapAnonymousIdentityCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapCACertCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapCAPathCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapClientCertCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapDomainSuffixMatchCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapEngineCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapEngineIDCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapIdentityCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapMethodCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapPasswordCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapPhase2MethodCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapPrivateKeyIdCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getEapSubjectMatchCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getGroupCipherCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getIdStrCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getKeyMgmtCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getPairwiseCipherCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getProtoCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getPskCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getPskPassphraseCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getRequirePmfCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getScanSsidCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getSsidCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getWepKeyCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getWepTxKeyIdxCallback;
import android.hardware.wifi.supplicant.V1_0.ISupplicantStaNetwork.getWpsNfcConfigurationTokenCallback;
import android.hardware.wifi.supplicant.V1_0.SupplicantStatus;
import android.util.MutableBoolean;
import java.util.ArrayList;
import vendor.qti.hardware.wifi.supplicant.V1_0.ISupplicantVendorStaNetwork.getWapiCertSelCallback;
import vendor.qti.hardware.wifi.supplicant.V1_0.ISupplicantVendorStaNetwork.getWapiCertSelModeCallback;
import vendor.qti.hardware.wifi.supplicant.V1_0.ISupplicantVendorStaNetwork.getWapiPskCallback;
import vendor.qti.hardware.wifi.supplicant.V1_0.ISupplicantVendorStaNetwork.getWapiPskTypeCallback;

final /* synthetic */ class -$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE implements getIdCallback {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f111-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f112-$f1;

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$10 */
    final /* synthetic */ class AnonymousClass10 implements getEapEngineIDCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f113-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f114-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f113-$f0).m67lambda$-com_android_server_wifi_SupplicantStaNetworkHal_109118((MutableBoolean) this.f114-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass10(Object obj, Object obj2) {
            this.f113-$f0 = obj;
            this.f114-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$11 */
    final /* synthetic */ class AnonymousClass11 implements getEapIdentityCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f115-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f116-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaNetworkHal) this.f115-$f0).m91lambda$-com_android_server_wifi_SupplicantStaNetworkHal_98596((MutableBoolean) this.f116-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass11(Object obj, Object obj2) {
            this.f115-$f0 = obj;
            this.f116-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$12 */
    final /* synthetic */ class AnonymousClass12 implements getEapMethodCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f117-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f118-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f117-$f0).m89lambda$-com_android_server_wifi_SupplicantStaNetworkHal_96592((MutableBoolean) this.f118-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass12(Object obj, Object obj2) {
            this.f117-$f0 = obj;
            this.f118-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$13 */
    final /* synthetic */ class AnonymousClass13 implements getEapPasswordCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f119-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f120-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaNetworkHal) this.f119-$f0).m59lambda$-com_android_server_wifi_SupplicantStaNetworkHal_101146((MutableBoolean) this.f120-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass13(Object obj, Object obj2) {
            this.f119-$f0 = obj;
            this.f120-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$14 */
    final /* synthetic */ class AnonymousClass14 implements getEapPhase2MethodCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f121-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f122-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f121-$f0).m90lambda$-com_android_server_wifi_SupplicantStaNetworkHal_97597((MutableBoolean) this.f122-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass14(Object obj, Object obj2) {
            this.f121-$f0 = obj;
            this.f122-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$15 */
    final /* synthetic */ class AnonymousClass15 implements getEapPrivateKeyIdCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f123-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f124-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f123-$f0).m63lambda$-com_android_server_wifi_SupplicantStaNetworkHal_105095((MutableBoolean) this.f124-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass15(Object obj, Object obj2) {
            this.f123-$f0 = obj;
            this.f124-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$16 */
    final /* synthetic */ class AnonymousClass16 implements getEapSubjectMatchCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f125-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f126-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f125-$f0).m64lambda$-com_android_server_wifi_SupplicantStaNetworkHal_106101((MutableBoolean) this.f126-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass16(Object obj, Object obj2) {
            this.f125-$f0 = obj;
            this.f126-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$17 */
    final /* synthetic */ class AnonymousClass17 implements getGroupCipherCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f127-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f128-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f127-$f0).m82lambda$-com_android_server_wifi_SupplicantStaNetworkHal_89590((MutableBoolean) this.f128-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass17(Object obj, Object obj2) {
            this.f127-$f0 = obj;
            this.f128-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$18 */
    final /* synthetic */ class AnonymousClass18 implements getIdStrCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f129-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f130-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f129-$f0).m69lambda$-com_android_server_wifi_SupplicantStaNetworkHal_111098((MutableBoolean) this.f130-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass18(Object obj, Object obj2) {
            this.f129-$f0 = obj;
            this.f130-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$19 */
    final /* synthetic */ class AnonymousClass19 implements getKeyMgmtCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f131-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f132-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f131-$f0).m79lambda$-com_android_server_wifi_SupplicantStaNetworkHal_86635((MutableBoolean) this.f132-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass19(Object obj, Object obj2) {
            this.f131-$f0 = obj;
            this.f132-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$1 */
    final /* synthetic */ class AnonymousClass1 implements getAuthAlgCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f133-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f134-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f133-$f0).m81lambda$-com_android_server_wifi_SupplicantStaNetworkHal_88585((MutableBoolean) this.f134-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.f133-$f0 = obj;
            this.f134-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$20 */
    final /* synthetic */ class AnonymousClass20 implements getPairwiseCipherCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f135-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f136-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f135-$f0).m83lambda$-com_android_server_wifi_SupplicantStaNetworkHal_90616((MutableBoolean) this.f136-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass20(Object obj, Object obj2) {
            this.f135-$f0 = obj;
            this.f136-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$21 */
    final /* synthetic */ class AnonymousClass21 implements getProtoCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f137-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f138-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f137-$f0).m80lambda$-com_android_server_wifi_SupplicantStaNetworkHal_87622((MutableBoolean) this.f138-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass21(Object obj, Object obj2) {
            this.f137-$f0 = obj;
            this.f138-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$22 */
    final /* synthetic */ class AnonymousClass22 implements getPskCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f139-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f140-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            ((SupplicantStaNetworkHal) this.f139-$f0).m85lambda$-com_android_server_wifi_SupplicantStaNetworkHal_92618((MutableBoolean) this.f140-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass22(Object obj, Object obj2) {
            this.f139-$f0 = obj;
            this.f140-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$23 */
    final /* synthetic */ class AnonymousClass23 implements getPskPassphraseCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f141-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f142-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f141-$f0).m84lambda$-com_android_server_wifi_SupplicantStaNetworkHal_91648((MutableBoolean) this.f142-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass23(Object obj, Object obj2) {
            this.f141-$f0 = obj;
            this.f142-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$24 */
    final /* synthetic */ class AnonymousClass24 implements getRequirePmfCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f143-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f144-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, boolean arg1) {
            ((SupplicantStaNetworkHal) this.f143-$f0).m88lambda$-com_android_server_wifi_SupplicantStaNetworkHal_95598((MutableBoolean) this.f144-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass24(Object obj, Object obj2) {
            this.f143-$f0 = obj;
            this.f144-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
            $m$0(supplicantStatus, z);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$25 */
    final /* synthetic */ class AnonymousClass25 implements getScanSsidCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f145-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f146-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, boolean arg1) {
            ((SupplicantStaNetworkHal) this.f145-$f0).m78lambda$-com_android_server_wifi_SupplicantStaNetworkHal_85649((MutableBoolean) this.f146-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass25(Object obj, Object obj2) {
            this.f145-$f0 = obj;
            this.f146-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
            $m$0(supplicantStatus, z);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$26 */
    final /* synthetic */ class AnonymousClass26 implements getSsidCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f147-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f148-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaNetworkHal) this.f147-$f0).m76lambda$-com_android_server_wifi_SupplicantStaNetworkHal_83673((MutableBoolean) this.f148-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass26(Object obj, Object obj2) {
            this.f147-$f0 = obj;
            this.f148-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$27 */
    final /* synthetic */ class AnonymousClass27 implements getWepKeyCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f149-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f150-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaNetworkHal) this.f149-$f0).m86lambda$-com_android_server_wifi_SupplicantStaNetworkHal_93578((MutableBoolean) this.f150-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass27(Object obj, Object obj2) {
            this.f149-$f0 = obj;
            this.f150-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$28 */
    final /* synthetic */ class AnonymousClass28 implements getWepTxKeyIdxCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f151-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f152-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f151-$f0).m87lambda$-com_android_server_wifi_SupplicantStaNetworkHal_94606((MutableBoolean) this.f152-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass28(Object obj, Object obj2) {
            this.f151-$f0 = obj;
            this.f152-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$29 */
    final /* synthetic */ class AnonymousClass29 implements getWpsNfcConfigurationTokenCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f153-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f154-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaNetworkHal) this.f153-$f0).m70lambda$-com_android_server_wifi_SupplicantStaNetworkHal_124553((Mutable) this.f154-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass29(Object obj, Object obj2) {
            this.f153-$f0 = obj;
            this.f154-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$2 */
    final /* synthetic */ class AnonymousClass2 implements getBssidCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f155-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f156-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, byte[] arg1) {
            ((SupplicantStaNetworkHal) this.f155-$f0).m77lambda$-com_android_server_wifi_SupplicantStaNetworkHal_84661((MutableBoolean) this.f156-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj, Object obj2) {
            this.f155-$f0 = obj;
            this.f156-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, byte[] bArr) {
            $m$0(supplicantStatus, bArr);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$30 */
    final /* synthetic */ class AnonymousClass30 implements getWapiCertSelCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f157-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f158-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f157-$f0).m75lambda$-com_android_server_wifi_SupplicantStaNetworkHal_82673((MutableBoolean) this.f158-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass30(Object obj, Object obj2) {
            this.f157-$f0 = obj;
            this.f158-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$31 */
    final /* synthetic */ class AnonymousClass31 implements getWapiCertSelModeCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f159-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f160-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f159-$f0).m74lambda$-com_android_server_wifi_SupplicantStaNetworkHal_81709((MutableBoolean) this.f160-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass31(Object obj, Object obj2) {
            this.f159-$f0 = obj;
            this.f160-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$32 */
    final /* synthetic */ class AnonymousClass32 implements getWapiPskCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f161-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f162-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f161-$f0).m73lambda$-com_android_server_wifi_SupplicantStaNetworkHal_80695((MutableBoolean) this.f162-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass32(Object obj, Object obj2) {
            this.f161-$f0 = obj;
            this.f162-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$33 */
    final /* synthetic */ class AnonymousClass33 implements getWapiPskTypeCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f163-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f164-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
            ((SupplicantStaNetworkHal) this.f163-$f0).m72lambda$-com_android_server_wifi_SupplicantStaNetworkHal_79696((MutableBoolean) this.f164-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass33(Object obj, Object obj2) {
            this.f163-$f0 = obj;
            this.f164-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, int i) {
            $m$0(supplicantStatus, i);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$3 */
    final /* synthetic */ class AnonymousClass3 implements getEapAltSubjectMatchCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f165-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f166-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f165-$f0).m65lambda$-com_android_server_wifi_SupplicantStaNetworkHal_107122((MutableBoolean) this.f166-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj, Object obj2) {
            this.f165-$f0 = obj;
            this.f166-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$4 */
    final /* synthetic */ class AnonymousClass4 implements getEapAnonymousIdentityCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f167-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f168-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, ArrayList arg1) {
            ((SupplicantStaNetworkHal) this.f167-$f0).m92lambda$-com_android_server_wifi_SupplicantStaNetworkHal_99634((MutableBoolean) this.f168-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj, Object obj2) {
            this.f167-$f0 = obj;
            this.f168-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, ArrayList arrayList) {
            $m$0(supplicantStatus, arrayList);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$5 */
    final /* synthetic */ class AnonymousClass5 implements getEapCACertCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f169-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f170-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f169-$f0).m60lambda$-com_android_server_wifi_SupplicantStaNetworkHal_102151((MutableBoolean) this.f170-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj, Object obj2) {
            this.f169-$f0 = obj;
            this.f170-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$6 */
    final /* synthetic */ class AnonymousClass6 implements getEapCAPathCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f171-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f172-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f171-$f0).m61lambda$-com_android_server_wifi_SupplicantStaNetworkHal_103113((MutableBoolean) this.f172-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj, Object obj2) {
            this.f171-$f0 = obj;
            this.f172-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$7 */
    final /* synthetic */ class AnonymousClass7 implements getEapClientCertCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f173-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f174-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f173-$f0).m62lambda$-com_android_server_wifi_SupplicantStaNetworkHal_104087((MutableBoolean) this.f174-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj, Object obj2) {
            this.f173-$f0 = obj;
            this.f174-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$8 */
    final /* synthetic */ class AnonymousClass8 implements getEapDomainSuffixMatchCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f175-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f176-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, String arg1) {
            ((SupplicantStaNetworkHal) this.f175-$f0).m68lambda$-com_android_server_wifi_SupplicantStaNetworkHal_110111((MutableBoolean) this.f176-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.f175-$f0 = obj;
            this.f176-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, String str) {
            $m$0(supplicantStatus, str);
        }
    }

    /* renamed from: com.android.server.wifi.-$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE$9 */
    final /* synthetic */ class AnonymousClass9 implements getEapEngineCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f177-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f178-$f1;

        private final /* synthetic */ void $m$0(SupplicantStatus arg0, boolean arg1) {
            ((SupplicantStaNetworkHal) this.f177-$f0).m66lambda$-com_android_server_wifi_SupplicantStaNetworkHal_108119((MutableBoolean) this.f178-$f1, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass9(Object obj, Object obj2) {
            this.f177-$f0 = obj;
            this.f178-$f1 = obj2;
        }

        public final void onValues(SupplicantStatus supplicantStatus, boolean z) {
            $m$0(supplicantStatus, z);
        }
    }

    private final /* synthetic */ void $m$0(SupplicantStatus arg0, int arg1) {
        ((SupplicantStaNetworkHal) this.f111-$f0).m71lambda$-com_android_server_wifi_SupplicantStaNetworkHal_54250((MutableBoolean) this.f112-$f1, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$red2_TYcnQD-fzDZf_XpXgVlYqE(Object obj, Object obj2) {
        this.f111-$f0 = obj;
        this.f112-$f1 = obj2;
    }

    public final void onValues(SupplicantStatus supplicantStatus, int i) {
        $m$0(supplicantStatus, i);
    }
}
