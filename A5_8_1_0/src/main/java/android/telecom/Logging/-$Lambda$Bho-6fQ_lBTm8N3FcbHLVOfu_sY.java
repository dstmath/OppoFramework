package android.telecom.Logging;

import android.telecom.Logging.EventManager.EventRecord;
import java.util.Comparator;
import java.util.function.Consumer;

final /* synthetic */ class -$Lambda$Bho-6fQ_lBTm8N3FcbHLVOfu_sY implements Comparator {
    public static final /* synthetic */ -$Lambda$Bho-6fQ_lBTm8N3FcbHLVOfu_sY $INST$0 = new -$Lambda$Bho-6fQ_lBTm8N3FcbHLVOfu_sY();

    /* renamed from: android.telecom.Logging.-$Lambda$Bho-6fQ_lBTm8N3FcbHLVOfu_sY$1 */
    final /* synthetic */ class AnonymousClass1 implements Consumer {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f44-$f0;

        private final /* synthetic */ void $m$0(Object arg0) {
            ((EventManager) this.f44-$f0).m25lambda$-android_telecom_Logging_EventManager_12735((EventRecord) arg0);
        }

        public /* synthetic */ AnonymousClass1(Object obj) {
            this.f44-$f0 = obj;
        }

        public final void accept(Object obj) {
            $m$0(obj);
        }
    }

    private /* synthetic */ -$Lambda$Bho-6fQ_lBTm8N3FcbHLVOfu_sY() {
    }

    public final int compare(Object obj, Object obj2) {
        return $m$0(obj, obj2);
    }
}
