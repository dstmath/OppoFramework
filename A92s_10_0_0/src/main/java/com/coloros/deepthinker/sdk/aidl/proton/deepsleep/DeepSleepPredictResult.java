package com.coloros.deepthinker.sdk.aidl.proton.deepsleep;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.List;

public class DeepSleepPredictResult implements Parcelable {
    public static final Parcelable.Creator<DeepSleepPredictResult> CREATOR = new Parcelable.Creator<DeepSleepPredictResult>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.deepsleep.DeepSleepPredictResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public DeepSleepPredictResult createFromParcel(Parcel source) {
            DeepSleepPredictResult result = new DeepSleepPredictResult(null, null);
            String mResultTypeString = source.readString();
            if (mResultTypeString != null) {
                PredictResultType unused = result.mResultType = PredictResultType.valueOf(mResultTypeString);
            }
            if (result.mDeepSleepClusterList == null) {
                List unused2 = result.mDeepSleepClusterList = new ArrayList();
            }
            source.readTypedList(result.mDeepSleepClusterList, DeepSleepCluster.CREATOR);
            return result;
        }

        @Override // android.os.Parcelable.Creator
        public DeepSleepPredictResult[] newArray(int size) {
            return new DeepSleepPredictResult[size];
        }
    };
    private static final String TAG = "DeepSleepPredictResult";
    /* access modifiers changed from: private */
    public List<DeepSleepCluster> mDeepSleepClusterList = null;
    /* access modifiers changed from: private */
    public PredictResultType mResultType = PredictResultType.PREDICT_RESULT_TYPE_UNKNOWN;

    public DeepSleepPredictResult(PredictResultType type, List<DeepSleepCluster> list) {
        this.mResultType = type;
        this.mDeepSleepClusterList = list;
    }

    public PredictResultType getResultType() {
        return this.mResultType;
    }

    public List<DeepSleepCluster> getDeepSleepClusterList() {
        return this.mDeepSleepClusterList;
    }

    public void setResultType(PredictResultType type) {
        this.mResultType = type;
    }

    public void setClusterList(List<DeepSleepCluster> clusterList) {
        this.mDeepSleepClusterList = clusterList;
    }

    public String toString() {
        StringBuilder str = new StringBuilder("DeepSleepPredictResult:resultType=" + this.mResultType);
        List<DeepSleepCluster> list = this.mDeepSleepClusterList;
        if (list != null && list.size() > 0) {
            for (DeepSleepCluster cluster : this.mDeepSleepClusterList) {
                str.append(cluster.toString());
            }
        }
        return str.toString();
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        PredictResultType predictResultType = this.mResultType;
        dest.writeString(predictResultType == null ? null : predictResultType.name());
        dest.writeTypedList(this.mDeepSleepClusterList);
    }
}
