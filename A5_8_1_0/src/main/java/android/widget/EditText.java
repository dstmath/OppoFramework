package android.widget;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.text.Editable;
import android.text.Editable.Factory;
import android.text.Selection;
import android.text.TextUtils.TruncateAt;
import android.text.method.ArrowKeyMovementMethod;
import android.text.method.MovementMethod;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.TextView.BufferType;
import com.android.internal.R;
import com.idatafa.SensitiveAnalyzerService.ISAService;
import com.idatafa.SensitiveAnalyzerService.ISAService.Stub;

public class EditText extends TextView {
    private static ISAService mSAService = null;
    private static boolean sCustomizeVersion = false;
    private static boolean sFirstCheck = false;
    private boolean mLoop;
    private ServiceConnection mServiceConnection;
    private Intent mServiceIntent;

    public EditText(Context context) {
        this(context, null);
    }

    public EditText(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextStyle);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditText(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mLoop = false;
        this.mServiceIntent = new Intent("SensitiveAnalyzerService.SAService.START");
        this.mServiceConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName arg0, IBinder arg1) {
                EditText.mSAService = Stub.asInterface(arg1);
            }

            public void onServiceDisconnected(ComponentName arg0) {
                EditText.mSAService = null;
            }
        };
        if (!sFirstCheck) {
            sCustomizeVersion = context.getPackageManager().hasSystemFeature("oppo.customize.function.analyse_text");
            sFirstCheck = true;
        }
    }

    public boolean getFreezesText() {
        return true;
    }

    protected boolean getDefaultEditable() {
        return true;
    }

    protected MovementMethod getDefaultMovementMethod() {
        return ArrowKeyMovementMethod.getInstance();
    }

    public Editable getText() {
        if (sCustomizeVersion) {
            return (Editable) analyseText(super.getText());
        }
        return (Editable) super.getText();
    }

    public void setText(CharSequence text, BufferType type) {
        super.setText(text, BufferType.EDITABLE);
    }

    public void setSelection(int start, int stop) {
        Selection.setSelection(getText(), start, stop);
    }

    public void setSelection(int index) {
        Selection.setSelection(getText(), index);
    }

    public void selectAll() {
        Selection.selectAll(getText());
    }

    public void extendSelection(int index) {
        Selection.extendSelection(getText(), index);
    }

    public void setEllipsize(TruncateAt ellipsis) {
        if (ellipsis == TruncateAt.MARQUEE) {
            throw new IllegalArgumentException("EditText cannot use the ellipsize mode TextUtils.TruncateAt.MARQUEE");
        }
        super.setEllipsize(ellipsis);
    }

    public CharSequence getAccessibilityClassName() {
        return EditText.class.getName();
    }

    protected boolean supportsAutoSizeText() {
        return false;
    }

    public void onInitializeAccessibilityNodeInfoInternal(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfoInternal(info);
        if (isEnabled()) {
            info.addAction(AccessibilityAction.ACTION_SET_TEXT);
        }
    }

    private CharSequence analyseText(CharSequence text) {
        boolean z = false;
        if (this.mLoop) {
            return text;
        }
        CharSequence newtext = text;
        Context context = getContext();
        if (mSAService == null) {
            try {
                if (this.mServiceIntent == null) {
                    this.mServiceIntent = new Intent("SensitiveAnalyzerService.SAService.START");
                }
                this.mServiceIntent.setPackage("com.idatafa.SensitiveAnalyzerService");
                context.bindService(this.mServiceIntent, this.mServiceConnection, 1);
            } catch (Exception e) {
                e.printStackTrace();
                return text;
            }
        } else if (text.length() > 0) {
            try {
                newtext = mSAService.EditText_getText(text, context.getPackageName());
                if (newtext != null) {
                    z = newtext.toString().equals(text.toString());
                }
                if (!z) {
                    this.mLoop = true;
                    super.setText(newtext);
                    this.mLoop = false;
                }
                newtext = Factory.getInstance().newEditable(newtext);
            } catch (Exception e2) {
                e2.printStackTrace();
                return text;
            }
        }
        return newtext;
    }
}
