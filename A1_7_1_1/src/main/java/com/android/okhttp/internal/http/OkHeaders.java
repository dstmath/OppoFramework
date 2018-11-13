package com.android.okhttp.internal.http;

import com.android.okhttp.Authenticator;
import com.android.okhttp.Challenge;
import com.android.okhttp.Headers;
import com.android.okhttp.Request;
import com.android.okhttp.Request.Builder;
import com.android.okhttp.Response;
import com.android.okhttp.internal.Util;
import java.io.IOException;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class OkHeaders {
    private static final Comparator<String> FIELD_NAME_COMPARATOR = null;
    static final String PREFIX = null;
    public static final String RECEIVED_MILLIS = null;
    public static final String SELECTED_PROTOCOL = null;
    public static final String SENT_MILLIS = null;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.okhttp.internal.http.OkHeaders.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.okhttp.internal.http.OkHeaders.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.okhttp.internal.http.OkHeaders.<clinit>():void");
    }

    private OkHeaders() {
    }

    public static long contentLength(Request request) {
        return contentLength(request.headers());
    }

    public static long contentLength(Response response) {
        return contentLength(response.headers());
    }

    public static long contentLength(Headers headers) {
        return stringToLong(headers.get("Content-Length"));
    }

    private static long stringToLong(String s) {
        long j = -1;
        if (s == null) {
            return j;
        }
        try {
            return Long.parseLong(s);
        } catch (NumberFormatException e) {
            return j;
        }
    }

    public static Map<String, List<String>> toMultimap(Headers headers, String valueForNullKey) {
        Map<String, List<String>> result = new TreeMap(FIELD_NAME_COMPARATOR);
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            String fieldName = headers.name(i);
            String value = headers.value(i);
            List<String> allValues = new ArrayList();
            List<String> otherValues = (List) result.get(fieldName);
            if (otherValues != null) {
                allValues.addAll(otherValues);
            }
            allValues.add(value);
            result.put(fieldName, Collections.unmodifiableList(allValues));
        }
        if (valueForNullKey != null) {
            result.put(null, Collections.unmodifiableList(Collections.singletonList(valueForNullKey)));
        }
        return Collections.unmodifiableMap(result);
    }

    public static void addCookies(Builder builder, Map<String, List<String>> cookieHeaders) {
        for (Entry<String, List<String>> entry : cookieHeaders.entrySet()) {
            String key = (String) entry.getKey();
            if (("Cookie".equalsIgnoreCase(key) || "Cookie2".equalsIgnoreCase(key)) && !((List) entry.getValue()).isEmpty()) {
                builder.addHeader(key, buildCookieHeader((List) entry.getValue()));
            }
        }
    }

    private static String buildCookieHeader(List<String> cookies) {
        if (cookies.size() == 1) {
            return (String) cookies.get(0);
        }
        StringBuilder sb = new StringBuilder();
        int size = cookies.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sb.append("; ");
            }
            sb.append((String) cookies.get(i));
        }
        return sb.toString();
    }

    public static boolean varyMatches(Response cachedResponse, Headers cachedRequest, Request newRequest) {
        for (String field : varyFields(cachedResponse)) {
            if (!Util.equal(cachedRequest.values(field), newRequest.headers(field))) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasVaryAll(Response response) {
        return hasVaryAll(response.headers());
    }

    public static boolean hasVaryAll(Headers responseHeaders) {
        return varyFields(responseHeaders).contains("*");
    }

    private static Set<String> varyFields(Response response) {
        return varyFields(response.headers());
    }

    public static Set<String> varyFields(Headers responseHeaders) {
        Set<String> result = Collections.emptySet();
        int size = responseHeaders.size();
        for (int i = 0; i < size; i++) {
            if ("Vary".equalsIgnoreCase(responseHeaders.name(i))) {
                String value = responseHeaders.value(i);
                if (result.isEmpty()) {
                    result = new TreeSet(String.CASE_INSENSITIVE_ORDER);
                }
                for (String varyField : value.split(",")) {
                    result.add(varyField.trim());
                }
            }
        }
        return result;
    }

    public static Headers varyHeaders(Response response) {
        return varyHeaders(response.networkResponse().request().headers(), response.headers());
    }

    public static Headers varyHeaders(Headers requestHeaders, Headers responseHeaders) {
        Set<String> varyFields = varyFields(responseHeaders);
        if (varyFields.isEmpty()) {
            return new Headers.Builder().build();
        }
        Headers.Builder result = new Headers.Builder();
        int size = requestHeaders.size();
        for (int i = 0; i < size; i++) {
            String fieldName = requestHeaders.name(i);
            if (varyFields.contains(fieldName)) {
                result.add(fieldName, requestHeaders.value(i));
            }
        }
        return result.build();
    }

    static boolean isEndToEnd(String fieldName) {
        if ("Connection".equalsIgnoreCase(fieldName) || "Keep-Alive".equalsIgnoreCase(fieldName) || "Proxy-Authenticate".equalsIgnoreCase(fieldName) || "Proxy-Authorization".equalsIgnoreCase(fieldName) || "TE".equalsIgnoreCase(fieldName) || "Trailers".equalsIgnoreCase(fieldName) || "Transfer-Encoding".equalsIgnoreCase(fieldName) || "Upgrade".equalsIgnoreCase(fieldName)) {
            return false;
        }
        return true;
    }

    public static List<Challenge> parseChallenges(Headers responseHeaders, String challengeHeader) {
        List<com.squareup.okhttp.Challenge> result = new ArrayList();
        int size = responseHeaders.size();
        for (int i = 0; i < size; i++) {
            if (challengeHeader.equalsIgnoreCase(responseHeaders.name(i))) {
                String value = responseHeaders.value(i);
                String originValue = value;
                String splitter = " realm=\"";
                String[] schemeParts = value.split("[r|R][e|E][a|A][l|L][m|M]=\"");
                int schemeCount = schemeParts.length - 1;
                System.out.println("Scheme count=" + schemeCount);
                if (schemeCount == 0) {
                    System.out.println("no scheme found!!!");
                    return result;
                }
                for (int l = 0; l < schemeCount; l++) {
                    if (schemeCount == 1) {
                        value = schemeParts[l] + splitter + schemeParts[l + 1].trim();
                    } else if (l == 0) {
                        value = schemeParts[l] + splitter + schemeParts[l + 1].substring(0, schemeParts[l + 1].lastIndexOf(",")).trim();
                    } else if (l == schemeCount - 1) {
                        value = schemeParts[l].substring(schemeParts[l].lastIndexOf(",") + 1).trim() + splitter + schemeParts[l + 1];
                    } else {
                        value = schemeParts[l].substring(schemeParts[l].lastIndexOf(",") + 1).trim() + splitter + schemeParts[l + 1].substring(0, schemeParts[l + 1].lastIndexOf(",")).trim();
                    }
                    System.out.println("Round " + l + " Scheme value: " + value);
                    int pos = 0;
                    while (pos < value.length()) {
                        int tokenStart = pos;
                        pos = HeaderParser.skipUntil(value, pos, " ");
                        String scheme = value.substring(tokenStart, pos).trim();
                        System.out.println("scheme=" + scheme);
                        pos = HeaderParser.skipWhitespace(value, pos);
                        String rest = value.substring(pos);
                        pos += rest.length();
                        String realm = null;
                        String nonce = null;
                        String stale = null;
                        String qop = null;
                        String opaque = null;
                        int j = 0;
                        for (String field : rest.split(",")) {
                            System.out.println("field[" + j + "]: " + field);
                            String[] keyValue = field.trim().split("=");
                            if (keyValue.length < 2) {
                                System.out.println("No support:" + field);
                            } else {
                                String kv;
                                String key = keyValue[0];
                                if (keyValue.length > 2) {
                                    kv = field.trim().substring(key.length() + 1);
                                } else {
                                    kv = keyValue[1];
                                }
                                if (kv.indexOf("\"") >= 0) {
                                    kv = HeaderParser.getQuoteString(kv, key, 0);
                                }
                                System.out.println("key=" + key + ", value=" + kv);
                                if ("realm".equals(key)) {
                                    realm = kv;
                                    System.setProperty("digest.realm", kv);
                                } else if (!("uri".equals(key) || "algorithm".equals(key))) {
                                    if ("domain".equals(key)) {
                                        String domain = kv;
                                    } else if ("nonce".equals(key)) {
                                        nonce = kv;
                                    } else if ("stale".equals(key)) {
                                        stale = kv;
                                    } else if ("qop".equals(key)) {
                                        qop = kv;
                                    } else if ("opaque".equals(key)) {
                                        opaque = kv;
                                    }
                                }
                            }
                            j++;
                        }
                        Challenge ch = new Challenge(scheme, realm, nonce, stale, qop, opaque);
                        result.add(ch);
                        System.out.println("ch(allenge)=" + ch);
                    }
                }
            }
        }
        System.out.println(result);
        return result;
    }

    public static Request processAuthHeader(Authenticator authenticator, Response response, Proxy proxy) throws IOException {
        if (response.code() == 407) {
            return authenticator.authenticateProxy(proxy, response);
        }
        return authenticator.authenticate(proxy, response);
    }
}
