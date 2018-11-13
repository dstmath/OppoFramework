package com.android.i18n.phonenumbers;

import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadata;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneMetadataCollection;
import com.android.i18n.phonenumbers.Phonemetadata.PhoneNumberDesc;
import com.google.i18n.phonenumbers.Phonemetadata;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
final class MultiFileMetadataSourceImpl implements MetadataSource {
    private static final String META_DATA_FILE_PREFIX = "/com/android/i18n/phonenumbers/data/PhoneNumberMetadataProto";
    private static final Logger logger = null;
    private final Map<Integer, PhoneMetadata> countryCodeToNonGeographicalMetadataMap;
    private final String currentFilePrefix;
    private final MetadataLoader metadataLoader;
    private final Map<String, PhoneMetadata> regionToMetadataMap;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.MultiFileMetadataSourceImpl.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.i18n.phonenumbers.MultiFileMetadataSourceImpl.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.MultiFileMetadataSourceImpl.<clinit>():void");
    }

    public MultiFileMetadataSourceImpl(String currentFilePrefix, MetadataLoader metadataLoader) {
        this.regionToMetadataMap = Collections.synchronizedMap(new HashMap());
        this.countryCodeToNonGeographicalMetadataMap = Collections.synchronizedMap(new HashMap());
        this.currentFilePrefix = currentFilePrefix;
        this.metadataLoader = metadataLoader;
    }

    public MultiFileMetadataSourceImpl(MetadataLoader metadataLoader) {
        this(META_DATA_FILE_PREFIX, metadataLoader);
    }

    public PhoneMetadata getMetadataForRegion(String regionCode) {
        synchronized (this.regionToMetadataMap) {
            if (!this.regionToMetadataMap.containsKey(regionCode)) {
                loadMetadataFromFile(this.currentFilePrefix, regionCode, 0, this.metadataLoader);
            }
        }
        PhoneMetadata ret = (PhoneMetadata) this.regionToMetadataMap.get(regionCode);
        if (ret != null) {
            return ret;
        }
        ret = new PhoneMetadata();
        ret.setNationalPrefixForParsing("34");
        ret.setGeneralDesc(new PhoneNumberDesc().setNationalNumberPattern("\\d{4,8}"));
        return ret;
    }

    public PhoneMetadata getMetadataForNonGeographicalRegion(int countryCallingCode) {
        synchronized (this.countryCodeToNonGeographicalMetadataMap) {
            if (!this.countryCodeToNonGeographicalMetadataMap.containsKey(Integer.valueOf(countryCallingCode))) {
                loadMetadataFromFile(this.currentFilePrefix, PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY, countryCallingCode, this.metadataLoader);
            }
        }
        PhoneMetadata ret = (PhoneMetadata) this.countryCodeToNonGeographicalMetadataMap.get(Integer.valueOf(countryCallingCode));
        if (ret != null) {
            return ret;
        }
        ret = new PhoneMetadata();
        ret.setNationalPrefixForParsing("34");
        ret.setGeneralDesc(new PhoneNumberDesc().setNationalNumberPattern("\\d{4,8}"));
        return ret;
    }

    void loadMetadataFromFile(String filePrefix, String regionCode, int countryCallingCode, MetadataLoader metadataLoader) {
        String valueOf;
        boolean isNonGeoRegion = PhoneNumberUtil.REGION_CODE_FOR_NON_GEO_ENTITY.equals(regionCode);
        StringBuilder append = new StringBuilder().append(filePrefix).append("_");
        if (isNonGeoRegion) {
            valueOf = String.valueOf(countryCallingCode);
        } else {
            valueOf = regionCode;
        }
        String fileName = append.append(valueOf).toString();
        InputStream source = metadataLoader.loadMetadata(fileName);
        if (source == null) {
            logger.log(Level.SEVERE, "missing metadata: " + fileName);
            throw new IllegalStateException("missing metadata: " + fileName);
        }
        ObjectInputStream in = null;
        int retryCount = 0;
        while (true) {
            IOException e;
            ObjectInputStream in2 = in;
            if (retryCount < 100) {
                try {
                    in = new ObjectInputStream(source);
                    try {
                        List<Phonemetadata.PhoneMetadata> metadataList = loadMetadataAndCloseInput(in).getMetadataList();
                        if (metadataList.isEmpty()) {
                            logger.log(Level.SEVERE, "empty metadata: " + fileName);
                            throw new IllegalStateException("empty metadata: " + fileName);
                        }
                        if (metadataList.size() > 1) {
                            logger.log(Level.WARNING, "invalid metadata (too many entries): " + fileName);
                        }
                        PhoneMetadata metadata = (PhoneMetadata) metadataList.get(0);
                        if (isNonGeoRegion) {
                            this.countryCodeToNonGeographicalMetadataMap.put(Integer.valueOf(countryCallingCode), metadata);
                        } else {
                            this.regionToMetadataMap.put(regionCode, metadata);
                        }
                        return;
                    } catch (IOException e2) {
                        e = e2;
                    }
                } catch (IOException e3) {
                    e = e3;
                    in = in2;
                }
            } else {
                logger.log(Level.SEVERE, "[DBG] load fail, fileName: " + fileName + ", retryCount: " + retryCount);
                return;
            }
            logger.log(Level.SEVERE, "cannot load/parse metadata: " + fileName, e);
            retryCount++;
        }
    }

    private static PhoneMetadataCollection loadMetadataAndCloseInput(ObjectInputStream source) {
        PhoneMetadataCollection metadataCollection = new PhoneMetadataCollection();
        try {
            metadataCollection.readExternal(source);
            try {
                source.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "error closing input stream (ignored)", e);
            }
        } catch (IOException e2) {
            logger.log(Level.WARNING, "error reading input (ignored)", e2);
            try {
                source.close();
            } catch (IOException e22) {
                logger.log(Level.WARNING, "error closing input stream (ignored)", e22);
            }
        } catch (Throwable th) {
            try {
                source.close();
            } catch (IOException e222) {
                logger.log(Level.WARNING, "error closing input stream (ignored)", e222);
            }
            throw th;
        }
        return metadataCollection;
    }
}
