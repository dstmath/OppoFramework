package com.color.inner.app.job;

import android.app.job.JobInfo;
import android.app.job.OppoBaseJobInfo;
import android.util.Log;
import com.color.util.ColorTypeCastingHelper;

public class JobInfoWrapper {
    private static final String TAG = "JobInfoWrapper";
    public static final int TYPE_PROTECT_FORE_FRAME = 0;
    public static final int TYPE_PROTECT_FORE_NET = 1;

    public static final class BuilderWrapper {
        public static void setRequiresBattIdle(JobInfo.Builder builder, boolean requiresBattIdle, int extra) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                baseBuilder.setRequiresBattIdle(requiresBattIdle, extra);
            }
        }

        public static boolean getRequiresBattIdle(JobInfo.Builder builder) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                return baseBuilder.mRequiresBattIdle;
            }
            Log.e(JobInfoWrapper.TAG, "no BaseJobInfo defined by OEM");
            return false;
        }

        public static void setColorJob(JobInfo.Builder builder, boolean isColorJob) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                baseBuilder.setOppoJob(isColorJob);
            }
        }

        public static boolean getColorJob(JobInfo.Builder builder) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                return baseBuilder.mIsOppoJob;
            }
            Log.e(JobInfoWrapper.TAG, "no BaseJobInfo defined by OEM");
            return false;
        }

        public static void setRequiresProtectFore(JobInfo.Builder builder, boolean requiresProtectFore) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                baseBuilder.setRequiresProtectFore(requiresProtectFore);
            }
        }

        public static void setRequiresProtectFore(JobInfo.Builder builder, boolean requiresProtectFore, int type) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                baseBuilder.setRequiresProtectFore(requiresProtectFore, type);
            }
        }

        public static boolean getRequiresProtectFore(JobInfo.Builder builder) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                return baseBuilder.mRequiresProtectFore;
            }
            Log.e(JobInfoWrapper.TAG, "no BaseJobInfo defined by OEM");
            return false;
        }

        public static void setHasCpuConstraint(JobInfo.Builder builder, boolean hasCpuConstraint) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                baseBuilder.setHasCpuConstraint(hasCpuConstraint);
            }
        }

        public static boolean getHasCpuConstraint(JobInfo.Builder builder) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                return baseBuilder.mHasCpuConstraint;
            }
            Log.e(JobInfoWrapper.TAG, "no BaseJobInfo defined by OEM");
            return false;
        }

        public static void setColorExtraStr(JobInfo.Builder builder, String str) {
            try {
                OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
                if (baseBuilder != null) {
                    baseBuilder.setOppoExtraStr(str);
                }
            } catch (Throwable e) {
                Log.d(JobInfoWrapper.TAG, e.toString());
            }
        }

        public static String getColorExtraStr(JobInfo.Builder builder) {
            OppoBaseJobInfo.BaseBuilder baseBuilder = typeCasting(builder);
            if (baseBuilder != null) {
                return baseBuilder.mOppoExtraStr;
            }
            Log.e(JobInfoWrapper.TAG, "no BaseJobInfo defined by OEM");
            return null;
        }

        private static OppoBaseJobInfo.BaseBuilder typeCasting(JobInfo.Builder builder) {
            return (OppoBaseJobInfo.BaseBuilder) ColorTypeCastingHelper.typeCasting(OppoBaseJobInfo.BaseBuilder.class, builder);
        }
    }
}
