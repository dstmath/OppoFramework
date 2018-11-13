package com.android.server.pm;

import android.os.Environment;
import android.os.FileUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

final class ResmonUidList {
    private final File mDataDir = new File(Environment.getDataDirectory(), "system");
    private final File mResmonUidListFile = new File(this.mDataDir, "resmon-uid.txt");

    ResmonUidList() {
    }

    void updateList(ArrayList<Integer> uidList) {
        try {
            FileWriter fw = new FileWriter(this.mResmonUidListFile);
            StringBuilder sb = new StringBuilder();
            if (!uidList.isEmpty() && uidList.size() > 0) {
                for (int i = 0; i < uidList.size(); i++) {
                    sb.append(String.valueOf(uidList.get(i)));
                    sb.append("\r\n");
                }
            }
            fw.write(sb.toString());
            fw.flush();
            fw.close();
            FileUtils.setPermissions(this.mResmonUidListFile.toString(), 436, -1, -1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
