package com.android.server.location.interfaces;

import android.content.Context;
import android.location.LocAppsOp;
import java.util.List;

public interface IPswLocationManagerServiceEx extends IOppoLocationManagerServiceEx {
    Context getContext();

    List<String> getInUsePackagesList();

    void getLocAppsOp(int i, LocAppsOp locAppsOp);

    void setLocAppsOp(int i, LocAppsOp locAppsOp);
}
