package com.android.server.pm;

import java.io.File;
import java.io.FilenameFilter;

/* renamed from: com.android.server.pm.-$$Lambda$PackageManagerService$xKFHvZAUir1Y_lClMWZh87peKs8  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$PackageManagerService$xKFHvZAUir1Y_lClMWZh87peKs8 implements FilenameFilter {
    public static final /* synthetic */ $$Lambda$PackageManagerService$xKFHvZAUir1Y_lClMWZh87peKs8 INSTANCE = new $$Lambda$PackageManagerService$xKFHvZAUir1Y_lClMWZh87peKs8();

    private /* synthetic */ $$Lambda$PackageManagerService$xKFHvZAUir1Y_lClMWZh87peKs8() {
    }

    public final boolean accept(File file, String str) {
        return PackageManagerService.lambda$deleteTempPackageFiles$15(file, str);
    }
}
