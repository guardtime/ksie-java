package com.guardtime.container.verification.result;

import com.guardtime.container.packaging.Container;
import com.guardtime.container.verification.rule.Rule;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class ContainerVerifierResultTest {
    private static final Container MOCK_CONTAINER = Mockito.mock(Container.class);
    private static final ResultHolder RESULT_HOLDER = new ResultHolder();
    private ContainerVerifierResult result;

    @Before
    public void setUp() throws Exception {
        RESULT_HOLDER.addResult(new GenericVerificationResult(VerificationResult.OK, Mockito.mock(Rule.class), "some-element"));
        RESULT_HOLDER.addResult(new GenericVerificationResult(VerificationResult.NOK, Mockito.mock(Rule.class), "some-other-element"));
        this.result = new ContainerVerifierResult(MOCK_CONTAINER, RESULT_HOLDER);
    }

    @Test
    public void getResults() throws Exception {
        assertNotNull(result.getResults());
        assertEquals(RESULT_HOLDER.getResults(), result.getResults());
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