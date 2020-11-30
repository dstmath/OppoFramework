package org.apache.http.cta;

import dalvik.system.PathClassLoader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.RequestLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EncodingUtils;

public final class CtaAdapter {
    private static String className = "com.mediatek.cta.CtaUtils";
    private static Method ctaPermissionChecker = null;
    private static String emailMessage = "Send emails";
    private static String emailPermission = "com.mediatek.permission.CTA_SEND_EMAIL";
    private static String jarPath = "system/framework/mediatek-cta.jar";
    private static String methodName = "enforceCheckPermission";
    private static String mmsMessage = "Send MMS";
    private static String mmsPermission = "com.mediatek.permission.CTA_SEND_MMS";

    public static boolean isSendingPermitted(HttpRequest request, HttpParams defaultParams) {
        System.out.println("[apache]:check permission begin!");
        try {
            if (ctaPermissionChecker == null) {
                ctaPermissionChecker = Class.forName(className, false, new PathClassLoader(jarPath, CtaAdapter.class.getClassLoader())).getDeclaredMethod(methodName, String.class, String.class);
                ctaPermissionChecker.setAccessible(true);
            }
            if (isMms(request, defaultParams)) {
                return ((Boolean) ctaPermissionChecker.invoke(null, mmsPermission, mmsMessage)).booleanValue();
            }
            if (isEmail(request)) {
                return ((Boolean) ctaPermissionChecker.invoke(null, emailPermission, emailMessage)).booleanValue();
            }
            return true;
        } catch (ReflectiveOperationException e) {
            PrintStream printStream = System.out;
            printStream.println("[apache] e:" + e);
            if (!(e.getCause() instanceof SecurityException)) {
                boolean z = e.getCause() instanceof ClassNotFoundException;
            } else {
                throw new SecurityException(e.getCause());
            }
        } catch (Throwable ee) {
            if (ee instanceof NoClassDefFoundError) {
                PrintStream printStream2 = System.out;
                printStream2.println("[apache] ee:" + ee);
            }
        }
    }

    private static boolean isMms(HttpRequest request, HttpParams defaultParams) {
        Header httpHeader;
        if (request.getRequestLine().getMethod().equals(HttpPost.METHOD_NAME)) {
            String userAgent = HttpProtocolParams.getUserAgent(defaultParams);
            if (!(userAgent == null || userAgent.indexOf("MMS") == -1)) {
                return isMmsSendPdu(request);
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (!(entity == null || (httpHeader = entity.getContentType()) == null || httpHeader.getValue() == null || !httpHeader.getValue().startsWith("application/vnd.wap.mms-message"))) {
                    return isMmsSendPdu(request);
                }
                Header[] headers = request.getHeaders(HTTP.CONTENT_TYPE);
                if (headers != null) {
                    for (Header header : headers) {
                        if (header.getValue().indexOf("application/vnd.wap.mms-message") != -1) {
                            return isMmsSendPdu(request);
                        }
                    }
                }
                Header[] headers2 = request.getHeaders("ACCEPT");
                if (headers2 != null) {
                    for (Header header2 : headers2) {
                        if (header2.getValue().indexOf("application/vnd.wap.mms-message") != -1) {
                            System.out.println("header done");
                            return isMmsSendPdu(request);
                        }
                    }
                }
            }
        }
        System.out.println("[apache]:not MMS!");
        return false;
    }

    private static boolean isEmail(HttpRequest request) {
        RequestLine reqLine = request.getRequestLine();
        System.out.println("isEmailSend:" + reqLine.getMethod());
        if (reqLine.getMethod().equals(HttpPost.METHOD_NAME) || reqLine.getMethod().equals(HttpPut.METHOD_NAME)) {
            Header[] hs = request.getAllHeaders();
            System.out.println("getAllHeaders:" + reqLine.getMethod());
            int length = hs.length;
            for (int i = 0; i < length; i++) {
                Header h = hs[i];
                System.out.println("test:" + h.getName() + ":" + h.getValue());
            }
            if (request instanceof HttpEntityEnclosingRequest) {
                HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                if (entity != null) {
                    Header httpHeader = entity.getContentType();
                    System.out.println("httpHeader:" + httpHeader);
                    if (!(httpHeader == null || httpHeader.getValue() == null || !(httpHeader.getValue().startsWith("message/rfc822") || httpHeader.getValue().startsWith("application/vnd.ms-sync.wbxml")))) {
                        return true;
                    }
                }
                Header[] headers = request.getHeaders(HTTP.CONTENT_TYPE);
                if (headers != null) {
                    for (Header header : headers) {
                        if (header.getValue().startsWith("message/rfc822") || header.getValue().startsWith("application/vnd.ms-sync.wbxml")) {
                            return true;
                        }
                    }
                }
            }
        }
        System.out.println("[apache]:not Email!");
        return false;
    }

    private static boolean isMmsSendPdu(HttpRequest request) {
        InputStream nis;
        if (request instanceof HttpEntityEnclosingRequest) {
            System.out.println("[apache]:Check isMmsSendPdu");
            HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
            if (entity != null) {
                byte[] buf = new byte[2];
                try {
                    InputStream is = entity.getContent();
                    Header contentEncoding = entity.getContentEncoding();
                    if (contentEncoding == null || !contentEncoding.getValue().equals("gzip")) {
                        nis = is;
                    } else {
                        nis = new GZIPInputStream(is);
                    }
                    int len = nis.read(buf);
                    PrintStream printStream = System.out;
                    printStream.println("PDU read len:" + len);
                    if (len == 2) {
                        PrintStream printStream2 = System.out;
                        printStream2.println("MMS PDU Type:" + (buf[0] & 255) + ":" + (buf[1] & 255));
                        if ((buf[0] & 255) == 140 && (buf[1] & 255) == 128) {
                            return true;
                        }
                    }
                } catch (IOException e) {
                    PrintStream printStream3 = System.out;
                    printStream3.println("[apache]:" + e);
                } catch (IndexOutOfBoundsException ee) {
                    PrintStream printStream4 = System.out;
                    printStream4.println("[apache]:" + ee);
                }
            }
        }
        System.out.println("[apache]:not MMS!");
        return false;
    }

    public static HttpResponse returnBadHttpResponse() {
        HttpResponse response = new BasicHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_BAD_REQUEST, "Bad Request"));
        ByteArrayEntity entity = new ByteArrayEntity(EncodingUtils.getAsciiBytes("User Permission is denied"));
        entity.setContentType("text/plain; charset=US-ASCII");
        response.setEntity(entity);
        return response;
    }
}
