package android.webkit;

import android.net.ParseException;
import android.net.Uri;
import android.net.WebAddress;
import android.util.Base64;
import android.util.Log;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public final class URLUtil {
    static final String ASSET_BASE = "file:///android_asset/";
    private static final Pattern BASE64_CONTENT_DISPOSITION_PATTERN = null;
    static final String CONTENT_BASE = "content:";
    private static final Pattern CONTENT_DISPOSITION_EXTRA_INLINE_PATTERN = null;
    private static final Pattern CONTENT_DISPOSITION_EXTRA_PATTERN = null;
    private static final Pattern CONTENT_DISPOSITION_PATTERN = null;
    static final String FILE_BASE = "file://";
    private static final String LOGTAG = "webkit";
    static final String PROXY_BASE = "file:///cookieless_proxy/";
    static final String RESOURCE_BASE = "file:///android_res/";
    private static final boolean TRACE = false;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.webkit.URLUtil.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.webkit.URLUtil.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.webkit.URLUtil.<clinit>():void");
    }

    public static String guessUrl(String inUrl) {
        String retVal = inUrl;
        if (inUrl.length() == 0 || inUrl.startsWith("about:") || inUrl.startsWith("data:") || inUrl.startsWith("file:") || inUrl.startsWith("javascript:")) {
            return inUrl;
        }
        if (inUrl.endsWith(".")) {
            inUrl = inUrl.substring(0, inUrl.length() - 1);
        }
        try {
            WebAddress webAddress = new WebAddress(inUrl);
            if (webAddress.getHost().indexOf(46) == -1) {
                webAddress.setHost("www." + webAddress.getHost() + ".com");
            }
            return webAddress.toString();
        } catch (ParseException e) {
            return retVal;
        }
    }

    public static String composeSearchUrl(String inQuery, String template, String queryPlaceHolder) {
        int placeHolderIndex = template.indexOf(queryPlaceHolder);
        if (placeHolderIndex < 0) {
            return null;
        }
        StringBuilder buffer = new StringBuilder();
        buffer.append(template.substring(0, placeHolderIndex));
        try {
            buffer.append(URLEncoder.encode(inQuery, "utf-8"));
            buffer.append(template.substring(queryPlaceHolder.length() + placeHolderIndex));
            return buffer.toString();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static byte[] decode(byte[] url) throws IllegalArgumentException {
        if (url.length == 0) {
            return new byte[0];
        }
        byte[] tempData = new byte[url.length];
        int tempCount = 0;
        int i = 0;
        while (i < url.length) {
            byte b = url[i];
            if (b == (byte) 37) {
                if (url.length - i > 2) {
                    b = (byte) ((parseHex(url[i + 1]) * 16) + parseHex(url[i + 2]));
                    i += 2;
                } else {
                    throw new IllegalArgumentException("Invalid format");
                }
            }
            int tempCount2 = tempCount + 1;
            tempData[tempCount] = b;
            i++;
            tempCount = tempCount2;
        }
        byte[] retData = new byte[tempCount];
        System.arraycopy(tempData, 0, retData, 0, tempCount);
        return retData;
    }

    static boolean verifyURLEncoding(String url) {
        int count = url.length();
        if (count == 0) {
            return false;
        }
        int index = url.indexOf(37);
        while (index >= 0 && index < count) {
            if (index >= count - 2) {
                return false;
            }
            index++;
            try {
                parseHex((byte) url.charAt(index));
                index++;
                parseHex((byte) url.charAt(index));
                index = url.indexOf(37, index + 1);
            } catch (IllegalArgumentException e) {
                return false;
            }
        }
        return true;
    }

    private static int parseHex(byte b) {
        if (b >= (byte) 48 && b <= (byte) 57) {
            return b - 48;
        }
        if (b >= (byte) 65 && b <= (byte) 70) {
            return (b - 65) + 10;
        }
        if (b >= (byte) 97 && b <= (byte) 102) {
            return (b - 97) + 10;
        }
        throw new IllegalArgumentException("Invalid hex char '" + b + "'");
    }

    public static boolean isAssetUrl(String url) {
        return url != null ? url.startsWith(ASSET_BASE) : false;
    }

    public static boolean isResourceUrl(String url) {
        return url != null ? url.startsWith(RESOURCE_BASE) : false;
    }

    @Deprecated
    public static boolean isCookielessProxyUrl(String url) {
        return url != null ? url.startsWith(PROXY_BASE) : false;
    }

    public static boolean isFileUrl(String url) {
        if (url == null || !url.startsWith(FILE_BASE) || url.startsWith(ASSET_BASE) || url.startsWith(PROXY_BASE)) {
            return false;
        }
        return true;
    }

    public static boolean isAboutUrl(String url) {
        return url != null ? url.startsWith("about:") : false;
    }

    public static boolean isDataUrl(String url) {
        return url != null ? url.startsWith("data:") : false;
    }

    public static boolean isJavaScriptUrl(String url) {
        return url != null ? url.startsWith("javascript:") : false;
    }

    public static boolean isHttpUrl(String url) {
        if (url == null || url.length() <= 6) {
            return false;
        }
        return url.substring(0, 7).equalsIgnoreCase("http://");
    }

    public static boolean isHttpsUrl(String url) {
        if (url == null || url.length() <= 7) {
            return false;
        }
        return url.substring(0, 8).equalsIgnoreCase("https://");
    }

    public static boolean isNetworkUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        return !isHttpUrl(url) ? isHttpsUrl(url) : true;
    }

    public static boolean isContentUrl(String url) {
        return url != null ? url.startsWith(CONTENT_BASE) : false;
    }

    public static boolean isValidUrl(String url) {
        if (url == null || url.length() == 0) {
            return false;
        }
        boolean z;
        if (isAssetUrl(url) || isResourceUrl(url) || isFileUrl(url) || isAboutUrl(url) || isHttpUrl(url) || isHttpsUrl(url) || isJavaScriptUrl(url)) {
            z = true;
        } else {
            z = isContentUrl(url);
        }
        return z;
    }

    public static String stripAnchor(String url) {
        int anchorIndex = url.indexOf(35);
        if (anchorIndex != -1) {
            return url.substring(0, anchorIndex);
        }
        return url;
    }

    public static final String guessFileName(String url, String contentDisposition, String mimeType) {
        int index;
        String filename = null;
        String extension = null;
        if (contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (filename != null) {
                index = filename.lastIndexOf(47) + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
        }
        if (filename == null) {
            String decodedUrl = Uri.decode(url);
            if (decodedUrl != null) {
                int queryIndex = decodedUrl.indexOf(63);
                if (queryIndex > 0) {
                    decodedUrl = decodedUrl.substring(0, queryIndex);
                }
                if (!decodedUrl.endsWith("/")) {
                    index = decodedUrl.lastIndexOf(47) + 1;
                    if (index > 0) {
                        filename = decodedUrl.substring(index);
                    }
                }
            }
        }
        if (filename == null) {
            filename = "downloadfile";
        }
        int dotIndex = filename.indexOf(46);
        if (dotIndex < 0) {
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                if (extension != null) {
                    extension = "." + extension;
                }
            }
            if (extension == null) {
                if (mimeType == null || !mimeType.toLowerCase(Locale.ROOT).startsWith("text/")) {
                    extension = ".bin";
                } else if (mimeType.equalsIgnoreCase("text/html")) {
                    extension = ".html";
                } else {
                    extension = ".txt";
                }
            }
        } else {
            if (mimeType != null) {
                String typeFromExt = MimeTypeMap.getSingleton().getMimeTypeFromExtension(filename.substring(filename.lastIndexOf(46) + 1));
                if (!(typeFromExt == null || typeFromExt.equalsIgnoreCase(mimeType))) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
                    if (extension != null) {
                        extension = "." + extension;
                    }
                }
            }
            if (extension == null) {
                extension = filename.substring(dotIndex);
            }
            filename = filename.substring(0, dotIndex);
        }
        return filename + extension;
    }

    public static String parseContentDispositionPublic(String contentDisposition) {
        return parseContentDisposition(contentDisposition);
    }

    static String parseContentDisposition(String contentDisposition) {
        Matcher m;
        String fileName = null;
        try {
            m = BASE64_CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                try {
                    fileName = URLDecoder.decode(new String(Base64.decode(m.group(3), 0), m.group(2)), m.group(2));
                } catch (UnsupportedEncodingException e) {
                    Log.d(LOGTAG, "UnsupportedEncodingException: " + contentDisposition);
                } catch (IllegalArgumentException e2) {
                    Log.d(LOGTAG, "IllegalArgumentException: " + contentDisposition);
                }
            }
        } catch (IllegalStateException e3) {
            Log.d(LOGTAG, "IllegalStateException: illBase64Ex: " + contentDisposition);
        }
        if (fileName == null) {
            try {
                m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
                if (m.find()) {
                    fileName = m.group(2);
                }
            } catch (IllegalStateException e4) {
                Log.d(LOGTAG, "IllegalStateException: ex: " + contentDisposition);
            }
        }
        if (fileName == null) {
            try {
                m = CONTENT_DISPOSITION_EXTRA_PATTERN.matcher(contentDisposition);
                if (m.find()) {
                    fileName = m.group(2);
                }
            } catch (IllegalStateException e5) {
                Log.d(LOGTAG, "Extra IllegalStateException: ex: " + contentDisposition);
            }
        }
        if (fileName != null) {
            return fileName;
        }
        try {
            m = CONTENT_DISPOSITION_EXTRA_INLINE_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(2);
            }
            return fileName;
        } catch (IllegalStateException e6) {
            Log.d(LOGTAG, "Extra inline IllegalStateException: ex: " + contentDisposition);
            return fileName;
        }
    }

    static void testParseContentDisposition() {
        Log.d(LOGTAG, parseContentDisposition("attachment; filename=\"image001.jpg\""));
        Log.d(LOGTAG, parseContentDisposition("attachment; filename=\"=?ISO-8859-1?B?aW1hZ2UwMDEuanBn?=\""));
        Log.d(LOGTAG, parseContentDisposition("attachment; filename=\"=?UTF-8?B?aW1hZ2UwMDEuanBn?=\""));
        Log.d(LOGTAG, parseContentDisposition("attachment; filename=\"=?GB2312?B?JWQ2JWQwJWNlJWM0JWJhJWJhJWQ3JWQ2?=\""));
    }
}
