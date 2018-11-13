package com.android.server.pm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

final class ResmonWhitelistPackage {
    final ArrayList<String> mPackages = new ArrayList();
    private final File mSystemDir = new File("/system/", "etc");
    private final File mWhitelistFile = new File(this.mSystemDir, "resmonwhitelist.txt");

    ResmonWhitelistPackage() {
    }

    void readList() {
        if (this.mWhitelistFile.exists()) {
            try {
                this.mPackages.clear();
                BufferedReader br = new BufferedReader(new FileReader(this.mWhitelistFile));
                for (String line = br.readLine(); line != null; line = br.readLine()) {
                    this.mPackages.add(line);
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
