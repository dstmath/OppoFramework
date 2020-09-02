package com.oppo.statistics.data;

public class SettingKeyDataBean extends CommonBean {
    public SettingKeyDataBean(String logTag, String eventID, String keyMap) {
        super(logTag, eventID, keyMap);
    }

    @Override // com.oppo.statistics.data.CommonBean, com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 14;
    }

    @Override // com.oppo.statistics.data.CommonBean
    public String toString() {
        return " type is :" + getDataType() + "," + " uploadNow is :" + getUploadNow() + "," + " tag is :" + getLogTag() + "," + " eventID is :" + getEventID() + "," + " map is :" + getLogMap();
    }
}
