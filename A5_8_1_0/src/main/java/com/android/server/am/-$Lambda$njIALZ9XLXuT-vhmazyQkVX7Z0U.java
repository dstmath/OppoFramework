package com.android.server.am;

import android.app.PictureInPictureParams;
import com.android.internal.os.ProcessCpuTracker.FilterStats;
import com.android.internal.os.ProcessCpuTracker.Stats;

final /* synthetic */ class -$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U implements FilterStats {
    public static final /* synthetic */ -$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U $INST$0 = new -$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U((byte) 0);
    public static final /* synthetic */ -$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U $INST$1 = new -$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U((byte) 1);
    private final /* synthetic */ byte $id;

    /* renamed from: com.android.server.am.-$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ Object f29-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f30-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f31-$f2;

        private final /* synthetic */ void $m$0() {
            ((ActivityManagerService) this.f29-$f0).m25lambda$-com_android_server_am_ActivityManagerService_410880((ActivityRecord) this.f30-$f1, (PictureInPictureParams) this.f31-$f2);
        }

        public /* synthetic */ AnonymousClass1(Object obj, Object obj2, Object obj3) {
            this.f29-$f0 = obj;
            this.f30-$f1 = obj2;
            this.f31-$f2 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private /* synthetic */ -$Lambda$njIALZ9XLXuT-vhmazyQkVX7Z0U(byte b) {
        this.$id = b;
    }

    public final boolean needed(Stats stats) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(stats);
            case (byte) 1:
                return $m$1(stats);
            default:
                throw new AssertionError();
        }
    }
}
