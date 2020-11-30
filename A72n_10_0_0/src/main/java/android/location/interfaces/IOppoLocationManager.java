package android.location.interfaces;

import android.location.LocAppsOp;
import java.util.List;

public interface IOppoLocationManager {
    List<String> getInUsePackagesList();

    void getLocAppsOp(int i, LocAppsOp locAppsOp);

    void setLocAppsOp(int i, LocAppsOp locAppsOp);
}
