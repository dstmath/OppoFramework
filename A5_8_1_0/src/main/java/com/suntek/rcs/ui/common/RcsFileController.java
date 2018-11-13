package com.suntek.rcs.ui.common;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import com.suntek.mway.rcs.client.api.exception.ServiceDisconnectedException;
import com.suntek.mway.rcs.client.api.message.MessageApi;
import java.io.File;

public class RcsFileController {
    private static final int BYTE_TRANSFER_TO_KB = 1024;
    private static final String CONTENT = "content";
    private static final int DATA_COLUMN_INDEX = 0;
    private static final String[] DATA_DOCUMENT_PROJECTION = new String[]{"_data"};
    private static final String DOWNLOAD_DOCUMENT_URI = "com.android.providers.downloads.documents";
    private static final String EXTRNAL_STORAGE_DOCUMENT_URI = "com.android.externalstorage.documents";
    private static final String FILE = "file";
    public static final int FILE_ERROR = 2;
    public static final int FILE_EXCEEDED_RCS_LIMIT = 1;
    public static final int FILE_NOT_EXCEEDED_RCS_LIMIT = 0;
    private static final String MEDIA_DOCUMENT_URI = "com.android.providers.media.documents";
    private static final String MEDIA_STORE_AUDIO = "audio";
    private static final String MEDIA_STORE_IMAGE = "image";
    private static final String MEDIA_STORE_VIDEO = "video";
    private static final String PRIMARY = "primary";
    private static final Uri PUBLIC_DOWNLOAD_URI = Uri.parse("content://downloads/public_downloads");

    public static int checkFileLegality(String filePath, int fileType) {
        if (TextUtils.isEmpty(filePath)) {
            return 2;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return 2;
        }
        long rcsFileLimit = getRcsTransferFileMaxSize(fileType);
        long fileLength = file.length() / 1024;
        RcsLog.d("file limit size:" + fileLength + "::" + rcsFileLimit);
        if (fileLength > rcsFileLimit) {
            return 1;
        }
        return 0;
    }

    public static long getFileSizes(Context context, Uri uri) {
        File file = new File(getFilePath(context, uri));
        if (file == null || !file.exists()) {
            return 0;
        }
        return file.length();
    }

    public static long getRcsTransferFileMaxSize(int fileType) {
        switch (fileType) {
            case 1:
                try {
                    return MessageApi.getInstance().getImageMaxSize();
                } catch (ServiceDisconnectedException exception) {
                    exception.printStackTrace();
                    return 0;
                } catch (Throwable e) {
                    RcsLog.w(e);
                    return 0;
                }
            case 2:
            case 3:
                return MessageApi.getInstance().getVideoMaxSize();
            default:
                return 0;
        }
    }

    public static long getRcsTransferFileMaxDuration(int fileType) {
        switch (fileType) {
            case 2:
                try {
                    return (long) MessageApi.getInstance().getAudioMaxDuration();
                } catch (ServiceDisconnectedException exception) {
                    exception.printStackTrace();
                    return 0;
                } catch (Throwable e) {
                    RcsLog.w(e);
                    return 0;
                }
            case 3:
                return (long) MessageApi.getInstance().getVideoMaxDuration();
            default:
                return 0;
        }
    }

    public static String getFilePath(Context context, Uri uri) {
        boolean isKitKat = VERSION.SDK_INT >= 19;
        String authority = uri.getAuthority();
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (EXTRNAL_STORAGE_DOCUMENT_URI.equals(authority)) {
                String[] split = getTypeString(uri);
                if (PRIMARY.equalsIgnoreCase(split[0])) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
                return "";
            } else if (MEDIA_DOCUMENT_URI.equals(authority)) {
                return getMediaStoreFilePath(context, uri);
            } else {
                if (DOWNLOAD_DOCUMENT_URI.equals(authority)) {
                    return getDataColumnFilePath(context, ContentUris.withAppendedId(PUBLIC_DOWNLOAD_URI, Long.valueOf(DocumentsContract.getDocumentId(uri)).longValue()), null, null);
                }
            }
        } else if (FILE.equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        } else {
            if ("content".equalsIgnoreCase(uri.getScheme())) {
                return getDataColumnFilePath(context, uri, null, null);
            }
        }
        return null;
    }

    private static String[] getTypeString(Uri uri) {
        return DocumentsContract.getDocumentId(uri).split(":");
    }

    private static String getMediaStoreFilePath(Context context, Uri uri) {
        String[] split = getTypeString(uri);
        Uri contentUri = null;
        if (MEDIA_STORE_IMAGE.equals(split[0])) {
            contentUri = Media.EXTERNAL_CONTENT_URI;
        } else if (MEDIA_STORE_AUDIO.equals(split[0])) {
            contentUri = Audio.Media.EXTERNAL_CONTENT_URI;
        } else if (MEDIA_STORE_VIDEO.equals(split[0])) {
            contentUri = Video.Media.EXTERNAL_CONTENT_URI;
        }
        String selection = "_id = ?";
        return getDataColumnFilePath(context, contentUri, "_id = ?", new String[]{split[1]});
    }

    public static String getDataColumnFilePath(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, DATA_DOCUMENT_PROJECTION, selection, selectionArgs, null);
            if (cursor == null || !cursor.moveToFirst()) {
                if (cursor != null) {
                    cursor.close();
                }
                return null;
            }
            String string = cursor.getString(0);
            return string;
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}
