package androidx.test.internal.runner;

import dalvik.system.DexFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ClassPathScanner {
    private final Set<String> classPath = new HashSet();

    public interface ClassNameFilter {
        boolean accept(String str);
    }

    public static class ChainedClassNameFilter implements ClassNameFilter {
        private final List<ClassNameFilter> filters = new ArrayList();

        public void add(ClassNameFilter filter) {
            this.filters.add(filter);
        }

        @Override // androidx.test.internal.runner.ClassPathScanner.ClassNameFilter
        public boolean accept(String className) {
            for (ClassNameFilter filter : this.filters) {
                if (!filter.accept(className)) {
                    return false;
                }
            }
            return true;
        }
    }

    public static class ExternalClassNameFilter implements ClassNameFilter {
        @Override // androidx.test.internal.runner.ClassPathScanner.ClassNameFilter
        public boolean accept(String pathName) {
            return !pathName.contains("$");
        }
    }

    public static class InclusivePackageNamesFilter implements ClassNameFilter {
        private final Collection<String> pkgNames;

        InclusivePackageNamesFilter(Collection<String> pkgNames2) {
            this.pkgNames = new ArrayList(pkgNames2.size());
            for (String packageName : pkgNames2) {
                if (!packageName.endsWith(".")) {
                    this.pkgNames.add(String.format("%s.", packageName));
                } else {
                    this.pkgNames.add(packageName);
                }
            }
        }

        @Override // androidx.test.internal.runner.ClassPathScanner.ClassNameFilter
        public boolean accept(String pathName) {
            for (String packageName : this.pkgNames) {
                if (pathName.startsWith(packageName)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class ExcludePackageNameFilter implements ClassNameFilter {
        private final String pkgName;

        ExcludePackageNameFilter(String pkgName2) {
            if (!pkgName2.endsWith(".")) {
                this.pkgName = String.format("%s.", pkgName2);
                return;
            }
            this.pkgName = pkgName2;
        }

        @Override // androidx.test.internal.runner.ClassPathScanner.ClassNameFilter
        public boolean accept(String pathName) {
            return !pathName.startsWith(this.pkgName);
        }
    }

    static class ExcludeClassNamesFilter implements ClassNameFilter {
        private Set<String> excludedClassNames;

        public ExcludeClassNamesFilter(Set<String> excludedClassNames2) {
            this.excludedClassNames = excludedClassNames2;
        }

        @Override // androidx.test.internal.runner.ClassPathScanner.ClassNameFilter
        public boolean accept(String className) {
            return !this.excludedClassNames.contains(className);
        }
    }

    public ClassPathScanner(Collection<String> paths) {
        this.classPath.addAll(paths);
    }

    private void addEntriesFromPath(Set<String> entryNames, String path, ClassNameFilter filter) throws IOException {
        DexFile dexFile = null;
        try {
            DexFile dexFile2 = new DexFile(path);
            Enumeration<String> classNames = getDexEntries(dexFile2);
            while (classNames.hasMoreElements()) {
                String className = classNames.nextElement();
                if (filter.accept(className)) {
                    entryNames.add(className);
                }
            }
            dexFile2.close();
        } catch (Throwable th) {
            if (0 != 0) {
                dexFile.close();
            }
            throw th;
        }
    }

    /* access modifiers changed from: package-private */
    public Enumeration<String> getDexEntries(DexFile dexFile) {
        return dexFile.entries();
    }

    public Set<String> getClassPathEntries(ClassNameFilter filter) throws IOException {
        Set<String> entryNames = new LinkedHashSet<>();
        for (String path : this.classPath) {
            addEntriesFromPath(entryNames, path, filter);
        }
        return entryNames;
    }
}
