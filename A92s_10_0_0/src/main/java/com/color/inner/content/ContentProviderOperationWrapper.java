package com.color.inner.content;

import android.content.ContentProviderOperation;
import android.util.Log;

public class ContentProviderOperationWrapper {
    private static final String TAG = "ContentProviderOperationWrapper";

    private ContentProviderOperationWrapper() {
    }

    public static int getType(ContentProviderOperation cpo) {
        try {
            return cpo.getType();
        } catch (Throwable e) {
            Log.e(TAG, e.toString());
            return -1;
        }
    }
}
