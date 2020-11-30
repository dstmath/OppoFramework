package com.color.antivirus.tencent;

import android.os.DropBoxManager;
import android.provider.SettingsStringUtil;
import android.provider.UserDictionary;
import android.telephony.SmsManager;
import android.text.TextUtils;
import android.util.Xml;
import com.color.antivirus.AntivirusLog;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.HashSet;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class TrpConfig implements Serializable {
    private static final String TAG = "TrpConfig";
    private static final long serialVersionUID = 4820599244944971202L;
    private int mDuration;
    private int mFrequency;
    private String mId;
    private int mIncrement;
    private int mInputSize;
    private int mLimit;
    private int mTime;
    private int mTimeStep;

    public TrpConfig(int time, int timeStep, int increment, String id, int frequency, int limit, int duration, int inputSize) {
        this.mTime = time;
        this.mTimeStep = timeStep;
        this.mIncrement = increment;
        this.mId = id;
        this.mFrequency = frequency * 1000;
        this.mLimit = limit;
        this.mDuration = duration * 60;
        this.mInputSize = inputSize;
    }

    public int getTime() {
        return this.mTime;
    }

    public int getTimeStep() {
        return this.mTimeStep;
    }

    public int getIncrement() {
        return this.mIncrement;
    }

    public String getId() {
        return this.mId;
    }

    public HashSet<Integer> getIdList() {
        String idStr;
        if (TextUtils.isEmpty(this.mId)) {
            return null;
        }
        HashSet<Integer> idset = new HashSet<>();
        int index = 0;
        int length = this.mId.length();
        while (index < length) {
            int split = this.mId.indexOf(SmsManager.REGEX_PREFIX_DELIMITER, index);
            if (split != -1) {
                idStr = this.mId.substring(index, split);
                index = split + 1;
            } else {
                idStr = this.mId.substring(index, length);
                index = length;
            }
            idset.add(Integer.valueOf(Integer.parseInt(idStr)));
        }
        return idset;
    }

    public int getLimit() {
        return this.mLimit;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public int getInputSize() {
        return this.mInputSize;
    }

    public int getFrequency() {
        return this.mFrequency;
    }

    public TrpConfig(byte[] aData) {
        parseTrpConfig(new ByteArrayInputStream(aData));
    }

    private void parseTrpConfig(InputStream inputStream) {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(inputStream, "utf-8");
            for (int type = parser.getEventType(); type != 1; type = parser.next()) {
                if (type == 2) {
                    String nameTemp = parser.getName();
                    if (nameTemp.compareToIgnoreCase(DropBoxManager.EXTRA_TIME) == 0) {
                        this.mTime = Integer.parseInt(parser.nextText());
                    } else if (nameTemp.compareToIgnoreCase("timestep") == 0) {
                        this.mTimeStep = Integer.parseInt(parser.nextText());
                    } else if (nameTemp.compareToIgnoreCase("increment") == 0) {
                        this.mIncrement = Integer.parseInt(parser.nextText());
                    } else if (nameTemp.compareToIgnoreCase("id") == 0) {
                        this.mId = parser.nextText();
                    } else if (nameTemp.compareToIgnoreCase(UserDictionary.Words.FREQUENCY) == 0) {
                        this.mFrequency = Integer.parseInt(parser.nextText()) * 1000;
                    } else if (nameTemp.compareToIgnoreCase("limit") == 0) {
                        this.mLimit = Integer.parseInt(parser.nextText());
                    } else if (nameTemp.compareToIgnoreCase("duration") == 0) {
                        this.mDuration = Integer.parseInt(parser.nextText()) * 60;
                    } else if (nameTemp.compareToIgnoreCase("inputsize") == 0) {
                        this.mInputSize = Integer.parseInt(parser.nextText());
                    }
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (XmlPullParserException e2) {
            AntivirusLog.e(TAG, "Failed to create TrpConfig instance: " + e2.toString());
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e3) {
            AntivirusLog.e(TAG, "Failed to create TrpConfig instance: " + e3.toString());
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e4) {
            AntivirusLog.e(TAG, "Failed to create TrpConfig instance: " + e4.toString());
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                    e5.printStackTrace();
                }
            }
            throw th;
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("TrpConfig{");
        sb.append("mTime");
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mTime);
        sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
        sb.append("mTimeStep");
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mTimeStep);
        sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
        sb.append("mIncrement");
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mIncrement);
        sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
        sb.append("mId");
        sb.append(":[");
        sb.append(this.mId);
        sb.append("],");
        sb.append("mFrequency");
        sb.append(":[");
        sb.append(this.mFrequency);
        sb.append("],");
        sb.append("mLimit");
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mLimit);
        sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
        sb.append("mDuration");
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mDuration);
        sb.append(SmsManager.REGEX_PREFIX_DELIMITER);
        sb.append("mInputSize");
        sb.append(SettingsStringUtil.DELIMITER);
        sb.append(this.mInputSize);
        sb.append("}");
        return sb.toString();
    }

    public boolean isValid() {
        int i;
        int i2 = this.mTime;
        if (i2 <= 0 || (i = this.mTimeStep) <= 0 || i % i2 != 0 || this.mIncrement <= 0 || this.mFrequency <= 0 || this.mLimit <= 0 || this.mDuration <= 0 || this.mInputSize <= 0 || this.mId == null) {
            return false;
        }
        return true;
    }
}
