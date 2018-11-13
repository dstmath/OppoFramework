package com.android.server.wifi.scanner;

import android.net.wifi.WifiScanner.ChannelSpec;
import android.net.wifi.WifiScanner.ScanSettings;
import android.util.ArraySet;
import com.android.server.wifi.WifiNative.BucketSettings;
import com.android.server.wifi.WifiNative.ChannelSettings;
import java.util.Set;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
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
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public abstract class ChannelHelper {
    protected static final ChannelSpec[] NO_CHANNELS = null;
    public static final int SCAN_PERIOD_PER_CHANNEL_MS = 200;

    public abstract class ChannelCollection {
        public abstract void addBand(int i);

        public abstract void addChannel(int i);

        public abstract void clear();

        public abstract boolean containsBand(int i);

        public abstract boolean containsChannel(int i);

        public abstract void fillBucketSettings(BucketSettings bucketSettings, int i);

        public abstract Set<Integer> getChannelSet();

        public abstract Set<Integer> getContainingChannelsFromBand(int i);

        public abstract Set<Integer> getMissingChannelsFromBand(int i);

        public abstract Set<Integer> getSupplicantScanFreqs();

        public abstract boolean isAllChannels();

        public abstract boolean isEmpty();

        public abstract boolean partiallyContainsBand(int i);

        public void addChannels(ScanSettings scanSettings) {
            if (scanSettings.band == 0) {
                for (ChannelSpec channelSpec : scanSettings.channels) {
                    addChannel(channelSpec.frequency);
                }
                return;
            }
            addBand(scanSettings.band);
        }

        public void addChannels(BucketSettings bucketSettings) {
            if (bucketSettings.band == 0) {
                for (ChannelSettings channelSettings : bucketSettings.channels) {
                    addChannel(channelSettings.frequency);
                }
                return;
            }
            addBand(bucketSettings.band);
        }

        public boolean containsSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return containsBand(scanSettings.band);
            }
            for (ChannelSpec channelSpec : scanSettings.channels) {
                if (!containsChannel(channelSpec.frequency)) {
                    return false;
                }
            }
            return true;
        }

        public boolean partiallyContainsSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return partiallyContainsBand(scanSettings.band);
            }
            for (ChannelSpec channelSpec : scanSettings.channels) {
                if (containsChannel(channelSpec.frequency)) {
                    return true;
                }
            }
            return false;
        }

        public Set<Integer> getMissingChannelsFromSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return getMissingChannelsFromBand(scanSettings.band);
            }
            ArraySet<Integer> missingChannels = new ArraySet();
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (!containsChannel(scanSettings.channels[j].frequency)) {
                    missingChannels.add(Integer.valueOf(scanSettings.channels[j].frequency));
                }
            }
            return missingChannels;
        }

        public Set<Integer> getContainingChannelsFromSettings(ScanSettings scanSettings) {
            if (scanSettings.band != 0) {
                return getContainingChannelsFromBand(scanSettings.band);
            }
            ArraySet<Integer> containingChannels = new ArraySet();
            for (int j = 0; j < scanSettings.channels.length; j++) {
                if (containsChannel(scanSettings.channels[j].frequency)) {
                    containingChannels.add(Integer.valueOf(scanSettings.channels[j].frequency));
                }
            }
            return containingChannels;
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.ChannelHelper.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.scanner.ChannelHelper.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.scanner.ChannelHelper.<clinit>():void");
    }

    public abstract ChannelCollection createChannelCollection();

    public abstract int estimateScanDuration(ScanSettings scanSettings);

    public abstract ChannelSpec[] getAvailableScanChannels(int i);

    public abstract boolean settingsContainChannel(ScanSettings scanSettings, int i);

    public void updateChannels() {
    }

    public static String toString(ScanSettings scanSettings) {
        if (scanSettings.band == 0) {
            return toString(scanSettings.channels);
        }
        return toString(scanSettings.band);
    }

    public static String toString(BucketSettings bucketSettings) {
        if (bucketSettings.band == 0) {
            return toString(bucketSettings.channels, bucketSettings.num_channels);
        }
        return toString(bucketSettings.band);
    }

    private static String toString(ChannelSpec[] channels) {
        if (channels == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int c = 0; c < channels.length; c++) {
            sb.append(channels[c].frequency);
            if (c != channels.length - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String toString(ChannelSettings[] channels, int numChannels) {
        if (channels == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int c = 0; c < numChannels; c++) {
            sb.append(channels[c].frequency);
            if (c != numChannels - 1) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    private static String toString(int band) {
        switch (band) {
            case 0:
                return "unspecified";
            case 1:
                return "24Ghz";
            case 2:
                return "5Ghz (no DFS)";
            case 3:
                return "24Ghz & 5Ghz (no DFS)";
            case 4:
                return "5Ghz (DFS only)";
            case 6:
                return "5Ghz (DFS incl)";
            case 7:
                return "24Ghz & 5Ghz (DFS incl)";
            default:
                return "invalid band";
        }
    }
}
