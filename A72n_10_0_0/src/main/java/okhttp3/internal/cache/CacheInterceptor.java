package okhttp3.internal.cache;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.Internal;
import okhttp3.internal.Util;
import okhttp3.internal.cache.CacheStrategy;
import okhttp3.internal.http.HttpHeaders;
import okhttp3.internal.http.HttpMethod;
import okhttp3.internal.http.RealResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Sink;
import okio.Source;
import okio.Timeout;

public final class CacheInterceptor implements Interceptor {
    private static final ResponseBody EMPTY_BODY = new ResponseBody() {
        /* class okhttp3.internal.cache.CacheInterceptor.AnonymousClass1 */

        @Override // okhttp3.ResponseBody
        public MediaType contentType() {
            return null;
        }

        @Override // okhttp3.ResponseBody
        public long contentLength() {
            return 0;
        }

        @Override // okhttp3.ResponseBody
        public BufferedSource source() {
            return new Buffer();
        }
    };
    final InternalCache cache;

    public CacheInterceptor(InternalCache cache2) {
        this.cache = cache2;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Response networkResponse = null;
        Response cacheCandidate = this.cache != null ? this.cache.get(chain.request()) : networkResponse;
        CacheStrategy strategy = new CacheStrategy.Factory(System.currentTimeMillis(), chain.request(), cacheCandidate).get();
        Request networkRequest = strategy.networkRequest;
        Response cacheResponse = strategy.cacheResponse;
        if (this.cache != null) {
            this.cache.trackResponse(strategy);
        }
        if (cacheCandidate != null && cacheResponse == null) {
            Util.closeQuietly(cacheCandidate.body());
        }
        if (networkRequest == null && cacheResponse == null) {
            return new Response.Builder().request(chain.request()).protocol(Protocol.HTTP_1_1).code(504).message("Unsatisfiable Request (only-if-cached)").body(EMPTY_BODY).sentRequestAtMillis(-1).receivedResponseAtMillis(System.currentTimeMillis()).build();
        }
        if (networkRequest == null) {
            return cacheResponse.newBuilder().cacheResponse(stripBody(cacheResponse)).build();
        }
        try {
            networkResponse = chain.proceed(networkRequest);
            if (cacheResponse != null) {
                if (validate(cacheResponse, networkResponse)) {
                    Response response = cacheResponse.newBuilder().headers(combine(cacheResponse.headers(), networkResponse.headers())).cacheResponse(stripBody(cacheResponse)).networkResponse(stripBody(networkResponse)).build();
                    networkResponse.body().close();
                    this.cache.trackConditionalCacheHit();
                    this.cache.update(cacheResponse, response);
                    return response;
                }
                Util.closeQuietly(cacheResponse.body());
            }
            Response response2 = networkResponse.newBuilder().cacheResponse(stripBody(cacheResponse)).networkResponse(stripBody(networkResponse)).build();
            if (HttpHeaders.hasBody(response2)) {
                return cacheWritingResponse(maybeCache(response2, networkResponse.request(), this.cache), response2);
            }
            return response2;
        } finally {
            if (networkResponse == null && cacheCandidate != null) {
                Util.closeQuietly(cacheCandidate.body());
            }
        }
    }

    private static Response stripBody(Response response) {
        if (response == null || response.body() == null) {
            return response;
        }
        return response.newBuilder().body(null).build();
    }

    private CacheRequest maybeCache(Response userResponse, Request networkRequest, InternalCache responseCache) throws IOException {
        if (responseCache == null) {
            return null;
        }
        if (CacheStrategy.isCacheable(userResponse, networkRequest)) {
            return responseCache.put(userResponse);
        }
        if (HttpMethod.invalidatesCache(networkRequest.method())) {
            try {
                responseCache.remove(networkRequest);
            } catch (IOException e) {
            }
        }
        return null;
    }

    private Response cacheWritingResponse(final CacheRequest cacheRequest, Response response) throws IOException {
        Sink cacheBodyUnbuffered;
        if (cacheRequest == null || (cacheBodyUnbuffered = cacheRequest.body()) == null) {
            return response;
        }
        final BufferedSource source = response.body().source();
        final BufferedSink cacheBody = Okio.buffer(cacheBodyUnbuffered);
        return response.newBuilder().body(new RealResponseBody(response.headers(), Okio.buffer(new Source() {
            /* class okhttp3.internal.cache.CacheInterceptor.AnonymousClass2 */
            boolean cacheRequestClosed;

            @Override // okio.Source
            public long read(Buffer sink, long byteCount) throws IOException {
                try {
                    long bytesRead = source.read(sink, byteCount);
                    if (bytesRead == -1) {
                        if (!this.cacheRequestClosed) {
                            this.cacheRequestClosed = true;
                            cacheBody.close();
                        }
                        return -1;
                    }
                    sink.copyTo(cacheBody.buffer(), sink.size() - bytesRead, bytesRead);
                    cacheBody.emitCompleteSegments();
                    return bytesRead;
                } catch (IOException e) {
                    if (!this.cacheRequestClosed) {
                        this.cacheRequestClosed = true;
                        cacheRequest.abort();
                    }
                    throw e;
                }
            }

            @Override // okio.Source
            public Timeout timeout() {
                return source.timeout();
            }

            @Override // okio.Source, java.io.Closeable, java.lang.AutoCloseable
            public void close() throws IOException {
                if (!this.cacheRequestClosed && !Util.discard(this, 100, TimeUnit.MILLISECONDS)) {
                    this.cacheRequestClosed = true;
                    cacheRequest.abort();
                }
                source.close();
            }
        }))).build();
    }

    private static boolean validate(Response cached, Response network) {
        Date networkLastModified;
        if (network.code() == 304) {
            return true;
        }
        Date lastModified = cached.headers().getDate("Last-Modified");
        if (lastModified == null || (networkLastModified = network.headers().getDate("Last-Modified")) == null || networkLastModified.getTime() >= lastModified.getTime()) {
            return false;
        }
        return true;
    }

    private static Headers combine(Headers cachedHeaders, Headers networkHeaders) {
        Headers.Builder result = new Headers.Builder();
        int size = cachedHeaders.size();
        for (int i = 0; i < size; i++) {
            String fieldName = cachedHeaders.name(i);
            String value = cachedHeaders.value(i);
            if ((!"Warning".equalsIgnoreCase(fieldName) || !value.startsWith("1")) && (!isEndToEnd(fieldName) || networkHeaders.get(fieldName) == null)) {
                Internal.instance.addLenient(result, fieldName, value);
            }
        }
        int size2 = networkHeaders.size();
        for (int i2 = 0; i2 < size2; i2++) {
            String fieldName2 = networkHeaders.name(i2);
            if (!"Content-Length".equalsIgnoreCase(fieldName2) && isEndToEnd(fieldName2)) {
                Internal.instance.addLenient(result, fieldName2, networkHeaders.value(i2));
            }
        }
        return result.build();
    }

    static boolean isEndToEnd(String fieldName) {
        return !"Connection".equalsIgnoreCase(fieldName) && !"Keep-Alive".equalsIgnoreCase(fieldName) && !"Proxy-Authenticate".equalsIgnoreCase(fieldName) && !"Proxy-Authorization".equalsIgnoreCase(fieldName) && !"TE".equalsIgnoreCase(fieldName) && !"Trailers".equalsIgnoreCase(fieldName) && !"Transfer-Encoding".equalsIgnoreCase(fieldName) && !"Upgrade".equalsIgnoreCase(fieldName);
    }
}
