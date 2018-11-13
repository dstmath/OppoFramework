package com.suntek.rcs.ui.common.mms;

import android.text.InputFilter;
import android.text.Spanned;
import com.suntek.mway.rcs.client.aidl.constant.Constants.MessageConstants;

public class RcsEditTextInputFilter implements InputFilter {
    private int unicodeLength = 0;

    public RcsEditTextInputFilter(int length) {
        this.unicodeLength = length;
    }

    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
        if (source == null) {
            return "";
        }
        CharSequence result;
        int sourceCount = getWordCount(source.toString());
        int destCount = getWordCount(dest.toString());
        int keepCount = this.unicodeLength - destCount;
        int count = destCount + sourceCount;
        if (keepCount <= 0) {
            result = "";
        } else if (count <= this.unicodeLength) {
            result = source;
        } else {
            int charCount = keepCount;
            keepCount = 0;
            for (char wordCount : source.toString().toCharArray()) {
                if (getWordCount(wordCount) == 3) {
                    charCount -= 3;
                } else {
                    charCount--;
                }
                if (charCount <= 0) {
                    break;
                }
                keepCount++;
            }
            result = source.subSequence(start, start + keepCount);
        }
        return result;
    }

    public int getWordCount(CharSequence s) {
        int length = 0;
        for (int i = 0; i < s.length(); i++) {
            if (isAsciiCode(Character.codePointAt(s, i))) {
                length++;
            } else {
                length += 3;
            }
        }
        return length;
    }

    private int getWordCount(char ascii) {
        if (ascii < 0 || ascii > 255) {
            return 3;
        }
        return 1;
    }

    private boolean isAsciiCode(int ascii) {
        return ascii >= 0 && ascii < MessageConstants.CONST_STATUS_SEND_FAIL;
    }
}
