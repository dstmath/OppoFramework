package com.suntek.rcs.ui.common.mms;

import java.util.HashMap;
import java.util.Iterator;

public class RcsMessageForwardToSmsCache {
    private static HashMap<Long[], String[]> mMessageCache;
    private static RcsMessageForwardToSmsCache sInstance = new RcsMessageForwardToSmsCache();

    private RcsMessageForwardToSmsCache() {
        mMessageCache = new HashMap();
    }

    public static RcsMessageForwardToSmsCache getInstance() {
        if (sInstance == null) {
            sInstance = new RcsMessageForwardToSmsCache();
        }
        return sInstance;
    }

    public void addSendMessage(Long[] ids, String[] value) {
        mMessageCache.put(ids, value);
    }

    public String[] getCacheVaule(Long[] ids) {
        return (String[]) mMessageCache.get(ids);
    }

    public void clearCacheMessage() {
        mMessageCache.clear();
    }

    public Iterator<Long[]> getChachIterator() {
        return mMessageCache.keySet().iterator();
    }

    public HashMap<Long[], String[]> getCacheMessage() {
        return mMessageCache;
    }
}
