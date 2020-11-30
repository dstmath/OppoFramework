package androidx.core.app;

import android.app.Activity;
import android.content.Intent;
import androidx.core.content.ContextCompat;

public class ActivityCompat extends ContextCompat {
    private static PermissionCompatDelegate sDelegate;

    public interface PermissionCompatDelegate {
        boolean onActivityResult(Activity activity, int i, int i2, Intent intent);
    }

    public static PermissionCompatDelegate getPermissionCompatDelegate() {
        return sDelegate;
    }
}
