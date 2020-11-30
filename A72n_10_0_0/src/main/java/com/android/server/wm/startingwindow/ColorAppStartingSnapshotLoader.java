package com.android.server.wm.startingwindow;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;

public class ColorAppStartingSnapshotLoader {
    ColorAppStartingSnapshotPersister mPersiter;

    public ColorAppStartingSnapshotLoader(ColorAppStartingSnapshotPersister persister) {
        this.mPersiter = persister;
    }

    /* access modifiers changed from: package-private */
    public Bitmap loadAppSnapshot(int userId, String packageName) {
        File bitmapFile = this.mPersiter.getBitmapFile(userId, packageName);
        ColorStartingWindowUtils.logD("loadAppSnapshot bitmapFile =: " + bitmapFile);
        if (bitmapFile == null || !bitmapFile.exists()) {
            return null;
        }
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
            if (bitmap == null) {
                ColorStartingWindowUtils.logD("Failed to load bitmap: " + bitmapFile.getPath());
            }
            return bitmap;
        } catch (Exception e) {
            ColorStartingWindowUtils.logD("Unable to load task snapshot data for packageName =:" + packageName);
            return null;
        }
    }
}
