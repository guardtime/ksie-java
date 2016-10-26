package com.guardtime.container.verification.rule;

import com.guardtime.container.verification.result.ResultHolder;
import com.guardtime.container.verification.rule.state.RuleState;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class RuleTest {
    @Test
    public void testVerifySkipsVerificationForIgnoreState() throws Exception {
        TestRule ruleSpy = spy(new TestRule(RuleState.IGNORE));
        assertFalse(ruleSpy.verify(Mockito.mock(ResultHolder.class), ""));
        verify(ruleSpy, never()).verifyRule(Mockito.any(ResultHolder.class), Mockito.any());

    }

    private class TestRule extends AbstractRule {
        TestRule(RuleState state) {
            super(state);
        }

        @Override
        public void verifyRule(ResultHolder holder, Object verifiable) throws RuleTerminatingException {
        }
    }

}