package com.mediatek.internal.telephony.cat;

/* access modifiers changed from: package-private */
public class BipChannelManager {
    public static final int MAXCHANNELID = 7;
    public static final int MAXPSCID = 5;
    public static final int MAXUICCSERVIER = 2;
    private int[] mBipChannelStatus;
    private byte mChannelIdPool;
    private Channel[] mChannels;
    private byte mCurrentOccupiedPSCh;
    private byte mCurrentOccupiedUICCSerCh;

    public BipChannelManager() {
        this.mChannelIdPool = 0;
        this.mChannels = null;
        this.mCurrentOccupiedPSCh = 0;
        this.mCurrentOccupiedUICCSerCh = 0;
        this.mBipChannelStatus = null;
        this.mBipChannelStatus = new int[7];
        this.mChannels = new Channel[7];
        for (int i = 0; i < 7; i++) {
            this.mChannels[i] = null;
            this.mBipChannelStatus[i] = 0;
        }
    }

    public boolean isChannelIdOccupied(int cId) {
        MtkCatLog.d("[BIP]", "isChannelIdOccupied, mChannelIdPool " + ((int) this.mChannelIdPool) + ":" + cId);
        return (this.mChannelIdPool & (1 << (cId + -1))) > 0;
    }

    public int getFreeChannelId() {
        for (int i = 0; i < 7; i++) {
            if ((this.mChannelIdPool & (1 << i)) == 0) {
                return i + 1;
            }
        }
        return 0;
    }

    public int acquireChannelId(int protocolType) {
        MtkCatLog.d("[BIP]", "acquireChannelId, protocolType " + protocolType + ",occupied " + ((int) this.mCurrentOccupiedPSCh) + "," + ((int) this.mCurrentOccupiedUICCSerCh));
        if ((3 == protocolType && 2 <= this.mCurrentOccupiedUICCSerCh) || ((1 == protocolType || 2 == protocolType) && 5 <= this.mCurrentOccupiedPSCh)) {
            return 0;
        }
        for (byte i = 0; i < 7; i = (byte) (i + 1)) {
            byte b = this.mChannelIdPool;
            if (((1 << i) & b) == 0) {
                this.mChannelIdPool = (byte) (((byte) (1 << i)) | b);
                if (3 == protocolType) {
                    this.mCurrentOccupiedUICCSerCh = (byte) (this.mCurrentOccupiedUICCSerCh + 1);
                } else if (1 == protocolType || 2 == protocolType) {
                    this.mCurrentOccupiedPSCh = (byte) (this.mCurrentOccupiedPSCh + 1);
                }
                MtkCatLog.d("[BIP]", "acquireChannelId, mChannelIdPool " + ((int) this.mChannelIdPool) + ":" + (i + 1));
                return i + 1;
            }
        }
        return 0;
    }

    public void releaseChannelId(int cId, int protocolType) {
        if (cId <= 0 || cId > 7) {
            MtkCatLog.e("[BIP]", "releaseChannelId, Invalid cid:" + cId);
            return;
        }
        try {
            if ((this.mChannelIdPool & (1 << ((byte) (cId - 1)))) == 0) {
                MtkCatLog.e("[BIP]", "releaseChannelId, cId:" + cId + " has been released.");
                return;
            }
            if (3 == protocolType && this.mCurrentOccupiedUICCSerCh >= 0) {
                this.mCurrentOccupiedUICCSerCh = (byte) (this.mCurrentOccupiedUICCSerCh - 1);
            } else if ((1 == protocolType || 2 == protocolType) && this.mCurrentOccupiedPSCh >= 0) {
                this.mCurrentOccupiedPSCh = (byte) (this.mCurrentOccupiedPSCh - 1);
            } else {
                MtkCatLog.e("[BIP]", "releaseChannelId, bad parameters.cId:" + cId + ":" + ((int) this.mChannelIdPool));
            }
            this.mChannelIdPool = (byte) (this.mChannelIdPool & ((byte) (~(1 << ((byte) (cId - 1))))));
            MtkCatLog.d("[BIP]", "releaseChannelId, cId " + cId + ",protocolType " + protocolType + ",occupied " + ((int) this.mCurrentOccupiedPSCh) + "," + ((int) this.mCurrentOccupiedUICCSerCh) + ":" + ((int) this.mChannelIdPool));
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException releaseChannelId cId=" + cId + ":" + ((int) this.mChannelIdPool));
        }
    }

    public void releaseChannelId(int cId) {
        if (cId <= 0 || cId > 7) {
            MtkCatLog.e("[BIP]", "releaseChannelId, Invalid cid:" + cId);
            return;
        }
        try {
            if ((this.mChannelIdPool & (1 << ((byte) (cId - 1)))) == 0) {
                MtkCatLog.e("[BIP]", "releaseChannelId, cId:" + cId + " has been released.");
            } else if (this.mChannels[cId - 1] != null) {
                int protocolType = this.mChannels[cId - 1].mProtocolType;
                if (3 == protocolType && this.mCurrentOccupiedUICCSerCh > 0) {
                    this.mCurrentOccupiedUICCSerCh = (byte) (this.mCurrentOccupiedUICCSerCh - 1);
                } else if ((1 == protocolType || 2 == protocolType) && this.mCurrentOccupiedPSCh > 0) {
                    this.mCurrentOccupiedPSCh = (byte) (this.mCurrentOccupiedPSCh - 1);
                } else {
                    MtkCatLog.e("[BIP]", "releaseChannelId, bad parameters.cId:" + cId + ":" + ((int) this.mChannelIdPool));
                }
                this.mChannelIdPool = (byte) (this.mChannelIdPool & ((byte) (~(1 << ((byte) (cId - 1))))));
                MtkCatLog.d("[BIP]", "releaseChannelId, cId " + cId + ",protocolType" + protocolType + ",occupied " + ((int) this.mCurrentOccupiedPSCh) + "," + ((int) this.mCurrentOccupiedUICCSerCh) + ":" + ((int) this.mChannelIdPool));
            } else {
                MtkCatLog.e("[BIP]", "channel object is null.");
            }
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException releaseChannelId cId=" + cId + ":" + ((int) this.mChannelIdPool));
        }
    }

    public int addChannel(int cId, Channel ch) {
        MtkCatLog.d("[BIP]", "BCM-addChannel:" + cId);
        if (cId > 0) {
            try {
                this.mChannels[cId - 1] = ch;
                this.mBipChannelStatus[cId - 1] = 4;
            } catch (IndexOutOfBoundsException e) {
                MtkCatLog.e("[BIP]", "IndexOutOfBoundsException addChannel cId=" + cId);
                return -1;
            }
        } else {
            MtkCatLog.e("[BIP]", "No free channel id.");
        }
        return cId;
    }

    public Channel getChannel(int cId) {
        try {
            return this.mChannels[cId - 1];
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException getChannel cId=" + cId);
            return null;
        }
    }

    public int getBipChannelStatus(int cId) {
        return this.mBipChannelStatus[cId - 1];
    }

    public void setBipChannelStatus(int cId, int status) {
        try {
            this.mBipChannelStatus[cId - 1] = status;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException setBipChannelStatus cId=" + cId);
        }
    }

    public int removeChannel(int cId) {
        MtkCatLog.d("[BIP]", "BCM-removeChannel:" + cId);
        try {
            releaseChannelId(cId);
            this.mChannels[cId - 1] = null;
            this.mBipChannelStatus[cId - 1] = 2;
            return 1;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException removeChannel cId=" + cId);
            return 0;
        } catch (NullPointerException e2) {
            MtkCatLog.e("[BIP]", "removeChannel channel:" + cId + " is null");
            return 0;
        }
    }

    public boolean isClientChannelOpened() {
        for (int i = 0; i < 7; i++) {
            try {
                if (!(this.mChannels == null || this.mChannels[i] == null || (this.mChannels[i].mProtocolType & 3) == 0)) {
                    return true;
                }
            } catch (NullPointerException e) {
                MtkCatLog.e("[BIP]", "isClientChannelOpened channel:" + i + " is null");
                return false;
            }
        }
        return false;
    }

    public void updateBipChannelStatus(int cId, int chStatus) {
        try {
            this.mChannels[cId - 1].mChannelStatus = chStatus;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException updateBipChannelStatus cId=" + cId);
        } catch (NullPointerException e2) {
            MtkCatLog.e("[BIP]", "updateBipChannelStatus id:" + cId + " is null");
        }
    }

    public void updateChannelStatus(int cId, int chStatus) {
        try {
            this.mChannels[cId - 1].mChannelStatusData.mChannelStatus = chStatus;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException updateChannelStatus cId=" + cId);
        } catch (NullPointerException e2) {
            MtkCatLog.e("[BIP]", "updateChannelStatus id:" + cId + " is null");
        }
    }

    public void updateChannelStatusInfo(int cId, int chStatusInfo) {
        try {
            this.mChannels[cId - 1].mChannelStatusData.mChannelStatusInfo = chStatusInfo;
        } catch (IndexOutOfBoundsException e) {
            MtkCatLog.e("[BIP]", "IndexOutOfBoundsException updateChannelStatusInfo cId=" + cId);
        } catch (NullPointerException e2) {
            MtkCatLog.e("[BIP]", "updateChannelStatusInfo id:" + cId + " is null");
        }
    }
}
