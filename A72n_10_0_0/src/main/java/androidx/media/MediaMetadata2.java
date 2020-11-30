package androidx.media;

import android.os.Bundle;
import androidx.collection.ArrayMap;

public final class MediaMetadata2 {
    static final ArrayMap<String, Integer> METADATA_KEYS_TYPE = new ArrayMap<>();
    private static final String[] PREFERRED_BITMAP_ORDER = {"android.media.metadata.DISPLAY_ICON", "android.media.metadata.ART", "android.media.metadata.ALBUM_ART"};
    private static final String[] PREFERRED_DESCRIPTION_ORDER = {"android.media.metadata.TITLE", "android.media.metadata.ARTIST", "android.media.metadata.ALBUM", "android.media.metadata.ALBUM_ARTIST", "android.media.metadata.WRITER", "android.media.metadata.AUTHOR", "android.media.metadata.COMPOSER"};
    private static final String[] PREFERRED_URI_ORDER = {"android.media.metadata.DISPLAY_ICON_URI", "android.media.metadata.ART_URI", "android.media.metadata.ALBUM_ART_URI"};
    final Bundle mBundle;

    static {
        METADATA_KEYS_TYPE.put("android.media.metadata.TITLE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.ARTIST", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DURATION", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.AUTHOR", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.WRITER", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.COMPOSER", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.COMPILATION", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DATE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.YEAR", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.GENRE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.TRACK_NUMBER", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.NUM_TRACKS", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISC_NUMBER", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM_ARTIST", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.ART", 2);
        METADATA_KEYS_TYPE.put("android.media.metadata.ART_URI", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM_ART", 2);
        METADATA_KEYS_TYPE.put("android.media.metadata.ALBUM_ART_URI", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.USER_RATING", 3);
        METADATA_KEYS_TYPE.put("android.media.metadata.RATING", 3);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_TITLE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_SUBTITLE", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_DESCRIPTION", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_ICON", 2);
        METADATA_KEYS_TYPE.put("android.media.metadata.DISPLAY_ICON_URI", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.MEDIA_ID", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.MEDIA_URI", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.RADIO_FREQUENCY", 4);
        METADATA_KEYS_TYPE.put("android.media.metadata.RADIO_PROGRAM_NAME", 1);
        METADATA_KEYS_TYPE.put("android.media.metadata.BT_FOLDER_TYPE", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.ADVERTISEMENT", 0);
        METADATA_KEYS_TYPE.put("android.media.metadata.DOWNLOAD_STATUS", 0);
    }

    MediaMetadata2(Bundle bundle) {
        this.mBundle = new Bundle(bundle);
        this.mBundle.setClassLoader(MediaMetadata2.class.getClassLoader());
    }

    public String getMediaId() {
        return getString("android.media.metadata.MEDIA_ID");
    }

    public String getString(String key) {
        if (key != null) {
            CharSequence text = this.mBundle.getCharSequence(key);
            if (text != null) {
                return text.toString();
            }
            return null;
        }
        throw new IllegalArgumentException("key shouldn't be null");
    }

    public static MediaMetadata2 fromBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        return new MediaMetadata2(bundle);
    }
}
