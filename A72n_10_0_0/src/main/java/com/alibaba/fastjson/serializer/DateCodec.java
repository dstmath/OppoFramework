package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.parser.deserializer.AbstractDateDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.IOUtils;
import com.alibaba.fastjson.util.TypeUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateCodec extends AbstractDateDeserializer implements ObjectDeserializer, ObjectSerializer {
    public static final DateCodec instance = new DateCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        Date date;
        char[] buf;
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        Class<?> clazz = object.getClass();
        if (clazz == java.sql.Date.class) {
            long millis = ((java.sql.Date) object).getTime();
            if (millis % ((long) serializer.timeZone.getOffset(millis)) == 0) {
                out.writeString(object.toString());
                return;
            }
        }
        if (clazz != Time.class || ((Time) object).getTime() >= 86400000) {
            if (object instanceof Date) {
                date = (Date) object;
            } else {
                date = TypeUtils.castToDate(object);
            }
            if (out.isEnabled(SerializerFeature.WriteDateUseDateFormat)) {
                DateFormat format = serializer.getDateFormat();
                if (format == null) {
                    format = new SimpleDateFormat(JSON.DEFFAULT_DATE_FORMAT, serializer.locale);
                    format.setTimeZone(serializer.timeZone);
                }
                out.writeString(format.format(date));
                return;
            }
            if (out.isEnabled(SerializerFeature.WriteClassName)) {
                if (clazz != fieldType) {
                    if (clazz == Date.class) {
                        out.write("new Date(");
                        out.writeLong(((Date) object).getTime());
                        out.write(41);
                        return;
                    }
                    out.write(123);
                    out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
                    serializer.write(clazz.getName());
                    out.writeFieldValue(',', "val", ((Date) object).getTime());
                    out.write(125);
                    return;
                }
            }
            long time = date.getTime();
            if (out.isEnabled(SerializerFeature.UseISO8601DateFormat)) {
                char quote = out.isEnabled(SerializerFeature.UseSingleQuotes) ? '\'' : '\"';
                out.write(quote);
                Calendar calendar = Calendar.getInstance(serializer.timeZone, serializer.locale);
                calendar.setTimeInMillis(time);
                int year = calendar.get(1);
                int month = calendar.get(2) + 1;
                int day = calendar.get(5);
                int hour = calendar.get(11);
                int minute = calendar.get(12);
                int second = calendar.get(13);
                int millis2 = calendar.get(14);
                if (millis2 != 0) {
                    buf = "0000-00-00T00:00:00.000".toCharArray();
                    IOUtils.getChars(millis2, 23, buf);
                    IOUtils.getChars(second, 19, buf);
                    IOUtils.getChars(minute, 16, buf);
                    IOUtils.getChars(hour, 13, buf);
                    IOUtils.getChars(day, 10, buf);
                    IOUtils.getChars(month, 7, buf);
                    IOUtils.getChars(year, 4, buf);
                } else if (second == 0 && minute == 0 && hour == 0) {
                    buf = "0000-00-00".toCharArray();
                    IOUtils.getChars(day, 10, buf);
                    IOUtils.getChars(month, 7, buf);
                    IOUtils.getChars(year, 4, buf);
                } else {
                    buf = "0000-00-00T00:00:00".toCharArray();
                    IOUtils.getChars(second, 19, buf);
                    IOUtils.getChars(minute, 16, buf);
                    IOUtils.getChars(hour, 13, buf);
                    IOUtils.getChars(day, 10, buf);
                    IOUtils.getChars(month, 7, buf);
                    IOUtils.getChars(year, 4, buf);
                }
                out.write(buf);
                float timeZoneF = ((float) calendar.getTimeZone().getOffset(calendar.getTimeInMillis())) / 3600000.0f;
                int timeZone = (int) timeZoneF;
                if (((double) timeZone) == 0.0d) {
                    out.write(90);
                } else {
                    if (timeZone > 9) {
                        out.write(43);
                        out.writeInt(timeZone);
                    } else if (timeZone > 0) {
                        out.write(43);
                        out.write(48);
                        out.writeInt(timeZone);
                    } else if (timeZone < -9) {
                        out.write(45);
                        out.writeInt(timeZone);
                    } else if (timeZone < 0) {
                        out.write(45);
                        out.write(48);
                        out.writeInt(-timeZone);
                    }
                    out.write(58);
                    out.append((CharSequence) String.format("%02d", Integer.valueOf((int) ((timeZoneF - ((float) timeZone)) * 60.0f))));
                }
                out.write(quote);
                return;
            }
            out.writeLong(time);
            return;
        }
        out.writeString(object.toString());
    }

    /* JADX DEBUG: Multi-variable search result rejected for r12v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // com.alibaba.fastjson.parser.deserializer.AbstractDateDeserializer
    public <T> T cast(DefaultJSONParser parser, Type clazz, Object fieldName, Object val) {
        if (val == 0) {
            return null;
        }
        if (val instanceof Date) {
            return val;
        }
        if (val instanceof BigDecimal) {
            return (T) new Date(TypeUtils.longValue((BigDecimal) val));
        }
        if (val instanceof Number) {
            return (T) new Date(((Number) val).longValue());
        }
        if (val instanceof String) {
            String strVal = (String) val;
            if (strVal.length() == 0) {
                return null;
            }
            JSONScanner dateLexer = new JSONScanner(strVal);
            try {
                if (dateLexer.scanISO8601DateIfMatch(false)) {
                    T t = (T) dateLexer.getCalendar();
                    if (clazz == Calendar.class) {
                        return t;
                    }
                    T t2 = (T) t.getTime();
                    dateLexer.close();
                    return t2;
                }
                dateLexer.close();
                if (strVal.length() == parser.getDateFomartPattern().length() || (strVal.length() == 22 && parser.getDateFomartPattern().equals("yyyyMMddHHmmssSSSZ"))) {
                    try {
                        return (T) parser.getDateFormat().parse(strVal);
                    } catch (ParseException e) {
                    }
                }
                if (strVal.startsWith("/Date(") && strVal.endsWith(")/")) {
                    strVal = strVal.substring(6, strVal.length() - 2);
                }
                if ("0000-00-00".equals(strVal) || "0000-00-00T00:00:00".equalsIgnoreCase(strVal) || "0001-01-01T00:00:00+08:00".equalsIgnoreCase(strVal)) {
                    return null;
                }
                int index = strVal.lastIndexOf(124);
                if (index > 20) {
                    TimeZone timeZone = TimeZone.getTimeZone(strVal.substring(index + 1));
                    if (!"GMT".equals(timeZone.getID())) {
                        JSONScanner dateLexer2 = new JSONScanner(strVal.substring(0, index));
                        try {
                            if (dateLexer2.scanISO8601DateIfMatch(false)) {
                                T t3 = (T) dateLexer2.getCalendar();
                                t3.setTimeZone(timeZone);
                                if (clazz == Calendar.class) {
                                    return t3;
                                }
                                T t4 = (T) t3.getTime();
                                dateLexer2.close();
                                return t4;
                            }
                            dateLexer2.close();
                        } finally {
                            dateLexer2.close();
                        }
                    }
                }
                return (T) new Date(Long.parseLong(strVal));
            } finally {
                dateLexer.close();
            }
        } else {
            throw new JSONException("parse error");
        }
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
