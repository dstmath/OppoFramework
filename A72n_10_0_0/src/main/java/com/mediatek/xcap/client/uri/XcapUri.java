package com.mediatek.xcap.client.uri;

import android.os.SystemProperties;
import com.mediatek.simservs.xcap.XcapElement;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

public class XcapUri {
    private static final char PATH_SEPARATOR = '/';
    private static final String RESOURCE_SELECTOR_SEPARATOR = "/~~/";
    private String mDocumentSelector = null;
    private String mNodeSelector = null;
    private String mXcapRoot = null;

    public String getXcapRoot() {
        return this.mXcapRoot;
    }

    public XcapUri setXcapRoot(String xcapRoot) throws IllegalArgumentException {
        if (xcapRoot.charAt(xcapRoot.length() - 1) == '/') {
            this.mXcapRoot = xcapRoot;
            return this;
        }
        throw new IllegalArgumentException("xcap root must end with /");
    }

    public String getDocumentSelector() {
        return this.mDocumentSelector;
    }

    public XcapUri setDocumentSelector(XcapDocumentSelector documentSelector) throws IllegalArgumentException {
        setDocumentSelector(documentSelector.toString());
        return this;
    }

    public XcapUri setDocumentSelector(String documentSelector) throws IllegalArgumentException {
        if (documentSelector.charAt(0) != '/') {
            this.mDocumentSelector = documentSelector;
            return this;
        }
        throw new IllegalArgumentException("document selector must not start with /");
    }

    public String getNodeSelector() {
        return this.mNodeSelector;
    }

    public XcapUri setNodeSelector(XcapNodeSelector nodeSelector) throws IllegalArgumentException {
        setNodeSelector(nodeSelector.toString());
        return this;
    }

    public XcapUri setNodeSelector(String nodeSelector) throws IllegalArgumentException {
        if (this.mDocumentSelector.charAt(0) != '/') {
            this.mNodeSelector = nodeSelector;
            return this;
        }
        throw new IllegalArgumentException("document selector must not start with /");
    }

    public URI toURI() throws URISyntaxException {
        StringBuilder sb = new StringBuilder(this.mXcapRoot);
        if (XcapElement.TRUE.equals(SystemProperties.get("persist.vendor.mtk.xcap.rawurl")) || XcapElement.TRUE.equals(SystemProperties.get("vendor.gsm.radio.ss.rawurl"))) {
            sb.append(this.mDocumentSelector);
            if (this.mNodeSelector != null) {
                sb.append(RESOURCE_SELECTOR_SEPARATOR);
                sb.append(this.mNodeSelector);
            }
        } else {
            sb.append(this.mDocumentSelector.replaceAll("\\+", "%2B"));
            if (this.mNodeSelector != null) {
                sb.append(RESOURCE_SELECTOR_SEPARATOR);
                sb.append(this.mNodeSelector.replaceAll("\\+", "%2B"));
            }
        }
        return new URI(sb.toString());
    }

    public static class XcapDocumentSelector {
        private static final String XCAP_GLOBAL_PATH = "global";
        private static final String XCAP_USER_PATH = "users";
        private String mAuid = null;
        private String mDocumentName = null;
        private StringBuilder mDocumentSelector = new StringBuilder();
        private String mXui = null;

        public XcapDocumentSelector queryPath(String newSegment) {
            if (this.mDocumentSelector.length() != 0) {
                this.mDocumentSelector.append(XcapUri.PATH_SEPARATOR);
            }
            this.mDocumentSelector.append(newSegment);
            return this;
        }

        public XcapDocumentSelector(String auid, String xui, String documentName) {
            this.mAuid = auid;
            this.mXui = xui;
            this.mDocumentName = documentName;
            queryPath(auid).queryPath(XCAP_USER_PATH).queryPath(xui).queryPath(documentName);
        }

        public XcapDocumentSelector(String auid, String documentName) {
            new StringBuilder();
            this.mAuid = auid;
            this.mDocumentName = documentName;
            queryPath(auid).queryPath(XCAP_GLOBAL_PATH).queryPath(documentName);
        }

        public String toString() {
            return this.mDocumentSelector.toString();
        }
    }

    public static class XcapNodeSelector {
        private final StringBuilder mNodeSelector = new StringBuilder();

        public XcapNodeSelector queryByNodeName(String elementName) {
            if (elementName == null) {
                return this;
            }
            if (this.mNodeSelector.length() != 0) {
                this.mNodeSelector.append(XcapUri.PATH_SEPARATOR);
            }
            this.mNodeSelector.append(elementName);
            return this;
        }

        public XcapNodeSelector queryByAttrName(String attrName) {
            if (this.mNodeSelector.length() != 0) {
                this.mNodeSelector.append(XcapUri.PATH_SEPARATOR);
            }
            StringBuilder sb = this.mNodeSelector;
            sb.append("@");
            sb.append(attrName);
            return this;
        }

        public XcapNodeSelector queryByNodeName(String elementName, String attrName) {
            if (this.mNodeSelector.length() != 0) {
                this.mNodeSelector.append(XcapUri.PATH_SEPARATOR);
            }
            this.mNodeSelector.append(elementName);
            queryByAttrName(attrName);
            return this;
        }

        public XcapNodeSelector queryByNodeName(String elementName, String attrName, String attrValue) {
            if (this.mNodeSelector.length() != 0) {
                this.mNodeSelector.append(XcapUri.PATH_SEPARATOR);
            }
            StringBuilder sb = this.mNodeSelector;
            sb.append(elementName);
            sb.append("[@");
            sb.append(attrName);
            sb.append("=\"");
            sb.append(attrValue);
            sb.append("\"]");
            return this;
        }

        public XcapNodeSelector queryByNodeName(String elementName, int pos) {
            if (this.mNodeSelector.length() != 0) {
                this.mNodeSelector.append(XcapUri.PATH_SEPARATOR);
            }
            StringBuilder sb = this.mNodeSelector;
            sb.append(elementName);
            sb.append("[");
            sb.append(pos);
            sb.append("]");
            return this;
        }

        public XcapNodeSelector queryByNodeNameWithPos(String elementName, int pos, String attrName, String attrValue) {
            if (this.mNodeSelector.length() != 0) {
                this.mNodeSelector.append(XcapUri.PATH_SEPARATOR);
            }
            StringBuilder sb = this.mNodeSelector;
            sb.append(elementName);
            sb.append("[");
            sb.append(pos);
            sb.append("]");
            sb.append("[@");
            sb.append(attrName);
            sb.append("=\"");
            sb.append(attrValue);
            sb.append("\"]");
            return this;
        }

        public XcapNodeSelector(String elementName) {
            queryByNodeName(elementName);
        }

        public XcapNodeSelector(String elementName, String attrName) {
            queryByNodeName(elementName, attrName);
        }

        public XcapNodeSelector(String elementName, String attrName, String attrValue) {
            queryByNodeName(elementName, attrName, attrValue);
        }

        public String toString() {
            return this.mNodeSelector.toString();
        }
    }

    public static String encodePath(String path) throws NullPointerException {
        if (path != null) {
            return new String(URLEncoder.encode(path));
        }
        throw new NullPointerException("string to encode is null");
    }
}
