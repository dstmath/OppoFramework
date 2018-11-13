package android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.MathUtils;
import android.view.View.BaseSavedState;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.R;
import java.util.Locale;
import libcore.icu.LocaleData;

/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.isAllBlocksEmpty(BlockUtils.java:546)
    	at jadx.core.dex.visitors.ExtractFieldInit.getConstructorsList(ExtractFieldInit.java:221)
    	at jadx.core.dex.visitors.ExtractFieldInit.moveCommonFieldsInit(ExtractFieldInit.java:121)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:46)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:42)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
/*  JADX ERROR: NullPointerException in pass: ClassModifier
    java.lang.NullPointerException
    	at jadx.core.utils.BlockUtils.collectAllInsns(BlockUtils.java:556)
    	at jadx.core.dex.visitors.ClassModifier.removeBridgeMethod(ClassModifier.java:197)
    	at jadx.core.dex.visitors.ClassModifier.removeSyntheticMethods(ClassModifier.java:135)
    	at jadx.core.dex.visitors.ClassModifier.lambda$visit$0(ClassModifier.java:49)
    	at java.util.ArrayList.forEach(ArrayList.java:1251)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:49)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.ClassModifier.visit(ClassModifier.java:40)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class TimePicker extends FrameLayout {
    public static final int MODE_CLOCK = 2;
    public static final int MODE_SPINNER = 1;
    private final TimePickerDelegate mDelegate;
    private final int mMode;

    interface TimePickerDelegate {
        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        int getBaseline();

        int getHour();

        int getMinute();

        boolean is24Hour();

        boolean isEnabled();

        void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        void onRestoreInstanceState(Parcelable parcelable);

        Parcelable onSaveInstanceState(Parcelable parcelable);

        void setEnabled(boolean z);

        void setHour(int i);

        void setIs24Hour(boolean z);

        void setMinute(int i);

        void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener);
    }

    static abstract class AbstractTimePickerDelegate implements TimePickerDelegate {
        protected final Context mContext;
        protected final TimePicker mDelegator;
        protected final Locale mLocale;
        protected OnTimeChangedListener mOnTimeChangedListener;

        /*  JADX ERROR: NullPointerException in pass: ReSugarCode
            java.lang.NullPointerException
            	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
            	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
            	at java.util.ArrayList.forEach(ArrayList.java:1251)
            	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
            	at jadx.core.ProcessClass.process(ProcessClass.java:32)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            */
        protected static class SavedState extends BaseSavedState {
            public static final Creator<SavedState> CREATOR = null;
            private final int mCurrentItemShowing;
            private final int mHour;
            private final boolean mIs24HourMode;
            private final int mMinute;

            /* renamed from: android.widget.TimePicker$AbstractTimePickerDelegate$SavedState$1 */
            static class AnonymousClass1 implements Creator<SavedState> {
                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.<init>():void, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                    Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
                    	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                    	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                    	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                    	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                    	... 8 more
                    */
                AnonymousClass1() {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.<init>():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.<init>():void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                    Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                    	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                    	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                    	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                    	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                    	... 8 more
                    */
                public /* bridge */ /* synthetic */ java.lang.Object createFromParcel(android.os.Parcel r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.newArray(int):java.lang.Object[], dex: 
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                    	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                    Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                    	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                    	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                    	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                    	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                    	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                    	... 8 more
                    */
                public /* bridge */ /* synthetic */ java.lang.Object[] newArray(int r1) {
                    /*
                    // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.newArray(int):java.lang.Object[], dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.1.newArray(int):java.lang.Object[]");
                }

                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in, null);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<clinit>():void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            static {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<clinit>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<clinit>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<init>(android.os.Parcel):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            private SavedState(android.os.Parcel r1) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<init>(android.os.Parcel):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<init>(android.os.Parcel):void");
            }

            /* synthetic */ SavedState(Parcel in, SavedState savedState) {
                this(in);
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<init>(android.os.Parcelable, int, int, boolean, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e6
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public SavedState(android.os.Parcelable r1, int r2, int r3, boolean r4, int r5) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<init>(android.os.Parcelable, int, int, boolean, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.<init>(android.os.Parcelable, int, int, boolean, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getCurrentItemShowing():int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public int getCurrentItemShowing() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getCurrentItemShowing():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getCurrentItemShowing():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getHour():int, dex:  in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getHour():int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getHour():int, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
                	at com.android.dx.io.instructions.InstructionCodec$22.decode(InstructionCodec.java:490)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            public int getHour() {
                /*
                // Can't load method instructions: Load method exception: null in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getHour():int, dex:  in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getHour():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getHour():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getMinute():int, dex:  in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getMinute():int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getMinute():int, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
                	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            public int getMinute() {
                /*
                // Can't load method instructions: Load method exception: null in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getMinute():int, dex:  in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getMinute():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.getMinute():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00ef in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.is24HourMode():boolean, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00ef
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public boolean is24HourMode() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00ef in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.is24HourMode():boolean, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.is24HourMode():boolean");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e3
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public void writeToParcel(android.os.Parcel r1, int r2) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void");
            }

            public SavedState(Parcelable superState, int hour, int minute, boolean is24HourMode) {
                this(superState, hour, minute, is24HourMode, 0);
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.TimePicker.AbstractTimePickerDelegate.<init>(android.widget.TimePicker, android.content.Context):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e8
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        public AbstractTimePickerDelegate(android.widget.TimePicker r1, android.content.Context r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.TimePicker.AbstractTimePickerDelegate.<init>(android.widget.TimePicker, android.content.Context):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.TimePicker.AbstractTimePickerDelegate.<init>(android.widget.TimePicker, android.content.Context):void");
        }
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker timePicker, int i, int i2);
    }

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timePickerStyle);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TimePicker, defStyleAttr, defStyleRes);
        boolean isDialogMode = a.getBoolean(12, false);
        int requestedMode = a.getInt(8, 1);
        a.recycle();
        if (requestedMode == 2 && isDialogMode) {
            this.mMode = context.getResources().getInteger(R.integer.time_picker_mode);
        } else {
            this.mMode = requestedMode;
        }
        switch (this.mMode) {
            case 2:
                this.mDelegate = new TimePickerClockDelegate(this, context, attrs, defStyleAttr, defStyleRes);
                return;
            default:
                this.mDelegate = new TimePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
                return;
        }
    }

    public int getMode() {
        return this.mMode;
    }

    public void setHour(int hour) {
        this.mDelegate.setHour(MathUtils.constrain(hour, 0, 23));
    }

    public int getHour() {
        return this.mDelegate.getHour();
    }

    public void setMinute(int minute) {
        this.mDelegate.setMinute(MathUtils.constrain(minute, 0, 59));
    }

    public int getMinute() {
        return this.mDelegate.getMinute();
    }

    @Deprecated
    public void setCurrentHour(Integer currentHour) {
        setHour(currentHour.intValue());
    }

    @Deprecated
    public Integer getCurrentHour() {
        return Integer.valueOf(getHour());
    }

    @Deprecated
    public void setCurrentMinute(Integer currentMinute) {
        setMinute(currentMinute.intValue());
    }

    @Deprecated
    public Integer getCurrentMinute() {
        return Integer.valueOf(getMinute());
    }

    public void setIs24HourView(Boolean is24HourView) {
        if (is24HourView != null) {
            this.mDelegate.setIs24Hour(is24HourView.booleanValue());
        }
    }

    public boolean is24HourView() {
        return this.mDelegate.is24Hour();
    }

    public void setOnTimeChangedListener(OnTimeChangedListener onTimeChangedListener) {
        this.mDelegate.setOnTimeChangedListener(onTimeChangedListener);
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mDelegate.setEnabled(enabled);
    }

    public boolean isEnabled() {
        return this.mDelegate.isEnabled();
    }

    public int getBaseline() {
        return this.mDelegate.getBaseline();
    }

    protected Parcelable onSaveInstanceState() {
        return this.mDelegate.onSaveInstanceState(super.-wrap0());
    }

    protected void onRestoreInstanceState(Parcelable state) {
        BaseSavedState ss = (BaseSavedState) state;
        super.-wrap2(ss.getSuperState());
        this.mDelegate.onRestoreInstanceState(ss);
    }

    public CharSequence getAccessibilityClassName() {
        return TimePicker.class.getName();
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        return this.mDelegate.dispatchPopulateAccessibilityEvent(event);
    }

    static String[] getAmPmStrings(Context context) {
        LocaleData d = LocaleData.get(context.getResources().getConfiguration().locale);
        String[] result = new String[2];
        result[0] = d.amPm[0].length() > 4 ? d.narrowAm : d.amPm[0];
        result[1] = d.amPm[1].length() > 4 ? d.narrowPm : d.amPm[1];
        return result;
    }
}
