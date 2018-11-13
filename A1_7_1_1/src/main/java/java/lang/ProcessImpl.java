package java.lang;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Map;

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
final class ProcessImpl {
    /* renamed from: -assertionsDisabled */
    static final /* synthetic */ boolean f32-assertionsDisabled = false;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: java.lang.ProcessImpl.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: java.lang.ProcessImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: java.lang.ProcessImpl.<clinit>():void");
    }

    private ProcessImpl() {
    }

    private static byte[] toCString(String s) {
        if (s == null) {
            return null;
        }
        byte[] bytes = s.getBytes();
        byte[] result = new byte[(bytes.length + 1)];
        System.arraycopy(bytes, 0, result, 0, bytes.length);
        result[result.length - 1] = (byte) 0;
        return result;
    }

    /* JADX WARNING: Removed duplicated region for block: B:45:0x00b8 A:{SYNTHETIC, Splitter: B:45:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00bd A:{SYNTHETIC, Splitter: B:48:0x00bd} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00b8 A:{SYNTHETIC, Splitter: B:45:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00bd A:{SYNTHETIC, Splitter: B:48:0x00bd} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00c2  */
    /* JADX WARNING: Removed duplicated region for block: B:45:0x00b8 A:{SYNTHETIC, Splitter: B:45:0x00b8} */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x00bd A:{SYNTHETIC, Splitter: B:48:0x00bd} */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00c2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static Process start(String[] cmdarray, Map<String, String> environment, String dir, Redirect[] redirects, boolean redirectErrorStream) throws IOException {
        int i;
        int[] std_fds;
        Throwable th;
        if (!f32-assertionsDisabled) {
            Object obj = (cmdarray == null || cmdarray.length <= 0) ? null : 1;
            if (obj == null) {
                throw new AssertionError();
            }
        }
        byte[][] args = new byte[(cmdarray.length - 1)][];
        int size = args.length;
        for (i = 0; i < args.length; i++) {
            args[i] = cmdarray[i + 1].getBytes();
            size += args[i].length;
        }
        byte[] argBlock = new byte[size];
        i = 0;
        for (byte[] arg : args) {
            System.arraycopy(arg, 0, argBlock, i, arg.length);
            i += arg.length + 1;
        }
        int[] envc = new int[1];
        byte[] envBlock = ProcessEnvironment.toEnvironmentBlock(environment, envc);
        FileInputStream f0 = null;
        FileOutputStream f1 = null;
        FileOutputStream f2 = null;
        if (redirects == null) {
            try {
                std_fds = new int[]{-1, -1, -1};
            } catch (Throwable th2) {
                th = th2;
                if (f0 != null) {
                }
                if (f1 != null) {
                }
                if (f2 != null) {
                }
                throw th;
            }
        }
        FileOutputStream fileOutputStream;
        std_fds = new int[3];
        if (redirects[0] == Redirect.PIPE) {
            std_fds[0] = -1;
        } else if (redirects[0] == Redirect.INHERIT) {
            std_fds[0] = 0;
        } else {
            FileInputStream f02 = new FileInputStream(redirects[0].file());
            try {
                std_fds[0] = f02.getFD().getInt$();
                f0 = f02;
            } catch (Throwable th3) {
                th = th3;
                f0 = f02;
                if (f0 != null) {
                }
                if (f1 != null) {
                }
                if (f2 != null) {
                }
                throw th;
            }
        }
        if (redirects[1] == Redirect.PIPE) {
            std_fds[1] = -1;
        } else if (redirects[1] == Redirect.INHERIT) {
            std_fds[1] = 1;
        } else {
            fileOutputStream = new FileOutputStream(redirects[1].file(), redirects[1].append());
            try {
                std_fds[1] = fileOutputStream.getFD().getInt$();
                f1 = fileOutputStream;
            } catch (Throwable th4) {
                th = th4;
                f1 = fileOutputStream;
                if (f0 != null) {
                    try {
                        f0.close();
                    } catch (Throwable th5) {
                        if (f2 != null) {
                            f2.close();
                        }
                    }
                }
                if (f1 != null) {
                    try {
                        f1.close();
                    } catch (Throwable th6) {
                        if (f2 != null) {
                            f2.close();
                        }
                    }
                }
                if (f2 != null) {
                    f2.close();
                }
                throw th;
            }
        }
        if (redirects[2] == Redirect.PIPE) {
            std_fds[2] = -1;
        } else if (redirects[2] == Redirect.INHERIT) {
            std_fds[2] = 2;
        } else {
            fileOutputStream = new FileOutputStream(redirects[2].file(), redirects[2].append());
            try {
                std_fds[2] = fileOutputStream.getFD().getInt$();
                f2 = fileOutputStream;
            } catch (Throwable th7) {
                th = th7;
                f2 = fileOutputStream;
                if (f0 != null) {
                }
                if (f1 != null) {
                }
                if (f2 != null) {
                }
                throw th;
            }
        }
        Process uNIXProcess = new UNIXProcess(toCString(cmdarray[0]), argBlock, args.length, envBlock, envc[0], toCString(dir), std_fds, redirectErrorStream);
        if (f0 != null) {
            try {
                f0.close();
            } catch (Throwable th8) {
                if (f2 != null) {
                    f2.close();
                }
            }
        }
        if (f1 != null) {
            try {
                f1.close();
            } catch (Throwable th9) {
                if (f2 != null) {
                    f2.close();
                }
            }
        }
        if (f2 != null) {
            f2.close();
        }
        return uNIXProcess;
    }
}
