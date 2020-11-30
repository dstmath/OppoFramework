package com.mediatek.simservs.xcap;

public interface Attributable {
    void deleteByAttrName(String str) throws XcapException;

    String getByAttrName(String str) throws XcapException;

    void setByAttrName(String str, String str2) throws XcapException;
}
