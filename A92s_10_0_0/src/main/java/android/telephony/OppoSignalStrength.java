package android.telephony;

public abstract class OppoSignalStrength {
    public int mOEMLevel_0 = 0;
    public int mOEMLevel_1 = 0;

    public int[] getColorOSLevel() {
        return new int[]{this.mOEMLevel_0, this.mOEMLevel_1};
    }

    public int getOEMLevel_0() {
        return this.mOEMLevel_0;
    }

    public int getOEMLevel_1() {
        return this.mOEMLevel_1;
    }

    public void setOEMLevel(int OEMLevel_0, int OEMLevel_1) {
        if (-1 != OEMLevel_0) {
            this.mOEMLevel_0 = OEMLevel_0;
        }
        if (-1 != OEMLevel_1) {
            this.mOEMLevel_1 = OEMLevel_1;
        }
    }
}
