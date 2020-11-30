package okhttp3;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import okhttp3.Headers;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.StatusLine;
import okio.Buffer;
import okio.BufferedSource;

public final class Response implements Closeable {
    private final ResponseBody body;
    private volatile CacheControl cacheControl;
    private final Response cacheResponse;
    private final int code;
    private final Handshake handshake;
    private final Headers headers;
    private final String message;
    private final Response networkResponse;
    private final Response priorResponse;
    private final Protocol protocol;
    private final long receivedResponseAtMillis;
    private final Request request;
    private final long sentRequestAtMillis;

    private Response(Builder builder) {
        this.request = builder.request;
        this.protocol = builder.protocol;
        this.code = builder.code;
        this.message = builder.message;
        this.handshake = builder.handshake;
        this.headers = builder.headers.build();
        this.body = builder.body;
        this.networkResponse = builder.networkResponse;
        this.cacheResponse = builder.cacheResponse;
        this.priorResponse = builder.priorResponse;
        this.sentRequestAtMillis = builder.sentRequestAtMillis;
        this.receivedResponseAtMillis = builder.receivedResponseAtMillis;
    }

    public Request request() {
        return this.request;
    }

    public Protocol protocol() {
        return this.protocol;
    }

    public int code() {
        return this.code;
    }

    public boolean isSuccessful() {
        return this.code >= 200 && this.code < 300;
    }

    public String message() {
        return this.message;
    }

    public Handshake handshake() {
        return this.handshake;
    }

    public List<String> headers(String name) {
        return this.headers.values(name);
    }

    public String header(String name) {
        return header(name, null);
    }

    public String header(String name, String defaultValue) {
        String result = this.headers.get(name);
        return result != null ? result : defaultValue;
    }

    public Headers headers() {
        return this.headers;
    }

    public ResponseBody peekBody(long byteCount) throws IOException {
        Buffer result;
        BufferedSource source = this.body.source();
        source.request(byteCount);
        Buffer copy = source.buffer().clone();
        if (copy.size() > byteCount) {
            result = new Buffer();
            result.write(copy, byteCount);
            copy.clear();
        } else {
            result = copy;
        }
        return ResponseBody.create(this.body.contentType(), result.size(), result);
    }

    public ResponseBody body() {
        return this.body;
    }

    public Builder newBuilder() {
        return new Builder();
    }

    public boolean isRedirect() {
        switch (this.code) {
            case 300:
            case 301:
            case 302:
            case 303:
            case StatusLine.HTTP_TEMP_REDIRECT /* 307 */:
            case StatusLine.HTTP_PERM_REDIRECT /* 308 */:
                return true;
            case 304:
            case 305:
            case 306:
            default:
                return false;
        }
    }

    public Response networkResponse() {
        return this.networkResponse;
    }

    public Response cacheResponse() {
        return this.cacheResponse;
    }

    public Response priorResponse() {
        return this.priorResponse;
    }

    public List<Challenge> challenges() {
        String responseField;
        if (this.code == 401) {
            responseField = "WWW-Authenticate";
        } else if (this.code != 407) {
            return Collections.emptyList();
        } else {
            responseField = "Proxy-Authenticate";
        }
        return HttpHeaders.parseChallenges(headers(), responseField);
    }

    public CacheControl cacheControl() {
        CacheControl result = this.cacheControl;
        if (result != null) {
            return result;
        }
        CacheControl parse = CacheControl.parse(this.headers);
        this.cacheControl = parse;
        return parse;
    }

    public long sentRequestAtMillis() {
        return this.sentRequestAtMillis;
    }

    public long receivedResponseAtMillis() {
        return this.receivedResponseAtMillis;
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() {
        this.body.close();
    }

    public String toString() {
        return "Response{protocol=" + this.protocol + ", code=" + this.code + ", message=" + this.message + ", url=" + this.request.url() + '}';
    }

    public static class Builder {
        private ResponseBody body;
        private Response cacheResponse;
        private int code;
        private Handshake handshake;
        private Headers.Builder headers;
        private String message;
        private Response networkResponse;
        private Response priorResponse;
        private Protocol protocol;
        private long receivedResponseAtMillis;
        private Request request;
        private long sentRequestAtMillis;

        public Builder() {
            this.code = -1;
            this.headers = new Headers.Builder();
        }

        private Builder(Response response) {
            this.code = -1;
            this.request = response.request;
            this.protocol = response.protocol;
            this.code = response.code;
            this.message = response.message;
            this.handshake = response.handshake;
            this.headers = response.headers.newBuilder();
            this.body = response.body;
            this.networkResponse = response.networkResponse;
            this.cacheResponse = response.cacheResponse;
            this.priorResponse = response.priorResponse;
            this.sentRequestAtMillis = response.sentRequestAtMillis;
            this.receivedResponseAtMillis = response.receivedResponseAtMillis;
        }

        public Builder request(Request request2) {
            this.request = request2;
            return this;
        }

        public Builder protocol(Protocol protocol2) {
            this.protocol = protocol2;
            return this;
        }

        public Builder code(int code2) {
            this.code = code2;
            return this;
        }

        public Builder message(String message2) {
            this.message = message2;
            return this;
        }

        public Builder handshake(Handshake handshake2) {
            this.handshake = handshake2;
            return this;
        }

        public Builder header(String name, String value) {
            this.headers.set(name, value);
            return this;
        }

        public Builder addHeader(String name, String value) {
            this.headers.add(name, value);
            return this;
        }

        public Builder removeHeader(String name) {
            this.headers.removeAll(name);
            return this;
        }

        public Builder headers(Headers headers2) {
            this.headers = headers2.newBuilder();
            return this;
        }

        public Builder body(ResponseBody body2) {
            this.body = body2;
            return this;
        }

        public Builder networkResponse(Response networkResponse2) {
            if (networkResponse2 != null) {
                checkSupportResponse("networkResponse", networkResponse2);
            }
            this.networkResponse = networkResponse2;
            return this;
        }

        public Builder cacheResponse(Response cacheResponse2) {
            if (cacheResponse2 != null) {
                checkSupportResponse("cacheResponse", cacheResponse2);
            }
            this.cacheResponse = cacheResponse2;
            return this;
        }

        private void checkSupportResponse(String name, Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException(name + ".body != null");
            } else if (response.networkResponse != null) {
                throw new IllegalArgumentException(name + ".networkResponse != null");
            } else if (response.cacheResponse != null) {
                throw new IllegalArgumentException(name + ".cacheResponse != null");
            } else if (response.priorResponse != null) {
                throw new IllegalArgumentException(name + ".priorResponse != null");
            }
        }

        public Builder priorResponse(Response priorResponse2) {
            if (priorResponse2 != null) {
                checkPriorResponse(priorResponse2);
            }
            this.priorResponse = priorResponse2;
            return this;
        }

        private void checkPriorResponse(Response response) {
            if (response.body != null) {
                throw new IllegalArgumentException("priorResponse.body != null");
            }
        }

        public Builder sentRequestAtMillis(long sentRequestAtMillis2) {
            this.sentRequestAtMillis = sentRequestAtMillis2;
            return this;
        }

        public Builder receivedResponseAtMillis(long receivedResponseAtMillis2) {
            this.receivedResponseAtMillis = receivedResponseAtMillis2;
            return this;
        }

        public Response build() {
            if (this.request == null) {
                throw new IllegalStateException("request == null");
            } else if (this.protocol == null) {
                throw new IllegalStateException("protocol == null");
            } else if (this.code >= 0) {
                return new Response(this);
            } else {
                throw new IllegalStateException("code < 0: " + this.code);
            }
        }
    }
}
