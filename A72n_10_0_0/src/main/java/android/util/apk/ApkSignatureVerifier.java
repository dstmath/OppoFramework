package android.util.apk;

import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.Signature;
import android.os.Trace;
import android.util.ArrayMap;
import android.util.Slog;
import android.util.apk.ApkSignatureSchemeV3Verifier;
import android.util.jar.StrictJarFile;
import com.android.internal.util.ArrayUtils;
import java.io.IOException;
import java.io.InputStream;
import java.security.DigestException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;

public class ApkSignatureVerifier {
    private static final int NUMBER_OF_CORES;
    private static final String TAG = "ApkSignatureVerifier";
    private static final AtomicReference<byte[]> sBuffer = new AtomicReference<>();

    static {
        int i = 4;
        if (Runtime.getRuntime().availableProcessors() < 4) {
            i = Runtime.getRuntime().availableProcessors();
        }
        NUMBER_OF_CORES = i;
    }

    public static PackageParser.SigningDetails verify(String apkPath, @PackageParser.SigningDetails.SignatureSchemeVersion int minSignatureSchemeVersion) throws PackageParser.PackageParserException {
        if (minSignatureSchemeVersion <= 3) {
            Trace.traceBegin(262144, "verifyV3");
            try {
                ApkSignatureSchemeV3Verifier.VerifiedSigner vSigner = ApkSignatureSchemeV3Verifier.verify(apkPath);
                Signature[] signerSigs = convertToSignatures(new Certificate[][]{vSigner.certs});
                Signature[] pastSignerSigs = null;
                if (vSigner.por != null) {
                    pastSignerSigs = new Signature[vSigner.por.certs.size()];
                    for (int i = 0; i < pastSignerSigs.length; i++) {
                        pastSignerSigs[i] = new Signature(vSigner.por.certs.get(i).getEncoded());
                        pastSignerSigs[i].setFlags(vSigner.por.flagsList.get(i).intValue());
                    }
                }
                PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(signerSigs, 3, pastSignerSigs);
                Trace.traceEnd(262144);
                return signingDetails;
            } catch (SignatureNotFoundException e) {
                if (minSignatureSchemeVersion < 3) {
                    Trace.traceEnd(262144);
                    if (minSignatureSchemeVersion <= 2) {
                        Trace.traceBegin(262144, "verifyV2");
                        try {
                            PackageParser.SigningDetails signingDetails2 = new PackageParser.SigningDetails(convertToSignatures(ApkSignatureSchemeV2Verifier.verify(apkPath)), 2);
                            Trace.traceEnd(262144);
                            return signingDetails2;
                        } catch (SignatureNotFoundException e2) {
                            if (minSignatureSchemeVersion < 2) {
                                Trace.traceEnd(262144);
                                if (minSignatureSchemeVersion <= 1) {
                                    return verifyV1Signature(apkPath, true);
                                }
                                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                            }
                            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No APK Signature Scheme v2 signature in package " + apkPath, e2);
                        } catch (Exception e3) {
                            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v2", e3);
                        } catch (Throwable th) {
                            Trace.traceEnd(262144);
                            throw th;
                        }
                    } else {
                        throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                    }
                } else {
                    throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No APK Signature Scheme v3 signature in package " + apkPath, e);
                }
            } catch (Exception e4) {
                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v3", e4);
            } catch (Throwable th2) {
                Trace.traceEnd(262144);
                throw th2;
            }
        } else {
            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
        }
    }

    private static PackageParser.SigningDetails verifyV1Signature(String apkPath, boolean verifyFull) throws PackageParser.PackageParserException {
        StrictJarFile jarFile = null;
        if (verifyFull && NUMBER_OF_CORES > 1) {
            return oppo_verifyV1Signature(apkPath, verifyFull);
        }
        try {
            Trace.traceBegin(262144, "strictJarFileCtor");
            jarFile = new StrictJarFile(apkPath, true, verifyFull);
            List<ZipEntry> toVerify = new ArrayList<>();
            ZipEntry manifestEntry = jarFile.findEntry(PackageParser.ANDROID_MANIFEST_FILENAME);
            if (manifestEntry != null) {
                Certificate[][] lastCerts = loadCertificates(jarFile, manifestEntry);
                if (!ArrayUtils.isEmpty(lastCerts)) {
                    Signature[] lastSigs = convertToSignatures(lastCerts);
                    if (verifyFull) {
                        Iterator<ZipEntry> i = jarFile.iterator();
                        while (i.hasNext()) {
                            ZipEntry entry = i.next();
                            if (!entry.isDirectory()) {
                                String entryName = entry.getName();
                                if (!entryName.startsWith("META-INF/")) {
                                    if (!entryName.equals(PackageParser.ANDROID_MANIFEST_FILENAME)) {
                                        toVerify.add(entry);
                                    }
                                }
                            }
                        }
                        for (ZipEntry entry2 : toVerify) {
                            Certificate[][] entryCerts = loadCertificates(jarFile, entry2);
                            if (ArrayUtils.isEmpty(entryCerts)) {
                                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Package " + apkPath + " has no certificates at entry " + entry2.getName());
                            } else if (!Signature.areExactMatch(lastSigs, convertToSignatures(entryCerts))) {
                                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, "Package " + apkPath + " has mismatched certificates at entry " + entry2.getName());
                            }
                        }
                    }
                    PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(lastSigs, 1);
                    Trace.traceEnd(262144);
                    closeQuietly(jarFile);
                    return signingDetails;
                }
                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Package " + apkPath + " has no certificates at entry " + PackageParser.ANDROID_MANIFEST_FILENAME);
            }
            throw new PackageParser.PackageParserException(-101, "Package " + apkPath + " has no manifest");
        } catch (GeneralSecurityException e) {
            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e);
        } catch (IOException | RuntimeException e2) {
            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath, e2);
        } catch (Throwable th) {
            Trace.traceEnd(262144);
            closeQuietly(jarFile);
            throw th;
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:73:0x01c3, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:76:0x01dc, code lost:
        throw new android.content.pm.PackageParser.PackageParserException(android.content.pm.PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + r26, r0);
     */
    private static PackageParser.SigningDetails oppo_verifyV1Signature(final String apkPath, boolean verifyFull) throws PackageParser.PackageParserException {
        Signature[] lastSigs;
        int objectNumber = verifyFull ? NUMBER_OF_CORES : 1;
        final StrictJarFile[] jarFile = new StrictJarFile[objectNumber];
        final ArrayMap<String, StrictJarFile> strictJarFiles = new ArrayMap<>();
        try {
            Trace.traceBegin(262144, "strictJarFileCtor");
            for (int i = 0; i < objectNumber; i++) {
                jarFile[i] = new StrictJarFile(apkPath, true, verifyFull);
            }
            List<ZipEntry> toVerify = new ArrayList<>();
            ZipEntry manifestEntry = jarFile[0].findEntry(PackageParser.ANDROID_MANIFEST_FILENAME);
            if (manifestEntry != null) {
                Certificate[][] lastCerts = loadCertificates(jarFile[0], manifestEntry);
                if (!ArrayUtils.isEmpty(lastCerts)) {
                    final Signature[] lastSigs2 = convertToSignatures(lastCerts);
                    if (verifyFull) {
                        Iterator<ZipEntry> i2 = jarFile[0].iterator();
                        while (i2.hasNext()) {
                            ZipEntry entry = i2.next();
                            if (!entry.isDirectory()) {
                                String entryName = entry.getName();
                                if (!entryName.startsWith("META-INF/")) {
                                    if (!entryName.equals(PackageParser.ANDROID_MANIFEST_FILENAME)) {
                                        toVerify.add(entry);
                                    }
                                }
                            }
                        }
                        final AnonymousClass1VerificationData vData = new Object() {
                            /* class android.util.apk.ApkSignatureVerifier.AnonymousClass1VerificationData */
                            public Exception exception;
                            public int exceptionFlag;
                            public int index;
                            public Object objWaitAll;
                            public boolean shutDown;
                            public boolean wait;
                        };
                        vData.objWaitAll = new Object();
                        ThreadPoolExecutor verificationExecutor = new ThreadPoolExecutor(NUMBER_OF_CORES, NUMBER_OF_CORES, 1, TimeUnit.SECONDS, new LinkedBlockingQueue());
                        for (final ZipEntry entry2 : toVerify) {
                            Runnable verifyTask = new Runnable() {
                                /* class android.util.apk.ApkSignatureVerifier.AnonymousClass1 */

                                public void run() {
                                    StrictJarFile tempJarFile;
                                    try {
                                        if (AnonymousClass1VerificationData.this.exceptionFlag != 0) {
                                            Slog.w(ApkSignatureVerifier.TAG, "VerifyV1 exit with exception " + AnonymousClass1VerificationData.this.exceptionFlag);
                                            return;
                                        }
                                        String tid = Long.toString(Thread.currentThread().getId());
                                        synchronized (strictJarFiles) {
                                            tempJarFile = (StrictJarFile) strictJarFiles.get(tid);
                                            if (tempJarFile == null) {
                                                if (AnonymousClass1VerificationData.this.index >= ApkSignatureVerifier.NUMBER_OF_CORES) {
                                                    AnonymousClass1VerificationData.this.index = 0;
                                                }
                                                StrictJarFile[] strictJarFileArr = jarFile;
                                                AnonymousClass1VerificationData r4 = AnonymousClass1VerificationData.this;
                                                int i = r4.index;
                                                r4.index = i + 1;
                                                tempJarFile = strictJarFileArr[i];
                                                strictJarFiles.put(tid, tempJarFile);
                                            }
                                        }
                                        Certificate[][] entryCerts = ApkSignatureVerifier.loadCertificates(tempJarFile, entry2);
                                        if (!ArrayUtils.isEmpty(entryCerts)) {
                                            if (!Signature.areExactMatch(lastSigs2, ApkSignatureVerifier.convertToSignatures(entryCerts))) {
                                                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, "Package " + apkPath + " has mismatched certificates at entry " + entry2.getName());
                                            }
                                            return;
                                        }
                                        throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Package " + apkPath + " has no certificates at entry " + entry2.getName());
                                    } catch (GeneralSecurityException e) {
                                        synchronized (AnonymousClass1VerificationData.this.objWaitAll) {
                                            AnonymousClass1VerificationData.this.exceptionFlag = PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING;
                                            AnonymousClass1VerificationData.this.exception = e;
                                        }
                                    } catch (PackageParser.PackageParserException e2) {
                                        synchronized (AnonymousClass1VerificationData.this.objWaitAll) {
                                            AnonymousClass1VerificationData.this.exceptionFlag = PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
                                            AnonymousClass1VerificationData.this.exception = e2;
                                        }
                                    }
                                }
                            };
                            vData = vData;
                            synchronized (vData.objWaitAll) {
                                if (vData.exceptionFlag == 0) {
                                    verificationExecutor.execute(verifyTask);
                                }
                            }
                            verificationExecutor = verificationExecutor;
                            lastSigs2 = lastSigs2;
                            manifestEntry = manifestEntry;
                            toVerify = toVerify;
                        }
                        lastSigs = lastSigs2;
                        vData.wait = true;
                        verificationExecutor.shutdown();
                        while (vData.wait) {
                            try {
                                if (vData.exceptionFlag != 0 && !vData.shutDown) {
                                    Slog.w(TAG, "verifyV1 Exception " + vData.exceptionFlag);
                                    verificationExecutor.shutdownNow();
                                    vData.shutDown = true;
                                }
                                vData.wait = !verificationExecutor.awaitTermination(50, TimeUnit.MILLISECONDS);
                            } catch (InterruptedException e) {
                                Slog.w(TAG, "VerifyV1 interrupted while awaiting all threads done...");
                            }
                        }
                        if (vData.exceptionFlag != 0) {
                            int i3 = vData.exceptionFlag;
                            throw new PackageParser.PackageParserException(i3, "Failed to collect certificates from " + apkPath, vData.exception);
                        }
                    } else {
                        lastSigs = lastSigs2;
                    }
                    PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(lastSigs, 1);
                    strictJarFiles.clear();
                    Trace.traceEnd(262144);
                    for (int i4 = 0; i4 < objectNumber; i4++) {
                        closeQuietly(jarFile[i4]);
                    }
                    return signingDetails;
                }
                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Package " + apkPath + " has no certificates at entry " + PackageParser.ANDROID_MANIFEST_FILENAME);
            }
            throw new PackageParser.PackageParserException(-101, "Package " + apkPath + " has no manifest");
        } catch (GeneralSecurityException e2) {
            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e2);
        } catch (Throwable th) {
            strictJarFiles.clear();
            Trace.traceEnd(262144);
            for (int i5 = 0; i5 < objectNumber; i5++) {
                closeQuietly(jarFile[i5]);
            }
            throw th;
        }
    }

    /* access modifiers changed from: private */
    public static Certificate[][] loadCertificates(StrictJarFile jarFile, ZipEntry entry) throws PackageParser.PackageParserException {
        InputStream is = null;
        try {
            is = jarFile.getInputStream(entry);
            readFullyIgnoringContents(is);
            Certificate[][] certificateChains = jarFile.getCertificateChains(entry);
            IoUtils.closeQuietly(is);
            return certificateChains;
        } catch (IOException | RuntimeException e) {
            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed reading " + entry.getName() + " in " + jarFile, e);
        } catch (Throwable th) {
            IoUtils.closeQuietly(is);
            throw th;
        }
    }

    private static void readFullyIgnoringContents(InputStream in) throws IOException {
        byte[] buffer = sBuffer.getAndSet(null);
        if (buffer == null) {
            buffer = new byte[4096];
        }
        int count = 0;
        while (true) {
            int n = in.read(buffer, 0, buffer.length);
            if (n != -1) {
                count += n;
            } else {
                sBuffer.set(buffer);
                return;
            }
        }
    }

    public static Signature[] convertToSignatures(Certificate[][] certs) throws CertificateEncodingException {
        Signature[] res = new Signature[certs.length];
        for (int i = 0; i < certs.length; i++) {
            res[i] = new Signature(certs[i]);
        }
        return res;
    }

    private static void closeQuietly(StrictJarFile jarFile) {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (Exception e) {
            }
        }
    }

    public static PackageParser.SigningDetails unsafeGetCertsWithoutVerification(String apkPath, int minSignatureSchemeVersion) throws PackageParser.PackageParserException {
        if (minSignatureSchemeVersion <= 3) {
            Trace.traceBegin(262144, "certsOnlyV3");
            try {
                ApkSignatureSchemeV3Verifier.VerifiedSigner vSigner = ApkSignatureSchemeV3Verifier.unsafeGetCertsWithoutVerification(apkPath);
                Signature[] signerSigs = convertToSignatures(new Certificate[][]{vSigner.certs});
                Signature[] pastSignerSigs = null;
                if (vSigner.por != null) {
                    pastSignerSigs = new Signature[vSigner.por.certs.size()];
                    for (int i = 0; i < pastSignerSigs.length; i++) {
                        pastSignerSigs[i] = new Signature(vSigner.por.certs.get(i).getEncoded());
                        pastSignerSigs[i].setFlags(vSigner.por.flagsList.get(i).intValue());
                    }
                }
                PackageParser.SigningDetails signingDetails = new PackageParser.SigningDetails(signerSigs, 3, pastSignerSigs);
                Trace.traceEnd(262144);
                return signingDetails;
            } catch (SignatureNotFoundException e) {
                if (minSignatureSchemeVersion < 3) {
                    Trace.traceEnd(262144);
                    if (minSignatureSchemeVersion <= 2) {
                        Trace.traceBegin(262144, "certsOnlyV2");
                        try {
                            PackageParser.SigningDetails signingDetails2 = new PackageParser.SigningDetails(convertToSignatures(ApkSignatureSchemeV2Verifier.unsafeGetCertsWithoutVerification(apkPath)), 2);
                            Trace.traceEnd(262144);
                            return signingDetails2;
                        } catch (SignatureNotFoundException e2) {
                            if (minSignatureSchemeVersion < 2) {
                                Trace.traceEnd(262144);
                                if (minSignatureSchemeVersion <= 1) {
                                    return verifyV1Signature(apkPath, false);
                                }
                                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                            }
                            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No APK Signature Scheme v2 signature in package " + apkPath, e2);
                        } catch (Exception e3) {
                            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v2", e3);
                        } catch (Throwable th) {
                            Trace.traceEnd(262144);
                            throw th;
                        }
                    } else {
                        throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
                    }
                } else {
                    throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No APK Signature Scheme v3 signature in package " + apkPath, e);
                }
            } catch (Exception e4) {
                throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v3", e4);
            } catch (Throwable th2) {
                Trace.traceEnd(262144);
                throw th2;
            }
        } else {
            throw new PackageParser.PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "No signature found in package of version " + minSignatureSchemeVersion + " or newer for package " + apkPath);
        }
    }

    public static byte[] getVerityRootHash(String apkPath) throws IOException, SecurityException {
        try {
            return ApkSignatureSchemeV3Verifier.getVerityRootHash(apkPath);
        } catch (SignatureNotFoundException e) {
            try {
                return ApkSignatureSchemeV2Verifier.getVerityRootHash(apkPath);
            } catch (SignatureNotFoundException e2) {
                return null;
            }
        }
    }

    public static byte[] generateApkVerity(String apkPath, ByteBufferFactory bufferFactory) throws IOException, SignatureNotFoundException, SecurityException, DigestException, NoSuchAlgorithmException {
        try {
            return ApkSignatureSchemeV3Verifier.generateApkVerity(apkPath, bufferFactory);
        } catch (SignatureNotFoundException e) {
            return ApkSignatureSchemeV2Verifier.generateApkVerity(apkPath, bufferFactory);
        }
    }

    public static byte[] generateApkVerityRootHash(String apkPath) throws NoSuchAlgorithmException, DigestException, IOException {
        try {
            return ApkSignatureSchemeV3Verifier.generateApkVerityRootHash(apkPath);
        } catch (SignatureNotFoundException e) {
            try {
                return ApkSignatureSchemeV2Verifier.generateApkVerityRootHash(apkPath);
            } catch (SignatureNotFoundException e2) {
                return null;
            }
        }
    }

    public static class Result {
        public final Certificate[][] certs;
        public final int signatureSchemeVersion;
        public final Signature[] sigs;

        public Result(Certificate[][] certs2, Signature[] sigs2, int signingVersion) {
            this.certs = certs2;
            this.sigs = sigs2;
            this.signatureSchemeVersion = signingVersion;
        }
    }
}
