package com.coloros.deepthinker.sdk.aidl.proton.appactionpredict;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class PredictAABResult implements Parcelable {
    public static final Parcelable.Creator<PredictAABResult> CREATOR = new Parcelable.Creator<PredictAABResult>() {
        /* class com.coloros.deepthinker.sdk.aidl.proton.appactionpredict.PredictAABResult.AnonymousClass1 */

        @Override // android.os.Parcelable.Creator
        public PredictAABResult createFromParcel(Parcel in) {
            PredictAABResult predictResult = new PredictAABResult();
            HashMap unused = predictResult.mPredictResultMap = in.readHashMap(PredictAABResult.class.getClassLoader());
            return predictResult;
        }

        @Override // android.os.Parcelable.Creator
        public PredictAABResult[] newArray(int size) {
            return new PredictAABResult[size];
        }
    };
    public static final int INVALID_BUCKET = -1;
    private static final String TAG = "PredictAABResult";
    /* access modifiers changed from: private */
    public HashMap mPredictResultMap;

    public PredictAABResult(ArrayList<String> activeList, ArrayList<String> workingsetList, ArrayList<String> frequentList, ArrayList<String> rareList) {
        this.mPredictResultMap = new HashMap();
        putArrayToMap(activeList, 10);
        putArrayToMap(workingsetList, 20);
        putArrayToMap(frequentList, 30);
        putArrayToMap(rareList, 40);
    }

    private PredictAABResult() {
    }

    @Override // android.os.Parcelable
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeMap(this.mPredictResultMap);
    }

    @Override // android.os.Parcelable
    public int describeContents() {
        return 0;
    }

    public Set<String> getPackages() {
        HashMap hashMap = this.mPredictResultMap;
        if (hashMap == null) {
            return null;
        }
        return hashMap.keySet();
    }

    public int getPredictAppStandbyBucket(String packageName) {
        Object appStandbyBucket = this.mPredictResultMap.get(packageName);
        if (appStandbyBucket instanceof Integer) {
            return ((Integer) appStandbyBucket).intValue();
        }
        return -1;
    }

    private void putArrayToMap(ArrayList<String> pkgs, int appStandbyBucket) {
        Iterator<String> it = pkgs.iterator();
        while (it.hasNext()) {
            this.mPredictResultMap.put(it.next(), Integer.valueOf(appStandbyBucket));
        }
    }
}
