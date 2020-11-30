package okhttp3.internal.tls;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

public abstract class TrustRootIndex {
    public abstract X509Certificate findByIssuerAndSignature(X509Certificate x509Certificate);

    public static TrustRootIndex get(X509TrustManager trustManager) {
        try {
            Method method = trustManager.getClass().getDeclaredMethod("findTrustAnchorByIssuerAndSignature", X509Certificate.class);
            method.setAccessible(true);
            return new AndroidTrustRootIndex(trustManager, method);
        } catch (NoSuchMethodException e) {
            return get(trustManager.getAcceptedIssuers());
        }
    }

    public static TrustRootIndex get(X509Certificate... caCerts) {
        return new BasicTrustRootIndex(caCerts);
    }

    /* access modifiers changed from: package-private */
    public static final class AndroidTrustRootIndex extends TrustRootIndex {
        private final Method findByIssuerAndSignatureMethod;
        private final X509TrustManager trustManager;

        AndroidTrustRootIndex(X509TrustManager trustManager2, Method findByIssuerAndSignatureMethod2) {
            this.findByIssuerAndSignatureMethod = findByIssuerAndSignatureMethod2;
            this.trustManager = trustManager2;
        }

        @Override // okhttp3.internal.tls.TrustRootIndex
        public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
            try {
                TrustAnchor trustAnchor = (TrustAnchor) this.findByIssuerAndSignatureMethod.invoke(this.trustManager, cert);
                if (trustAnchor != null) {
                    return trustAnchor.getTrustedCert();
                }
                return null;
            } catch (IllegalAccessException e) {
                throw new AssertionError();
            } catch (InvocationTargetException e2) {
                return null;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static final class BasicTrustRootIndex extends TrustRootIndex {
        private final Map<X500Principal, List<X509Certificate>> subjectToCaCerts = new LinkedHashMap();

        public BasicTrustRootIndex(X509Certificate... caCerts) {
            for (X509Certificate caCert : caCerts) {
                X500Principal subject = caCert.getSubjectX500Principal();
                List<X509Certificate> subjectCaCerts = this.subjectToCaCerts.get(subject);
                if (subjectCaCerts == null) {
                    subjectCaCerts = new ArrayList(1);
                    this.subjectToCaCerts.put(subject, subjectCaCerts);
                }
                subjectCaCerts.add(caCert);
            }
        }

        @Override // okhttp3.internal.tls.TrustRootIndex
        public X509Certificate findByIssuerAndSignature(X509Certificate cert) {
            List<X509Certificate> subjectCaCerts = this.subjectToCaCerts.get(cert.getIssuerX500Principal());
            if (subjectCaCerts == null) {
                return null;
            }
            for (X509Certificate caCert : subjectCaCerts) {
                try {
                    cert.verify(caCert.getPublicKey());
                    return caCert;
                } catch (Exception e) {
                }
            }
            return null;
        }
    }
}
