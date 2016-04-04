package com.guardtime.container.verification;

import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactoryType;
import com.guardtime.container.verification.ContainerVerifier;
import com.guardtime.container.verification.context.SimpleVerificationContext;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.DefaultVerificationPolicy;
import com.guardtime.container.verification.report.ContainerVerificationReport;
import com.guardtime.container.verification.report.generic.GenericContainerVerificationReportFactory;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.container.verification.rule.Rule;
import com.guardtime.container.verification.rule.generic.MimeTypeIntegrityRule;
import com.guardtime.container.verification.rule.generic.ksi.KsiPolicyBasedSignatureIntegrityRule;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.unisignature.KSISignature;
import com.guardtime.ksi.unisignature.verifier.VerificationResult;
import com.guardtime.ksi.unisignature.verifier.policies.KeyBasedVerificationPolicy;
import com.guardtime.ksi.unisignature.verifier.policies.Policy;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class VerificationTest {

    private static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    private ZipContainerPackagingFactory factory;

    @Mock
    private SignatureFactory mockSignatureFactory;

    @Mock
    private KSI mockKSI;

    @Mock
    private VerificationResult mockResult;

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
        MockitoAnnotations.initMocks(this);
        when(mockSignatureFactory.getSignatureFactoryType()).thenReturn(new KsiSignatureFactoryType());
        when(mockSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedSignature);
        when(mockKSI.read(Mockito.any(byte[].class))).thenReturn(Mockito.mock(KSISignature.class));
        when(mockKSI.verify(Mockito.any(KSISignature.class), Mockito.any(Policy.class))).thenReturn(mockResult);
        TlvContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
        factory = new ZipContainerPackagingFactory(mockSignatureFactory, manifestFactory);
    }

    private VerificationContext getVerificationContext(String containerPath) throws IOException, URISyntaxException, InvalidPackageException {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(containerPath).toURI()));
        BlockChainContainer container = factory.read(input);
        return new SimpleVerificationContext(container);
    }

    private DefaultVerificationPolicy getDefaultVerificationPolicy() {
        return new DefaultVerificationPolicy(Arrays.asList((Rule)
                new MimeTypeIntegrityRule(factory),
                new KsiPolicyBasedSignatureIntegrityRule(mockKSI, new KeyBasedVerificationPolicy())
        ));
    }

    private VerifierResult getGenericVerifierResult() throws IOException, URISyntaxException, InvalidPackageException {
        VerificationContext context = getVerificationContext(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        DefaultVerificationPolicy policy = getDefaultVerificationPolicy();
        ContainerVerifier verifier = new ContainerVerifier(policy);
        return verifier.verify(context);
    }

    private void setSignatureVerificationResult(boolean result) {
        when(mockResult.isOk()).thenReturn(result);
    }

    @Test
    public void testGenericVerificationWithValidContainer() throws Exception {
        setSignatureVerificationResult(true);
        VerifierResult result = getGenericVerifierResult();

        assertEquals(RuleResult.OK, result.getVerificationResult());
    }

    @Test
    public void testGenericVerificationWithBrokenContainer() throws Exception {
        setSignatureVerificationResult(false);
        VerifierResult result = getGenericVerifierResult();

        assertEquals(RuleResult.NOK, result.getVerificationResult());
    }

    @Test
    public void testGenericContainerVerificationReportWithValidContainer() throws Exception {
        when(mockResult.isOk()).thenReturn(true);
        ContainerVerificationReport report = new GenericContainerVerificationReportFactory().create(getGenericVerifierResult());
        assertNotNull(report);
        assertEquals(RuleResult.OK, report.getResult());
        assertTrue(report.getUsedRules().size() > 0);
        assertTrue(report.getAffectedFiles().size() > 0);
    }
}
