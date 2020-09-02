package com.android.server.neuron;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AppUsage {
    private static final int APP_NUM = 30;
    private static final int APP_QUEUE_MAX = 300;
    private static final int APP_QUEUE_MIN = 200;
    private static final String APP_USAGE_STAT = "/data/system/neuron_system/app_usage";
    public static final String TAG = "NeuronSystem";
    private List<String> mAppHistory;
    private HashMap<String, Integer> mAppUsageMap;
    private String[] mApps = null;

    public AppUsage() {
        loadAppUsage();
        this.mApps = updateAppUsageMap();
    }

    public String[] appPreloadPredict() {
        return this.mApps;
    }

    public void onAppForeground(String pkg) {
        if (pkg != null) {
            this.mAppHistory.add(pkg);
        }
        if (this.mAppHistory.size() > 300) {
            List<String> list = this.mAppHistory;
            this.mAppHistory = list.subList(100, list.size());
        }
        if (this.mAppHistory.size() % 20 == 0) {
            this.mApps = updateAppUsageMap();
            storeAppUsage();
        }
    }

    private void loadAppUsage() {
        this.mAppHistory = new ArrayList();
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(APP_USAGE_STAT));
            while (true) {
                String line = bufferedReader.readLine();
                if (line != null) {
                    this.mAppHistory.add(line);
                } else {
                    bufferedReader.close();
                    return;
                }
            }
        } catch (Exception e) {
            Log.e("NeuronSystem", "Exception while read file:", e);
            new File(APP_USAGE_STAT).delete();
            this.mAppHistory.clear();
        }
    }

    private void storeAppUsage() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(APP_USAGE_STAT));
            for (String line : this.mAppHistory) {
                bufferedWriter.write(line);
                bufferedWriter.newLine();
            }
            bufferedWriter.close();
        } catch (Exception e) {
            Log.e("NeuronSystem", "Exception while read file:", e);
            new File(APP_USAGE_STAT).delete();
        }
    }

    private String[] updateAppUsageMap() {
        int c;
        this.mAppUsageMap = new HashMap<>();
        for (String line : this.mAppHistory) {
            if (!this.mAppUsageMap.containsKey(line)) {
                c = 1;
            } else {
                c = this.mAppUsageMap.get(line).intValue() + 1;
            }
            this.mAppUsageMap.put(line, Integer.valueOf(c));
        }
        ArrayList<Entry> entrys = new ArrayList<>();
        for (Map.Entry<String, Integer> e : this.mAppUsageMap.entrySet()) {
            entrys.add(new Entry(e.getKey(), e.getValue().intValue()));
        }
        Collections.sort(entrys);
        String[] result = new String[entrys.size()];
        int i = 0;
        Iterator<Entry> it = entrys.iterator();
        while (it.hasNext()) {
            result[i] = it.next().pkg;
            i++;
        }
        return result;
    }

    private class Entry implements Comparable<Entry> {
        int count;
        String pkg;

        public Entry(String pkg2, int c) {
            this.pkg = pkg2;
            this.count = c;
        }

        public int compareTo(Entry o) {
            return o.count - this.count;
        }
    }
}
