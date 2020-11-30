package com.android.server.wm;

import android.os.Environment;
import com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister;
import java.io.File;

/* renamed from: com.android.server.wm.-$$Lambda$DuM3fJ7DDorCTGUoF_TMwVpActg  reason: invalid class name */
/* compiled from: lambda */
public final /* synthetic */ class $$Lambda$DuM3fJ7DDorCTGUoF_TMwVpActg implements ColorAppStartingSnapshotPersister.DirectoryResolver {
    public static final /* synthetic */ $$Lambda$DuM3fJ7DDorCTGUoF_TMwVpActg INSTANCE = new $$Lambda$DuM3fJ7DDorCTGUoF_TMwVpActg();

    private /* synthetic */ $$Lambda$DuM3fJ7DDorCTGUoF_TMwVpActg() {
    }

    @Override // com.android.server.wm.startingwindow.ColorAppStartingSnapshotPersister.DirectoryResolver
    public final File getSystemDirectoryForUser(int i) {
        return Environment.getDataSystemCeDirectory(i);
    }
}
