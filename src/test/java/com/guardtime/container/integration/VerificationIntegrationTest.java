package com.guardtime.container.integration;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.result.ContainerVerifierResult;
import com.guardtime.container.verification.result.SignatureResult;
import com.guardtime.container.verification.result.VerificationResult;
import com.guardtime.container.verification.rule.signature.SignatureVerifier;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.hashing.HashAlgorithm;
import com.guardtime.ksi.unisignature.KSISignature;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.FileInputStream;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

public class VerificationIntegrationTest extends AbstractCommonIntegrationTest {
    @Mock
    private KSISignature mockedKsiSignature;

    @Mock
    private SignatureVerifier<KSISignature> mockSignatureVerifier;

    private DataHash nullDataHash;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        nullDataHash = new DataHash(HashAlgorithm.SHA2_256, new byte[32]);
        ContainerSignature mockedContainerSignature = Mockito.mock(ContainerSignature.class);
        when(mockedSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedContainerSignature);
        when(mockSignatureVerifier.isSupported(Mockito.any(ContainerSignature.class))).thenReturn(true);
        when(mockedContainerSignature.getSignature()).thenReturn(mockedKsiSignature);
        when(mockKsi.read(any(InputStream.class))).thenReturn(mockedKsiSignature);
        when(mockedKsiSignature.getInputHash()).thenReturn(nullDataHash);
        ContainerManifestFactory manifestFactorySpy = spy(manifestFactory);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                Manifest spyManifest = (Manifest) spy(invocationOnMock.callRealMethod());
                doReturn(nullDataHash).when(spyManifest).getDataHash(any(HashAlgorithm.class));
                return spyManifest;
            }
        }).when(manifestFactorySpy).readManifest(any(InputStream.class));
        this.packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactorySpy);
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

    private void setSignatureVerificationResult(final VerificationResult result) throws Exception {
        when(mockSignatureVerifier.getSignatureVerificationResult(Mockito.any(KSISignature.class), Mockito.any(Manifest.class))).
                thenReturn(new SignatureResult() {
                    @Override
                    public VerificationResult getSimplifiedResult() {
                        return result;
                    }

                    @Override
                    public Object getSignature() {
                        return null;
                    }

                    @Override
                    public Object getFullResult() {
                        return null;
                    }
                });
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
