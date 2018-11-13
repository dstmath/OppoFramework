package com.android.vcard;

public interface VCardInterpreter {
    void onEntryEnded();

    void onEntryStarted();

    void onPropertyCreated(VCardProperty vCardProperty);

    void onVCardEnded();

    void onVCardStarted();
}
