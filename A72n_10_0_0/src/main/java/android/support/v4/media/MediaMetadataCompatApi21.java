package android.support.v4.media;

import android.media.MediaMetadata;
import android.os.Parcel;

class MediaMetadataCompatApi21 {
    public static void writeToParcel(Object metadataObj, Parcel dest, int flags) {
        ((MediaMetadata) metadataObj).writeToParcel(dest, flags);
    }
}
