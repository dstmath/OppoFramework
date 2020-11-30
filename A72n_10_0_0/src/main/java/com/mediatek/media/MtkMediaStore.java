package com.mediatek.media;

import android.net.Uri;
import android.provider.MediaStore;

public final class MtkMediaStore {
    private static final String CONTENT_AUTHORITY_SLASH = "content://media/";
    public static final String MTP_TRANSFER_FILE_PATH = "mtp_transfer_file_path";

    public interface FileColumns extends MediaStore.Files.FileColumns {
        public static final String FILE_NAME = "file_name";
        public static final String FILE_TYPE = "file_type";
    }

    public interface ImageColumns extends MediaStore.Images.ImageColumns {
        public static final String CAMERA_REFOCUS = "camera_refocus";
    }

    public interface MediaColumns extends MediaStore.MediaColumns {
        public static final String DRM_CONTENT_DESCRIPTION = "drm_content_description";
        public static final String DRM_CONTENT_NAME = "drm_content_name";
        public static final String DRM_CONTENT_URI = "drm_content_uri";
        public static final String DRM_CONTENT_VENDOR = "drm_content_vendor";
        public static final String DRM_DATA_LEN = "drm_dataLen";
        public static final String DRM_ICON_URI = "drm_icon_uri";
        public static final String DRM_METHOD = "drm_method";
        public static final String DRM_OFFSET = "drm_offset";
        public static final String DRM_RIGHTS_ISSUER = "drm_rights_issuer";
    }

    public interface VideoColumns extends MediaStore.Video.VideoColumns {
        public static final String ORIENTATION = "orientation";
    }

    public static Uri getMtpTransferFileUri() {
        return Uri.parse("content://media/none/mtp_transfer_file");
    }
}
