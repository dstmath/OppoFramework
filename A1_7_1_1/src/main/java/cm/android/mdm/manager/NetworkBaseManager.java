package cm.android.mdm.manager;

import cm.android.mdm.exception.MdmException;
import cm.android.mdm.interfaces.INetworkManager;
import java.util.List;
import java.util.Map;

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
public class NetworkBaseManager implements INetworkManager {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.manager.NetworkBaseManager.<init>():void, dex: 
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
    public NetworkBaseManager() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: cm.android.mdm.manager.NetworkBaseManager.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.manager.NetworkBaseManager.<init>():void");
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: cm.android.mdm.manager.NetworkBaseManager.getSupportMethods():java.util.List<java.lang.String>, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: cm.android.mdm.manager.NetworkBaseManager.getSupportMethods():java.util.List<java.lang.String>, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: cm.android.mdm.manager.NetworkBaseManager.getSupportMethods():java.util.List<java.lang.String>");
    }

    public void addApn(Map<String, String> map) {
        throw new MdmException("Not implement yet");
    }

    public void removeApn(String apnId) {
        throw new MdmException("Not implement yet");
    }

    public void updateApn(Map<String, String> map, String apnId) {
        throw new MdmException("Not implement yet");
    }

    public void setPreferApn(String apnId) {
        throw new MdmException("Not implement yet");
    }

    public List<String> queryApn(Map<String, String> map) {
        throw new MdmException("Not implement yet");
    }

    public Map<String, String> getApnInfo(String apnId) {
        throw new MdmException("Not implement yet");
    }

    public void setNetworkRestriction(int pattern) {
        throw new MdmException("Not implement yet");
    }

    public void addNetworkRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    public void removeNetworkRestriction(int pattern, List<String> list) {
        throw new MdmException("Not implement yet");
    }

    public void removeNetworkRestriction(int pattern) {
        throw new MdmException("Not implement yet");
    }
}
