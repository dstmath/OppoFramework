package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.serializer.BeanContext;
import com.alibaba.fastjson.serializer.ContextObjectSerializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Locale;
import java.util.TimeZone;

public class Jdk8DateCodec extends ContextObjectDeserializer implements ObjectDeserializer, ContextObjectSerializer, ObjectSerializer {
    private static final DateTimeFormatter ISO_FIXED_FORMAT = DateTimeFormatter.ofPattern(defaultPatttern).withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern(defaultPatttern);
    private static final DateTimeFormatter defaultFormatter_23 = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final String defaultPatttern = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter formatter_d10_cn = DateTimeFormatter.ofPattern("yyyy年M月d日");
    private static final DateTimeFormatter formatter_d10_de = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter formatter_d10_eur = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter formatter_d10_in = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final DateTimeFormatter formatter_d10_kr = DateTimeFormatter.ofPattern("yyyy년M월d일");
    private static final DateTimeFormatter formatter_d10_tw = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter formatter_d10_us = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final DateTimeFormatter formatter_d8 = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter formatter_dt19_cn = DateTimeFormatter.ofPattern("yyyy年M月d日 HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_cn_1 = DateTimeFormatter.ofPattern("yyyy年M月d日 H时m分s秒");
    private static final DateTimeFormatter formatter_dt19_de = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_eur = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_in = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_kr = DateTimeFormatter.ofPattern("yyyy년M월d일 HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_tw = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private static final DateTimeFormatter formatter_dt19_us = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm:ss");
    private static final DateTimeFormatter formatter_iso8601 = DateTimeFormatter.ofPattern(formatter_iso8601_pattern);
    private static final String formatter_iso8601_pattern = "yyyy-MM-dd'T'HH:mm:ss";
    private static final String formatter_iso8601_pattern_23 = "yyyy-MM-dd'T'HH:mm:ss.SSS";
    private static final String formatter_iso8601_pattern_29 = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS";
    public static final Jdk8DateCodec instance = new Jdk8DateCodec();

    /* JADX INFO: Multiple debug info for r2v24 T: [D('localDateTime' java.time.LocalDateTime), D('localDate' java.time.LocalTime)] */
    /* JADX INFO: Multiple debug info for r2v28 T: [D('localDateTime' java.time.LocalDateTime), D('localDate' java.time.LocalDate)] */
    /* JADX INFO: Multiple debug info for r2v30 T: [D('localDateTime' java.time.LocalDateTime), D('localDate' java.time.LocalDate)] */
    @Override // com.alibaba.fastjson.parser.deserializer.ContextObjectDeserializer
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
                    formatter = DateTimeFormatter.ofPattern(format);
                }
            }
            if ("".equals(text)) {
                return null;
            }
            if (type == LocalDateTime.class) {
                return (text.length() == 10 || text.length() == 8) ? (T) LocalDateTime.of(parseLocalDate(text, format, formatter), LocalTime.MIN) : (T) parseDateTime(text, formatter);
            }
            if (type == LocalDate.class) {
                if (text.length() != 23) {
                    return (T) parseLocalDate(text, format, formatter);
                }
                LocalDateTime localDateTime = LocalDateTime.parse(text);
                return (T) LocalDate.of(localDateTime.getYear(), localDateTime.getMonthValue(), localDateTime.getDayOfMonth());
            } else if (type == LocalTime.class) {
                if (text.length() != 23) {
                    return (T) LocalTime.parse(text);
                }
                LocalDateTime localDateTime2 = LocalDateTime.parse(text);
                return (T) LocalTime.of(localDateTime2.getHour(), localDateTime2.getMinute(), localDateTime2.getSecond(), localDateTime2.getNano());
            } else if (type == ZonedDateTime.class) {
                if (formatter == defaultFormatter) {
                    formatter = ISO_FIXED_FORMAT;
                }
                if (formatter == null && text.length() <= 19) {
                    JSONScanner s = new JSONScanner(text);
                    TimeZone timeZone = parser.lexer.getTimeZone();
                    s.setTimeZone(timeZone);
                    if (s.scanISO8601DateIfMatch(false)) {
                        return (T) ZonedDateTime.ofInstant(s.getCalendar().getTime().toInstant(), timeZone.toZoneId());
                    }
                }
                return (T) parseZonedDateTime(text, formatter);
            } else if (type == OffsetDateTime.class) {
                return (T) OffsetDateTime.parse(text);
            } else {
                if (type == OffsetTime.class) {
                    return (T) OffsetTime.parse(text);
                }
                if (type == ZoneId.class) {
                    return (T) ZoneId.of(text);
                }
                if (type == Period.class) {
                    return (T) Period.parse(text);
                }
                if (type == Duration.class) {
                    return (T) Duration.parse(text);
                }
                if (type == Instant.class) {
                    return (T) Instant.parse(text);
                }
                return null;
            }
        } else if (lexer.token() == 2) {
            long millis = lexer.longValue();
            lexer.nextToken();
            if ("unixtime".equals(format)) {
                millis *= 1000;
            }
            if (type == LocalDateTime.class) {
                return (T) LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), JSON.defaultTimeZone.toZoneId());
            }
            if (type == LocalDate.class) {
                return (T) LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), JSON.defaultTimeZone.toZoneId()).toLocalDate();
            }
            if (type == LocalTime.class) {
                return (T) LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), JSON.defaultTimeZone.toZoneId()).toLocalTime();
            }
            if (type == ZonedDateTime.class) {
                return (T) ZonedDateTime.ofInstant(Instant.ofEpochMilli(millis), JSON.defaultTimeZone.toZoneId());
            }
            throw new UnsupportedOperationException();
        } else {
            throw new UnsupportedOperationException();
        }
    }

    /* JADX INFO: Multiple debug info for r1v7 java.time.format.DateTimeFormatter: [D('c4' char), D('formatter' java.time.format.DateTimeFormatter)] */
    /* JADX INFO: Multiple debug info for r1v11 java.time.format.DateTimeFormatter: [D('c7' char), D('formatter' java.time.format.DateTimeFormatter)] */
    /* JADX INFO: Multiple debug info for r1v14 java.time.format.DateTimeFormatter: [D('formatter' java.time.format.DateTimeFormatter), D('c2' char)] */
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
                            formatter2 = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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
            JSONScanner dateScanner = new JSONScanner(text);
            if (dateScanner.scanISO8601DateIfMatch(false)) {
                return LocalDateTime.ofInstant(dateScanner.getCalendar().toInstant(), ZoneId.systemDefault());
            }
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
    public ZonedDateTime parseZonedDateTime(String text, DateTimeFormatter formatter) {
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
                            formatter2 = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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
            return ZonedDateTime.parse(text);
        }
        return ZonedDateTime.parse(text, formatter2);
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
                } else {
                    int nano = dateTime.getNano();
                    if (nano == 0) {
                        format = formatter_iso8601_pattern;
                    } else if (nano % 1000000 == 0) {
                        format = formatter_iso8601_pattern_23;
                    } else {
                        format = formatter_iso8601_pattern_29;
                    }
                }
            }
            if (format != null) {
                write(out, dateTime, format);
            } else if (out.isEnabled(SerializerFeature.WriteDateUseDateFormat)) {
                write(out, dateTime, JSON.DEFFAULT_DATE_FORMAT);
            } else {
                out.writeLong(dateTime.atZone(JSON.defaultTimeZone.toZoneId()).toInstant().toEpochMilli());
            }
        } else {
            out.writeString(object.toString());
        }
    }

    @Override // com.alibaba.fastjson.serializer.ContextObjectSerializer
    public void write(JSONSerializer serializer, Object object, BeanContext context) throws IOException {
        write(serializer.out, (TemporalAccessor) object, context.getFormat());
    }

    private void write(SerializeWriter out, TemporalAccessor object, String format) {
        DateTimeFormatter formatter;
        if (!"unixtime".equals(format) || !(object instanceof ChronoZonedDateTime)) {
            if (format == formatter_iso8601_pattern) {
                formatter = formatter_iso8601;
            } else {
                formatter = DateTimeFormatter.ofPattern(format);
            }
            out.writeString(formatter.format(object));
            return;
        }
        out.writeInt((int) ((ChronoZonedDateTime) object).toEpochSecond());
    }
}
