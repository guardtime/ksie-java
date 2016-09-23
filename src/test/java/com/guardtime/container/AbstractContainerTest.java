package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.indexing.IndexProvider;
import com.guardtime.container.manifest.*;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.rule.DefaultRuleStateProvider;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;

public class AbstractContainerTest {

    protected static final String EMPTY_CONTAINER = "containers/container-empty.ksie";
    protected static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    protected static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-document.ksie";
    protected static final String CONTAINER_WITH_UNKNOWN_FILES = "containers/container-unknown-files.ksie";
    protected static final String CONTAINER_WITH_BROKEN_SIGNATURE = "containers/container-broken-signature.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION = "containers/container-missing-annotation.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    protected static final String CONTAINER_WITH_WRONG_SIGNATURE_FILE = "containers/container-wrong-signature-file.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION_DATA = "containers/container-missing-annotation-data.ksie";
    protected static final String CONTAINERS_CONTAINER_INVALID_ANNOTATION_TYPE = "containers/container-invalid-annotation-type.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_EXTENDABLE_SIGNATURES = "containers/container-multiple-signatures-non-verifying.ksie";
    protected static final String CONTAINERS_CONTAINER_DOCUMENT_MISSING_MIMETYPE = "containers/container-document-missing-mimetype.ksie";
    protected static final String CONTAINERS_CONTAINER_NO_DOCUMENT_URI_IN_MANIFEST = "containers/container-no-document-uri-in-manifest.ksie";

    protected static final String MIME_TYPE_APPLICATION_TXT = "application/txt";
    protected static final String MIME_TYPE_APPLICATION_PDF = "application/pdf";
    protected static final String SIGNATURE_MIME_TYPE = "application/ksi-signature";
    protected static final String TEST_FILE_PATH_TEST_TXT = "test-data-files/test.txt";
    protected static final String TEST_FILE_NAME_TEST_DOC = "test.doc";
    protected static final String TEST_FILE_NAME_TEST_TXT = "test.txt";
    protected static final String TEST_FILE_NAME_TEST_PDF = "test.pdf";
    protected static final byte[] TEST_DATA_TXT_CONTENT = new byte[200];

    protected static final byte[] TEST_DATA_PDF_CONTENT = new byte[256];
    protected static final String ANNOTATION_DOMAIN_COM_GUARDTIME = "com.guardtime";

    protected static final String ANNOTATION_CONTENT = "42";
    protected static final String DOCUMENTS_MANIFEST_URI = "/META-INF/datamanifest-1.tlv";

    protected static final String ANNOTATIONS_MANIFEST_URI = "/META-INF/annotmanifest-1.tlv";
    protected static final ContainerDocument TEST_DOCUMENT_HELLO_TEXT = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
    protected static final ContainerDocument TEST_DOCUMENT_HELLO_PDF = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_PDF_CONTENT), MIME_TYPE_APPLICATION_PDF, TEST_FILE_NAME_TEST_PDF);
    protected static final ContainerAnnotation MOCKED_ANNOTATION = new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME);

    protected final DefaultRuleStateProvider defaultRuleStateProvider = new DefaultRuleStateProvider();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    protected SignatureFactory mockedSignatureFactory;

    @Mock
    protected ContainerManifestFactory mockedManifestFactory;

    @Mock
    protected ManifestFactoryType mockedManifestFactoryType;

    @Mock
    protected SignatureFactoryType mockedSignatureFactoryType;

    @Mock
    protected DocumentsManifest mockedDocumentsManifest;

    @Mock
    protected AnnotationsManifest mockedAnnotationsManifest;

    @Mock
    private SingleAnnotationManifest mockedSingleAnnotationManifest;

    @Mock
    private Manifest mockedManifest;

    @Mock
    private HashAlgorithmProvider mockHashAlgorithmProvider;

    @Mock
    protected IndexProvider mockIndexProvider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockedManifestFactory.getManifestFactoryType()).thenReturn(mockedManifestFactoryType);
        when(mockedManifestFactory.getHashAlgorithmProvider()).thenReturn(mockHashAlgorithmProvider);
        when(mockedManifestFactory.createDocumentsManifest(anyListOf(ContainerDocument.class))).thenReturn(mockedDocumentsManifest);
        when(mockedManifestFactory.createAnnotationsManifest(anyMap())).thenReturn(mockedAnnotationsManifest);
        when(mockedManifestFactory.createSingleAnnotationManifest(Mockito.any(Pair.class), Mockito.any(Pair.class))).thenReturn(mockedSingleAnnotationManifest);
        when(mockedManifestFactory.createManifest(Mockito.any(Pair.class), Mockito.any(Pair.class), Mockito.any(Pair.class))).thenReturn(mockedManifest);
        when(mockedSignatureFactory.getSignatureFactoryType()).thenReturn(mockedSignatureFactoryType);
        when(mockedSignatureFactoryType.getSignatureMimeType()).thenReturn(SIGNATURE_MIME_TYPE);
    }


    protected File loadFile(String filePath) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        return new File(url.toURI());
    }

}
