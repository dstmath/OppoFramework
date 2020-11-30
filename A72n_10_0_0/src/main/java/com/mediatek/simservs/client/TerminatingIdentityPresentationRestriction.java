package com.mediatek.simservs.client;

import android.net.Network;
import android.util.Log;
import com.mediatek.simservs.xcap.XcapException;
import com.mediatek.xcap.client.uri.XcapUri;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TerminatingIdentityPresentationRestriction extends SimservType {
    public static int NODE_DEFAULT_BEHAVIOUR = 2;
    public static final String NODE_NAME = "terminating-identity-presentation-restriction";
    public static int NODE_ROOT_FULL_CHILD = 0;
    public static int NODE_ROOT_NO_CHILD = 1;
    public boolean mContainDefaultBehaviour = false;
    public DefaultBehaviour mDefaultBehaviour;
    public int mNodeSelector = NODE_ROOT_FULL_CHILD;
    public boolean mShowActivePara = false;

    public TerminatingIdentityPresentationRestriction(XcapUri documentUri, String parentUri, String xui) throws Exception {
        super(documentUri, parentUri, xui);
    }

    @Override // com.mediatek.simservs.client.SimservType
    public void initServiceInstance(Document domDoc) {
        NodeList defaultBehaviour = domDoc.getElementsByTagName(DefaultBehaviour.NODE_NAME);
        if (defaultBehaviour.getLength() > 0) {
            this.mContainDefaultBehaviour = true;
            this.mDefaultBehaviour = new DefaultBehaviour(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) defaultBehaviour.item(0));
            if (this.mNetwork != null) {
                this.mDefaultBehaviour.setNetwork(this.mNetwork);
                return;
            }
            return;
        }
        NodeList defaultBehaviour2 = domDoc.getElementsByTagNameNS("http://uri.etsi.org/ngn/params/xml/simservs/xcap", DefaultBehaviour.NODE_NAME);
        if (defaultBehaviour2.getLength() > 0) {
            this.mContainDefaultBehaviour = true;
            this.mDefaultBehaviour = new DefaultBehaviour(this.mXcapUri, NODE_NAME, this.mIntendedId, (Element) defaultBehaviour2.item(0));
            if (this.mNetwork != null) {
                this.mDefaultBehaviour.setNetwork(this.mNetwork);
                return;
            }
            return;
        }
        this.mDefaultBehaviour = new DefaultBehaviour(this.mXcapUri, NODE_NAME, this.mIntendedId);
        if (this.mNetwork != null) {
            this.mDefaultBehaviour.setNetwork(this.mNetwork);
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    public void saveConfiguration() throws XcapException {
        String serviceXml = toXmlString();
        if (serviceXml == null) {
            Log.e(SimservType.TAG, "saveConfiguration: null xml string");
            return;
        }
        setContent(serviceXml);
        this.mContainDefaultBehaviour = true;
    }

    public String toXmlString() {
        try {
            Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = document.createElement(NODE_NAME);
            Log.d(SimservType.TAG, "toXmlString: mShowActivePara=" + this.mShowActivePara + ", mActived=" + this.mActived + ", mNodeSelector=" + this.mNodeSelector);
            if (this.mShowActivePara) {
                root.setAttribute("active", String.valueOf(this.mActived));
            }
            document.appendChild(root);
            if (this.mNodeSelector != NODE_ROOT_NO_CHILD) {
                root.appendChild(this.mDefaultBehaviour.toXmlElement(document));
            }
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

    public boolean isContainDefaultBehaviour() {
        return this.mContainDefaultBehaviour;
    }

    public boolean isDefaultPresentationRestricted() {
        return this.mDefaultBehaviour.isPresentationRestricted();
    }

    public void setDefaultPresentationRestricted(boolean presentationRestricted) throws XcapException {
        this.mDefaultBehaviour.setPresentationRestricted(presentationRestricted);
        if (isDefaultPresentationRestricted()) {
            this.mDefaultBehaviour.setContent(this.mDefaultBehaviour.toXmlString());
            return;
        }
        saveConfiguration();
    }

    public void setDefaultPresentationRestricted(boolean presentationRestricted, boolean nodeActive, int nodeSelector, boolean showActivePara) throws XcapException {
        this.mDefaultBehaviour.setPresentationRestricted(presentationRestricted);
        this.mActived = nodeActive;
        this.mShowActivePara = showActivePara;
        this.mNodeSelector = nodeSelector;
        if (this.mNodeSelector == NODE_DEFAULT_BEHAVIOUR) {
            this.mDefaultBehaviour.setContent(this.mDefaultBehaviour.toXmlString());
            return;
        }
        saveConfiguration();
    }

    @Override // com.mediatek.simservs.xcap.XcapElement
    public void setNetwork(Network network) {
        super.setNetwork(network);
        if (network != null && this.mDefaultBehaviour != null) {
            Log.i(SimservType.TAG, "XCAP dedicated network netid to mDefaultBehaviour: " + network);
            this.mDefaultBehaviour.setNetwork(network);
        }
    }
}
