package com.guardtime.container.verification;

import com.guardtime.container.AbstractCommonIntegrationTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ksi.KsiContainerSignature;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.ksi.KsiPolicyBasedSignatureIntegrityRule;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.policies.KeyBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class VerificationIT extends AbstractCommonIntegrationTest {

    @Mock
    private com.guardtime.ksi.unisignature.verifier.VerificationResult mockUnisignatureVerificationResult;

    @Mock
    private KSISignature mockKsiSignature;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockKsi.verify(Mockito.any(KSISignature.class), Mockito.any(Policy.class), Mockito.any(DataHash.class))).thenReturn(mockUnisignatureVerificationResult);
        when(mockedSignatureFactoryType.getSignatureFileExtension()).thenReturn("ksi");
        when(mockedSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(new KsiContainerSignature(mockKsiSignature));
        when(mockKsiSignature.getInputHash()).thenReturn(new DataHash(HashAlgorithm.SHA2_256, "12345678901234567890123456789012".getBytes()));

        this.packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, manifestFactory);
    }

    private DefaultVerificationPolicy getDefaultVerificationPolicy() {
        return new DefaultVerificationPolicy(
                new KsiPolicyBasedSignatureIntegrityRule(mockKsi, new KeyBasedVerificationPolicy()),
                new MimeTypeIntegrityRule(packagingFactory)
        );
    }

    private ContainerVerifierResult getGenericVerifierResult(String path) throws Exception {
        DefaultVerificationPolicy policy = getDefaultVerificationPolicy();
        ContainerVerifier verifier = new ContainerVerifier(policy);
        InputStream input = new FileInputStream(loadFile(path));
        Container container = packagingFactory.read(input);
        return verifier.verify(container);
    }

    private void setSignatureVerificationResult(boolean result) {
        when(mockUnisignatureVerificationResult.isOk()).thenReturn(result);
    }

    @Test
    public void testGenericVerificationWithValidContainer() throws Exception {
        setSignatureVerificationResult(true);
        ContainerVerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MULTIPLE_SIGNATURES);

        assertEquals(VerificationResult.OK, result.getVerificationResult());
    }

    @Test
    public void testGenericVerificationWithBrokenContainer() throws Exception {
        setSignatureVerificationResult(false);
        ContainerVerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MULTIPLE_SIGNATURES);

        assertEquals(VerificationResult.NOK, result.getVerificationResult());
    }
}
