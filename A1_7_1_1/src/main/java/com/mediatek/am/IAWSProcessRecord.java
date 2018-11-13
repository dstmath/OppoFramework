package com.mediatek.am;

import android.util.ArrayMap;
import com.android.internal.app.procstats.ProcessStats.ProcessStateHolder;

public interface IAWSProcessRecord {
    int getAdj();

    int getPid();

    String getPkgName();

    int getPkgVer();

    String getProcName();

    int getUid();

    String getWaitingToKill();

    ArrayMap<String, ProcessStateHolder> getpkgList();

    int getprocState();

    boolean isKilled();

    boolean isKilledByAm();
}
