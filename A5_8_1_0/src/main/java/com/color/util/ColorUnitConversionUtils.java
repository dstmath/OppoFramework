package com.color.util;

import android.content.Context;
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
        if (country == null || (!country.equalsIgnoreCase("CN") && !country.equalsIgnoreCase("TW") && !country.equalsIgnoreCase("HK"))) {
            return false;
        }
        return true;
    }

    private String formatNumber(double number, String pointNum, boolean isRound) {
        DecimalFormat df = new DecimalFormat(pointNum, new DecimalFormatSymbols(Locale.CHINA));
        if (isRound) {
            df.setRoundingMode(RoundingMode.HALF_UP);
        } else {
            df.setRoundingMode(RoundingMode.FLOOR);
        }
        return df.format(number);
    }

    private String formatLocaleNumber(double number, String pointNum) {
        return new DecimalFormat(pointNum, new DecimalFormatSymbols(this.mContext.getResources().getConfiguration().locale)).format(number);
    }

    private String getChineseDownloadValue(long number) {
        double value;
        int temp;
        if (0 <= number && ((double) number) < 10000.0d) {
            if (number == 0) {
                number++;
            }
            return number + " ";
        } else if (10000.0d <= ((double) number) && ((double) number) < 100000.0d) {
            value = Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue();
            temp = (int) value;
            if (value == ((double) temp)) {
                return temp + this.mMoreDownLoad;
            }
            return value + this.mMoreDownLoad;
        } else if (100000.0d <= ((double) number) && ((double) number) < MILLION) {
            value = Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue();
            temp = (int) value;
            if (value == ((double) temp)) {
                return temp + this.mMoreDownLoad;
            }
            return value + this.mMoreDownLoad;
        } else if (MILLION <= ((double) number) && ((double) number) < 1.0E7d) {
            return ((int) Double.valueOf(formatNumber(((double) number) / 10000.0d, TWOPOINT, true)).doubleValue()) + this.mMoreDownLoad;
        } else if (1.0E7d <= ((double) number) && ((double) number) < 1.0E8d) {
            return ((int) Double.valueOf(formatNumber(((double) number) / 10000.0d, TWOPOINT, true)).doubleValue()) + this.mMoreDownLoad;
        } else if (((double) number) >= 1.0E8d) {
            return formatNumber(Double.valueOf(formatNumber(((double) number) / 1.0E8d, SIXPOINT, true)).doubleValue(), ONEPOINT, false) + this.mMostDownLoad;
        } else {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
    }

    private String getEnglishDownloadValue(long number) {
        String tempString;
        double value;
        int temp;
        if (0 <= number && ((double) number) < 10000.0d) {
            if (number == 0) {
                number++;
            }
            return number + " ";
        } else if (10000.0d <= ((double) number) && ((double) number) < 100000.0d) {
            return ((int) (TEN * Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue())) + this.mMoreDownLoad;
        } else if (100000.0d <= ((double) number) && ((double) number) < MILLION) {
            return ((int) (TEN * Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue())) + this.mMoreDownLoad;
        } else if (MILLION <= ((double) number) && ((double) number) < 1.0E7d) {
            tempString = formatNumber(((double) number) / 10000.0d, TWOPOINT, true);
            value = Double.valueOf(tempString).doubleValue() / HUNDRED;
            temp = (int) value;
            if (value == ((double) temp)) {
                return temp + this.mMostDownLoad;
            }
            return Double.valueOf(tempString) + this.mMostDownLoad;
        } else if (1.0E7d <= ((double) number) && ((double) number) < 1.0E8d) {
            tempString = formatNumber(((double) number) / 10000.0d, TWOPOINT, true);
            value = Double.valueOf(tempString).doubleValue() / HUNDRED;
            temp = (int) value;
            if (value == ((double) temp)) {
                return temp + this.mMostDownLoad;
            }
            return Double.valueOf(tempString) + this.mMostDownLoad;
        } else if (((double) number) >= 1.0E8d) {
            return ((int) (Double.valueOf(formatNumber(Double.valueOf(formatNumber(((double) number) / 1.0E8d, SIXPOINT, true)).doubleValue(), ONEPOINT, false)).doubleValue() * HUNDRED)) + this.mMostDownLoad;
        } else {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
    }

    private String getChineseStripValue(long number) {
        if (0 <= number && ((double) number) < 10000.0d) {
            return number + " ";
        }
        if (10000.0d <= ((double) number) && ((double) number) < MILLION) {
            double value = Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue();
            int temp = (int) value;
            if (value == ((double) temp)) {
                return temp + this.mMoreDownLoad;
            }
            return value + this.mMoreDownLoad;
        } else if (MILLION <= ((double) number) && ((double) number) < 1.0E8d) {
            return formatNumber(((double) number) / 10000.0d, NOPOINT, true) + this.mMoreDownLoad;
        } else {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
    }

    private String getEnglishStripValue(long number) {
        if (0 <= number && ((double) number) < 10000.0d) {
            return number + " ";
        }
        if (10000.0d <= ((double) number) && ((double) number) < MILLION) {
            return ((int) (Double.valueOf(formatNumber(((double) number) / 10000.0d, ONEPOINT, true)).doubleValue() * TEN)) + this.mMoreDownLoad;
        } else if (MILLION <= ((double) number) && ((double) number) < 1.0E8d) {
            return ((int) (Double.valueOf(formatNumber(((double) number) / 10000.0d, NOPOINT, true)).doubleValue() * TEN)) + this.mMoreDownLoad;
        } else {
            throw new IllegalArgumentException("the value of the incoming is wrong");
        }
    }

    public String getUnitValue(long number) throws IllegalArgumentException {
        String unitValue;
        String tempString;
        long temp;
        if (0 <= number && ((double) number) < THOUSAND) {
            tempString = formatNumber((double) number, NOPOINT, true);
            temp = Long.valueOf(tempString).longValue();
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
            if (THOUSAND <= ((double) temp) && ((double) temp) < SPECIAL) {
                return getUnitValue(temp);
            }
            unitValue = tempString + B;
        } else if (THOUSAND <= ((double) number) && ((double) number) < 1024000.0d) {
            tempString = formatNumber(((double) number) / SPECIAL, NOPOINT, true);
            temp = Long.valueOf(tempString).longValue() * 1024;
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
            if (1024000.0d <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
                return getUnitValue(temp);
            }
            unitValue = tempString + KB;
        } else if (1024000.0d <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
            tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 2.0d), ONEPOINT, true);
            temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 2.0d));
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
            if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
                return getUnitValue(temp);
            }
            unitValue = tempString + MB;
        } else if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
            tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 2.0d), NOPOINT, true);
            temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 2.0d));
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
            if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 3.0d)) {
                return getUnitValue(temp);
            }
            unitValue = tempString + MB;
        } else if (Math.pow(SPECIAL, 2.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 3.0d)) {
            if (Math.pow(SPECIAL, 3.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 3.0d) * TEN) {
                tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), TWOPOINT, true);
                temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 3.0d));
                tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), TWOPOINT);
                if (Math.pow(SPECIAL, 3.0d) * TEN <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                    return getUnitValue(temp);
                }
                unitValue = tempString + GB;
            } else if (Math.pow(SPECIAL, 3.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), ONEPOINT, true);
                temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 3.0d));
                tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
                if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                    return getUnitValue(temp);
                }
                unitValue = tempString + GB;
            } else if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), NOPOINT, true);
                temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 3.0d));
                tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
                if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 4.0d)) {
                    return getUnitValue(temp);
                }
                unitValue = tempString + GB;
            } else if (Math.pow(SPECIAL, 3.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 4.0d)) {
                if (Math.pow(SPECIAL, 4.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * TEN) {
                    tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), TWOPOINT, true);
                    temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 4.0d));
                    tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), TWOPOINT);
                    if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                        return getUnitValue(temp);
                    }
                    unitValue = tempString + TB;
                } else if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                    tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), ONEPOINT, true);
                    temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 4.0d));
                    tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
                    if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                        return getUnitValue(temp);
                    }
                    unitValue = tempString + TB;
                } else if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                    tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), NOPOINT, true);
                    temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 4.0d));
                    tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
                    if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 5.0d)) {
                        return getUnitValue(temp);
                    }
                    unitValue = tempString + TB;
                } else if (Math.pow(SPECIAL, 4.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d)) {
                    if (Math.pow(SPECIAL, 5.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * TEN) {
                        tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), TWOPOINT, true);
                        temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 5.0d));
                        tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), TWOPOINT);
                        if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                            return getUnitValue(temp);
                        }
                        unitValue = tempString + PB;
                    } else if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                        tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), ONEPOINT, true);
                        temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 5.0d));
                        tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
                        if (Math.pow(SPECIAL, 5.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                            return getUnitValue(temp);
                        }
                        unitValue = tempString + PB;
                    } else if (Math.pow(SPECIAL, 5.0d) * HUNDRED > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                        throw new IllegalArgumentException("the value of the incoming is wrong");
                    } else {
                        unitValue = formatLocaleNumber(((double) number) / Math.pow(SPECIAL, 5.0d), NOPOINT) + PB;
                    }
                } else if (((double) number) > Math.pow(SPECIAL, 4.0d) * 1023.0d) {
                    return getUnitValue((long) Math.pow(SPECIAL, 5.0d));
                } else {
                    unitValue = this.mSpecialPoint + PB;
                }
            } else if (((double) number) > Math.pow(SPECIAL, 3.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 4.0d));
            } else {
                unitValue = this.mSpecialPoint + TB;
            }
        } else if (((double) number) > Math.pow(SPECIAL, 2.0d) * 1023.0d) {
            return getUnitValue((long) Math.pow(SPECIAL, 3.0d));
        } else {
            unitValue = this.mSpecialPoint + GB;
        }
        return unitValue;
    }

    public String getSpeedValue(long number) throws IllegalArgumentException {
        String speedValue;
        String tempString;
        long temp;
        if (0 <= number && ((double) number) < THOUSAND) {
            tempString = formatNumber((double) number, NOPOINT, true);
            temp = Long.valueOf(tempString).longValue();
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
            if (THOUSAND <= ((double) temp) && ((double) temp) < SPECIAL) {
                return getUnitValue(temp);
            }
            speedValue = tempString + B_S;
        } else if (THOUSAND <= ((double) number) && ((double) number) < 1024000.0d) {
            tempString = formatNumber(((double) number) / SPECIAL, NOPOINT, true);
            temp = Long.valueOf(tempString).longValue() * 1024;
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
            if (1024000.0d <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
                return getUnitValue(temp);
            }
            speedValue = tempString + KB_S;
        } else if (1024000.0d <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 2.0d) * HUNDRED) {
            tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 2.0d), ONEPOINT, true);
            temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 2.0d));
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
            if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
                return getUnitValue(temp);
            }
            speedValue = tempString + MB_S;
        } else if (Math.pow(SPECIAL, 2.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 2.0d) * THOUSAND) {
            tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 2.0d), NOPOINT, true);
            temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 2.0d));
            tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
            if (Math.pow(SPECIAL, 2.0d) * THOUSAND <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 3.0d)) {
                return getUnitValue(temp);
            }
            speedValue = tempString + MB_S;
        } else if (Math.pow(SPECIAL, 2.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 3.0d)) {
            if (Math.pow(SPECIAL, 3.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 3.0d) * TEN) {
                tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), TWOPOINT, true);
                temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 3.0d));
                tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), TWOPOINT);
                if (Math.pow(SPECIAL, 3.0d) * TEN <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                    return getUnitValue(temp);
                }
                speedValue = tempString + GB_S;
            } else if (Math.pow(SPECIAL, 3.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 3.0d) * HUNDRED) {
                tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), ONEPOINT, true);
                temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 3.0d));
                tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
                if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                    return getUnitValue(temp);
                }
                speedValue = tempString + GB_S;
            } else if (Math.pow(SPECIAL, 3.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 3.0d) * THOUSAND) {
                tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 3.0d), NOPOINT, true);
                temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 3.0d));
                tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), NOPOINT);
                if (Math.pow(SPECIAL, 3.0d) * THOUSAND <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 4.0d)) {
                    return getUnitValue(temp);
                }
                speedValue = tempString + GB_S;
            } else if (Math.pow(SPECIAL, 3.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 4.0d)) {
                if (Math.pow(SPECIAL, 4.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * TEN) {
                    tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), TWOPOINT, true);
                    temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 4.0d));
                    tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), TWOPOINT);
                    if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                        return getUnitValue(temp);
                    }
                    speedValue = tempString + TB_S;
                } else if (Math.pow(SPECIAL, 4.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * HUNDRED) {
                    tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), ONEPOINT, true);
                    temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 4.0d));
                    tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
                    if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                        return getUnitValue(temp);
                    }
                    speedValue = tempString + TB_S;
                } else if (Math.pow(SPECIAL, 4.0d) * HUNDRED <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 4.0d) * THOUSAND) {
                    tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 4.0d), NOPOINT, true);
                    temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 4.0d));
                    if (Math.pow(SPECIAL, 4.0d) * THOUSAND <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 5.0d)) {
                        return getUnitValue(temp);
                    }
                    speedValue = tempString + TB_S;
                } else if (Math.pow(SPECIAL, 4.0d) * THOUSAND > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d)) {
                    if (Math.pow(SPECIAL, 5.0d) <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * TEN) {
                        tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), TWOPOINT, true);
                        temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 5.0d));
                        tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), TWOPOINT);
                        if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                            return getUnitValue(temp);
                        }
                        speedValue = tempString + PB_S;
                    } else if (Math.pow(SPECIAL, 5.0d) * TEN <= ((double) number) && ((double) number) < Math.pow(SPECIAL, 5.0d) * HUNDRED) {
                        tempString = formatNumber(((double) number) / Math.pow(SPECIAL, 5.0d), ONEPOINT, true);
                        temp = (long) (Double.valueOf(tempString).doubleValue() * Math.pow(SPECIAL, 5.0d));
                        tempString = formatLocaleNumber(Double.valueOf(tempString).doubleValue(), ONEPOINT);
                        if (Math.pow(SPECIAL, 5.0d) * HUNDRED <= ((double) temp) && ((double) temp) < Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                            return getUnitValue(temp);
                        }
                        speedValue = tempString + PB_S;
                    } else if (Math.pow(SPECIAL, 5.0d) * HUNDRED > ((double) number) || ((double) number) >= Math.pow(SPECIAL, 5.0d) * THOUSAND) {
                        throw new IllegalArgumentException("the value of the incoming is wrong");
                    } else {
                        speedValue = formatLocaleNumber(((double) number) / Math.pow(SPECIAL, 5.0d), NOPOINT) + PB_S;
                    }
                } else if (((double) number) > Math.pow(SPECIAL, 4.0d) * 1023.0d) {
                    return getUnitValue((long) Math.pow(SPECIAL, 5.0d));
                } else {
                    speedValue = this.mSpecialPoint + PB_S;
                }
            } else if (((double) number) > Math.pow(SPECIAL, 3.0d) * 1023.0d) {
                return getUnitValue((long) Math.pow(SPECIAL, 4.0d));
            } else {
                speedValue = this.mSpecialPoint + TB_S;
            }
        } else if (((double) number) > Math.pow(SPECIAL, 2.0d) * 1023.0d) {
            return getUnitValue((long) Math.pow(SPECIAL, 3.0d));
        } else {
            speedValue = this.mSpecialPoint + GB_S;
        }
        return speedValue;
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
