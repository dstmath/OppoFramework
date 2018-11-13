package com.android.server.pm;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageParser.Package;
import android.content.pm.Signature;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.Xml;
import com.android.server.pm.Policy.PolicyBuilder;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import libcore.io.IoUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public final class SELinuxMMAC {
    private static final String AUTOPLAY_APP_STR = ":autoplayapp";
    private static String COLOROS_MAC_PERMISSIONS = null;
    public static final String COLOROS_SEAPP_MD5_FILE = null;
    private static final boolean DEBUG_POLICY = false;
    private static final boolean DEBUG_POLICY_INSTALL = false;
    private static final boolean DEBUG_POLICY_ORDER = false;
    private static final boolean EXP_VERSION = false;
    private static final File MAC_PERMISSIONS = null;
    private static final String OPPO_SECURITYPAY_FEATURE = "oppo.securitypay.support";
    private static final String PRIVILEGED_APP_STR = ":privapp";
    static final String TAG = "SELinuxMMAC";
    private static List<Policy> sPolicies;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.SELinuxMMAC.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.pm.SELinuxMMAC.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.pm.SELinuxMMAC.<clinit>():void");
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x005e A:{Splitter: B:3:0x0011, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x005e A:{Splitter: B:3:0x0011, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01ea A:{Splitter: B:1:0x000a, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:92:0x01ea A:{Splitter: B:1:0x000a, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01e0 A:{Splitter: B:38:0x00ec, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:86:0x01e0 A:{Splitter: B:38:0x00ec, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0140 A:{Splitter: B:40:0x00f3, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0140 A:{Splitter: B:40:0x00f3, ExcHandler: java.lang.IllegalStateException (e java.lang.IllegalStateException)} */
    /* JADX WARNING: Missing block: B:13:0x005e, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:14:0x005f, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:16:?, code:
            r10 = new java.lang.StringBuilder("Exception @");
            r10.append(r4.getPositionDescription());
            r10.append(" while parsing ");
            r10.append(MAC_PERMISSIONS);
            r10.append(":");
            r10.append(r1);
            android.util.Slog.w(TAG, r10.toString());
     */
    /* JADX WARNING: Missing block: B:17:0x008d, code:
            libcore.io.IoUtils.closeQuietly(r6);
     */
    /* JADX WARNING: Missing block: B:18:0x0091, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:50:0x0140, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:52:?, code:
            r10 = new java.lang.StringBuilder("Exception @");
            r10.append(r4.getPositionDescription());
            r10.append(" while parsing ");
            r10.append(COLOROS_MAC_PERMISSIONS);
            r10.append(":");
            r10.append(r1);
            android.util.Slog.w(TAG, r10.toString());
     */
    /* JADX WARNING: Missing block: B:53:0x016e, code:
            libcore.io.IoUtils.closeQuietly(r6);
     */
    /* JADX WARNING: Missing block: B:54:0x0172, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:55:0x0173, code:
            r11 = th;
     */
    /* JADX WARNING: Missing block: B:56:0x0174, code:
            libcore.io.IoUtils.closeQuietly(r6);
     */
    /* JADX WARNING: Missing block: B:57:0x0177, code:
            throw r11;
     */
    /* JADX WARNING: Missing block: B:85:0x01de, code:
            r11 = th;
     */
    /* JADX WARNING: Missing block: B:86:0x01e0, code:
            r1 = e;
     */
    /* JADX WARNING: Missing block: B:87:0x01e1, code:
            r6 = r7;
     */
    /* JADX WARNING: Missing block: B:92:0x01ea, code:
            r1 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean readInstallPolicy(PackageManagerService pms) {
        IOException ioe;
        Throwable th;
        List<Policy> policies = new ArrayList();
        AutoCloseable policyFile = null;
        XmlPullParser parser = Xml.newPullParser();
        try {
            FileReader policyFile2 = new FileReader(MAC_PERMISSIONS);
            try {
                Slog.d(TAG, "Using policy file " + MAC_PERMISSIONS);
                parser.setInput(policyFile2);
                parser.nextTag();
                parser.require(2, null, "policy");
                while (parser.next() != 3) {
                    if (parser.getEventType() == 2) {
                        if (parser.getName().equals("signer")) {
                            policies.add(readSignerOrThrow(parser));
                        } else {
                            skip(parser);
                        }
                    }
                }
                IoUtils.closeQuietly(policyFile2);
                boolean debug = true;
                if (SystemProperties.getInt("persist.sys.coloros.sandbox", 1) == 0) {
                    debug = false;
                }
                if (!new File(COLOROS_MAC_PERMISSIONS).exists()) {
                    Slog.d(TAG, "coloros mac file not exist!!");
                    debug = false;
                }
                if (!pms.hasSystemFeature(OPPO_SECURITYPAY_FEATURE, 0)) {
                    debug = false;
                }
                if (debug) {
                    FileReader policyFile3;
                    try {
                        policyFile3 = new FileReader(COLOROS_MAC_PERMISSIONS);
                        try {
                            Slog.d(TAG, "Using policy file " + COLOROS_MAC_PERMISSIONS);
                            parser.setInput(policyFile3);
                            parser.nextTag();
                            parser.require(2, null, "policy");
                            while (parser.next() != 3) {
                                if (parser.getEventType() == 2) {
                                    if (parser.getName().equals("signer")) {
                                        policies.add(readSignerOrThrow(parser));
                                    } else {
                                        skip(parser);
                                    }
                                }
                            }
                            IoUtils.closeQuietly(policyFile3);
                        } catch (IllegalStateException e) {
                        } catch (IOException e2) {
                            ioe = e2;
                        }
                    } catch (IllegalStateException e3) {
                    } catch (IOException e4) {
                        ioe = e4;
                        policyFile3 = policyFile2;
                        Slog.w(TAG, "Exception parsing " + COLOROS_MAC_PERMISSIONS, ioe);
                        IoUtils.closeQuietly(policyFile3);
                        return false;
                    } catch (Throwable th2) {
                        th = th2;
                        policyFile3 = policyFile2;
                        IoUtils.closeQuietly(policyFile3);
                        throw th;
                    }
                }
                PolicyComparator policySort = new PolicyComparator();
                Collections.sort(policies, policySort);
                if (policySort.foundDuplicate()) {
                    Slog.w(TAG, "ERROR! Duplicate entries found parsing " + MAC_PERMISSIONS);
                    return false;
                }
                synchronized (sPolicies) {
                    sPolicies = policies;
                }
                return true;
            } catch (IllegalStateException e5) {
            } catch (IOException e6) {
                ioe = e6;
                policyFile = policyFile2;
            } catch (Throwable th3) {
                th = th3;
                Object policyFile4 = policyFile2;
            }
        } catch (IllegalStateException e7) {
        } catch (IOException e8) {
            ioe = e8;
            Slog.w(TAG, "Exception parsing " + MAC_PERMISSIONS, ioe);
            IoUtils.closeQuietly(policyFile);
            return false;
        }
    }

    private static Policy readSignerOrThrow(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "signer");
        PolicyBuilder pb = new PolicyBuilder();
        String cert = parser.getAttributeValue(null, "signature");
        if (cert != null) {
            pb.addSignature(cert);
        }
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                String tagName = parser.getName();
                if ("seinfo".equals(tagName)) {
                    pb.setGlobalSeinfoOrThrow(parser.getAttributeValue(null, "value"));
                    readSeinfo(parser);
                } else if ("package".equals(tagName)) {
                    readPackageOrThrow(parser, pb);
                } else if ("cert".equals(tagName)) {
                    pb.addSignature(parser.getAttributeValue(null, "signature"));
                    readCert(parser);
                } else {
                    skip(parser);
                }
            }
        }
        return pb.build();
    }

    private static void readPackageOrThrow(XmlPullParser parser, PolicyBuilder pb) throws IOException, XmlPullParserException {
        parser.require(2, null, "package");
        String pkgName = parser.getAttributeValue(null, "name");
        while (parser.next() != 3) {
            if (parser.getEventType() == 2) {
                if ("seinfo".equals(parser.getName())) {
                    pb.addInnerPackageMapOrThrow(pkgName, parser.getAttributeValue(null, "value"));
                    readSeinfo(parser);
                } else {
                    skip(parser);
                }
            }
        }
    }

    private static void readCert(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "cert");
        parser.nextTag();
    }

    private static void readSeinfo(XmlPullParser parser) throws IOException, XmlPullParserException {
        parser.require(2, null, "seinfo");
        parser.nextTag();
    }

    private static void skip(XmlPullParser p) throws IOException, XmlPullParserException {
        if (p.getEventType() != 2) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (p.next()) {
                case 2:
                    depth++;
                    break;
                case 3:
                    depth--;
                    break;
                default:
                    break;
            }
        }
    }

    public static void assignSeinfoValue(Package pkg) {
        ApplicationInfo applicationInfo;
        synchronized (sPolicies) {
            for (Policy policy : sPolicies) {
                String seinfo = policy.getMatchedSeinfo(pkg);
                if (seinfo != null) {
                    pkg.applicationInfo.seinfo = seinfo;
                    break;
                }
            }
        }
        if (pkg.applicationInfo.isAutoPlayApp()) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.seinfo += AUTOPLAY_APP_STR;
        }
        if (pkg.applicationInfo.isPrivilegedApp()) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.seinfo += PRIVILEGED_APP_STR;
        }
        if (!pkg.isSystemApp() && pkg.mSharedUserId != null) {
            Slog.d(TAG, "third app shared user : " + pkg.mSharedUserId);
            for (Policy policy2 : sPolicies) {
                if (Signature.areExactMatch((Signature[]) policy2.mCerts.toArray(new Signature[0]), pkg.mSignatures)) {
                    Slog.i(TAG, "share user package (" + pkg.packageName + ") labeled with safespace");
                    for (String name : policy2.mPkgMap.keySet()) {
                        if ("safespace".equals(policy2.mPkgMap.get(name)) && ColorSecurePayManager.getInstance().isSecurePayApp(name)) {
                            Slog.i(TAG, "share uid safe app : " + name);
                            pkg.applicationInfo.seinfo = "safespace";
                        }
                    }
                }
            }
        }
    }
}
