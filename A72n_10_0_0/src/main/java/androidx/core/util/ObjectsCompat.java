package androidx.core.util;

import android.os.Build;
import java.util.Objects;

public class ObjectsCompat {
    public static boolean equals(Object a, Object b) {
        if (Build.VERSION.SDK_INT >= 19) {
            return Objects.equals(a, b);
        }
        return a == b || (a != null && a.equals(b));
    }
}
