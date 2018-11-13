package com.android.server.am;

final /* synthetic */ class -$Lambda$-Oweh9FamEB8lkqsrsDtt1mh9YE implements Runnable {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ int f76-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ int f77-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ int f78-$f2;
    /* renamed from: -$f3 */
    private final /* synthetic */ int f79-$f3;
    /* renamed from: -$f4 */
    private final /* synthetic */ int f80-$f4;
    /* renamed from: -$f5 */
    private final /* synthetic */ int f81-$f5;
    /* renamed from: -$f6 */
    private final /* synthetic */ int f82-$f6;
    /* renamed from: -$f7 */
    private final /* synthetic */ int f83-$f7;
    /* renamed from: -$f8 */
    private final /* synthetic */ Object f84-$f8;

    private final /* synthetic */ void $m$0() {
        ((BatteryStatsService) this.f84-$f8).m36lambda$-com_android_server_am_BatteryStatsService_35168(this.f76-$f0, this.f77-$f1, this.f78-$f2, this.f79-$f3, this.f80-$f4, this.f81-$f5, this.f82-$f6, this.f83-$f7);
    }

    private final /* synthetic */ void $m$1() {
        ((BatteryStatsService) this.f84-$f8).m35lambda$-com_android_server_am_BatteryStatsService_33781(this.f76-$f0, this.f77-$f1, this.f78-$f2, this.f79-$f3, this.f80-$f4, this.f81-$f5, this.f82-$f6, this.f83-$f7);
    }

    public /* synthetic */ -$Lambda$-Oweh9FamEB8lkqsrsDtt1mh9YE(byte b, int i, int i2, int i3, int i4, int i5, int i6, int i7, int i8, Object obj) {
        this.$id = b;
        this.f76-$f0 = i;
        this.f77-$f1 = i2;
        this.f78-$f2 = i3;
        this.f79-$f3 = i4;
        this.f80-$f4 = i5;
        this.f81-$f5 = i6;
        this.f82-$f6 = i7;
        this.f83-$f7 = i8;
        this.f84-$f8 = obj;
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
