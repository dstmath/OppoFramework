package com.android.server.wifi.hotspot2.pps;

import com.android.server.wifi.hotspot2.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
public class DomainMatcher {
    private static final String[] TestDomains = null;
    private final Label mRoot;

    private static class Label {
        private final Match mMatch;
        private final Map<String, Label> mSubDomains;

        /* synthetic */ Label(Match match, Label label) {
            this(match);
        }

        private Label(Match match) {
            this.mMatch = match;
            this.mSubDomains = match == Match.None ? new HashMap() : null;
        }

        private void addDomain(Iterator<String> labels, Match match) {
            String labelName = (String) labels.next();
            if (labels.hasNext()) {
                Label subLabel = new Label(Match.None);
                this.mSubDomains.put(labelName, subLabel);
                subLabel.addDomain(labels, match);
                return;
            }
            this.mSubDomains.put(labelName, new Label(match));
        }

        private Label getSubLabel(String labelString) {
            return (Label) this.mSubDomains.get(labelString);
        }

        public Match getMatch() {
            return this.mMatch;
        }

        private void toString(StringBuilder sb) {
            if (this.mSubDomains != null) {
                sb.append(".{");
                for (Entry<String, Label> entry : this.mSubDomains.entrySet()) {
                    sb.append((String) entry.getKey());
                    ((Label) entry.getValue()).toString(sb);
                }
                sb.append('}');
                return;
            }
            sb.append('=').append(this.mMatch);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            toString(sb);
            return sb.toString();
        }
    }

    /*  JADX ERROR: NullPointerException in pass: EnumVisitor
        java.lang.NullPointerException
        	at jadx.core.dex.visitors.EnumVisitor.visit(EnumVisitor.java:102)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
        	at jadx.core.dex.visitors.DepthTraversal.lambda$visit$0(DepthTraversal.java:13)
        	at java.util.ArrayList.forEach(ArrayList.java:1251)
        	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:13)
        	at jadx.core.ProcessClass.process(ProcessClass.java:32)
        	at jadx.core.ProcessClass.lambda$processDependencies$0(ProcessClass.java:51)
        	at java.lang.Iterable.forEach(Iterable.java:75)
        	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:51)
        	at jadx.core.ProcessClass.process(ProcessClass.java:37)
        	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
        	at jadx.api.JavaClass.decompile(JavaClass.java:62)
        	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
        */
    public enum Match {
        ;

        /*  JADX ERROR: Method load error
            jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.Match.<clinit>():void, dex: 
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
        static {
            /*
            // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.Match.<clinit>():void, dex: 
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.pps.DomainMatcher.Match.<clinit>():void");
        }
    }

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.<clinit>():void, dex: 
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
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.wifi.hotspot2.pps.DomainMatcher.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.wifi.hotspot2.pps.DomainMatcher.<clinit>():void");
    }

    public DomainMatcher(List<String> primary, List<List<String>> secondary) {
        this.mRoot = new Label(Match.None, null);
        for (List<String> secondaryLabel : secondary) {
            this.mRoot.addDomain(secondaryLabel.iterator(), Match.Secondary);
        }
        this.mRoot.addDomain(primary.iterator(), Match.Primary);
    }

    public Match isSubDomain(List<String> domain) {
        Label label = this.mRoot;
        for (String labelString : domain) {
            label = label.getSubLabel(labelString);
            if (label == null) {
                return Match.None;
            }
            if (label.getMatch() != Match.None) {
                return label.getMatch();
            }
        }
        return Match.None;
    }

    public static boolean arg2SubdomainOfArg1(List<String> arg1, List<String> arg2) {
        if (arg2.size() < arg1.size()) {
            return false;
        }
        Iterator<String> l2 = arg2.iterator();
        for (String equals : arg1) {
            if (!equals.equals(l2.next())) {
                return false;
            }
        }
        return true;
    }

    public String toString() {
        return "Domain matcher " + this.mRoot;
    }

    public static void main(String[] args) {
        String domain;
        int i = 0;
        DomainMatcher dm1 = new DomainMatcher(Utils.splitDomain("android.google.com"), Collections.emptyList());
        for (String domain2 : TestDomains) {
            System.out.println(domain2 + ": " + dm1.isSubDomain(Utils.splitDomain(domain2)));
        }
        List<List<String>> secondaries = new ArrayList();
        secondaries.add(Utils.splitDomain("apple.com"));
        secondaries.add(Utils.splitDomain("net"));
        DomainMatcher dm2 = new DomainMatcher(Utils.splitDomain("android.google.com"), secondaries);
        String[] strArr = TestDomains;
        int length = strArr.length;
        while (i < length) {
            domain2 = strArr[i];
            System.out.println(domain2 + ": " + dm2.isSubDomain(Utils.splitDomain(domain2)));
            i++;
        }
    }
}
