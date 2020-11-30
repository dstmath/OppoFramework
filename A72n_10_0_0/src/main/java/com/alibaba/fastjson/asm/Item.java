package com.alibaba.fastjson.asm;

import com.alibaba.fastjson.parser.JSONToken;

/* access modifiers changed from: package-private */
public final class Item {
    int hashCode;
    int index;
    int intVal;
    long longVal;
    Item next;
    String strVal1;
    String strVal2;
    String strVal3;
    int type;

    Item() {
    }

    Item(int index2, Item i) {
        this.index = index2;
        this.type = i.type;
        this.intVal = i.intVal;
        this.longVal = i.longVal;
        this.strVal1 = i.strVal1;
        this.strVal2 = i.strVal2;
        this.strVal3 = i.strVal3;
        this.hashCode = i.hashCode;
    }

    /* access modifiers changed from: package-private */
    public void set(int type2, String strVal12, String strVal22, String strVal32) {
        this.type = type2;
        this.strVal1 = strVal12;
        this.strVal2 = strVal22;
        this.strVal3 = strVal32;
        switch (type2) {
            case 1:
            case JSONToken.FALSE /* 7 */:
            case JSONToken.NULL /* 8 */:
            case JSONToken.RBRACE /* 13 */:
                this.hashCode = Integer.MAX_VALUE & (strVal12.hashCode() + type2);
                return;
            case JSONToken.LBRACE /* 12 */:
                this.hashCode = Integer.MAX_VALUE & ((strVal12.hashCode() * strVal22.hashCode()) + type2);
                return;
            default:
                this.hashCode = Integer.MAX_VALUE & ((strVal12.hashCode() * strVal22.hashCode() * strVal32.hashCode()) + type2);
                return;
        }
    }

    /* access modifiers changed from: package-private */
    public void set(int intVal2) {
        this.type = 3;
        this.intVal = intVal2;
        this.hashCode = (this.type + intVal2) & Integer.MAX_VALUE;
    }

    /* access modifiers changed from: package-private */
    public boolean isEqualTo(Item i) {
        switch (this.type) {
            case 1:
            case JSONToken.FALSE /* 7 */:
            case JSONToken.NULL /* 8 */:
            case JSONToken.RBRACE /* 13 */:
                return i.strVal1.equals(this.strVal1);
            case 2:
            case 9:
            case 10:
            case 11:
            case 14:
            default:
                return i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2) && i.strVal3.equals(this.strVal3);
            case 3:
            case 4:
                return i.intVal == this.intVal;
            case 5:
            case JSONToken.TRUE /* 6 */:
            case JSONToken.RBRACKET /* 15 */:
                return i.longVal == this.longVal;
            case JSONToken.LBRACE /* 12 */:
                return i.strVal1.equals(this.strVal1) && i.strVal2.equals(this.strVal2);
        }
    }
}
