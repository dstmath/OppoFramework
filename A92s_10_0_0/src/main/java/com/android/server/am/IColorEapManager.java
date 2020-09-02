package com.android.server.am;

import android.app.ApplicationErrorReport;
import android.common.IOppoCommonFeature;
import android.common.OppoFeatureList;
import android.content.Context;
import android.os.DropBoxManager;
import java.io.File;
import java.io.IOException;

public interface IColorEapManager extends IOppoCommonFeature {
    public static final IColorEapManager DEFAULT = new IColorEapManager() {
        /* class com.android.server.am.IColorEapManager.AnonymousClass1 */
    };
    public static final String NAME = "IColorEapManager";

    default OppoFeatureList.OppoIndex index() {
        return OppoFeatureList.OppoIndex.IColorEapManager;
    }

    default IOppoCommonFeature getDefault() {
        return DEFAULT;
    }

    default void init(IColorActivityManagerServiceEx amsEx) {
    }

    default void addTimeInfo(StringBuilder sb) {
    }

    default void setErrorPackageName(String pkg) {
    }

    default void collectEapInfo(Context context, String dropboxTag, String eventType, ProcessRecord process, String subject, File dataFile, ApplicationErrorReport.CrashInfo crashInfo) {
    }

    default int getDataFileSizeAjusted(int prevSize, int lineSize, File file) {
        return prevSize;
    }

    default void appendCpuInfo(StringBuilder sb, String eventType) {
    }

    default void setCrashProcessRecord(ProcessRecord processRecord) {
    }

    default void addEntryToEap(String tag, DropBoxManager.Entry entry) throws IOException {
    }

    default void setFileName(String fileName) {
    }
}
