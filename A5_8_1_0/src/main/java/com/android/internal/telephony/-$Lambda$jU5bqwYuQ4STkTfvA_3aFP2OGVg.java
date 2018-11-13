package com.android.internal.telephony;

import android.telephony.SubscriptionInfo;
import java.util.Comparator;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg implements Comparator {
    public static final /* synthetic */ -$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg $INST$0 = new -$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg();

    /* renamed from: com.android.internal.telephony.-$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg$1 */
    final /* synthetic */ class AnonymousClass1 implements Predicate {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f0-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f1-$f1;

        private final /* synthetic */ boolean $m$0(Object arg0) {
            return ((SubscriptionController) this.f0-$f0).m1x951b4c18((String) this.f1-$f1, (SubscriptionInfo) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2) {
            this.f0-$f0 = obj;
            this.f1-$f1 = obj2;
        }

        public final boolean test(Object obj) {
            return $m$0(obj);
        }
    }

    private /* synthetic */ -$Lambda$jU5bqwYuQ4STkTfvA_3aFP2OGVg() {
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
