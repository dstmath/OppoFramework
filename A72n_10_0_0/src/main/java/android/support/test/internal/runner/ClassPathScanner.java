package android.support.test.internal.runner;

import dalvik.system.DexFile;
import java.util.Enumeration;

public class ClassPathScanner {
    /* access modifiers changed from: package-private */
    public Enumeration<String> getDexEntries(DexFile dexFile) {
        return dexFile.entries();
    }
}
