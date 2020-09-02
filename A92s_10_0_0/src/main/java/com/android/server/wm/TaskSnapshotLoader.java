package com.android.server.wm;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.util.Slog;
import com.android.server.display.OppoBrightUtils;
import com.android.server.wm.nano.WindowManagerProtos;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

class TaskSnapshotLoader {
    private static final String TAG = "WindowManager";
    private final TaskSnapshotPersister mPersister;

    TaskSnapshotLoader(TaskSnapshotPersister persister) {
        this.mPersister = persister;
    }

    /* access modifiers changed from: package-private */
    public ActivityManager.TaskSnapshot loadTask(int taskId, int userId, boolean reducedResolution) {
        File bitmapFile;
        String str;
        File protoFile = this.mPersister.getProtoFile(taskId, userId);
        if (reducedResolution) {
            bitmapFile = this.mPersister.getReducedResolutionBitmapFile(taskId, userId);
        } else {
            bitmapFile = this.mPersister.getBitmapFile(taskId, userId);
        }
        if (bitmapFile == null || !protoFile.exists() || !bitmapFile.exists()) {
            return null;
        }
        try {
            WindowManagerProtos.TaskSnapshotProto proto = WindowManagerProtos.TaskSnapshotProto.parseFrom(Files.readAllBytes(protoFile.toPath()));
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.HARDWARE;
            Bitmap bitmap = BitmapFactory.decodeFile(bitmapFile.getPath(), options);
            if (bitmap == null) {
                Slog.w("WindowManager", "Failed to load bitmap: " + bitmapFile.getPath());
                return null;
            }
            GraphicBuffer buffer = bitmap.createGraphicBufferHandle();
            if (buffer == null) {
                Slog.w("WindowManager", "Failed to retrieve gralloc buffer for bitmap: " + bitmapFile.getPath());
                return null;
            }
            str = "WindowManager";
            try {
                return new ActivityManager.TaskSnapshot(ComponentName.unflattenFromString(proto.topActivityComponent), buffer, bitmap.getColorSpace(), proto.orientation, new Rect(proto.insetLeft, proto.insetTop, proto.insetRight, proto.insetBottom), reducedResolution, Float.compare(proto.scale, OppoBrightUtils.MIN_LUX_LIMITI) != 0 ? proto.scale : reducedResolution ? this.mPersister.getReducedScale() : 1.0f, proto.isRealSnapshot, proto.windowingMode, proto.systemUiVisibility, proto.isTranslucent);
            } catch (IOException e) {
                Slog.w(str, "Unable to load task snapshot data for taskId=" + taskId);
                return null;
            }
        } catch (IOException e2) {
            str = "WindowManager";
            Slog.w(str, "Unable to load task snapshot data for taskId=" + taskId);
            return null;
        }
    }
}
