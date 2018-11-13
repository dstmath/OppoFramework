package org.apache.http.client.methods;

import java.net.URI;

@Deprecated
public class HttpPost extends HttpEntityEnclosingRequestBase {
    public static final String METHOD_NAME = "POST";

    public HttpPost(URI uri) {
        setURI(uri);
    }

    public HttpPost(String uri) {
        if (uri == null) {
            throw new IllegalArgumentException();
        }
        try {
            if ("eng".equals((String) Class.forName("android.os.Build").getDeclaredField("TYPE").get(null))) {
                System.out.println("httppost:" + uri);
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
        setURI(URI.create(uri));
    }

    public String getMethod() {
        return METHOD_NAME;
    }
}
