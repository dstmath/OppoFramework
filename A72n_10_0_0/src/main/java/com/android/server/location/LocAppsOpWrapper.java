package com.android.server.location;

import android.location.LocAppsOp;
import java.util.HashMap;

public class LocAppsOpWrapper {
    private LocAppsOp mLocAppsOp = new LocAppsOp();

    public int getOpLevel() {
        return this.mLocAppsOp.getOpLevel();
    }

    public void setOpLevel(int opLevel) {
        this.mLocAppsOp.setOpLevel(opLevel);
    }

    public HashMap<String, Integer> getAppsOp() {
        return this.mLocAppsOp.getAppsOp();
    }

    public void setAppsOp(HashMap<String, Integer> opList) {
        this.mLocAppsOp.setAppsOp(opList);
    }

    public void setAppOp(String name, int op) {
        this.mLocAppsOp.setAppOp(name, op);
    }

    public LocAppsOp getLocAppsOp() {
        return this.mLocAppsOp;
    }
}
