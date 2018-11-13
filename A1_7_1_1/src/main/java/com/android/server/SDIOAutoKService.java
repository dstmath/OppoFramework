package com.android.server;

import android.content.Context;
import android.os.Binder;
import android.os.UEventObserver;
import android.os.UEventObserver.UEvent;
import android.util.Slog;
import com.android.server.oppo.IElsaManager;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

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
public final class SDIOAutoKService extends Binder {
    private static final String TAG = null;
    private final Context mContext;
    private final UEventObserver mSDIOAutoKObserver;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.server.SDIOAutoKService.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.server.SDIOAutoKService.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.SDIOAutoKService.<clinit>():void");
    }

    public SDIOAutoKService(Context context) {
        this.mSDIOAutoKObserver = new UEventObserver() {
            public void onUEvent(UEvent event) {
                Slog.d(SDIOAutoKService.TAG, ">>>>>>> SDIOAutoK UEVENT: " + event.toString() + " <<<<<<<");
                String from = event.get("FROM");
                byte[] autokParams = new byte[256];
                byte[] procParams = new byte[512];
                File fAutoK = new File("data/autok");
                BufferedInputStream bis;
                BufferedOutputStream bos;
                int autokLen;
                String str;
                int i;
                if ("sdio_autok".equals(from)) {
                    if (!fAutoK.exists()) {
                        try {
                            bis = new BufferedInputStream(new FileInputStream("proc/autok"));
                            bos = new BufferedOutputStream(new FileOutputStream("data/autok"));
                            while (true) {
                                autokLen = bis.read(autokParams);
                                if (autokLen == -1) {
                                    break;
                                }
                                str = IElsaManager.EMPTY_PACKAGE;
                                for (i = 0; i < autokLen; i++) {
                                    str = str + Byte.toString(autokParams[i]);
                                }
                                Slog.d(SDIOAutoKService.TAG, "read from proc (Str): " + str + " \n length: " + String.valueOf(autokLen));
                                bos.write(autokParams, 0, autokLen);
                            }
                            bos.flush();
                            bos.close();
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else if ("lte_drv".equals(from)) {
                    byte[] stage = new byte[1];
                    String paramsStr = IElsaManager.EMPTY_PACKAGE;
                    stage[0] = (byte) 0;
                    byte[] sdiofunc_addr = SDIOAutoKService.hexStringToByteArray_reverse(event.get("SDIOFUNC").substring(2));
                    System.arraycopy(sdiofunc_addr, 0, procParams, 0, sdiofunc_addr.length);
                    int paramsOffset = sdiofunc_addr.length + 0;
                    if (fAutoK.exists()) {
                        Slog.d(SDIOAutoKService.TAG, "/data/autok exists, do stage 2 auto-K");
                        stage[0] = (byte) 2;
                        System.arraycopy(stage, 0, procParams, paramsOffset, stage.length);
                        paramsOffset += stage.length;
                        try {
                            bis = new BufferedInputStream(new FileInputStream(fAutoK));
                            while (true) {
                                autokLen = bis.read(autokParams);
                                if (autokLen == -1) {
                                    break;
                                }
                                str = IElsaManager.EMPTY_PACKAGE;
                                System.arraycopy(autokParams, 0, procParams, paramsOffset, autokLen);
                                paramsOffset += autokLen;
                                for (i = 0; i < autokLen; i++) {
                                    str = str + Byte.toString(autokParams[i]);
                                }
                                paramsStr = paramsStr + str;
                            }
                            Slog.d(SDIOAutoKService.TAG, "/data/autok content:");
                            Slog.d(SDIOAutoKService.TAG, " " + paramsStr);
                            bis.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    } else {
                        stage[0] = (byte) 1;
                        System.arraycopy(stage, 0, procParams, paramsOffset, stage.length);
                        paramsOffset += stage.length;
                    }
                    Slog.d(SDIOAutoKService.TAG, "length of params write to proc:" + String.valueOf(paramsOffset));
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream("proc/autok"));
                        bos.write(procParams, 0, paramsOffset);
                        bos.flush();
                        bos.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                } else if ("autok_done".equals(from)) {
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream("proc/lte_autok"));
                        byte[] lteprocParams = "autok_done".getBytes("UTF-8");
                        str = IElsaManager.EMPTY_PACKAGE;
                        for (byte b : lteprocParams) {
                            str = str + Byte.toString(b) + " ";
                        }
                        Slog.d(SDIOAutoKService.TAG, "autok_done procParams.length: " + String.valueOf(lteprocParams.length));
                        Slog.d(SDIOAutoKService.TAG, "autok_done procParam: " + str);
                        bos.write(lteprocParams, 0, lteprocParams.length);
                        bos.flush();
                        bos.close();
                    } catch (IOException e222) {
                        e222.printStackTrace();
                    }
                }
            }
        };
        File fAutoK = new File("proc/lte_autok");
        Slog.d(TAG, ">>>>>>> SDIOAutoK Start Observing <<<<<<<");
        this.mContext = context;
        this.mSDIOAutoKObserver.startObserving("FROM=");
        if (fAutoK.exists()) {
            try {
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream("proc/lte_autok"));
                byte[] procParams = "system_server".getBytes("UTF-8");
                String str = IElsaManager.EMPTY_PACKAGE;
                for (byte b : procParams) {
                    str = str + Byte.toString(b) + " ";
                }
                Slog.d(TAG, "system_server procParams.length: " + String.valueOf(procParams.length));
                Slog.d(TAG, "system_server procParam: " + str);
                bos.write(procParams, 0, procParams.length);
                bos.flush();
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] hexStringToByteArray_reverse(String s) {
        int len = s.length();
        byte[] data = new byte[(len / 2)];
        for (int i = 0; i < len; i += 2) {
            data[((len - i) - 2) / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
