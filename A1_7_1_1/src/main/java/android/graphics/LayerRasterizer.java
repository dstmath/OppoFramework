package android.graphics;

import android.hardware.camera2.params.TonemapCurve;

@Deprecated
public class LayerRasterizer extends Rasterizer {
    private static native void nativeAddLayer(long j, long j2, float f, float f2);

    private static native long nativeConstructor();

    public LayerRasterizer() {
        this.native_instance = nativeConstructor();
    }

    public void addLayer(Paint paint, float dx, float dy) {
        nativeAddLayer(this.native_instance, paint.getNativeInstance(), dx, dy);
    }

    public void addLayer(Paint paint) {
        nativeAddLayer(this.native_instance, paint.getNativeInstance(), TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK);
    }
}
