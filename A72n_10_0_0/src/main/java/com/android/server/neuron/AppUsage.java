package com.android.server.neuron;

import android.util.ArrayMap;
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
import java.util.Set;
import java.util.TreeSet;

public class AppUsage {
    private static final String APP_BLACK_STAT = "/data/system/neuron_system/app_black";
    private static final int APP_NUM = 30;
    private static final int APP_QUEUE_MAX = 300;
    private static final int APP_QUEUE_MIN = 200;
    private static final String APP_USAGE_STAT = "/data/system/neuron_system/app_usage";
    private static final String NEURON_DIR = "/data/system/neuron_system/";
    public static final String TAG = "NeuronSystem";
    private ArrayMap<Integer, List<String>> mUserAppHistory = new ArrayMap<>();
    private ArrayMap<Integer, HashMap<String, Integer>> mUserAppUsageMap;
    private ArrayMap<Integer, Set<String>> mUserHateApps = new ArrayMap<>();
    private ArrayMap<Integer, String[]> mUserPreferApps = new ArrayMap<>();

    public AppUsage() {
        loadAppUsage();
        this.mUserPreferApps = updateUserAppUsageMap();
    }

    public List<String> appPreloadPredict() {
        List<String> result = new ArrayList<>();
        String[] pkgs = this.mUserPreferApps.get(0);
        Set<String> hateAppSet = this.mUserHateApps.get(0);
        for (String app : pkgs) {
            if (!result.contains(app) && (hateAppSet == null || !hateAppSet.contains(app))) {
                result.add(app);
            }
        }
        return result;
    }

    public void enableRecommendedApps(boolean enable, List<String> pkgs) {
        Set<String> set = this.mUserHateApps.get(0);
        if (set == null) {
            set = new TreeSet();
            this.mUserHateApps.put(0, set);
        }
        if (enable) {
            for (String pkg : pkgs) {
                set.remove(pkg);
            }
            return;
        }
        set.addAll(pkgs);
    }

    public void onAppForeground(String pkg) {
        List<String> appHistory = this.mUserAppHistory.get(0);
        if (appHistory == null) {
            appHistory = new ArrayList();
        }
        if (pkg != null) {
            appHistory.add(pkg);
        }
        if (appHistory.size() > 300) {
            appHistory = appHistory.subList(100, appHistory.size());
        }
        this.mUserAppHistory.put(0, appHistory);
        if (appHistory.size() % 20 == 0) {
            this.mUserPreferApps = updateUserAppUsageMap();
            storeAppUsage();
        }
    }

    private void loadAppUsage() {
        File dir = new File(NEURON_DIR);
        if (!dir.exists()) {
            dir.mkdir();
        }
        char c = 1;
        char c2 = 0;
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(APP_USAGE_STAT));
            while (true) {
                String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                String[] strs = line.split("-");
                if (strs.length == 2) {
                    int user = Integer.parseInt(strs[c2]);
                    List<String> appHistory = this.mUserAppHistory.get(Integer.valueOf(user));
                    if (appHistory == null) {
                        appHistory = new ArrayList();
                    }
                    appHistory.add(strs[c]);
                    this.mUserAppHistory.put(Integer.valueOf(user), appHistory);
                }
                c = 1;
                c2 = 0;
            }
            bufferedReader.close();
        } catch (Exception e) {
            Log.e("NeuronSystem", "Exception while read file:", e);
            new File(APP_USAGE_STAT).delete();
            this.mUserAppHistory.clear();
        }
        try {
            BufferedReader bufferedReader2 = new BufferedReader(new FileReader(APP_BLACK_STAT));
            while (true) {
                String line2 = bufferedReader2.readLine();
                if (line2 != null) {
                    String[] strs2 = line2.split("-");
                    if (strs2.length == 2) {
                        int user2 = Integer.parseInt(strs2[0]);
                        Set<String> disableApps = this.mUserHateApps.get(Integer.valueOf(user2));
                        if (disableApps == null) {
                            disableApps = new TreeSet();
                        }
                        disableApps.add(strs2[1]);
                        this.mUserHateApps.put(Integer.valueOf(user2), disableApps);
                    }
                } else {
                    bufferedReader2.close();
                    return;
                }
            }
        } catch (Exception e2) {
            Log.e("NeuronSystem", "Exception while read file:", e2);
            new File(APP_BLACK_STAT).delete();
            this.mUserHateApps.clear();
        }
    }

    private void storeAppUsage() {
        try {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(APP_USAGE_STAT));
            for (int i = 0; i < this.mUserAppHistory.size(); i++) {
                int user = this.mUserAppHistory.keyAt(i).intValue();
                StringBuffer sb = new StringBuffer();
                for (String pkg : this.mUserAppHistory.valueAt(i)) {
                    sb.setLength(0);
                    sb.append(user);
                    sb.append("-");
                    sb.append(pkg);
                    bufferedWriter.write(sb.toString());
                    bufferedWriter.newLine();
                }
            }
            bufferedWriter.close();
        } catch (Exception e) {
            Log.e("NeuronSystem", "Exception while read file:", e);
            new File(APP_USAGE_STAT).delete();
        }
        try {
            BufferedWriter bufferedWriter2 = new BufferedWriter(new FileWriter(APP_BLACK_STAT));
            for (int i2 = 0; i2 < this.mUserHateApps.size(); i2++) {
                int user2 = this.mUserHateApps.keyAt(i2).intValue();
                StringBuffer sb2 = new StringBuffer();
                for (String pkg2 : this.mUserHateApps.valueAt(i2)) {
                    sb2.setLength(0);
                    sb2.append(user2);
                    sb2.append("-");
                    sb2.append(pkg2);
                    bufferedWriter2.write(sb2.toString());
                    bufferedWriter2.newLine();
                }
            }
            bufferedWriter2.close();
        } catch (Exception e2) {
            Log.e("NeuronSystem", "Exception while read file:", e2);
            new File(APP_BLACK_STAT).delete();
        }
    }

    private ArrayMap<Integer, String[]> updateUserAppUsageMap() {
        int c;
        ArrayMap<Integer, String[]> result = new ArrayMap<>();
        for (int i = 0; i < this.mUserAppHistory.size(); i++) {
            int user = this.mUserAppHistory.keyAt(i).intValue();
            HashMap<String, Integer> map = new HashMap<>();
            for (String pkg : this.mUserAppHistory.valueAt(i)) {
                if (!map.containsKey(pkg)) {
                    c = 1;
                } else {
                    c = map.get(pkg).intValue() + 1;
                }
                map.put(pkg, Integer.valueOf(c));
            }
            ArrayList<Entry> entrys = new ArrayList<>();
            for (Map.Entry<String, Integer> e : map.entrySet()) {
                entrys.add(new Entry(e.getKey(), e.getValue().intValue()));
            }
            Collections.sort(entrys);
            String[] preferApps = new String[entrys.size()];
            int index = 0;
            Iterator<Entry> it = entrys.iterator();
            while (it.hasNext()) {
                preferApps[index] = it.next().pkg;
                index++;
            }
            result.put(Integer.valueOf(user), preferApps);
        }
        return result;
    }

    /* access modifiers changed from: private */
    public class Entry implements Comparable<Entry> {
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
