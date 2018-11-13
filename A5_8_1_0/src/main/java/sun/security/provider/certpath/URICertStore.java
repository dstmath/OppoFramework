package sun.security.provider.certpath;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.cert.CRLSelector;
import java.security.cert.CertSelector;
import java.security.cert.CertStore;
import java.security.cert.CertStoreException;
import java.security.cert.CertStoreParameters;
import java.security.cert.CertStoreSpi;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509CRL;
import java.security.cert.X509CertSelector;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import sun.security.action.GetIntegerAction;
import sun.security.util.Cache;
import sun.security.util.Debug;
import sun.security.x509.AccessDescription;
import sun.security.x509.GeneralNameInterface;
import sun.security.x509.URIName;

class URICertStore extends CertStoreSpi {
    private static final int CACHE_SIZE = 185;
    private static final int CHECK_INTERVAL = 30000;
    private static final int CRL_CONNECT_TIMEOUT = initializeTimeout();
    private static final int DEFAULT_CRL_CONNECT_TIMEOUT = 15000;
    private static final Cache<URICertStoreParameters, CertStore> certStoreCache = Cache.newSoftMemoryCache(CACHE_SIZE);
    private static final Debug debug = Debug.getInstance("certpath");
    private Collection<X509Certificate> certs = Collections.emptySet();
    private X509CRL crl;
    private final CertificateFactory factory;
    private long lastChecked;
    private long lastModified;
    private boolean ldap = false;
    private CertStore ldapCertStore;
    private CertStoreHelper ldapHelper;
    private String ldapPath;
    private URI uri;

    private static class UCS extends CertStore {
        protected UCS(CertStoreSpi spi, Provider p, String type, CertStoreParameters params) {
            super(spi, p, type, params);
        }
    }

    static class URICertStoreParameters implements CertStoreParameters {
        private volatile int hashCode = 0;
        private final URI uri;

        URICertStoreParameters(URI uri) {
            this.uri = uri;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof URICertStoreParameters)) {
                return false;
            }
            return this.uri.equals(((URICertStoreParameters) obj).uri);
        }

        public int hashCode() {
            if (this.hashCode == 0) {
                this.hashCode = this.uri.hashCode() + 629;
            }
            return this.hashCode;
        }

        public Object clone() {
            try {
                return super.clone();
            } catch (CloneNotSupportedException e) {
                throw new InternalError(e.toString(), e);
            }
        }
    }

    private static int initializeTimeout() {
        Integer tmp = (Integer) AccessController.doPrivileged(new GetIntegerAction("com.sun.security.crl.timeout"));
        if (tmp == null || tmp.lambda$-java_util_stream_IntPipeline_14709() < 0) {
            return DEFAULT_CRL_CONNECT_TIMEOUT;
        }
        return tmp.lambda$-java_util_stream_IntPipeline_14709() * 1000;
    }

    URICertStore(CertStoreParameters params) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
        super(params);
        if (params instanceof URICertStoreParameters) {
            this.uri = ((URICertStoreParameters) params).uri;
            if (this.uri.getScheme().toLowerCase(Locale.ENGLISH).equals("ldap")) {
                this.ldap = true;
                this.ldapHelper = CertStoreHelper.getInstance("LDAP");
                this.ldapCertStore = this.ldapHelper.getCertStore(this.uri);
                this.ldapPath = this.uri.getPath();
                if (this.ldapPath.charAt(0) == '/') {
                    this.ldapPath = this.ldapPath.substring(1);
                }
            }
            try {
                this.factory = CertificateFactory.getInstance("X.509");
                return;
            } catch (CertificateException e) {
                throw new RuntimeException();
            }
        }
        throw new InvalidAlgorithmParameterException("params must be instanceof URICertStoreParameters");
    }

    static synchronized CertStore getInstance(URICertStoreParameters params) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        CertStore ucs;
        synchronized (URICertStore.class) {
            if (debug != null) {
                debug.println("CertStore URI:" + params.uri);
            }
            ucs = (CertStore) certStoreCache.get(params);
            if (ucs == null) {
                ucs = new UCS(new URICertStore(params), null, "URI", params);
                certStoreCache.put(params, ucs);
            } else if (debug != null) {
                debug.println("URICertStore.getInstance: cache hit");
            }
        }
        return ucs;
    }

    static CertStore getInstance(AccessDescription ad) {
        if (!ad.getAccessMethod().equals(AccessDescription.Ad_CAISSUERS_Id)) {
            return null;
        }
        GeneralNameInterface gn = ad.getAccessLocation().getName();
        if (!(gn instanceof URIName)) {
            return null;
        }
        try {
            return getInstance(new URICertStoreParameters(((URIName) gn).getURI()));
        } catch (Object ex) {
            if (debug != null) {
                debug.println("exception creating CertStore: " + ex);
                ex.printStackTrace();
            }
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:50:0x00b7 A:{ExcHandler: java.io.IOException (r3_0 'e' java.lang.Exception), Splitter: B:29:0x005c} */
    /* JADX WARNING: Missing block: B:50:0x00b7, code:
            r3 = move-exception;
     */
    /* JADX WARNING: Missing block: B:53:0x00ba, code:
            if (debug != null) goto L_0x00bc;
     */
    /* JADX WARNING: Missing block: B:54:0x00bc, code:
            debug.println("Exception fetching certificates:");
            r3.printStackTrace();
     */
    /* JADX WARNING: Missing block: B:55:0x00c7, code:
            r17.lastModified = 0;
            r17.certs = java.util.Collections.emptySet();
     */
    /* JADX WARNING: Missing block: B:57:0x00da, code:
            return r17.certs;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized Collection<X509Certificate> engineGetCertificates(CertSelector selector) throws CertStoreException {
        Throwable th;
        InputStream inputStream;
        Throwable th2;
        if (this.ldap) {
            X509CertSelector xsel = (X509CertSelector) selector;
            try {
                return this.ldapCertStore.getCertificates(this.ldapHelper.wrap(xsel, xsel.getSubject(), this.ldapPath));
            } catch (Throwable ioe) {
                throw new CertStoreException(ioe);
            }
        }
        long time = System.currentTimeMillis();
        if (time - this.lastChecked < 30000) {
            if (debug != null) {
                debug.println("Returning certificates from cache");
            }
            return getMatchingCerts(this.certs, selector);
        }
        this.lastChecked = time;
        try {
            URLConnection connection = this.uri.toURL().openConnection();
            if (this.lastModified != 0) {
                connection.setIfModifiedSince(this.lastModified);
            }
            long oldLastModified = this.lastModified;
            th = null;
            inputStream = null;
            try {
                inputStream = connection.getInputStream();
                this.lastModified = connection.getLastModified();
                if (oldLastModified != 0) {
                    Collection<X509Certificate> matchingCerts;
                    if (oldLastModified == this.lastModified) {
                        if (debug != null) {
                            debug.println("Not modified, using cached copy");
                        }
                        matchingCerts = getMatchingCerts(this.certs, selector);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th3) {
                                th = th3;
                            }
                        }
                        if (th == null) {
                            return matchingCerts;
                        }
                        throw th;
                    } else if ((connection instanceof HttpURLConnection) && ((HttpURLConnection) connection).getResponseCode() == HttpURLConnection.HTTP_NOT_MODIFIED) {
                        if (debug != null) {
                            debug.println("Not modified, using cached copy");
                        }
                        matchingCerts = getMatchingCerts(this.certs, selector);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (Throwable th4) {
                                th = th4;
                            }
                        }
                        if (th == null) {
                            return matchingCerts;
                        }
                        throw th;
                    }
                }
                if (debug != null) {
                    debug.println("Downloading new certificates...");
                }
                this.certs = this.factory.generateCertificates(inputStream);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Throwable th5) {
                        th = th5;
                    }
                }
                if (th != null) {
                    throw th;
                } else {
                    return getMatchingCerts(this.certs, selector);
                }
            } catch (Throwable th6) {
                Throwable th7 = th6;
                th6 = th2;
                th2 = th7;
            }
        } catch (Exception e) {
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (Throwable th8) {
                if (th6 == null) {
                    th6 = th8;
                } else if (th6 != th8) {
                    th6.addSuppressed(th8);
                }
            }
        }
        if (th6 != null) {
            throw th6;
        }
        throw th2;
    }

    private static Collection<X509Certificate> getMatchingCerts(Collection<X509Certificate> certs, CertSelector selector) {
        if (selector == null) {
            return certs;
        }
        List<X509Certificate> matchedCerts = new ArrayList(certs.size());
        for (X509Certificate cert : certs) {
            if (selector.match(cert)) {
                matchedCerts.-java_util_stream_SpinedBuffer-mthref-0(cert);
            }
        }
        return matchedCerts;
    }

    /*  JADX ERROR: JadxRuntimeException in pass: RegionMakerVisitor
        jadx.core.utils.exceptions.JadxRuntimeException: Exception block dominator not found, method:sun.security.provider.certpath.URICertStore.engineGetCRLs(java.security.cert.CRLSelector):java.util.Collection<java.security.cert.X509CRL>, dom blocks: [B:5:0x000f, B:7:0x0029, B:32:0x0085]
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.searchTryCatchDominators(ProcessTryCatchRegions.java:89)
        	at jadx.core.dex.visitors.regions.ProcessTryCatchRegions.process(ProcessTryCatchRegions.java:45)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.postProcessRegions(RegionMakerVisitor.java:63)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:58)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:27)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$1(DepthTraversal.java:14)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0152 A:{ExcHandler: java.io.IOException (r6_1 'e' java.lang.Throwable), Splitter: B:32:0x0085} */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0152 A:{ExcHandler: java.io.IOException (r6_1 'e' java.lang.Throwable), Splitter: B:32:0x0085} */
    public synchronized java.util.Collection<java.security.cert.X509CRL> engineGetCRLs(java.security.cert.CRLSelector r24) throws java.security.cert.CertStoreException {
        /*
        r23 = this;
        monitor-enter(r23);
        r0 = r23;	 Catch:{ all -> 0x0042 }
        r0 = r0.ldap;	 Catch:{ all -> 0x0042 }
        r17 = r0;	 Catch:{ all -> 0x0042 }
        if (r17 == 0) goto L_0x0053;	 Catch:{ all -> 0x0042 }
    L_0x0009:
        r0 = r24;	 Catch:{ all -> 0x0042 }
        r0 = (java.security.cert.X509CRLSelector) r0;	 Catch:{ all -> 0x0042 }
        r16 = r0;	 Catch:{ all -> 0x0042 }
        r0 = r23;	 Catch:{ IOException -> 0x0039 }
        r0 = r0.ldapHelper;	 Catch:{ IOException -> 0x0039 }
        r17 = r0;	 Catch:{ IOException -> 0x0039 }
        r0 = r23;	 Catch:{ IOException -> 0x0039 }
        r0 = r0.ldapPath;	 Catch:{ IOException -> 0x0039 }
        r18 = r0;	 Catch:{ IOException -> 0x0039 }
        r19 = 0;	 Catch:{ IOException -> 0x0039 }
        r0 = r17;	 Catch:{ IOException -> 0x0039 }
        r1 = r16;	 Catch:{ IOException -> 0x0039 }
        r2 = r19;	 Catch:{ IOException -> 0x0039 }
        r3 = r18;	 Catch:{ IOException -> 0x0039 }
        r16 = r0.wrap(r1, r2, r3);	 Catch:{ IOException -> 0x0039 }
        r0 = r23;	 Catch:{ CertStoreException -> 0x0045 }
        r0 = r0.ldapCertStore;	 Catch:{ CertStoreException -> 0x0045 }
        r17 = r0;	 Catch:{ CertStoreException -> 0x0045 }
        r0 = r17;	 Catch:{ CertStoreException -> 0x0045 }
        r1 = r16;	 Catch:{ CertStoreException -> 0x0045 }
        r17 = r0.getCRLs(r1);	 Catch:{ CertStoreException -> 0x0045 }
        monitor-exit(r23);
        return r17;
    L_0x0039:
        r9 = move-exception;
        r17 = new java.security.cert.CertStoreException;	 Catch:{ all -> 0x0042 }
        r0 = r17;	 Catch:{ all -> 0x0042 }
        r0.<init>(r9);	 Catch:{ all -> 0x0042 }
        throw r17;	 Catch:{ all -> 0x0042 }
    L_0x0042:
        r17 = move-exception;
        monitor-exit(r23);
        throw r17;
    L_0x0045:
        r5 = move-exception;
        r17 = new sun.security.provider.certpath.PKIX$CertStoreTypeException;	 Catch:{ all -> 0x0042 }
        r18 = "LDAP";	 Catch:{ all -> 0x0042 }
        r0 = r17;	 Catch:{ all -> 0x0042 }
        r1 = r18;	 Catch:{ all -> 0x0042 }
        r0.<init>(r1, r5);	 Catch:{ all -> 0x0042 }
        throw r17;	 Catch:{ all -> 0x0042 }
    L_0x0053:
        r14 = java.lang.System.currentTimeMillis();	 Catch:{ all -> 0x0042 }
        r0 = r23;	 Catch:{ all -> 0x0042 }
        r0 = r0.lastChecked;	 Catch:{ all -> 0x0042 }
        r18 = r0;	 Catch:{ all -> 0x0042 }
        r18 = r14 - r18;	 Catch:{ all -> 0x0042 }
        r20 = 30000; // 0x7530 float:4.2039E-41 double:1.4822E-319;	 Catch:{ all -> 0x0042 }
        r17 = (r18 > r20 ? 1 : (r18 == r20 ? 0 : -1));	 Catch:{ all -> 0x0042 }
        if (r17 >= 0) goto L_0x0081;	 Catch:{ all -> 0x0042 }
    L_0x0065:
        r17 = debug;	 Catch:{ all -> 0x0042 }
        if (r17 == 0) goto L_0x0071;	 Catch:{ all -> 0x0042 }
    L_0x0069:
        r17 = debug;	 Catch:{ all -> 0x0042 }
        r18 = "Returning CRL from cache";	 Catch:{ all -> 0x0042 }
        r17.println(r18);	 Catch:{ all -> 0x0042 }
    L_0x0071:
        r0 = r23;	 Catch:{ all -> 0x0042 }
        r0 = r0.crl;	 Catch:{ all -> 0x0042 }
        r17 = r0;	 Catch:{ all -> 0x0042 }
        r0 = r17;	 Catch:{ all -> 0x0042 }
        r1 = r24;	 Catch:{ all -> 0x0042 }
        r17 = getMatchingCRLs(r0, r1);	 Catch:{ all -> 0x0042 }
        monitor-exit(r23);
        return r17;
    L_0x0081:
        r0 = r23;	 Catch:{ all -> 0x0042 }
        r0.lastChecked = r14;	 Catch:{ all -> 0x0042 }
        r0 = r23;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r0.uri;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = r0;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = r17.toURL();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r4 = r17.openConnection();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r23;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r0.lastModified;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r0;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r20 = 0;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = (r18 > r20 ? 1 : (r18 == r20 ? 0 : -1));	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        if (r17 == 0) goto L_0x00aa;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x009f:
        r0 = r23;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r0.lastModified;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r0;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r4.setIfModifiedSince(r0);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x00aa:
        r0 = r23;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r10 = r0.lastModified;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = CRL_CONNECT_TIMEOUT;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r17;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r4.setConnectTimeout(r0);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = "crl.read.timeout";	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r13 = java.security.Security.getProperty(r17);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = java.lang.System.out;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18.<init>();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r19 = "crl.read.timeout:";	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r18.append(r19);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r0.append(r13);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r18.-java_util_stream_Collectors-mthref-7();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17.println(r18);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        if (r13 == 0) goto L_0x010b;
    L_0x00d9:
        r12 = 0;
        r12 = java.lang.Integer.parseInt(r13);	 Catch:{ Exception -> 0x0182 }
    L_0x00de:
        r17 = java.lang.System.out;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18.<init>();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r19 = "Configure readTimeout:";	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r18.append(r19);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r0.append(r12);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r19 = " seconds";	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r18.append(r19);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r18.-java_util_stream_Collectors-mthref-7();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17.println(r18);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        if (r12 == 0) goto L_0x010b;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0102:
        r0 = r12 * 1000;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = r0;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r17;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r4.setReadTimeout(r0);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x010b:
        r18 = 0;
        r8 = 0;
        r8 = r4.getInputStream();	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r20 = r4.getLastModified();	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r20;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r2 = r23;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r2.lastModified = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r20 = 0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = (r10 > r20 ? 1 : (r10 == r20 ? 0 : -1));	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r17 == 0) goto L_0x01e4;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x0122:
        r0 = r23;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r0.lastModified;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r20 = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = (r10 > r20 ? 1 : (r10 == r20 ? 0 : -1));	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r17 != 0) goto L_0x01a4;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x012c:
        r17 = debug;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r17 == 0) goto L_0x013c;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x0130:
        r17 = debug;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r19 = "Not modified, using cached copy";	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1 = r19;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0.println(r1);	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x013c:
        r0 = r23;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r0.crl;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1 = r24;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = getMatchingCRLs(r0, r1);	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r8 == 0) goto L_0x014f;
    L_0x014c:
        r8.close();	 Catch:{ Throwable -> 0x01a0 }
    L_0x014f:
        if (r18 == 0) goto L_0x01a2;
    L_0x0151:
        throw r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0152:
        r6 = move-exception;
        r17 = debug;	 Catch:{ all -> 0x0042 }
        if (r17 == 0) goto L_0x0162;	 Catch:{ all -> 0x0042 }
    L_0x0157:
        r17 = debug;	 Catch:{ all -> 0x0042 }
        r18 = "Exception fetching CRL:";	 Catch:{ all -> 0x0042 }
        r17.println(r18);	 Catch:{ all -> 0x0042 }
        r6.printStackTrace();	 Catch:{ all -> 0x0042 }
    L_0x0162:
        r18 = 0;	 Catch:{ all -> 0x0042 }
        r0 = r18;	 Catch:{ all -> 0x0042 }
        r2 = r23;	 Catch:{ all -> 0x0042 }
        r2.lastModified = r0;	 Catch:{ all -> 0x0042 }
        r17 = 0;	 Catch:{ all -> 0x0042 }
        r0 = r17;	 Catch:{ all -> 0x0042 }
        r1 = r23;	 Catch:{ all -> 0x0042 }
        r1.crl = r0;	 Catch:{ all -> 0x0042 }
        r17 = new sun.security.provider.certpath.PKIX$CertStoreTypeException;	 Catch:{ all -> 0x0042 }
        r18 = "URI";	 Catch:{ all -> 0x0042 }
        r19 = new java.security.cert.CertStoreException;	 Catch:{ all -> 0x0042 }
        r0 = r19;	 Catch:{ all -> 0x0042 }
        r0.<init>(r6);	 Catch:{ all -> 0x0042 }
        r17.<init>(r18, r19);	 Catch:{ all -> 0x0042 }
        throw r17;	 Catch:{ all -> 0x0042 }
    L_0x0182:
        r6 = move-exception;
        r17 = java.lang.System.out;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18.<init>();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r19 = "read time parse error:";	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r18.append(r19);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r0.append(r6);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r18 = r18.-java_util_stream_Collectors-mthref-7();	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17.println(r18);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        goto L_0x00de;
    L_0x01a0:
        r18 = move-exception;
        goto L_0x014f;
    L_0x01a2:
        monitor-exit(r23);
        return r17;
    L_0x01a4:
        r0 = r4 instanceof java.net.HttpURLConnection;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r17 == 0) goto L_0x01e4;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x01aa:
        r0 = r4;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = (java.net.HttpURLConnection) r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r7 = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = r7.getResponseCode();	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r19 = 304; // 0x130 float:4.26E-43 double:1.5E-321;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1 = r19;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r0 != r1) goto L_0x01e4;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x01ba:
        r17 = debug;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r17 == 0) goto L_0x01ca;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x01be:
        r17 = debug;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r19 = "Not modified, using cached copy";	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1 = r19;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0.println(r1);	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x01ca:
        r0 = r23;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r0.crl;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1 = r24;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = getMatchingCRLs(r0, r1);	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r8 == 0) goto L_0x01dd;
    L_0x01da:
        r8.close();	 Catch:{ Throwable -> 0x01e0 }
    L_0x01dd:
        if (r18 == 0) goto L_0x01e2;
    L_0x01df:
        throw r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x01e0:
        r18 = move-exception;
        goto L_0x01dd;
    L_0x01e2:
        monitor-exit(r23);
        return r17;
    L_0x01e4:
        r17 = debug;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r17 == 0) goto L_0x01f4;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x01e8:
        r17 = debug;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r19 = "Downloading new CRL...";	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1 = r19;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0.println(r1);	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
    L_0x01f4:
        r0 = r23;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r0.factory;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = r0.generateCRL(r8);	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r17 = (java.security.cert.X509CRL) r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r0 = r17;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1 = r23;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        r1.crl = r0;	 Catch:{ Throwable -> 0x0212, all -> 0x0244 }
        if (r8 == 0) goto L_0x020d;
    L_0x020a:
        r8.close();	 Catch:{ Throwable -> 0x0210 }
    L_0x020d:
        if (r18 == 0) goto L_0x0234;
    L_0x020f:
        throw r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0210:
        r18 = move-exception;
        goto L_0x020d;
    L_0x0212:
        r17 = move-exception;
        throw r17;	 Catch:{ all -> 0x0214 }
    L_0x0214:
        r18 = move-exception;
        r22 = r18;
        r18 = r17;
        r17 = r22;
    L_0x021b:
        if (r8 == 0) goto L_0x0220;
    L_0x021d:
        r8.close();	 Catch:{ Throwable -> 0x0223 }
    L_0x0220:
        if (r18 == 0) goto L_0x0233;
    L_0x0222:
        throw r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0223:
        r19 = move-exception;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        if (r18 != 0) goto L_0x0229;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0226:
        r18 = r19;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        goto L_0x0220;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0229:
        r0 = r18;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r1 = r19;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        if (r0 == r1) goto L_0x0220;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x022f:
        r18.addSuppressed(r19);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        goto L_0x0220;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0233:
        throw r17;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
    L_0x0234:
        r0 = r23;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r0.crl;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = r0;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r0 = r17;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r1 = r24;	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        r17 = getMatchingCRLs(r0, r1);	 Catch:{ IOException -> 0x0152, IOException -> 0x0152 }
        monitor-exit(r23);
        return r17;
    L_0x0244:
        r17 = move-exception;
        goto L_0x021b;
        */
        throw new UnsupportedOperationException("Method not decompiled: sun.security.provider.certpath.URICertStore.engineGetCRLs(java.security.cert.CRLSelector):java.util.Collection<java.security.cert.X509CRL>");
    }

    private static Collection<X509CRL> getMatchingCRLs(X509CRL crl, CRLSelector selector) {
        if (selector == null || (crl != null && selector.match(crl))) {
            return Collections.singletonList(crl);
        }
        return Collections.emptyList();
    }
}
