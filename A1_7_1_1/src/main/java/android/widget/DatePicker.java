package android.widget;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.icu.util.Calendar;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.View.BaseSavedState;
import android.view.accessibility.AccessibilityEvent;
import com.android.internal.R;
import java.util.Locale;

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
public class DatePicker extends FrameLayout {
    public static final int MODE_CALENDAR = 2;
    public static final int MODE_SPINNER = 1;
    private final DatePickerDelegate mDelegate;
    private final int mMode;

    interface DatePickerDelegate {
        boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        CalendarView getCalendarView();

        boolean getCalendarViewShown();

        int getDayOfMonth();

        int getFirstDayOfWeek();

        Calendar getMaxDate();

        Calendar getMinDate();

        int getMonth();

        boolean getSpinnersShown();

        int getYear();

        void init(int i, int i2, int i3, OnDateChangedListener onDateChangedListener);

        boolean isEnabled();

        void onConfigurationChanged(Configuration configuration);

        void onPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent);

        void onRestoreInstanceState(Parcelable parcelable);

        Parcelable onSaveInstanceState(Parcelable parcelable);

        void setCalendarViewShown(boolean z);

        void setEnabled(boolean z);

        void setFirstDayOfWeek(int i);

        void setMaxDate(long j);

        void setMinDate(long j);

        void setSpinnersShown(boolean z);

        void setValidationCallback(ValidationCallback validationCallback);

        void updateDate(int i, int i2, int i3);
    }

    static abstract class AbstractDatePickerDelegate implements DatePickerDelegate {
        protected Context mContext;
        protected Locale mCurrentLocale;
        protected DatePicker mDelegator;
        protected OnDateChangedListener mOnDateChangedListener;
        protected ValidationCallback mValidationCallback;

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
        static class SavedState extends BaseSavedState {
            public static final Creator<SavedState> CREATOR = null;
            private final int mCurrentView;
            private final int mListPosition;
            private final int mListPositionOffset;
            private final long mMaxDate;
            private final long mMinDate;
            private final int mSelectedDay;
            private final int mSelectedMonth;
            private final int mSelectedYear;

            /* renamed from: android.widget.DatePicker$AbstractDatePickerDelegate$SavedState$1 */
            static class AnonymousClass1 implements Creator<SavedState> {
                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.<init>():void, dex: 
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
                    // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.<init>():void, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.<init>():void");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
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
                    // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object, dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.createFromParcel(android.os.Parcel):java.lang.Object");
                }

                /*  JADX ERROR: Method load error
                    jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.newArray(int):java.lang.Object[], dex: 
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
                    // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.newArray(int):java.lang.Object[], dex: 
                    */
                    throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.1.newArray(int):java.lang.Object[]");
                }

                public SavedState createFromParcel(Parcel in) {
                    return new SavedState(in, null);
                }

                public SavedState[] newArray(int size) {
                    return new SavedState[size];
                }
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<clinit>():void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<clinit>():void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<clinit>():void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e9 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<init>(android.os.Parcel):void, dex: 
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
                // Can't load method instructions: Load method exception: bogus opcode: 00e9 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<init>(android.os.Parcel):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<init>(android.os.Parcel):void");
            }

            /* synthetic */ SavedState(Parcel in, SavedState savedState) {
                this(in);
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e6 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<init>(android.os.Parcelable, int, int, int, long, long, int, int, int):void, dex: 
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
            public SavedState(android.os.Parcelable r1, int r2, int r3, int r4, long r5, long r7, int r9, int r10, int r11) {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e6 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<init>(android.os.Parcelable, int, int, int, long, long, int, int, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.<init>(android.os.Parcelable, int, int, int, long, long, int, int, int):void");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getCurrentView():int, dex: 
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
            public int getCurrentView() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getCurrentView():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getCurrentView():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPosition():int, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPosition():int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPosition():int, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.InstructionCodec.decodeRegisterList(InstructionCodec.java:920)
                	at com.android.dx.io.instructions.InstructionCodec.access$900(InstructionCodec.java:31)
                	at com.android.dx.io.instructions.InstructionCodec$25.decode(InstructionCodec.java:572)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            public int getListPosition() {
                /*
                // Can't load method instructions: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPosition():int, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPosition():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPosition():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPositionOffset():int, dex: 
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
            public int getListPositionOffset() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPositionOffset():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getListPositionOffset():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e4 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMaxDate():long, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e4
                	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
                	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
                	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
                	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
                	... 7 more
                */
            public long getMaxDate() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e4 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMaxDate():long, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMaxDate():long");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMinDate():long, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMinDate():long, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMinDate():long, dex: 
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
            public long getMinDate() {
                /*
                // Can't load method instructions: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMinDate():long, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMinDate():long, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getMinDate():long");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedDay():int, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedDay():int, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedDay():int, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.ShortArrayCodeInput.readInt(ShortArrayCodeInput.java:62)
                	at com.android.dx.io.instructions.InstructionCodec$23.decode(InstructionCodec.java:514)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            public int getSelectedDay() {
                /*
                // Can't load method instructions: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedDay():int, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedDay():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedDay():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedMonth():int, dex: 
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
            public int getSelectedMonth() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedMonth():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedMonth():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedYear():int, dex: 
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
            public int getSelectedYear() {
                /*
                // Can't load method instructions: Load method exception: bogus opcode: 00e3 in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedYear():int, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.getSelectedYear():int");
            }

            /*  JADX ERROR: Method load error
                jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void, dex: 
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
                	at jadx.core.ProcessClass.process(ProcessClass.java:29)
                	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
                	at jadx.api.JavaClass.decompile(JavaClass.java:62)
                	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
                Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void, dex: 
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
                	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
                	... 7 more
                Caused by: java.io.EOFException
                	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
                	at com.android.dx.io.instructions.InstructionCodec$21.decode(InstructionCodec.java:471)
                	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
                	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
                	... 8 more
                */
            public void writeToParcel(android.os.Parcel r1, int r2) {
                /*
                // Can't load method instructions: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void, dex: 
                */
                throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.SavedState.writeToParcel(android.os.Parcel, int):void");
            }

            public SavedState(Parcelable superState, int year, int month, int day, long minDate, long maxDate) {
                this(superState, year, month, day, minDate, maxDate, 0, 0, 0);
            }
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e8 in method: android.widget.DatePicker.AbstractDatePickerDelegate.<init>(android.widget.DatePicker, android.content.Context):void, dex: 
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
        public AbstractDatePickerDelegate(android.widget.DatePicker r1, android.content.Context r2) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e8 in method: android.widget.DatePicker.AbstractDatePickerDelegate.<init>(android.widget.DatePicker, android.content.Context):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.<init>(android.widget.DatePicker, android.content.Context):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: android.widget.DatePicker.AbstractDatePickerDelegate.onLocaleChanged(java.util.Locale):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
            	... 6 more
            */
        protected void onLocaleChanged(java.util.Locale r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: android.widget.DatePicker.AbstractDatePickerDelegate.onLocaleChanged(java.util.Locale):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.onLocaleChanged(java.util.Locale):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.DatePicker.AbstractDatePickerDelegate.onValidationChanged(boolean):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void onValidationChanged(boolean r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.DatePicker.AbstractDatePickerDelegate.onValidationChanged(boolean):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.onValidationChanged(boolean):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 00e5 in method: android.widget.DatePicker.AbstractDatePickerDelegate.setCurrentLocale(java.util.Locale):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e5
            	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1227)
            	at com.android.dx.io.OpcodeInfo.getName(OpcodeInfo.java:1234)
            	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:581)
            	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:74)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:104)
            	... 6 more
            */
        protected void setCurrentLocale(java.util.Locale r1) {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 00e5 in method: android.widget.DatePicker.AbstractDatePickerDelegate.setCurrentLocale(java.util.Locale):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.setCurrentLocale(java.util.Locale):void");
        }

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.setValidationCallback(android.widget.DatePicker$ValidationCallback):void, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.setValidationCallback(android.widget.DatePicker$ValidationCallback):void, dex: 
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
            	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:254)
            	at jadx.core.ProcessClass.process(ProcessClass.java:29)
            	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
            	at jadx.api.JavaClass.decompile(JavaClass.java:62)
            	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
            Caused by: jadx.core.utils.exceptions.DecodeException: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.setValidationCallback(android.widget.DatePicker$ValidationCallback):void, dex: 
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:51)
            	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:103)
            	... 6 more
            Caused by: java.io.EOFException
            	at com.android.dx.io.instructions.ShortArrayCodeInput.read(ShortArrayCodeInput.java:54)
            	at com.android.dx.io.instructions.ShortArrayCodeInput.readLong(ShortArrayCodeInput.java:71)
            	at com.android.dx.io.instructions.InstructionCodec$31.decode(InstructionCodec.java:652)
            	at jadx.core.dex.instructions.InsnDecoder.decodeRawInsn(InsnDecoder.java:66)
            	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:48)
            	... 7 more
            */
        public void setValidationCallback(android.widget.DatePicker.ValidationCallback r1) {
            /*
            // Can't load method instructions: Load method exception: null in method: android.widget.DatePicker.AbstractDatePickerDelegate.setValidationCallback(android.widget.DatePicker$ValidationCallback):void, dex:  in method: android.widget.DatePicker.AbstractDatePickerDelegate.setValidationCallback(android.widget.DatePicker$ValidationCallback):void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: android.widget.DatePicker.AbstractDatePickerDelegate.setValidationCallback(android.widget.DatePicker$ValidationCallback):void");
        }
    }

    public interface OnDateChangedListener {
        void onDateChanged(DatePicker datePicker, int i, int i2, int i3);
    }

    public interface ValidationCallback {
        void onValidationChanged(boolean z);
    }

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.datePickerStyle);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DatePicker, defStyleAttr, defStyleRes);
        boolean isDialogMode = a.getBoolean(20, false);
        int requestedMode = a.getInt(16, 1);
        int firstDayOfWeek = a.getInt(3, 0);
        a.recycle();
        if (requestedMode == 2 && isDialogMode) {
            this.mMode = context.getResources().getInteger(R.integer.date_picker_mode);
        } else {
            this.mMode = requestedMode;
        }
        switch (this.mMode) {
            case 2:
                this.mDelegate = createCalendarUIDelegate(context, attrs, defStyleAttr, defStyleRes);
                break;
            default:
                this.mDelegate = createSpinnerUIDelegate(context, attrs, defStyleAttr, defStyleRes);
                break;
        }
        if (firstDayOfWeek != 0) {
            setFirstDayOfWeek(firstDayOfWeek);
        }
    }

    private DatePickerDelegate createSpinnerUIDelegate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        return new DatePickerSpinnerDelegate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    private DatePickerDelegate createCalendarUIDelegate(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        return new DatePickerCalendarDelegate(this, context, attrs, defStyleAttr, defStyleRes);
    }

    public int getMode() {
        return this.mMode;
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        this.mDelegate.init(year, monthOfYear, dayOfMonth, onDateChangedListener);
    }

    public void updateDate(int year, int month, int dayOfMonth) {
        this.mDelegate.updateDate(year, month, dayOfMonth);
    }

    public int getYear() {
        return this.mDelegate.getYear();
    }

    public int getMonth() {
        return this.mDelegate.getMonth();
    }

    public int getDayOfMonth() {
        return this.mDelegate.getDayOfMonth();
    }

    public long getMinDate() {
        return this.mDelegate.getMinDate().getTimeInMillis();
    }

    public void setMinDate(long minDate) {
        this.mDelegate.setMinDate(minDate);
    }

    public long getMaxDate() {
        return this.mDelegate.getMaxDate().getTimeInMillis();
    }

    public void setMaxDate(long maxDate) {
        this.mDelegate.setMaxDate(maxDate);
    }

    public void setValidationCallback(ValidationCallback callback) {
        this.mDelegate.setValidationCallback(callback);
    }

    public void setEnabled(boolean enabled) {
        if (this.mDelegate.isEnabled() != enabled) {
            super.setEnabled(enabled);
            this.mDelegate.setEnabled(enabled);
        }
    }

    public boolean isEnabled() {
        return this.mDelegate.isEnabled();
    }

    public boolean dispatchPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        return this.mDelegate.dispatchPopulateAccessibilityEvent(event);
    }

    public void onPopulateAccessibilityEventInternal(AccessibilityEvent event) {
        super.onPopulateAccessibilityEventInternal(event);
        this.mDelegate.onPopulateAccessibilityEvent(event);
    }

    public CharSequence getAccessibilityClassName() {
        return DatePicker.class.getName();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mDelegate.onConfigurationChanged(newConfig);
    }

    public void setFirstDayOfWeek(int firstDayOfWeek) {
        if (firstDayOfWeek < 1 || firstDayOfWeek > 7) {
            throw new IllegalArgumentException("firstDayOfWeek must be between 1 and 7");
        }
        this.mDelegate.setFirstDayOfWeek(firstDayOfWeek);
    }

    public int getFirstDayOfWeek() {
        return this.mDelegate.getFirstDayOfWeek();
    }

    @Deprecated
    public boolean getCalendarViewShown() {
        return this.mDelegate.getCalendarViewShown();
    }

    @Deprecated
    public CalendarView getCalendarView() {
        return this.mDelegate.getCalendarView();
    }

    @Deprecated
    public void setCalendarViewShown(boolean shown) {
        this.mDelegate.setCalendarViewShown(shown);
    }

    @Deprecated
    public boolean getSpinnersShown() {
        return this.mDelegate.getSpinnersShown();
    }

    @Deprecated
    public void setSpinnersShown(boolean shown) {
        this.mDelegate.setSpinnersShown(shown);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    protected Parcelable onSaveInstanceState() {
        return this.mDelegate.onSaveInstanceState(super.-wrap0());
    }

    protected void onRestoreInstanceState(Parcelable state) {
        BaseSavedState ss = (BaseSavedState) state;
        super.-wrap2(ss.getSuperState());
        this.mDelegate.onRestoreInstanceState(ss);
    }
}
