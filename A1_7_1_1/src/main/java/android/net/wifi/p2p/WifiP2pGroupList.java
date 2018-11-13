package android.net.wifi.p2p;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.LruCache;
import java.util.Collection;
import java.util.Map.Entry;

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
public class WifiP2pGroupList implements Parcelable {
    public static final Creator<WifiP2pGroupList> CREATOR = null;
    private static final int CREDENTIAL_MAX_NUM = 32;
    private boolean isClearCalled;
    private final LruCache<Integer, WifiP2pGroup> mGroups;
    private final GroupDeleteListener mListener;

    public interface GroupDeleteListener {
        void onDeleteGroup(int i);
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pGroupList.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.net.wifi.p2p.WifiP2pGroupList.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android.net.wifi.p2p.WifiP2pGroupList.<clinit>():void");
    }

    public WifiP2pGroupList() {
        this(null, null);
    }

    public WifiP2pGroupList(WifiP2pGroupList source, GroupDeleteListener listener) {
        this.isClearCalled = false;
        this.mListener = listener;
        this.mGroups = new LruCache<Integer, WifiP2pGroup>(32) {
            protected void entryRemoved(boolean evicted, Integer netId, WifiP2pGroup oldValue, WifiP2pGroup newValue) {
                if (WifiP2pGroupList.this.mListener != null && !WifiP2pGroupList.this.isClearCalled) {
                    WifiP2pGroupList.this.mListener.onDeleteGroup(oldValue.getNetworkId());
                }
            }
        };
        if (source != null) {
            for (Entry<Integer, WifiP2pGroup> item : source.mGroups.snapshot().entrySet()) {
                this.mGroups.put((Integer) item.getKey(), (WifiP2pGroup) item.getValue());
            }
        }
    }

    public Collection<WifiP2pGroup> getGroupList() {
        return this.mGroups.snapshot().values();
    }

    public void add(WifiP2pGroup group) {
        this.mGroups.put(Integer.valueOf(group.getNetworkId()), group);
    }

    public void remove(int netId) {
        this.mGroups.remove(Integer.valueOf(netId));
    }

    void remove(String deviceAddress) {
        remove(getNetworkId(deviceAddress));
    }

    public boolean clear() {
        if (this.mGroups.size() == 0) {
            return false;
        }
        this.isClearCalled = true;
        this.mGroups.evictAll();
        this.isClearCalled = false;
        return true;
    }

    public int getNetworkId(String deviceAddress) {
        if (deviceAddress == null) {
            return -1;
        }
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            if (deviceAddress.equalsIgnoreCase(grp.getOwner().deviceAddress)) {
                this.mGroups.get(Integer.valueOf(grp.getNetworkId()));
                return grp.getNetworkId();
            }
        }
        return -1;
    }

    public int getNetworkId(String deviceAddress, String ssid) {
        if (deviceAddress == null || ssid == null) {
            return -1;
        }
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            if (deviceAddress.equalsIgnoreCase(grp.getOwner().deviceAddress) && ssid.equals(grp.getNetworkName())) {
                this.mGroups.get(Integer.valueOf(grp.getNetworkId()));
                return grp.getNetworkId();
            }
        }
        return -1;
    }

    public String getOwnerAddr(int netId) {
        WifiP2pGroup grp = (WifiP2pGroup) this.mGroups.get(Integer.valueOf(netId));
        if (grp != null) {
            return grp.getOwner().deviceAddress;
        }
        return null;
    }

    public boolean contains(int netId) {
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            if (netId == grp.getNetworkId()) {
                return true;
            }
        }
        return false;
    }

    public String toString() {
        StringBuffer sbuf = new StringBuffer();
        for (WifiP2pGroup grp : this.mGroups.snapshot().values()) {
            sbuf.append(grp).append("\n");
        }
        return sbuf.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        Collection<WifiP2pGroup> groups = this.mGroups.snapshot().values();
        dest.writeInt(groups.size());
        for (WifiP2pGroup group : groups) {
            dest.writeParcelable(group, flags);
        }
    }
}
