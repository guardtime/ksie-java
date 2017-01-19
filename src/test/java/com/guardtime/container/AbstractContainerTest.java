package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.document.ContainerDocument;
import com.guardtime.container.document.StreamContainerDocument;
import com.guardtime.container.hash.HashAlgorithmProvider;
import com.guardtime.container.indexing.IndexProviderFactory;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DocumentsManifest;
import com.guardtime.container.manifest.Manifest;
import com.guardtime.container.manifest.ManifestFactoryType;
import com.guardtime.container.manifest.SingleAnnotationManifest;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.Pair;
import com.guardtime.container.verification.rule.state.DefaultRuleStateProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.when;

public class AbstractContainerTest {

    /**
     * Containers - Internally correct and does verify against anchors.
     */
    protected static final String CONTAINER_WITH_NO_DOCUMENTS = "containers/container-no-documents.ksie";
    protected static final String CONTAINER_WITH_ONE_DOCUMENT = "containers/container-one-document.ksie";
    protected static final String CONTAINER_WITH_UNKNOWN_FILES = "containers/container-unknown-files.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_SIGNATURES = "containers/container-multiple-signatures.ksie";
    protected static final String CONTAINER_WITH_RANDOM_UUID_INDEXES = "containers/container-random-uuid-indexes.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_ANNOTATIONS = "containers/container-multiple-annotations.ksie";
    protected static final String CONTAINER_WITH_MIXED_INDEX_TYPES = "containers/container-content-with-mixed-index-types.ksie";
    protected static final String CONTAINER_WITH_NON_REMOVABLE_ANNOTATION = "containers/container-with-non-removable-annotation.ksie";
    protected static final String CONTAINER_WITH_RANDOM_INCREMENTING_INDEXES = "containers/multi-content-random-incrementing-indexes.ksie";
    protected static final String CONTAINER_WITH_MIXED_INDEX_TYPES_IN_CONTENTS = "containers/container-contents-with-different-index-types.ksie";

    /**
     * Containers - Internally invalid or does not verify against anchors.
     */
    protected static final String EMPTY_CONTAINER = "containers/invalid/container-empty.ksie";
    protected static final String CONTAINER_WITH_MISSING_MANIFEST = "containers/invalid/container-missing-manifest.ksie";
    protected static final String CONTAINER_WITH_MISSING_MIMETYPE = "containers/invalid/container-missing-mimetype.ksie";
    protected static final String CONTAINER_WITH_CHANGED_DOCUMENT = "containers/invalid/container-changed-document.ksie";
    protected static final String CONTAINER_WITH_MISSING_SIGNATURE = "containers/invalid/container-missing-signature.ksie";
    protected static final String CONTAINER_WITH_MIMETYPE_IS_EMPTY = "containers/invalid/container-mimetype-is-empty.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION = "containers/invalid/container-missing-annotation.ksie";
    protected static final String CONTAINER_WITH_WRONG_SIGNATURE_FILE = "containers/invalid/container-wrong-signature-file.ksie";
    protected static final String CONTAINER_WITH_MISSING_DOCUMENTS_MANIFEST = "containers/container-with-missing-datamanifest.ksie";
    protected static final String CONTAINER_WITH_CONTAINS_ONLY_MANIFEST = "containers/invalid/container-contains-only-manifest.ksie";
    protected static final String CONTAINER_WITH_CHANGED_ANNOTATION_DATA = "containers/invalid/container-changed-annotation-data.ksie";
    protected static final String CONTAINER_WITH_MISSING_ANNOTATION_DATA = "containers/invalid/container-missing-annotation-data.ksie";
    protected static final String CONTAINER_WITH_INVALID_ANNOTATION_TYPE = "containers/invalid/container-invalid-annotation-type.ksie";
    protected static final String CONTAINER_WITH_BROKEN_SIGNATURE_CONTENT = "containers/invalid/container-broken-signature-content.ksie";
    protected static final String CONTAINER_WITH_DOCUMENT_MISSING_MIMETYPE = "containers/invalid/container-document-missing-mimetype.ksie";
    protected static final String CONTAINER_WITH_NO_DOCUMENT_URI_IN_MANIFEST = "containers/invalid/container-no-document-uri-in-manifest.ksie";
    protected static final String CONTAINER_WITH_MIMETYPE_CONTAINS_INVALID_VALUE = "containers/invalid/container-mimetype-contains-invalid-value.ksie";
    protected static final String CONTAINER_WITH_MULTIPLE_EXTENDABLE_SIGNATURES = "containers/invalid/container-multiple-signatures-non-verifying.ksie";
    protected static final String CONTAINER_WITH_MIMETYPE_CONTAINS_ADDITIONAL_VALUE = "containers/invalid/container-mimetype-contains-additional-value.ksie";
    protected static final String CONTAINER_WITH_TWO_CONTENTS_AND_ONE_MANIFEST_REMOVED = "containers/invalid/container-two-contents-one-manifest-removed.ksie";
    protected static final String CONTAINER_WITH_CHANGED_SIGNATURE_FILE = "containers/invalid/container-invalid-signature-from-last-aggregation-hash-chain.ksie";
    protected static final String CONTAINER_WITH_CHANGED_DATAMANIFEST_HASH_IN_MANIFEST = "containers/invalid/container-changed-datamanifest-hash-in-manifest.ksie";
    protected static final String CONTAINER_WITH_MULTI_CONTENT_ONE_SIGNATURE_IS_INVALID = "containers/invalid/container-multi-content-one-signature-is-invalid.ksie";
    protected static final String CONTAINER_WITH_MULTI_CONTENT_ONE_IS_MISSING_DATAMANIFEST = "containers/invalid/multi-content-one-content-is-missing-datamanifest.ksie";
    protected static final String CONTAINER_WITH_CHANGED_ANNOTATIONS_MANIFEST_HASH_IN_MANIFEST = "containers/invalid/container-changed-annotations-manifest-hash-in-manifest.ksie";
    protected static final String CONTAINER_WITH_CHANGED_AND_EXTENDED_SIGNATURE_FILE = "containers/invalid/container-invalid-signature-from-last-aggregation-hash-chain-extended.ksie";
    protected static final String CONTAINER_WITH_CHANGED_DATAMANIFEST_HASH_IN_ANNOTATION_MANIFEST = "containers/invalid/container-changed-datamanifest-hash-in-annotation-manifest.ksie";
    protected static final String CONTAINER_WITH_CHANGED_ANNOTATION_MANIFEST_HASH_IN_ANNOTATIONS_MANIFEST = "containers/invalid/container-changed-annotation-manifest-hash-in-annotations-manifest.ksie";

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

    protected final DefaultRuleStateProvider defaultRuleStateProvider = new DefaultRuleStateProvider();

    protected ContainerAnnotation STRING_CONTAINER_ANNOTATION;
    protected ContainerDocument TEST_DOCUMENT_HELLO_TEXT;
    protected ContainerDocument TEST_DOCUMENT_HELLO_PDF;
    protected final List<AutoCloseable> containerElements = new LinkedList<>();

    @Before
    public void setUpDocumentsAndAnnotations() {
        STRING_CONTAINER_ANNOTATION  = new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME);
        TEST_DOCUMENT_HELLO_TEXT = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
        TEST_DOCUMENT_HELLO_PDF = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_PDF_CONTENT), MIME_TYPE_APPLICATION_PDF, TEST_FILE_NAME_TEST_PDF);
        containerElements.addAll(Arrays.asList(
                TEST_DOCUMENT_HELLO_PDF,
                TEST_DOCUMENT_HELLO_TEXT,
                STRING_CONTAINER_ANNOTATION));
    }

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
    protected SingleAnnotationManifest mockedSingleAnnotationManifest;

    @Mock
    protected Manifest mockedManifest;

    @Mock
    protected HashAlgorithmProvider mockHashAlgorithmProvider;

    @Mock
    protected IndexProviderFactory mockIndexProviderFactory;

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

    @After
    public void tearDown() throws Exception {
        closeAll(containerElements);
    }

    protected void closeAll(Collection<? extends AutoCloseable> list) throws Exception {
        for (AutoCloseable c : list) {
            c.close();
        }
    }

    protected File loadFile(String filePath) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        return new File(url.toURI());
    }
}
