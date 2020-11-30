package com.android.server.job.controllers;

import android.app.job.JobInfo;
import android.app.job.OppoBaseJobInfo;
import android.common.OppoFeatureCache;
import android.os.Parcel;
import com.android.server.IColorDeepSleepHelper;
import com.color.util.ColorTypeCastingHelper;
import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class OppoBaseJobStatus {
    static final int CONSTRAINT_BATTERY_DILE = 1024;
    public static final int CONSTRAINT_CPU = 4096;
    public static final int CONSTRAINT_FORE_APP = 2048;
    public final AtomicInteger cpuLevel = new AtomicInteger();
    boolean deviceIdleMode;
    private boolean isOppoJob;
    IColorJobStatusInner mColorJSInner = null;
    public final AtomicInteger oldCpuLevel = new AtomicInteger();

    /* access modifiers changed from: protected */
    public abstract int getIntCcov();

    /* access modifiers changed from: protected */
    public abstract int getIntSocv();

    /* access modifiers changed from: protected */
    public abstract JobInfo getJobInfo();

    /* access modifiers changed from: protected */
    public abstract int getRequiredConstraints();

    public abstract int getSatisfiedConstraintFlags();

    /* access modifiers changed from: protected */
    public abstract int getSatisfiedConstraints();

    /* access modifiers changed from: protected */
    public abstract boolean setOppoConstraintSatisfied(int i, boolean z);

    public OppoBaseJobStatus() {
    }

    public OppoBaseJobStatus(Parcel in) {
    }

    public int initRequiredConstraints(JobInfo job) {
        int requiredConstraints = 0;
        OppoBaseJobInfo baseJobInfo = typeCasting(job);
        if (baseJobInfo != null) {
            if (baseJobInfo.isRequireBattIdle()) {
                requiredConstraints = 0 | 1024;
            }
            if (baseJobInfo.isRequireProtectFore()) {
                requiredConstraints |= 2048;
            }
            if (baseJobInfo.hasCpuConstraint()) {
                requiredConstraints |= 4096;
            }
            this.isOppoJob = baseJobInfo.getOppoJob();
        }
        return requiredConstraints;
    }

    public boolean hasProtectForeConstraint() {
        return (getRequiredConstraints() & 2048) != 0;
    }

    public boolean hasCpuConstraint() {
        return (getRequiredConstraints() & 4096) != 0;
    }

    public boolean isOppoJob() {
        return this.isOppoJob;
    }

    public String getOppoExtraStr() {
        OppoBaseJobInfo baseJobInfo = typeCasting(getJobInfo());
        if (baseJobInfo != null) {
            return baseJobInfo.getOppoExtraStr();
        }
        return null;
    }

    public int getProtectForeType() {
        OppoBaseJobInfo baseJobInfo = typeCasting(getJobInfo());
        if (baseJobInfo != null) {
            return baseJobInfo.getProtectForeType();
        }
        return 0;
    }

    public boolean setProtectForeConstraintSatisfied(boolean state) {
        return setOppoConstraintSatisfied(2048, state);
    }

    public boolean setCpuConstraintSatisfied(boolean state) {
        return setOppoConstraintSatisfied(4096, state);
    }

    public boolean isCpuConstraintSatisfied() {
        return (getSatisfiedConstraints() & 4096) != 0;
    }

    /* access modifiers changed from: package-private */
    public void dumpConstraints(PrintWriter pw, int constraints) {
        if ((constraints & 1024) != 0) {
            pw.print(" BATT_IDLE");
        }
        if ((constraints & 2048) != 0) {
            pw.print(" PROTECT_FORE");
        }
        if ((constraints & 4096) != 0) {
            pw.print(" CPU");
        }
    }

    public boolean hasBattIdleConstraint() {
        return (getRequiredConstraints() & 1024) != 0;
    }

    public boolean setBattIdleConstraintSatisfied(boolean state) {
        return setOppoConstraintSatisfied(1024, state);
    }

    public void setDeviceIdleMode(boolean isDeviceIdleMode) {
        this.deviceIdleMode = isDeviceIdleMode;
    }

    /* access modifiers changed from: protected */
    public void initColorDeepSleepHelper(JobStatus jobs, JobInfo job) {
        try {
            this.mColorJSInner = new ColorJobStatusInner(job);
            OppoFeatureCache.get(IColorDeepSleepHelper.DEFAULT).init(jobs, this.mColorJSInner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final class ColorJobStatusInner implements IColorJobStatusInner {
        JobInfo job;

        public ColorJobStatusInner(JobInfo job2) {
            this.job = job2;
        }

        @Override // com.android.server.job.controllers.IColorJobStatusInner
        public JobInfo getJobInfo() {
            return this.job;
        }

        @Override // com.android.server.job.controllers.IColorJobStatusInner
        public int getIntRequiredConstraintsVal() {
            return OppoBaseJobStatus.this.getRequiredConstraints();
        }

        @Override // com.android.server.job.controllers.IColorJobStatusInner
        public int getIntSatisfiedConstraintsVal() {
            return OppoBaseJobStatus.this.getSatisfiedConstraintFlags();
        }

        @Override // com.android.server.job.controllers.IColorJobStatusInner
        public int getIntConstraintsOfInterestVal() {
            return OppoBaseJobStatus.this.getIntCcov();
        }

        @Override // com.android.server.job.controllers.IColorJobStatusInner
        public int getIntSoftOverrideConstraintsVal() {
            return OppoBaseJobStatus.this.getIntSocv();
        }
    }

    private static OppoBaseJobInfo typeCasting(JobInfo jobInfo) {
        return (OppoBaseJobInfo) ColorTypeCastingHelper.typeCasting(OppoBaseJobInfo.class, jobInfo);
    }
}
