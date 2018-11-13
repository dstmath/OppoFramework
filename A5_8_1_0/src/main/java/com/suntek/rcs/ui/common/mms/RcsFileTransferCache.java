package com.suntek.rcs.ui.common.mms;

import java.util.HashMap;
import java.util.Iterator;

public class RcsFileTransferCache {
    private static HashMap<Long, Long> mFileTrasnfer;
    private static RcsFileTransferCache sInstance;

    private RcsFileTransferCache() {
        mFileTrasnfer = new HashMap();
    }

    public static RcsFileTransferCache getInstance() {
        if (sInstance == null) {
            sInstance = new RcsFileTransferCache();
        }
        return sInstance;
    }

    public void addFileTransferPercent(Long key, Long value) {
        mFileTrasnfer.put(key, value);
    }

    public void removeFileTransferPercent(Long key) {
        mFileTrasnfer.remove(key);
    }

    public Long getFileTransferPercent(Long key) {
        return (Long) mFileTrasnfer.get(key);
    }

    public Iterator<Long> getFileTransferPercentKeys() {
        return mFileTrasnfer.keySet().iterator();
    }

    public boolean hasFileTransferPercent(Long key) {
        return mFileTrasnfer.containsKey(key);
    }
}
