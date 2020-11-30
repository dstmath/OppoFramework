package okhttp3.internal.framed;

import java.io.Closeable;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Protocol;
import okhttp3.internal.NamedRunnable;
import okhttp3.internal.Util;
import okhttp3.internal.framed.FrameReader;
import okhttp3.internal.platform.Platform;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.ByteString;
import okio.Okio;

public final class FramedConnection implements Closeable {
    static final /* synthetic */ boolean $assertionsDisabled = false;
    private static final int OKHTTP_CLIENT_WINDOW_SIZE = 16777216;
    private static final ExecutorService executor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60, TimeUnit.SECONDS, new SynchronousQueue(), Util.threadFactory("OkHttp FramedConnection", true));
    long bytesLeftInWriteWindow;
    final boolean client;
    private final Set<Integer> currentPushRequests;
    final FrameWriter frameWriter;
    private final String hostname;
    private int lastGoodStreamId;
    private final Listener listener;
    private int nextPingId;
    private int nextStreamId;
    Settings okHttpSettings;
    final Settings peerSettings;
    private Map<Integer, Ping> pings;
    final Protocol protocol;
    private final ExecutorService pushExecutor;
    private final PushObserver pushObserver;
    final Reader readerRunnable;
    private boolean receivedInitialPeerSettings;
    private boolean shutdown;
    final Socket socket;
    private final Map<Integer, FramedStream> streams;
    long unacknowledgedBytesRead;
    final Variant variant;

    private FramedConnection(Builder builder) {
        this.streams = new HashMap();
        this.unacknowledgedBytesRead = 0;
        this.okHttpSettings = new Settings();
        this.peerSettings = new Settings();
        this.receivedInitialPeerSettings = false;
        this.currentPushRequests = new LinkedHashSet();
        this.protocol = builder.protocol;
        this.pushObserver = builder.pushObserver;
        this.client = builder.client;
        this.listener = builder.listener;
        int i = 2;
        this.nextStreamId = builder.client ? 1 : 2;
        if (builder.client && this.protocol == Protocol.HTTP_2) {
            this.nextStreamId += 2;
        }
        this.nextPingId = builder.client ? 1 : i;
        if (builder.client) {
            this.okHttpSettings.set(7, 0, OKHTTP_CLIENT_WINDOW_SIZE);
        }
        this.hostname = builder.hostname;
        if (this.protocol == Protocol.HTTP_2) {
            this.variant = new Http2();
            this.pushExecutor = new ThreadPoolExecutor(0, 1, 60, TimeUnit.SECONDS, new LinkedBlockingQueue(), Util.threadFactory(Util.format("OkHttp %s Push Observer", this.hostname), true));
            this.peerSettings.set(7, 0, 65535);
            this.peerSettings.set(5, 0, 16384);
        } else if (this.protocol == Protocol.SPDY_3) {
            this.variant = new Spdy3();
            this.pushExecutor = null;
        } else {
            throw new AssertionError(this.protocol);
        }
        this.bytesLeftInWriteWindow = (long) this.peerSettings.getInitialWindowSize(65536);
        this.socket = builder.socket;
        this.frameWriter = this.variant.newWriter(builder.sink, this.client);
        this.readerRunnable = new Reader(this.variant.newReader(builder.source, this.client));
    }

    public Protocol getProtocol() {
        return this.protocol;
    }

    public synchronized int openStreamCount() {
        return this.streams.size();
    }

    /* access modifiers changed from: package-private */
    public synchronized FramedStream getStream(int id) {
        return this.streams.get(Integer.valueOf(id));
    }

    /* access modifiers changed from: package-private */
    public synchronized FramedStream removeStream(int streamId) {
        FramedStream stream;
        stream = this.streams.remove(Integer.valueOf(streamId));
        notifyAll();
        return stream;
    }

    public synchronized int maxConcurrentStreams() {
        return this.peerSettings.getMaxConcurrentStreams(Integer.MAX_VALUE);
    }

    public FramedStream pushStream(int associatedStreamId, List<Header> requestHeaders, boolean out) throws IOException {
        if (this.client) {
            throw new IllegalStateException("Client cannot push requests.");
        } else if (this.protocol == Protocol.HTTP_2) {
            return newStream(associatedStreamId, requestHeaders, out, false);
        } else {
            throw new IllegalStateException("protocol != HTTP_2");
        }
    }

    public FramedStream newStream(List<Header> requestHeaders, boolean out, boolean in) throws IOException {
        return newStream(0, requestHeaders, out, in);
    }

    /* JADX DEBUG: Failed to insert an additional move for type inference into block B:21:0x0049 */
    /* JADX WARN: Multi-variable type inference failed */
    /* JADX WARN: Type inference failed for: r3v14, types: [java.util.Map, java.util.Map<java.lang.Integer, okhttp3.internal.framed.FramedStream>] */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0047, code lost:
        if (r17 != 0) goto L_0x005d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x004b, code lost:
        r3 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:?, code lost:
        r16.frameWriter.synStream(r4, r5, r0, r17, r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0058, code lost:
        r0 = th;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0059, code lost:
        r3 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005a, code lost:
        r3 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x005d, code lost:
        r3 = r13;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0060, code lost:
        if (r16.client != false) goto L_0x0072;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:?, code lost:
        r16.frameWriter.pushPromise(r17, r0, r18);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0069, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x006a, code lost:
        if (r2 == false) goto L_0x0071;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x006c, code lost:
        r16.frameWriter.flush();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x0071, code lost:
        return r8;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x007b, code lost:
        throw new java.lang.IllegalArgumentException("client streams shouldn't have associated stream IDs");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x007c, code lost:
        r0 = th;
        r3 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x0097, code lost:
        r0 = th;
        r3 = r3;
     */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003d A[Catch:{ all -> 0x0089, all -> 0x008f }] */
    /* JADX WARNING: Unknown variable types count: 1 */
    private FramedStream newStream(int associatedStreamId, List<Header> requestHeaders, boolean out, boolean in) throws IOException {
        boolean flushHeaders;
        boolean isOpen;
        boolean outFinished = !out;
        boolean inFinished = !in;
        FrameWriter frameWriter2 = this.frameWriter;
        synchronized (frameWriter2) {
            try {
                synchronized (this) {
                    try {
                        if (!this.shutdown) {
                            int streamId = this.nextStreamId;
                            this.nextStreamId += 2;
                            FramedStream stream = new FramedStream(streamId, this, outFinished, inFinished, requestHeaders);
                            if (out && this.bytesLeftInWriteWindow != 0) {
                                if (stream.bytesLeftInWriteWindow != 0) {
                                    flushHeaders = false;
                                    isOpen = stream.isOpen();
                                    FrameWriter frameWriter3 = isOpen;
                                    if (isOpen) {
                                        ?? r3 = this.streams;
                                        r3.put(Integer.valueOf(streamId), stream);
                                        frameWriter3 = r3;
                                    }
                                }
                            }
                            flushHeaders = true;
                            isOpen = stream.isOpen();
                            FrameWriter frameWriter32 = isOpen;
                            if (isOpen) {
                            }
                        } else {
                            throw new IOException("shutdown");
                        }
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                }
            } catch (Throwable th2) {
                Throwable th3 = th2;
                FrameWriter frameWriter4 = frameWriter2;
                throw th3;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void writeSynReply(int streamId, boolean outFinished, List<Header> alternating) throws IOException {
        this.frameWriter.synReply(outFinished, streamId, alternating);
    }

    public void writeData(int streamId, boolean outFinished, Buffer buffer, long byteCount) throws IOException {
        int toWrite;
        if (byteCount == 0) {
            this.frameWriter.data(outFinished, streamId, buffer, 0);
            return;
        }
        while (byteCount > 0) {
            synchronized (this) {
                while (this.bytesLeftInWriteWindow <= 0) {
                    try {
                        if (this.streams.containsKey(Integer.valueOf(streamId))) {
                            wait();
                        } else {
                            throw new IOException("stream closed");
                        }
                    } catch (InterruptedException e) {
                        throw new InterruptedIOException();
                    }
                }
                toWrite = Math.min((int) Math.min(byteCount, this.bytesLeftInWriteWindow), this.frameWriter.maxDataLength());
                this.bytesLeftInWriteWindow -= (long) toWrite;
            }
            byteCount -= (long) toWrite;
            this.frameWriter.data(outFinished && byteCount == 0, streamId, buffer, toWrite);
        }
    }

    /* access modifiers changed from: package-private */
    public void addBytesToWriteWindow(long delta) {
        this.bytesLeftInWriteWindow += delta;
        if (delta > 0) {
            notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void writeSynResetLater(final int streamId, final ErrorCode errorCode) {
        executor.submit(new NamedRunnable("OkHttp %s stream %d", new Object[]{this.hostname, Integer.valueOf(streamId)}) {
            /* class okhttp3.internal.framed.FramedConnection.AnonymousClass1 */

            @Override // okhttp3.internal.NamedRunnable
            public void execute() {
                try {
                    FramedConnection.this.writeSynReset(streamId, errorCode);
                } catch (IOException e) {
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void writeSynReset(int streamId, ErrorCode statusCode) throws IOException {
        this.frameWriter.rstStream(streamId, statusCode);
    }

    /* access modifiers changed from: package-private */
    public void writeWindowUpdateLater(final int streamId, final long unacknowledgedBytesRead2) {
        executor.execute(new NamedRunnable("OkHttp Window Update %s stream %d", new Object[]{this.hostname, Integer.valueOf(streamId)}) {
            /* class okhttp3.internal.framed.FramedConnection.AnonymousClass2 */

            @Override // okhttp3.internal.NamedRunnable
            public void execute() {
                try {
                    FramedConnection.this.frameWriter.windowUpdate(streamId, unacknowledgedBytesRead2);
                } catch (IOException e) {
                }
            }
        });
    }

    public Ping ping() throws IOException {
        int pingId;
        Ping ping = new Ping();
        synchronized (this) {
            if (!this.shutdown) {
                pingId = this.nextPingId;
                this.nextPingId += 2;
                if (this.pings == null) {
                    this.pings = new HashMap();
                }
                this.pings.put(Integer.valueOf(pingId), ping);
            } else {
                throw new IOException("shutdown");
            }
        }
        writePing(false, pingId, 1330343787, ping);
        return ping;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writePingLater(final boolean reply, final int payload1, final int payload2, final Ping ping) {
        executor.execute(new NamedRunnable("OkHttp %s ping %08x%08x", new Object[]{this.hostname, Integer.valueOf(payload1), Integer.valueOf(payload2)}) {
            /* class okhttp3.internal.framed.FramedConnection.AnonymousClass3 */

            @Override // okhttp3.internal.NamedRunnable
            public void execute() {
                try {
                    FramedConnection.this.writePing(reply, payload1, payload2, ping);
                } catch (IOException e) {
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void writePing(boolean reply, int payload1, int payload2, Ping ping) throws IOException {
        synchronized (this.frameWriter) {
            if (ping != null) {
                try {
                    ping.send();
                } catch (Throwable th) {
                    throw th;
                }
            }
            this.frameWriter.ping(reply, payload1, payload2);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized Ping removePing(int id) {
        return this.pings != null ? this.pings.remove(Integer.valueOf(id)) : null;
    }

    public void flush() throws IOException {
        this.frameWriter.flush();
    }

    public void shutdown(ErrorCode statusCode) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (!this.shutdown) {
                    this.shutdown = true;
                    this.frameWriter.goAway(this.lastGoodStreamId, statusCode, Util.EMPTY_BYTE_ARRAY);
                }
            }
        }
    }

    @Override // java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void close(ErrorCode connectionCode, ErrorCode streamCode) throws IOException {
        IOException thrown = null;
        try {
            shutdown(connectionCode);
        } catch (IOException e) {
            thrown = e;
        }
        FramedStream[] streamsToClose = null;
        Ping[] pingsToCancel = null;
        synchronized (this) {
            if (!this.streams.isEmpty()) {
                streamsToClose = (FramedStream[]) this.streams.values().toArray(new FramedStream[this.streams.size()]);
                this.streams.clear();
            }
            if (this.pings != null) {
                pingsToCancel = (Ping[]) this.pings.values().toArray(new Ping[this.pings.size()]);
                this.pings = null;
            }
        }
        if (streamsToClose != null) {
            IOException thrown2 = thrown;
            for (FramedStream stream : streamsToClose) {
                try {
                    stream.close(streamCode);
                } catch (IOException e2) {
                    if (thrown2 != null) {
                        thrown2 = e2;
                    }
                }
            }
            thrown = thrown2;
        }
        if (pingsToCancel != null) {
            for (Ping ping : pingsToCancel) {
                ping.cancel();
            }
        }
        try {
            this.frameWriter.close();
        } catch (IOException e3) {
            if (thrown == null) {
                thrown = e3;
            }
        }
        try {
            this.socket.close();
        } catch (IOException e4) {
            thrown = e4;
        }
        if (thrown != null) {
            throw thrown;
        }
    }

    public void start() throws IOException {
        start(true);
    }

    /* access modifiers changed from: package-private */
    public void start(boolean sendConnectionPreface) throws IOException {
        if (sendConnectionPreface) {
            this.frameWriter.connectionPreface();
            this.frameWriter.settings(this.okHttpSettings);
            int windowSize = this.okHttpSettings.getInitialWindowSize(65536);
            if (windowSize != 65536) {
                this.frameWriter.windowUpdate(0, (long) (windowSize - 65536));
            }
        }
        new Thread(this.readerRunnable).start();
    }

    public void setSettings(Settings settings) throws IOException {
        synchronized (this.frameWriter) {
            synchronized (this) {
                if (!this.shutdown) {
                    this.okHttpSettings.merge(settings);
                    this.frameWriter.settings(settings);
                } else {
                    throw new IOException("shutdown");
                }
            }
        }
    }

    public static class Builder {
        private boolean client;
        private String hostname;
        private Listener listener = Listener.REFUSE_INCOMING_STREAMS;
        private Protocol protocol = Protocol.SPDY_3;
        private PushObserver pushObserver = PushObserver.CANCEL;
        private BufferedSink sink;
        private Socket socket;
        private BufferedSource source;

        public Builder(boolean client2) {
            this.client = client2;
        }

        public Builder socket(Socket socket2) throws IOException {
            return socket(socket2, ((InetSocketAddress) socket2.getRemoteSocketAddress()).getHostName(), Okio.buffer(Okio.source(socket2)), Okio.buffer(Okio.sink(socket2)));
        }

        public Builder socket(Socket socket2, String hostname2, BufferedSource source2, BufferedSink sink2) {
            this.socket = socket2;
            this.hostname = hostname2;
            this.source = source2;
            this.sink = sink2;
            return this;
        }

        public Builder listener(Listener listener2) {
            this.listener = listener2;
            return this;
        }

        public Builder protocol(Protocol protocol2) {
            this.protocol = protocol2;
            return this;
        }

        public Builder pushObserver(PushObserver pushObserver2) {
            this.pushObserver = pushObserver2;
            return this;
        }

        public FramedConnection build() throws IOException {
            return new FramedConnection(this);
        }
    }

    /* access modifiers changed from: package-private */
    public class Reader extends NamedRunnable implements FrameReader.Handler {
        final FrameReader frameReader;

        private Reader(FrameReader frameReader2) {
            super("OkHttp %s", FramedConnection.this.hostname);
            this.frameReader = frameReader2;
        }

        /* access modifiers changed from: protected */
        @Override // okhttp3.internal.NamedRunnable
        public void execute() {
            ErrorCode connectionErrorCode = ErrorCode.INTERNAL_ERROR;
            ErrorCode streamErrorCode = ErrorCode.INTERNAL_ERROR;
            try {
                if (!FramedConnection.this.client) {
                    this.frameReader.readConnectionPreface();
                }
                while (this.frameReader.nextFrame(this)) {
                }
                try {
                    FramedConnection.this.close(ErrorCode.NO_ERROR, ErrorCode.CANCEL);
                } catch (IOException e) {
                }
            } catch (IOException e2) {
                connectionErrorCode = ErrorCode.PROTOCOL_ERROR;
                try {
                    FramedConnection.this.close(connectionErrorCode, ErrorCode.PROTOCOL_ERROR);
                } catch (IOException e3) {
                }
            } catch (Throwable th) {
                try {
                    FramedConnection.this.close(connectionErrorCode, streamErrorCode);
                } catch (IOException e4) {
                }
                Util.closeQuietly(this.frameReader);
                throw th;
            }
            Util.closeQuietly(this.frameReader);
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void data(boolean inFinished, int streamId, BufferedSource source, int length) throws IOException {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushDataLater(streamId, source, length, inFinished);
                return;
            }
            FramedStream dataStream = FramedConnection.this.getStream(streamId);
            if (dataStream == null) {
                FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                source.skip((long) length);
                return;
            }
            dataStream.receiveData(source, length);
            if (inFinished) {
                dataStream.receiveFin();
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:31:0x0097, code lost:
            if (r20.failIfStreamPresent() == false) goto L_0x00a4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:32:0x0099, code lost:
            r0.closeLater(okhttp3.internal.framed.ErrorCode.PROTOCOL_ERROR);
            r14.this$0.removeStream(r17);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:33:0x00a3, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a4, code lost:
            r0.receiveHeaders(r19, r20);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:35:0x00a9, code lost:
            if (r16 == false) goto L_?;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:36:0x00ab, code lost:
            r0.receiveFin();
         */
        /* JADX WARNING: Code restructure failed: missing block: B:42:0x00b4, code lost:
            r0 = th;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:45:?, code lost:
            return;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:46:?, code lost:
            return;
         */
        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void headers(boolean outFinished, boolean inFinished, int streamId, int associatedStreamId, List<Header> headerBlock, HeadersMode headersMode) {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushHeadersLater(streamId, headerBlock, inFinished);
                return;
            }
            synchronized (FramedConnection.this) {
                if (!FramedConnection.this.shutdown) {
                    FramedStream stream = FramedConnection.this.getStream(streamId);
                    if (stream == null) {
                        if (headersMode.failIfStreamAbsent()) {
                            FramedConnection.this.writeSynResetLater(streamId, ErrorCode.INVALID_STREAM);
                            return;
                        } else if (streamId > FramedConnection.this.lastGoodStreamId) {
                            if (streamId % 2 != FramedConnection.this.nextStreamId % 2) {
                                final FramedStream newStream = new FramedStream(streamId, FramedConnection.this, outFinished, inFinished, headerBlock);
                                FramedConnection.this.lastGoodStreamId = streamId;
                                FramedConnection.this.streams.put(Integer.valueOf(streamId), newStream);
                                FramedConnection.executor.execute(new NamedRunnable("OkHttp %s stream %d", new Object[]{FramedConnection.this.hostname, Integer.valueOf(streamId)}) {
                                    /* class okhttp3.internal.framed.FramedConnection.Reader.AnonymousClass1 */

                                    @Override // okhttp3.internal.NamedRunnable
                                    public void execute() {
                                        try {
                                            FramedConnection.this.listener.onStream(newStream);
                                        } catch (IOException e) {
                                            Platform platform = Platform.get();
                                            platform.log(4, "FramedConnection.Listener failure for " + FramedConnection.this.hostname, e);
                                            try {
                                                newStream.close(ErrorCode.PROTOCOL_ERROR);
                                            } catch (IOException e2) {
                                            }
                                        }
                                    }
                                });
                                return;
                            }
                            return;
                        } else {
                            return;
                        }
                    }
                } else {
                    return;
                }
            }
            while (true) {
            }
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void rstStream(int streamId, ErrorCode errorCode) {
            if (FramedConnection.this.pushedStream(streamId)) {
                FramedConnection.this.pushResetLater(streamId, errorCode);
                return;
            }
            FramedStream rstStream = FramedConnection.this.removeStream(streamId);
            if (rstStream != null) {
                rstStream.receiveRstStream(errorCode);
            }
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void settings(boolean clearPrevious, Settings newSettings) {
            int i;
            long delta = 0;
            FramedStream[] streamsToNotify = null;
            synchronized (FramedConnection.this) {
                int priorWriteWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (clearPrevious) {
                    FramedConnection.this.peerSettings.clear();
                }
                FramedConnection.this.peerSettings.merge(newSettings);
                if (FramedConnection.this.getProtocol() == Protocol.HTTP_2) {
                    applyAndAckSettings(newSettings);
                }
                int peerInitialWindowSize = FramedConnection.this.peerSettings.getInitialWindowSize(65536);
                if (!(peerInitialWindowSize == -1 || peerInitialWindowSize == priorWriteWindowSize)) {
                    delta = (long) (peerInitialWindowSize - priorWriteWindowSize);
                    if (!FramedConnection.this.receivedInitialPeerSettings) {
                        FramedConnection.this.addBytesToWriteWindow(delta);
                        FramedConnection.this.receivedInitialPeerSettings = true;
                    }
                    if (!FramedConnection.this.streams.isEmpty()) {
                        streamsToNotify = (FramedStream[]) FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                    }
                }
                FramedConnection.executor.execute(new NamedRunnable("OkHttp %s settings", FramedConnection.this.hostname) {
                    /* class okhttp3.internal.framed.FramedConnection.Reader.AnonymousClass2 */

                    @Override // okhttp3.internal.NamedRunnable
                    public void execute() {
                        FramedConnection.this.listener.onSettings(FramedConnection.this);
                    }
                });
            }
            if (!(streamsToNotify == null || delta == 0)) {
                for (FramedStream stream : streamsToNotify) {
                    synchronized (stream) {
                        stream.addBytesToWriteWindow(delta);
                    }
                }
            }
        }

        private void applyAndAckSettings(final Settings peerSettings) {
            FramedConnection.executor.execute(new NamedRunnable("OkHttp %s ACK Settings", new Object[]{FramedConnection.this.hostname}) {
                /* class okhttp3.internal.framed.FramedConnection.Reader.AnonymousClass3 */

                @Override // okhttp3.internal.NamedRunnable
                public void execute() {
                    try {
                        FramedConnection.this.frameWriter.applyAndAckSettings(peerSettings);
                    } catch (IOException e) {
                    }
                }
            });
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void ackSettings() {
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void ping(boolean reply, int payload1, int payload2) {
            if (reply) {
                Ping ping = FramedConnection.this.removePing(payload1);
                if (ping != null) {
                    ping.receive();
                    return;
                }
                return;
            }
            FramedConnection.this.writePingLater(true, payload1, payload2, null);
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void goAway(int lastGoodStreamId, ErrorCode errorCode, ByteString debugData) {
            FramedStream[] streamsCopy;
            debugData.size();
            synchronized (FramedConnection.this) {
                streamsCopy = (FramedStream[]) FramedConnection.this.streams.values().toArray(new FramedStream[FramedConnection.this.streams.size()]);
                FramedConnection.this.shutdown = true;
            }
            for (FramedStream framedStream : streamsCopy) {
                if (framedStream.getId() > lastGoodStreamId && framedStream.isLocallyInitiated()) {
                    framedStream.receiveRstStream(ErrorCode.REFUSED_STREAM);
                    FramedConnection.this.removeStream(framedStream.getId());
                }
            }
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void windowUpdate(int streamId, long windowSizeIncrement) {
            if (streamId == 0) {
                synchronized (FramedConnection.this) {
                    FramedConnection.this.bytesLeftInWriteWindow += windowSizeIncrement;
                    FramedConnection.this.notifyAll();
                }
                return;
            }
            FramedStream stream = FramedConnection.this.getStream(streamId);
            if (stream != null) {
                synchronized (stream) {
                    stream.addBytesToWriteWindow(windowSizeIncrement);
                }
            }
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void priority(int streamId, int streamDependency, int weight, boolean exclusive) {
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void pushPromise(int streamId, int promisedStreamId, List<Header> requestHeaders) {
            FramedConnection.this.pushRequestLater(promisedStreamId, requestHeaders);
        }

        @Override // okhttp3.internal.framed.FrameReader.Handler
        public void alternateService(int streamId, String origin, ByteString protocol, String host, int port, long maxAge) {
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private boolean pushedStream(int streamId) {
        return this.protocol == Protocol.HTTP_2 && streamId != 0 && (streamId & 1) == 0;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushRequestLater(final int streamId, final List<Header> requestHeaders) {
        synchronized (this) {
            if (this.currentPushRequests.contains(Integer.valueOf(streamId))) {
                writeSynResetLater(streamId, ErrorCode.PROTOCOL_ERROR);
                return;
            }
            this.currentPushRequests.add(Integer.valueOf(streamId));
            this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Request[%s]", new Object[]{this.hostname, Integer.valueOf(streamId)}) {
                /* class okhttp3.internal.framed.FramedConnection.AnonymousClass4 */

                @Override // okhttp3.internal.NamedRunnable
                public void execute() {
                    if (FramedConnection.this.pushObserver.onRequest(streamId, requestHeaders)) {
                        try {
                            FramedConnection.this.frameWriter.rstStream(streamId, ErrorCode.CANCEL);
                            synchronized (FramedConnection.this) {
                                FramedConnection.this.currentPushRequests.remove(Integer.valueOf(streamId));
                            }
                        } catch (IOException e) {
                        }
                    }
                }
            });
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushHeadersLater(final int streamId, final List<Header> requestHeaders, final boolean inFinished) {
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Headers[%s]", new Object[]{this.hostname, Integer.valueOf(streamId)}) {
            /* class okhttp3.internal.framed.FramedConnection.AnonymousClass5 */

            @Override // okhttp3.internal.NamedRunnable
            public void execute() {
                boolean cancel = FramedConnection.this.pushObserver.onHeaders(streamId, requestHeaders, inFinished);
                if (cancel) {
                    try {
                        FramedConnection.this.frameWriter.rstStream(streamId, ErrorCode.CANCEL);
                    } catch (IOException e) {
                        return;
                    }
                }
                if (cancel || inFinished) {
                    synchronized (FramedConnection.this) {
                        FramedConnection.this.currentPushRequests.remove(Integer.valueOf(streamId));
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushDataLater(final int streamId, BufferedSource source, final int byteCount, final boolean inFinished) throws IOException {
        final Buffer buffer = new Buffer();
        source.require((long) byteCount);
        source.read(buffer, (long) byteCount);
        if (buffer.size() == ((long) byteCount)) {
            this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Data[%s]", new Object[]{this.hostname, Integer.valueOf(streamId)}) {
                /* class okhttp3.internal.framed.FramedConnection.AnonymousClass6 */

                @Override // okhttp3.internal.NamedRunnable
                public void execute() {
                    try {
                        boolean cancel = FramedConnection.this.pushObserver.onData(streamId, buffer, byteCount, inFinished);
                        if (cancel) {
                            FramedConnection.this.frameWriter.rstStream(streamId, ErrorCode.CANCEL);
                        }
                        if (cancel || inFinished) {
                            synchronized (FramedConnection.this) {
                                FramedConnection.this.currentPushRequests.remove(Integer.valueOf(streamId));
                            }
                        }
                    } catch (IOException e) {
                    }
                }
            });
            return;
        }
        throw new IOException(buffer.size() + " != " + byteCount);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void pushResetLater(final int streamId, final ErrorCode errorCode) {
        this.pushExecutor.execute(new NamedRunnable("OkHttp %s Push Reset[%s]", new Object[]{this.hostname, Integer.valueOf(streamId)}) {
            /* class okhttp3.internal.framed.FramedConnection.AnonymousClass7 */

            @Override // okhttp3.internal.NamedRunnable
            public void execute() {
                FramedConnection.this.pushObserver.onReset(streamId, errorCode);
                synchronized (FramedConnection.this) {
                    FramedConnection.this.currentPushRequests.remove(Integer.valueOf(streamId));
                }
            }
        });
    }

    public static abstract class Listener {
        public static final Listener REFUSE_INCOMING_STREAMS = new Listener() {
            /* class okhttp3.internal.framed.FramedConnection.Listener.AnonymousClass1 */

            @Override // okhttp3.internal.framed.FramedConnection.Listener
            public void onStream(FramedStream stream) throws IOException {
                stream.close(ErrorCode.REFUSED_STREAM);
            }
        };

        public abstract void onStream(FramedStream framedStream) throws IOException;

        public void onSettings(FramedConnection connection) {
        }
    }
}
