package com.oppo.statistics.data;

public class PeriodDataBean extends CommonBean {
    public PeriodDataBean(String logTag, String eventID, String logMap) {
        super(logTag, eventID, logMap);
    }

    @Override // com.oppo.statistics.data.CommonBean, com.oppo.statistics.data.StatisticBean
    public int getDataType() {
        return 13;
    }

    @Override // com.oppo.statistics.data.CommonBean
    public String toString() {
        return " type is :" + getDataType() + ", uploadNow is :" + getUploadNow() + ", tag is :" + getLogTag() + ", eventID is :" + getEventID() + ", map is :" + getLogMap();
    }
}
