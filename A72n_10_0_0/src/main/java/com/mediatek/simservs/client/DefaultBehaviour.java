package com.mediatek.simservs.client;

import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DefaultBehaviour extends XcapElement {
    public static final String DEFAULT_BEHAVIOUR_PRESENTATION_NOT_RESTRICTED = "presentation-not-restricted";
    public static final String DEFAULT_BEHAVIOUR_PRESENTATION_RESTRICTED = "presentation-restricted";
    public static final String NODE_NAME = "default-behaviour";
    public boolean mPresentationRestricted;

    public DefaultBehaviour(XcapUri cdUri, String parentUri, String intendedId) {
        super(cdUri, parentUri, intendedId);
    }

    public DefaultBehaviour(XcapUri xcapUri, String parentUri, String intendedId, Element domElement) {
        super(xcapUri, parentUri, intendedId);
        this.mPresentationRestricted = domElement.getTextContent().equals(DEFAULT_BEHAVIOUR_PRESENTATION_RESTRICTED);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    public String toXmlString() {
        if (this.mPresentationRestricted) {
            return "<default-behaviour>presentation-restricted</default-behaviour>";
        }
        return "<default-behaviour>presentation-not-restricted</default-behaviour>";
    }

    public Element toXmlElement(Document document) {
        Element defaultElement = document.createElement(NODE_NAME);
        if (this.mPresentationRestricted) {
            defaultElement.setTextContent(DEFAULT_BEHAVIOUR_PRESENTATION_RESTRICTED);
        } else {
            defaultElement.setTextContent(DEFAULT_BEHAVIOUR_PRESENTATION_NOT_RESTRICTED);
        }
        return defaultElement;
    }

    public boolean isPresentationRestricted() {
        return this.mPresentationRestricted;
    }

    public void setPresentationRestricted(boolean presentationRestricted) {
        this.mPresentationRestricted = presentationRestricted;
    }
}
