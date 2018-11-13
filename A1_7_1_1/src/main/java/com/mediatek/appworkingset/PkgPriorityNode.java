package com.mediatek.appworkingset;

import java.util.ArrayList;

class PkgPriorityNode {
    PkgPriorityNode next;
    String pkgName;
    PkgPriorityNode prev;
    ArrayList<ProcessRecordStore> procList;

    PkgPriorityNode() {
        this.procList = new ArrayList();
        this.pkgName = "N/A";
        this.next = null;
        this.prev = null;
    }

    PkgPriorityNode(String str, ProcessRecordStore processRecordStore) {
        this.procList = new ArrayList();
        this.pkgName = str;
        this.procList.add(processRecordStore);
    }

    String getPkgName() {
        return this.pkgName;
    }
}
