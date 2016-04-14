package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.annotation.ContainerAnnotationType;
import com.guardtime.container.annotation.StringContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.datafile.StreamContainerDocument;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.ContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import com.guardtime.container.signature.SignatureFactoryType;
import com.guardtime.container.util.Pair;
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
    protected static final String DATAFILES_MANIFEST_URI = "/META-INF/datamanifest1.tlv";

    protected static final String ANNOTATIONS_MANIFEST_URI = "/META-INF/annotmanifest1.tlv";
    protected static final ContainerDocument TEST_DOCUMENT_HELLO_TEXT = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_TXT_CONTENT), MIME_TYPE_APPLICATION_TXT, TEST_FILE_NAME_TEST_TXT);
    protected static final ContainerDocument TEST_DOCUMENT_HELLO_PDF = new StreamContainerDocument(new ByteArrayInputStream(TEST_DATA_PDF_CONTENT), MIME_TYPE_APPLICATION_PDF, TEST_FILE_NAME_TEST_PDF);
    protected static final ContainerAnnotation MOCKED_ANNOTATION = new StringContainerAnnotation(ContainerAnnotationType.NON_REMOVABLE, ANNOTATION_CONTENT, ANNOTATION_DOMAIN_COM_GUARDTIME);

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    protected SignatureFactory mockedSignatureFactory;

    @Mock
    protected ContainerManifestFactory mockedManifestFactory;

    @Mock
    protected ContainerPackagingFactory mockedPackagingFactory;

    @Mock
    protected ManifestFactoryType mockedManifestFactoryType;

    @Mock
    protected SignatureFactoryType mockedSignatureFactoryType;

    @Mock
    protected DataFilesManifest mockedDataFilesManifest;

    @Mock
    protected AnnotationsManifest mockedAnnotationsManifest;

    @Mock
    private AnnotationInfoManifest mockedAnnotationInfoManifest;

    @Mock
    private SignatureManifest mockedSignatureManifest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockedManifestFactory.getManifestFactoryType()).thenReturn(mockedManifestFactoryType);
        when(mockedManifestFactory.createDataFilesManifest(anyListOf(ContainerDocument.class))).thenReturn(mockedDataFilesManifest);
        when(mockedManifestFactory.createAnnotationsManifest(anyMap())).thenReturn(mockedAnnotationsManifest);
        when(mockedManifestFactory.createAnnotationInfoManifest(Mockito.any(Pair.class), Mockito.any(Pair.class))).thenReturn(mockedAnnotationInfoManifest);
        when(mockedManifestFactory.createSignatureManifest(Mockito.any(Pair.class), Mockito.any(Pair.class), Mockito.any(Pair.class))).thenReturn(mockedSignatureManifest);
        when(mockedSignatureFactory.getSignatureFactoryType()).thenReturn(mockedSignatureFactoryType);
        when(mockedSignatureFactoryType.getSignatureMimeType()).thenReturn(SIGNATURE_MIME_TYPE);
    }


    protected File loadFile(String filePath) throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource(filePath);
        return new File(url.toURI());
    }

}
