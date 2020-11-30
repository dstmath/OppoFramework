package com.mediatek.internal.telephony.cat;

public class ChannelStatus {
    public static final int CHANNEL_STATUS_INFO_LINK_DROPED = 5;
    public static final int CHANNEL_STATUS_INFO_NO_FURTHER_INFO = 0;
    public static final int CHANNEL_STATUS_LINK = 128;
    public static final int CHANNEL_STATUS_NO_LINK = 0;
    public boolean isActivated = false;
    public int mChannelId;
    public int mChannelStatus;
    public int mChannelStatusInfo;

    public ChannelStatus(int cid, int status, int info) {
        this.mChannelId = cid;
        this.mChannelStatus = status;
        this.mChannelStatusInfo = info;
    }
}
