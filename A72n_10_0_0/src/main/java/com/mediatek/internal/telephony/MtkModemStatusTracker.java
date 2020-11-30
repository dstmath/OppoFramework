package com.mediatek.internal.telephony;

import android.telephony.Rlog;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class MtkModemStatusTracker {
    private static final String LOG_TAG = "MtkModemStatusTracker";
    private static final int SLP_SHM_LTE_RX_TIME = 136;
    private static final int SLP_SHM_LTE_TX_PWR_L1 = 184;
    private static final int SLP_SHM_LTE_TX_PWR_L2 = 192;
    private static final int SLP_SHM_LTE_TX_PWR_L3 = 200;
    private static final int SLP_SHM_LTE_TX_PWR_L4 = 208;
    private static final int SLP_SHM_LTE_TX_PWR_L5 = 216;
    private static final int SLP_SHM_LTE_TX_PWR_L6 = 224;
    private static final int SLP_SHM_LTE_TX_PWR_L7 = 232;
    private static final int SLP_SHM_LTE_TX_PWR_L8 = 240;
    private static final int SLP_SHM_LTE_TX_TIME = 152;
    private static final int SLP_SHM_MD_SLEEP_TIME = 56;
    private static final int SLP_SHM_MD_TOTAL_TIME = 320;
    private static final int SLP_SHM_NR_RX_TIME = 144;
    private static final int SLP_SHM_NR_TX_PWR_L1 = 248;
    private static final int SLP_SHM_NR_TX_PWR_L2 = 256;
    private static final int SLP_SHM_NR_TX_PWR_L3 = 264;
    private static final int SLP_SHM_NR_TX_PWR_L4 = 272;
    private static final int SLP_SHM_NR_TX_PWR_L5 = 280;
    private static final int SLP_SHM_NR_TX_PWR_L6 = 288;
    private static final int SLP_SHM_NR_TX_PWR_L7 = 296;
    private static final int SLP_SHM_NR_TX_PWR_L8 = 304;
    private static final int SLP_SHM_NR_TX_TIME = 160;
    private static File file = new File("/proc/ccci_lp_mem");

    public enum Rat {
        RADIO_4G,
        RADIO_5G
    }

    public enum TxLevel {
        TX_LEVEL_BELOW_MINUS_FIVE,
        TX_LEVEL_MINUS_FIVE_TO_ONE,
        TX_LEVEL_ONE_TO_FIVE,
        TX_LEVEL_FIVE_TO_TEN,
        TX_LEVEL_TEN_TO_FIFTEEN,
        TX_LEVEL_FIFTEEN_TO_TWENTY,
        TX_LEVEL_TWENTY_TO_TWENTY_THREE,
        TX_LEVEL_TWENTY_THREE_TO_TWENTY_SIX
    }

    private static long readDataFromFile(int index) {
        long finish = System.currentTimeMillis() + 100;
        while (System.currentTimeMillis() < finish) {
            boolean mIsReadSuccess = true;
            try {
                byte[] mModemStatus = Files.readAllBytes(file.toPath());
                int i = 0;
                while (true) {
                    if (i >= 4) {
                        break;
                    } else if (mModemStatus[i] != mModemStatus[(mModemStatus.length - 4) + i]) {
                        Rlog.d(LOG_TAG, "File counter not match");
                        mIsReadSuccess = false;
                        continue;
                        break;
                    } else {
                        i++;
                    }
                }
                if (mIsReadSuccess) {
                    return Long.reverseBytes(ByteBuffer.wrap(mModemStatus).getLong(index));
                }
            } catch (IOException e) {
                Rlog.d(LOG_TAG, "File open fail:" + e.getMessage());
                return -1;
            }
        }
        return -1;
    }

    public static long getModemTotalTime() {
        return readDataFromFile(SLP_SHM_MD_TOTAL_TIME);
    }

    public static long getModemSleepTime() {
        return readDataFromFile(56);
    }

    public static long getModemRxTime(Rat rat) {
        if (rat == Rat.RADIO_4G) {
            return readDataFromFile(SLP_SHM_LTE_RX_TIME);
        }
        if (rat == Rat.RADIO_5G) {
            return readDataFromFile(SLP_SHM_NR_RX_TIME);
        }
        return -1;
    }

    public static long getModemTxTime(Rat rat) {
        if (rat == Rat.RADIO_4G) {
            return readDataFromFile(SLP_SHM_LTE_TX_TIME);
        }
        if (rat == Rat.RADIO_5G) {
            return readDataFromFile(SLP_SHM_NR_TX_TIME);
        }
        return -1;
    }

    public static long getModemTxLevelTime(Rat rat, TxLevel txLevel) {
        if (rat == Rat.RADIO_4G) {
            switch (txLevel) {
                case TX_LEVEL_BELOW_MINUS_FIVE:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L1);
                case TX_LEVEL_MINUS_FIVE_TO_ONE:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L2);
                case TX_LEVEL_ONE_TO_FIVE:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L3);
                case TX_LEVEL_FIVE_TO_TEN:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L4);
                case TX_LEVEL_TEN_TO_FIFTEEN:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L5);
                case TX_LEVEL_FIFTEEN_TO_TWENTY:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L6);
                case TX_LEVEL_TWENTY_TO_TWENTY_THREE:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L7);
                case TX_LEVEL_TWENTY_THREE_TO_TWENTY_SIX:
                    return readDataFromFile(SLP_SHM_LTE_TX_PWR_L8);
                default:
                    return -1;
            }
        } else if (rat != Rat.RADIO_5G) {
            return -1;
        } else {
            switch (txLevel) {
                case TX_LEVEL_BELOW_MINUS_FIVE:
                    return readDataFromFile(SLP_SHM_NR_TX_PWR_L1);
                case TX_LEVEL_MINUS_FIVE_TO_ONE:
                    return readDataFromFile(256);
                case TX_LEVEL_ONE_TO_FIVE:
                    return readDataFromFile(SLP_SHM_NR_TX_PWR_L3);
                case TX_LEVEL_FIVE_TO_TEN:
                    return readDataFromFile(SLP_SHM_NR_TX_PWR_L4);
                case TX_LEVEL_TEN_TO_FIFTEEN:
                    return readDataFromFile(SLP_SHM_NR_TX_PWR_L5);
                case TX_LEVEL_FIFTEEN_TO_TWENTY:
                    return readDataFromFile(SLP_SHM_NR_TX_PWR_L6);
                case TX_LEVEL_TWENTY_TO_TWENTY_THREE:
                    return readDataFromFile(SLP_SHM_NR_TX_PWR_L7);
                case TX_LEVEL_TWENTY_THREE_TO_TWENTY_SIX:
                    return readDataFromFile(SLP_SHM_NR_TX_PWR_L8);
                default:
                    return -1;
            }
        }
    }
}
