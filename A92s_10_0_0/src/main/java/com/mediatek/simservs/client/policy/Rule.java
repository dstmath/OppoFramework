package com.mediatek.simservs.client.policy;

import android.util.Log;
import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.uri.XcapUri;
import java.net.URISyntaxException;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Rule extends XcapElement implements ConfigureType {
    public static final String NODE_NAME = "cp:rule";
    public static final String NODE_XML_NAMESPACE = "?xmlns(cp=urn:ietf:params:xml:ns:common-policy)";
    public Actions mActions;
    public Conditions mConditions;
    public String mId = "none";

    public Rule(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    public Rule(XcapUri xcapUri, String parentUri, String intendedId, Element domElement) {
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
        this.mId = domElement.getAttribute("id");
        NodeList conditionsNode = domElement.getElementsByTagName("conditions");
        if (conditionsNode.getLength() > 0) {
            this.mConditions = new Conditions(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) conditionsNode.item(0));
        } else {
            NodeList conditionsNode2 = domElement.getElementsByTagNameNS("urn:ietf:params:xml:ns:common-policy", "conditions");
            if (conditionsNode2.getLength() > 0) {
                this.mConditions = new Conditions(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) conditionsNode2.item(0));
            } else {
                NodeList conditionsNode3 = domElement.getElementsByTagName(Conditions.NODE_NAME);
                if (conditionsNode3.getLength() > 0) {
                    this.mConditions = new Conditions(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) conditionsNode3.item(0));
                } else {
                    this.mConditions = new Conditions(this.mXcapUri, NODE_NAME, this.mIntendedId);
                }
            }
        }
        NodeList actionsNode = domElement.getElementsByTagName("actions");
        if (actionsNode.getLength() > 0) {
            this.mActions = new Actions(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) actionsNode.item(0));
            return;
        }
        NodeList actionsNode2 = domElement.getElementsByTagNameNS("urn:ietf:params:xml:ns:common-policy", "actions");
        if (actionsNode2.getLength() > 0) {
            this.mActions = new Actions(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) actionsNode2.item(0));
            return;
        }
        NodeList actionsNode3 = domElement.getElementsByTagName(Actions.NODE_NAME);
        if (actionsNode3.getLength() > 0) {
            this.mActions = new Actions(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) actionsNode3.item(0));
        } else {
            this.mActions = new Actions(this.mXcapUri, NODE_NAME, this.mIntendedId);
        }
    }

    public Element toXmlElement(Document document) throws TransformerException {
        Element ruleElement = document.createElement(NODE_NAME);
        ruleElement.setAttribute("id", this.mId);
        Conditions conditions = this.mConditions;
        if (conditions != null) {
            ruleElement.appendChild(conditions.toXmlElement(document));
        }
        Actions actions = this.mActions;
        if (actions != null) {
            ruleElement.appendChild(actions.toXmlElement(document));
        }
        return ruleElement;
    }

    public Actions createActions() {
        if (this.mActions == null) {
            this.mActions = new Actions(this.mXcapUri, NODE_NAME, this.mIntendedId);
        }
        return this.mActions;
    }

    public Conditions createConditions() {
        if (this.mConditions == null) {
            this.mConditions = new Conditions(this.mXcapUri, NODE_NAME, this.mIntendedId);
        }
        return this.mConditions;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public Conditions getConditions() {
        return this.mConditions;
    }

    public Actions getActions() {
        return this.mActions;
    }

    @Override // com.mediatek.simservs.xcap.XcapElement
    public void setContent(String xml) throws XcapException {
        try {
            this.mNodeUri = getNodeUri().toString();
            if (getNodeName().equals(NODE_NAME)) {
                this.mNodeUri += "%5b@id=%22" + this.mId.replaceAll(" ", "%20") + "%22%5d" + NODE_XML_NAMESPACE;
            }
            Log.d("Rule", "setContent etag=" + this.mEtag);
            saveContent(xml);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void setContent(String xml, boolean hasXcapNS) throws XcapException {
        try {
            Log.d("Rule", "setContent hasXcapNS=" + hasXcapNS);
            this.mNodeUri = getNodeUri().toString();
            if (getNodeName().equals(NODE_NAME)) {
                this.mNodeUri += "%5b@id=%22" + this.mId.replaceAll(" ", "%20") + "%22%5d" + ("?" + (hasXcapNS ? "xmlns(ss=http://uri.etsi.org/ngn/params/xml/simservs/xcap)" : "") + "xmlns(cp=urn:ietf:params:xml:ns:common-policy)");
            }
            Log.d("Rule", "setContent etag=" + this.mEtag);
            saveContent(xml);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public String toXmlString() {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = toXmlElement(document);
            document.appendChild(root);
            return domToXmlText(root);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
            return null;
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
            return null;
        } catch (TransformerException e2) {
            e2.printStackTrace();
            return null;
        }
    }
}
