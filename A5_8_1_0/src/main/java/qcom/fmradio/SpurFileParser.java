package qcom.fmradio;

import android.util.Log;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import qcom.fmradio.SpurFileFormatConst.LineType;

public class SpurFileParser implements SpurFileParserInterface {
    private static final String TAG = "SPUR";

    private boolean parse(BufferedReader reader, SpurTable t) {
        int entryFound = 0;
        if (t == null) {
            return false;
        }
        if (reader == null) {
            return false;
        }
        LineType lastLine = LineType.EMPTY_LINE;
        int SpurFreq = 0;
        byte noOfSpursFreq = (byte) 0;
        int freqCnt = 0;
        while (reader.ready()) {
            try {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                line = removeSpaces(line);
                System.out.println("line : " + line);
                if (!lineIsComment(line)) {
                    if (entryFound != 2 || freqCnt > noOfSpursFreq) {
                        if (entryFound == 1) {
                            if (!lineIsOfType(line, SpurFileFormatConst.SPUR_NUM_ENTRY)) {
                                return false;
                            }
                            noOfSpursFreq = Byte.parseByte(line.substring(line.indexOf(61) + 1));
                            t.SetspurNoOfFreq(noOfSpursFreq);
                            entryFound++;
                        } else if (!lineIsOfType(line, SpurFileFormatConst.SPUR_MODE)) {
                            return false;
                        } else {
                            t.SetMode(Byte.parseByte(line.substring(line.indexOf(61) + 1)));
                            entryFound++;
                        }
                    } else if (lastLine == LineType.EMPTY_LINE && lineIsOfType(line, SpurFileFormatConst.SPUR_FREQ)) {
                        SpurFreq = Integer.parseInt(line.substring(line.indexOf(61) + 1));
                        lastLine = LineType.SPUR_FR_LINE;
                        freqCnt = (byte) (freqCnt + 1);
                    } else if (lastLine != LineType.SPUR_FR_LINE || !lineIsOfType(line, SpurFileFormatConst.SPUR_NO_OF)) {
                        return false;
                    } else {
                        byte NoOfSpursToTrack = Byte.parseByte(line.substring(line.indexOf(61) + 1));
                        Spur spur = new Spur();
                        spur.setSpurFreq(SpurFreq);
                        spur.setNoOfSpursToTrack(NoOfSpursToTrack);
                        for (int i = 0; i < 3; i++) {
                            SpurDetails spurDetails = new SpurDetails();
                            int j = 0;
                            while (reader.ready() && j < SpurFileFormatConst.SPUR_DETAILS_FOR_EACH_FREQ_CNT) {
                                line = removeSpaces(reader.readLine());
                                System.out.println("inside line: " + line);
                                if (lineIsOfType(line, SpurFileFormatConst.SPUR_ROTATION_VALUE + i)) {
                                    spurDetails.setRotationValue(Integer.parseInt(line.substring(line.indexOf(61) + 1)));
                                } else if (lineIsOfType(line, SpurFileFormatConst.SPUR_LSB_LENGTH + i)) {
                                    spurDetails.setLsbOfIntegrationLength(Byte.parseByte(line.substring(line.indexOf(61) + 1)));
                                } else if (lineIsOfType(line, SpurFileFormatConst.SPUR_FILTER_COEFF + i)) {
                                    spurDetails.setFilterCoefficeint(Byte.parseByte(line.substring(line.indexOf(61) + 1)));
                                } else if (lineIsOfType(line, SpurFileFormatConst.SPUR_IS_ENABLE + i)) {
                                    spurDetails.setIsEnableSpur(Byte.parseByte(line.substring(line.indexOf(61) + 1)));
                                } else if (lineIsOfType(line, SpurFileFormatConst.SPUR_LEVEL + i)) {
                                    spurDetails.setSpurLevel(Byte.parseByte(line.substring(line.indexOf(61) + 1)));
                                }
                                j++;
                            }
                            spur.addSpurDetails(spurDetails);
                        }
                        t.InsertSpur(spur);
                        lastLine = LineType.EMPTY_LINE;
                    }
                }
            } catch (NumberFormatException e) {
                Log.d(TAG, "NumberFormatException");
                e.printStackTrace();
                return false;
            } catch (IOException e2) {
                Log.d(TAG, "IOException");
                e2.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private String removeSpaces(String s) {
        return SpurFileFormatConst.SPACE_PATTERN.matcher(s).replaceAll("");
    }

    /* JADX WARNING: Missing block: B:12:0x0022, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean lineIsOfType(String line, String lineType) {
        try {
            int indexEqual = line.indexOf(61);
            if (indexEqual < 0 || indexEqual >= line.length() || !line.startsWith(lineType)) {
                return false;
            }
            int num = Integer.parseInt(line.substring(indexEqual + 1));
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /* JADX WARNING: Missing block: B:4:0x0009, code:
            return true;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean lineIsComment(String s) {
        return s == null || s == "" || s == " " || s.length() == 0 || s.charAt(0) == SpurFileFormatConst.COMMENT;
    }

    public SpurTable GetSpurTable(String fileName) {
        IOException e;
        SpurTable t = new SpurTable();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
            try {
                parse(reader, t);
                reader.close();
                BufferedReader bufferedReader = reader;
            } catch (IOException e2) {
                e = e2;
                e.printStackTrace();
                return t;
            }
        } catch (IOException e3) {
            e = e3;
            e.printStackTrace();
            return t;
        }
        return t;
    }
}
