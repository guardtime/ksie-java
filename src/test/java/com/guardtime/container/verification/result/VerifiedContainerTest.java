package com.guardtime.container.verification.result;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.SignatureReference;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.SignatureContent;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.rule.Rule;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.doReturn;

public class VerifiedContainerTest {
    private static final Container MOCK_CONTAINER = Mockito.mock(Container.class);
    private static final ResultHolder RESULT_HOLDER = new ResultHolder();
    private VerifiedContainer result;

    @Before
    public void setUp() throws Exception {
        RESULT_HOLDER.addResult(new GenericVerificationResult(VerificationResult.OK, Mockito.mock(Rule.class), "some-element"));
        RESULT_HOLDER.addResult(new GenericVerificationResult(VerificationResult.NOK, Mockito.mock(Rule.class), "some-other-element"));
        this.result = new VerifiedContainer(MOCK_CONTAINER, RESULT_HOLDER);
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

    @Test
    public void getSignatureResult() throws Exception {
        String path = "signature.ksig";
        SignatureContent mockSignatureContent = Mockito.mock(SignatureContent.class);
        Manifest mockManifest = Mockito.mock(Manifest.class);
        SignatureReference mockSignatureReference = Mockito.mock(SignatureReference.class);

        doReturn(Arrays.asList(mockSignatureContent)).when(MOCK_CONTAINER).getSignatureContents();
        doReturn(Pair.of("str", mockManifest)).when(mockSignatureContent).getManifest();
        doReturn(mockSignatureReference).when(mockManifest).getSignatureReference();
        doReturn(path).when(mockSignatureReference).getUri();

        RESULT_HOLDER.setSignatureResult(path, Mockito.mock(SignatureResult.class));
        VerifiedContainer result = new VerifiedContainer(MOCK_CONTAINER, RESULT_HOLDER);

        assertNotNull(result.getSignatureResult(mockSignatureContent));
    }

}