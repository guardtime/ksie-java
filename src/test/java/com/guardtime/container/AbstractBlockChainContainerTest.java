package com.guardtime.container;

import com.guardtime.container.annotation.ContainerAnnotation;
import com.guardtime.container.datafile.ContainerDocument;
import com.guardtime.container.manifest.*;
import com.guardtime.container.packaging.BlockChainContainerPackagingFactory;
import com.guardtime.container.signature.SignatureFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.verification.VerificationMode;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

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
        when(mockedManifestFactory.createAnnotationsManifest(Mockito.anyListOf(AnnotationInfoManifest.class))).thenReturn(mockedAnnotationsManifest);
        when(mockedManifestFactory.createAnnotationManifest(Mockito.any(DataFilesManifest.class), Mockito.any(ContainerAnnotation.class))).thenReturn(annotationInfoManifest);
        when(mockedManifestFactory.createDataFilesManifest(Mockito.anyListOf(ContainerDocument[].class))).thenReturn(mockedDataFileManifest);
        when(mockedManifestFactory.createSignatureManifest(Mockito.any(DataFilesManifest.class), Mockito.any(AnnotationsManifest.class))).thenReturn(mockedSignatureManifest);
    }

}
