package com.android.server.wm;

import android.app.Dialog;
import android.content.Context;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.TextView;

public final class LaunchWarningWindow extends Dialog {
    public LaunchWarningWindow(Context context, ActivityRecord cur, ActivityRecord next) {
        super(context, 16974907);
        requestWindowFeature(3);
        getWindow().setType(2003);
        getWindow().addFlags(24);
        setContentView(17367176);
        setTitle(context.getText(17040232));
        TypedValue out = new TypedValue();
        getContext().getTheme().resolveAttribute(16843605, out, true);
        getWindow().setFeatureDrawableResource(3, out.resourceId);
        ((ImageView) findViewById(16909282)).setImageDrawable(next.info.applicationInfo.loadIcon(context.getPackageManager()));
        ((TextView) findViewById(16909283)).setText(context.getResources().getString(17040231, next.info.applicationInfo.loadLabel(context.getPackageManager()).toString()));
        ((ImageView) findViewById(16909200)).setImageDrawable(cur.info.applicationInfo.loadIcon(context.getPackageManager()));
        ((TextView) findViewById(16909201)).setText(context.getResources().getString(17040230, cur.info.applicationInfo.loadLabel(context.getPackageManager()).toString()));
    }
}
