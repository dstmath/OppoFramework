package com.android.server;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Slog;
import com.android.server.notification.NotificationManagerService;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

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
public class EntropyMixer extends Binder {
    private static final int ENTROPY_WHAT = 1;
    private static final int ENTROPY_WRITE_PERIOD = 10800000;
    private static final long START_NANOTIME = 0;
    private static final long START_TIME = 0;
    private static final String TAG = "EntropyMixer";
    private final String entropyFile;
    private final String hwRandomDevice;
    private final BroadcastReceiver mBroadcastReceiver;
    private final Handler mHandler;
    private final String randomDevice;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.EntropyMixer.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.EntropyMixer.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.EntropyMixer.<clinit>():void");
    }

    public EntropyMixer(Context context) {
        this(context, getSystemDir() + "/entropy.dat", "/dev/urandom", "/dev/hw_random");
    }

    public EntropyMixer(Context context, String entropyFile, String randomDevice, String hwRandomDevice) {
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (msg.what != 1) {
                    Slog.e(EntropyMixer.TAG, "Will not process invalid message");
                    return;
                }
                EntropyMixer.this.addHwRandomEntropy();
                EntropyMixer.this.writeEntropy();
                EntropyMixer.this.scheduleEntropyWriter();
            }
        };
        this.mBroadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                EntropyMixer.this.writeEntropy();
            }
        };
        if (randomDevice == null) {
            throw new NullPointerException("randomDevice");
        } else if (hwRandomDevice == null) {
            throw new NullPointerException("hwRandomDevice");
        } else if (entropyFile == null) {
            throw new NullPointerException("entropyFile");
        } else {
            this.randomDevice = randomDevice;
            this.hwRandomDevice = hwRandomDevice;
            this.entropyFile = entropyFile;
            loadInitialEntropy();
            addDeviceSpecificEntropy();
            addHwRandomEntropy();
            writeEntropy();
            scheduleEntropyWriter();
            IntentFilter broadcastFilter = new IntentFilter("android.intent.action.ACTION_SHUTDOWN");
            broadcastFilter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
            broadcastFilter.addAction("android.intent.action.REBOOT");
            context.registerReceiver(this.mBroadcastReceiver, broadcastFilter);
        }
    }

    private void scheduleEntropyWriter() {
        this.mHandler.removeMessages(1);
        this.mHandler.sendEmptyMessageDelayed(1, NotificationManagerService.TIME_UPLOAD_THRESHOLD);
    }

    private void loadInitialEntropy() {
        try {
            RandomBlock.fromFile(this.entropyFile).toFile(this.randomDevice, false);
        } catch (FileNotFoundException e) {
            Slog.w(TAG, "No existing entropy file -- first boot?");
        } catch (IOException e2) {
            Slog.w(TAG, "Failure loading existing entropy file", e2);
        }
    }

    private void writeEntropy() {
        try {
            Slog.i(TAG, "Writing entropy...");
            RandomBlock.fromFile(this.randomDevice).toFile(this.entropyFile, true);
        } catch (IOException e) {
            Slog.w(TAG, "Unable to write entropy", e);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:22:? A:{SYNTHETIC, RETURN} */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x00a0  */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x00a7  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addDeviceSpecificEntropy() {
        IOException e;
        Throwable th;
        PrintWriter out = null;
        try {
            PrintWriter out2 = new PrintWriter(new FileOutputStream(this.randomDevice));
            try {
                out2.println("Copyright (C) 2009 The Android Open Source Project");
                out2.println("All Your Randomness Are Belong To Us");
                out2.println(START_TIME);
                out2.println(START_NANOTIME);
                out2.println(SystemProperties.get("ro.serialno"));
                out2.println(SystemProperties.get("ro.bootmode"));
                out2.println(SystemProperties.get("ro.baseband"));
                out2.println(SystemProperties.get("ro.carrier"));
                out2.println(SystemProperties.get("ro.bootloader"));
                out2.println(SystemProperties.get("ro.hardware"));
                out2.println(SystemProperties.get("ro.revision"));
                out2.println(SystemProperties.get("ro.build.fingerprint"));
                out2.println(new Object().hashCode());
                out2.println(System.currentTimeMillis());
                out2.println(System.nanoTime());
                if (out2 != null) {
                    out2.close();
                }
                out = out2;
            } catch (IOException e2) {
                e = e2;
                out = out2;
                try {
                    Slog.w(TAG, "Unable to add device specific data to the entropy pool", e);
                    if (out == null) {
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (out != null) {
                        out.close();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                out = out2;
                if (out != null) {
                }
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            Slog.w(TAG, "Unable to add device specific data to the entropy pool", e);
            if (out == null) {
                out.close();
            }
        }
    }

    private void addHwRandomEntropy() {
        try {
            RandomBlock.fromFile(this.hwRandomDevice).toFile(this.randomDevice, false);
            Slog.i(TAG, "Added HW RNG output to entropy pool");
        } catch (FileNotFoundException e) {
        } catch (IOException e2) {
            Slog.w(TAG, "Failed to add HW RNG output to entropy pool", e2);
        }
    }

    private static String getSystemDir() {
        File systemDir = new File(Environment.getDataDirectory(), "system");
        systemDir.mkdirs();
        return systemDir.toString();
    }
}
