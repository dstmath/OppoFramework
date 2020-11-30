package com.alibaba.fastjson.serializer;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONLexer;
import com.alibaba.fastjson.parser.ParseContext;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.IOException;
import java.lang.reflect.Type;

public class AwtCodec implements ObjectDeserializer, ObjectSerializer {
    public static final AwtCodec instance = new AwtCodec();

    public static boolean support(Class<?> clazz) {
        return clazz == Point.class || clazz == Rectangle.class || clazz == Font.class || clazz == Color.class;
    }

    @Override // com.alibaba.fastjson.serializer.ObjectSerializer
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features) throws IOException {
        SerializeWriter out = serializer.out;
        if (object == null) {
            out.writeNull();
            return;
        }
        if (object instanceof Point) {
            Point font = (Point) object;
            out.writeFieldValue(writeClassName(out, Point.class, '{'), "x", font.x);
            out.writeFieldValue(',', "y", font.y);
        } else if (object instanceof Font) {
            Font font2 = (Font) object;
            out.writeFieldValue(writeClassName(out, Font.class, '{'), "name", font2.getName());
            out.writeFieldValue(',', "style", font2.getStyle());
            out.writeFieldValue(',', "size", font2.getSize());
        } else if (object instanceof Rectangle) {
            Rectangle rectangle = (Rectangle) object;
            out.writeFieldValue(writeClassName(out, Rectangle.class, '{'), "x", rectangle.x);
            out.writeFieldValue(',', "y", rectangle.y);
            out.writeFieldValue(',', "width", rectangle.width);
            out.writeFieldValue(',', "height", rectangle.height);
        } else if (object instanceof Color) {
            Color color = (Color) object;
            out.writeFieldValue(writeClassName(out, Color.class, '{'), "r", color.getRed());
            out.writeFieldValue(',', "g", color.getGreen());
            out.writeFieldValue(',', "b", color.getBlue());
            if (color.getAlpha() > 0) {
                out.writeFieldValue(',', "alpha", color.getAlpha());
            }
        } else {
            throw new JSONException("not support awt class : " + object.getClass().getName());
        }
        out.write(125);
    }

    /* access modifiers changed from: protected */
    public char writeClassName(SerializeWriter out, Class<?> clazz, char sep) {
        if (!out.isEnabled(SerializerFeature.WriteClassName)) {
            return sep;
        }
        out.write(123);
        out.writeFieldName(JSON.DEFAULT_TYPE_KEY);
        out.writeString(clazz.getName());
        return ',';
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        T obj;
        JSONLexer lexer = parser.lexer;
        if (lexer.token() == 8) {
            lexer.nextToken(16);
            return null;
        } else if (lexer.token() == 12 || lexer.token() == 16) {
            lexer.nextToken();
            if (type == Point.class) {
                obj = (T) parsePoint(parser, fieldName);
            } else if (type == Rectangle.class) {
                obj = (T) parseRectangle(parser);
            } else if (type == Color.class) {
                obj = (T) parseColor(parser);
            } else if (type == Font.class) {
                obj = (T) parseFont(parser);
            } else {
                throw new JSONException("not support awt class : " + type);
            }
            ParseContext context = parser.getContext();
            parser.setContext(obj, fieldName);
            parser.setContext(context);
            return obj;
        } else {
            throw new JSONException("syntax error");
        }
    }

    /* access modifiers changed from: protected */
    public Font parseFont(DefaultJSONParser parser) {
        JSONLexer lexer = parser.lexer;
        int size = 0;
        int style = 0;
        String name = null;
        while (lexer.token() != 13) {
            if (lexer.token() == 4) {
                String key = lexer.stringVal();
                lexer.nextTokenWithColon(2);
                if (key.equalsIgnoreCase("name")) {
                    if (lexer.token() == 4) {
                        name = lexer.stringVal();
                        lexer.nextToken();
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if (key.equalsIgnoreCase("style")) {
                    if (lexer.token() == 2) {
                        style = lexer.intValue();
                        lexer.nextToken();
                    } else {
                        throw new JSONException("syntax error");
                    }
                } else if (!key.equalsIgnoreCase("size")) {
                    throw new JSONException("syntax error, " + key);
                } else if (lexer.token() == 2) {
                    size = lexer.intValue();
                    lexer.nextToken();
                } else {
                    throw new JSONException("syntax error");
                }
                if (lexer.token() == 16) {
                    lexer.nextToken(4);
                }
            } else {
                throw new JSONException("syntax error");
            }
        }
        lexer.nextToken();
        return new Font(name, style, size);
    }

    /* access modifiers changed from: protected */
    public Color parseColor(DefaultJSONParser parser) {
        JSONLexer lexer = parser.lexer;
        int r = 0;
        int g = 0;
        int b = 0;
        int alpha = 0;
        while (lexer.token() != 13) {
            if (lexer.token() == 4) {
                String key = lexer.stringVal();
                lexer.nextTokenWithColon(2);
                if (lexer.token() == 2) {
                    int val = lexer.intValue();
                    lexer.nextToken();
                    if (key.equalsIgnoreCase("r")) {
                        r = val;
                    } else if (key.equalsIgnoreCase("g")) {
                        g = val;
                    } else if (key.equalsIgnoreCase("b")) {
                        b = val;
                    } else if (key.equalsIgnoreCase("alpha")) {
                        alpha = val;
                    } else {
                        throw new JSONException("syntax error, " + key);
                    }
                    if (lexer.token() == 16) {
                        lexer.nextToken(4);
                    }
                } else {
                    throw new JSONException("syntax error");
                }
            } else {
                throw new JSONException("syntax error");
            }
        }
        lexer.nextToken();
        return new Color(r, g, b, alpha);
    }

    /* access modifiers changed from: protected */
    public Rectangle parseRectangle(DefaultJSONParser parser) {
        int val;
        JSONLexer lexer = parser.lexer;
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;
        while (lexer.token() != 13) {
            if (lexer.token() == 4) {
                String key = lexer.stringVal();
                lexer.nextTokenWithColon(2);
                int token = lexer.token();
                if (token == 2) {
                    val = lexer.intValue();
                    lexer.nextToken();
                } else if (token == 3) {
                    val = (int) lexer.floatValue();
                    lexer.nextToken();
                } else {
                    throw new JSONException("syntax error");
                }
                if (key.equalsIgnoreCase("x")) {
                    x = val;
                } else if (key.equalsIgnoreCase("y")) {
                    y = val;
                } else if (key.equalsIgnoreCase("width")) {
                    width = val;
                } else if (key.equalsIgnoreCase("height")) {
                    height = val;
                } else {
                    throw new JSONException("syntax error, " + key);
                }
                if (lexer.token() == 16) {
                    lexer.nextToken(4);
                }
            } else {
                throw new JSONException("syntax error");
            }
        }
        lexer.nextToken();
        return new Rectangle(x, y, width, height);
    }

    /* access modifiers changed from: protected */
    public Point parsePoint(DefaultJSONParser parser, Object fieldName) {
        int val;
        JSONLexer lexer = parser.lexer;
        int x = 0;
        int y = 0;
        while (lexer.token() != 13) {
            if (lexer.token() == 4) {
                String key = lexer.stringVal();
                if (JSON.DEFAULT_TYPE_KEY.equals(key)) {
                    parser.acceptType("java.awt.Point");
                } else if ("$ref".equals(key)) {
                    return (Point) parseRef(parser, fieldName);
                } else {
                    lexer.nextTokenWithColon(2);
                    int token = lexer.token();
                    if (token == 2) {
                        val = lexer.intValue();
                        lexer.nextToken();
                    } else if (token == 3) {
                        val = (int) lexer.floatValue();
                        lexer.nextToken();
                    } else {
                        throw new JSONException("syntax error : " + lexer.tokenName());
                    }
                    if (key.equalsIgnoreCase("x")) {
                        x = val;
                    } else if (key.equalsIgnoreCase("y")) {
                        y = val;
                    } else {
                        throw new JSONException("syntax error, " + key);
                    }
                    if (lexer.token() == 16) {
                        lexer.nextToken(4);
                    }
                }
            } else {
                throw new JSONException("syntax error");
            }
        }
        lexer.nextToken();
        return new Point(x, y);
    }

    private Object parseRef(DefaultJSONParser parser, Object fieldName) {
        JSONLexer lexer = parser.getLexer();
        lexer.nextTokenWithColon(4);
        String ref = lexer.stringVal();
        parser.setContext(parser.getContext(), fieldName);
        parser.addResolveTask(new DefaultJSONParser.ResolveTask(parser.getContext(), ref));
        parser.popContext();
        parser.setResolveStatus(1);
        lexer.nextToken(13);
        parser.accept(13);
        return null;
    }

    @Override // com.alibaba.fastjson.parser.deserializer.ObjectDeserializer
    public int getFastMatchToken() {
        return 12;
    }
}
