package com.android.server.pm;

import android.content.pm.SharedLibraryInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$xJ_pDKPvSH0lgOwva8xeI2zJ2ns  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$xJ_pDKPvSH0lgOwva8xeI2zJ2ns implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PackageManagerService$xJ_pDKPvSH0lgOwva8xeI2zJ2ns INSTANCE = new $$Lambda$PackageManagerService$xJ_pDKPvSH0lgOwva8xeI2zJ2ns();

    private /* synthetic */ $$Lambda$PackageManagerService$xJ_pDKPvSH0lgOwva8xeI2zJ2ns() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((SharedLibraryInfo) obj).addDependency((SharedLibraryInfo) obj2);
    }
}
