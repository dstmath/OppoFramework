package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.map.MapPoint;
import android_maps_conflict_avoidance.com.google.map.Zoom;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

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
public class Tile {
    private static final int[] CACHE_SIZES = null;
    private static Tile[] tileObjectCache;
    private static int tileObjectCacheSize;
    private final byte flags;
    private final int hashCode;
    private final int xIndex;
    private final int yIndex;
    private final Zoom zoom;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.googlenav.map.Tile.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android_maps_conflict_avoidance.com.google.googlenav.map.Tile.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.googlenav.map.Tile.<clinit>():void");
    }

    public static void initializeTileObjectCache(int workingSetSize) {
        tileObjectCacheSize = getCacheSizeFromMinCacheSize(workingSetSize * 6);
        tileObjectCache = new Tile[tileObjectCacheSize];
    }

    private static int getCacheSizeFromMinCacheSize(int minSize) {
        for (int prime : CACHE_SIZES) {
            if (prime >= minSize) {
                return prime;
            }
        }
        return CACHE_SIZES[CACHE_SIZES.length - 1];
    }

    public static Tile getTile(byte flags, MapPoint point, Zoom zoom) {
        return getTile(flags, getXTileIndex(point, zoom), getYTileIndex(point, zoom), zoom);
    }

    public static Tile getTile(byte flags, Tile oldTile) {
        return getTile(flags, oldTile.xIndex, oldTile.yIndex, oldTile.zoom);
    }

    public static Tile getTile(byte flags, int xIndex, int yIndex, Zoom zoom) {
        xIndex %= zoom.getEquatorPixels() / 256;
        if (xIndex < 0) {
            xIndex += zoom.getEquatorPixels() / 256;
        }
        int hashCode = calculateHashCode(xIndex, yIndex, zoom, flags);
        int objectCacheIndex = hashCode % tileObjectCacheSize;
        if (objectCacheIndex < 0) {
            objectCacheIndex += tileObjectCacheSize;
        }
        Tile tile = tileObjectCache[objectCacheIndex];
        if (tile != null && tile.flags == flags && tile.xIndex == xIndex && tile.yIndex == yIndex && tile.zoom == zoom) {
            return tile;
        }
        tile = new Tile(flags, xIndex, yIndex, zoom, hashCode);
        tileObjectCache[objectCacheIndex] = tile;
        return tile;
    }

    private Tile(byte flags, int xIndex, int yIndex, Zoom zoom, int hashCode) {
        if (zoom == null) {
            throw new IllegalArgumentException("Zoom cannot be null");
        }
        this.flags = flags;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.zoom = zoom;
        this.hashCode = hashCode;
    }

    public static byte getSatType() {
        return Config.isChinaVersion() ? (byte) 3 : (byte) 6;
    }

    public byte getFlags() {
        return this.flags;
    }

    public int getXIndex() {
        return this.xIndex;
    }

    public int getYIndex() {
        return this.yIndex;
    }

    public Zoom getZoom() {
        return this.zoom;
    }

    public String toString() {
        return "(" + this.xIndex + ", " + this.yIndex + ", " + this.zoom + ")";
    }

    public int getXPixelTopLeft() {
        return this.xIndex * 256;
    }

    public int getYPixelTopLeft() {
        return this.yIndex * 256;
    }

    public Tile getZoomParent() {
        Zoom newZoom = Zoom.getZoom(this.zoom.getZoomLevel() - 1);
        if (newZoom == null) {
            return null;
        }
        int x = this.xIndex;
        int y = this.yIndex;
        if (x < 0) {
            x--;
        }
        if (y < 0) {
            y--;
        }
        return getTile(this.flags, x / 2, y / 2, newZoom);
    }

    public Tile toTraffic() {
        return getTile((byte) 4, this);
    }

    public static Tile read(DataInput is) throws IOException {
        try {
            return getTile(is.readByte(), is.readInt(), is.readInt(), Zoom.getZoom(is.readUnsignedByte()));
        } catch (IllegalArgumentException e) {
            throw new IOException(e.getMessage());
        }
    }

    public void write(DataOutput os) throws IOException {
        os.writeByte(this.flags);
        os.writeInt(this.xIndex);
        os.writeInt(this.yIndex);
        os.writeByte(this.zoom.getZoomLevel());
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Tile)) {
            return false;
        }
        Tile tile = (Tile) o;
        if (this.xIndex == tile.xIndex && this.yIndex == tile.yIndex && this.zoom == tile.zoom && this.flags == tile.flags) {
            return true;
        }
        return false;
    }

    public final int hashCode() {
        return this.hashCode;
    }

    private static int calculateHashCode(int xIndex, int yIndex, Zoom zoom, int flags) {
        return (((((xIndex * 29) ^ yIndex) * 29) + zoom.getZoomLevel()) << 8) + flags;
    }

    public boolean notValid() {
        return this.yIndex < 0 || this.yIndex >= this.zoom.getEquatorPixels() / 256;
    }

    public static int getXTileIndex(MapPoint point, Zoom zoom) {
        return point.getXPixel(zoom) / 256;
    }

    public static int getYTileIndex(MapPoint point, Zoom zoom) {
        return point.getYPixel(zoom) / 256;
    }
}
