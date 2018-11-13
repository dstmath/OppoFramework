package android_maps_conflict_avoidance.com.google.googlenav.map;

import java.io.DataInput;
import java.io.DataOutput;
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
class FlashEntry {
    static int SIZE_IN_CATALOG;
    private final int dataSize;
    private FlashRecord flashRecord;
    private final Tile tile;
    private int time;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.googlenav.map.FlashEntry.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.googlenav.map.FlashEntry.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.map.FlashEntry.<clinit>():void");
    }

    public FlashEntry(MapTile mapTile) {
        this(mapTile.getLocation(), mapTile.getLastAccessTime(), mapTile.getDataSize());
    }

    private FlashEntry(Tile tile, long time, int dataSize) {
        this.tile = tile;
        setLastAccessTime(time);
        this.dataSize = dataSize;
    }

    public void setFlashRecord(FlashRecord newRecord) {
        if (this.flashRecord != null) {
            throw new IllegalStateException("FlashRecord already set");
        }
        this.flashRecord = newRecord;
    }

    public Tile getTile() {
        return this.tile;
    }

    public void setLastAccessTime(long time) {
        this.time = (int) ((time / 1000) - 1112219496);
    }

    public long getLastAccessTime() {
        return (((long) this.time) + 1112219496) * 1000;
    }

    public int getByteSize() {
        return this.dataSize + 12;
    }

    public String toString() {
        return this.tile.toString() + "B" + getByteSize();
    }

    public FlashRecord getFlashRecord() {
        return this.flashRecord;
    }

    public static FlashEntry readFromCatalog(DataInput is) throws IOException {
        int time = is.readInt();
        return new FlashEntry(Tile.read(is), (long) time, is.readUnsignedShort());
    }

    public void writeToCatalog(DataOutput os) throws IOException {
        os.writeInt(this.time);
        os.writeShort(this.dataSize);
        this.tile.write(os);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof FlashEntry)) {
            return false;
        }
        FlashEntry flashEntry = (FlashEntry) o;
        if (this.dataSize != flashEntry.dataSize) {
            return false;
        }
        if (this.tile != null) {
            return this.tile.equals(flashEntry.tile);
        }
        if (flashEntry.tile != null) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return ((this.tile != null ? this.tile.hashCode() : 0) * 29) + this.dataSize;
    }
}
