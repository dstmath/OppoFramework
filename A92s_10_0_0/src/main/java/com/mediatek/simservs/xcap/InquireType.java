package com.mediatek.simservs.xcap;

import android.util.Log;
import com.android.okhttp.Headers;
import com.android.okhttp.Response;
import com.mediatek.simservs.client.SimServs;
import com.mediatek.xcap.client.XcapClient;
import com.mediatek.xcap.client.XcapConstants;
import com.mediatek.xcap.client.XcapDebugParam;
import com.mediatek.xcap.client.uri.XcapUri;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.parsers.ParserConfigurationException;

public abstract class InquireType extends XcapElement {
    public InquireType(XcapUri xcapUri, String parentUri, String intendedId) throws XcapException, ParserConfigurationException {
        super(xcapUri, parentUri, intendedId);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:69:0x013e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:71:?, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:72:0x0142, code lost:
        if (r3 != null) goto L_0x0138;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:75:0x0160, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x0161, code lost:
        r0.printStackTrace();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:77:0x0169, code lost:
        throw new com.mediatek.simservs.xcap.XcapException(r0);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:69:0x013e A[ExcHandler: URISyntaxException (r0v4 'e' java.net.URISyntaxException A[CUSTOM_DECLARE]), PHI: r3 r5 
      PHI: (r3v1 'xcapClient' com.mediatek.xcap.client.XcapClient) = (r3v0 'xcapClient' com.mediatek.xcap.client.XcapClient), (r3v3 'xcapClient' com.mediatek.xcap.client.XcapClient) binds: [B:1:0x000e, B:35:0x00bc] A[DONT_GENERATE, DONT_INLINE]
      PHI: (r5v2 'ret' java.lang.String) = (r5v0 'ret' java.lang.String), (r5v6 'ret' java.lang.String) binds: [B:1:0x000e, B:35:0x00bc] A[DONT_GENERATE, DONT_INLINE], Splitter:B:1:0x000e] */
    public String getContent() throws XcapException {
        boolean z;
        XcapClient xcapClient = null;
        String ret = null;
        Headers.Builder headers = new Headers.Builder();
        try {
            String nodeUri = getNodeUri().toString();
            XcapDebugParam debugParam = XcapDebugParam.getInstance();
            if (debugParam.getEnableSimservQueryWhole() ? true : SimServs.sSimservQueryWhole) {
                nodeUri = nodeUri.substring(0, nodeUri.lastIndexOf(SimServs.SIMSERVS_FILENAME) + SimServs.SIMSERVS_FILENAME.length());
            }
            URI uri = new URI(nodeUri);
            SimServs simSrv = SimServs.getInstance();
            if (this.mNetwork != null) {
                xcapClient = new XcapClient(simSrv.getContext(), this.mNetwork, simSrv.getPhoneId());
            } else {
                xcapClient = new XcapClient(simSrv.getContext(), simSrv.getPhoneId());
            }
            if (this.mIntendedId != null) {
                headers.add("X-3GPP-Intended-Identity", "\"" + this.mIntendedId + "\"");
            }
            boolean disableETag = debugParam.getDisableETag() ? true : SimServs.sETagDisable;
            if (this.mEtag != null && !disableETag) {
                headers.add(XcapConstants.HDR_KEY_IF_NONE_MATCH, this.mEtag);
            }
            Response response = xcapClient.get(uri, headers.build());
            if (response != null) {
                if (response.code() != 200) {
                    if (response.code() != 304) {
                        if (response.code() == 409) {
                            ret = null;
                            InputStream is = response.body().byteStream();
                            if (is == null || !XcapElement.TRUE.equals(System.getProperty("xcap.handl409"))) {
                                throw new XcapException(409);
                            }
                            throw new XcapException(409, parse409ErrorMessage("phrase", is));
                        }
                        throw new XcapException(response.code());
                    }
                }
                String etagValue = response.header(XcapConstants.HDR_KEY_ETAG);
                if (etagValue != null) {
                    this.mIsSupportEtag = true;
                    this.mEtag = etagValue;
                    z = false;
                } else {
                    z = false;
                    this.mIsSupportEtag = false;
                    this.mEtag = null;
                }
                if (disableETag) {
                    this.mIsSupportEtag = z;
                }
                if (response.code() == 200) {
                    InputStream is2 = null;
                    try {
                        is2 = response.body().byteStream();
                        ret = convertStreamToString(is2);
                    } finally {
                        if (is2 != null) {
                            is2.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new XcapException(409);
        } catch (URISyntaxException e2) {
        } catch (Throwable th) {
            if (xcapClient != null) {
                xcapClient.shutdown();
            }
            throw th;
        }
        xcapClient.shutdown();
        Log.d(XcapElement.TAG, "Response XML:" + SimServs.encryptString(ret));
        return ret;
    }
}
