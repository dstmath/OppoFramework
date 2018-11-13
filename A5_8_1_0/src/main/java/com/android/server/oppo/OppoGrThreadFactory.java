package com.android.server.oppo;

import android.content.Context;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OppoGrThreadFactory {
    public static final ThreadPoolExecutor executor = new ThreadPoolExecutor(2, 10, 1, TimeUnit.DAYS, new LinkedBlockingQueue(20));

    private OppoGrThreadFactory() {
    }

    public static OppoGrThread newOppoGrThread(String fileName, Context context, String basePathCode, String tipTitle, String tipContent, Map<String, String> exceptionMaps, String grAbandon, String grOk, String exceptionContent, String appName, String lastPkgName) {
        return new OppoGrThread(fileName, context, basePathCode, tipTitle, tipContent, exceptionMaps, grAbandon, grOk, exceptionContent, appName, lastPkgName);
    }

    public static OppoGrThread newOppoGrThread(Context context, String basePathCode, String tipTitle, String tipContent, String grAbandon, String grOk, String exceptionContent, String appName, String lastPkgName) {
        return new OppoGrThread(context, basePathCode, tipTitle, tipContent, grAbandon, grOk, exceptionContent, appName, lastPkgName);
    }

    public static OppoGrThread newOppoGrThread(Context context, String basePathCode, String tipTitle, String tipContent, String grAbandon, String grOk, String exceptionContent, String appName, String lastPkgName, int action) {
        return new OppoGrThread(context, basePathCode, tipTitle, tipContent, grAbandon, grOk, exceptionContent, appName, lastPkgName, action);
    }

    public static OppoGrThread newOppoGrThread(Context context) {
        return new OppoGrThread(context);
    }

    public static OppoGrThread newOppoGrThread(Context context, Map<String, String> exceptionMaps) {
        return new OppoGrThread(context, exceptionMaps);
    }
}
