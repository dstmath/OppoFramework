package android_maps_conflict_avoidance.com.google.android.gsf;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public final class GoogleSettingsContract {

    public static class NameValueTable implements BaseColumns {
    }

    public static final class Partner extends NameValueTable {
        public static final Uri CONTENT_URI = Uri.parse("content://com.google.settings/partner");

        public static String getString(ContentResolver resolver, String name) {
            String value = null;
            Cursor c = null;
            try {
                ContentResolver contentResolver = resolver;
                c = contentResolver.query(CONTENT_URI, new String[]{"value"}, "name=?", new String[]{name}, null);
                if (c != null && c.moveToNext()) {
                    value = c.getString(0);
                }
                if (c != null) {
                    c.close();
                }
            } catch (SQLException e) {
                Log.e("GoogleSettings", "Can't get key " + name + " from " + CONTENT_URI, e);
                if (c != null) {
                    c.close();
                }
            } catch (Throwable th) {
                if (c != null) {
                    c.close();
                }
            }
            return value;
        }

        public static String getString(ContentResolver resolver, String name, String defaultValue) {
            String value = getString(resolver, name);
            if (value == null) {
                return defaultValue;
            }
            return value;
        }
    }
}
