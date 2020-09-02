package com.mediatek.common.widget;

import android.os.Bundle;

public interface IMtkWidget {
    void enterAppwidgetScreen();

    int getPermittedCount();

    int getScreen();

    int getWidgetId();

    void leaveAppwidgetScreen();

    void moveIn(int i);

    boolean moveOut(int i);

    void onPauseWhenShown(int i);

    void onRestoreInstanceState(Bundle bundle);

    void onResumeWhenShown(int i);

    void onSaveInstanceState(Bundle bundle);

    void setScreen(int i);

    void setWidgetId(int i);

    void startCovered(int i);

    void startDrag();

    void stopCovered(int i);

    void stopDrag();
}
