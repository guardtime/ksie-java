package com.guardtime.container.integration;

import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class VerificationIntegrationTest extends AbstractCommonIntegrationTest {

    @Mock
    private KSISignature mockedKsiSignature;

    @Mock
    private SignatureVerifier<KSISignature> mockSignatureVerifier;

    private DataHash mockedDataHash;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockSignatureVerifier.isSupported(Mockito.any(ContainerSignature.class))).thenReturn(true);
        when(mockedSignatureFactoryType.getSignatureFileExtension()).thenReturn("ksi");
        ContainerSignature mockedContainerSignature = Mockito.mock(ContainerSignature.class);
        when(mockedContainerSignature.getSignature()).thenReturn(mockedKsiSignature);
        when(mockedSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedContainerSignature);
        when(mockedKsiSignature.getInputHash()).thenReturn(new DataHash(HashAlgorithm.SHA2_256, new byte[32]));
        mockedDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        when(mockedKsiSignature.getInputHash()).thenReturn(mockedDataHash);
        this.packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, manifestFactory);
    }

    private DefaultVerificationPolicy getDefaultVerificationPolicy() {
        return new DefaultVerificationPolicy(
                defaultRuleStateProvider,
                mockSignatureVerifier,
                packagingFactory
        );
    }

    private ContainerVerifierResult getGenericVerifierResult(String path) throws Exception {
        DefaultVerificationPolicy policy = getDefaultVerificationPolicy();
        ContainerVerifier verifier = new ContainerVerifier(policy);
        InputStream input = new FileInputStream(loadFile(path));
        Container container = packagingFactory.read(input);
        return verifier.verify(container);
    }

    private void setSignatureVerificationResult(VerificationResult result) throws Exception {
        when(mockSignatureVerifier.getSignatureVerificationResult(Mockito.any(KSISignature.class), Mockito.any(Manifest.class))).
                thenReturn(result);
    }

    @Test
    public void testGenericVerificationWithValidContainer() throws Exception {
        setSignatureVerificationResult(VerificationResult.OK);
        ContainerVerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MULTIPLE_SIGNATURES);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testGenericVerificationWithBrokenContainer() throws Exception {
        setSignatureVerificationResult(VerificationResult.NOK);
        ContainerVerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MULTIPLE_SIGNATURES);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }
}
