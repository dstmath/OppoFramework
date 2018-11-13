package com.android.i18n.phonenumbers;

import java.util.HashSet;
import java.util.Set;
import javax.sip.message.Response;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class AlternateFormatsCountryCodeSet {
    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.AlternateFormatsCountryCodeSet.<init>():void, dex: 
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
    public AlternateFormatsCountryCodeSet() {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.i18n.phonenumbers.AlternateFormatsCountryCodeSet.<init>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.i18n.phonenumbers.AlternateFormatsCountryCodeSet.<init>():void");
    }

    static Set<Integer> getCountryCodeSet() {
        Set<Integer> countryCodeSet = new HashSet(57);
        countryCodeSet.add(Integer.valueOf(7));
        countryCodeSet.add(Integer.valueOf(27));
        countryCodeSet.add(Integer.valueOf(30));
        countryCodeSet.add(Integer.valueOf(31));
        countryCodeSet.add(Integer.valueOf(34));
        countryCodeSet.add(Integer.valueOf(36));
        countryCodeSet.add(Integer.valueOf(43));
        countryCodeSet.add(Integer.valueOf(44));
        countryCodeSet.add(Integer.valueOf(49));
        countryCodeSet.add(Integer.valueOf(54));
        countryCodeSet.add(Integer.valueOf(55));
        countryCodeSet.add(Integer.valueOf(58));
        countryCodeSet.add(Integer.valueOf(61));
        countryCodeSet.add(Integer.valueOf(62));
        countryCodeSet.add(Integer.valueOf(63));
        countryCodeSet.add(Integer.valueOf(66));
        countryCodeSet.add(Integer.valueOf(81));
        countryCodeSet.add(Integer.valueOf(84));
        countryCodeSet.add(Integer.valueOf(90));
        countryCodeSet.add(Integer.valueOf(91));
        countryCodeSet.add(Integer.valueOf(94));
        countryCodeSet.add(Integer.valueOf(95));
        countryCodeSet.add(Integer.valueOf(255));
        countryCodeSet.add(Integer.valueOf(350));
        countryCodeSet.add(Integer.valueOf(351));
        countryCodeSet.add(Integer.valueOf(352));
        countryCodeSet.add(Integer.valueOf(358));
        countryCodeSet.add(Integer.valueOf(359));
        countryCodeSet.add(Integer.valueOf(372));
        countryCodeSet.add(Integer.valueOf(373));
        countryCodeSet.add(Integer.valueOf(Response.ALTERNATIVE_SERVICE));
        countryCodeSet.add(Integer.valueOf(381));
        countryCodeSet.add(Integer.valueOf(385));
        countryCodeSet.add(Integer.valueOf(Response.VERSION_NOT_SUPPORTED));
        countryCodeSet.add(Integer.valueOf(506));
        countryCodeSet.add(Integer.valueOf(595));
        countryCodeSet.add(Integer.valueOf(675));
        countryCodeSet.add(Integer.valueOf(676));
        countryCodeSet.add(Integer.valueOf(679));
        countryCodeSet.add(Integer.valueOf(855));
        countryCodeSet.add(Integer.valueOf(971));
        countryCodeSet.add(Integer.valueOf(972));
        countryCodeSet.add(Integer.valueOf(995));
        return countryCodeSet;
    }
}
