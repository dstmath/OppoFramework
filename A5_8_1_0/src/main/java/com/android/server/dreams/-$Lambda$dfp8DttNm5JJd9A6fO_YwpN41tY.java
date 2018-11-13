package com.android.server.dreams;

import android.content.ComponentName;
import android.os.Binder;
import android.os.PowerManager.WakeLock;

final /* synthetic */ class -$Lambda$dfp8DttNm5JJd9A6fO_YwpN41tY implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ boolean f236-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ boolean f237-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ int f238-$f2;
    /* renamed from: -$f3 */
    private final /* synthetic */ Object f239-$f3;
    /* renamed from: -$f4 */
    private final /* synthetic */ Object f240-$f4;
    /* renamed from: -$f5 */
    private final /* synthetic */ Object f241-$f5;
    /* renamed from: -$f6 */
    private final /* synthetic */ Object f242-$f6;

    private final /* synthetic */ void $m$0() {
        ((DreamManagerService) this.f239-$f3).m169lambda$-com_android_server_dreams_DreamManagerService_15097((Binder) this.f240-$f4, (ComponentName) this.f241-$f5, this.f236-$f0, this.f237-$f1, this.f238-$f2, (WakeLock) this.f242-$f6);
    }

    public /* synthetic */ -$Lambda$dfp8DttNm5JJd9A6fO_YwpN41tY(boolean z, boolean z2, int i, Object obj, Object obj2, Object obj3, Object obj4) {
        this.f236-$f0 = z;
        this.f237-$f1 = z2;
        this.f238-$f2 = i;
        this.f239-$f3 = obj;
        this.f240-$f4 = obj2;
        this.f241-$f5 = obj3;
        this.f242-$f6 = obj4;
    }

    public final void run() {
        $m$0();
    }
}
