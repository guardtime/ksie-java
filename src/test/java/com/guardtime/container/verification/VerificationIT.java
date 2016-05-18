package com.guardtime.container.verification;

import com.guardtime.container.AbstractCommonIntegrationTest;
import com.guardtime.container.packaging.Container;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.verification.context.SimpleVerificationContext;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.generic.ksi.KsiPolicyBasedSignatureIntegrityRule;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.KeyBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class VerificationIT extends AbstractCommonIntegrationTest {

    @Mock
    private VerificationResult mockUnisignatureVerificationResult;

    private ContainerSignature mockedSignature = new ContainerSignature() {

        @Override
        public void writeTo(OutputStream output) {
            try {
                output.write("TEST-SIGNATURE".getBytes());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockKsi.verify(Mockito.any(KSISignature.class), Mockito.any(Policy.class))).thenReturn(mockUnisignatureVerificationResult);

        when(mockedSignatureFactoryType.getSignatureFileExtension()).thenReturn("ksi");
        when(mockedSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedSignature);
        this.packagingFactory = new ZipContainerPackagingFactory(mockedSignatureFactory, manifestFactory);
    }

    private VerificationContext getVerificationContext(String containerPath) throws Exception {
        InputStream input = new FileInputStream(loadFile(containerPath));
        Container container = packagingFactory.read(input);
        return new SimpleVerificationContext(container);
    }

    private DefaultVerificationPolicy getDefaultVerificationPolicy() {
        return new DefaultVerificationPolicy(Arrays.asList((Rule)
                        new MimeTypeIntegrityRule(packagingFactory),
                new KsiPolicyBasedSignatureIntegrityRule(mockKsi, new KeyBasedVerificationPolicy())
        ));
    }

    private VerifierResult getGenericVerifierResult(String path) throws Exception {
        VerificationContext context = getVerificationContext(path);
        DefaultVerificationPolicy policy = getDefaultVerificationPolicy();
        ContainerVerifier verifier = new ContainerVerifier(policy);
        return verifier.verify(context);
    }

    private void setSignatureVerificationResult(boolean result) {
        when(mockUnisignatureVerificationResult.isOk()).thenReturn(result);
    }

    @Test
    public void testGenericVerificationWithValidContainer() throws Exception {
        setSignatureVerificationResult(true);
        VerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MULTIPLE_SIGNATURES);

        assertEquals(RuleResult.OK, result.getVerificationResult());
    }

    @Test
    public void testGenericVerificationWithBrokenContainer() throws Exception {
        setSignatureVerificationResult(false);
        VerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MULTIPLE_SIGNATURES);

        assertEquals(RuleResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testGenericVerification_ContainerWithMissingAnnotation() throws Exception {
        setSignatureVerificationResult(true);
        VerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MISSING_ANNOTATION);

        assertEquals(RuleResult.OK, result.getVerificationResult());
    }

    @Test
    public void testGenericVerification_ContainerWithMissingAnnotationData() throws Exception {
        setSignatureVerificationResult(true);
        VerifierResult result = getGenericVerifierResult(CONTAINER_WITH_MISSING_ANNOTATION_DATA);

        assertEquals(RuleResult.OK, result.getVerificationResult());
    }
}
