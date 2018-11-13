package com.android.internal.alsa;

import android.util.Slog;
import com.android.internal.telephony.PhoneConstants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

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
public class AlsaCardsParser {
    protected static final boolean DEBUG = false;
    private static final String TAG = "AlsaCardsParser";
    private static final String kCardsFilePath = "/proc/asound/cards";
    private static LineTokenizer mTokenizer;
    private ArrayList<AlsaCardRecord> mCardRecords;

    public class AlsaCardRecord {
        private static final String TAG = "AlsaCardRecord";
        private static final String kUsbCardKeyStr = "at usb-";
        public String mCardDescription = PhoneConstants.MVNO_TYPE_NONE;
        public String mCardName = PhoneConstants.MVNO_TYPE_NONE;
        public int mCardNum = -1;
        public String mField1 = PhoneConstants.MVNO_TYPE_NONE;
        public boolean mIsUsb = false;

        public boolean parse(String line, int lineIndex) {
            boolean z = false;
            int tokenIndex;
            if (lineIndex == 0) {
                tokenIndex = AlsaCardsParser.mTokenizer.nextToken(line, 0);
                int delimIndex = AlsaCardsParser.mTokenizer.nextDelimiter(line, tokenIndex);
                try {
                    this.mCardNum = Integer.parseInt(line.substring(tokenIndex, delimIndex));
                    tokenIndex = AlsaCardsParser.mTokenizer.nextToken(line, delimIndex);
                    delimIndex = AlsaCardsParser.mTokenizer.nextDelimiter(line, tokenIndex);
                    this.mField1 = line.substring(tokenIndex, delimIndex);
                    this.mCardName = line.substring(AlsaCardsParser.mTokenizer.nextToken(line, delimIndex));
                } catch (NumberFormatException e) {
                    Slog.e(TAG, "Failed to parse line " + lineIndex + " of " + AlsaCardsParser.kCardsFilePath + ": " + line.substring(tokenIndex, delimIndex));
                    return false;
                }
            } else if (lineIndex == 1) {
                tokenIndex = AlsaCardsParser.mTokenizer.nextToken(line, 0);
                if (tokenIndex != -1) {
                    int keyIndex = line.indexOf(kUsbCardKeyStr);
                    if (keyIndex != -1) {
                        z = true;
                    }
                    this.mIsUsb = z;
                    if (this.mIsUsb) {
                        this.mCardDescription = line.substring(tokenIndex, keyIndex - 1);
                    }
                }
            }
            return true;
        }

        public String textFormat() {
            return this.mCardName + " : " + this.mCardDescription;
        }

        public void log(int listIndex) {
            Slog.d(TAG, PhoneConstants.MVNO_TYPE_NONE + listIndex + " [" + this.mCardNum + " " + this.mCardName + " : " + this.mCardDescription + " usb:" + this.mIsUsb);
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.internal.alsa.AlsaCardsParser.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.internal.alsa.AlsaCardsParser.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.alsa.AlsaCardsParser.<clinit>():void");
    }

    public AlsaCardsParser() {
        this.mCardRecords = new ArrayList();
    }

    public void scan() {
        this.mCardRecords = new ArrayList();
        try {
            FileReader reader = new FileReader(new File(kCardsFilePath));
            BufferedReader bufferedReader = new BufferedReader(reader);
            String str = PhoneConstants.MVNO_TYPE_NONE;
            while (true) {
                str = bufferedReader.readLine();
                if (str == null) {
                    break;
                }
                AlsaCardRecord cardRecord = new AlsaCardRecord();
                cardRecord.parse(str, 0);
                str = bufferedReader.readLine();
                if (str == null) {
                    break;
                }
                cardRecord.parse(str, 1);
                this.mCardRecords.add(cardRecord);
            }
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        }
    }

    public ArrayList<AlsaCardRecord> getScanRecords() {
        return this.mCardRecords;
    }

    public AlsaCardRecord getCardRecordAt(int index) {
        return (AlsaCardRecord) this.mCardRecords.get(index);
    }

    public AlsaCardRecord getCardRecordFor(int cardNum) {
        for (AlsaCardRecord rec : this.mCardRecords) {
            if (rec.mCardNum == cardNum) {
                return rec;
            }
        }
        return null;
    }

    public int getNumCardRecords() {
        return this.mCardRecords.size();
    }

    public boolean isCardUsb(int cardNum) {
        for (AlsaCardRecord rec : this.mCardRecords) {
            if (rec.mCardNum == cardNum) {
                return rec.mIsUsb;
            }
        }
        return false;
    }

    public int getDefaultUsbCard() {
        ArrayList<AlsaCardRecord> prevRecs = this.mCardRecords;
        scan();
        for (AlsaCardRecord rec : getNewCardRecords(prevRecs)) {
            if (rec.mIsUsb) {
                return rec.mCardNum;
            }
        }
        for (AlsaCardRecord rec2 : prevRecs) {
            if (rec2.mIsUsb) {
                return rec2.mCardNum;
            }
        }
        return -1;
    }

    public int getDefaultCard() {
        int card = getDefaultUsbCard();
        if (card >= 0 || getNumCardRecords() <= 0) {
            return card;
        }
        return getCardRecordAt(getNumCardRecords() - 1).mCardNum;
    }

    public static boolean hasCardNumber(ArrayList<AlsaCardRecord> recs, int cardNum) {
        for (AlsaCardRecord cardRec : recs) {
            if (cardRec.mCardNum == cardNum) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<AlsaCardRecord> getNewCardRecords(ArrayList<AlsaCardRecord> prevScanRecs) {
        ArrayList<AlsaCardRecord> newRecs = new ArrayList();
        for (AlsaCardRecord rec : this.mCardRecords) {
            if (!hasCardNumber(prevScanRecs, rec.mCardNum)) {
                newRecs.add(rec);
            }
        }
        return newRecs;
    }

    public void Log(String heading) {
    }

    public static void LogDevices(String caption, ArrayList<AlsaCardRecord> deviceList) {
        Slog.d(TAG, caption + " ----------------");
        int listIndex = 0;
        for (AlsaCardRecord device : deviceList) {
            int listIndex2 = listIndex + 1;
            device.log(listIndex);
            listIndex = listIndex2;
        }
        Slog.d(TAG, "----------------");
    }
}
