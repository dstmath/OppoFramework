package com.mediatek.simservs.client;

import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.uri.XcapUri;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

public class CommunicationWaiting extends SimservType {
    public static final String NODE_NAME = "communication-waiting";

    public CommunicationWaiting(XcapUri documentUri, String parentUri, String intendedId) throws XcapException, ParserConfigurationException {
        super(documentUri, parentUri, intendedId);
    }

    @Override // com.mediatek.simservs.client.SimservType
    public void initServiceInstance(Document domDoc) {
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }
}
