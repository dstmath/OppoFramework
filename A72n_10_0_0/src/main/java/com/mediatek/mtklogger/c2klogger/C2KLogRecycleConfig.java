package com.mediatek.mtklogger.c2klogger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class C2KLogRecycleConfig {
    private List<String> mContents = new LinkedList();
    private boolean mIsChanged = false;
    private String mRecycleConfig = "";

    public C2KLogRecycleConfig(String recycleConfig) {
        this.mRecycleConfig = recycleConfig;
        this.mIsChanged = true;
        readContents();
    }

    public void addLogpathToLastLine(String logPath) {
        this.mIsChanged = true;
        synchronized (this.mContents) {
            this.mContents.add(logPath);
        }
    }

    public void removeLogpathFromFirstLine() {
        this.mIsChanged = true;
        synchronized (this.mContents) {
            if (this.mContents.size() > 0) {
                this.mContents.remove(0);
            }
        }
    }

    public String getLogpathFromFirstLine() {
        String str;
        synchronized (this.mContents) {
            str = this.mContents.size() > 0 ? this.mContents.get(0) : "";
        }
        return str;
    }

    public List<String> getContents() {
        List<String> list;
        synchronized (this.mContents) {
            list = this.mContents;
        }
        return list;
    }

    public void readContents() {
        synchronized (this.mContents) {
            try {
                if (this.mIsChanged) {
                    this.mIsChanged = false;
                    this.mContents.clear();
                    FileReader fr = new FileReader(new File(this.mRecycleConfig));
                    BufferedReader br = new BufferedReader(fr);
                    while (true) {
                        String line = br.readLine();
                        if (line == null) {
                            break;
                        }
                        this.mContents.add(line);
                    }
                    br.close();
                    fr.close();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    public void writeContents() {
        synchronized (this.mContents) {
            if (this.mIsChanged) {
                this.mIsChanged = false;
                File recycleConfigFile = new File(this.mRecycleConfig);
                if (recycleConfigFile.exists()) {
                    recycleConfigFile.delete();
                }
                try {
                    FileWriter fw = new FileWriter(recycleConfigFile);
                    BufferedWriter bw = new BufferedWriter(fw);
                    Iterator<String> it = this.mContents.iterator();
                    while (it.hasNext()) {
                        bw.write(it.next() + "\r\n");
                    }
                    bw.close();
                    fw.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
