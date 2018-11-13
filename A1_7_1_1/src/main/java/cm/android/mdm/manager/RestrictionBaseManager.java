package cm.android.mdm.manager;

import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.IRestrictionManager;

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
public class RestrictionBaseManager implements IRestrictionManager {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.manager.RestrictionBaseManager.<init>():void, dex: 
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
    public RestrictionBaseManager() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.manager.RestrictionBaseManager.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.manager.RestrictionBaseManager.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: cm.android.mdm.manager.RestrictionBaseManager.getSupportMethods():java.util.List<java.lang.String>, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: cm.android.mdm.manager.RestrictionBaseManager.getSupportMethods():java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.manager.RestrictionBaseManager.getSupportMethods():java.util.List<java.lang.String>");
    }

    public void setWifiDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isWifiDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setBluetoothDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isBluetoothDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setWifiApDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isWifiApDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setUSBDataDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isUSBDataDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setExternalStorageDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isExternalStorageDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setNFCDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isNFCDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setMobileDataDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isMobileDataDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setVoiceDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isVoiceDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setSMSDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isSMSDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setSafeModeDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isSafeModeDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setAdbDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isAdbDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setUSBOtgDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isUSBOtgDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setGPSDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isGPSDisabled() {
        throw new MdmException("Not implement yet");
    }

    public void setDeveloperOptionsDisabled(boolean disabled) {
        throw new MdmException("Not implement yet");
    }

    public boolean isDeveloperOptionsDisabled() {
        throw new MdmException("Not implement yet");
    }
}
