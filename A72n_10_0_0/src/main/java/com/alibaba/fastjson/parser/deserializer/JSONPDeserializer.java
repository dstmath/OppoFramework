package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONPObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexerBase;
import java.lang.reflect.Type;

public class JSONPDeserializer implements ObjectDeserializer {
    public static final JSONPDeserializer instance = new JSONPDeserializer();

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        int tok;
        JSONLexerBase lexer = (JSONLexerBase) parser.getLexer();
        String funcName = lexer.scanSymbolUnQuoted(parser.getSymbolTable());
        lexer.nextToken();
        int tok2 = lexer.token();
        if (tok2 == 25) {
            String funcName2 = funcName + ".";
            funcName = funcName2 + lexer.scanSymbolUnQuoted(parser.getSymbolTable());
            lexer.nextToken();
            tok2 = lexer.token();
        }
        T t = (T) new JSONPObject(funcName);
        if (tok2 == 10) {
            lexer.nextToken();
            while (true) {
                t.addParameter(parser.parse());
                tok = lexer.token();
                if (tok != 16) {
                    break;
                }
                lexer.nextToken();
            }
            if (tok == 11) {
                lexer.nextToken();
                if (lexer.token() == 24) {
                    lexer.nextToken();
                }
                return t;
            }
            throw new JSONException("illegal jsonp : " + lexer.info());
        }
        throw new JSONException("illegal jsonp : " + lexer.info());
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 0;
    }
}
