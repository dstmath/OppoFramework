package com.android.server.wm;

import android.view.IApplicationToken;
import java.util.ArrayList;

public class TaskGroup {
    public int taskId = -1;
    public ArrayList<IApplicationToken> tokens = new ArrayList();

    public String toString() {
        return "id=" + this.taskId + " tokens=" + this.tokens;
    }
}
