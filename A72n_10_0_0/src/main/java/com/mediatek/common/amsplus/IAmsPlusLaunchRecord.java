package com.mediatek.common.amsplus;

import android.content.Intent;
import java.util.ArrayList;

public interface IAmsPlusLaunchRecord {
    Intent getIntent();

    IAmsPlusProcessRecord getLaunchedProcess();

    ArrayList<IAmsPlusProcessRecord> getRecords();

    IAmsPlusProcessRecord getWaitProcess();

    boolean isLaunchingHomeActivity();
}
