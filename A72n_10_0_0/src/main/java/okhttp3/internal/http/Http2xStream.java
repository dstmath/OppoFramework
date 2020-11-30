package okhttp3.internal.http;

import java.io.IOException;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.framed.ErrorCode;
import okhttp3.internal.framed.FramedConnection;
import okhttp3.internal.framed.FramedStream;
import okhttp3.internal.framed.Header;
import okio.ByteString;
import okio.ForwardingSource;
import okio.Okio;
import okio.Sink;
import okio.Source;

public final class Http2xStream implements HttpStream {
    private static final ByteString CONNECTION = ByteString.encodeUtf8("connection");
    private static final ByteString ENCODING = ByteString.encodeUtf8("encoding");
    private static final ByteString HOST = ByteString.encodeUtf8("host");
    private static final List<ByteString> HTTP_2_SKIPPED_REQUEST_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
    private static final List<ByteString> HTTP_2_SKIPPED_RESPONSE_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TE, TRANSFER_ENCODING, ENCODING, UPGRADE);
    private static final ByteString KEEP_ALIVE = ByteString.encodeUtf8("keep-alive");
    private static final ByteString PROXY_CONNECTION = ByteString.encodeUtf8("proxy-connection");
    private static final List<ByteString> SPDY_3_SKIPPED_REQUEST_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TRANSFER_ENCODING, Header.TARGET_METHOD, Header.TARGET_PATH, Header.TARGET_SCHEME, Header.TARGET_AUTHORITY, Header.TARGET_HOST, Header.VERSION);
    private static final List<ByteString> SPDY_3_SKIPPED_RESPONSE_HEADERS = Util.immutableList(CONNECTION, HOST, KEEP_ALIVE, PROXY_CONNECTION, TRANSFER_ENCODING);
    private static final ByteString TE = ByteString.encodeUtf8("te");
    private static final ByteString TRANSFER_ENCODING = ByteString.encodeUtf8("transfer-encoding");
    private static final ByteString UPGRADE = ByteString.encodeUtf8("upgrade");
    private final OkHttpClient client;
    private final FramedConnection framedConnection;
    private FramedStream stream;
    private final StreamAllocation streamAllocation;

    public Http2xStream(OkHttpClient client2, StreamAllocation streamAllocation2, FramedConnection framedConnection2) {
        this.client = client2;
        this.streamAllocation = streamAllocation2;
        this.framedConnection = framedConnection2;
    }

    @Override // okhttp3.internal.http.HttpStream
    public Sink createRequestBody(Request request, long contentLength) {
        return this.stream.getSink();
    }

    @Override // okhttp3.internal.http.HttpStream
    public void writeRequestHeaders(Request request) throws IOException {
        List<Header> requestHeaders;
        if (this.stream == null) {
            boolean permitsRequestBody = HttpMethod.permitsRequestBody(request.method());
            if (this.framedConnection.getProtocol() == Protocol.HTTP_2) {
                requestHeaders = http2HeadersList(request);
            } else {
                requestHeaders = spdy3HeadersList(request);
            }
            this.stream = this.framedConnection.newStream(requestHeaders, permitsRequestBody, true);
            this.stream.readTimeout().timeout((long) this.client.readTimeoutMillis(), TimeUnit.MILLISECONDS);
            this.stream.writeTimeout().timeout((long) this.client.writeTimeoutMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Override // okhttp3.internal.http.HttpStream
    public void finishRequest() throws IOException {
        this.stream.getSink().close();
    }

    @Override // okhttp3.internal.http.HttpStream
    public Response.Builder readResponseHeaders() throws IOException {
        if (this.framedConnection.getProtocol() == Protocol.HTTP_2) {
            return readHttp2HeadersList(this.stream.getResponseHeaders());
        }
        return readSpdy3HeadersList(this.stream.getResponseHeaders());
    }

    public static List<Header> spdy3HeadersList(Request request) {
        Headers headers = request.headers();
        List<Header> result = new ArrayList<>(headers.size() + 5);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.url())));
        result.add(new Header(Header.VERSION, "HTTP/1.1"));
        result.add(new Header(Header.TARGET_HOST, Util.hostHeader(request.url(), false)));
        result.add(new Header(Header.TARGET_SCHEME, request.url().scheme()));
        Set<ByteString> names = new LinkedHashSet<>();
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!SPDY_3_SKIPPED_REQUEST_HEADERS.contains(name)) {
                String value = headers.value(i);
                if (names.add(name)) {
                    result.add(new Header(name, value));
                } else {
                    int j = 0;
                    while (true) {
                        if (j >= result.size()) {
                            break;
                        } else if (result.get(j).name.equals(name)) {
                            result.set(j, new Header(name, joinOnNull(result.get(j).value.utf8(), value)));
                            break;
                        } else {
                            j++;
                        }
                    }
                }
            }
        }
        return result;
    }

    private static String joinOnNull(String first, String second) {
        return first + (char) 0 + second;
    }

    public static List<Header> http2HeadersList(Request request) {
        Headers headers = request.headers();
        List<Header> result = new ArrayList<>(headers.size() + 4);
        result.add(new Header(Header.TARGET_METHOD, request.method()));
        result.add(new Header(Header.TARGET_PATH, RequestLine.requestPath(request.url())));
        result.add(new Header(Header.TARGET_AUTHORITY, Util.hostHeader(request.url(), false)));
        result.add(new Header(Header.TARGET_SCHEME, request.url().scheme()));
        int size = headers.size();
        for (int i = 0; i < size; i++) {
            ByteString name = ByteString.encodeUtf8(headers.name(i).toLowerCase(Locale.US));
            if (!HTTP_2_SKIPPED_REQUEST_HEADERS.contains(name)) {
                result.add(new Header(name, headers.value(i)));
            }
        }
        return result;
    }

    public static Response.Builder readSpdy3HeadersList(List<Header> headerBlock) throws IOException {
        String status = null;
        String status2 = "HTTP/1.1";
        Headers.Builder headersBuilder = new Headers.Builder();
        int i = 0;
        int size = headerBlock.size();
        while (i < size) {
            ByteString name = headerBlock.get(i).name;
            String values = headerBlock.get(i).value.utf8();
            String version = status2;
            String status3 = status;
            int start = 0;
            while (start < values.length()) {
                int end = values.indexOf(0, start);
                if (end == -1) {
                    end = values.length();
                }
                String value = values.substring(start, end);
                if (name.equals(Header.RESPONSE_STATUS)) {
                    status3 = value;
                } else if (name.equals(Header.VERSION)) {
                    version = value;
                } else if (!SPDY_3_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                    Internal.instance.addLenient(headersBuilder, name.utf8(), value);
                }
                start = end + 1;
            }
            i++;
            status = status3;
            status2 = version;
        }
        if (status != null) {
            StatusLine statusLine = StatusLine.parse(status2 + " " + status);
            return new Response.Builder().protocol(Protocol.SPDY_3).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
        }
        throw new ProtocolException("Expected ':status' header not present");
    }

    public static Response.Builder readHttp2HeadersList(List<Header> headerBlock) throws IOException {
        String status = null;
        Headers.Builder headersBuilder = new Headers.Builder();
        int size = headerBlock.size();
        for (int i = 0; i < size; i++) {
            ByteString name = headerBlock.get(i).name;
            String value = headerBlock.get(i).value.utf8();
            if (name.equals(Header.RESPONSE_STATUS)) {
                status = value;
            } else if (!HTTP_2_SKIPPED_RESPONSE_HEADERS.contains(name)) {
                Internal.instance.addLenient(headersBuilder, name.utf8(), value);
            }
        }
        if (status != null) {
            StatusLine statusLine = StatusLine.parse("HTTP/1.1 " + status);
            return new Response.Builder().protocol(Protocol.HTTP_2).code(statusLine.code).message(statusLine.message).headers(headersBuilder.build());
        }
        throw new ProtocolException("Expected ':status' header not present");
    }

    @Override // okhttp3.internal.http.HttpStream
    public ResponseBody openResponseBody(Response response) throws IOException {
        return new RealResponseBody(response.headers(), Okio.buffer(new StreamFinishingSource(this.stream.getSource())));
    }

    @Override // okhttp3.internal.http.HttpStream
    public void cancel() {
        if (this.stream != null) {
            this.stream.closeLater(ErrorCode.CANCEL);
        }
    }

    class StreamFinishingSource extends ForwardingSource {
        public StreamFinishingSource(Source delegate) {
            super(delegate);
        }

        @Override // okio.Source, okio.ForwardingSource, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            Http2xStream.this.streamAllocation.streamFinished(false, Http2xStream.this);
            super.close();
        }
    }
}
