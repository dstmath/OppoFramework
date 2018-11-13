package com.android.server.hdmi;

import com.android.server.am.OppoGameSpaceManager;
import java.util.Arrays;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
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
/*  JADX ERROR: NullPointerException in pass: ExtractFieldInit
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ExtractFieldInit.checkStaticFieldsInit(ExtractFieldInit.java:58)
    	at jadx.core.dex.visitors.ExtractFieldInit.visit(ExtractFieldInit.java:44)
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
public final class HdmiCecMessage {
    public static final byte[] EMPTY_PARAM = null;
    private final int mDestination;
    private final int mOpcode;
    private final byte[] mParams;
    private final int mSource;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.hdmi.HdmiCecMessage.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 9 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.hdmi.HdmiCecMessage.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.hdmi.HdmiCecMessage.<clinit>():void");
    }

    public HdmiCecMessage(int source, int destination, int opcode, byte[] params) {
        this.mSource = source;
        this.mDestination = destination;
        this.mOpcode = opcode & 255;
        this.mParams = Arrays.copyOf(params, params.length);
    }

    public int getSource() {
        return this.mSource;
    }

    public int getDestination() {
        return this.mDestination;
    }

    public int getOpcode() {
        return this.mOpcode;
    }

    public byte[] getParams() {
        return this.mParams;
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        Object[] objArr = new Object[3];
        objArr[0] = opcodeToString(this.mOpcode);
        objArr[1] = Integer.valueOf(this.mSource);
        objArr[2] = Integer.valueOf(this.mDestination);
        s.append(String.format("<%s> src: %d, dst: %d", objArr));
        if (this.mParams.length > 0) {
            s.append(", params:");
            for (byte data : this.mParams) {
                Object[] objArr2 = new Object[1];
                objArr2[0] = Byte.valueOf(data);
                s.append(String.format(" %02X", objArr2));
            }
        }
        return s.toString();
    }

    private static String opcodeToString(int opcode) {
        switch (opcode) {
            case 0:
                return "Feature Abort";
            case 4:
                return "Image View On";
            case 5:
                return "Tuner Step Increment";
            case 6:
                return "Tuner Step Decrement";
            case 7:
                return "Tuner Device Staus";
            case 8:
                return "Give Tuner Device Status";
            case 9:
                return "Record On";
            case 10:
                return "Record Status";
            case 11:
                return "Record Off";
            case 13:
                return "Text View On";
            case 15:
                return "Record Tv Screen";
            case H.DO_ANIMATION_CALLBACK /*26*/:
                return "Give Deck Status";
            case 27:
                return "Deck Status";
            case 50:
                return "Set Menu Language";
            case 51:
                return "Clear Analog Timer";
            case 52:
                return "Set Analog Timer";
            case 53:
                return "Timer Status";
            case 54:
                return "Standby";
            case HdmiCecKeycode.CEC_KEYCODE_VOLUME_UP /*65*/:
                return "Play";
            case HdmiCecKeycode.CEC_KEYCODE_VOLUME_DOWN /*66*/:
                return "Deck Control";
            case HdmiCecKeycode.CEC_KEYCODE_MUTE /*67*/:
                return "Timer Cleared Status";
            case HdmiCecKeycode.CEC_KEYCODE_PLAY /*68*/:
                return "User Control Pressed";
            case HdmiCecKeycode.CEC_KEYCODE_STOP /*69*/:
                return "User Control Release";
            case HdmiCecKeycode.CEC_KEYCODE_PAUSE /*70*/:
                return "Give Osd Name";
            case HdmiCecKeycode.CEC_KEYCODE_RECORD /*71*/:
                return "Set Osd Name";
            case 100:
                return "Set Osd String";
            case 103:
                return "Set Timer Program Title";
            case 112:
                return "System Audio Mode Request";
            case 113:
                return "Give Audio Status";
            case 114:
                return "Set System Audio Mode";
            case 122:
                return "Report Audio Status";
            case 125:
                return "Give System Audio Mode Status";
            case 126:
                return "System Audio Mode Status";
            case 128:
                return "Routing Change";
            case 129:
                return "Routing Information";
            case 130:
                return "Active Source";
            case 131:
                return "Give Physical Address";
            case 132:
                return "Report Physical Address";
            case 133:
                return "Request Active Source";
            case 134:
                return "Set Stream Path";
            case 135:
                return "Device Vendor Id";
            case 137:
                return "Vendor Commandn";
            case 138:
                return "Vendor Remote Button Down";
            case 139:
                return "Vendor Remote Button Up";
            case 140:
                return "Give Device Vendor Id";
            case OppoGameSpaceManager.MSG_SEND_GAME_STOP /*141*/:
                return "Menu REquest";
            case 142:
                return "Menu Status";
            case 143:
                return "Give Device Power Status";
            case 144:
                return "Report Power Status";
            case HdmiCecKeycode.UI_BROADCAST_DIGITAL_COMMNICATIONS_SATELLITE_2 /*145*/:
                return "Get Menu Language";
            case 146:
                return "Select Analog Service";
            case 147:
                return "Select Digital Service";
            case 151:
                return "Set Digital Timer";
            case 153:
                return "Clear Digital Timer";
            case 154:
                return "Set Audio Rate";
            case 157:
                return "InActive Source";
            case 158:
                return "Cec Version";
            case 159:
                return "Get Cec Version";
            case 160:
                return "Vendor Command With Id";
            case 161:
                return "Clear External Timer";
            case 162:
                return "Set External Timer";
            case 163:
                return "Repot Short Audio Descriptor";
            case 164:
                return "Request Short Audio Descriptor";
            case 192:
                return "Initiate ARC";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_PLUS /*193*/:
                return "Report ARC Initiated";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_NEUTRAL /*194*/:
                return "Report ARC Terminated";
            case HdmiCecKeycode.UI_SOUND_PRESENTATION_TREBLE_STEP_MINUS /*195*/:
                return "Request ARC Initiation";
            case 196:
                return "Request ARC Termination";
            case 197:
                return "Terminate ARC";
            case 248:
                return "Cdc Message";
            case 255:
                return "Abort";
            default:
                Object[] objArr = new Object[1];
                objArr[0] = Integer.valueOf(opcode);
                return String.format("Opcode: %02X", objArr);
        }
    }
}
