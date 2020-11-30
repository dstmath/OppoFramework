package com.alibaba.fastjson.asm;

public class MethodCollector {
    private int currentParameter;
    protected boolean debugInfoPresent;
    private final int ignoreCount;
    private final int paramCount;
    private final StringBuilder result = new StringBuilder();

    protected MethodCollector(int ignoreCount2, int paramCount2) {
        this.ignoreCount = ignoreCount2;
        this.paramCount = paramCount2;
        boolean z = false;
        this.currentParameter = 0;
        this.debugInfoPresent = paramCount2 == 0 ? true : z;
    }

    /* access modifiers changed from: protected */
    public void visitLocalVariable(String name, int index) {
        if (index >= this.ignoreCount && index < this.ignoreCount + this.paramCount) {
            if (!name.equals("arg" + this.currentParameter)) {
                this.debugInfoPresent = true;
            }
            this.result.append(',');
            this.result.append(name);
            this.currentParameter++;
        }
    }

    /* access modifiers changed from: protected */
    public String getResult() {
        return this.result.length() != 0 ? this.result.substring(1) : "";
    }
}
