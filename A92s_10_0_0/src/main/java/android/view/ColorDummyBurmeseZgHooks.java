package android.view;

import android.content.Context;
import android.content.res.Configuration;

public class ColorDummyBurmeseZgHooks implements IColorBurmeseZgHooks {
    @Override // android.view.IColorBurmeseZgHooks
    public void initBurmeseZgFlag(Context context) {
    }

    @Override // android.view.IColorBurmeseZgHooks
    public void updateBurmeseZgFlag(Context context) {
    }

    @Override // android.view.IColorBurmeseZgHooks
    public boolean getZgFlag() {
        return false;
    }

    @Override // android.view.IColorBurmeseZgHooks
    public void updateBurmeseEncodingForUser(Context context, Configuration config, int userId) {
    }
}
