package androidx.core.widget;

import android.os.Build;
import android.widget.EdgeEffect;

public final class EdgeEffectCompat {
    public static void onPull(EdgeEffect edgeEffect, float deltaDistance, float displacement) {
        if (Build.VERSION.SDK_INT >= 21) {
            edgeEffect.onPull(deltaDistance, displacement);
        } else {
            edgeEffect.onPull(deltaDistance);
        }
    }
}
