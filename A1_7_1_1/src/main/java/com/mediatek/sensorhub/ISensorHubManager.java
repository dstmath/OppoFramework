package com.mediatek.sensorhub;

import java.util.List;

public interface ISensorHubManager {
    public static final String SENSORHUB_SERVICE = "sensorhubservice";

    void addConfigurableGesture(int i, int i2);

    boolean cancelAction(int i);

    void cancelConfigurableGesture(int i, int i2);

    boolean enableGestureWakeup(boolean z);

    List<Integer> getCGestureList();

    List<Integer> getContextList();

    boolean isCGestureSupported(int i);

    boolean isContextSupported(int i);

    int requestAction(Condition condition, Action action);

    boolean updateCondition(int i, Condition condition);
}
