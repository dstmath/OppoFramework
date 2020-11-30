package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONScanner;
import com.alibaba.fastjson.util.TypeUtils;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Date;

public class SqlDateDeserializer extends AbstractDateDeserializer implements ObjectDeserializer {
    public static final SqlDateDeserializer instance = new SqlDateDeserializer();
    public static final SqlDateDeserializer instance_timestamp = new SqlDateDeserializer(true);
    private boolean timestamp;

    public SqlDateDeserializer() {
        this.timestamp = false;
    }

    public SqlDateDeserializer(boolean timestmap) {
        this.timestamp = false;
        this.timestamp = true;
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    @Override // com.alibaba.fastjson.parser.deserializer.AbstractDateDeserializer
    public <T> T cast(DefaultJSONParser parser, Type clazz, Object fieldName, Object val) {
        long longVal;
        if (this.timestamp) {
            return (T) castTimestamp(parser, clazz, fieldName, val);
        }
        if (val == null) {
            return null;
        }
        if (val instanceof Date) {
            return (T) new java.sql.Date(((Date) val).getTime());
        }
        if (val instanceof BigDecimal) {
            return (T) new java.sql.Date(TypeUtils.longValue((BigDecimal) val));
        }
        if (val instanceof Number) {
            return (T) new java.sql.Date(((Number) val).longValue());
        }
        if (val instanceof String) {
            String strVal = (String) val;
            if (strVal.length() == 0) {
                return null;
            }
            JSONScanner dateLexer = new JSONScanner(strVal);
            try {
                if (dateLexer.scanISO8601DateIfMatch()) {
                    longVal = dateLexer.getCalendar().getTimeInMillis();
                } else {
                    try {
                        T t = (T) new java.sql.Date(parser.getDateFormat().parse(strVal).getTime());
                        dateLexer.close();
                        return t;
                    } catch (ParseException e) {
                        longVal = Long.parseLong(strVal);
                    }
                }
                dateLexer.close();
                return (T) new java.sql.Date(longVal);
            } catch (Throwable th) {
                dateLexer.close();
                throw th;
            }
        } else {
            throw new JSONException("parse error : " + val);
        }
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: protected */
    public <T> T castTimestamp(DefaultJSONParser parser, Type clazz, Object fieldName, Object val) {
        long longVal;
        if (val == null) {
            return null;
        }
        if (val instanceof Date) {
            return (T) new Timestamp(((Date) val).getTime());
        }
        if (val instanceof BigDecimal) {
            return (T) new Timestamp(TypeUtils.longValue((BigDecimal) val));
        }
        if (val instanceof Number) {
            return (T) new Timestamp(((Number) val).longValue());
        }
        if (val instanceof String) {
            String strVal = (String) val;
            if (strVal.length() == 0) {
                return null;
            }
            JSONScanner dateLexer = new JSONScanner(strVal);
            try {
                if (dateLexer.scanISO8601DateIfMatch(false)) {
                    longVal = dateLexer.getCalendar().getTimeInMillis();
                } else {
                    try {
                        T t = (T) new Timestamp(parser.getDateFormat().parse(strVal).getTime());
                        dateLexer.close();
                        return t;
                    } catch (ParseException e) {
                        longVal = Long.parseLong(strVal);
                    }
                }
                dateLexer.close();
                return (T) new Timestamp(longVal);
            } catch (Throwable th) {
                dateLexer.close();
                throw th;
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
