package com.mediatek.simservs.xcap;

import com.mediatek.simservs.client.policy.Rule;
import com.mediatek.simservs.client.policy.RuleSet;

public interface RuleType {
    RuleSet createNewRuleSet();

    RuleSet getRuleSet();

    void saveRule(Rule rule) throws XcapException;

    void saveRule(String str) throws XcapException;

    void saveRuleSet() throws XcapException;
}
