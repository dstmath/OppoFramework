package org.apache.http.client.methods;

import java.net.URI;

@Deprecated
public class HttpGet extends HttpRequestBase {
    public static final String METHOD_NAME = "GET";

    public HttpGet(URI uri) {
        setURI(uri);
    }

    public HttpGet(String uri) {
        String encodeUri = uri.trim().replaceAll(" ", "%20");
        try {
            if ("eng".equals((String) Class.forName("android.os.Build").getDeclaredField("TYPE").get(null))) {
                System.out.println("httpget:" + uri);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        setURI(URI.create(encodeUri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
