package com.android.server.secrecy.work;

import android.content.Context;
import android.content.pm.ActivityInfo;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class ActivityEncryptWork extends SecrecyWork {
    private static String TAG = "SecrecyService.ActivityEncryptWork";
    private final ActivityConfig mActivityCofig;

    public ActivityEncryptWork(Context context, String name) {
        super(context, name);
        this.mActivityCofig = ActivityConfig.getInstance();
    }

    public ActivityEncryptWork(Context context) {
        this(context, TAG);
    }

    public boolean preWork(ActivityInfo info) {
        return this.mActivityCofig.isInActivityConfig(info);
    }

    public boolean doWork(ActivityInfo info) {
        return true;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
        super.dump(fd, pw, prefix);
        pw.print(prefix);
        this.mActivityCofig.dump(fd, pw, prefix + "    ");
    }
}
