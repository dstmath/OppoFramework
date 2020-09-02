package com.mediatek.simservs.capability;

import com.mediatek.simservs.xcap.ConfigureType;
import com.mediatek.simservs.xcap.XcapElement;
import com.mediatek.xcap.client.uri.XcapUri;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MediaConditions extends XcapElement implements ConfigureType {
    public static final String NODE_NAME = "serv-cap-media";
    static final String TAG_MEDIA = "media";
    List<String> mMedias;

    public MediaConditions(XcapUri xcapUri, String parentUri, String intendedId) {
        super(xcapUri, parentUri, intendedId);
    }

    public MediaConditions(XcapUri xcapUri, String parentUri, String intendedId, Element domElement) {
        super(xcapUri, parentUri, intendedId);
        instantiateFromXmlNode(domElement);
    }

    @Override // com.mediatek.simservs.xcap.ConfigureType
    public void instantiateFromXmlNode(Node domNode) {
        NodeList mediasNode = ((Element) domNode).getElementsByTagName(TAG_MEDIA);
        this.mMedias = new LinkedList();
        for (int i = 0; i < mediasNode.getLength(); i++) {
            this.mMedias.add(((Element) mediasNode.item(i)).getTextContent());
        }
    }

    /* access modifiers changed from: protected */
    @Override // com.mediatek.simservs.xcap.XcapElement
    public String getNodeName() {
        return NODE_NAME;
    }

    public List<String> getMedias() {
        return this.mMedias;
    }
}
