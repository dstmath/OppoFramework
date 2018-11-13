package android_maps_conflict_avoidance.com.google.googlenav.map;

import android_maps_conflict_avoidance.com.google.common.Config;
import android_maps_conflict_avoidance.com.google.common.Log;
import android_maps_conflict_avoidance.com.google.common.OutOfMemoryHandler;
import android_maps_conflict_avoidance.com.google.common.StaticUtil;
import android_maps_conflict_avoidance.com.google.common.graphics.GoogleImage;
import android_maps_conflict_avoidance.com.google.common.ui.RepaintListener;
import android_maps_conflict_avoidance.com.google.common.util.ArrayUtil;
import android_maps_conflict_avoidance.com.google.googlenav.StartupHelper;
import android_maps_conflict_avoidance.com.google.googlenav.Stats;
import android_maps_conflict_avoidance.com.google.googlenav.datarequest.DataRequestDispatcher;
import android_maps_conflict_avoidance.com.google.googlenav.map.LayerService.TileUpdateObserver;
import com.google.android.maps.MapView.LayoutParams;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class MapService implements OutOfMemoryHandler, Runnable {
    private final boolean autoConfigCache;
    private MapTileRequest currentRequest = null;
    volatile boolean exitWorkThread = true;
    final MapTileStorage flashService;
    private final Object indefiniteThreadLockObject = new Object();
    private long lastMapMoveTime;
    private final Vector layerImageTiles = new Vector();
    private final Vector layerServices = new Vector();
    final Hashtable mapCache;
    private volatile boolean mapCacheLocked;
    private int maxCacheDataSize;
    private TileUpdateObserver observer;
    private long outOfMemoryTime = Long.MIN_VALUE;
    private final Vector repaintListeners = new Vector();
    private int requestType = 26;
    private int requestsOutstanding = 0;
    private int targetCacheDataSize;
    private final Hashtable tempScaledImages;
    private final Object timedThreadLockObject = new Object();

    private class MapTileRequest extends BaseTileRequest {
        private boolean closed = false;
        private boolean isForeground = true;
        private Vector tilePriorityList = new Vector();
        private Vector tileSchedule = new Vector();

        MapTileRequest(byte flags) {
            super(MapService.this.requestType, flags);
        }

        synchronized void requestTile(MapTile mapTile, int priority) {
            if (this.closed) {
                throw new RuntimeException("Adding tiles to closed request!");
            } else if (this.tileSchedule.indexOf(mapTile) == -1) {
                int i = this.tileSchedule.size();
                while (i > 0) {
                    if (priority >= ((Integer) this.tilePriorityList.elementAt(i - 1)).intValue()) {
                        this.tileSchedule.insertElementAt(mapTile, i);
                        this.tilePriorityList.insertElementAt(new Integer(priority), i);
                        break;
                    }
                    i--;
                }
                if (i == 0) {
                    this.tileSchedule.insertElementAt(mapTile, 0);
                    this.tilePriorityList.insertElementAt(new Integer(priority), 0);
                }
            }
        }

        public boolean isForeground() {
            return this.isForeground;
        }

        public void writeRequestData(DataOutput dos) throws IOException {
            MapService.this.requestsOutstanding = MapService.this.requestsOutstanding + 1;
            synchronized (this) {
                this.closed = true;
            }
            this.tilePriorityList = null;
            Tile[] tileList = new Tile[this.tileSchedule.size()];
            for (int i = 0; i < this.tileSchedule.size(); i++) {
                tileList[i] = ((MapTile) this.tileSchedule.elementAt(i)).getLocation();
            }
            writeRequestForTiles(tileList, dos);
        }

        public boolean readResponseData(DataInput dis) throws IOException {
            MapService.this.requestsOutstanding = MapService.this.requestsOutstanding - 1;
            super.readResponseData(dis);
            return this.tileSchedule.size() == 0;
        }

        protected void setTileEditionAndTextSize(int tileEdition, int textSize) {
            MapService.this.setTileEditionAndTextSize(tileEdition, textSize);
        }

        protected void handleEndOfResponse(int tileIndex) {
            Vector skippedTiles = new Vector();
            ArrayUtil.copyIntoVector(this.tileSchedule, tileIndex, skippedTiles);
            this.tileSchedule = skippedTiles;
            MapService.this.tempScaledImages.clear();
        }

        protected boolean processDownloadedTile(int tileIndex, Tile location, byte[] imageBytes) {
            MapTile mapTile = (MapTile) this.tileSchedule.elementAt(tileIndex);
            if (mapTile != null) {
                if (!mapTile.getLocation().equals(location)) {
                    return true;
                }
                mapTile.setData(imageBytes);
                mapTile.setLastAccessTime(mapTile.getLastAccessTime() - ((long) tileIndex));
                for (int i = 0; i < MapService.this.repaintListeners.size(); i++) {
                    ((RepaintListener) MapService.this.repaintListeners.elementAt(i)).repaint();
                }
            }
            return false;
        }
    }

    void setMapCacheLocked(boolean mapCacheLocked) {
        this.mapCacheLocked = mapCacheLocked;
    }

    MapService(int maxCacheDataSize, int targetCacheDataSize, int maxFlashSize, int maxRecordStores, String tileRecordStoreName) {
        if (maxCacheDataSize == -1) {
            this.autoConfigCache = true;
            this.maxCacheDataSize = 25000;
            setAutoTargetCacheSize();
        } else {
            this.autoConfigCache = false;
            this.maxCacheDataSize = maxCacheDataSize;
            if (targetCacheDataSize == -1) {
                setAutoTargetCacheSize();
            } else {
                this.targetCacheDataSize = targetCacheDataSize;
            }
        }
        this.tempScaledImages = new Hashtable();
        this.mapCache = new Hashtable();
        this.mapCacheLocked = false;
        if (maxFlashSize > 0) {
            this.flashService = new MapFlashService(this, tileRecordStoreName, maxFlashSize, maxRecordStores);
        } else {
            this.flashService = new NullMapTileStorage();
        }
        this.lastMapMoveTime = getRelativeTime();
        StartupHelper.addPostStartupBgCallback(new Runnable() {
            public void run() {
                MapService.this.startWorkThread();
            }
        });
        StaticUtil.registerOutOfMemoryHandler(this);
    }

    Hashtable getMapCache() {
        return this.mapCache;
    }

    private void setAutoTargetCacheSize() {
        this.targetCacheDataSize = (this.maxCacheDataSize * 4) / 5;
    }

    void close(boolean saveState) {
        StaticUtil.removeOutOfMemoryHandler(this);
        stopWorkThread();
        this.flashService.close(saveState);
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            LayerService layerService = (LayerService) this.layerServices.elementAt(i);
            layerService.close();
            StaticUtil.removeOutOfMemoryHandler(layerService);
        }
        this.layerServices.removeAllElements();
    }

    public MapTile getTile(Tile tile, int priority, boolean loadTile, boolean scaleOk) {
        return getTile(tile, priority, loadTile, scaleOk, Long.MIN_VALUE);
    }

    MapTile getTile(Tile tile, int priority, boolean loadTile, boolean scaleOk, long accessTime) {
        return getTile(tile, priority, loadTile, scaleOk ? 2 : 0, accessTime);
    }

    MapTile getTile(Tile tile, int priority, boolean loadTile, int scaleMode, long accessTime) {
        Throwable th;
        MapTile mapTile = (MapTile) this.mapCache.get(tile);
        if (accessTime == Long.MIN_VALUE) {
            accessTime = Config.getInstance().getClock().currentTimeMillis();
        }
        if (mapTile == null) {
            if (this.mapCacheLocked) {
                mapTile = new MapTile(tile, (GoogleImage) null, true);
            } else {
                synchronized (this.mapCache) {
                    setMapCacheLocked(true);
                    try {
                        mapTile = this.flashService.getMapTile(tile);
                        if (mapTile == null) {
                            GoogleImage tempImage = getTempImage(tile, scaleMode);
                            MapTile mapTile2;
                            if (loadTile && DataRequestDispatcher.getInstance().canDispatchNow()) {
                                mapTile2 = new MapTile(tile, tempImage);
                                try {
                                    queueTileRequest(mapTile2, priority);
                                    addMapEntry(mapTile2);
                                    Stats.getInstance().flashCacheMiss();
                                    mapTile = mapTile2;
                                } catch (Throwable th2) {
                                    th = th2;
                                    mapTile = mapTile2;
                                    setMapCacheLocked(false);
                                    throw th;
                                }
                            }
                            mapTile2 = new MapTile(tile, tempImage, true);
                            addMapEntry(mapTile2);
                            mapTile = mapTile2;
                        } else {
                            if (!loadTile) {
                                accessTime -= 20000;
                            }
                            addMapEntry(mapTile);
                            Stats.getInstance().flashCacheHit();
                        }
                        setMapCacheLocked(false);
                    } catch (Throwable th3) {
                        th = th3;
                    }
                }
            }
        } else if (!mapTile.isComplete() && !mapTile.getRequested() && loadTile && DataRequestDispatcher.getInstance().canDispatchNow()) {
            queueTileRequest(mapTile, priority);
            Stats.getInstance().flashCacheMiss();
        }
        mapTile.setLastAccessTime(accessTime);
        return mapTile;
    }

    private GoogleImage getTempImage(Tile tile, int scaleMode) {
        switch (scaleMode) {
            case LayoutParams.MODE_MAP /*0*/:
                return null;
            case 1:
                return getScaledImageFromCache(tile);
            default:
                return getOrCreateScaledImage(tile);
        }
    }

    private GoogleImage getScaledImageFromCache(Tile tile) {
        return (GoogleImage) this.tempScaledImages.get(tile);
    }

    private GoogleImage getOrCreateScaledImage(Tile tile) {
        GoogleImage image = (GoogleImage) this.tempScaledImages.get(tile);
        if (image == null) {
            image = createScaledImage(tile);
            if (image != null) {
                this.tempScaledImages.put(tile, image);
            }
        }
        return image;
    }

    private GoogleImage createScaledImage(Tile tile) {
        long currentTime = getRelativeTime();
        if (currentTime < this.outOfMemoryTime + 10000) {
            return null;
        }
        try {
            Tile parent = tile.getZoomParent();
            if (parent == null) {
                return null;
            }
            int ratio = parent.getZoom().getZoomRatio(tile.getZoom());
            MapTile parentMapTile = getTile(parent, 0, false, false);
            if (ratio == 2 && parentMapTile.hasImage()) {
                return createScaledImage(tile, parent, parentMapTile.getImage());
            }
            return null;
        } catch (OutOfMemoryError e) {
            clearScaledImages();
            this.outOfMemoryTime = currentTime;
            Log.logQuietThrowable("Map Service image scaling", e);
            return null;
        }
    }

    private void clearScaledImages() {
        synchronized (this.mapCache) {
            this.mapCacheLocked = true;
            this.tempScaledImages.clear();
            Enumeration enumeration = this.mapCache.elements();
            while (enumeration.hasMoreElements()) {
                ((MapTile) enumeration.nextElement()).removeScaledImage();
            }
            this.mapCacheLocked = false;
        }
    }

    private GoogleImage createScaledImage(Tile tile, Tile parentTile, GoogleImage parentImage) {
        int xOffset;
        int yOffset;
        if (tile.getXIndex() == parentTile.getXIndex() * 2) {
            xOffset = 0;
        } else {
            xOffset = 128;
        }
        if (tile.getYIndex() == parentTile.getYIndex() * 2) {
            yOffset = 0;
        } else {
            yOffset = 128;
        }
        return parentImage.createScaledImage(xOffset, yOffset, 128, 128, 256, 256);
    }

    private void queueTileRequest(MapTile mapTile, int priority) {
        if (this.currentRequest == null) {
            this.currentRequest = new MapTileRequest(mapTile.getLocation().getFlags());
        }
        this.currentRequest.requestTile(mapTile, priority);
        mapTile.setRequested(true);
    }

    private void doCompact(boolean emergency) {
        long maxAge = emergency ? 2000 : 4000;
        synchronized (this.mapCache) {
            setMapCacheLocked(true);
            try {
                long currentTime = Config.getInstance().getClock().currentTimeMillis();
                Enumeration keys = this.mapCache.keys();
                while (keys.hasMoreElements()) {
                    Tile tile = (Tile) keys.nextElement();
                    MapTile mapTile = (MapTile) this.mapCache.get(tile);
                    if (mapTile.getLastAccessTime() + maxAge < currentTime) {
                        mapTile.compact();
                        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
                            ((LayerService) this.layerServices.elementAt(i)).doCompact(Tile.getTile((byte) 8, tile));
                        }
                    }
                }
                setMapCacheLocked(false);
            } catch (Throwable th) {
                setMapCacheLocked(false);
            }
        }
    }

    private void addMapEntry(MapTile mapTile) {
        this.mapCache.put(mapTile.getLocation(), mapTile);
    }

    void checkTrimCache() {
        int cacheSize = getCacheSize();
        if (cacheSize > this.maxCacheDataSize) {
            if (this.autoConfigCache) {
                System.gc();
                this.maxCacheDataSize = Math.max(25000, Math.min(((int) ((Runtime.getRuntime().freeMemory() + ((long) cacheSize)) - 40000)) / 2, ((int) Runtime.getRuntime().totalMemory()) / 3));
                setAutoTargetCacheSize();
                if (cacheSize < this.maxCacheDataSize) {
                    return;
                }
            }
            trimCache(cacheSize);
        }
    }

    private void trimCache(int cacheSize) {
        synchronized (this.mapCache) {
            try {
                this.mapCacheLocked = true;
                Tile[] sortedList = getSortedCacheList();
                for (int i = 0; i < sortedList.length && cacheSize > this.targetCacheDataSize; i++) {
                    Tile minKey = sortedList[i];
                    MapTile mapTile = (MapTile) this.mapCache.get(minKey);
                    if (mapTile.isComplete() || !mapTile.getRequested()) {
                        this.mapCache.remove(minKey);
                        cacheSize -= mapTile.getDataSize();
                    }
                }
                this.mapCacheLocked = false;
            } catch (Throwable th) {
                this.mapCacheLocked = false;
            }
        }
    }

    int getCacheSize() {
        int cacheSize = 0;
        synchronized (this.mapCache) {
            Enumeration entries = this.mapCache.elements();
            while (entries.hasMoreElements()) {
                cacheSize += ((MapTile) entries.nextElement()).getDataSize();
            }
        }
        return cacheSize;
    }

    public int restoreBaseImagesIfNeeded() {
        int renderedImageCount;
        synchronized (this.mapCache) {
            try {
                setMapCacheLocked(true);
                renderedImageCount = getRenderedImageCount();
                if (renderedImageCount > 48) {
                    Tile[] sortedList = getSortedCacheList();
                    for (int i = 0; i < sortedList.length && renderedImageCount > 24; i++) {
                        MapTile mapTile = (MapTile) this.mapCache.get(sortedList[i]);
                        if (mapTile.hasRenderedImage()) {
                            mapTile.restoreBaseImage();
                            renderedImageCount--;
                        }
                    }
                }
                setMapCacheLocked(false);
            } catch (Throwable th) {
                setMapCacheLocked(false);
            }
        }
        return renderedImageCount;
    }

    int getRenderedImageCount() {
        int renderedImageCount = 0;
        Enumeration entries = this.mapCache.elements();
        while (entries.hasMoreElements()) {
            if (((MapTile) entries.nextElement()).hasRenderedImage()) {
                renderedImageCount++;
            }
        }
        return renderedImageCount;
    }

    static long getScore(Tile tile, long currentTime, long lastAccessTime) {
        return currentTime - lastAccessTime;
    }

    long getTileDate(Tile tile) {
        return ((MapTile) this.mapCache.get(tile)).getLastAccessTime();
    }

    Tile[] getSortedCacheList() {
        long startTime = Config.getInstance().getClock().currentTimeMillis();
        Tile[] list = new Tile[this.mapCache.size()];
        long[] scoreList = new long[this.mapCache.size()];
        int index = 0;
        Enumeration enumeration = this.mapCache.keys();
        while (enumeration.hasMoreElements()) {
            list[index] = (Tile) enumeration.nextElement();
            scoreList[index] = getScore(list[index], startTime, getTileDate(list[index]));
            index++;
        }
        sort(scoreList, list);
        return list;
    }

    private void swap(long[] scoreList, Tile[] list, int indexA, int indexB) {
        Tile tempTile = list[indexB];
        list[indexB] = list[indexA];
        list[indexA] = tempTile;
        long tempScore = scoreList[indexB];
        scoreList[indexB] = scoreList[indexA];
        scoreList[indexA] = tempScore;
    }

    private int partition(long[] scoreList, Tile[] list, int left, int right, int pivotIndex) {
        long pivotValue = scoreList[pivotIndex];
        swap(scoreList, list, pivotIndex, right);
        int i = left;
        int store = left;
        while (i < right) {
            int store2;
            if (scoreList[i] >= pivotValue) {
                store2 = store + 1;
                swap(scoreList, list, i, store);
            } else {
                store2 = store;
            }
            i++;
            store = store2;
        }
        if (scoreList[right] <= scoreList[store]) {
            return right;
        }
        swap(scoreList, list, right, store);
        return store;
    }

    private void qsort(long[] scoreList, Tile[] list, int left, int right) {
        if (right > left) {
            int newPivot = partition(scoreList, list, left, right, left);
            qsort(scoreList, list, left, newPivot - 1);
            qsort(scoreList, list, newPivot + 1, right);
        }
    }

    private void sort(long[] scoreList, Tile[] list) {
        qsort(scoreList, list, 0, list.length - 1);
    }

    boolean requestTiles() {
        if (this.currentRequest == null) {
            return false;
        }
        MapTileRequest tempRequest = this.currentRequest;
        this.currentRequest = null;
        DataRequestDispatcher.getInstance().addDataRequest(tempRequest);
        return true;
    }

    void requestLayerTiles() {
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            LayerService layerService = (LayerService) this.layerServices.elementAt(i);
            if (layerService.needFetchLayerTiles()) {
                layerService.requestTiles();
            }
        }
    }

    public Vector getLayerTiles(Tile tile, boolean fetch) {
        this.layerImageTiles.removeAllElements();
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            LayerService layerService = (LayerService) this.layerServices.elementAt(i);
            if (layerService.needFetchLayerTiles()) {
                LayerTile layerTile = layerService.getTile(Tile.getTile((byte) 8, tile), fetch);
                if (layerTile != null && layerTile.hasImage()) {
                    this.layerImageTiles.addElement(layerTile.getImage());
                }
            }
        }
        return this.layerImageTiles;
    }

    public void notifyLayerTilesDirty() {
        if (this.observer != null) {
            this.observer.setLayerTilesDirty();
        }
        for (int i = this.layerServices.size() - 1; i >= 0; i--) {
            ((LayerService) this.layerServices.elementAt(i)).notifyLayerTilesDirty();
        }
    }

    void setTileEditionAndTextSize(int tileEdition, int textSize) {
        if (this.flashService.setTileEditionAndTextSize(tileEdition, textSize)) {
            synchronized (this.mapCache) {
                Enumeration mapTiles = this.mapCache.keys();
                Vector toRemove = new Vector();
                while (mapTiles.hasMoreElements()) {
                    Tile tile = (Tile) mapTiles.nextElement();
                    if (((MapTile) this.mapCache.get(tile)).isComplete()) {
                        toRemove.addElement(tile);
                    }
                }
                for (int i = 0; i < toRemove.size(); i++) {
                    this.mapCache.remove(toRemove.elementAt(i));
                }
            }
        }
    }

    void mapChanged() {
        this.lastMapMoveTime = getRelativeTime();
        this.flashService.mapChanged();
        synchronized (this.indefiniteThreadLockObject) {
            this.indefiniteThreadLockObject.notify();
        }
    }

    public void handleOutOfMemory(boolean warning) {
        FlashRecord.clearDataCache();
        clearScaledImages();
        synchronized (this.mapCache) {
            doCompact(true);
            if (this.autoConfigCache) {
                this.maxCacheDataSize = 25000;
                setAutoTargetCacheSize();
            } else {
                this.maxCacheDataSize = Math.max(this.maxCacheDataSize - 8000, 25000);
                setAutoTargetCacheSize();
            }
            checkTrimCache();
        }
    }

    public void run() {
        long nextTrimTime = getRelativeTime() + 2101;
        long nextCompactTime = getRelativeTime() + 3113;
        while (!this.exitWorkThread) {
            try {
                long currentTime;
                synchronized (this.timedThreadLockObject) {
                    try {
                        long j;
                        currentTime = getRelativeTime();
                        if (nextTrimTime < nextCompactTime) {
                            j = nextTrimTime;
                        } else {
                            j = nextCompactTime;
                        }
                        long nextWakeup = j - currentTime;
                        if (nextWakeup > 0) {
                            this.timedThreadLockObject.wait(nextWakeup);
                        }
                    } catch (InterruptedException e) {
                    }
                }
                if (!this.exitWorkThread) {
                    currentTime = getRelativeTime();
                    if (nextTrimTime < currentTime) {
                        checkTrimCache();
                        nextTrimTime = currentTime + 2101;
                    }
                    if (nextCompactTime < currentTime) {
                        doCompact(false);
                        nextCompactTime = currentTime + 3113;
                    }
                    if (!this.flashService.writeCache() && this.lastMapMoveTime + 4000 < currentTime) {
                        synchronized (this.indefiniteThreadLockObject) {
                            try {
                                this.indefiniteThreadLockObject.wait();
                            } catch (InterruptedException e2) {
                            }
                        }
                    }
                }
            } catch (Exception e3) {
                Log.logThrowable("MapService BG", e3);
            } catch (OutOfMemoryError e4) {
                StaticUtil.handleOutOfMemory();
            }
        }
    }

    private static long getRelativeTime() {
        return Config.getInstance().getClock().relativeTimeMillis();
    }

    private void stopWorkThread() {
        if (!this.exitWorkThread) {
            this.exitWorkThread = true;
            synchronized (this.timedThreadLockObject) {
                this.timedThreadLockObject.notify();
            }
            synchronized (this.indefiniteThreadLockObject) {
                this.indefiniteThreadLockObject.notify();
            }
        }
    }

    private void startWorkThread() {
        if (this.exitWorkThread) {
            this.exitWorkThread = false;
            Thread bgThread = new Thread(this, "MapService");
            bgThread.setPriority(1);
            bgThread.start();
        }
    }

    void pause() {
        stopWorkThread();
    }

    void resume() {
        startWorkThread();
    }
}
