package android.content.pm;

import android.annotation.OppoHook;
import android.annotation.OppoHook.OppoHookType;
import android.annotation.OppoHook.OppoRomType;
import android.app.ActivityManager;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.content.pm.ActivityInfo.WindowLayout;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.hardware.Camera.Parameters;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiEnterpriseConfig;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.FileUtils;
import android.os.PatternMatcher;
import android.os.Trace;
import android.os.UserHandle;
import android.security.keystore.KeyProperties;
import android.service.notification.ZenModeConfig;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.util.Slog;
import android.util.TypedValue;
import android.util.apk.ApkSignatureSchemeV2Verifier;
import android.util.apk.ApkSignatureSchemeV2Verifier.SignatureNotFoundException;
import android.util.jar.StrictJarFile;
import com.android.internal.R;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.XmlUtils;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import libcore.io.IoUtils;
import oppo.util.OppoMultiLauncherUtil;
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
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public class PackageParser {
    private static final String ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml";
    private static final String ANDROID_RESOURCES = "http://schemas.android.com/apk/res/android";
    public static final int APK_SIGNING_UNKNOWN = 0;
    public static final int APK_SIGNING_V1 = 1;
    public static final int APK_SIGNING_V2 = 2;
    private static final Set<String> CHILD_PACKAGE_TAGS = null;
    private static final String CTEMM_PEM_PUBLIC_KEY = "-----BEGIN CERTIFICATE-----\nMIIFrDCCA5SgAwIBAgICEAEwDQYJKoZIhvcNAQELBQAwczELMAkGA1UEBhMCQ04x\nCzAJBgNVBAgMAkJKMQswCQYDVQQKDAJDVDENMAsGA1UECwwEQ1RTSTEaMBgGA1UE\nAwwRQ1RDQS1JTlRFUk1FRElBVEUxHzAdBgkqhkiG9w0BCQEWEGN0Y2FAY3RzaS5j\nb20uY24wHhcNMTYwODIwMDgwNjU0WhcNMTcwODMwMDgwNjU0WjB6MQswCQYDVQQG\nEwJDTjELMAkGA1UECAwCQkoxCzAJBgNVBAcMAkJKMQswCQYDVQQKDAJDVDENMAsG\nA1UECwwEQ1RTSTEVMBMGA1UEAwwMQ1RTSS1FTU1DRVJUMR4wHAYJKoZIhvcNAQkB\nFg9zb2NAY3RzaS5jb20uY24wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIB\nAQC6WgQ2S/gqgY4ldZUeCb1immgAXo63ilseCy0Va5kBii2bHO1QAjrfZo8dd5KJ\nBb5SA3F6unitVqiJNwjX/ZhMvB3uWy/FGlp0TFlJAdQ/HM6ZY0XkbfeRH7v1C4+E\naxlzqCWlfbXWFSwaZXRRwuAlbHUdlrSmoanpNRaN3Z/W0nl4WWkRlrrMPIM1AuXT\nIr2pwgTadbVK9J9JXXeJ2RhZFV9GJXLM3RB9DDNsjHHnIxlbdaC17YVnNasZjmp/\nF4WGvt+21M/p4t1bnS/Kv3Jj30icuyD9zypuOoNvM4t3VScIv/iIzEDWiZq2sg/w\nr2QKoqjAFowTO8GVPWqBAxVJAgMBAAGjggFBMIIBPTAJBgNVHRMEAjAAMBEGCWCG\nSAGG+EIBAQQEAwIGQDAzBglghkgBhvhCAQ0EJhYkT3BlblNTTCBHZW5lcmF0ZWQg\nU2VydmVyIENlcnRpZmljYXRlMB0GA1UdDgQWBBStVpCoWbmPOf1VrpLVdoOLvXLq\nqzCBowYDVR0jBIGbMIGYgBSGbtcFWWd5ThOWroYYzhxe1CdAEaF8pHoweDELMAkG\nA1UEBhMCQ04xCzAJBgNVBAgMAkJKMQswCQYDVQQHDAJCSjELMAkGA1UECgwCQ1Qx\nDTALBgNVBAsMBENUU0kxEjAQBgNVBAMMCUNUQ0EtUk9PVDEfMB0GCSqGSIb3DQEJ\nARYQY3RjYUBjdHNpLmNvbS5jboICEAEwDgYDVR0PAQH/BAQDAgWgMBMGA1UdJQQM\nMAoGCCsGAQUFBwMBMA0GCSqGSIb3DQEBCwUAA4ICAQBBgfrc9WswQCV5plZ5FfPF\ncPXsoubLfkG6+8fmcjy+9h9LRCJGn/HgUz81Q+FYQt6MFU9I6j9rwSw4AxUO2++7\n2Env5OMP1AhhALnF8VJVANv1HbEwwwnuvKG1I1kDgWPLaBdhZRKYlKaH6nZJPkZL\n1UO8cHYNoLPbBaAV60W9P3A2DwClcBYa6jPYXzDdtjPk2UOh9hFr63KQ+hiXhnKF\nOXbyB5BVqgGjpxJU5peb/21QFY+wSCNvK3f7iV4DGhEB0ulL2rO1Ui/xdEDmo9V5\nTn9Ixd/AazHAIkhsgivQZ5wyiH7rExlcdxUzOuNc0FSHcPnHeGlhbdh/IxeyxCDc\nZcbl/JbL1a81s6AIygjf9kMVWIgPiCFuayybzOOrKgn7MRWsW71cV5QjphAPHlHN\nv5k+LM7g37pPsNTMYsi21Taj2YuzQf4TiRYle8M+eYdypq1gMU3xrvgx2BAaBG8B\nMIKaU2zrQoG09VKrDbxwjzC7dXDEOlnhO9x1E+rEyCP1vq9Wa7ZtuN9tjc0j23Jd\nv/9lMJQoH+MzKOxLqcdF2EDbKXklu3NLL6deSRJz+X+2yb8lUuSsmOrfXQp1VVEf\nxPfWG6fMPmpPU0tvflIHjZPXJyq88nDxAVxcYxEaOPSJp6duotOptKxq1aqQqH/9\nZNoAt2cRFf8WiFbjuqpQlA==\n-----END CERTIFICATE-----\n";
    private static final boolean DEBUG_BACKUP = false;
    private static final boolean DEBUG_JAR = false;
    private static final boolean DEBUG_PARSER = false;
    private static final boolean IsForceHardwareAccelerated = false;
    private static final int MAX_PACKAGES_PER_APK = 5;
    private static final String MNT_EXPAND = "/mnt/expand/";
    private static final boolean MULTI_PACKAGE_APK_ENABLED = false;
    public static final NewPermissionInfo[] NEW_PERMISSIONS = null;
    public static final int PARSE_CHATTY = 2;
    public static final int PARSE_COLLECT_CERTIFICATES = 256;
    private static final int PARSE_DEFAULT_INSTALL_LOCATION = -1;
    public static final int PARSE_ENFORCE_CODE = 1024;
    public static final int PARSE_EXTERNAL_STORAGE = 32;
    public static final int PARSE_FORCE_SDK = 4096;
    public static final int PARSE_FORWARD_LOCK = 16;
    public static final int PARSE_IGNORE_PROCESSES = 8;
    public static final int PARSE_IS_EPHEMERAL = 2048;
    public static final int PARSE_IS_OPERATOR = 8192;
    public static final int PARSE_IS_PRIVILEGED = 128;
    public static final int PARSE_IS_SYSTEM = 1;
    public static final int PARSE_IS_SYSTEM_DIR = 64;
    public static final int PARSE_MUST_BE_APK = 4;
    public static final int PARSE_TRUSTED_OVERLAY = 512;
    private static final boolean RIGID_PARSER = false;
    private static final String[] SDK_CODENAMES = null;
    private static final int SDK_VERSION = 0;
    public static final SplitPermissionInfo[] SPLIT_PERMISSIONS = null;
    private static final String TAG = "PackageParser";
    private static final String TAG_ADOPT_PERMISSIONS = "adopt-permissions";
    private static final String TAG_APPLICATION = "application";
    private static final String TAG_COMPATIBLE_SCREENS = "compatible-screens";
    private static final String TAG_EAT_COMMENT = "eat-comment";
    private static final String TAG_FEATURE_GROUP = "feature-group";
    private static final String TAG_INSTRUMENTATION = "instrumentation";
    private static final String TAG_KEY_SETS = "key-sets";
    private static final String TAG_MANIFEST = "manifest";
    private static final String TAG_ORIGINAL_PACKAGE = "original-package";
    private static final String TAG_OVERLAY = "overlay";
    private static final String TAG_PACKAGE = "package";
    private static final String TAG_PERMISSION = "permission";
    private static final String TAG_PERMISSION_GROUP = "permission-group";
    private static final String TAG_PERMISSION_TREE = "permission-tree";
    private static final String TAG_PROTECTED_BROADCAST = "protected-broadcast";
    private static final String TAG_RESTRICT_UPDATE = "restrict-update";
    private static final String TAG_SUPPORTS_INPUT = "supports-input";
    private static final String TAG_SUPPORT_SCREENS = "supports-screens";
    private static final String TAG_USES_CONFIGURATION = "uses-configuration";
    private static final String TAG_USES_FEATURE = "uses-feature";
    private static final String TAG_USES_GL_TEXTURE = "uses-gl-texture";
    private static final String TAG_USES_PERMISSION = "uses-permission";
    private static final String TAG_USES_PERMISSION_SDK_23 = "uses-permission-sdk-23";
    private static final String TAG_USES_PERMISSION_SDK_M = "uses-permission-sdk-m";
    private static final String TAG_USES_SDK = "uses-sdk";
    private static AtomicReference<byte[]> sBuffer;
    private static boolean sCompatibilityModeEnabled;
    private static final Comparator<String> sSplitNameComparator = null;
    @Deprecated
    private String mArchiveSourcePath;
    private DisplayMetrics mMetrics;
    private boolean mOnlyCoreApps;
    private ParseComponentArgs mParseActivityAliasArgs;
    private ParseComponentArgs mParseActivityArgs;
    private int mParseError;
    private ParsePackageItemArgs mParseInstrumentationArgs;
    private ParseComponentArgs mParseProviderArgs;
    private ParseComponentArgs mParseServiceArgs;
    private String[] mSeparateProcesses;

    public static class Component<II extends IntentInfo> {
        public final String className;
        ComponentName componentName;
        String componentShortName;
        public final ArrayList<II> intents;
        public Bundle metaData;
        public final Package owner;

        public Component(Package _owner) {
            this.owner = _owner;
            this.intents = null;
            this.className = null;
        }

        public Component(ParsePackageItemArgs args, PackageItemInfo outInfo) {
            this.owner = args.owner;
            this.intents = new ArrayList(0);
            if (PackageParser.parsePackageItemInfo(args.owner, outInfo, args.outError, args.tag, args.sa, true, args.nameRes, args.labelRes, args.iconRes, args.roundIconRes, args.logoRes, args.bannerRes)) {
                this.className = outInfo.name;
            } else {
                this.className = null;
            }
        }

        public Component(ParseComponentArgs args, ComponentInfo outInfo) {
            this((ParsePackageItemArgs) args, (PackageItemInfo) outInfo);
            if (args.outError[0] == null) {
                if (args.processRes != 0) {
                    CharSequence pname;
                    if (this.owner.applicationInfo.targetSdkVersion >= 8) {
                        pname = args.sa.getNonConfigurationString(args.processRes, 1024);
                    } else {
                        pname = args.sa.getNonResourceString(args.processRes);
                    }
                    outInfo.processName = PackageParser.buildProcessName(this.owner.applicationInfo.packageName, this.owner.applicationInfo.processName, pname, args.flags, args.sepProcesses, args.outError);
                }
                if (args.descriptionRes != 0) {
                    outInfo.descriptionRes = args.sa.getResourceId(args.descriptionRes, 0);
                }
                outInfo.enabled = args.sa.getBoolean(args.enabledRes, true);
            }
        }

        public Component(Component<II> clone) {
            this.owner = clone.owner;
            this.intents = clone.intents;
            this.className = clone.className;
            this.componentName = clone.componentName;
            this.componentShortName = clone.componentShortName;
        }

        public ComponentName getComponentName() {
            if (this.componentName != null) {
                return this.componentName;
            }
            if (this.className != null) {
                this.componentName = new ComponentName(this.owner.applicationInfo.packageName, this.className);
            }
            return this.componentName;
        }

        public void appendComponentShortName(StringBuilder sb) {
            ComponentName.appendShortString(sb, this.owner.applicationInfo.packageName, this.className);
        }

        public void printComponentShortName(PrintWriter pw) {
            ComponentName.printShortString(pw, this.owner.applicationInfo.packageName, this.className);
        }

        public void setPackageName(String packageName) {
            this.componentName = null;
            this.componentShortName = null;
        }
    }

    public static final class Activity extends Component<ActivityIntentInfo> {
        public final ActivityInfo info;

        public Activity(ParseComponentArgs args, ActivityInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Activity{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class IntentInfo extends IntentFilter {
        public int banner;
        public boolean hasDefault;
        public int icon;
        public int labelRes;
        public int logo;
        public CharSequence nonLocalizedLabel;
        public int preferred;
    }

    public static final class ActivityIntentInfo extends IntentInfo {
        public final Activity activity;

        public ActivityIntentInfo(Activity _activity) {
            this.activity = _activity;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ActivityIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.activity.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static class ApkLite {
        public final Certificate[][] certificates;
        public final String codePath;
        public final boolean coreApp;
        public final boolean extractNativeLibs;
        public final int installLocation;
        public final boolean multiArch;
        public final String packageName;
        public final int revisionCode;
        public final Signature[] signatures;
        public final String splitName;
        public final boolean use32bitAbi;
        public final VerifierInfo[] verifiers;
        public final int versionCode;

        public ApkLite(String codePath, String packageName, String splitName, int versionCode, int revisionCode, int installLocation, List<VerifierInfo> verifiers, Signature[] signatures, Certificate[][] certificates, boolean coreApp, boolean multiArch, boolean use32bitAbi, boolean extractNativeLibs) {
            this.codePath = codePath;
            this.packageName = packageName;
            this.splitName = splitName;
            this.versionCode = versionCode;
            this.revisionCode = revisionCode;
            this.installLocation = installLocation;
            this.verifiers = (VerifierInfo[]) verifiers.toArray(new VerifierInfo[verifiers.size()]);
            this.signatures = signatures;
            this.certificates = certificates;
            this.coreApp = coreApp;
            this.multiArch = multiArch;
            this.use32bitAbi = use32bitAbi;
            this.extractNativeLibs = extractNativeLibs;
        }
    }

    public static final class Instrumentation extends Component<IntentInfo> {
        public final InstrumentationInfo info;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.content.pm.PackageParser.Instrumentation.<init>(android.content.pm.PackageParser$ParsePackageItemArgs, android.content.pm.InstrumentationInfo):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public Instrumentation(android.content.pm.PackageParser.ParsePackageItemArgs r1, android.content.pm.InstrumentationInfo r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.content.pm.PackageParser.Instrumentation.<init>(android.content.pm.PackageParser$ParsePackageItemArgs, android.content.pm.InstrumentationInfo):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.Instrumentation.<init>(android.content.pm.PackageParser$ParsePackageItemArgs, android.content.pm.InstrumentationInfo):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.content.pm.PackageParser.Instrumentation.setPackageName(java.lang.String):void, dex:  in method: android.content.pm.PackageParser.Instrumentation.setPackageName(java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.content.pm.PackageParser.Instrumentation.setPackageName(java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
            	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public void setPackageName(java.lang.String r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.content.pm.PackageParser.Instrumentation.setPackageName(java.lang.String):void, dex:  in method: android.content.pm.PackageParser.Instrumentation.setPackageName(java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.Instrumentation.setPackageName(java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.content.pm.PackageParser.Instrumentation.toString():java.lang.String, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        public java.lang.String toString() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.content.pm.PackageParser.Instrumentation.toString():java.lang.String, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.Instrumentation.toString():java.lang.String");
        }
    }

    private static class ManifestDigest {
        private static final String DIGEST_ALGORITHM = "SHA-256";
        private static final String TAG = "ManifestDigest";
        private final byte[] mDigest;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.content.pm.PackageParser.ManifestDigest.<init>(byte[]):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        ManifestDigest(byte[] r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.content.pm.PackageParser.ManifestDigest.<init>(byte[]):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.ManifestDigest.<init>(byte[]):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.content.pm.PackageParser.ManifestDigest.fromInputStream(java.io.InputStream):android.content.pm.PackageParser$ManifestDigest, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
            	... 10 more
            */
        static android.content.pm.PackageParser.ManifestDigest fromInputStream(java.io.InputStream r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.content.pm.PackageParser.ManifestDigest.fromInputStream(java.io.InputStream):android.content.pm.PackageParser$ManifestDigest, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.ManifestDigest.fromInputStream(java.io.InputStream):android.content.pm.PackageParser$ManifestDigest");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.content.pm.PackageParser.ManifestDigest.equals(java.lang.Object):boolean, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public boolean equals(java.lang.Object r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.content.pm.PackageParser.ManifestDigest.equals(java.lang.Object):boolean, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.ManifestDigest.equals(java.lang.Object):boolean");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus registerCount: f in method: android.content.pm.PackageParser.ManifestDigest.hashCode():int, dex:  in method: android.content.pm.PackageParser.ManifestDigest.hashCode():int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: bogus registerCount: f in method: android.content.pm.PackageParser.ManifestDigest.hashCode():int, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 10 more
            Caused by: com.android.dex.DexException: bogus registerCount: f
            	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:962)
            	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
            	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 11 more
            */
        public int hashCode() {
            /*
            // Can't load method instructions: Load method exception: bogus registerCount: f in method: android.content.pm.PackageParser.ManifestDigest.hashCode():int, dex:  in method: android.content.pm.PackageParser.ManifestDigest.hashCode():int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.ManifestDigest.hashCode():int");
        }
    }

    public static class NewPermissionInfo {
        public final int fileVersion;
        public final String name;
        public final int sdkVersion;

        public NewPermissionInfo(String name, int sdkVersion, int fileVersion) {
            this.name = name;
            this.sdkVersion = sdkVersion;
            this.fileVersion = fileVersion;
        }
    }

    public static final class Package {
        public final ArrayList<Activity> activities;
        public final ApplicationInfo applicationInfo;
        public String baseCodePath;
        public boolean baseHardwareAccelerated;
        public int baseRevisionCode;
        public ArrayList<Package> childPackages;
        public String codePath;
        public ArrayList<ConfigurationInfo> configPreferences;
        public boolean coreApp;
        public String cpuAbiOverride;
        public ArrayList<FeatureGroupInfo> featureGroups;
        public int installLocation;
        public final ArrayList<Instrumentation> instrumentation;
        public ArrayList<String> libraryNames;
        public ArrayList<String> mAdoptPermissions;
        public Bundle mAppMetaData;
        public Certificate[][] mCertificates;
        public Object mExtras;
        public ArrayMap<String, ArraySet<PublicKey>> mKeySetMapping;
        public long[] mLastPackageUsageTimeInMills;
        public ArrayList<String> mOriginalPackages;
        public int mOverlayPriority;
        public String mOverlayTarget;
        public int mPreferredOrder;
        public String mRealPackage;
        public String mRequiredAccountType;
        public boolean mRequiredForAllUsers;
        public String mRestrictedAccountType;
        public String mSharedUserId;
        public int mSharedUserLabel;
        public Signature[] mSignatures;
        public ArraySet<PublicKey> mSigningKeys;
        public boolean mTrustedOverlay;
        public ArraySet<String> mUpgradeKeySets;
        public int mVersionCode;
        public String mVersionName;
        public String packageName;
        public Package parentPackage;
        public final ArrayList<PermissionGroup> permissionGroups;
        public final ArrayList<Permission> permissions;
        public ArrayList<ActivityIntentInfo> preferredActivityFilters;
        public ArrayList<String> protectedBroadcasts;
        public final ArrayList<Provider> providers;
        public final ArrayList<Activity> receivers;
        public ArrayList<FeatureInfo> reqFeatures;
        public final ArrayList<String> requestedPermissions;
        public byte[] restrictUpdateHash;
        public final ArrayList<Service> services;
        public String[] splitCodePaths;
        public int[] splitFlags;
        public String[] splitNames;
        public int[] splitPrivateFlags;
        public int[] splitRevisionCodes;
        public boolean use32bitAbi;
        public ArrayList<String> usesLibraries;
        public String[] usesLibraryFiles;
        public ArrayList<String> usesOptionalLibraries;
        public String volumeUuid;

        public Package(String packageName) {
            this.applicationInfo = new ApplicationInfo();
            this.permissions = new ArrayList(0);
            this.permissionGroups = new ArrayList(0);
            this.activities = new ArrayList(0);
            this.receivers = new ArrayList(0);
            this.providers = new ArrayList(0);
            this.services = new ArrayList(0);
            this.instrumentation = new ArrayList(0);
            this.requestedPermissions = new ArrayList();
            this.libraryNames = null;
            this.usesLibraries = null;
            this.usesOptionalLibraries = null;
            this.usesLibraryFiles = null;
            this.preferredActivityFilters = null;
            this.mOriginalPackages = null;
            this.mRealPackage = null;
            this.mAdoptPermissions = null;
            this.mAppMetaData = null;
            this.mPreferredOrder = 0;
            this.mLastPackageUsageTimeInMills = new long[8];
            this.configPreferences = null;
            this.reqFeatures = null;
            this.featureGroups = null;
            this.packageName = packageName;
            this.applicationInfo.packageName = packageName;
            this.applicationInfo.uid = -1;
        }

        public void setApplicationVolumeUuid(String volumeUuid) {
            this.applicationInfo.volumeUuid = volumeUuid;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).applicationInfo.volumeUuid = volumeUuid;
                }
            }
        }

        public void setApplicationInfoCodePath(String codePath) {
            this.applicationInfo.setCodePath(codePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setCodePath(codePath);
                }
            }
        }

        public void setApplicationInfoResourcePath(String resourcePath) {
            this.applicationInfo.setResourcePath(resourcePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setResourcePath(resourcePath);
                }
            }
        }

        public void setApplicationInfoBaseResourcePath(String resourcePath) {
            this.applicationInfo.setBaseResourcePath(resourcePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setBaseResourcePath(resourcePath);
                }
            }
        }

        public void setApplicationInfoBaseCodePath(String baseCodePath) {
            this.applicationInfo.setBaseCodePath(baseCodePath);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).applicationInfo.setBaseCodePath(baseCodePath);
                }
            }
        }

        public boolean hasChildPackage(String packageName) {
            int childCount = this.childPackages != null ? this.childPackages.size() : 0;
            for (int i = 0; i < childCount; i++) {
                if (((Package) this.childPackages.get(i)).packageName.equals(packageName)) {
                    return true;
                }
            }
            return false;
        }

        public void setApplicationInfoSplitCodePaths(String[] splitCodePaths) {
            this.applicationInfo.setSplitCodePaths(splitCodePaths);
        }

        public void setApplicationInfoSplitResourcePaths(String[] resroucePaths) {
            this.applicationInfo.setSplitResourcePaths(resroucePaths);
        }

        public void setSplitCodePaths(String[] codePaths) {
            this.splitCodePaths = codePaths;
        }

        public void setCodePath(String codePath) {
            this.codePath = codePath;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).codePath = codePath;
                }
            }
        }

        public void setBaseCodePath(String baseCodePath) {
            this.baseCodePath = baseCodePath;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).baseCodePath = baseCodePath;
                }
            }
        }

        public void setSignatures(Signature[] signatures) {
            this.mSignatures = signatures;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).mSignatures = signatures;
                }
            }
        }

        public void setVolumeUuid(String volumeUuid) {
            this.volumeUuid = volumeUuid;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).volumeUuid = volumeUuid;
                }
            }
        }

        public void setApplicationInfoFlags(int mask, int flags) {
            this.applicationInfo.flags = (this.applicationInfo.flags & (~mask)) | (mask & flags);
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).applicationInfo.flags = (this.applicationInfo.flags & (~mask)) | (mask & flags);
                }
            }
        }

        public void setUse32bitAbi(boolean use32bitAbi) {
            this.use32bitAbi = use32bitAbi;
            if (this.childPackages != null) {
                int packageCount = this.childPackages.size();
                for (int i = 0; i < packageCount; i++) {
                    ((Package) this.childPackages.get(i)).use32bitAbi = use32bitAbi;
                }
            }
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }

        public List<String> getAllCodePathsExcludingResourceOnly() {
            ArrayList<String> paths = new ArrayList();
            if ((this.applicationInfo.flags & 4) != 0) {
                paths.add(this.baseCodePath);
            }
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                for (int i = 0; i < this.splitCodePaths.length; i++) {
                    if ((this.splitFlags[i] & 4) != 0) {
                        paths.add(this.splitCodePaths[i]);
                    }
                }
            }
            return paths;
        }

        public void setPackageName(String newName) {
            int i;
            this.packageName = newName;
            this.applicationInfo.packageName = newName;
            for (i = this.permissions.size() - 1; i >= 0; i--) {
                ((Permission) this.permissions.get(i)).setPackageName(newName);
            }
            for (i = this.permissionGroups.size() - 1; i >= 0; i--) {
                ((PermissionGroup) this.permissionGroups.get(i)).setPackageName(newName);
            }
            for (i = this.activities.size() - 1; i >= 0; i--) {
                ((Activity) this.activities.get(i)).setPackageName(newName);
            }
            for (i = this.receivers.size() - 1; i >= 0; i--) {
                ((Activity) this.receivers.get(i)).setPackageName(newName);
            }
            for (i = this.providers.size() - 1; i >= 0; i--) {
                ((Provider) this.providers.get(i)).setPackageName(newName);
            }
            for (i = this.services.size() - 1; i >= 0; i--) {
                ((Service) this.services.get(i)).setPackageName(newName);
            }
            for (i = this.instrumentation.size() - 1; i >= 0; i--) {
                ((Instrumentation) this.instrumentation.get(i)).setPackageName(newName);
            }
        }

        public boolean hasComponentClassName(String name) {
            int i;
            for (i = this.activities.size() - 1; i >= 0; i--) {
                if (name.equals(((Activity) this.activities.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.receivers.size() - 1; i >= 0; i--) {
                if (name.equals(((Activity) this.receivers.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.providers.size() - 1; i >= 0; i--) {
                if (name.equals(((Provider) this.providers.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.services.size() - 1; i >= 0; i--) {
                if (name.equals(((Service) this.services.get(i)).className)) {
                    return true;
                }
            }
            for (i = this.instrumentation.size() - 1; i >= 0; i--) {
                if (name.equals(((Instrumentation) this.instrumentation.get(i)).className)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isForwardLocked() {
            return this.applicationInfo.isForwardLocked();
        }

        public boolean isSystemApp() {
            return this.applicationInfo.isSystemApp();
        }

        public boolean isPrivilegedApp() {
            return this.applicationInfo.isPrivilegedApp();
        }

        public boolean isUpdatedSystemApp() {
            return this.applicationInfo.isUpdatedSystemApp();
        }

        public boolean canHaveOatDir() {
            boolean isVendorApps = this.applicationInfo.isVendorApp();
            if (isSystemApp()) {
                isVendorApps = true;
            }
            if ((isVendorApps && !isUpdatedSystemApp()) || isForwardLocked() || this.applicationInfo.isExternalAsec()) {
                return false;
            }
            return true;
        }

        public boolean isMatch(int flags) {
            if ((1048576 & flags) != 0) {
                return isSystemApp();
            }
            return true;
        }

        public long getLatestPackageUseTimeInMills() {
            long latestUse = 0;
            for (long use : this.mLastPackageUsageTimeInMills) {
                latestUse = Math.max(latestUse, use);
            }
            return latestUse;
        }

        public long getLatestForegroundPackageUseTimeInMills() {
            int i = 0;
            int[] foregroundReasons = new int[]{0, 2};
            long latestUse = 0;
            int length = foregroundReasons.length;
            while (i < length) {
                latestUse = Math.max(latestUse, this.mLastPackageUsageTimeInMills[foregroundReasons[i]]);
                i++;
            }
            return latestUse;
        }

        public String toString() {
            return "Package{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.packageName + "}";
        }
    }

    public static class PackageLite {
        public final String baseCodePath;
        public final int baseRevisionCode;
        public final String codePath;
        public final boolean coreApp;
        public final boolean extractNativeLibs;
        public final int installLocation;
        public final boolean multiArch;
        public final String packageName;
        public final String[] splitCodePaths;
        public final String[] splitNames;
        public final int[] splitRevisionCodes;
        public final boolean use32bitAbi;
        public final VerifierInfo[] verifiers;
        public final int versionCode;

        public PackageLite(String codePath, ApkLite baseApk, String[] splitNames, String[] splitCodePaths, int[] splitRevisionCodes) {
            this.packageName = baseApk.packageName;
            this.versionCode = baseApk.versionCode;
            this.installLocation = baseApk.installLocation;
            this.verifiers = baseApk.verifiers;
            this.splitNames = splitNames;
            this.codePath = codePath;
            this.baseCodePath = baseApk.codePath;
            this.splitCodePaths = splitCodePaths;
            this.baseRevisionCode = baseApk.revisionCode;
            this.splitRevisionCodes = splitRevisionCodes;
            this.coreApp = baseApk.coreApp;
            this.multiArch = baseApk.multiArch;
            this.use32bitAbi = baseApk.use32bitAbi;
            this.extractNativeLibs = baseApk.extractNativeLibs;
        }

        public List<String> getAllCodePaths() {
            ArrayList<String> paths = new ArrayList();
            paths.add(this.baseCodePath);
            if (!ArrayUtils.isEmpty(this.splitCodePaths)) {
                Collections.addAll(paths, this.splitCodePaths);
            }
            return paths;
        }
    }

    public static class PackageParserException extends Exception {
        public final int error;

        public PackageParserException(int error, String detailMessage) {
            super(detailMessage);
            this.error = error;
        }

        public PackageParserException(int error, String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
            this.error = error;
        }
    }

    static class ParsePackageItemArgs {
        final int bannerRes;
        final int iconRes;
        final int labelRes;
        final int logoRes;
        final int nameRes;
        final String[] outError;
        final Package owner;
        final int roundIconRes;
        TypedArray sa;
        String tag;

        ParsePackageItemArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _roundIconRes, int _logoRes, int _bannerRes) {
            this.owner = _owner;
            this.outError = _outError;
            this.nameRes = _nameRes;
            this.labelRes = _labelRes;
            this.iconRes = _iconRes;
            this.logoRes = _logoRes;
            this.bannerRes = _bannerRes;
            this.roundIconRes = _roundIconRes;
        }
    }

    static class ParseComponentArgs extends ParsePackageItemArgs {
        final int descriptionRes;
        final int enabledRes;
        int flags;
        final int processRes;
        final String[] sepProcesses;

        ParseComponentArgs(Package _owner, String[] _outError, int _nameRes, int _labelRes, int _iconRes, int _roundIconRes, int _logoRes, int _bannerRes, String[] _sepProcesses, int _processRes, int _descriptionRes, int _enabledRes) {
            super(_owner, _outError, _nameRes, _labelRes, _iconRes, _roundIconRes, _logoRes, _bannerRes);
            this.sepProcesses = _sepProcesses;
            this.processRes = _processRes;
            this.descriptionRes = _descriptionRes;
            this.enabledRes = _enabledRes;
        }
    }

    public static final class Permission extends Component<IntentInfo> {
        public PermissionGroup group;
        public final PermissionInfo info;
        public boolean tree;

        public Permission(Package _owner) {
            super(_owner);
            this.info = new PermissionInfo();
        }

        public Permission(Package _owner, PermissionInfo _info) {
            super(_owner);
            this.info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "Permission{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.info.name + "}";
        }
    }

    public static final class PermissionGroup extends Component<IntentInfo> {
        public final PermissionGroupInfo info;

        public PermissionGroup(Package _owner) {
            super(_owner);
            this.info = new PermissionGroupInfo();
        }

        public PermissionGroup(Package _owner, PermissionGroupInfo _info) {
            super(_owner);
            this.info = _info;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            return "PermissionGroup{" + Integer.toHexString(System.identityHashCode(this)) + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + this.info.name + "}";
        }
    }

    public static final class Provider extends Component<ProviderIntentInfo> {
        public final ProviderInfo info;
        public boolean syncable;

        public Provider(ParseComponentArgs args, ProviderInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
            this.syncable = false;
        }

        public Provider(Provider existingProvider) {
            super((Component) existingProvider);
            this.info = existingProvider.info;
            this.syncable = existingProvider.syncable;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Provider{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ProviderIntentInfo extends IntentInfo {
        public final Provider provider;

        public ProviderIntentInfo(Provider provider) {
            this.provider = provider;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ProviderIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.provider.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class Service extends Component<ServiceIntentInfo> {
        public final ServiceInfo info;

        public Service(ParseComponentArgs args, ServiceInfo _info) {
            super(args, (ComponentInfo) _info);
            this.info = _info;
            this.info.applicationInfo = args.owner.applicationInfo;
        }

        public void setPackageName(String packageName) {
            super.setPackageName(packageName);
            this.info.packageName = packageName;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("Service{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    public static final class ServiceIntentInfo extends IntentInfo {
        public final Service service;

        public ServiceIntentInfo(Service _service) {
            this.service = _service;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(128);
            sb.append("ServiceIntentInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(' ');
            this.service.appendComponentShortName(sb);
            sb.append('}');
            return sb.toString();
        }
    }

    private static class SplitNameComparator implements Comparator<String> {
        /* synthetic */ SplitNameComparator(SplitNameComparator splitNameComparator) {
            this();
        }

        private SplitNameComparator() {
        }

        public int compare(String lhs, String rhs) {
            if (lhs == null) {
                return -1;
            }
            if (rhs == null) {
                return 1;
            }
            return lhs.compareTo(rhs);
        }
    }

    public static class SplitPermissionInfo {
        public final String[] newPerms;
        public final String rootPerm;
        public final int targetSdk;

        public SplitPermissionInfo(String rootPerm, String[] newPerms, int targetSdk) {
            this.rootPerm = rootPerm;
            this.newPerms = newPerms;
            this.targetSdk = targetSdk;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.content.pm.PackageParser.<clinit>():void, dex: 
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
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.content.pm.PackageParser.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.content.pm.PackageParser.<clinit>():void");
    }

    public PackageParser() {
        this.mParseError = 1;
        this.mMetrics = new DisplayMetrics();
        this.mMetrics.setToDefaults();
    }

    public void setSeparateProcesses(String[] procs) {
        this.mSeparateProcesses = procs;
    }

    public void setOnlyCoreApps(boolean onlyCoreApps) {
        this.mOnlyCoreApps = onlyCoreApps;
    }

    public void setDisplayMetrics(DisplayMetrics metrics) {
        this.mMetrics = metrics;
    }

    public static final boolean isApkFile(File file) {
        return isApkPath(file.getName());
    }

    private static boolean isApkPath(String path) {
        return path.endsWith(".apk");
    }

    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state) {
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime, grantedPermissions, state, UserHandle.getCallingUserId());
    }

    private static boolean checkUseInstalledOrHidden(int flags, PackageUserState state) {
        return (state.installed && !state.hidden) || (flags & 8192) != 0;
    }

    public static boolean isAvailable(PackageUserState state) {
        return checkUseInstalledOrHidden(0, state);
    }

    public static PackageInfo generatePackageInfo(Package p, int[] gids, int flags, long firstInstallTime, long lastUpdateTime, Set<String> grantedPermissions, PackageUserState state, int userId) {
        if (!checkUseInstalledOrHidden(flags, state) || !p.isMatch(flags)) {
            return null;
        }
        int N;
        ActivityInfo[] res;
        int i;
        int num;
        Activity a;
        int num2;
        PackageInfo pi = new PackageInfo();
        pi.packageName = p.packageName;
        pi.splitNames = p.splitNames;
        pi.versionCode = p.mVersionCode;
        pi.baseRevisionCode = p.baseRevisionCode;
        pi.splitRevisionCodes = p.splitRevisionCodes;
        pi.versionName = p.mVersionName;
        pi.sharedUserId = p.mSharedUserId;
        pi.sharedUserLabel = p.mSharedUserLabel;
        pi.applicationInfo = generateApplicationInfo(p, flags, state, userId);
        pi.installLocation = p.installLocation;
        pi.coreApp = p.coreApp;
        if (!((pi.applicationInfo == null || (pi.applicationInfo.flags & 1) == 0) && (pi.applicationInfo.flags & 128) == 0)) {
            pi.requiredForAllUsers = p.mRequiredForAllUsers;
        }
        pi.restrictedAccountType = p.mRestrictedAccountType;
        pi.requiredAccountType = p.mRequiredAccountType;
        pi.overlayTarget = p.mOverlayTarget;
        pi.firstInstallTime = firstInstallTime;
        pi.lastUpdateTime = lastUpdateTime;
        if ((flags & 256) != 0) {
            pi.gids = gids;
        }
        if ((flags & 16384) != 0) {
            N = p.configPreferences != null ? p.configPreferences.size() : 0;
            if (N > 0) {
                pi.configPreferences = new ConfigurationInfo[N];
                p.configPreferences.toArray(pi.configPreferences);
            }
            N = p.reqFeatures != null ? p.reqFeatures.size() : 0;
            if (N > 0) {
                pi.reqFeatures = new FeatureInfo[N];
                p.reqFeatures.toArray(pi.reqFeatures);
            }
            N = p.featureGroups != null ? p.featureGroups.size() : 0;
            if (N > 0) {
                pi.featureGroups = new FeatureGroupInfo[N];
                p.featureGroups.toArray(pi.featureGroups);
            }
        }
        if ((flags & 1) != 0) {
            N = p.activities.size();
            if (N > 0) {
                res = new ActivityInfo[N];
                i = 0;
                num = 0;
                while (i < N) {
                    a = (Activity) p.activities.get(i);
                    if (state.isMatch(a.info, flags)) {
                        num2 = num + 1;
                        res[num] = generateActivityInfo(a, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i++;
                    num = num2;
                }
                pi.activities = (ActivityInfo[]) ArrayUtils.trimToSize(res, num);
            }
        }
        if ((flags & 2) != 0) {
            N = p.receivers.size();
            if (N > 0) {
                res = new ActivityInfo[N];
                i = 0;
                num = 0;
                while (i < N) {
                    a = (Activity) p.receivers.get(i);
                    if (state.isMatch(a.info, flags)) {
                        num2 = num + 1;
                        res[num] = generateActivityInfo(a, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i++;
                    num = num2;
                }
                pi.receivers = (ActivityInfo[]) ArrayUtils.trimToSize(res, num);
            }
        }
        if ((flags & 4) != 0) {
            N = p.services.size();
            if (N > 0) {
                ServiceInfo[] res2 = new ServiceInfo[N];
                i = 0;
                num = 0;
                while (i < N) {
                    Service s = (Service) p.services.get(i);
                    if (state.isMatch(s.info, flags)) {
                        num2 = num + 1;
                        res2[num] = generateServiceInfo(s, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i++;
                    num = num2;
                }
                pi.services = (ServiceInfo[]) ArrayUtils.trimToSize(res2, num);
            }
        }
        if ((flags & 8) != 0) {
            N = p.providers.size();
            if (N > 0) {
                ProviderInfo[] res3 = new ProviderInfo[N];
                i = 0;
                num = 0;
                while (i < N) {
                    Provider pr = (Provider) p.providers.get(i);
                    if (state.isMatch(pr.info, flags)) {
                        num2 = num + 1;
                        res3[num] = generateProviderInfo(pr, flags, state, userId);
                    } else {
                        num2 = num;
                    }
                    i++;
                    num = num2;
                }
                pi.providers = (ProviderInfo[]) ArrayUtils.trimToSize(res3, num);
            }
        }
        if ((flags & 16) != 0) {
            N = p.instrumentation.size();
            if (N > 0) {
                pi.instrumentation = new InstrumentationInfo[N];
                for (i = 0; i < N; i++) {
                    pi.instrumentation[i] = generateInstrumentationInfo((Instrumentation) p.instrumentation.get(i), flags);
                }
            }
        }
        if ((flags & 4096) != 0) {
            N = p.permissions.size();
            if (N > 0) {
                pi.permissions = new PermissionInfo[N];
                for (i = 0; i < N; i++) {
                    pi.permissions[i] = generatePermissionInfo((Permission) p.permissions.get(i), flags);
                }
            }
            N = p.requestedPermissions.size();
            if (N > 0) {
                pi.requestedPermissions = new String[N];
                pi.requestedPermissionsFlags = new int[N];
                for (i = 0; i < N; i++) {
                    String perm = (String) p.requestedPermissions.get(i);
                    pi.requestedPermissions[i] = perm;
                    int[] iArr = pi.requestedPermissionsFlags;
                    iArr[i] = iArr[i] | 1;
                    if (grantedPermissions != null && grantedPermissions.contains(perm)) {
                        iArr = pi.requestedPermissionsFlags;
                        iArr[i] = iArr[i] | 2;
                    }
                }
            }
        }
        if ((flags & 64) != 0) {
            N = p.mSignatures != null ? p.mSignatures.length : 0;
            if (N > 0) {
                pi.signatures = new Signature[N];
                System.arraycopy(p.mSignatures, 0, pi.signatures, 0, N);
            }
        }
        return pi;
    }

    /* JADX WARNING: Removed duplicated region for block: B:5:0x0010 A:{ExcHandler: java.io.IOException (r0_0 'e' java.lang.Exception), Splitter: B:1:0x0001, PHI: r1 } */
    /* JADX WARNING: Missing block: B:5:0x0010, code:
            r0 = move-exception;
     */
    /* JADX WARNING: Missing block: B:8:0x003b, code:
            throw new android.content.pm.PackageParser.PackageParserException(android.content.pm.PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed reading " + r6.getName() + " in " + r5, r0);
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static Certificate[][] loadCertificates(StrictJarFile jarFile, ZipEntry entry) throws PackageParserException {
        AutoCloseable is = null;
        try {
            is = jarFile.getInputStream(entry);
            readFullyIgnoringContents(is);
            Certificate[][] certificateChains = jarFile.getCertificateChains(entry);
            IoUtils.closeQuietly(is);
            return certificateChains;
        } catch (Exception e) {
        } catch (Throwable th) {
            IoUtils.closeQuietly(is);
        }
    }

    public static PackageLite parsePackageLite(File packageFile, int flags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackageLite(packageFile, flags);
        }
        return parseMonolithicPackageLite(packageFile, flags);
    }

    private static PackageLite parseMonolithicPackageLite(File packageFile, int flags) throws PackageParserException {
        return new PackageLite(packageFile.getAbsolutePath(), parseApkLite(packageFile, flags), null, null, null);
    }

    private static PackageLite parseClusterPackageLite(File packageDir, int flags) throws PackageParserException {
        File[] files = packageDir.listFiles();
        if (ArrayUtils.isEmpty(files)) {
            throw new PackageParserException(-100, "No packages found in split");
        }
        String packageName = null;
        int versionCode = 0;
        ArrayMap<String, ApkLite> apks = new ArrayMap();
        for (File file : files) {
            if (isApkFile(file)) {
                ApkLite lite = parseApkLite(file, flags);
                if (packageName == null) {
                    packageName = lite.packageName;
                    versionCode = lite.versionCode;
                } else if (!packageName.equals(lite.packageName)) {
                    throw new PackageParserException(-101, "Inconsistent package " + lite.packageName + " in " + file + "; expected " + packageName);
                } else if (versionCode != lite.versionCode) {
                    throw new PackageParserException(-101, "Inconsistent version " + lite.versionCode + " in " + file + "; expected " + versionCode);
                }
                if (apks.put(lite.splitName, lite) != null) {
                    throw new PackageParserException(-101, "Split name " + lite.splitName + " defined more than once; most recent was " + file);
                }
            }
        }
        ApkLite baseApk = (ApkLite) apks.remove(null);
        if (baseApk == null) {
            throw new PackageParserException(-101, "Missing base APK in " + packageDir);
        }
        int size = apks.size();
        String[] splitNames = null;
        String[] splitCodePaths = null;
        int[] splitRevisionCodes = null;
        if (size > 0) {
            splitCodePaths = new String[size];
            splitRevisionCodes = new int[size];
            splitNames = (String[]) apks.keySet().toArray(new String[size]);
            Arrays.sort(splitNames, sSplitNameComparator);
            for (int i = 0; i < size; i++) {
                splitCodePaths[i] = ((ApkLite) apks.get(splitNames[i])).codePath;
                splitRevisionCodes[i] = ((ApkLite) apks.get(splitNames[i])).revisionCode;
            }
        }
        return new PackageLite(packageDir.getAbsolutePath(), baseApk, splitNames, splitCodePaths, splitRevisionCodes);
    }

    public Package parsePackage(File packageFile, int flags) throws PackageParserException {
        if (packageFile.isDirectory()) {
            return parseClusterPackage(packageFile, flags);
        }
        return parseMonolithicPackage(packageFile, flags);
    }

    private Package parseClusterPackage(File packageDir, int flags) throws PackageParserException {
        int i = 0;
        PackageLite lite = parseClusterPackageLite(packageDir, 0);
        if (!this.mOnlyCoreApps || lite.coreApp) {
            AssetManager assets = new AssetManager();
            try {
                loadApkIntoAssetManager(assets, lite.baseCodePath, flags);
                if (!ArrayUtils.isEmpty(lite.splitCodePaths)) {
                    String[] strArr = lite.splitCodePaths;
                    int length = strArr.length;
                    while (i < length) {
                        loadApkIntoAssetManager(assets, strArr[i], flags);
                        i++;
                    }
                }
                File baseApk = new File(lite.baseCodePath);
                Package pkg = parseBaseApk(baseApk, assets, flags);
                if (pkg == null) {
                    throw new PackageParserException(-100, "Failed to parse base APK: " + baseApk);
                }
                if (!ArrayUtils.isEmpty(lite.splitNames)) {
                    int num = lite.splitNames.length;
                    pkg.splitNames = lite.splitNames;
                    pkg.splitCodePaths = lite.splitCodePaths;
                    pkg.splitRevisionCodes = lite.splitRevisionCodes;
                    pkg.splitFlags = new int[num];
                    pkg.splitPrivateFlags = new int[num];
                    for (int i2 = 0; i2 < num; i2++) {
                        parseSplitApk(pkg, i2, assets, flags);
                    }
                }
                pkg.setCodePath(packageDir.getAbsolutePath());
                pkg.setUse32bitAbi(lite.use32bitAbi);
                return pkg;
            } finally {
                IoUtils.closeQuietly(assets);
            }
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + packageDir);
        }
    }

    @Deprecated
    public Package parseMonolithicPackage(File apkFile, int flags) throws PackageParserException {
        PackageLite lite = parseMonolithicPackageLite(apkFile, flags);
        if (!this.mOnlyCoreApps || lite.coreApp) {
            AssetManager assets = new AssetManager();
            try {
                Package pkg = parseBaseApk(apkFile, assets, flags);
                pkg.setCodePath(apkFile.getAbsolutePath());
                pkg.setUse32bitAbi(lite.use32bitAbi);
                return pkg;
            } finally {
                IoUtils.closeQuietly(assets);
            }
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Not a coreApp: " + apkFile);
        }
    }

    private static int loadApkIntoAssetManager(AssetManager assets, String apkPath, int flags) throws PackageParserException {
        if ((flags & 4) == 0 || isApkPath(apkPath)) {
            int cookie = assets.addAssetPath(apkPath);
            if (cookie != 0) {
                return cookie;
            }
            throw new PackageParserException(-101, "Failed adding asset path: " + apkPath);
        }
        throw new PackageParserException(-100, "Invalid package file: " + apkPath);
    }

    private Package parseBaseApk(File apkFile, AssetManager assets, int flags) throws PackageParserException {
        PackageParserException e;
        Resources resources;
        Throwable th;
        Throwable e2;
        String apkPath = apkFile.getAbsolutePath();
        String volumeUuid = null;
        if (apkPath.startsWith(MNT_EXPAND)) {
            int end = apkPath.indexOf(47, MNT_EXPAND.length());
            volumeUuid = apkPath.substring(MNT_EXPAND.length(), end);
        }
        this.mParseError = 1;
        this.mArchiveSourcePath = apkFile.getAbsolutePath();
        int cookie = loadApkIntoAssetManager(assets, apkPath, flags);
        XmlResourceParser parser = null;
        try {
            Resources resources2 = new Resources(assets, this.mMetrics, null);
            try {
                assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, VERSION.RESOURCES_SDK_INT);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                String[] outError = new String[1];
                Package pkg = parseBaseApk(resources2, parser, flags, outError);
                if (pkg == null) {
                    throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
                }
                pkg.setVolumeUuid(volumeUuid);
                pkg.setApplicationVolumeUuid(volumeUuid);
                pkg.setBaseCodePath(apkPath);
                pkg.setSignatures(null);
                IoUtils.closeQuietly(parser);
                return pkg;
            } catch (PackageParserException e3) {
                e = e3;
                resources = resources2;
                try {
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e4) {
                e2 = e4;
                resources = resources2;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
            } catch (Throwable th3) {
                th = th3;
                resources = resources2;
                IoUtils.closeQuietly(parser);
                throw th;
            }
        } catch (PackageParserException e5) {
            e = e5;
            throw e;
        } catch (Exception e6) {
            e2 = e6;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
        }
    }

    private void parseSplitApk(Package pkg, int splitIndex, AssetManager assets, int flags) throws PackageParserException {
        XmlResourceParser parser;
        PackageParserException e;
        Resources res;
        Throwable th;
        Throwable e2;
        String apkPath = pkg.splitCodePaths[splitIndex];
        this.mParseError = 1;
        this.mArchiveSourcePath = apkPath;
        int cookie = loadApkIntoAssetManager(assets, apkPath, flags);
        try {
            Resources resources = new Resources(assets, this.mMetrics, null);
            try {
                assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, VERSION.RESOURCES_SDK_INT);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
            } catch (PackageParserException e3) {
                e = e3;
                parser = null;
                res = resources;
                try {
                    throw e;
                } catch (Throwable th2) {
                    th = th2;
                }
            } catch (Exception e4) {
                e2 = e4;
                parser = null;
                res = resources;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
            } catch (Throwable th3) {
                th = th3;
                parser = null;
                res = resources;
                IoUtils.closeQuietly(parser);
                throw th;
            }
            try {
                String[] outError = new String[1];
                if (parseSplitApk(pkg, resources, parser, flags, splitIndex, outError) == null) {
                    throw new PackageParserException(this.mParseError, apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
                }
                IoUtils.closeQuietly(parser);
            } catch (PackageParserException e5) {
                e = e5;
                res = resources;
                throw e;
            } catch (Exception e6) {
                e2 = e6;
                res = resources;
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
            } catch (Throwable th4) {
                th = th4;
                res = resources;
                IoUtils.closeQuietly(parser);
                throw th;
            }
        } catch (PackageParserException e7) {
            e = e7;
            parser = null;
            throw e;
        } catch (Exception e8) {
            e2 = e8;
            parser = null;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to read manifest from " + apkPath, e2);
        } catch (Throwable th5) {
            th = th5;
            parser = null;
            IoUtils.closeQuietly(parser);
            throw th;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:21:0x0090  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Package parseSplitApk(Package pkg, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException, PackageParserException {
        XmlResourceParser attrs = parser;
        parsePackageSplitNames(parser, parser);
        this.mParseInstrumentationArgs = null;
        this.mParseActivityArgs = null;
        this.mParseServiceArgs = null;
        this.mParseProviderArgs = null;
        boolean foundApp = false;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!foundApp) {
                    outError[0] = "<manifest> does not contain an <application>";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
                }
            } else if (!(type == 3 || type == 4)) {
                if (!parser.getName().equals(TAG_APPLICATION)) {
                    Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                } else if (foundApp) {
                    Slog.w(TAG, "<manifest> has more than one <application>");
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    foundApp = true;
                    if (!parseSplitApplication(pkg, res, parser, flags, splitIndex, outError)) {
                        return null;
                    }
                }
            }
        }
        if (foundApp) {
        }
        return pkg;
    }

    public static int getApkSigningVersion(Package pkg) {
        try {
            if (ApkSignatureSchemeV2Verifier.hasSignature(pkg.baseCodePath)) {
                return 2;
            }
            return 1;
        } catch (IOException e) {
            return 0;
        }
    }

    public static void populateCertificates(Package pkg, Certificate[][] certificates) throws PackageParserException {
        pkg.mCertificates = null;
        pkg.mSignatures = null;
        pkg.mSigningKeys = null;
        pkg.mCertificates = certificates;
        try {
            int i;
            pkg.mSignatures = convertToSignatures(certificates);
            pkg.mSigningKeys = new ArraySet(certificates.length);
            for (Certificate[] signerCerts : certificates) {
                pkg.mSigningKeys.add(signerCerts[0].getPublicKey());
            }
            int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
            for (i = 0; i < childCount; i++) {
                Package childPkg = (Package) pkg.childPackages.get(i);
                childPkg.mCertificates = pkg.mCertificates;
                childPkg.mSignatures = pkg.mSignatures;
                childPkg.mSigningKeys = pkg.mSigningKeys;
            }
        } catch (CertificateEncodingException e) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + pkg.baseCodePath, e);
        }
    }

    public static void collectCertificates(Package pkg, int parseFlags) throws PackageParserException {
        collectCertificatesInternal(pkg, parseFlags);
        int childCount = pkg.childPackages != null ? pkg.childPackages.size() : 0;
        for (int i = 0; i < childCount; i++) {
            Package childPkg = (Package) pkg.childPackages.get(i);
            childPkg.mCertificates = pkg.mCertificates;
            childPkg.mSignatures = pkg.mSignatures;
            childPkg.mSigningKeys = pkg.mSigningKeys;
        }
    }

    private static void collectCertificatesInternal(Package pkg, int parseFlags) throws PackageParserException {
        pkg.mCertificates = null;
        pkg.mSignatures = null;
        pkg.mSigningKeys = null;
        Trace.traceBegin(1048576, "collectCertificates");
        try {
            collectCertificates(pkg, new File(pkg.baseCodePath), parseFlags);
            if (!ArrayUtils.isEmpty(pkg.splitCodePaths)) {
                for (String file : pkg.splitCodePaths) {
                    collectCertificates(pkg, new File(file), parseFlags);
                }
            }
            Trace.traceEnd(1048576);
        } catch (Throwable th) {
            Trace.traceEnd(1048576);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:105:0x0352 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:29:0x0136} */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x024c A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:43:0x017f} */
    /* JADX WARNING: Missing block: B:76:0x024c, code:
            r7 = e;
     */
    /* JADX WARNING: Missing block: B:77:0x024d, code:
            r16 = r0;
     */
    /* JADX WARNING: Missing block: B:80:0x0272, code:
            throw new android.content.pm.PackageParser.PackageParserException(android.content.pm.PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + r5, r7);
     */
    /* JADX WARNING: Missing block: B:105:0x0352, code:
            r7 = e;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void collectCertificates(Package pkg, File apkFile, int parseFlags) throws PackageParserException {
        GeneralSecurityException e;
        Throwable th;
        String apkPath = apkFile.getAbsolutePath();
        boolean verified = false;
        Certificate[][] certificateArr = null;
        Signature[] signatureArr = null;
        try {
            Trace.traceBegin(1048576, "verifyV2");
            certificateArr = ApkSignatureSchemeV2Verifier.verify(apkPath);
            signatureArr = convertToSignatures(certificateArr);
            verified = true;
        } catch (SignatureNotFoundException e2) {
        } catch (Exception e3) {
            if (pkg.applicationInfo.targetSdkVersion == 0 || pkg.applicationInfo.targetSdkVersion >= 24) {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v2", e3);
            }
            Slog.d(TAG, "INSTALLATION WARNNING!!!", new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Failed to collect certificates from " + apkPath + " using APK Signature Scheme v2", e3));
        } finally {
            Trace.traceEnd(1048576);
        }
        if (verified) {
            if (pkg.mCertificates == null) {
                pkg.mCertificates = certificateArr;
                pkg.mSignatures = signatureArr;
                pkg.mSigningKeys = new ArraySet(certificateArr.length);
                for (Certificate[] signerCerts : certificateArr) {
                    pkg.mSigningKeys.add(signerCerts[0].getPublicKey());
                }
            } else if (!Signature.areExactMatch(pkg.mSignatures, signatureArr)) {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, apkPath + " has mismatched certificates");
            }
        }
        StrictJarFile jarFile = null;
        try {
            Trace.traceBegin(1048576, "strictJarFileCtor");
            boolean signatureSchemeRollbackProtectionsEnforced = (parseFlags & 64) == 0 ? pkg.applicationInfo.targetSdkVersion != 0 ? pkg.applicationInfo.targetSdkVersion >= 24 : true : false;
            if (!signatureSchemeRollbackProtectionsEnforced) {
            }
            StrictJarFile strictJarFile = new StrictJarFile(apkPath, !verified, signatureSchemeRollbackProtectionsEnforced);
            try {
                Trace.traceEnd(1048576);
                ZipEntry manifestEntry = strictJarFile.findEntry(ANDROID_MANIFEST_FILENAME);
                if (manifestEntry == null) {
                    throw new PackageParserException(-101, "Package " + apkPath + " has no manifest");
                } else if (verified) {
                    closeQuietly(strictJarFile);
                } else {
                    ZipEntry entry;
                    Trace.traceBegin(1048576, "verifyV1");
                    List<ZipEntry> toVerify = new ArrayList();
                    toVerify.add(manifestEntry);
                    if ((parseFlags & 64) == 0) {
                        Iterator<ZipEntry> i = strictJarFile.iterator();
                        while (i.hasNext()) {
                            entry = (ZipEntry) i.next();
                            if (!entry.isDirectory()) {
                                String entryName = entry.getName();
                                if (!(entryName.startsWith("META-INF/") || entryName.equals(ANDROID_MANIFEST_FILENAME))) {
                                    toVerify.add(entry);
                                }
                            }
                        }
                    }
                    for (ZipEntry entry2 : toVerify) {
                        Certificate[][] entryCerts = loadCertificates(strictJarFile, entry2);
                        if (ArrayUtils.isEmpty(entryCerts)) {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_NO_CERTIFICATES, "Package " + apkPath + " has no certificates at entry " + entry2.getName());
                        }
                        Signature[] entrySignatures = convertToSignatures(entryCerts);
                        if (pkg.mCertificates == null) {
                            pkg.mCertificates = entryCerts;
                            pkg.mSignatures = entrySignatures;
                            pkg.mSigningKeys = new ArraySet();
                            for (Certificate[] certificateArr2 : entryCerts) {
                                pkg.mSigningKeys.add(certificateArr2[0].getPublicKey());
                            }
                        } else if (!Signature.areExactMatch(pkg.mSignatures, entrySignatures)) {
                            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_INCONSISTENT_CERTIFICATES, "Package " + apkPath + " has mismatched certificates at entry " + entry2.getName());
                        }
                    }
                    Trace.traceEnd(1048576);
                    closeQuietly(strictJarFile);
                }
            } catch (GeneralSecurityException e4) {
                e = e4;
                jarFile = strictJarFile;
                try {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e);
                } catch (Throwable th2) {
                    th = th2;
                    closeQuietly(jarFile);
                    throw th;
                }
            } catch (IOException e5) {
            } catch (Throwable th3) {
                th = th3;
                jarFile = strictJarFile;
                closeQuietly(jarFile);
                throw th;
            }
        } catch (GeneralSecurityException e6) {
            e = e6;
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_CERTIFICATE_ENCODING, "Failed to collect certificates from " + apkPath, e);
        } catch (IOException e7) {
        }
    }

    private static Signature[] convertToSignatures(Certificate[][] certs) throws CertificateEncodingException {
        Signature[] res = new Signature[certs.length];
        for (int i = 0; i < certs.length; i++) {
            res[i] = new Signature(certs[i]);
        }
        return res;
    }

    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e6 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:1:0x0008} */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00e6 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:1:0x0008} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x004b A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:3:0x000d} */
    /* JADX WARNING: Removed duplicated region for block: B:8:0x004b A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:3:0x000d} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d6 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:20:0x0095} */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d6 A:{ExcHandler: org.xmlpull.v1.XmlPullParserException (e org.xmlpull.v1.XmlPullParserException), Splitter: B:20:0x0095} */
    /* JADX WARNING: Missing block: B:8:0x004b, code:
            r24 = e;
     */
    /* JADX WARNING: Missing block: B:9:0x004c, code:
            r5 = null;
     */
    /* JADX WARNING: Missing block: B:12:0x006d, code:
            throw new android.content.pm.PackageParser.PackageParserException(android.content.pm.PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, "Failed to parse " + r20, r24);
     */
    /* JADX WARNING: Missing block: B:13:0x006e, code:
            r3 = th;
     */
    /* JADX WARNING: Missing block: B:36:0x00d6, code:
            r24 = e;
     */
    /* JADX WARNING: Missing block: B:42:0x00e6, code:
            r24 = e;
     */
    /* JADX WARNING: Missing block: B:43:0x00e7, code:
            r5 = null;
            r2 = null;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ApkLite parseApkLite(File apkFile, int flags) throws PackageParserException {
        Throwable th;
        String apkPath = apkFile.getAbsolutePath();
        AssetManager assets;
        XmlResourceParser parser;
        try {
            assets = new AssetManager();
            try {
                assets.setConfiguration(0, 0, null, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, VERSION.RESOURCES_SDK_INT);
                int cookie = assets.addAssetPath(apkPath);
                if (cookie == 0) {
                    throw new PackageParserException(-100, "Failed to parse " + apkPath);
                }
                Signature[] signatures;
                Certificate[][] certificates;
                DisplayMetrics metrics = new DisplayMetrics();
                metrics.setToDefaults();
                Resources res = new Resources(assets, metrics, null);
                parser = assets.openXmlResourceParser(cookie, ANDROID_MANIFEST_FILENAME);
                if ((flags & 256) != 0) {
                    try {
                        Package packageR = new Package(null);
                        Trace.traceBegin(1048576, "collectCertificates");
                        collectCertificates(packageR, apkFile, 0);
                        Trace.traceEnd(1048576);
                        signatures = packageR.mSignatures;
                        certificates = packageR.mCertificates;
                    } catch (XmlPullParserException e) {
                    } catch (Throwable th2) {
                        Trace.traceEnd(1048576);
                    }
                } else {
                    signatures = null;
                    certificates = null;
                }
                XmlResourceParser attrs = parser;
                ApkLite parseApkLite = parseApkLite(apkPath, res, parser, parser, flags, signatures, certificates);
                IoUtils.closeQuietly(parser);
                IoUtils.closeQuietly(assets);
                return parseApkLite;
            } catch (XmlPullParserException e2) {
            } catch (Throwable th3) {
                th = th3;
                parser = null;
                IoUtils.closeQuietly(parser);
                IoUtils.closeQuietly(assets);
                throw th;
            }
        } catch (XmlPullParserException e3) {
        } catch (Throwable th4) {
            th = th4;
            parser = null;
            assets = null;
            IoUtils.closeQuietly(parser);
            IoUtils.closeQuietly(assets);
            throw th;
        }
    }

    private static String validateName(String name, boolean requireSeparator, boolean requireFilename) {
        int N = name.length();
        boolean hasSep = false;
        boolean front = true;
        for (int i = 0; i < N; i++) {
            char c = name.charAt(i);
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                front = false;
            } else if (front || ((c < '0' || c > '9') && c != '_')) {
                if (c != '.') {
                    return "bad character '" + c + "'";
                }
                hasSep = true;
                front = true;
            }
        }
        if (requireFilename && !FileUtils.isValidExtFilename(name)) {
            return "Invalid filename";
        }
        String str = (hasSep || !requireSeparator) ? null : "must have at least one '.' separator";
        return str;
    }

    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianJun.Dan@Plf.SDK : Modify for oppo package", property = OppoRomType.ROM)
    private static Pair<String, String> parsePackageSplitNames(XmlPullParser parser, AttributeSet attrs) throws IOException, XmlPullParserException, PackageParserException {
        int type;
        do {
            type = parser.next();
            if (type == 2) {
                break;
            }
        } while (type != 1);
        if (type != 2) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No start tag found");
        } else if (parser.getName().equals(TAG_MANIFEST)) {
            String error;
            String packageName = attrs.getAttributeValue(null, TAG_PACKAGE);
            if (!ZenModeConfig.SYSTEM_AUTHORITY.equals(packageName)) {
                error = PackageParser.filterNameError(packageName, validateName(packageName, true, true));
                if (error != null) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest package: " + error);
                }
            }
            Object splitName = attrs.getAttributeValue(null, "split");
            if (splitName != null) {
                if (splitName.length() == 0) {
                    splitName = null;
                } else {
                    error = validateName(splitName, false, false);
                    if (error != null) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME, "Invalid manifest split: " + error);
                    }
                }
            }
            String intern = packageName.intern();
            if (splitName != null) {
                splitName = splitName.intern();
            }
            return Pair.create(intern, splitName);
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "No <manifest> tag");
        }
    }

    /* JADX WARNING: Missing block: B:28:0x00b6, code:
            if (r18.equals("com.sohu.inputmethod.sogouoem") != false) goto L_0x00b8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static ApkLite parseApkLite(String codePath, Resources res, XmlPullParser parser, AttributeSet attrs, int flags, Signature[] signatures, Certificate[][] certificates) throws IOException, XmlPullParserException, PackageParserException {
        int i;
        String attr;
        Pair<String, String> packageSplit = parsePackageSplitNames(parser, attrs);
        int installLocation = -1;
        int versionCode = 0;
        int revisionCode = 0;
        boolean coreApp = false;
        boolean multiArch = false;
        boolean use32bitAbi = false;
        boolean extractNativeLibs = true;
        for (i = 0; i < attrs.getAttributeCount(); i++) {
            attr = attrs.getAttributeName(i);
            if (attr.equals("installLocation")) {
                installLocation = attrs.getAttributeIntValue(i, -1);
            } else {
                if (attr.equals("versionCode")) {
                    versionCode = attrs.getAttributeIntValue(i, 0);
                } else {
                    if (attr.equals("revisionCode")) {
                        revisionCode = attrs.getAttributeIntValue(i, 0);
                    } else {
                        if (attr.equals("coreApp")) {
                            coreApp = attrs.getAttributeBooleanValue(i, false);
                        }
                    }
                }
            }
        }
        String packageName = attrs.getAttributeValue(null, TAG_PACKAGE);
        if (!(coreApp || packageName == null)) {
            if (!packageName.equals("com.google.android.inputmethod.latin")) {
                if (!packageName.equals("com.nuance.swype.oppo")) {
                    if (!packageName.equals("com.google.android.keep")) {
                        if (!packageName.equals("com.emoji.keyboard.touchpal")) {
                        }
                    }
                }
            }
            Slog.d(TAG, "Parser swype for enter password in ecryption mode");
            coreApp = true;
        }
        int searchDepth = parser.getDepth() + 1;
        List<VerifierInfo> verifiers = new ArrayList();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() < searchDepth)) {
            } else if (!(type == 3 || type == 4)) {
                if (parser.getDepth() == searchDepth && "package-verifier".equals(parser.getName())) {
                    VerifierInfo verifier = parseVerifier(res, parser, attrs, flags);
                    if (verifier != null) {
                        verifiers.add(verifier);
                    }
                }
                if (parser.getDepth() == searchDepth && TAG_APPLICATION.equals(parser.getName())) {
                    for (i = 0; i < attrs.getAttributeCount(); i++) {
                        attr = attrs.getAttributeName(i);
                        if ("multiArch".equals(attr)) {
                            multiArch = attrs.getAttributeBooleanValue(i, false);
                        }
                        if ("use32bitAbi".equals(attr)) {
                            use32bitAbi = attrs.getAttributeBooleanValue(i, false);
                        }
                        if ("extractNativeLibs".equals(attr)) {
                            extractNativeLibs = attrs.getAttributeBooleanValue(i, true);
                        }
                    }
                }
            }
        }
        return new ApkLite(codePath, (String) packageSplit.first, (String) packageSplit.second, versionCode, revisionCode, installLocation, verifiers, signatures, certificates, coreApp, multiArch, use32bitAbi, extractNativeLibs);
    }

    public static Signature stringToSignature(String str) {
        int N = str.length();
        byte[] sig = new byte[N];
        for (int i = 0; i < N; i++) {
            sig[i] = (byte) str.charAt(i);
        }
        return new Signature(sig);
    }

    private boolean parseBaseApkChild(Package parentPkg, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        if (parentPkg.childPackages == null || parentPkg.childPackages.size() + 2 <= 5) {
            String childPackageName = parser.getAttributeValue(null, TAG_PACKAGE);
            String message;
            if (validateName(childPackageName, true, false) != null) {
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
                return false;
            } else if (childPackageName.equals(parentPkg.packageName)) {
                message = "Child package name cannot be equal to parent package name: " + parentPkg.packageName;
                Slog.w(TAG, message);
                outError[0] = message;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            } else if (parentPkg.hasChildPackage(childPackageName)) {
                message = "Duplicate child package:" + childPackageName;
                Slog.w(TAG, message);
                outError[0] = message;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            } else {
                Package childPkg = new Package(childPackageName);
                childPkg.mVersionCode = parentPkg.mVersionCode;
                childPkg.baseRevisionCode = parentPkg.baseRevisionCode;
                childPkg.mVersionName = parentPkg.mVersionName;
                childPkg.applicationInfo.targetSdkVersion = parentPkg.applicationInfo.targetSdkVersion;
                childPkg.applicationInfo.minSdkVersion = parentPkg.applicationInfo.minSdkVersion;
                childPkg = parseBaseApkCommon(childPkg, CHILD_PACKAGE_TAGS, res, parser, flags, outError);
                if (childPkg == null) {
                    return false;
                }
                if (parentPkg.childPackages == null) {
                    parentPkg.childPackages = new ArrayList();
                }
                parentPkg.childPackages.add(childPkg);
                childPkg.parentPackage = parentPkg;
                return true;
            }
        }
        outError[0] = "Maximum number of packages per APK is: 5";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return false;
    }

    private Package parseBaseApk(Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        try {
            Pair<String, String> packageSplit = parsePackageSplitNames(parser, parser);
            String pkgName = packageSplit.first;
            String splitName = packageSplit.second;
            if (TextUtils.isEmpty(splitName)) {
                Package pkg = new Package(pkgName);
                TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifest);
                int integer = sa.getInteger(1, 0);
                pkg.applicationInfo.versionCode = integer;
                pkg.mVersionCode = integer;
                pkg.baseRevisionCode = sa.getInteger(5, 0);
                pkg.mVersionName = sa.getNonConfigurationString(2, 0);
                if (pkg.mVersionName != null) {
                    pkg.mVersionName = pkg.mVersionName.intern();
                }
                pkg.coreApp = parser.getAttributeBooleanValue(null, "coreApp", false);
                sa.recycle();
                return parseBaseApkCommon(pkg, null, res, parser, flags, outError);
            }
            outError[0] = "Expected base APK, but found split " + splitName;
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        } catch (PackageParserException e) {
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_PACKAGE_NAME;
            return null;
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:129:0x03f0  */
    /* JADX WARNING: Missing block: B:263:0x087a, code:
            r11 = NEW_PERMISSIONS.length;
            r27 = null;
            r31 = 0;
     */
    /* JADX WARNING: Missing block: B:265:0x0883, code:
            if (r31 >= r11) goto L_0x0895;
     */
    /* JADX WARNING: Missing block: B:266:0x0885, code:
            r37 = NEW_PERMISSIONS[r31];
     */
    /* JADX WARNING: Missing block: B:267:0x0893, code:
            if (r55.applicationInfo.targetSdkVersion < r37.sdkVersion) goto L_0x08f1;
     */
    /* JADX WARNING: Missing block: B:268:0x0895, code:
            if (r27 == null) goto L_0x08a1;
     */
    /* JADX WARNING: Missing block: B:269:0x0897, code:
            android.util.Slog.i(TAG, r27.toString());
     */
    /* JADX WARNING: Missing block: B:270:0x08a1, code:
            r12 = SPLIT_PERMISSIONS.length;
            r32 = 0;
     */
    /* JADX WARNING: Missing block: B:272:0x08a8, code:
            if (r32 >= r12) goto L_0x093f;
     */
    /* JADX WARNING: Missing block: B:273:0x08aa, code:
            r43 = SPLIT_PERMISSIONS[r32];
     */
    /* JADX WARNING: Missing block: B:274:0x08b8, code:
            if (r55.applicationInfo.targetSdkVersion >= r43.targetSdk) goto L_0x093b;
     */
    /* JADX WARNING: Missing block: B:276:0x08c6, code:
            if (r55.requestedPermissions.contains(r43.rootPerm) == false) goto L_0x093b;
     */
    /* JADX WARNING: Missing block: B:277:0x08c8, code:
            r28 = 0;
     */
    /* JADX WARNING: Missing block: B:279:0x08d1, code:
            if (r28 >= r43.newPerms.length) goto L_0x093b;
     */
    /* JADX WARNING: Missing block: B:280:0x08d3, code:
            r40 = r43.newPerms[r28];
     */
    /* JADX WARNING: Missing block: B:281:0x08e3, code:
            if (r55.requestedPermissions.contains(r40) != false) goto L_0x08ee;
     */
    /* JADX WARNING: Missing block: B:282:0x08e5, code:
            r55.requestedPermissions.add(r40);
     */
    /* JADX WARNING: Missing block: B:283:0x08ee, code:
            r28 = r28 + 1;
     */
    /* JADX WARNING: Missing block: B:285:0x08fd, code:
            if (r55.requestedPermissions.contains(r37.name) != false) goto L_0x092f;
     */
    /* JADX WARNING: Missing block: B:286:0x08ff, code:
            if (r27 != null) goto L_0x0933;
     */
    /* JADX WARNING: Missing block: B:287:0x0901, code:
            r0 = new java.lang.StringBuilder(128);
            r0.append(r55.packageName);
            r0.append(": compat added ");
     */
    /* JADX WARNING: Missing block: B:288:0x091b, code:
            r27.append(r37.name);
            r55.requestedPermissions.add(r37.name);
     */
    /* JADX WARNING: Missing block: B:289:0x092f, code:
            r31 = r31 + 1;
     */
    /* JADX WARNING: Missing block: B:290:0x0933, code:
            r27.append(' ');
     */
    /* JADX WARNING: Missing block: B:291:0x093b, code:
            r32 = r32 + 1;
     */
    /* JADX WARNING: Missing block: B:292:0x093f, code:
            if (r47 < 0) goto L_0x094c;
     */
    /* JADX WARNING: Missing block: B:293:0x0941, code:
            if (r47 <= 0) goto L_0x0956;
     */
    /* JADX WARNING: Missing block: B:295:0x094a, code:
            if (r55.applicationInfo.targetSdkVersion < 4) goto L_0x0956;
     */
    /* JADX WARNING: Missing block: B:296:0x094c, code:
            r5 = r55.applicationInfo;
            r5.flags |= 512;
     */
    /* JADX WARNING: Missing block: B:297:0x0956, code:
            if (r46 == 0) goto L_0x0962;
     */
    /* JADX WARNING: Missing block: B:298:0x0958, code:
            r5 = r55.applicationInfo;
            r5.flags |= 1024;
     */
    /* JADX WARNING: Missing block: B:299:0x0962, code:
            if (r45 < 0) goto L_0x096f;
     */
    /* JADX WARNING: Missing block: B:300:0x0964, code:
            if (r45 <= 0) goto L_0x0979;
     */
    /* JADX WARNING: Missing block: B:302:0x096d, code:
            if (r55.applicationInfo.targetSdkVersion < 4) goto L_0x0979;
     */
    /* JADX WARNING: Missing block: B:303:0x096f, code:
            r5 = r55.applicationInfo;
            r5.flags |= 2048;
     */
    /* JADX WARNING: Missing block: B:304:0x0979, code:
            if (r48 < 0) goto L_0x0987;
     */
    /* JADX WARNING: Missing block: B:305:0x097b, code:
            if (r48 <= 0) goto L_0x0992;
     */
    /* JADX WARNING: Missing block: B:307:0x0985, code:
            if (r55.applicationInfo.targetSdkVersion < 9) goto L_0x0992;
     */
    /* JADX WARNING: Missing block: B:308:0x0987, code:
            r5 = r55.applicationInfo;
            r5.flags |= 524288;
     */
    /* JADX WARNING: Missing block: B:309:0x0992, code:
            if (r41 < 0) goto L_0x099f;
     */
    /* JADX WARNING: Missing block: B:310:0x0994, code:
            if (r41 <= 0) goto L_0x09a9;
     */
    /* JADX WARNING: Missing block: B:312:0x099d, code:
            if (r55.applicationInfo.targetSdkVersion < 4) goto L_0x09a9;
     */
    /* JADX WARNING: Missing block: B:313:0x099f, code:
            r5 = r55.applicationInfo;
            r5.flags |= 4096;
     */
    /* JADX WARNING: Missing block: B:314:0x09a9, code:
            if (r14 < 0) goto L_0x09b6;
     */
    /* JADX WARNING: Missing block: B:315:0x09ab, code:
            if (r14 <= 0) goto L_0x09c0;
     */
    /* JADX WARNING: Missing block: B:317:0x09b4, code:
            if (r55.applicationInfo.targetSdkVersion < 4) goto L_0x09c0;
     */
    /* JADX WARNING: Missing block: B:318:0x09b6, code:
            r5 = r55.applicationInfo;
            r5.flags |= 8192;
     */
    /* JADX WARNING: Missing block: B:320:0x09c4, code:
            if (com.mediatek.cta.CtaUtils.isCtaSupported() == false) goto L_0x09d7;
     */
    /* JADX WARNING: Missing block: B:321:0x09c6, code:
            r17 = (com.mediatek.cta.CtaPackageManagerInternal) com.android.server.LocalServices.getService(com.mediatek.cta.CtaPackageManagerInternal.class);
     */
    /* JADX WARNING: Missing block: B:322:0x09ce, code:
            if (r17 == null) goto L_0x09d7;
     */
    /* JADX WARNING: Missing block: B:323:0x09d0, code:
            r17.linkCtaPermissions(r55);
     */
    /* JADX WARNING: Missing block: B:324:0x09d7, code:
            return r55;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "JianJun.Dan@Plf.SDK : Modify for oppo package", property = OppoRomType.ROM)
    private Package parseBaseApkCommon(Package pkg, Set<String> acceptedTags, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        ApplicationInfo applicationInfo;
        this.mParseInstrumentationArgs = null;
        this.mParseActivityArgs = null;
        this.mParseServiceArgs = null;
        this.mParseProviderArgs = null;
        boolean foundApp = false;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifest);
        String str = sa.getNonConfigurationString(0, 0);
        if (str != null && str.length() > 0) {
            String nameError = PackageParser.filterNameError(pkg.packageName, validateName(str, true, false));
            if (nameError == null || ZenModeConfig.SYSTEM_AUTHORITY.equals(pkg.packageName)) {
                pkg.mSharedUserId = str.intern();
                pkg.mSharedUserLabel = sa.getResourceId(3, 0);
            } else {
                outError[0] = "<manifest> specifies bad sharedUserId name \"" + str + "\": " + nameError;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_SHARED_USER_ID;
                return null;
            }
        }
        pkg.installLocation = sa.getInteger(4, -1);
        pkg.applicationInfo.installLocation = pkg.installLocation;
        if ((flags & 16) != 0) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= 4;
        }
        if ((flags & 32) != 0) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.flags |= 262144;
        }
        if ((flags & 2048) != 0) {
            applicationInfo = pkg.applicationInfo;
            applicationInfo.privateFlags |= 512;
        }
        int supportsSmallScreens = 1;
        int supportsNormalScreens = 1;
        int supportsLargeScreens = 1;
        int supportsXLargeScreens = 1;
        int resizeable = 1;
        int anyDensity = 1;
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!foundApp && pkg.instrumentation.size() == 0) {
                    outError[0] = "<manifest> does not contain an <application> or <instrumentation>";
                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_EMPTY;
                }
            } else if (!(type == 3 || type == 4)) {
                String tagName = parser.getName();
                if (acceptedTags == null || acceptedTags.contains(tagName)) {
                    if (!tagName.equals(TAG_APPLICATION)) {
                        if (tagName.equals(TAG_OVERLAY)) {
                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestResourceOverlay);
                            pkg.mOverlayTarget = sa.getString(1);
                            pkg.mOverlayPriority = sa.getInt(0, -1);
                            sa.recycle();
                            if (pkg.mOverlayTarget == null) {
                                outError[0] = "<overlay> does not specify a target package";
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return null;
                            } else if (pkg.mOverlayPriority < 0 || pkg.mOverlayPriority > 9999) {
                                outError[0] = "<overlay> priority must be between 0 and 9999";
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            } else {
                                XmlUtils.skipCurrentTag(parser);
                            }
                        } else {
                            if (!tagName.equals(TAG_KEY_SETS)) {
                                if (!tagName.equals(TAG_PERMISSION_GROUP)) {
                                    if (!tagName.equals("permission")) {
                                        if (!tagName.equals(TAG_PERMISSION_TREE)) {
                                            if (!tagName.equals(TAG_USES_PERMISSION)) {
                                                if (!tagName.equals(TAG_USES_PERMISSION_SDK_M)) {
                                                    if (!tagName.equals(TAG_USES_PERMISSION_SDK_23)) {
                                                        ConfigurationInfo cPref;
                                                        if (tagName.equals(TAG_USES_CONFIGURATION)) {
                                                            cPref = new ConfigurationInfo();
                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesConfiguration);
                                                            cPref.reqTouchScreen = sa.getInt(0, 0);
                                                            cPref.reqKeyboardType = sa.getInt(1, 0);
                                                            if (sa.getBoolean(2, false)) {
                                                                cPref.reqInputFeatures |= 1;
                                                            }
                                                            cPref.reqNavigation = sa.getInt(3, 0);
                                                            if (sa.getBoolean(4, false)) {
                                                                cPref.reqInputFeatures |= 2;
                                                            }
                                                            sa.recycle();
                                                            pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref);
                                                            XmlUtils.skipCurrentTag(parser);
                                                        } else {
                                                            if (tagName.equals(TAG_USES_FEATURE)) {
                                                                FeatureInfo fi = parseUsesFeature(res, parser);
                                                                pkg.reqFeatures = ArrayUtils.add(pkg.reqFeatures, fi);
                                                                if (fi.name == null) {
                                                                    cPref = new ConfigurationInfo();
                                                                    cPref.reqGlEsVersion = fi.reqGlEsVersion;
                                                                    pkg.configPreferences = ArrayUtils.add(pkg.configPreferences, cPref);
                                                                }
                                                                XmlUtils.skipCurrentTag(parser);
                                                            } else {
                                                                if (tagName.equals(TAG_FEATURE_GROUP)) {
                                                                    FeatureGroupInfo group = new FeatureGroupInfo();
                                                                    ArrayList features = null;
                                                                    int innerDepth = parser.getDepth();
                                                                    while (true) {
                                                                        type = parser.next();
                                                                        if (type == 1 || (type == 3 && parser.getDepth() <= innerDepth)) {
                                                                            if (features != null) {
                                                                                group.features = new FeatureInfo[features.size()];
                                                                                group.features = (FeatureInfo[]) features.toArray(group.features);
                                                                            }
                                                                        } else if (!(type == 3 || type == 4)) {
                                                                            String innerTagName = parser.getName();
                                                                            if (innerTagName.equals(TAG_USES_FEATURE)) {
                                                                                FeatureInfo featureInfo = parseUsesFeature(res, parser);
                                                                                featureInfo.flags |= 1;
                                                                                features = ArrayUtils.add(features, featureInfo);
                                                                            } else {
                                                                                Slog.w(TAG, "Unknown element under <feature-group>: " + innerTagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                                            }
                                                                            XmlUtils.skipCurrentTag(parser);
                                                                        }
                                                                    }
                                                                    if (features != null) {
                                                                    }
                                                                    pkg.featureGroups = ArrayUtils.add(pkg.featureGroups, group);
                                                                } else {
                                                                    if (tagName.equals(TAG_USES_SDK)) {
                                                                        if (SDK_VERSION > 0) {
                                                                            boolean allowedCodename;
                                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesSdk);
                                                                            int minVers = 1;
                                                                            String minCode = null;
                                                                            int targetVers = 0;
                                                                            String targetCode = null;
                                                                            TypedValue val = sa.peekValue(0);
                                                                            if (val != null) {
                                                                                if (val.type != 3 || val.string == null) {
                                                                                    minVers = val.data;
                                                                                    targetVers = minVers;
                                                                                } else {
                                                                                    minCode = val.string.toString();
                                                                                    targetCode = minCode;
                                                                                }
                                                                            }
                                                                            val = sa.peekValue(1);
                                                                            if (val != null) {
                                                                                if (val.type != 3 || val.string == null) {
                                                                                    targetVers = val.data;
                                                                                } else {
                                                                                    targetCode = val.string.toString();
                                                                                    if (minCode == null) {
                                                                                        minCode = targetCode;
                                                                                    }
                                                                                }
                                                                            }
                                                                            sa.recycle();
                                                                            if (minCode != null) {
                                                                                allowedCodename = false;
                                                                                for (String codename : SDK_CODENAMES) {
                                                                                    if (minCode.equals(codename)) {
                                                                                        allowedCodename = true;
                                                                                        break;
                                                                                    }
                                                                                }
                                                                                if (allowedCodename) {
                                                                                    pkg.applicationInfo.minSdkVersion = 10000;
                                                                                } else {
                                                                                    if (SDK_CODENAMES.length > 0) {
                                                                                        outError[0] = "Requires development platform " + minCode + " (current platform is any of " + Arrays.toString(SDK_CODENAMES) + ")";
                                                                                    } else {
                                                                                        outError[0] = "Requires development platform " + minCode + " but this is a release platform.";
                                                                                    }
                                                                                    this.mParseError = -12;
                                                                                    return null;
                                                                                }
                                                                            } else if (minVers > SDK_VERSION) {
                                                                                outError[0] = "Requires newer sdk version #" + minVers + " (current version is #" + SDK_VERSION + ")";
                                                                                this.mParseError = -12;
                                                                                return null;
                                                                            } else {
                                                                                pkg.applicationInfo.minSdkVersion = minVers;
                                                                            }
                                                                            if (targetCode != null) {
                                                                                allowedCodename = false;
                                                                                for (String codename2 : SDK_CODENAMES) {
                                                                                    if (targetCode.equals(codename2)) {
                                                                                        allowedCodename = true;
                                                                                        break;
                                                                                    }
                                                                                }
                                                                                if (allowedCodename) {
                                                                                    pkg.applicationInfo.targetSdkVersion = 10000;
                                                                                } else {
                                                                                    if (SDK_CODENAMES.length > 0) {
                                                                                        outError[0] = "Requires development platform " + targetCode + " (current platform is any of " + Arrays.toString(SDK_CODENAMES) + ")";
                                                                                    } else {
                                                                                        outError[0] = "Requires development platform " + targetCode + " but this is a release platform.";
                                                                                    }
                                                                                    this.mParseError = -12;
                                                                                    return null;
                                                                                }
                                                                            }
                                                                            pkg.applicationInfo.targetSdkVersion = targetVers;
                                                                        }
                                                                        XmlUtils.skipCurrentTag(parser);
                                                                    } else {
                                                                        if (tagName.equals(TAG_SUPPORT_SCREENS)) {
                                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestSupportsScreens);
                                                                            pkg.applicationInfo.requiresSmallestWidthDp = sa.getInteger(6, 0);
                                                                            pkg.applicationInfo.compatibleWidthLimitDp = sa.getInteger(7, 0);
                                                                            pkg.applicationInfo.largestWidthLimitDp = sa.getInteger(8, 0);
                                                                            supportsSmallScreens = sa.getInteger(1, supportsSmallScreens);
                                                                            supportsNormalScreens = sa.getInteger(2, supportsNormalScreens);
                                                                            supportsLargeScreens = sa.getInteger(3, supportsLargeScreens);
                                                                            supportsXLargeScreens = sa.getInteger(5, supportsXLargeScreens);
                                                                            resizeable = sa.getInteger(4, resizeable);
                                                                            anyDensity = sa.getInteger(0, anyDensity);
                                                                            sa.recycle();
                                                                            XmlUtils.skipCurrentTag(parser);
                                                                        } else {
                                                                            String name;
                                                                            if (tagName.equals(TAG_PROTECTED_BROADCAST)) {
                                                                                sa = res.obtainAttributes(parser, R.styleable.AndroidManifestProtectedBroadcast);
                                                                                name = sa.getNonResourceString(0);
                                                                                sa.recycle();
                                                                                if (!(name == null || (flags & 1) == 0)) {
                                                                                    if (pkg.protectedBroadcasts == null) {
                                                                                        pkg.protectedBroadcasts = new ArrayList();
                                                                                    }
                                                                                    if (!pkg.protectedBroadcasts.contains(name)) {
                                                                                        pkg.protectedBroadcasts.add(name.intern());
                                                                                    }
                                                                                }
                                                                                XmlUtils.skipCurrentTag(parser);
                                                                            } else {
                                                                                if (!tagName.equals(TAG_INSTRUMENTATION)) {
                                                                                    if (tagName.equals(TAG_ORIGINAL_PACKAGE)) {
                                                                                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestOriginalPackage);
                                                                                        String orig = sa.getNonConfigurationString(0, 0);
                                                                                        if (!pkg.packageName.equals(orig)) {
                                                                                            if (pkg.mOriginalPackages == null) {
                                                                                                pkg.mOriginalPackages = new ArrayList();
                                                                                                pkg.mRealPackage = pkg.packageName;
                                                                                            }
                                                                                            pkg.mOriginalPackages.add(orig);
                                                                                        }
                                                                                        sa.recycle();
                                                                                        XmlUtils.skipCurrentTag(parser);
                                                                                    } else {
                                                                                        if (tagName.equals(TAG_ADOPT_PERMISSIONS)) {
                                                                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestOriginalPackage);
                                                                                            name = sa.getNonConfigurationString(0, 0);
                                                                                            sa.recycle();
                                                                                            if (name != null) {
                                                                                                if (pkg.mAdoptPermissions == null) {
                                                                                                    pkg.mAdoptPermissions = new ArrayList();
                                                                                                }
                                                                                                pkg.mAdoptPermissions.add(name);
                                                                                            }
                                                                                            XmlUtils.skipCurrentTag(parser);
                                                                                        } else {
                                                                                            if (tagName.equals(TAG_USES_GL_TEXTURE)) {
                                                                                                XmlUtils.skipCurrentTag(parser);
                                                                                            } else {
                                                                                                if (tagName.equals(TAG_COMPATIBLE_SCREENS)) {
                                                                                                    XmlUtils.skipCurrentTag(parser);
                                                                                                } else {
                                                                                                    if (tagName.equals(TAG_SUPPORTS_INPUT)) {
                                                                                                        XmlUtils.skipCurrentTag(parser);
                                                                                                    } else {
                                                                                                        if (tagName.equals(TAG_EAT_COMMENT)) {
                                                                                                            XmlUtils.skipCurrentTag(parser);
                                                                                                        } else {
                                                                                                            if (tagName.equals(TAG_PACKAGE)) {
                                                                                                                XmlUtils.skipCurrentTag(parser);
                                                                                                            } else {
                                                                                                                if (tagName.equals(TAG_RESTRICT_UPDATE)) {
                                                                                                                    if ((flags & 64) != 0) {
                                                                                                                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestRestrictUpdate);
                                                                                                                        String hash = sa.getNonConfigurationString(0, 0);
                                                                                                                        sa.recycle();
                                                                                                                        pkg.restrictUpdateHash = null;
                                                                                                                        if (hash != null) {
                                                                                                                            int hashLength = hash.length();
                                                                                                                            byte[] hashBytes = new byte[(hashLength / 2)];
                                                                                                                            for (int i = 0; i < hashLength; i += 2) {
                                                                                                                                hashBytes[i / 2] = (byte) ((Character.digit(hash.charAt(i), 16) << 4) + Character.digit(hash.charAt(i + 1), 16));
                                                                                                                            }
                                                                                                                            pkg.restrictUpdateHash = hashBytes;
                                                                                                                        }
                                                                                                                    }
                                                                                                                    XmlUtils.skipCurrentTag(parser);
                                                                                                                } else {
                                                                                                                    Slog.w(TAG, "Unknown element under <manifest>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                                                                                    XmlUtils.skipCurrentTag(parser);
                                                                                                                }
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                } else if (parseInstrumentation(pkg, res, parser, outError) == null) {
                                                                                    return null;
                                                                                }
                                                                            }
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                                if (!parseUsesPermission(pkg, res, parser)) {
                                                    return null;
                                                }
                                            } else if (!parseUsesPermission(pkg, res, parser)) {
                                                return null;
                                            }
                                        } else if (parsePermissionTree(pkg, res, parser, outError) == null) {
                                            return null;
                                        }
                                    } else if (parsePermission(pkg, res, parser, outError) == null) {
                                        return null;
                                    }
                                } else if (parsePermissionGroup(pkg, flags, res, parser, outError) == null) {
                                    return null;
                                }
                            } else if (!parseKeySets(pkg, res, parser, outError)) {
                                return null;
                            }
                        }
                    } else if (foundApp) {
                        Slog.w(TAG, "<manifest> has more than one <application>");
                        XmlUtils.skipCurrentTag(parser);
                    } else {
                        foundApp = true;
                        if (!parseBaseApplication(pkg, res, parser, flags, outError)) {
                            return null;
                        }
                    }
                } else {
                    Slog.w(TAG, "Skipping unsupported element under <manifest>: " + tagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        outError[0] = "<overlay> priority must be between 0 and 9999";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private FeatureInfo parseUsesFeature(Resources res, AttributeSet attrs) {
        FeatureInfo fi = new FeatureInfo();
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestUsesFeature);
        fi.name = sa.getNonResourceString(0);
        fi.version = sa.getInt(3, 0);
        if (fi.name == null) {
            fi.reqGlEsVersion = sa.getInt(1, 0);
        }
        if (sa.getBoolean(2, true)) {
            fi.flags |= 1;
        }
        sa.recycle();
        return fi;
    }

    private boolean parseUsesPermission(Package pkg, Resources res, XmlResourceParser parser) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesPermission);
        String name = sa.getNonResourceString(0);
        int maxSdkVersion = 0;
        TypedValue val = sa.peekValue(1);
        if (val != null && val.type >= 16 && val.type <= 31) {
            maxSdkVersion = val.data;
        }
        sa.recycle();
        if ((maxSdkVersion == 0 || maxSdkVersion >= VERSION.RESOURCES_SDK_INT) && name != null && pkg.requestedPermissions.indexOf(name) == -1) {
            pkg.requestedPermissions.add(name.intern());
        }
        XmlUtils.skipCurrentTag(parser);
        return true;
    }

    private static String buildClassName(String pkg, CharSequence clsSeq, String[] outError) {
        if (clsSeq == null || clsSeq.length() <= 0) {
            outError[0] = "Empty class name in package " + pkg;
            return null;
        }
        String cls = clsSeq.toString();
        if (cls.charAt(0) == '.') {
            return (pkg + cls).intern();
        }
        if (cls.indexOf(46) >= 0) {
            return cls.intern();
        }
        StringBuilder b = new StringBuilder(pkg);
        b.append('.');
        b.append(cls);
        return b.toString().intern();
    }

    private static String buildCompoundName(String pkg, CharSequence procSeq, String type, String[] outError) {
        String proc = procSeq.toString();
        char c = proc.charAt(0);
        String nameError;
        if (pkg == null || c != ':') {
            nameError = validateName(proc, true, false);
            if (nameError == null || "system".equals(proc)) {
                return proc.intern();
            }
            outError[0] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError;
            return null;
        } else if (proc.length() < 2) {
            outError[0] = "Bad " + type + " name " + proc + " in package " + pkg + ": must be at least two characters";
            return null;
        } else {
            nameError = validateName(proc.substring(1), false, false);
            if (nameError == null) {
                return (pkg + proc).intern();
            }
            outError[0] = "Invalid " + type + " name " + proc + " in package " + pkg + ": " + nameError;
            return null;
        }
    }

    private static String buildProcessName(String pkg, String defProc, CharSequence procSeq, int flags, String[] separateProcesses, String[] outError) {
        if ((flags & 8) == 0 || "system".equals(procSeq)) {
            if (separateProcesses != null) {
                for (int i = separateProcesses.length - 1; i >= 0; i--) {
                    String sp = separateProcesses[i];
                    if (sp.equals(pkg) || sp.equals(defProc) || sp.equals(procSeq)) {
                        return pkg;
                    }
                }
            }
            if (procSeq == null || procSeq.length() <= 0) {
                return defProc;
            }
            return buildCompoundName(pkg, procSeq, "process", outError);
        }
        if (defProc == null) {
            defProc = pkg;
        }
        return defProc;
    }

    private static String buildTaskAffinityName(String pkg, String defProc, CharSequence procSeq, String[] outError) {
        if (procSeq == null) {
            return defProc;
        }
        if (procSeq.length() <= 0) {
            return null;
        }
        return buildCompoundName(pkg, procSeq, "taskAffinity", outError);
    }

    private boolean parseKeySets(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        int currentKeySetDepth = -1;
        String currentKeySet = null;
        ArrayMap<String, PublicKey> publicKeys = new ArrayMap();
        ArraySet<String> upgradeKeySets = new ArraySet();
        ArrayMap<String, ArraySet<String>> definedKeySets = new ArrayMap();
        ArraySet<String> improperKeySets = new ArraySet();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
            } else if (type != 3) {
                String tagName = parser.getName();
                TypedArray sa;
                if (tagName.equals("key-set")) {
                    if (currentKeySet != null) {
                        outError[0] = "Improperly nested 'key-set' tag at " + parser.getPositionDescription();
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    sa = res.obtainAttributes(parser, R.styleable.AndroidManifestKeySet);
                    String keysetName = sa.getNonResourceString(0);
                    definedKeySets.put(keysetName, new ArraySet());
                    currentKeySet = keysetName;
                    currentKeySetDepth = parser.getDepth();
                    sa.recycle();
                } else if (tagName.equals("public-key")) {
                    if (currentKeySet == null) {
                        outError[0] = "Improperly nested 'key-set' tag at " + parser.getPositionDescription();
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        return false;
                    }
                    sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPublicKey);
                    String publicKeyName = sa.getNonResourceString(0);
                    String encodedKey = sa.getNonResourceString(1);
                    if (encodedKey == null && publicKeys.get(publicKeyName) == null) {
                        outError[0] = "'public-key' " + publicKeyName + " must define a public-key value" + " on first use at " + parser.getPositionDescription();
                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                        sa.recycle();
                        return false;
                    }
                    if (encodedKey != null) {
                        PublicKey currentKey = parsePublicKey(encodedKey);
                        if (currentKey == null) {
                            Slog.w(TAG, "No recognized valid key in 'public-key' tag at " + parser.getPositionDescription() + " key-set " + currentKeySet + " will not be added to the package's defined key-sets.");
                            sa.recycle();
                            improperKeySets.add(currentKeySet);
                            XmlUtils.skipCurrentTag(parser);
                        } else if (publicKeys.get(publicKeyName) == null || ((PublicKey) publicKeys.get(publicKeyName)).equals(currentKey)) {
                            publicKeys.put(publicKeyName, currentKey);
                        } else {
                            outError[0] = "Value of 'public-key' " + publicKeyName + " conflicts with previously defined value at " + parser.getPositionDescription();
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            sa.recycle();
                            return false;
                        }
                    }
                    ((ArraySet) definedKeySets.get(currentKeySet)).add(publicKeyName);
                    sa.recycle();
                    XmlUtils.skipCurrentTag(parser);
                } else if (tagName.equals("upgrade-key-set")) {
                    sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUpgradeKeySet);
                    upgradeKeySets.add(sa.getNonResourceString(0));
                    sa.recycle();
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    Slog.w(TAG, "Unknown element under <key-sets>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            } else if (parser.getDepth() == currentKeySetDepth) {
                currentKeySet = null;
                currentKeySetDepth = -1;
            }
        }
        if (publicKeys.keySet().removeAll(definedKeySets.keySet())) {
            outError[0] = "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' and 'public-key' names must be distinct.";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return false;
        }
        owner.mKeySetMapping = new ArrayMap();
        for (Entry<String, ArraySet<String>> e : definedKeySets.entrySet()) {
            String keySetName = (String) e.getKey();
            if (((ArraySet) e.getValue()).size() == 0) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' " + keySetName + " has no valid associated 'public-key'." + " Not including in package's defined key-sets.");
            } else if (improperKeySets.contains(keySetName)) {
                Slog.w(TAG, "Package" + owner.packageName + " AndroidManifext.xml " + "'key-set' " + keySetName + " contained improper 'public-key'" + " tags. Not including in package's defined key-sets.");
            } else {
                owner.mKeySetMapping.put(keySetName, new ArraySet());
                for (String s : (ArraySet) e.getValue()) {
                    ((ArraySet) owner.mKeySetMapping.get(keySetName)).add((PublicKey) publicKeys.get(s));
                }
            }
        }
        if (owner.mKeySetMapping.keySet().containsAll(upgradeKeySets)) {
            owner.mUpgradeKeySets = upgradeKeySets;
            return true;
        }
        outError[0] = "Package" + owner.packageName + " AndroidManifext.xml " + "does not define all 'upgrade-key-set's .";
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return false;
    }

    private PermissionGroup parsePermissionGroup(Package owner, int flags, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Component perm = new PermissionGroup(owner);
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermissionGroup);
        if (parsePackageItemInfo(owner, perm.info, outError, "<permission-group>", sa, true, 2, 0, 1, 8, 5, 7)) {
            perm.info.descriptionRes = sa.getResourceId(4, 0);
            perm.info.flags = sa.getInt(6, 0);
            perm.info.priority = sa.getInt(3, 0);
            if (perm.info.priority > 0 && (flags & 1) == 0) {
                perm.info.priority = 0;
            }
            sa.recycle();
            if (parseAllMetaData(res, parser, "<permission-group>", perm, outError)) {
                owner.permissionGroups.add(perm);
                return perm;
            }
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        sa.recycle();
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private Permission parsePermission(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Component perm = new Permission(owner);
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermission);
        if (parsePackageItemInfo(owner, perm.info, outError, "<permission>", sa, true, 2, 0, 1, 9, 6, 8)) {
            perm.info.group = sa.getNonResourceString(4);
            if (perm.info.group != null) {
                perm.info.group = perm.info.group.intern();
            }
            perm.info.descriptionRes = sa.getResourceId(5, 0);
            perm.info.protectionLevel = sa.getInt(3, 0);
            perm.info.flags = sa.getInt(7, 0);
            sa.recycle();
            if (perm.info.protectionLevel == -1) {
                outError[0] = "<permission> does not specify protectionLevel";
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;
            }
            perm.info.protectionLevel = PermissionInfo.fixProtectionLevel(perm.info.protectionLevel);
            if ((perm.info.protectionLevel & PermissionInfo.PROTECTION_MASK_FLAGS) == 0 || (perm.info.protectionLevel & 15) == 2) {
                if (parseAllMetaData(res, parser, "<permission>", perm, outError)) {
                    owner.permissions.add(perm);
                    return perm;
                }
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;
            }
            outError[0] = "<permission>  protectionLevel specifies a flag but is not based on signature type";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        sa.recycle();
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private Permission parsePermissionTree(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        Component perm = new Permission(owner);
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPermissionTree);
        if (parsePackageItemInfo(owner, perm.info, outError, "<permission-tree>", sa, true, 2, 0, 1, 5, 3, 4)) {
            sa.recycle();
            int index = perm.info.name.indexOf(46);
            if (index > 0) {
                index = perm.info.name.indexOf(46, index + 1);
            }
            if (index < 0) {
                outError[0] = "<permission-tree> name has less than three segments: " + perm.info.name;
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return null;
            }
            perm.info.descriptionRes = 0;
            perm.info.protectionLevel = 0;
            perm.tree = true;
            if (parseAllMetaData(res, parser, "<permission-tree>", perm, outError)) {
                owner.permissions.add(perm);
                return perm;
            }
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        sa.recycle();
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private Instrumentation parseInstrumentation(Package owner, Resources res, XmlResourceParser parser, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestInstrumentation);
        if (this.mParseInstrumentationArgs == null) {
            this.mParseInstrumentationArgs = new ParsePackageItemArgs(owner, outError, 2, 0, 1, 8, 6, 7);
            this.mParseInstrumentationArgs.tag = "<instrumentation>";
        }
        this.mParseInstrumentationArgs.sa = sa;
        Instrumentation a = new Instrumentation(this.mParseInstrumentationArgs, new InstrumentationInfo());
        if (outError[0] != null) {
            sa.recycle();
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        String str = sa.getNonResourceString(3);
        a.info.targetPackage = str != null ? str.intern() : null;
        a.info.handleProfiling = sa.getBoolean(4, false);
        a.info.functionalTest = sa.getBoolean(5, false);
        sa.recycle();
        if (a.info.targetPackage == null) {
            outError[0] = "<instrumentation> does not specify targetPackage";
            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
            return null;
        }
        if (parseAllMetaData(res, parser, "<instrumentation>", a, outError)) {
            owner.instrumentation.add(a);
            return a;
        }
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return null;
    }

    private boolean parseBaseApplication(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        ApplicationInfo ai = owner.applicationInfo;
        String pkgName = owner.applicationInfo.packageName;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestApplication);
        if (parsePackageItemInfo(owner, ai, outError, "<application>", sa, false, 3, 1, 2, 42, 22, 30)) {
            if (ai.name != null) {
                ai.className = ai.name;
            }
            String manageSpaceActivity = sa.getNonConfigurationString(4, 1024);
            if (manageSpaceActivity != null) {
                ai.manageSpaceActivityName = buildClassName(pkgName, manageSpaceActivity, outError);
            }
            if (sa.getBoolean(17, true)) {
                ai.flags |= 32768;
                String backupAgent = sa.getNonConfigurationString(16, 1024);
                if (backupAgent != null) {
                    ai.backupAgentName = buildClassName(pkgName, backupAgent, outError);
                    if (sa.getBoolean(18, true)) {
                        ai.flags |= 65536;
                    }
                    if (sa.getBoolean(21, false)) {
                        ai.flags |= 131072;
                    }
                    if (sa.getBoolean(32, false)) {
                        ai.flags |= 67108864;
                    }
                    if (sa.getBoolean(40, false)) {
                        ai.privateFlags |= 4096;
                    }
                }
                TypedValue v = sa.peekValue(35);
                if (v != null) {
                    int i = v.resourceId;
                    ai.fullBackupContent = i;
                    if (i == 0) {
                        ai.fullBackupContent = v.data == 0 ? -1 : 0;
                    }
                }
            }
            ai.theme = sa.getResourceId(0, 0);
            ai.descriptionRes = sa.getResourceId(13, 0);
            if ((flags & 1) != 0 && sa.getBoolean(8, false)) {
                ai.flags |= 8;
            }
            if (sa.getBoolean(27, false)) {
                owner.mRequiredForAllUsers = true;
            }
            String restrictedAccountType = sa.getString(28);
            if (restrictedAccountType != null && restrictedAccountType.length() > 0) {
                owner.mRestrictedAccountType = restrictedAccountType;
            }
            String requiredAccountType = sa.getString(29);
            if (requiredAccountType != null && requiredAccountType.length() > 0) {
                owner.mRequiredAccountType = requiredAccountType;
            }
            if (sa.getBoolean(10, false)) {
                ai.flags |= 2;
            }
            if (sa.getBoolean(20, false)) {
                ai.flags |= 16384;
            }
            owner.baseHardwareAccelerated = sa.getBoolean(23, owner.applicationInfo.targetSdkVersion >= 14);
            if (owner.baseHardwareAccelerated) {
                ai.flags |= 536870912;
            }
            if (sa.getBoolean(7, true)) {
                ai.flags |= 4;
            }
            if (sa.getBoolean(14, false)) {
                ai.flags |= 32;
            }
            if (sa.getBoolean(5, true)) {
                ai.flags |= 64;
            }
            if (owner.parentPackage == null && sa.getBoolean(15, false)) {
                ai.flags |= 256;
            }
            if (sa.getBoolean(24, false)) {
                ai.flags |= 1048576;
            }
            if (sa.getBoolean(36, true)) {
                ai.flags |= 134217728;
            }
            if (sa.getBoolean(26, false)) {
                ai.flags |= 4194304;
            }
            if (sa.getBoolean(33, false)) {
                ai.flags |= Integer.MIN_VALUE;
            }
            if (sa.getBoolean(34, true)) {
                ai.flags |= 268435456;
            }
            if (sa.getBoolean(38, false)) {
                ai.privateFlags |= 32;
            }
            if (sa.getBoolean(39, false)) {
                ai.privateFlags |= 64;
            }
            if (sa.getBoolean(37, owner.applicationInfo.targetSdkVersion >= 24)) {
                ai.privateFlags |= 2048;
            }
            ai.networkSecurityConfigRes = sa.getResourceId(41, 0);
            String str = sa.getNonConfigurationString(6, 0);
            String intern = (str == null || str.length() <= 0) ? null : str.intern();
            ai.permission = intern;
            if (owner.applicationInfo.targetSdkVersion >= 8) {
                str = sa.getNonConfigurationString(12, 1024);
            } else {
                str = sa.getNonResourceString(12);
            }
            ai.taskAffinity = buildTaskAffinityName(ai.packageName, ai.packageName, str, outError);
            if (outError[0] == null) {
                CharSequence pname;
                if (owner.applicationInfo.targetSdkVersion >= 8) {
                    pname = sa.getNonConfigurationString(11, 1024);
                } else {
                    pname = sa.getNonResourceString(11);
                }
                ai.processName = buildProcessName(ai.packageName, null, pname, flags, this.mSeparateProcesses, outError);
                ai.enabled = sa.getBoolean(9, true);
                if (sa.getBoolean(31, false)) {
                    ai.flags |= 33554432;
                }
            }
            ai.uiOptions = sa.getInt(25, 0);
            sa.recycle();
            if (outError[0] != null) {
                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                return false;
            }
            int innerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= innerDepth)) {
                    modifySharedLibrariesForBackwardCompatibility(owner);
                } else if (!(type == 3 || type == 4)) {
                    String tagName = parser.getName();
                    Activity a;
                    if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                        a = parseActivity(owner, res, parser, flags, outError, false, owner.baseHardwareAccelerated);
                        if (a == null) {
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            return false;
                        }
                        owner.activities.add(a);
                    } else {
                        if (tagName.equals("receiver")) {
                            a = parseActivity(owner, res, parser, flags, outError, true, false);
                            if (a == null) {
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return false;
                            }
                            owner.receivers.add(a);
                        } else {
                            if (tagName.equals(Notification.CATEGORY_SERVICE)) {
                                Service s = parseService(owner, res, parser, flags, outError);
                                if (s == null) {
                                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                owner.services.add(s);
                            } else {
                                if (tagName.equals("provider")) {
                                    Provider p = parseProvider(owner, res, parser, flags, outError);
                                    if (p == null) {
                                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        return false;
                                    }
                                    owner.providers.add(p);
                                } else {
                                    if (tagName.equals("activity-alias")) {
                                        a = parseActivityAlias(owner, res, parser, flags, outError);
                                        if (a == null) {
                                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                            return false;
                                        }
                                        owner.activities.add(a);
                                    } else if (parser.getName().equals("meta-data")) {
                                        Bundle parseMetaData = parseMetaData(res, parser, owner.mAppMetaData, outError);
                                        owner.mAppMetaData = parseMetaData;
                                        if (parseMetaData == null) {
                                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                            return false;
                                        }
                                    } else {
                                        String lname;
                                        if (tagName.equals("library")) {
                                            sa = res.obtainAttributes(parser, R.styleable.AndroidManifestLibrary);
                                            lname = sa.getNonResourceString(0);
                                            sa.recycle();
                                            if (lname != null) {
                                                lname = lname.intern();
                                                if (!ArrayUtils.contains(owner.libraryNames, lname)) {
                                                    owner.libraryNames = ArrayUtils.add(owner.libraryNames, lname);
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            if (tagName.equals("uses-library")) {
                                                sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesLibrary);
                                                lname = sa.getNonResourceString(0);
                                                boolean req = sa.getBoolean(1, true);
                                                sa.recycle();
                                                if (lname != null) {
                                                    lname = lname.intern();
                                                    if (req) {
                                                        owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname);
                                                    } else {
                                                        owner.usesOptionalLibraries = ArrayUtils.add(owner.usesOptionalLibraries, lname);
                                                    }
                                                }
                                                XmlUtils.skipCurrentTag(parser);
                                            } else {
                                                if (tagName.equals("uses-package")) {
                                                    XmlUtils.skipCurrentTag(parser);
                                                } else {
                                                    Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                    XmlUtils.skipCurrentTag(parser);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            modifySharedLibrariesForBackwardCompatibility(owner);
            ApplicationInfo applicationInfo;
            if (hasDomainURLs(owner)) {
                applicationInfo = owner.applicationInfo;
                applicationInfo.privateFlags |= 16;
            } else {
                applicationInfo = owner.applicationInfo;
                applicationInfo.privateFlags &= -17;
            }
            return true;
        }
        sa.recycle();
        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
        return false;
    }

    private static void modifySharedLibrariesForBackwardCompatibility(Package owner) {
        owner.usesLibraries = ArrayUtils.remove(owner.usesLibraries, "org.apache.http.legacy");
        owner.usesOptionalLibraries = ArrayUtils.remove(owner.usesOptionalLibraries, "org.apache.http.legacy");
    }

    private static boolean hasDomainURLs(Package pkg) {
        if (pkg == null || pkg.activities == null) {
            return false;
        }
        ArrayList<Activity> activities = pkg.activities;
        int countActivities = activities.size();
        for (int n = 0; n < countActivities; n++) {
            ArrayList<ActivityIntentInfo> filters = ((Activity) activities.get(n)).intents;
            if (filters != null) {
                int countFilters = filters.size();
                for (int m = 0; m < countFilters; m++) {
                    ActivityIntentInfo aii = (ActivityIntentInfo) filters.get(m);
                    if (aii.hasAction("android.intent.action.VIEW") && aii.hasAction("android.intent.action.VIEW") && (aii.hasDataScheme(IntentFilter.SCHEME_HTTP) || aii.hasDataScheme(IntentFilter.SCHEME_HTTPS))) {
                        return true;
                    }
                }
                continue;
            }
        }
        return false;
    }

    private boolean parseSplitApplication(Package owner, Resources res, XmlResourceParser parser, int flags, int splitIndex, String[] outError) throws XmlPullParserException, IOException {
        if (res.obtainAttributes(parser, R.styleable.AndroidManifestApplication).getBoolean(7, true)) {
            int[] iArr = owner.splitFlags;
            iArr[splitIndex] = iArr[splitIndex] | 4;
        }
        int innerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > innerDepth)) {
                if (!(type == 3 || type == 4)) {
                    String tagName = parser.getName();
                    Activity a;
                    if (tagName.equals(Context.ACTIVITY_SERVICE)) {
                        a = parseActivity(owner, res, parser, flags, outError, false, owner.baseHardwareAccelerated);
                        if (a == null) {
                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                            return false;
                        }
                        owner.activities.add(a);
                    } else {
                        if (tagName.equals("receiver")) {
                            a = parseActivity(owner, res, parser, flags, outError, true, false);
                            if (a == null) {
                                this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                return false;
                            }
                            owner.receivers.add(a);
                        } else {
                            if (tagName.equals(Notification.CATEGORY_SERVICE)) {
                                Service s = parseService(owner, res, parser, flags, outError);
                                if (s == null) {
                                    this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                    return false;
                                }
                                owner.services.add(s);
                            } else {
                                if (tagName.equals("provider")) {
                                    Provider p = parseProvider(owner, res, parser, flags, outError);
                                    if (p == null) {
                                        this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                        return false;
                                    }
                                    owner.providers.add(p);
                                } else {
                                    if (tagName.equals("activity-alias")) {
                                        a = parseActivityAlias(owner, res, parser, flags, outError);
                                        if (a == null) {
                                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                            return false;
                                        }
                                        owner.activities.add(a);
                                    } else if (parser.getName().equals("meta-data")) {
                                        Bundle parseMetaData = parseMetaData(res, parser, owner.mAppMetaData, outError);
                                        owner.mAppMetaData = parseMetaData;
                                        if (parseMetaData == null) {
                                            this.mParseError = PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED;
                                            return false;
                                        }
                                    } else {
                                        if (tagName.equals("uses-library")) {
                                            TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestUsesLibrary);
                                            String lname = sa.getNonResourceString(0);
                                            boolean req = sa.getBoolean(1, true);
                                            sa.recycle();
                                            if (lname != null) {
                                                lname = lname.intern();
                                                if (req) {
                                                    owner.usesLibraries = ArrayUtils.add(owner.usesLibraries, lname);
                                                    owner.usesOptionalLibraries = ArrayUtils.remove(owner.usesOptionalLibraries, lname);
                                                } else if (!ArrayUtils.contains(owner.usesLibraries, lname)) {
                                                    owner.usesOptionalLibraries = ArrayUtils.add(owner.usesOptionalLibraries, lname);
                                                }
                                            }
                                            XmlUtils.skipCurrentTag(parser);
                                        } else {
                                            if (tagName.equals("uses-package")) {
                                                XmlUtils.skipCurrentTag(parser);
                                            } else {
                                                Slog.w(TAG, "Unknown element under <application>: " + tagName + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                                XmlUtils.skipCurrentTag(parser);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private static boolean parsePackageItemInfo(Package owner, PackageItemInfo outInfo, String[] outError, String tag, TypedArray sa, boolean nameRequired, int nameRes, int labelRes, int iconRes, int roundIconRes, int logoRes, int bannerRes) {
        String name = sa.getNonConfigurationString(nameRes, 0);
        if (name != null) {
            outInfo.name = buildClassName(owner.applicationInfo.packageName, name, outError);
            if (outInfo.name == null) {
                return false;
            }
        } else if (nameRequired) {
            outError[0] = tag + " does not specify android:name";
            return false;
        }
        int roundIconVal = Resources.getSystem().getBoolean(17957044) ? sa.getResourceId(roundIconRes, 0) : 0;
        if (roundIconVal != 0) {
            outInfo.icon = roundIconVal;
            outInfo.nonLocalizedLabel = null;
        } else {
            int iconVal = sa.getResourceId(iconRes, 0);
            if (iconVal != 0) {
                outInfo.icon = iconVal;
                outInfo.nonLocalizedLabel = null;
            }
        }
        int logoVal = sa.getResourceId(logoRes, 0);
        if (logoVal != 0) {
            outInfo.logo = logoVal;
        }
        int bannerVal = sa.getResourceId(bannerRes, 0);
        if (bannerVal != 0) {
            outInfo.banner = bannerVal;
        }
        TypedValue v = sa.peekValue(labelRes);
        if (v != null) {
            int i = v.resourceId;
            outInfo.labelRes = i;
            if (i == 0) {
                outInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        outInfo.packageName = owner.packageName;
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:180:0x065c  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Activity parseActivity(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError, boolean receiver, boolean hardwareAccelerated) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestActivity);
        if (this.mParseActivityArgs == null) {
            this.mParseActivityArgs = new ParseComponentArgs(owner, outError, 3, 1, 2, 44, 23, 30, this.mSeparateProcesses, 7, 17, 5);
        }
        this.mParseActivityArgs.tag = receiver ? "<receiver>" : "<activity>";
        this.mParseActivityArgs.sa = sa;
        this.mParseActivityArgs.flags = flags;
        Activity activity = new Activity(this.mParseActivityArgs, new ActivityInfo());
        if (outError[0] != null) {
            sa.recycle();
            return null;
        }
        ActivityInfo activityInfo;
        boolean setExported = sa.hasValue(6);
        if (setExported) {
            activity.info.exported = sa.getBoolean(6, false);
        }
        activity.info.theme = sa.getResourceId(0, 0);
        activity.info.uiOptions = sa.getInt(26, activity.info.applicationInfo.uiOptions);
        String parentName = sa.getNonConfigurationString(27, 1024);
        if (parentName != null) {
            String parentClassName = buildClassName(activity.info.packageName, parentName, outError);
            if (outError[0] == null) {
                activity.info.parentActivityName = parentClassName;
            } else {
                Log.e(TAG, "Activity " + activity.info.name + " specified invalid parentActivityName " + parentName);
                outError[0] = null;
            }
        }
        String str = sa.getNonConfigurationString(4, 0);
        if (str == null) {
            activity.info.permission = owner.applicationInfo.permission;
        } else {
            activity.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        activity.info.taskAffinity = buildTaskAffinityName(owner.applicationInfo.packageName, owner.applicationInfo.taskAffinity, sa.getNonConfigurationString(8, 1024), outError);
        activity.info.flags = 0;
        if (sa.getBoolean(9, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 1;
        }
        if (sa.getBoolean(10, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 2;
        }
        if (sa.getBoolean(11, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 4;
        }
        if (sa.getBoolean(21, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 128;
        }
        if (sa.getBoolean(18, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 8;
        }
        if (sa.getBoolean(12, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 16;
        }
        if (sa.getBoolean(13, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 32;
        }
        if (sa.getBoolean(19, (owner.applicationInfo.flags & 32) != 0)) {
            activityInfo = activity.info;
            activityInfo.flags |= 64;
        }
        if (sa.getBoolean(22, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 256;
        }
        if (sa.getBoolean(29, false) || sa.getBoolean(39, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 1024;
        }
        if (sa.getBoolean(24, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 2048;
        }
        if (sa.getBoolean(45, false)) {
            activityInfo = activity.info;
            activityInfo.flags |= 536870912;
        }
        boolean z;
        if (receiver) {
            activity.info.launchMode = 0;
            activity.info.configChanges = 0;
            if (sa.getBoolean(28, false)) {
                activityInfo = activity.info;
                activityInfo.flags |= 1073741824;
                if (activity.info.exported && (flags & 128) == 0) {
                    Slog.w(TAG, "Activity exported request ignored due to singleUser: " + activity.className + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    activity.info.exported = false;
                    setExported = true;
                }
            }
            activityInfo = activity.info;
            z = sa.getBoolean(42, false);
            activity.info.directBootAware = z;
            activityInfo.encryptionAware = z;
        } else {
            if (sa.getBoolean(25, hardwareAccelerated)) {
                activityInfo = activity.info;
                activityInfo.flags |= 512;
            }
            activity.info.launchMode = sa.getInt(14, 0);
            activity.info.documentLaunchMode = sa.getInt(33, 0);
            activity.info.maxRecents = sa.getInt(34, ActivityManager.getDefaultAppRecentsLimitStatic());
            activity.info.configChanges = sa.getInt(16, 0);
            activity.info.softInputMode = sa.getInt(20, 0);
            activity.info.persistableMode = sa.getInteger(32, 0);
            if (sa.getBoolean(31, false)) {
                activityInfo = activity.info;
                activityInfo.flags |= Integer.MIN_VALUE;
            }
            if (sa.getBoolean(35, false)) {
                activityInfo = activity.info;
                activityInfo.flags |= 8192;
            }
            if (sa.getBoolean(36, false)) {
                activityInfo = activity.info;
                activityInfo.flags |= 4096;
            }
            if (sa.getBoolean(37, false)) {
                activityInfo = activity.info;
                activityInfo.flags |= 16384;
            }
            activity.info.screenOrientation = sa.getInt(15, -1);
            activity.info.resizeMode = 0;
            boolean appDefault = (owner.applicationInfo.privateFlags & 2048) != 0;
            boolean resizeableSetExplicitly = sa.hasValue(40);
            if (sa.getBoolean(40, appDefault)) {
                if (sa.getBoolean(41, false)) {
                    activity.info.resizeMode = 3;
                } else {
                    activity.info.resizeMode = 2;
                }
            } else if (owner.applicationInfo.targetSdkVersion >= 24 || resizeableSetExplicitly) {
                activity.info.resizeMode = 0;
            } else if (!activity.info.isFixedOrientation() && (activity.info.flags & 2048) == 0) {
                activity.info.resizeMode = 4;
            }
            if (sa.getBoolean(46, false)) {
                activityInfo = activity.info;
                activityInfo.flags |= 262144;
            }
            activity.info.lockTaskLaunchMode = sa.getInt(38, 0);
            activityInfo = activity.info;
            z = sa.getBoolean(42, false);
            activity.info.directBootAware = z;
            activityInfo.encryptionAware = z;
            activity.info.requestedVrComponent = sa.getString(43);
        }
        if (activity.info.directBootAware) {
            ApplicationInfo applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags |= 256;
        }
        sa.recycle();
        if (receiver && (owner.applicationInfo.privateFlags & 2) != 0 && activity.info.processName == owner.packageName) {
            outError[0] = "Heavy-weight applications can not have receivers in main process";
        }
        if (outError[0] != null) {
            return null;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!setExported) {
                    activity.info.exported = activity.intents.size() > 0;
                }
            } else if (!(type == 3 || type == 4)) {
                ActivityIntentInfo intent;
                if (parser.getName().equals("intent-filter")) {
                    intent = new ActivityIntentInfo(activity);
                    if (!parseIntent(res, parser, true, true, intent, outError)) {
                        return null;
                    }
                    if (intent.countActions() != 0) {
                        activity.intents.add(intent);
                    }
                } else if (!receiver && parser.getName().equals("preferred")) {
                    intent = new ActivityIntentInfo(activity);
                    if (!parseIntent(res, parser, false, false, intent, outError)) {
                        return null;
                    }
                    if (intent.countActions() == 0) {
                        Slog.w(TAG, "No actions in preferred at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    } else {
                        if (owner.preferredActivityFilters == null) {
                            owner.preferredActivityFilters = new ArrayList();
                        }
                        owner.preferredActivityFilters.add(intent);
                    }
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, activity.metaData, outError);
                    activity.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return null;
                    }
                } else if (receiver || !parser.getName().equals(TtmlUtils.TAG_LAYOUT)) {
                    if (receiver) {
                        Slog.w(TAG, "Unknown element under <receiver>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    } else {
                        Slog.w(TAG, "Unknown element under <activity>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    }
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    parseLayout(res, parser, activity);
                }
            }
        }
        if (setExported) {
        }
        return activity;
    }

    private void parseLayout(Resources res, AttributeSet attrs, Activity a) {
        TypedArray sw = res.obtainAttributes(attrs, R.styleable.AndroidManifestLayout);
        int width = -1;
        float widthFraction = -1.0f;
        int height = -1;
        float heightFraction = -1.0f;
        int widthType = sw.getType(3);
        if (widthType == 6) {
            widthFraction = sw.getFraction(3, 1, 1, -1.0f);
        } else if (widthType == 5) {
            width = sw.getDimensionPixelSize(3, -1);
        }
        int heightType = sw.getType(4);
        if (heightType == 6) {
            heightFraction = sw.getFraction(4, 1, 1, -1.0f);
        } else if (heightType == 5) {
            height = sw.getDimensionPixelSize(4, -1);
        }
        int gravity = sw.getInt(0, 17);
        int minWidth = sw.getDimensionPixelSize(1, -1);
        int minHeight = sw.getDimensionPixelSize(2, -1);
        sw.recycle();
        a.info.windowLayout = new WindowLayout(width, widthFraction, height, heightFraction, gravity, minWidth, minHeight);
    }

    /* JADX WARNING: Removed duplicated region for block: B:76:0x0336  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    @OppoHook(level = OppoHookType.CHANGE_CODE, note = "YaoJun.Luo@Plf.SDK : Modify for rom activity-alias can set theme", property = OppoRomType.ROM)
    private Activity parseActivityAlias(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestActivityAlias);
        String targetActivity = sa.getNonConfigurationString(8, 1024);
        if (targetActivity == null) {
            outError[0] = "<activity-alias> does not specify android:targetActivity";
            sa.recycle();
            return null;
        }
        targetActivity = buildClassName(owner.applicationInfo.packageName, targetActivity, outError);
        if (targetActivity == null) {
            sa.recycle();
            return null;
        }
        if (this.mParseActivityAliasArgs == null) {
            this.mParseActivityAliasArgs = new ParseComponentArgs(owner, outError, 3, 1, 2, 12, 9, 11, this.mSeparateProcesses, 0, 7, 5);
            this.mParseActivityAliasArgs.tag = "<activity-alias>";
        }
        this.mParseActivityAliasArgs.sa = sa;
        this.mParseActivityAliasArgs.flags = flags;
        Activity target = null;
        int NA = owner.activities.size();
        for (int i = 0; i < NA; i++) {
            Activity t = (Activity) owner.activities.get(i);
            if (targetActivity.equals(t.info.name)) {
                target = t;
                break;
            }
        }
        if (target == null) {
            outError[0] = "<activity-alias> target activity " + targetActivity + " not found in manifest";
            sa.recycle();
            return null;
        }
        ActivityInfo info = new ActivityInfo();
        info.targetActivity = targetActivity;
        info.configChanges = target.info.configChanges;
        info.flags = target.info.flags;
        info.icon = target.info.icon;
        info.logo = target.info.logo;
        info.banner = target.info.banner;
        info.labelRes = target.info.labelRes;
        info.nonLocalizedLabel = target.info.nonLocalizedLabel;
        info.launchMode = target.info.launchMode;
        info.lockTaskLaunchMode = target.info.lockTaskLaunchMode;
        info.processName = target.info.processName;
        if (info.descriptionRes == 0) {
            info.descriptionRes = target.info.descriptionRes;
        }
        info.screenOrientation = target.info.screenOrientation;
        info.taskAffinity = target.info.taskAffinity;
        info.theme = target.info.theme;
        info.softInputMode = target.info.softInputMode;
        info.uiOptions = target.info.uiOptions;
        info.parentActivityName = target.info.parentActivityName;
        info.maxRecents = target.info.maxRecents;
        info.windowLayout = target.info.windowLayout;
        info.resizeMode = target.info.resizeMode;
        boolean z = target.info.directBootAware;
        info.directBootAware = z;
        info.encryptionAware = z;
        Activity activity = new Activity(this.mParseActivityAliasArgs, info);
        if (outError[0] != null) {
            sa.recycle();
            return null;
        }
        activity.info.theme = sa.getResourceId(0, target.info.theme);
        boolean setExported = sa.hasValue(6);
        if (setExported) {
            activity.info.exported = sa.getBoolean(6, false);
        }
        String str = sa.getNonConfigurationString(4, 0);
        if (str != null) {
            activity.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        String parentName = sa.getNonConfigurationString(10, 1024);
        if (parentName != null) {
            String parentClassName = buildClassName(activity.info.packageName, parentName, outError);
            if (outError[0] == null) {
                activity.info.parentActivityName = parentClassName;
            } else {
                Log.e(TAG, "Activity alias " + activity.info.name + " specified invalid parentActivityName " + parentName);
                outError[0] = null;
            }
        }
        sa.recycle();
        if (outError[0] != null) {
            return null;
        }
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                if (!setExported) {
                    activity.info.exported = activity.intents.size() > 0;
                }
            } else if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("intent-filter")) {
                    ActivityIntentInfo intent = new ActivityIntentInfo(activity);
                    if (!parseIntent(res, parser, true, true, intent, outError)) {
                        return null;
                    }
                    if (intent.countActions() != 0) {
                        activity.intents.add(intent);
                    }
                } else if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, activity.metaData, outError);
                    activity.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return null;
                    }
                } else {
                    Slog.w(TAG, "Unknown element under <activity-alias>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        if (setExported) {
        }
        return activity;
    }

    private Provider parseProvider(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestProvider);
        if (this.mParseProviderArgs == null) {
            this.mParseProviderArgs = new ParseComponentArgs(owner, outError, 2, 0, 1, 19, 15, 17, this.mSeparateProcesses, 8, 14, 6);
            this.mParseProviderArgs.tag = "<provider>";
        }
        this.mParseProviderArgs.sa = sa;
        this.mParseProviderArgs.flags = flags;
        Provider provider = new Provider(this.mParseProviderArgs, new ProviderInfo());
        if (outError[0] != null) {
            sa.recycle();
            return null;
        }
        ProviderInfo providerInfo;
        boolean providerExportedDefault = false;
        if (owner.applicationInfo.targetSdkVersion < 17) {
            providerExportedDefault = true;
        }
        provider.info.exported = sa.getBoolean(7, providerExportedDefault);
        String cpname = sa.getNonConfigurationString(10, 0);
        provider.info.isSyncable = sa.getBoolean(11, false);
        String permission = sa.getNonConfigurationString(3, 0);
        String str = sa.getNonConfigurationString(4, 0);
        if (str == null) {
            str = permission;
        }
        if (str == null) {
            provider.info.readPermission = owner.applicationInfo.permission;
        } else {
            provider.info.readPermission = str.length() > 0 ? str.toString().intern() : null;
        }
        str = sa.getNonConfigurationString(5, 0);
        if (str == null) {
            str = permission;
        }
        if (str == null) {
            provider.info.writePermission = owner.applicationInfo.permission;
        } else {
            provider.info.writePermission = str.length() > 0 ? str.toString().intern() : null;
        }
        provider.info.grantUriPermissions = sa.getBoolean(13, false);
        provider.info.multiprocess = sa.getBoolean(9, false);
        provider.info.initOrder = sa.getInt(12, 0);
        provider.info.flags = 0;
        if (sa.getBoolean(16, false)) {
            providerInfo = provider.info;
            providerInfo.flags |= 1073741824;
            if (provider.info.exported && (flags & 128) == 0) {
                Slog.w(TAG, "Provider exported request ignored due to singleUser: " + provider.className + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                provider.info.exported = false;
            }
        }
        providerInfo = provider.info;
        boolean z = sa.getBoolean(18, false);
        provider.info.directBootAware = z;
        providerInfo.encryptionAware = z;
        if (provider.info.directBootAware) {
            ApplicationInfo applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags |= 256;
        }
        sa.recycle();
        if ((owner.applicationInfo.privateFlags & 2) != 0 && provider.info.processName == owner.packageName) {
            outError[0] = "Heavy-weight applications can not have providers in main process";
            return null;
        } else if (cpname == null) {
            outError[0] = "<provider> does not include authorities attribute";
            return null;
        } else if (cpname.length() <= 0) {
            outError[0] = "<provider> has empty authorities attribute";
            return null;
        } else {
            provider.info.authority = cpname.intern();
            if (parseProviderTags(res, parser, provider, outError)) {
                return provider;
            }
            return null;
        }
    }

    private boolean parseProviderTags(Resources res, XmlResourceParser parser, Provider outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type != 1 && (type != 3 || parser.getDepth() > outerDepth)) {
                if (!(type == 3 || type == 4)) {
                    TypedArray sa;
                    int N;
                    if (parser.getName().equals("intent-filter")) {
                        ProviderIntentInfo intent = new ProviderIntentInfo(outInfo);
                        if (!parseIntent(res, parser, true, false, intent, outError)) {
                            return false;
                        }
                        outInfo.intents.add(intent);
                    } else if (parser.getName().equals("meta-data")) {
                        Bundle parseMetaData = parseMetaData(res, parser, outInfo.metaData, outError);
                        outInfo.metaData = parseMetaData;
                        if (parseMetaData == null) {
                            return false;
                        }
                    } else if (parser.getName().equals("grant-uri-permission")) {
                        PatternMatcher patternMatcher;
                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestGrantUriPermission);
                        PatternMatcher patternMatcher2 = null;
                        String str = sa.getNonConfigurationString(0, 0);
                        if (str != null) {
                            patternMatcher = new PatternMatcher(str, 0);
                        }
                        str = sa.getNonConfigurationString(1, 0);
                        if (str != null) {
                            patternMatcher = new PatternMatcher(str, 1);
                        }
                        str = sa.getNonConfigurationString(2, 0);
                        if (str != null) {
                            patternMatcher = new PatternMatcher(str, 2);
                        }
                        sa.recycle();
                        if (patternMatcher2 != null) {
                            if (outInfo.info.uriPermissionPatterns == null) {
                                outInfo.info.uriPermissionPatterns = new PatternMatcher[1];
                                outInfo.info.uriPermissionPatterns[0] = patternMatcher2;
                            } else {
                                N = outInfo.info.uriPermissionPatterns.length;
                                PatternMatcher[] newp = new PatternMatcher[(N + 1)];
                                System.arraycopy(outInfo.info.uriPermissionPatterns, 0, newp, 0, N);
                                newp[N] = patternMatcher2;
                                outInfo.info.uriPermissionPatterns = newp;
                            }
                            outInfo.info.grantUriPermissions = true;
                            XmlUtils.skipCurrentTag(parser);
                        } else {
                            Slog.w(TAG, "Unknown element under <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    } else if (parser.getName().equals("path-permission")) {
                        sa = res.obtainAttributes(parser, R.styleable.AndroidManifestPathPermission);
                        PathPermission pathPermission = null;
                        String permission = sa.getNonConfigurationString(0, 0);
                        String readPermission = sa.getNonConfigurationString(1, 0);
                        if (readPermission == null) {
                            readPermission = permission;
                        }
                        String writePermission = sa.getNonConfigurationString(2, 0);
                        if (writePermission == null) {
                            writePermission = permission;
                        }
                        boolean havePerm = false;
                        if (readPermission != null) {
                            readPermission = readPermission.intern();
                            havePerm = true;
                        }
                        if (writePermission != null) {
                            writePermission = writePermission.intern();
                            havePerm = true;
                        }
                        if (havePerm) {
                            PathPermission pathPermission2;
                            String path = sa.getNonConfigurationString(3, 0);
                            if (path != null) {
                                pathPermission2 = new PathPermission(path, 0, readPermission, writePermission);
                            }
                            path = sa.getNonConfigurationString(4, 0);
                            if (path != null) {
                                pathPermission2 = new PathPermission(path, 1, readPermission, writePermission);
                            }
                            path = sa.getNonConfigurationString(5, 0);
                            if (path != null) {
                                pathPermission2 = new PathPermission(path, 2, readPermission, writePermission);
                            }
                            sa.recycle();
                            if (pathPermission != null) {
                                if (outInfo.info.pathPermissions == null) {
                                    outInfo.info.pathPermissions = new PathPermission[1];
                                    outInfo.info.pathPermissions[0] = pathPermission;
                                } else {
                                    N = outInfo.info.pathPermissions.length;
                                    PathPermission[] newp2 = new PathPermission[(N + 1)];
                                    System.arraycopy(outInfo.info.pathPermissions, 0, newp2, 0, N);
                                    newp2[N] = pathPermission;
                                    outInfo.info.pathPermissions = newp2;
                                }
                                XmlUtils.skipCurrentTag(parser);
                            } else {
                                Slog.w(TAG, "No path, pathPrefix, or pathPattern for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                                XmlUtils.skipCurrentTag(parser);
                            }
                        } else {
                            Slog.w(TAG, "No readPermission or writePermssion for <path-permission>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                            XmlUtils.skipCurrentTag(parser);
                        }
                    } else {
                        Slog.w(TAG, "Unknown element under <provider>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
        }
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:69:0x0257  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private Service parseService(Package owner, Resources res, XmlResourceParser parser, int flags, String[] outError) throws XmlPullParserException, IOException {
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestService);
        if (this.mParseServiceArgs == null) {
            this.mParseServiceArgs = new ParseComponentArgs(owner, outError, 2, 0, 1, 15, 8, 12, this.mSeparateProcesses, 6, 7, 4);
            this.mParseServiceArgs.tag = "<service>";
        }
        this.mParseServiceArgs.sa = sa;
        this.mParseServiceArgs.flags = flags;
        Service service = new Service(this.mParseServiceArgs, new ServiceInfo());
        if (outError[0] != null) {
            sa.recycle();
            return null;
        }
        ServiceInfo serviceInfo;
        boolean setExported = sa.hasValue(5);
        if (setExported) {
            service.info.exported = sa.getBoolean(5, false);
        }
        String str = sa.getNonConfigurationString(3, 0);
        if (str == null) {
            service.info.permission = owner.applicationInfo.permission;
        } else {
            service.info.permission = str.length() > 0 ? str.toString().intern() : null;
        }
        service.info.flags = 0;
        if (sa.getBoolean(9, false)) {
            serviceInfo = service.info;
            serviceInfo.flags |= 1;
        }
        if (sa.getBoolean(10, false)) {
            serviceInfo = service.info;
            serviceInfo.flags |= 2;
        }
        if (sa.getBoolean(14, false)) {
            serviceInfo = service.info;
            serviceInfo.flags |= 4;
        }
        if (sa.getBoolean(11, false)) {
            serviceInfo = service.info;
            serviceInfo.flags |= 1073741824;
            if (service.info.exported && (flags & 128) == 0) {
                Slog.w(TAG, "Service exported request ignored due to singleUser: " + service.className + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                service.info.exported = false;
                setExported = true;
            }
        }
        serviceInfo = service.info;
        boolean z = sa.getBoolean(13, false);
        service.info.directBootAware = z;
        serviceInfo.encryptionAware = z;
        if (service.info.directBootAware) {
            ApplicationInfo applicationInfo = owner.applicationInfo;
            applicationInfo.privateFlags |= 256;
        }
        sa.recycle();
        if ((owner.applicationInfo.privateFlags & 2) == 0 || service.info.processName != owner.packageName) {
            int outerDepth = parser.getDepth();
            while (true) {
                int type = parser.next();
                if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                    if (!setExported) {
                        service.info.exported = service.intents.size() > 0;
                    }
                } else if (!(type == 3 || type == 4)) {
                    if (parser.getName().equals("intent-filter")) {
                        ServiceIntentInfo intent = new ServiceIntentInfo(service);
                        if (!parseIntent(res, parser, true, false, intent, outError)) {
                            return null;
                        }
                        service.intents.add(intent);
                    } else if (parser.getName().equals("meta-data")) {
                        Bundle parseMetaData = parseMetaData(res, parser, service.metaData, outError);
                        service.metaData = parseMetaData;
                        if (parseMetaData == null) {
                            return null;
                        }
                    } else {
                        Slog.w(TAG, "Unknown element under <service>: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                        XmlUtils.skipCurrentTag(parser);
                    }
                }
            }
            if (setExported) {
            }
            return service;
        }
        outError[0] = "Heavy-weight applications can not have services in main process";
        return null;
    }

    private boolean parseAllMetaData(Resources res, XmlResourceParser parser, String tag, Component<?> outInfo, String[] outError) throws XmlPullParserException, IOException {
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                return true;
            }
            if (!(type == 3 || type == 4)) {
                if (parser.getName().equals("meta-data")) {
                    Bundle parseMetaData = parseMetaData(res, parser, outInfo.metaData, outError);
                    outInfo.metaData = parseMetaData;
                    if (parseMetaData == null) {
                        return false;
                    }
                } else {
                    Slog.w(TAG, "Unknown element under " + tag + ": " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        return true;
    }

    private Bundle parseMetaData(Resources res, XmlResourceParser parser, Bundle data, String[] outError) throws XmlPullParserException, IOException {
        String str = null;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestMetaData);
        if (data == null) {
            data = new Bundle();
        }
        String name = sa.getNonConfigurationString(0, 0);
        if (name == null) {
            outError[0] = "<meta-data> requires an android:name attribute";
            sa.recycle();
            return null;
        }
        name = name.intern();
        TypedValue v = sa.peekValue(2);
        if (v == null || v.resourceId == 0) {
            v = sa.peekValue(1);
            if (v == null) {
                outError[0] = "<meta-data> requires an android:value or android:resource attribute";
                data = null;
            } else if (v.type == 3) {
                CharSequence cs = v.coerceToString();
                if (cs != null) {
                    str = cs.toString().intern();
                }
                data.putString(name, str);
            } else if (v.type == 18) {
                data.putBoolean(name, v.data != 0);
            } else if (v.type >= 16 && v.type <= 31) {
                data.putInt(name, v.data);
            } else if (v.type == 4) {
                data.putFloat(name, v.getFloat());
            } else {
                Slog.w(TAG, "<meta-data> only supports string, integer, float, color, boolean, and resource reference types: " + parser.getName() + " at " + this.mArchiveSourcePath + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + parser.getPositionDescription());
            }
        } else {
            data.putInt(name, v.resourceId);
        }
        sa.recycle();
        XmlUtils.skipCurrentTag(parser);
        return data;
    }

    private static VerifierInfo parseVerifier(Resources res, XmlPullParser parser, AttributeSet attrs, int flags) {
        TypedArray sa = res.obtainAttributes(attrs, R.styleable.AndroidManifestPackageVerifier);
        String packageName = sa.getNonResourceString(0);
        String encodedPublicKey = sa.getNonResourceString(1);
        sa.recycle();
        if (packageName == null || packageName.length() == 0) {
            Slog.i(TAG, "verifier package name was null; skipping");
            return null;
        }
        PublicKey publicKey = parsePublicKey(encodedPublicKey);
        if (publicKey != null) {
            return new VerifierInfo(packageName, publicKey);
        }
        Slog.i(TAG, "Unable to parse verifier public key for " + packageName);
        return null;
    }

    public static final PublicKey parsePublicKey(String encodedPublicKey) {
        EncodedKeySpec keySpec;
        if (encodedPublicKey == null) {
            Slog.w(TAG, "Could not parse null public key");
            return null;
        }
        try {
            keySpec = new X509EncodedKeySpec(Base64.decode(encodedPublicKey, 0));
            try {
                return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_RSA).generatePublic(keySpec);
            } catch (NoSuchAlgorithmException e) {
                Slog.wtf(TAG, "Could not parse public key: RSA KeyFactory not included in build");
            } catch (InvalidKeySpecException e2) {
            }
        } catch (IllegalArgumentException e3) {
            Slog.w(TAG, "Could not parse verifier public key; invalid Base64");
            return null;
        }
        return null;
        try {
            return KeyFactory.getInstance(KeyProperties.KEY_ALGORITHM_EC).generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e4) {
            Slog.wtf(TAG, "Could not parse public key: EC KeyFactory not included in build");
        } catch (InvalidKeySpecException e5) {
        }
        try {
            return KeyFactory.getInstance("DSA").generatePublic(keySpec);
        } catch (NoSuchAlgorithmException e6) {
            Slog.wtf(TAG, "Could not parse public key: DSA KeyFactory not included in build");
        } catch (InvalidKeySpecException e7) {
        }
    }

    /* JADX WARNING: Missing block: B:31:0x00bc, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:86:0x01fb, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean parseIntent(Resources res, XmlResourceParser parser, boolean allowGlobs, boolean allowAutoVerify, IntentInfo outInfo, String[] outError) throws XmlPullParserException, IOException {
        int roundIconVal;
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestIntentFilter);
        outInfo.setPriority(sa.getInt(2, 0));
        TypedValue v = sa.peekValue(0);
        if (v != null) {
            int i = v.resourceId;
            outInfo.labelRes = i;
            if (i == 0) {
                outInfo.nonLocalizedLabel = v.coerceToString();
            }
        }
        if (Resources.getSystem().getBoolean(17957044)) {
            roundIconVal = sa.getResourceId(6, 0);
        } else {
            roundIconVal = 0;
        }
        if (roundIconVal != 0) {
            outInfo.icon = roundIconVal;
        } else {
            outInfo.icon = sa.getResourceId(1, 0);
        }
        outInfo.logo = sa.getResourceId(3, 0);
        outInfo.banner = sa.getResourceId(4, 0);
        if (allowAutoVerify) {
            outInfo.setAutoVerify(sa.getBoolean(5, false));
        }
        sa.recycle();
        int outerDepth = parser.getDepth();
        while (true) {
            int type = parser.next();
            if (type == 1 || (type == 3 && parser.getDepth() <= outerDepth)) {
                outInfo.hasDefault = outInfo.hasCategory(Intent.CATEGORY_DEFAULT);
            } else if (!(type == 3 || type == 4)) {
                String nodeName = parser.getName();
                String value;
                if (nodeName.equals(Parameters.SCENE_MODE_ACTION)) {
                    value = parser.getAttributeValue(ANDROID_RESOURCES, "name");
                    if (value == null || value == "") {
                        outError[0] = "No value supplied for <android:name>";
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                        outInfo.addAction(value);
                    }
                } else if (nodeName.equals("category")) {
                    value = parser.getAttributeValue(ANDROID_RESOURCES, "name");
                    if (value == null || value == "") {
                        outError[0] = "No value supplied for <android:name>";
                    } else {
                        XmlUtils.skipCurrentTag(parser);
                        outInfo.addCategory(value);
                    }
                } else if (nodeName.equals("data")) {
                    sa = res.obtainAttributes(parser, R.styleable.AndroidManifestData);
                    String str = sa.getNonConfigurationString(0, 0);
                    if (str != null) {
                        try {
                            outInfo.addDataType(str);
                        } catch (MalformedMimeTypeException e) {
                            outError[0] = e.toString();
                            sa.recycle();
                            return false;
                        }
                    }
                    str = sa.getNonConfigurationString(1, 0);
                    if (str != null) {
                        outInfo.addDataScheme(str);
                    }
                    str = sa.getNonConfigurationString(7, 0);
                    if (str != null) {
                        outInfo.addDataSchemeSpecificPart(str, 0);
                    }
                    str = sa.getNonConfigurationString(8, 0);
                    if (str != null) {
                        outInfo.addDataSchemeSpecificPart(str, 1);
                    }
                    str = sa.getNonConfigurationString(9, 0);
                    if (str != null) {
                        if (allowGlobs) {
                            outInfo.addDataSchemeSpecificPart(str, 2);
                        } else {
                            outError[0] = "sspPattern not allowed here; ssp must be literal";
                            return false;
                        }
                    }
                    String host = sa.getNonConfigurationString(2, 0);
                    String port = sa.getNonConfigurationString(3, 0);
                    if (host != null) {
                        outInfo.addDataAuthority(host, port);
                    }
                    str = sa.getNonConfigurationString(4, 0);
                    if (str != null) {
                        outInfo.addDataPath(str, 0);
                    }
                    str = sa.getNonConfigurationString(5, 0);
                    if (str != null) {
                        outInfo.addDataPath(str, 1);
                    }
                    str = sa.getNonConfigurationString(6, 0);
                    if (str != null) {
                        if (allowGlobs) {
                            outInfo.addDataPath(str, 2);
                        } else {
                            outError[0] = "pathPattern not allowed here; path must be literal";
                            return false;
                        }
                    }
                    sa.recycle();
                    XmlUtils.skipCurrentTag(parser);
                } else {
                    XmlUtils.skipCurrentTag(parser);
                }
            }
        }
        outError[0] = "No value supplied for <android:name>";
        return false;
    }

    /* JADX WARNING: Missing block: B:23:0x0030, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean copyNeeded(int flags, Package p, PackageUserState state, Bundle metaData, int userId) {
        if (userId != 0) {
            return true;
        }
        if (state.enabled != 0) {
            if (p.applicationInfo.enabled != (state.enabled == 1)) {
                return true;
            }
        }
        if (state.suspended != ((p.applicationInfo.flags & 1073741824) != 0) || !state.installed || state.hidden || state.stopped) {
            return true;
        }
        if ((flags & 128) == 0 || (metaData == null && p.mAppMetaData == null)) {
            return ((flags & 1024) == 0 || p.usesLibraryFiles == null) ? false : true;
        } else {
            return true;
        }
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state) {
        return generateApplicationInfo(p, flags, state, UserHandle.getCallingUserId());
    }

    private static void updateApplicationInfo(ApplicationInfo ai, int flags, PackageUserState state) {
        boolean z = true;
        if (!sCompatibilityModeEnabled) {
            ai.disableCompatibilityMode();
        }
        if (state.installed) {
            ai.flags |= 8388608;
        } else {
            ai.flags &= -8388609;
        }
        if (state.suspended) {
            ai.flags |= 1073741824;
        } else {
            ai.flags &= -1073741825;
        }
        if (state.hidden) {
            ai.privateFlags |= 1;
        } else {
            ai.privateFlags &= -2;
        }
        if (state.enabled == 1) {
            ai.enabled = true;
        } else if (state.enabled == 4) {
            if ((32768 & flags) == 0) {
                z = false;
            }
            ai.enabled = z;
        } else if (state.enabled == 2 || state.enabled == 3) {
            ai.enabled = false;
        }
        ai.enabledSetting = state.enabled;
    }

    public static ApplicationInfo generateApplicationInfo(Package p, int flags, PackageUserState state, int userId) {
        if (p == null || !checkUseInstalledOrHidden(flags, state) || !p.isMatch(flags)) {
            return null;
        }
        if (!(userId != 999 || p == null || p.applicationInfo == null || p.applicationInfo.packageName == null || OppoMultiLauncherUtil.getInstance().isMultiApp(p.applicationInfo.packageName))) {
            userId = 0;
            p.applicationInfo.uid = UserHandle.getUid(0, p.applicationInfo.uid);
        }
        if (copyNeeded(flags, p, state, null, userId) || ((32768 & flags) != 0 && state.enabled == 4)) {
            ApplicationInfo ai = new ApplicationInfo(p.applicationInfo);
            ai.initForUser(userId);
            if ((flags & 128) != 0) {
                ai.metaData = p.mAppMetaData;
            }
            if ((flags & 1024) != 0) {
                ai.sharedLibraryFiles = p.usesLibraryFiles;
            }
            if (state.stopped) {
                ai.flags |= 2097152;
            } else {
                ai.flags &= -2097153;
            }
            updateApplicationInfo(ai, flags, state);
            return ai;
        }
        updateApplicationInfo(p.applicationInfo, flags, state);
        return p.applicationInfo;
    }

    public static ApplicationInfo generateApplicationInfo(ApplicationInfo ai, int flags, PackageUserState state, int userId) {
        if (ai == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        ApplicationInfo ai2 = new ApplicationInfo(ai);
        ai2.initForUser(userId);
        if (state.stopped) {
            ai2.flags |= 2097152;
        } else {
            ai2.flags &= -2097153;
        }
        updateApplicationInfo(ai2, flags, state);
        return ai2;
    }

    public static final PermissionInfo generatePermissionInfo(Permission p, int flags) {
        if (p == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return p.info;
        }
        PermissionInfo pi = new PermissionInfo(p.info);
        pi.metaData = p.metaData;
        return pi;
    }

    public static final PermissionGroupInfo generatePermissionGroupInfo(PermissionGroup pg, int flags) {
        if (pg == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return pg.info;
        }
        PermissionGroupInfo pgi = new PermissionGroupInfo(pg.info);
        pgi.metaData = pg.metaData;
        return pgi;
    }

    public static final ActivityInfo generateActivityInfo(Activity a, int flags, PackageUserState state, int userId) {
        if (a == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, a.owner, state, a.metaData, userId)) {
            return a.info;
        }
        ActivityInfo ai = new ActivityInfo(a.info);
        ai.metaData = a.metaData;
        ai.applicationInfo = generateApplicationInfo(a.owner, flags, state, userId);
        return ai;
    }

    public static final ActivityInfo generateActivityInfo(ActivityInfo ai, int flags, PackageUserState state, int userId) {
        if (ai == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        ActivityInfo ai2 = new ActivityInfo(ai);
        ai2.applicationInfo = generateApplicationInfo(ai2.applicationInfo, flags, state, userId);
        return ai2;
    }

    public static final ServiceInfo generateServiceInfo(Service s, int flags, PackageUserState state, int userId) {
        if (s == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, s.owner, state, s.metaData, userId)) {
            return s.info;
        }
        ServiceInfo si = new ServiceInfo(s.info);
        si.metaData = s.metaData;
        si.applicationInfo = generateApplicationInfo(s.owner, flags, state, userId);
        return si;
    }

    public static final ProviderInfo generateProviderInfo(Provider p, int flags, PackageUserState state, int userId) {
        if (p == null || !checkUseInstalledOrHidden(flags, state)) {
            return null;
        }
        if (!copyNeeded(flags, p.owner, state, p.metaData, userId) && ((flags & 2048) != 0 || p.info.uriPermissionPatterns == null)) {
            return p.info;
        }
        ProviderInfo pi = new ProviderInfo(p.info);
        pi.metaData = p.metaData;
        if ((flags & 2048) == 0) {
            pi.uriPermissionPatterns = null;
        }
        pi.applicationInfo = generateApplicationInfo(p.owner, flags, state, userId);
        return pi;
    }

    public static final InstrumentationInfo generateInstrumentationInfo(Instrumentation i, int flags) {
        if (i == null) {
            return null;
        }
        if ((flags & 128) == 0) {
            return i.info;
        }
        InstrumentationInfo ii = new InstrumentationInfo(i.info);
        ii.metaData = i.metaData;
        return ii;
    }

    public static void setCompatibilityModeEnabled(boolean compatibilityModeEnabled) {
        sCompatibilityModeEnabled = compatibilityModeEnabled;
    }

    public static long readFullyIgnoringContents(InputStream in) throws IOException {
        byte[] buffer = (byte[]) sBuffer.getAndSet(null);
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
                return (long) count;
            }
        }
    }

    public static void closeQuietly(StrictJarFile jarFile) {
        if (jarFile != null) {
            try {
                jarFile.close();
            } catch (Exception e) {
            }
        }
    }

    public void verifyEMMApkIfNeed(Context context, Package pkg) throws PackageParserException {
        for (String usesPerm : pkg.requestedPermissions) {
            if ("com.chinatelecom.permission.security.EMM".equals(usesPerm)) {
                verifyEMMCert(context, pkg);
                return;
            }
        }
    }

    private byte[] inputStream2ByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[256];
        while (true) {
            int nRead = is.read(data, 0, data.length);
            if (nRead != -1) {
                buffer.write(data, 0, nRead);
            } else {
                buffer.flush();
                return buffer.toByteArray();
            }
        }
    }

    private void verifyEMMCert(Context context, Package pkg) throws PackageParserException {
        byte[] emmCertBytes = getEMMCert(pkg);
        if (emmCertBytes == null) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Failed to get META-INF/EMM.CER");
        }
        String emmCert = new String(emmCertBytes);
        int index = emmCert.indexOf("Signature:");
        if (index == -1) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Signature of EMM.CER is missing");
        }
        Properties props = new Properties();
        try {
            props.load(new ByteArrayInputStream(emmCertBytes));
        } catch (IOException e) {
        }
        String signature = props.getProperty("Signature");
        if (signature == null) {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Signature of EMM.CER is missing");
        } else if (checkSignature(emmCert.substring(0, index), signature)) {
            String apkHash = props.getProperty("ApkHash");
            String deviceIDs = props.getProperty("DeviceIds");
            if ((apkHash != null && !NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER.equals(apkHash.trim())) || (deviceIDs != null && !NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER.equals(deviceIDs.trim()))) {
                ManifestDigest digest = getManifestDigest(pkg);
                if (digest == null) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Failed to get META-INF/MANIFEST.MF");
                }
                if (!(apkHash == null || NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER.equals(apkHash.trim()))) {
                    ManifestDigest apkHashDigest = new ManifestDigest(hexStringToByteArray(apkHash));
                    if (!apkHashDigest.equals(digest)) {
                        throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Package has mismatched apk hash: expected " + apkHashDigest + ", got " + digest);
                    }
                }
                if (!validateDeviceIDs(context, props)) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "IMEI isn't in the device list");
                } else if (!validateFromTo(props)) {
                    throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Validity of the certificate has expired");
                }
            } else if (NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER.equals(apkHash) && NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER.equals(deviceIDs)) {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Both ApkHash and DeviceIDs are '*'");
            } else {
                throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Both ApkHash and DeviceIDs are missing");
            }
        } else {
            throw new PackageParserException(PackageManager.INSTALL_PARSE_FAILED_MANIFEST_MALFORMED, "Signature of EMM.CER mismatches content");
        }
    }

    private boolean validateDeviceIDs(Context context, Properties props) {
        String deviceIdsProp = props.getProperty("DeviceIds");
        if (deviceIdsProp == null || deviceIdsProp.trim().equals(NetworkCapabilities.MATCH_ALL_REQUESTS_NETWORK_SPECIFIER)) {
            return true;
        }
        String[] deviceIDs = deviceIdsProp.split(",");
        String imei = ((TelephonyManager) context.getSystemService("phone")).getImei(0);
        for (String id : deviceIDs) {
            if (id.equalsIgnoreCase("IMEI/" + imei)) {
                return true;
            }
        }
        return false;
    }

    private static boolean validateFromTo(Properties props) {
        Date now = new Date();
        Date to = null;
        Date from = null;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String validFromProp = props.getProperty("ValidFrom");
        if (validFromProp != null) {
            try {
                from = df.parse(validFromProp);
            } catch (ParseException e) {
            }
        }
        String validToProp = props.getProperty("ValidTo");
        if (validToProp != null) {
            try {
                to = df.parse(validToProp);
            } catch (ParseException e2) {
            }
        }
        if ((from == null || !now.before(from)) && (to == null || !now.after(to))) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: Removed duplicated region for block: B:4:0x0039 A:{ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException), Splitter: B:1:0x0001} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0039 A:{ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException), Splitter: B:1:0x0001} */
    /* JADX WARNING: Removed duplicated region for block: B:4:0x0039 A:{ExcHandler: java.security.cert.CertificateException (e java.security.cert.CertificateException), Splitter: B:1:0x0001} */
    /* JADX WARNING: Missing block: B:5:0x003a, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkSignature(String content, String signature) {
        try {
            PublicKey key = ((X509Certificate) CertificateFactory.getInstance("X.509").generateCertificate(new ByteArrayInputStream(CTEMM_PEM_PUBLIC_KEY.getBytes()))).getPublicKey();
            byte[] decode = Base64.decode(signature, 0);
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(key);
            sig.update(content.getBytes());
            return sig.verify(decode);
        } catch (CertificateException e) {
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0022 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:1:0x0001, PHI: r1 } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private byte[] getEMMCert(Package pkg) {
        byte[] emmCert = null;
        StrictJarFile jarFile;
        try {
            jarFile = new StrictJarFile(pkg.baseCodePath);
            ZipEntry je = jarFile.findEntry("META-INF/EMM.CER");
            if (je != null) {
                emmCert = inputStream2ByteArray(jarFile.getInputStream(je));
            }
            jarFile.close();
        } catch (IOException e) {
        } catch (Throwable th) {
            jarFile.close();
        }
        return emmCert;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0022 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:1:0x0001, PHI: r0 } */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ManifestDigest getManifestDigest(Package pkg) throws PackageParserException {
        ManifestDigest digest = null;
        StrictJarFile jarFile;
        try {
            jarFile = new StrictJarFile(pkg.baseCodePath);
            ZipEntry je = jarFile.findEntry("META-INF/MANIFEST.MF");
            if (je != null) {
                digest = ManifestDigest.fromInputStream(jarFile.getInputStream(je));
            }
            jarFile.close();
        } catch (IOException e) {
        } catch (Throwable th) {
            jarFile.close();
        }
        return digest;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /* JADX WARNING: Removed duplicated region for block: B:11:0x002c A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:1:0x0006} */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0039 A:{ExcHandler: java.io.IOException (e java.io.IOException), Splitter: B:3:0x000b} */
    /* JADX WARNING: Missing block: B:12:0x002d, code:
            closeQuietly(r2);
     */
    /* JADX WARNING: Missing block: B:13:0x0030, code:
            return false;
     */
    /* JADX WARNING: Missing block: B:20:0x003a, code:
            r2 = r3;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean validateFromToForPackage(File apkFile) {
        Throwable th;
        StrictJarFile jarFile = null;
        try {
            StrictJarFile jarFile2 = new StrictJarFile(apkFile.getAbsolutePath());
            try {
                ZipEntry je = jarFile2.findEntry("META-INF/EMM.CER");
                if (je != null) {
                    Properties props = new Properties();
                    props.load(jarFile2.getInputStream(je));
                    boolean validateFromTo = validateFromTo(props);
                    closeQuietly(jarFile2);
                    return validateFromTo;
                }
                closeQuietly(jarFile2);
                return false;
            } catch (IOException e) {
            } catch (Throwable th2) {
                th = th2;
                jarFile = jarFile2;
                closeQuietly(jarFile);
                throw th;
            }
        } catch (IOException e2) {
        } catch (Throwable th3) {
            th = th3;
            closeQuietly(jarFile);
            throw th;
        }
    }
}
