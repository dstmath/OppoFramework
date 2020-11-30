package okhttp3;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import okhttp3.internal.NamedRunnable;
import okhttp3.internal.cache.CacheInterceptor;
import okhttp3.internal.connection.ConnectInterceptor;
import okhttp3.internal.connection.StreamAllocation;
import okhttp3.internal.http.BridgeInterceptor;
import okhttp3.internal.http.CallServerInterceptor;
import okhttp3.internal.http.RealInterceptorChain;
import okhttp3.internal.http.RetryAndFollowUpInterceptor;
import okhttp3.internal.platform.Platform;

/* access modifiers changed from: package-private */
public final class RealCall implements Call {
    private final OkHttpClient client;
    private boolean executed;
    Request originalRequest;
    private final RetryAndFollowUpInterceptor retryAndFollowUpInterceptor;

    protected RealCall(OkHttpClient client2, Request originalRequest2) {
        this.client = client2;
        this.originalRequest = originalRequest2;
        this.retryAndFollowUpInterceptor = new RetryAndFollowUpInterceptor(client2);
    }

    @Override // okhttp3.Call
    public Request request() {
        return this.originalRequest;
    }

    @Override // okhttp3.Call
    public Response execute() throws IOException {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        try {
            this.client.dispatcher().executed(this);
            Response result = getResponseWithInterceptorChain();
            if (result != null) {
                return result;
            }
            throw new IOException("Canceled");
        } finally {
            this.client.dispatcher().finished(this);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized void setForWebSocket() {
        if (!this.executed) {
            this.retryAndFollowUpInterceptor.setForWebSocket(true);
        } else {
            throw new IllegalStateException("Already Executed");
        }
    }

    @Override // okhttp3.Call
    public void enqueue(Callback responseCallback) {
        synchronized (this) {
            if (!this.executed) {
                this.executed = true;
            } else {
                throw new IllegalStateException("Already Executed");
            }
        }
        this.client.dispatcher().enqueue(new AsyncCall(responseCallback));
    }

    @Override // okhttp3.Call
    public void cancel() {
        this.retryAndFollowUpInterceptor.cancel();
    }

    @Override // okhttp3.Call
    public synchronized boolean isExecuted() {
        return this.executed;
    }

    @Override // okhttp3.Call
    public boolean isCanceled() {
        return this.retryAndFollowUpInterceptor.isCanceled();
    }

    /* access modifiers changed from: package-private */
    public StreamAllocation streamAllocation() {
        return this.retryAndFollowUpInterceptor.streamAllocation();
    }

    /* access modifiers changed from: package-private */
    public final class AsyncCall extends NamedRunnable {
        private final Callback responseCallback;

        private AsyncCall(Callback responseCallback2) {
            super("OkHttp %s", RealCall.this.redactedUrl().toString());
            this.responseCallback = responseCallback2;
        }

        /* access modifiers changed from: package-private */
        public String host() {
            return RealCall.this.originalRequest.url().host();
        }

        /* access modifiers changed from: package-private */
        public Request request() {
            return RealCall.this.originalRequest;
        }

        /* access modifiers changed from: package-private */
        public RealCall get() {
            return RealCall.this;
        }

        /* access modifiers changed from: protected */
        @Override // okhttp3.internal.NamedRunnable
        public void execute() {
            try {
                Response response = RealCall.this.getResponseWithInterceptorChain();
                if (RealCall.this.retryAndFollowUpInterceptor.isCanceled()) {
                    this.responseCallback.onFailure(RealCall.this, new IOException("Canceled"));
                } else {
                    this.responseCallback.onResponse(RealCall.this, response);
                }
            } catch (IOException e) {
                if (0 != 0) {
                    Platform platform = Platform.get();
                    platform.log(4, "Callback failure for " + RealCall.this.toLoggableString(), e);
                } else {
                    this.responseCallback.onFailure(RealCall.this, e);
                }
            } catch (Throwable th) {
                RealCall.this.client.dispatcher().finished(this);
                throw th;
            }
            RealCall.this.client.dispatcher().finished(this);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private String toLoggableString() {
        String string = this.retryAndFollowUpInterceptor.isCanceled() ? "canceled call" : "call";
        return string + " to " + redactedUrl();
    }

    /* access modifiers changed from: package-private */
    public HttpUrl redactedUrl() {
        return this.originalRequest.url().resolve("/...");
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private Response getResponseWithInterceptorChain() throws IOException {
        List<Interceptor> interceptors = new ArrayList<>();
        interceptors.addAll(this.client.interceptors());
        interceptors.add(this.retryAndFollowUpInterceptor);
        interceptors.add(new BridgeInterceptor(this.client.cookieJar()));
        interceptors.add(new CacheInterceptor(this.client.internalCache()));
        interceptors.add(new ConnectInterceptor(this.client));
        if (!this.retryAndFollowUpInterceptor.isForWebSocket()) {
            interceptors.addAll(this.client.networkInterceptors());
        }
        interceptors.add(new CallServerInterceptor(this.retryAndFollowUpInterceptor.isForWebSocket()));
        return new RealInterceptorChain(interceptors, null, null, null, 0, this.originalRequest).proceed(this.originalRequest);
    }
}
