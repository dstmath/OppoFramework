package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Locale;
import java.util.TimeZone;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Duration;
import org.joda.time.Instant;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.ReadablePartial;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class JodaCodec implements ObjectDeserializer, ContextObjectSerializer, ObjectSerializer {
    private static final DateTimeFormatter ISO_FIXED_FORMAT = DateTimeFormat.forPattern(defaultPatttern).withZone(DateTimeZone.getDefault());
    private static final DateTimeFormatter defaultFormatter = DateTimeFormat.forPattern(defaultPatttern);
    private static final DateTimeFormatter defaultFormatter_23 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String defaultPatttern = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter_d10_cn = DateTimeFormat.forPattern("yyyy年M月d日");
    private static final DateTimeFormatter formatter_d10_de = DateTimeFormat.forPattern("dd.MM.yyyy");
    private static final DateTimeFormatter formatter_d10_eur = DateTimeFormat.forPattern("dd/MM/yyyy");
    private static final DateTimeFormatter formatter_d10_in = DateTimeFormat.forPattern("dd-MM-yyyy");
    private static final DateTimeFormatter formatter_d10_kr = DateTimeFormat.forPattern("yyyy년M월d일");
    private static final DateTimeFormatter formatter_d10_tw = DateTimeFormat.forPattern("yyyy/MM/dd");
    private static final DateTimeFormatter formatter_d10_us = DateTimeFormat.forPattern("MM/dd/yyyy");
    private static final DateTimeFormatter formatter_d8 = DateTimeFormat.forPattern("yyyyMMdd");
    private static final DateTimeFormatter formatter_dt19_cn = DateTimeFormat.forPattern("yyyy年M月d日 HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_cn_1 = DateTimeFormat.forPattern("yyyy年M月d日 H时m分s秒");
    private static final DateTimeFormatter formatter_dt19_de = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_eur = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_in = DateTimeFormat.forPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_kr = DateTimeFormat.forPattern("yyyy년M월d일 HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_tw = DateTimeFormat.forPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_us = DateTimeFormat.forPattern("MM/dd/yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_iso8601 = DateTimeFormat.forPattern(formatter_iso8601_pattern);
    private static final String formatter_iso8601_pattern = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String formatter_iso8601_pattern_23 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String formatter_iso8601_pattern_29 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS";
    public static final JodaCodec instance = new JodaCodec();

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        return (T) deserialze(parser, type, fieldName, null, 0);
    }

    /* JADX INFO: Multiple debug info for r2v20 T: [D('localDateTime' org.joda.time.LocalDateTime), D('localDate' org.joda.time.LocalTime)] */
    /* JADX INFO: Multiple debug info for r2v24 T: [D('localDateTime' org.joda.time.LocalDateTime), D('localDate' org.joda.time.LocalDate)] */
    /* JADX INFO: Multiple debug info for r2v26 T: [D('localDate' org.joda.time.LocalDate), D('localDateTime' org.joda.time.LocalDateTime)] */
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName, String format, int feature) {
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken();
            return null;
        } else if (lexer.token() == 4) {
            String text = lexer.stringVal();
            lexer.nextToken();
            DateTimeFormatter formatter = null;
            if (format != null) {
                if (defaultPatttern.equals(format)) {
                    formatter = defaultFormatter;
                } else {
                    formatter = DateTimeFormat.forPattern(format);
                }
            }
            if ("".equals(text)) {
                return null;
            }
            if (type == LocalDateTime.class) {
                return (text.length() == 10 || text.length() == 8) ? (T) parseLocalDate(text, format, formatter).toLocalDateTime(LocalTime.MIDNIGHT) : (T) parseDateTime(text, formatter);
            }
            if (type == LocalDate.class) {
                return text.length() == 23 ? (T) LocalDateTime.parse(text).toLocalDate() : (T) parseLocalDate(text, format, formatter);
            }
            if (type == LocalTime.class) {
                return text.length() == 23 ? (T) LocalDateTime.parse(text).toLocalTime() : (T) LocalTime.parse(text);
            }
            if (type == DateTime.class) {
                if (formatter == defaultFormatter) {
                    formatter = ISO_FIXED_FORMAT;
                }
                return (T) parseZonedDateTime(text, formatter);
            } else if (type == DateTimeZone.class) {
                return (T) DateTimeZone.forID(text);
            } else {
                if (type == Period.class) {
                    return (T) Period.parse(text);
                }
                if (type == Duration.class) {
                    return (T) Duration.parse(text);
                }
                if (type == Instant.class) {
                    return (T) Instant.parse(text);
                }
                if (type == DateTimeFormatter.class) {
                    return (T) DateTimeFormat.forPattern(text);
                }
                return null;
            }
        } else if (lexer.token() == 2) {
            long millis = lexer.longValue();
            lexer.nextToken();
            TimeZone timeZone = JSON.defaultTimeZone;
            if (timeZone == null) {
                timeZone = TimeZone.getDefault();
            }
            if (type == DateTime.class) {
                return (T) new DateTime(millis, DateTimeZone.forTimeZone(timeZone));
            }
            T t = (T) new LocalDateTime(millis, DateTimeZone.forTimeZone(timeZone));
            if (type == LocalDateTime.class) {
                return t;
            }
            if (type == LocalDate.class) {
                return (T) t.toLocalDate();
            }
            if (type == LocalTime.class) {
                return (T) t.toLocalTime();
            }
            if (type == Instant.class) {
                return (T) new Instant(millis);
            }
            throw new UnsupportedOperationException();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* access modifiers changed from: protected */
    public LocalDateTime parseDateTime(String text, DateTimeFormatter formatter) {
        DateTimeFormatter formatter2;
        if (formatter == null) {
            if (text.length() == 19) {
                char c4 = text.charAt(4);
                char c7 = text.charAt(7);
                char c10 = text.charAt(10);
                char c13 = text.charAt(13);
                char c16 = text.charAt(16);
                if (c13 == ':' && c16 == ':') {
                    if (c4 == '-' && c7 == '-') {
                        if (c10 == 'T') {
                            formatter2 = formatter_iso8601;
                        } else if (c10 == ' ') {
                            formatter2 = defaultFormatter;
                        }
                    } else if (c4 == '-' && c7 == '-') {
                        formatter2 = defaultFormatter;
                    } else if (c4 == '/' && c7 == '/') {
                        formatter2 = formatter_dt19_tw;
                    } else {
                        char c0 = text.charAt(0);
                        char c1 = text.charAt(1);
                        char c2 = text.charAt(2);
                        char c3 = text.charAt(3);
                        char c5 = text.charAt(5);
                        if (c2 == '/' && c5 == '/') {
                            int v1 = ((c3 - '0') * 10) + (c4 - '0');
                            if (((c0 - '0') * 10) + (c1 - '0') > 12) {
                                formatter2 = formatter_dt19_eur;
                            } else if (v1 > 12) {
                                formatter2 = formatter_dt19_us;
                            } else {
                                String country = Locale.getDefault().getCountry();
                                if (country.equals("US")) {
                                    formatter2 = formatter_dt19_us;
                                } else if (country.equals("BR") || country.equals("AU")) {
                                    formatter2 = formatter_dt19_eur;
                                } else {
                                    formatter2 = formatter;
                                }
                            }
                        } else if (c2 == '.' && c5 == '.') {
                            formatter2 = formatter_dt19_de;
                        } else if (c2 == '-' && c5 == '-') {
                            formatter2 = formatter_dt19_in;
                        }
                    }
                }
                formatter2 = formatter;
            } else {
                if (text.length() == 23) {
                    char c42 = text.charAt(4);
                    char c72 = text.charAt(7);
                    char c102 = text.charAt(10);
                    char c132 = text.charAt(13);
                    char c162 = text.charAt(16);
                    char c19 = text.charAt(19);
                    if (c132 == ':' && c162 == ':' && c42 == '-' && c72 == '-' && c102 == ' ' && c19 == '.') {
                        formatter2 = defaultFormatter_23;
                    }
                }
                formatter2 = formatter;
            }
            if (text.length() >= 17) {
                char c43 = text.charAt(4);
                if (c43 == 24180) {
                    if (text.charAt(text.length() - 1) == 31186) {
                        formatter2 = formatter_dt19_cn_1;
                    } else {
                        formatter2 = formatter_dt19_cn;
                    }
                } else if (c43 == 45380) {
                    formatter2 = formatter_dt19_kr;
                }
            }
        } else {
            formatter2 = formatter;
        }
        if (formatter2 == null) {
            return LocalDateTime.parse(text);
        }
        return LocalDateTime.parse(text, formatter2);
    }

    /* access modifiers changed from: protected */
    public LocalDate parseLocalDate(String text, String format, DateTimeFormatter formatter) {
        if (formatter == null) {
            if (text.length() == 8) {
                formatter = formatter_d8;
            }
            if (text.length() == 10) {
                char c4 = text.charAt(4);
                char c7 = text.charAt(7);
                if (c4 == '/' && c7 == '/') {
                    formatter = formatter_d10_tw;
                }
                char c0 = text.charAt(0);
                char c1 = text.charAt(1);
                char c2 = text.charAt(2);
                char c3 = text.charAt(3);
                char c5 = text.charAt(5);
                if (c2 == '/' && c5 == '/') {
                    int v1 = ((c3 - '0') * 10) + (c4 - '0');
                    if (((c0 - '0') * 10) + (c1 - '0') > 12) {
                        formatter = formatter_d10_eur;
                    } else if (v1 > 12) {
                        formatter = formatter_d10_us;
                    } else {
                        String country = Locale.getDefault().getCountry();
                        if (country.equals("US")) {
                            formatter = formatter_d10_us;
                        } else if (country.equals("BR") || country.equals("AU")) {
                            formatter = formatter_d10_eur;
                        }
                    }
                } else if (c2 == '.' && c5 == '.') {
                    formatter = formatter_d10_de;
                } else if (c2 == '-' && c5 == '-') {
                    formatter = formatter_d10_in;
                }
            }
            if (text.length() >= 9) {
                char c42 = text.charAt(4);
                if (c42 == 24180) {
                    formatter = formatter_d10_cn;
                } else if (c42 == 45380) {
                    formatter = formatter_d10_kr;
                }
            }
        }
        if (formatter == null) {
            return LocalDate.parse(text);
        }
        return LocalDate.parse(text, formatter);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x00cc  */
    public DateTime parseZonedDateTime(String text, DateTimeFormatter formatter) {
        DateTimeFormatter formatter2;
        if (formatter == null) {
            if (text.length() == 19) {
                char c4 = text.charAt(4);
                char c7 = text.charAt(7);
                char c10 = text.charAt(10);
                char c13 = text.charAt(13);
                char c16 = text.charAt(16);
                if (c13 == ':' && c16 == ':') {
                    if (c4 == '-' && c7 == '-') {
                        if (c10 == 'T') {
                            formatter2 = formatter_iso8601;
                        } else if (c10 == ' ') {
                            formatter2 = defaultFormatter;
                        }
                        if (text.length() >= 17) {
                        }
                    } else if (c4 == '-' && c7 == '-') {
                        formatter2 = defaultFormatter;
                        if (text.length() >= 17) {
                        }
                    } else if (c4 == '/' && c7 == '/') {
                        formatter2 = formatter_dt19_tw;
                        if (text.length() >= 17) {
                        }
                    } else {
                        char c0 = text.charAt(0);
                        char c1 = text.charAt(1);
                        char c2 = text.charAt(2);
                        char c3 = text.charAt(3);
                        char c5 = text.charAt(5);
                        if (c2 == '/' && c5 == '/') {
                            int v1 = ((c3 - '0') * 10) + (c4 - '0');
                            if (((c0 - '0') * 10) + (c1 - '0') > 12) {
                                formatter2 = formatter_dt19_eur;
                            } else if (v1 > 12) {
                                formatter2 = formatter_dt19_us;
                            } else {
                                String country = Locale.getDefault().getCountry();
                                if (country.equals("US")) {
                                    formatter2 = formatter_dt19_us;
                                } else if (country.equals("BR") || country.equals("AU")) {
                                    formatter2 = formatter_dt19_eur;
                                } else {
                                    formatter2 = formatter;
                                }
                            }
                            if (text.length() >= 17) {
                            }
                        } else if (c2 == '.' && c5 == '.') {
                            formatter2 = formatter_dt19_de;
                            if (text.length() >= 17) {
                            }
                        } else if (c2 == '-' && c5 == '-') {
                            formatter2 = formatter_dt19_in;
                            if (text.length() >= 17) {
                                char c42 = text.charAt(4);
                                if (c42 == 24180) {
                                    if (text.charAt(text.length() - 1) == 31186) {
                                        formatter2 = formatter_dt19_cn_1;
                                    } else {
                                        formatter2 = formatter_dt19_cn;
                                    }
                                } else if (c42 == 45380) {
                                    formatter2 = formatter_dt19_kr;
                                }
                            }
                        }
                    }
                }
            }
            formatter2 = formatter;
            if (text.length() >= 17) {
            }
        } else {
            formatter2 = formatter;
        }
        if (formatter2 == null) {
            return DateTime.parse(text);
        }
        return DateTime.parse(text, formatter2);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 4;
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        if (fieldType == null) {
            fieldType = object.getClass();
        }
        if (fieldType == LocalDateTime.class) {
            int mask = SerializerFeature.UseISO8601DateFormat.getMask();
            LocalDateTime dateTime = (LocalDateTime) object;
            String format = serializer.getDateFormatPattern();
            if (format == null) {
                if ((features & mask) != 0 || serializer.isEnabled(SerializerFeature.UseISO8601DateFormat)) {
                    format = formatter_iso8601_pattern;
                } else if (dateTime.getMillisOfSecond() == 0) {
                    format = formatter_iso8601_pattern_23;
                } else {
                    format = formatter_iso8601_pattern_29;
                }
            }
            if (format != null) {
                write(out, (ReadablePartial) dateTime, format);
            } else if (out.isEnabled(SerializerFeature.WriteDateUseDateFormat)) {
                write(out, (ReadablePartial) dateTime, JSON.DEFFAULT_DATE_FORMAT);
            } else {
                out.writeLong(dateTime.toDateTime(DateTimeZone.forTimeZone(JSON.defaultTimeZone)).toInstant().getMillis());
            }
        } else {
            out.writeString(object.toString());
        }
    }

    @Override // com.alibaba.fastjson.serializer.ContextObjectSerializer
    public void write(JSONSerializer serializer, Object object, BeanContext context) throws IOException {
        write(serializer.out, (ReadablePartial) object, context.getFormat());
    }

    private void write(SerializeWriter out, ReadablePartial object, String format) {
        DateTimeFormatter formatter;
        if (format == formatter_iso8601_pattern) {
            formatter = formatter_iso8601;
        } else {
            formatter = DateTimeFormat.forPattern(format);
        }
        out.writeString(formatter.print(object));
    }
}
