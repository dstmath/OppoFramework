package android.content;

import android.os.OppoThermalState;
import java.util.List;

public abstract class OppoBatteryStatsInternal {
    public abstract List<String> getUid0ProcessListImpl();

    public abstract List<String> getUid1kProcessListImpl();

    public abstract List<String> getUidPowerListImpl();

    public abstract void noteScreenBrightnessModeChangedImpl(boolean z);

    public abstract void restOpppBatteryStatsImpl();

    public abstract void setThermalConfigImpl();

    public abstract void setThermalStateImpl(OppoThermalState oppoThermalState);
}
