package android.app;

import android.os.Bundle;

public abstract class OppoActivityManagerInternal {
    public abstract int getProcPid(int i);

    public abstract int startActivityAsUserEmpty(Bundle bundle);
}
