package com.android.server.am;

import android.util.Slog;
import java.util.HashMap;

public class OppoAbnormalAppInfo {
    int mAPassNum = 0;
    int mARestrictNum = 0;
    int mBPassNum = 0;
    int mBRestrictNum = 0;
    int mOtherPassNum = 0;
    int mOtherRestrictNum = 0;
    int mPPassNum = 0;
    int mPRestrictNum = 0;
    String mPkgName = "";
    int mRPassNum = 0;
    int mRRestrictNum = 0;
    int mRepeatPassNum = 0;
    int mRepeatRestrictNum = 0;
    int mSPassNum = 0;
    int mSRestrictNum = 0;

    static OppoAbnormalAppInfo builder(boolean isRestrict, String pkgName, int type) {
        OppoAbnormalAppInfo appInfo = new OppoAbnormalAppInfo();
        appInfo.setPkgName(pkgName);
        appInfo.increaseTypeCount(isRestrict, type);
        return appInfo;
    }

    public String getPkgName() {
        return this.mPkgName;
    }

    public void setPkgName(String pkgName) {
        this.mPkgName = pkgName;
    }

    public void increaseTypeCount(boolean isRestrict, int type) {
        switch (type) {
            case 1:
                this.mARestrictNum++;
                return;
            case 2:
                this.mAPassNum++;
                return;
            case 3:
                this.mBRestrictNum++;
                return;
            case 4:
                this.mBPassNum++;
                return;
            case 5:
                this.mSRestrictNum++;
                return;
            case 6:
                this.mSPassNum++;
                return;
            case 7:
                this.mPRestrictNum++;
                return;
            case 8:
                this.mPPassNum++;
                return;
            case 9:
                this.mRRestrictNum++;
                return;
            case 10:
                this.mRPassNum++;
                return;
            case 11:
                this.mOtherRestrictNum++;
                return;
            case 12:
                this.mOtherPassNum++;
                return;
            case 20:
                this.mRepeatRestrictNum++;
                return;
            case 21:
                this.mRepeatPassNum++;
                return;
            default:
                return;
        }
    }

    protected HashMap<String, String> getAbnormalAppMap() {
        HashMap<String, String> appMap = new HashMap();
        appMap.put("pkg", getPkgName());
        appMap.put("a_rnum", Integer.toString(this.mARestrictNum));
        appMap.put("a_pnum", Integer.toString(this.mAPassNum));
        appMap.put("s_rnum", Integer.toString(this.mSRestrictNum));
        appMap.put("s_pnum", Integer.toString(this.mSPassNum));
        appMap.put("b_rnum", Integer.toString(this.mBRestrictNum));
        appMap.put("b_pnum", Integer.toString(this.mBPassNum));
        appMap.put("p_rnum", Integer.toString(this.mPRestrictNum));
        appMap.put("p_pnum", Integer.toString(this.mPPassNum));
        appMap.put("r_rnum", Integer.toString(this.mRRestrictNum));
        appMap.put("r_pnum", Integer.toString(this.mRPassNum));
        appMap.put("other_rnum", Integer.toString(this.mOtherRestrictNum));
        appMap.put("other_pnum", Integer.toString(this.mOtherPassNum));
        appMap.put("repeat_rnum", Integer.toString(this.mRepeatRestrictNum));
        appMap.put("repeat_pnum", Integer.toString(this.mRepeatPassNum));
        return appMap;
    }

    public void cleanup() {
        this.mPkgName = "";
        this.mARestrictNum = 0;
        this.mAPassNum = 0;
        this.mBRestrictNum = 0;
        this.mBPassNum = 0;
        this.mSRestrictNum = 0;
        this.mSPassNum = 0;
        this.mPRestrictNum = 0;
        this.mPPassNum = 0;
        this.mRRestrictNum = 0;
        this.mRPassNum = 0;
        this.mOtherRestrictNum = 0;
        this.mOtherPassNum = 0;
        this.mRepeatRestrictNum = 0;
        this.mRepeatPassNum = 0;
    }

    public void dumpInfo() {
        Slog.d(OppoAbnormalAppManager.TAG, infoToString());
    }

    public String infoToString() {
        String str = "";
        StringBuilder sb = new StringBuilder(256);
        sb.append(getPkgName()).append(" ").append("a_rnum").append(":").append(Integer.toString(this.mARestrictNum)).append(" ").append("a_pnum").append(":").append(Integer.toString(this.mAPassNum)).append(" ").append("s_rnum").append(":").append(Integer.toString(this.mSRestrictNum)).append(" ").append("s_pnum").append(":").append(Integer.toString(this.mSPassNum)).append(" ").append("b_rnum").append(":").append(Integer.toString(this.mBRestrictNum)).append(" ").append("b_pnum").append(":").append(Integer.toString(this.mBPassNum)).append(" ").append("p_rnum").append(":").append(Integer.toString(this.mPRestrictNum)).append(" ").append("p_pnum").append(":").append(Integer.toString(this.mPPassNum)).append(" ").append("r_rnum").append(":").append(Integer.toString(this.mRRestrictNum)).append(" ").append("r_pnum").append(":").append(Integer.toString(this.mRPassNum)).append(" ").append("other_rnum").append(":").append(Integer.toString(this.mOtherRestrictNum)).append(" ").append("other_pnum").append(":").append(Integer.toString(this.mOtherPassNum)).append(" ").append("repeat_rnum").append(":").append(Integer.toString(this.mRepeatRestrictNum)).append(" ").append("repeat_pnum").append(":").append(Integer.toString(this.mRepeatPassNum));
        return sb.toString();
    }
}
