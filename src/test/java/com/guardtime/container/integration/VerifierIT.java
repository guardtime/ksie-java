package com.guardtime.container.integration;

import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.BlockChainContainer;
import com.guardtime.container.packaging.InvalidPackageException;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.ContainerSignature;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactoryType;
import com.guardtime.container.verification.BlockChainContainerVerifier;
import com.guardtime.container.verification.context.SimpleVerificationContext;
import com.guardtime.container.verification.context.VerificationContext;
import com.guardtime.container.verification.policy.RecommendedVerificationPolicy;
import com.guardtime.container.verification.result.RuleResult;
import com.guardtime.container.verification.result.VerifierResult;
import com.guardtime.ksi.hashing.DataHash;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

public class VerifierIT {

    private static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-file.ksie";
    private static final String EMPTY_CONTAINER = "containers/container-empty.ksie";
    private static final String CONTAINER_WITH_EXTRA_FILES = "containers/container-extra-files.ksie";
    private static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    private static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    private static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    private static final String CONTAINER_WITH_BROKEN_SIGNATURE = "containers/container-broken-signature.ksie";
    private ZipContainerPackagingFactory factory;

    @Mock
    private SignatureFactory mockSignatureFactory;

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
        when(mockSignatureFactory.create(Mockito.any(DataHash.class))).thenReturn(mockedSignature);
        when(mockSignatureFactory.read(Mockito.any(InputStream.class))).thenReturn(mockedSignature);
        TlvContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
        factory = new ZipContainerPackagingFactory(mockSignatureFactory, manifestFactory);
    }

    private BlockChainContainer getContainer(String containerPath) throws IOException, URISyntaxException, InvalidPackageException {
        InputStream input = Files.newInputStream(Paths.get(ClassLoader.getSystemResource(containerPath).toURI()));
        return factory.read(input);
    }

    @Test
    public void testGenericVerification() throws Exception {
        BlockChainContainer container = getContainer(CONTAINER_WITH_MULTIPLE_SIGNATURES);
        VerificationContext context = new SimpleVerificationContext(container);
        RecommendedVerificationPolicy policy = new RecommendedVerificationPolicy();
        BlockChainContainerVerifier verifier = new BlockChainContainerVerifier(policy);
        VerifierResult result = verifier.verify(context);
        assertEquals(RuleResult.OK, result.getVerificationResult());
    }
}
