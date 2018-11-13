package com.android.server.pm;

import android.content.pm.PackageParser.Package;
import android.content.pm.ShortcutInfo;
import android.util.ArraySet;
import com.android.server.pm.dex.DexManager;
import java.util.function.Predicate;

final /* synthetic */ class -$Lambda$KFbchFEqJgs_hY1HweauKRNA_ds implements Predicate {
    private final /* synthetic */ byte $id;
    /* renamed from: -$f0 */
    private final /* synthetic */ Object f38-$f0;

    private final /* synthetic */ boolean $m$0(Object arg0) {
        return ((UninstalledInstantAppState) arg0).mInstantAppInfo.getPackageName().equals((String) this.f38-$f0);
    }

    private final /* synthetic */ boolean $m$1(Object arg0) {
        return ((UninstalledInstantAppState) arg0).mInstantAppInfo.getPackageName().equals(((Package) this.f38-$f0).packageName);
    }

    private final /* synthetic */ boolean $m$2(Object arg0) {
        return ((ArraySet) this.f38-$f0).contains(((Package) arg0).packageName);
    }

    private final /* synthetic */ boolean $m$3(Object arg0) {
        return ((DexManager) this.f38-$f0).getPackageUseInfoOrDefault(((Package) arg0).packageName).isAnyCodePathUsedByOtherApps();
    }

    private final /* synthetic */ boolean $m$4(Object arg0) {
        return ((ArraySet) this.f38-$f0).contains(((Package) arg0).packageName);
    }

    private final /* synthetic */ boolean $m$5(Object arg0) {
        return ((String) this.f38-$f0).equals(((ShortcutInfo) arg0).getId());
    }

    public /* synthetic */ -$Lambda$KFbchFEqJgs_hY1HweauKRNA_ds(byte b, Object obj) {
        this.$id = b;
        this.f38-$f0 = obj;
    }

    public final boolean test(Object obj) {
        switch (this.$id) {
            case (byte) 0:
                return $m$0(obj);
            case (byte) 1:
                return $m$1(obj);
            case (byte) 2:
                return $m$2(obj);
            case (byte) 3:
                return $m$3(obj);
            case (byte) 4:
                return $m$4(obj);
            case (byte) 5:
                return $m$5(obj);
            default:
                throw new AssertionError();
        }
    }
}
