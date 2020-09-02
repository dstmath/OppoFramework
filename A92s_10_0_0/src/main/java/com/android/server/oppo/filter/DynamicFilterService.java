package com.android.server.oppo.filter;

import android.content.Context;
import android.os.Binder;
import android.os.SystemProperties;
import android.util.ArrayMap;
import android.util.AtomicFile;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.JsonWriter;
import android.util.Log;
import android.util.Slog;
import com.android.server.SystemService;
import com.oppo.filter.IDynamicFilterService;
import com.oppo.romupdate.RomUpdateHelper;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class DynamicFilterService extends SystemService {
    private static final int ADDED_FILTER_MASK = 4;
    private static final int BASE_FILTER_MASK = 1;
    public static boolean DEBUG = SystemProperties.getBoolean("persist.sys.assert.panic", false);
    private static final int DISABLED_FILTER_MASK = 2;
    public static final String TAG = "DynamicFilterService";
    /* access modifiers changed from: private */
    public final Context mContext;
    /* access modifiers changed from: private */
    public FilterList mDynamicFilterList;
    /* access modifiers changed from: private */
    public FilterListHelper mFilterListHelper;
    /* access modifiers changed from: private */
    public final Object mLock = new Object();

    public DynamicFilterService(Context context) {
        super(context);
        this.mContext = context;
        this.mFilterListHelper = new FilterListHelper();
        this.mDynamicFilterList = this.mFilterListHelper.initializeDynamicFilterList();
    }

    public void systemReady() {
        Slog.d(TAG, "systemReady");
        this.mFilterListHelper.registerRomUpdate();
    }

    private final class DynamicFilterServiceWrapper extends IDynamicFilterService.Stub {
        private DynamicFilterServiceWrapper() {
        }

        public boolean hasFilter(String name) {
            boolean containsKey;
            checkPermission();
            synchronized (DynamicFilterService.this.mLock) {
                containsKey = DynamicFilterService.this.mDynamicFilterList.mData.containsKey(name);
            }
            return containsKey;
        }

        /* JADX WARNING: Code restructure failed: missing block: B:11:0x002c, code lost:
            return r2;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002e, code lost:
            return false;
         */
        public boolean inFilter(String name, String tag) {
            int index;
            checkPermission();
            synchronized (DynamicFilterService.this.mLock) {
                FilterList.FilterDetail detail = DynamicFilterService.this.mDynamicFilterList.mData.get(name);
                boolean z = false;
                if (detail != null && (index = detail.indexOf(tag)) != -1) {
                    if ((detail.getStatus(index) & 2) == 0) {
                        z = true;
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0067, code lost:
            if (r5.equals(r10) == false) goto L_0x0069;
         */
        public void addToFilter(String name, String tag, String value) {
            checkPermission();
            if (name != null && tag != null) {
                synchronized (DynamicFilterService.this.mLock) {
                    boolean changed = false;
                    FilterList.FilterDetail detail = DynamicFilterService.this.mDynamicFilterList.mData.get(name);
                    if (detail == null) {
                        FilterList.FilterDetail detail2 = new FilterList.FilterDetail();
                        detail2.add(tag, value, 4);
                        DynamicFilterService.this.mDynamicFilterList.mData.put(name, detail2);
                        changed = true;
                    } else {
                        int index = detail.indexOf(tag);
                        if (index == -1) {
                            detail.add(tag, value, 4);
                            changed = true;
                        } else {
                            int status = detail.getStatus(index);
                            if ((status & 2) != 0) {
                                detail.setStatus(index, status ^ 2);
                                changed = true;
                            }
                            String currValue = detail.getValue(index);
                            if (currValue == null) {
                                if (value == null) {
                                }
                            }
                            detail.setValue(index, value);
                            changed = true;
                        }
                    }
                    if (changed) {
                        DynamicFilterService.this.mFilterListHelper.saveFilterListToFile("data/format_unclear/filter/sys_dynamic_filter_list.json", DynamicFilterService.this.mDynamicFilterList);
                    }
                }
            }
        }

        public void removeFromFilter(String name, String tag) {
            int index;
            checkPermission();
            if (name != null && tag != null) {
                synchronized (DynamicFilterService.this.mLock) {
                    boolean changed = false;
                    FilterList.FilterDetail detail = DynamicFilterService.this.mDynamicFilterList.mData.get(name);
                    if (!(detail == null || (index = detail.indexOf(tag)) == -1)) {
                        int status = detail.getStatus(index);
                        if ((status & 4) != 0) {
                            detail.remove(index);
                            if (detail.size() == 0) {
                                DynamicFilterService.this.mDynamicFilterList.mData.remove(name);
                            }
                            changed = true;
                        } else if ((status & 2) == 0) {
                            detail.setStatus(index, status | 2);
                            changed = true;
                        }
                    }
                    if (changed) {
                        DynamicFilterService.this.mFilterListHelper.saveFilterListToFile("data/format_unclear/filter/sys_dynamic_filter_list.json", DynamicFilterService.this.mDynamicFilterList);
                    }
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:13:0x002d, code lost:
            return null;
         */
        public String getFilterTagValue(String name, String tag) {
            int index;
            synchronized (DynamicFilterService.this.mLock) {
                FilterList.FilterDetail detail = DynamicFilterService.this.mDynamicFilterList.mData.get(name);
                if (detail != null && (index = detail.indexOf(tag)) != -1 && (detail.getStatus(index) & 2) == 0) {
                    String value = detail.getValue(index);
                    return value;
                }
            }
        }

        /* access modifiers changed from: protected */
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            int uid = Binder.getCallingUid();
            if (uid == 1000 || DynamicFilterService.this.mContext.checkCallingOrSelfPermission("android.permission.DUMP") == 0) {
                synchronized (DynamicFilterService.this.mLock) {
                    pw.print("version: ");
                    pw.println(DynamicFilterService.this.mDynamicFilterList.mVersion);
                    for (Map.Entry<String, FilterList.FilterDetail> entry : DynamicFilterService.this.mDynamicFilterList.mData.entrySet()) {
                        pw.print("name: ");
                        pw.println(entry.getKey());
                        pw.print("tag: ");
                        pw.println(entry.getValue().mTags);
                        pw.print("value: ");
                        pw.println(entry.getValue().mValues);
                        pw.print("status: ");
                        pw.println(entry.getValue().mStatuses);
                    }
                }
                return;
            }
            pw.println("Permission Denial: can't dump dynamic filter service from from pid=" + Binder.getCallingPid() + ", uid=" + uid);
        }

        private boolean checkPermission() {
            int uid = Binder.getCallingUid();
            if (uid == 1000 || DynamicFilterService.this.mContext.checkCallingOrSelfPermission("android.permission.CRYPT_KEEPER") == 0) {
                return true;
            }
            int pid = Binder.getCallingPid();
            throw new SecurityException("Permission Denial: can't access dynamic filter from pid=" + pid + ", uid=" + uid);
        }
    }

    /* JADX WARN: Type inference failed for: r0v1, types: [com.android.server.oppo.filter.DynamicFilterService$DynamicFilterServiceWrapper, android.os.IBinder] */
    @Override // com.android.server.SystemService
    public void onStart() {
        Slog.d(TAG, "onStart");
        publishBinderService("dynamic_filter", new DynamicFilterServiceWrapper());
    }

    /* access modifiers changed from: package-private */
    public class FilterListHelper {
        private static final String DATA_FILTER_FILE = "data/format_unclear/filter/sys_filter_list.json";
        private static final String DYNAMIC_FILTER_FILE = "data/format_unclear/filter/sys_dynamic_filter_list.json";
        private static final String FILTER_NAME = "sys_filter_list";
        private static final String SYS_FILTER_FILE = "system/etc/sys_filter_list.json";
        private RomUpdateHelper mRomUpdateHelper;

        FilterListHelper() {
        }

        /* access modifiers changed from: package-private */
        public FilterList initializeDynamicFilterList() {
            FilterList filterListSys = new FilterList();
            readFilterListFromFile(SYS_FILTER_FILE, filterListSys, true);
            FilterList filterListData = new FilterList();
            readFilterListFromFile(DATA_FILTER_FILE, filterListData, true);
            FilterList filterListDynamic = new FilterList();
            readFilterListFromFile(DYNAMIC_FILTER_FILE, filterListDynamic, false);
            if (filterListDynamic.mVersion == -1) {
                if (filterListData.mVersion >= filterListSys.mVersion) {
                    readFilterListFromFile(DATA_FILTER_FILE, filterListData, false);
                    return filterListData;
                }
                readFilterListFromFile(SYS_FILTER_FILE, filterListSys, false);
                return filterListSys;
            } else if (filterListData.mVersion <= filterListDynamic.mVersion && filterListSys.mVersion <= filterListDynamic.mVersion) {
                return filterListDynamic;
            } else {
                if (filterListData.mVersion >= filterListSys.mVersion) {
                    readFilterListFromFile(DATA_FILTER_FILE, filterListData, false);
                    mergeBaseFilterToDynamicFilterList(filterListData, filterListDynamic);
                } else {
                    readFilterListFromFile(SYS_FILTER_FILE, filterListSys, false);
                    mergeBaseFilterToDynamicFilterList(filterListSys, filterListDynamic);
                }
                saveFilterListToFile(DYNAMIC_FILTER_FILE, filterListDynamic);
                return filterListDynamic;
            }
        }

        /* access modifiers changed from: package-private */
        public void registerRomUpdate() {
            this.mRomUpdateHelper = new RomUpdateHelper(DynamicFilterService.this.mContext, FILTER_NAME);
            this.mRomUpdateHelper.setUpdateInfoListener(new RomUpdateHelper.UpdateInfoListener() {
                /* class com.android.server.oppo.filter.DynamicFilterService.FilterListHelper.AnonymousClass1 */

                public void onUpdateInfoChanged(String content) {
                    FilterList filterList = new FilterList();
                    StringReader reader = new StringReader(content);
                    FilterListParser.parseVersion(reader, filterList);
                    if (filterList.mVersion > DynamicFilterService.this.mDynamicFilterList.mVersion) {
                        try {
                            reader.reset();
                        } catch (IOException e) {
                        }
                        FilterListParser.parseContent(reader, filterList);
                        synchronized (DynamicFilterService.this.mLock) {
                            FilterListHelper.this.mergeBaseFilterToDynamicFilterList(filterList, DynamicFilterService.this.mDynamicFilterList);
                        }
                        FilterListHelper.this.saveFilterListToFile(FilterListHelper.DATA_FILTER_FILE, filterList);
                        FilterListHelper filterListHelper = FilterListHelper.this;
                        filterListHelper.saveFilterListToFile(FilterListHelper.DYNAMIC_FILTER_FILE, DynamicFilterService.this.mDynamicFilterList);
                    }
                }
            });
            this.mRomUpdateHelper.registerUpdateBroadcastReceiver();
        }

        /* access modifiers changed from: package-private */
        public void mergeBaseFilterToDynamicFilterList(FilterList baseFilterList, FilterList dynamicFilter) {
            dynamicFilter.mVersion = baseFilterList.mVersion;
            ArrayMap<String, FilterList.FilterDetail> baseData = baseFilterList.mData;
            ArrayMap<String, FilterList.FilterDetail> dynamicData = dynamicFilter.mData;
            if ("MAGIC_REBUILD_DYNAMIC_FILTER".equals(baseFilterList.mComment)) {
                dynamicFilter.mData = baseFilterList.mData;
                return;
            }
            Iterator<Map.Entry<String, FilterList.FilterDetail>> it = dynamicData.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, FilterList.FilterDetail> entry = it.next();
                String key = entry.getKey();
                FilterList.FilterDetail detail = entry.getValue();
                for (int i = detail.size() - 1; i >= 0; i--) {
                    if ((detail.getStatus(i) & 1) != 0) {
                        FilterList.FilterDetail detailBase = baseData.get(key);
                        if (detailBase == null) {
                            detail.remove(i);
                            if (detail.size() == 0) {
                                it.remove();
                            }
                        } else {
                            int indexBase = detailBase.indexOf(detail.getTag(i));
                            if (indexBase == -1) {
                                detail.remove(i);
                            } else {
                                String value = detailBase.getValue(indexBase);
                                if (value != null) {
                                    detail.setValue(i, value);
                                }
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String, FilterList.FilterDetail> entry2 : baseData.entrySet()) {
                String key2 = entry2.getKey();
                FilterList.FilterDetail detailBase2 = entry2.getValue();
                FilterList.FilterDetail detail2 = dynamicData.get(key2);
                if (detail2 != null) {
                    for (int i2 = 0; i2 < detailBase2.size(); i2++) {
                        String tag = detailBase2.getTag(i2);
                        int index = detail2.indexOf(tag);
                        if (index == -1) {
                            detail2.add(tag, detailBase2.getValue(i2), Integer.valueOf(detailBase2.getStatus(i2)));
                        } else {
                            detail2.setValue(index, detailBase2.getValue(i2));
                            if ((detail2.getStatus(index) & 4) != 0) {
                                detail2.setStatus(index, detailBase2.getStatus(i2));
                            }
                        }
                    }
                } else {
                    dynamicData.put(key2, detailBase2);
                }
            }
        }

        /* JADX WARNING: Code restructure failed: missing block: B:12:0x0023, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0024, code lost:
            $closeResource(r2, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0027, code lost:
            throw r3;
         */
        private void readFilterListFromFile(String filePath, FilterList filterList, boolean versionOnly) {
            try {
                InputStreamReader reader = new InputStreamReader(new AtomicFile(new File(filePath)).openRead());
                if (versionOnly) {
                    FilterListParser.parseVersion(reader, filterList);
                } else {
                    FilterListParser.parseContent(reader, filterList);
                }
                $closeResource(null, reader);
            } catch (Exception e) {
                Log.d(DynamicFilterService.TAG, "readFilterListFromFile", e);
            }
        }

        private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
            if (x0 != null) {
                try {
                    x1.close();
                } catch (Throwable th) {
                    x0.addSuppressed(th);
                }
            } else {
                x1.close();
            }
        }

        /* access modifiers changed from: private */
        /* JADX WARNING: Code restructure failed: missing block: B:13:0x0027, code lost:
            r4 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:14:0x0028, code lost:
            $closeResource(r3, r2);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:15:0x002b, code lost:
            throw r4;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:19:0x002e, code lost:
            r3 = move-exception;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:20:0x002f, code lost:
            if (r1 != null) goto L_0x0031;
         */
        /* JADX WARNING: Code restructure failed: missing block: B:21:0x0031, code lost:
            $closeResource(r2, r1);
         */
        /* JADX WARNING: Code restructure failed: missing block: B:22:0x0034, code lost:
            throw r3;
         */
        public void saveFilterListToFile(String filePath, FilterList filterList) {
            AtomicFile file = new AtomicFile(new File(filePath));
            try {
                FileOutputStream fos = file.startWrite();
                OutputStreamWriter writer = new OutputStreamWriter(fos);
                FilterListParser.saveFilterList(writer, filterList);
                file.finishWrite(fos);
                $closeResource(null, writer);
                if (fos != null) {
                    $closeResource(null, fos);
                }
            } catch (Exception e) {
                Log.d(DynamicFilterService.TAG, "saveFilterListToFile", e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public static class FilterList {
        String mComment = "";
        ArrayMap<String, FilterDetail> mData = new ArrayMap<>();
        long mVersion = -1;

        FilterList() {
        }

        static class FilterDetail {
            /* access modifiers changed from: private */
            public ArrayList<Integer> mStatuses;
            /* access modifiers changed from: private */
            public ArrayList<String> mTags;
            /* access modifiers changed from: private */
            public ArrayList<String> mValues;

            FilterDetail() {
                this.mTags = new ArrayList<>();
                this.mValues = new ArrayList<>();
                this.mStatuses = new ArrayList<>();
            }

            FilterDetail(ArrayList<String> tags, ArrayList<String> values, ArrayList<Integer> statuses) {
                this.mTags = tags;
                this.mValues = values;
                this.mStatuses = statuses;
            }

            /* access modifiers changed from: package-private */
            public void add(String tag, String value, Integer status) {
                this.mTags.add(tag);
                this.mValues.add(value);
                this.mStatuses.add(status);
            }

            /* access modifiers changed from: package-private */
            public void remove(int index) {
                this.mTags.remove(index);
                this.mValues.remove(index);
                this.mStatuses.remove(index);
            }

            /* access modifiers changed from: package-private */
            public int indexOf(String tag) {
                return this.mTags.indexOf(tag);
            }

            /* access modifiers changed from: package-private */
            public int size() {
                return this.mTags.size();
            }

            /* access modifiers changed from: package-private */
            public String getTag(int index) {
                return this.mTags.get(index);
            }

            /* access modifiers changed from: package-private */
            public void setValue(int index, String value) {
                this.mValues.set(index, value);
            }

            /* access modifiers changed from: package-private */
            public void setStatus(int index, int status) {
                this.mStatuses.set(index, Integer.valueOf(status));
            }

            /* access modifiers changed from: package-private */
            public String getValue(int index) {
                return this.mValues.get(index);
            }

            /* access modifiers changed from: package-private */
            public int getStatus(int index) {
                return this.mStatuses.get(index).intValue();
            }

            static /* synthetic */ boolean lambda$isValueAllDef$0(String s) {
                return s == null;
            }

            /* access modifiers changed from: package-private */
            public boolean isValueAllDef() {
                return this.mValues.stream().allMatch($$Lambda$DynamicFilterService$FilterList$FilterDetail$2OAMiOEgVr6tuU52x2lnfhpzE1c.INSTANCE);
            }

            static /* synthetic */ boolean lambda$isStatusAllDef$1(Integer s) {
                return s.intValue() == 1;
            }

            /* access modifiers changed from: package-private */
            public boolean isStatusAllDef() {
                return this.mStatuses.stream().allMatch($$Lambda$DynamicFilterService$FilterList$FilterDetail$ZuLBB4HBwX4tYq4DTKDZTKbHg.INSTANCE);
            }
        }
    }

    static class FilterListParser {
        static final String COMMENT = "comment";
        static final String FILTER_CONTENT = "filter_content";
        static final String FILTER_NAME = "name";
        static final String FILTER_STATUS_NAME = "status";
        static final String FILTER_TAG_NAME = "tag";
        static final String FILTER_VALUE_NAME = "value";
        static final String MAGIC_REBUILD_DYNAMIC_FILTER = "MAGIC_REBUILD_DYNAMIC_FILTER";
        static final String VERSION = "version";

        FilterListParser() {
        }

        static void parseVersion(Reader reader, FilterList filterList) {
            try {
                parseFilterList(reader, filterList, true);
            } catch (Exception e) {
                filterList.mVersion = -1;
                Log.d(DynamicFilterService.TAG, "parse version failed", e);
            }
        }

        static void parseContent(Reader reader, FilterList filterList) {
            try {
                parseFilterList(reader, filterList, false);
            } catch (Exception e) {
                filterList.mVersion = -1;
                Log.d(DynamicFilterService.TAG, "parse content failed", e);
            }
        }

        static void saveFilterList(Writer wt, FilterList filterList) throws IOException {
            JsonWriter writer = new JsonWriter(wt);
            writer.beginObject();
            writer.name(COMMENT).value(filterList.mComment);
            writer.name("version").value(filterList.mVersion);
            writer.name(FILTER_CONTENT);
            writer.beginArray();
            for (Map.Entry<String, FilterList.FilterDetail> entry : filterList.mData.entrySet()) {
                writer.beginObject();
                writer.name("name").value(entry.getKey());
                writer.name(FILTER_TAG_NAME);
                FilterList.FilterDetail detail = entry.getValue();
                writer.beginArray();
                Iterator it = detail.mTags.iterator();
                while (it.hasNext()) {
                    writer.value((String) it.next());
                }
                writer.endArray();
                if (!detail.isValueAllDef()) {
                    writer.name(FILTER_VALUE_NAME);
                    writer.beginArray();
                    Iterator it2 = detail.mValues.iterator();
                    while (it2.hasNext()) {
                        writer.value((String) it2.next());
                    }
                    writer.endArray();
                }
                if (!detail.isStatusAllDef()) {
                    writer.name(FILTER_STATUS_NAME);
                    writer.beginArray();
                    Iterator it3 = detail.mStatuses.iterator();
                    while (it3.hasNext()) {
                        writer.value((long) ((Integer) it3.next()).intValue());
                    }
                    writer.endArray();
                }
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.flush();
        }

        private static void parseFilterList(Reader rd, FilterList filterList, boolean versionOnly) throws IOException {
            JsonReader reader = new JsonReader(rd);
            reader.beginObject();
            if (COMMENT.equals(reader.nextName())) {
                if (JsonToken.NULL == reader.peek()) {
                    reader.skipValue();
                } else {
                    filterList.mComment = reader.nextString();
                }
                if ("version".equals(reader.nextName())) {
                    filterList.mVersion = reader.nextLong();
                    if (!versionOnly) {
                        while (reader.hasNext()) {
                            if (FILTER_CONTENT.equals(reader.nextName())) {
                                ArrayMap<String, FilterList.FilterDetail> data = filterList.mData;
                                reader.beginArray();
                                while (reader.hasNext()) {
                                    reader.beginObject();
                                    if ("name".equals(reader.nextName())) {
                                        String filterName = reader.nextString();
                                        if (!data.containsKey(filterName)) {
                                            ArrayList<String> tagList = new ArrayList<>();
                                            ArrayList<String> valueList = new ArrayList<>();
                                            ArrayList<Integer> statusList = new ArrayList<>();
                                            if (FILTER_TAG_NAME.equals(reader.nextName())) {
                                                reader.beginArray();
                                                while (reader.hasNext()) {
                                                    tagList.add(reader.nextString());
                                                }
                                                reader.endArray();
                                                while (reader.peek() == JsonToken.NAME) {
                                                    String jsonName = reader.nextName();
                                                    char c = 65535;
                                                    int hashCode = jsonName.hashCode();
                                                    if (hashCode != -892481550) {
                                                        if (hashCode == 111972721 && jsonName.equals(FILTER_VALUE_NAME)) {
                                                            c = 0;
                                                        }
                                                    } else if (jsonName.equals(FILTER_STATUS_NAME)) {
                                                        c = 1;
                                                    }
                                                    if (c == 0) {
                                                        reader.beginArray();
                                                        while (reader.hasNext()) {
                                                            if (JsonToken.NULL == reader.peek()) {
                                                                reader.nextNull();
                                                                valueList.add(null);
                                                            } else {
                                                                valueList.add(reader.nextString());
                                                            }
                                                        }
                                                        reader.endArray();
                                                    } else if (c == 1) {
                                                        reader.beginArray();
                                                        while (reader.hasNext()) {
                                                            statusList.add(Integer.valueOf(reader.nextInt()));
                                                        }
                                                        reader.endArray();
                                                    } else {
                                                        throw new IOException("value or status is expected");
                                                    }
                                                }
                                                if (valueList.size() == 0) {
                                                    for (int i = 0; i < tagList.size(); i++) {
                                                        valueList.add(null);
                                                    }
                                                }
                                                if (statusList.size() == 0) {
                                                    for (int i2 = 0; i2 < tagList.size(); i2++) {
                                                        statusList.add(1);
                                                    }
                                                }
                                                if (tagList.size() == statusList.size() && tagList.size() == valueList.size()) {
                                                    reader.endObject();
                                                    data.put(filterName, new FilterList.FilterDetail(tagList, valueList, statusList));
                                                } else {
                                                    throw new IOException("Filter size isn't equal in " + filterName);
                                                }
                                            } else {
                                                throw new IOException("filter is expected");
                                            }
                                        } else {
                                            throw new IOException("name is duplicate");
                                        }
                                    } else {
                                        throw new IOException("name is expected");
                                    }
                                }
                                reader.endArray();
                            } else {
                                throw new IOException("filter_content is expected");
                            }
                        }
                        reader.endObject();
                        return;
                    }
                    return;
                }
                throw new IOException("version is expected");
            }
            throw new IOException("comment is expected");
        }
    }
}
