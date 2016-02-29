package com.guardtime.container;

import com.guardtime.container.manifest.AnnotationInfoManifest;
import com.guardtime.container.manifest.AnnotationsManifest;
import com.guardtime.container.manifest.ContainerManifestFactory;
import com.guardtime.container.manifest.DataFilesManifest;
import com.guardtime.container.manifest.SignatureManifest;
import com.guardtime.container.packaging.BlockChainContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;

import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.times;

public class AbstractBlockChainContainerTest {

    protected static final String MIME_TYPE_APPLICATION_TXT = "application/txt";
    protected static final String TEST_FILE_PATH_TEST_TXT = "test-data-files/test.txt";
    protected static final String TEST_FILE_NAME_TEST2_DOC = "test2.doc";
    protected static final String TEST_FILE_NAME_TEST_TXT = "test.txt";
    protected static final VerificationMode ONE_EXECUTION = times(1);
    protected static final byte[] TEST_DATA = new byte[200];

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    protected SignatureFactory mockedSignatureFactory;

    @Mock
    protected ContainerManifestFactory mockedManifestFactory;

    @Mock
    protected BlockChainContainerPackagingFactory mockedPackagingFactory;

    @Mock
    protected AnnotationInfoManifest annotationInfoManifest;

    @Mock
    protected AnnotationsManifest mockedAnnotationsManifest;

    @Mock
    protected DataFilesManifest mockedDataFileManifest;

    @Mock
    protected SignatureManifest mockedSignatureManifest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
//        when(mockedManifestFactory.createAnnotationsManifest(Mockito.anyMapOf(ContainerAnnotation.class, AnnotationInfoManifest.class), Mockito.anyString())).thenReturn(mockedAnnotationsManifest);
//        when(mockedManifestFactory.createAnnotationManifest(Mockito.any(DataFilesManifest.class), Mockito.any(ContainerAnnotation.class), Mockito.anyString())).thenReturn(annotationInfoManifest);
//        when(mockedManifestFactory.createDataFilesManifest(Mockito.anyListOf(ContainerDocument[].class), Mockito.anyString())).thenReturn(mockedDataFileManifest);
//        when(mockedManifestFactory.createSignatureManifest(Mockito.any(DataFilesManifest.class), Mockito.any(AnnotationsManifest.class), Mockito.anyString(), Mockito.anyString())).thenReturn(mockedSignatureManifest);
    }

}
