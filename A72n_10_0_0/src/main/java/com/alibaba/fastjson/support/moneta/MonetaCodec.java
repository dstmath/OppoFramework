package com.alibaba.fastjson.support.moneta;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;
import com.alibaba.fastjson.serializer.SerializeWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import javax.money.Monetary;
import org.javamoney.moneta.Money;

public class MonetaCodec implements ObjectDeserializer, ObjectSerializer {
    public static final MonetaCodec instance = new MonetaCodec();

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        Money money = (Money) object;
        if (money == null) {
            serializer.writeNull();
            return;
        }
        SerializeWriter out = serializer.out;
        out.writeFieldValue('{', "numberStripped", money.getNumberStripped());
        out.writeFieldValue(',', "currency", money.getCurrency().getCurrencyCode());
        out.write(125);
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        JSONObject object = parser.parseObject();
        Object currency = object.get("currency");
        String currencyCode = null;
        if (currency instanceof JSONObject) {
            currencyCode = ((JSONObject) currency).getString("currencyCode");
        } else if (currency instanceof String) {
            currencyCode = (String) currency;
        }
        Object numberStripped = object.get("numberStripped");
        if (numberStripped instanceof BigDecimal) {
            return (T) Money.of((BigDecimal) numberStripped, Monetary.getCurrency(currencyCode, new String[0]));
        }
        throw new UnsupportedOperationException();
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 0;
    }
}
