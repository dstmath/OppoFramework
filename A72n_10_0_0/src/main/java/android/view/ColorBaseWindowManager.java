package android.view;

import android.content.Context;
import oppo.app.OppoCommonManager;

public abstract class ColorBaseWindowManager extends OppoCommonManager {
    public ColorBaseWindowManager() {
        super(Context.WINDOW_SERVICE);
    }
}
