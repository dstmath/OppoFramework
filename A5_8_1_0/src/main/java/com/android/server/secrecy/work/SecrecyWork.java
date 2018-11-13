package com.android.server.secrecy.work;

import android.content.Context;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class SecrecyWork {
    private final Context mContext;
    private final String mName;
    private final boolean mPolicy = true;

    public SecrecyWork(Context context, String name) {
        this.mContext = context;
        this.mName = name;
    }

    public String getName() {
        return this.mName;
    }

    public void doWork() {
    }

    public boolean getWorkPolicy() {
        return true;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String prefix) {
    }
}
