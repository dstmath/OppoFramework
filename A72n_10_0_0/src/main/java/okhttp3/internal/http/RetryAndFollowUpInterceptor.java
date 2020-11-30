package okhttp3.internal.http;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.HttpRetryException;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.Address;
import okhttp3.CertificatePinner;
import okhttp3.Connection;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.internal.Util;
import okhttp3.internal.connection.RouteException;
import okhttp3.internal.connection.StreamAllocation;

public final class RetryAndFollowUpInterceptor implements Interceptor {
    private static final int MAX_FOLLOW_UPS = 20;
    private volatile boolean canceled;
    private final OkHttpClient client;
    private boolean forWebSocket;
    private StreamAllocation streamAllocation;

    public RetryAndFollowUpInterceptor(OkHttpClient client2) {
        this.client = client2;
    }

    public void cancel() {
        this.canceled = true;
        StreamAllocation streamAllocation2 = this.streamAllocation;
        if (streamAllocation2 != null) {
            streamAllocation2.cancel();
        }
    }

    public boolean isCanceled() {
        return this.canceled;
    }

    public OkHttpClient client() {
        return this.client;
    }

    public void setForWebSocket(boolean forWebSocket2) {
        this.forWebSocket = forWebSocket2;
    }

    public boolean isForWebSocket() {
        return this.forWebSocket;
    }

    public StreamAllocation streamAllocation() {
        return this.streamAllocation;
    }

    @Override // okhttp3.Interceptor
    public Response intercept(Interceptor.Chain chain) throws IOException {
        Request request = chain.request();
        this.streamAllocation = new StreamAllocation(this.client.connectionPool(), createAddress(request.url()));
        int followUpCount = 0;
        Request request2 = request;
        Response priorResponse = null;
        while (!this.canceled) {
            try {
                Response response = ((RealInterceptorChain) chain).proceed(request2, this.streamAllocation, null, null);
                if (0 != 0) {
                    this.streamAllocation.streamFailed(null);
                    this.streamAllocation.release();
                }
                if (priorResponse != null) {
                    response = response.newBuilder().priorResponse(priorResponse.newBuilder().body(null).build()).build();
                }
                Request followUp = followUpRequest(response);
                if (followUp == null) {
                    if (!this.forWebSocket) {
                        this.streamAllocation.release();
                    }
                    return response;
                }
                Util.closeQuietly(response.body());
                followUpCount++;
                if (followUpCount > 20) {
                    this.streamAllocation.release();
                    throw new ProtocolException("Too many follow-up requests: " + followUpCount);
                } else if (!(followUp.body() instanceof UnrepeatableRequestBody)) {
                    if (!sameConnection(response, followUp.url())) {
                        this.streamAllocation.release();
                        this.streamAllocation = new StreamAllocation(this.client.connectionPool(), createAddress(followUp.url()));
                    } else if (this.streamAllocation.stream() != null) {
                        throw new IllegalStateException("Closing the body of " + response + " didn't close its backing stream. Bad interceptor?");
                    }
                    request2 = followUp;
                    priorResponse = response;
                } else {
                    throw new HttpRetryException("Cannot retry streamed HTTP body", response.code());
                }
            } catch (RouteException e) {
                if (!recover(e.getLastConnectException(), true, request2)) {
                    throw e.getLastConnectException();
                } else if (0 != 0) {
                    this.streamAllocation.streamFailed(null);
                    this.streamAllocation.release();
                }
            } catch (IOException e2) {
                if (!recover(e2, false, request2)) {
                    throw e2;
                } else if (0 != 0) {
                    this.streamAllocation.streamFailed(null);
                    this.streamAllocation.release();
                }
            } catch (Throwable th) {
                if (1 != 0) {
                    this.streamAllocation.streamFailed(null);
                    this.streamAllocation.release();
                }
                throw th;
            }
        }
        this.streamAllocation.release();
        throw new IOException("Canceled");
    }

    private Address createAddress(HttpUrl url) {
        SSLSocketFactory sslSocketFactory = null;
        HostnameVerifier hostnameVerifier = null;
        CertificatePinner certificatePinner = null;
        if (url.isHttps()) {
            sslSocketFactory = this.client.sslSocketFactory();
            hostnameVerifier = this.client.hostnameVerifier();
            certificatePinner = this.client.certificatePinner();
        }
        return new Address(url.host(), url.port(), this.client.dns(), this.client.socketFactory(), sslSocketFactory, hostnameVerifier, certificatePinner, this.client.proxyAuthenticator(), this.client.proxy(), this.client.protocols(), this.client.connectionSpecs(), this.client.proxySelector());
    }

    private boolean recover(IOException e, boolean routeException, Request userRequest) {
        this.streamAllocation.streamFailed(e);
        if (!this.client.retryOnConnectionFailure()) {
            return false;
        }
        if ((routeException || !(userRequest.body() instanceof UnrepeatableRequestBody)) && isRecoverable(e, routeException) && this.streamAllocation.hasMoreRoutes()) {
            return true;
        }
        return false;
    }

    private boolean isRecoverable(IOException e, boolean routeException) {
        if (e instanceof ProtocolException) {
            return false;
        }
        if (e instanceof InterruptedIOException) {
            if (!(e instanceof SocketTimeoutException) || !routeException) {
                return false;
            }
            return true;
        } else if ((!(e instanceof SSLHandshakeException) || !(e.getCause() instanceof CertificateException)) && !(e instanceof SSLPeerUnverifiedException)) {
            return true;
        } else {
            return false;
        }
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    private Request followUpRequest(Response userResponse) throws IOException {
        String location;
        HttpUrl url;
        Proxy selectedProxy;
        if (userResponse != null) {
            Connection connection = this.streamAllocation.connection();
            Route route = connection != null ? connection.route() : null;
            int responseCode = userResponse.code();
            String method = userResponse.request().method();
            switch (responseCode) {
                case 300:
                case 301:
                case 302:
                case 303:
                    break;
                case StatusLine.HTTP_TEMP_REDIRECT /* 307 */:
                case StatusLine.HTTP_PERM_REDIRECT /* 308 */:
                    if (!method.equals("GET") && !method.equals("HEAD")) {
                        return null;
                    }
                case 401:
                    return this.client.authenticator().authenticate(route, userResponse);
                case 407:
                    if (route != null) {
                        selectedProxy = route.proxy();
                    } else {
                        selectedProxy = this.client.proxy();
                    }
                    if (selectedProxy.type() == Proxy.Type.HTTP) {
                        return this.client.proxyAuthenticator().authenticate(route, userResponse);
                    }
                    throw new ProtocolException("Received HTTP_PROXY_AUTH (407) code while not using proxy");
                case 408:
                    if (userResponse.request().body() instanceof UnrepeatableRequestBody) {
                        return null;
                    }
                    return userResponse.request();
                default:
                    return null;
            }
            if (!this.client.followRedirects() || (location = userResponse.header("Location")) == null || (url = userResponse.request().url().resolve(location)) == null) {
                return null;
            }
            if (!url.scheme().equals(userResponse.request().url().scheme()) && !this.client.followSslRedirects()) {
                return null;
            }
            Request.Builder requestBuilder = userResponse.request().newBuilder();
            if (HttpMethod.permitsRequestBody(method)) {
                if (HttpMethod.redirectsToGet(method)) {
                    requestBuilder.method("GET", null);
                } else {
                    requestBuilder.method(method, null);
                }
                requestBuilder.removeHeader("Transfer-Encoding");
                requestBuilder.removeHeader("Content-Length");
                requestBuilder.removeHeader("Content-Type");
            }
            if (!sameConnection(userResponse, url)) {
                requestBuilder.removeHeader("Authorization");
            }
            return requestBuilder.url(url).build();
        }
        throw new IllegalStateException();
    }

    private boolean sameConnection(Response response, HttpUrl followUp) {
        HttpUrl url = response.request().url();
        return url.host().equals(followUp.host()) && url.port() == followUp.port() && url.scheme().equals(followUp.scheme());
    }
}
