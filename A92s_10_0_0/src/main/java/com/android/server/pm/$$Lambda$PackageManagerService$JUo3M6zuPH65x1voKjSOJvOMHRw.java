package com.android.server.pm;

import android.content.pm.SharedLibraryInfo;
import java.util.function.BiConsumer;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$JUo3M6zuPH65x1voKjSOJvOMHRw  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$JUo3M6zuPH65x1voKjSOJvOMHRw implements BiConsumer {
    public static final /* synthetic */ $$Lambda$PackageManagerService$JUo3M6zuPH65x1voKjSOJvOMHRw INSTANCE = new $$Lambda$PackageManagerService$JUo3M6zuPH65x1voKjSOJvOMHRw();

    private /* synthetic */ $$Lambda$PackageManagerService$JUo3M6zuPH65x1voKjSOJvOMHRw() {
    }

    @Override // java.util.function.BiConsumer
    public final void accept(Object obj, Object obj2) {
        ((SharedLibraryInfo) obj).clearDependencies();
    }
}
