package com.color.inner.os;

import android.os.Trace;
import android.util.Log;

public class TraceWrapper {
    public static final String TAG = "TraceWrapper";
    public static final long TRACE_TAG_GRAPHICS = 2;

    private TraceWrapper() {
    }

    public static void traceBegin(long traceTag, String methodName) {
        try {
            Trace.traceBegin(traceTag, methodName);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void traceEnd(long traceTag) {
        try {
            Trace.traceEnd(traceTag);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void asyncTraceBegin(long traceTag, String methodName, int cookie) {
        try {
            Trace.asyncTraceBegin(traceTag, methodName, cookie);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }

    public static void asyncTraceEnd(long traceTag, String methodName, int cookie) {
        try {
            Trace.asyncTraceEnd(traceTag, methodName, cookie);
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
        }
    }
}
