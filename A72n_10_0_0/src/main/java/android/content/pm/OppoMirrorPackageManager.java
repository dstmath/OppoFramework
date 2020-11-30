package android.content.pm;

import android.content.ComponentName;
import android.graphics.drawable.Drawable;
import com.oppo.reflect.MethodParams;
import com.oppo.reflect.RefClass;
import com.oppo.reflect.RefMethod;

public class OppoMirrorPackageManager {
    public static Class<?> TYPE = RefClass.load(OppoMirrorPackageManager.class, PackageManager.class);
    @MethodParams({ComponentName.class})
    public static RefMethod<Void> clearCachedIconForActivity;
    @MethodParams({Drawable.class, boolean.class})
    public static RefMethod<Drawable> getUxIconDrawable;
}
