package android.app;

import android.content.Context;
import android.text.TextUtils;
import oppo.util.OppoMultiLauncherUtil;

public class ColorBaseNotification {
    private static final String TAG = "ColorBaseNotification";

    public static class Builder {
        private Context mContext;
        private Notification mN;

        public Builder(Context context, Notification notification) {
            this.mContext = context;
            this.mN = notification;
        }

        /* access modifiers changed from: protected */
        public String oppoLoadMultiHeaderAppName() {
            CharSequence name = null;
            if (this.mContext.getUserId() == 999) {
                name = OppoMultiLauncherUtil.getInstance().getAliasByPackage(this.mContext.getPackageName());
            }
            if (TextUtils.isEmpty(name)) {
                return null;
            }
            return String.valueOf(name);
        }
    }
}
