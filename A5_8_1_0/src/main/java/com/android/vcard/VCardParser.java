package com.android.vcard;

import com.android.vcard.exception.VCardException;
import java.io.IOException;
import java.io.InputStream;

public abstract class VCardParser {
    public abstract void addInterpreter(VCardInterpreter vCardInterpreter);

    public abstract void cancel();

    public abstract void parse(InputStream inputStream) throws IOException, VCardException;

    public abstract void parseOne(InputStream inputStream) throws IOException, VCardException;

    @Deprecated
    public void parse(InputStream is, VCardInterpreter interpreter) throws IOException, VCardException {
        addInterpreter(interpreter);
        parse(is);
    }
}
