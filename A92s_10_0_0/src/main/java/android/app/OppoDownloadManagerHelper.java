package android.app;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Downloads;
import java.io.File;

public class OppoDownloadManagerHelper {
    public static Uri fixUriWhenGetUriForDownloadedFile(Cursor cursor, long id) {
        int destination = cursor.getInt(cursor.getColumnIndexOrThrow("destination"));
        return (destination == 1 || destination == 5 || destination == 3 || destination == 2) ? ContentUris.withAppendedId(Downloads.Impl.ALL_DOWNLOADS_CONTENT_URI, id) : Uri.fromFile(new File(cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_FILENAME))));
    }
}
