package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.JSONScanner;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;

public abstract class AbstractDateDeserializer extends ContextObjectDeserializer implements ObjectDeserializer {
    /* access modifiers changed from: protected */
    public abstract <T> T cast(DefaultJSONParser defaultJSONParser, Type type, Object obj, Object obj2);

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer, com.alibaba.fastjson.parser.deserializer.ContextObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, clazz, fieldName, null, 0);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ContextObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName, String format, int features) {
        Object val;
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 2) {
            long millis = lexer.longValue();
            lexer.nextToken(16);
            if ("unixtime".equals(format)) {
                millis *= 1000;
            }
            val = Long.valueOf(millis);
        } else {
            Object val2 = null;
            if (lexer.token() == 4) {
                String strVal = lexer.stringVal();
                if (format != null) {
                    SimpleDateFormat simpleDateFormat = null;
                    try {
                        simpleDateFormat = new SimpleDateFormat(format, parser.lexer.getLocale());
                    } catch (IllegalArgumentException ex) {
                        if (format.contains("T")) {
                            try {
                                simpleDateFormat = new SimpleDateFormat(format.replaceAll("T", "'T'"), parser.lexer.getLocale());
                            } catch (IllegalArgumentException e) {
                                throw ex;
                            }
                        }
                    }
                    if (JSON.defaultTimeZone != null) {
                        simpleDateFormat.setTimeZone(parser.lexer.getTimeZone());
                    }
                    try {
                        val2 = simpleDateFormat.parse(strVal);
                    } catch (ParseException e2) {
                    }
                    if (val2 == null && JSON.defaultLocale == Locale.CHINA) {
                        try {
                            simpleDateFormat = new SimpleDateFormat(format, Locale.US);
                        } catch (IllegalArgumentException ex2) {
                            if (format.contains("T")) {
                                try {
                                    simpleDateFormat = new SimpleDateFormat(format.replaceAll("T", "'T'"), parser.lexer.getLocale());
                                } catch (IllegalArgumentException e3) {
                                    throw ex2;
                                }
                            }
                        }
                        simpleDateFormat.setTimeZone(parser.lexer.getTimeZone());
                        try {
                            val2 = simpleDateFormat.parse(strVal);
                        } catch (ParseException e4) {
                            val2 = null;
                        }
                    }
                    if (val2 == null) {
                        if (!format.equals("yyyy-MM-dd'T'HH:mm:ss.SSS") || strVal.length() != 19) {
                            val2 = null;
                        } else {
                            try {
                                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", JSON.defaultLocale);
                                df.setTimeZone(JSON.defaultTimeZone);
                                val2 = df.parse(strVal);
                            } catch (ParseException e5) {
                                val2 = null;
                            }
                        }
                    }
                }
                Object val3 = val2;
                if (val3 == null) {
                    val3 = strVal;
                    lexer.nextToken(16);
                    if (lexer.isEnabled(Feature.AllowISO8601DateFormat)) {
                        JSONScanner iso8601Lexer = new JSONScanner(strVal);
                        if (iso8601Lexer.scanISO8601DateIfMatch()) {
                            val3 = iso8601Lexer.getCalendar().getTime();
                        }
                        iso8601Lexer.close();
                    }
                }
                val = val3;
            } else if (lexer.token() == 8) {
                lexer.nextToken();
                val = null;
            } else if (lexer.token() == 12) {
                lexer.nextToken();
                if (lexer.token() == 4) {
                    if (JSON.DEFAULT_TYPE_KEY.equals(lexer.stringVal())) {
                        lexer.nextToken();
                        parser.accept(17);
                        Class<?> type = parser.getConfig().checkAutoType(lexer.stringVal(), null, lexer.getFeatures());
                        if (type != null) {
                            clazz = type;
                        }
                        parser.accept(4);
                        parser.accept(16);
                    }
                    lexer.nextTokenWithColon(2);
                    if (lexer.token() == 2) {
                        long timeMillis = lexer.longValue();
                        lexer.nextToken();
                        Object val4 = Long.valueOf(timeMillis);
                        parser.accept(13);
                        val = val4;
                    } else {
                        throw new JSONException("syntax error : " + lexer.tokenName());
                    }
                } else {
                    throw new JSONException("syntax error");
                }
            } else if (parser.getResolveStatus() == 2) {
                parser.setResolveStatus(0);
                parser.accept(16);
                if (lexer.token() != 4) {
                    throw new JSONException("syntax error");
                } else if ("val".equals(lexer.stringVal())) {
                    lexer.nextToken();
                    parser.accept(17);
                    val = parser.parse();
                    parser.accept(13);
                } else {
                    throw new JSONException("syntax error");
                }
            } else {
                val = parser.parse();
            }
        }
        return (T) cast(parser, clazz, fieldName, val);
    }
}
