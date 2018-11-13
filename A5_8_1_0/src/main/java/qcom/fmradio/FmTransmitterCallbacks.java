package qcom.fmradio;

public interface FmTransmitterCallbacks {
    void FmTxEvContRDSGroupsComplete();

    void FmTxEvRDSGroupsAvailable();

    void FmTxEvRDSGroupsComplete();

    void FmTxEvRadioDisabled();

    void FmTxEvRadioEnabled();

    void FmTxEvRadioReset();

    void FmTxEvTuneStatusChange(int i);
}
