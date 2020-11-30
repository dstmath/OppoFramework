package androidx.test.runner.screenshot;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

final class TakeScreenshotCallable implements Callable<Bitmap> {
    private WeakReference<View> viewRef;

    static class Factory {
        Factory() {
        }
    }

    @Override // java.util.concurrent.Callable
    public Bitmap call() {
        Bitmap bitmap = null;
        this.viewRef.get().setDrawingCacheEnabled(true);
        try {
            bitmap = Bitmap.createBitmap(this.viewRef.get().getDrawingCache());
        } catch (OutOfMemoryError omm) {
            Log.e("TakeScreenshotCallable", "Out of memory exception while trying to take a screenshot.", omm);
        } catch (Throwable th) {
            this.viewRef.get().setDrawingCacheEnabled(false);
            throw th;
        }
        this.viewRef.get().setDrawingCacheEnabled(false);
        return bitmap;
    }
}
