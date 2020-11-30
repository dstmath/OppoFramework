package android.view;

import com.color.screenshot.ColorLongshotViewRoot;

public interface IColorBaseViewRoot {
    ColorLongshotViewRoot getLongshotViewRoot();

    default ColorViewRootImplHooks getColorViewRootImplHooks() {
        return null;
    }

    default OppoBurnConfigData getOppoBurnConfigData() {
        return null;
    }
}
