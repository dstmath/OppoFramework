package com.android.i18n.phonenumbers.prefixmapper;

import com.android.i18n.phonenumbers.Phonenumber.PhoneNumber;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class PrefixFileReader {
    private static final Logger LOGGER = null;
    private Map<String, PhonePrefixMap> availablePhonePrefixMaps;
    private MappingFileProvider mappingFileProvider;
    private final String phonePrefixDataDirectory;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
        	at jadx.core.ProcessClass.process(ProcessClass.java:29)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
        	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
        	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
        	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
        	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.prefixmapper.PrefixFileReader.<clinit>():void");
    }

    public PrefixFileReader(String phonePrefixDataDirectory) {
        this.mappingFileProvider = new MappingFileProvider();
        this.availablePhonePrefixMaps = new HashMap();
        this.phonePrefixDataDirectory = phonePrefixDataDirectory;
        loadMappingFileProvider();
    }

    private void loadMappingFileProvider() {
        IOException e;
        Throwable th;
        InputStream source = PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + "config");
        ObjectInputStream in = null;
        int retryCount = 0;
        while (true) {
            ObjectInputStream in2 = in;
            if (retryCount < 100) {
                try {
                    in = new ObjectInputStream(source);
                    try {
                        this.mappingFileProvider.readExternal(in);
                        LOGGER.log(Level.WARNING, "[DBG]loadMappingFileProvider success!");
                        close(in);
                        return;
                    } catch (IOException e2) {
                        e = e2;
                    }
                } catch (IOException e3) {
                    e = e3;
                    in = in2;
                    retryCount++;
                    try {
                        close(in);
                        LOGGER.log(Level.WARNING, e.toString());
                        close(in);
                    } catch (Throwable th2) {
                        th = th2;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    in = in2;
                }
            } else {
                LOGGER.log(Level.WARNING, "[DBG]loadMappingFileProvider start retry count: " + retryCount);
                return;
            }
            close(in);
        }
        close(in);
        throw th;
    }

    private PhonePrefixMap getPhonePrefixDescriptions(int prefixMapKey, String language, String script, String region) {
        String fileName = this.mappingFileProvider.getFileName(prefixMapKey, language, script, region);
        if (fileName.length() == 0) {
            return null;
        }
        if (!this.availablePhonePrefixMaps.containsKey(fileName)) {
            loadPhonePrefixMapFromFile(fileName);
        }
        return (PhonePrefixMap) this.availablePhonePrefixMaps.get(fileName);
    }

    private void loadPhonePrefixMapFromFile(String fileName) {
        IOException e;
        Throwable th;
        InputStream in = null;
        try {
            InputStream in2 = new ObjectInputStream(PrefixFileReader.class.getResourceAsStream(this.phonePrefixDataDirectory + fileName));
            try {
                PhonePrefixMap map = new PhonePrefixMap();
                map.readExternal(in2);
                this.availablePhonePrefixMaps.put(fileName, map);
                close(in2);
                in = in2;
            } catch (IOException e2) {
                e = e2;
                in = in2;
                try {
                    LOGGER.log(Level.WARNING, e.toString());
                    close(in);
                } catch (Throwable th2) {
                    th = th2;
                    close(in);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                in = in2;
                close(in);
                throw th;
            }
        } catch (IOException e3) {
            e = e3;
            LOGGER.log(Level.WARNING, e.toString());
            close(in);
        }
    }

    private static void close(InputStream in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.toString());
            }
        }
    }

    public String getDescriptionForNumber(PhoneNumber number, String lang, String script, String region) {
        int phonePrefix;
        String description = null;
        int countryCallingCode = number.getCountryCode();
        if (countryCallingCode != 1) {
            phonePrefix = countryCallingCode;
        } else {
            phonePrefix = ((int) (number.getNationalNumber() / 10000000)) + 1000;
        }
        PhonePrefixMap phonePrefixDescriptions = getPhonePrefixDescriptions(phonePrefix, lang, script, region);
        if (phonePrefixDescriptions != null) {
            description = phonePrefixDescriptions.lookup(number);
        }
        if ((description == null || description.length() == 0) && mayFallBackToEnglish(lang)) {
            PhonePrefixMap defaultMap = getPhonePrefixDescriptions(phonePrefix, "en", "", "");
            if (defaultMap == null) {
                return "";
            }
            description = defaultMap.lookup(number);
        }
        if (description == null) {
            description = "";
        }
        return description;
    }

    private boolean mayFallBackToEnglish(String lang) {
        return (lang.equals("zh") || lang.equals("ja") || lang.equals("ko")) ? false : true;
    }
}
