package okio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

public final class Okio {
    static final Logger logger = Logger.getLogger(Okio.class.getName());

    private Okio() {
    }

    public static BufferedSource buffer(Source source) {
        return new RealBufferedSource(source);
    }

    public static BufferedSink buffer(Sink sink) {
        return new RealBufferedSink(sink);
    }

    public static Sink sink(OutputStream out) {
        return sink(out, new Timeout());
    }

    private static Sink sink(final OutputStream out, final Timeout timeout) {
        if (out == null) {
            throw new IllegalArgumentException("out == null");
        } else if (timeout != null) {
            return new Sink() {
                /* class okio.Okio.AnonymousClass1 */

                @Override // okio.Sink
                public void write(Buffer source, long byteCount) throws IOException {
                    Util.checkOffsetAndCount(source.size, 0, byteCount);
                    while (byteCount > 0) {
                        timeout.throwIfReached();
                        Segment head = source.head;
                        int toCopy = (int) Math.min(byteCount, (long) (head.limit - head.pos));
                        out.write(head.data, head.pos, toCopy);
                        head.pos += toCopy;
                        byteCount -= (long) toCopy;
                        source.size -= (long) toCopy;
                        if (head.pos == head.limit) {
                            source.head = head.pop();
                            SegmentPool.recycle(head);
                        }
                    }
                }

                @Override // okio.Sink, java.io.Flushable
                public void flush() throws IOException {
                    out.flush();
                }

                @Override // java.io.Closeable, java.lang.AutoCloseable, okio.Sink
                public void close() throws IOException {
                    out.close();
                }

                @Override // okio.Sink
                public Timeout timeout() {
                    return timeout;
                }

                public String toString() {
                    return "sink(" + out + ")";
                }
            };
        } else {
            throw new IllegalArgumentException("timeout == null");
        }
    }

    public static Sink sink(Socket socket) throws IOException {
        if (socket != null) {
            AsyncTimeout timeout = timeout(socket);
            return timeout.sink(sink(socket.getOutputStream(), timeout));
        }
        throw new IllegalArgumentException("socket == null");
    }

    public static Source source(InputStream in) {
        return source(in, new Timeout());
    }

    private static Source source(final InputStream in, final Timeout timeout) {
        if (in == null) {
            throw new IllegalArgumentException("in == null");
        } else if (timeout != null) {
            return new Source() {
                /* class okio.Okio.AnonymousClass2 */

                @Override // okio.Source
                public long read(Buffer sink, long byteCount) throws IOException {
                    if (byteCount < 0) {
                        throw new IllegalArgumentException("byteCount < 0: " + byteCount);
                    } else if (byteCount == 0) {
                        return 0;
                    } else {
                        try {
                            timeout.throwIfReached();
                            Segment tail = sink.writableSegment(1);
                            int bytesRead = in.read(tail.data, tail.limit, (int) Math.min(byteCount, (long) (8192 - tail.limit)));
                            if (bytesRead == -1) {
                                return -1;
                            }
                            tail.limit += bytesRead;
                            sink.size += (long) bytesRead;
                            return (long) bytesRead;
                        } catch (AssertionError e) {
                            if (Okio.isAndroidGetsocknameError(e)) {
                                throw new IOException(e);
                            }
                            throw e;
                        }
                    }
                }

                @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
                public void close() throws IOException {
                    in.close();
                }

                @Override // okio.Source
                public Timeout timeout() {
                    return timeout;
                }

                public String toString() {
                    return "source(" + in + ")";
                }
            };
        } else {
            throw new IllegalArgumentException("timeout == null");
        }
    }

    public static Source source(File file) throws FileNotFoundException {
        if (file != null) {
            return source(new FileInputStream(file));
        }
        throw new IllegalArgumentException("file == null");
    }

    @IgnoreJRERequirement
    public static Source source(Path path, OpenOption... options) throws IOException {
        if (path != null) {
            return source(Files.newInputStream(path, options));
        }
        throw new IllegalArgumentException("path == null");
    }

    public static Sink sink(File file) throws FileNotFoundException {
        if (file != null) {
            return sink(new FileOutputStream(file));
        }
        throw new IllegalArgumentException("file == null");
    }

    public static Sink appendingSink(File file) throws FileNotFoundException {
        if (file != null) {
            return sink(new FileOutputStream(file, true));
        }
        throw new IllegalArgumentException("file == null");
    }

    @IgnoreJRERequirement
    public static Sink sink(Path path, OpenOption... options) throws IOException {
        if (path != null) {
            return sink(Files.newOutputStream(path, options));
        }
        throw new IllegalArgumentException("path == null");
    }

    public static Source source(Socket socket) throws IOException {
        if (socket != null) {
            AsyncTimeout timeout = timeout(socket);
            return timeout.source(source(socket.getInputStream(), timeout));
        }
        throw new IllegalArgumentException("socket == null");
    }

    private static AsyncTimeout timeout(final Socket socket) {
        return new AsyncTimeout() {
            /* class okio.Okio.AnonymousClass3 */

            /* access modifiers changed from: protected */
            @Override // okio.AsyncTimeout
            public IOException newTimeoutException(IOException cause) {
                InterruptedIOException ioe = new SocketTimeoutException("timeout");
                if (cause != null) {
                    ioe.initCause(cause);
                }
                return ioe;
            }

            /* access modifiers changed from: protected */
            @Override // okio.AsyncTimeout
            public void timedOut() {
                try {
                    socket.close();
                } catch (Exception e) {
                    Logger logger = Okio.logger;
                    Level level = Level.WARNING;
                    logger.log(level, "Failed to close timed out socket " + socket, (Throwable) e);
                } catch (AssertionError e2) {
                    if (Okio.isAndroidGetsocknameError(e2)) {
                        Logger logger2 = Okio.logger;
                        Level level2 = Level.WARNING;
                        logger2.log(level2, "Failed to close timed out socket " + socket, (Throwable) e2);
                        return;
                    }
                    throw e2;
                }
            }
        };
    }

    static boolean isAndroidGetsocknameError(AssertionError e) {
        return (e.getCause() == null || e.getMessage() == null || !e.getMessage().contains("getsockname failed")) ? false : true;
    }
}
