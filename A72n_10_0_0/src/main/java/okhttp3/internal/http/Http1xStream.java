package okhttp3.internal.http;

import java.io.EOFException;
import java.io.IOException;
import java.net.ProtocolException;
import java.util.concurrent.TimeUnit;
import okhttp3.Headers;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RealConnection;
import okhttp3.internal.connection.StreamAllocation;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ForwardingTimeout;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;

public final class Http1xStream implements HttpStream {
    private static final int STATE_CLOSED = 6;
    private static final int STATE_IDLE = 0;
    private static final int STATE_OPEN_REQUEST_BODY = 1;
    private static final int STATE_OPEN_RESPONSE_BODY = 4;
    private static final int STATE_READING_RESPONSE_BODY = 5;
    private static final int STATE_READ_RESPONSE_HEADERS = 3;
    private static final int STATE_WRITING_REQUEST_BODY = 2;
    private final OkHttpClient client;
    private final BufferedSink sink;
    private final BufferedSource source;
    private int state = 0;
    private final StreamAllocation streamAllocation;

    public Http1xStream(OkHttpClient client2, StreamAllocation streamAllocation2, BufferedSource source2, BufferedSink sink2) {
        this.client = client2;
        this.streamAllocation = streamAllocation2;
        this.source = source2;
        this.sink = sink2;
    }

    @Override // okhttp3.internal.http.HttpStream
    public Sink createRequestBody(Request request, long contentLength) {
        if ("chunked".equalsIgnoreCase(request.header("Transfer-Encoding"))) {
            return newChunkedSink();
        }
        if (contentLength != -1) {
            return newFixedLengthSink(contentLength);
        }
        throw new IllegalStateException("Cannot stream a request body without chunked encoding or a known content length!");
    }

    @Override // okhttp3.internal.http.HttpStream
    public void cancel() {
        RealConnection connection = this.streamAllocation.connection();
        if (connection != null) {
            connection.cancel();
        }
    }

    @Override // okhttp3.internal.http.HttpStream
    public void writeRequestHeaders(Request request) throws IOException {
        writeRequest(request.headers(), RequestLine.get(request, this.streamAllocation.connection().route().proxy().type()));
    }

    @Override // okhttp3.internal.http.HttpStream
    public Response.Builder readResponseHeaders() throws IOException {
        return readResponse();
    }

    @Override // okhttp3.internal.http.HttpStream
    public ResponseBody openResponseBody(Response response) throws IOException {
        return new RealResponseBody(response.headers(), Okio.buffer(getTransferStream(response)));
    }

    private Source getTransferStream(Response response) throws IOException {
        if (!HttpHeaders.hasBody(response)) {
            return newFixedLengthSource(0);
        }
        if ("chunked".equalsIgnoreCase(response.header("Transfer-Encoding"))) {
            return newChunkedSource(response.request().url());
        }
        long contentLength = HttpHeaders.contentLength(response);
        if (contentLength != -1) {
            return newFixedLengthSource(contentLength);
        }
        return newUnknownLengthSource();
    }

    public boolean isClosed() {
        return this.state == 6;
    }

    @Override // okhttp3.internal.http.HttpStream
    public void finishRequest() throws IOException {
        this.sink.flush();
    }

    public void writeRequest(Headers headers, String requestLine) throws IOException {
        if (this.state == 0) {
            this.sink.writeUtf8(requestLine).writeUtf8("\r\n");
            int size = headers.size();
            for (int i = 0; i < size; i++) {
                this.sink.writeUtf8(headers.name(i)).writeUtf8(": ").writeUtf8(headers.value(i)).writeUtf8("\r\n");
            }
            this.sink.writeUtf8("\r\n");
            this.state = 1;
            return;
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Response.Builder readResponse() throws IOException {
        StatusLine statusLine;
        Response.Builder responseBuilder;
        if (this.state == 1 || this.state == 3) {
            do {
                try {
                    statusLine = StatusLine.parse(this.source.readUtf8LineStrict());
                    responseBuilder = new Response.Builder().protocol(statusLine.protocol).code(statusLine.code).message(statusLine.message).headers(readHeaders());
                } catch (EOFException e) {
                    IOException exception = new IOException("unexpected end of stream on " + this.streamAllocation);
                    exception.initCause(e);
                    throw exception;
                }
            } while (statusLine.code == 100);
            this.state = 4;
            return responseBuilder;
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Headers readHeaders() throws IOException {
        Headers.Builder headers = new Headers.Builder();
        while (true) {
            String line = this.source.readUtf8LineStrict();
            if (line.length() == 0) {
                return headers.build();
            }
            Internal.instance.addLenient(headers, line);
        }
    }

    public Sink newChunkedSink() {
        if (this.state == 1) {
            this.state = 2;
            return new ChunkedSink();
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Sink newFixedLengthSink(long contentLength) {
        if (this.state == 1) {
            this.state = 2;
            return new FixedLengthSink(contentLength);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Source newFixedLengthSource(long length) throws IOException {
        if (this.state == 4) {
            this.state = 5;
            return new FixedLengthSource(length);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Source newChunkedSource(HttpUrl url) throws IOException {
        if (this.state == 4) {
            this.state = 5;
            return new ChunkedSource(url);
        }
        throw new IllegalStateException("state: " + this.state);
    }

    public Source newUnknownLengthSource() throws IOException {
        if (this.state != 4) {
            throw new IllegalStateException("state: " + this.state);
        } else if (this.streamAllocation != null) {
            this.state = 5;
            this.streamAllocation.noNewStreams();
            return new UnknownLengthSource();
        } else {
            throw new IllegalStateException("streamAllocation == null");
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void detachTimeout(ForwardingTimeout timeout) {
        Timeout oldDelegate = timeout.delegate();
        timeout.setDelegate(Timeout.NONE);
        oldDelegate.clearDeadline();
        oldDelegate.clearTimeout();
    }

    /* access modifiers changed from: private */
    public final class FixedLengthSink implements Sink {
        private long bytesRemaining;
        private boolean closed;
        private final ForwardingTimeout timeout;

        private FixedLengthSink(long bytesRemaining2) {
            this.timeout = new ForwardingTimeout(Http1xStream.this.sink.timeout());
            this.bytesRemaining = bytesRemaining2;
        }

        @Override // okio.Sink
        public Timeout timeout() {
            return this.timeout;
        }

        @Override // okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            if (!this.closed) {
                Util.checkOffsetAndCount(source.size(), 0, byteCount);
                if (byteCount <= this.bytesRemaining) {
                    Http1xStream.this.sink.write(source, byteCount);
                    this.bytesRemaining -= byteCount;
                    return;
                }
                throw new ProtocolException("expected " + this.bytesRemaining + " bytes but received " + byteCount);
            }
            throw new IllegalStateException("closed");
        }

        @Override // okio.Sink, java.io.Flushable
        public void flush() throws IOException {
            if (!this.closed) {
                Http1xStream.this.sink.flush();
            }
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable, okio.Sink
        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.bytesRemaining <= 0) {
                    Http1xStream.this.detachTimeout(this.timeout);
                    Http1xStream.this.state = 3;
                    return;
                }
                throw new ProtocolException("unexpected end of stream");
            }
        }
    }

    /* access modifiers changed from: private */
    public final class ChunkedSink implements Sink {
        private boolean closed;
        private final ForwardingTimeout timeout;

        private ChunkedSink() {
            this.timeout = new ForwardingTimeout(Http1xStream.this.sink.timeout());
        }

        @Override // okio.Sink
        public Timeout timeout() {
            return this.timeout;
        }

        @Override // okio.Sink
        public void write(Buffer source, long byteCount) throws IOException {
            if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (byteCount != 0) {
                Http1xStream.this.sink.writeHexadecimalUnsignedLong(byteCount);
                Http1xStream.this.sink.writeUtf8("\r\n");
                Http1xStream.this.sink.write(source, byteCount);
                Http1xStream.this.sink.writeUtf8("\r\n");
            }
        }

        @Override // okio.Sink, java.io.Flushable
        public synchronized void flush() throws IOException {
            if (!this.closed) {
                Http1xStream.this.sink.flush();
            }
        }

        @Override // java.io.Closeable, java.lang.AutoCloseable, okio.Sink
        public synchronized void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                Http1xStream.this.sink.writeUtf8("0\r\n\r\n");
                Http1xStream.this.detachTimeout(this.timeout);
                Http1xStream.this.state = 3;
            }
        }
    }

    private abstract class AbstractSource implements Source {
        protected boolean closed;
        protected final ForwardingTimeout timeout;

        private AbstractSource() {
            this.timeout = new ForwardingTimeout(Http1xStream.this.source.timeout());
        }

        @Override // okio.Source
        public Timeout timeout() {
            return this.timeout;
        }

        /* access modifiers changed from: protected */
        public final void endOfInput(boolean reuseConnection) throws IOException {
            if (Http1xStream.this.state != 6) {
                if (Http1xStream.this.state == 5) {
                    Http1xStream.this.detachTimeout(this.timeout);
                    Http1xStream.this.state = 6;
                    if (Http1xStream.this.streamAllocation != null) {
                        Http1xStream.this.streamAllocation.streamFinished(!reuseConnection, Http1xStream.this);
                        return;
                    }
                    return;
                }
                throw new IllegalStateException("state: " + Http1xStream.this.state);
            }
        }
    }

    /* access modifiers changed from: private */
    public class FixedLengthSource extends AbstractSource {
        private long bytesRemaining;

        public FixedLengthSource(long length) throws IOException {
            super();
            this.bytesRemaining = length;
            if (this.bytesRemaining == 0) {
                endOfInput(true);
            }
        }

        @Override // okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (this.bytesRemaining == 0) {
                return -1;
            } else {
                long read = Http1xStream.this.source.read(sink, Math.min(this.bytesRemaining, byteCount));
                if (read != -1) {
                    this.bytesRemaining -= read;
                    if (this.bytesRemaining == 0) {
                        endOfInput(true);
                    }
                    return read;
                }
                endOfInput(false);
                throw new ProtocolException("unexpected end of stream");
            }
        }

        @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                if (this.bytesRemaining != 0 && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    endOfInput(false);
                }
                this.closed = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class ChunkedSource extends AbstractSource {
        private static final long NO_CHUNK_YET = -1;
        private long bytesRemainingInChunk = NO_CHUNK_YET;
        private boolean hasMoreChunks = true;
        private final HttpUrl url;

        ChunkedSource(HttpUrl url2) {
            super();
            this.url = url2;
        }

        @Override // okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (!this.hasMoreChunks) {
                return NO_CHUNK_YET;
            } else {
                if (this.bytesRemainingInChunk == 0 || this.bytesRemainingInChunk == NO_CHUNK_YET) {
                    readChunkSize();
                    if (!this.hasMoreChunks) {
                        return NO_CHUNK_YET;
                    }
                }
                long read = Http1xStream.this.source.read(sink, Math.min(byteCount, this.bytesRemainingInChunk));
                if (read != NO_CHUNK_YET) {
                    this.bytesRemainingInChunk -= read;
                    return read;
                }
                endOfInput(false);
                throw new ProtocolException("unexpected end of stream");
            }
        }

        private void readChunkSize() throws IOException {
            if (this.bytesRemainingInChunk != NO_CHUNK_YET) {
                Http1xStream.this.source.readUtf8LineStrict();
            }
            try {
                this.bytesRemainingInChunk = Http1xStream.this.source.readHexadecimalUnsignedLong();
                String extensions = Http1xStream.this.source.readUtf8LineStrict().trim();
                if (this.bytesRemainingInChunk < 0 || (!extensions.isEmpty() && !extensions.startsWith(";"))) {
                    throw new ProtocolException("expected chunk size and optional extensions but was \"" + this.bytesRemainingInChunk + extensions + "\"");
                } else if (this.bytesRemainingInChunk == 0) {
                    this.hasMoreChunks = false;
                    HttpHeaders.receiveHeaders(Http1xStream.this.client.cookieJar(), this.url, Http1xStream.this.readHeaders());
                    endOfInput(true);
                }
            } catch (NumberFormatException e) {
                throw new ProtocolException(e.getMessage());
            }
        }

        @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                if (this.hasMoreChunks && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    endOfInput(false);
                }
                this.closed = true;
            }
        }
    }

    /* access modifiers changed from: private */
    public class UnknownLengthSource extends AbstractSource {
        private boolean inputExhausted;

        private UnknownLengthSource() {
            super();
        }

        @Override // okio.Source
        public long read(Buffer sink, long byteCount) throws IOException {
            if (byteCount < 0) {
                throw new IllegalArgumentException("byteCount < 0: " + byteCount);
            } else if (this.closed) {
                throw new IllegalStateException("closed");
            } else if (this.inputExhausted) {
                return -1;
            } else {
                long read = Http1xStream.this.source.read(sink, byteCount);
                if (read != -1) {
                    return read;
                }
                this.inputExhausted = true;
                endOfInput(true);
                return -1;
            }
        }

        @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
        public void close() throws IOException {
            if (!this.closed) {
                if (!this.inputExhausted) {
                    endOfInput(false);
                }
                this.closed = true;
            }
        }
    }
}
