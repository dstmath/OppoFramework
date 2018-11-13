package com.android.internal.telephony.euicc;

import android.service.euicc.GetDefaultDownloadableSubscriptionListResult;
import android.service.euicc.GetDownloadableSubscriptionMetadataResult;
import android.service.euicc.GetEuiccProfileInfoListResult;
import android.telephony.euicc.EuiccInfo;
import com.android.internal.telephony.euicc.EuiccConnector.BaseEuiccCommandCallback;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass10;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass11;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass2;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass3;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass4;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass5;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass6;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass7;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass8;
import com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass9;

final /* synthetic */ class -$Lambda$XGQhG5dbeONW7ErYP0MG74e0DLY implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f36-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f37-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f38-$f2;

    /* renamed from: com.android.internal.telephony.euicc.-$Lambda$XGQhG5dbeONW7ErYP0MG74e0DLY$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f39-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f40-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f41-$f2;

        private final /* synthetic */ void $m$0() {
            ((AnonymousClass10) this.f40-$f1).m34x7e90dcad((BaseEuiccCommandCallback) this.f41-$f2, this.f39-$f0);
        }

        private final /* synthetic */ void $m$1() {
            ((AnonymousClass11) this.f40-$f1).m35xb3778869((BaseEuiccCommandCallback) this.f41-$f2, this.f39-$f0);
        }

        private final /* synthetic */ void $m$2() {
            ((AnonymousClass3) this.f40-$f1).m38xbb9e07e9((BaseEuiccCommandCallback) this.f41-$f2, this.f39-$f0);
        }

        private final /* synthetic */ void $m$3() {
            ((AnonymousClass7) this.f40-$f1).m42x8f425250((BaseEuiccCommandCallback) this.f41-$f2, this.f39-$f0);
        }

        private final /* synthetic */ void $m$4() {
            ((AnonymousClass8) this.f40-$f1).m43xc4290232((BaseEuiccCommandCallback) this.f41-$f2, this.f39-$f0);
        }

        private final /* synthetic */ void $m$5() {
            ((AnonymousClass9) this.f40-$f1).m44xf90fb4f6((BaseEuiccCommandCallback) this.f41-$f2, this.f39-$f0);
        }

        public /* synthetic */ AnonymousClass1(byte b, int i, Object obj, Object obj2) {
            this.$id = b;
            this.f39-$f0 = i;
            this.f40-$f1 = obj;
            this.f41-$f2 = obj2;
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
                case (byte) 4:
                    $m$4();
                    return;
                case (byte) 5:
                    $m$5();
                    return;
                default:
                    throw new AssertionError();
            }
        }
    }

    private final /* synthetic */ void $m$0() {
        ((com.android.internal.telephony.euicc.EuiccConnector.ConnectedState.AnonymousClass1) this.f36-$f0).m36x51d09a3d((BaseEuiccCommandCallback) this.f37-$f1, (String) this.f38-$f2);
    }

    private final /* synthetic */ void $m$1() {
        ((AnonymousClass2) this.f36-$f0).m37x86b75166((BaseEuiccCommandCallback) this.f37-$f1, (GetDownloadableSubscriptionMetadataResult) this.f38-$f2);
    }

    private final /* synthetic */ void $m$2() {
        ((AnonymousClass4) this.f36-$f0).m39xf08e4030((BaseEuiccCommandCallback) this.f37-$f1, (GetEuiccProfileInfoListResult) this.f38-$f2);
    }

    private final /* synthetic */ void $m$3() {
        ((AnonymousClass5) this.f36-$f0).m40x2574fa36((BaseEuiccCommandCallback) this.f37-$f1, (GetDefaultDownloadableSubscriptionListResult) this.f38-$f2);
    }

    private final /* synthetic */ void $m$4() {
        ((AnonymousClass6) this.f36-$f0).m41x5a5ba310((BaseEuiccCommandCallback) this.f37-$f1, (EuiccInfo) this.f38-$f2);
    }

    public /* synthetic */ -$Lambda$XGQhG5dbeONW7ErYP0MG74e0DLY(byte b, Object obj, Object obj2, Object obj3) {
        this.$id = b;
        this.f36-$f0 = obj;
        this.f37-$f1 = obj2;
        this.f38-$f2 = obj3;
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
            case (byte) 4:
                $m$4();
                return;
            default:
                throw new AssertionError();
        }
    }
}
