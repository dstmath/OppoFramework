package com.android.server.connectivity.tethering;

import android.hardware.tetheroffload.control.V1_0.IOffloadControl.addDownstreamCallback;
import android.hardware.tetheroffload.control.V1_0.IOffloadControl.getForwardedStatsCallback;
import android.hardware.tetheroffload.control.V1_0.IOffloadControl.initOffloadCallback;
import android.hardware.tetheroffload.control.V1_0.IOffloadControl.removeDownstreamCallback;
import android.hardware.tetheroffload.control.V1_0.IOffloadControl.setDataLimitCallback;
import android.hardware.tetheroffload.control.V1_0.IOffloadControl.setLocalPrefixesCallback;
import android.hardware.tetheroffload.control.V1_0.IOffloadControl.setUpstreamParametersCallback;
import android.hardware.tetheroffload.control.V1_0.IOffloadControl.stopOffloadCallback;
import android.hardware.tetheroffload.control.V1_0.NatTimeoutUpdate;
import com.android.server.connectivity.tethering.OffloadHardwareInterface.ForwardedStats;

final /* synthetic */ class -$Lambda$LVMU292iEsklodYmav2xkNUv4MU implements addDownstreamCallback {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f201-$f0;

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$1 */
    final /* synthetic */ class AnonymousClass1 implements getForwardedStatsCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f202-$f0;

        private final /* synthetic */ void $m$0(long arg0, long arg1) {
            OffloadHardwareInterface.m62x698d6633((ForwardedStats) this.f202-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f202-$f0 = obj;
        }

        public final void onValues(long j, long j2) {
            $m$0(j, j2);
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$2 */
    final /* synthetic */ class AnonymousClass2 implements initOffloadCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f203-$f0;

        private final /* synthetic */ void $m$0(boolean arg0, String arg1) {
            OffloadHardwareInterface.m61x698ce745((CbResults) this.f203-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass2(Object obj) {
            this.f203-$f0 = obj;
        }

        public final void onValues(boolean z, String str) {
            $m$0(z, str);
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$3 */
    final /* synthetic */ class AnonymousClass3 implements removeDownstreamCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f204-$f0;

        private final /* synthetic */ void $m$0(boolean arg0, String arg1) {
            OffloadHardwareInterface.m67x698f2cc8((CbResults) this.f204-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass3(Object obj) {
            this.f204-$f0 = obj;
        }

        public final void onValues(boolean z, String str) {
            $m$0(z, str);
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$4 */
    final /* synthetic */ class AnonymousClass4 implements setDataLimitCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f205-$f0;

        private final /* synthetic */ void $m$0(boolean arg0, String arg1) {
            OffloadHardwareInterface.m64x698e3561((CbResults) this.f205-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass4(Object obj) {
            this.f205-$f0 = obj;
        }

        public final void onValues(boolean z, String str) {
            $m$0(z, str);
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$5 */
    final /* synthetic */ class AnonymousClass5 implements setLocalPrefixesCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f206-$f0;

        private final /* synthetic */ void $m$0(boolean arg0, String arg1) {
            OffloadHardwareInterface.m63x698dcf84((CbResults) this.f206-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass5(Object obj) {
            this.f206-$f0 = obj;
        }

        public final void onValues(boolean z, String str) {
            $m$0(z, str);
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$6 */
    final /* synthetic */ class AnonymousClass6 implements setUpstreamParametersCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f207-$f0;

        private final /* synthetic */ void $m$0(boolean arg0, String arg1) {
            OffloadHardwareInterface.m65x698ead23((CbResults) this.f207-$f0, arg0, arg1);
        }

        public /* synthetic */ AnonymousClass6(Object obj) {
            this.f207-$f0 = obj;
        }

        public final void onValues(boolean z, String str) {
            $m$0(z, str);
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$7 */
    final /* synthetic */ class AnonymousClass7 implements stopOffloadCallback {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f208-$f0;

        private final /* synthetic */ void $m$0(boolean arg0, String arg1) {
            ((OffloadHardwareInterface) this.f208-$f0).m68x698cfa0f(arg0, arg1);
        }

        public /* synthetic */ AnonymousClass7(Object obj) {
            this.f208-$f0 = obj;
        }

        public final void onValues(boolean z, String str) {
            $m$0(z, str);
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$8 */
    final /* synthetic */ class AnonymousClass8 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f209-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f210-$f1;

        private final /* synthetic */ void $m$0() {
            ((TetheringOffloadCallback) this.f209-$f0).m70x20a3c1d7((NatTimeoutUpdate) this.f210-$f1);
        }

        public /* synthetic */ AnonymousClass8(Object obj, Object obj2) {
            this.f209-$f0 = obj;
            this.f210-$f1 = obj2;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.connectivity.tethering.-$Lambda$LVMU292iEsklodYmav2xkNUv4MU$9 */
    final /* synthetic */ class AnonymousClass9 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f211-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f212-$f1;

        private final /* synthetic */ void $m$0() {
            ((TetheringOffloadCallback) this.f212-$f1).m69x20a349f5(this.f211-$f0);
        }

        public /* synthetic */ AnonymousClass9(int i, Object obj) {
            this.f211-$f0 = i;
            this.f212-$f1 = obj;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0(boolean arg0, String arg1) {
        OffloadHardwareInterface.m66x698ec6cc((CbResults) this.f201-$f0, arg0, arg1);
    }

    public /* synthetic */ -$Lambda$LVMU292iEsklodYmav2xkNUv4MU(Object obj) {
        this.f201-$f0 = obj;
    }

    public final void onValues(boolean z, String str) {
        $m$0(z, str);
    }
}
