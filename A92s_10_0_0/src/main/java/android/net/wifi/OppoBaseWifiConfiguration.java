package android.net.wifi;

public class OppoBaseWifiConfiguration {
    protected WapiWrapper mWrapper = new WapiWrapper();

    public static class WapiWrapper {
        public String wapiCertSel;
        public int wapiCertSelMode;
        public String wapiPsk;
        public int wapiPskType;

        public WapiWrapper() {
            this.wapiPskType = -1;
            this.wapiPsk = null;
            this.wapiCertSelMode = -1;
            this.wapiCertSel = null;
        }

        public WapiWrapper(int wapiPskType2, String wapiPsk2, int wapiCertSelMode2, String wapiCertSel2) {
            this.wapiPskType = wapiPskType2;
            this.wapiPsk = wapiPsk2;
            this.wapiCertSelMode = wapiCertSelMode2;
            this.wapiCertSel = wapiCertSel2;
        }
    }
}
