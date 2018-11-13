package qcom.fmradio;

interface FmRxEvCallbacks {
    void FmRxEvDisableReceiver();

    void FmRxEvECCInfo();

    void FmRxEvERTInfo();

    void FmRxEvEnableReceiver();

    void FmRxEvEnableSlimbus(int i);

    void FmRxEvEnableSoftMute(int i);

    void FmRxEvRTPlus();

    void FmRxEvRadioReset();

    void FmRxEvRadioTuneStatus(int i);

    void FmRxEvRdsAfInfo();

    void FmRxEvRdsGroupData();

    void FmRxEvRdsLockStatus(boolean z);

    void FmRxEvRdsPsInfo();

    void FmRxEvRdsRtInfo();

    void FmRxEvSearchCancelled();

    void FmRxEvSearchComplete(int i);

    void FmRxEvSearchInProgress();

    void FmRxEvSearchListComplete();

    void FmRxEvServiceAvailable(boolean z);

    void FmRxEvStereoStatus(boolean z);
}
