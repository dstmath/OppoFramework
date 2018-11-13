package com.android.server.wm;

import java.util.Comparator;

final /* synthetic */ class -$Lambda$LEqle-ue9vesHjZva-SwvAvwBx8 implements Comparator {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f387-$f0;

    private final /* synthetic */ int $m$0(Object arg0, Object arg1) {
        return ((NonAppWindowContainers) this.f387-$f0).m244x641405e2((WindowToken) arg0, (WindowToken) arg1);
    }

    private final /* synthetic */ int $m$1(Object arg0, Object arg1) {
        return ((WindowToken) this.f387-$f0).m249lambda$-com_android_server_wm_WindowToken_3278((WindowState) arg0, (WindowState) arg1);
    }

    public /* synthetic */ -$Lambda$LEqle-ue9vesHjZva-SwvAvwBx8(byte b, Object obj) {
        this.$id = b;
        this.f387-$f0 = obj;
    }

    public final int compare(Object obj, Object obj2) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj, obj2);
            case (byte) 1:
                return $m$1(obj, obj2);
            default:
                throw new AssertionError();
        }
    }
}
