package com.android.server.wifi.hotspot2.pps;

import android.util.Base64;
import com.android.server.wifi.hotspot2.Utils;
import com.android.server.wifi.hotspot2.omadm.OMAException;
import com.android.server.wifi.hotspot2.omadm.OMANode;
import com.android.server.wifi.hotspot2.omadm.PasspointManagementObjectManager;
import java.nio.charset.StandardCharsets;

public class UpdateInfo {
    private final String mCertFP;
    private final String mCertURL;
    private final long mInterval;
    private final String mPassword;
    private final boolean mSPPClientInitiated;
    private final String mURI;
    private final UpdateRestriction mUpdateRestriction;
    private final String mUsername;

    /*  JADX ERROR: NullPointerException in pass: ReSugarCode
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
        	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum UpdateRestriction {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.pps.UpdateInfo.UpdateRestriction.<clinit>():void, dex: 
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
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.pps.UpdateInfo.UpdateRestriction.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.pps.UpdateInfo.UpdateRestriction.<clinit>():void");
        }
    }

    public UpdateInfo(OMANode policyUpdate) throws OMAException {
        this.mInterval = PasspointManagementObjectManager.getLong(policyUpdate, PasspointManagementObjectManager.TAG_UpdateInterval, null) * PasspointManagementObjectManager.IntervalFactor;
        this.mSPPClientInitiated = ((Boolean) PasspointManagementObjectManager.getSelection(policyUpdate, PasspointManagementObjectManager.TAG_UpdateMethod)).booleanValue();
        this.mUpdateRestriction = (UpdateRestriction) PasspointManagementObjectManager.getSelection(policyUpdate, PasspointManagementObjectManager.TAG_Restriction);
        this.mURI = PasspointManagementObjectManager.getString(policyUpdate, PasspointManagementObjectManager.TAG_URI);
        OMANode unp = policyUpdate.getChild(PasspointManagementObjectManager.TAG_UsernamePassword);
        if (unp != null) {
            this.mUsername = PasspointManagementObjectManager.getString(unp.getChild(PasspointManagementObjectManager.TAG_Username));
            this.mPassword = new String(Base64.decode(PasspointManagementObjectManager.getString(unp.getChild(PasspointManagementObjectManager.TAG_Password)).getBytes(StandardCharsets.US_ASCII), 0), StandardCharsets.UTF_8);
        } else {
            this.mUsername = null;
            this.mPassword = null;
        }
        OMANode trustRoot = PasspointManagementObjectManager.getChild(policyUpdate, PasspointManagementObjectManager.TAG_TrustRoot);
        this.mCertURL = PasspointManagementObjectManager.getString(trustRoot, PasspointManagementObjectManager.TAG_CertURL);
        this.mCertFP = PasspointManagementObjectManager.getString(trustRoot, PasspointManagementObjectManager.TAG_CertSHA256Fingerprint);
    }

    public long getInterval() {
        return this.mInterval;
    }

    public boolean isSPPClientInitiated() {
        return this.mSPPClientInitiated;
    }

    public UpdateRestriction getUpdateRestriction() {
        return this.mUpdateRestriction;
    }

    public String getURI() {
        return this.mURI;
    }

    public String getUsername() {
        return this.mUsername;
    }

    public String getPassword() {
        return this.mPassword;
    }

    public String getCertURL() {
        return this.mCertURL;
    }

    public String getCertFP() {
        return this.mCertFP;
    }

    public String toString() {
        return "UpdateInfo{interval=" + Utils.toHMS(this.mInterval) + ", SPPClientInitiated=" + this.mSPPClientInitiated + ", updateRestriction=" + this.mUpdateRestriction + ", URI='" + this.mURI + '\'' + ", username='" + this.mUsername + '\'' + ", password=" + this.mPassword + ", certURL='" + this.mCertURL + '\'' + ", certFP='" + this.mCertFP + '\'' + '}';
    }
}
