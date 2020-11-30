package android.support.test.runner.screenshot;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;

final class TakeScreenshotCallable implements Callable<Bitmap> {
    private WeakReference<View> mViewRef;

    static class Factory {
        Factory() {
        }
    }

    @Override // java.util.concurrent.Callable
    public Bitmap call() {
        Bitmap bitmap = null;
        this.mViewRef.get().setDrawingCacheEnabled(true);
        try {
            bitmap = Bitmap.createBitmap(this.mViewRef.get().getDrawingCache());
        } catch (OutOfMemoryError omm) {
            Log.e("TakeScreenshotCallable", "Out of memory exception while trying to take a screenshot.", omm);
        } catch (Throwable th) {
            this.mViewRef.get().setDrawingCacheEnabled(false);
            throw th;
        }
        this.mViewRef.get().setDrawingCacheEnabled(false);
        return bitmap;
    }
}
