package android.support.v4.media;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.os.ResultReceiver;
import android.util.Log;
import com.alibaba.fastjson.parser.JSONLexer;
import java.util.ArrayList;
import java.util.List;

public final class MediaBrowserCompat {
    static final boolean DEBUG = Log.isLoggable("MediaBrowserCompat", 3);
    private final MediaBrowserImpl mImpl;

    interface MediaBrowserImpl {
        void disconnect();
    }

    public void disconnect() {
        this.mImpl.disconnect();
    }

    public static class MediaItem implements Parcelable {
        public static final Parcelable.Creator<MediaItem> CREATOR = new Parcelable.Creator<MediaItem>() {
            /* class android.support.v4.media.MediaBrowserCompat.MediaItem.AnonymousClass1 */

            @Override // android.os.Parcelable.Creator
            public MediaItem createFromParcel(Parcel in) {
                return new MediaItem(in);
            }

            @Override // android.os.Parcelable.Creator
            public MediaItem[] newArray(int size) {
                return new MediaItem[size];
            }
        };
        private final MediaDescriptionCompat mDescription;
        private final int mFlags;

        MediaItem(Parcel in) {
            this.mFlags = in.readInt();
            this.mDescription = MediaDescriptionCompat.CREATOR.createFromParcel(in);
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel out, int flags) {
            out.writeInt(this.mFlags);
            this.mDescription.writeToParcel(out, flags);
        }

        public String toString() {
            return "MediaItem{mFlags=" + this.mFlags + ", mDescription=" + this.mDescription + '}';
        }
    }

    public static abstract class ItemCallback {
        public void onItemLoaded(MediaItem item) {
        }

        public void onError(String itemId) {
        }
    }

    public static abstract class SearchCallback {
        public void onSearchResult(String query, Bundle extras, List<MediaItem> list) {
        }

        public void onError(String query, Bundle extras) {
        }
    }

    public static abstract class CustomActionCallback {
        public void onProgressUpdate(String action, Bundle extras, Bundle data) {
        }

        public void onResult(String action, Bundle extras, Bundle resultData) {
        }

        public void onError(String action, Bundle extras, Bundle data) {
        }
    }

    private static class ItemReceiver extends ResultReceiver {
        private final ItemCallback mCallback;
        private final String mMediaId;

        /* access modifiers changed from: protected */
        @Override // android.support.v4.os.ResultReceiver
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData != null) {
                resultData.setClassLoader(MediaBrowserCompat.class.getClassLoader());
            }
            if (resultCode != 0 || resultData == null || !resultData.containsKey("media_item")) {
                this.mCallback.onError(this.mMediaId);
                return;
            }
            Parcelable item = resultData.getParcelable("media_item");
            if (item == null || (item instanceof MediaItem)) {
                this.mCallback.onItemLoaded((MediaItem) item);
            } else {
                this.mCallback.onError(this.mMediaId);
            }
        }
    }

    private static class SearchResultReceiver extends ResultReceiver {
        private final SearchCallback mCallback;
        private final Bundle mExtras;
        private final String mQuery;

        /* access modifiers changed from: protected */
        @Override // android.support.v4.os.ResultReceiver
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultData != null) {
                resultData.setClassLoader(MediaBrowserCompat.class.getClassLoader());
            }
            if (resultCode != 0 || resultData == null || !resultData.containsKey("search_results")) {
                this.mCallback.onError(this.mQuery, this.mExtras);
                return;
            }
            Parcelable[] items = resultData.getParcelableArray("search_results");
            List<MediaItem> results = null;
            if (items != null) {
                results = new ArrayList<>();
                for (Parcelable item : items) {
                    results.add((MediaItem) item);
                }
            }
            this.mCallback.onSearchResult(this.mQuery, this.mExtras, results);
        }
    }

    private static class CustomActionResultReceiver extends ResultReceiver {
        private final String mAction;
        private final CustomActionCallback mCallback;
        private final Bundle mExtras;

        /* access modifiers changed from: protected */
        @Override // android.support.v4.os.ResultReceiver
        public void onReceiveResult(int resultCode, Bundle resultData) {
            if (this.mCallback != null) {
                switch (resultCode) {
                    case JSONLexer.NOT_MATCH /* -1 */:
                        this.mCallback.onError(this.mAction, this.mExtras, resultData);
                        return;
                    case 0:
                        this.mCallback.onResult(this.mAction, this.mExtras, resultData);
                        return;
                    case 1:
                        this.mCallback.onProgressUpdate(this.mAction, this.mExtras, resultData);
                        return;
                    default:
                        Log.w("MediaBrowserCompat", "Unknown result code: " + resultCode + " (extras=" + this.mExtras + ", resultData=" + resultData + ")");
                        return;
                }
            }
        }
    }
}
