package com.android.server.display;

abstract class OppoBaseDisplayPowerController {
    OppoBrightUtils mOppoBrightUtils;
    OppoDisplayPowerControlBrightnessHelper mOppoDisplayBrightnessHelper;
    OppoDisplayPowerHelper mOppoDisplayPowerHelper;

    OppoBaseDisplayPowerController() {
    }

    public void blockScreenOnByBiometrics(String reason) {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            oppoDisplayPowerHelper.blockScreenOnByBiometrics(reason);
        }
    }

    public boolean getUseProximityForceSuspendState() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            return oppoDisplayPowerHelper.getUseProximityForceSuspendState();
        }
        return false;
    }

    public void setUseProximityForceSuspend(boolean enable) {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            oppoDisplayPowerHelper.setUseProximityForceSuspend(enable);
        }
    }

    public void onProximityDebounceTimeArrived() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            oppoDisplayPowerHelper.onProximityDebounceTimeArrived();
        }
    }

    public void onProximityDebounceTimeWaitting() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            oppoDisplayPowerHelper.onProximityDebounceTimeWaitting();
        }
    }

    public void unblockScreenOnByBiometrics(String reason) {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            oppoDisplayPowerHelper.unblockScreenOnByBiometrics(reason);
        }
    }

    public boolean isBlockDisplayByBiometrics() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            return oppoDisplayPowerHelper.isBlockDisplayByBiometrics();
        }
        return false;
    }

    public boolean isBlockScreenOnByBiometrics() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            return oppoDisplayPowerHelper.isBlockScreenOnByBiometrics();
        }
        return false;
    }

    public boolean isScreenOnBlockedByFace() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            return oppoDisplayPowerHelper.isScreenOnBlockedByFace();
        }
        return false;
    }

    public int getScreenState() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            return oppoDisplayPowerHelper.getScreenState();
        }
        return -1;
    }

    public boolean hasBiometricsBlockedReason(String reason) {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            return oppoDisplayPowerHelper.hasBiometricsBlockedReason(reason);
        }
        return false;
    }

    public void updateScreenOnBlockedState(boolean isBlockedScreenOn) {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            oppoDisplayPowerHelper.updateScreenOnBlockedState(isBlockedScreenOn);
        }
    }

    public void removeFaceBlockReasonFromBlockReasonList() {
        OppoDisplayPowerHelper oppoDisplayPowerHelper = this.mOppoDisplayPowerHelper;
        if (oppoDisplayPowerHelper != null) {
            oppoDisplayPowerHelper.removeFaceBlockReasonFromBlockReasonList();
        }
    }

    public int caculateRate(boolean slowChange, boolean autoBrightnessEnabled) {
        OppoDisplayPowerControlBrightnessHelper oppoDisplayPowerControlBrightnessHelper = this.mOppoDisplayBrightnessHelper;
        if (oppoDisplayPowerControlBrightnessHelper != null) {
            return oppoDisplayPowerControlBrightnessHelper.caculateRate(slowChange, autoBrightnessEnabled);
        }
        return 0;
    }

    public int caculateBrightness(int brightness, boolean autoBrightnessEnabled) {
        OppoDisplayPowerControlBrightnessHelper oppoDisplayPowerControlBrightnessHelper = this.mOppoDisplayBrightnessHelper;
        if (oppoDisplayPowerControlBrightnessHelper != null) {
            return oppoDisplayPowerControlBrightnessHelper.caculateBrightness(brightness, autoBrightnessEnabled);
        }
        return brightness;
    }

    public void setOutdoorModeInBase(boolean enable) {
        OppoDisplayPowerControlBrightnessHelper oppoDisplayPowerControlBrightnessHelper = this.mOppoDisplayBrightnessHelper;
        if (oppoDisplayPowerControlBrightnessHelper != null) {
            oppoDisplayPowerControlBrightnessHelper.setOutdoorMode(enable);
        }
    }
}
