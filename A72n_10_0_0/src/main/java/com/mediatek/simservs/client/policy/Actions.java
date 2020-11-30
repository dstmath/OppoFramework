package com.mediatek.simservs.client.policy;

import android.util.Log;
import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Actions extends XcapElement implements ConfigureType {
    public static final String NODE_NAME = "cp:actions";
    static final String TAG_ALLOW = "allow";
    static final String TAG_FORWARD_TO = "forward-to";
    public boolean mAllow;
    private boolean mAppendAllow = false;
    public ForwardTo mForwardTo;
    public NoReplyTimer mNoReplyTimer;

    public Actions(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
        this.mForwardTo = new ForwardTo(xcapUri, parentUri, intendedId);
    }

    public Actions(XcapUri xcapUri, String parentUri, String intendedId, Element domElement) {
        super(xcapUri, parentUri, intendedId);
        instantiateFromXmlNode(domElement);
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    @Override // com.mediatek.simservs.xcap.ConfigureType
    public void instantiateFromXmlNode(Node domNode) {
        Element domElement = (Element) domNode;
        NodeList actionNode = domElement.getElementsByTagName(TAG_ALLOW);
        if (actionNode.getLength() > 0) {
            this.mAllow = ((Element) actionNode.item(0)).getTextContent().equals(XcapElement.TRUE);
            this.mAppendAllow = true;
        } else {
            NodeList actionNode2 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", TAG_ALLOW);
            if (actionNode2.getLength() > 0) {
                this.mAllow = ((Element) actionNode2.item(0)).getTextContent().equals(XcapElement.TRUE);
                this.mAppendAllow = true;
            } else {
                NodeList actionNode3 = domElement.getElementsByTagName("ss:allow");
                if (actionNode3.getLength() > 0) {
                    this.mAllow = ((Element) actionNode3.item(0)).getTextContent().equals(XcapElement.TRUE);
                    this.mAppendAllow = true;
                }
            }
        }
        if (domElement.getElementsByTagName("forward-to").getLength() > 0) {
            this.mForwardTo = new ForwardTo(this.mXcapUri, NODE_NAME, this.mIntendedId, domElement);
        } else if (domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "forward-to").getLength() > 0) {
            this.mForwardTo = new ForwardTo(this.mXcapUri, NODE_NAME, this.mIntendedId, domElement);
        } else if (domElement.getElementsByTagName("ss:forward-to").getLength() > 0) {
            this.mForwardTo = new ForwardTo(this.mXcapUri, NODE_NAME, this.mIntendedId, domElement);
        }
        NodeList actionNode4 = domElement.getElementsByTagName("NoReplyTimer");
        if (actionNode4.getLength() > 0) {
            Log.d("Actions", "Got NoReplyTimer");
            this.mNoReplyTimer = new NoReplyTimer(this.mXcapUri, "NoReplyTimer", this.mIntendedId, Integer.parseInt(((Element) actionNode4.item(0)).getTextContent()));
            return;
        }
        NodeList actionNode5 = domElement.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "NoReplyTimer");
        if (actionNode5.getLength() > 0) {
            Log.d("Actions", "Got NoReplyTimer with xcap namespace");
            this.mNoReplyTimer = new NoReplyTimer(this.mXcapUri, "NoReplyTimer", this.mIntendedId, Integer.parseInt(((Element) actionNode5.item(0)).getTextContent()));
            return;
        }
        NodeList actionNode6 = domElement.getElementsByTagName("ss:NoReplyTimer");
        if (actionNode6.getLength() > 0) {
            Log.d("Actions", "Got ss:NoReplyTimer");
            this.mNoReplyTimer = new NoReplyTimer(this.mXcapUri, "NoReplyTimer", this.mIntendedId, Integer.parseInt(((Element) actionNode6.item(0)).getTextContent()));
        }
    }

    public Element toXmlElement(Document document) {
        Element actionsElement = document.createElement(NODE_NAME);
        ForwardTo forwardTo = this.mForwardTo;
        if (forwardTo != null) {
            actionsElement.appendChild(forwardTo.toXmlElement(document));
        } else if (this.mAppendAllow) {
            String str = XcapElement.FALSE;
            if (XcapElement.TRUE.equals(System.getProperty("xcap.ns.ss", str))) {
                Element allowElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:allow");
                if (this.mAllow) {
                    str = XcapElement.TRUE;
                }
                allowElement.setTextContent(str);
                actionsElement.appendChild(allowElement);
            } else {
                Element allowElement2 = document.createElement(TAG_ALLOW);
                if (this.mAllow) {
                    str = XcapElement.TRUE;
                }
                allowElement2.setTextContent(str);
                actionsElement.appendChild(allowElement2);
            }
        }
        NoReplyTimer noReplyTimer = this.mNoReplyTimer;
        if (noReplyTimer != null) {
            actionsElement.appendChild(noReplyTimer.toXmlElement(document));
        }
        return actionsElement;
    }

    public void setAllow(boolean allow) {
        this.mAllow = allow;
        this.mAppendAllow = true;
    }

    public boolean isAllow() {
        return this.mAllow;
    }

    public void setFowardTo(String target, boolean notifyCaller) {
        if (this.mForwardTo == null) {
            this.mForwardTo = new ForwardTo(this.mXcapUri, this.mParentUri, this.mIntendedId);
        }
        this.mForwardTo.setTarget(target);
        this.mForwardTo.setNotifyCaller(notifyCaller);
    }

    public ForwardTo getFowardTo() {
        return this.mForwardTo;
    }

    public void setNoReplyTimer(int value) {
        NoReplyTimer noReplyTimer = this.mNoReplyTimer;
        if (noReplyTimer != null) {
            noReplyTimer.setValue(value);
        } else if (value != -1) {
            this.mNoReplyTimer = new NoReplyTimer(this.mXcapUri, "NoReplyTimer", this.mIntendedId, value);
        }
    }

    public int getNoReplyTimer() {
        NoReplyTimer noReplyTimer = this.mNoReplyTimer;
        if (noReplyTimer != null) {
            return noReplyTimer.getValue();
        }
        return -1;
    }

    public class NoReplyTimer extends XcapElement {
        public static final String NODE_NAME = "NoReplyTimer";
        public int mValue;

        public NoReplyTimer(XcapUri cdUri, String parentUri, String intendedId) {
            super(cdUri, parentUri, intendedId);
        }

        public NoReplyTimer(XcapUri cdUri, String parentUri, String intendedId, int initValue) {
            super(cdUri, parentUri, intendedId);
            this.mValue = initValue;
            Log.d("Actions", "new NoReplyTimer  mValue=" + this.mValue);
        }

        /* access modifiers changed from: protected */
        @Override // com.mediatek.simservs.xcap.XcapElement
        public String getNodeName() {
            return "NoReplyTimer";
        }

        public int getValue() {
            return this.mValue;
        }

        public void setValue(int value) {
            this.mValue = value;
        }

        public String toXmlString() {
            return "<NoReplyTimer>" + String.valueOf(this.mValue) + "</NoReplyTimer>";
        }

        public Element toXmlElement(Document document) {
            if (XcapElement.TRUE.equals(System.getProperty("xcap.ns.ss", XcapElement.FALSE))) {
                Element noReplyTimerElement = document.createElementNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", "ss:NoReplyTimer");
                noReplyTimerElement.setTextContent(String.valueOf(this.mValue));
                return noReplyTimerElement;
            }
            Element noReplyTimerElement2 = document.createElement("NoReplyTimer");
            noReplyTimerElement2.setTextContent(String.valueOf(this.mValue));
            return noReplyTimerElement2;
        }
    }
}
