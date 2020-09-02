package com.mediatek.simservs.capability;

import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.uri.XcapUri;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class BarringServiceCapability extends CapabilitiesType {
    public static final String NODE_NAME = "communication-barring-serv-cap";
    ConditionCapabilities mConditionCapabilities;

    public BarringServiceCapability(XcapUri xcapUri, String parentUri, String intendedId) throws XcapException, ParserConfigurationException {
        super(xcapUri, parentUri, intendedId);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    public ConditionCapabilities getConditionCapabilities() {
        return this.mConditionCapabilities;
    }

    @Override // com.mediatek.simservs.capability.CapabilitiesType
    public void initServiceInstance(Document domDoc) {
        NodeList conditionsNode = domDoc.getElementsByTagName(ConditionCapabilities.NODE_NAME);
        if (conditionsNode.getLength() > 0) {
            this.mConditionCapabilities = new ConditionCapabilities(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) conditionsNode.item(0));
        }
    }
}
