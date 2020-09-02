package android.nfc.cardemulation;

public class OppoBaseApduServiceInfo {
    byte[] mByteArrayBanner = {0};
    protected int mServiceState;

    public boolean isServiceEnabled(String category) {
        int i;
        if (!"other".equals(category) || (i = this.mServiceState) == 1 || i == 3) {
            return true;
        }
        return false;
    }

    public int setServiceState(String category, int state) {
        if (category != "other") {
            return 1;
        }
        this.mServiceState = state;
        return this.mServiceState;
    }
}
