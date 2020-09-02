package com.android.internal.telephony;

public class AbstractIccPhoneBookInterfaceManager {
    protected IOppoIccPhoneBookInterfaceManager mReference;
    public boolean phonebookReady;

    public int oppoGetAdnEmailLen() {
        return this.mReference.oppoGetAdnEmailLen();
    }

    public int oppoGetSimPhonebookAllSpace() {
        return this.mReference.oppoGetSimPhonebookAllSpace();
    }

    public int oppoGetSimPhonebookUsedSpace() {
        return this.mReference.oppoGetSimPhonebookUsedSpace();
    }

    public int oppoGetSimPhonebookNameLength() {
        return this.mReference.oppoGetSimPhonebookNameLength();
    }

    public boolean isPhoneBookReady() {
        return this.mReference.isPhoneBookReady();
    }

    public void resetSimNameLength() {
        this.mReference.resetSimNameLength();
    }

    public int getRecordsSize() {
        return 0;
    }

    public boolean isEmptyRecords(int i) {
        return true;
    }

    public int getSlotId() {
        return -1;
    }

    public Phone getPhone() {
        return null;
    }

    public void oppoCheckThread() {
    }
}
