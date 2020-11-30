package android.os;

import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.BatteryStats;
import android.telephony.SignalStrength;
import android.text.format.DateFormat;
import android.util.ArraySet;
import com.android.internal.os.BackgroundCpuStatsHelper;
import java.io.PrintWriter;
import java.util.Set;

public abstract class OppoBaseBatteryStats {
    public BackgroundCpuStatsHelper mBgCpuStatsHelper = new BackgroundCpuStatsHelper();

    public static final class ThermalItem implements Parcelable {
        public static final byte CMD_AUDIOONOFF = 11;
        public static final byte CMD_BACKLIGHTINFO = 3;
        public static final byte CMD_BAT_INFO = 1;
        public static final byte CMD_CAMEARAONOFF = 10;
        public static final byte CMD_COMMON_UPDATE = 26;
        public static final byte CMD_CONNECTNETTYPE = 9;
        public static final byte CMD_ENVITEMP = 24;
        public static final byte CMD_FLASHLIGHTONOFF = 14;
        public static final byte CMD_FOREPRCINFO = 17;
        public static final byte CMD_GPSONOFF = 13;
        public static final byte CMD_JOBINFO = 15;
        public static final byte CMD_NETSTATE = 8;
        public static final byte CMD_NETSYNCINFO = 16;
        public static final byte CMD_NULL = 0;
        public static final byte CMD_PHONE_ONFF = 5;
        public static final byte CMD_PHONE_SIGNAL = 7;
        public static final byte CMD_PHONE_STATE = 6;
        public static final byte CMD_RESET = 19;
        public static final byte CMD_TEMPINFO = 2;
        public static final byte CMD_THERMALRATIO = 20;
        public static final byte CMD_THERMALRATIO1 = 21;
        public static final byte CMD_THERMALRATIO2 = 22;
        public static final byte CMD_THERMALRATIO3 = 23;
        public static final byte CMD_TOPPROCINFO = 18;
        public static final byte CMD_UPDATE_TIME = 25;
        public static final byte CMD_VIDEOONOFF = 12;
        public static final byte CMD_WIFIINFO = 4;
        public static final int CONNECT_MOBILE = 0;
        public static final int CONNECT_NONE = -1;
        public static final int CONNECT_WIFI = 1;
        private static final int INVALID_DATA = -1023;
        public static final byte NETWORK_CLASS_2_G = 2;
        public static final byte NETWORK_CLASS_3_G = 3;
        public static final byte NETWORK_CLASS_4_G = 4;
        public static final byte NETWORK_CLASS_UNKNOWN = 0;
        public static final byte NETWORK_CLASS_WIFI = 1;
        public static final int WIFI_OFF = 0;
        public static final int WIFI_ON = 1;
        public static final int WIFI_RUN = 2;
        public static final int WIFI_STOP = 3;
        public boolean audioOn;
        public int backlight;
        public long baseElapsedRealtime;
        public int batPercent;
        public int batRm;
        public int batTemp;
        public boolean cameraOn;
        public int chargePlug;
        public byte cmd = 0;
        public byte connectNetType;
        public int cpuLoading;
        public long currentTime;
        public boolean dataNetStatus;
        public long elapsedRealtime;
        public int enviTemp = INVALID_DATA;
        public boolean flashlightOn;
        public String foreProc = "null";
        public boolean gpsOn;
        public boolean isAutoBrightness;
        public String jobSchedule = "null";
        public String netSync = "null";
        public ThermalItem next;
        public int numReadInts;
        public boolean phoneOnff;
        public byte phoneSignal;
        public byte phoneState;
        public int phoneTemp = INVALID_DATA;
        public int phoneTemp1 = INVALID_DATA;
        public int phoneTemp2 = INVALID_DATA;
        public int phoneTemp3 = INVALID_DATA;
        public byte thermalRatio;
        public byte thermalRatio1;
        public byte thermalRatio2;
        public byte thermalRatio3;
        public int topCpu;
        public String topProc = "null";
        public long upTime;
        public String versionName;
        public boolean videoOn;
        public int volume;
        public int wifiSignal;
        public int wifiStats;

        @Override // android.os.Parcelable
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte(this.cmd);
            dest.writeLong(this.currentTime);
            dest.writeLong(this.elapsedRealtime);
            dest.writeLong(this.upTime);
            dest.writeLong(this.baseElapsedRealtime);
            dest.writeInt(this.batRm);
            dest.writeInt(this.batTemp);
            dest.writeInt(this.phoneTemp);
            dest.writeInt(this.phoneTemp1);
            dest.writeInt(this.phoneTemp2);
            dest.writeInt(this.phoneTemp3);
            dest.writeByte(this.thermalRatio);
            dest.writeByte(this.thermalRatio1);
            dest.writeByte(this.thermalRatio2);
            dest.writeByte(this.thermalRatio3);
            dest.writeInt(this.enviTemp);
            dest.writeInt(this.batPercent);
            dest.writeInt(this.chargePlug);
            dest.writeInt(this.backlight);
            dest.writeInt(this.volume);
            dest.writeInt(this.wifiStats);
            dest.writeInt(this.wifiSignal);
            dest.writeBoolean(this.phoneOnff);
            dest.writeByte(this.phoneState);
            dest.writeByte(this.phoneSignal);
            dest.writeBoolean(this.dataNetStatus);
            dest.writeByte(this.connectNetType);
            dest.writeBoolean(this.cameraOn);
            dest.writeBoolean(this.audioOn);
            dest.writeBoolean(this.videoOn);
            dest.writeBoolean(this.gpsOn);
            dest.writeBoolean(this.flashlightOn);
            dest.writeString(this.jobSchedule);
            dest.writeString(this.netSync);
            dest.writeString(this.foreProc);
            dest.writeString(this.topProc);
            dest.writeInt(this.cpuLoading);
            dest.writeInt(this.topCpu);
        }

        public void readFromParcel(Parcel src) {
            int start = src.dataPosition();
            this.cmd = src.readByte();
            this.currentTime = src.readLong();
            this.elapsedRealtime = src.readLong();
            this.upTime = src.readLong();
            this.batRm = src.readInt();
            this.batTemp = src.readInt();
            this.phoneTemp = src.readInt();
            this.phoneTemp1 = src.readInt();
            this.phoneTemp2 = src.readInt();
            this.phoneTemp3 = src.readInt();
            this.thermalRatio = src.readByte();
            this.thermalRatio1 = src.readByte();
            this.thermalRatio2 = src.readByte();
            this.thermalRatio3 = src.readByte();
            this.enviTemp = src.readInt();
            this.batPercent = src.readInt();
            this.chargePlug = src.readInt();
            this.backlight = src.readInt();
            this.volume = src.readInt();
            this.wifiStats = src.readInt();
            this.wifiSignal = src.readInt();
            this.phoneOnff = src.readBoolean();
            this.phoneState = src.readByte();
            this.phoneSignal = src.readByte();
            this.dataNetStatus = src.readBoolean();
            this.connectNetType = src.readByte();
            this.cameraOn = src.readBoolean();
            this.audioOn = src.readBoolean();
            this.videoOn = src.readBoolean();
            this.gpsOn = src.readBoolean();
            this.flashlightOn = src.readBoolean();
            this.jobSchedule = src.readString();
            this.netSync = src.readString();
            this.foreProc = src.readString();
            this.topProc = src.readString();
            this.cpuLoading = src.readInt();
            this.topCpu = src.readInt();
            this.numReadInts += (src.dataPosition() - start) / 4;
        }

        public void clear() {
            this.cmd = 0;
            this.currentTime = -1;
            this.elapsedRealtime = -1;
            this.upTime = -1;
            this.batRm = -1;
            this.batTemp = -1;
            this.phoneTemp = INVALID_DATA;
            this.phoneTemp1 = INVALID_DATA;
            this.phoneTemp2 = INVALID_DATA;
            this.phoneTemp3 = INVALID_DATA;
            this.thermalRatio = -127;
            this.thermalRatio1 = -127;
            this.thermalRatio2 = -127;
            this.thermalRatio3 = -127;
            this.enviTemp = INVALID_DATA;
            this.batPercent = -1;
            this.chargePlug = -1;
            this.backlight = -1;
            this.volume = 0;
            this.wifiStats = -1;
            this.wifiSignal = -1;
            this.phoneState = -1;
            this.phoneOnff = false;
            this.phoneSignal = 0;
            this.dataNetStatus = false;
            this.connectNetType = 0;
            this.cameraOn = false;
            this.audioOn = false;
            this.videoOn = false;
            this.gpsOn = false;
            this.flashlightOn = false;
            this.jobSchedule = "null";
            this.netSync = "null";
            this.foreProc = "null";
            this.topProc = "null";
            this.cpuLoading = 0;
            this.topCpu = 0;
            this.isAutoBrightness = false;
        }

        public void setTo(ThermalItem o) {
            setToCommon(o);
        }

        private void setToCommon(ThermalItem o) {
            this.cmd = o.cmd;
            this.currentTime = o.currentTime;
            this.elapsedRealtime = o.elapsedRealtime;
            this.upTime = o.upTime;
            this.baseElapsedRealtime = o.baseElapsedRealtime;
            this.batRm = o.batRm;
            this.batTemp = o.batTemp;
            this.phoneTemp = o.phoneTemp;
            this.phoneTemp1 = o.phoneTemp1;
            this.phoneTemp2 = o.phoneTemp2;
            this.phoneTemp3 = o.phoneTemp3;
            this.thermalRatio = o.thermalRatio;
            this.thermalRatio1 = o.thermalRatio1;
            this.thermalRatio2 = o.thermalRatio2;
            this.thermalRatio3 = o.thermalRatio3;
            this.enviTemp = o.enviTemp;
            this.batPercent = o.batPercent;
            this.chargePlug = o.chargePlug;
            this.backlight = o.backlight;
            this.volume = o.volume;
            this.wifiStats = o.wifiStats;
            this.wifiSignal = o.wifiSignal;
            this.phoneOnff = o.phoneOnff;
            this.phoneState = o.phoneState;
            this.phoneSignal = o.phoneSignal;
            this.dataNetStatus = o.dataNetStatus;
            this.connectNetType = o.connectNetType;
            this.cameraOn = o.cameraOn;
            this.audioOn = o.audioOn;
            this.videoOn = o.videoOn;
            this.gpsOn = o.gpsOn;
            this.flashlightOn = o.flashlightOn;
            this.jobSchedule = o.jobSchedule;
            this.netSync = o.netSync;
            this.foreProc = o.foreProc;
            this.versionName = o.versionName;
            this.topProc = o.topProc;
            this.cpuLoading = o.cpuLoading;
            this.topCpu = o.topCpu;
            this.isAutoBrightness = o.isAutoBrightness;
        }

        public boolean same(ThermalItem o) {
            return this.currentTime == o.currentTime && this.elapsedRealtime == o.elapsedRealtime && this.upTime == o.upTime && this.baseElapsedRealtime == o.baseElapsedRealtime && this.batRm == o.batRm && this.batTemp == o.batTemp && this.phoneTemp == o.phoneTemp && this.phoneTemp1 == o.phoneTemp1 && this.phoneTemp2 == o.phoneTemp2 && this.phoneTemp3 == o.phoneTemp3 && this.thermalRatio == o.thermalRatio && this.thermalRatio1 == o.thermalRatio1 && this.thermalRatio2 == o.thermalRatio2 && this.thermalRatio3 == o.thermalRatio3 && this.enviTemp == o.enviTemp && this.batPercent == o.batPercent && this.chargePlug == o.chargePlug && this.backlight == o.backlight && this.volume == o.volume && this.wifiStats == o.wifiStats && this.wifiSignal == o.wifiSignal && this.phoneOnff == o.phoneOnff && this.phoneState == o.phoneState && this.phoneSignal == o.phoneSignal && this.dataNetStatus == o.dataNetStatus && this.connectNetType == o.connectNetType && this.audioOn == o.audioOn && this.videoOn == o.videoOn && this.gpsOn == o.gpsOn && this.flashlightOn == o.flashlightOn && this.jobSchedule == o.jobSchedule && this.netSync == o.netSync && this.foreProc == o.foreProc && this.versionName == o.versionName && this.topProc == o.topProc && this.cpuLoading == o.cpuLoading && this.topCpu == o.topCpu && this.isAutoBrightness == o.isAutoBrightness;
        }

        @Override // android.os.Parcelable
        public int describeContents() {
            return 0;
        }
    }

    public static class ThermalHistoryPrinter {
        public void printNextItem(PrintWriter pw, ThermalItem rec) {
            StringBuilder sb = new StringBuilder();
            sb.append(DateFormat.format("yyyy-MM-dd-HH-mm-ss", (rec.elapsedRealtime - rec.baseElapsedRealtime) + rec.currentTime).toString());
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            OppoBaseBatteryStats.formatThermalTimeMsNoSpace(sb, rec.elapsedRealtime);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            OppoBaseBatteryStats.formatThermalTimeMsNoSpace(sb, rec.upTime);
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(Integer.toString(rec.batPercent));
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(Integer.toString(rec.backlight));
            sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
            sb.append(Integer.toString(rec.volume));
            if (rec.cmd == 0) {
                sb.append(" START RECORD");
                pw.print(sb.toString());
                pw.println();
            } else if (rec.cmd == 19) {
                sb.append(" THERMAL HISTORY RESET");
                pw.print(sb.toString());
                pw.println();
            } else {
                sb.append(" batTemp=");
                sb.append(Integer.toString(rec.batTemp));
                sb.append(" phoneTemp=");
                sb.append(Integer.toString(rec.phoneTemp));
                sb.append(" thermalRatio=");
                sb.append(Float.toString(((float) rec.thermalRatio) / 10.0f));
                if (rec.enviTemp == -1023) {
                    sb.append(" enviTemp=");
                    sb.append("unknow");
                } else {
                    sb.append(" enviTemp=");
                    sb.append(Integer.toString(rec.enviTemp));
                }
                sb.append(" batRm=");
                sb.append(Integer.toString(rec.batRm));
                sb.append(" plug=");
                int i = rec.chargePlug;
                if (i == 0) {
                    sb.append("none");
                } else if (i == 1) {
                    sb.append("ac");
                } else if (i == 2) {
                    sb.append(Context.USB_SERVICE);
                } else if (i != 4) {
                    sb.append("none");
                } else {
                    sb.append("wireless");
                }
                sb.append(" wifiStats=");
                sb.append(Integer.toString(rec.wifiStats));
                sb.append(" wifiSignal=");
                sb.append(Integer.toString(rec.wifiSignal));
                sb.append(" phoneOnff=");
                sb.append(Boolean.toString(rec.phoneOnff));
                sb.append(" simState=");
                sb.append(OppoBaseBatteryStats.formatSimState((rec.phoneState >> 4) & 15));
                sb.append(" phoneState=");
                sb.append(OppoBaseBatteryStats.formatPhoneState(rec.phoneState & 15));
                sb.append(" phoneSignal=");
                sb.append(SignalStrength.SIGNAL_STRENGTH_NAMES[rec.phoneSignal]);
                sb.append(" dataNetStatus=");
                sb.append(Boolean.toString(rec.dataNetStatus));
                sb.append(" connectNetType=");
                sb.append(OppoBaseBatteryStats.formatNetType(rec.connectNetType));
                sb.append(" cameraOn=");
                sb.append(Boolean.toString(rec.cameraOn));
                sb.append(" audioOn=");
                sb.append(Boolean.toString(rec.audioOn));
                sb.append(" videoOn=");
                sb.append(Boolean.toString(rec.videoOn));
                sb.append(" gpsOn=");
                sb.append(Boolean.toString(rec.gpsOn));
                sb.append(" flashlightOn=");
                sb.append(Boolean.toString(rec.flashlightOn));
                sb.append(" jobSchedule=");
                sb.append(rec.jobSchedule);
                sb.append(" netSync=");
                sb.append(rec.netSync);
                sb.append(" foreProc=");
                sb.append(rec.foreProc);
                sb.append(" cpuLoading=");
                sb.append(Float.toString(((float) rec.cpuLoading) / 10.0f));
                sb.append("%");
                sb.append(" topProc=");
                sb.append(rec.topProc);
                sb.append(" topCpu=");
                sb.append(Float.toString(((float) rec.topCpu) / 10.0f));
                sb.append("%");
                sb.append(" isAutoBrightness=");
                sb.append(Boolean.toString(rec.isAutoBrightness));
                sb.append(" version=");
                sb.append(rec.versionName);
                pw.print(sb.toString());
                pw.println();
            }
        }
    }

    private static final void formatThermalTimeRaw(StringBuilder out, long seconds) {
        long days = seconds / 86400;
        if (days != 0) {
            out.append(days);
            out.append("d ");
        }
        long used = days * 60 * 60 * 24;
        long hours = (seconds - used) / 3600;
        if (!(hours == 0 && used == 0)) {
            out.append(hours);
            out.append("h");
        }
        long used2 = used + (hours * 60 * 60);
        long mins = (seconds - used2) / 60;
        if (!(mins == 0 && used2 == 0)) {
            out.append(mins);
            out.append("m");
        }
        long used3 = used2 + (60 * mins);
        if (seconds != 0 || used3 != 0) {
            out.append(seconds - used3);
            out.append("s");
        }
    }

    /* access modifiers changed from: private */
    public static final String formatNetType(int netType) {
        if (netType == 0) {
            return "none";
        }
        if (netType == 1) {
            return "wifi";
        }
        if (netType == 2) {
            return "2g";
        }
        if (netType == 3) {
            return "3g";
        }
        if (netType != 4) {
            return "none";
        }
        return "4g";
    }

    /* access modifiers changed from: private */
    public static final String formatSimState(int simState) {
        switch (simState) {
            case 0:
                return "unknow";
            case 1:
                return "absent";
            case 2:
                return "pin_required";
            case 3:
                return "puk_required";
            case 4:
                return "network_locked";
            case 5:
                return "ready";
            case 6:
                return "not_ready";
            case 7:
                return "perm_disabled";
            case 8:
                return "card_io_error";
            case 9:
                return "card_restricted";
            default:
                return "unknow";
        }
    }

    /* access modifiers changed from: private */
    public static final String formatPhoneState(int state) {
        if (state == 0) {
            return "in_service";
        }
        if (state == 1) {
            return "out_of_service";
        }
        if (state == 2) {
            return "emergency_only";
        }
        if (state != 3) {
            return "out_of_service";
        }
        return "state_power_off";
    }

    /* access modifiers changed from: private */
    public static final void formatThermalTimeMsNoSpace(StringBuilder sb, long time) {
        long sec = time / 1000;
        formatThermalTimeRaw(sb, sec);
        sb.append(time - (1000 * sec));
        sb.append("ms");
    }

    public static abstract class OppoBaseUid {
        public BatteryStats.Timer getBgCameraTurnedOnTimer() {
            return null;
        }

        public long getBgCpuActiveTime() {
            return 0;
        }

        public long[] getBgCpuClusterTimes() {
            return null;
        }

        public long getBgTimeAtCpuSpeed(int cluster, int step, int which) {
            return 0;
        }

        public long getUidScreenBrightnessTime(int level, long elapsedRealtimeUs, int which) {
            return 0;
        }

        public void noteScreenBrightnessLocked(long elapsedRealtime, int oldLevel, int newLevel) {
        }

        public void dumpApkScreenBrightnessLocked(String state) {
        }

        public void screenOffScreenPowerApkHandleLocked(long elapsedRealtime) {
        }

        public static abstract class OppoBaseProc {
            private BackgroundCpuStatsHelper cpuStatsHelper = null;
            public long mBackgroundCpuTime = 0;
            public long mCpuTimeWhenIntoBackground = Long.MAX_VALUE;
            public int mCurrentPid = -1;
            public int mCurrentUid = -1;
            public Set<String> mPkgList = new ArraySet();

            public void setArgs(BackgroundCpuStatsHelper helper) {
                this.cpuStatsHelper = helper;
            }

            public void setUid(int uid) {
                this.mCurrentUid = uid;
            }

            public void addPackage(String pkg) {
                if (pkg != null && !"".equals(pkg) && !this.mPkgList.contains(pkg)) {
                    this.mPkgList.add(pkg);
                }
            }

            public void updatePackageList(Set<String> list) {
                if (this.mPkgList.size() > 0) {
                    this.mPkgList.clear();
                }
                this.mPkgList.addAll(list);
            }

            public Set<String> getPackageList() {
                return this.mPkgList;
            }

            public int getCurrentPid() {
                return this.mCurrentPid;
            }

            public long getCpuTimeWhenIntoBackground() {
                return this.mCpuTimeWhenIntoBackground;
            }

            public void setCpuTimeWhenIntoBackground(long cpu) {
                this.mCpuTimeWhenIntoBackground = cpu;
            }

            public void addBackgroundCpuTime(long diff) {
                this.mBackgroundCpuTime += diff;
            }

            public void setCurrentPid(int pid, boolean onBattery) {
                if (this.mCurrentPid == -1 && pid > 0 && onBattery && (this.cpuStatsHelper.mTopPackageName == null || !this.mPkgList.contains(this.cpuStatsHelper.mTopPackageName))) {
                    this.mCpuTimeWhenIntoBackground = this.cpuStatsHelper.mCpuTracker.getCpuTimeForPid(pid);
                }
                this.mCurrentPid = pid;
            }

            public long getBackgroundCpuTime(long userTime, long systemTime, boolean onBattery) {
                long delta = 0;
                long j = 0;
                if (onBattery && this.mCurrentPid > 0) {
                    long j2 = this.mCpuTimeWhenIntoBackground;
                    if (j2 > 0 && j2 != Long.MAX_VALUE) {
                        long cpu = this.cpuStatsHelper.mCpuTracker.getCpuTimeForPid(this.mCurrentPid);
                        long j3 = this.mCpuTimeWhenIntoBackground;
                        if (cpu > j3) {
                            j = cpu - j3;
                        }
                        delta = j;
                        return this.mBackgroundCpuTime + delta;
                    }
                }
                if (this.mCurrentUid == 0) {
                    long total = userTime + systemTime;
                    long j4 = this.mBackgroundCpuTime;
                    if (total > j4) {
                        j = total - j4;
                    }
                    delta = j;
                }
                return this.mBackgroundCpuTime + delta;
            }

            public void updateWhenBatteryChange(boolean onBatteryNow) {
                if (this.mCurrentPid > 0) {
                    if (onBatteryNow) {
                        if (this.cpuStatsHelper.mTopPackageName == null || !this.mPkgList.contains(this.cpuStatsHelper.mTopPackageName)) {
                            this.mCpuTimeWhenIntoBackground = this.cpuStatsHelper.mCpuTracker.getCpuTimeForPid(this.mCurrentPid);
                        }
                    } else if (this.cpuStatsHelper.mTopPackageName == null || !this.mPkgList.contains(this.cpuStatsHelper.mTopPackageName)) {
                        long cpuTime = this.cpuStatsHelper.mCpuTracker.getCpuTimeForPid(this.mCurrentPid);
                        long j = this.mCpuTimeWhenIntoBackground;
                        if (j >= 0 && cpuTime > j) {
                            this.mBackgroundCpuTime += cpuTime - j;
                            this.mCpuTimeWhenIntoBackground = cpuTime;
                        }
                    }
                }
            }

            public String getName() {
                return "";
            }

            public void setCurrentPid(int pid) {
            }
        }

        public static abstract class OppoBasePkg {
            public long getScreenBrightnessTime(int level, long elapsedRealtimeUs, int which) {
                return 0;
            }
        }
    }
}
