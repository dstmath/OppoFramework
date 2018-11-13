package android.view.animation;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.util.AttributeSet;

public class OppoAnimationUtils {
    public static BaseInterpolator createInterpolatorFromXml(String name, Resources res, Theme theme, AttributeSet attrs) {
        if (name.equals("oppoDecelerateInterpolator")) {
            return new OppoDecelerateInterpolator();
        }
        if (name.equals("oppoAccelerateDecelerateInterpolator")) {
            return new OppoAccelerateDecelerateInterpolator();
        }
        if (name.equals("oppoBezierInterpolator")) {
            return new OppoBezierInterpolator(res, theme, attrs);
        }
        return null;
    }
}
