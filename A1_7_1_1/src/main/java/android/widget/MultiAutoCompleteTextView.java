package android.widget;

import android.content.Context;
import android.text.Editable;
import android.text.TextUtils;
import android.text.method.QwertyKeyListener;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView.Validator;
import com.android.internal.R;
import com.android.internal.telephony.PhoneConstants;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
    	at java.lang.Iterable.forEach(Iterable.java:75)
    	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
    	at jadx.core.ProcessClass.process(ProcessClass.java:37)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class MultiAutoCompleteTextView extends AutoCompleteTextView {
    private Tokenizer mTokenizer;

    public interface Tokenizer {
        int findTokenEnd(CharSequence charSequence, int i);

        int findTokenStart(CharSequence charSequence, int i);

        CharSequence terminateToken(CharSequence charSequence);
    }

    public static class CommaTokenizer implements Tokenizer {
        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.MultiAutoCompleteTextView.CommaTokenizer.<init>():void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public CommaTokenizer() {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.MultiAutoCompleteTextView.CommaTokenizer.<init>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.MultiAutoCompleteTextView.CommaTokenizer.<init>():void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.MultiAutoCompleteTextView.CommaTokenizer.terminateToken(java.lang.CharSequence):java.lang.CharSequence, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
            	at java.lang.Iterable.forEach(Iterable.java:75)
            	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
            	at jadx.core.ProcessClass.process(ProcessClass.java:37)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 10 more
            */
        public java.lang.CharSequence terminateToken(java.lang.CharSequence r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.MultiAutoCompleteTextView.CommaTokenizer.terminateToken(java.lang.CharSequence):java.lang.CharSequence, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.MultiAutoCompleteTextView.CommaTokenizer.terminateToken(java.lang.CharSequence):java.lang.CharSequence");
        }

        public int findTokenStart(CharSequence text, int cursor) {
            int i = cursor;
            while (i > 0 && text.charAt(i - 1) != ',') {
                i--;
            }
            while (i < cursor && text.charAt(i) == ' ') {
                i++;
            }
            return i;
        }

        public int findTokenEnd(CharSequence text, int cursor) {
            int len = text.length();
            for (int i = cursor; i < len; i++) {
                if (text.charAt(i) == ',') {
                    return i;
                }
            }
            return len;
        }
    }

    public MultiAutoCompleteTextView(Context context) {
        this(context, null);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.autoCompleteTextViewStyle);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public MultiAutoCompleteTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    void finishInit() {
    }

    public void setTokenizer(Tokenizer t) {
        this.mTokenizer = t;
    }

    protected void performFiltering(CharSequence text, int keyCode) {
        if (enoughToFilter()) {
            int end = getSelectionEnd();
            performFiltering(text, this.mTokenizer.findTokenStart(text, end), end, keyCode);
            return;
        }
        dismissDropDown();
        Filter f = getFilter();
        if (f != null) {
            f.filter(null);
        }
    }

    /* JADX WARNING: Missing block: B:4:0x000f, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean enoughToFilter() {
        Editable text = getText();
        int end = getSelectionEnd();
        if (end < 0 || this.mTokenizer == null || end - this.mTokenizer.findTokenStart(text, end) < getThreshold()) {
            return false;
        }
        return true;
    }

    public void performValidation() {
        Validator v = getValidator();
        if (v != null && this.mTokenizer != null) {
            Editable e = getText();
            int i = getText().length();
            while (i > 0) {
                int start = this.mTokenizer.findTokenStart(e, i);
                CharSequence sub = e.subSequence(start, this.mTokenizer.findTokenEnd(e, start));
                if (TextUtils.isEmpty(sub)) {
                    e.replace(start, i, PhoneConstants.MVNO_TYPE_NONE);
                } else if (!v.isValid(sub)) {
                    e.replace(start, i, this.mTokenizer.terminateToken(v.fixText(sub)));
                }
                i = start;
            }
        }
    }

    protected void performFiltering(CharSequence text, int start, int end, int keyCode) {
        getFilter().filter(text.subSequence(start, end), this);
    }

    protected void replaceText(CharSequence text) {
        clearComposingText();
        int end = getSelectionEnd();
        int start = this.mTokenizer.findTokenStart(getText(), end);
        Editable editable = getText();
        QwertyKeyListener.markAsReplaced(editable, start, end, TextUtils.substring(editable, start, end));
        editable.replace(start, end, this.mTokenizer.terminateToken(text));
    }

    public CharSequence getAccessibilityClassName() {
        return MultiAutoCompleteTextView.class.getName();
    }
}
