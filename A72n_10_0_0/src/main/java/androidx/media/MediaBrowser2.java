package androidx.media;

import android.os.Bundle;
import android.support.v4.media.MediaBrowserCompat;
import android.util.Log;
import androidx.media.MediaController2;
import java.util.HashMap;

public class MediaBrowser2 extends MediaController2 {
    static final boolean DEBUG = Log.isLoggable("MediaBrowser2", 3);
    private final HashMap<Bundle, MediaBrowserCompat> mBrowserCompats;
    private final Object mLock;

    public static class BrowserCallback extends MediaController2.ControllerCallback {
        public void onChildrenChanged(MediaBrowser2 browser, String parentId, int itemCount, Bundle extras) {
        }

        public void onSearchResultChanged(MediaBrowser2 browser, String query, int itemCount, Bundle extras) {
        }
    }

    @Override // androidx.media.MediaController2, java.lang.AutoCloseable
    public void close() {
        synchronized (this.mLock) {
            for (MediaBrowserCompat browser : this.mBrowserCompats.values()) {
                browser.disconnect();
            }
            this.mBrowserCompats.clear();
            super.close();
        }
    }
}
