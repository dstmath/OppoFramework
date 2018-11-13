package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$5qSWip3Q3NYNf0S8FNRU2st8ZfA implements Predicate {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ long f37-$f0;

    private final /* synthetic */ boolean $m$0(Object arg0) {
        return InstantAppRegistry.m73lambda$-com_android_server_pm_InstantAppRegistry_31751(this.f37-$f0, (UninstalledInstantAppState) arg0);
    }

    private final /* synthetic */ boolean $m$1(Object arg0) {
        return PackageManagerServiceUtils.m87lambda$-com_android_server_pm_PackageManagerServiceUtils_7346(this.f37-$f0, (Package) arg0);
    }

    public /* synthetic */ -$Lambda$5qSWip3Q3NYNf0S8FNRU2st8ZfA(byte b, long j) {
        this.$id = b;
        this.f37-$f0 = j;
    }

    public final boolean test(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj);
            case (byte) 1:
                return $m$1(obj);
            default:
                throw new AssertionError();
        }
    }
}
