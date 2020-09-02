package gov.nist.javax.sip.header;

import java.util.Arrays;

class Indentation {
    private int indentation;

    protected Indentation() {
        this.indentation = 0;
    }

    protected Indentation(int initval) {
        this.indentation = initval;
    }

    /* access modifiers changed from: protected */
    public void setIndentation(int initval) {
        this.indentation = initval;
    }

    /* access modifiers changed from: protected */
    public int getCount() {
        return this.indentation;
    }

    /* access modifiers changed from: protected */
    public void increment() {
        this.indentation++;
    }

    /* access modifiers changed from: protected */
    public void decrement() {
        this.indentation--;
    }

    /* JADX DEBUG: Failed to find minimal casts for resolve overloaded methods, cast all args instead
     method: ClspMth{java.util.Arrays.fill(char[], char):void}
     arg types: [char[], int]
     candidates:
      ClspMth{java.util.Arrays.fill(double[], double):void}
      ClspMth{java.util.Arrays.fill(byte[], byte):void}
      ClspMth{java.util.Arrays.fill(long[], long):void}
      ClspMth{java.util.Arrays.fill(boolean[], boolean):void}
      ClspMth{java.util.Arrays.fill(short[], short):void}
      ClspMth{java.util.Arrays.fill(java.lang.Object[], java.lang.Object):void}
      ClspMth{java.util.Arrays.fill(int[], int):void}
      ClspMth{java.util.Arrays.fill(float[], float):void}
      ClspMth{java.util.Arrays.fill(char[], char):void} */
    /* access modifiers changed from: protected */
    public String getIndentation() {
        char[] chars = new char[this.indentation];
        Arrays.fill(chars, ' ');
        return new String(chars);
    }
}
