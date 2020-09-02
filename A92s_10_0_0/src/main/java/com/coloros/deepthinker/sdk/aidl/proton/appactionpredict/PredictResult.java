package com.coloros.deepthinker.sdk.aidl.proton.appactionpredict;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.Map;
import java.util.Set;

public class PredictResult implements Parcelable {
    public static final Parcelable.Creator<PredictResult> CREATOR = new Parcelable.Creator<PredictResult>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.PredictResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PredictResult createFromParcel(Parcel in) {
            PredictResult predictResult = new PredictResult();
            int unused = predictResult.mPredictTime = in.readInt();
            Map unused2 = predictResult.mPredictResultMap = in.readHashMap(PredictResult.class.getClassLoader());
            return predictResult;
        }

        @Override // android.os.Parcelable.Creator
        public PredictResult[] newArray(int size) {
            return new PredictResult[size];
        }
    };
    public static final int INVALID_TIME = -1;
    private static final String TAG = "PredictResult";
    /* access modifiers changed from: private */
    public Map mPredictResultMap;
    /* access modifiers changed from: private */
    public int mPredictTime;

    private PredictResult() {
    }

    public PredictResult(int predictTime, Map predictResultMap) {
        this.mPredictTime = predictTime;
        this.mPredictResultMap = predictResultMap;
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mPredictTime);
        dest.writeMap(this.mPredictResultMap);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Set<String> getPackages() {
        Map map = this.mPredictResultMap;
        if (map == null) {
            return null;
        }
        return map.keySet();
    }

    public int getCountdownTimeByPackage(String packageName) {
        Object countdownTime = this.mPredictResultMap.get(packageName);
        if (countdownTime instanceof String) {
            return Integer.parseInt((String) countdownTime);
        }
        return -1;
    }

    public int getPredictTime() {
        return this.mPredictTime;
    }
}
