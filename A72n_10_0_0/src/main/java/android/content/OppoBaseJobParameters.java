package android.content;

import android.os.Parcel;

public class OppoBaseJobParameters {
    private int cpuLevel;
    private String oppoExtraStr;

    public void setCpuLevel(int level) {
        this.cpuLevel = level;
    }

    public int getCpuLevel() {
        return this.cpuLevel;
    }

    public void setOppoExtraStr(String str) {
        this.oppoExtraStr = str;
    }

    public String getOppoExtraStr() {
        return this.oppoExtraStr;
    }

    public OppoBaseJobParameters() {
    }

    public OppoBaseJobParameters(Parcel in) {
    }

    public void initJobParameters(Parcel in) {
        this.cpuLevel = in.readInt();
        this.oppoExtraStr = in.readString();
    }

    public void writeToParcelJobParameters(Parcel dest) {
        dest.writeInt(this.cpuLevel);
        dest.writeString(this.oppoExtraStr);
    }
}
