package com.guardtime.container;

import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.tlv.TlvContainerManifestFactory;
import com.guardtime.container.packaging.zip.ZipContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.ksi.KsiSignatureFactory;
import com.guardtime.ksi.KSI;
import com.guardtime.ksi.hashing.DataHash;
import com.guardtime.ksi.unisignature.KSISignature;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class AbstractCommonIntegrationTest extends AbstractContainerTest {
    protected static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-file.ksie";
    protected static final String EMPTY_CONTAINER = "containers/container-empty.ksie";
    protected static final String CONTAINER_WITH_EXTRA_FILES = "containers/container-extra-files.ksie";
    protected static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    protected static final String CONTAINER_WITH_BROKEN_SIGNATURE = "containers/container-broken-signature.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION = "containers/container-missing-annotation.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION_DATA = "containers/container-missing-annotation-data.ksie";

    protected ContainerManifestFactory manifestFactory = new TlvContainerManifestFactory();
    protected SignatureFactory signatureFactory;
    protected ZipContainerPackagingFactory packagingFactory;

    @Mock
    protected KSI mockKsi;

    @Before
    public void setUp() throws Exception {
        super.setUp();
        when(mockKsi.sign(Mockito.any(DataHash.class))).thenReturn(Mockito.mock(KSISignature.class));
        when(mockKsi.extend(Mockito.any(KSISignature.class))).thenReturn(Mockito.mock(KSISignature.class));
        signatureFactory = new KsiSignatureFactory(mockKsi);
        packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory);
    }
}
