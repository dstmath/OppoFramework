package android.net.http;

import org.apache.http.HttpHost;

/* access modifiers changed from: package-private */
public interface RequestFeeder {
    Request getRequest();

    Request getRequest(HttpHost httpHost);

    boolean haveRequest(HttpHost httpHost);

    void requeueRequest(Request request);
}
