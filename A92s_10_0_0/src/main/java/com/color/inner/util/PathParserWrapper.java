package com.color.inner.util;

import android.graphics.Path;
import android.util.Log;
import android.util.PathParser;

public class PathParserWrapper {
    private static final String TAG = "PathParserWrapper";

    private PathParserWrapper() {
    }

    public static Path createPathFromPathData(String pathString) {
        try {
            return PathParser.createPathFromPathData(pathString);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return null;
        }
    }
}
