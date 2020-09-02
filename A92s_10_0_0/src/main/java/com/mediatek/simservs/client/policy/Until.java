package com.mediatek.simservs.client.policy;

import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;

public class Until extends XcapElement {
    public static final String NODE_NAME = "until";

    public Until(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }
}
