package com.mediatek.internal.telephony;

public class NetworkInfoWithAcT {
    int nAct;
    int nPriority;
    String operatorAlphaName;
    String operatorNumeric;

    public String getOperatorAlphaName() {
        return this.operatorAlphaName;
    }

    public String getOperatorNumeric() {
        return this.operatorNumeric;
    }

    public int getAccessTechnology() {
        return this.nAct;
    }

    public int getPriority() {
        return this.nPriority;
    }

    public void setOperatorAlphaName(String operatorAlphaName2) {
        this.operatorAlphaName = operatorAlphaName2;
    }

    public void setOperatorNumeric(String operatorNumeric2) {
        this.operatorNumeric = operatorNumeric2;
    }

    public void setAccessTechnology(int nAct2) {
        this.nAct = nAct2;
    }

    public void setPriority(int nIndex) {
        this.nPriority = nIndex;
    }

    public NetworkInfoWithAcT(String operatorAlphaLong, String operatorNumeric2, int nAct2, int nPriority2) {
        this.operatorAlphaName = operatorAlphaLong;
        this.operatorNumeric = operatorNumeric2;
        this.nAct = nAct2;
        this.nPriority = nPriority2;
    }

    public String toString() {
        return "NetworkInfoWithAcT " + this.operatorAlphaName + "/" + this.operatorNumeric + "/" + this.nAct + "/" + this.nPriority;
    }
}
