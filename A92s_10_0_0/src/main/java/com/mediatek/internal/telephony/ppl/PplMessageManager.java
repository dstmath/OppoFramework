package com.mediatek.internal.telephony.ppl;

import android.content.Context;
import java.util.Arrays;
import java.util.regex.Pattern;

public class PplMessageManager {
    private static final String[] SMS_PATTERNS = {"我的手机可能被盗，请保留发送此短信的号码。", " *#suoding# *", "已接受到您的锁屏指令，锁屏成功。", " *#jiesuo# *", "已接受到您的解锁指令，解锁成功。", " *#mima# *", "您的手机防盗密码为[0-9]*。", " *#xiaohui# *", "远程删除数据已开始。", "远程数据删除已完成，您的隐私得到保护，请放心。", "我开启了手机防盗功能，已将你的手机号码设置为紧急联系人号码，这样手机丢失也能够远程控制啦。\n以下是相关指令：\n远程锁定： #suoding#\n远程销毁数据： #xiaohui#\n找回密码： #mima#"};
    public static final String SMS_SENT_ACTION = "com.mediatek.ppl.SMS_SENT";
    private static final String[] SMS_TEMPLATES = {"我的手机可能被盗，请保留发送此短信的号码。", "#suoding#", "已接受到您的锁屏指令，锁屏成功。", "#jiesuo#", "已接受到您的解锁指令，解锁成功。", "#mima#", "您的手机防盗密码为%s。", "#xiaohui#", "远程删除数据已开始。", "远程数据删除已完成，您的隐私得到保护，请放心。", "我开启了手机防盗功能，已将你的手机号码设置为紧急联系人号码，这样手机丢失也能够远程控制啦。\n以下是相关指令：\n远程锁定： #suoding#\n远程销毁数据： #xiaohui#\n找回密码： #mima#"};
    private final Context mContext;
    private final Pattern[] mMessagePatterns;
    private final String[] mMessageTemplates = SMS_TEMPLATES;

    public static class Type {
        public static final byte INSTRUCTION_DESCRIPTION = 10;
        public static final byte INSTRUCTION_DESCRIPTION2 = 11;
        public static final byte INVALID = -1;
        public static final byte LOCK_REQUEST = 1;
        public static final byte LOCK_RESPONSE = 2;
        public static final byte RESET_PW_REQUEST = 5;
        public static final byte RESET_PW_RESPONSE = 6;
        public static final byte SIM_CHANGED = 0;
        public static final byte UNLOCK_REQUEST = 3;
        public static final byte UNLOCK_RESPONSE = 4;
        public static final byte WIPE_COMPLETED = 9;
        public static final byte WIPE_REQUEST = 7;
        public static final byte WIPE_STARTED = 8;
    }

    public static class PendingMessage {
        public static final int ALL_SIM_ID = -2;
        public static final int ANY_SIM_ID = -1;
        public static final long INVALID_ID = -1;
        public static final int INVALID_SIM_ID = -3;
        public static final String KEY_FIRST_TRIAL = "firstTrial";
        public static final String KEY_ID = "id";
        public static final String KEY_NUMBER = "number";
        public static final String KEY_SEGMENT_INDEX = "segmentIndex";
        public static final String KEY_SIM_ID = "simId";
        public static final String KEY_TYPE = "type";
        public static final int PENDING_MESSAGE_LENGTH = 49;
        public String content;
        public long id;
        public String number;
        public int simId;
        public byte type;

        public static long getNextId() {
            return System.currentTimeMillis();
        }

        public PendingMessage(long id2, byte type2, String number2, int simId2, String content2) {
            this.id = id2;
            this.type = type2;
            this.number = number2;
            this.simId = simId2;
            this.content = content2;
        }

        public PendingMessage() {
            this.id = -1;
            this.type = -1;
            this.number = null;
            this.simId = -1;
            this.content = null;
        }

        public PendingMessage(byte[] buffer, int offset) {
            decode(buffer, offset);
        }

        public PendingMessage clone() {
            return new PendingMessage(this.id, this.type, this.number, this.simId, this.content);
        }

        public String toString() {
            return "PendingMessage " + hashCode() + " {" + this.id + ", " + ((int) this.type) + ", " + this.number + ", " + this.simId + ", " + this.content + "}";
        }

        public void encode(byte[] buffer, int offset) {
            int offset2 = offset + 1;
            buffer[offset] = this.type;
            byte[] idBytes = long2bytes(this.id);
            System.arraycopy(idBytes, 0, buffer, offset2, idBytes.length);
            int offset3 = offset2 + 8;
            byte[] numberBytes = this.number.getBytes();
            if (numberBytes.length <= 40) {
                byte[] numberBytes2 = Arrays.copyOf(numberBytes, 40);
                System.arraycopy(numberBytes2, 0, buffer, offset3, numberBytes2.length);
                return;
            }
            throw new Error("Destination number is too long");
        }

        public void decode(byte[] buffer, int offset) {
            int offset2 = offset + 1;
            this.type = buffer[offset];
            this.id = bytes2long(buffer, offset2);
            int offset3 = offset2 + 8;
            int j = offset3;
            while (j < offset3 + 40 && buffer[j] != 0) {
                j++;
            }
            this.number = new String(buffer, offset3, j - offset3);
        }

        private static long bytes2long(byte[] b, int offset) {
            long res = 0;
            for (int i = 0; i < 8; i++) {
                res = (res << 8) | ((long) (b[i + offset] & Type.INVALID));
            }
            return res;
        }

        private static byte[] long2bytes(long num) {
            byte[] b = new byte[8];
            for (int i = 0; i < 8; i++) {
                b[i] = (byte) ((int) (num >>> (56 - (i * 8))));
            }
            return b;
        }
    }

    public PplMessageManager(Context context) {
        this.mContext = context;
        this.mContext.getResources();
        String[] patternStrings = SMS_PATTERNS;
        this.mMessagePatterns = new Pattern[patternStrings.length];
        for (int i = 0; i < patternStrings.length; i++) {
            this.mMessagePatterns[i] = Pattern.compile(patternStrings[i], 2);
        }
    }

    public byte getMessageType(String message) {
        byte result = -1;
        byte i = 0;
        while (true) {
            Pattern[] patternArr = this.mMessagePatterns;
            if (i >= patternArr.length) {
                break;
            } else if (patternArr[i].matcher(message).matches()) {
                result = i;
                break;
            } else {
                i = (byte) (i + 1);
            }
        }
        if (result == 11) {
            return 10;
        }
        return result;
    }

    public String getMessageTemplate(byte type) {
        return this.mMessageTemplates[type];
    }

    public String buildMessage(byte type, Object... args) {
        return String.format(getMessageTemplate(type), args);
    }
}
