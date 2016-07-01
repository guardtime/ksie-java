package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.rule.Rule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.LinkedList;

import static org.junit.Assert.*;

public class ContainerVerifierResultTest {
    private static final Container MOCK_CONTAINER = Mockito.mock(Container.class);
    private static final LinkedList<RuleVerificationResult> VERIFICATION_RESULTS = new LinkedList<>();
    private ContainerVerifierResult result;

    @Before
    public void setUp() throws Exception {
        VERIFICATION_RESULTS.add(new GenericVerificationResult(VerificationResult.OK, Mockito.mock(Rule.class), "some-element"));
        VERIFICATION_RESULTS.add(new GenericVerificationResult(VerificationResult.NOK, Mockito.mock(Rule.class), "some-other-element"));
        this.result = new ContainerVerifierResult(MOCK_CONTAINER, VERIFICATION_RESULTS);
    }

    @Test
    public void getResults() throws Exception {
        assertNotNull(result.getResults());
        assertEquals(VERIFICATION_RESULTS, result.getResults());
        assertNotEquals(0, result.getResults().size());
    }

    @Test
    public void getVerificationResult() throws Exception {
        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }

    @Test
    public void getContainer() throws Exception {
        assertNotNull(result.getContainer());
        assertEquals(MOCK_CONTAINER, result.getContainer());
    }

}