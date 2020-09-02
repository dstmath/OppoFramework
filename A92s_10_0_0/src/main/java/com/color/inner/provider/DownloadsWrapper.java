package com.color.inner.provider;

import android.net.Uri;
import android.provider.Downloads;

public final class DownloadsWrapper {
    private DownloadsWrapper() {
    }

    public static final class Impl {
        public static final String ACTION_DOWNLOAD_COMPLETED = "android.intent.action.DOWNLOAD_COMPLETED";
        public static final String COLUMN_APP_DATA = "entity";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_DESTINATION = "destination";
        public static final String COLUMN_FILE_NAME_HINT = "hint";
        public static final String COLUMN_MIME_TYPE = "mimetype";
        public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";
        public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";
        public static final String COLUMN_REFERER = "referer";
        public static final String COLUMN_STATUS = "status";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_URI = "uri";
        public static final String COLUMN_VISIBILITY = "visibility";
        public static final Uri CONTENT_URI = Downloads.Impl.CONTENT_URI;
        public static final int DESTINATION_FILE_URI = 4;
        public static final int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;
        public static final String _COUNT = "_count";
        public static final String _DATA = "_data";
        public static final String _ID = "_id";

        private Impl() {
        }
    }

    public static boolean isStatusSuccess(int status) {
        return Downloads.Impl.isStatusSuccess(status);
    }

    public static boolean isStatusCompleted(int status) {
        return Downloads.Impl.isStatusCompleted(status);
    }
}
