package com.android.server.accounts;

import android.accounts.Account;
import android.accounts.AccountManagerInternal.OnAppPermissionChangeListener;
import android.content.pm.PackageManager.OnPermissionsChangedListener;

final /* synthetic */ class -$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU implements OnPermissionsChangedListener {
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f72-$f0;

    /* renamed from: com.android.server.accounts.-$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU$1 */
    final /* synthetic */ class AnonymousClass1 implements Runnable {
        private final /* synthetic */ byte $id;
        /* renamed from: -$f0 */
        private final /* synthetic */ int f73-$f0;
        /* renamed from: -$f1 */
        private final /* synthetic */ Object f74-$f1;
        /* renamed from: -$f2 */
        private final /* synthetic */ Object f75-$f2;

        private final /* synthetic */ void $m$0() {
            ((OnAppPermissionChangeListener) this.f74-$f1).onAppPermissionChanged((Account) this.f75-$f2, this.f73-$f0);
        }

        private final /* synthetic */ void $m$1() {
            ((AccountManagerService) this.f74-$f1).m112lambda$-com_android_server_accounts_AccountManagerService_105727((Account) this.f75-$f2, this.f73-$f0);
        }

        private final /* synthetic */ void $m$2() {
            ((OnAppPermissionChangeListener) this.f74-$f1).onAppPermissionChanged((Account) this.f75-$f2, this.f73-$f0);
        }

        public /* synthetic */ AnonymousClass1(byte b, int i, Object obj, Object obj2) {
            this.$id = b;
            this.f73-$f0 = i;
            this.f74-$f1 = obj;
            this.f75-$f2 = obj2;
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
                default:
                    throw new AssertionError();
            }
        }
    }

    private final /* synthetic */ void $m$0(int arg0) {
        ((AccountManagerService) this.f72-$f0).m113lambda$-com_android_server_accounts_AccountManagerService_16514(arg0);
    }

    public /* synthetic */ -$Lambda$JwXVQhqSYlVkCeKB5Nx7U6RsZlU(Object obj) {
        this.f72-$f0 = obj;
    }

    public final void onPermissionsChanged(int i) {
        $m$0(i);
    }
}
