package android.support.v4.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.os.Parcelable;
import android.view.View;
import android.widget.ImageView;
import java.util.List;
import java.util.Map;

public abstract class SharedElementCallback {
    private Matrix mTempMatrix;

    public void onSharedElementStart(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onSharedElementEnd(List<String> list, List<View> list2, List<View> list3) {
    }

    public void onRejectSharedElements(List<View> list) {
    }

    public void onMapSharedElements(List<String> list, Map<String, View> map) {
    }

    public Parcelable onCaptureSharedElementSnapshot(View sharedElement, Matrix viewToGlobalMatrix, RectF screenBounds) {
        int bitmapWidth = Math.round(screenBounds.width());
        int bitmapHeight = Math.round(screenBounds.height());
        if (bitmapWidth <= 0 || bitmapHeight <= 0) {
            return null;
        }
        if (this.mTempMatrix == null) {
            this.mTempMatrix = new Matrix();
        }
        this.mTempMatrix.set(viewToGlobalMatrix);
        this.mTempMatrix.postTranslate(-screenBounds.left, -screenBounds.top);
        Bitmap bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.concat(this.mTempMatrix);
        sharedElement.draw(canvas);
        return bitmap;
    }

    public View onCreateSnapshotView(Context context, Parcelable snapshot) {
        if (!(snapshot instanceof Bitmap)) {
            return null;
        }
        Bitmap bitmap = (Bitmap) snapshot;
        ImageView view = new ImageView(context);
        view.setImageBitmap(bitmap);
        return view;
    }
}
