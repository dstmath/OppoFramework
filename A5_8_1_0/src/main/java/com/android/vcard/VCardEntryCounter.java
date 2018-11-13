package com.android.vcard;

public class VCardEntryCounter implements VCardInterpreter {
    private int mCount;

    public int getCount() {
        return this.mCount;
    }

    public void onVCardStarted() {
    }

    public void onVCardEnded() {
    }

    public void onEntryStarted() {
    }

    public void onEntryEnded() {
        this.mCount++;
    }

    public void onPropertyCreated(VCardProperty property) {
    }
}
