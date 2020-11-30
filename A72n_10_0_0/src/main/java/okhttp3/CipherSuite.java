package okhttp3;

import com.alibaba.fastjson.asm.Opcodes;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class CipherSuite {
    private static final ConcurrentMap<String, CipherSuite> INSTANCES = new ConcurrentHashMap();
    public static final CipherSuite TLS_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA = of("SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA", 17, 4346, 6, 10);
    public static final CipherSuite TLS_DHE_DSS_WITH_3DES_EDE_CBC_SHA = of("SSL_DHE_DSS_WITH_3DES_EDE_CBC_SHA", 19, 5246, 6, 10);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA = of("TLS_DHE_DSS_WITH_AES_128_CBC_SHA", 50, 5246, 6, 10);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_CBC_SHA256 = of("TLS_DHE_DSS_WITH_AES_128_CBC_SHA256", 64, 5246, 7, 21);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_128_GCM_SHA256 = of("TLS_DHE_DSS_WITH_AES_128_GCM_SHA256", Opcodes.IF_ICMPGE, 5288, 8, 21);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA = of("TLS_DHE_DSS_WITH_AES_256_CBC_SHA", 56, 5246, 6, 10);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_CBC_SHA256 = of("TLS_DHE_DSS_WITH_AES_256_CBC_SHA256", 106, 5246, 7, 21);
    public static final CipherSuite TLS_DHE_DSS_WITH_AES_256_GCM_SHA384 = of("TLS_DHE_DSS_WITH_AES_256_GCM_SHA384", Opcodes.IF_ICMPGT, 5288, 8, 21);
    public static final CipherSuite TLS_DHE_DSS_WITH_DES_CBC_SHA = of("SSL_DHE_DSS_WITH_DES_CBC_SHA", 18, 5469, 6, 10);
    public static final CipherSuite TLS_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA = of("SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", 20, 4346, 6, 10);
    public static final CipherSuite TLS_DHE_RSA_WITH_3DES_EDE_CBC_SHA = of("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA", 22, 5246, 6, 10);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA = of("TLS_DHE_RSA_WITH_AES_128_CBC_SHA", 51, 5246, 6, 10);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_DHE_RSA_WITH_AES_128_CBC_SHA256", 103, 5246, 7, 21);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_DHE_RSA_WITH_AES_128_GCM_SHA256", Opcodes.IFLE, 5288, 8, 21);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA = of("TLS_DHE_RSA_WITH_AES_256_CBC_SHA", 57, 5246, 6, 10);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_CBC_SHA256 = of("TLS_DHE_RSA_WITH_AES_256_CBC_SHA256", 107, 5246, 7, 21);
    public static final CipherSuite TLS_DHE_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_DHE_RSA_WITH_AES_256_GCM_SHA384", Opcodes.IF_ICMPEQ, 5288, 8, 21);
    public static final CipherSuite TLS_DHE_RSA_WITH_DES_CBC_SHA = of("SSL_DHE_RSA_WITH_DES_CBC_SHA", 21, 5469, 6, 10);
    public static final CipherSuite TLS_DH_anon_EXPORT_WITH_DES40_CBC_SHA = of("SSL_DH_anon_EXPORT_WITH_DES40_CBC_SHA", 25, 4346, 6, 10);
    public static final CipherSuite TLS_DH_anon_EXPORT_WITH_RC4_40_MD5 = of("SSL_DH_anon_EXPORT_WITH_RC4_40_MD5", 23, 4346, 6, 10);
    public static final CipherSuite TLS_DH_anon_WITH_3DES_EDE_CBC_SHA = of("SSL_DH_anon_WITH_3DES_EDE_CBC_SHA", 27, 5246, 6, 10);
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA = of("TLS_DH_anon_WITH_AES_128_CBC_SHA", 52, 5246, 6, 10);
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_CBC_SHA256 = of("TLS_DH_anon_WITH_AES_128_CBC_SHA256", 108, 5246, 7, 21);
    public static final CipherSuite TLS_DH_anon_WITH_AES_128_GCM_SHA256 = of("TLS_DH_anon_WITH_AES_128_GCM_SHA256", Opcodes.IF_ACMPNE, 5288, 8, 21);
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA = of("TLS_DH_anon_WITH_AES_256_CBC_SHA", 58, 5246, 6, 10);
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_CBC_SHA256 = of("TLS_DH_anon_WITH_AES_256_CBC_SHA256", 109, 5246, 7, 21);
    public static final CipherSuite TLS_DH_anon_WITH_AES_256_GCM_SHA384 = of("TLS_DH_anon_WITH_AES_256_GCM_SHA384", Opcodes.GOTO, 5288, 8, 21);
    public static final CipherSuite TLS_DH_anon_WITH_DES_CBC_SHA = of("SSL_DH_anon_WITH_DES_CBC_SHA", 26, 5469, 6, 10);
    public static final CipherSuite TLS_DH_anon_WITH_RC4_128_MD5 = of("SSL_DH_anon_WITH_RC4_128_MD5", 24, 5246, 6, 10);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDHE_ECDSA_WITH_3DES_EDE_CBC_SHA", 49160, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA = of("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA", 49161, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDHE_ECDSA_WITH_AES_128_CBC_SHA256", 49187, 5289, 7, 21);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDHE_ECDSA_WITH_AES_128_GCM_SHA256", 49195, 5289, 8, 21);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA = of("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA", 49162, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDHE_ECDSA_WITH_AES_256_CBC_SHA384", 49188, 5289, 7, 21);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDHE_ECDSA_WITH_AES_256_GCM_SHA384", 49196, 5289, 8, 21);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_NULL_SHA = of("TLS_ECDHE_ECDSA_WITH_NULL_SHA", 49158, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_ECDSA_WITH_RC4_128_SHA = of("TLS_ECDHE_ECDSA_WITH_RC4_128_SHA", 49159, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA", 49170, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA = of("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA", 49171, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256", 49191, 5289, 7, 21);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256", 49199, 5289, 8, 21);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA = of("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA", 49172, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384", 49192, 5289, 7, 21);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384", 49200, 5289, 8, 21);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_NULL_SHA = of("TLS_ECDHE_RSA_WITH_NULL_SHA", 49168, 4492, 7, 14);
    public static final CipherSuite TLS_ECDHE_RSA_WITH_RC4_128_SHA = of("TLS_ECDHE_RSA_WITH_RC4_128_SHA", 49169, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDH_ECDSA_WITH_3DES_EDE_CBC_SHA", 49155, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA = of("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA", 49156, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDH_ECDSA_WITH_AES_128_CBC_SHA256", 49189, 5289, 7, 21);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDH_ECDSA_WITH_AES_128_GCM_SHA256", 49197, 5289, 8, 21);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA = of("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA", 49157, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDH_ECDSA_WITH_AES_256_CBC_SHA384", 49190, 5289, 7, 21);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDH_ECDSA_WITH_AES_256_GCM_SHA384", 49198, 5289, 8, 21);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_NULL_SHA = of("TLS_ECDH_ECDSA_WITH_NULL_SHA", 49153, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_ECDSA_WITH_RC4_128_SHA = of("TLS_ECDH_ECDSA_WITH_RC4_128_SHA", 49154, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDH_RSA_WITH_3DES_EDE_CBC_SHA", 49165, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA = of("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA", 49166, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_ECDH_RSA_WITH_AES_128_CBC_SHA256", 49193, 5289, 7, 21);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_ECDH_RSA_WITH_AES_128_GCM_SHA256", 49201, 5289, 8, 21);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA = of("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA", 49167, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384 = of("TLS_ECDH_RSA_WITH_AES_256_CBC_SHA384", 49194, 5289, 7, 21);
    public static final CipherSuite TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_ECDH_RSA_WITH_AES_256_GCM_SHA384", 49202, 5289, 8, 21);
    public static final CipherSuite TLS_ECDH_RSA_WITH_NULL_SHA = of("TLS_ECDH_RSA_WITH_NULL_SHA", 49163, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_RSA_WITH_RC4_128_SHA = of("TLS_ECDH_RSA_WITH_RC4_128_SHA", 49164, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA = of("TLS_ECDH_anon_WITH_3DES_EDE_CBC_SHA", 49175, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_anon_WITH_AES_128_CBC_SHA = of("TLS_ECDH_anon_WITH_AES_128_CBC_SHA", 49176, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_anon_WITH_AES_256_CBC_SHA = of("TLS_ECDH_anon_WITH_AES_256_CBC_SHA", 49177, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_anon_WITH_NULL_SHA = of("TLS_ECDH_anon_WITH_NULL_SHA", 49173, 4492, 7, 14);
    public static final CipherSuite TLS_ECDH_anon_WITH_RC4_128_SHA = of("TLS_ECDH_anon_WITH_RC4_128_SHA", 49174, 4492, 7, 14);
    public static final CipherSuite TLS_EMPTY_RENEGOTIATION_INFO_SCSV = of("TLS_EMPTY_RENEGOTIATION_INFO_SCSV", 255, 5746, 6, 14);
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5 = of("TLS_KRB5_EXPORT_WITH_DES_CBC_40_MD5", 41, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA = of("TLS_KRB5_EXPORT_WITH_DES_CBC_40_SHA", 38, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_RC4_40_MD5 = of("TLS_KRB5_EXPORT_WITH_RC4_40_MD5", 43, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_EXPORT_WITH_RC4_40_SHA = of("TLS_KRB5_EXPORT_WITH_RC4_40_SHA", 40, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_WITH_3DES_EDE_CBC_MD5 = of("TLS_KRB5_WITH_3DES_EDE_CBC_MD5", 35, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_WITH_3DES_EDE_CBC_SHA = of("TLS_KRB5_WITH_3DES_EDE_CBC_SHA", 31, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_WITH_DES_CBC_MD5 = of("TLS_KRB5_WITH_DES_CBC_MD5", 34, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_WITH_DES_CBC_SHA = of("TLS_KRB5_WITH_DES_CBC_SHA", 30, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_WITH_RC4_128_MD5 = of("TLS_KRB5_WITH_RC4_128_MD5", 36, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_KRB5_WITH_RC4_128_SHA = of("TLS_KRB5_WITH_RC4_128_SHA", 32, 2712, 6, Integer.MAX_VALUE);
    public static final CipherSuite TLS_RSA_EXPORT_WITH_DES40_CBC_SHA = of("SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", 8, 4346, 6, 10);
    public static final CipherSuite TLS_RSA_EXPORT_WITH_RC4_40_MD5 = of("SSL_RSA_EXPORT_WITH_RC4_40_MD5", 3, 4346, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_3DES_EDE_CBC_SHA = of("SSL_RSA_WITH_3DES_EDE_CBC_SHA", 10, 5246, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA = of("TLS_RSA_WITH_AES_128_CBC_SHA", 47, 5246, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_AES_128_CBC_SHA256 = of("TLS_RSA_WITH_AES_128_CBC_SHA256", 60, 5246, 7, 21);
    public static final CipherSuite TLS_RSA_WITH_AES_128_GCM_SHA256 = of("TLS_RSA_WITH_AES_128_GCM_SHA256", 156, 5288, 8, 21);
    public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA = of("TLS_RSA_WITH_AES_256_CBC_SHA", 53, 5246, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_AES_256_CBC_SHA256 = of("TLS_RSA_WITH_AES_256_CBC_SHA256", 61, 5246, 7, 21);
    public static final CipherSuite TLS_RSA_WITH_AES_256_GCM_SHA384 = of("TLS_RSA_WITH_AES_256_GCM_SHA384", 157, 5288, 8, 21);
    public static final CipherSuite TLS_RSA_WITH_DES_CBC_SHA = of("SSL_RSA_WITH_DES_CBC_SHA", 9, 5469, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_NULL_MD5 = of("SSL_RSA_WITH_NULL_MD5", 1, 5246, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_NULL_SHA = of("SSL_RSA_WITH_NULL_SHA", 2, 5246, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_NULL_SHA256 = of("TLS_RSA_WITH_NULL_SHA256", 59, 5246, 7, 21);
    public static final CipherSuite TLS_RSA_WITH_RC4_128_MD5 = of("SSL_RSA_WITH_RC4_128_MD5", 4, 5246, 6, 10);
    public static final CipherSuite TLS_RSA_WITH_RC4_128_SHA = of("SSL_RSA_WITH_RC4_128_SHA", 5, 5246, 6, 10);
    final String javaName;

    public static CipherSuite forJavaName(String javaName2) {
        CipherSuite result = INSTANCES.get(javaName2);
        if (result != null) {
            return result;
        }
        CipherSuite sample = new CipherSuite(javaName2);
        CipherSuite canonical = INSTANCES.putIfAbsent(javaName2, sample);
        return canonical == null ? sample : canonical;
    }

    private CipherSuite(String javaName2) {
        if (javaName2 != null) {
            this.javaName = javaName2;
            return;
        }
        throw new NullPointerException();
    }

    private static CipherSuite of(String javaName2, int value, int rfc, int sinceJavaVersion, int sinceAndroidVersion) {
        return forJavaName(javaName2);
    }

    public String javaName() {
        return this.javaName;
    }

    public String toString() {
        return this.javaName;
    }
}
