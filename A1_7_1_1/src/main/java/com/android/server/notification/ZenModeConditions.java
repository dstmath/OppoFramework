package com.android.server.notification;

import android.content.ComponentName;
import android.net.Uri;
import android.service.notification.Condition;
import android.service.notification.IConditionProvider;
import android.service.notification.ZenModeConfig;
import android.service.notification.ZenModeConfig.ZenRule;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import com.android.server.notification.ConditionProviders.Callback;
import com.android.server.secrecy.policy.DecryptTool;
import java.io.PrintWriter;
import java.util.Objects;

/*  JADX ERROR: NullPointerException in pass: ReSugarCode
    java.lang.NullPointerException
    	at jadx.core.dex.visitors.ReSugarCode.initClsEnumMap(ReSugarCode.java:159)
    	at jadx.core.dex.visitors.ReSugarCode.visit(ReSugarCode.java:44)
    	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:12)
    	at jadx.core.ProcessClass.process(ProcessClass.java:32)
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
    	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:292)
    	at jadx.api.JavaClass.decompile(JavaClass.java:62)
    	at jadx.api.JadxDecompiler.lambda$appendSourcesSave$0(JadxDecompiler.java:200)
    */
public class ZenModeConditions implements Callback {
    private static final boolean DEBUG = false;
    private static final String TAG = "ZenModeHelper";
    private final ConditionProviders mConditionProviders;
    private boolean mFirstEvaluation;
    private final ZenModeHelper mHelper;
    private final ArrayMap<Uri, ComponentName> mSubscriptions;

    /*  JADX ERROR: Method load error
        jadx.core.utils.exceptions.DecodeException: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.ZenModeConditions.<clinit>():void, dex: 
        	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:118)
        	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:248)
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
        	... 5 more
        */
    static {
        /*
        // Can't load method instructions: Load method exception: bogus opcode: 0073 in method: com.android.server.notification.ZenModeConditions.<clinit>():void, dex: 
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.notification.ZenModeConditions.<clinit>():void");
    }

    public ZenModeConditions(ZenModeHelper helper, ConditionProviders conditionProviders) {
        this.mSubscriptions = new ArrayMap();
        this.mFirstEvaluation = true;
        this.mHelper = helper;
        this.mConditionProviders = conditionProviders;
        if (this.mConditionProviders.isSystemProviderEnabled(DecryptTool.COUNTDOWN)) {
            this.mConditionProviders.addSystemProvider(new CountdownConditionProvider());
        }
        if (this.mConditionProviders.isSystemProviderEnabled("schedule")) {
            this.mConditionProviders.addSystemProvider(new ScheduleConditionProvider());
        }
        if (this.mConditionProviders.isSystemProviderEnabled("event")) {
            this.mConditionProviders.addSystemProvider(new EventConditionProvider());
        }
        this.mConditionProviders.setCallback(this);
    }

    public void dump(PrintWriter pw, String prefix) {
        pw.print(prefix);
        pw.print("mSubscriptions=");
        pw.println(this.mSubscriptions);
    }

    public void evaluateConfig(ZenModeConfig config, boolean processSubscriptions) {
        if (config != null) {
            if (!(config.manualRule == null || config.manualRule.condition == null || config.manualRule.isTrueOrUnknown())) {
                if (DEBUG) {
                    Log.d(TAG, "evaluateConfig: clearing manual rule");
                }
                config.manualRule = null;
            }
            ArraySet<Uri> current = new ArraySet();
            evaluateRule(config.manualRule, current, processSubscriptions);
            for (ZenRule automaticRule : config.automaticRules.values()) {
                evaluateRule(automaticRule, current, processSubscriptions);
                updateSnoozing(automaticRule);
            }
            for (int i = this.mSubscriptions.size() - 1; i >= 0; i--) {
                Uri id = (Uri) this.mSubscriptions.keyAt(i);
                ComponentName component = (ComponentName) this.mSubscriptions.valueAt(i);
                if (processSubscriptions && !current.contains(id)) {
                    this.mConditionProviders.unsubscribeIfNecessary(component, id);
                    this.mSubscriptions.removeAt(i);
                }
            }
            this.mFirstEvaluation = false;
        }
    }

    public void onBootComplete() {
    }

    public void onUserSwitched() {
    }

    public void onServiceAdded(ComponentName component) {
        if (DEBUG) {
            Log.d(TAG, "onServiceAdded " + component);
        }
        this.mHelper.setConfig(this.mHelper.getConfig(), "zmc.onServiceAdded");
    }

    public void onConditionChanged(Uri id, Condition condition) {
        if (DEBUG) {
            Log.d(TAG, "onConditionChanged " + id);
        }
        ZenModeConfig config = this.mHelper.getConfig();
        if (config != null) {
            boolean updated = updateCondition(id, condition, config.manualRule);
            for (ZenRule automaticRule : config.automaticRules.values()) {
                updated = (updated | updateCondition(id, condition, automaticRule)) | updateSnoozing(automaticRule);
            }
            if (updated) {
                this.mHelper.setConfig(config, "conditionChanged");
            }
        }
    }

    private void evaluateRule(ZenRule rule, ArraySet<Uri> current, boolean processSubscriptions) {
        if (rule != null && rule.conditionId != null) {
            Uri id = rule.conditionId;
            boolean isSystemCondition = false;
            for (SystemConditionProviderService sp : this.mConditionProviders.getSystemProviders()) {
                if (sp.isValidConditionId(id)) {
                    this.mConditionProviders.ensureRecordExists(sp.getComponent(), id, sp.asInterface());
                    rule.component = sp.getComponent();
                    isSystemCondition = true;
                }
            }
            if (!isSystemCondition) {
                IConditionProvider cp = this.mConditionProviders.findConditionProvider(rule.component);
                if (DEBUG) {
                    boolean z;
                    String str = TAG;
                    StringBuilder append = new StringBuilder().append("Ensure external rule exists: ");
                    if (cp != null) {
                        z = true;
                    } else {
                        z = false;
                    }
                    Log.d(str, append.append(z).append(" for ").append(id).toString());
                }
                if (cp != null) {
                    this.mConditionProviders.ensureRecordExists(rule.component, id, cp);
                }
            }
            if (rule.component == null) {
                Log.w(TAG, "No component found for automatic rule: " + rule.conditionId);
                rule.enabled = false;
                return;
            }
            if (current != null) {
                current.add(id);
            }
            if (processSubscriptions) {
                if (this.mConditionProviders.subscribeIfNecessary(rule.component, rule.conditionId)) {
                    this.mSubscriptions.put(rule.conditionId, rule.component);
                } else {
                    rule.condition = null;
                    if (DEBUG) {
                        Log.d(TAG, "zmc failed to subscribe");
                    }
                }
            }
            if (rule.condition == null) {
                rule.condition = this.mConditionProviders.findCondition(rule.component, rule.conditionId);
                if (rule.condition != null && DEBUG) {
                    Log.d(TAG, "Found existing condition for: " + rule.conditionId);
                }
            }
        }
    }

    private boolean isAutomaticActive(ComponentName component) {
        if (component == null) {
            return false;
        }
        ZenModeConfig config = this.mHelper.getConfig();
        if (config == null) {
            return false;
        }
        for (ZenRule rule : config.automaticRules.values()) {
            if (component.equals(rule.component) && rule.isAutomaticActive()) {
                return true;
            }
        }
        return false;
    }

    private boolean updateSnoozing(ZenRule rule) {
        if (rule == null || !rule.snoozing || (!this.mFirstEvaluation && rule.isTrueOrUnknown())) {
            return false;
        }
        rule.snoozing = false;
        if (DEBUG) {
            Log.d(TAG, "Snoozing reset for " + rule.conditionId);
        }
        return true;
    }

    /* JADX WARNING: Missing block: B:3:0x0005, code:
            return false;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateCondition(Uri id, Condition condition, ZenRule rule) {
        if (id == null || rule == null || rule.conditionId == null || !rule.conditionId.equals(id) || Objects.equals(condition, rule.condition)) {
            return false;
        }
        rule.condition = condition;
        return true;
    }
}
