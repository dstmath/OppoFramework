package com.alibaba.fastjson.parser;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Locale;
import java.util.TimeZone;

public interface JSONLexer {
    public static final int ARRAY = 2;
    public static final int END = 4;
    public static final char EOI = 26;
    public static final int NOT_MATCH = -1;
    public static final int NOT_MATCH_NAME = -2;
    public static final int OBJECT = 1;
    public static final int UNKNOWN = 0;
    public static final int VALUE = 3;
    public static final int VALUE_NULL = 5;

    byte[] bytesValue();

    void close();

    void config(Feature feature, boolean z);

    Number decimalValue(boolean z);

    BigDecimal decimalValue();

    float floatValue();

    char getCurrent();

    int getFeatures();

    Locale getLocale();

    TimeZone getTimeZone();

    String info();

    int intValue();

    Number integerValue();

    boolean isBlankInput();

    boolean isEnabled(int i);

    boolean isEnabled(Feature feature);

    boolean isRef();

    long longValue();

    char next();

    void nextToken();

    void nextToken(int i);

    void nextTokenWithColon();

    void nextTokenWithColon(int i);

    String numberString();

    int pos();

    void resetStringPosition();

    boolean scanBoolean(char c);

    BigDecimal scanDecimal(char c);

    double scanDouble(char c);

    Enum<?> scanEnum(Class<?> cls, SymbolTable symbolTable, char c);

    float scanFloat(char c);

    int scanInt(char c);

    long scanLong(char c);

    void scanNumber();

    String scanString(char c);

    void scanString();

    void scanStringArray(Collection<String> collection, char c);

    String scanSymbol(SymbolTable symbolTable);

    String scanSymbol(SymbolTable symbolTable, char c);

    String scanSymbolUnQuoted(SymbolTable symbolTable);

    String scanSymbolWithSeperator(SymbolTable symbolTable, char c);

    void setLocale(Locale locale);

    void setTimeZone(TimeZone timeZone);

    void skipWhitespace();

    String stringVal();

    int token();

    String tokenName();
}
