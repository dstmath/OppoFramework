package com.mediatek.powerhalmgr;

public class PowerHalMgrFactoryImpl extends PowerHalMgrFactory {
    public PowerHalMgr makePowerHalMgr() {
        return PowerHalMgrImpl.getInstance();
    }
}
