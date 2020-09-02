package com.mediatek.simservs.capability;

import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;

public class ServiceCapabilities extends XcapElement {
    public static final String ATT_PROVISIONED = "provisioned";

    public ServiceCapabilities(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return null;
    }
}
