package com.guardtime.container;

import com.guardtime.container.indexing.IndexProvider;
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
    protected static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-document.ksie";
    protected static final String EMPTY_CONTAINER = "containers/container-empty.ksie";
    protected static final String CONTAINER_WITH_UNKOWN_FILES = "containers/container-unkown-files.ksie";
    protected static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_EXTENDABLE_SIGNATURES = "containers/container-multiple-signatures-non-verifying.ksie";
    protected static final String CONTAINER_WITH_BROKEN_SIGNATURE = "containers/container-broken-signature.ksie";
    protected static final String CONTAINER_WITH_WRONG_SIGNATURE_FILE = "containers/container-wrong-signature-file.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION = "containers/container-missing-annotation.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION_DATA = "containers/container-missing-annotation-data.ksie";
    protected static final String CONTAINERS_CONTAINER_INVALID_ANNOTATION_TYPE = "containers/container-invalid-annotation-type.ksie";
    protected static final String CONTAINERS_CONTAINER_DOCUMENT_MISSING_MIMETYPE = "containers/container-document-missing-mimetype.ksie";
    protected static final String CONTAINERS_CONTAINER_NO_DOCUMENT_URI_IN_MANIFEST = "containers/container-no-document-uri-in-manifest.ksie";

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
        packagingFactory = new ZipContainerPackagingFactory(signatureFactory, manifestFactory, Mockito.mock(IndexProvider.class));
    }
}
