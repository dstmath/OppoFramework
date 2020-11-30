package com.color.util;

import android.content.Context;
import android.net.wifi.WifiEnterpriseConfig;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class ColorUnitConversionUtils {
    private static final String B = " B";
    private static final String B_S = " B/s";
    private static final String GB = " GB";
    private static final String GB_S = " GB/s";
    private static final double HUNDRED = 100.0d;
    private static final String KB = " KB";
    private static final String KB_S = " KB/s";
    private static final String MB = " MB";
    private static final String MB_S = " MB/s";
    private static final double MILLION = 1000000.0d;
    private static final String NOPOINT = "0";
    private static final String ONEPOINT = "0.0";
    private static final String PB = " PB";
    private static final String PB_S = " PB/s";
    private static final String SIXPOINT = "0.00000";
    private static final double SPECIAL = 1024.0d;
    private static final int SQUARE_FIVE = 5;
    private static final int SQUARE_FOUR = 4;
    private static final int SQUARE_THREE = 3;
    private static final String TAG = "ColorUnitConversionUtils";
    private static final String TB = " TB";
    private static final String TB_S = " TB/s";
    private static final double TEN = 10.0d;
    private static final double THOUSAND = 1000.0d;
    private static final String TWOPOINT = "0.00";
    private Context mContext;
    private String mMoreDownLoad = null;
    private String mMostDownLoad = null;
    private String mSpecialPoint = "0.98";

    public ColorUnitConversionUtils(Context context) {
        this.mContext = context;
        this.mMoreDownLoad = context.getResources().getString(201590126);
        this.mMostDownLoad = context.getResources().getString(201590127);
        this.mSpecialPoint = formatLocaleNumber(0.98d, TWOPOINT);
    }

    private boolean isChinese() {
        String country = this.mContext.getResources().getConfiguration().locale.getCountry();
        if (country == null) {
            return false;
        }
        if (country.equalsIgnoreCase("CN") || country.equalsIgnoreCase("TW") || country.equalsIgnoreCase("HK")) {
            return true;
        }
        return false;
    }

    private String formatNumber(double number, String pointNum, boolean isRound) {
        DecimalFormat df = new DecimalFormat(pointNum, new DecimalFormatSymbols(Locale.CHINA));
        if (!isRound) {
            df.setRoundingMode(RoundingMode.FLOOR);
        } else {
            df.setRoundingMode(RoundingMode.HALF_UP);
        }
        return df.format(number);
    }

    private String formatLocaleNumber(double number, String pointNum) {
        return new DecimalFormat(pointNum, new DecimalFormatSymbols(this.mContext.getResources().getConfiguration().locale)).format(number);
    }

    private String getChineseDownloadValue(long number) {
        if (0 <= number && ((double) number) < 10000.0d) {
            if (number == 0) {
                number++;
            }
            return number + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        } else if (10000.0d <= ((double) number) && ((double) number) < 100000.0d) {
            double value = Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue();
            int temp = (int) value;
            if (value == ((double) temp)) {
                return temp + this.mMoreDownLoad;
            }
            return value + this.mMoreDownLoad;
        } else if (100000.0d <= ((double) number) && ((double) number) < MILLION) {
            double value2 = Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue();
            int temp2 = (int) value2;
            if (value2 == ((double) temp2)) {
                return temp2 + this.mMoreDownLoad;
            }
            return value2 + this.mMoreDownLoad;
        } else if (MILLION <= ((double) number) && ((double) number) < 1.0E7d) {
            double value3 = Double.valueOf(formatNumber(((double) number) / 10000.0d, TWOPOINT, true)).doubleValue();
            return ((int) value3) + this.mMoreDownLoad;
        } else if (1.0E7d <= ((double) number) && ((double) number) < 1.0E8d) {
            double value4 = Double.valueOf(formatNumber(((double) number) / 10000.0d, TWOPOINT, true)).doubleValue();
            return ((int) value4) + this.mMoreDownLoad;
        } else if (((double) number) >= 1.0E8d) {
            double value5 = Double.valueOf(formatNumber(((double) number) / 1.0E8d, SIXPOINT, true)).doubleValue();
            return formatNumber(value5, ONEPOINT, false) + this.mMostDownLoad;
        } else {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
    }

    private String getEnglishDownloadValue(long number) {
        long number2 = number;
        if (0 <= number2 && ((double) number2) < 10000.0d) {
            if (number2 == 0) {
                number2++;
            }
            return number2 + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        } else if (10000.0d <= ((double) number2) && ((double) number2) < 100000.0d) {
            double value = Double.valueOf(formatNumber(((double) number2) / 10000.0d, ONEPOINT, true)).doubleValue();
            return ((int) (TEN * value)) + this.mMoreDownLoad;
        } else if (100000.0d <= ((double) number2) && ((double) number2) < MILLION) {
            double value2 = Double.valueOf(formatNumber(((double) number2) / 10000.0d, ONEPOINT, true)).doubleValue();
            return ((int) (TEN * value2)) + this.mMoreDownLoad;
        } else if (MILLION <= ((double) number2) && ((double) number2) < 1.0E7d) {
            String tempString = formatNumber(((double) number2) / 10000.0d, TWOPOINT, true);
            double value3 = Double.valueOf(tempString).doubleValue() / HUNDRED;
            int temp = (int) value3;
            if (value3 == ((double) temp)) {
                return temp + this.mMostDownLoad;
            }
            return Double.valueOf(tempString) + this.mMostDownLoad;
        } else if (1.0E7d <= ((double) number2) && ((double) number2) < 1.0E8d) {
            String tempString2 = formatNumber(((double) number2) / 10000.0d, TWOPOINT, true);
            double value4 = Double.valueOf(tempString2).doubleValue() / HUNDRED;
            int temp2 = (int) value4;
            if (value4 == ((double) temp2)) {
                return temp2 + this.mMostDownLoad;
            }
            return Double.valueOf(tempString2) + this.mMostDownLoad;
        } else if (((double) number2) >= 1.0E8d) {
            double value5 = Double.valueOf(formatNumber(((double) number2) / 1.0E8d, SIXPOINT, true)).doubleValue();
            return ((int) (Double.valueOf(formatNumber(value5, ONEPOINT, false)).doubleValue() * HUNDRED)) + this.mMostDownLoad;
        } else {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
    }

    private String getChineseStripValue(long number) {
        if (0 <= number && ((double) number) < 10000.0d) {
            return number + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        } else if (10000.0d <= ((double) number) && ((double) number) < MILLION) {
            double value = Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue();
            int temp = (int) value;
            if (value == ((double) temp)) {
                return temp + this.mMoreDownLoad;
            }
            return value + this.mMoreDownLoad;
        } else if (MILLION > ((double) number) || ((double) number) >= 1.0E8d) {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        } else {
            return formatNumber(((double) number) / 10000.0d, "0", true) + this.mMoreDownLoad;
        }
    }

    private String getEnglishStripValue(long number) {
        if (0 <= number && ((double) number) < 10000.0d) {
            return number + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER;
        } else if (10000.0d <= ((double) number) && ((double) number) < MILLION) {
            double value = Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue();
            return ((int) (TEN * value)) + this.mMoreDownLoad;
        } else if (MILLION > ((double) number) || ((double) number) >= 1.0E8d) {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        } else {
            return ((int) (Double.valueOf(formatNumber(((double) number) / 10000.0d, "0", true)).doubleValue() * TEN)) + this.mMoreDownLoad;
        }
    }

    public String getUnitValue(long number) {
        return getTransformUnitValue(number, SPECIAL);
    }

    public String getUnitThousandValue(long number) {
        return getTransformUnitValue(number, THOUSAND);
    }

    /* JADX INFO: Multiple debug info for r5v28 java.lang.String: [D('tempString' java.lang.String), D('unitValue' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v49 java.lang.String: [D('tempString' java.lang.String), D('unitValue' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v56 java.lang.String: [D('tempString' java.lang.String), D('unitValue' java.lang.String)] */
    /* JADX INFO: Multiple debug info for r5v63 java.lang.String: [D('tempString' java.lang.String), D('unitValue' java.lang.String)] */
    public String getTransformUnitValue(long number, double unit) throws IllegalArgumentException {
        double d;
        String unitValue;
        String str;
        String str2;
        if (0 <= number && ((double) number) < THOUSAND) {
            String tempString = formatNumber((double) number, "0", true);
            long temp = Long.valueOf(tempString).longValue();
            String tempString2 = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), "0");
            if (THOUSAND <= ((double) temp) && ((double) temp) < SPECIAL) {
                return getUnitValue(temp);
            }
            return tempString2 + B;
        } else if (THOUSAND <= ((double) number) && ((double) number) < 1024000.0d) {
            String tempString3 = formatNumber(((double) number) / unit, "0", true);
            long temp2 = Long.valueOf(tempString3).longValue() * ((long) unit);
            String tempString4 = formatLocaleNumber(Double.valueOf(tempString3).doubleValue(), "0");
            if (1024000.0d <= ((double) temp2) && ((double) temp2) < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
                return getTransformUnitValue(temp2, unit);
            }
            return tempString4 + KB;
        } else if (1024000.0d > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 2.0d) * HUNDRED) {
            if (Math.pow(SPECIAL, 2.0d) * HUNDRED > ((double) number)) {
                d = 3.0d;
            } else if (((double) number) < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
                String tempString5 = formatNumber(((double) number) / Math.pow(unit, 2.0d), "0", true);
                long temp3 = (long) (Double.valueOf(tempString5).doubleValue() * Math.pow(unit, 2.0d));
                String tempString6 = formatLocaleNumber(Double.valueOf(tempString5).doubleValue(), "0");
                if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= ((double) temp3) && ((double) temp3) < Math.pow(SPECIAL, 3.0d)) {
                    return getTransformUnitValue(temp3, unit);
                }
                return tempString6 + MB;
            } else {
                d = 3.0d;
            }
            if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= ((double) number)) {
                str = "0";
                if (((double) number) < Math.pow(SPECIAL, d)) {
                    if (unit == THOUSAND) {
                        String tempString7 = formatNumber(((double) number) / Math.pow(unit, d), TWOPOINT, true);
                        long doubleValue = (long) (Double.valueOf(tempString7).doubleValue() * Math.pow(unit, d));
                        return formatLocaleNumber(Double.valueOf(tempString7).doubleValue(), TWOPOINT) + GB;
                    } else if (unit != SPECIAL) {
                        unitValue = null;
                        return unitValue;
                    } else if (((double) number) > Math.pow(SPECIAL, 2.0d) * 1023.0d) {
                        return getUnitValue((long) Math.pow(SPECIAL, 3.0d));
                    } else {
                        return this.mSpecialPoint + GB;
                    }
                }
            } else {
                str = "0";
            }
            if (Math.pow(SPECIAL, 3.0d) <= ((double) number)) {
                unitValue = null;
                if (((double) number) < Math.pow(SPECIAL, 3.0d) * TEN) {
                    String tempString8 = formatNumber(((double) number) / Math.pow(unit, 3.0d), TWOPOINT, true);
                    long temp4 = (long) (Double.valueOf(tempString8).doubleValue() * Math.pow(unit, 3.0d));
                    String tempString9 = formatLocaleNumber(Double.valueOf(tempString8).doubleValue(), TWOPOINT);
                    if (Math.pow(SPECIAL, 3.0d) * TEN <= ((double) temp4) && ((double) temp4) < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                        return getTransformUnitValue(temp4, unit);
                    }
                    return tempString9 + GB;
                }
            } else {
                unitValue = null;
            }
            if (Math.pow(SPECIAL, 3.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                String tempString10 = formatNumber(((double) number) / Math.pow(unit, 3.0d), ONEPOINT, true);
                long temp5 = (long) (Double.valueOf(tempString10).doubleValue() * Math.pow(unit, 3.0d));
                String tempString11 = formatLocaleNumber(Double.valueOf(tempString10).doubleValue(), ONEPOINT);
                if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= ((double) temp5) && ((double) temp5) < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                    return getTransformUnitValue(temp5, unit);
                }
                return tempString11 + GB;
            } else if (Math.pow(SPECIAL, 3.0d) * HUNDRED > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= ((double) number)) {
                    str2 = str;
                    if (((double) number) < Math.pow(SPECIAL, 4.0d)) {
                        if (unit == THOUSAND) {
                            String tempString12 = formatNumber(((double) number) / Math.pow(unit, 4.0d), TWOPOINT, true);
                            long doubleValue2 = (long) (Double.valueOf(tempString12).doubleValue() * Math.pow(unit, 4.0d));
                            return formatLocaleNumber(Double.valueOf(tempString12).doubleValue(), TWOPOINT) + TB;
                        }
                        if (unit == SPECIAL) {
                            if (((double) number) > Math.pow(SPECIAL, 3.0d) * 1023.0d) {
                                return getUnitValue((long) Math.pow(SPECIAL, 4.0d));
                            }
                            return this.mSpecialPoint + TB;
                        }
                        return unitValue;
                    }
                } else {
                    str2 = str;
                }
                if (Math.pow(SPECIAL, 4.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * TEN) {
                    String tempString13 = formatNumber(((double) number) / Math.pow(unit, 4.0d), TWOPOINT, true);
                    long temp6 = (long) (Double.valueOf(tempString13).doubleValue() * Math.pow(unit, 4.0d));
                    String tempString14 = formatLocaleNumber(Double.valueOf(tempString13).doubleValue(), TWOPOINT);
                    if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) temp6) && ((double) temp6) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                        return getTransformUnitValue(temp6, unit);
                    }
                    return tempString14 + TB;
                } else if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                    String tempString15 = formatNumber(((double) number) / Math.pow(unit, 4.0d), ONEPOINT, true);
                    long temp7 = (long) (Double.valueOf(tempString15).doubleValue() * Math.pow(unit, 4.0d));
                    String tempString16 = formatLocaleNumber(Double.valueOf(tempString15).doubleValue(), ONEPOINT);
                    if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) temp7) && ((double) temp7) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                        return getTransformUnitValue(temp7, unit);
                    }
                    return tempString16 + TB;
                } else if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                    String tempString17 = formatNumber(((double) number) / Math.pow(unit, 4.0d), str2, true);
                    long temp8 = (long) (Double.valueOf(tempString17).doubleValue() * Math.pow(unit, 4.0d));
                    String tempString18 = formatLocaleNumber(Double.valueOf(tempString17).doubleValue(), str2);
                    if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= ((double) temp8) && ((double) temp8) < Math.pow(SPECIAL, 5.0d)) {
                        return getTransformUnitValue(temp8, unit);
                    }
                    return tempString18 + TB;
                } else if (Math.pow(SPECIAL, 4.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d)) {
                    if (Math.pow(SPECIAL, 5.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * TEN) {
                        String tempString19 = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), TWOPOINT, true);
                        long temp9 = (long) (Double.valueOf(tempString19).doubleValue() * Math.pow(SPECIAL, 5.0d));
                        String tempString20 = formatLocaleNumber(Double.valueOf(tempString19).doubleValue(), TWOPOINT);
                        if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) temp9) && ((double) temp9) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                            return getUnitValue(temp9);
                        }
                        return tempString20 + PB;
                    } else if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                        String tempString21 = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), ONEPOINT, true);
                        long temp10 = (long) (Double.valueOf(tempString21).doubleValue() * Math.pow(SPECIAL, 5.0d));
                        String tempString22 = formatLocaleNumber(Double.valueOf(tempString21).doubleValue(), ONEPOINT);
                        if (Math.pow(SPECIAL, 5.0d) * HUNDRED <= ((double) temp10) && ((double) temp10) < Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                            return getUnitValue(temp10);
                        }
                        return tempString22 + PB;
                    } else if (Math.pow(SPECIAL, 5.0d) * HUNDRED > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                        throw new IllegalArgumentException("the value of the incoming is wrong");
                    } else {
                        return formatLocaleNumber(((double) number) / Math.pow(SPECIAL, 5.0d), str2) + PB;
                    }
                } else if (unit == THOUSAND) {
                    String tempString23 = formatNumber(((double) number) / Math.pow(unit, 5.0d), TWOPOINT, true);
                    long doubleValue3 = (long) (Double.valueOf(tempString23).doubleValue() * Math.pow(unit, 5.0d));
                    return formatLocaleNumber(Double.valueOf(tempString23).doubleValue(), TWOPOINT) + PB;
                } else {
                    if (unit == SPECIAL) {
                        if (((double) number) > Math.pow(SPECIAL, 4.0d) * 1023.0d) {
                            return getUnitValue((long) Math.pow(SPECIAL, 5.0d));
                        }
                        return this.mSpecialPoint + PB;
                    }
                    return unitValue;
                }
            } else {
                String tempString24 = formatNumber(((double) number) / Math.pow(unit, 3.0d), str, true);
                long temp11 = (long) (Double.valueOf(tempString24).doubleValue() * Math.pow(unit, 3.0d));
                String tempString25 = formatLocaleNumber(Double.valueOf(tempString24).doubleValue(), str);
                if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= ((double) temp11) && ((double) temp11) < Math.pow(SPECIAL, 4.0d)) {
                    return getTransformUnitValue(temp11, unit);
                }
                return tempString25 + GB;
            }
        } else {
            String tempString26 = formatNumber(((double) number) / Math.pow(unit, 2.0d), ONEPOINT, true);
            long temp12 = (long) (Double.valueOf(tempString26).doubleValue() * Math.pow(unit, 2.0d));
            String tempString27 = formatLocaleNumber(Double.valueOf(tempString26).doubleValue(), ONEPOINT);
            if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= ((double) temp12) && ((double) temp12) < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
                return getTransformUnitValue(temp12, unit);
            }
            return tempString27 + MB;
        }
    }

    public String getSpeedValue(long number) throws IllegalArgumentException {
        String str;
        String str2;
        String str3;
        if (0 <= number && ((double) number) < THOUSAND) {
            String tempString = formatNumber((double) number, "0", true);
            long temp = Long.valueOf(tempString).longValue();
            String tempString2 = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), "0");
            if (THOUSAND <= ((double) temp) && ((double) temp) < SPECIAL) {
                return getUnitValue(temp);
            }
            return tempString2 + B_S;
        } else if (THOUSAND <= ((double) number) && ((double) number) < 1024000.0d) {
            String tempString3 = formatNumber(((double) number) / SPECIAL, "0", true);
            long temp2 = Long.valueOf(tempString3).longValue() * 1024;
            String tempString4 = formatLocaleNumber(Double.valueOf(tempString3).doubleValue(), "0");
            if (1024000.0d <= ((double) temp2) && ((double) temp2) < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
                return getUnitValue(temp2);
            }
            return tempString4 + KB_S;
        } else if (1024000.0d <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
            String tempString5 = formatNumber(((double) number) / Math.pow(SPECIAL, 2.0d), ONEPOINT, true);
            long temp3 = (long) (Double.valueOf(tempString5).doubleValue() * Math.pow(SPECIAL, 2.0d));
            String tempString6 = formatLocaleNumber(Double.valueOf(tempString5).doubleValue(), ONEPOINT);
            if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= ((double) temp3) && ((double) temp3) < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
                return getUnitValue(temp3);
            }
            return tempString6 + MB_S;
        } else if (Math.pow(SPECIAL, 2.0d) * HUNDRED > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 2.0d) * THOUSAND) {
            if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= ((double) number)) {
                str = "0";
                if (((double) number) < Math.pow(SPECIAL, 3.0d)) {
                    if (((double) number) > Math.pow(SPECIAL, 2.0d) * 1023.0d) {
                        return getUnitValue((long) Math.pow(SPECIAL, 3.0d));
                    }
                    return this.mSpecialPoint + GB_S;
                }
            } else {
                str = "0";
            }
            if (Math.pow(SPECIAL, 3.0d) > ((double) number)) {
                str2 = GB_S;
            } else if (((double) number) < Math.pow(SPECIAL, 3.0d) * TEN) {
                String tempString7 = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), TWOPOINT, true);
                long temp4 = (long) (Double.valueOf(tempString7).doubleValue() * Math.pow(SPECIAL, 3.0d));
                String tempString8 = formatLocaleNumber(Double.valueOf(tempString7).doubleValue(), TWOPOINT);
                if (Math.pow(SPECIAL, 3.0d) * TEN <= ((double) temp4) && ((double) temp4) < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                    return getUnitValue(temp4);
                }
                return tempString8 + GB_S;
            } else {
                str2 = GB_S;
            }
            if (Math.pow(SPECIAL, 3.0d) * TEN > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= ((double) number)) {
                    str3 = ONEPOINT;
                    if (((double) number) < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                        String tempString9 = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), str, true);
                        long temp5 = (long) (Double.valueOf(tempString9).doubleValue() * Math.pow(SPECIAL, 3.0d));
                        String tempString10 = formatLocaleNumber(Double.valueOf(tempString9).doubleValue(), str);
                        if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= ((double) temp5) && ((double) temp5) < Math.pow(SPECIAL, 4.0d)) {
                            return getUnitValue(temp5);
                        }
                        return tempString10 + str2;
                    }
                } else {
                    str3 = ONEPOINT;
                }
                if (Math.pow(SPECIAL, 3.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 4.0d)) {
                    if (Math.pow(SPECIAL, 4.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * TEN) {
                        String tempString11 = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), TWOPOINT, true);
                        long temp6 = (long) (Double.valueOf(tempString11).doubleValue() * Math.pow(SPECIAL, 4.0d));
                        String tempString12 = formatLocaleNumber(Double.valueOf(tempString11).doubleValue(), TWOPOINT);
                        if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) temp6) && ((double) temp6) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                            return getUnitValue(temp6);
                        }
                        return tempString12 + TB_S;
                    } else if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                        String tempString13 = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), str3, true);
                        long temp7 = (long) (Double.valueOf(tempString13).doubleValue() * Math.pow(SPECIAL, 4.0d));
                        String tempString14 = formatLocaleNumber(Double.valueOf(tempString13).doubleValue(), str3);
                        if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) temp7) && ((double) temp7) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                            return getUnitValue(temp7);
                        }
                        return tempString14 + TB_S;
                    } else if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                        String tempString15 = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), str, true);
                        long temp8 = (long) (Double.valueOf(tempString15).doubleValue() * Math.pow(SPECIAL, 4.0d));
                        if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= ((double) temp8) && ((double) temp8) < Math.pow(SPECIAL, 5.0d)) {
                            return getUnitValue(temp8);
                        }
                        return tempString15 + TB_S;
                    } else if (Math.pow(SPECIAL, 4.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d)) {
                        if (Math.pow(SPECIAL, 5.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * TEN) {
                            String tempString16 = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), TWOPOINT, true);
                            long temp9 = (long) (Double.valueOf(tempString16).doubleValue() * Math.pow(SPECIAL, 5.0d));
                            String tempString17 = formatLocaleNumber(Double.valueOf(tempString16).doubleValue(), TWOPOINT);
                            if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) temp9) && ((double) temp9) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                                return getUnitValue(temp9);
                            }
                            return tempString17 + PB_S;
                        } else if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                            String tempString18 = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), str3, true);
                            long temp10 = (long) (Double.valueOf(tempString18).doubleValue() * Math.pow(SPECIAL, 5.0d));
                            String tempString19 = formatLocaleNumber(Double.valueOf(tempString18).doubleValue(), str3);
                            if (Math.pow(SPECIAL, 5.0d) * HUNDRED <= ((double) temp10) && ((double) temp10) < Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                                return getUnitValue(temp10);
                            }
                            return tempString19 + PB_S;
                        } else if (Math.pow(SPECIAL, 5.0d) * HUNDRED > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                            throw new IllegalArgumentException("the value of the incoming is wrong");
                        } else {
                            return formatLocaleNumber(((double) number) / Math.pow(SPECIAL, 5.0d), str) + PB_S;
                        }
                    } else if (((double) number) > Math.pow(SPECIAL, 4.0d) * 1023.0d) {
                        return getUnitValue((long) Math.pow(SPECIAL, 5.0d));
                    } else {
                        return this.mSpecialPoint + PB_S;
                    }
                } else if (((double) number) > Math.pow(SPECIAL, 3.0d) * 1023.0d) {
                    return getUnitValue((long) Math.pow(SPECIAL, 4.0d));
                } else {
                    return this.mSpecialPoint + TB_S;
                }
            } else {
                String tempString20 = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), ONEPOINT, true);
                long temp11 = (long) (Double.valueOf(tempString20).doubleValue() * Math.pow(SPECIAL, 3.0d));
                String tempString21 = formatLocaleNumber(Double.valueOf(tempString20).doubleValue(), ONEPOINT);
                if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= ((double) temp11) && ((double) temp11) < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                    return getUnitValue(temp11);
                }
                return tempString21 + str2;
            }
        } else {
            String tempString22 = formatNumber(((double) number) / Math.pow(SPECIAL, 2.0d), "0", true);
            long temp12 = (long) (Double.valueOf(tempString22).doubleValue() * Math.pow(SPECIAL, 2.0d));
            String tempString23 = formatLocaleNumber(Double.valueOf(tempString22).doubleValue(), "0");
            if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= ((double) temp12) && ((double) temp12) < Math.pow(SPECIAL, 3.0d)) {
                return getUnitValue(temp12);
            }
            return tempString23 + MB_S;
        }
    }

    public String getDownLoadValue(long number) throws IllegalArgumentException {
        if (isChinese()) {
            return getChineseDownloadValue(number);
        }
        return getEnglishDownloadValue(number);
    }

    public String getStripValue(long number) throws IllegalArgumentException {
        if (isChinese()) {
            return getChineseStripValue(number);
        }
        return getEnglishStripValue(number);
    }
}
