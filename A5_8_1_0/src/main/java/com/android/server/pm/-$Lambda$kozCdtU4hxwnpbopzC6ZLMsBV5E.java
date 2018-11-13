package com.android.server.pm;

import android.content.IntentSender;
import android.content.pm.IDexModuleRegisterCallback;
import android.content.pm.IPackageDataObserver;
import com.android.server.pm.dex.DexManager.RegisterDexModuleResult;

final /* synthetic */ class -$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E implements Runnable {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f300-$f0;
    /* renamed from: -$f1 */
    private final /* synthetic */ Object f301-$f1;
    /* renamed from: -$f2 */
    private final /* synthetic */ Object f302-$f2;

    /* renamed from: com.android.server.pm.-$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ int f303-$f0;

        private final /* synthetic */ void $m$0() {
            PackageManagerService.m41lambda$-com_android_server_pm_PackageManagerService_1122926(this.f303-$f0);
        }

        public /* synthetic */ AnonymousClass1(int i) {
            this.f303-$f0 = i;
        }

        public final void run() {
            $m$0();
        }
    }

    /* renamed from: com.android.server.pm.-$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E$2 */
    final /* synthetic */ class AnonymousClass2 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f304-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ long f305-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f306-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f307-$f3;
        /* renamed from: -$f4 */
        private final /* synthetic */ Object f308-$f4;

        private final /* synthetic */ void $m$0() {
            ((PackageManagerService) this.f306-$f2).m45lambda$-com_android_server_pm_PackageManagerService_251604((String) this.f307-$f3, this.f305-$f1, this.f304-$f0, (IntentSender) this.f308-$f4);
        }

        private final /* synthetic */ void $m$1() {
            ((PackageManagerService) this.f306-$f2).m44lambda$-com_android_server_pm_PackageManagerService_250805((String) this.f307-$f3, this.f305-$f1, this.f304-$f0, (IPackageDataObserver) this.f308-$f4);
        }

        public /* synthetic */ AnonymousClass2(byte b, int i, long j, Object obj, Object obj2, Object obj3) {
            this.$id = b;
            this.f304-$f0 = i;
            this.f305-$f1 = j;
            this.f306-$f2 = obj;
            this.f307-$f3 = obj2;
            this.f308-$f4 = obj3;
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

    /* renamed from: com.android.server.pm.-$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E$3 */
    final /* synthetic */ class AnonymousClass3 implements Runnable {
        /* renamed from: -$f0 */
        private final /* synthetic */ boolean f309-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f310-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f311-$f2;
        /* renamed from: -$f3 */
        private final /* synthetic */ Object f312-$f3;

        private final /* synthetic */ void $m$0() {
            ((PackageManagerService) this.f310-$f1).m46lambda$-com_android_server_pm_PackageManagerService_822076((int[]) this.f311-$f2, (String) this.f312-$f3, this.f309-$f0);
        }

        public /* synthetic */ AnonymousClass3(boolean z, Object obj, Object obj2, Object obj3) {
            this.f309-$f0 = z;
            this.f310-$f1 = obj;
            this.f311-$f2 = obj2;
            this.f312-$f3 = obj3;
        }

        public final void run() {
            $m$0();
        }
    }

    private final /* synthetic */ void $m$0() {
        PackageManagerService.m42lambda$-com_android_server_pm_PackageManagerService_555440((IDexModuleRegisterCallback) this.f300-$f0, (String) this.f301-$f1, (RegisterDexModuleResult) this.f302-$f2);
    }

    public /* synthetic */ -$Lambda$kozCdtU4hxwnpbopzC6ZLMsBV5E(Object obj, Object obj2, Object obj3) {
        this.f300-$f0 = obj;
        this.f301-$f1 = obj2;
        this.f302-$f2 = obj3;
    }

    public final void run() {
        $m$0();
    }
}
