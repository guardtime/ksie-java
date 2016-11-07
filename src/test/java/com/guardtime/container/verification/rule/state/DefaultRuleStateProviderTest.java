package com.guardtime.container.verification.rule.state;

import com.guardtime.container.verification.rule.RuleType;

import org.junit.Test;

import java.util.UUID;

import static org.junit.Assert.*;

public class DefaultRuleStateProviderTest {
    private RuleStateProvider provider = new DefaultRuleStateProvider();

    @Test
    public void testGetStateForRuleReturnFailForValidRuleName() throws Exception {
        assertEquals(RuleState.FAIL, provider.getStateForRule(RuleType.KSIE_FORMAT.getName()));
    }

    @Test
    public void testGetStateForRuleReturnFailForRandomString() throws Exception {
        assertEquals(RuleState.FAIL, provider.getStateForRule(UUID.randomUUID().toString()));
    }

}