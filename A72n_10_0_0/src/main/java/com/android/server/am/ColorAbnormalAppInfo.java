package com.android.server.am;

import android.util.Slog;
import com.android.server.wm.startingwindow.ColorStartingWindowRUSHelper;
import java.util.HashMap;

public class ColorAbnormalAppInfo {
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

    static ColorAbnormalAppInfo builder(boolean isRestrict, String pkgName, int type) {
        ColorAbnormalAppInfo appInfo = new ColorAbnormalAppInfo();
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
        if (type == 20) {
            this.mRepeatRestrictNum++;
        } else if (type != 21) {
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
                case ColorStartingWindowRUSHelper.TASK_SNAPSHOT_BLACK_TOKEN_START_FROM_LAUNCHER /* 7 */:
                    this.mPRestrictNum++;
                    return;
                case 8:
                    this.mPPassNum++;
                    return;
                case ColorStartingWindowRUSHelper.FORCE_USE_COLOR_DRAWABLE_WHEN_SPLASH_WINDOW_TRANSLUCENT /* 9 */:
                    this.mRRestrictNum++;
                    return;
                case ColorStartingWindowRUSHelper.STARTING_WINDOW_EXIT_LONG_DURATION_PACKAGE /* 10 */:
                    this.mRPassNum++;
                    return;
                case ColorStartingWindowRUSHelper.SNAPSHOT_FORCE_CLEAR_WHEN_DIFF_ORIENTATION /* 11 */:
                    this.mOtherRestrictNum++;
                    return;
                case ColorStartingWindowRUSHelper.USE_TRANSLUCENT_DRAWABLE_FOR_SPLASH_WINDOW /* 12 */:
                    this.mOtherPassNum++;
                    return;
                default:
                    return;
            }
        } else {
            this.mRepeatPassNum++;
        }
    }

    /* access modifiers changed from: protected */
    public HashMap<String, String> getAbnormalAppMap() {
        HashMap<String, String> appMap = new HashMap<>();
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
        Slog.d("ColorAbnormalAppManager", infoToString());
    }

    public String infoToString() {
        StringBuilder sb = new StringBuilder(256);
        sb.append(getPkgName());
        sb.append(" ");
        sb.append("a_rnum");
        sb.append(":");
        sb.append(Integer.toString(this.mARestrictNum));
        sb.append(" ");
        sb.append("a_pnum");
        sb.append(":");
        sb.append(Integer.toString(this.mAPassNum));
        sb.append(" ");
        sb.append("s_rnum");
        sb.append(":");
        sb.append(Integer.toString(this.mSRestrictNum));
        sb.append(" ");
        sb.append("s_pnum");
        sb.append(":");
        sb.append(Integer.toString(this.mSPassNum));
        sb.append(" ");
        sb.append("b_rnum");
        sb.append(":");
        sb.append(Integer.toString(this.mBRestrictNum));
        sb.append(" ");
        sb.append("b_pnum");
        sb.append(":");
        sb.append(Integer.toString(this.mBPassNum));
        sb.append(" ");
        sb.append("p_rnum");
        sb.append(":");
        sb.append(Integer.toString(this.mPRestrictNum));
        sb.append(" ");
        sb.append("p_pnum");
        sb.append(":");
        sb.append(Integer.toString(this.mPPassNum));
        sb.append(" ");
        sb.append("r_rnum");
        sb.append(":");
        sb.append(Integer.toString(this.mRRestrictNum));
        sb.append(" ");
        sb.append("r_pnum");
        sb.append(":");
        sb.append(Integer.toString(this.mRPassNum));
        sb.append(" ");
        sb.append("other_rnum");
        sb.append(":");
        sb.append(Integer.toString(this.mOtherRestrictNum));
        sb.append(" ");
        sb.append("other_pnum");
        sb.append(":");
        sb.append(Integer.toString(this.mOtherPassNum));
        sb.append(" ");
        sb.append("repeat_rnum");
        sb.append(":");
        sb.append(Integer.toString(this.mRepeatRestrictNum));
        sb.append(" ");
        sb.append("repeat_pnum");
        sb.append(":");
        sb.append(Integer.toString(this.mRepeatPassNum));
        return sb.toString();
    }
}
