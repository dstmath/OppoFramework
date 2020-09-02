package com.mediatek.simservs.client.policy;

import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;

public class Sphere extends XcapElement {
    public static final String NODE_NAME = "sphere";

    public Sphere(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }
}
