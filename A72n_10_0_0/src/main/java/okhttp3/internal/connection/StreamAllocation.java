package okhttp3.internal.connection;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;
import okhttp3.Address;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.Route;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.framed.ErrorCode;
import okhttp3.internal.framed.StreamResetException;
import okhttp3.internal.http.Http1xStream;
import okhttp3.internal.http.Http2xStream;
import okhttp3.internal.http.HttpStream;

public final class StreamAllocation {
    public final Address address;
    private boolean canceled;
    private RealConnection connection;
    private final ConnectionPool connectionPool;
    private int refusedStreamCount;
    private boolean released;
    private Route route;
    private final RouteSelector routeSelector;
    private HttpStream stream;

    public StreamAllocation(ConnectionPool connectionPool2, Address address2) {
        this.connectionPool = connectionPool2;
        this.address = address2;
        this.routeSelector = new RouteSelector(address2, routeDatabase());
    }

    public HttpStream newStream(OkHttpClient client, boolean doExtensiveHealthChecks) {
        HttpStream resultStream;
        int connectTimeout = client.connectTimeoutMillis();
        int readTimeout = client.readTimeoutMillis();
        int writeTimeout = client.writeTimeoutMillis();
        try {
            RealConnection resultConnection = findHealthyConnection(connectTimeout, readTimeout, writeTimeout, client.retryOnConnectionFailure(), doExtensiveHealthChecks);
            if (resultConnection.framedConnection != null) {
                resultStream = new Http2xStream(client, this, resultConnection.framedConnection);
            } else {
                resultConnection.socket().setSoTimeout(readTimeout);
                resultConnection.source.timeout().timeout((long) readTimeout, TimeUnit.MILLISECONDS);
                resultConnection.sink.timeout().timeout((long) writeTimeout, TimeUnit.MILLISECONDS);
                resultStream = new Http1xStream(client, this, resultConnection.source, resultConnection.sink);
            }
            synchronized (this.connectionPool) {
                this.stream = resultStream;
            }
            return resultStream;
        } catch (IOException e) {
            throw new RouteException(e);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0018, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x0012, code lost:
        if (r0.isHealthy(r8) != false) goto L_0x0018;
     */
    private RealConnection findHealthyConnection(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled, boolean doExtensiveHealthChecks) throws IOException {
        while (true) {
            RealConnection candidate = findConnection(connectTimeout, readTimeout, writeTimeout, connectionRetryEnabled);
            synchronized (this.connectionPool) {
                if (candidate.successCount == 0) {
                    return candidate;
                }
            }
            noNewStreams();
        }
    }

    /* JADX INFO: Multiple debug info for r1v8 okhttp3.Route: [D('allocatedConnection' okhttp3.internal.connection.RealConnection), D('selectedRoute' okhttp3.Route)] */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x002d, code lost:
        if (r1 != null) goto L_0x0043;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x002f, code lost:
        r0 = r9.routeSelector.next();
        r2 = r9.connectionPool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0037, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:?, code lost:
        r9.route = r0;
        r9.refusedStreamCount = 0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003d, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x003e, code lost:
        r1 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x0043, code lost:
        r0 = new okhttp3.internal.connection.RealConnection(r1);
        acquire(r0);
        r2 = r9.connectionPool;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x004e, code lost:
        monitor-enter(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
        okhttp3.internal.Internal.instance.put(r9.connectionPool, r0);
        r9.connection = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x005a, code lost:
        if (r9.canceled != false) goto L_0x0077;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x005c, code lost:
        monitor-exit(r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x005d, code lost:
        r0.connect(r10, r11, r12, r9.address.connectionSpecs(), r13);
        routeDatabase().connected(r0.route());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0076, code lost:
        return r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x007e, code lost:
        throw new java.io.IOException("Canceled");
     */
    private RealConnection findConnection(int connectTimeout, int readTimeout, int writeTimeout, boolean connectionRetryEnabled) throws IOException {
        synchronized (this.connectionPool) {
            if (this.released) {
                throw new IllegalStateException("released");
            } else if (this.stream != null) {
                throw new IllegalStateException("stream != null");
            } else if (!this.canceled) {
                RealConnection allocatedConnection = this.connection;
                if (allocatedConnection != null && !allocatedConnection.noNewStreams) {
                    return allocatedConnection;
                }
                RealConnection pooledConnection = Internal.instance.get(this.connectionPool, this.address, this);
                if (pooledConnection != null) {
                    this.connection = pooledConnection;
                    return pooledConnection;
                }
                Route selectedRoute = this.route;
            } else {
                throw new IOException("Canceled");
            }
        }
    }

    public void streamFinished(boolean noNewStreams, HttpStream stream2) {
        synchronized (this.connectionPool) {
            if (stream2 != null) {
                if (stream2 == this.stream) {
                    if (!noNewStreams) {
                        this.connection.successCount++;
                    }
                }
            }
            throw new IllegalStateException("expected " + this.stream + " but was " + stream2);
        }
        deallocate(noNewStreams, false, true);
    }

    public HttpStream stream() {
        HttpStream httpStream;
        synchronized (this.connectionPool) {
            httpStream = this.stream;
        }
        return httpStream;
    }

    private RouteDatabase routeDatabase() {
        return Internal.instance.routeDatabase(this.connectionPool);
    }

    public synchronized RealConnection connection() {
        return this.connection;
    }

    public void release() {
        deallocate(false, true, false);
    }

    public void noNewStreams() {
        deallocate(true, false, false);
    }

    private void deallocate(boolean noNewStreams, boolean released2, boolean streamFinished) {
        RealConnection connectionToClose = null;
        synchronized (this.connectionPool) {
            if (streamFinished) {
                try {
                    this.stream = null;
                } catch (Throwable th) {
                    throw th;
                }
            }
            if (released2) {
                this.released = true;
            }
            if (this.connection != null) {
                if (noNewStreams) {
                    this.connection.noNewStreams = true;
                }
                if (this.stream == null && (this.released || this.connection.noNewStreams)) {
                    release(this.connection);
                    if (this.connection.allocations.isEmpty()) {
                        this.connection.idleAtNanos = System.nanoTime();
                        if (Internal.instance.connectionBecameIdle(this.connectionPool, this.connection)) {
                            connectionToClose = this.connection;
                        }
                    }
                    this.connection = null;
                }
            }
        }
        if (connectionToClose != null) {
            Util.closeQuietly(connectionToClose.socket());
        }
    }

    public void cancel() {
        HttpStream streamToCancel;
        RealConnection connectionToCancel;
        synchronized (this.connectionPool) {
            this.canceled = true;
            streamToCancel = this.stream;
            connectionToCancel = this.connection;
        }
        if (streamToCancel != null) {
            streamToCancel.cancel();
        } else if (connectionToCancel != null) {
            connectionToCancel.cancel();
        }
    }

    public void streamFailed(IOException e) {
        boolean noNewStreams = false;
        synchronized (this.connectionPool) {
            if (e instanceof StreamResetException) {
                StreamResetException streamResetException = (StreamResetException) e;
                if (streamResetException.errorCode == ErrorCode.REFUSED_STREAM) {
                    this.refusedStreamCount++;
                }
                if (streamResetException.errorCode != ErrorCode.REFUSED_STREAM || this.refusedStreamCount > 1) {
                    noNewStreams = true;
                    this.route = null;
                }
            } else if (this.connection != null && !this.connection.isMultiplexed()) {
                noNewStreams = true;
                if (this.connection.successCount == 0) {
                    if (!(this.route == null || e == null)) {
                        this.routeSelector.connectFailed(this.route, e);
                    }
                    this.route = null;
                }
            }
        }
        deallocate(noNewStreams, false, true);
    }

    public void acquire(RealConnection connection2) {
        connection2.allocations.add(new WeakReference(this));
    }

    private void release(RealConnection connection2) {
        int size = connection2.allocations.size();
        for (int i = 0; i < size; i++) {
            if (connection2.allocations.get(i).get() == this) {
                connection2.allocations.remove(i);
                return;
            }
        }
        throw new IllegalStateException();
    }

    public boolean hasMoreRoutes() {
        return this.route != null || this.routeSelector.hasNext();
    }

    public String toString() {
        return this.address.toString();
    }
}
