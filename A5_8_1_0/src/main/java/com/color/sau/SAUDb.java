package com.color.sau;

import android.net.Uri;
import android.provider.BaseColumns;

public class SAUDb {
    public static final String AUTHORITY = "com.coloros.sau.db";

    public static final class UpdateInfoColumns implements BaseColumns {
        public static final String ALL_SIZE = "all_size";
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.oppo.update_info";
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.oppo.update_info";
        public static final Uri CONTENT_URI = Uri.parse("content://com.coloros.sau.db/update_info");
        public static final String DESCRIPTION = "description";
        public static final String DOWNLOADED_SIZE = "downloaded_size";
        public static final String DOWNLOAD_FINISHED = "download_finished";
        public static final String ERROR_TYPE = "error_type";
        public static final String FILE_NAME = "file_name";
        public static final String FORCE_DOWNLOAD = "force_download";
        public static final String FORCE_INSTALL = "force_install";
        public static final String ICON_EXISTS = "icon_exists";
        public static final String INSTALL_FINISHED = "install_finished";
        public static final String MD5_ALL = "md5_all";
        public static final String MD5_PATCH = "md5_patch";
        public static final String NEW_VERSION_CODE = "new_version_code";
        public static final String NEW_VERSION_NAME = "new_version_name";
        public static final String OLD_FILE_DIR = "old_file_dir";
        public static final String PATCH_FILE_NAME = "patch_file_name";
        public static final String PATCH_FINISHED = "patch_finished";
        public static final String PKG_NAME = "pkg_name";
        public static final String SAU_TYPE = "sau_type";
        public static final String SILENT_UPDATING_STATUS = "status_updating";
        public static final String SIZE = "size";
        public static final String TYPE = "type";
        public static final String UPGRADE_STATUS = "upgrade_status";
        public static final String URL = "url";
        public static final String USE_OLD = "can_use_old";
    }
}
