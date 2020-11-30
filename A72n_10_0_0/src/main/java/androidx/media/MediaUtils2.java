package androidx.media;

import android.os.Bundle;
import android.os.Parcelable;
import androidx.media.MediaSession2;
import java.util.ArrayList;
import java.util.List;

class MediaUtils2 {
    static List<MediaItem2> fromMediaItem2ParcelableArray(Parcelable[] itemParcelableList) {
        MediaItem2 item;
        List<MediaItem2> playlist = new ArrayList<>();
        if (itemParcelableList != null) {
            for (int i = 0; i < itemParcelableList.length; i++) {
                if ((itemParcelableList[i] instanceof Bundle) && (item = MediaItem2.fromBundle((Bundle) itemParcelableList[i])) != null) {
                    playlist.add(item);
                }
            }
        }
        return playlist;
    }

    static List<MediaSession2.CommandButton> fromCommandButtonParcelableArray(Parcelable[] list) {
        MediaSession2.CommandButton button;
        List<MediaSession2.CommandButton> layout = new ArrayList<>();
        for (int i = 0; i < list.length; i++) {
            if ((list[i] instanceof Bundle) && (button = MediaSession2.CommandButton.fromBundle((Bundle) list[i])) != null) {
                layout.add(button);
            }
        }
        return layout;
    }

    static List<Bundle> toBundleList(Parcelable[] array) {
        if (array == null) {
            return null;
        }
        List<Bundle> bundleList = new ArrayList<>();
        for (Parcelable p : array) {
            bundleList.add((Bundle) p);
        }
        return bundleList;
    }
}
