package com.android.server.autofill;

import com.android.internal.os.IResultReceiver;

final /* synthetic */ class -$Lambda$mpPqaCtNJERkwd7tRkFrIaSM3WQ implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ int f114-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f115-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f116-$f2;

    private final /* synthetic */ void $m$0() {
        ((AutofillManagerServiceShellCommand) this.f115-$f1).m121xad566f0(this.f114-$f0, (IResultReceiver) this.f116-$f2);
    }

    private final /* synthetic */ void $m$1() {
        ((AutofillManagerServiceShellCommand) this.f115-$f1).m122xad584ff(this.f114-$f0, (IResultReceiver) this.f116-$f2);
    }

    public /* synthetic */ -$Lambda$mpPqaCtNJERkwd7tRkFrIaSM3WQ(byte b, int i, Object obj, Object obj2) {
        this.$id = b;
        this.f114-$f0 = i;
        this.f115-$f1 = obj;
        this.f116-$f2 = obj2;
    }

    public final void run() {
        switch (this.$id) {
            case (byte) 0:
                $m$0();
                return;
            case (byte) 1:
                $m$1();
                return;
            default:
                throw new AssertionError();
        }
    }
}
