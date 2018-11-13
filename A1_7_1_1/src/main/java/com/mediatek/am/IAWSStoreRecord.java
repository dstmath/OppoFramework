package com.mediatek.am;

import java.util.ArrayList;

public interface IAWSStoreRecord {
    long getExtraVal();

    IAWSProcessRecord getRecord();

    ArrayList<IAWSProcessRecord> getRecords();

    String getTopPkgName();
}
