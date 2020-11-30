package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.deserializer.ContextObjectDeserializer;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.util.IOUtils;
import java.io.IOException;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

public class CalendarCodec extends ContextObjectDeserializer implements ObjectDeserializer, ContextObjectSerializer, ObjectSerializer {
    public static final CalendarCodec instance = new CalendarCodec();
    private DatatypeFactory dateFactory;

    @Override // com.alibaba.fastjson.serializer.ContextObjectSerializer
    public void write(JSONSerializer serializer, Object object, BeanContext context) throws IOException {
        SerializeWriter out = serializer.out;
        String format = context.getFormat();
        Calendar calendar = (Calendar) object;
        if (format.equals("unixtime")) {
            out.writeInt((int) (calendar.getTimeInMillis() / 1000));
            return;
        }
        DateFormat dateFormat = new SimpleDateFormat(format);
        dateFormat.setTimeZone(serializer.timeZone);
        out.writeString(dateFormat.format(calendar.getTime()));
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        Calendar calendar;
        char[] buf;
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        if (object instanceof XMLGregorianCalendar) {
            calendar = ((XMLGregorianCalendar) object).toGregorianCalendar();
        } else {
            calendar = (Calendar) object;
        }
        if (out.isEnabled(SerializerFeature.UseISO8601DateFormat)) {
            char quote = out.isEnabled(SerializerFeature.UseSingleQuotes) ? '\'' : '\"';
            out.append(quote);
            int year = calendar.get(1);
            int month = calendar.get(2) + 1;
            int day = calendar.get(5);
            int hour = calendar.get(11);
            int minute = calendar.get(12);
            int second = calendar.get(13);
            int millis = calendar.get(14);
            if (millis != 0) {
                buf = "0000-00-00T00:00:00.000".toCharArray();
                IOUtils.getChars(millis, 23, buf);
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
            out.append(quote);
            return;
        }
        serializer.write(calendar.getTime());
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer, com.alibaba.fastjson.parser.deserializer.ContextObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type clazz, Object fieldName) {
        return (T) deserialze(parser, clazz, fieldName, null, 0);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ContextObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName, String format, int features) {
        T t = (T) DateCodec.instance.deserialze(parser, type, fieldName, format, features);
        if (t instanceof Calendar) {
            return t;
        }
        T date = t;
        if (date == null) {
            return null;
        }
        JSONLexer lexer = parser.lexer;
        T t2 = (T) Calendar.getInstance(lexer.getTimeZone(), lexer.getLocale());
        t2.setTime(date);
        return type == XMLGregorianCalendar.class ? (T) createXMLGregorianCalendar(t2) : t2;
    }

    public XMLGregorianCalendar createXMLGregorianCalendar(Calendar calendar) {
        if (this.dateFactory == null) {
            try {
                this.dateFactory = DatatypeFactory.newInstance();
            } catch (DatatypeConfigurationException e) {
                throw new IllegalStateException("Could not obtain an instance of DatatypeFactory.", e);
            }
        }
        return this.dateFactory.newXMLGregorianCalendar((GregorianCalendar) calendar);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 2;
    }
}
