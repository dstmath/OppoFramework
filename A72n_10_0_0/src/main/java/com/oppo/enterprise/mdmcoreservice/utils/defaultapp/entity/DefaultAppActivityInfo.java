package com.oppo.enterprise.mdmcoreservice.utils.defaultapp.entity;

import android.content.pm.ActivityInfo;
import java.util.ArrayList;
import java.util.List;

public class DefaultAppActivityInfo {
    private final List<ActivityInfo> mActivityInfoList = new ArrayList();
    private final List<Integer> mPriorityList = new ArrayList();

    public void addActivityInfo(ActivityInfo activityInfo) {
        this.mActivityInfoList.add(activityInfo);
    }

    public List<ActivityInfo> getActivityInfo() {
        return this.mActivityInfoList;
    }

    public void addPriority(int priority) {
        this.mPriorityList.add(Integer.valueOf(priority));
    }

    public List<Integer> getPriorityList() {
        return this.mPriorityList;
    }
}
