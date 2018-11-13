package com.android.server;

import android.content.Context;
import com.android.server.input.InputManagerService;
import com.android.server.media.MediaRouterService;
import com.android.server.net.NetworkPolicyManagerService;
import com.android.server.net.NetworkStatsService;
import com.android.server.oppo.OppoService;
import com.android.server.oppo.OppoUsageService;
import com.android.server.wm.WindowManagerService;
import com.oppo.media.OppoMultimediaService;
import com.oppo.roundcorner.OppoRoundCornerService;

final /* synthetic */ class -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo implements Runnable {
    public static final /* synthetic */ -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo $INST$0 = new -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo((byte) 0);
    public static final /* synthetic */ -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo $INST$1 = new -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo((byte) 1);
    public static final /* synthetic */ -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo $INST$2 = new -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo((byte) 2);
    public static final /* synthetic */ -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo $INST$3 = new -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo((byte) 3);
    private final /* synthetic */ byte $id;

    /* renamed from: com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f0-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f1-$f1;
        /* renamed from: -$f10 */
        private final /* synthetic */ Object f2-$f10;
        /* renamed from: -$f11 */
        private final /* synthetic */ Object f3-$f11;
        /* renamed from: -$f12 */
        private final /* synthetic */ Object f4-$f12;
        /* renamed from: -$f13 */
        private final /* synthetic */ Object f5-$f13;
        /* renamed from: -$f14 */
        private final /* synthetic */ Object f6-$f14;
        /* renamed from: -$f15 */
        private final /* synthetic */ Object f7-$f15;
        /* renamed from: -$f16 */
        private final /* synthetic */ Object f8-$f16;
        /* renamed from: -$f17 */
        private final /* synthetic */ Object f9-$f17;
        /* renamed from: -$f18 */
        private final /* synthetic */ Object f10-$f18;
        /* renamed from: -$f19 */
        private final /* synthetic */ Object f11-$f19;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f12-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f13-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f14-$f4;
        /* renamed from: -$f5 */
        private final /* synthetic */ Object f15-$f5;
        /* renamed from: -$f6 */
        private final /* synthetic */ Object f16-$f6;
        /* renamed from: -$f7 */
        private final /* synthetic */ Object f17-$f7;
        /* renamed from: -$f8 */
        private final /* synthetic */ Object f18-$f8;
        /* renamed from: -$f9 */
        private final /* synthetic */ Object f19-$f9;

        private final /* synthetic */ void $m$0() {
            ((SystemServer) this.f0-$f0).m10lambda$-com_android_server_SystemServer_104889((Context) this.f1-$f1, (WindowManagerService) this.f12-$f2, (NetworkScoreService) this.f13-$f3, (NetworkManagementService) this.f14-$f4, (NetworkPolicyManagerService) this.f15-$f5, (NetworkStatsService) this.f16-$f6, (ConnectivityService) this.f17-$f7, (LocationManagerService) this.f18-$f8, (CountryDetectorService) this.f19-$f9, (NetworkTimeUpdateService) this.f2-$f10, (OppoMultimediaService) this.f3-$f11, (OppoRoundCornerService) this.f4-$f12, (CommonTimeManagementService) this.f5-$f13, (InputManagerService) this.f6-$f14, (TelephonyRegistry) this.f7-$f15, (MediaRouterService) this.f8-$f16, (OppoUsageService) this.f9-$f17, (MmsServiceBroker) this.f10-$f18, (OppoService) this.f11-$f19);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3, Object obj4, Object obj5, Object obj6, Object obj7, Object obj8, Object obj9, Object obj10, Object obj11, Object obj12, Object obj13, Object obj14, Object obj15, Object obj16, Object obj17, Object obj18, Object obj19, Object obj20) {
            this.f0-$f0 = obj;
            this.f1-$f1 = obj2;
            this.f12-$f2 = obj3;
            this.f13-$f3 = obj4;
            this.f14-$f4 = obj5;
            this.f15-$f5 = obj6;
            this.f16-$f6 = obj7;
            this.f17-$f7 = obj8;
            this.f18-$f8 = obj9;
            this.f19-$f9 = obj10;
            this.f2-$f10 = obj11;
            this.f3-$f11 = obj12;
            this.f4-$f12 = obj13;
            this.f5-$f13 = obj14;
            this.f6-$f14 = obj15;
            this.f7-$f15 = obj16;
            this.f8-$f16 = obj17;
            this.f9-$f17 = obj18;
            this.f10-$f18 = obj19;
            this.f11-$f19 = obj20;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.-$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f20-$f0;

        private final /* synthetic */ void $m$0() {
            ((SystemServer) this.f20-$f0).m11lambda$-com_android_server_SystemServer_105794();
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f20-$f0 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private /* synthetic */ -$Lambda$T7cKu_OKm_Fk2kBNthmo_uUJTSo(byte b) {
        this.$id = b;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            case (byte) 2:
                $m$2();
                return;
            case (byte) 3:
                $m$3();
                return;
            default:
                throw new AssertionError();
        }
    }
}
