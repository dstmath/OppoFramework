package com.android.server.pm;

public class ColorMergedProcessSplitManager implements IColorMergedProcessSplitManager {
    private static final String TAG = "ColorAppProcessMergeManager";
    private static volatile ColorMergedProcessSplitManager sInstance = null;

    public void init() {
    }

    public static ColorMergedProcessSplitManager getInstance() {
        if (sInstance == null) {
            synchronized (ColorMergedProcessSplitManager.class) {
                if (sInstance == null) {
                    sInstance = new ColorMergedProcessSplitManager();
                }
            }
        }
        return sInstance;
    }

    private ColorMergedProcessSplitManager() {
    }

    public boolean isAppProcessNeedSplit(String processName) {
        if (processName.equals("com.coloros.persist.system")) {
            return true;
        }
        return false;
    }
}
