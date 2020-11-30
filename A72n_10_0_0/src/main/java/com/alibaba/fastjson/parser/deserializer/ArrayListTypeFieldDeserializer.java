package com.alibaba.fastjson.parser.deserializer;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.FieldInfo;
import com.alibaba.fastjson.util.ParameterizedTypeImpl;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class ArrayListTypeFieldDeserializer extends FieldDeserializer {
    private ObjectDeserializer deserializer;
    private int itemFastMatchToken;
    private final Type itemType;

    public ArrayListTypeFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo) {
        super(clazz, fieldInfo);
        if (fieldInfo.fieldType instanceof ParameterizedType) {
            Type argType = ((ParameterizedType) fieldInfo.fieldType).getActualTypeArguments()[0];
            if (argType instanceof WildcardType) {
                Type[] upperBounds = ((WildcardType) argType).getUpperBounds();
                if (upperBounds.length == 1) {
                    argType = upperBounds[0];
                }
            }
            this.itemType = argType;
            return;
        }
        this.itemType = Object.class;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.FieldDeserializer
    public int getFastMatchToken() {
        return 14;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.FieldDeserializer
    public void parseField(DefaultJSONParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        JSONLexer lexer = parser.lexer;
        int token = lexer.token();
        if (token == 8 || (token == 4 && lexer.stringVal().length() == 0)) {
            setValue(object, (String) null);
            return;
        }
        ArrayList list = new ArrayList();
        ParseContext context = parser.getContext();
        parser.setContext(context, object, this.fieldInfo.name);
        parseArray(parser, objectType, list);
        parser.setContext(context);
        if (object == null) {
            fieldValues.put(this.fieldInfo.name, list);
        } else {
            setValue(object, list);
        }
    }

    /* JADX INFO: Multiple debug info for r5v1 com.alibaba.fastjson.parser.JSONLexer: [D('lexer' com.alibaba.fastjson.parser.JSONLexer), D('objectClass' java.lang.Class)] */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00c8  */
    public final void parseArray(DefaultJSONParser parser, Type objectType, Collection array) {
        ObjectDeserializer itemTypeDeser;
        ObjectDeserializer itemTypeDeser2;
        Type itemType2 = this.itemType;
        ObjectDeserializer itemTypeDeser3 = this.deserializer;
        if (!(objectType instanceof ParameterizedType)) {
            itemTypeDeser = itemTypeDeser3;
            if ((itemType2 instanceof TypeVariable) && (objectType instanceof Class)) {
                Class objectClass = (Class) objectType;
                TypeVariable typeVar = (TypeVariable) itemType2;
                objectClass.getTypeParameters();
                int i = 0;
                int size = objectClass.getTypeParameters().length;
                while (true) {
                    if (i >= size) {
                        break;
                    }
                    TypeVariable item = objectClass.getTypeParameters()[i];
                    if (item.getName().equals(typeVar.getName())) {
                        Type[] bounds = item.getBounds();
                        if (bounds.length == 1) {
                            itemType2 = bounds[0];
                        }
                    } else {
                        i++;
                    }
                }
            }
        } else if (itemType2 instanceof TypeVariable) {
            TypeVariable typeVar2 = (TypeVariable) itemType2;
            ParameterizedType paramType = (ParameterizedType) objectType;
            Class<?> objectClass2 = null;
            if (paramType.getRawType() instanceof Class) {
                objectClass2 = (Class) paramType.getRawType();
            }
            int paramIndex = -1;
            if (objectClass2 != null) {
                int i2 = 0;
                int size2 = objectClass2.getTypeParameters().length;
                while (true) {
                    if (i2 >= size2) {
                        break;
                    } else if (objectClass2.getTypeParameters()[i2].getName().equals(typeVar2.getName())) {
                        paramIndex = i2;
                        break;
                    } else {
                        i2++;
                    }
                }
            }
            if (paramIndex != -1) {
                itemType2 = paramType.getActualTypeArguments()[paramIndex];
                if (!itemType2.equals(this.itemType)) {
                    itemTypeDeser3 = parser.getConfig().getDeserializer(itemType2);
                }
            }
            itemTypeDeser = itemTypeDeser3;
        } else if (itemType2 instanceof ParameterizedType) {
            ParameterizedType parameterizedItemType = (ParameterizedType) itemType2;
            Type[] itemActualTypeArgs = parameterizedItemType.getActualTypeArguments();
            if (itemActualTypeArgs.length != 1 || !(itemActualTypeArgs[0] instanceof TypeVariable)) {
                itemTypeDeser = itemTypeDeser3;
            } else {
                TypeVariable typeVar3 = (TypeVariable) itemActualTypeArgs[0];
                ParameterizedType paramType2 = (ParameterizedType) objectType;
                Class<?> objectClass3 = null;
                if (paramType2.getRawType() instanceof Class) {
                    objectClass3 = (Class) paramType2.getRawType();
                }
                int paramIndex2 = -1;
                if (objectClass3 != null) {
                    int i3 = 0;
                    int size3 = objectClass3.getTypeParameters().length;
                    while (true) {
                        if (i3 >= size3) {
                            break;
                        }
                        itemTypeDeser = itemTypeDeser3;
                        if (objectClass3.getTypeParameters()[i3].getName().equals(typeVar3.getName())) {
                            paramIndex2 = i3;
                            break;
                        } else {
                            i3++;
                            itemTypeDeser3 = itemTypeDeser;
                        }
                    }
                    if (paramIndex2 != -1) {
                        itemActualTypeArgs[0] = paramType2.getActualTypeArguments()[paramIndex2];
                        itemType2 = new ParameterizedTypeImpl(itemActualTypeArgs, parameterizedItemType.getOwnerType(), parameterizedItemType.getRawType());
                    }
                }
                itemTypeDeser = itemTypeDeser3;
                if (paramIndex2 != -1) {
                }
            }
        } else {
            itemTypeDeser = itemTypeDeser3;
        }
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 14) {
            if (itemTypeDeser == null) {
                ObjectDeserializer deserializer2 = parser.getConfig().getDeserializer(itemType2);
                this.deserializer = deserializer2;
                itemTypeDeser = deserializer2;
                this.itemFastMatchToken = this.deserializer.getFastMatchToken();
            }
            lexer.nextToken(this.itemFastMatchToken);
            int i4 = 0;
            while (true) {
                if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                    while (lexer.token() == 16) {
                        lexer.nextToken();
                    }
                }
                if (lexer.token() == 15) {
                    lexer.nextToken(16);
                    return;
                }
                array.add(itemTypeDeser.deserialze(parser, itemType2, Integer.valueOf(i4)));
                parser.checkListResolve(array);
                if (lexer.token() == 16) {
                    lexer.nextToken(this.itemFastMatchToken);
                }
                i4++;
            }
        } else {
            if (itemTypeDeser == null) {
                itemTypeDeser2 = parser.getConfig().getDeserializer(itemType2);
                this.deserializer = itemTypeDeser2;
            } else {
                itemTypeDeser2 = itemTypeDeser;
            }
            array.add(itemTypeDeser2.deserialze(parser, itemType2, 0));
            parser.checkListResolve(array);
        }
    }
}
