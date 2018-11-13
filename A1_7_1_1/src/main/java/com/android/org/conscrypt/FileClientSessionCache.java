package com.android.org.conscrypt;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.net.ssl.SSLSession;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class FileClientSessionCache {
    public static final int MAX_SIZE = 12;
    static final Map<File, Impl> caches = null;

    static class CacheFile extends File {
        long lastModified;
        final String name;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.<init>(java.io.File, java.lang.String):void, dex:  in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.<init>(java.io.File, java.lang.String):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.<init>(java.io.File, java.lang.String):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:73)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        CacheFile(java.io.File r1, java.lang.String r2) {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.<init>(java.io.File, java.lang.String):void, dex:  in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.<init>(java.io.File, java.lang.String):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.FileClientSessionCache.CacheFile.<init>(java.io.File, java.lang.String):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.compareTo(java.io.File):int, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public int compareTo(java.io.File r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.compareTo(java.io.File):int, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.FileClientSessionCache.CacheFile.compareTo(java.io.File):int");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.lastModified():long, dex:  in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.lastModified():long, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.lastModified():long, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public long lastModified() {
            /*
            // Can't load method instructions: Load method exception: null in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.lastModified():long, dex:  in method: com.android.org.conscrypt.FileClientSessionCache.CacheFile.lastModified():long, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.FileClientSessionCache.CacheFile.lastModified():long");
        }
    }

    static class Impl implements SSLClientSessionCache {
        Map<String, File> accessOrder;
        final File directory;
        String[] initialFiles;
        int size;

        Impl(File directory) throws IOException {
            this.accessOrder = newAccessOrder();
            boolean exists = directory.exists();
            if (!exists || directory.isDirectory()) {
                if (exists) {
                    this.initialFiles = directory.list();
                    if (this.initialFiles == null) {
                        throw new IOException(directory + " exists but cannot list contents.");
                    }
                    Arrays.sort(this.initialFiles);
                    this.size = this.initialFiles.length;
                } else if (directory.mkdirs()) {
                    this.size = 0;
                } else {
                    throw new IOException("Creation of " + directory + " directory failed.");
                }
                this.directory = directory;
                return;
            }
            throw new IOException(directory + " exists but is not a directory.");
        }

        private static Map<String, File> newAccessOrder() {
            return new LinkedHashMap(12, 0.75f, true);
        }

        private static String fileName(String host, int port) {
            if (host != null) {
                return host + "." + port;
            }
            throw new NullPointerException("host == null");
        }

        public synchronized byte[] getSessionData(String host, int port) {
            byte[] data;
            String name = fileName(host, port);
            File file = (File) this.accessOrder.get(name);
            if (file == null) {
                if (this.initialFiles == null) {
                    return null;
                }
                if (Arrays.binarySearch(this.initialFiles, name) < 0) {
                    return null;
                }
                file = new File(this.directory, name);
                this.accessOrder.put(name, file);
            }
            try {
                FileInputStream in = new FileInputStream(file);
                try {
                    data = new byte[((int) file.length())];
                    new DataInputStream(in).readFully(data);
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    logReadError(host, file, e);
                    if (in != null) {
                        in.close();
                    }
                    return null;
                } catch (Throwable th) {
                    if (in != null) {
                        in.close();
                    }
                }
            } catch (RuntimeException rethrown) {
                throw rethrown;
            } catch (Exception e2) {
            } catch (RuntimeException rethrown2) {
                throw rethrown2;
            } catch (Exception e3) {
            } catch (RuntimeException rethrown22) {
                throw rethrown22;
            } catch (Exception e4) {
            } catch (FileNotFoundException e5) {
                logReadError(host, file, e5);
                return null;
            }
            return data;
        }

        static void logReadError(String host, File file, Throwable t) {
            System.err.println("FileClientSessionCache: Error reading session data for " + host + " from " + file + ".");
            t.printStackTrace();
        }

        public synchronized void putSessionData(SSLSession session, byte[] sessionData) {
            String host = session.getPeerHost();
            if (sessionData == null) {
                throw new NullPointerException("sessionData == null");
            }
            String name = fileName(host, session.getPeerPort());
            File file = new File(this.directory, name);
            boolean existedBefore = file.exists();
            try {
                FileOutputStream out = new FileOutputStream(file);
                if (!existedBefore) {
                    this.size++;
                    makeRoom();
                }
                boolean writeSuccessful = false;
                try {
                    out.write(sessionData);
                    writeSuccessful = true;
                    try {
                        out.close();
                        if (writeSuccessful && true) {
                            this.accessOrder.put(name, file);
                        } else {
                            delete(file);
                        }
                    } catch (IOException e) {
                        logWriteError(host, file, e);
                        if (!writeSuccessful || null == null) {
                            delete(file);
                        } else {
                            this.accessOrder.put(name, file);
                        }
                    } catch (Throwable th) {
                        if (!writeSuccessful || null == null) {
                            delete(file);
                        } else {
                            this.accessOrder.put(name, file);
                        }
                    }
                } catch (IOException e2) {
                    logWriteError(host, file, e2);
                    try {
                        out.close();
                        if (writeSuccessful && true) {
                            this.accessOrder.put(name, file);
                        } else {
                            delete(file);
                        }
                    } catch (IOException e22) {
                        logWriteError(host, file, e22);
                        if (!writeSuccessful || null == null) {
                            delete(file);
                        } else {
                            this.accessOrder.put(name, file);
                        }
                    } catch (Throwable th2) {
                        if (!writeSuccessful || null == null) {
                            delete(file);
                        } else {
                            this.accessOrder.put(name, file);
                        }
                    }
                } finally {
                    try {
                        out.close();
                        if (writeSuccessful && true) {
                            this.accessOrder.put(name, file);
                        } else {
                            delete(file);
                        }
                    } catch (IOException e222) {
                        logWriteError(host, file, e222);
                        if (!writeSuccessful || null == null) {
                            delete(file);
                        } else {
                            this.accessOrder.put(name, file);
                        }
                    } catch (Throwable th3) {
                        if (!writeSuccessful || null == null) {
                            delete(file);
                        } else {
                            this.accessOrder.put(name, file);
                        }
                    }
                }
            } catch (FileNotFoundException e3) {
                logWriteError(host, file, e3);
            }
        }

        private void makeRoom() {
            if (this.size > 12) {
                indexFiles();
                int removals = this.size - 12;
                Iterator<File> i = this.accessOrder.values().iterator();
                do {
                    delete((File) i.next());
                    i.remove();
                    removals--;
                } while (removals > 0);
            }
        }

        private void indexFiles() {
            String[] initialFiles = this.initialFiles;
            if (initialFiles != null) {
                this.initialFiles = null;
                Set<org.conscrypt.FileClientSessionCache.CacheFile> diskOnly = new TreeSet();
                for (String name : initialFiles) {
                    if (!this.accessOrder.containsKey(name)) {
                        diskOnly.add(new CacheFile(this.directory, name));
                    }
                }
                if (!diskOnly.isEmpty()) {
                    Map<String, File> newOrder = newAccessOrder();
                    Iterator cacheFile$iterator = diskOnly.iterator();
                    while (cacheFile$iterator.hasNext()) {
                        CacheFile cacheFile = (CacheFile) cacheFile$iterator.next();
                        newOrder.put(cacheFile.name, cacheFile);
                    }
                    newOrder.putAll(this.accessOrder);
                    this.accessOrder = newOrder;
                }
            }
        }

        private void delete(File file) {
            if (!file.delete()) {
                new IOException("FileClientSessionCache: Failed to delete " + file + ".").printStackTrace();
            }
            this.size--;
        }

        static void logWriteError(String host, File file, Throwable t) {
            System.err.println("FileClientSessionCache: Error writing session data for " + host + " to " + file + ".");
            t.printStackTrace();
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.FileClientSessionCache.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.org.conscrypt.FileClientSessionCache.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.org.conscrypt.FileClientSessionCache.<clinit>():void");
    }

    private FileClientSessionCache() {
    }

    public static synchronized SSLClientSessionCache usingDirectory(File directory) throws IOException {
        Impl cache;
        synchronized (FileClientSessionCache.class) {
            cache = (Impl) caches.get(directory);
            if (cache == null) {
                cache = new Impl(directory);
                caches.put(directory, cache);
            }
        }
        return cache;
    }

    static synchronized void reset() {
        synchronized (FileClientSessionCache.class) {
            caches.clear();
        }
    }
}
