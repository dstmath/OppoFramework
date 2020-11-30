package android.view;

public class OppoBurnConfigData {
    private long mBurnCfgReceiveTime = -1;
    private boolean mHasBurnCfg = false;

    public boolean getHasBurnCfg() {
        return this.mHasBurnCfg;
    }

    public long getBurnUpdateTime() {
        return this.mBurnCfgReceiveTime;
    }

    public void updateBurnCfg(boolean hasCfg, long updateTime) {
        this.mHasBurnCfg = hasCfg;
        this.mBurnCfgReceiveTime = updateTime;
    }
}
