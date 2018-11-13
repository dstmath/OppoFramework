package cm.android.mdm.manager;

import android.content.ComponentName;
import android.graphics.Bitmap;
import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.IDeviceManager;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class DeviceBaseManager implements IDeviceManager {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.manager.DeviceBaseManager.<init>():void, dex: 
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
    public DeviceBaseManager() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.manager.DeviceBaseManager.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.manager.DeviceBaseManager.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: cm.android.mdm.manager.DeviceBaseManager.getSupportMethods():java.util.List<java.lang.String>, dex: 
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
    public java.util.List<java.lang.String> getSupportMethods() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: cm.android.mdm.manager.DeviceBaseManager.getSupportMethods():java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.manager.DeviceBaseManager.getSupportMethods():java.util.List<java.lang.String>");
    }

    public boolean setDeviceOwner(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public boolean setDeviceOwner(ComponentName componentName) {
        throw new MdmException("Not implement yet");
    }

    public boolean setActiveAdmin(ComponentName adminReceiver) {
        throw new MdmException("Not implement yet");
    }

    public void enableAccessibilityService(ComponentName componentName) {
        throw new MdmException("Not implement yet");
    }

    public void allowGetUsageStats(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void ignoringBatteryOptimizations(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void allowDrawOverlays(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void allowReadLogs(String packageName) {
        throw new MdmException("Not implement yet");
    }

    public void shutdownDevice() {
        throw new MdmException("Not implement yet");
    }

    public void rebootDevice() {
        throw new MdmException("Not implement yet");
    }

    public boolean isRooted() {
        throw new MdmException("Not implement yet");
    }

    public void turnOnGPS(boolean on) {
        throw new MdmException("Not implement yet");
    }

    public boolean isGPSTurnOn() {
        throw new MdmException("Not implement yet");
    }

    public void setTimeChangeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public void setLanguageChangeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public void setFaceLockDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public Bitmap captureScreen() {
        throw new MdmException("Not implement yet");
    }

    public void setWallPaper(Bitmap bitmap) {
        throw new MdmException("Not implement yet");
    }

    public void setLockWallPaper(Bitmap bitmap) {
        throw new MdmException("Not implement yet");
    }
}
