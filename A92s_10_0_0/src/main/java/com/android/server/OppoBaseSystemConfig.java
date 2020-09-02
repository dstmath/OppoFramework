package com.android.server;

import android.content.pm.FeatureInfo;
import android.util.ArrayMap;
import java.io.File;

public class OppoBaseSystemConfig {
    public ArrayMap<String, FeatureInfo> loadOppoAvailableFeatures(String name) {
        return new ArrayMap<>();
    }

    /* access modifiers changed from: protected */
    public boolean filterOppoFeatureFile(File file) {
        return false;
    }
}
