package com.android.internal.os;

import android.net.Credentials;
import android.net.LocalSocket;
import android.os.Process;
import android.os.SystemProperties;
import android.os.Trace;
import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.util.Log;
import com.android.internal.os.ZygoteInit.MethodAndArgsCaller;
import com.android.internal.telephony.PhoneConstants;
import dalvik.system.VMRuntime;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import libcore.io.IoUtils;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
class ZygoteConnection {
    private static final int CONNECTION_TIMEOUT_MILLIS = 1000;
    private static final int MAX_ZYGOTE_ARGC = 1024;
    private static final String TAG = "Zygote";
    private static final int[][] intArray2d = null;
    private final String abiList;
    private final LocalSocket mSocket;
    private final DataOutputStream mSocketOutStream;
    private final BufferedReader mSocketReader;
    private final Credentials peer;

    static class Arguments {
        boolean abiListQuery;
        String appDataDir;
        boolean capabilitiesSpecified;
        int debugFlags;
        long effectiveCapabilities;
        int gid = 0;
        boolean gidSpecified;
        int[] gids;
        String instructionSet;
        String invokeWith;
        int mountExternal = 0;
        String niceName;
        long permittedCapabilities;
        String[] remainingArgs;
        ArrayList<int[]> rlimits;
        String seInfo;
        boolean seInfoSpecified;
        int targetSdkVersion;
        boolean targetSdkVersionSpecified;
        boolean trySecondaryZygote;
        int uid = 0;
        boolean uidSpecified;

        Arguments(String[] args) throws IllegalArgumentException {
            parseArgs(args);
        }

        private void parseArgs(String[] args) throws IllegalArgumentException {
            int curArg = 0;
            boolean seenRuntimeArgs = false;
            while (curArg < args.length) {
                String arg = args[curArg];
                if (arg.equals("--")) {
                    curArg++;
                    break;
                }
                if (!arg.startsWith("--setuid=")) {
                    if (!arg.startsWith("--setgid=")) {
                        if (!arg.startsWith("--target-sdk-version=")) {
                            if (!arg.equals("--enable-debugger")) {
                                if (!arg.equals("--enable-safemode")) {
                                    if (!arg.equals("--enable-checkjni")) {
                                        if (!arg.equals("--generate-debug-info")) {
                                            if (!arg.equals("--always-jit")) {
                                                if (!arg.equals("--native-debuggable")) {
                                                    if (!arg.equals("--enable-jni-logging")) {
                                                        if (!arg.equals("--enable-assert")) {
                                                            if (!arg.equals("--runtime-args")) {
                                                                if (!arg.startsWith("--seinfo=")) {
                                                                    if (!arg.startsWith("--capabilities=")) {
                                                                        int i;
                                                                        if (!arg.startsWith("--rlimit=")) {
                                                                            if (!arg.startsWith("--setgroups=")) {
                                                                                if (!arg.equals("--invoke-with")) {
                                                                                    if (!arg.startsWith("--nice-name=")) {
                                                                                        if (!arg.equals("--mount-external-default")) {
                                                                                            if (!arg.equals("--mount-external-read")) {
                                                                                                if (!arg.equals("--mount-external-write")) {
                                                                                                    if (!arg.equals("--query-abi-list")) {
                                                                                                        if (!arg.startsWith("--instruction-set=")) {
                                                                                                            if (!arg.startsWith("--app-data-dir=")) {
                                                                                                                if (!arg.startsWith("--try-secondary-zygote")) {
                                                                                                                    break;
                                                                                                                } else if (ZygoteInit.sZygoteOnDemandEnabled) {
                                                                                                                    if (ZygoteInit.DEBUG_ZYGOTE_ON_DEMAND) {
                                                                                                                        Log.d(ZygoteConnection.TAG, "ZygoteOnDemand: parseArgs --try-secondary-zygote");
                                                                                                                    }
                                                                                                                    this.trySecondaryZygote = true;
                                                                                                                }
                                                                                                            } else {
                                                                                                                this.appDataDir = arg.substring(arg.indexOf(61) + 1);
                                                                                                            }
                                                                                                        } else {
                                                                                                            this.instructionSet = arg.substring(arg.indexOf(61) + 1);
                                                                                                        }
                                                                                                    } else {
                                                                                                        this.abiListQuery = true;
                                                                                                    }
                                                                                                } else {
                                                                                                    this.mountExternal = 3;
                                                                                                }
                                                                                            } else {
                                                                                                this.mountExternal = 2;
                                                                                            }
                                                                                        } else {
                                                                                            this.mountExternal = 1;
                                                                                        }
                                                                                    } else if (this.niceName != null) {
                                                                                        throw new IllegalArgumentException("Duplicate arg specified");
                                                                                    } else {
                                                                                        this.niceName = arg.substring(arg.indexOf(61) + 1);
                                                                                    }
                                                                                } else if (this.invokeWith != null) {
                                                                                    throw new IllegalArgumentException("Duplicate arg specified");
                                                                                } else {
                                                                                    curArg++;
                                                                                    try {
                                                                                        this.invokeWith = args[curArg];
                                                                                    } catch (IndexOutOfBoundsException e) {
                                                                                        throw new IllegalArgumentException("--invoke-with requires argument");
                                                                                    }
                                                                                }
                                                                            } else if (this.gids != null) {
                                                                                throw new IllegalArgumentException("Duplicate arg specified");
                                                                            } else {
                                                                                String[] params = arg.substring(arg.indexOf(61) + 1).split(",");
                                                                                this.gids = new int[params.length];
                                                                                for (i = params.length - 1; i >= 0; i--) {
                                                                                    this.gids[i] = Integer.parseInt(params[i]);
                                                                                }
                                                                            }
                                                                        } else {
                                                                            String[] limitStrings = arg.substring(arg.indexOf(61) + 1).split(",");
                                                                            if (limitStrings.length != 3) {
                                                                                throw new IllegalArgumentException("--rlimit= should have 3 comma-delimited ints");
                                                                            }
                                                                            int[] rlimitTuple = new int[limitStrings.length];
                                                                            for (i = 0; i < limitStrings.length; i++) {
                                                                                rlimitTuple[i] = Integer.parseInt(limitStrings[i]);
                                                                            }
                                                                            if (this.rlimits == null) {
                                                                                this.rlimits = new ArrayList();
                                                                            }
                                                                            this.rlimits.add(rlimitTuple);
                                                                        }
                                                                    } else if (this.capabilitiesSpecified) {
                                                                        throw new IllegalArgumentException("Duplicate arg specified");
                                                                    } else {
                                                                        this.capabilitiesSpecified = true;
                                                                        String[] capStrings = arg.substring(arg.indexOf(61) + 1).split(",", 2);
                                                                        if (capStrings.length == 1) {
                                                                            this.effectiveCapabilities = Long.decode(capStrings[0]).longValue();
                                                                            this.permittedCapabilities = this.effectiveCapabilities;
                                                                        } else {
                                                                            this.permittedCapabilities = Long.decode(capStrings[0]).longValue();
                                                                            this.effectiveCapabilities = Long.decode(capStrings[1]).longValue();
                                                                        }
                                                                    }
                                                                } else if (this.seInfoSpecified) {
                                                                    throw new IllegalArgumentException("Duplicate arg specified");
                                                                } else {
                                                                    this.seInfoSpecified = true;
                                                                    this.seInfo = arg.substring(arg.indexOf(61) + 1);
                                                                }
                                                            } else {
                                                                seenRuntimeArgs = true;
                                                            }
                                                        } else {
                                                            this.debugFlags |= 4;
                                                        }
                                                    } else {
                                                        this.debugFlags |= 16;
                                                    }
                                                } else {
                                                    this.debugFlags |= 128;
                                                }
                                            } else {
                                                this.debugFlags |= 64;
                                            }
                                        } else {
                                            this.debugFlags |= 32;
                                        }
                                    } else {
                                        this.debugFlags |= 2;
                                    }
                                } else {
                                    this.debugFlags |= 8;
                                }
                            } else {
                                this.debugFlags |= 1;
                            }
                        } else if (this.targetSdkVersionSpecified) {
                            throw new IllegalArgumentException("Duplicate target-sdk-version specified");
                        } else {
                            this.targetSdkVersionSpecified = true;
                            this.targetSdkVersion = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                        }
                    } else if (this.gidSpecified) {
                        throw new IllegalArgumentException("Duplicate arg specified");
                    } else {
                        this.gidSpecified = true;
                        this.gid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                    }
                } else if (this.uidSpecified) {
                    throw new IllegalArgumentException("Duplicate arg specified");
                } else {
                    this.uidSpecified = true;
                    this.uid = Integer.parseInt(arg.substring(arg.indexOf(61) + 1));
                }
                curArg++;
            }
            if (this.abiListQuery) {
                if (args.length - curArg > 0) {
                    throw new IllegalArgumentException("Unexpected arguments after --query-abi-list.");
                }
            } else if (!this.trySecondaryZygote) {
                if (seenRuntimeArgs) {
                    this.remainingArgs = new String[(args.length - curArg)];
                    System.arraycopy(args, curArg, this.remainingArgs, 0, this.remainingArgs.length);
                    return;
                }
                throw new IllegalArgumentException("Unexpected argument : " + args[curArg]);
            }
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.ZygoteConnection.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.os.ZygoteConnection.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.os.ZygoteConnection.<clinit>():void");
    }

    ZygoteConnection(LocalSocket socket, String abiList) throws IOException {
        this.mSocket = socket;
        this.abiList = abiList;
        this.mSocketOutStream = new DataOutputStream(socket.getOutputStream());
        this.mSocketReader = new BufferedReader(new InputStreamReader(socket.getInputStream()), 256);
        this.mSocket.setSoTimeout(1000);
        try {
            this.peer = this.mSocket.getPeerCredentials();
        } catch (IOException ex) {
            Log.e(TAG, "Cannot read peer credentials", ex);
            throw ex;
        }
    }

    FileDescriptor getFileDesciptor() {
        return this.mSocket.getFileDescriptor();
    }

    /* JADX WARNING: Removed duplicated region for block: B:62:0x01a9 A:{SYNTHETIC, Splitter: B:62:0x01a9} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00dc A:{SYNTHETIC, Splitter: B:39:0x00dc} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00dc A:{SYNTHETIC, Splitter: B:39:0x00dc} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01a9 A:{SYNTHETIC, Splitter: B:62:0x01a9} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01a9 A:{SYNTHETIC, Splitter: B:62:0x01a9} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00dc A:{SYNTHETIC, Splitter: B:39:0x00dc} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00dc A:{SYNTHETIC, Splitter: B:39:0x00dc} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01a9 A:{SYNTHETIC, Splitter: B:62:0x01a9} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01a9 A:{SYNTHETIC, Splitter: B:62:0x01a9} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00dc A:{SYNTHETIC, Splitter: B:39:0x00dc} */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00dc A:{SYNTHETIC, Splitter: B:39:0x00dc} */
    /* JADX WARNING: Removed duplicated region for block: B:62:0x01a9 A:{SYNTHETIC, Splitter: B:62:0x01a9} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean runOnce() throws MethodAndArgsCaller {
        Throwable ex;
        Throwable ex2;
        Throwable ex3;
        Arguments parsedArgs = null;
        try {
            String[] args = readArgumentList();
            FileDescriptor[] descriptors = this.mSocket.getAncillaryFileDescriptors();
            if (args == null) {
                closeSocket();
                return true;
            }
            PrintStream printStream = null;
            if (descriptors != null && descriptors.length >= 3) {
                PrintStream printStream2 = new PrintStream(new FileOutputStream(descriptors[2]));
            }
            int pid = -1;
            FileDescriptor childPipeFd = null;
            FileDescriptor serverPipeFd = null;
            try {
                Arguments arguments = new Arguments(args);
                try {
                    if (arguments.trySecondaryZygote) {
                        if (ZygoteInit.DEBUG_ZYGOTE_ON_DEMAND) {
                            Log.d(TAG, "ZygoteOnDemand: runOnce() trySecondaryZygote");
                        }
                        return handleTrySecondaryZygote();
                    } else if (arguments.abiListQuery) {
                        return handleAbiListQuery();
                    } else {
                        if (arguments.permittedCapabilities == 0 && arguments.effectiveCapabilities == 0) {
                            applyUidSecurityPolicy(arguments, this.peer);
                            applyInvokeWithSecurityPolicy(arguments, this.peer);
                            applyDebuggerSystemProperty(arguments);
                            applyInvokeWithSystemProperty(arguments);
                            int[][] iArr = null;
                            if (arguments.rlimits != null) {
                                iArr = (int[][]) arguments.rlimits.toArray(intArray2d);
                            }
                            if (arguments.invokeWith != null) {
                                FileDescriptor[] pipeFds = Os.pipe2(OsConstants.O_CLOEXEC);
                                childPipeFd = pipeFds[1];
                                serverPipeFd = pipeFds[0];
                                Os.fcntlInt(childPipeFd, OsConstants.F_SETFD, 0);
                            }
                            int[] fdsToClose = new int[]{-1, -1};
                            FileDescriptor fd = this.mSocket.getFileDescriptor();
                            if (fd != null) {
                                fdsToClose[0] = fd.getInt$();
                            }
                            fd = ZygoteInit.getServerSocketFileDescriptor();
                            if (fd != null) {
                                fdsToClose[1] = fd.getInt$();
                            }
                            pid = Zygote.forkAndSpecialize(arguments.uid, arguments.gid, arguments.gids, arguments.debugFlags, iArr, arguments.mountExternal, arguments.seInfo, arguments.niceName, fdsToClose, arguments.instructionSet, arguments.appDataDir);
                            parsedArgs = arguments;
                            if (pid == 0) {
                                try {
                                    IoUtils.closeQuietly(serverPipeFd);
                                    serverPipeFd = null;
                                    handleChildProc(parsedArgs, descriptors, childPipeFd, printStream);
                                } finally {
                                    IoUtils.closeQuietly(childPipeFd);
                                    IoUtils.closeQuietly(serverPipeFd);
                                }
                            } else {
                                IoUtils.closeQuietly(childPipeFd);
                                childPipeFd = null;
                                boolean handleParentProc = handleParentProc(pid, descriptors, serverPipeFd, parsedArgs);
                                IoUtils.closeQuietly(null);
                                IoUtils.closeQuietly(serverPipeFd);
                                return handleParentProc;
                            }
                            return true;
                        }
                        throw new ZygoteSecurityException("Client may not specify capabilities: permitted=0x" + Long.toHexString(arguments.permittedCapabilities) + ", effective=0x" + Long.toHexString(arguments.effectiveCapabilities));
                    }
                } catch (ErrnoException e) {
                    ex = e;
                    parsedArgs = arguments;
                    logAndPrintError(printStream, "Exception creating pipe", ex);
                    if (pid == 0) {
                    }
                } catch (IllegalArgumentException e2) {
                    ex2 = e2;
                    parsedArgs = arguments;
                    logAndPrintError(printStream, "Invalid zygote arguments", ex2);
                    if (pid == 0) {
                    }
                    return true;
                } catch (ZygoteSecurityException e3) {
                    ex3 = e3;
                    parsedArgs = arguments;
                    logAndPrintError(printStream, "Zygote security policy prevents request: ", ex3);
                    if (pid == 0) {
                    }
                }
            } catch (ErrnoException e4) {
                ex = e4;
                logAndPrintError(printStream, "Exception creating pipe", ex);
                if (pid == 0) {
                }
                return true;
            } catch (IllegalArgumentException e5) {
                ex2 = e5;
                logAndPrintError(printStream, "Invalid zygote arguments", ex2);
                if (pid == 0) {
                }
            } catch (ZygoteSecurityException e6) {
                ex3 = e6;
                logAndPrintError(printStream, "Zygote security policy prevents request: ", ex3);
                if (pid == 0) {
                }
                return true;
            }
        } catch (IOException ex4) {
            Log.w(TAG, "IOException on command socket " + ex4.getMessage());
            closeSocket();
            return true;
        }
    }

    private boolean handleAbiListQuery() {
        try {
            byte[] abiListBytes = this.abiList.getBytes(StandardCharsets.US_ASCII);
            this.mSocketOutStream.writeInt(abiListBytes.length);
            this.mSocketOutStream.write(abiListBytes);
            return false;
        } catch (IOException ioe) {
            Log.e(TAG, "Error writing to command socket", ioe);
            return true;
        }
    }

    private boolean handleTrySecondaryZygote() {
        boolean usingWrapper = ZygoteInit.sZygoteReady;
        if (ZygoteInit.DEBUG_ZYGOTE_ON_DEMAND) {
            Log.d(TAG, "ZygoteOnDemand: handleTrySecondaryZygote " + usingWrapper);
        }
        try {
            this.mSocketOutStream.writeInt(0);
            this.mSocketOutStream.writeBoolean(usingWrapper);
            return false;
        } catch (IOException ex) {
            Log.e(TAG, "ZygoteOnDemand: Error writing to command socket", ex);
            return true;
        }
    }

    void closeSocket() {
        try {
            this.mSocket.close();
        } catch (IOException ex) {
            Log.e(TAG, "Exception while closing command socket in parent", ex);
        }
    }

    private String[] readArgumentList() throws IOException {
        try {
            String s = this.mSocketReader.readLine();
            if (s == null) {
                return null;
            }
            int argc = Integer.parseInt(s);
            if (argc > 1024) {
                throw new IOException("max arg count exceeded");
            }
            String[] result = new String[argc];
            for (int i = 0; i < argc; i++) {
                result[i] = this.mSocketReader.readLine();
                if (result[i] == null) {
                    throw new IOException("truncated request");
                }
            }
            return result;
        } catch (NumberFormatException e) {
            Log.e(TAG, "invalid Zygote wire format: non-int at argc");
            throw new IOException("invalid wire format");
        }
    }

    private static void applyUidSecurityPolicy(Arguments args, Credentials peer) throws ZygoteSecurityException {
        if (peer.getUid() == 1000) {
            boolean uidRestricted;
            String factoryTest = SystemProperties.get("ro.factorytest");
            if (factoryTest.equals("1") || factoryTest.equals("2")) {
                uidRestricted = false;
            } else {
                uidRestricted = true;
            }
            if (uidRestricted && args.uidSpecified && args.uid < 1000) {
                throw new ZygoteSecurityException("System UID may not launch process with UID < 1000");
            }
        }
        if (!args.uidSpecified) {
            args.uid = peer.getUid();
            args.uidSpecified = true;
        }
        if (!args.gidSpecified) {
            args.gid = peer.getGid();
            args.gidSpecified = true;
        }
    }

    public static void applyDebuggerSystemProperty(Arguments args) {
        if ("1".equals(SystemProperties.get("ro.debuggable"))) {
            args.debugFlags |= 1;
        }
    }

    private static void applyInvokeWithSecurityPolicy(Arguments args, Credentials peer) throws ZygoteSecurityException {
        int peerUid = peer.getUid();
        if (args.invokeWith != null && peerUid != 0) {
            throw new ZygoteSecurityException("Peer is not permitted to specify an explicit invoke-with wrapper command");
        }
    }

    public static void applyInvokeWithSystemProperty(Arguments args) {
        if (args.invokeWith == null && args.niceName != null) {
            String property = "wrap." + args.niceName;
            if (property.length() > 31) {
                if (property.charAt(30) != '.') {
                    property = property.substring(0, 31);
                } else {
                    property = property.substring(0, 30);
                }
            }
            args.invokeWith = SystemProperties.get(property);
            if (args.invokeWith != null && args.invokeWith.length() == 0) {
                args.invokeWith = null;
            }
        }
    }

    private void handleChildProc(Arguments parsedArgs, FileDescriptor[] descriptors, FileDescriptor pipeFd, PrintStream newStderr) throws MethodAndArgsCaller {
        closeSocket();
        ZygoteInit.closeServerSocket();
        if (descriptors != null) {
            try {
                Os.dup2(descriptors[0], OsConstants.STDIN_FILENO);
                Os.dup2(descriptors[1], OsConstants.STDOUT_FILENO);
                Os.dup2(descriptors[2], OsConstants.STDERR_FILENO);
                for (FileDescriptor fd : descriptors) {
                    IoUtils.closeQuietly(fd);
                }
                newStderr = System.err;
            } catch (ErrnoException ex) {
                Log.e(TAG, "Error reopening stdio", ex);
            }
        }
        if (parsedArgs.niceName != null) {
            Process.setArgV0(parsedArgs.niceName);
        }
        Trace.traceEnd(64);
        if (parsedArgs.invokeWith != null) {
            WrapperInit.execApplication(parsedArgs.invokeWith, parsedArgs.niceName, parsedArgs.targetSdkVersion, VMRuntime.getCurrentInstructionSet(), pipeFd, parsedArgs.remainingArgs);
            return;
        }
        RuntimeInit.zygoteInit(parsedArgs.targetSdkVersion, parsedArgs.remainingArgs, null);
    }

    private boolean handleParentProc(int pid, FileDescriptor[] descriptors, FileDescriptor pipeFd, Arguments parsedArgs) {
        if (pid > 0) {
            setChildPgid(pid);
        }
        if (descriptors != null) {
            for (FileDescriptor fd : descriptors) {
                IoUtils.closeQuietly(fd);
            }
        }
        boolean usingWrapper = false;
        if (pipeFd != null && pid > 0) {
            DataInputStream is = new DataInputStream(new FileInputStream(pipeFd));
            int innerPid = -1;
            try {
                innerPid = is.readInt();
                try {
                    is.close();
                } catch (IOException e) {
                }
            } catch (IOException ex) {
                Log.w(TAG, "Error reading pid from wrapped process, child may have died", ex);
                try {
                    is.close();
                } catch (IOException e2) {
                }
            } catch (Throwable th) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
                throw th;
            }
            if (innerPid > 0) {
                int parentPid = innerPid;
                while (parentPid > 0 && parentPid != pid) {
                    parentPid = Process.getParentPid(parentPid);
                }
                if (parentPid > 0) {
                    Log.i(TAG, "Wrapped process has pid " + innerPid);
                    pid = innerPid;
                    usingWrapper = true;
                } else {
                    Log.w(TAG, "Wrapped process reported a pid that is not a child of the process that we forked: childPid=" + pid + " innerPid=" + innerPid);
                }
            }
        }
        try {
            this.mSocketOutStream.writeInt(pid);
            this.mSocketOutStream.writeBoolean(usingWrapper);
            return false;
        } catch (IOException ex2) {
            Log.e(TAG, "Error writing to command socket", ex2);
            return true;
        }
    }

    private void setChildPgid(int pid) {
        try {
            Os.setpgid(pid, Os.getpgid(this.peer.getPid()));
        } catch (ErrnoException e) {
            Log.i(TAG, "Zygote: setpgid failed. This is normal if peer is not in our session");
        }
    }

    private static void logAndPrintError(PrintStream newStderr, String message, Throwable ex) {
        Log.e(TAG, message, ex);
        if (newStderr != null) {
            StringBuilder append = new StringBuilder().append(message);
            if (ex == null) {
                ex = PhoneConstants.MVNO_TYPE_NONE;
            }
            newStderr.println(append.append(ex).toString());
        }
    }
}
