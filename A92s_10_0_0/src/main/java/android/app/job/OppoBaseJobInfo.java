package android.app.job;

import android.app.job.JobInfo;
import android.os.Parcel;
import com.color.util.ColorTypeCastingHelper;

public class OppoBaseJobInfo {
    public static final int LEVEL_CPU_ABNORMAL_HEAVY = 3;
    public static final int LEVEL_CPU_ABNORMAL_MIDDLE = 2;
    public static final int LEVEL_CPU_ABNORMAL_SLIGHT = 1;
    public static final int LEVEL_CPU_NORMAL = 0;
    public static final int TYPE_PROTECT_FORE_FRAME = 0;
    public static final int TYPE_PROTECT_FORE_NET = 1;
    private boolean hasCpuConstraint;
    protected long intervalMillis;
    private boolean isNotSysApp = false;
    private boolean isOppoJob;
    protected long maxExecutionDelayMillis;
    private String oppoExtraStr;
    private int protectForeType;
    private boolean requireBattIdle;
    private boolean requireProtectFore;

    public boolean isRequireProtectFore() {
        return this.requireProtectFore;
    }

    public boolean hasCpuConstraint() {
        return this.hasCpuConstraint;
    }

    public boolean getOppoJob() {
        return this.isOppoJob;
    }

    public String getOppoExtraStr() {
        return this.oppoExtraStr;
    }

    public int getProtectForeType() {
        return this.protectForeType;
    }

    public void initJobInfo(Parcel in) {
        boolean z = false;
        this.requireBattIdle = in.readInt() == 1;
        this.isOppoJob = in.readInt() == 1;
        this.requireProtectFore = in.readInt() == 1;
        this.hasCpuConstraint = in.readInt() == 1;
        this.oppoExtraStr = in.readString();
        this.protectForeType = in.readInt();
        if (in.readInt() == 1) {
            z = true;
        }
        this.isNotSysApp = z;
    }

    public void initJobInfo(BaseBuilder b) {
        this.requireBattIdle = b.mRequiresBattIdle;
        this.isOppoJob = b.mIsOppoJob;
        this.requireProtectFore = b.mRequiresProtectFore;
        this.hasCpuConstraint = b.mHasCpuConstraint;
        this.oppoExtraStr = b.mOppoExtraStr;
        this.protectForeType = b.mProtectForeType;
    }

    public void writeToParcelJobInfo(Parcel out, int flags) {
        out.writeInt(this.requireBattIdle ? 1 : 0);
        out.writeInt(this.isOppoJob ? 1 : 0);
        out.writeInt(this.requireProtectFore ? 1 : 0);
        out.writeInt(this.hasCpuConstraint ? 1 : 0);
        out.writeString(this.oppoExtraStr);
        out.writeInt(this.protectForeType);
        out.writeInt(this.isNotSysApp ? 1 : 0);
    }

    public static class BaseBuilder {
        public boolean mHasCpuConstraint;
        public boolean mIsOppoJob;
        public String mOppoExtraStr;
        public int mProtectForeType;
        public boolean mRequiresBattIdle;
        public boolean mRequiresProtectFore;

        public JobInfo.Builder setRequiresBattIdle(boolean requiresBattIdle, int extra) {
            this.mRequiresBattIdle = requiresBattIdle;
            return OppoBaseJobInfo.typeCasting(this);
        }

        public JobInfo.Builder setOppoJob(boolean isOppoJob) {
            this.mIsOppoJob = isOppoJob;
            return OppoBaseJobInfo.typeCasting(this);
        }

        public JobInfo.Builder setRequiresProtectFore(boolean requiresProtectFore) {
            setRequiresProtectFore(requiresProtectFore, 0);
            return OppoBaseJobInfo.typeCasting(this);
        }

        public JobInfo.Builder setRequiresProtectFore(boolean requiresProtectFore, int type) {
            this.mRequiresProtectFore = requiresProtectFore;
            this.mProtectForeType = type;
            return OppoBaseJobInfo.typeCasting(this);
        }

        public JobInfo.Builder setHasCpuConstraint(boolean hasCpuConstraint) {
            this.mHasCpuConstraint = hasCpuConstraint;
            return OppoBaseJobInfo.typeCasting(this);
        }

        public JobInfo.Builder setOppoExtraStr(String str) {
            this.mOppoExtraStr = str;
            return OppoBaseJobInfo.typeCasting(this);
        }
    }

    public boolean isRequireBattIdle() {
        return this.requireBattIdle;
    }

    public boolean isSystemApp() {
        return !this.isNotSysApp;
    }

    public void setSysApp(boolean isSys) {
        this.isNotSysApp = !isSys;
    }

    public void setMaxExecutionDelayMillis(long maxDelay) {
        this.maxExecutionDelayMillis = maxDelay;
    }

    public void setIntervalMillis(long interval) {
        this.intervalMillis = interval;
    }

    /* access modifiers changed from: private */
    public static JobInfo.Builder typeCasting(BaseBuilder builder) {
        return (JobInfo.Builder) ColorTypeCastingHelper.typeCasting(JobInfo.Builder.class, builder);
    }
}
