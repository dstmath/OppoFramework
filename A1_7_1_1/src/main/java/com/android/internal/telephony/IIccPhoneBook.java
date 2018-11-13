package com.android.internal.telephony;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import com.android.internal.telephony.uicc.AdnRecord;
import com.mediatek.internal.telephony.uicc.AlphaTag;
import com.mediatek.internal.telephony.uicc.UsimGroup;
import com.mediatek.internal.telephony.uicc.UsimPBMemInfo;
import java.util.List;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
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
public interface IIccPhoneBook extends IInterface {

    public static abstract class Stub extends Binder implements IIccPhoneBook {
        private static final String DESCRIPTOR = "com.android.internal.telephony.IIccPhoneBook";
        static final int TRANSACTION_addContactToGroup = 21;
        static final int TRANSACTION_colorAddAdnRecordsInEfBySearchExUsingSubId = 45;
        static final int TRANSACTION_colorGetAdnEmailLenUsingSubId = 49;
        static final int TRANSACTION_colorGetSimNameLenUsingSubId = 44;
        static final int TRANSACTION_colorGetSimPhonebookAllSpace = 50;
        static final int TRANSACTION_colorGetSimPhonebookUsedSpace = 51;
        static final int TRANSACTION_colorISPhoneBookReady = 47;
        static final int TRANSACTION_colorIsPhoneBookPbrExist = 48;
        static final int TRANSACTION_colorUpdateAdnRecordsInEfByIndexExUsingSubId = 46;
        static final int TRANSACTION_getAdnRecordsCapacity = 42;
        static final int TRANSACTION_getAdnRecordsCapacityForSubscriber = 43;
        static final int TRANSACTION_getAdnRecordsInEf = 1;
        static final int TRANSACTION_getAdnRecordsInEfForSubscriber = 2;
        static final int TRANSACTION_getAdnRecordsSize = 13;
        static final int TRANSACTION_getAdnRecordsSizeForSubscriber = 14;
        static final int TRANSACTION_getAnrCount = 31;
        static final int TRANSACTION_getEmailCount = 32;
        static final int TRANSACTION_getPhonebookMemStorageExt = 40;
        static final int TRANSACTION_getSneRecordLen = 38;
        static final int TRANSACTION_getUpbDone = 41;
        static final int TRANSACTION_getUsimAasById = 29;
        static final int TRANSACTION_getUsimAasList = 28;
        static final int TRANSACTION_getUsimAasMaxCount = 33;
        static final int TRANSACTION_getUsimAasMaxNameLen = 34;
        static final int TRANSACTION_getUsimGroupById = 17;
        static final int TRANSACTION_getUsimGroups = 16;
        static final int TRANSACTION_getUsimGrpMaxCount = 27;
        static final int TRANSACTION_getUsimGrpMaxNameLen = 26;
        static final int TRANSACTION_hasExistGroup = 25;
        static final int TRANSACTION_hasSne = 37;
        static final int TRANSACTION_insertUsimAas = 30;
        static final int TRANSACTION_insertUsimGroup = 19;
        static final int TRANSACTION_isAdnAccessible = 39;
        static final int TRANSACTION_isPhbReady = 15;
        static final int TRANSACTION_moveContactFromGroupsToGroups = 24;
        static final int TRANSACTION_removeContactFromGroup = 22;
        static final int TRANSACTION_removeUsimAasById = 36;
        static final int TRANSACTION_removeUsimGroupById = 18;
        static final int TRANSACTION_updateAdnRecordsInEfByIndex = 7;
        static final int TRANSACTION_updateAdnRecordsInEfByIndexForSubscriber = 8;
        static final int TRANSACTION_updateAdnRecordsInEfByIndexWithError = 9;
        static final int TRANSACTION_updateAdnRecordsInEfBySearch = 3;
        static final int TRANSACTION_updateAdnRecordsInEfBySearchForSubscriber = 4;
        static final int TRANSACTION_updateAdnRecordsInEfBySearchWithError = 5;
        static final int TRANSACTION_updateContactToGroups = 23;
        static final int TRANSACTION_updateUsimAas = 35;
        static final int TRANSACTION_updateUsimGroup = 20;
        static final int TRANSACTION_updateUsimPBRecordsByIndexWithError = 11;
        static final int TRANSACTION_updateUsimPBRecordsBySearchWithError = 12;
        static final int TRANSACTION_updateUsimPBRecordsInEfByIndexWithError = 10;
        static final int TRANSACTION_updateUsimPBRecordsInEfBySearchWithError = 6;

        private static class Proxy implements IIccPhoneBook {
            private IBinder mRemote;

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            Proxy(android.os.IBinder r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.<init>(android.os.IBinder):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.<init>(android.os.IBinder):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.addContactToGroup(int, int, int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean addContactToGroup(int r1, int r2, int r3) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.addContactToGroup(int, int, int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.addContactToGroup(int, int, int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.asBinder():android.os.IBinder, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
                	at java.lang.Iterable.forEach(Iterable.java:75)
                	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
                	at jadx.core.ProcessClass.process(ProcessClass.java:37)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 11 more
                */
            public android.os.IBinder asBinder() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.asBinder():android.os.IBinder, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.asBinder():android.os.IBinder");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorAddAdnRecordsInEfBySearchExUsingSubId(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int colorAddAdnRecordsInEfBySearchExUsingSubId(int r1, int r2, java.lang.String r3, java.lang.String r4, java.lang.String r5, java.lang.String r6, java.lang.String r7, java.lang.String r8, java.lang.String r9) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorAddAdnRecordsInEfBySearchExUsingSubId(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorAddAdnRecordsInEfBySearchExUsingSubId(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetAdnEmailLenUsingSubId(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int colorGetAdnEmailLenUsingSubId(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetAdnEmailLenUsingSubId(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetAdnEmailLenUsingSubId(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimNameLenUsingSubId(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int colorGetSimNameLenUsingSubId(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimNameLenUsingSubId(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimNameLenUsingSubId(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimPhonebookAllSpace(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int colorGetSimPhonebookAllSpace(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimPhonebookAllSpace(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimPhonebookAllSpace(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimPhonebookUsedSpace(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int colorGetSimPhonebookUsedSpace(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimPhonebookUsedSpace(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorGetSimPhonebookUsedSpace(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorISPhoneBookReady(int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean colorISPhoneBookReady(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorISPhoneBookReady(int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorISPhoneBookReady(int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorIsPhoneBookPbrExist(int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean colorIsPhoneBookPbrExist(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorIsPhoneBookPbrExist(int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorIsPhoneBookPbrExist(int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorUpdateAdnRecordsInEfByIndexExUsingSubId(int, int, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId(int r1, int r2, java.lang.String r3, java.lang.String r4, java.lang.String r5, int r6, java.lang.String r7, java.lang.String r8) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorUpdateAdnRecordsInEfByIndexExUsingSubId(int, int, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.colorUpdateAdnRecordsInEfByIndexExUsingSubId(int, int, java.lang.String, java.lang.String, java.lang.String, int, java.lang.String, java.lang.String):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsCapacity():int[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int[] getAdnRecordsCapacity() throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsCapacity():int[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsCapacity():int[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsCapacityForSubscriber(int):int[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int[] getAdnRecordsCapacityForSubscriber(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsCapacityForSubscriber(int):int[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsCapacityForSubscriber(int):int[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsInEf(int):java.util.List<com.android.internal.telephony.uicc.AdnRecord>, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public java.util.List<com.android.internal.telephony.uicc.AdnRecord> getAdnRecordsInEf(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsInEf(int):java.util.List<com.android.internal.telephony.uicc.AdnRecord>, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsInEf(int):java.util.List<com.android.internal.telephony.uicc.AdnRecord>");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsInEfForSubscriber(int, int):java.util.List<com.android.internal.telephony.uicc.AdnRecord>, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public java.util.List<com.android.internal.telephony.uicc.AdnRecord> getAdnRecordsInEfForSubscriber(int r1, int r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsInEfForSubscriber(int, int):java.util.List<com.android.internal.telephony.uicc.AdnRecord>, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsInEfForSubscriber(int, int):java.util.List<com.android.internal.telephony.uicc.AdnRecord>");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsSize(int):int[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int[] getAdnRecordsSize(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsSize(int):int[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsSize(int):int[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsSizeForSubscriber(int, int):int[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int[] getAdnRecordsSizeForSubscriber(int r1, int r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsSizeForSubscriber(int, int):int[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAdnRecordsSizeForSubscriber(int, int):int[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAnrCount(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getAnrCount(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAnrCount(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getAnrCount(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getEmailCount(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getEmailCount(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getEmailCount(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getEmailCount(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getPhonebookMemStorageExt(int):com.mediatek.internal.telephony.uicc.UsimPBMemInfo[], dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public com.mediatek.internal.telephony.uicc.UsimPBMemInfo[] getPhonebookMemStorageExt(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getPhonebookMemStorageExt(int):com.mediatek.internal.telephony.uicc.UsimPBMemInfo[], dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getPhonebookMemStorageExt(int):com.mediatek.internal.telephony.uicc.UsimPBMemInfo[]");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getSneRecordLen(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getSneRecordLen(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getSneRecordLen(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getSneRecordLen(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUpbDone(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getUpbDone(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUpbDone(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUpbDone(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasById(int, int):java.lang.String, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public java.lang.String getUsimAasById(int r1, int r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasById(int, int):java.lang.String, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasById(int, int):java.lang.String");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasList(int):java.util.List<com.mediatek.internal.telephony.uicc.AlphaTag>, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public java.util.List<com.mediatek.internal.telephony.uicc.AlphaTag> getUsimAasList(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasList(int):java.util.List<com.mediatek.internal.telephony.uicc.AlphaTag>, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasList(int):java.util.List<com.mediatek.internal.telephony.uicc.AlphaTag>");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasMaxCount(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getUsimAasMaxCount(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasMaxCount(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasMaxCount(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasMaxNameLen(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getUsimAasMaxNameLen(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasMaxNameLen(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimAasMaxNameLen(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGroupById(int, int):java.lang.String, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public java.lang.String getUsimGroupById(int r1, int r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGroupById(int, int):java.lang.String, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGroupById(int, int):java.lang.String");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGroups(int):java.util.List<com.mediatek.internal.telephony.uicc.UsimGroup>, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public java.util.List<com.mediatek.internal.telephony.uicc.UsimGroup> getUsimGroups(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGroups(int):java.util.List<com.mediatek.internal.telephony.uicc.UsimGroup>, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGroups(int):java.util.List<com.mediatek.internal.telephony.uicc.UsimGroup>");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGrpMaxCount(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getUsimGrpMaxCount(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGrpMaxCount(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGrpMaxCount(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGrpMaxNameLen(int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int getUsimGrpMaxNameLen(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGrpMaxNameLen(int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.getUsimGrpMaxNameLen(int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.hasExistGroup(int, java.lang.String):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int hasExistGroup(int r1, java.lang.String r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.hasExistGroup(int, java.lang.String):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.hasExistGroup(int, java.lang.String):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.hasSne(int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean hasSne(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.hasSne(int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.hasSne(int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.insertUsimAas(int, java.lang.String):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int insertUsimAas(int r1, java.lang.String r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.insertUsimAas(int, java.lang.String):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.insertUsimAas(int, java.lang.String):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.insertUsimGroup(int, java.lang.String):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int insertUsimGroup(int r1, java.lang.String r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.insertUsimGroup(int, java.lang.String):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.insertUsimGroup(int, java.lang.String):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.isAdnAccessible(int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean isAdnAccessible(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.isAdnAccessible(int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.isAdnAccessible(int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.isPhbReady(int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean isPhbReady(int r1) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.isPhbReady(int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.isPhbReady(int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.moveContactFromGroupsToGroups(int, int, int[], int[]):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean moveContactFromGroupsToGroups(int r1, int r2, int[] r3, int[] r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.moveContactFromGroupsToGroups(int, int, int[], int[]):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.moveContactFromGroupsToGroups(int, int, int[], int[]):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeContactFromGroup(int, int, int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean removeContactFromGroup(int r1, int r2, int r3) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeContactFromGroup(int, int, int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeContactFromGroup(int, int, int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeUsimAasById(int, int, int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean removeUsimAasById(int r1, int r2, int r3) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeUsimAasById(int, int, int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeUsimAasById(int, int, int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeUsimGroupById(int, int):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean removeUsimGroupById(int r1, int r2) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeUsimGroupById(int, int):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.removeUsimGroupById(int, int):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndex(int, java.lang.String, java.lang.String, int, java.lang.String):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean updateAdnRecordsInEfByIndex(int r1, java.lang.String r2, java.lang.String r3, int r4, java.lang.String r5) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndex(int, java.lang.String, java.lang.String, int, java.lang.String):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndex(int, java.lang.String, java.lang.String, int, java.lang.String):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndexForSubscriber(int, int, java.lang.String, java.lang.String, int, java.lang.String):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean updateAdnRecordsInEfByIndexForSubscriber(int r1, int r2, java.lang.String r3, java.lang.String r4, int r5, java.lang.String r6) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndexForSubscriber(int, int, java.lang.String, java.lang.String, int, java.lang.String):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndexForSubscriber(int, int, java.lang.String, java.lang.String, int, java.lang.String):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndexWithError(int, int, java.lang.String, java.lang.String, int, java.lang.String):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int updateAdnRecordsInEfByIndexWithError(int r1, int r2, java.lang.String r3, java.lang.String r4, int r5, java.lang.String r6) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndexWithError(int, int, java.lang.String, java.lang.String, int, java.lang.String):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfByIndexWithError(int, int, java.lang.String, java.lang.String, int, java.lang.String):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearch(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean updateAdnRecordsInEfBySearch(int r1, java.lang.String r2, java.lang.String r3, java.lang.String r4, java.lang.String r5, java.lang.String r6) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearch(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearch(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearchForSubscriber(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean updateAdnRecordsInEfBySearchForSubscriber(int r1, int r2, java.lang.String r3, java.lang.String r4, java.lang.String r5, java.lang.String r6, java.lang.String r7) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearchForSubscriber(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearchForSubscriber(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearchWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int updateAdnRecordsInEfBySearchWithError(int r1, int r2, java.lang.String r3, java.lang.String r4, java.lang.String r5, java.lang.String r6, java.lang.String r7) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearchWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateAdnRecordsInEfBySearchWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateContactToGroups(int, int, int[]):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean updateContactToGroups(int r1, int r2, int[] r3) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateContactToGroups(int, int, int[]):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateContactToGroups(int, int, int[]):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimAas(int, int, int, java.lang.String):boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public boolean updateUsimAas(int r1, int r2, int r3, java.lang.String r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimAas(int, int, int, java.lang.String):boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimAas(int, int, int, java.lang.String):boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimGroup(int, int, java.lang.String):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int updateUsimGroup(int r1, int r2, java.lang.String r3) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimGroup(int, int, java.lang.String):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimGroup(int, int, java.lang.String):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsByIndexWithError(int, int, com.android.internal.telephony.uicc.AdnRecord, int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int updateUsimPBRecordsByIndexWithError(int r1, int r2, com.android.internal.telephony.uicc.AdnRecord r3, int r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsByIndexWithError(int, int, com.android.internal.telephony.uicc.AdnRecord, int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsByIndexWithError(int, int, com.android.internal.telephony.uicc.AdnRecord, int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsBySearchWithError(int, int, com.android.internal.telephony.uicc.AdnRecord, com.android.internal.telephony.uicc.AdnRecord):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int updateUsimPBRecordsBySearchWithError(int r1, int r2, com.android.internal.telephony.uicc.AdnRecord r3, com.android.internal.telephony.uicc.AdnRecord r4) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsBySearchWithError(int, int, com.android.internal.telephony.uicc.AdnRecord, com.android.internal.telephony.uicc.AdnRecord):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsBySearchWithError(int, int, com.android.internal.telephony.uicc.AdnRecord, com.android.internal.telephony.uicc.AdnRecord):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsInEfByIndexWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[], int):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int updateUsimPBRecordsInEfByIndexWithError(int r1, int r2, java.lang.String r3, java.lang.String r4, java.lang.String r5, java.lang.String r6, java.lang.String[] r7, int r8) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsInEfByIndexWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[], int):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsInEfByIndexWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[], int):int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsInEfBySearchWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[]):int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
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
                	... 11 more
                */
            public int updateUsimPBRecordsInEfBySearchWithError(int r1, int r2, java.lang.String r3, java.lang.String r4, java.lang.String r5, java.lang.String r6, java.lang.String[] r7, java.lang.String r8, java.lang.String r9, java.lang.String r10, java.lang.String r11, java.lang.String[] r12) throws android.os.RemoteException {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsInEfBySearchWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[]):int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: com.android.internal.telephony.IIccPhoneBook.Stub.Proxy.updateUsimPBRecordsInEfBySearchWithError(int, int, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[], java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String[]):int");
            }

            public String getInterfaceDescriptor() {
                return Stub.DESCRIPTOR;
            }
        }

        public Stub() {
            attachInterface(this, DESCRIPTOR);
        }

        public static IIccPhoneBook asInterface(IBinder obj) {
            if (obj == null) {
                return null;
            }
            IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
            if (iin == null || !(iin instanceof IIccPhoneBook)) {
                return new Proxy(obj);
            }
            return (IIccPhoneBook) iin;
        }

        public IBinder asBinder() {
            return this;
        }

        public boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
            List<AdnRecord> _result;
            boolean _result2;
            int _result3;
            int _arg0;
            int _arg1;
            AdnRecord _arg2;
            int[] _result4;
            String _result5;
            switch (code) {
                case 1:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdnRecordsInEf(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case 2:
                    data.enforceInterface(DESCRIPTOR);
                    _result = getAdnRecordsInEfForSubscriber(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result);
                    return true;
                case 3:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfBySearch(data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 4:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfBySearchForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 5:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateAdnRecordsInEfBySearchWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 6:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateUsimPBRecordsInEfBySearchWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.createStringArray(), data.readString(), data.readString(), data.readString(), data.readString(), data.createStringArray());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 7:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfByIndex(data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 8:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateAdnRecordsInEfByIndexForSubscriber(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 9:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateAdnRecordsInEfByIndexWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 10:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateUsimPBRecordsInEfByIndexWithError(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.createStringArray(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 11:
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (AdnRecord) AdnRecord.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    _result3 = updateUsimPBRecordsByIndexWithError(_arg0, _arg1, _arg2, data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 12:
                    AdnRecord _arg3;
                    data.enforceInterface(DESCRIPTOR);
                    _arg0 = data.readInt();
                    _arg1 = data.readInt();
                    if (data.readInt() != 0) {
                        _arg2 = (AdnRecord) AdnRecord.CREATOR.createFromParcel(data);
                    } else {
                        _arg2 = null;
                    }
                    if (data.readInt() != 0) {
                        _arg3 = (AdnRecord) AdnRecord.CREATOR.createFromParcel(data);
                    } else {
                        _arg3 = null;
                    }
                    _result3 = updateUsimPBRecordsBySearchWithError(_arg0, _arg1, _arg2, _arg3);
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 13:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnRecordsSize(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result4);
                    return true;
                case 14:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnRecordsSizeForSubscriber(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result4);
                    return true;
                case 15:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isPhbReady(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 16:
                    data.enforceInterface(DESCRIPTOR);
                    List<UsimGroup> _result6 = getUsimGroups(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result6);
                    return true;
                case 17:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getUsimGroupById(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 18:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = removeUsimGroupById(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 19:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = insertUsimGroup(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 20:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = updateUsimGroup(data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 21:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = addContactToGroup(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 22:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = removeContactFromGroup(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 23:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateContactToGroups(data.readInt(), data.readInt(), data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 24:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = moveContactFromGroupsToGroups(data.readInt(), data.readInt(), data.createIntArray(), data.createIntArray());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 25:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = hasExistGroup(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 26:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getUsimGrpMaxNameLen(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 27:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getUsimGrpMaxCount(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 28:
                    data.enforceInterface(DESCRIPTOR);
                    List<AlphaTag> _result7 = getUsimAasList(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedList(_result7);
                    return true;
                case 29:
                    data.enforceInterface(DESCRIPTOR);
                    _result5 = getUsimAasById(data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeString(_result5);
                    return true;
                case 30:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = insertUsimAas(data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 31:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getAnrCount(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 32:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getEmailCount(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 33:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getUsimAasMaxCount(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 34:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getUsimAasMaxNameLen(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 35:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = updateUsimAas(data.readInt(), data.readInt(), data.readInt(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 36:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = removeUsimAasById(data.readInt(), data.readInt(), data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 37:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = hasSne(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 38:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getSneRecordLen(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 39:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = isAdnAccessible(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 40:
                    data.enforceInterface(DESCRIPTOR);
                    UsimPBMemInfo[] _result8 = getPhonebookMemStorageExt(data.readInt());
                    reply.writeNoException();
                    reply.writeTypedArray(_result8, 1);
                    return true;
                case 41:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = getUpbDone(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 42:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnRecordsCapacity();
                    reply.writeNoException();
                    reply.writeIntArray(_result4);
                    return true;
                case 43:
                    data.enforceInterface(DESCRIPTOR);
                    _result4 = getAdnRecordsCapacityForSubscriber(data.readInt());
                    reply.writeNoException();
                    reply.writeIntArray(_result4);
                    return true;
                case 44:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = colorGetSimNameLenUsingSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 45:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = colorAddAdnRecordsInEfBySearchExUsingSubId(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 46:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = colorUpdateAdnRecordsInEfByIndexExUsingSubId(data.readInt(), data.readInt(), data.readString(), data.readString(), data.readString(), data.readInt(), data.readString(), data.readString());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 47:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = colorISPhoneBookReady(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case TRANSACTION_colorIsPhoneBookPbrExist /*48*/:
                    data.enforceInterface(DESCRIPTOR);
                    _result2 = colorIsPhoneBookPbrExist(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result2 ? 1 : 0);
                    return true;
                case 49:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = colorGetAdnEmailLenUsingSubId(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 50:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = colorGetSimPhonebookAllSpace(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 51:
                    data.enforceInterface(DESCRIPTOR);
                    _result3 = colorGetSimPhonebookUsedSpace(data.readInt());
                    reply.writeNoException();
                    reply.writeInt(_result3);
                    return true;
                case 1598968902:
                    reply.writeString(DESCRIPTOR);
                    return true;
                default:
                    return super.onTransact(code, data, reply, flags);
            }
        }
    }

    boolean addContactToGroup(int i, int i2, int i3) throws RemoteException;

    int colorAddAdnRecordsInEfBySearchExUsingSubId(int i, int i2, String str, String str2, String str3, String str4, String str5, String str6, String str7) throws RemoteException;

    int colorGetAdnEmailLenUsingSubId(int i) throws RemoteException;

    int colorGetSimNameLenUsingSubId(int i) throws RemoteException;

    int colorGetSimPhonebookAllSpace(int i) throws RemoteException;

    int colorGetSimPhonebookUsedSpace(int i) throws RemoteException;

    boolean colorISPhoneBookReady(int i) throws RemoteException;

    boolean colorIsPhoneBookPbrExist(int i) throws RemoteException;

    boolean colorUpdateAdnRecordsInEfByIndexExUsingSubId(int i, int i2, String str, String str2, String str3, int i3, String str4, String str5) throws RemoteException;

    int[] getAdnRecordsCapacity() throws RemoteException;

    int[] getAdnRecordsCapacityForSubscriber(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsInEf(int i) throws RemoteException;

    List<AdnRecord> getAdnRecordsInEfForSubscriber(int i, int i2) throws RemoteException;

    int[] getAdnRecordsSize(int i) throws RemoteException;

    int[] getAdnRecordsSizeForSubscriber(int i, int i2) throws RemoteException;

    int getAnrCount(int i) throws RemoteException;

    int getEmailCount(int i) throws RemoteException;

    UsimPBMemInfo[] getPhonebookMemStorageExt(int i) throws RemoteException;

    int getSneRecordLen(int i) throws RemoteException;

    int getUpbDone(int i) throws RemoteException;

    String getUsimAasById(int i, int i2) throws RemoteException;

    List<AlphaTag> getUsimAasList(int i) throws RemoteException;

    int getUsimAasMaxCount(int i) throws RemoteException;

    int getUsimAasMaxNameLen(int i) throws RemoteException;

    String getUsimGroupById(int i, int i2) throws RemoteException;

    List<UsimGroup> getUsimGroups(int i) throws RemoteException;

    int getUsimGrpMaxCount(int i) throws RemoteException;

    int getUsimGrpMaxNameLen(int i) throws RemoteException;

    int hasExistGroup(int i, String str) throws RemoteException;

    boolean hasSne(int i) throws RemoteException;

    int insertUsimAas(int i, String str) throws RemoteException;

    int insertUsimGroup(int i, String str) throws RemoteException;

    boolean isAdnAccessible(int i) throws RemoteException;

    boolean isPhbReady(int i) throws RemoteException;

    boolean moveContactFromGroupsToGroups(int i, int i2, int[] iArr, int[] iArr2) throws RemoteException;

    boolean removeContactFromGroup(int i, int i2, int i3) throws RemoteException;

    boolean removeUsimAasById(int i, int i2, int i3) throws RemoteException;

    boolean removeUsimGroupById(int i, int i2) throws RemoteException;

    boolean updateAdnRecordsInEfByIndex(int i, String str, String str2, int i2, String str3) throws RemoteException;

    boolean updateAdnRecordsInEfByIndexForSubscriber(int i, int i2, String str, String str2, int i3, String str3) throws RemoteException;

    int updateAdnRecordsInEfByIndexWithError(int i, int i2, String str, String str2, int i3, String str3) throws RemoteException;

    boolean updateAdnRecordsInEfBySearch(int i, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateAdnRecordsInEfBySearchForSubscriber(int i, int i2, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    int updateAdnRecordsInEfBySearchWithError(int i, int i2, String str, String str2, String str3, String str4, String str5) throws RemoteException;

    boolean updateContactToGroups(int i, int i2, int[] iArr) throws RemoteException;

    boolean updateUsimAas(int i, int i2, int i3, String str) throws RemoteException;

    int updateUsimGroup(int i, int i2, String str) throws RemoteException;

    int updateUsimPBRecordsByIndexWithError(int i, int i2, AdnRecord adnRecord, int i3) throws RemoteException;

    int updateUsimPBRecordsBySearchWithError(int i, int i2, AdnRecord adnRecord, AdnRecord adnRecord2) throws RemoteException;

    int updateUsimPBRecordsInEfByIndexWithError(int i, int i2, String str, String str2, String str3, String str4, String[] strArr, int i3) throws RemoteException;

    int updateUsimPBRecordsInEfBySearchWithError(int i, int i2, String str, String str2, String str3, String str4, String[] strArr, String str5, String str6, String str7, String str8, String[] strArr2) throws RemoteException;
}
