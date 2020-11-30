package okio;

import java.io.IOException;

public abstract class ForwardingSource implements Source {
    private final Source delegate;

    public ForwardingSource(Source delegate2) {
        if (delegate2 != null) {
            this.delegate = delegate2;
            return;
        }
        throw new IllegalArgumentException("delegate == null");
    }

    public final Source delegate() {
        return this.delegate;
    }

    @Override // okio.Source
    public long read(Buffer sink, long byteCount) throws IOException {
        return this.delegate.read(sink, byteCount);
    }

    @Override // okio.Source
    public Timeout timeout() {
        return this.delegate.timeout();
    }

    @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
    public void close() throws IOException {
        this.delegate.close();
    }

    public String toString() {
        return getClass().getSimpleName() + "(" + this.delegate.toString() + ")";
    }
}
