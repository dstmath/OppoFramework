package android.support.v4.content.res;

import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;

public class ResourcesCompat {
    public Drawable getDrawable(Resources res, int id, Theme theme) throws NotFoundException {
        if (VERSION.SDK_INT >= 21) {
            return ResourcesCompatApi21.getDrawable(res, id, theme);
        }
        return res.getDrawable(id);
    }
}
