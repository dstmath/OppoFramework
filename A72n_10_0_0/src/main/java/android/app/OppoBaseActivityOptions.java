package android.app;

public abstract class OppoBaseActivityOptions {
    protected static final String KEY_RP_LAUNCH_HINT = "android:activity.isRPLaunch";
    protected boolean mIsRPLaunch;

    public boolean isRPLaunch() {
        return this.mIsRPLaunch;
    }

    public void setRPLaunch(boolean rpLaunch) {
        this.mIsRPLaunch = rpLaunch;
    }
}
