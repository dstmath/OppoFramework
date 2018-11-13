package android.net.wifi.p2p;

import android.net.wifi.WifiEnterpriseConfig;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
public class WifiP2pGroup implements Parcelable {
    public static final Creator<WifiP2pGroup> CREATOR = null;
    public static final int PERSISTENT_NET_ID = -2;
    private static final String TAG = "WifiP2pGroup";
    public static final int TEMPORARY_NET_ID = -1;
    private static final Pattern groupStartedPattern = null;
    private static final Pattern groupStartedPattern2 = null;
    private List<WifiP2pDevice> mClients;
    private int mFreq;
    private String mInterface;
    private boolean mIsGroupOwner;
    private int mNetId;
    private String mNetworkName;
    private WifiP2pDevice mOwner;
    private String mPassphrase;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pGroup.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pGroup.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pGroup.<clinit>():void");
    }

    public WifiP2pGroup() {
        this.mClients = new ArrayList();
    }

    public WifiP2pGroup(String supplicantEvent) throws IllegalArgumentException {
        this.mClients = new ArrayList();
        String[] tokens = supplicantEvent.split(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
        if (tokens.length < 3) {
            throw new IllegalArgumentException("Malformed supplicant event");
        } else if (tokens[0].startsWith("P2P-GROUP")) {
            this.mInterface = tokens[1];
            this.mIsGroupOwner = tokens[2].equals("GO");
            Matcher match = groupStartedPattern.matcher(supplicantEvent);
            Matcher match2 = groupStartedPattern2.matcher(supplicantEvent);
            if (match != null && match.find()) {
                this.mNetworkName = match.group(1);
                this.mFreq = Integer.parseInt(match.group(2));
                this.mPassphrase = match.group(4);
                this.mOwner = new WifiP2pDevice(match.group(5));
                if (match.group(6) != null) {
                    this.mNetId = -2;
                } else {
                    this.mNetId = -1;
                }
                Log.d(TAG, "Normal match! frequency = " + this.mFreq);
            } else if (match2 == null || !match2.find()) {
                this.mNetworkName = "";
                this.mPassphrase = "";
                this.mOwner = new WifiP2pDevice();
                this.mNetId = -1;
                Log.d(TAG, "Match nothing!");
            } else {
                this.mNetworkName = "";
                this.mPassphrase = "";
                this.mOwner = new WifiP2pDevice(match2.group(1));
                if (match2.group(2) != null) {
                    this.mNetId = -2;
                } else {
                    this.mNetId = -1;
                }
                Log.d(TAG, "Not matched SSID, but matched device address!");
            }
        } else if (tokens[0].equals("P2P-INVITATION-RECEIVED")) {
            this.mNetId = -2;
            for (String token : tokens) {
                String[] nameValue = token.split("=");
                if (nameValue.length == 2) {
                    if (nameValue[0].equals("sa")) {
                        String sa = nameValue[1];
                        WifiP2pDevice dev = new WifiP2pDevice();
                        dev.deviceAddress = nameValue[1];
                        this.mClients.add(dev);
                    } else if (nameValue[0].equals("go_dev_addr")) {
                        this.mOwner = new WifiP2pDevice(nameValue[1]);
                    } else if (nameValue[0].equals("persistent")) {
                        this.mNetId = Integer.parseInt(nameValue[1]);
                    }
                }
            }
        } else {
            throw new IllegalArgumentException("Malformed supplicant event");
        }
    }

    public void setNetworkName(String networkName) {
        this.mNetworkName = networkName;
    }

    public String getNetworkName() {
        return this.mNetworkName;
    }

    public void setIsGroupOwner(boolean isGo) {
        this.mIsGroupOwner = isGo;
    }

    public boolean isGroupOwner() {
        return this.mIsGroupOwner;
    }

    public void setOwner(WifiP2pDevice device) {
        this.mOwner = device;
    }

    public WifiP2pDevice getOwner() {
        return this.mOwner;
    }

    public void addClient(String address) {
        addClient(new WifiP2pDevice(address));
    }

    public void addClient(WifiP2pDevice device) {
        for (WifiP2pDevice client : this.mClients) {
            if (client.equals(device)) {
                return;
            }
        }
        this.mClients.add(device);
    }

    public boolean removeClient(String address) {
        return this.mClients.remove(new WifiP2pDevice(address));
    }

    public boolean removeClient(WifiP2pDevice device) {
        return this.mClients.remove(device);
    }

    public boolean isClientListEmpty() {
        return this.mClients.size() == 0;
    }

    public int getClientAmount() {
        return this.mClients.size();
    }

    public boolean contains(WifiP2pDevice device) {
        if (this.mOwner.equals(device) || this.mClients.contains(device)) {
            return true;
        }
        return false;
    }

    public Collection<WifiP2pDevice> getClientList() {
        return Collections.unmodifiableCollection(this.mClients);
    }

    public void setPassphrase(String passphrase) {
        this.mPassphrase = passphrase;
    }

    public String getPassphrase() {
        return this.mPassphrase;
    }

    public void setInterface(String intf) {
        this.mInterface = intf;
    }

    public String getInterface() {
        return this.mInterface;
    }

    public int getNetworkId() {
        return this.mNetId;
    }

    public void setNetworkId(int netId) {
        this.mNetId = netId;
    }

    public int getOperaFreqency() {
        return this.mFreq;
    }

    public void setOperaFreqency(int freq) {
        this.mFreq = freq;
    }

    public int getFrequency() {
        return this.mFreq;
    }

    public void setFrequency(int mFreq) {
        this.mFreq = mFreq;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append("network: ").append(this.mNetworkName);
        sbuf.append("\n isGO: ").append(this.mIsGroupOwner);
        sbuf.append("\n GO: ").append(this.mOwner);
        for (WifiP2pDevice client : this.mClients) {
            sbuf.append("\n Client: ").append(client);
        }
        sbuf.append("\n interface: ").append(this.mInterface);
        sbuf.append("\n networkId: ").append(this.mNetId);
        sbuf.append("\n frequency: ").append(this.mFreq);
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public WifiP2pGroup(WifiP2pGroup source) {
        this.mClients = new ArrayList();
        if (source != null) {
            this.mNetworkName = source.getNetworkName();
            this.mOwner = new WifiP2pDevice(source.getOwner());
            this.mIsGroupOwner = source.mIsGroupOwner;
            for (WifiP2pDevice d : source.getClientList()) {
                this.mClients.add(d);
            }
            this.mPassphrase = source.getPassphrase();
            this.mInterface = source.getInterface();
            this.mNetId = source.getNetworkId();
            this.mFreq = source.getFrequency();
        }
    }

    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mNetworkName);
        dest.writeParcelable(this.mOwner, flags);
        dest.writeByte(this.mIsGroupOwner ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mClients.size());
        for (WifiP2pDevice client : this.mClients) {
            dest.writeParcelable(client, flags);
        }
        dest.writeString(this.mPassphrase);
        dest.writeString(this.mInterface);
        dest.writeInt(this.mNetId);
        dest.writeInt(this.mFreq);
    }
}
