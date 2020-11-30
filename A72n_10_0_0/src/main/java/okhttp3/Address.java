package okhttp3;

import java.net.Proxy;
import java.net.ProxySelector;
import java.util.List;
import javax.net.SocketFactory;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import okhttp3.HttpUrl;
import okhttp3.internal.Util;

public final class Address {
    final CertificatePinner certificatePinner;
    final List<ConnectionSpec> connectionSpecs;
    final Dns dns;
    final HostnameVerifier hostnameVerifier;
    final List<Protocol> protocols;
    final Proxy proxy;
    final Authenticator proxyAuthenticator;
    final ProxySelector proxySelector;
    final SocketFactory socketFactory;
    final SSLSocketFactory sslSocketFactory;
    final HttpUrl url;

    public Address(String uriHost, int uriPort, Dns dns2, SocketFactory socketFactory2, SSLSocketFactory sslSocketFactory2, HostnameVerifier hostnameVerifier2, CertificatePinner certificatePinner2, Authenticator proxyAuthenticator2, Proxy proxy2, List<Protocol> protocols2, List<ConnectionSpec> connectionSpecs2, ProxySelector proxySelector2) {
        this.url = new HttpUrl.Builder().scheme(sslSocketFactory2 != null ? "https" : "http").host(uriHost).port(uriPort).build();
        if (dns2 != null) {
            this.dns = dns2;
            if (socketFactory2 != null) {
                this.socketFactory = socketFactory2;
                if (proxyAuthenticator2 != null) {
                    this.proxyAuthenticator = proxyAuthenticator2;
                    if (protocols2 != null) {
                        this.protocols = Util.immutableList(protocols2);
                        if (connectionSpecs2 != null) {
                            this.connectionSpecs = Util.immutableList(connectionSpecs2);
                            if (proxySelector2 != null) {
                                this.proxySelector = proxySelector2;
                                this.proxy = proxy2;
                                this.sslSocketFactory = sslSocketFactory2;
                                this.hostnameVerifier = hostnameVerifier2;
                                this.certificatePinner = certificatePinner2;
                                return;
                            }
                            throw new NullPointerException("proxySelector == null");
                        }
                        throw new NullPointerException("connectionSpecs == null");
                    }
                    throw new NullPointerException("protocols == null");
                }
                throw new NullPointerException("proxyAuthenticator == null");
            }
            throw new NullPointerException("socketFactory == null");
        }
        throw new NullPointerException("dns == null");
    }

    public HttpUrl url() {
        return this.url;
    }

    public Dns dns() {
        return this.dns;
    }

    public SocketFactory socketFactory() {
        return this.socketFactory;
    }

    public Authenticator proxyAuthenticator() {
        return this.proxyAuthenticator;
    }

    public List<Protocol> protocols() {
        return this.protocols;
    }

    public List<ConnectionSpec> connectionSpecs() {
        return this.connectionSpecs;
    }

    public ProxySelector proxySelector() {
        return this.proxySelector;
    }

    public Proxy proxy() {
        return this.proxy;
    }

    public SSLSocketFactory sslSocketFactory() {
        return this.sslSocketFactory;
    }

    public HostnameVerifier hostnameVerifier() {
        return this.hostnameVerifier;
    }

    public CertificatePinner certificatePinner() {
        return this.certificatePinner;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Address)) {
            return false;
        }
        Address that = (Address) other;
        if (!this.url.equals(that.url) || !this.dns.equals(that.dns) || !this.proxyAuthenticator.equals(that.proxyAuthenticator) || !this.protocols.equals(that.protocols) || !this.connectionSpecs.equals(that.connectionSpecs) || !this.proxySelector.equals(that.proxySelector) || !Util.equal(this.proxy, that.proxy) || !Util.equal(this.sslSocketFactory, that.sslSocketFactory) || !Util.equal(this.hostnameVerifier, that.hostnameVerifier) || !Util.equal(this.certificatePinner, that.certificatePinner)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int i = 0;
        int hashCode = 31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * ((31 * 17) + this.url.hashCode())) + this.dns.hashCode())) + this.proxyAuthenticator.hashCode())) + this.protocols.hashCode())) + this.connectionSpecs.hashCode())) + this.proxySelector.hashCode())) + (this.proxy != null ? this.proxy.hashCode() : 0))) + (this.sslSocketFactory != null ? this.sslSocketFactory.hashCode() : 0))) + (this.hostnameVerifier != null ? this.hostnameVerifier.hashCode() : 0));
        if (this.certificatePinner != null) {
            i = this.certificatePinner.hashCode();
        }
        return hashCode + i;
    }
}
