package com.android.server.am;

import java.util.List;

/* access modifiers changed from: package-private */
/* compiled from: ColorHansImportance */
public class DynamicImportantAppList {
    private List<Integer> audioList;
    private List<String> navigationList;

    public void setNavigationList(List<String> navigationList2) {
        this.navigationList = navigationList2;
    }

    public void setAudioList(List<Integer> audioList2) {
        this.audioList = audioList2;
    }

    public List<String> getNavigationList() {
        return this.navigationList;
    }

    public List<Integer> getAudioList() {
        return this.audioList;
    }
}
