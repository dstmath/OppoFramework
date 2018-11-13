package com.mediatek.common.sms;

import android.app.PendingIntent;
import android.content.Context;
import java.util.ArrayList;

public interface IDataOnlySmsFwkExt {
    boolean is4GDataOnlyMode(PendingIntent pendingIntent, int i, Context context);

    boolean is4GDataOnlyMode(ArrayList<PendingIntent> arrayList, int i, Context context);
}
